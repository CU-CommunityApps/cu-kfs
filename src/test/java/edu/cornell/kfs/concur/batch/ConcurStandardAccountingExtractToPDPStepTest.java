package edu.cornell.kfs.concur.batch;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.exception.ValidationException;


import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.dto.ConcurStandardAccountingExtractDTO;

public class ConcurStandardAccountingExtractToPDPStepTest {
	
	private ConcurStandardAccountingExtractToPDPStep concurStandardAccountingExtractToPDPStep;

	@Before
	public void setUp() throws Exception {
		Logger.getLogger(ConcurStandardAccountingExtractToPDPStep.class).setLevel(Level.DEBUG);
		concurStandardAccountingExtractToPDPStep = new ConcurStandardAccountingExtractToPDPStep();
		concurStandardAccountingExtractToPDPStep.setConcurStandardAccountingExtractService(new TestableConcurStandardAccountingExtractService());
		concurStandardAccountingExtractToPDPStep.setDirectoryPath("/infra/work/staging/concur/standardAccountingExtract/");
	}

	@After
	public void tearDown() throws Exception {
		concurStandardAccountingExtractToPDPStep = null;
	}

	@Test
	public void executeGood() throws InterruptedException {
		TestableConcurStandardAccountingExtractService service = (TestableConcurStandardAccountingExtractService) 
				concurStandardAccountingExtractToPDPStep.getConcurStandardAccountingExtractService();
		service.setShouldProccessSucceed(true);
		assertTrue(concurStandardAccountingExtractToPDPStep.execute("standardAccountExtractJob", Calendar.getInstance().getTime()));
	}
	
	private class TestableConcurStandardAccountingExtractService implements ConcurStandardAccountingExtractService {
		private boolean shouldProccessSucceed;

		@Override
		public List<ConcurStandardAccountingExtractDTO> parseStandardAccoutingExtractFile(File standardAccountingExtractFile) throws ValidationException {
			List<ConcurStandardAccountingExtractDTO> dtos = new ArrayList<ConcurStandardAccountingExtractDTO>();
			return dtos;
		}

		@Override
		public boolean proccessConcurStandardAccountExtractDTOs(List<ConcurStandardAccountingExtractDTO> concurStandardAccountingExtractDTOs) {
			return isShouldProccessSucceed();
		}

		public boolean isShouldProccessSucceed() {
			return shouldProccessSucceed;
		}

		public void setShouldProccessSucceed(boolean shouldProccessSucceed) {
			this.shouldProccessSucceed = shouldProccessSucceed;
		}
		
	}

}
