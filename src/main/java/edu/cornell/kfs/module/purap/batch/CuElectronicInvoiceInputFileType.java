package edu.cornell.kfs.module.purap.batch;

import org.kuali.kfs.module.purap.batch.ElectronicInvoiceInputFileType;

import edu.cornell.kfs.sys.batch.CuBatchInputFileType;

public class CuElectronicInvoiceInputFileType extends ElectronicInvoiceInputFileType implements CuBatchInputFileType {
	
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuElectronicInvoiceInputFileType.class);

    @Override
    public boolean isDoneFileRequired() {
        return false;
    }
    
    @Override
    public String getSchemaLocation() {
    	LOG.info("The schema is actually '" + schemaLocation + "' but actually returning http://localhost:8080/kfs/static/xsd/purap/electronicInvoice.xsd");
        //return schemaLocation;
    	return "http://localhost:8080/kfs/static/xsd/purap/electronicInvoice.xsd";
    }

}
