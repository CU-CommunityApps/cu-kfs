package edu.cornell.kfs.fp.batch.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class AccountingXmlDocumentPojoTest {

    private static final String BASE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/";

    private CUMarshalService marshalService;

    @Before
    public void setUp() throws Exception {
        this.marshalService = new CUMarshalServiceImpl();
    }

    @Test
    public void testLoadMultipleDIsFromSameFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_TEST, "multi-di-document-test.xml");
    }

    @Test
    public void testLoadSingleDIFromFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST, "single-di-document-test.xml");
    }

    @Test
    public void testLoadDIWithFullyFilledAccountingLines() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.DI_FULL_ACCOUNT_LINE_TEST, "di-full-account-line-test.xml");
    }

    @Test
    public void testLoadDIWithSingletonElementLists() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.DI_SINGLE_ELEMENT_LISTS_TEST, "di-single-element-lists-test.xml");
    }

    @Test
    public void testLoadDIWithEmptyElementLists() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.DI_EMPTY_ELEMENT_LISTS_TEST, "di-empty-element-lists-test.xml");
    }

    @Test
    public void testLoadDIWithoutAnyElementLists() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.DI_WITHOUT_ELEMENT_LISTS_TEST, "di-without-element-lists-test.xml");
    }

    @Test
    public void testLoadXmlWithEmptyDocumentList() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_LIST_TEST, "empty-document-list-test.xml");
    }

    @Test
    public void testLoadXmlWithoutAnyDocumentList() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.NO_DOCUMENT_LIST_TEST, "no-document-list-test.xml");
    }

    @Test
    public void testLoadMultipleIBsFromSameFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_TEST, "multi-ib-document-test.xml");
    }

    @Test
    public void testLoadSingleIBFromFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_TEST, "single-ib-document-test.xml");
    }

    @Test
    public void testLoadSingleBAFromFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_DOCUMENT_TEST, "single-ba-document-test.xml");
    }

    @Test
    public void testLoadMultipleBAsFromSameFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_TEST, "multi-ba-document-test.xml");
    }

    @Test
    public void testLoadMultipleSBsFromSameFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.MULTI_SB_DOCUMENT_TEST, "multi-sb-document-test.xml");
    }

    @Test
    public void testLoadSingleSBFromFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.SINGLE_SB_DOCUMENT_TEST, "single-sb-document-test.xml");
    }

    @Test
    public void testLoadVaryingDocTypesFromSameFile() throws Exception {
        assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
                AccountingXmlDocumentListWrapperFixture.MULTI_DOCUMENT_TYPES_TEST, "multi-doc-types-test.xml");
    }

    private void assertAccountingDocumentXmlFileCanBeUnmarshalledCorrectly(
            AccountingXmlDocumentListWrapperFixture expectedResultFixture, String localFileName) throws Exception {
        AccountingXmlDocumentListWrapper expectedResult = expectedResultFixture.toDocumentListWrapperPojo();
        AccountingXmlDocumentListWrapper actualResult = loadAccountingDocumentXmlFromFile(localFileName);
        assertAccountingDocumentXmlFileWasUnmarshalledCorrectly(expectedResult, actualResult);
    }

    private void assertAccountingDocumentXmlFileWasUnmarshalledCorrectly(
            AccountingXmlDocumentListWrapper expectedResult, AccountingXmlDocumentListWrapper actualResult) {
        assertEquals("Wrong create date", expectedResult.getCreateDate(), actualResult.getCreateDate());
        assertEquals("Wrong report email", expectedResult.getReportEmail(), actualResult.getReportEmail());
        assertEquals("Wrong overview", expectedResult.getOverview(), actualResult.getOverview());
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedResult.getDocuments(), actualResult.getDocuments(),
                "documents", this::assertDocumentWasUnmarshalledCorrectly);
    }

    private void assertDocumentWasUnmarshalledCorrectly(
            AccountingXmlDocumentEntry expectedDocument, AccountingXmlDocumentEntry actualDocument) {
        assertEquals("Wrong document index", expectedDocument.getIndex(), actualDocument.getIndex());
        assertEquals("Wrong document type", expectedDocument.getDocumentTypeCode(), actualDocument.getDocumentTypeCode());
        assertEquals("Wrong document description", expectedDocument.getDescription(), actualDocument.getDescription());
        assertEquals("Wrong document explanation", expectedDocument.getExplanation(), actualDocument.getExplanation());
        assertEquals("Wrong org doc number", expectedDocument.getOrganizationDocumentNumber(), actualDocument.getOrganizationDocumentNumber());
        assertEquals("Wrong posting fiscal year", expectedDocument.getPostingFiscalYear(), actualDocument.getPostingFiscalYear());
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedDocument.getSourceAccountingLines(), actualDocument.getSourceAccountingLines(),
                "sourceAccountingLines", this::assertAccountingLineWasUnmarshalledCorrectly);
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedDocument.getTargetAccountingLines(), actualDocument.getTargetAccountingLines(),
                "targetAccountingLines", this::assertAccountingLineWasUnmarshalledCorrectly);
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedDocument.getItems(), actualDocument.getItems(),
                "items", this::assertItemWasUnmarshalledCorrectly);
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedDocument.getNotes(), actualDocument.getNotes(),
                "notes", this::assertNoteWasUnmarshalledCorrectly);
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedDocument.getAdHocRecipients(), actualDocument.getAdHocRecipients(),
                "adHocRecipients", this::assertAdHocRecipientWasUnmarshalledCorrectly);
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedDocument.getBackupLinks(), actualDocument.getBackupLinks(),
                "backupLinks", this::assertBackupLinkWasUnmarshalledCorrectly);
    }

    private <T> void assertListOfXmlPojosWasUnmarshalledCorrectly(
            List<T> expectedItems, List<T> actualItems, String listName, BiConsumer<T, T> itemValidator) {
        assertEquals(listName + " list has the wrong number of items", expectedItems.size(), actualItems.size());
        for (int i = 0; i < expectedItems.size(); i++) {
            itemValidator.accept(expectedItems.get(i), actualItems.get(i));
        }
    }

    private void assertAccountingLineWasUnmarshalledCorrectly(
            AccountingXmlDocumentAccountingLine expectedLine, AccountingXmlDocumentAccountingLine actualLine) {
        assertEquals("Wrong chart code", expectedLine.getChartCode(), actualLine.getChartCode());
        assertEquals("Wrong account number", expectedLine.getAccountNumber(), actualLine.getAccountNumber());
        assertEquals("Wrong sub-account number", expectedLine.getSubAccountNumber(), actualLine.getSubAccountNumber());
        assertEquals("Wrong object code", expectedLine.getObjectCode(), actualLine.getObjectCode());
        assertEquals("Wrong sub-object code", expectedLine.getSubObjectCode(), actualLine.getSubObjectCode());
        assertEquals("Wrong project code", expectedLine.getProjectCode(), actualLine.getProjectCode());
        assertEquals("Wrong org ref id", expectedLine.getOrgRefId(), actualLine.getOrgRefId());
        assertEquals("Wrong line description", expectedLine.getLineDescription(), actualLine.getLineDescription());
        assertEquals("Wrong line amount", expectedLine.getAmount(), actualLine.getAmount());
        assertEquals("Wrong base amount", expectedLine.getBaseAmount(), actualLine.getBaseAmount());
        assertEquals("Wrong month 01 amount", expectedLine.getMonth01Amount(), actualLine.getMonth01Amount());
        assertEquals("Wrong month 02 amount", expectedLine.getMonth02Amount(), actualLine.getMonth02Amount());
        assertEquals("Wrong month 03 amount", expectedLine.getMonth03Amount(), actualLine.getMonth03Amount());
        assertEquals("Wrong month 04 amount", expectedLine.getMonth04Amount(), actualLine.getMonth04Amount());
        assertEquals("Wrong month 05 amount", expectedLine.getMonth05Amount(), actualLine.getMonth05Amount());
        assertEquals("Wrong month 06 amount", expectedLine.getMonth06Amount(), actualLine.getMonth06Amount());
        assertEquals("Wrong month 07 amount", expectedLine.getMonth07Amount(), actualLine.getMonth07Amount());
        assertEquals("Wrong month 08 amount", expectedLine.getMonth08Amount(), actualLine.getMonth08Amount());
        assertEquals("Wrong month 09 amount", expectedLine.getMonth09Amount(), actualLine.getMonth09Amount());
        assertEquals("Wrong month 10 amount", expectedLine.getMonth10Amount(), actualLine.getMonth10Amount());
        assertEquals("Wrong month 11 amount", expectedLine.getMonth11Amount(), actualLine.getMonth11Amount());
        assertEquals("Wrong month 12 amount", expectedLine.getMonth12Amount(), actualLine.getMonth12Amount());
    }

    private void assertItemWasUnmarshalledCorrectly(
            AccountingXmlDocumentItem expectedItem, AccountingXmlDocumentItem actualItem) {
        assertEquals("Wrong service date", expectedItem.getServiceDate(), actualItem.getServiceDate());
        assertEquals("Wrong stock number", expectedItem.getStockNumber(), actualItem.getStockNumber());
        assertEquals("Wrong item description", expectedItem.getDescription(), actualItem.getDescription());
        assertEquals("Wrong item quantity", expectedItem.getQuantity(), actualItem.getQuantity());
        assertEquals("Wrong UOM code", expectedItem.getUnitOfMeasureCode(), actualItem.getUnitOfMeasureCode());
        assertEquals("Wrong item cost", expectedItem.getItemCost(), actualItem.getItemCost());
    }

    private void assertNoteWasUnmarshalledCorrectly(
            AccountingXmlDocumentNote expectedNote, AccountingXmlDocumentNote actualNote) {
        assertEquals("Wrong note text", expectedNote.getDescription(), actualNote.getDescription());
    }

    private void assertAdHocRecipientWasUnmarshalledCorrectly(
            AccountingXmlDocumentAdHocRecipient expectedRecipient, AccountingXmlDocumentAdHocRecipient actualRecipient) {
        assertEquals("Wrong recipient netid", expectedRecipient.getNetId(), actualRecipient.getNetId());
        assertEquals("Wrong action requested", expectedRecipient.getActionRequested(), actualRecipient.getActionRequested());
    }

    private void assertBackupLinkWasUnmarshalledCorrectly(
            AccountingXmlDocumentBackupLink expectedLink, AccountingXmlDocumentBackupLink actualLink) {
        assertEquals("Wrong link URL", expectedLink.getLinkUrl(), actualLink.getLinkUrl());
        assertEquals("Wrong link description", expectedLink.getDescription(), actualLink.getDescription());
    }

    private AccountingXmlDocumentListWrapper loadAccountingDocumentXmlFromFile(String localFileName) throws Exception {
        File testFile = new File(BASE_TEST_FILE_PATH + localFileName);
        return marshalService.unmarshalFile(testFile, AccountingXmlDocumentListWrapper.class);
    }

}
