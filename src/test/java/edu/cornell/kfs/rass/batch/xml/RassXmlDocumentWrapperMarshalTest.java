package edu.cornell.kfs.rass.batch.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;

import jakarta.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlDocumentWrapperFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class RassXmlDocumentWrapperMarshalTest {
    public static final String RASS_EXAMPLE_FILE_BASE_PATH = "src/test/resources/edu/cornell/kfs/rass/";
    
    private CUMarshalService cuMarshalService;
    
    @Before
    public void setup() {
        cuMarshalService = new CUMarshalServiceImpl();
    }
    
    @After
    public void tearDown() {
        cuMarshalService = null;
    }
    
    @Test
    public void testUnmarshalBasicExampleFile() throws JAXBException {
        assertRassXmlParsesCorrectly("rass_example.xml", RassXmlDocumentWrapperFixture.RASS_EXAMPLE);
    }

    @Test
    public void testUnmarshalAwardsOnlyFile() throws JAXBException {
        assertRassXmlParsesCorrectly("rass_awards_only.xml", RassXmlDocumentWrapperFixture.RASS_AWARDS_ONLY);
    }

    @Test
    public void testUnmarshalAgenciesOnlyFile() throws JAXBException {
        assertRassXmlParsesCorrectly("rass_agencies_only.xml", RassXmlDocumentWrapperFixture.RASS_AGENCIES_ONLY);
    }

    @Test
    public void testUnmarshalSingleAwardOnlyFile() throws JAXBException {
        assertRassXmlParsesCorrectly("rass_single_award_only.xml", RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_CREATE_FILE);
    }

    @Test
    public void testUnmarshalSingleAgencyOnlyFile() throws JAXBException {
        assertRassXmlParsesCorrectly("rass_single_agency_only.xml", RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE);
    }

    @Test
    public void testUnmarshalFileWithEmptyAwardAndAgencyLists() throws JAXBException {
        assertRassXmlParsesCorrectly("rass_empty.xml", RassXmlDocumentWrapperFixture.RASS_EMPTY_FILE);
    }

    @Test
    public void testUnmarshalFileWithComplexAwardsAndAgencies() throws JAXBException {
        assertRassXmlParsesCorrectly("rass_complex_example.xml", RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_AND_AWARDS_FILE);
    }

    protected void assertRassXmlParsesCorrectly(String testXmlFileName, RassXmlDocumentWrapperFixture expectedFixture) throws JAXBException {
        File xmlFile = new File(RASS_EXAMPLE_FILE_BASE_PATH + testXmlFileName);
        RassXmlDocumentWrapper expectedWrapper = expectedFixture.toRassXmlDocumentWrapper();
        RassXmlDocumentWrapper actualWrapper = cuMarshalService.unmarshalFile(xmlFile, RassXmlDocumentWrapper.class);
        assertEquals("Wrappers should match", expectedWrapper, actualWrapper);
        assertEquals("Wrappers' hash code should match", expectedWrapper.hashCode(), actualWrapper.hashCode());
    }

}
