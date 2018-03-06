package edu.cornell.kfs.module.purap.document.service.impl;

import edu.cornell.kfs.module.purap.CUPurapPropertyConstants;
import edu.cornell.kfs.module.purap.document.service.CuB2BShoppingErrorEmailService;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import java.util.HashSet;
import java.util.Set;

public class CuB2BShoppingErrorEmailServiceImpl implements CuB2BShoppingErrorEmailService {

    private EmailService emailService;
    private ConfigurationService kualiConfigurationService;

    public void sendDuplicateRequisitionAccountErrorEmail(RequisitionDocument requisitionDocument, String errorMessage) {
        BodyMailMessage message = new BodyMailMessage();

        message.setFromAddress(emailService.getDefaultFromAddress());
        message.setSubject(kualiConfigurationService.getPropertyValueAsString(CUPurapPropertyConstants.REQS_DUPLICATE_ACCT_ERROR_EMAIL_SUBJECT));
        message.setToAddresses(getToAddresses(requisitionDocument));
        message.setMessage(errorMessage);

        emailService.sendMessage(message, false);
    }

    protected Set<String> getToAddresses(RequisitionDocument requisitionDocument) {
        Set<String> addresses = new HashSet<>();
        addresses.add(requisitionDocument.getRequestorPersonEmailAddress());
        return addresses;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }

    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }
}

