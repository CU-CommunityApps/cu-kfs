package edu.cornell.kfs.fp.batch.xml.fixture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAwsResultWrapper;

public class DefaultKfsAccountForAwsWrapperFixture {

    private static final String EXPECTED_XML_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/default-kfs-account-for-aws-test.xml";
    private static final String BAD_DATE_XML_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/default-kfs-account-for-aws-bad-date-test.xml";
    private static final String EMPTY_DATE_XML_EXAMPLE_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/default-kfs-account-for-aws-empty-date-test.xml";

    private static final String[] EXPECTED_AWS_ACCOUNTS_VALUES = { "999913976919", "999970681744", "999972846379", "999937185330" };
    private static final String[] EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES = { "1719999", "R999999", "L999999" };
    private static final String[] EXPECTED_UPDATED_AT_VALUES = { "2016-04-26 18:43:03", "2017-05-26 19:42:23", "2016-08-05 04:12:56", "2016-04-26 18:43:03" };

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_hh_mm_ss);

    public static DefaultKfsAccountForAwsResultWrapper generateExpectedDefaultKfsAccountForAwsResultWrapper() {
        DefaultKfsAccountForAwsResultWrapper defaultKfsAccountForAwsResultWrapper = new DefaultKfsAccountForAwsResultWrapper();

        ArrayList<DefaultKfsAccountForAws> defaultKfsAccountForAwsList = new ArrayList<>();
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[0], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[0], EXPECTED_UPDATED_AT_VALUES[0]));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[1], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[1], EXPECTED_UPDATED_AT_VALUES[1]));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[2], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[1], EXPECTED_UPDATED_AT_VALUES[2]));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[3], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[2], EXPECTED_UPDATED_AT_VALUES[3]));

        defaultKfsAccountForAwsResultWrapper.setDefaultKfsAccountsForAws(defaultKfsAccountForAwsList);
        return defaultKfsAccountForAwsResultWrapper;
    }

    public static DefaultKfsAccountForAwsResultWrapper generateExpectedDefaultKfsAccountForAwsResultWrapperForBadDateTest() {
        DefaultKfsAccountForAwsResultWrapper defaultKfsAccountForAwsResultWrapper = new DefaultKfsAccountForAwsResultWrapper();

        ArrayList<DefaultKfsAccountForAws> defaultKfsAccountForAwsList = new ArrayList<>();
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[0], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[0], null));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[2], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[1], null));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[3], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[2], null));

        defaultKfsAccountForAwsResultWrapper.setDefaultKfsAccountsForAws(defaultKfsAccountForAwsList);
        return defaultKfsAccountForAwsResultWrapper;
    }

    public static DefaultKfsAccountForAwsResultWrapper generateExpectedDefaultKfsAccountForAwsResultWrapperForEmptyDateTest() {
        DefaultKfsAccountForAwsResultWrapper defaultKfsAccountForAwsResultWrapper = new DefaultKfsAccountForAwsResultWrapper();

        ArrayList<DefaultKfsAccountForAws> defaultKfsAccountForAwsList = new ArrayList<>();
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[0], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[0], null));
        defaultKfsAccountForAwsList.add(buildDefaultKfsAccountForAws(EXPECTED_AWS_ACCOUNTS_VALUES[1], EXPECTED_KFS_DEFAULT_ACCOUNT_VALUES[1], null));

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
