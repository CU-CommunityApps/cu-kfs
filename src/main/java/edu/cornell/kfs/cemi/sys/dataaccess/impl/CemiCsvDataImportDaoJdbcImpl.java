package edu.cornell.kfs.cemi.sys.dataaccess.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.CemiCsvBatchInputFileType;
import edu.cornell.kfs.cemi.sys.dataaccess.CemiCsvDataImportDao;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiCsvDataImportDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiCsvDataImportDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void truncateDestinationTable(final CemiCsvBatchInputFileType batchInputFileType) {
        final String tableName = validateAndGetTableName(batchInputFileType);
        LOG.info("truncateDestinationTable, Truncating table: {}", tableName);
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.", tableName);
        executeUpdate(query);
        LOG.info("truncateDestinationTable, Finished truncating table: {}", tableName);
    }

    @Override
    public void storeCsvData(final CemiCsvBatchInputFileType batchInputFileType, final Iterator<String[]> csvIterator) {
        final String tableName = validateAndGetTableName(batchInputFileType);
        final List<String> columnNames = validateAndGetColumnNamesFromEnum(batchInputFileType.getCsvEnumClass());
        final CuSqlQuery query = createBatchDataInsertionQuery(tableName, columnNames);
        final int expectedRowLength = columnNames.size();
        final List<String[]> batchedRows = new ArrayList<>();
        String[] currentRow = null;
        int rowNumber = 0;
        int rowNumberAdjustment = 0;
        LOG.info("storeCsvData, Storing CSV data in table: {}", tableName);

        if (csvIterator.hasNext() && batchInputFileType.isHasHeaderRow()) {
            LOG.info("storeCsvData, Skipping header row from CSV file", tableName);
            rowNumber++;
            rowNumberAdjustment = -1;
            currentRow = csvIterator.next();
            validateRowLength(rowNumber, expectedRowLength, currentRow);
        }

        while (csvIterator.hasNext()) {
            rowNumber++;
            currentRow = csvIterator.next();
            batchedRows.add(currentRow);
            if (batchedRows.size() >= CemiBaseConstants.BULK_DATA_BATCH_SIZE) {
                executeBatchUpdate(query, batchedRows);
                batchedRows.clear();
            }
        }

        if (!batchedRows.isEmpty()) {
            executeBatchUpdate(query, batchedRows);
        }

        final int numRowsInserted = rowNumber + rowNumberAdjustment;
        LOG.info("storeCsvData, Finished storing {} CSV data rows in table: {}", numRowsInserted, tableName);
    }

    private String validateAndGetTableName(final CemiCsvBatchInputFileType batchInputFileType) {
        final String tableName = batchInputFileType.getTableName();
        Validate.validState(CemiUtils.valueOnlyContainsWordCharacters(tableName),
                "batchInputFileType has an invalid table name: %s", tableName);
        return tableName;
    }

    private List<String> validateAndGetColumnNamesFromEnum(final Class<?> csvEnumClass) {
        final String enumClassName = csvEnumClass.getSimpleName();
        final Object[] enumConstants = csvEnumClass.getEnumConstants();
        final List<String> columnNames = Arrays.stream(enumConstants)
                .map(Object::toString)
                .collect(Collectors.toUnmodifiableList());
        int constantIndex = 0;

        for (final String columnName : columnNames) {
            Validate.validState(CemiUtils.valueOnlyContainsWordCharacters(columnName),
                    "Enum constant from % at index % has a toString() value containing non-word characters",
                    enumClassName, constantIndex);
            constantIndex++;
        }

        return columnNames;
    }

    private CuSqlQuery createBatchDataInsertionQuery(final String tableName, final List<String> columnNames) {
        final CuSqlChunk query = new CuSqlChunk()
                .append("INSERT INTO KFS.")
                .append(tableName)
                .append(" (");

        String prefix = KFSConstants.EMPTY_STRING;
        for (final String columnName : columnNames) {
            query.append(prefix)
                    .append(columnName);
            prefix = CUKFSConstants.COMMA_AND_SPACE;
        }

        query.append(") VALUES (");

        prefix = KFSConstants.EMPTY_STRING;
        for (int i = 0; i < columnNames.size(); i++) {
            final int currentIndex = i;
            final Function<String[], String> csvArrayCellGetter = csvRow -> csvRow[currentIndex];
            query.append(prefix)
                    .appendAsParameter(Types.VARCHAR, csvArrayCellGetter);
            prefix = CUKFSConstants.COMMA_AND_SPACE;
        }

        query.append(")");

        return query.toQuery();
    }

    private void validateRowLength(final int rowNumber, final int expectedRowLength, final String[] csvRow) {
        Validate.validState(expectedRowLength == csvRow.length,
                "Row %s has the wrong number of columns; expected: %s, actual: %s",
                rowNumber, expectedRowLength, csvRow.length);
    }

}
