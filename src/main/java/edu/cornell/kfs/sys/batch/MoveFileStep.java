package edu.cornell.kfs.sys.batch;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Generic batch step for moving files from one directory to another.
 * 
 * At a minimum, the sourcePath and targetPath must be set to valid pre-existing directories,
 * and the fileNamePattern must be set to a valid Java regex String. Only files whose names
 * match the regex will be moved from the source directory to the target directory.
 * This step will also automatically create ".done" files in the target directory for each moved file.
 * 
 * The source/target directory paths are not required to have trailing slashes. This step
 * will automatically add them as needed.
 */
public class MoveFileStep extends AbstractStep {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MoveFileStep.class);

    protected String sourcePath;
    protected String targetPath;
    protected Pattern fileNamePattern;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(sourcePath)) {
            throw new IllegalStateException("sourcePath cannot be blank");
        } else if (StringUtils.isBlank(targetPath)) {
            throw new IllegalStateException("targetPath cannot be blank");
        } else if (fileNamePattern == null) {
            throw new IllegalStateException("fileNamePattern cannot be null");
        }
        
        super.afterPropertiesSet();
    }

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        return moveFiles();
    }

    protected boolean moveFiles() {
        verifyDirectoriesExist(sourcePath, targetPath);
        
        try {
            File sourceDirectory = new File(sourcePath);
            Matcher fileNameMatcher = fileNamePattern.matcher(StringUtils.EMPTY);
            int numFilesMoved = 0;
            
            LOG.info("Moving files from " + sourcePath + " to " + targetPath);
            
            for (File sourceFile : FileUtils.listFiles(sourceDirectory, null, false)) {
                fileNameMatcher.reset(sourceFile.getName());
                if (fileNameMatcher.matches()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Moving file: " + sourceFile.getName());
                    }
                    moveFileAndMarkAsReady(sourceFile);
                    numFilesMoved++;
                }
            }
            
            LOG.info("File move completed.");
            LOG.info(Integer.toString(numFilesMoved) + " file(s) have been moved to the target directory.");
        } catch (Exception e) {
            LOG.error("Encountered error while moving files", e);
            return false;
        }
        
        return true;
    }

    protected void verifyDirectoriesExist(String... directoryPaths) {
        for (String directoryPath : directoryPaths) {
            File directory = new File(directoryPath);
            if (!directory.exists() || !directory.isDirectory()) {
                throw new IllegalStateException("Directory does not exist or does not represent a directory path: " + directoryPath);
            }
        }
    }

    protected void moveFileAndMarkAsReady(File sourceFile) throws IOException {
        String targetFilePath = buildFilePath(targetPath, sourceFile.getName());
        File targetFile = new File(targetFilePath);
        FileUtils.moveFile(sourceFile, targetFile);
        
        String doneFilePath = StringUtils.substringBeforeLast(targetFilePath, CUKFSConstants.FILE_EXTENSION_DELIMITER)
                + CUKFSConstants.DONE_FILE_EXTENSION;
        File doneFile = new File(doneFilePath);
        FileUtils.touch(doneFile);
    }

    protected String buildFilePath(String prefix, String fileName) {
        String actualPrefix = prefix;
        if (!actualPrefix.endsWith(CUKFSConstants.SLASH)) {
            actualPrefix += CUKFSConstants.SLASH;
        }
        return actualPrefix + fileName;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public void setFileNamePattern(String fileNamePattern) {
        try {
            this.fileNamePattern = Pattern.compile(fileNamePattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid fileNamePattern syntax", e);
        }
    }

}
