package edu.cornell.kfs.cemi.sys.dataaccess.impl;

import java.sql.Types;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.core.api.util.ClassLoaderUtils;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.cemi.sys.dataaccess.CemiSheetOrmDataHandlerDao;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiSheetOrmDataHandlerDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiSheetOrmDataHandlerDao {

    @Override
    public void getSheetDataAndWriteToFile(final CemiExcelWriter writer, final CemiSheetDefinition sheetDefinition,
            final String jobRunDate) {
        final Class<? extends CemiIndexedBusinessObjectBase> sheetBoClass = getSheetBoClass(sheetDefinition);

        final Criteria criteria = new Criteria();
        criteria.addEqualTo(CemiBasePropertyConstants.JOB_RUN_DATE, jobRunDate);

        final ReportQueryByCriteria query = new ReportQueryByCriteria(sheetBoClass, criteria);
        query.setAttributes(getFieldNamesToQuery(sheetDefinition));
        query.setJdbcTypes(getAllVarcharJdbcTypes(sheetDefinition));
        query.addOrderByAscending(CemiBasePropertyConstants.ROW_INDEX);

        try (
            final Stream<String[]> sheetDataRows = CuOjbUtils.buildCloseableStreamForReportQueryResults(
                    () -> getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query),
                    this::convertToSheetDataRow);
        ) {
            final String sheetName = sheetDefinition.getName();
            sheetDataRows.forEach(sheetDataRow -> writer.writeRow(sheetName, sheetDataRow));
        }
    }

    private Class<? extends CemiIndexedBusinessObjectBase> getSheetBoClass(final CemiSheetDefinition sheetDefinition) {
        try {
            final String boClassName = sheetDefinition.getBusinessObjectClass();
            Validate.validState(StringUtils.isNotBlank(boClassName), "No business object class defined for sheet: %s",
                    sheetDefinition.getName());
            return ClassLoaderUtils.getClass(boClassName, CemiIndexedBusinessObjectBase.class);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getFieldNamesToQuery(final CemiSheetDefinition sheetDefinition) {
        return sheetDefinition.getFields().stream()
                .map(CemiFieldDefinition::getKey)
                .toArray(String[]::new);
    }

    private int[] getAllVarcharJdbcTypes(final CemiSheetDefinition sheetDefinition) {
        final int[] jdbcTypes = new int[sheetDefinition.getFields().size()];
        Arrays.fill(jdbcTypes, Types.VARCHAR);
        return jdbcTypes;
    }

    private String[] convertToSheetDataRow(final Object[] reportRowData) {
        return Arrays.copyOf(reportRowData, reportRowData.length, String[].class);
    }

}
