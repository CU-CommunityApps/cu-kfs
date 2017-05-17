package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.resources.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;

public class ConcurBatchUtilityServiceImplTest {
    
    private static final String EMPTY_FILE_PATH = "src/test/resources/edu/cornell/kfs/concur/batch/service/impl/fixture/empty.txt";
    private static final String SIMPLE_CONTENTS_FILE_PATH = "src/test/resources/edu/cornell/kfs/concur/batch/service/impl/fixture/simpleContents.txt";
    
    ConcurBatchUtilityServiceImpl utilityService;

    @Before
    public void setUp() throws Exception {
        utilityService = new ConcurBatchUtilityServiceImpl();
        FileSystemFileStorageServiceImpl fileService = new FileSystemFileStorageServiceImpl();
        fileService.setPathPrefix(StringUtils.EMPTY);
        utilityService.setFileStorageService(fileService);
    }

    @After
    public void tearDown() throws Exception {
        utilityService = null;
    }

    @Test
    public void getFileContents_nonFile() {
        String actual = utilityService.getFileContents("foo");
        String expected = StringUtils.EMPTY;
        assertEquals("A null file should have no contents", expected, actual);
    }
    
    @Test
    public void getFileContents_emptyFile() {
        String actual = utilityService.getFileContents(EMPTY_FILE_PATH);
        String expected = StringUtils.EMPTY;
        assertEquals("An empty file should have no contents", expected, actual);
    }
    
    @Test
    public void getFileContents_simpleContentsFile() {
        String actual = utilityService.getFileContents(SIMPLE_CONTENTS_FILE_PATH);
        StringBuilder sb = new StringBuilder("This is the first line.").append(KFSConstants.NEWLINE);
        sb.append("\t").append("This is the second line indented.");
        String expected = sb.toString();
        assertEquals("The simple contents file should have formatted values", expected, actual);
    }

}
