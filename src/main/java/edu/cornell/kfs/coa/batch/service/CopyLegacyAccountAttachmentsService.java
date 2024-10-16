package edu.cornell.kfs.coa.batch.service;

import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;

public interface CopyLegacyAccountAttachmentsService {

    boolean copyLegacyAccountAttachmentsToKfs();

    boolean copyLegacyAccountAttachmentToKfs(final LegacyAccountAttachment legacyAccountAttachment);

}
