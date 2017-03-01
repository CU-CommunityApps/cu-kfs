package edu.cornell.kfs.concur.batch;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;

public class ConcurStandardAccountingExtractToPdpStepTest {
    private static final String BATCH_DIRECTORY = "test/opt/work/staging/concur/standardAccountingExtract/";
    private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/concur/batch/fixture/ConcurStandardAccountingExtract.txt";

    private ConcurStandardAccountingExtractToPdpStep concurStandardAccountingExtractToPdpStep;
    private File dataFileSrc;

    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurStandardAccountingExtractToPdpStep.class).setLevel(Level.DEBUG);
        concurStandardAccountingExtractToPdpStep = new ConcurStandardAccountingExtractToPdpStep();
        concurStandardAccountingExtractToPdpStep.setConcurStandardAccountingExtractService(new TestableConcurStandardAccountingExtractService());
        setupDirectories();

        dataFileSrc = new File(DATA_FILE_PATH);
    }

    protected void setupDirectories() {
        concurStandardAccountingExtractToPdpStep.setIncomingDirectoryName(BATCH_DIRECTORY);
        concurStandardAccountingExtractToPdpStep.setAcceptedDirectoryName(BATCH_DIRECTORY + "/accept");
        concurStandardAccountingExtractToPdpStep.setRejectedDirectoryName(BATCH_DIRECTORY + "/reject");
        
        makeDirectoryIfNotExist(concurStandardAccountingExtractToPdpStep.getIncomingDirectoryName());
        makeDirectoryIfNotExist(concurStandardAccountingExtractToPdpStep.getAcceptedDirectoryName());
        makeDirectoryIfNotExist(concurStandardAccountingExtractToPdpStep.getRejectedDirectoryName());
    }
    
    private void makeDirectoryIfNotExist(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File(concurStandardAccountingExtractToPdpStep.getIncomingDirectoryName()));
        concurStandardAccountingExtractToPdpStep = null;
    }

    @Test
    public void executeGoodNoFiles() throws InterruptedException {
        assertTrue(concurStandardAccountingExtractToPdpStep.execute("standardAccountExtractJob", Calendar.getInstance().getTime()));
    }

    @Test
    public void executeGood1File() throws InterruptedException, IOException {
        prepFile("test1.txt");

        assertTrue(concurStandardAccountingExtractToPdpStep.execute("standardAccountExtractJob",Calendar.getInstance().getTime()));
        assertNumberOfFilesAccepted(1);
        assertNumberOfFilesRejected(0);
    }

    @Test
    public void executeGoodMultipleFile() throws InterruptedException, IOException {
        prepFile("test1.txt");
        prepFile("test2.txt");
        prepFile("test3.txt");
        prepFile("test4.txt");
        prepFile("test5.txt");

        assertTrue(concurStandardAccountingExtractToPdpStep.execute("standardAccountExtractJob", Calendar.getInstance().getTime()));
        assertNumberOfFilesAccepted(5);
        assertNumberOfFilesRejected(0);
    }

    @Test
    public void executeBad1File() throws InterruptedException, IOException {
        TestableConcurStandardAccountingExtractService service = (TestableConcurStandardAccountingExtractService) concurStandardAccountingExtractToPdpStep
                .getConcurStandardAccountingExtractService();
        service.setShouldThrowValidationExcpetion(true);

        prepFile("test1.txt");
        assertFalse(concurStandardAccountingExtractToPdpStep.execute("standardAccountExtractJob", Calendar.getInstance().getTime()));
        assertNumberOfFilesAccepted(0);
        assertNumberOfFilesRejected(1);
    }
    
    @Test
    public void executeNoTextFiles() throws InterruptedException, IOException {
        prepFile("foo.bar");
        prepFile("some_excel_file.xls");

        assertTrue(concurStandardAccountingExtractToPdpStep.execute("standardAccountExtractJob", Calendar.getInstance().getTime()));
        assertNumberOfFilesAccepted(0);
        assertNumberOfFilesRejected(0);
    }

    protected void prepFile(String fileName) throws IOException {
        File dataFileDest = new File(BATCH_DIRECTORY + fileName);
        FileUtils.copyFile(dataFileSrc, dataFileDest);
    }

    public void assertNumberOfFilesAccepted(int numberOfExpectedFiles) {
        File acceptedDirectory = new File(concurStandardAccountingExtractToPdpStep.getAcceptedDirectoryName());
        File[] listOfFiles = acceptedDirectory.listFiles();
        String message = "The number of files expected in the accept directory is not what we expected ";
        for (File file : listOfFiles) {
            message = message + "  File: " + file.getAbsolutePath();
        }
        assertEquals(message, numberOfExpectedFiles, listOfFiles.length);
    }

    public void assertNumberOfFilesRejected(int numberOfExpectedFiles) {
        File rejectedDirectory = new File(concurStandardAccountingExtractToPdpStep.getRejectedDirectoryName());
        File[] listOfFiles = rejectedDirectory.listFiles();
        String message = "The number of files expected in the reject directory is not what we expected ";
        for (File file : listOfFiles) {
            message = message + "  File: " + file.getAbsolutePath();
        }
        assertEquals(message, numberOfExpectedFiles, listOfFiles.length);
    }

    private class TestableConcurStandardAccountingExtractService implements ConcurStandardAccountingExtractService {
        private boolean shouldProccessSucceed = true;
        private boolean shouldThrowValidationExcpetion = false;

        @Override
        public ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFileToStandardAccountingExtractFile(
                File standardAccountingExtractFile) throws ValidationException {
            if (shouldThrowValidationExcpetion) {
                throw new ValidationException("A validation exception");
            }
            return new ConcurStandardAccountingExtractFile();
        }

        @Override
        public boolean extractPdpFeedFromStandardAccounitngExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
            return shouldProccessSucceed;
        }

        public void setShouldProccessSucceed(boolean shouldProccessSucceed) {
            this.shouldProccessSucceed = shouldProccessSucceed;
        }

        public void setShouldThrowValidationExcpetion(boolean shouldThrowValidationExcpetion) {
            this.shouldThrowValidationExcpetion = shouldThrowValidationExcpetion;
        }

    }

}
