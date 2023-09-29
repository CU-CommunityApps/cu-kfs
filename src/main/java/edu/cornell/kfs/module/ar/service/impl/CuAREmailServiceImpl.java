package edu.cornell.kfs.module.ar.service.impl;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.impl.AREmailServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kim.impl.identity.Person;

public class CuAREmailServiceImpl extends AREmailServiceImpl {
    
    @Override
    protected String getSubject(final ContractsGrantsInvoiceDocument invoice) {
        final String grantNumber = invoice.getInvoiceGeneralDetail().getAward().getProposal().getGrantNumber();
        String subject;
        String message;
        if (StringUtils.isBlank(grantNumber)) {
            subject = kualiConfigurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_EMAIL_SUBJECT_NO_GRANT_NUMBER);
            message = MessageFormat.format(subject, invoice.getDocumentNumber(), 
                    invoice.getInvoiceGeneralDetail().getAward().getProposal().getProposalNumber());
        } else {
            subject = kualiConfigurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_EMAIL_SUBJECT);
            ContractsAndGrantsBillingAward award = invoice.getInvoiceGeneralDetail().getAward();
            message = MessageFormat.format(subject, invoice.getDocumentNumber(), 
                    award.getProposal().getGrantNumber(), 
                    award.getProposal().getProposalNumber(),
                    award.getAwardPrimaryProjectDirector().getProjectDirector().getName());
        }
        return message;
    }
    
    @Override
    protected String getMessageBody(final ContractsGrantsInvoiceDocument invoice, final CustomerAddress customerAddress) {
        final String message = kualiConfigurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_EMAIL_BODY);

        final Person fundManager = invoice.getInvoiceGeneralDetail().getAward().getAwardPrimaryFundManager().getFundManager();
        return MessageFormat.format(message, customerAddress.getCustomerAddressName(),
            fundManager.getFirstName() + KFSConstants.BLANK_SPACE + fundManager.getLastName(), fundManager.getPhoneNumber(),
            fundManager.getEmailAddress());
    }

}
