package edu.cornell.kfs.coa.batch.dataaccess;

import java.util.List;

import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
import edu.cornell.kfs.coa.batch.businessobject.RemappedAccountAttachment;

public interface CopyLegacyAccountAttachmentsDao {

    List<LegacyAccountAttachment> getLegacyAccountAttachmentsToCopy(final int fetchSize, final int maxRetryCount);

    void markLegacyAccountAttachmentAsCopied(final LegacyAccountAttachment legacyAccountAttachment);

    void recordCopyingErrorForLegacyAccountAttachment(final LegacyAccountAttachment legacyAccountAttachment,
            final String errorMessage);

    List<RemappedAccountAttachment> getRemappedAccountAttachments();

}
