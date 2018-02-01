package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.List;

import org.joda.time.DateTime;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AccountingXmlDocumentListWrapperFixture {
    BASE_WRAPPER("09/28/2017", "abc123@cornell.edu", "Example XML file", documents()),

    MULTI_DI_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC4)),
    MULTI_DI_DOCUMENT_WITH_INVALID_SECOND_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3)),
    MULTI_DI_DOCUMENT_WITH_BAD_RULES_FIRST_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1_BAD,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3)),
    SINGLE_DI_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_DI_DOCUMENT_TEST_DOC1)),
    DI_FULL_ACCOUNT_LINE_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_FULL_ACCOUNT_LINE_TEST_DOC1)),
    DI_SINGLE_ELEMENT_LISTS_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_SINGLE_ELEMENT_LISTS_TEST_DOC1)),
    DI_EMPTY_ELEMENT_LISTS_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_EMPTY_ELEMENT_LISTS_TEST_DOC1)),
    DI_WITHOUT_ELEMENT_LISTS_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_WITHOUT_ELEMENT_LISTS_TEST_DOC1)),
    EMPTY_DOCUMENT_LIST_TEST(
            BASE_WRAPPER, documents()),
    NO_DOCUMENT_LIST_TEST(
            BASE_WRAPPER, documents());

    public final String createDate;
    public final String reportEmail;
    public final String overview;
    public final List<AccountingXmlDocumentEntryFixture> documents;

    private AccountingXmlDocumentListWrapperFixture(
            AccountingXmlDocumentListWrapperFixture baseFixture, AccountingXmlDocumentEntryFixture[] documents) {
        this(baseFixture.createDate, baseFixture.reportEmail, baseFixture.overview, documents);
    }

    private AccountingXmlDocumentListWrapperFixture(String createDate, String reportEmail,
            String overview, AccountingXmlDocumentEntryFixture[] documents) {
        this.createDate = createDate;
        this.reportEmail = reportEmail;
        this.overview = overview;
        this.documents = AccountingXmlDocumentFixtureUtils.toImmutableList(documents);
    }

    public AccountingXmlDocumentListWrapper toDocumentListWrapperPojo() {
        DateTime parsedCreateDate = StringToJavaDateAdapter.parseToDateTime(createDate);
        AccountingXmlDocumentListWrapper listWrapper = new AccountingXmlDocumentListWrapper();
        listWrapper.setCreateDate(parsedCreateDate.toDate());
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
