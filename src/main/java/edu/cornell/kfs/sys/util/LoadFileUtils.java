package edu.cornell.kfs.sys.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadFileUtils {
    private static final Logger LOG = LogManager.getLogger(LoadFileUtils.class);
    
    public LoadFileUtils() {
        throw new IllegalAccessError("This utility class as static methods, you should not instantiate this object.");
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
            try {
                IOUtils.closeQuietly(fileContents);
            } catch (Exception e) {
                LOG.error("safelyLoadFileBytes, unable tlose input stream.", e);
            }
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
}
