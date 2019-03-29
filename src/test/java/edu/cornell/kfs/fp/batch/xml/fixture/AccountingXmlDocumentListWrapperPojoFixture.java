package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.List;

import org.joda.time.DateTime;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AccountingXmlDocumentListWrapperPojoFixture {
    GOOD_HEADER_DATA("03/06/2019", "abc123@cornell.edu", "Example XML file", documents(), true),
    BAD_EMAIL_IN_HEADER("03/06/2019", "abc 123@cornell.edu", "Example XML file", documents(), false),
    BAD_OVERVIEW_IN_HEADER("03/06/2019", "abc123@cornell.edu", null, documents(), false),
    BAD_EMAIL_OVERVIEW_IN_HEADER("03/06/2019", "abc12 @cornell.edu", null, documents(), false);

    public final String createDate;
    public final String reportEmail;
    public final String overview;
    public final List<AccountingXmlDocumentEntryFixture> documents;
    public final boolean expectedResults;

    private AccountingXmlDocumentListWrapperPojoFixture(String createDate, String reportEmail, String overview, 
            AccountingXmlDocumentEntryFixture[] documents, boolean dataIsValid) {
        this.createDate = createDate;
        this.reportEmail = reportEmail;
        this.overview = overview;
        this.documents = XmlDocumentFixtureUtils.toImmutableList(documents);
        this.expectedResults = dataIsValid;
    }

    public AccountingXmlDocumentListWrapper toAccountingXmlDocumentListWrappePojo() {
        DateTime parsedCreateDate = StringToJavaDateAdapter.parseToDateTime(createDate);
        AccountingXmlDocumentListWrapper listWrapper = new AccountingXmlDocumentListWrapper();
        listWrapper.setCreateDate(parsedCreateDate.toDate());
        listWrapper.setReportEmail(reportEmail);
        listWrapper.setOverview(overview);
        listWrapper.setDocuments(
                XmlDocumentFixtureUtils.convertToPojoList(documents, AccountingXmlDocumentEntryFixture::toDocumentEntryPojo));
        return listWrapper;
    }
    
    // This method is only meant to improve the setup and readability of this enum's constants.
    private static AccountingXmlDocumentEntryFixture[] documents(AccountingXmlDocumentEntryFixture... fixtures) {
        return fixtures;
    }
}
