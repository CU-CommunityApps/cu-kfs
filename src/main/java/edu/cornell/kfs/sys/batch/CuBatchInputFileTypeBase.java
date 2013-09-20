package edu.cornell.kfs.sys.batch;


import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;

public abstract class CuBatchInputFileTypeBase extends BatchInputFileTypeBase implements CuBatchInputFileType {

    public boolean isDoneFileRequired() {
        return false;
    }

}
