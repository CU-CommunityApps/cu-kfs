package edu.cornell.kfs.fp.batch.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Locale;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilderException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.mo.common.GloballyUnique;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.AttachmentService;
import org.springframework.beans.factory.DisposableBean;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.service.impl.DisposableClientServiceImplBase;

public class AccountingXmlDocumentDownloadAttachmentServiceImpl extends DisposableClientServiceImplBase implements AccountingXmlDocumentDownloadAttachmentService, DisposableBean {
    private static final Logger LOG = LogManager.getLogger(AccountingXmlDocumentDownloadAttachmentServiceImpl.class);
    private static final String UNABLE_TO_DOWNLOAD_ATTACHMENT_MESSAGE = "Unable to download attachment: ";
    private static final String SSL_HANDSHAKE_ERROR_MESSAGE = "When attempting to download the backup documentation url %s, the following error was encountered. Please contact your IT support.\n%s";

    protected AttachmentService attachmentService;
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public Attachment createAttachmentFromBackupLink(GloballyUnique parentObject, AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink) {
        if (StringUtils.isBlank(accountingXmlDocumentBackupLink.getCredentialGroupCode())) {
            LOG.error("createAttachmentFromBackupLink, the Credential Group Code is blank");
            throw new ValidationException("Unable to download attachment with blank Credential Group Code: " + accountingXmlDocumentBackupLink.getLinkUrl());
        }
        try {
            byte[] formFile = downloadByteArray(accountingXmlDocumentBackupLink);

            if (formFile.length > 0) {
                String uploadFileName = accountingXmlDocumentBackupLink.getFileName();
                String mimeType = findMimeType(uploadFileName);
                int fileSize = (int) formFile.length;
                String attachmentType = null;

                if (LOG.isDebugEnabled()) {
                    LOG.debug("createAttachmentFromBackupLink, uploadFileName: " + uploadFileName + " mimeType: " + mimeType
                            + " fileSize: " + fileSize);
                }
                InputStream inputStream = new ByteArrayInputStream(formFile);

                Attachment attachment = attachmentService.createAttachment(parentObject, uploadFileName, mimeType, fileSize, inputStream, attachmentType);
                return attachment;

            } else {
                LOG.error("createAttachmentFromBackupLink, the form file is NULL");
                throw new ValidationException(UNABLE_TO_DOWNLOAD_ATTACHMENT_MESSAGE + accountingXmlDocumentBackupLink.getLinkUrl());
            }
        } catch (IOException e) {
            LOG.error("createAttachmentFromBackupLink, Unable to download attachment: " + accountingXmlDocumentBackupLink.getLinkUrl(), e);
            throw new ValidationException(UNABLE_TO_DOWNLOAD_ATTACHMENT_MESSAGE + accountingXmlDocumentBackupLink.getLinkUrl());
        } catch (IllegalArgumentException iae) {
            if (StringUtils.equalsIgnoreCase(CUKFSConstants.ANTIVIRUS_FAILED_MESSAGE, iae.getMessage())) {
                throw new ValidationException("Unable to download attachment due to failing antivirus scan: " + accountingXmlDocumentBackupLink.getLinkUrl());
            } else {
                LOG.error("createAttachmentFromBackupLink, Unable to download attachment due to illegal argument exception: " + accountingXmlDocumentBackupLink.getLinkUrl(), iae);
                throw new ValidationException(UNABLE_TO_DOWNLOAD_ATTACHMENT_MESSAGE + accountingXmlDocumentBackupLink.getLinkUrl());
            }
        } catch (ProcessingException processingException) {
            LOG.error("createAttachmentFromBackupLink, Unable to download attachment: " + accountingXmlDocumentBackupLink.getLinkUrl(), processingException);

            String errorMessage = String.format(SSL_HANDSHAKE_ERROR_MESSAGE, accountingXmlDocumentBackupLink.getLinkUrl(), processingException.getMessage());
            throw new ValidationException(errorMessage);
        }
    }

    protected String findMimeType(String uploadFileName) {
        if (StringUtils.isBlank(uploadFileName)) {
            return StringUtils.EMPTY;
        }
        String mimeType = URLConnection.guessContentTypeFromName(uploadFileName);
        if (StringUtils.isBlank(mimeType)) {
            LOG.error("findMimeType, could not determine mime type from file name using URLConnection object. the file name is " + uploadFileName);
            String[] splitFileName = StringUtils.split(uploadFileName, ".");
            if (splitFileName != null) {
                int lastArrayElementIndex = splitFileName.length - 1;
                if (lastArrayElementIndex > -1) {
                    mimeType = splitFileName[lastArrayElementIndex];
                    LOG.info("findMimeType, determined mime type from file name's last extension value: " + mimeType);
                } else {
                    LOG.error("findMimeType, could not parse the file name, setting mime type to empty string");
                    mimeType = StringUtils.EMPTY;
                }
            }
        }
        return StringUtils.lowerCase(mimeType, Locale.US);
    }

    protected byte[] downloadByteArray(AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink) throws IOException {
        Collection<WebServiceCredential> credentials = getWebServiceCredentials(accountingXmlDocumentBackupLink);
        try {
            if (isLinkUrlValidForGroupCode(accountingXmlDocumentBackupLink, credentials)) {
                Invocation request = buildClientRequest(accountingXmlDocumentBackupLink.getLinkUrl(), credentials);
                return downloadByteArrayFromWebResource(accountingXmlDocumentBackupLink.getFileName(), request);
            } else {
                LOG.error("downloadByteArray, the group code isn't valid for the link URL");
                throw new ValidationException("The group code: " + accountingXmlDocumentBackupLink.getCredentialGroupCode()
                                + " isn't valid for the link URL: " + accountingXmlDocumentBackupLink.getLinkUrl());
            }
        } catch (URISyntaxException | UriBuilderException e) {
            LOG.error("downloadByteArray, the URL has an incorrect syntax", e);
            throw new ValidationException("The URL has an incorrect syntax: " + accountingXmlDocumentBackupLink.getLinkUrl());
        }
    }
    
    protected boolean isLinkUrlValidForGroupCode(AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink, Collection<WebServiceCredential> credentials) {
        if (CollectionUtils.isEmpty(credentials)) {
            LOG.error("isLinkUrlValidForGroupCode, no credential values for group code, so link is invalid");
            return false;
        } else {
            for (WebServiceCredential cred : credentials) {
                if (isCredentialUsedForValidatingBackupLinkURL(cred) && isCredentialBaseURLInBackupLinkURL(accountingXmlDocumentBackupLink, cred)) {
                    LOG.debug("isLinkUrlValidForGroupCode, found a CREDENTIAL_BASE_URL in the credentials table that is in the back up link URL");
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isCredentialBaseURLInBackupLinkURL(AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink, WebServiceCredential cred) {
        return StringUtils.containsIgnoreCase(accountingXmlDocumentBackupLink.getLinkUrl(), cred.getCredentialValue());
    }

    protected boolean isCredentialUsedForValidatingBackupLinkURL(WebServiceCredential cred) {
        return StringUtils.startsWithIgnoreCase(cred.getCredentialKey(), CuFPConstants.CREDENTIAL_BASE_URL);
    }

    protected Collection<WebServiceCredential> getWebServiceCredentials(AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink) {
        Collection<WebServiceCredential> webServiceCredentials = webServiceCredentialService
                .getWebServiceCredentialsByGroupCode(accountingXmlDocumentBackupLink.getCredentialGroupCode());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWebServiceCredentials, the group code is " + accountingXmlDocumentBackupLink.getCredentialGroupCode() + " for the file " + 
                    accountingXmlDocumentBackupLink.getFileName());
            if (CollectionUtils.isNotEmpty(webServiceCredentials)) {
                webServiceCredentials.stream().forEach(cred -> LOG.debug("getWebServiceCredentials, found a credential key: " + cred.getCredentialKey()));
            } else {
                LOG.debug("getWebServiceCredentials, no credentials found");
            }
        }
        return webServiceCredentials;
    }

    protected Invocation buildClientRequest(String url, Collection<WebServiceCredential> creds) throws URISyntaxException {
        URI uri = new URI(url);
        Builder builder = getClient().target(uri).request();
        if (CollectionUtils.isNotEmpty(creds)) {
            for (WebServiceCredential cred : creds) {
                if (!isCredentialUsedForValidatingBackupLinkURL(cred)) {
                    builder.header(cred.getCredentialKey(), cred.getCredentialValue());
                }
            }
        }
        return builder.buildGet();
    }

    protected byte[] downloadByteArrayFromWebResource(String fileName, Invocation request) throws IOException {
        byte[] byteArray;
        Response response = request.invoke();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            InputStream is = null;
            try {
                is = response.readEntity(InputStream.class);
                byteArray = IOUtils.toByteArray(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            LOG.error("downloadByteArrayFromWebResource, unable to download file " + fileName + ".  The HTTP response was " + response.getStatus());
            throw new IOException("Invalid response code: " + response.getStatus());
        }
        return byteArray;
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }

}
