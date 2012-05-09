package edu.cornell.kfs.module.bc.batch.service;

/**
 * A service that contains methods to populate the BC and SIP related tables with PS data.
 */
public interface SipImportToHumanResourcesService {

    /**
     * Creates the tab delimited SIP file to be sent to Human Resources.
     * 
     * @return true if successful, false otherwise
     */
    public StringBuilder createSipImportFileForHumanResources();
}
