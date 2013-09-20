package edu.cornell.kfs.module.purap.batch;

import org.kuali.kfs.module.purap.batch.ElectronicInvoiceInputFileType;

import edu.cornell.kfs.sys.batch.CuBatchInputFileType;

public class CuElectronicInvoiceInputFileType extends ElectronicInvoiceInputFileType implements CuBatchInputFileType {

    @Override
    public boolean isDoneFileRequired() {
        return false;
    }

}
