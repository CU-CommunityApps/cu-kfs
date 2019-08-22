package edu.cornell.kfs.rass.batch;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapperMarshalTest;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class TestRassBatchUtils {
    private static final String FULL_EXTRACT_FILE_NAME = RassXmlDocumentWrapperMarshalTest.RASS_EXAMPLE_FILE_BASE_PATH + "rass-full-extract-agencies.xml";
    private static final String NO_PARENTS_FILE_NAME = RassXmlDocumentWrapperMarshalTest.RASS_EXAMPLE_FILE_BASE_PATH + "rass-agencies-no-parents.xml";
    private static final String SINGLE_AWARD_FILE = RassXmlDocumentWrapperMarshalTest.RASS_EXAMPLE_FILE_BASE_PATH + "rass_single_award_only.xml";
    private static final Logger LOG = LogManager.getLogger(TestRassBatchUtils.class);
    private CUMarshalService cuMarshalService;

    @Before
    public void setUp() throws Exception {
        Configurator.setLevel(RassBatchUtils.class.getName(), Level.DEBUG);
        cuMarshalService = new CUMarshalServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        cuMarshalService = null;
    }

    @Test
    public void checkFullExtractBeforeSort() throws JAXBException {
        File xmlFile = new File(FULL_EXTRACT_FILE_NAME);
        RassXmlDocumentWrapper rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        
        TestResults results = checkReportsToAgencyBeforeChildAgencies(rassXmlDocumentWrapper.getAgencies());
        LOG.info("checkBeforeSort, processed: " + results.processedAgencies);
        LOG.info("checkBeforeSort, failed: " + results.failedAgencies);
        assertTrue("There shoulld be some agencies before their reports to agency before the sort", results.failedAgencies.size() > 0);
    }
    
    @Test
    public void checkFullExtractAfterSort() throws JAXBException {
        File xmlFile = new File(FULL_EXTRACT_FILE_NAME);
        RassXmlDocumentWrapper rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        
        int preSortCount = rassXmlDocumentWrapper.getAgencies().size();
        List<RassXmlAgencyEntry> sortedAgencyEntries = RassBatchUtils.sortRassXmlAgencyEntriesForUpdate(rassXmlDocumentWrapper.getAgencies());
        int postSortCount = sortedAgencyEntries.size();
        
        assertEquals("The sort should return the same nnumber of agencies as was passed in", preSortCount, postSortCount);
        
        TestResults results = checkReportsToAgencyBeforeChildAgencies(sortedAgencyEntries);
        LOG.info("checkAfterSort, processed: " + results.processedAgencies);
        LOG.info("checkAfterSort, failed: " + results.failedAgencies);
        assertTrue("There shoulld be no agencies before their reports to agency after the sort", results.failedAgencies.size() == 0);
    }
    
    @Test
    public void testNoParentAgengiesFile() throws JAXBException {
        File xmlFile = new File(NO_PARENTS_FILE_NAME);
        RassXmlDocumentWrapper rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        
        int preSortCount = rassXmlDocumentWrapper.getAgencies().size();
        List<RassXmlAgencyEntry> sortedAgencyEntries = RassBatchUtils.sortRassXmlAgencyEntriesForUpdate(rassXmlDocumentWrapper.getAgencies());
        int postSortCount = sortedAgencyEntries.size();
        
        assertEquals("The sort should return the same nnumber of agencies as was passed in", preSortCount, postSortCount);
    }
    
    @Test
    public void testSingleAwardFile() throws JAXBException {
        File xmlFile = new File(SINGLE_AWARD_FILE);
        RassXmlDocumentWrapper rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        
        int preSortCount = rassXmlDocumentWrapper.getAgencies().size();
        List<RassXmlAgencyEntry> sortedAgencyEntries = RassBatchUtils.sortRassXmlAgencyEntriesForUpdate(rassXmlDocumentWrapper.getAgencies());
        int postSortCount = sortedAgencyEntries.size();
        
        assertEquals("The sort should return the same nnumber of agencies as was passed in", preSortCount, postSortCount);
    }
    
    private TestResults checkReportsToAgencyBeforeChildAgencies(List<RassXmlAgencyEntry> agencies) {
        TestResults results = new TestResults();
        
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
    
    private class TestResults {
        public List<String> processedAgencies;
        public List<String> failedAgencies;
        
        TestResults() {
            processedAgencies = new ArrayList<String>();
            failedAgencies = new ArrayList<String>();
        }
        
    }

}
