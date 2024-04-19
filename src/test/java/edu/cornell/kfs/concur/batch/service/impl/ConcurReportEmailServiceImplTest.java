package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurEmailableReportData;
import edu.cornell.kfs.concur.batch.service.impl.fixture.EmailFileFixture;

public class ConcurReportEmailServiceImplTest {
    private static final String CONCUR_FILE_NAME = "extract_travel_request_t0025645jsau_20170427121117.txt";
    private static final String REPORT_TYPE = "request extract";
    private static final String BASE_SUBJECT = "The request extract file extract_travel_request_t0025645jsau_20170427121117.txt has been processed.";
    private static final String HEADER_VALIDATION_SUBJECT = "  There are header validation errors.";
    private static final String LINE_VALIDATON_SUBJECT = "  There are line level validation errors.";
    
    private ConcurReportEmailServiceImpl concurReportEmailServiceImpl;
    private TestableConcurEmailableReportData reportData;

    @Before
    public void setUp() throws Exception {
        concurReportEmailServiceImpl = new ConcurReportEmailServiceImpl();
        concurReportEmailServiceImpl.setConcurBatchUtilityService(new ConcurBatchUtilityServiceImpl());
        
        List<String> headerValidationErrors = new ArrayList<String>();
        List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines = new ArrayList<ConcurBatchReportLineValidationErrorItem>();
        reportData = new TestableConcurEmailableReportData(CONCUR_FILE_NAME, REPORT_TYPE, headerValidationErrors, validationErrorFileLines);
    }

    @After
    public void tearDown() throws Exception {
        concurReportEmailServiceImpl = null;
    }

    @Test
    public void readReportFileToString_noContentFile() {
        File emptyFile = new File(EmailFileFixture.EMPTY_FILE.fullFilePath);
        String expected = "Could not read the request extract file.";
        String actual = concurReportEmailServiceImpl.readReportFileToString(reportData, emptyFile);
        assertEquals("shouldn't find any lines in the empty file.", expected, actual);
    }
    
    @Test
    public void readReportFileToString_nullFile() {
        File emptyFile = new File("foo.xml");
        String expected = "Could not read the request extract file.";
        String actual = concurReportEmailServiceImpl.readReportFileToString(reportData, emptyFile);
        assertEquals("shouldn't find any lines in a NULL file.", expected, actual);
    }
    
    @Test
    public void readReportFileToString_populatedFile() {
        File emptyFile = new File(EmailFileFixture.SIMPLE_FILE.fullFilePath);
        String expected = EmailFileFixture.SIMPLE_FILE.fileContents;
        String actual = concurReportEmailServiceImpl.readReportFileToString(reportData, emptyFile);
        assertEquals("simiple contents file should contain the expected contents.", expected, actual);
    }

    @Test
    public void buildEmailSubject() {
        String actual = concurReportEmailServiceImpl.buildEmailSubject(reportData);
        String expected = BASE_SUBJECT;
        assertEquals(expected, actual);
    }
    
    @Test
    public void buildEmailSubject_headerErrors() {
        addHeaderErrorToReportData("an error occured to do testing");
        String actual = concurReportEmailServiceImpl.buildEmailSubject(reportData);
        String expected = BASE_SUBJECT + HEADER_VALIDATION_SUBJECT;
        assertEquals(expected, actual);
    }
    
    @Test
    public void buildEmailSubject_lineValidation() {
        addLineErrorToReportData();
        String actual = concurReportEmailServiceImpl.buildEmailSubject(reportData);
        String expected = BASE_SUBJECT + LINE_VALIDATON_SUBJECT;
        assertEquals(expected, actual);
    }
    
    @Test
    public void buildEmailSubject_headerAndLineValidation() {
        addLineErrorToReportData();
        addHeaderErrorToReportData("an error occured to do testing");
        String actual = concurReportEmailServiceImpl.buildEmailSubject(reportData);
        String expected = BASE_SUBJECT + HEADER_VALIDATION_SUBJECT + LINE_VALIDATON_SUBJECT;
        assertEquals(expected, actual);
    }
    
    private void addHeaderErrorToReportData(String errorMessage) {
        reportData.getHeaderValidationErrors().add(errorMessage);
    }
    
    private void  addLineErrorToReportData() {
        ConcurBatchReportLineValidationErrorItem item = new ConcurBatchReportLineValidationErrorItem();
        item.addItemErrorResult("Invalid debit +999.99");
        reportData.getValidationErrorFileLines().add(item);
    }
    
    private class TestableConcurEmailableReportData implements ConcurEmailableReportData {
        private String concurFileName;
        private String reportTypeName;
        private List<String> headerValidationErrors;
        private List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines;
        
        public TestableConcurEmailableReportData(String concurFileName, String reportTypeName, List<String> headerValidationErrors, 
                List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines) {
            this.concurFileName = concurFileName;
            this.reportTypeName = reportTypeName;
            this.headerValidationErrors = headerValidationErrors;
            this.validationErrorFileLines = validationErrorFileLines;
        }

        @Override
        public String getConcurFileName() {
            return concurFileName;
        }

        @Override
        public List<String> getHeaderValidationErrors() {
            return headerValidationErrors;
        }

        @Override
        public List<ConcurBatchReportLineValidationErrorItem> getValidationErrorFileLines() {
            return validationErrorFileLines;
        }

        @Override
        public String getReportTypeName() {
            return reportTypeName;
        }
        
    }

}
