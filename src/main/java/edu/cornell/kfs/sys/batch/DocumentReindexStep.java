package edu.cornell.kfs.sys.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeIndexingQueue;

import edu.cornell.kfs.sys.CUKFSConstants;

public class DocumentReindexStep extends CuAbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    private DocumentAttributeIndexingQueue documentAttributeIndexingQueue;
    private String stagingDirectory;

    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        try {
            File documentReindexFile = new File(stagingDirectory + File.separator + getDocumentReindexFilename());
            List<String> documentIds = getDocumentIdsToProcess(documentReindexFile);
            reindexDocuments(documentIds);
            addTimeStampToFileName(documentReindexFile, getDocumentReindexFilename(), stagingDirectory);
        } catch (Exception e) {
            LOG.error("execute, Unexpected error occurred while indexing documents", e);
            throw new RuntimeException(e);
        }
        return true;
    }

    private String getDocumentReindexFilename() {
        return CUKFSConstants.DOCUMENT_REINDEX_FILE_NAME_PREFIX + CUKFSConstants.TEXT_FILE_EXTENSION;
    }

    private List<String> getDocumentIdsToProcess(File documentReindexFile) throws IOException {
        LOG.info("getDocumentIdsToProcess, Reading document IDs to be processed for reindexing...");
        try (FileReader fileReader = new FileReader(documentReindexFile, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            List<String> documentIds = bufferedReader.lines().collect(Collectors.toUnmodifiableList());
            LOG.info("getDocumentIdsToProcess, Found " + documentIds.size() + " documents to reindex");
            return documentIds;
        }
    }

    private void reindexDocuments(List<String> documentIds) {
        for (String documentId : documentIds) {
            try {
                LOG.info("reindexDocuments, Indexing document " + documentId);
                documentAttributeIndexingQueue.indexDocument(documentId);
            } catch (Exception e) {
                LOG.error("reindexDocuments, Unexpected error occurred while indexing document " + documentId, e);
            }
        }
        LOG.info("reindexDocuments, Finished indexing documents");
    }

    public void setDocumentAttributeIndexingQueue(DocumentAttributeIndexingQueue documentAttributeIndexingQueue) {
        this.documentAttributeIndexingQueue = documentAttributeIndexingQueue;
    }

    public void setStagingDirectory(String stagingDirectory) {
        this.stagingDirectory = stagingDirectory;
    }

}
