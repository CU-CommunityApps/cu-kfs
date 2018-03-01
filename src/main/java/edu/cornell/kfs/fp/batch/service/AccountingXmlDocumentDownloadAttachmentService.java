package edu.cornell.kfs.fp.batch.service;

import java.io.IOException;

import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.document.Document;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;

public interface AccountingXmlDocumentDownloadAttachmentService {

    public Attachment createAttachmentFromBackupLink(Document document,
            AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink);

}
