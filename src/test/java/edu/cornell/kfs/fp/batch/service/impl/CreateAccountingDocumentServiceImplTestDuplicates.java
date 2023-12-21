package edu.cornell.kfs.fp.batch.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestDuplicates extends CreateAccountingDocumentServiceImplTestBase {

    private static final String SINGLE_YEDI_DOCUMENT_TEST = "single-yedi-document-test";
    private static final String SINGLE_DI_DOCUMENT_TEST = "single-di-document-test";
    private static final String SINGLE_DI_DOCUMENT_TEST_XML = SINGLE_DI_DOCUMENT_TEST + CuFPConstants.XML_FILE_EXTENSION;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.DI_DOCUMENT,
                AccountingDocumentMapping.YEDI_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testCannotReprocessFileByDefault() throws Exception {
        copyTestFilesAndCreateDoneFiles(SINGLE_DI_DOCUMENT_TEST);
        overwriteFileEntry(SINGLE_DI_DOCUMENT_TEST_XML,
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST);
    }

    @Test
    public void testCannotReprocessCaseInsensitiveFileMatchByDefault() throws Exception {
        copyTestFilesAndCreateDoneFiles(SINGLE_DI_DOCUMENT_TEST);
        overwriteFileEntry(SINGLE_DI_DOCUMENT_TEST_XML,
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
        renameTestAndDoneFileToUppercase(SINGLE_DI_DOCUMENT_TEST);
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST);
    }

    @Test
    public void testCanReprocessFileIfRelatedParameterIsDisabled() throws Exception {
        copyTestFilesAndCreateDoneFiles(SINGLE_DI_DOCUMENT_TEST);
        overwriteFileEntry(SINGLE_DI_DOCUMENT_TEST_XML,
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
        configureParameterToAllowDuplicateFileProcessing();
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
    }

    @Test
    public void testCanReprocessCaseInsensitiveFileMatchIfRelatedParameterIsDisabled() throws Exception {
        copyTestFilesAndCreateDoneFiles(SINGLE_DI_DOCUMENT_TEST);
        overwriteFileEntry(SINGLE_DI_DOCUMENT_TEST_XML,
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
        renameTestAndDoneFileToUppercase(SINGLE_DI_DOCUMENT_TEST);
        configureParameterToAllowDuplicateFileProcessing();
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
    }

    @Test
    public void testMixOfDuplicateAndNonDuplicateFiles() throws Exception {
        copyTestFilesAndCreateDoneFiles(SINGLE_DI_DOCUMENT_TEST, SINGLE_YEDI_DOCUMENT_TEST);
        overwriteFileEntry(SINGLE_DI_DOCUMENT_TEST_XML,
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.SINGLE_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testMixOfDuplicateAndNonDuplicateFilesAfterDisablingParameter() throws Exception {
        copyTestFilesAndCreateDoneFiles(SINGLE_DI_DOCUMENT_TEST, SINGLE_YEDI_DOCUMENT_TEST);
        overwriteFileEntry(SINGLE_DI_DOCUMENT_TEST_XML,
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
        configureParameterToAllowDuplicateFileProcessing();
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.SINGLE_YEDI_DOCUMENT_TEST);
    }

    private void configureParameterToAllowDuplicateFileProcessing() {
        Mockito.when(parameterService.getParameterValueAsBoolean(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME,
                CuFPParameterConstants.CreateAccountingDocumentService.DUPLICATE_FILE_CHECK_IND))
                .thenReturn(Boolean.FALSE);
    }

}
