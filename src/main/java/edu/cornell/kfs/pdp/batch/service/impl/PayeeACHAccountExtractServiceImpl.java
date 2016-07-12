package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.pdp.service.AchService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.InvalidAddressException;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.MailService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.uif.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractStep;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractService;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public class PayeeACHAccountExtractServiceImpl implements PayeeACHAccountExtractService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PayeeACHAccountExtractServiceImpl.class);

    private BatchInputFileService batchInputFileService;
    private List<BatchInputFileType> batchInputFileTypes;
    private ParameterService parameterService;
    private MailService mailService;
    private DocumentService documentService;
    private DataDictionaryService dataDictionaryService;
    private PersonService personService;
    private SequenceAccessorService sequenceAccessorService;
    private AchService achService;
    private AchBankService achBankService;

    // Portions of this method are based on code and logic from CustomerLoadServiceImpl.
    @Transactional
    @Override
    public boolean processACHBatchDetails() {
        LOG.info("Beginning processing of ACH input files");
        
        int numSuccess = 0;
        int numPartial = 0;
        int numFail = 0;
        
        // create a list of the files to process.
        Map<String,BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("Found " + fileNamesToLoad.size() + " file(s) to process.");
        
        // process each file in turn
        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad.keySet()) {
            
            LOG.info("Beginning processing of filename: " + inputFileName);
            processedFiles.add(inputFileName);
            
            try {
                if (loadACHBatchDetailFile(inputFileName, fileNamesToLoad.get(inputFileName))) {
                    LOG.info("Successfully loaded ACH input file");
                    numSuccess++;
                } else {
                    LOG.warn("ACH input file contained one or more rows that could not be processed");
                    numPartial++;
                }
            } catch (Exception e) {
                LOG.error("Failed to load ACH input file", e);
                numFail++;
            }
        }

        removeDoneFiles(processedFiles);

        LOG.info("==== Summary of Payee ACH Account Extract ====");
        LOG.info("Files loaded successfully: " + Integer.toString(numSuccess));
        LOG.info("Files loaded with one or more failed rows: " + Integer.toString(numPartial));
        LOG.info("Files with errors: " + Integer.toString(numFail));
        LOG.info("==== End Summary ====");
        
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
                criticalError("BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifer()
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
    protected boolean loadACHBatchDetailFile(String inputFileName, BatchInputFileType batchInputFileType) {
        boolean result = true;
        
        byte[] fileByteContent = safelyLoadFileBytes(inputFileName);
        
        LOG.info("Attempting to parse the file.");
        
        Object parsedObject = null;
        try {
            parsedObject = batchInputFileService.parse(batchInputFileType, fileByteContent);
        } catch (ParseException e) {
            String errorMessage = "Error parsing batch file: " + e.getMessage();
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        if (!(parsedObject instanceof List)) {
            String errorMessage = "Parsed file was not of the expected type.  Expected [" + List.class + "] but got [" + parsedObject.getClass() + "].";
            criticalError(errorMessage);
        }
        
        @SuppressWarnings("unchecked")
        List<PayeeACHAccountExtractDetail> achDetails = (List<PayeeACHAccountExtractDetail>) parsedObject;
        
        for (PayeeACHAccountExtractDetail achDetail : achDetails) {
            if (!processACHBatchDetail(achDetail)) {
                result = false;
            }
        }
        
        return result;
    }

    /**
     * Processes a single ACH batch detail, and routes a Payee ACH Account maintenance document accordingly.
     */
    protected boolean processACHBatchDetail(PayeeACHAccountExtractDetail achDetail) {
        Person payee = personService.getPersonByPrincipalName(achDetail.getNetID());

        if (!validateACHBatchDetail(achDetail, payee)) {
            return false;
        }
        
        // Check for existing ACH accounts.
        PayeeACHAccount entityAccount = achService.getAchInformation(
                PayeeIdTypeCodes.ENTITY, payee.getEntityId(), getDirectDepositTransactionType());
        PayeeACHAccount employeeAccount = achService.getAchInformation(
                PayeeIdTypeCodes.EMPLOYEE, payee.getEmployeeId(), getDirectDepositTransactionType());
        
        // Add or update Entity ID account.
        if (ObjectUtils.isNull(entityAccount)) {
            addACHAccount(payee, achDetail, PayeeIdTypeCodes.ENTITY);
            LOG.info("Created new ACH Account of Entity type for payee " + payee.getPrincipalName());
        } else {
            LOG.info("Checking for updates to ACH Account of Entity type for payee " + payee.getPrincipalName());
            updateACHAccountIfNecessary(payee, achDetail, entityAccount);
        }
        
        // Add or update Employee ID account.
        if (ObjectUtils.isNull(employeeAccount)) {
            addACHAccount(payee, achDetail, PayeeIdTypeCodes.EMPLOYEE);
            LOG.info("Created new ACH Account of Employee type for payee " + payee.getPrincipalName());
        } else {
            LOG.info("Checking for updates to ACH Account of Employee type for payee " + payee.getPrincipalName());
            updateACHAccountIfNecessary(payee, achDetail, employeeAccount);
        }
        
        return true;
    }

    /**
     * Validates whether a single file-loaded ACH batch detail is valid for processing.
     * A warning message will be added to the logs if the row should be skipped.
     * 
     * @param achDetail The batch detail to check; cannot be null.
     * @param payee The object representing the payee matching the batch detail's netID; may be null.
     * @return True if the ACH batch detail passed validation, false otherwise.
     */
    protected boolean validateACHBatchDetail(PayeeACHAccountExtractDetail achDetail, Person payee) {
        final int BUILDER_START_SIZE = 100;
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
        }
        
        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE.equalsIgnoreCase(achDetail.getPaymentType())) {
            // Input file payment type is not "Direct Deposit".
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Payment type is \"").append(achDetail.getPaymentType()).append("\" instead of \"").append(
                    CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE).append("\". ");
        }
        
        if (ObjectUtils.isNull(achBankService.getByPrimaryId(achDetail.getBankRoutingNumber()))) {
            // Could not find bank.
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Could not find bank \"").append(achDetail.getBankName()).append("\" under the given routing number. ");
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
            LOG.warn(failureMessage.toString());
            return false;
        }
        return true;
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



    /**
     * Creates and routes a PAAT document to create a new ACH Account of the given payee type.
     */
    protected void addACHAccount(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType) {
        // Create and route a new PAAT (Payee ACH Account Maintenance) document.
        try {
            // Create document and set description.
            MaintenanceDocument paatDocument = (MaintenanceDocument) documentService.getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE);
            paatDocument.getDocumentHeader().setDocumentDescription(getDocumentDescription(payee, PayeeIdTypeCodes.ENTITY.equals(payeeType), true));
            
            // Configure as "New" maintenance and get maintained object.
            PayeeACHAccountMaintainableImpl maintainable = (PayeeACHAccountMaintainableImpl) paatDocument.getNewMaintainableObject();
            maintainable.setMaintenanceAction(KFSConstants.MAINTENANCE_NEW_ACTION);
            PayeeACHAccount achAccount = (PayeeACHAccount) maintainable.getDataObject();
            
            // Setup payee ID and type.
            if (PayeeIdTypeCodes.ENTITY.equals(payeeType)) {
                achAccount.setPayeeIdNumber(payee.getEntityId());
            } else if (PayeeIdTypeCodes.EMPLOYEE.equals(payeeType)) {
                achAccount.setPayeeIdNumber(payee.getEmployeeId());
            } else {
                throw new WorkflowException("Unexpected payee ID type for automated Payee ACH Account creation: " + payeeType);
            }
            achAccount.setPayeeIdentifierTypeCode(payeeType);
            
            // Setup other fields.
            achAccount.setAchAccountGeneratedIdentifier(
                    new KualiInteger(sequenceAccessorService.getNextAvailableSequenceNumber(PdpConstants.ACH_ACCOUNT_IDENTIFIER_SEQUENCE_NAME)));
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
            
            // Add a note indicating that this document was generated by a batch process.
            addNote(paatDocument, parameterService.getParameterValueAsString(
                    PayeeACHAccountExtractStep.class, CUPdpParameterConstants.GENERATED_PAYEE_ACH_ACCOUNT_DOC_NOTE_TEXT));
            
            // Route the document and send notifications.
            paatDocument = (MaintenanceDocument) documentService.routeDocument(paatDocument, KFSConstants.EMPTY_STRING, null);
            sendPayeeACHAccountAddOrUpdateEmail((PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject(), payee,
                    getPayeeACHAccountAddOrUpdateEmailSubject(true), getUnresolvedPayeeACHAccountAddOrUpdateEmailBody(true));
            
        } catch (WorkflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the existing ACH account matches the data fed in from the input file, and creates and routes
     * a new PAAT document to edit the account if necessary.
     * 
     * NOTE: The current implementation just logs match/mismatch status without making updates.
     * This will be modified in the future to perform actual updates.
     */
    protected void updateACHAccountIfNecessary(Person payee, PayeeACHAccountExtractDetail achDetail, PayeeACHAccount achAccount) {
        if (!StringUtils.equals(achDetail.getBankRoutingNumber(), achAccount.getBankRoutingNumber())
                || !StringUtils.equals(achDetail.getBankAccountNumber(), achAccount.getBankAccountNumber())) {
            /*
             * For a future enhancement, we will modify this method to actually update the Payee ACH Account
             * via a maintenance document, and possibly use more fields for change comparison.
             */
            LOG.info("Account information from input file for payee " + achDetail.getNetID() + " and payee type '"
                    + achAccount.getPayeeIdentifierTypeCode() + "' does not match what is in KFS, but will be left as-is.");
        } else {
            LOG.info("Input file's account information for payee " + achDetail.getNetID() + " of type '" + achAccount.getPayeeIdentifierTypeCode()
                    + "' matches what is already in KFS; no updates will be made for this entry.");
        }
    }



    /**
     * Returns the ACH transaction code for the given input file transaction type.
     * The default implementation expects the input file type to be "Checking" or "Savings",
     * and uses parameters initially set to map those types to the "22PPD" (Personal Checking)
     * and "32PPD" (Personal Savings) ACH transaction codes, respectively.
     */
    protected String getACHTransactionCode(String workdayAccountType) {
        if (CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE.equalsIgnoreCase(workdayAccountType)) {
            return parameterService.getParameterValueAsString(
                    PayeeACHAccountExtractStep.class, CUPdpParameterConstants.ACH_PERSONAL_CHECKING_TRANSACTION_CODE);
        } else if (CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE.equalsIgnoreCase(workdayAccountType)) {
            return parameterService.getParameterValueAsString(
                    PayeeACHAccountExtractStep.class, CUPdpParameterConstants.ACH_PERSONAL_SAVINGS_TRANSACTION_CODE);
        } else {
            throw new IllegalArgumentException("Unrecognized account type from file: " + workdayAccountType);
        }
    }

    /**
     * Returns the ACH transaction type representing direct deposits, for use by
     * ACH accounts generated from the input file extract. The default implementation
     * uses a parameter initially set to the "PRAP" (Purchasing/AP Direct Deposit Records) type.
     */
    protected String getDirectDepositTransactionType() {
        return parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class, CUPdpParameterConstants.ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE);
    }

    /**
     * Returns the email subject line for notification messages concerning the addition
     * or update of a Payee ACH Account by this service.
     */
    protected String getPayeeACHAccountAddOrUpdateEmailSubject(boolean forAdd) {
        return parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class,
                forAdd ? CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT : CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT);
    }

    /**
     * Returns the unresolved email body for notification messages concerning the addition
     * or update of a Payee ACH Account by this service. The calling code is responsible
     * for resolving any property placeholders or literal "\n" strings accordingly.
     */
    protected String getUnresolvedPayeeACHAccountAddOrUpdateEmailBody(boolean forAdd) {
        return parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class,
                forAdd ? CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_BODY : CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_BODY);
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
        MailMessage message = new MailMessage();
        message.setFromAddress(parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, KFSConstants.FROM_EMAIL_ADDRESS_PARM_NM));
        message.setSubject(emailSubject);
        message.setMessage(getResolvedEmailBody(achAccount, emailBody));
        message.addToAddress(payee.getEmailAddressUnmasked());
        
        // Send the message.
        try {
            mailService.sendMessage(message);
        } catch (InvalidAddressException e) {
            LOG.error("Invalid email address for notification of ACH account add/update. Message not sent.", e);
        } catch (MessagingException e) {
            LOG.error("Unexpected error sending notification email for ACH account add/update. Message not sent.", e);
        }
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
                        && !CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_BANK_NAME_PROPERTY.equals(propertyName)) {
                    // Replace potentially-sensitive placeholders with an empty string.
                    replacement = KFSConstants.EMPTY_STRING;
                } else {
                    // Replace the placeholder with the property value, or with an empty string if null or invalid.
                    try {
                        Object propertyValue = ObjectPropertyUtils.getPropertyValue(achAccount, propertyName);
                        replacement = ObjectUtils.isNotNull(propertyValue) ? propertyValue.toString() : KFSConstants.EMPTY_STRING;
                        // If a values finder is defined, use the label from the matching key/value pair instead.
                        if (attDefinition.getControl() != null && StringUtils.isNotBlank(attDefinition.getControl().getValuesFinderClass())) {
                            KeyValuesFinder valuesFinder = (KeyValuesFinder) Class.forName(attDefinition.getControl().getValuesFinderClass()).newInstance();
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
                        
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | RuntimeException e) {
                        replacement = KFSConstants.EMPTY_STRING;
                    }
                }
            } else {
                // Replace non-data-dictionary-defined property placeholders with an empty string.
                replacement = KFSConstants.EMPTY_STRING;
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
        note.setRemoteObjectIdentifier(document.getObjectId());
        note.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
        note.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
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
     * Accepts a file name and returns a byte-array of the file name contents, if possible.
     * 
     * Throws RuntimeExceptions if FileNotFound or IOExceptions occur.
     * 
     * This method has been copied from CustomerLoadServiceImpl, but has been tweaked
     * to properly close the file stream after use.
     * 
     * @param fileName String containing valid path & filename (relative or absolute) of file to load.
     * @return A Byte Array of the contents of the file.
     */
    protected byte[] safelyLoadFileBytes(String fileName) {
        InputStream fileContents;
        byte[] fileByteContent;
        
        try {
            fileContents = new FileInputStream(fileName);
        } catch (FileNotFoundException e1) {
            LOG.error("Batch file not found [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("Batch File not found [" + fileName + "]. " + e1.getMessage());
        }
        
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        } catch (IOException e1) {
            LOG.error("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
        } finally {
            IOUtils.closeQuietly(fileContents);
        }
        
        return fileByteContent;
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

    /**
     * Builds a document description for New or Edit PAAT documents generated by this class.
     * Will prefix the description with payee's principal name.
     */
    protected String getDocumentDescription(Person payee, boolean isEntityId, boolean isNewAccount) {
        int descMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(), KRADPropertyConstants.DOCUMENT_DESCRIPTION).intValue();
        StringBuilder docDescription = new StringBuilder(descMaxLength);
        
        docDescription.append(payee.getPrincipalName());
        docDescription.append(isNewAccount ? " -- New " : " -- Edit ");
        docDescription.append(isEntityId ? "Entity" : "Employee").append(" Account");
        
        return (descMaxLength < docDescription.length()) ? docDescription.substring(0, descMaxLength) : docDescription.toString();
    }

    /**
     * LOG error and throw RunTimeException
     * 
     * @param errorMessage
     */
    // Copied this method from CustomerLoadServiceImpl.
    private void criticalError(String errorMessage) {
        LOG.error(errorMessage);
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

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
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

    public void setAchService(AchService achService) {
        this.achService = achService;
    }

    public void setAchBankService(AchBankService achBankService) {
        this.achBankService = achBankService;
    }

}
