package edu.cornell.kfs.sys.batch.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.batch.AutoDisapproveDocumentsStep;
import org.kuali.kfs.sys.batch.service.impl.AutoDisapproveDocumentsServiceImpl;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.doctype.DocumentType;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.UserSessionUtils;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

@Transactional
public class CuAutoDisapproveDocumentsServiceImpl extends AutoDisapproveDocumentsServiceImpl {

    private static final String TAB = "\t";
	

    protected boolean processAutoDisapproveDocuments(String principalId, String annotation, Date documentCompareDate) {
        boolean success = true;
        
        DocumentSearchCriteria.Builder criteria = DocumentSearchCriteria.Builder.create();
        criteria.setDocumentStatuses(Collections.singletonList(DocumentStatus.ENROUTE));
        criteria.setSaveName(null);
        List<DocumentType> parentDocumentTypes = this.getYearEndAutoDisapproveParentDocumentTypes();
        
		for (DocumentType parentDocumentType : parentDocumentTypes) {
			// If optional document parameters have been defined, use these in
			// the criteria, if not, use the default values
			setOptionalDocumentSearchCriteria(criteria, parentDocumentType.getName());

			try {
				// If optional document parameters have been defined, use these
				DocumentSearchResults results = KewApiServiceLocator.getWorkflowDocumentService().documentSearch(principalId, criteria.build());

				String documentHeaderId = null;

				for (DocumentSearchResult result : results.getSearchResults()) {
					documentHeaderId = result.getDocument().getDocumentId();
					Document document = findDocumentForAutoDisapproval(documentHeaderId);
					if (document != null) {
						if (checkIfDocumentEligibleForAutoDisapproval(document, parentDocumentTypes)) {
							if (!exceptionsToAutoDisapproveProcess(document, documentCompareDate)) {
								try {
	                            	String successMessage = buildSuccessMessage(document);
									autoDisapprovalYearEndDocument(document, annotation);
									LOG.info("The document with header id: " + documentHeaderId + " is automatically disapproved by this job.");
									getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(successMessage);
								} catch (Exception e) {
									LOG.error("Exception encountered trying to auto disapprove the document " + e.getMessage());
	                                String message = ("Exception encountered trying to auto disapprove the document: ").concat(documentHeaderId);                                    
									getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);
								}
							} else {
								LOG.info("Year End Auto Disapproval Exceptions:  The document: " + documentHeaderId + " is NOT AUTO DISAPPROVED.");
							}
						}
					} else {
						LOG.error("Document is NULL.  It should never have been null");
						String message = ("Error: Document with id: ").concat(documentHeaderId).concat(" - Document is NULL.  It should never have been null");
						getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);
					}
				}
			} catch (WorkflowRuntimeException wfre) {
				success = false;
				LOG.warn("Error with workflow search for documents for auto disapproval");
				String message = ("Error with workflow search for documents for auto disapproval.  The auto disapproval job is stopped.");
				getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);
			}
		}
        return success;
    }

    protected void setOptionalDocumentSearchCriteria(DocumentSearchCriteria.Builder documentSearchCriteriaDTO, String docType) {
        try {
			if (checkIfParentDocumentTypeParameterExists()) {
				LOG.info("\"Root Document Parameter\" set to: " + docType);
				documentSearchCriteriaDTO.setDocumentTypeName(docType);
			} else {
				documentSearchCriteriaDTO.setDocumentTypeName(KFSConstants.ROOT_DOCUMENT_TYPE);
			}
			if (checkIfStartDateParameterExists()) {
				String startDateCreated = getParameterService().getParameterValueAsString(AutoDisapproveDocumentsStep.class,
								CUKFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_START_DATE);
				LOG.info("\"Start Date Parameter\" set to: " + startDateCreated);
				documentSearchCriteriaDTO.setDateCreatedFrom(new DateTime(getDateTimeService().convertToDateTime(startDateCreated)));
			}
			if (checkIfDocumentCompareCreateDateParameterExists()) {
				String toDateCreated = getParameterService().getParameterValueAsString(AutoDisapproveDocumentsStep.class,
								KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE);
				LOG.info("\"To Date Parameter\" set to: " + toDateCreated);
				documentSearchCriteriaDTO.setDateCreatedTo(new DateTime(getDateTimeService().convertToDateTime(toDateCreated)));
			}
		} catch (ParseException e) {
			LOG.info("exception " + e.getMessage());
		}

    }

    protected boolean checkIfStartDateParameterExists() {
	   	boolean parameterExists = true;
	   	
	   	// check to see if a more specific document type has been specified for the search parameter as opposed to "KFS" which is the root document type
	   	if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class, CUKFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_START_DATE)) {
	   		LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_START_DATE System parameter does not exist in the parameters list. Although the job can continue without this parameter, it will be less efficient");
	   		getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine("YEAR_END_AUTO_DISAPPROVE_START_DATE System parameter does not exist in the parameters list.  The job will continue without this parameter");
	        return false;
	   	}
	    try {
	        getDateTimeService().convertToDate(getParameterService().getParameterValueAsString(AutoDisapproveDocumentsStep.class, CUKFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_START_DATE));
	    }
	    catch (ParseException pe) {
	        LOG.warn("ParseException: System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_START_DATE can not be determined.");
	        String message = ("ParseException: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_START_DATE is invalid.  The auto disapproval job will not use this value.");
	        getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);                         
	        return false;
	    }
	   	
	   	return parameterExists;
   }

    protected boolean checkIfDocumentEligibleForAutoDisapproval(Document document, List<DocumentType> parentDocumentTypes) {
    	// foundation is named as checkIfDocumentEligibleForAutoDispproval. ie, no 'a' between 'Dis' and 'pproval'.
    	// Following is not efficient because it is retrieving for every document ? so pass as argument
		//List<DocumentType> parentDocumentTypes = this.getYearEndAutoDisapproveParentDocumentTypes();

		String documentTypeName = document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName();
		DocumentType childDocumentType = (DocumentType) getDocumentTypeService().getDocumentTypeByName(documentTypeName);

		for (DocumentType parentDocumentType : parentDocumentTypes) {
			// TODO :this is a discouraged access,  other alternative to check this ?
			if (org.kuali.rice.kew.doctype.bo.DocumentType.from(parentDocumentType).isParentOf(org.kuali.rice.kew.doctype.bo.DocumentType.from(childDocumentType))) {
				return true;
			}
		}

		return false;
    }

    protected boolean checkIfRunDateParameterExists() {
        boolean parameterExists = true;
        
        // check to make sure the system parameter for run date check has already been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
            getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine("YEAR_END_AUTO_DISAPPROVE_DOCUMENTS_RUN_DATE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
            return false;
        }
	    try {
	        Date runDate = getDateTimeService().convertToDate(getParameterService().getParameterValueAsString(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE));
	    }
	    catch (ParseException pe) {
	        LOG.warn("ParseException: System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE can not be determined.");
	        String message = ("ParseException: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE is invalid.  The auto disapproval job will not use this value.");
	        getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);                         
	        return false;
	    }
     
        return parameterExists;
    }

    private String buildSuccessMessage(Document document) throws Exception{

    	PersonService ps = SpringContext.getBean(PersonService.class);//.getPerson(max.getPrincipalId()).getPrincipalName()
    	
    	StringBuilder headerBuilder = new StringBuilder();
    	headerBuilder.append(document.getDocumentNumber());
    	headerBuilder.append(TAB);
    	headerBuilder.append(document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
    	headerBuilder.append(TAB);
    	headerBuilder.append(document.getDocumentHeader().getDocumentDescription());
    	headerBuilder.append(TAB);
    	headerBuilder.append(ps.getPerson(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId()).getPrincipalName());
    	headerBuilder.append(TAB);
    	headerBuilder.append(ps.getPerson(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId()).getName());
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

		headerBuilder.append(ps.getPerson(max.getPrincipalId())
				.getPrincipalName());
		headerBuilder.append(TAB);
		headerBuilder.append(ps.getPerson(max.getPrincipalId()).getName());
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
        boolean parameterExists = true;
        
        // check to make sure the system parameter for Parent Document Type = FP has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
            getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine("YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
            return false;
        }
        List<DocumentType> parentDocumentTypes = this.getYearEndAutoDisapproveParentDocumentTypes();
        
        for (DocumentType parentDocumentType : parentDocumentTypes) {   
            if (ObjectUtils.isNull(getDocumentTypeService().getDocumentTypeByName(parentDocumentType.getName()))) {
            	LOG.warn("Invalid Document Type: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE is invalid. The auto disapproval job cannot use this value.");
            	getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine("Invalid Document Type: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE is invalid. The auto disapproval job cannot use this value.");        	                			
            }           
        }
    
        return parameterExists;
    }

    protected boolean checkIfDocumentCompareCreateDateParameterExists() {
        boolean parameterExists = true;
        
        // check to make sure the system parameter for create date to compare has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE)) {
          LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          return false;
        }
	    try {
	        Date compareDate = getDateTimeService().convertToDate(getParameterService().getParameterValueAsString(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE));
	    }
	    catch (ParseException pe) {
	        LOG.warn("ParseException: System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE can not be determined.");
	        String message = ("ParseException: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREAT_DATE is invalid.  The auto disapproval job will not use this value.");
	        getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);                         
	        return false;
	    }
	   	
	   	return parameterExists;
    }
 
    protected boolean checkIfDocumentTypesExceptionParameterExists() {
        boolean parameterExists = true;
        
        // check to make sure the system parameter for Document Types that are exceptions has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES)) {
          LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter does not exist in the parameters list.  The job can not continue without this parameter");
          return false;
        }
        
        Collection<String> documentTypes = getParameterService().getParameterValuesAsString(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES);
        for (String dT : documentTypes)
        {
        	if (ObjectUtils.isNull(getDocumentTypeService().getDocumentTypeByName( dT ))) {        		
                LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter contains invalid value: \"" + dT + "\" The job can not continue with invalid values in this parameter.");
                getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter contains invalid value: \"" + dT + "\" The job can not continue with invalid values in this parameter.");
                parameterExists = false;
        	}
        }
        return parameterExists;
    }

    protected void autoDisapprovalYearEndDocument(Document document, String annotationForAutoDisapprovalDocument)  throws Exception {
        Person systemUser = getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER);      
        
        Note approveNote = getNoteService().createNote(new Note(), document.getDocumentHeader(), systemUser.getPrincipalId());
        approveNote.setNoteText(annotationForAutoDisapprovalDocument);

        approveNote.setAuthorUniversalIdentifier(systemUser.getPrincipalId());
        
        approveNote.setNotePostedTimestampToCurrent();
        
        getNoteService().save(approveNote);
        
        document.addNote(approveNote);
        
        getDocumentService().prepareWorkflowDocument(document);
        getDocumentService().superUserDisapproveDocumentWithoutSaving(document, "Disapproval of Outstanding Documents - Year End Cancellation Process");
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),document.getDocumentHeader().getWorkflowDocument());
   }

}
