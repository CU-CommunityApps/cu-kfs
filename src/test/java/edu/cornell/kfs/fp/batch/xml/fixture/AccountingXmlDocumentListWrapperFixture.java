package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.List;

import org.joda.time.DateTime;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AccountingXmlDocumentListWrapperFixture {
    MULTI_DI_DOCUMENT_TEST("09/28/2017", "abc123@cornell.edu", "Example XML file",
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3));

    public final DateTime createDate;
    public final String reportEmail;
    public final String overview;
    public final List<AccountingXmlDocumentEntryFixture> documents;

    private AccountingXmlDocumentListWrapperFixture(String createDate, String reportEmail,
            String overview, AccountingXmlDocumentEntryFixture[] documents) {
        this.createDate = StringToJavaDateAdapter.parseToDateTime(createDate);
        this.reportEmail = reportEmail;
        this.overview = overview;
        this.documents = AccountingXmlDocumentFixtureUtils.toImmutableList(documents);
    }

    public AccountingXmlDocumentListWrapper toDocumentListWrapperPojo() {
        AccountingXmlDocumentListWrapper listWrapper = new AccountingXmlDocumentListWrapper();
        listWrapper.setCreateDate(createDate.toDate());
        listWrapper.setReportEmail(reportEmail);
        listWrapper.setOverview(overview);
        listWrapper.setDocuments(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(documents, AccountingXmlDocumentEntryFixture::toDocumentEntryPojo));
        return listWrapper;
    }

    // This method is only meant to improve the setup and readability of this enum's constants.
    private static AccountingXmlDocumentEntryFixture[] documents(AccountingXmlDocumentEntryFixture... fixtures) {
        return fixtures;
    }

}
