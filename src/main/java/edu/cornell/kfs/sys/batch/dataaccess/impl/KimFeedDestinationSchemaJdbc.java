package edu.cornell.kfs.sys.batch.dataaccess.impl;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class KimFeedDestinationSchemaJdbc extends SchemaDao {
    private static final Logger LOG = LogManager.getLogger(KimFeedDestinationSchemaJdbc.class);
    
    //these should be consolidated and refactored where appropriate
    private static final String DB_VALIDATION_SQL = "select 1 from dual";
    private static final String DATABASE_TEST_SQL = "SELECT 1 FROM DUAL";
    
    public KimFeedDestinationSchemaJdbc(String schemaName) {
        initialize(schemaName);
    }
    
    protected void initialize(String schemaName) {
        super.setSchemaName(schemaName);
    }
    
    public String getDatabaseValidationSql() {
        return DB_VALIDATION_SQL;
    }
    
    public String getDatabaseTestSql() {
        return DATABASE_TEST_SQL;
    }
}
