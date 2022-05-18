package edu.cornell.kfs.module.ld.document.service.impl;

import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.document.service.LaborPendingEntryConverterService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.ld.document.CuSalaryExpenseTransferDocument;
import edu.cornell.kfs.module.ld.fixture.CuSalaryExpenseTransferDocumentFixture;

@ConfigureContext(session = UserNameFixture.amg49)
public class CuLaborPendingEntryConverterServiceImplTest extends KualiIntegTestBase {

    private LaborPendingEntryConverterService laborPendingEntryConverterService;
    private DocumentService documentService;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        laborPendingEntryConverterService = SpringContext.getBean(LaborPendingEntryConverterService.class);
        documentService = SpringContext.getBean(DocumentService.class);

    }
    
    public void testGetBenefitClearingPendingEntry() throws IllegalAccessException, InstantiationException, WorkflowException {
        CuSalaryExpenseTransferDocument document = CuSalaryExpenseTransferDocumentFixture.GOOD_ST.createSalaryExpenseDocument();
        AccountingDocumentTestUtils.saveDocument(document, documentService);
        
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        LaborLedgerPendingEntry laborLedgerPendingEntry = ((CuLaborPendingEntryConverterServiceImpl) laborPendingEntryConverterService).
                getBenefitClearingPendingEntry(document, sequenceHelper, "R704750", "IT", "MX", new KualiDecimal(6916.68), "5200");
        assertEquals("1", laborLedgerPendingEntry.getEmplid());
    }

}
