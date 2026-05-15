package edu.cornell.kfs.cemi.sys.batch.dataaccess.impl;

import java.sql.Types;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
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
    public void getAndHandleSheetRowDataForPrinting(final CemiSheetOrmMetadata sheetMetadata, final String jobRunDate,
            final Consumer<String[]> rowDataHandler) {
        final Criteria criteria = new Criteria();
        criteria.addEqualTo(CemiBasePropertyConstants.JOB_RUN_DATE, jobRunDate);

        final ReportQueryByCriteria query = new ReportQueryByCriteria(sheetMetadata.getBoClass(), criteria);
        query.setAttributes(getFieldsToSelect(sheetMetadata));
        query.setJdbcTypes(getAllVarcharJdbcTypes(sheetMetadata));
        query.addOrderBy(CemiBasePropertyConstants.ROW_INDEX, true);

        try (
            final Stream<String[]> results = CuOjbUtils.buildCloseableStreamForReportQueryResults(
                    () -> getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query),
                    rowAsArray -> Arrays.copyOf(rowAsArray, rowAsArray.length, String[].class));
         ) {
            final Iterator<String[]> resultsIterator = results.iterator();
            for (final String[] sheetDataRow : IteratorUtils.asIterable(resultsIterator)) {
                rowDataHandler.accept(sheetDataRow);
            }
         }
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
