package edu.cornell.kfs.fp.batch.service.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.service.impl.CreateAccountingDocumentServiceImplTestBase.TestCreateAccountingDocumentServiceImpl;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestPE extends CreateAccountingDocumentServiceImplTestBase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Configurator.setLevel(PreEncumbranceDocumentGenerator.class.getName(), Level.DEBUG);
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.PE_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }
    
    @Test
    public void testLoadSingleFileWithSinglePEDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-pe-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_PE_DOCUMENT_TEST);
    }
}
