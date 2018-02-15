package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAwsResultsWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DefaultKfsAccountForAwsWrapperFixture {

    private static final String EXPECTED_XML_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/default-kfs-account-for-aws-test.xml";

    public static DefaultKfsAccountForAwsResultsWrapper getExampleObject() {
        DefaultKfsAccountForAwsResultsWrapper ret = new DefaultKfsAccountForAwsResultsWrapper();
        ArrayList<DefaultKfsAccountForAws> defaultKfsAccountForAwsList = new ArrayList<>();
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999913976919", "1719999", "2016-04-26 18:43:03"));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999970681744", "R999999", "2017-05-26 19:42:23"));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999972846379", "R999999", "2016-08-05 04:12:56"));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999937185330", "L999999", "2016-04-26 18:43:03"));
        ret.setDefaultKfsAccountForAws(defaultKfsAccountForAwsList);
        return ret;
    }

    private static DefaultKfsAccountForAws buildDefaultKfsAccountForAws(String awsAccount, String kfsDefaultCostCenter, String updatedAt) {
        DefaultKfsAccountForAws defaultKfsAccountForAws = new DefaultKfsAccountForAws();
        defaultKfsAccountForAws.setAwsAccount(awsAccount);
        defaultKfsAccountForAws.setKfsDefaultCostCenter(kfsDefaultCostCenter);
        defaultKfsAccountForAws.setUpdatedAt(updatedAt);
        return defaultKfsAccountForAws;
    }

    public static String getXmlFromExampleFile() throws IOException {
        String exampleXmlFileContents = new String(Files.readAllBytes(Paths.get(EXPECTED_XML_EXAMPLE_FILE_PATH)));
        return exampleXmlFileContents;
    }
}
