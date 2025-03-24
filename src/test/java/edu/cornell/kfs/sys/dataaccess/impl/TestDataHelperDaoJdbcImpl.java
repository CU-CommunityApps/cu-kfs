package edu.cornell.kfs.sys.dataaccess.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.TriFunction;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.TestDataHelperDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.sys.util.TestDateUtils;

public class TestDataHelperDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements TestDataHelperDao {

    private enum CsvMode {
        READ_TABLE_NAME(TestDataHelperDaoJdbcImpl::processTableNameIfPresent),
        READ_COLUMN_MAPPINGS(TestDataHelperDaoJdbcImpl::processColumnMappings),
        READ_COLUMN_DATA(TestDataHelperDaoJdbcImpl::processColumnDataIfPresent);

        private TriFunction<TestDataHelperDaoJdbcImpl, DataWrapper, String[], CsvMode> csvLineHandler;

        private CsvMode(
                final TriFunction<TestDataHelperDaoJdbcImpl, DataWrapper, String[], CsvMode> csvLineHandler) {
            this.csvLineHandler = csvLineHandler;
        }
    }

    private static final String ENCRYPTED_MODE = "ENCRYPTED";
    private static final String UNESCAPED_MODE = "UNESCAPED";
    private static final String END_OF_TABLE_DATA_PREFIX = "END ";
    private static final int BATCH_SIZE = 50;



    private EncryptionService encryptionService;

    @Override
    public int runQuery(final CuSqlQuery query) throws SQLException {
        return executeUpdate(query, false);
    }

    @Override
    public void loadCsvDataIntoDatabase(final String filePath) throws IOException, SQLException {
        if (StringUtils.startsWith(filePath, ResourcePatternResolver.CLASSPATH_URL_PREFIX)) {
            loadCsvDataIntoDatabase(filePath, CuCoreUtilities::getResourceAsStream);
        } else {
            loadCsvDataIntoDatabase(filePath, FileInputStream::new);
        }
    }

    private void loadCsvDataIntoDatabase(final String filePath,
            final FailableFunction<String, InputStream, IOException> inputStreamFactory) throws IOException {
        try (
                final InputStream inputStream = inputStreamFactory.apply(filePath);
                final Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                final CSVReader csvReader = buildCsvReader(reader);
        ) {
            final DataWrapper dataWrapper = new DataWrapper();
            final Iterator<String[]> csvIterator = csvReader.iterator();
            CsvMode csvMode = CsvMode.READ_TABLE_NAME;

            for (final String[] csvLine : IteratorUtils.asIterable(csvIterator)) {
                csvMode = csvMode.csvLineHandler.apply(this, dataWrapper, csvLine);
            }

            if (csvMode == CsvMode.READ_COLUMN_DATA) {
                final String[] dummyEndLine = { END_OF_TABLE_DATA_PREFIX + dataWrapper.tableName };
                csvMode.csvLineHandler.apply(this, dataWrapper, dummyEndLine);
            }
        }
    }

    private CSVReader buildCsvReader(final Reader reader) {
        return new CSVReaderBuilder(reader)
                .build();
    }

    private CsvMode processTableNameIfPresent(final DataWrapper dataWrapper, final String[] csvLine) {
        if (csvLine.length == 0 || StringUtils.isBlank(csvLine[0])) {
            return CsvMode.READ_TABLE_NAME;
        }
        dataWrapper.tableName = csvLine[0];
        return CsvMode.READ_COLUMN_MAPPINGS;
    }

    private CsvMode processColumnMappings(final DataWrapper dataWrapper, final String[] csvLine) {
        final Stream.Builder<ColumnDefinition> columnDefinitions = Stream.builder();

        for (final String csvCell : csvLine) {
            final String[] columnMetadata = StringUtils.split(csvCell, CUKFSConstants.COLON);
            Validate.validState(columnMetadata.length >= 2, "Column definition cannot have less than 2 sub-items");
            final String columnName = columnMetadata[0];
            final JDBCType jdbcType = JDBCType.valueOf(columnMetadata[1]);
            final String specialMode = columnMetadata.length > 2 ? columnMetadata[2] : null;
            columnDefinitions.add(new ColumnDefinition(columnName, jdbcType, specialMode));
        }

        dataWrapper.columnDefinitions = columnDefinitions.build().toArray(ColumnDefinition[]::new);
        return CsvMode.READ_COLUMN_DATA;
    }

    private CsvMode processColumnDataIfPresent(final DataWrapper dataWrapper, final String[] csvLine) {
        if (csvLine.length == 1 && StringUtils.equals(csvLine[0], END_OF_TABLE_DATA_PREFIX + dataWrapper.tableName)) {
            insertIntoDatabase(dataWrapper);
            dataWrapper.reset();
            return CsvMode.READ_TABLE_NAME;
        }

        if (dataWrapper.parsedLines.size() >= BATCH_SIZE) {
            insertIntoDatabase(dataWrapper);
            dataWrapper.parsedLines.clear();
        }

        Object[] parsedLine = new Object[dataWrapper.columnDefinitions.length];
        for (int i = 0; i < parsedLine.length; i++) {
            final Object parsedValue = parseCsvCellData(dataWrapper.columnDefinitions[i], csvLine[i]);
            parsedLine[i] = parsedValue;
        }

        dataWrapper.parsedLines.add(parsedLine);
        return CsvMode.READ_COLUMN_DATA;
    }

    private Object parseCsvCellData(final ColumnDefinition columnDefinition, final String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        } else if (columnDefinition.encrypted) {
            try {
                return encryptionService.encrypt(value);
            } catch (final GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        } else if (columnDefinition.unescaped) {
            return value;
        }

        switch (columnDefinition.jdbcType) {
            case CHAR:
            case VARCHAR:
                return value;
            case INTEGER:
                return Integer.valueOf(value);
            case BIGINT:
                return Long.valueOf(value);
            case DECIMAL:
                return new BigDecimal(value);
            case DATE:
                return TestDateUtils.toSqlDate(value);
            case TIMESTAMP:
                return TestDateUtils.toSqlTimestamp(value);
            default:
                return value;
        }
    }

    private void insertIntoDatabase(final DataWrapper dataWrapper) {
        final CuSqlQuery query = buildBulkInsertionQuery(dataWrapper);
        executeUpdate(query, false);
    }

    private CuSqlQuery buildBulkInsertionQuery(final DataWrapper dataWrapper) {
        Validate.validState(StringUtils.isNotBlank(dataWrapper.tableName), "Table name was not specified");
        Validate.validState(ArrayUtils.isNotEmpty(dataWrapper.columnDefinitions),
                "Column mappings were not specified");
        Validate.validState(CollectionUtils.isNotEmpty(dataWrapper.parsedLines),
                "No data rows were specified");

        final String columnList = Arrays.stream(dataWrapper.columnDefinitions)
                .map(columnDefinition -> columnDefinition.columnName)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));

        final CuSqlChunk builder = new CuSqlChunk()
                .append("INSERT INTO ").append(dataWrapper.tableName).append(" (").append(columnList).append(") ")
                .append("WITH SRC_DATA (").append(columnList).append(") AS (");

        boolean firstRow = true;
        for (final Object[] parsedLine : dataWrapper.parsedLines) {
            builder.append(firstRow ? "SELECT " : " UNION ALL SELECT ");
            for (int i = 0; i < dataWrapper.columnDefinitions.length; i++) {
                if (i > 0) {
                    builder.append(CUKFSConstants.COMMA_AND_SPACE);
                }
                final ColumnDefinition columnDefinition = dataWrapper.columnDefinitions[i];
                if (columnDefinition.unescaped) {
                    builder.append(Objects.toString(parsedLine[i]));
                } else {
                    builder.appendAsParameter(columnDefinition.jdbcType.getVendorTypeNumber(), parsedLine[i]);
                }
            }
            builder.append(" FROM DUAL");
            firstRow = false;
        }

        builder.append(") SELECT * FROM SRC_DATA");
        return builder.toQuery();
    }

    public void setEncryptionService(final EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }



    private static final class DataWrapper {
        private String tableName;
        private ColumnDefinition[] columnDefinitions;
        private List<Object[]> parsedLines;

        public DataWrapper() {
            reset();
        }

        private void reset() {
            tableName = KFSConstants.EMPTY_STRING;
            columnDefinitions = null;
            parsedLines = new ArrayList<>();
        }
    }

    private static final class ColumnDefinition {
        private final String columnName;
        private final JDBCType jdbcType;
        private final boolean encrypted;
        private final boolean unescaped;

        private ColumnDefinition(final String columnName, final JDBCType jdbcType, final String specialMode) {
            this.columnName = columnName;
            this.jdbcType = jdbcType;
            this.encrypted = StringUtils.equalsIgnoreCase(specialMode, ENCRYPTED_MODE);
            this.unescaped = StringUtils.equalsIgnoreCase(specialMode, UNESCAPED_MODE);
        }
    }

}
