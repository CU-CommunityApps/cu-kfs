package edu.cornell.kfs.module.ar.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.impl.AREmailServiceImpl;

import java.text.MessageFormat;

public class CuAREmailServiceImpl extends AREmailServiceImpl {
    
    @Override
    protected String getSubject(ContractsGrantsInvoiceDocument invoice) {
        String grantNumber = invoice.getInvoiceGeneralDetail().getAward().getProposal().getGrantNumber();
        String subject;
        String message;
        if (StringUtils.isBlank(grantNumber)) {
            subject = kualiConfigurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_EMAIL_SUBJECT_NO_GRANT_NUMBER);
            message = MessageFormat.format(subject, invoice.getDocumentNumber(), 
                    invoice.getInvoiceGeneralDetail().getAward().getProposal().getProposalNumber());
        } else {
            subject = kualiConfigurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_EMAIL_SUBJECT);
            message = MessageFormat.format(subject, invoice.getDocumentNumber(), 
                    invoice.getInvoiceGeneralDetail().getAward().getProposal().getGrantNumber(), 
                    invoice.getInvoiceGeneralDetail().getAward().getProposal().getProposalNumber());
        }
        return message;
    }
    
    @Override
    protected String getMessageBody(ContractsGrantsInvoiceDocument invoice, CustomerAddress customerAddress) {
        String message = kualiConfigurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_EMAIL_BODY);

        return MessageFormat.format(message, customerAddress.getCustomer().getCustomerName(),
            invoice.getInvoiceGeneralDetail().getAward().getAwardPrimaryFundManager().getFundManager().getName(),
            invoice.getInvoiceGeneralDetail().getAward().getAwardPrimaryFundManager().getFundManager().getPhoneNumber(),
            invoice.getInvoiceGeneralDetail().getAward().getAwardPrimaryFundManager().getFundManager().getEmailAddress());
    }

}
