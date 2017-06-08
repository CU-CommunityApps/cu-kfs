package edu.cornell.kfs.sys.batch;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.sys.CUKFSConstants;

public class MoveFileStepTest {

    protected static final String COPYING_SOURCE_PATH = "src/test/java/edu/cornell/kfs/sys/batch/fixture";
    protected static final String BASE_TEST_DIRECTORY_PATH = "test/sys";
    protected static final String TESTING_SOURCE_PATH = BASE_TEST_DIRECTORY_PATH + "/sourceFolder";
    protected static final String TARGET_PATH = BASE_TEST_DIRECTORY_PATH + "/targetFolder";

    protected static final String FIRST_TEST_FILE = "firstTestFile.txt";
    protected static final String SECOND_TEST_FILE = "secondTestFile.properties";
    protected static final String THIRD_TEST_FILE = "thirdTestFile.txt";

    protected static final String MATCH_ALL_PATTERN = "^.+$";
    protected static final String MATCH_FIRST_FILE_PATTERN = "^first.*$";
    protected static final String MATCH_SECOND_FILE_PATTERN = "^second.*$";
    protected static final String MATCH_TEXT_FILE_PATTERN = "^[^\\.]+\\.txt$";
    protected static final String MATCH_NO_FILES_PATTERN = "^nonExistentFile\\.xslt$";

    protected static final String TEST_JOB_NAME = "testMoveJob";
    protected static final DateTime TEST_DATE = new DateTime(2017, 3, 1, 0, 0);
    protected static final boolean FILES_WERE_MOVED = true;
    protected static final boolean FILES_WERE_NOT_MOVED = false;

    protected MoveFileStep moveFileStep;

    @Before
    public void setUp() throws Exception {
        moveFileStep = new MoveFileStep();
        moveFileStep.setSourcePath(TESTING_SOURCE_PATH);
        moveFileStep.setTargetPath(TARGET_PATH);
        
        createDirectories(TESTING_SOURCE_PATH, TARGET_PATH);
    }

    @After
    public void tearDown() throws Exception {
        removeTestingFilesAndDirectories(BASE_TEST_DIRECTORY_PATH, TESTING_SOURCE_PATH, TARGET_PATH);
    }

    @Test
    public void testMoveAllFiles() throws Exception {
        copyFilesToSourceDirectory(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        initializeBatchStep(MATCH_ALL_PATTERN);
        runBatchStep();
        assertFilesWereMoved(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        assertDirectoryIsEmpty(TESTING_SOURCE_PATH);
    }

    @Test
    public void testMoveAllFilesWhenUsingTrailingSlashesOnDirectoryPaths() throws Exception {
        moveFileStep.setSourcePath(TESTING_SOURCE_PATH + CUKFSConstants.SLASH);
        moveFileStep.setTargetPath(TARGET_PATH + CUKFSConstants.SLASH);
        copyFilesToSourceDirectory(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        initializeBatchStep(MATCH_ALL_PATTERN);
        runBatchStep();
        assertFilesWereMoved(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        assertDirectoryIsEmpty(TESTING_SOURCE_PATH);
    }

    @Test
    public void testMoveSingleFileMatchingPattern() throws Exception {
        copyFilesToSourceDirectory(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        initializeBatchStep(MATCH_FIRST_FILE_PATTERN);
        runBatchStep();
        assertFilesWereMoved(FIRST_TEST_FILE);
        assertFilesWereNotMoved(SECOND_TEST_FILE, THIRD_TEST_FILE);
    }

    @Test
    public void testMoveSingleFileMatchingDifferentPattern() throws Exception {
        copyFilesToSourceDirectory(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        initializeBatchStep(MATCH_SECOND_FILE_PATTERN);
        runBatchStep();
        assertFilesWereMoved(SECOND_TEST_FILE);
        assertFilesWereNotMoved(FIRST_TEST_FILE, THIRD_TEST_FILE);
    }

    @Test
    public void testMoveMultipleFilesMatchingPattern() throws Exception {
        copyFilesToSourceDirectory(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        initializeBatchStep(MATCH_TEXT_FILE_PATTERN);
        runBatchStep();
        assertFilesWereMoved(FIRST_TEST_FILE, THIRD_TEST_FILE);
        assertFilesWereNotMoved(SECOND_TEST_FILE);
    }

    @Test
    public void testNoFilesAreMovedWhenPatternIsNotMatched() throws Exception {
        copyFilesToSourceDirectory(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        initializeBatchStep(MATCH_NO_FILES_PATTERN);
        runBatchStep();
        assertFilesWereNotMoved(FIRST_TEST_FILE, SECOND_TEST_FILE, THIRD_TEST_FILE);
        assertDirectoryIsEmpty(TARGET_PATH);
    }

    @Test
    public void testNoFilesAreMovedWhenSourceDirectoryIsEmpty() throws Exception {
        assertDirectoryIsEmpty(TESTING_SOURCE_PATH);
        initializeBatchStep(MATCH_ALL_PATTERN);
        runBatchStep();
        assertDirectoryIsEmpty(TESTING_SOURCE_PATH);
        assertDirectoryIsEmpty(TARGET_PATH);
    }

    @Test
    public void testCannotSetInvalidRegexOnBatchStep() throws Exception {
        assertCannotSetInvalidRegex(null);
        assertCannotSetInvalidRegex("[2}");
    }

    @Test
    public void testCannotInitializeBatchStepWithoutRegex() throws Exception {
        // The regex has not been configured yet at this stage of execution.
        assertCallFails(this::finishBatchStepInitialization);
    }

    @Test
    public void testCannotInitializeBatchStepWithBlankDirectoryPaths() throws Exception {
        moveFileStep.setFileNamePattern(MATCH_ALL_PATTERN);
        
        List<String> blankValues = Arrays.asList(null, StringUtils.EMPTY, KRADConstants.BLANK_SPACE);
        
        assertCallFailsAfterSettingInvalidPath(blankValues, this::finishBatchStepInitialization);
    }

    @Test
    public void testCannotRunBatchStepWithInvalidDirectoryPaths() throws Exception {
        moveFileStep.setFileNamePattern(MATCH_ALL_PATTERN);
        
        List<String> invalidPaths = Arrays.asList(
                "test/&&&&\\%%%%",
                "test/nonExistentDirectory",
                TESTING_SOURCE_PATH + CUKFSConstants.SLASH + FIRST_TEST_FILE);
        
        assertCallFailsAfterSettingInvalidPath(invalidPaths, () -> {
            finishBatchStepInitialization();
            return runBatchStep();
        });
    }

    protected void assertFilesWereMoved(String... fileNames) throws Exception {
        assertFileState(FILES_WERE_MOVED, fileNames);
    }

    protected void assertFilesWereNotMoved(String... fileNames) throws Exception {
        assertFileState(FILES_WERE_NOT_MOVED, fileNames);
    }

    protected void assertFileState(boolean fileMoveExpected, String... fileNames) throws Exception {
        String yesOrNoText = (fileMoveExpected ? "should" : "should not");
        String fileMoveAssertionMessageFormat = "The file \"%s\" %s have been moved to the target directory.";
        String doneFileAssertionMessageFormat = " The file \"%s\" %s have been created.";
        
        for (String fileName : fileNames) {
            String doneFileName = StringUtils.substringBeforeLast(fileName, CUKFSConstants.FILE_EXTENSION_DELIMITER)
                    + CUKFSConstants.DONE_FILE_EXTENSION;
            String fileMoveAssertionMessage = String.format(fileMoveAssertionMessageFormat, fileName, yesOrNoText);
            String doneFileAssertionMessage = String.format(doneFileAssertionMessageFormat, doneFileName, yesOrNoText);
            
            File sourceFile = new File(TESTING_SOURCE_PATH + CUKFSConstants.SLASH + fileName);
            File targetFile = new File(TARGET_PATH + CUKFSConstants.SLASH + fileName);
            File doneFile = new File(TARGET_PATH + CUKFSConstants.SLASH + doneFileName);
            
            assertTrue(fileMoveAssertionMessage, fileMoveExpected != sourceFile.exists());
            assertTrue(fileMoveAssertionMessage, fileMoveExpected == targetFile.exists());
            assertTrue(doneFileAssertionMessage, fileMoveExpected == doneFile.exists());
            
            if (fileMoveExpected) {
                assertTrue("The moved file should have been non-empty: " + fileName, FileUtils.sizeOf(targetFile) > 0L);
                assertTrue("The .done file should have been empty: " + doneFileName, FileUtils.sizeOf(doneFile) == 0L);
            }
        }
    }

    protected void assertDirectoryIsEmpty(String directoryPath) throws Exception {
        File directory = new File(directoryPath);
        Collection<File> files = FileUtils.listFiles(directory, null, false);
        assertTrue("The directory should have been empty: " + directoryPath, files.isEmpty());
    }

    protected void assertCannotSetInvalidRegex(String regex) throws Exception {
        assertCallFails(() -> {
            moveFileStep.setFileNamePattern(regex);
            return null;
        });
    }

    protected void assertCallFailsAfterSettingInvalidPath(List<String> invalidValues, Callable<?> callable) throws Exception {
        List<Consumer<String>> setters = Arrays.<Consumer<String>>asList(
                moveFileStep::setSourcePath, moveFileStep::setTargetPath);
        
        for (Consumer<String> setter : setters) {
            for (String invalidValue : invalidValues) {
                setter.accept(invalidValue);
                assertCallFails(callable);
            }
            moveFileStep.setSourcePath(TESTING_SOURCE_PATH);
            moveFileStep.setTargetPath(TARGET_PATH);
        }
    }

    protected void assertCallFails(Callable<?> callable) throws Exception {
        try {
            callable.call();
            fail("The operation should have thrown an exception but didn't");
        } catch (Exception e) {
            // Ignore; the operation is expected to throw an exception.
        }
    }

    protected void initializeBatchStep(String regex) throws Exception {
        moveFileStep.setFileNamePattern(regex);
        finishBatchStepInitialization();
    }

    // This method has a return value (though null) to conform to the Callable interface's method signature.
    protected Object finishBatchStepInitialization() throws Exception {
        moveFileStep.afterPropertiesSet();
        return null;
    }

    protected boolean runBatchStep() throws Exception {
        return moveFileStep.execute(TEST_JOB_NAME, TEST_DATE.toDate());
    }

    protected void createDirectories(String... directoryPaths) throws IOException {
        for (String directoryPath : directoryPaths) {
            File directory = new File(directoryPath);
            FileUtils.forceMkdir(directory);
        }
    }

    protected void copyFilesToSourceDirectory(String... fileNames) throws IOException {
        for (String fileName : fileNames) {
            File fileToCopy = new File(COPYING_SOURCE_PATH + CUKFSConstants.SLASH + fileName);
            File fileForProcessing = new File(TESTING_SOURCE_PATH + CUKFSConstants.SLASH + fileName);
            FileUtils.copyFile(fileToCopy, fileForProcessing);
        }
    }

    protected void removeTestingFilesAndDirectories(String baseDirectoryPath, String... immediateSubDirectoryPaths) throws Exception {
        removeImmediateSubDirectories(immediateSubDirectoryPaths);
        removeDirectoriesInPath(baseDirectoryPath);
    }

    protected void removeImmediateSubDirectories(String... subDirectoryPaths) throws Exception {
        for (String directoryPath : subDirectoryPaths) {
            File testDirectory = new File(directoryPath);
            if (testDirectory.exists() && testDirectory.isDirectory()) {
                for (File testFile : testDirectory.listFiles()) {
                    testFile.delete();
                }
                testDirectory.delete();
            }
        }
    }

    protected void removeDirectoriesInPath(String directoryPath) throws Exception {
        if (StringUtils.endsWith(directoryPath, CUKFSConstants.SLASH)) {
            directoryPath = directoryPath.substring(0, directoryPath.length() - 1);
        }
        
        if (StringUtils.isBlank(directoryPath)) {
            return;
        }
        
        int endIndex = directoryPath.length();
        while (endIndex != -1) {
            File tempDirectory = new File(directoryPath.substring(0, endIndex));
            tempDirectory.delete();
            endIndex = directoryPath.lastIndexOf(CUKFSConstants.SLASH, endIndex - 1);
        }
    }

}
