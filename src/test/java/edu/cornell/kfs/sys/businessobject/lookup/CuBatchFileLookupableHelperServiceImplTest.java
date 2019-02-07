package edu.cornell.kfs.sys.businessobject.lookup;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.rice.krad.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuBatchFileLookupableHelperServiceImplTest {

    public static final String USER_DIR = "user.dir";
    public static final String FILE_SEPARATOR = "file.separator";
    public static final String TARGET_ROOT = "target/test-classes/";
    public static final String FIXTURE_SUB1_PATH = "edu/cornell/kfs/sys/businessobject/lookup/fixture/sub1";
    public static final String FIXTURE_SUB2_PATH = "edu/cornell/kfs/sys/businessobject/lookup/fixture/sub2";

    private LookupableHelperService cuLookupableHelperService;
    private List<File> rootDirectories;
    private List<String> fileNames;
    private String userDir;
    private String fileSeparator;
    private String filePrefix;

    @Before
    public void setUp() throws IOException {
        userDir = System.getProperty(USER_DIR);
        fileSeparator = System.getProperty(FILE_SEPARATOR);
        filePrefix = userDir + fileSeparator + TARGET_ROOT;

        cuLookupableHelperService = new TestableCuBatchFileLookupableHelperServiceImpl();
        rootDirectories = ((TestableCuBatchFileLookupableHelperServiceImpl)cuLookupableHelperService).retrieveRootDirectories();
        fileNames = setupFileNames(rootDirectories);
    }

    private List<String> setupFileNames(List<File> rootDirectories) throws IOException {
        List<String> fileNames = new ArrayList<>();

        for (File rootDirectory : rootDirectories) {
            for (String fileName: FileUtils.getFileNames(rootDirectory, null, null, true)) {

                final int lastIndexOfFileSeparator = fileName.lastIndexOf(fileSeparator);
                if (lastIndexOfFileSeparator > 0) {
                    fileNames.add(fileName.substring(lastIndexOfFileSeparator + 1));
                } else {
                    fileNames.add(fileName);
                }
            }
        }

        return fileNames;
    }

    @Test
    public void getSearchResults() {
        Map<String, String> fieldValues = new HashMap<>();

        List<BatchFile> searchResults = (List<BatchFile>) cuLookupableHelperService.getSearchResults(fieldValues);

        Assert.assertEquals("size of the search results weren't equal as expected", fileNames.size(), searchResults.size());
        checkResults(searchResults);
    }

    @Test
    public void getSearchResultsSubdirectorySelected() throws IOException {
        Map<String, String> fieldValues = new HashMap<>();
        String[] selectedPaths = new String[]{FIXTURE_SUB1_PATH};

        validateSearchResults(fieldValues, selectedPaths, 2);
    }

    @Test
    public void getSearchResultsMultipleSubdirectoriesSelected() throws IOException {
        Map<String, String> fieldValues = new HashMap<>();
        String[] selectedPaths = new String[]{FIXTURE_SUB1_PATH, FIXTURE_SUB2_PATH};

        validateSearchResults(fieldValues, selectedPaths, 4);
    }

    private void validateSearchResults(Map<String, String> fieldValues, String[] selectedPaths, int expectedFileCount) throws IOException {
        ((TestableCuBatchFileLookupableHelperServiceImpl)cuLookupableHelperService).setSelectedPaths(selectedPaths);
        fileNames = setupFileNames(((TestableCuBatchFileLookupableHelperServiceImpl)cuLookupableHelperService).getSelectedDirectories(selectedPaths));

        List<BatchFile> searchResults = (List<BatchFile>) cuLookupableHelperService.getSearchResults(fieldValues);

        Assert.assertEquals("size of the search results weren't equal as expected", fileNames.size(), searchResults.size());
        Assert.assertEquals("size of the search results weren't equal as expected", expectedFileCount, searchResults.size());
        checkResults(searchResults);
    }

    private void checkResults(List<BatchFile> searchResults) {
        for (BatchFile batchFile: searchResults) {
            Assert.assertTrue("file [" + batchFile.getFileName() + "] was in lookupable results, but not base results", fileNames.contains(batchFile.getFileName()));
        }
    }

    private class TestableCuBatchFileLookupableHelperServiceImpl extends CuBatchFileLookupableHelperServiceImpl {

        public static final String FP_PATH = "edu/cornell/kfs/fp";
        public static final String SYS_PATH = "edu/cornell/kfs/sys";
        private String[] selectedPaths;

        protected List<File> retrieveRootDirectories() {
            List<File> rootDirectories = new ArrayList<>();

            rootDirectories.add(new File(filePrefix + FP_PATH));
            rootDirectories.add(new File(filePrefix + SYS_PATH));
            
            return rootDirectories;
        }

        protected List<File> getSelectedDirectories(String[] selectedPaths) {
            List<File> selectedDirectories = new ArrayList<>();

            if (ObjectUtils.isNotNull(getSelectedPaths())) {
                for (String selectedPath: getSelectedPaths()) {
                    selectedDirectories.add(new File(filePrefix + selectedPath));
                }
            }

            return selectedDirectories;
        }

        protected String[] getSelectedPaths() {
            return selectedPaths;
        }

        public void setSelectedPaths(String[] selectedPaths) {
            this.selectedPaths = selectedPaths;
        }
    }
}