package edu.cornell.kfs.rass.batch;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapperMarshalTest;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class TestRassBatchUtils {
    private static final Logger LOG = LogManager.getLogger(TestRassBatchUtils.class);
    private CUMarshalService cuMarshalService;
    private RassXmlDocumentWrapper rassXmlDocumentWrapper;

    @Before
    public void setUp() throws Exception {
        cuMarshalService = new CUMarshalServiceImpl();
        File xmlFile = new File(RassXmlDocumentWrapperMarshalTest.RASS_EXAMPLE_FILE_BASE_PATH + "rass-full-extract-agencies.xml");
        rassXmlDocumentWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
    }

    @After
    public void tearDown() throws Exception {
        cuMarshalService = null;
        rassXmlDocumentWrapper = null;
    }

    @Test
    public void checkBeforeSort() {
        TestResults results = checkReportsToAgencyBeforeChildAgencies(rassXmlDocumentWrapper.getAgencies());
        LOG.info("checkBeforeSort, processed: " + results.processedAgencies);
        LOG.info("checkBeforeSort, failed: " + results.failedAgencies);
        assertTrue("There shoulld be some agencies before their reports to agency before the sort", results.failedAgencies.size() > 0);
    }
    
    @Test
    public void checkAfterSort() {
        int preSortCount = rassXmlDocumentWrapper.getAgencies().size();
        List<RassXmlAgencyEntry> sortedAgencyEntries = RassBatchUtils.sortRassXmlAgencyEntriesForUpdate(rassXmlDocumentWrapper.getAgencies());
        int postSortCount = sortedAgencyEntries.size();
        
        assertEquals("The sort should return the same nnumber of agencies as was passed in", preSortCount, postSortCount);
        
        TestResults results = checkReportsToAgencyBeforeChildAgencies(sortedAgencyEntries);
        LOG.info("checkAfterSort, processed: " + results.processedAgencies);
        LOG.info("checkAfterSort, failed: " + results.failedAgencies);
        assertTrue("There shoulld be no agencies before their reports to agency after the sort", results.failedAgencies.size() == 0);
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
