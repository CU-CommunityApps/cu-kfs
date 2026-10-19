package edu.cornell.kfs.krad.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.mo.common.GloballyUnique;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.impl.AmazonS3AttachmentServiceImpl;

import edu.cornell.kfs.krad.antivirus.service.AntiVirusService;
import edu.cornell.kfs.krad.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.service.BlackListAttachmentService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuAmazonS3AttachmentServiceImpl extends AmazonS3AttachmentServiceImpl {

    private static final Logger LOG = LogManager.getLogger();

    private final AntiVirusService antiVirusService;
    private final BlackListAttachmentService blackListAttachmentService;

    public CuAmazonS3AttachmentServiceImpl(
            final String bucketName,
            final BusinessObjectService businessObjectService,
            final AntiVirusService antiVirusService,
            final BlackListAttachmentService blackListAttachmentService
    ) {
        super(bucketName, businessObjectService);
        this.antiVirusService = antiVirusService;
        this.blackListAttachmentService = blackListAttachmentService;
    }

    @Override
    public Attachment createAttachment(
            final GloballyUnique parent,
            final String uploadedFileName,
            final String mimeType,
            final int fileSize,
            final InputStream fileContents,
            final String attachmentType
    ) throws IOException {
        Validate.isTrue(parent != null, "invalid (null or uninitialized) document");
        Validate.isTrue(fileContents != null, "invalid (null) inputStream");
        Validate.isTrue(StringUtils.isNotBlank(uploadedFileName), "invalid (blank) fileName");
        Validate.isTrue(StringUtils.isNotBlank(mimeType), "invalid (blank) mimeType");
        Validate.isTrue(fileSize > 0, "invalid (non-positive) fileSize");

        if (blackListAttachmentService.attachmentFileExtensionIsDisallowed(uploadedFileName)) {
            LOG.error("createAttachment: ATTACHMENTS WITH THIS EXTENSION ARE NOT ALLOWED!  uploadedFileName:{} "
                    + " mimeType:{}  attachmentType:{}  parent object:{} ", 
                        uploadedFileName, mimeType, attachmentType, parent);
            throw new IllegalArgumentException(CUKFSConstants.DISALLOWED_FILE_EXTENSION_MESSAGE + uploadedFileName);
        }

        final byte[] fileContentBytes = IOUtils.toByteArray(fileContents);
        final ScanResult virusScanResults = antiVirusService.scan(fileContentBytes);
        if (!ScanResult.Status.PASSED.equals(virusScanResults.getStatus())) {
            LOG.error("createAttachment, virus protection failure!  uploadedFileName: {}, mimeType: {},"
                    + " attachmentType: {}, scan result: {}, parent object: {}",
                        uploadedFileName, mimeType, attachmentType, virusScanResults.getStatus(), parent);
            throw new IllegalArgumentException(CUKFSConstants.ANTIVIRUS_FAILED_MESSAGE);
        }

        try (
            final InputStream fileBytesStream = new ByteArrayInputStream(fileContentBytes);
        ) {
            return super.createAttachment(parent, uploadedFileName, mimeType, fileSize, fileBytesStream, attachmentType);
        }
    }

}
