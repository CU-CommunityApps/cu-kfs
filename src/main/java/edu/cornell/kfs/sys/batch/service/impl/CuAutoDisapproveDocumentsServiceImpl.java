package edu.cornell.kfs.sys.batch.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.UserSessionUtils;
import org.kuali.rice.krad.bo.DocumentHeader;
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
        Date documentStartDate = null;
        if(checkIfStartDateParameterExists()){
        	documentStartDate = getDocumentStartDateParameter();
        }
        Collection<FinancialSystemDocumentHeader> documentList = this.getFinancialSystemDocumentService().findByWorkflowStatusCode(DocumentStatus.ENROUTE);
		String documentHeaderId = null;

		for (FinancialSystemDocumentHeader result : documentList) {
			documentHeaderId = result.getDocumentNumber();
			Document document = findDocumentForAutoDisapproval(documentHeaderId);
			if (document != null) {
				if (checkIfDocumentEligibleForAutoDispproval(document.getDocumentHeader())) {
					if (!exceptionsToAutoDisapproveProcess(document.getDocumentHeader(), documentCompareDate, documentStartDate)) {
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
			
        return success;
    }
    
    /**
     * This method first checks the document's create date with system parameter date and then
     * checks the document type name to the system parameter values and returns true if the type name exists
     * @param document document to check for its document type,  documentCompareDate the system parameter specified date
     * to compare the current date to this date.
     * @return true if  document's create date is <= documentCompareDate and if document type is not in the
     * system parameter document types that are set to disallow.
     */
    protected boolean exceptionsToAutoDisapproveProcess(DocumentHeader documentHeader, Date documentCompareDate, Date documentStartDate) {
        boolean exceptionToDisapprove = true;
        Date createDate = null;

        String documentNumber =  documentHeader.getDocumentNumber();

        DateTime documentCreateDate = documentHeader.getWorkflowDocument().getDateCreated();
        createDate = documentCreateDate.toDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(documentCompareDate);
        String strCompareDate = calendar.getTime().toString();

        calendar.setTime(createDate);
        String strCreateDate = calendar.getTime().toString();
        
        boolean startDateValid = documentStartDate == null  || (documentStartDate!=null && (createDate.after(documentStartDate) || createDate.equals(documentStartDate))); 

        if (startDateValid && (createDate.before(documentCompareDate) || createDate.equals(documentCompareDate))) {
            String documentTypeName = documentHeader.getWorkflowDocument().getDocumentTypeName();

            ParameterEvaluator evaluatorDocumentType = /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES, documentTypeName);
            exceptionToDisapprove = !evaluatorDocumentType.evaluationSucceeds();
            if (exceptionToDisapprove) {
                LOG.info("Document Id: " + documentNumber + " - Exception to Auto Disapprove:  Document's type: " + documentTypeName + " is in the System Parameter For Document Types Exception List.");
            }
        }
        else {
            LOG.info("Document Id: " + documentNumber + " - Exception to Auto Disapprove:  Document's create date: " + strCreateDate + " is NOT less than or equal to System Parameter Compare Date: " + strCompareDate);
            exceptionToDisapprove = true;
        }

        return exceptionToDisapprove;
    }
    
    /**
     * This method finds the date in the system parameters that will be used to compare the create date.
     * It then adds 23 hours, 59 minutes and 59 seconds to the compare date.
     * @return  documentCompareDate returns YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE from the system parameter
     */
    protected Date getDocumentStartDateParameter() {
        Date documentCompareDate = null;

        String yearEndAutoDisapproveDocumentDate = getParameterService().getParameterValueAsString(AutoDisapproveDocumentsStep.class, CUKFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_START_DATE);

        if (ObjectUtils.isNull(yearEndAutoDisapproveDocumentDate)) {
            LOG.warn("Exception: System Parameter YEAR_END_AUTO_DISAPPROVE_START_DATE can not be determined.");
            String message = ("Exception: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_START_DATE can not be determined.  The auto disapproval job is stopped.");
            getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);
            throw new RuntimeException("Exception: AutoDisapprovalStep job stopped because System Parameter YEAR_END_AUTO_DISAPPROVE_START_DATE is null");
        }

        try {
            Date compareDate = getDateTimeService().convertToDate(yearEndAutoDisapproveDocumentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(compareDate);
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            documentCompareDate = calendar.getTime();
        }
        catch (ParseException pe) {
            LOG.warn("ParseException: System Parameter YEAR_END_AUTO_DISAPPROVE_START_DATE can not be determined.");
            String message = ("ParseException: The value for System Parameter YEAR_END_AUTO_DISAPPROVE_START_DATE is invalid.  The auto disapproval job is stopped.");
            getAutoDisapproveErrorReportWriterService().writeFormattedMessageLine(message);
            throw new RuntimeException("ParseException: AutoDisapprovalStep job stopped because System Parameter YEAR_END_AUTO_DISAPPROVE_START_DATE is invalid");
        }

        return documentCompareDate;
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
    
    /**
     * This method will check the document's document type against the parent document types as specified in the system parameter
     * @param document
     * @return true if  document type of the document is a child of one of the the parent document types.
     */
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
	
}
