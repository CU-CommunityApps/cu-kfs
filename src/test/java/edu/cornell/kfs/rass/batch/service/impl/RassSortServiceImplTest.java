package edu.cornell.kfs.rass.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapperMarshalTest;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class RassSortServiceImplTest {
    private static final String AGENCY_BASIC_TEST_FILE_NAME = RassXmlDocumentWrapperMarshalTest.RASS_EXAMPLE_FILE_BASE_PATH + "rass-agency-basic-test.xml";
    private static final String AGENCIES_WITH_REPORTS_TO_AGENCY_NOT_IN_FILE_FILE_NAME = RassXmlDocumentWrapperMarshalTest.RASS_EXAMPLE_FILE_BASE_PATH + "rass-agencies-with-reports-agancy-not-in-file.xml";
    private static final String SINGLE_AWARD_FILE = RassXmlDocumentWrapperMarshalTest.RASS_EXAMPLE_FILE_BASE_PATH + "rass_single_award_only.xml";
    
    private CUMarshalService cuMarshalService;
    private RassSortServiceImpl rassSortServiceImpl;

    @Before
    public void setUp() throws Exception {
        Configurator.setLevel(RassSortServiceImpl.class.getName(), Level.DEBUG);
        cuMarshalService = new CUMarshalServiceImpl();
        rassSortServiceImpl = new RassSortServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        cuMarshalService = null;
        rassSortServiceImpl = null;
    }
    
    @Test
    public void basicAgencyFileBeforeSortTest() throws JAXBException {
        File xmlFile = new File(AGENCY_BASIC_TEST_FILE_NAME);
        RassXmlDocumentWrapper rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        
        SortAgenciesTestResults results = buildSortAgenciesTestResults(rassXmlDocumentWrapper.getAgencies());
        assertTrue("There shoulld be some agencies before their reports to agency before the sort", results.failedAgencies.size() > 0);
    }
    
    @Test
    public void basicAgencyFileAfterSortTest() throws JAXBException {
        File xmlFile = new File(AGENCY_BASIC_TEST_FILE_NAME);
        RassXmlDocumentWrapper rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        
        int preSortCount = rassXmlDocumentWrapper.getAgencies().size();
        List<RassXmlAgencyEntry> sortedAgencyEntries = rassSortServiceImpl.sortRassXmlAgencyEntriesForUpdate(rassXmlDocumentWrapper.getAgencies());
        int postSortCount = sortedAgencyEntries.size();
        
        assertListCountsMatch(preSortCount, postSortCount);
        
        SortAgenciesTestResults results = buildSortAgenciesTestResults(sortedAgencyEntries);
        assertTrue("There should be no agencies before their reports to agency after the sort", results.failedAgencies.size() == 0);
    }

    private void assertListCountsMatch(int preSortCount, int postSortCount) {
        assertEquals("The sort should return the same number of agencies as was passed in", preSortCount, postSortCount);
    }
    
    @Test
    public void testSingleAwardFile() throws JAXBException {
        File xmlFile = new File(SINGLE_AWARD_FILE);
        RassXmlDocumentWrapper rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        
        int preSortCount = rassXmlDocumentWrapper.getAgencies().size();
        List<RassXmlAgencyEntry> sortedAgencyEntries = rassSortServiceImpl.sortRassXmlAgencyEntriesForUpdate(rassXmlDocumentWrapper.getAgencies());
        int postSortCount = sortedAgencyEntries.size();
        
        assertListCountsMatch(preSortCount, postSortCount);
        assertEquals("There shouldn't be any agencies in this test", 0, postSortCount);
    }
    
    @Test
    public void testAgenciesWithReportsToAgencyNotInFile() throws JAXBException {
        File xmlFile = new File(AGENCIES_WITH_REPORTS_TO_AGENCY_NOT_IN_FILE_FILE_NAME);
        RassXmlDocumentWrapper rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        
        int preSortCount = rassXmlDocumentWrapper.getAgencies().size();
        List<RassXmlAgencyEntry> sortedAgencyEntries = rassSortServiceImpl.sortRassXmlAgencyEntriesForUpdate(rassXmlDocumentWrapper.getAgencies());
        int postSortCount = sortedAgencyEntries.size();
        
        assertListCountsMatch(preSortCount, postSortCount);
        
        String[] expectedAgencyNumbers = {"Gamma", "Alpha", "Delta", "Beta"};
        
        assertEquals("expected count should match actualCount", expectedAgencyNumbers.length, sortedAgencyEntries.size());
        
        int i = 0;
        for (RassXmlAgencyEntry entry : sortedAgencyEntries) {
            assertEquals("Agency index " + i + " should equal expected value", expectedAgencyNumbers[i], entry.getNumber());
            i++;
        }
        
    }
    
    private SortAgenciesTestResults buildSortAgenciesTestResults(List<RassXmlAgencyEntry> agencies) {
        SortAgenciesTestResults results = new SortAgenciesTestResults();
        
        for (RassXmlAgencyEntry agency : agencies) {
            if (StringUtils.isNotBlank(agency.getReportsToAgencyNumber())) {
                if (results.processedAgencies.contains(agency.getReportsToAgencyNumber())) {
                    results.processedAgencies.add(agency.getNumber());
                } else {
                    results.failedAgencies.add(agency.getNumber());
                }
            } else {
                results.processedAgencies.add(agency.getNumber());
            }
        }
        
        return results;
    }
    
    private class SortAgenciesTestResults {
        public List<String> processedAgencies;
        public List<String> failedAgencies;
        
        SortAgenciesTestResults() {
            processedAgencies = new ArrayList<String>();
            failedAgencies = new ArrayList<String>();
        }
        
    }
    
    @Test
    public void testSortRassXmlFileParseResultSimpleFileNames() {
        String a = "a";
        String b = "b";
        String c = "c";
        List<RassXmlFileParseResult> results = buildRassXmlFileParseResultList(c, b, a);
        
        assertEquals(a, results.get(2).getRassXmlFileName());
        
        List<RassXmlFileParseResult> sortedResults = rassSortServiceImpl.sortRassXmlFileParseResult(results);
        
        assertEquals(a, sortedResults.get(0).getRassXmlFileName());
        assertEquals(b, sortedResults.get(1).getRassXmlFileName());
        assertEquals(c, sortedResults.get(2).getRassXmlFileName());
    }
    
    @Test
    public void testSortRassXmlFileParseResultRealLifeFileNames() {
        String fullExtractFileName = "kfs.xml";
        String sept8FileName = "rass_20190908044608.xml";
        String sept9FileName = "rass_20190909044604.xml";
        String sept10FileName = "rass_20190910044609.xml";
        String aug10FileName = "rass_20190810044609.xml";
        
        List<RassXmlFileParseResult> results = buildRassXmlFileParseResultList(aug10FileName, sept10FileName, sept9FileName, sept8FileName,fullExtractFileName);
        assertEquals(aug10FileName, results.get(0).getRassXmlFileName());
        
        List<RassXmlFileParseResult> sortedResults = rassSortServiceImpl.sortRassXmlFileParseResult(results);
        
        assertEquals(fullExtractFileName, sortedResults.get(0).getRassXmlFileName());
        assertEquals(aug10FileName, sortedResults.get(1).getRassXmlFileName());
        assertEquals(sept8FileName, sortedResults.get(2).getRassXmlFileName());
        assertEquals(sept9FileName, sortedResults.get(3).getRassXmlFileName());
        assertEquals(sept10FileName, sortedResults.get(4).getRassXmlFileName());
        
    }
    
    private List<RassXmlFileParseResult> buildRassXmlFileParseResultList(String... fileNames) {
        List<RassXmlFileParseResult> results = new ArrayList<RassXmlFileParseResult>();
        for (String fileName : fileNames) {
            RassXmlFileParseResult result = new RassXmlFileParseResult(fileName, null, null);
            results.add(result);
        }
        return results;
    }

}
