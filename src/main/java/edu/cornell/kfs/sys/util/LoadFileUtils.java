package edu.cornell.kfs.sys.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadFileUtils {
    private static final Logger LOG = LogManager.getLogger(LoadFileUtils.class);
    
    public LoadFileUtils() {
        throw new IllegalAccessError("This utility class has static methods, you should not instantiate this object.");
    }
    
    public static byte[] safelyLoadFileBytes(String fullyQualifiedFileName) {
        InputStream fileContents;
        byte[] fileByteContent;
        try {
            fileContents = new FileInputStream(fullyQualifiedFileName);
        } catch (FileNotFoundException e1) {
            LOG.error("safelyLoadFileBytes:  Batch file not found [" + fullyQualifiedFileName + "]. " + e1.getMessage());
            throw new RuntimeException("Batch File not found [" + fullyQualifiedFileName + "]. " + e1.getMessage(), e1);
        }
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        } catch (IOException e1) {
            LOG.error("safelyLoadFileBytes:  IO Exception loading: [" + fullyQualifiedFileName + "]. " + e1.getMessage());
            throw new RuntimeException("IO Exception loading: [" + fullyQualifiedFileName + "]. " + e1.getMessage(), e1);
        } finally {
            IOUtils.closeQuietly(fileContents);
        }
        return fileByteContent;
    }
    
    public static byte[] safelyLoadFileBytes(File file) {
        if (file == null) {
            LOG.error("safelyLoadFileBytes, a NULL file was provided");
            throw new IllegalArgumentException("A file must be provided");
        }
        return safelyLoadFileBytes(file.getAbsolutePath());
    }
    
    /**
     * Returns the string contents of a file.  If the file can not be loaded, an empty string is returned.
     * @param fullyQualifiedFileName
     * @return
     */
    public static String safelyLoadFileString(String fullyQualifiedFileName) {
        try {
            byte[] fileByteArray = safelyLoadFileBytes(fullyQualifiedFileName);
            String formattedString = new String(fileByteArray);
            return formattedString;
        } catch (RuntimeException e) {
            LOG.error("safelyLoadFileString, unable to read the file.", e);
            return StringUtils.EMPTY;
        }
    }
}
