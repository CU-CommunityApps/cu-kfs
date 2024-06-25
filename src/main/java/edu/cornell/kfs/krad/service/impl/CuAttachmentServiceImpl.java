package edu.cornell.kfs.krad.service.impl;

import edu.cornell.kfs.krad.dao.CuAttachmentDao;
import edu.cornell.kfs.krad.service.BlackListAttachmentService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.krad.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.antivirus.service.AntiVirusService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.impl.AttachmentServiceImpl;
import org.kuali.kfs.core.api.mo.common.GloballyUnique;
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
 * Added attachment file extension black list parameter processing.
 */
@Transactional
public class CuAttachmentServiceImpl extends AttachmentServiceImpl {

    private static final Logger LOG = LogManager.getLogger();

    private AntiVirusService antiVirusService;
    private NoteService noteService;
    private BlackListAttachmentService blackListAttachmentService;

    /**
     * Overridden to get attachment by attachment ID if necessary.
     */
    @Override
    public InputStream retrieveAttachmentContents(final Attachment attachment) throws IOException {
        if (attachment.getNoteIdentifier() != null) {
            attachment.refreshNonUpdateableReferences();
        }

        String parentDirectory = "";
        if (attachment.getNote() != null && attachment.getNote().getRemoteObjectIdentifier() != null) {
            parentDirectory = attachment.getNote().getRemoteObjectIdentifier();
        }

        if (StringUtils.isEmpty(parentDirectory)) {
            final Attachment attachmentTemp = getAttachmentByAttachmentId(attachment.getAttachmentIdentifier());
            if (attachmentTemp != null) {
                final Note nte = noteService.getNoteByNoteId(attachmentTemp.getNoteIdentifier());
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
     * @see org.kuali.kfs.krad.service.impl.AttachmentServiceImpl#createAttachment(GloballyUnique, String, String, int, InputStream, String)
     */
    @Override
    public Attachment createAttachment(
            final GloballyUnique parent, final String uploadedFileName, 
            final String mimeType, final int fileSize, 
            final InputStream fileContents, final String attachmentTypeCode) throws IOException {
        if(parent == null) {
            throw new IllegalArgumentException("invalid (null or uninitialized) document");
        } else if(StringUtils.isBlank(uploadedFileName)) {
            throw new IllegalArgumentException("invalid (blank) fileName");
        } else if (blackListAttachmentService.attachmentFileExtensionIsDisallowed(uploadedFileName)) {
            LOG.error("createAttachment: ATTACHMENTS WITH THIS EXTENSION ARE NOT ALLOWED!  uploadedFileName:{} "
                    + " mimeType:{}  attachmentTypeCode:{}  parent object:{} ", 
                        uploadedFileName, mimeType, attachmentTypeCode, parent);
            throw new IllegalArgumentException(CUKFSConstants.DISALLOWED_FILE_EXTENSION_MESSAGE + uploadedFileName);
        } else if(StringUtils.isBlank(mimeType)) {
            throw new IllegalArgumentException("invalid (blank) mimeType");
        } else if(fileSize <= 0) {
            throw new IllegalArgumentException("invalid (non-positive) fileSize");
        } else if(fileContents == null) {
            throw new IllegalArgumentException("invalid (null) inputStream");
        } else {
            byte[] fileContentsAsByteArray = IOUtils.toByteArray(fileContents);
            final InputStream fileContentsToScan = new ByteArrayInputStream(fileContentsAsByteArray);

            final ScanResult virusScanResults = antiVirusService.scan(fileContentsToScan);
            if (!ScanResult.Status.PASSED.equals(virusScanResults.getStatus())) {
                LOG.error("createAttachment, virus protection failure!  uploadedFileName: " + uploadedFileName 
                        + ", mimeType: " + mimeType + ", attachmentTypeCode: " + attachmentTypeCode + ", scan result: " + 
                                virusScanResults.getStatus() + ", parent object: " + parent);
                throw new IllegalArgumentException(CUKFSConstants.ANTIVIRUS_FAILED_MESSAGE);
            } else {
                final InputStream fileContentsForAttachment = new ByteArrayInputStream(fileContentsAsByteArray);
                return super.createAttachment(parent, uploadedFileName, mimeType, fileSize, fileContentsForAttachment, attachmentTypeCode);
            }
        }
    }

    public Attachment getAttachmentByAttachmentId(final String attachmentIdentifier) {
        if (attachmentIdentifier == null) {
            return null;
        }

        return ((CuAttachmentDao)getAttachmentDao()).getAttachmentByAttachmentId(attachmentIdentifier);
    }

    public AntiVirusService getAntiVirusService() {
        return antiVirusService;
    }

    public void setAntiVirusService(final AntiVirusService antiVirusService) {
        this.antiVirusService = antiVirusService;
    }

    public NoteService getNoteService() {
        return noteService;
    }

    public void setNoteService(final NoteService noteService) {
        this.noteService = noteService;
    }

    public BlackListAttachmentService getBlackListAttachmentService() {
        return blackListAttachmentService;
    }

    public void setBlackListAttachmentService(BlackListAttachmentService blackListAttachmentService) {
        this.blackListAttachmentService = blackListAttachmentService;
    }
}