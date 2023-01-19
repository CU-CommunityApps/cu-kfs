package edu.cornell.kfs.pdp.batch.service.impl;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.businessobject.options.StandardEntryClassValuesFinder;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.CUPdpPropertyConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractStep;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountDocumentService;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public class PayeeACHAccountDocumentServiceImpl implements PayeeACHAccountDocumentService {
    private static final Logger LOG = LogManager.getLogger();
    
    private ConfigurationService configurationService;
    private DataDictionaryService dataDictionaryService;
    private DocumentService documentService;
    private EmailService emailService;
    private ParameterService parameterService;
    private PersonService personService;
    private SequenceAccessorService sequenceAccessorService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public String addACHAccount(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType) {
        if (ObjectUtils.isNotNull(payee)) {
            LOG.info("addACHAccount, adding account for " + payee.getName());
        }
        PayeeACHData achData = new PayeeACHData(payee, achDetail, payeeType);
        return createAndRoutePayeeACHAccountDocument(achData, this::setupDocumentForACHCreate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public String updateACHAccountIfNecessary(Person payee, PayeeACHAccountExtractDetail achDetail,
            PayeeACHAccount achAccount) {
        if (ObjectUtils.isNotNull(payee)) {
            LOG.info("updateACHAccountIfNecessary, updating account for " + payee.getName());
        }
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
    
    @Override
    public String getDirectDepositTransactionType() {
        return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE);
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
        achAccount.setStandardEntryClass(determineStandardEntryClass(achAccount.getBankAccountTypeCode()));
        if (StringUtils.isNotBlank(payee.getNameUnmasked())) {
            achAccount.setPayeeName(payee.getNameUnmasked());
        }
        if (StringUtils.isNotBlank(payee.getEmailAddressUnmasked())) {
            achAccount.setPayeeEmailAddress(payee.getEmailAddressUnmasked());
        }
        achAccount.setActive(true);
    }
    
    protected String getPayeeACHAccountExtractParameter(String parameterName) {
        return parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class, parameterName);
    }
    
    protected String getACHTransactionCode(String workdayAccountType) {
        if (CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE.equalsIgnoreCase(workdayAccountType)) {
            return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_PERSONAL_CHECKING_TRANSACTION_CODE);
        } else if (CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE.equalsIgnoreCase(workdayAccountType)) {
            return getPayeeACHAccountExtractParameter(CUPdpParameterConstants.ACH_PERSONAL_SAVINGS_TRANSACTION_CODE);
        } else {
            throw new IllegalArgumentException("Unrecognized account type from file: " + workdayAccountType);
        }
    }
    
    protected String determineStandardEntryClass(String achTransactionCode) {
        if (StringUtils.equalsIgnoreCase(StandardEntryClassValuesFinder.StandardEntryClass.PPD.name(),
                (StringUtils.right(achTransactionCode, 3)))) {
            return StandardEntryClassValuesFinder.StandardEntryClass.PPD.name();
            
        } else if (StringUtils.equalsIgnoreCase(StandardEntryClassValuesFinder.StandardEntryClass.CTX.name(),
                (StringUtils.right(achTransactionCode, 3)))) {
            return StandardEntryClassValuesFinder.StandardEntryClass.CTX.name();
            
        } else if (StringUtils.equalsIgnoreCase(StandardEntryClassValuesFinder.StandardEntryClass.CCD.name(),
                (StringUtils.right(achTransactionCode, 3)))) {
            return StandardEntryClassValuesFinder.StandardEntryClass.CCD.name();
            
        } else {
            throw new IllegalArgumentException("Unrecognized ACH transaction code from file: " + achTransactionCode);
        }
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
    
    protected void sendPayeeACHAccountAddOrUpdateEmail(PayeeACHAccount achAccount, Person payee, String emailSubject, String emailBody) {
        if (StringUtils.isBlank(payee.getEmailAddressUnmasked())) {
            LOG.warn("Payee " + payee.getPrincipalName() + " has no email address defined in KFS. No notification emails will be sent for this user.");
            return;
        }
        
        // Construct mail message, and replace property placeholders and literal "\n" strings in the email body accordingly.
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, KFSConstants.FROM_EMAIL_ADDRESS_PARAM_NM));
        message.setSubject(emailSubject);
        message.setMessage(getResolvedEmailBody(achAccount, emailBody));
        message.addToAddress(payee.getEmailAddressUnmasked());
        
        // Send the message.
        emailService.sendMessage(message, false);        
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
        newAccount.setStandardEntryClass(determineStandardEntryClass(newAccount.getBankAccountTypeCode()));
        newAccount.setActive(true);
        
        paatDocument.getOldMaintainableObject().setDataObject(oldAccount);
        paatDocument.getNewMaintainableObject().setDataObject(newAccount);
        paatDocument.getNewMaintainableObject().setMaintenanceAction(KFSConstants.MAINTENANCE_EDIT_ACTION);
    }
    
    
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setSequenceAccessorService(SequenceAccessorService sequenceAccessorService) {
        this.sequenceAccessorService = sequenceAccessorService;
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
