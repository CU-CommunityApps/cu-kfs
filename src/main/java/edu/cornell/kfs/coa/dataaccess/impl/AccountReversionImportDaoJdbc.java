package edu.cornell.kfs.coa.dataaccess.impl;

import org.kuali.rice.kns.dao.jdbc.PlatformAwareDaoBaseJdbc;

import edu.cornell.kfs.coa.dataaccess.AccountReversionImportDao;

public class AccountReversionImportDaoJdbc extends PlatformAwareDaoBaseJdbc implements AccountReversionImportDao {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionImportDaoJdbc.class);

	/**
     * Deletes all existing records in gl_rvrsn_ctgry_amt_t and gl_rvrsn_unit_wrk_t
     * 
     * @see org..kuali.kfs.gl.batch.dataaccess.ReversionUnitOfWorkDao#destroyAllUnitOfWorkSummaries()
     */
    public void destroyAccountReversionsAndDetails() {
        LOG.info("Attempting to wipe out account reversions and details");
        getSimpleJdbcTemplate().update("delete from CA_ACCT_REVERSION_T");
        getSimpleJdbcTemplate().update("delete from GL_ACCT_RVRSN_DTL_T");
        LOG.info("All account reversions and details should be now removed");
    }

}
