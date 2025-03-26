package edu.cornell.kfs.sys.dataaccess.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.core.io.support.ResourcePatternResolver;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.dataaccess.TestDataHelperDao;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.sys.util.FileBasedDatabaseHelper;

public class TestDataHelperDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements TestDataHelperDao {

    private static final String TEMP_FILES_SUB_FOLDER = "tempfiles/";
    private static final String CSV_CHUNK_START_MARKER_PREFIX = "<<<<START ";
    private static final String CSV_CHUNK_START_MARKER_SUFFIX = ">>>>";
    private static final String CSV_CHUNK_COPY_MARKER_PREFIX = "[[[[COPY TO ";
    private static final String CSV_CHUNK_COPY_MARKER_SUFFIX = "]]]]";
    private static final String CSV_CHUNK_END_MARKER = "<<<<END>>>>";
    private static final Pattern FILENAME_CHARS_TO_REPLACE = Pattern.compile("[\\.\\-]");
    private static final Pattern DISALLOWED_NAME_CHARS = Pattern.compile("[^\\w\\.]");

    private EncryptionService encryptionService;
    private FileBasedDatabaseHelper databaseHelper;

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        createKfsSchema();
        forceUtf8EncodingForTextTables();
    }

    private void createKfsSchema() {
        final CuSqlQuery query = CuSqlQuery.of("CREATE SCHEMA KFS");
        execute(query);
    }

    private void forceUtf8EncodingForTextTables() {
        final CuSqlQuery query = CuSqlQuery.of("SET DATABASE TEXT TABLE DEFAULTS 'encoding=UTF-8'");
        execute(query);
    }

    @Override
    public void execute(final CuSqlQuery sqlQuery) {
        execute(sqlQuery, preparedStatement -> {
            preparedStatement.execute();
            return null;
        });
    }

    @Override
    public int executeUpdate(final CuSqlQuery sqlQuery) {
        return super.executeUpdate(sqlQuery);
    }

    @Override
    public void forciblyCommitTransaction() {
        execute(CuSqlQuery.of("COMMIT"));
    }

    @Override
    public void copyCsvFilesFromClasspathToDatabaseFolder(final List<String> filesToCopy) throws IOException {
        Validate.validState(databaseHelper.databaseIsFileBased(),
                "The current test is not running a file-based database");
        final String directoryPath = databaseHelper.getBaseDirectoryPathForDatabase();
        for (final String fileToCopy : filesToCopy) {
            final String sourcePath = !StringUtils.startsWith(fileToCopy, ResourcePatternResolver.CLASSPATH_URL_PREFIX)
                    ? ResourcePatternResolver.CLASSPATH_URL_PREFIX + fileToCopy : fileToCopy;
            final String fileName = CuBatchFileUtils.getFileNameWithoutPath(fileToCopy);
            try (final InputStream sourceStream = CuCoreUtilities.getResourceAsStream(sourcePath)) {
                final File destinationFile = new File(directoryPath + TEMP_FILES_SUB_FOLDER + fileName);
                FileUtils.copyToFile(sourceStream, destinationFile);
            }
        }
    }

    @Override
    public void splitAndConnectCsvFileToDatabase(final String fileName) throws IOException {
        final String sourcePath = !StringUtils.startsWith(fileName, ResourcePatternResolver.CLASSPATH_URL_PREFIX)
                ? ResourcePatternResolver.CLASSPATH_URL_PREFIX + fileName : fileName;

        try (
                final InputStream sourceStream = CuCoreUtilities.getResourceAsStream(sourcePath);
                final InputStreamReader streamReader = new InputStreamReader(sourceStream, StandardCharsets.UTF_8);
                final BufferedReader reader = new BufferedReader(streamReader);
        ) {
            CsvChunk csvChunk = null;

            for (String fileLine = reader.readLine(); fileLine != null; fileLine = reader.readLine()) {
                if (csvChunk == null) {
                    if (StringUtils.startsWith(fileLine, CSV_CHUNK_START_MARKER_PREFIX)) {
                        csvChunk = initializeCsvChunkTracker(fileLine);
                    }
                } else if (StringUtils.equals(fileLine, CSV_CHUNK_END_MARKER)) {
                    copyAndConnectCsvFileChunkToDatabase(fileName, csvChunk.tableName, csvChunk.csvContent);
                    if (StringUtils.isNotBlank(csvChunk.copyTableName)) {
                        copyTableContents(csvChunk.tableName, csvChunk.copyTableName);
                    }
                    csvChunk = null;
                } else {
                    csvChunk.csvContent.append(fileLine).append(KFSConstants.NEWLINE);
                }
            }

            Validate.validState(csvChunk == null,
                    "File %s contained mismatched start/end markers for its various CSV chunks", fileName);
        }
    }

    private CsvChunk initializeCsvChunkTracker(final String csvMarkerLine) {
        final String tableName = StringUtils.substringBetween(csvMarkerLine, CSV_CHUNK_START_MARKER_PREFIX,
                CSV_CHUNK_START_MARKER_SUFFIX);
        final String copyTableName;
        if (StringUtils.contains(csvMarkerLine, CSV_CHUNK_COPY_MARKER_PREFIX)) {
            copyTableName = StringUtils.substringBetween(csvMarkerLine,
                    CSV_CHUNK_COPY_MARKER_PREFIX, CSV_CHUNK_COPY_MARKER_SUFFIX);
        } else {
            copyTableName = KFSConstants.EMPTY_STRING;
        }
        return new CsvChunk(tableName, copyTableName);
    }

    private void copyAndConnectCsvFileChunkToDatabase(final String fileName, final String tableName,
            final CharSequence csvContent) throws IOException {
        final String destinationFileName = generateFileNameForTableDerivedFromCsvChunk(fileName, tableName);
        final File destinationFile = new File(destinationFileName);
        FileUtils.write(destinationFile, csvContent, StandardCharsets.UTF_8, false);
        connectTableToCsvFileContainingHeaderRow(tableName, destinationFileName);
    }

    @Override
    public String generateFileNameForTableDerivedFromCsvChunk(
                final String chunkedCsvFileName, final String tableName) {
        final String bareFileName = CuBatchFileUtils.getFileNameWithoutPathOrExtension(chunkedCsvFileName);
        final String adjustedFileName = FILENAME_CHARS_TO_REPLACE.matcher(bareFileName)
                .replaceAll(CUKFSConstants.UNDERSCORE);
        final String directoryPath = databaseHelper.getBaseDirectoryPathForDatabase();
        final String tableNameForFile = StringUtils.replace(
                tableName, KFSConstants.DELIMITER, CUKFSConstants.UNDERSCORE);
        return StringUtils.join(directoryPath, TEMP_FILES_SUB_FOLDER, adjustedFileName,
                CUKFSConstants.UNDERSCORE, tableNameForFile, FileExtensions.CSV);
    }

    @Override
    public void connectTableToCsvFileWithoutHeaderRow(final String tableName, final String fileName) {
        connectTableToCsvFile(tableName, fileName, false);
    }

    @Override
    public void connectTableToCsvFileContainingHeaderRow(final String tableName, final String fileName) {
        connectTableToCsvFile(tableName, fileName, true);
    }

    private void connectTableToCsvFile(final String tableName, final String fileName, boolean fileHasHeaderRow) {
        final String cleanedTableName = cleanName(tableName);
        final String simpleFileName = CuBatchFileUtils.getFileNameWithoutPath(fileName);
        final String cleanedFileName = cleanName(simpleFileName);
        final String filePath = TEMP_FILES_SUB_FOLDER + cleanedFileName;
        final String fileOptions = fileHasHeaderRow ? ";ignore_first=true" : KFSConstants.EMPTY_STRING;

        final CuSqlQuery query = CuSqlQuery.of(
                "SET TABLE ", cleanedTableName, " SOURCE \"", filePath, fileOptions, "\"");
        execute(query);
    }

    @Override
    public void disconnectTablesFromCsvFiles(final List<String> tableNames) {
        for (final String tableName : tableNames) {
            final String cleanedTableName = cleanName(tableName);
            final CuSqlQuery query = CuSqlQuery.of("SET TABLE ", cleanedTableName, " SOURCE OFF");
            execute(query);
        }
    }

    @Override
    public void copyTableContents(final String sourceTableName, final String targetTableName) {
        final String cleanedSourceTableName = cleanName(sourceTableName);
        final String cleanedTargetTableName = cleanName(targetTableName);
        final CuSqlQuery query = CuSqlQuery.of("INSERT INTO ", cleanedTargetTableName,
                " SELECT * FROM ", cleanedSourceTableName);
        executeUpdate(query);
    }

    @Override
    public void forciblyEncryptColumns(final String tableName, final List<String> columnNames) {
        final String cleanedTableName = cleanName(tableName);
        final String[] cleanedColumnNames = columnNames.stream()
                .map(this::cleanName)
                .toArray(String[]::new);

        /*executeUpdate(CuSqlQuery.of(
            "MERGE INTO KFS.TX_TRANSACTION_DETAIL_T TBL1 USING (",
                    "SELECT IRS_1099_1042S_DETAIL_ID, 'AA' \"DOC_TYPE\" FROM KFS.TX_TRANSACTION_DETAIL_T",
            ") TBL2 ",
            "ON (TBL1.IRS_1099_1042S_DETAIL_ID = TBL2.IRS_1099_1042S_DETAIL_ID) ",
            "WHEN MATCHED THEN UPDATE SET TBL1.DOC_TYPE = TBL2.DOC_TYPE"
        ));*/

        final CuSqlQuery query = CuSqlQuery.of("SELECT TBL1.* FROM ", cleanedTableName, " TBL1");

        queryForUpdatableResults(query, resultSet -> {
            while (resultSet.next()) {
                for (final String columnName : cleanedColumnNames) {
                    final String oldValue = resultSet.getString(columnName);
                    final String newValue = encrypt(oldValue);
                    resultSet.updateString(columnName, newValue);
                }
                resultSet.updateRow();
            }
            return null;
        });
    }

    private String encrypt(final String value) {
        try {
            return encryptionService.encrypt(value);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void truncateTables(final List<String> tableNames) {
        for (final String tableName : tableNames) {
            final String cleanedTableName = cleanName(tableName);
            final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE ", cleanedTableName);
            execute(query);
        }
    }

    private String cleanName(final String name) {
        return DISALLOWED_NAME_CHARS.matcher(name).replaceAll(KFSConstants.EMPTY_STRING);
    }

    public void setEncryptionService(final EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public void setDatabaseHelper(final FileBasedDatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }



    private static final class CsvChunk {
        public final StringBuilder csvContent;
        public final String tableName;
        public final String copyTableName;

        public CsvChunk(final String tableName, final String copyTableName) {
            this.csvContent = new StringBuilder(1000);
            this.tableName = tableName;
            this.copyTableName = copyTableName;
        }
    }

}
