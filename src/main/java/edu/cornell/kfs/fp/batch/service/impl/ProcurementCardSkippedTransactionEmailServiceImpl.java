package edu.cornell.kfs.fp.batch.service.impl;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.ProcurementCardSkippedTransaction;
import edu.cornell.kfs.fp.batch.service.ProcurementCardSkippedTransactionEmailService;

public class ProcurementCardSkippedTransactionEmailServiceImpl implements ProcurementCardSkippedTransactionEmailService {
    private static final Logger LOG = LogManager.getLogger(ProcurementCardSkippedTransactionEmailServiceImpl.class);
    protected ParameterService parameterService;
    protected EmailService emailService;
    private String messageSubject;
    private String bankFileType;

    @Override
    public void sendSkippedTransactionEmail(List<ProcurementCardSkippedTransaction> skippedTransactions) {
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(emailService.getDefaultFromAddress());
        LOG.info("sendSkippedTransactionEmail, the email subject: " + messageSubject);
        message.setSubject(messageSubject);
        message.setToAddresses(getToAddresses());
        String emailMessage = buildEmailMessage(skippedTransactions);
        LOG.info("sendSkippedTransactionEmail, the email message: " + emailMessage);
        message.setMessage(emailMessage);
        emailService.sendMessage(message, false);
    }
    
    protected Set<String> getToAddresses() {
        Set<String> addresses = new HashSet<String>();
        addresses.add(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                KFSConstants.ProcurementCardParameters.PCARD_BATCH_LOAD_STEP, CuFPParameterConstants.ProcurementCardDocument.CARD_TRANSACTIONS_SKIPPED_EMAIL_ADDRESS));
        return addresses;
    }
    
    protected String buildEmailMessage(List<ProcurementCardSkippedTransaction> skippedTransactions) {
        StringBuilder sb = new StringBuilder();
        //sb.append("The ").append(bankFileType).append(" bank file had the following transactions skipped: ");
        String body = MessageFormat.format(getEmailBodyTempalte(), bankFileType);
        sb.append(body).append(KFSConstants.NEWLINE).append(KFSConstants.NEWLINE);
        
        for (ProcurementCardSkippedTransaction skippedTransaction : skippedTransactions) {
            sb.append("File Line Number: ").append(skippedTransaction.getFileLineNumber());
            sb.append(" Card Holder Name: ").append(skippedTransaction.getCardHolderName());
            sb.append(" Transaction Amount: ").append(skippedTransaction.getTransactionAmount());
            sb.append(KFSConstants.NEWLINE);
        }
        
        return sb.toString();
    }
    
    protected String getEmailBodyTempalte() {
        return parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                KFSConstants.ProcurementCardParameters.PCARD_BATCH_LOAD_STEP, CuFPParameterConstants.ProcurementCardDocument.CARD_TRANSACTIONS_SKIPPED_EMAIL_BODY_TEMPLATE);
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public String getMessageSubject() {
        return messageSubject;
    }

    public void setMessageSubject(String messageSubject) {
        this.messageSubject = messageSubject;
    }

    public String getBankFileType() {
        return bankFileType;
    }

    public void setBankFileType(String bankFileType) {
        this.bankFileType = bankFileType;
    }

}
