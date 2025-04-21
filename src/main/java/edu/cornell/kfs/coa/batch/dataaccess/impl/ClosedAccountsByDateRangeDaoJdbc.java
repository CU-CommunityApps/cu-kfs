package edu.cornell.kfs.coa.batch.dataaccess.impl;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.businessobject.ClosedAccount;
import edu.cornell.kfs.coa.batch.dataaccess.ClosedAccountsByDateRangeDao;


public class ClosedAccountsByDateRangeDaoJdbc extends PlatformAwareDaoBaseJdbc implements ClosedAccountsByDateRangeDao {
    private static final Logger LOG = LogManager.getLogger(ClosedAccountsByDateRangeDaoJdbc.class);
    
    private static final String ACCT_FIN_COA_CD = "ACCT_FIN_COA_CD";
    private static final String ACCT_ACCOUNT_NBR = "ACCT_ACCOUNT_NBR";
    private static final String SUB_SUB_ACCT_NBR = "SUB_SUB_ACCT_NBR";
    private static final String ACCT_ACCT_CLOSED_IND = "ACCT_ACCT_CLOSED_IND";
    private static final String ACCTX_ACCT_CLOSED_DT = "ACCTX_ACCT_CLOSED_DT";
    
    @Override
    public List<ClosedAccount> obtainClosedAccountsDataFor(Map<String, Date> dateRange) {
        List<ClosedAccount> closedAccounts = findAllClosedAccountsFor(dateRange);
        return closedAccounts;
    }
    
    private List<ClosedAccount> findAllClosedAccountsFor(Map<String, Date> dateRange) {
        try {
            RowMapper<ClosedAccount> mapRow = new RowMapper<ClosedAccount>() {
                public ClosedAccount mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                    ClosedAccount closedAccountDataRow = new ClosedAccount();
                    closedAccountDataRow.setChart(resultSet.getString(ACCT_FIN_COA_CD));
                    closedAccountDataRow.setAccountNumber(resultSet.getString(ACCT_ACCOUNT_NBR));
                    closedAccountDataRow.setSubAccountNumber(resultSet.getString(SUB_SUB_ACCT_NBR));
                    closedAccountDataRow.setAccountClosedIndicator(resultSet.getString(ACCT_ACCT_CLOSED_IND));
                    closedAccountDataRow.setAccountClosedDate(resultSet.getDate(ACCTX_ACCT_CLOSED_DT));
                    return closedAccountDataRow;
                }
            };
            return this.getJdbcTemplate().query(buildClosedAccountsForItChartDateRangeSql(), buildClosedAccountsForItChartArgumentList(dateRange), mapRow);
        } catch (Exception e) {
            LOG.info("findAllClosedAccountsFor Exception: " + e.getMessage());
            return null;
        }
    }
    
    private Object[] buildClosedAccountsForItChartArgumentList(Map<String, Date> dateRange) {
        List<Object> argumentList = new ArrayList<>();
        argumentList.add(dateRange.get(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FROM_DATE));
        argumentList.add(dateRange.get(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.TO_DATE));
        return argumentList.toArray();
    }

    private String buildClosedAccountsForItChartDateRangeSql() {
        StringBuilder sqlBuilder = new StringBuilder("SELECT A.FIN_COA_CD AS ACCT_FIN_COA_CD, A.ACCOUNT_NBR AS ACCT_ACCOUNT_NBR, ");
        sqlBuilder.append("S.SUB_ACCT_NBR AS SUB_SUB_ACCT_NBR, A.ACCT_CLOSED_IND AS ACCT_ACCT_CLOSED_IND, X.ACCT_CLOSED_DT AS ACCTX_ACCT_CLOSED_DT ");
        sqlBuilder.append("FROM KFS.CA_ACCOUNT_T A ");
        sqlBuilder.append("LEFT JOIN KFS.CA_ACCOUNT_TX X ON A.FIN_COA_CD = X.FIN_COA_CD AND A.ACCOUNT_NBR = X.ACCOUNT_NBR ");
        sqlBuilder.append("LEFT JOIN KFS.CA_SUB_ACCT_T S ON A.FIN_COA_CD = S.FIN_COA_CD AND A.ACCOUNT_NBR = S.ACCOUNT_NBR ");
        sqlBuilder.append("WHERE A.FIN_COA_CD = X.FIN_COA_CD AND A.ACCOUNT_NBR = X.ACCOUNT_NBR AND A.FIN_COA_CD = 'IT' AND A.ACCT_CLOSED_IND = 'Y' ");
        sqlBuilder.append("AND X.ACCT_CLOSED_DT >= ? AND X.ACCT_CLOSED_DT < ? ");
        LOG.info("buildClosedAccountsForItChartDateRangeSql: SQL to be executed = " + sqlBuilder.toString());
        return sqlBuilder.toString();
    }
}
