package edu.cornell.kfs.cemi.sys.dataaccess.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.dataaccess.CemiCsvDataImportDao;
import edu.cornell.kfs.cemi.sys.util.CemiCuSqlChunk;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiCsvDataImportDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiCsvDataImportDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void truncateDestinationTable(final String legacyDataDestinationTableName) {
        validateTableName(legacyDataDestinationTableName);
        LOG.info("truncateDestinationTable, Truncating table: {}", legacyDataDestinationTableName);
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.", legacyDataDestinationTableName);
        executeUpdate(query);
        LOG.info("truncateDestinationTable, Finished truncating table: {}", legacyDataDestinationTableName);
    }

    @Override
    public void storeCsvData(final String legacyDataDestinationTableName,
            final List<String> legacyDataDestinationTableColumns, final Iterator<String[]> csvIterator) {
        validateTableName(legacyDataDestinationTableName);
        validateColumnNames(legacyDataDestinationTableName, legacyDataDestinationTableColumns);
        final CuSqlQuery query = createBatchDataInsertionQuery(
                legacyDataDestinationTableName, legacyDataDestinationTableColumns);
        final int expectedRowLength = legacyDataDestinationTableColumns.size();

        LOG.info("storeCsvData, Storing CSV data in table: {}", legacyDataDestinationTableName);
        final int rowCount = storeCsvDataInBatches(csvIterator, query, expectedRowLength);
        LOG.info("storeCsvData, Finished storing {} CSV data rows in table: {}", rowCount, legacyDataDestinationTableName);
    }

    private void validateTableName(final String tableName) {
        Validate.validState(CemiUtils.valueOnlyContainsWordCharacters(tableName),
                "Input File Type has an invalid non-word-characters table name: %s", tableName);
    }

    private void validateColumnNames(final String tableName, final List<String> columnNames) {
        int index = 0;
        for (final String columnName : columnNames) {
            Validate.validState(CemiUtils.valueOnlyContainsWordCharacters(columnName),
                    "Column name at index %s for table %s contains non-word characters",
                    index, tableName);
            index++;
        }
    }

    private CuSqlQuery createBatchDataInsertionQuery(final String tableName, final List<String> columnNames) {
        return new CuSqlChunk()
                .append("INSERT INTO KFS.")
                .append(tableName)
                .append(" (")
                .append(CemiCuSqlChunk.asListingOfColumnNames(columnNames))
                .append(") VALUES (")
                .append(CemiCuSqlChunk.asListingOfStringArrayValuesForBatchUpdate(columnNames.size()))
                .append(")")
                .toQuery();
    }

    private int storeCsvDataInBatches(final Iterator<String[]> csvIterator, final CuSqlQuery query,
            final int expectedRowLength) {
        final List<String[]> batchedRows = new ArrayList<>();
        int rowNumber = 0;

        while (csvIterator.hasNext()) {
            rowNumber++;
            final String[] currentRow = csvIterator.next();
            validateRowLength(rowNumber, expectedRowLength, currentRow);
            batchedRows.add(currentRow);
            if (batchedRows.size() >= CemiBaseConstants.BULK_DATA_BATCH_SIZE) {
                executeBatchUpdate(query, batchedRows);
                batchedRows.clear();
            }
        }

        if (!batchedRows.isEmpty()) {
            executeBatchUpdate(query, batchedRows);
        }

        return rowNumber;
    }

    private void validateRowLength(final int rowNumber, final int expectedRowLength, final String[] csvRow) {
        Validate.validState(expectedRowLength == csvRow.length,
                "Row %s has the wrong number of columns; expected: %s, actual: %s",
                rowNumber, expectedRowLength, csvRow.length);
    }

}
