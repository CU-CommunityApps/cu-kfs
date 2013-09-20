package edu.cornell.kfs.sys.batch;

import org.kuali.kfs.sys.batch.BatchInputFileType;

public interface CuBatchInputFileType extends BatchInputFileType {
    public boolean isDoneFileRequired();
}
