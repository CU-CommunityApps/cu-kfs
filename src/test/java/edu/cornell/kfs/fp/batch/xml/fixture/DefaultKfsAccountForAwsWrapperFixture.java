package edu.cornell.kfs.fp.batch.xml.fixture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAwsResultWrapper;

public class DefaultKfsAccountForAwsWrapperFixture {

    private static final String EXPECTED_XML_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/default-kfs-account-for-aws-test.xml";
    private static final String BAD_DATE_XML_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/default-kfs-account-for-aws-bad-date-test.xml";
    private static final String EMPTY_DATE_XML_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/default-kfs-account-for-aws-empty-date-test.xml";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static DefaultKfsAccountForAwsResultWrapper generateExpectedDefaultKfsAccountForAwsResultWrapper() {
        DefaultKfsAccountForAwsResultWrapper defaultKfsAccountForAwsResultWrapper = new DefaultKfsAccountForAwsResultWrapper();

        ArrayList<DefaultKfsAccountForAws> defaultKfsAccountForAwsList = new ArrayList<>();
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999913976919", "1719999", "2016-04-26 18:43:03"));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999970681744", "R999999", "2017-05-26 19:42:23"));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999972846379", "R999999", "2016-08-05 04:12:56"));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999937185330", "L999999", "2016-04-26 18:43:03"));

        defaultKfsAccountForAwsResultWrapper.setDefaultKfsAccountsForAws(defaultKfsAccountForAwsList);
        return defaultKfsAccountForAwsResultWrapper;
    }

    public static DefaultKfsAccountForAwsResultWrapper generateExpectedDefaultKfsAccountForAwsResultWrapperForBadDateTest() {
        DefaultKfsAccountForAwsResultWrapper defaultKfsAccountForAwsResultWrapper = new DefaultKfsAccountForAwsResultWrapper();

        ArrayList<DefaultKfsAccountForAws> defaultKfsAccountForAwsList = new ArrayList<>();
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999913976919", "1719999", null));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999972846379", "R999999", null));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999937185330", "L999999", null));

        defaultKfsAccountForAwsResultWrapper.setDefaultKfsAccountsForAws(defaultKfsAccountForAwsList);
        return defaultKfsAccountForAwsResultWrapper;
    }

    public static DefaultKfsAccountForAwsResultWrapper generateExpectedDefaultKfsAccountForAwsResultWrapperForEmptyDateTest() {
        DefaultKfsAccountForAwsResultWrapper defaultKfsAccountForAwsResultWrapper = new DefaultKfsAccountForAwsResultWrapper();

        ArrayList<DefaultKfsAccountForAws> defaultKfsAccountForAwsList = new ArrayList<>();
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws("999913976919", "1719999", null));

        defaultKfsAccountForAwsResultWrapper.setDefaultKfsAccountsForAws(defaultKfsAccountForAwsList);
        return defaultKfsAccountForAwsResultWrapper;
    }

    private static DefaultKfsAccountForAws buildDefaultKfsAccountForAws(String awsAccount, String kfsDefaultAccount, String updatedAt) {
        DefaultKfsAccountForAws defaultKfsAccountForAws = new DefaultKfsAccountForAws();
        defaultKfsAccountForAws.setAwsAccount(awsAccount);
        defaultKfsAccountForAws.setKfsDefaultAccount(kfsDefaultAccount);

        Date updatedAtDateTime = null;
        try{
            updatedAtDateTime = parseDateTime(updatedAt);
        }
        catch(NullPointerException nullPointerException){}

        defaultKfsAccountForAws.setUpdatedAt(updatedAtDateTime);
        return defaultKfsAccountForAws;
    }

    private static Date parseDateTime(String updatedAt) {
        return DateTime.parse(updatedAt, DATE_FORMATTER).toDate();
    }

    public static String getXmlFromExampleFile() throws IOException {
        String exampleXmlFileContents = new String(Files.readAllBytes(Paths.get(EXPECTED_XML_EXAMPLE_FILE_PATH)));
        return exampleXmlFileContents;
    }

    public static String getXmlFromExampleFileWithBadDate() throws IOException {
        String exampleXmlFileContents = new String(Files.readAllBytes(Paths.get(BAD_DATE_XML_EXAMPLE_FILE_PATH)));
        return exampleXmlFileContents;
    }

    public static String getXmlFromExampleFileWithEmptyDate() throws IOException {
        String exampleXmlFileContents = new String(Files.readAllBytes(Paths.get(EMPTY_DATE_XML_EXAMPLE_FILE_PATH)));
        return exampleXmlFileContents;
    }
}
