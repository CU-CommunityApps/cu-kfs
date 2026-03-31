package edu.cornell.kfs.coa.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.coa.batch.service.CopyLegacyAccountAttachmentsService;

public class FixRemappedAccountAttachmentsStep extends AbstractStep {

    private CopyLegacyAccountAttachmentsService copyLegacyAccountAttachmentsService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        copyLegacyAccountAttachmentsService.moveFileContentsForRemappedAttachments();
        return true;
    }

    public void setCopyLegacyAccountAttachmentsService(
            final CopyLegacyAccountAttachmentsService copyLegacyAccountAttachmentsService) {
        this.copyLegacyAccountAttachmentsService = copyLegacyAccountAttachmentsService;
    }

}
