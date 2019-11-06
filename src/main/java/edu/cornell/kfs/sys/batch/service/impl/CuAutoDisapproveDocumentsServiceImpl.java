package edu.cornell.kfs.sys.batch.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.joda.time.DateTime;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.batch.AutoDisapproveDocumentsStep;
import org.kuali.kfs.sys.batch.service.impl.AutoDisapproveDocumentsServiceImpl;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.doctype.DocumentType;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.datadictionary.exception.UnknownDocumentTypeException;
import org.kuali.kfs.krad.UserSessionUtils;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

@Transactional
public class CuAutoDisapproveDocumentsServiceImpl extends AutoDisapproveDocumentsServiceImpl {
	private static final Logger LOG = LogManager.getLogger(CuAutoDisapproveDocumentsServiceImpl.class);
    private static final String TAB = "\t";

    private RouteHeaderService routeHeaderService;
    private PersonService personService;

    @Override
    protected boolean processAutoDisapproveDocuments(String principalId, String annotation) {
        Collection<FinancialSystemDocumentHeader> documentList = getDocumentsToDisapprove();
        LOG.info("Total documents to process " + documentList.size());
        String documentHeaderId = null;

        // CU Customization: Filter out documents whose workflow statuses are not actually ENROUTE (see referenced method for details).
        documentList = getDocumentsWithActualWorkflowStatus(documentList, DocumentStatus.ENROUTE);

        for (FinancialSystemDocumentHeader result : documentList) {
            documentHeaderId = result.getDocumentNumber();
            Document document = findDocumentForAutoDisapproval(documentHeaderId);

           if (document != null) {
               if (checkIfDocumentEligibleForAutoDispproval(document.getDocumentHeader())) {
                   try {
                       String successMessage = buildSuccessMessage(document);
                       autoDisapprovalYearEndDocument(document, annotation);
                       sendAcknowledgement(document.getDocumentHeader(), annotation);
                       LOG.info("The document with header id: " + documentHeaderId + " is automatically disapproved by this job.");
                       getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(successMessage);
                   } catch (Exception e) {
                       LOG.error("Exception encountered trying to auto disapprove the document " + e.getMessage());
                       String message = "Exception encountered trying to auto disapprove the document: ".concat(documentHeaderId);
                       getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);
                   }
               }
           } else {
               LOG.error("Document is NULL.  It should never have been null");
               String message = ("Error: Document with id: ").concat(documentHeaderId).concat(" - Document is NULL.  It should never have been null");
               getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);
           }
        }

        return true;
    }

    protected boolean checkIfRunDateParameterExists() {
        // check to make sure the system parameter for run date check has already been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE System parameter does not exist in the " +
                    "parameters list.  The job can not continue without this parameter");
            getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENTS_RUN_DATE System parameter does not exist in the parameters " +
                            "list.  The job can not continue without this parameter");
            return false;
        }
	    try {
	        Date runDate = getDateTimeService().convertToDate(
	                getParameterService().getParameterValueAsString(AutoDisapproveDocumentsStep.class,
	                        KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE));
	    }
	    catch (ParseException pe) {
	        LOG.warn("ParseException: System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE can not be determined.");
	        String message = "ParseException: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE " +
	                "is invalid.  The auto disapproval job will not use this value.";
	        getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);                         
	        return false;
	    }
     
        return true;
    }

    private String buildSuccessMessage(Document document) throws Exception{

    	StringBuilder headerBuilder = new StringBuilder();
    	headerBuilder.append(document.getDocumentNumber());
    	headerBuilder.append(TAB);
    	headerBuilder.append(document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
    	headerBuilder.append(TAB);
    	headerBuilder.append(document.getDocumentHeader().getDocumentDescription());
    	headerBuilder.append(TAB);
    	headerBuilder.append(personService.getPerson(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId()).getPrincipalName());
    	headerBuilder.append(TAB);
    	headerBuilder.append(personService.getPerson(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId()).getName());
    	headerBuilder.append(TAB);
    	headerBuilder.append(document.getDocumentHeader().getWorkflowDocument().getDateCreated());
    	headerBuilder.append(TAB);
    
    	
    	ConfigurationService k = KRADServiceLocator.getKualiConfigurationService();
    	StringBuilder routeBuilder = new StringBuilder();
    	String url = k.getPropertyValueAsString("workflow.url");
    	routeBuilder.append(url);
    	routeBuilder.append("/RouteLog.do?routeHeaderId=");
    	routeBuilder.append(document.getDocumentNumber());
    	routeBuilder.append(TAB);
    	
    	headerBuilder.append(routeBuilder);
    	
    	List<ActionTaken> actions;
//    	try {
//    		actions = KEWServiceLocator.getActionTakenService().findByDocumentId(document.getDocumentHeader().getWorkflowDocument().getDocumentId());
		actions = KewApiServiceLocator.getWorkflowDocumentService().getAllActionsTaken(document.getDocumentNumber());
		ActionTaken max = null;
		for (ActionTaken at : actions) {

			if (ObjectUtils.isNull(max)) {
				max = at;
			} else if (at.getActionDate().compareTo(max.getActionDate()) > 0) {
				max = at;
			}

		}

		headerBuilder.append(personService.getPerson(max.getPrincipalId())
				.getPrincipalName());
		headerBuilder.append(TAB);
		headerBuilder.append(personService.getPerson(max.getPrincipalId()).getName());
		headerBuilder.append(TAB);
		headerBuilder.append(max.getActionDate());
		headerBuilder.append(TAB);
		headerBuilder.append(max.getActionTaken().getLabel());
		headerBuilder.append(TAB);

//    	} catch (WorkflowException e) {
//    		e.printStackTrace();
//    	}
    	
    	String headerString = headerBuilder.toString(); 
    	StringBuilder builder = new StringBuilder();

    	if (document instanceof AccountingDocumentBase) {
    		for (Object o : ((AccountingDocumentBase) document).getSourceAccountingLines()) {
    			SourceAccountingLine sal = (SourceAccountingLine)o;
    			builder.append(headerString);
    			builder.append(sal.getChartOfAccountsCode());
    			builder.append(TAB);
    			builder.append(sal.getAccountNumber());
    			builder.append(TAB);
    			builder.append(sal.getAmount());
    			builder.append(TAB);
    			builder.append(sal.getAccount().getOrganizationCode());
    			builder.append(TAB);
    			builder.append(KFSConstants.NEWLINE);
    		}
    		for (Object o : ((AccountingDocumentBase) document).getTargetAccountingLines()) {
    			TargetAccountingLine tal = (TargetAccountingLine)o;
    			builder.append(headerString);
    			builder.append(tal.getChartOfAccountsCode());
    			builder.append(TAB);
    			builder.append(tal.getAccountNumber());
    			builder.append(TAB);
    			builder.append(tal.getAmount());
    			builder.append(TAB);
    			builder.append(tal.getAccount().getOrganizationCode());
    			builder.append(TAB);
    			builder.append(KFSConstants.NEWLINE);
    		}
    	}
    	
    	return builder.toString();
    }

    /**
     * Gets the year end auto disapprove parent document types. 
     * @return Returns the parentDocumentTypes.
     */
    
    protected List<DocumentType> getYearEndAutoDisapproveParentDocumentTypes() {
		List<DocumentType> parentDocumentTypes = new ArrayList<DocumentType>();

		Collection<String> documentTypeCodes = getParameterService().getParameterValuesAsString(
			AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE);

		for (String documentTypeCode : documentTypeCodes) {
			DocumentType parentDocumentType = (DocumentType) getDocumentTypeService().getDocumentTypeByName(documentTypeCode);

			if (ObjectUtils.isNotNull(parentDocumentType)) {
				parentDocumentTypes.add(parentDocumentType);
			}
		}

		return parentDocumentTypes;
    }

    protected boolean checkIfParentDocumentTypeParameterExists() {
        // check to make sure the system parameter for Parent Document Type = FP has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE System parameter does not exist in the " +
                    "parameters list.  The job can not continue without this parameter");
            getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(
                    "YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE System parameter does not exist in the " +
                            "parameters list.  The job can not continue without this parameter");
            return false;
        }
        List<DocumentType> parentDocumentTypes = this.getYearEndAutoDisapproveParentDocumentTypes();
        
        for (DocumentType parentDocumentType : parentDocumentTypes) {   
            if (ObjectUtils.isNull(getDocumentTypeService().getDocumentTypeByName(parentDocumentType.getName()))) {
            	LOG.warn("Invalid Document Type: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE " +
            	        "is invalid. The auto disapproval job cannot use this value.");
            	getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(
            	        "Invalid Document Type: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE " +
            	                "is invalid. The auto disapproval job cannot use this value.");        	                			
            }           
        }
    
        return true;
    }

    protected boolean checkIfDocumentCompareCreateDateParameterExists() {
        // check to make sure the system parameter for create date to compare has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE System parameter does not exist in the " +
                    "parameters list.  The job can not continue without this parameter");
            getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE System parameter does not exist in the " +
                            "parameters list.  The job can not continue without this parameter");
            return false;
        }
	    try {
	        Date compareDate = getDateTimeService().convertToDate(
	                getParameterService().getParameterValueAsString(AutoDisapproveDocumentsStep.class,
	                        KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE));
	    }
	    catch (ParseException pe) {
	        LOG.warn("ParseException: System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE can not be determined.");
	        String message = ("ParseException: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREAT_DATE " +
	                "is invalid.  The auto disapproval job will not use this value.");
	        getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);                         
	        return false;
	    }
	   	
	   	return true;
    }
 
    protected boolean checkIfDocumentTypesExceptionParameterExists() {
        // check to make sure the system parameter for Document Types that are exceptions has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter does not exist in the parameters " +
                    "list.  The job can not continue without this parameter");
            getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter does not exist in the parameters list. " +
            "The job can not continue without this parameter");
            return false;
        }
        
        boolean parameterExists = true;
        Collection<String> documentTypes = getParameterService().getParameterValuesAsString(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES);
        for (String dT : documentTypes) {
        	if (ObjectUtils.isNull(getDocumentTypeService().getDocumentTypeByName(dT))) {        		
                LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter contains invalid value: \"" + dT +
                        "\" The job can not continue with invalid values in this parameter.");
                getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(
                        "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter contains invalid value: \"" + dT +
                                "\" The job can not continue with invalid values in this parameter.");
                parameterExists = false;
        	}
        }
        return parameterExists;
    }

    protected void autoDisapprovalYearEndDocument(Document document,
            String annotationForAutoDisapprovalDocument)  throws Exception {
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);      
        
        Note approveNote = getNoteService().createNote(new Note(), document.getDocumentHeader(), systemUser.getPrincipalId());
        approveNote.setNoteText(annotationForAutoDisapprovalDocument);

        approveNote.setAuthorUniversalIdentifier(systemUser.getPrincipalId());
        
        approveNote.setNotePostedTimestampToCurrent();
        
        getNoteService().save(approveNote);
        
        document.addNote(approveNote);
        
        getDocumentService().prepareWorkflowDocument(document);
        getDocumentService().superUserDisapproveDocumentWithoutSaving(document,
                "Disapproval of Outstanding Documents - Year End Cancellation Process");
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(), document.getDocumentHeader().getWorkflowDocument());
   }

    /**
     * @see org.kuali.kfs.sys.batch.service.impl.AutoDisapproveDocumentsServiceImpl#checkIfDocumentEligibleForAutoDispproval(org.kuali.rice.krad.bo.DocumentHeader)
     */
    @Override
    protected boolean checkIfDocumentEligibleForAutoDispproval(DocumentHeader documentHeader) {
        boolean documentEligible = false;
        
        List<DocumentType> parentDocumentTypes = this.getYearEndAutoDisapproveParentDocumentTypes();
        String documentTypeName = documentHeader.getWorkflowDocument().getDocumentTypeName();
        DocumentType childDocumentType = getDocumentTypeService().getDocumentTypeByName(documentTypeName);
        
        for (DocumentType parentDocumentType : parentDocumentTypes) {
            documentEligible = childDocumentType.getParentId().equals(parentDocumentType.getId());
            
            if (documentEligible) {
                break;
            }
            
        }
        
        return documentEligible;
        
    }

    /**
     * CU Customization:
     * 
     * There is a known Rice issue where the KFS document headers are not auto-saved when a KFS
     * document gets recalled to the action list. This is due to Rice explicitly avoiding any
     * auto-saves on documents moving to SAVED status, to prevent Optimistic Locking problems
     * for end-users as per the Rice PostProcessorServiceImpl code comments.
     * 
     * Therefore, to prevent problems with auto-disapprovals accidentally targeting recalled
     * documents (due to them being retrieved based on KFS document header status), this method
     * filters out any documents whose KEW doc statuses do not match the expected one.
     * 
     * @param documentList The document headers to filter; cannot be null.
     * @param workflowStatus The workflow status that the documents are expected to have; cannot be null.
     * @return A new collection containing only the KFS doc headers whose matching route headers actually have the given workflow status.
     */
    protected Collection<FinancialSystemDocumentHeader> getDocumentsWithActualWorkflowStatus(
            Collection<FinancialSystemDocumentHeader> documentList, DocumentStatus status) {
        final int SCALED_SET_SIZE = (int) (documentList.size() * 1.4);
        Set<String> documentIds = new HashSet<String>(SCALED_SET_SIZE);
        Collection<DocumentRouteHeaderValue> routeHeaders;
        Collection<FinancialSystemDocumentHeader> finalList = new ArrayList<FinancialSystemDocumentHeader>(documentList.size());
        
        // Assemble document IDs, then search for workflow headers.
        for (FinancialSystemDocumentHeader docHeader : documentList) {
            documentIds.add(docHeader.getDocumentNumber());
        }
        routeHeaders = routeHeaderService.getRouteHeaders(documentIds);
        
        // Track which headers have the expected document status.
        documentIds = new HashSet<String>(SCALED_SET_SIZE);
        if (routeHeaders != null) {
            for (DocumentRouteHeaderValue routeHeader : routeHeaders) {
                if (status.equals(routeHeader.getStatus())) {
                    documentIds.add(routeHeader.getDocumentId());
                }
            }
        }
        
        // Update final-headers collection with any doc headers that actually have the given workflow status in KEW.
        for (FinancialSystemDocumentHeader docHeader : documentList) {
            if (documentIds.contains(docHeader.getDocumentNumber())) {
                finalList.add(docHeader);
            }
        }
        
        return finalList;
    }
    
    /**
     * This method finds the document for the given document header id
     *
     * @param documentHeaderId
     * @return document The document in the workflow that matches the document header id.
     */
    protected Document findDocumentForAutoDisapproval(String documentHeaderId) {
        Document document = null;

        try {
            document = getDocumentService().getByDocumentHeaderId(documentHeaderId);
        } catch (WorkflowException ex) {
            LOG.error("Exception encountered on finding the document: " + documentHeaderId, ex);
        } catch (UnknownDocumentTypeException ex) {
            // don't blow up just because a document type is not installed (but don't return it either)
            LOG.error("Exception encountered on finding the document: " + documentHeaderId, ex);
        }

        return document;
    }

    public void setRouteHeaderService(RouteHeaderService routeHeaderService) {
        this.routeHeaderService = routeHeaderService;
    } 

    public void setPersonService(PersonService personService) {
        super.setPersonService(personService);
        this.personService = personService;       
    }


}
