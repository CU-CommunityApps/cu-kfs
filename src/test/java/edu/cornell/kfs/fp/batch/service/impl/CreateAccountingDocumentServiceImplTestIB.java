package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.fp.businessobject.InternalBillingItem;
import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestIB extends CreateAccountingDocumentServiceImplTestBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.IB_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testLoadSingleFileWithSingleIBDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ib-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleIBDocumentLackingItems() throws Exception {
        copyTestFilesAndCreateDoneFiles("ib-without-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_NO_ITEMS_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleIBDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ib-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleIBDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ib-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST);
    }

    @SuppressWarnings("unchecked")
    protected void assertAccountingDocumentIsCorrect(Class<? extends AccountingDocument> documentClass,
            AccountingDocument expectedDocument, AccountingDocument actualDocument) {
        super.assertAccountingDocumentIsCorrect(documentClass, expectedDocument, actualDocument);
        if (InternalBillingDocument.class.isAssignableFrom(documentClass)) {
            assertObjectListIsCorrect("items", ((InternalBillingDocument) expectedDocument).getItems(),
                    ((InternalBillingDocument) actualDocument).getItems(), this::assertInternalBillingItemIsCorrect);
        }
    }

    private void assertInternalBillingItemIsCorrect(InternalBillingItem expectedItem, InternalBillingItem actualItem) {
        assertEquals("Wrong document number", expectedItem.getDocumentNumber(), actualItem.getDocumentNumber());
        assertEquals("Wrong item sequence number", expectedItem.getItemSequenceId(), actualItem.getItemSequenceId());
        assertEquals("Wrong service date", expectedItem.getItemServiceDate(), actualItem.getItemServiceDate());
        assertEquals("Wrong stock number", expectedItem.getItemStockNumber(), actualItem.getItemStockNumber());
        assertEquals("Wrong item description", expectedItem.getItemStockDescription(),
                actualItem.getItemStockDescription());
        assertEquals("Wrong item quantity", expectedItem.getItemQuantity(), actualItem.getItemQuantity());
        assertEquals("Wrong unit of measure", expectedItem.getUnitOfMeasureCode(), actualItem.getUnitOfMeasureCode());
        assertEquals("Wrong item cost", expectedItem.getItemUnitAmount(), actualItem.getItemUnitAmount());
    }

}
