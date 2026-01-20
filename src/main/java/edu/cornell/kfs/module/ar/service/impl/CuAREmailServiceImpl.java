package edu.cornell.kfs.module.ar.service.impl;

import java.text.MessageFormat;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.module.ar.businessobject.InvoiceAddressDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.impl.AREmailServiceImpl;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kim.impl.identity.Person;

public class CuAREmailServiceImpl extends AREmailServiceImpl {
    
    @Override
    protected String getSubject(final ContractsGrantsInvoiceDocument invoice) {
        final String grantNumber = invoice.getInvoiceGeneralDetail().getAward().getProposalNumber();
        String subject;
        String message;
        if (StringUtils.isBlank(grantNumber)) {
            subject = kualiConfigurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_EMAIL_SUBJECT_NO_GRANT_NUMBER);
            message = MessageFormat.format(subject, invoice.getDocumentNumber(), 
                    invoice.getInvoiceGeneralDetail().getAward().getProposal().getProposalNumber());
        } else {
            subject = kualiConfigurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_EMAIL_SUBJECT);
            Award award = invoice.getInvoiceGeneralDetail().getAward();
            message = MessageFormat.format(subject, invoice.getDocumentNumber(), 
                    award.getGrantNumber(), 
                    award.getProposalNumber());
        }
        return message;
    }
    
    @Override
    protected String getMessageBody(final ContractsGrantsInvoiceDocument invoice, final InvoiceAddressDetail invoiceAddressDetail, final CustomerAddress customerAddress) {
        final String message = kualiConfigurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_EMAIL_BODY);

        final Person fundManager = invoice.getInvoiceGeneralDetail().getAward().getAwardPrimaryFundManager().getFundManager();
        final String ccRecipients = invoice
                .getInvoiceAddressDetails().stream().filter(
                        e -> !e.equals(invoiceAddressDetail)
                                && ArConstants.InvoiceTransmissionMethod.EMAIL.equals(e.getInvoiceTransmissionMethodCode())
                                && (e.isSent() || e.isQueued()))
                .map(InvoiceAddressDetail::getCustomerEmailAddress).collect(Collectors.joining(", "));
        return MessageFormat.format(message, customerAddress.getCustomerAddressName(),
            fundManager.getFirstName() + KFSConstants.BLANK_SPACE + fundManager.getLastName(), fundManager.getPhoneNumber(),
            fundManager.getEmailAddress(),
            StringUtils.isNotBlank(ccRecipients) ? "CC: " + ccRecipients : "");
    }

}
