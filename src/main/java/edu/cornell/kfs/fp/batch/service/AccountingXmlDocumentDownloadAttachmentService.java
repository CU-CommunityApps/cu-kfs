package edu.cornell.kfs.fp.batch.service;

import org.kuali.kfs.core.api.mo.common.GloballyUnique;
import org.kuali.kfs.krad.bo.Attachment;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;

public interface AccountingXmlDocumentDownloadAttachmentService {

    public Attachment createAttachmentFromBackupLink(GloballyUnique parentObject,
            AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink);

}
