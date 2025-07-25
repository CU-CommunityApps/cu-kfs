package edu.cornell.kfs.module.ld.businessobject;

import java.io.File;
import java.io.FileNotFoundException;

import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;

@NAMESPACE(namespace = KFSConstants.OptionalModuleNamespaces.LABOR_DISTRIBUTION)
public class LaborLedgerBatchFile extends BatchFile {

    private static final long serialVersionUID = 1L;


    public LaborLedgerBatchFile(String id) throws FileNotFoundException {
        super(id);
    }

    public LaborLedgerBatchFile(File file) {
        super(file);
    }

}