package edu.cornell.kfs.sys.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import com.rsmart.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;

public class CuBatchFileUtils {
    private static ConfigurationService configurationService;

    public static List<File> retrieveBatchFileStagingRootDirectories() {
        final List<File> directories = new ArrayList<File>();
        final String configProperty = getConfigurationService().getPropertyValueAsString(KFSConstants.STAGING_DIRECTORY_KEY);

        final String[] directoryNames = StringUtils.split(configProperty, ";");
        for (final String directoryName : directoryNames) {
            final File rootDirectory = new File(directoryName).getAbsoluteFile();
            directories.add(rootDirectory);
        }

        // sanity check: make sure directories are set up so that they will not present problems for pathRelativeToRootDirectory and
        // resolvePathToAbsolutePath methods
        for (int i = 0; i < directories.size(); i++) {
            for (int j = i + 1; j < directories.size(); j++) {
                final File directoryI = directories.get(i);
                final File directoryJ = directories.get(j);

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
    
    private static boolean isPrefixOfAnother(final String str1, final String str2) {
        return str1.startsWith(str2) || str2.startsWith(str1);
    }
    
    public static String getFileNameWithoutPathOrExtension(final String fileName) {
        String result = getFileNameWithoutPath(fileName);
        result = StringUtils.substringBefore(result, org.kuali.kfs.sys.KFSConstants.DELIMITER);
        return result;
    }

    public static String getFileNameWithoutPath(final String fileName) {
        String result = fileName;
        if (StringUtils.contains(result, CUKFSConstants.SLASH)) {
            result = StringUtils.substringAfterLast(fileName, CUKFSConstants.SLASH);
        }
        if (StringUtils.contains(result, CUKFSConstants.BACKSLASH)) {
            result = StringUtils.substringAfterLast(result, CUKFSConstants.BACKSLASH);
        }
        return result;
    }

    private static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }
}
