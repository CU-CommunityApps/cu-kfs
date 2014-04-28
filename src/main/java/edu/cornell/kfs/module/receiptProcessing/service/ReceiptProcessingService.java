/**
 * @author cab379
 */
package edu.cornell.kfs.module.receiptProcessing.service;

import org.kuali.kfs.sys.batch.BatchInputFileType;

public interface ReceiptProcessingService {

    /**
     * Validates and parses all files ready to go in the batch staging area.
     * @return
     */
    public boolean loadFiles();

    /**
     * 
     */
    public boolean attachFiles(String fileName, BatchInputFileType batchInputFileType);           
   
}

