package edu.cornell.kfs.sys.batch.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.UserSessionUtils;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.exception.UnknownDocumentTypeException;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.batch.AutoDisapproveDocumentsStep;
import org.kuali.kfs.sys.batch.service.impl.AutoDisapproveDocumentsServiceImpl;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CuAutoDisapproveDocumentsServiceImpl extends AutoDisapproveDocumentsServiceImpl {
	private static final Logger LOG = LogManager.getLogger();
    private static final String TAB = "\t";

    private RouteHeaderService routeHeaderService;

    @Override
    protected boolean processAutoDisapproveDocuments(String principalId, String annotation) {
        Collection<DocumentHeader> documentList = getDocumentsToDisapprove();
        LOG.info("Total documents to process {}", documentList::size);
        String documentHeaderId = null;

        // CU Customization: Filter out documents whose workflow statuses are not actually ENROUTE (see referenced method for details).
        documentList = getDocumentsWithActualWorkflowStatus(documentList, DocumentStatus.ENROUTE);

        for (DocumentHeader result : documentList) {
            documentHeaderId = result.getDocumentNumber();
            Document document = findDocumentForAutoDisapproval(documentHeaderId);

           if (document != null) {
               if (checkIfDocumentEligibleForAutoDispproval(document.getDocumentHeader(), null /* FIX THIS! */)) {
                   try {
                       String successMessage = buildSuccessMessage(document);
                       autoDisapprovalYearEndDocument(document, annotation);
                       sendAcknowledgement(document.getDocumentHeader(), annotation);
                       LOG.info(
                               "The document with header id: {} is automatically disapproved by this job.",
                               documentHeaderId
                       );
                       getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(successMessage);
                   } catch (Exception e) {
                       LOG.error("Exception encountered trying to auto disapprove the document {}", e::getMessage);
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
    	headerBuilder.append(getPersonService().getPerson(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId()).getPrincipalName());
    	headerBuilder.append(TAB);
    	headerBuilder.append(getPersonService().getPerson(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId()).getName());
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
		actions = KEWServiceLocator.getActionTakenService().findByDocumentIdIgnoreCurrentInd(document.getDocumentNumber());
		ActionTaken max = null;
		for (ActionTaken at : actions) {

			if (ObjectUtils.isNull(max)) {
				max = at;
			} else if (at.getActionDate().compareTo(max.getActionDate()) > 0) {
				max = at;
			}

		}

		headerBuilder.append(getPersonService().getPerson(max.getPrincipalId())
				.getPrincipalName());
		headerBuilder.append(TAB);
		headerBuilder.append(getPersonService().getPerson(max.getPrincipalId()).getName());
		headerBuilder.append(TAB);
		headerBuilder.append(max.getActionDate());
		headerBuilder.append(TAB);
		headerBuilder.append(max.getActionTakenLabel());
		headerBuilder.append(TAB);
    	
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
			DocumentType parentDocumentType = (DocumentType) documentTypeService.getDocumentTypeByName(documentTypeCode);

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
            if (ObjectUtils.isNull(documentTypeService.getDocumentTypeByName(parentDocumentType.getName()))) {
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
        	if (ObjectUtils.isNull(documentTypeService.getDocumentTypeByName(dT))) {        		
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
        Person systemUser = getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER);      
        
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
     * @see org.kuali.kfs.sys.batch.service.impl.AutoDisapproveDocumentsServiceImpl#checkIfDocumentEligibleForAutoDispproval(org.kuali.kfs.sys.businessobject.DocumentHeader)
     */
    @Override
    protected boolean checkIfDocumentEligibleForAutoDispproval(
            final DocumentHeader documentHeader,
            final List<String> eligibleDocumentTypeIds
    ) {
        boolean documentEligible = false;
        
        List<DocumentType> parentDocumentTypes = this.getYearEndAutoDisapproveParentDocumentTypes();
        
        final WorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();

        // Documents that have been recalled may still have an enroute doc header status (due to a bug we need to
        // fix), so we need to double-check the workflow doc here since only ENROUTE docs can be DISAPPROVED.
        if (!workflowDocument.isEnroute()) {
            return false;
        }
        
        String documentTypeName = workflowDocument.getDocumentTypeName();
        DocumentType childDocumentType = documentTypeService.getDocumentTypeByName(documentTypeName);
        
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
     * There is a known issue where the KFS document headers are not auto-saved when a KFS
     * document gets recalled to the action list. The auto-saves are not being performed to
     * explicitly avoid Optimistic Locking problems for end users in the PostProcessorServiceImpl
     * code on documents that are being moved into SAVED status.
     * 
     * Therefore, to prevent problems with auto-disapprovals accidentally targeting recalled
     * documents (due to them being retrieved based on KFS document header status), this method
     * filters out any documents whose KEW doc statuses do not match the expected one.
     * 
     * @param documentList The document headers to filter; cannot be null.
     * @param workflowStatus The workflow status that the documents are expected to have; cannot be null.
     * @return A new collection containing only the KFS doc headers whose matching route headers actually have the given workflow status.
     */
    protected Collection<DocumentHeader> getDocumentsWithActualWorkflowStatus(
            Collection<DocumentHeader> documentList, DocumentStatus status) {
        final int SCALED_SET_SIZE = (int) (documentList.size() * 1.4);
        Set<String> documentIds = new HashSet<String>(SCALED_SET_SIZE);
        Collection<DocumentHeader> finalList = new ArrayList<DocumentHeader>(documentList.size());
        
        // Assemble document IDs, then search for workflow headers.
        for (DocumentHeader docHeader : documentList) {
        	DocumentRouteHeaderValue routeHeader = routeHeaderService.getRouteHeader(docHeader.getDocumentNumber());
        	if (status.equals(routeHeader.getStatus())) {
                documentIds.add(routeHeader.getDocumentId());
        	}
        }
        
        // Update final-headers collection with any doc headers that actually have the given workflow status in KEW.
        for (DocumentHeader docHeader : documentList) {
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
        } catch (UnknownDocumentTypeException ex) {
            // don't blow up just because a document type is not installed (but don't return it either)
            LOG.error("Exception encountered on finding the document: " + documentHeaderId, ex);
        }

        return document;
    }

    public void setRouteHeaderService(RouteHeaderService routeHeaderService) {
        this.routeHeaderService = routeHeaderService;
    } 

}
