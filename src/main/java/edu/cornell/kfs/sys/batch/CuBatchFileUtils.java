package edu.cornell.kfs.sys.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import com.rsmart.kuali.kfs.sys.KFSConstants;

public class CuBatchFileUtils extends BatchFileUtils {

    public static List<File> retrieveBatchFileStagingRootDirectories() {
        ConfigurationService kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        List<File> directories = new ArrayList<File>();
        String configProperty = kualiConfigurationService.getPropertyValueAsString(KFSConstants.STAGING_DIRECTORY_KEY);

        String[] directoryNames = StringUtils.split(configProperty, ";");
        for (String directoryName : directoryNames) {
            File rootDirectory = new File(directoryName).getAbsoluteFile();
            directories.add(rootDirectory);
        }

        // sanity check: make sure directories are set up so that they will not present problems for pathRelativeToRootDirectory and
        // resolvePathToAbsolutePath methods
        for (int i = 0; i < directories.size(); i++) {
            for (int j = i + 1; j < directories.size(); j++) {
                File directoryI = directories.get(i);
                File directoryJ = directories.get(j);

                if (isPrefixOfAnother(directoryI.getAbsolutePath(), directoryJ.getAbsolutePath())) {
                    throw new RuntimeException("Cannot have any two directories in config property batch.file.lookup.root.directories that have absolute paths that are prefix of another");
                }
                if (isPrefixOfAnother(directoryI.getName(), directoryJ.getName())) {
                    throw new RuntimeException("Cannot have any two directories in config property batch.file.lookup.root.directories that have names that are prefix of another");
                }
            }
        }
        return directories;
    }

    public static byte[] safelyLoadFileBytes(String fileName) {
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(fileName);
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static void removeDoneFiles(List<String> dataFileNames) {

        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }
    
    private static boolean isPrefixOfAnother(String str1, String str2) {
        return str1.startsWith(str2) || str2.startsWith(str1);
    }
}
