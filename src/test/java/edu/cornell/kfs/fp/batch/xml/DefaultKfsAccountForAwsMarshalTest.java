package edu.cornell.kfs.fp.batch.xml;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.DefaultKfsAccountForAwsWrapperFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

import static org.junit.Assert.assertTrue;

public class DefaultKfsAccountForAwsMarshalTest {

    private CUMarshalService cUMarshalService;

    @Before
    public void setUp() throws Exception {
        cUMarshalService = new CUMarshalServiceImpl();
    }
    
    @Test
    public void verifyDefaultKfsAccountForAwsXmlCanBeMarshalled() throws JAXBException, IOException {
        String marshalledXml = cUMarshalService.marshalObjectToXmlString(
                DefaultKfsAccountForAwsWrapperFixture.getExampleObject());
        marshalledXml = removeWhitespace(marshalledXml.substring(marshalledXml.indexOf('\n') + 1));

        String expectedXmlExample = DefaultKfsAccountForAwsWrapperFixture.getXmlFromExampleFile();
        expectedXmlExample = removeWhitespace(expectedXmlExample);
        assertTrue("The Marshalled XML should be equal to the example XML", marshalledXml.equalsIgnoreCase(expectedXmlExample));
    }

    @Test
    public void verifyDefaultKfsAccountForAwsXmlCanBeUnmarshalled() throws JAXBException, IOException {
        DefaultKfsAccountForAwsResultsWrapper marshalledObject =
                cUMarshalService.unmarshalString(DefaultKfsAccountForAwsWrapperFixture.getXmlFromExampleFile(), DefaultKfsAccountForAwsResultsWrapper.class);

        assertTrue(isDefaultKfsAccountForAwsResultsWrapperEqual(marshalledObject, DefaultKfsAccountForAwsWrapperFixture.getExampleObject()));
    }

    private boolean isDefaultKfsAccountForAwsResultsWrapperEqual(DefaultKfsAccountForAwsResultsWrapper a, DefaultKfsAccountForAwsResultsWrapper b) {
        List<DefaultKfsAccountForAws> aList = a.getDefaultKfsAccountForAws();
        List<DefaultKfsAccountForAws> bList = b.getDefaultKfsAccountForAws();
        if (aList.size() != bList.size()) {
            return false;
        }
        for (DefaultKfsAccountForAws defaultKfsAccountForAws : aList) {
            if (!listContainsDefaultKfsAccountForAws(bList, defaultKfsAccountForAws)) {
                return false;
            }
        }
        return true;
    }

    private static boolean listContainsDefaultKfsAccountForAws(List<DefaultKfsAccountForAws> accountList, DefaultKfsAccountForAws accountForAws){
        return accountList.stream().filter(a -> isKfsAccountsEqual(a, accountForAws)).count() > 0;
    }

    private static boolean isKfsAccountsEqual(DefaultKfsAccountForAws a, DefaultKfsAccountForAws b) {
        return a.getUpdatedAt().equals(b.getUpdatedAt()) && a.getAwsAccount().equals(b.getAwsAccount()) &&
                a.getKfsDefaultCostCenter().equals(b.getKfsDefaultCostCenter());
    }

    private String removeWhitespace(String s) {
        return s.replaceAll("\\s+", "");
    }

}
