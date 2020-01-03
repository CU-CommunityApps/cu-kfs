package edu.cornell.kfs.coa.batch.dataaccess.impl;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.businessobject.LaborClosedAccount;
import edu.cornell.kfs.coa.batch.dataaccess.ClosedLaborAccountsByDateRangeDao;


public class ClosedLaborAccountsByDateRangeDaoJdbc extends PlatformAwareDaoBaseJdbc implements ClosedLaborAccountsByDateRangeDao {
    private static final Logger LOG = LogManager.getLogger(ClosedLaborAccountsByDateRangeDaoJdbc.class);
    
    private static final String ACCT_FIN_COA_CD = "ACCT_FIN_COA_CD";
    private static final String ACCT_ACCOUNT_NBR = "ACCT_ACCOUNT_NBR";
    private static final String SUB_SUB_ACCT_NBR = "SUB_SUB_ACCT_NBR";
    private static final String ACCT_ACCT_CLOSED_IND = "ACCT_ACCT_CLOSED_IND";
    private static final String ACCTX_ACCT_CLOSED_DT = "ACCTX_ACCT_CLOSED_DT";
    
    @Override
    public List<LaborClosedAccount> obtainLaborClosedAccountsDataFor(Map<String, Date> dateRange) {
        List<LaborClosedAccount> closedLaborAccounts = findAllClosedLaborAccountsFor(dateRange);
        return closedLaborAccounts;
    }
    
    private List<LaborClosedAccount> findAllClosedLaborAccountsFor(Map<String, Date> dateRange) {
        try {
            RowMapper<LaborClosedAccount> mapRow = new RowMapper<LaborClosedAccount>() {
                public LaborClosedAccount mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                    LaborClosedAccount laborClosedAccountDataRow = new LaborClosedAccount();
                    laborClosedAccountDataRow.setChart(resultSet.getString(ACCT_FIN_COA_CD));
                    laborClosedAccountDataRow.setAccountNumber(resultSet.getString(ACCT_ACCOUNT_NBR));
                    laborClosedAccountDataRow.setSubAccountNumber(resultSet.getString(SUB_SUB_ACCT_NBR));
                    laborClosedAccountDataRow.setAccountClosedIndicator(resultSet.getString(ACCT_ACCT_CLOSED_IND));
                    laborClosedAccountDataRow.setAccountClosedDate(resultSet.getDate(ACCTX_ACCT_CLOSED_DT));
                    return laborClosedAccountDataRow;
                }
            };
            return this.getJdbcTemplate().query(buildClosedLaborAccountsForDateRangeSQL(dateRange), mapRow);
        } catch (Exception e) {
            LOG.info("findAllClosedLaborAccountsFor Exception: " + e.getMessage());
            return null;
        }
    }

    private String buildClosedLaborAccountsForDateRangeSQL(Map<String, Date> dateRange) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy", Locale.US);
        StringBuilder sqlBuilder = new StringBuilder("SELECT A.FIN_COA_CD AS ACCT_FIN_COA_CD, A.ACCOUNT_NBR AS ACCT_ACCOUNT_NBR, ");
        sqlBuilder.append("S.SUB_ACCT_NBR AS SUB_SUB_ACCT_NBR, A.ACCT_CLOSED_IND AS ACCT_ACCT_CLOSED_IND, X.ACCT_CLOSED_DT AS ACCTX_ACCT_CLOSED_DT ");
        sqlBuilder.append("FROM KFS.CA_ACCOUNT_T A ");
        sqlBuilder.append("LEFT JOIN KFS.CA_ACCOUNT_TX X ON A.FIN_COA_CD = X.FIN_COA_CD AND A.ACCOUNT_NBR = X.ACCOUNT_NBR ");
        sqlBuilder.append("LEFT JOIN KFS.CA_SUB_ACCT_T S ON A.FIN_COA_CD = S.FIN_COA_CD AND A.ACCOUNT_NBR = S.ACCOUNT_NBR ");
        sqlBuilder.append("WHERE A.FIN_COA_CD = X.FIN_COA_CD AND A.ACCOUNT_NBR = X.ACCOUNT_NBR AND A.FIN_COA_CD = 'IT' AND A.ACCT_CLOSED_IND = 'Y' ");
        sqlBuilder.append("AND X.ACCT_CLOSED_DT >= '").append(formatter.format(dateRange.get(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FROM_DATE))).append("' ");
        sqlBuilder.append("AND X.ACCT_CLOSED_DT < '").append(formatter.format(dateRange.get(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.TO_DATE))).append("' ");
        LOG.info("buildClosedLaborAccountsForDateRangeSQL: SQL to be executed = " + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

}
