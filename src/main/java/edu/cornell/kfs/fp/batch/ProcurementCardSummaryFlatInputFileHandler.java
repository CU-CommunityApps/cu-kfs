package edu.cornell.kfs.fp.batch;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;

public class ProcurementCardSummaryFlatInputFileHandler implements FlatFileDataHandler {

    /**
     * @see org.kuali.kfs.sys.batch.FlatFileDataHandler#validate(java.lang.Object)
     */
    public boolean validate(Object parsedFileContents) {

        return false;
    }

    /**
     * @see org.kuali.kfs.sys.batch.FlatFileDataHandler#process(java.lang.String, java.lang.Object)
     */
    public void process(String fileName, Object parsedFileContents) {

    }

}
