package edu.cornell.kfs.krad.service.impl;

import edu.cornell.cynergy.antivirus.service.AntiVirusService;
import edu.cornell.cynergy.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.dao.CuAttachmentDao;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.impl.AttachmentServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Custom subclass of AttachmentServiceImpl featuring the following enhancements:
 * Added anti-virus scanning for attachments.
 * Added the ability to retrieve attachments by attachment ID.
 *
 * This is very similar to the CynergyAttachmentServiceImpl, but needed since KFS
 * now has it's own AttachmentService separate from Rice.
 */
@Transactional
public class CuAttachmentServiceImpl extends AttachmentServiceImpl {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAttachmentServiceImpl.class);

    private AntiVirusService antiVirusService;
    private NoteService noteService;

    /**
     * Overridden to get attachment by attachment ID if necessary.
     *
     * @see org.kuali.kfs.krad.service.impl.AttachmentServiceImpl#retrieveAttachmentContents(Attachment)
     */
    @Override
    public InputStream retrieveAttachmentContents(Attachment attachment) throws IOException {
        if (attachment.getNoteIdentifier() != null) {
            attachment.refreshNonUpdateableReferences();
        }

        String parentDirectory = "";
        if (attachment.getNote() != null && attachment.getNote().getRemoteObjectIdentifier() != null) {
            parentDirectory = attachment.getNote().getRemoteObjectIdentifier();
        }

        if (StringUtils.isEmpty(parentDirectory)) {
            Attachment attachmentTemp = getAttachmentByAttachmentId(attachment.getAttachmentIdentifier());
            if (attachmentTemp != null) {
                Note nte = noteService.getNoteByNoteId(attachmentTemp.getNoteIdentifier());
                if (nte != null) {
                    parentDirectory = nte.getRemoteObjectIdentifier();
                }
            }
        }

        return new BufferedInputStream(new FileInputStream(getDocumentDirectory(parentDirectory) + File.separator + attachment.getAttachmentIdentifier()));
    }

    /**
     * Overridden to add anti-virus scanning.
     *
     * @see org.kuali.kfs.krad.service.impl.AttachmentServiceImpl#createAttachment(PersistableBusinessObject, String, String, int, InputStream, String)
     */
    public Attachment createAttachment(PersistableBusinessObject parent, String uploadedFileName, String mimeType, int fileSize, InputStream fileContents, String attachmentTypeCode) throws IOException {
        if(parent == null) {
            throw new IllegalArgumentException("invalid (null or uninitialized) document");
        } else if(StringUtils.isBlank(uploadedFileName)) {
            throw new IllegalArgumentException("invalid (blank) fileName");
        } else if(StringUtils.isBlank(mimeType)) {
            throw new IllegalArgumentException("invalid (blank) mimeType");
        } else if(fileSize <= 0) {
            throw new IllegalArgumentException("invalid (non-positive) fileSize");
        } else if(fileContents == null) {
            throw new IllegalArgumentException("invalid (null) inputStream");
        } else {
            byte[] fileContentsAsByteArray = IOUtils.toByteArray(fileContents);
            InputStream fileContentsToScan = new ByteArrayInputStream(fileContentsAsByteArray);
            ScanResult scnResults = antiVirusService.scan(fileContentsToScan);
            System.err.println("scnResults: " +scnResults);
            if (!ScanResult.Status.PASSED.equals(scnResults.getStatus())) {
                throw new IllegalArgumentException("file contents failed virus scan");
            } else {
                InputStream fileContentsForAttachment = new ByteArrayInputStream(fileContentsAsByteArray);
                return super.createAttachment(parent, uploadedFileName, mimeType, fileSize, fileContentsForAttachment, attachmentTypeCode);
            }
        }
    }

    public Attachment getAttachmentByAttachmentId(String attachmentIdentifier) {
        if (attachmentIdentifier == null) {
            return null;
        }

        return ((CuAttachmentDao)getAttachmentDao()).getAttachmentByAttachmentId(attachmentIdentifier);
    }

    public AntiVirusService getAntiVirusService() {
        return antiVirusService;
    }

    public void setAntiVirusService(AntiVirusService antiVirusService) {
        this.antiVirusService = antiVirusService;
    }

    public NoteService getNoteService() {
        return noteService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }
}