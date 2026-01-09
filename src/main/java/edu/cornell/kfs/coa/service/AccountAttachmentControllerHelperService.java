package edu.cornell.kfs.coa.service;

import org.kuali.kfs.krad.bo.Attachment;

import edu.cornell.kfs.coa.rest.jsonObjects.AccountAttachmentListingDto;

public interface AccountAttachmentControllerHelperService {

    AccountAttachmentListingDto getAccountAttachmentListing(final String chartCode, final String accountNumber);

    Attachment getAccountAttachment(final String chartCode, final String accountNumber, final String attachmentId);

}
