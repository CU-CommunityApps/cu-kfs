package edu.cornell.kfs.module.ld.service;

import java.io.InputStream;

public interface LaborLedgerEnterpriseFeedService {

    /**
     * Creates a disencumbrance file together with a .recon and a .done file for the input encumbrance and recon file.
     * 
     * @param encumbranceFile
     * @return true if the disencumbrance file was created, false otherwise
     */
    public InputStream createDisencumbrance(InputStream encumbranceFile);
}
