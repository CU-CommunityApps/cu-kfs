package edu.cornell.kfs.module.bc.document.service;

import java.util.List;

import edu.cornell.kfs.module.bc.businessobject.SipImportData;

public interface SipDistributionService {

    /**
     * Calculates and applies SIP to BC.
     * 
     * @param updateMode
     * @param sipImportDataCollection
     * @return
     */
    public StringBuilder distributeSip(boolean updateMode, List<SipImportData> sipImportDataCollection);

}
