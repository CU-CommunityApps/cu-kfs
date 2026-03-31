package edu.cornell.kfs.krad.service;

import org.kuali.kfs.krad.service.AttachmentService;

import edu.cornell.kfs.coa.batch.businessobject.RemappedAccountAttachment;

public interface CuAttachmentService extends AttachmentService {

    boolean fixRemappedAttachmentIfPossible(final RemappedAccountAttachment remappedAttachment);

}
