package edu.cornell.kfs.fp.batch.xml;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import edu.cornell.kfs.fp.batch.xml.fixture.DefaultKfsAccountForAwsWrapperFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class DefaultKfsAccountForAwsMarshalTest {

    private CUMarshalService cUMarshalService;

    @Before
    public void setUp() throws Exception {
        cUMarshalService = new CUMarshalServiceImpl();
    }
    
    @Test
    public void verifyDefaultKfsAccountForAwsXmlCanBeMarshalled() throws JAXBException, IOException, ParseException {
        String marshalledXml = cUMarshalService.marshalObjectToXmlString(
                DefaultKfsAccountForAwsWrapperFixture.generateExpectedDefaultKfsAccountForAwsResultWrapper());
        marshalledXml = removeWhitespace(marshalledXml.substring(marshalledXml.indexOf('\n') + 1));

        String expectedXmlExample = DefaultKfsAccountForAwsWrapperFixture.getXmlFromExampleFile();
        expectedXmlExample = removeWhitespace(expectedXmlExample);
        assertTrue("The Marshalled XML should be equal to the example XML", StringUtils.equalsIgnoreCase(marshalledXml, expectedXmlExample));
    }

    @Test
    public void verifyDefaultKfsAccountForAwsXmlCanBeUnmarshalled() throws JAXBException, IOException, ParseException {
        DefaultKfsAccountForAwsResultWrapper unmarshalledObject =
                cUMarshalService.unmarshalString(DefaultKfsAccountForAwsWrapperFixture.getXmlFromExampleFile(), DefaultKfsAccountForAwsResultWrapper.class);

        DefaultKfsAccountForAwsResultWrapper expectedResultWrapper =
                DefaultKfsAccountForAwsWrapperFixture.generateExpectedDefaultKfsAccountForAwsResultWrapper();

        assertTrue(Objects.equals(expectedResultWrapper, unmarshalledObject));
    }

    @Test
    public void verifyDefaultKfsAccountForAwsXmlCanBeUnmarshalledWithBadDate() throws JAXBException, IOException, ParseException {
        DefaultKfsAccountForAwsResultWrapper unmarshalledObject =
                cUMarshalService.unmarshalString(DefaultKfsAccountForAwsWrapperFixture.getXmlFromExampleFileWithBadDate(), DefaultKfsAccountForAwsResultWrapper.class);

        DefaultKfsAccountForAwsResultWrapper expectedResultWrapper =
                DefaultKfsAccountForAwsWrapperFixture.generateExpectedDefaultKfsAccountForAwsResultWrapperForBadDateTest();

        assertTrue(Objects.equals(expectedResultWrapper, unmarshalledObject));
    }

    @Test
    public void verifyDefaultKfsAccountForAwsXmlCanBeUnmarshalledWithEmptyDate() throws JAXBException, IOException, ParseException {
        DefaultKfsAccountForAwsResultWrapper unmarshalledObject =
                cUMarshalService.unmarshalString(DefaultKfsAccountForAwsWrapperFixture.getXmlFromExampleFileWithEmptyDate(), DefaultKfsAccountForAwsResultWrapper.class);

        DefaultKfsAccountForAwsResultWrapper expectedResultWrapper =
                DefaultKfsAccountForAwsWrapperFixture.generateExpectedDefaultKfsAccountForAwsResultWrapperForEmptyDateTest();

        assertTrue(Objects.equals(expectedResultWrapper, unmarshalledObject));
    }

    private String removeWhitespace(String s) {
        return s.replaceAll("\\s+", "");
    }

}
