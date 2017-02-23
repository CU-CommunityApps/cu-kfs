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
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.dto.ConcurStandardAccountingExtractDTO;

public class ConcurStandardAccountingExtractToPDPStepTest {
	private static final String BATCH_DIRECTORY = "test/opt/work/staging/concur/standardAccountingExtract/";
	private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/concur/batch/fixture/ConcurStandardAccountingExtract.txt";

	private ConcurStandardAccountingExtractToPDPStep concurStandardAccountingExtractToPDPStep;
	private File batchDirectoryFile;
	private File dataFileSrc;

	@Before
	public void setUp() throws Exception {
		Logger.getLogger(ConcurStandardAccountingExtractToPDPStep.class).setLevel(Level.DEBUG);
		concurStandardAccountingExtractToPDPStep = new ConcurStandardAccountingExtractToPDPStep();
		concurStandardAccountingExtractToPDPStep
		        .setConcurStandardAccountingExtractService(new TestableConcurStandardAccountingExtractService());
		concurStandardAccountingExtractToPDPStep.setDirectoryPath(BATCH_DIRECTORY);

		batchDirectoryFile = new File(BATCH_DIRECTORY);
		batchDirectoryFile.mkdir();

		dataFileSrc = new File(DATA_FILE_PATH);
	}

	@After
	public void tearDown() throws Exception {
		concurStandardAccountingExtractToPDPStep = null;
		FileUtils.deleteDirectory(batchDirectoryFile);
	}

	@Test
	public void executeGoodNoFiles() throws InterruptedException {
		assertTrue(concurStandardAccountingExtractToPDPStep.execute("standardAccountExtractJob",
		        Calendar.getInstance().getTime()));
	}

	@Test
	public void executeGood1File() throws InterruptedException, IOException {
		prepFile("test1.txt");

		assertTrue(concurStandardAccountingExtractToPDPStep.execute("standardAccountExtractJob",
		        Calendar.getInstance().getTime()));
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

		assertTrue(concurStandardAccountingExtractToPDPStep.execute("standardAccountExtractJob",
		        Calendar.getInstance().getTime()));
		assertNumberOfFilesAccepted(5);
		assertNumberOfFilesRejected(0);
	}

	@Test
	public void executeBad1File() throws InterruptedException, IOException {
		TestableConcurStandardAccountingExtractService service = (TestableConcurStandardAccountingExtractService) concurStandardAccountingExtractToPDPStep
		        .getConcurStandardAccountingExtractService();
		service.setShouldThrowValidationExcpetion(true);

		prepFile("test1.txt");
		try {
			assertFalse(concurStandardAccountingExtractToPDPStep.execute("standardAccountExtractJob",
			        Calendar.getInstance().getTime()));
		} catch (RuntimeException re) {
			assertTrue("We expected a runtime exception", true);
		}
		assertNumberOfFilesAccepted(0);
		assertNumberOfFilesRejected(1);
	}

	protected void prepFile(String fileName) throws IOException {
		File dataFileDest = new File(BATCH_DIRECTORY + fileName);
		FileUtils.copyFile(dataFileSrc, dataFileDest);
	}

	public void assertNumberOfFilesAccepted(int numberOfExpectedFiles) {
		File acceptedDirectory = new File(BATCH_DIRECTORY + ConcurConstants.ACCEPT_SUB_FOLDER_NAME + ConcurConstants.FORWARD_SLASH);
		if (!acceptedDirectory.exists()) {
			acceptedDirectory.mkdir();
		}
		File[] listOfFiles = acceptedDirectory.listFiles();
		assertEquals("The number of files expected in the accept directory is not what we expected",
		        numberOfExpectedFiles, listOfFiles.length);
	}

	public void assertNumberOfFilesRejected(int numberOfExpectedFiles) {
		File rejectedDirectory = new File(BATCH_DIRECTORY + ConcurConstants.REJECT_SUB_FOLDER_NAME + ConcurConstants.FORWARD_SLASH);
		if (!rejectedDirectory.exists()) {
			rejectedDirectory.mkdir();
		}
		File[] listOfFiles = rejectedDirectory.listFiles();
		assertEquals("The number of files expected in the accept directory is not what we expected",
		        numberOfExpectedFiles, listOfFiles.length);
	}

	private class TestableConcurStandardAccountingExtractService implements ConcurStandardAccountingExtractService {
		private boolean shouldProccessSucceed = true;
		private boolean shouldThrowValidationExcpetion = false;

		@Override
		public List<ConcurStandardAccountingExtractDTO> parseStandardAccoutingExtractFileToStandardAccountingExtractDTO(
		        File standardAccountingExtractFile) throws ValidationException {
			if (shouldThrowValidationExcpetion) {
				throw new ValidationException("A validation exception");
			}
			List<ConcurStandardAccountingExtractDTO> dtos = new ArrayList<ConcurStandardAccountingExtractDTO>();
			return dtos;
		}

		@Override
		public boolean extractPdpFeedFromStandardAccounitngExtractDTOs(
		        List<ConcurStandardAccountingExtractDTO> concurStandardAccountingExtractDTOs) {
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
