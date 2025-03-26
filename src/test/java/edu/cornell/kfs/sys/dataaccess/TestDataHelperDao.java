package edu.cornell.kfs.sys.dataaccess;

import java.io.IOException;
import java.util.List;

import edu.cornell.kfs.sys.util.CuSqlQuery;

public interface TestDataHelperDao {

    void execute(final CuSqlQuery sqlQuery);

    int executeUpdate(final CuSqlQuery sqlQuery);

    void forciblyCommitTransaction();

    void copyCsvFilesFromClasspathToDatabaseFolder(final List<String> filesToCopy) throws IOException;

    void splitAndConnectCsvFileToDatabase(final String fileName) throws IOException;

    String generateFileNameForTableDerivedFromCsvChunk(final String chunkedCsvFileName, final String tableName);

    void connectTableToCsvFileWithoutHeaderRow(final String tableName, final String fileName);

    void connectTableToCsvFileContainingHeaderRow(final String tableName, final String fileName);

    void disconnectTablesFromCsvFiles(final List<String> tableNames);

    void copyTableContents(final String sourceTableName, final String targetTableName);

    void forciblyEncryptColumns(final String tableName, final List<String> columnNames);

    void truncateTables(final List<String> tableNames);
}
