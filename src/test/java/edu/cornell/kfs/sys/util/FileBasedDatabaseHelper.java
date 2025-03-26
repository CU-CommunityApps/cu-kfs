package edu.cornell.kfs.sys.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Utility bean to help manage file-based HyperSQL database directories before/after testing.
 * Also provides utility methods for connecting/disconnecting existing text tables.
 * 
 * Pre-test/post-test cleanup will be skipped if the database URL is misconfigured or if
 * some other database type (such as an in-memory HyperSQL database) is being used instead.
 * 
 * NOTE: In order for the file cleanup to succeed, the HyperSQL database MUST shut down
 * before this bean gets disposed. Specifying the "shutdown=true" setting in the database's
 * connection URL should be sufficient, as long as the DB connections are closed properly,
 * and as long as the datasource beans are configured to depend upon this bean.
 */
public class FileBasedDatabaseHelper implements InitializingBean, DisposableBean {

    private static final String HSQL_FILE_DATABASE_URL_PREFIX = "jdbc:hsqldb:file:";

    private final String databaseUrl;

    public FileBasedDatabaseHelper(final String databaseUrl) {
        this.databaseUrl = databaseUrl;
        System.setProperty("hsqldb.reconfig_logging", "false");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!databaseIsFileBased()) {
            return;
        }
        deleteDatabaseFilesIfPresent();
        createBaseDirectoryForDatabase();
    }

    public boolean databaseIsFileBased() {
        return StringUtils.startsWithIgnoreCase(databaseUrl, HSQL_FILE_DATABASE_URL_PREFIX);
    }

    private void createBaseDirectoryForDatabase() throws Exception {
        final String directoryPath = getBaseDirectoryPathForDatabase();
        final File baseDirectory = new File(directoryPath);
        FileUtils.forceMkdir(baseDirectory);
    }

    @Override
    public void destroy() throws Exception {
        if (!databaseIsFileBased()) {
            return;
        }
        deleteDatabaseFilesIfPresent();
    }

    private void deleteDatabaseFilesIfPresent() throws Exception {
        final String directoryPath = getBaseDirectoryPathForDatabase();
        final File baseDirectory = new File(directoryPath);
        if (baseDirectory.exists() && baseDirectory.isDirectory()) {
            FileUtils.forceDelete(baseDirectory.getAbsoluteFile());
        }
    }

    public String getBaseDirectoryPathForDatabase() {
        String result = StringUtils.substringAfter(databaseUrl, HSQL_FILE_DATABASE_URL_PREFIX);
        if (StringUtils.contains(result, CUKFSConstants.SEMICOLON)) {
            result = StringUtils.substringBefore(result, CUKFSConstants.SEMICOLON);
        }
        if (StringUtils.contains(result, CUKFSConstants.SLASH)) {
            result = StringUtils.substringBeforeLast(result, CUKFSConstants.SLASH) + CUKFSConstants.SLASH;
        }
        return result;
    }

}
