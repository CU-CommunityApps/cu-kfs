package edu.cornell.kfs.fp.batch.xml.fixture;

import static edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils.defaultToEmptyStringIfBlank;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

@SuppressWarnings("deprecation")
public enum AccountingXmlDocumentEntryPojoFixture {
    GOOD_DATA_ALL_ITEMS(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING, "Good Description", "Good Explanation for ducment entry unit tests", true),
    BAD_INDEX_TEST(0, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING, "Good Description", "Good Explanation for ducment entry unit tests", false),
    BAD_DOCUMENT_TYPE_TEST(1, null, "Good Description", "Good Explanation for ducment entry unit tests", false), 
    BAD_DESCRIPTION_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING, null, "Good Explanation for ducment entry unit tests", false),
    BAD_EXPLANATION_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING, "Good Description", null, false),
    BAD_INDEX_DOCUMENT_TYPE_TEST(0, null, "Good Description", "Good Explanation for ducment entry unit tests", false),
    BAD_INDEX_DESCRIPTION_TEST(0, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING, null, "Good Explanation for ducment entry unit tests", false),
    BAD_INDEX_EXPLANATION_TEST(0, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING, "Good Description", null, false),
    BAD_DOCUMENT_TYPE_DESCRIPTION_TEST(1, null, null, "Good Explanation for ducment entry unit tests", false),
    BAD_DOCUMENT_TYPE_EXPLANATION_TEST(1, null, "Good Description", null, false),
    BAD_DESCRIPTION_EXPLANATION_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING, null, null, false),
    BAD_INDEX_DOCUMENT_TYPE_DESCRIPTION_TEST(0, null, null, "Good Explanation for ducment entry unit tests", false),
    BAD_INDEX_DOCUMENT_TYPE_EXPLANATION_TEST(0, null, "Good Description", null, false),
    BAD_INDEX_DESCRIPTION_EXPLANATION_TEST(0, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING, null, null, false),
    BAD_DOCUMENT_TYPE_DESCRIPTION_EXPLANATION_TEST(1, null, null, null, false),
    BAD_INDEX_DOCUMENT_TYPE_DESCRIPTION_EXPLANATION_TEST(0, null, null, null, false);

    public final Long index;
    public final String documentTypeCode;
    public final String description;
    public final String explanation;
    public final boolean expectedResults;

    private AccountingXmlDocumentEntryPojoFixture(int index, String documentTypeCode, String description, String explanation, boolean dataIsValid) {
        this.index =(index != 0) ? Long.valueOf(index) : null;
        this.documentTypeCode = documentTypeCode;
        this.description = description;
        this.explanation = defaultToEmptyStringIfBlank(explanation);
        this.expectedResults = dataIsValid;
    }

    public AccountingXmlDocumentEntry toDocumentEntryPojo() {
        AccountingXmlDocumentEntry documentEntry = new AccountingXmlDocumentEntry();
        documentEntry.setIndex(index);
        documentEntry.setDocumentTypeCode(documentTypeCode);
        documentEntry.setDescription(description);
        documentEntry.setExplanation(explanation);
        return documentEntry;
    }

}
