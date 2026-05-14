package edu.cornell.kfs.cemi.sys.batch.dataaccess.impl;

import java.sql.Types;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetOrmMetadata;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiSheetDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiSheetDao {

    @Override
    public Stream<String[]> getSheetRowDataForPrinting(final CemiSheetOrmMetadata sheetMetadata, final String jobRunDate) {
        final Criteria criteria = new Criteria();
        criteria.addEqualTo(CemiBasePropertyConstants.JOB_RUN_DATE, jobRunDate);

        final ReportQueryByCriteria query = new ReportQueryByCriteria(sheetMetadata.getBoClass(), criteria);
        query.setAttributes(getFieldsToSelect(sheetMetadata));
        query.setJdbcTypes(getAllVarcharJdbcTypes(sheetMetadata));
        query.addOrderBy(CemiBasePropertyConstants.ROW_INDEX, true);

        return CuOjbUtils.buildCloseableStreamForReportQueryResults(
                () -> getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query),
                rowAsArray -> Arrays.copyOf(rowAsArray, rowAsArray.length, String[].class));
    }

    private String[] getFieldsToSelect(final CemiSheetOrmMetadata sheetMetadata) {
        return sheetMetadata.getFieldMappings().stream()
                .map(Pair::getRight)
                .toArray(String[]::new);
    }

    private int[] getAllVarcharJdbcTypes(final CemiSheetOrmMetadata sheetMetadata) {
        final int[] jdbcTypes = new int[sheetMetadata.getFieldMappings().size()];
        Arrays.fill(jdbcTypes, Types.VARCHAR);
        return jdbcTypes;
    }

}
