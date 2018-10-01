package edu.cornell.kfs.sys.util;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.concur.batch.service.impl.fixture.EmailFileFixture;

public class LoadFileUtilsTest {

    @Test
    public void testLoadFileUtils() {
        try {
            LoadFileUtils util = new LoadFileUtils();
        } catch (IllegalAccessError iae) {
            assertTrue(true);
            return;
        }
        assertTrue("Should not be able to initiate ", false);
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
        } catch (RuntimeException re) {
            assertTrue(true);
            return; 
        }
        assertTrue("An empty file name should throw a run time axpection as the file can't be found ", false);
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
        } catch (IllegalArgumentException iae) {
            assertTrue(true);
            return;
        }
        assertTrue("Expected to cause illegal argument exception, but did not.", false);
    }

}
