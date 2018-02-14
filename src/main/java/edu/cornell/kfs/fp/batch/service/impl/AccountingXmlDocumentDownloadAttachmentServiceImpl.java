package edu.cornell.kfs.fp.batch.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collection;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.AttachmentService;
import org.springframework.beans.factory.DisposableBean;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.service.impl.DisposableClientServiceImplBase;

public class AccountingXmlDocumentDownloadAttachmentServiceImpl extends DisposableClientServiceImplBase implements AccountingXmlDocumentDownloadAttachmentService, DisposableBean {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountingXmlDocumentDownloadAttachmentServiceImpl.class);

    protected AttachmentService attachmentService;
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public Attachment createAttachmentFromBackupLink(Document document,
            AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink) throws IOException {
        if (StringUtils.isBlank(accountingXmlDocumentBackupLink.getCredentialGroupCode())) {
            LOG.error("createAttachmentFromBackupLink, the Credential Group Code is blank");
            throw new IOException("Unable to download attachment with blank Credential Group Code: " + accountingXmlDocumentBackupLink.getLinkUrl());
        }
        
        byte[] formFile = downloadByteArray(accountingXmlDocumentBackupLink);
        
        if (formFile.length > 0) {
            String uploadFileName = accountingXmlDocumentBackupLink.getFileName();
            String mimeType = URLConnection.guessContentTypeFromName(uploadFileName);
            int fileSize = (int) formFile.length;
            String attachmentType = null;
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("createAttachmentFromBackupLink, uploadFileName: " + uploadFileName + " mimeType: " + mimeType
                    + " fileSize: " + fileSize);
            }
            InputStream inputStream = new ByteArrayInputStream(formFile);
            Attachment attachment = attachmentService.createAttachment(document, uploadFileName, mimeType, fileSize,
                    inputStream, attachmentType);
            return attachment;
        } else {
            LOG.error("createAttachmentFromBackupLink, the form file is NULL");
        }
        
        throw new IOException("Unable to download attachment: " + accountingXmlDocumentBackupLink.getLinkUrl());
    }

    protected byte[] downloadByteArray(AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink) throws IOException {
        Collection<WebServiceCredential> credentials = getWebServiceCredentials(accountingXmlDocumentBackupLink);
        try {
            if (isLinkUrlValidForGroupCode(accountingXmlDocumentBackupLink, credentials)) {
                Invocation request = buildClientRequest(accountingXmlDocumentBackupLink.getLinkUrl(), credentials);
                return downloadByteArrayFromWebResource(accountingXmlDocumentBackupLink.getFileName(), request);
            } else {
                LOG.error("downloadByteArray, the group code isn't valid for the link URL");
                throw new IOException("The group code isn't valid for the link URL");
            }
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    protected boolean isLinkUrlValidForGroupCode(AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink, Collection<WebServiceCredential> credentials) {
        if (CollectionUtils.isEmpty(credentials)) {
            LOG.debug("isLinkUrlValidForGroupCode, no credential values for group code, so is valid");
            return true;
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
