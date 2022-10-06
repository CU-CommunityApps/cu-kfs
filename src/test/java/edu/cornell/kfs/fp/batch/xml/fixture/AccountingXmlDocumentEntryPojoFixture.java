package edu.cornell.kfs.fp.batch.xml.fixture;

import static edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils.defaultToEmptyStringIfBlank;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventLocator;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

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
    BAD_INDEX_DOCUMENT_TYPE_DESCRIPTION_EXPLANATION_TEST(0, null, null, null, false),
    BAD_XML_NUMERIC_ERROR_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", "Good Explanation for document entry unit tests", false,
            validationError(25, "java.lang.NumberFormatException")),
    BAD_XML_DESCRIPTIVE_NUMERIC_ERROR_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", "Good Explanation for document entry unit tests", false,
            validationError(25, "java.lang.NumberFormatException: Commas are not allowed in numbers")),
    BAD_XML_DATE_ERROR_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", "Good Explanation for document entry unit tests", false,
            validationError(66, "org.joda.time.IllegalFieldValueException: Cannot parse \"02/31/2018\": Value 31 for dayOfMonth must be in the range [1,28]")),
    BAD_XML_GENERIC_ERROR_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", "Good Explanation for document entry unit tests", false,
            validationError(4, "java.lang.IllegalArgumentException")),
    BAD_XML_ERROR_MESSAGE_WITHOUT_CLASSNAME_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", "Good Explanation for document entry unit tests", false,
            validationError(103, "Account number does not reference an actual KFS account")),
    BAD_XML_ERROR_WITHOUT_LINE_NUMBER_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", "Good Explanation for document entry unit tests", false,
            validationError(-1, "java.lang.NumberFormatException: Commas are not allowed in numbers")),
    BAD_XML_ERROR_WITHOUT_LINE_OR_MESSAGE_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", "Good Explanation for document entry unit tests", false,
            validationError(-1, null)),
    BAD_XML_MULTIPLE_ADAPTER_ERRORS_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", "Good Explanation for document entry unit tests", false,
            validationError(25, "java.lang.NumberFormatException"),
            validationError(66, "org.joda.time.IllegalFieldValueException: Cannot parse \"02/31/2018\": Value 31 for dayOfMonth must be in the range [1,28]")),
    BAD_XML_ADAPTER_AND_REQUIRED_DATA_ERRORS_TEST(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Good Description", null, false,
            validationError(25, "java.lang.NumberFormatException: Commas are not allowed in numbers"));

    public final Long index;
    public final String documentTypeCode;
    public final String description;
    public final String explanation;
    public final boolean expectedResults;
    public final List<Pair<Integer, String>> parserValidationErrors;

    @SafeVarargs
    private AccountingXmlDocumentEntryPojoFixture(int index, String documentTypeCode, String description,
            String explanation, boolean dataIsValid, Pair<Integer, String>... parserValidationErrors) {
        this.index = index != 0 ? Long.valueOf(index) : null;
        this.documentTypeCode = documentTypeCode;
        this.description = description;
        this.explanation = defaultToEmptyStringIfBlank(explanation);
        this.expectedResults = dataIsValid;
        this.parserValidationErrors = Collections.unmodifiableList(Arrays.asList(parserValidationErrors));
    }

    public AccountingXmlDocumentEntry toDocumentEntryPojo() {
        AccountingXmlDocumentEntry documentEntry = new AccountingXmlDocumentEntry();
        documentEntry.setIndex(index);
        documentEntry.setDocumentTypeCode(documentTypeCode);
        documentEntry.setDescription(description);
        documentEntry.setExplanation(explanation);
        if (!parserValidationErrors.isEmpty()) {
            parserValidationErrors.stream()
                    .map(this::buildMockValidationEvent)
                    .forEach(documentEntry::addValidationError);
        }
        return documentEntry;
    }

    private ValidationEvent buildMockValidationEvent(Pair<Integer, String> validationError) {
        ValidationEvent event = Mockito.mock(ValidationEvent.class);
        ValidationEventLocator locator = Mockito.mock(ValidationEventLocator.class);
        
        Mockito.when(locator.getLineNumber())
                .thenReturn(validationError.getLeft());
        
        Mockito.when(event.getSeverity())
                .thenReturn(ValidationEvent.ERROR);
        Mockito.when(event.getMessage())
                .thenReturn(validationError.getRight());
        Mockito.when(event.getLocator())
                .thenReturn(locator);
        
        return event;
    }

    private static Pair<Integer, String> validationError(int lineNumber, String message) {
        return Pair.of(lineNumber, message);
    }

}
