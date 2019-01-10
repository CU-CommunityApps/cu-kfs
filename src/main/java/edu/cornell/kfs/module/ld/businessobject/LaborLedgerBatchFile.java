package edu.cornell.kfs.module.ld.businessobject;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;

import java.io.File;
import java.io.FileNotFoundException;

import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;

@NAMESPACE(namespace = KFSConstants.OptionalModuleNamespaces.LABOR_DISTRIBUTION)
public class LaborLedgerBatchFile extends BatchFile {

	private static final long serialVersionUID = 1L;
	
	public LaborLedgerBatchFile() {
		super();
	}
    
    public LaborLedgerBatchFile(String id) throws FileNotFoundException {
		super(id);
	}
    
    public LaborLedgerBatchFile(File file) {
		super(file);
	}

}