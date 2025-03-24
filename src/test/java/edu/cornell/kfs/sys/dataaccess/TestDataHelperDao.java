package edu.cornell.kfs.sys.dataaccess;

import java.io.IOException;
import java.sql.SQLException;

import edu.cornell.kfs.sys.util.CuSqlQuery;

public interface TestDataHelperDao {

    public static final String BEAN_NAME = "testDataHelperDao";

    int runQuery(final CuSqlQuery query) throws SQLException;

    void loadCsvDataIntoDatabase(final String filePath) throws IOException, SQLException;

}
