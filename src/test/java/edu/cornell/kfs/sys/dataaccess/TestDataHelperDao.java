package edu.cornell.kfs.sys.dataaccess;

import java.io.IOException;
import java.util.List;

import edu.cornell.kfs.sys.util.CuSqlQuery;

/**
 * Unit/Integration-test-only utility service for running SQL, managing CSV-based text tables
 * in HyperSQL databases, and performing test-grade encryption/decryption of data values.
 */
public interface TestDataHelperDao {

    void execute(final CuSqlQuery sqlQuery);

    int executeUpdate(final CuSqlQuery sqlQuery);

    void forciblyCommitTransaction();

    void copyCsvFilesFromClasspathToDatabaseFolder(final List<String> filesToCopy) throws IOException;

    /**
     * Convenience method for splitting a chunked CSV file into multiple smaller CSV files
     * and connecting them to existing HSQL text tables. Each CSV chunk should start with
     * "<<<<START TABLE_NAME>>>>", where TABLE_NAME is the qualified name of the text table
     * that the CSV content should be connected to. (That starter line can optionally be suffixed
     * with "[[[[ENCRYPT LIST_OF_COL_NAMES]]]]", where LIST_OF_COL_NAMES is a semicolon-delimited
     * list of columns that will have their values forcibly encrypted.) Each chunk should have
     * a line of "<<<<END>>>>" as an end marker. The lines between these start and end markers
     * will be added to a separate generated file.
     * 
     * It is assumed that the first CSV line between each start/end marker pair is a header line.
     * Also, any content outside of a start/end marker pair will be ignored, allowing for comments
     * to be added to the files.
     */
    void splitAndConnectCsvFileToDatabase(final String fileName) throws IOException;

    String generateFileNameForTableDerivedFromCsvChunk(final String chunkedCsvFileName, final String tableName);

    void connectTableToCsvFileWithoutHeaderRow(final String tableName, final String fileName);

    void connectTableToCsvFileContainingHeaderRow(final String tableName, final String fileName);

    void disconnectTablesFromCsvFiles(final List<String> tableNames);

    void copyTableContents(final String sourceTableName, final String targetTableName);

    void forciblyEncryptColumns(final String tableName, final List<String> columnNames);

    void truncateTables(final List<String> tableNames);

    String encrypt(final String value);

    String decrypt(final String value);
}
