package edu.cornell.kfs.fp.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;

public class CreateAccountingDocumentServiceImpl implements CreateAccountingDocumentService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CreateAccountingDocumentServiceImpl.class);

    private BatchInputFileService batchInputFileService;
    private BatchInputFileType accountingDocumentBatchInputFileType;
    private DocumentService documentService;

    @Override
    public void createAccountingDocumentsFromXml() {
        List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(accountingDocumentBatchInputFileType);
        LOG.info("createAccountingDocumentsFromXml: Found " + inputFileNames.size() + " files to process");
        
        inputFileNames.stream()
                .forEach(this::processAccountingDocumentFromXml);
        
        LOG.info("createAccountingDocumentsFromXml: Finished processing all pending accounting document XML files");
    }

    protected void processAccountingDocumentFromXml(String fileName) {
        try {
            LOG.info("processAccountingDocumentFromXml: Started processing accounting document XML file: " + fileName);
            
            byte[] fileData = safelyLoadFileBytes(fileName);
            AccountingXmlDocumentListWrapper accountingXmlDocuments = (AccountingXmlDocumentListWrapper) batchInputFileService.parse(
                    accountingDocumentBatchInputFileType, fileData);
            int documentCount = accountingXmlDocuments.getDocuments().size();
            LOG.info("processAccountingDocumentFromXml: Found " + documentCount + " documents to process from file: " + fileName);
            accountingXmlDocuments.getDocuments().stream()
                    .forEach(this::processAccountingDocumentEntryFromXml);
            
            LOG.info("processAccountingDocumentFromXml: Finished processing accounting document XML file: " + fileName);
        } catch (Exception e) {
            LOG.error("processAccountingDocumentFromXml: Error processing accounting document XML file", e);
        } finally {
            removeDoneFileQuietly(fileName);
        }
    }

    protected byte[] safelyLoadFileBytes(String fileName) {
        FileInputStream inputStream = null;
        
        try {
            inputStream = new FileInputStream(fileName);
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    protected void processAccountingDocumentEntryFromXml(AccountingXmlDocumentEntry accountingXmlDocument) {
        try {
            LOG.info("processAccountingDocumentEntryFromXml: Started processing accounting document of type: "
                    + accountingXmlDocument.getDocumentTypeCode());
            
            String documentGeneratorBeanName = CuFPConstants.ACCOUNTING_DOCUMENT_GENERATOR_BEAN_PREFIX + accountingXmlDocument.getDocumentTypeCode();
            AccountingDocumentGenerator<? extends AccountingDocument> documentGenerator = findDocumentGenerator(documentGeneratorBeanName);
            AccountingDocument document = documentGenerator.createDocument(this::getNewDocument, accountingXmlDocument);
            documentService.routeDocument(
                    document, CuFPConstants.ACCOUNTING_DOCUMENT_XML_ROUTE_ANNOTATION, getAllAdHocRecipients(document));
            
            LOG.info("processAccountingDocumentEntryFromXml: Finished processing accounting document of type: "
                    + accountingXmlDocument.getDocumentTypeCode());
        } catch (Exception e) {
            LOG.error("processAccountingDocumentEntryFromXml: Error processing accounting XML document", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected AccountingDocumentGenerator<? extends AccountingDocument> findDocumentGenerator(String beanName) {
        return SpringContext.getBean(AccountingDocumentGenerator.class, beanName);
    }

    protected Document getNewDocument(Class<? extends Document> documentClass) {
        try {
            return documentService.getNewDocument(documentClass);
        } catch (WorkflowException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<AdHocRouteRecipient> getAllAdHocRecipients(Document document) {
        return Stream.concat(document.getAdHocRoutePersons().stream(), document.getAdHocRouteWorkgroups().stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected void removeDoneFileQuietly(String dataFileName) {
        try {
            String doneFileName = StringUtils.replace(
                    dataFileName, CuFPConstants.XML_FILE_EXTENSION, GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION);
            if (!FileUtils.deleteQuietly(new File(doneFileName))) {
                LOG.error("removeDoneFileQuietly: Could not delete .done file: " + doneFileName);
            }
        } catch (RuntimeException e) {
            LOG.error("removeDoneFileQuietly: Could not delete .done file for accounting document XML", e);
        }
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setAccountingDocumentBatchInputFileType(BatchInputFileType accountingDocumentBatchInputFileType) {
        this.accountingDocumentBatchInputFileType = accountingDocumentBatchInputFileType;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

}
