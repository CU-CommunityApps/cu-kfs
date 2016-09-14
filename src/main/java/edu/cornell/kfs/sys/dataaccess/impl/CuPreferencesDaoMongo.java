package edu.cornell.kfs.sys.dataaccess.impl;

import org.kuali.kfs.sys.dataaccess.PreferencesDao;
import org.kuali.kfs.sys.dataaccess.impl.PreferencesDaoMongo;

public class CuPreferencesDaoMongo extends PreferencesDaoMongo implements PreferencesDao {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPreferencesDaoMongo.class);

    private MongoDBClient documentStoreClient;
    private String tableNamePrefix;

    public void setDocumentStoreClient(MongoDBClient documentStoreClient) {
        this.documentStoreClient = documentStoreClient;
        super.setMongoTemplate(documentStoreClient.getClient());
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(String tableNamePrefix) {
        this.tableNamePrefix = tableNamePrefix;
    }
}

