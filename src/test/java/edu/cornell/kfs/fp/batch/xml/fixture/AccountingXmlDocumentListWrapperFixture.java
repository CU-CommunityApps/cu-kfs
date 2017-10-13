package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.List;

import org.joda.time.DateTime;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AccountingXmlDocumentListWrapperFixture {
    TEST_WRAPPER1("01/01/2017", null, null, null);

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

}
