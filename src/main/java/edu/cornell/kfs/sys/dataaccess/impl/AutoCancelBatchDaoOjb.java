/**
 * 
 */
package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.action.ActionRequestStatus;
import org.kuali.rice.kew.api.doctype.DocumentType;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.SessionDocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.util.OjbCollectionAware;
import org.kuali.rice.krad.workflow.service.WorkflowDocumentService;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.AutoCancelBatchStep;
import edu.cornell.kfs.sys.dataaccess.AutoCancelBatchDao;
import edu.cornell.kfs.sys.dataaccess.DbmsOutput;

/**
 * @author Admin-dwf5
 *
 */
public class AutoCancelBatchDaoOjb extends PlatformAwareDaoBaseOjb implements OjbCollectionAware, AutoCancelBatchDao {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AutoCancelBatchDaoOjb.class);

    private BusinessObjectService businessObjectService;
    private DocumentService documentService; 
    private ParameterService parameterService; 
    
    private Collection<String> cancelDocumentTypes = Collections.emptySet();
    
    
    private static final String SAVED_STATUS_CODE = "S";
    private static final String DONE_STATUS_CODE = "D";
    
    /**
     * 
     * @throws Exception
     */
    public boolean cancelFYIsAndAcknowledgements() throws Exception {
    	boolean result = true;
    	Connection conn = getPersistenceBroker(true).serviceConnectionManager().getConnection();
    	CallableStatement cs = conn.prepareCall("{call AUTO_CANCEL_FYI_ACK}");
    	DbmsOutput dbmsOutput = new DbmsOutput(conn);
    	dbmsOutput.enable(1000000);
    	result = cs.execute();
    	cs.close();
    	dbmsOutput.show();
    	dbmsOutput.close();
    	conn.close();
    	
    	return result;
    }
 
    /**
     * Super User Cancel Documents
     * 
     * Use parameters to retrieve aging period and doc types to be canceled
     * Super user cancel docs
     */
    public void cancelDocuments() throws Exception {
    	
	    String stringDays = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(AutoCancelBatchStep.class, CUKFSParameterKeyConstants.DAYS_TO_AUTO_CANCEL_PARAMETER).getValue();
	    if (StringUtils.isNotBlank(stringDays)) {
	        
	        Map<String, String> cancelIds = findSavedDocumentIds(stringDays);
	        Set<String> cancelDocumentIds = cancelIds.keySet();
	        
 	        // Loop thru and cancel docs from Cancel List
        	int i = 0;
            for(String docId : cancelDocumentIds) {
            	String docTypeId = cancelIds.get(docId);
            	if (autoCancelAllowedForDocType(docTypeId)) {
            		Document document = documentService.getByDocumentHeaderId(docId.trim());
        			
                	try {
            			if (!ObjectUtils.isNull(document)) {
            				LOG.info("Document Number to cancel : " + document.getDocumentNumber());
            				i++;
            				 documentService.prepareWorkflowDocument(document);		 
            			     SpringContext.getBean(WorkflowDocumentService.class).superUserCancel(document.getDocumentHeader().getWorkflowDocument(), "AutoCancelBatchStep: Older Than "+stringDays+" Days"); 
            			     SpringContext.getBean(SessionDocumentService.class).addDocumentToUserSession(GlobalVariables.getUserSession(), document.getDocumentHeader().getWorkflowDocument());

            			}
                	} catch (WorkflowException e) {
                		LOG.error("AutoCancelBatchStep Encountered WorkflowException " + document.getDocumentNumber(),e);
                	}
            	}
	        }
			LOG.info("Total number of docs canceled : " + i);
	    }
	    else {
	        LOG.info("ERROR: DAYS_TO_CANCEL parameter is empty or missing");
	    }	    	
    		
    }
    
    /**
     * 
     * @param compareDate
     * @return
     */
    private Map<String, String> findSavedDocumentIds(String autoCancelDays) {
    	Map<String, String> ids = new HashMap<String, String>();

    	Criteria crit = new Criteria();
    	String sqlStatement = "doc_hdr_stat_cd='"+SAVED_STATUS_CODE+"' and (trunc(crte_dt) +"+autoCancelDays+")<=trunc(sysdate)";
		LOG.info("SQL Statement where clause : " + sqlStatement);
    	crit.addSql(sqlStatement);
    	ReportQueryByCriteria qbc = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, crit);
    	qbc.setAttributes(new String[] {"doc_hdr_id", "doc_typ_id"});
    	Iterator<Object[]> results = this.getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(qbc);
    	
    	while(results.hasNext()) {
    		Object[] retrievedIds = results.next();
    		String docId = retrievedIds[0].toString();
    		String docTypId = retrievedIds[1].toString();
    		LOG.info("Doc ID : " + docId + " Doc Typ ID : " + docTypId);
    		ids.put(docId, docTypId);
    	}
    	
    	return ids;
    }
    
    /**
     * 
     * @param actionRequestedCd
     * @return
     */
    public List<ActionRequestValue> findPendingByActionRequested(String actionRequestedCd) {

        int age = (Integer.parseInt(parameterService.getParameterValueAsString(AutoCancelBatchStep.class, CUKFSParameterKeyConstants.DAYS_TO_AUTO_CANCEL_PARAMETER)));
        
        Calendar agedDate = Calendar.getInstance();
        agedDate.add(Calendar.DATE, (age*-1));
    	
    	Criteria crit = new Criteria();
        crit.addEqualTo("actionRequested", actionRequestedCd);
        crit.addLessOrEqualThan("createDate", new java.sql.Date(agedDate.getTimeInMillis()));
        crit.addNotEqualTo("status", DONE_STATUS_CODE);
        crit.addEqualTo("currentIndicator", Boolean.TRUE);
        crit.addAndCriteria(getPendingCriteria());
        QueryByCriteria query = new QueryByCriteria(ActionRequestValue.class, crit);
        Iterator<ActionRequestValue> iter = (Iterator<ActionRequestValue>) this.getPersistenceBrokerTemplate().getIteratorByQuery(query);

        ArrayList<ActionRequestValue> results = new ArrayList<ActionRequestValue>();
        int i=0;
        while(iter.hasNext()) {
        	ActionRequestValue arv = iter.next();
        	LOG.info("Adding item "+ ++i, null);
        	results.add(arv);
        }
        
        return results;
    }
    
    /**
     * 
     * @return
     */
    private Criteria getPendingCriteria() {
        Criteria pendingCriteria = new Criteria();
        Criteria activatedCriteria = new Criteria();
        activatedCriteria.addEqualTo("status", ActionRequestStatus.ACTIVATED);
        Criteria initializedCriteria = new Criteria();
        initializedCriteria.addEqualTo("status", ActionRequestStatus.INITIALIZED);
        pendingCriteria.addOrCriteria(activatedCriteria);
        pendingCriteria.addOrCriteria(initializedCriteria);
        return pendingCriteria;
    }

    /**
     * 
     * @param docType
     * @return
     */
    private boolean autoCancelAllowedForDocType(String docTypeId) {
    	Map<String, DocumentType> docTypes = new HashMap<String, DocumentType>();
    	DocumentType docType = docTypes.get(docTypeId);
    	
    	
    	if(ObjectUtils.isNull(docType)) {
    		docType = KewApiServiceLocator.getDocumentTypeService().getDocumentTypeById(docTypeId);
    		docTypes.put(docTypeId, docType);
    	}
    	
    	
    	if(cancelDocumentTypes.isEmpty()) {
    		cancelDocumentTypes = (Collection<String>) parameterService.getParameterValuesAsString(AutoCancelBatchStep.class, CUKFSParameterKeyConstants.AUTO_CANCEL_DOC_TYPES_PARAMETER);
    	}
    	return cancelDocumentTypes.contains(docType.getName());
    }
    
	/**
     * Get ParameterService
     * 
     * @return ParameterService
     */
    public ParameterService getParameterService() {
        return parameterService;
    }

    /**
     * Set ParameterService
     * 
     * @param parameterService
     */
    public void setParameterService(ParameterService parameterSerivce) {
		this.parameterService = parameterSerivce;
	}

	/**
     * Get BusinessObjectService
     * 
     * @return BusinessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Set BusinessObjectService
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Get DocumentService
     * 
     * @return
     */
    public DocumentService getDocumentService() {
        return documentService;
    }

    /**
     * Set DocumentService
     * 
     * @param documentService
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }	
    
}
