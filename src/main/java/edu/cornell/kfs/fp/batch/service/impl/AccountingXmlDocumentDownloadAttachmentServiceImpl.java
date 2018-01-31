package edu.cornell.kfs.fp.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.AttachmentService;
import org.springframework.beans.factory.DisposableBean;

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
        File formFile = downloadFile(accountingXmlDocumentBackupLink);
        
        if (formFile != null) {
            String uploadFileName = accountingXmlDocumentBackupLink.getFileName();
            String mimeType = URLConnection.guessContentTypeFromName(formFile.getName());
            int fileSize = (int) formFile.length();
            String attachmentType = null;
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("createAttachmentFromBackupLink, uploadFileName: " + uploadFileName + " mimeType: " + mimeType
                    + " fileSize: " + fileSize);
            }
            InputStream fileContents = new FileInputStream(formFile);
            Attachment attachment = attachmentService.createAttachment(document, uploadFileName, mimeType, fileSize,
                    fileContents, attachmentType);
            formFile.delete();
            return attachment;
        } else {
            LOG.error("createAttachmentFromBackupLink, the form file is NULL");
        }
        
        throw new IOException ("Unable to download attachment: " + accountingXmlDocumentBackupLink.getLinkUrl());
    }

    protected File downloadFile(AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink) {
        Collection<WebServiceCredential> creds = getWebServiceCredtials(accountingXmlDocumentBackupLink);
        File formFile = null;
        try { 
            Invocation request = buildClientRequest(accountingXmlDocumentBackupLink.getLinkUrl(), creds);
            formFile = downloadFileFromWebResource(accountingXmlDocumentBackupLink.getFileName(), request);
        } catch (URISyntaxException | IOException e) {
            LOG.error("downloadFile, unable to download file.", e);
        }
        return formFile;
    }

    protected Collection<WebServiceCredential> getWebServiceCredtials(AccountingXmlDocumentBackupLink accountingXmlDocumentBackupLink) {
        Collection<WebServiceCredential> webServiceCredentials = webServiceCredentialService
                .getWebServiceCredentialsByGroupCode(accountingXmlDocumentBackupLink.getCredentialGroupCode());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWebServiceCredtials, the group code is " + accountingXmlDocumentBackupLink.getCredentialGroupCode() + " for the file " + 
                    accountingXmlDocumentBackupLink.getFileName());
            if (CollectionUtils.isNotEmpty(webServiceCredentials)) {
                webServiceCredentials.stream().forEach(cred -> LOG.debug("getWebServiceCredtials, found a credential key: " + cred.getCredentialKey()));
            } else {
                LOG.debug("getWebServiceCredtials, no credentials found");
            }
        }
        return webServiceCredentials;
    }

    protected Invocation buildClientRequest(String url, Collection<WebServiceCredential> creds)
            throws URISyntaxException {
        URI uri = new URI(url);
        Builder builder = getClient().target(uri).request();
        if (CollectionUtils.isNotEmpty(creds)) {
            for (WebServiceCredential cred : creds) {
                builder.header(cred.getCredentialKey(), cred.getCredentialValue());
            }
        }
        return builder.buildGet();
    }

    protected File downloadFileFromWebResource(String fileName, Invocation request) throws IOException {
        File downloadfile = new File(fileName);
        Response response = request.invoke();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            InputStream is = response.readEntity(InputStream.class);
            byte[] byteArray = IOUtils.toByteArray(is);
            FileOutputStream fos = new FileOutputStream(downloadfile);
            fos.write(byteArray);
            fos.flush();
            fos.close();
            IOUtils.closeQuietly(is);
        } else {
            LOG.error("downloadFileFromWebResource, unable to download file " + fileName + ".  The HTTP response was " + response.getStatus());
            throw new IOException("Invalid response code: " + response.getStatus());
        }
        return downloadfile;
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }

}
