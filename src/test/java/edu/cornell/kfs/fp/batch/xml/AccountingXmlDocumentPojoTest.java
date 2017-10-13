package edu.cornell.kfs.fp.batch.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.collections.CollectionUtils;
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
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedDocument.getSourceAccountingLines(), actualDocument.getSourceAccountingLines(),
                "sourceAccountingLines", this::assertAccountingLineWasUnmarshalledCorrectly);
        assertListOfXmlPojosWasUnmarshalledCorrectly(
                expectedDocument.getTargetAccountingLines(), actualDocument.getTargetAccountingLines(),
                "targetAccountingLines", this::assertAccountingLineWasUnmarshalledCorrectly);
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
        if (expectedItems.isEmpty()) {
            assertTrue(listName + " list should have been null or empty", CollectionUtils.isEmpty(actualItems));
        } else {
            assertEquals(listName + " list has the wrong number of items", expectedItems.size(), actualItems.size());
            for (int i = 0; i < expectedItems.size(); i++) {
                itemValidator.accept(expectedItems.get(i), actualItems.get(i));
            }
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
