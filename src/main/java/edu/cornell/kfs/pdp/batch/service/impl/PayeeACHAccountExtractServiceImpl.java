package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.uif.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.CUPdpPropertyConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractStep;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractService;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;
import edu.cornell.kfs.pdp.service.CuAchService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class PayeeACHAccountExtractServiceImpl implements PayeeACHAccountExtractService {
	private static final Logger LOG = LogManager.getLogger(PayeeACHAccountExtractServiceImpl.class);

    private BatchInputFileService batchInputFileService;
    private List<BatchInputFileType> batchInputFileTypes;
    private ConfigurationService configurationService;
    private ParameterService parameterService;
    private EmailService emailService;
    private DocumentService documentService;
    private DataDictionaryService dataDictionaryService;
    private PersonService personService;
    private SequenceAccessorService sequenceAccessorService;
    private CuAchService achService;
    private AchBankService achBankService;

    private Map<String, List<String>> partialProcessingSummary;

    // Portions of this method are based on code and logic from CustomerLoadServiceImpl.
    @Transactional
    @Override
    public boolean processACHBatchDetails() {
        LOG.info("processACHBatchDetails: Beginning processing of ACH input files");
        
        int numSuccess = 0;
        int numPartial = 0;
        int numFail = 0;
        partialProcessingSummary = new HashMap<String, List<String>>();

        Map<String,BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("processACHBatchDetails: Found " + fileNamesToLoad.size() + " file(s) to process.");

        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad.keySet()) {
            
            LOG.info("processACHBatchDetails: Beginning processing of filename: " + inputFileName);
            processedFiles.add(inputFileName);
            
            try {
                List<String> errorList = new ArrayList();
                errorList = loadACHBatchDetailFile(inputFileName, fileNamesToLoad.get(inputFileName));
                if (errorList.isEmpty()) {
                    LOG.info("processACHBatchDetails: Successfully loaded ACH input file");
                    numSuccess++;
                } else {
                    LOG.warn("processACHBatchDetails: ACH input file contained "+ errorList.size() + " rows that could not be processed.");
                    partialProcessingSummary.put(inputFileName, errorList);
                    numPartial++;
                }
            } catch (Exception e) {
                LOG.error("processACHBatchDetails: Failed to load ACH input file due to this Exception:", e);
                numFail++;
            }
        }

        removeDoneFiles(processedFiles);

        LOG.info("processACHBatchDetails: ==============================================");
        LOG.info("processACHBatchDetails: ==== Summary of Payee ACH Account Extract ====");
        LOG.info("processACHBatchDetails: ==============================================");
        LOG.info("processACHBatchDetails: Files loaded successfully: " + numSuccess);
        LOG.info("processACHBatchDetails: Files loaded with one or more failed rows: " + numPartial);
        if (!partialProcessingSummary.isEmpty()) {
            for (String failingFileName : partialProcessingSummary.keySet()) {
                List<String> errorsEncountered = partialProcessingSummary.get(failingFileName);
                LOG.error("processACHBatchDetails: ACH input file contained "+ errorsEncountered.size() + " rows that could not be processed.");
                for (Iterator iterator = errorsEncountered.iterator(); iterator.hasNext();) {
                    String dataError = (String) iterator.next();
                    LOG.error("processACHBatchDetails: " + dataError);
                }
            }
        }
        LOG.info("processACHBatchDetails: Files with errors: " + numFail);
        LOG.info("processACHBatchDetails: =====================");
        LOG.info("processACHBatchDetails: ==== End Summary ====");
        LOG.info("processACHBatchDetails: =====================");

        // For now, return true even if files or rows did not load successfully. Functionals will address the failed rows/files accordingly.
        return true;
    }

    /**
     * Create a collection of the files to process with the mapped value of the BatchInputFileType
     */
    // This is a slightly-tweaked copy of a method from CustomerLoadServiceImpl.
    protected Map<String,BatchInputFileType> getListOfFilesToProcess() {
        Map<String,BatchInputFileType> inputFileTypeMap = new LinkedHashMap<String, BatchInputFileType>();

        for (BatchInputFileType batchInputFileType : batchInputFileTypes) {

            List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
            if (inputFileNames == null) {
                criticalError("BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifier()
                        + ") returned NULL which should never happen.");
            } else {
                // update the file name mapping
                for (String inputFileName : inputFileNames) {

                    // filenames returned should never be blank/empty/null
                    if (StringUtils.isBlank(inputFileName)) {
                        criticalError("One of the file names returned as ready to process [" + inputFileName
                                + "] was blank.  This should not happen, so throwing an error to investigate.");
                    }

                    inputFileTypeMap.put(inputFileName, batchInputFileType);
                }
            }
        }

        return inputFileTypeMap;
    }

    /**
     * Processes a single ACH input file.
     */
    // Portions of this method are based on the code and logic from CustomerLoadServiceImpl.loadFile
    protected List<String> loadACHBatchDetailFile(String inputFileName, BatchInputFileType batchInputFileType) {
        List<String>failedRowsErrors = new ArrayList();
        
        byte[] fileByteContent = LoadFileUtils.safelyLoadFileBytes(inputFileName);
        
        LOG.info("loadACHBatchDetailFile: Attempting to parse the file.");
        
        Object parsedObject = null;
        try {
            parsedObject = batchInputFileService.parse(batchInputFileType, fileByteContent);
        } catch (ParseException e) {
            String errorMessage = "loadACHBatchDetailFile: Error parsing batch file: " + e.getMessage();
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        if (!(parsedObject instanceof List)) {
            String errorMessage = "loadACHBatchDetailFile: Parsed file was not of the expected type.  Expected [" + List.class + "] but got [" + parsedObject.getClass() + "].";
            criticalError(errorMessage);
        }

        List<PayeeACHAccountExtractDetail> achDetails = (List<PayeeACHAccountExtractDetail>) parsedObject;
        
        for (PayeeACHAccountExtractDetail achDetail : achDetails) {
            cleanPayeeACHAccountExtractDetail(achDetail);
            String error = processACHBatchDetail(achDetail);
            if (StringUtils.isNotBlank(error)) {
                failedRowsErrors.add(error);
            }
        }
        
        return failedRowsErrors;
    }
    
    protected void cleanPayeeACHAccountExtractDetail(PayeeACHAccountExtractDetail detail) {
        if (!StringUtils.isNumeric(detail.getBankAccountNumber())) {
            String logMessageStarter = "cleanPayeeACHAccountExtractDetail, the bank account for " + detail.getNetID();
            String bankAccountNumber = detail.getBankAccountNumber();
            if (StringUtils.contains(bankAccountNumber, KFSConstants.DASH)) {
                LOG.info(logMessageStarter + " contains dashes, so removing them");
                bankAccountNumber = StringUtils.remove(bankAccountNumber, KFSConstants.DASH);
            }
            if (StringUtils.contains(bankAccountNumber, StringUtils.SPACE)) {
                LOG.info(logMessageStarter + " contains spaces, so removing them");
                bankAccountNumber = StringUtils.remove(bankAccountNumber,  StringUtils.SPACE);
            }
            
            detail.setBankAccountNumber(bankAccountNumber);
            
            if (!StringUtils.isNumeric(detail.getBankAccountNumber())) {
                LOG.error(logMessageStarter + " is not numeric after cleaning");
            }
        }
    }

    /**
     * Processes a single ACH batch detail, and routes a Payee ACH Account maintenance document accordingly.
     */
    protected String processACHBatchDetail(PayeeACHAccountExtractDetail achDetail) {
        LOG.info("processACHBatchDetail: Starting processACHBatchDetail for: " + achDetail.getLogData());
        Person payee = personService.getPersonByPrincipalName(achDetail.getNetID());

        String processingError = validateACHBatchDetail(achDetail, payee);
        if (StringUtils.isNotBlank(processingError)) {
            return processingError;
        }
        
        processingError = addOrUpdateACHAccountIfNecessary(payee, achDetail, PayeeIdTypeCodes.ENTITY, payee.getEntityId());
        if (StringUtils.isNotBlank(processingError)) {
            return processingError;
        }
        
        processingError = addOrUpdateACHAccountIfNecessary(payee, achDetail, PayeeIdTypeCodes.EMPLOYEE, payee.getEmployeeId());
        
        return processingError;
    }

    protected String addOrUpdateACHAccountIfNecessary(
            Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType, String payeeIdNumber) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        PayeeACHAccount achAccount = achService.getAchInformationIncludingInactive(
                payeeType, payeeIdNumber, getDirectDepositTransactionType());
        
        if (ObjectUtils.isNull(achAccount)) {
            return addACHAccount(payee, achDetail, payeeType);
        } else {
            return updateACHAccountIfNecessary(payee, achDetail, achAccount);
        }
    }

    /**
     * Validates whether a single file-loaded ACH batch detail is valid for processing.
     * A warning message will be added to the logs if the row should be skipped.
     * 
     * @param achDetail The batch detail to check; cannot be null.
     * @param payee The object representing the payee matching the batch detail's netID; may be null.
     * @return True if the ACH batch detail passed validation, false otherwise.
     */
    protected String validateACHBatchDetail(PayeeACHAccountExtractDetail achDetail, Person payee) {
        final int BUILDER_START_SIZE = 200;
        StringBuilder failureMessage = new StringBuilder(BUILDER_START_SIZE);
        int listIndex = 1;
        
        if (ObjectUtils.isNull(payee) || StringUtils.isBlank(payee.getEntityId())) {
            // Person doesn't exist in Rice/KFS.
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Payee does not exist in KFS. ");
        } else {
            // Some validations can only occur if payee exists.
            if (!StringUtils.equals(achDetail.getEmployeeID(), payee.getEmployeeId())) {
                // Employee ID mismatch between input file and Rice/KFS.
                appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
                failureMessage.append(" Payee has an employee ID of \"").append(achDetail.getEmployeeID()).append(
                        "\" in input file, but has an employee ID of \"").append(payee.getEmployeeId()).append("\" in KFS. ");
            }
            if (StringUtils.isBlank(payee.getEmailAddressUnmasked())) {
                appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
                failureMessage.append(" Payee has no email address defined in KFS. No notification emails will be sent for this user.");
            }
        }

        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE.equalsIgnoreCase(achDetail.getPaymentType())) {
            // Input file payment type is not "Direct Deposit".
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Payment type is \"").append(achDetail.getPaymentType()).append("\" instead of \"").append(
                    CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE).append("\". ");
        }
        
        if (StringUtils.isBlank(achDetail.getBankName())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank Name was not supplied. ");
        }

        if (StringUtils.isBlank(achDetail.getBankRoutingNumber())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank routing number was not supplied. ");
        } else if (!StringUtils.isNumeric(achDetail.getBankRoutingNumber())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank routing number must only contain digits. ");
        } else if (ObjectUtils.isNull(achBankService.getByPrimaryId(achDetail.getBankRoutingNumber()))) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Could not find bank \"").append(achDetail.getBankName()).append("\" under the given routing number. ");
        }

        if (StringUtils.isBlank(achDetail.getBankAccountNumber())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank account number was not supplied. ");
        } else if (!StringUtils.isNumeric(achDetail.getBankAccountNumber())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank account number must only contain digits. ");
        }

        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_BALANCE_ACCOUNT_YES_INDICATOR.equals(achDetail.getBalanceAccount())) {
            // ACH detail is not for a balance account.
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Account is not a balance account. ");
        }
        
        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE.equalsIgnoreCase(achDetail.getBankAccountType())
                && !CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE.equalsIgnoreCase(achDetail.getBankAccountType())) {
            // ACH detail is not for a checking or savings account.
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Account is not a checking or savings account. ");
        }

        // Log the message as a warning if non-blank, and also treat non-blank messages as failures; otherwise return success.
        if (failureMessage.length() > 0) {
            LOG.warn("validateACHBatchDetail:" + failureMessage.toString());
            return failureMessage.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Helper method invoked by validateACHBatchDetail() when an ACH batch detail is not
     * valid for processing and the failure message needs to have a prefix added for the
     * first or subsequent failures detected.
     * 
     * @param failureMessage The StringBuilder to append the prefix to.
     * @param netID The payee's netID.
     * @param listIndex The index of the next failure reason to be appended by the calling code.
     */
    protected void appendFailurePrefix(StringBuilder failureMessage, String netID, int listIndex) {
        if (failureMessage.length() == 0) {
            failureMessage.append("ACH Detail for payee \"").append(netID).append("\" could not be processed for the following reasons: ");
        }
        failureMessage.append('[').append(listIndex).append(']');
    }

    protected String addACHAccount(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType) {
        PayeeACHData achData = new PayeeACHData(payee, achDetail, payeeType);
        return createAndRoutePayeeACHAccountDocument(achData, this::setupDocumentForACHCreate);
    }

    protected void setupDocumentForACHCreate(MaintenanceDocument paatDocument, PayeeACHData achData) {
        Person payee = achData.getPayee();
        PayeeACHAccountExtractDetail achDetail = achData.getAchDetail();
        String payeeType = achData.getPayeeType();
        
        PayeeACHAccountMaintainableImpl maintainable = (PayeeACHAccountMaintainableImpl) paatDocument.getNewMaintainableObject();
        maintainable.setMaintenanceAction(KFSConstants.MAINTENANCE_NEW_ACTION);
        PayeeACHAccount achAccount = (PayeeACHAccount) maintainable.getDataObject();

        if (StringUtils.equals(PayeeIdTypeCodes.ENTITY, payeeType)) {
            achAccount.setPayeeIdNumber(payee.getEntityId());
        } else if (StringUtils.equals(PayeeIdTypeCodes.EMPLOYEE, payeeType)) {
            achAccount.setPayeeIdNumber(payee.getEmployeeId());
        } else {
            throw new RuntimeException("Invalid payee ID type: " + payeeType);
        }
        achAccount.setPayeeIdentifierTypeCode(payeeType);
        
        Long newId = sequenceAccessorService.getNextAvailableSequenceNumber(PdpConstants.ACH_ACCOUNT_IDENTIFIER_SEQUENCE_NAME);
        achAccount.setAchAccountGeneratedIdentifier(new KualiInteger(newId));
        achAccount.setAchTransactionType(getDirectDepositTransactionType());
        achAccount.setBankRoutingNumber(achDetail.getBankRoutingNumber());
        achAccount.setBankAccountNumber(achDetail.getBankAccountNumber());
        achAccount.setBankAccountTypeCode(getACHTransactionCode(achDetail.getBankAccountType()));
        if (StringUtils.isNotBlank(payee.getNameUnmasked())) {
            achAccount.setPayeeName(payee.getNameUnmasked());
        }
        if (StringUtils.isNotBlank(payee.getEmailAddressUnmasked())) {
            achAccount.setPayeeEmailAddress(payee.getEmailAddressUnmasked());
        }
        achAccount.setActive(true);
    }

    protected String updateACHAccountIfNecessary(Person payee, PayeeACHAccountExtractDetail achDetail, PayeeACHAccount achAccount) {
        StringBuilder processingResults = new StringBuilder();

        if (accountHasChanged(achDetail, achAccount)) {
            PayeeACHData achData = new PayeeACHData(payee, achDetail, achAccount.getPayeeIdentifierTypeCode(), achAccount);
            String accountUpdateErrors = createAndRoutePayeeACHAccountDocument(achData, this::setupDocumentForACHUpdate);
            processingResults.append(accountUpdateErrors);
        } else {
            LOG.info("updateACHAccountIfNecessary: Input file's account information for payee of type '" + achAccount.getPayeeIdentifierTypeCode()
                    + "' matches what is already in KFS; no updates will be made for this entry.");
        }
        
        if (processingResults.length() > 0) {
            processingResults.append(" Update was NOT performed.");
            LOG.warn("updateACHAccountIfNecessary: " + processingResults.toString());
        }
        
        return processingResults.toString();
    }

    protected boolean accountHasChanged(PayeeACHAccountExtractDetail achDetail, PayeeACHAccount achAccount) {
        return !StringUtils.equals(achDetail.getBankRoutingNumber(), achAccount.getBankRoutingNumber())
                || !StringUtils.equals(achDetail.getBankAccountNumber(), achAccount.getBankAccountNumber())
                || !StringUtils.equals(
                        getACHTransactionCode(achDetail.getBankAccountType()), achAccount.getBankAccountTypeCode());
    }

    protected void setupDocumentForACHUpdate(MaintenanceDocument paatDocument, PayeeACHData achData) {
        if (!achData.hasOldAccount()) {
            throw new RuntimeException("An existing ACH account should have been present");
        }
        PayeeACHAccountExtractDetail achDetail = achData.getAchDetail();
        PayeeACHAccount oldAccount = achData.getOldAccount();
        PayeeACHAccount newAccount = (PayeeACHAccount) ObjectUtils.deepCopy(oldAccount);
        
        newAccount.setBankRoutingNumber(achDetail.getBankRoutingNumber());
        newAccount.setBankAccountNumber(achDetail.getBankAccountNumber());
        newAccount.setBankAccountTypeCode(getACHTransactionCode(achDetail.getBankAccountType()));
        newAccount.setActive(true);
        
        paatDocument.getOldMaintainableObject().setDataObject(oldAccount);
        paatDocument.getNewMaintainableObject().setDataObject(newAccount);
        paatDocument.getNewMaintainableObject().setMaintenanceAction(KFSConstants.MAINTENANCE_EDIT_ACTION);
    }

    protected String createAndRoutePayeeACHAccountDocument(
            PayeeACHData achData, BiConsumer<MaintenanceDocument, PayeeACHData> documentConfigurer) {
        try {
            MaintenanceDocument paatDocument = (MaintenanceDocument) documentService.getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE);
            documentConfigurer.accept(paatDocument, achData);
            
            Person payee = achData.getPayee();
            PayeeACHAccount achAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();
            paatDocument.getDocumentHeader().setDocumentDescription(
                    buildDocumentDescription(payee, achAccount, paatDocument));
            
            addNote(paatDocument, getPayeeACHAccountExtractParameter(CUPdpParameterConstants.GENERATED_PAYEE_ACH_ACCOUNT_DOC_NOTE_TEXT));

            paatDocument = (MaintenanceDocument) documentService.routeDocument(
                    paatDocument, CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_ROUTE_ANNOTATION, null);
            
            achAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();
            sendPayeeACHAccountAddOrUpdateEmail((PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject(), payee, paatDocument);
            LOG.info("createAndRoutePayeeACHAccountDocument: " + getSuccessMessageStart(paatDocument) + "ACH Account of type "
                    + achAccount.getPayeeIdentifierTypeCode() + " for payee " + payee.getPrincipalName());
        } catch (Exception e) {
            LOG.error("createAndRoutePayeeACHAccountDocument: " + getFailRequestMessage(e), e);
            return "createAndRoutePayeeACHAccountDocument: " + achData.getAchDetail().getLogData() + " STE was generated. " + getFailRequestMessage(e);
        }
        
        return StringUtils.EMPTY;
    }

    private String getSuccessMessageStart(MaintenanceDocument document) {
        return isDocumentCreatingNewAccount(document) ? "Created new " : "Updated existing ";
    }

    private String getFailRequestMessage(Exception e) {
        if (e instanceof ValidationException) {
            return "Failed request : "+ e.getMessage() + " - " +  getValidationErrorMessage();
        } else {
            return "Failed request : " + e.getCause() + " - " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
    }

    private String getValidationErrorMessage() {
        StringBuilder validationError = new StringBuilder();
        for (String errorProperty : GlobalVariables.getMessageMap().getAllPropertiesWithErrors()) {
            for (Object errorMessage : GlobalVariables.getMessageMap().getMessages(errorProperty)) {
                String errorMsg = configurationService.getPropertyValueAsString(((ErrorMessage) errorMessage).getErrorKey());
                if (errorMsg == null) {
                    throw new RuntimeException("Cannot find message for error key: " + ((ErrorMessage) errorMessage).getErrorKey());
                }
                else {
                    Object[] arguments = (Object[]) ((ErrorMessage) errorMessage).getMessageParameters();
                    if (arguments != null && arguments.length != 0) {
                        errorMsg = MessageFormat.format(errorMsg, arguments);
                    }
                }
                validationError.append(errorMsg + KFSConstants.NEWLINE);
            }
        }
        return validationError.toString();
    }

    /**
     * Returns the ACH transaction code for the given input file transaction type.
     * The default implementation expects the input file type to be "Checking" or "Savings",
     * and uses parameters initially set to map those types to the "22PPD" (Personal Checking)
     * and "32PPD" (Personal Savings) ACH transaction codes, respectively.
     */
    protected String getACHTransactionCode(String workdayAccountType) {
        if (CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE.equalsIgnoreCase(workdayAccountType)) {
            return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_PERSONAL_CHECKING_TRANSACTION_CODE);
        } else if (CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE.equalsIgnoreCase(workdayAccountType)) {
            return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_PERSONAL_SAVINGS_TRANSACTION_CODE);
        } else {
            throw new IllegalArgumentException("Unrecognized account type from file: " + workdayAccountType);
        }
    }

    protected String getDirectDepositTransactionType() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE);
    }

    protected String getEmailSubjectForNewPayeeACHAccount() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT);
    }

    protected String getEmailSubjectForUpdatedPayeeACHAccount() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT);
    }

    protected String getUnresolvedEmailBodyForNewPayeeACHAccount() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_BODY);
    }

    protected String getUnresolvedEmailBodyForUpdatedPayeeACHAccount() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_BODY);
    }

    protected String getPayeeACHAccountExtractParameter(String parameterName) {
        return parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class, parameterName);
    }

    protected void sendPayeeACHAccountAddOrUpdateEmail(PayeeACHAccount achAccount, Person payee, MaintenanceDocument document) {
        if (isDocumentCreatingNewAccount(document)) {
            sendPayeeACHAccountAddOrUpdateEmail(achAccount, payee,
                    getEmailSubjectForNewPayeeACHAccount(), getUnresolvedEmailBodyForNewPayeeACHAccount());
        } else {
            sendPayeeACHAccountAddOrUpdateEmail(achAccount, payee,
                    getEmailSubjectForUpdatedPayeeACHAccount(), getUnresolvedEmailBodyForUpdatedPayeeACHAccount());
        }
    }

    /**
     * Sends an ACH Account add/update notification email to the given payee.
     * 
     * @param achAccount The Payee ACH Account that was added or updated.
     * @param payee The person affected by the change.
     * @param emailSubject The email subject line.
     * @param emailBody The email body; may contain "[propertyName]"-style placeholders to substitute in ACH Account values, as well as newlines.
     */
    protected void sendPayeeACHAccountAddOrUpdateEmail(PayeeACHAccount achAccount, Person payee, String emailSubject, String emailBody) {
        if (StringUtils.isBlank(payee.getEmailAddressUnmasked())) {
            LOG.warn("Payee " + payee.getPrincipalName() + " has no email address defined in KFS. No notification emails will be sent for this user.");
            return;
        }
        
        // Construct mail message, and replace property placeholders and literal "\n" strings in the email body accordingly.
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, KFSConstants.FROM_EMAIL_ADDRESS_PARM_NM));
        message.setSubject(emailSubject);
        message.setMessage(getResolvedEmailBody(achAccount, emailBody));
        message.addToAddress(payee.getEmailAddressUnmasked());
        
        // Send the message.
        emailService.sendMessage(message, false);        
    }

    /**
     * Helper method for replacing "[propertyName]"-style placeholders in the email body
     * with actual property values from the given Payee ACH Account, in addition to
     * replacing literal "\n" with newline characters accordingly. Potentially sensitive
     * placeholders will be replaced with empty text except for the bank name.
     * Placeholders referencing properties with value finders will print the matching key/value
     * label instead. Any placeholders that could not be resolved successfully will be replaced
     * with empty text.
     */
    @SuppressWarnings("deprecation")
    protected String getResolvedEmailBody(PayeeACHAccount achAccount, String emailBody) {
        Pattern placeholderPattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher emailMatcher = placeholderPattern.matcher(emailBody.replace("\\n", "\n"));
        // Use a StringBuffer here, due to the Matcher class not supporting StringBuilder for appending operations.
        StringBuffer resolvedEmailBody = new StringBuffer(emailBody.length());
        
        // Replace all placeholders one by one. The pattern has a single group in it to help with retrieving just the property name and not the brackets.
        while (emailMatcher.find()) {
            String propertyName = emailMatcher.group(1);
            AttributeDefinition attDefinition = dataDictionaryService.getAttributeDefinition(PayeeACHAccount.class.getName(), propertyName);
            
            String replacement;
            // Make sure property exists in data dictionary and is either not potentially sensitive or is the safe-to-use bank name property.
            if (attDefinition != null) {
                AttributeSecurity attSecurity = attDefinition.getAttributeSecurity();
                if (attSecurity != null && (attSecurity.isHide() || attSecurity.isMask() || attSecurity.isPartialMask())
                        && !CUPdpPropertyConstants.PAYEE_ACH_BANK_NAME.equals(propertyName)) {
                    // Replace potentially-sensitive placeholders with an empty string.
                    replacement = StringUtils.EMPTY;
                } else {
                    // Replace the placeholder with the property value, or with an empty string if null or invalid.
                    try {
                        Object propertyValue = ObjectPropertyUtils.getPropertyValue(achAccount, propertyName);
                        replacement = ObjectUtils.isNotNull(propertyValue) ? propertyValue.toString() : StringUtils.EMPTY;
                        // If a values finder is defined, use the label from the matching key/value pair instead.
                        if (attDefinition.getControl() != null && ObjectUtils.isNotNull(attDefinition.getControl().getValuesFinder())) {
                            KeyValuesFinder valuesFinder = attDefinition.getControl().getValuesFinder();
                            String key = replacement;
                            replacement = valuesFinder.getKeyLabel(key);
                            // If the key is in the label, then remove it from the label.
                            if (attDefinition.getControl().getIncludeKeyInLabel() != null
                                    && attDefinition.getControl().getIncludeKeyInLabel().booleanValue()) {
                                // Check for key-and-dash or key-in-parentheses, and remove them if found.
                                // (Former can come from BO values finders, latter is only for custom values finders that append the keys as such.)
                                String keyAndDashPrefix = key + " - ";
                                String keyInParenSuffix = " (" + key + ")";
                                replacement = replacement.startsWith(keyAndDashPrefix) ? StringUtils.substringAfter(replacement, keyAndDashPrefix)
                                        : (replacement.endsWith(keyInParenSuffix)
                                                ? StringUtils.substringBeforeLast(replacement, keyInParenSuffix) : replacement);
                            }
                        }
                        // Because of the way that Matcher.appendReplacement() works, escape the special replacement characters accordingly.
                        if (replacement.indexOf('\\') != -1) {
                            replacement = replacement.replace("\\", "\\\\");
                        }
                        if (replacement.indexOf('$') != -1) {
                            replacement = replacement.replace("$", "\\$");
                        }
                        
                    } catch (RuntimeException e) {
                        replacement = StringUtils.EMPTY;
                    }
                }
            } else {
                // Replace non-data-dictionary-defined property placeholders with an empty string.
                replacement = StringUtils.EMPTY;
            }
            
            emailMatcher.appendReplacement(resolvedEmailBody, replacement);
        }
        
        emailMatcher.appendTail(resolvedEmailBody);
        return resolvedEmailBody.toString();
    }

    /*
     * Copied this method from VendorBatchServiceImpl and tweaked accordingly.
     */
    protected void addNote(Document document, String noteText) {
        Note note = createEmptyNote();

        note.setNoteText(noteText);
        note.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
        note.setNotePostedTimestampToCurrent();
        document.addNote(note);
    }

    /*
     * This has been separated into its own method for unit testing convenience.
     */
    protected Note createEmptyNote() {
        return new Note();
    }

    /*
     * Copied this method from VendorBatchServiceImpl.
     */
    private Person getSystemUser() {
        return personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
    }

    /**
     * Clears out associated .done files for the processed data files.
     * 
     * @param dataFileNames
     */
    // Copied this method from CustomerLoadServiceImpl.
    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    protected String buildDocumentDescription(Person payee, PayeeACHAccount achAccount, MaintenanceDocument document) {
        boolean isNewAccount = isDocumentCreatingNewAccount(document);
        boolean isEntityId = StringUtils.equalsIgnoreCase(PayeeIdTypeCodes.ENTITY, achAccount.getPayeeIdentifierTypeCode());
        int descMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(), KRADPropertyConstants.DOCUMENT_DESCRIPTION).intValue();
        StringBuilder docDescription = new StringBuilder(descMaxLength);
        
        docDescription.append(payee.getPrincipalName());
        docDescription.append(isNewAccount ? " -- New " : " -- Edit ");
        docDescription.append(isEntityId ? "Entity" : "Employee");
        docDescription.append(" Account");
        
        return StringUtils.left(docDescription.toString(), descMaxLength);
    }

    protected boolean isDocumentCreatingNewAccount(MaintenanceDocument document) {
        return StringUtils.equalsIgnoreCase(KFSConstants.MAINTENANCE_NEW_ACTION, document.getNewMaintainableObject().getMaintenanceAction());
    }

    /**
     * LOG error and throw RuntimeException
     * 
     * @param errorMessage
     */
    private void criticalError(String errorMessage) {
        LOG.error("criticalError: " + errorMessage);
        throw new RuntimeException(errorMessage);
    }    

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setBatchInputFileTypes(List<BatchInputFileType> batchInputFileTypes) {
        this.batchInputFileTypes = batchInputFileTypes;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setSequenceAccessorService(SequenceAccessorService sequenceAccessorService) {
        this.sequenceAccessorService = sequenceAccessorService;
    }

    public void setAchService(CuAchService achService) {
        this.achService = achService;
    }

    public void setAchBankService(AchBankService achBankService) {
        this.achBankService = achBankService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    protected static class PayeeACHData {
        private Person payee;
        private PayeeACHAccountExtractDetail achDetail;
        private String payeeType;
        private Optional<PayeeACHAccount> oldAccount;
        
        public PayeeACHData(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType) {
            this(payee, achDetail, payeeType, null);
        }
        
        public PayeeACHData(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType, PayeeACHAccount oldAccount) {
            this.payee = payee;
            this.achDetail = achDetail;
            this.payeeType = payeeType;
            this.oldAccount = Optional.ofNullable(oldAccount);
        }
        
        public Person getPayee() {
            return payee;
        }
        
        public PayeeACHAccountExtractDetail getAchDetail() {
            return achDetail;
        }
        
        public String getPayeeType() {
            return payeeType;
        }
        
        public boolean hasOldAccount() {
            return oldAccount.isPresent();
        }
        
        public PayeeACHAccount getOldAccount() {
            return oldAccount.get();
        }
    }

}
