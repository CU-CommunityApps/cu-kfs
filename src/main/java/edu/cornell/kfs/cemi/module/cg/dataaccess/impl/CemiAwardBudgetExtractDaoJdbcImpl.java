package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardBudgetExtractDao;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiAwardBudgetExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiAwardBudgetExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearExistingListOfExtractableProposalNumbers() {
//TODO FIXME truncate of the in-scope keys table will be added in the data scope iteration of this extract.
    }

    @Override
    public void queryAndStoreAwardProposalNumbersForAwardBudgetExtract() {
//TODO FIXME capture of in-scope Award keys from the Award extract data will be added in the data scope iteration of this extract.
    }

}
