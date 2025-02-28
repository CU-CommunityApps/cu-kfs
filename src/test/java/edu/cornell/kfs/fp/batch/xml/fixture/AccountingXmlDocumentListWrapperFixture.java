package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.List;

import org.joda.time.DateTime;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AccountingXmlDocumentListWrapperFixture {
    BASE_WRAPPER("09/28/2017", "abc123@cornell.edu", "Example XML file", documents()),
    BASE_IB_WRAPPER("02/26/2018", "xyz789@cornell.edu", "Example IB XML file", documents()),
    BASE_BA_WRAPPER("04/20/2018", "ggg555@cornell.edu", "Example BA XML file", documents()),
    BASE_YEBA_WRAPPER("04/18/2019", "xyz555@cornell.edu", "Example YEBA XML file", documents()),
    BASE_SB_WRAPPER("05/24/2018", "cba001@cornell.edu", "Example SB XML file", documents()),
    BASE_AV_WRAPPER("02/20/2019", "zyx321@cornell.edu", "Example AV XML file", documents()),

    MULTI_DI_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC4)),
    MULTI_DI_DOCUMENT_WITH_BAD_CONVERSION_SECOND_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.BAD_CONVERSION_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3), false),
    MULTI_DI_DOCUMENT_WITH_BAD_RULES_FIRST_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC3)),
    MULTI_DI_DOCUMENT_WITH_BAD_ATTACHMENTS_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_DI_DOCUMENT_TEST_DOC4,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER)),
    SINGLE_DI_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_DI_DOCUMENT_TEST_DOC1)),
    SINGLE_DI_DOCUMENT_WITH_BAD_ATTACHMENT_TEST(
            "09/28/2017", "abc123@cornell.edu", "Example XML file",
            documents(
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER), true),
    SINGLE_DI_DOCUMENT_WITH_BAD_AMOUNT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.BAD_CONVERSION_DOCUMENT_PLACEHOLDER)),
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
            BASE_WRAPPER, documents()),
    SINGLE_IB_DOCUMENT_TEST(
            BASE_IB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_IB_DOCUMENT_TEST_DOC1)),
    SINGLE_IB_DOCUMENT_NO_ITEMS_TEST(
            BASE_IB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_IB_NO_ITEMS_DOCUMENT_TEST_DOC1)),
    MULTI_IB_DOCUMENT_TEST(
            BASE_IB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_IB_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_IB_DOCUMENT_TEST_DOC2)),
    MULTI_IB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST(
            BASE_IB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_IB_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_IB_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER)),
    SINGLE_BA_DOCUMENT_TEST(
            BASE_BA_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_BA_DOCUMENT_TEST_DOC1)),
    SINGLE_YEBA_DOCUMENT_TEST(
            BASE_YEBA_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_YEBA_DOCUMENT_TEST_DOC1)),
    MUTLI_YEBA_DOCUMENT_TEST(
            BASE_YEBA_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_YEBA_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.BASE_YEBA_WITH_ZERO_AND_SINGLE_MONTHS_NO_ADHOC_NO_BACKUP)),
    SINGLE_BA_NO_BASEAMOUNT_OR_MONTHS_DOCUMENT_TEST(
            BASE_BA_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_BA_NO_BASEAMOUNT_OR_MONTHS_DOCUMENT_TEST_DOC1)),
    SINGLE_BA_NONZERO_BASEAMOUNT_DOCUMENT_TEST(
            BASE_BA_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_BA_NONZERO_BASEAMOUNT_DOCUMENT_TEST_DOC1)),
    SINGLE_BA_MULTI_MONTHS_DOCUMENT_TEST(
            BASE_BA_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_BA_MULTI_MONTHS_DOCUMENT_TEST_DOC1)),
    MULTI_BA_DOCUMENT_TEST(
            BASE_BA_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_BA_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_BA_DOCUMENT_TEST_DOC2)),
    MULTI_BA_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST(
            BASE_BA_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_BA_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_BA_DOCUMENT_TEST_DOC3,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER)),
    SINGLE_SB_DOCUMENT_TEST(
            BASE_SB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_SB_DOCUMENT_TEST_DOC1)),
    SINGLE_SB_DOCUMENT_NO_ITEMS_TEST(
            BASE_SB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_SB_NO_ITEMS_DOCUMENT_TEST_DOC1)),
    MULTI_SB_DOCUMENT_TEST(
            BASE_SB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_SB_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_SB_DOCUMENT_TEST_DOC2)),
    MULTI_SB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST(
            BASE_SB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_SB_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_SB_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER)),
    MULTI_SB_DOCUMENT_WITH_BAD_DATE_VALUES_DOCUMENT_TEST(
            BASE_SB_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_SB_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.BAD_CONVERSION_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_SB_DOCUMENT_DATE_VALUES_TEST_DOC3)),
    SINGLE_AV_DOCUMENT_TEST(
            BASE_AV_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_AV_DOCUMENT_TEST_DOC1)),
    MULTI_AV_DOCUMENT_TEST(
            BASE_AV_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_AV_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_AV_DOCUMENT_TEST_DOC2,
                    AccountingXmlDocumentEntryFixture.MULTI_AV_DOCUMENT_TEST_DOC3)),
    MULTI_AV_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST(
            BASE_AV_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_AV_DOCUMENT_TEST_DOC3,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER)),
    MULTI_DOCUMENT_TYPES_TEST(
            "02/26/2018", "xyz789@cornell.edu", "Example multi-doc-type XML file",
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_DI,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_IB,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_TF1,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_TF2,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_BA,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_SB,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_YEDI,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_DV,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_YEBA,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_YETF,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_AV,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_PE)),
    TF_DOCUMENT_TEST("02/26/2018", "xyz789@cornell.edu", "Example multi-doc-type XML file",
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_DOC_TYPE_TEST_TF1)),
    TF_DOCUMENT_TEST2("02/26/2018", "xyz789@cornell.edu", "Example multi-doc-type XML file",
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_DOC_TYPE_TEST_TF1,
                    AccountingXmlDocumentEntryFixture.MULTI_DOC_TYPE_TEST_TF2)),
    DV_DOCUMENT_TEST(
            "03/30/2020", "xyz789@cornell.edu", "Example DV XML file",
            documents(
                    AccountingXmlDocumentEntryFixture.DV_DOC_TEST1,
                    AccountingXmlDocumentEntryFixture.DV_DOC_TEST2)),
    DV_DOCUMENT_VENDOR_TEST(
            "12/22/2020", "xyz789@cornell.edu", "Example DV XML file with vendors",
            documents(
                    AccountingXmlDocumentEntryFixture.DV_DOC_VENDOR_TEST1,
                    AccountingXmlDocumentEntryFixture.DV_DOC_VENDOR_TEST2)),
    DI_WITH_IB_ITEMS_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.DI_WITH_IB_ITEMS_TEST_DOC1)),
    SINGLE_YEDI_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_YEDI_DOCUMENT_TEST_DOC1)),
    SINGLE_YETF_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_DOC_TYPE_TEST_YETF)),
    EMPTY_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(), false),
    BAD_XML_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(), false),
    MULTI_YEDI_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_YEDI_DOCUMENT_TEST_DOC1,
            AccountingXmlDocumentEntryFixture.MULTI_YEDI_DOCUMENT_TEST_DOC2,
            AccountingXmlDocumentEntryFixture.MULTI_YEDI_DOCUMENT_TEST_DOC3,
            AccountingXmlDocumentEntryFixture.MULTI_YEDI_DOCUMENT_TEST_DOC4)),
    MULTI_YEDI_DOCUMENT_WITH_BAD_CONVERSION_SECOND_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MULTI_YEDI_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.BAD_CONVERSION_DOCUMENT_PLACEHOLDER,
                    AccountingXmlDocumentEntryFixture.MULTI_YEDI_DOCUMENT_TEST_DOC3), false),
    SINGLE_PE_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.SINGLE_PE_DOCUMENT_TEST_DOC1)),
    MULTI_PE_DOCUMENT_TEST(
            BASE_WRAPPER,
            documents(
                    AccountingXmlDocumentEntryFixture.MUTLI_PE_DOCUMENT_TEST_DOC1,
                    AccountingXmlDocumentEntryFixture.MULTI_PE_DOCUMENT_TEST_DOC2));

    public final String createDate;
    public final String reportEmail;
    public final String overview;
    public final List<AccountingXmlDocumentEntryFixture> documents;
    public final boolean expectedResults;

    private AccountingXmlDocumentListWrapperFixture(AccountingXmlDocumentListWrapperFixture baseFixture, AccountingXmlDocumentEntryFixture[] documents) {
        this(baseFixture.createDate, baseFixture.reportEmail, baseFixture.overview, documents);
    }
    
    private AccountingXmlDocumentListWrapperFixture(AccountingXmlDocumentListWrapperFixture baseFixture, AccountingXmlDocumentEntryFixture[] documents, boolean expectedResults) {
        this(baseFixture.createDate, baseFixture.reportEmail, baseFixture.overview, documents, expectedResults);
    }

    private AccountingXmlDocumentListWrapperFixture(String createDate, String reportEmail,
            String overview, AccountingXmlDocumentEntryFixture[] documents) {
        this(createDate, reportEmail, overview, documents, true);
    }
    
    private AccountingXmlDocumentListWrapperFixture(String createDate, String reportEmail,
            String overview, AccountingXmlDocumentEntryFixture[] documents, boolean expectedResults) {
        this.createDate = createDate;
        this.reportEmail = reportEmail;
        this.overview = overview;
        this.documents = XmlDocumentFixtureUtils.toImmutableList(documents);
        this.expectedResults = expectedResults;
    }

    public AccountingXmlDocumentListWrapper toDocumentListWrapperPojo() {
        DateTime parsedCreateDate = getCreateDateAsDateTime();
        AccountingXmlDocumentListWrapper listWrapper = new AccountingXmlDocumentListWrapper();
        listWrapper.setCreateDate(parsedCreateDate.toDate());
        listWrapper.setReportEmail(reportEmail);
        listWrapper.setOverview(overview);
        listWrapper.setDocuments(
                XmlDocumentFixtureUtils.convertToPojoList(documents, AccountingXmlDocumentEntryFixture::toDocumentEntryPojo));
        return listWrapper;
    }

    public DateTime getCreateDateAsDateTime() {
        return StringToJavaDateAdapter.parseToDateTime(createDate);
    }

    // This method is only meant to improve the setup and readability of this enum's constants.
    private static AccountingXmlDocumentEntryFixture[] documents(AccountingXmlDocumentEntryFixture... fixtures) {
        return fixtures;
    }

}
