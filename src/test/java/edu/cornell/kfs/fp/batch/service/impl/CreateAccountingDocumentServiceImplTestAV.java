package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.impl.AccountingPeriodServiceImpl;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingPeriodFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestAV extends CreateAccountingDocumentServiceImplBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new AVTestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService, buildAccountingPeriodService());
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.AV_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testLoadSingleFileWithSingleAVDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-av-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_AV_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleAVDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-av-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_AV_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleAVDocumentsPlusDocumentsWithRulesFailures() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-av-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_AV_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST);
    }

    protected void assertAccountingDocumentIsCorrect(Class<? extends AccountingDocument> documentClass,
            AccountingDocument expectedDocument, AccountingDocument actualDocument) {
        super.assertAccountingDocumentIsCorrect(documentClass, expectedDocument, actualDocument);
        if (AuxiliaryVoucherDocument.class.isAssignableFrom(documentClass)) {
            assertAuxiliaryVoucherDocumentIsCorrect((AuxiliaryVoucherDocument) expectedDocument,
                    (AuxiliaryVoucherDocument) actualDocument);
        }
    }

    private void assertAuxiliaryVoucherDocumentIsCorrect(AuxiliaryVoucherDocument expectedDocument,
            AuxiliaryVoucherDocument actualDocument) {
        assertEquals("Wrong accounting period code", expectedDocument.getPostingPeriodCode(),
                actualDocument.getPostingPeriodCode());
        assertEquals("Wrong accounting period fiscal year", expectedDocument.getPostingYear(),
                actualDocument.getPostingYear());
        assertEquals("Wrong AV document type", expectedDocument.getTypeCode(), actualDocument.getTypeCode());
        assertEquals("Wrong reversal date", expectedDocument.getReversalDate(), actualDocument.getReversalDate());
    }

    @Override
    protected ConfigurationService buildMockConfigurationService() throws Exception {
        ConfigurationService configurationService = super.buildMockConfigurationService();
        Mockito.when(configurationService
                .getPropertyValueAsString(Mockito.startsWith(CuFPTestConstants.AV_VALIDATION_MESSAGE_KEY_PREFIX)))
                .then(this::buildAuxiliaryVoucherErrorMessage);
        return configurationService;
    }

    private String buildAuxiliaryVoucherErrorMessage(InvocationOnMock invocation) {
        String messageKey = invocation.getArgument(0);
        return messageKey + " {0}";
    }

    private AccountingPeriodService buildAccountingPeriodService() {
        AccountingPeriodServiceImpl accountingPeriodService = Mockito.spy(new AccountingPeriodServiceImpl());
        accountingPeriodService.setBusinessObjectService(buildMockBusinessObjectService());
        Mockito.doReturn(buildListOfOpenAccountingPeriods()).when(accountingPeriodService).getOpenAccountingPeriods();
        return accountingPeriodService;
    }

    private BusinessObjectService buildMockBusinessObjectService() {
        BusinessObjectService businessObjectService = Mockito.mock(BusinessObjectService.class);
        Mockito.when(businessObjectService.findByPrimaryKey(Mockito.eq(AccountingPeriod.class), Mockito.anyMap()))
                .then(this::findAccountingPeriod);
        return businessObjectService;
    }

    private AccountingPeriod findAccountingPeriod(InvocationOnMock invocation) {
        Map<?, ?> primaryKeys = invocation.getArgument(1);
        String periodCode = (String) primaryKeys.get(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE);
        Integer fiscalYear = (Integer) primaryKeys.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
        return findAccountingPeriod(fiscalYear, periodCode);
    }

    private AccountingPeriod findAccountingPeriod(Integer fiscalYear, String periodCode) {
        Optional<AccountingPeriodFixture> result = AccountingPeriodFixture.findAccountingPeriod(fiscalYear, periodCode);
        return result.isPresent() ? result.get().toAccountingPeriod() : null;
    }

    private List<AccountingPeriod> buildListOfOpenAccountingPeriods() {
        return AccountingPeriodFixture.findOpenAccountingPeriodsAsStream()
                .map(AccountingPeriodFixture::toAccountingPeriod).collect(Collectors.toCollection(ArrayList::new));
    }

    protected static class AVTestCreateAccountingDocumentServiceImpl extends TestCreateAccountingDocumentServiceImpl {
        public AVTestCreateAccountingDocumentServiceImpl(PersonService personService,
                AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService,
                ConfigurationService configurationService, UniversityDateService universityDateService,
                DateTimeService dateTimeService, AccountingPeriodService accountingPeriodService) {
            super(personService, downloadAttachmentService, configurationService, universityDateService,
                    dateTimeService);
            this.accountingPeriodService = accountingPeriodService;

        }

        protected AccountingDocumentGenerator<?> buildAccountingDocumentGenerator(
                BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>> generatorConstructor) {
            AuxiliaryVoucherDocumentGenerator avGenerator = (AuxiliaryVoucherDocumentGenerator) super.buildAccountingDocumentGenerator(
                    generatorConstructor);
            avGenerator.setAccountingPeriodService(accountingPeriodService);
            avGenerator.setDateTimeService(dateTimeService);
            return avGenerator;
        }
    }
}
