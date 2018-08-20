package edu.cornell.kfs.fp.dataaccess.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.context.SpringContext;

import org.springframework.jdbc.core.ConnectionCallback;

import edu.cornell.kfs.fp.dataaccess.RecurringDisbursementVoucherSearchDao;

public class RecurringDisbursementVoucherSearchDaoJdbc extends PlatformAwareDaoBaseJdbc implements RecurringDisbursementVoucherSearchDao {

	private static final Logger LOG = LogManager.getLogger(RecurringDisbursementVoucherSearchDaoJdbc.class);

    protected DateTimeService dateTimeService;

    @Override
    public Collection<String> findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods(Date currentFisalPeriodEndDate) {
        final Date currentFisalPeriodEndDateForSql= currentFisalPeriodEndDate;
        Collection<DisbursementVoucherDocument> dvsFound = null;

        return getJdbcTemplate().execute(new ConnectionCallback<Collection<String>>() {
            public Collection<String> doInConnection(Connection con) throws SQLException {
                PreparedStatement selectStatement = null;
                ResultSet queryResultSet = null;
                List<String> finalResults = new ArrayList<String>();

                try {
                    String selectStatementSql = getSavedDvsSpawnedByRecurringDvForCurrentAndPastFiscalPeriodsSelectSql(currentFisalPeriodEndDateForSql);
                    LOG.info("findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods: SQL Statement to obtain saved DVs spawned by RCDV for current and past fiscal period follows.");
                    LOG.info("findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods: *************************************");
                    LOG.info("findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods: " +selectStatementSql);
                    LOG.info("findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods: *************************************");
                    selectStatement = con.prepareStatement(selectStatementSql);
                    queryResultSet = selectStatement.executeQuery();

                    while (queryResultSet.next()) {
                        String dvDocId = queryResultSet.getString(1);
                        String dvDueDate = queryResultSet.getString(2);
                        finalResults.add(dvDocId);
                        LOG.info("findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods: Found DV DocID=" +dvDocId+ "=  DVCheckDateSameAsDueDate=" +dvDueDate+ "=");
                    }
                    queryResultSet.close();

                } finally {
                    if (queryResultSet != null) {
                        try {
                            queryResultSet.close();
                        } catch (SQLException e) {
                            LOG.error("findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods: Could not close ResultSet");
                        }
                    }
                    if (selectStatement != null) {
                        try {
                            selectStatement.close();
                        } catch (SQLException e) {
                            LOG.error("findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods: Could not close selection PreparedStatement");
                        }
                    }
                }
                return finalResults;
            }
        });
    }

    private String getSavedDvsSpawnedByRecurringDvForCurrentAndPastFiscalPeriodsSelectSql(Date currentFisalPeriodEndDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy", Locale.US);
        StringBuilder sqlBuilder = new StringBuilder("SELECT B.DV_DOC_NBR, B.DV_CHECK_DT ");
        sqlBuilder.append("FROM KFS.FP_DV_DOC_T A, KFS.FP_RCDV_DTL_T B, CYNERGY.KREW_DOC_HDR_T C ");
        sqlBuilder.append("WHERE A.FDOC_NBR = B.DV_DOC_NBR AND C.DOC_HDR_ID = B.DV_DOC_NBR ");
        sqlBuilder.append("AND C.DOC_HDR_STAT_CD = '").append(KewApiConstants.ROUTE_HEADER_SAVED_CD).append("' ");
        sqlBuilder.append("AND B.DV_CHECK_DT <= '").append(formatter.format(currentFisalPeriodEndDate)).append("'");
        return sqlBuilder.toString();
    }

    protected DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
