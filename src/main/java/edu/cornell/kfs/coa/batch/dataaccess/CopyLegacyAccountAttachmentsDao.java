package edu.cornell.kfs.coa.batch.dataaccess;

import java.util.List;

import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;

public interface CopyLegacyAccountAttachmentsDao {

    List<LegacyAccountAttachment> getLegacyAccountAttachmentsToCopy(final int fetchSize, final int maxRetryCount);

    void markLegacyAccountAttachmentsAsCopied(final List<LegacyAccountAttachment> legacyAccountAttachments);

    void incrementRetryCountsOnLegacyAccountAttachments(final List<LegacyAccountAttachment> legacyAccountAttachments);

}
