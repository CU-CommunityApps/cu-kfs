package edu.cornell.kfs.coa.batch.service;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;

public interface CopyLegacyAccountAttachmentsService {

    boolean copyLegacyAccountAttachmentsToKfs();

    void copyLegacyAccountAttachmentToKfs(final LegacyAccountAttachment legacyAccountAttachment);

    void recordCopyingErrorsForLegacyAccountAttachments(
            final List<Pair<LegacyAccountAttachment, String>> attachmentsWithErrors);

}
