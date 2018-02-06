package edu.cornell.kfs.sys.batch.service;

public interface KualiDeveloperFeedService {

    /**
     * Loads CU_KUALI_DEVELOPER_T table with custom data.
     *
     * @param fileName the file containing the data to be loaded
     *
     * @return true if successful, false otherwise
     */
    public boolean loadKualiDeveloperDataFromBatchFile(String fileName);

}
