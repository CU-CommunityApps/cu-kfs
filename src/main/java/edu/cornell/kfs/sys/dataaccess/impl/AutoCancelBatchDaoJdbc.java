package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SessionDocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.AutoCancelBatchStep;
import edu.cornell.kfs.sys.dataaccess.AutoCancelBatchDao;
import edu.cornell.kfs.sys.dataaccess.DbmsOutput;

public class AutoCancelBatchDaoJdbc extends PlatformAwareDaoBaseJdbc implements AutoCancelBatchDao {

	private static final Logger LOG = LogManager.getLogger(AutoCancelBatchDaoJdbc.class);

    private BusinessObjectService businessObjectService;
    private DocumentService documentService;
    private ParameterService parameterService;
    private SessionDocumentService sessionDocumentService;
    private WorkflowDocumentService workflowDocumentService;

    private Collection<String> cancelDocumentTypes = Collections.emptySet();
    private Map<String, DocumentType> docTypes = new HashMap<>();

    /**
     * @see AutoCancelBatchDao#cancelFYIsAndAcknowledgements()
     */
    @Override
    public boolean cancelFYIsAndAcknowledgements() {
        Connection conn = null;
        CallableStatement cs = null;
        DbmsOutput dbmsOutput = null;
        boolean result;

        try {
            conn = getJdbcTemplate().getDataSource().getConnection();
            cs = conn.prepareCall("{call KFS.AUTO_CANCEL_FYI_ACK}");
            dbmsOutput = new DbmsOutput(conn);
            dbmsOutput.enable(1000000);
            result = cs.execute();
            dbmsOutput.show();
        } catch (SQLException e) {
            throw new RuntimeException("Issue encountered with cancelFYIsAndAcknowledgements", e);
        } finally {
            if (dbmsOutput != null) {
                dbmsOutput.close();
            }

            if (cs != null) {
                try {
                    cs.close();
                } catch (SQLException e) {
                    LOG.error("cancelFYIsAndAcknowledgements: Could not close CallableStatement");
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOG.error("cancelFYIsAndAcknowledgements: Could not close Connection");
                }
            }
        }

        return result;
    }

    /**
     * @see AutoCancelBatchDao#cancelDocuments()
     */
    @Override
    public void cancelDocuments() throws Exception {
        final String daysToAutoCancel = parameterService.getParameterValueAsString(AutoCancelBatchStep.class, CUKFSParameterKeyConstants.DAYS_TO_AUTO_CANCEL_PARAMETER);

        if (StringUtils.isNotBlank(daysToAutoCancel)) {
            Map<String, String> cancelIds = findSavedDocumentIds(Integer.parseInt(daysToAutoCancel));
            Set<String> cancelDocumentIds = cancelIds.keySet();

            int canceledDocumentCount = 0;
            for (String docId : cancelDocumentIds) {
                String docTypeId = cancelIds.get(docId);
                if (canAutoCancelDocType(docTypeId)) {
                    LOG.info("Retrieving document : " + docId.trim());
                    Document document = documentService.getByDocumentHeaderId(docId.trim());

                    if (!ObjectUtils.isNull(document)) {
                        LOG.info("Document Number to cancel : " + document.getDocumentNumber());
                        canceledDocumentCount++;
                        documentService.prepareWorkflowDocument(document);
                        workflowDocumentService.superUserCancel(document.getDocumentHeader().getWorkflowDocument(), "AutoCancelBatchStep: Older Than " + daysToAutoCancel + " Days");
                        sessionDocumentService.addDocumentToUserSession(GlobalVariables.getUserSession(), document.getDocumentHeader().getWorkflowDocument());
                    }
                }
            }
            LOG.info("Total number of docs canceled : " + canceledDocumentCount);
        } else {
            LOG.info("ERROR: DAYS_TO_CANCEL parameter is empty or missing");
        }

    }

    private Map<String, String> findSavedDocumentIds(int daysToAutoCancel) {
        Map<String, String> ids = new HashMap<String, String>();

        StringBuilder sql = new StringBuilder();
        sql.append("select DOC_HDR_ID, DOC_TYP_ID from KFS.KREW_DOC_HDR_T where ");
        sql.append("DOC_HDR_STAT_CD = '" + DocumentStatus.SAVED.getCode() + "' and (trunc(CRTE_DT) + " + daysToAutoCancel + ") <= trunc(SYSDATE)");

        LOG.info("SQL Statement: " + sql);
        List<Map<String, Object>> results = getJdbcTemplate().queryForList(sql.toString());

        for (Map<String, Object> result : results) {
            String docId = (String) result.get("DOC_HDR_ID");
            String docTypId = (String) result.get("DOC_TYP_ID");
            LOG.info("Doc ID : " + docId + " Doc Typ ID : " + docTypId);
            ids.put(docId, docTypId);
        }

        return ids;
    }

    private boolean canAutoCancelDocType(String docTypeId) {
        DocumentType docType = docTypes.get(docTypeId);

        if (ObjectUtils.isNull(docType)) {
            docType = KEWServiceLocator.getDocumentTypeService().getDocumentTypeById(docTypeId);
            docTypes.put(docTypeId, docType);
        }

        if (cancelDocumentTypes.isEmpty()) {
            cancelDocumentTypes = parameterService.getParameterValuesAsString(AutoCancelBatchStep.class, CUKFSParameterKeyConstants.AUTO_CANCEL_DOC_TYPES_PARAMETER);
        }

        return cancelDocumentTypes.contains(docType.getName());
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterSerivce) {
        this.parameterService = parameterSerivce;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public SessionDocumentService getSessionDocumentService() {
        return sessionDocumentService;
    }

    public void setSessionDocumentService(SessionDocumentService sessionDocumentService) {
        this.sessionDocumentService = sessionDocumentService;
    }

    public WorkflowDocumentService getWorkflowDocumentService() {
        return workflowDocumentService;
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

}
