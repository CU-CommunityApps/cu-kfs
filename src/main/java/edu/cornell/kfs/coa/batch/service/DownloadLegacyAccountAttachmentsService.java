package edu.cornell.kfs.coa.batch.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.springframework.core.io.buffer.DataBuffer;

import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;

public interface DownloadLegacyAccountAttachmentsService {

    void downloadAndProcessLegacyAccountAttachment(final LegacyAccountAttachment legacyAccountAttachment,
            final FailableBiConsumer<LegacyAccountAttachment, DataBuffer, IOException> attachmentProcessor)
            throws IOException, URISyntaxException;

}
