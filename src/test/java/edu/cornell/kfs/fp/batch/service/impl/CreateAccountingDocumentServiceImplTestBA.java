package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTravelService;
import org.kuali.kfs.fp.service.FiscalYearFunctionControlService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.document.service.VendorService;

import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;

public class CreateAccountingDocumentServiceImplTestBA extends CreateAccountingDocumentServiceImplBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new BaTestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockFiscalYearFunctionControlService(), buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.BA_DOCUMENT,
                AccountingDocumentMapping.YEBA_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testLoadSingleFileWithSingleBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentLackingBAAccountProperties() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-no-base-or-months-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_NO_BASEAMOUNT_OR_MONTHS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentUsingNonZeroBaseAmounts() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-nonzero-base-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_NONZERO_BASEAMOUNT_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentUsingMultipleMonthAmounts() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-multi-months-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_MULTI_MONTHS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleBADocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleBADocumentsPlusDocumentsWithRulesFailures() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ba-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST);
    }
    
    @Test
    public void testLoadSingleFileWithSingleYEBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yeba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_YEBA_DOCUMENT_TEST);
    }
    
    @Test
    public void testLoadSingleFileWithMutliYEBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-yeba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MUTLI_YEBA_DOCUMENT_TEST);
    }
    
    @Override
    protected void assertAccountingLineIsCorrect(AccountingLine expectedLine, AccountingLine actualLine) {
        super.assertAccountingLineIsCorrect(expectedLine, actualLine);
        assertBudgetAdjustmentAccountingLinePropertiesAreCorrect(
                (BudgetAdjustmentAccountingLine) expectedLine, (BudgetAdjustmentAccountingLine) actualLine);
    }
    
    private void assertBudgetAdjustmentAccountingLinePropertiesAreCorrect(
            BudgetAdjustmentAccountingLine expectedLine, BudgetAdjustmentAccountingLine actualLine) {
        assertEquals("Wrong base amount", expectedLine.getBaseBudgetAdjustmentAmount(), actualLine.getBaseBudgetAdjustmentAmount());
        assertEquals("Wrong current amount", expectedLine.getCurrentBudgetAdjustmentAmount(), actualLine.getCurrentBudgetAdjustmentAmount());
        assertEquals("Wrong month 01 amount",
                expectedLine.getFinancialDocumentMonth1LineAmount(), actualLine.getFinancialDocumentMonth1LineAmount());
        assertEquals("Wrong month 02 amount",
                expectedLine.getFinancialDocumentMonth2LineAmount(), actualLine.getFinancialDocumentMonth2LineAmount());
        assertEquals("Wrong month 03 amount",
                expectedLine.getFinancialDocumentMonth3LineAmount(), actualLine.getFinancialDocumentMonth3LineAmount());
        assertEquals("Wrong month 04 amount",
                expectedLine.getFinancialDocumentMonth4LineAmount(), actualLine.getFinancialDocumentMonth4LineAmount());
        assertEquals("Wrong month 05 amount",
                expectedLine.getFinancialDocumentMonth5LineAmount(), actualLine.getFinancialDocumentMonth5LineAmount());
        assertEquals("Wrong month 06 amount",
                expectedLine.getFinancialDocumentMonth6LineAmount(), actualLine.getFinancialDocumentMonth6LineAmount());
        assertEquals("Wrong month 07 amount",
                expectedLine.getFinancialDocumentMonth7LineAmount(), actualLine.getFinancialDocumentMonth7LineAmount());
        assertEquals("Wrong month 08 amount",
                expectedLine.getFinancialDocumentMonth8LineAmount(), actualLine.getFinancialDocumentMonth8LineAmount());
        assertEquals("Wrong month 09 amount",
                expectedLine.getFinancialDocumentMonth9LineAmount(), actualLine.getFinancialDocumentMonth9LineAmount());
        assertEquals("Wrong month 10 amount",
                expectedLine.getFinancialDocumentMonth10LineAmount(), actualLine.getFinancialDocumentMonth10LineAmount());
        assertEquals("Wrong month 11 amount",
                expectedLine.getFinancialDocumentMonth11LineAmount(), actualLine.getFinancialDocumentMonth11LineAmount());
        assertEquals("Wrong month 12 amount",
                expectedLine.getFinancialDocumentMonth12LineAmount(), actualLine.getFinancialDocumentMonth12LineAmount());
    }
    
    protected static class BaTestCreateAccountingDocumentServiceImpl extends TestCreateAccountingDocumentServiceImpl {

        public BaTestCreateAccountingDocumentServiceImpl(
                PersonService personService, AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService,
                ConfigurationService configurationService, FiscalYearFunctionControlService fiscalYearFunctionControlService, 
                UniversityDateService universityDateService, DateTimeService dateTimeService) {
            super(personService, downloadAttachmentService, configurationService, fiscalYearFunctionControlService, universityDateService, dateTimeService);
        }
        
        @Override 
        protected AccountingDocumentGenerator<?> buildAccountingDocumentGenerator(
                BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>> generatorConstructor) {
            AccountingDocumentGenerator accountingDocumentGenerator = super.buildAccountingDocumentGenerator(generatorConstructor);
            if (accountingDocumentGenerator instanceof CuBudgetAdjustmentDocumentGenerator) {
                CuBudgetAdjustmentDocumentGenerator baGenerator = (CuBudgetAdjustmentDocumentGenerator) accountingDocumentGenerator;
                baGenerator.setFiscalYearFunctionControlService(fiscalYearFunctionControlService);
            } else if (accountingDocumentGenerator instanceof CuYearEndBudgetAdjustmentDocumentGenerator) {
                CuYearEndBudgetAdjustmentDocumentGenerator yebaGenerator = (CuYearEndBudgetAdjustmentDocumentGenerator) accountingDocumentGenerator;
                yebaGenerator.setFiscalYearFunctionControlService(fiscalYearFunctionControlService);
            }
            return accountingDocumentGenerator;
        }
    }
}
