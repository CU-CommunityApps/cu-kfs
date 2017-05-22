package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;

import edu.cornell.kfs.concur.batch.service.impl.fixture.EmailFileFixture;

public class ConcurBatchUtilityServiceImplTest {
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
        String actual = utilityService.getFileContents(EmailFileFixture.EMPTY_FILE.fullFilePath);
        String expected = EmailFileFixture.EMPTY_FILE.fileContents;
        assertEquals("An empty file should have no contents", expected, actual);
    }
    
    @Test
    public void getFileContents_simpleContentsFile() {
        String actual = utilityService.getFileContents(EmailFileFixture.SIMPLE_FILE.fullFilePath);
        assertEquals("The simple contents file should have formatted values", EmailFileFixture.SIMPLE_FILE.fileContents, actual);
    }

}
