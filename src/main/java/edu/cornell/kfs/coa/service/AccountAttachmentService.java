package edu.cornell.kfs.coa.service;

import org.kuali.kfs.krad.bo.Attachment;

import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListingDto;

public interface AccountAttachmentService {

    AccountAttachmentListingDto getAccountAttachmentListing(
            final String chartOfAccountsCode, final String accountNumber);

    Attachment getAccountAttachment(
            final String chartOfAccountsCode, final String accountNumber, final String attachmentId);

}
