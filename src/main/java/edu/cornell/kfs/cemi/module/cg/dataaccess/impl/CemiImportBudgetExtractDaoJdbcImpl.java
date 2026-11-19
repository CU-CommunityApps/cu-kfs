package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiImportBudgetExtractDao;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiImportBudgetExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements CemiImportBudgetExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution() {
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution was called.");
    }

    @Override
    public void queryAndStoreInScopeBusinessObjectKeysForDataExtract() {
        LOG.info("queryAndStoreInScopeBusinessObjectKeysForDataExtract was called.");
    }

}
