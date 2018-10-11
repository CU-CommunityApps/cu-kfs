package edu.cornell.kfs.sys.util;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import edu.cornell.kfs.concur.batch.service.impl.fixture.EmailFileFixture;

public class LoadFileUtilsTest {

    @Test
    public void testConstructor() {
        try {
            LoadFileUtils util = new LoadFileUtils();
            fail("Should not be able to initiate");
        } catch (IllegalAccessError iae) {
        }
    }

    @Test
    public void testLoadSimpleFileByFileName() {
        byte[] simpleContents = LoadFileUtils.safelyLoadFileBytes(EmailFileFixture.SIMPLE_FILE.fullFilePath);
        assertEquals(EmailFileFixture.SIMPLE_FILE.fileContents, new String(simpleContents));
    }
    
    @Test
    public void testLoadEmptyFileByFileName() {
        byte[] emptyContents = LoadFileUtils.safelyLoadFileBytes(EmailFileFixture.EMPTY_FILE.fullFilePath);
        assertEquals(EmailFileFixture.EMPTY_FILE.fileContents, new String(emptyContents));
    }
    
    @Test
    public void testNullFileByFileName() {
        try {
            byte[] nullContents = LoadFileUtils.safelyLoadFileBytes(StringUtils.EMPTY);
            fail("An empty file name should throw a run time axpection as the file can't be found ");
        } catch (RuntimeException re) {
        }
    }

    @Test
    public void testSafelyLoadSimpleFileBytesFile() {
        File simpleFile = new File(EmailFileFixture.SIMPLE_FILE.fullFilePath);
        byte[] simpleContents = LoadFileUtils.safelyLoadFileBytes(simpleFile);
        assertEquals(EmailFileFixture.SIMPLE_FILE.fileContents, new String(simpleContents));
    }
    
    @Test
    public void testSafelyLoadEmptyFileBytesFile() {
        File emptyFile = new File(EmailFileFixture.EMPTY_FILE.fullFilePath);
        byte[] emptyFileContents = LoadFileUtils.safelyLoadFileBytes(emptyFile);
        assertEquals(EmailFileFixture.EMPTY_FILE.fileContents, new String(emptyFileContents));
    }
    
    @Test
    public void testSafelyLoadNullFileBytesFile() {
        try {
            File nullFile = null;
            byte[] nullFIleContents = LoadFileUtils.safelyLoadFileBytes(nullFile);
            fail("Expected to cause illegal argument exception, but did not.");
        } catch (IllegalArgumentException iae) {
        }
    }

}
