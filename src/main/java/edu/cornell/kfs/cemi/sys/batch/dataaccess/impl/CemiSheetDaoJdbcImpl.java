package edu.cornell.kfs.cemi.sys.batch.dataaccess.impl;

import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiColumnMetadata;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiTableMetadata;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;

public class CemiSheetDaoJdbcImpl extends CemiDaoBaseJdbc implements CemiSheetDao {

    private static final Logger LOG = LogManager.getLogger();

    private EncryptionService encryptionService;

    @Override
    public void insertSheetTableRows(final CemiTableMetadata metadata, final List<Object> rowObjects) {
        final CuSqlChunk query = new CuSqlChunk();
        final String columnListing = metadata.getColumns().stream()
                .map(CemiColumnMetadata::getColumnName)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));

        query.append("INSERT INTO ")
                .append(CemiBaseConstants.DATA_SCHEMA_PREFIX).append(metadata.getTableName())
                .append(" (").append(columnListing).append(") VALUES (");

        boolean firstColumn = true;
        for (final CemiColumnMetadata column : metadata.getColumns()) {
            if (!firstColumn) {
                query.append(CUKFSConstants.COMMA_AND_SPACE);
            }
            query.appendAsParameter(column.getJdbcType(),
                    rowObject -> getColumnValue(rowObject, column));
            firstColumn = false;
        }

        query.append(")");

        final int[] insertCounts = executeBatchUpdate(query.toQuery(), rowObjects);

        for (int i = 0; i < insertCounts.length; i++) {
            if (insertCounts[i] != 1) {
                LOG.warn("insertSheetTableRows, Batch insert for sheet data row "
                        + "at batch index {} should have inserted 1 row, but it actually inserted {} rows instead!",
                        i, insertCounts[i]);
            }
        }
    }

    private Object getColumnValue(final Object rowObject, final CemiColumnMetadata column) {
        if (column.isRepresentsStaticValue()) {
            return column.getStaticValue();
        }

        final Object propertyValue = ObjectUtils.getPropertyValue(rowObject, column.getDtoFieldName());
        if (column.isEncrypted()) {
            final String stringValue = (String) propertyValue;
            return StringUtils.isNotBlank(stringValue) ? encrypt(stringValue) : null;
        } else {
            return propertyValue;
        }
    }

    private String encrypt(final String value) {
        try {
            return encryptionService.encrypt(value);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private String decrypt(final String value) {
        try {
            return encryptionService.decrypt(value);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<String[]> getSheetTableRowsFormattedForFileOutput(final CemiTableMetadata metadata,
            final Map<String, Object> criteria, final List<String> orderByFields) {
        final CuSqlChunk query = new CuSqlChunk();
        final List<CemiColumnMetadata> columnsToSelect = metadata.getColumns().stream()
                .filter(CemiColumnMetadata::isIncludedInFileOutput)
                .collect(Collectors.toUnmodifiableList());

        appendColumnAndTableSelection(query, metadata, columnsToSelect);
        appendCriteria(query, metadata, criteria);
        appendOrderByClause(query, metadata, orderByFields);
        return queryForStream(query.toQuery(),
                (resultSet, rowNumber) -> readRowForFileOutput(resultSet, columnsToSelect));
    }

    private void appendColumnAndTableSelection(final CuSqlChunk query, final CemiTableMetadata metadata,
            final List<CemiColumnMetadata> columnsToSelect) {
        final String columnListing = columnsToSelect.stream()
                .map(CemiColumnMetadata::getColumnName)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));

        query.append("SELECT ").append(columnListing)
                .append(" FROM ").append(CemiBaseConstants.DATA_SCHEMA_PREFIX).append(metadata.getTableName());
    }

    private void appendCriteria(final CuSqlChunk query, final CemiTableMetadata metadata,
            final Map<String, Object> criteria) {
        boolean firstCriterion = true;
        query.append(" WHERE ");

        for (final Map.Entry<String, Object> criterion : criteria.entrySet()) {
            final String fieldName = criterion.getKey();
            final Object value = criterion.getValue();
            if (!firstCriterion) {
                query.append(" AND ");
            }
            appendCondition(query, fieldName, value);
            firstCriterion = false;
        }
    }

    private void appendCondition(final CuSqlChunk query, final String fieldName, final Object value) {
        final String columnName = CemiUtils.formatSheetColumnName(fieldName);
        final int jdbcType = determineJdbcType(fieldName, value);

        if (value instanceof List) {
            query.append(CuSqlChunk.asSqlInCondition(columnName, jdbcType, (List<?>) value));
        } else if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
            query.append(columnName).append(" IS NULL");
        } else {
            query.append(columnName).append(" = ").appendAsParameter(jdbcType, value);
        }
    }

    private int determineJdbcType(final String fieldName, final Object value) {
        if (value instanceof String) {
            return Types.VARCHAR;
        } else if (value instanceof Integer) {
            return Types.INTEGER;
        } else if (value instanceof Long) {
            return Types.BIGINT;
        } else if (value instanceof List) {
            final List<?> listValue = (List<?>) value;
            Validate.validState(!listValue.isEmpty(),
                    "List-based criteria values cannot be empty for field: %s", fieldName);
            return determineJdbcType(fieldName, listValue.get(0));
        } else {
            LOG.warn("determineJdbcType, Could not determine explicit JDBC type, defaulting to VARCHAR for field: %s",
                    fieldName);
            return Types.VARCHAR;
        }
    }

    private void appendOrderByClause(final CuSqlChunk query, final CemiTableMetadata metadata,
            final List<String> orderByFields) {
        final String columnListing = orderByFields.stream()
                .map(CemiUtils::formatSheetColumnName)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));
        query.append(" ORDER BY ").append(columnListing);
    }

    private String[] readRowForFileOutput(final ResultSet resultSet, final List<CemiColumnMetadata> columnsToSelect)
            throws SQLException {
        final Stream.Builder<String> cellValues = Stream.builder();

        for (final CemiColumnMetadata columnToSelect : columnsToSelect) {
            final String cellValue = StringUtils.defaultString(
                    resultSet.getString(columnToSelect.getColumnName()));
            if (columnToSelect.isEncrypted() && StringUtils.isNotBlank(cellValue)) {
                cellValues.add(decrypt(cellValue));
            } else {
                cellValues.add(cellValue);
            }
        }

        return cellValues.build().toArray(String[]::new);
    }

}
