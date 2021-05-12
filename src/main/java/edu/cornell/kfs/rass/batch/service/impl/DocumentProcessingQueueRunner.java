package edu.cornell.kfs.rass.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.api.document.DocumentProcessingQueue;

public class DocumentProcessingQueueRunner implements Runnable {
    private static final Logger LOG = LogManager.getLogger(DocumentProcessingQueueRunner.class);
    
    private String documentNumber;
    private DocumentProcessingQueue documentProcessingQueue;
    
    public DocumentProcessingQueueRunner(String documentNumber, DocumentProcessingQueue documentProcessingQueue) {
        this.documentNumber = documentNumber;
        this.documentProcessingQueue = documentProcessingQueue;
    }

    @Override
    public void run() {
        LOG.info("run, started proccessing for document number " + documentNumber);
        documentProcessingQueue.process(documentNumber);
        LOG.info("run, completed proccessing for document number " + documentNumber);
    }

}
