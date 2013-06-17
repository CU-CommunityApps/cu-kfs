package edu.cornell.kfs.coa.service;

import java.io.File;

public interface AccountReversionImportService {

    /**
     * Imports accounts reversions from an input csv file.
     * 
     * @param f file containing accounts reversions to be imported
     */
    public void importAccountReversions(File f);

}
