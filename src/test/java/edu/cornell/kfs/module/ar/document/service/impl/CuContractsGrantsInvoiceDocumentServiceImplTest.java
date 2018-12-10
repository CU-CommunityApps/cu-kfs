package edu.cornell.kfs.module.ar.document.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.krad.document.DocumentBase;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContractsGrantsInvoiceDocument.class})
public class CuContractsGrantsInvoiceDocumentServiceImplTest {
    
    private CuContractsGrantsInvoiceDocumentServiceImpl cuContractsGrantsInvoiceDocumentServiceImpl;
    private ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument;

    @Before
    public void setUp() throws Exception {
        cuContractsGrantsInvoiceDocumentServiceImpl   = PowerMockito.spy(new CuContractsGrantsInvoiceDocumentServiceImpl());
        PowerMockito.doNothing().when(cuContractsGrantsInvoiceDocumentServiceImpl, "recalculateObjectCodeByCategory", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @After
    public void tearDown() throws Exception {
        cuContractsGrantsInvoiceDocumentServiceImpl = null;
        contractsGrantsInvoiceDocument = null;
    }
    
    @Test
    public void testCinvProrateOneInvoiceOverBudget() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(50);
        KualiDecimal budgetTotal = new KualiDecimal(150);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 200);
        assertEquals(new KualiDecimal(100), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateOneInvoiceUnderBudget() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(50);
        KualiDecimal budgetTotal = new KualiDecimal(150);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 75);
        assertEquals(new KualiDecimal(75), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateOneInvoiceOnBudget() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(50);
        KualiDecimal budgetTotal = new KualiDecimal(150);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 100);
        assertEquals(new KualiDecimal(100), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateTwoInvoicesOverBudgetSameAmount() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(50);
        KualiDecimal budgetTotal = new KualiDecimal(150);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 100, 100);
        assertEquals(new KualiDecimal(50), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
        assertEquals(new KualiDecimal(50), contractsGrantsInvoiceDocument.getInvoiceDetails().get(1).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateTwoInvoicesOverBudgetDifferingAmount() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(50);
        KualiDecimal budgetTotal = new KualiDecimal(150);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 75, 100);
        assertEquals(new KualiDecimal(42.85), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
        assertEquals(new KualiDecimal(57.15), contractsGrantsInvoiceDocument.getInvoiceDetails().get(1).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateTwoInvoicesUnderBudgetSameAmount() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(50);
        KualiDecimal budgetTotal = new KualiDecimal(300);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 100, 100);
        assertEquals(new KualiDecimal(100), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
        assertEquals(new KualiDecimal(100), contractsGrantsInvoiceDocument.getInvoiceDetails().get(1).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateTwoInvoicesUnderBudgetDifferingAmount() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(50);
        KualiDecimal budgetTotal = new KualiDecimal(300);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 75, 100);
        assertEquals(new KualiDecimal(75), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
        assertEquals(new KualiDecimal(100), contractsGrantsInvoiceDocument.getInvoiceDetails().get(1).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateOneInvoiceNoPreviousAmountOverBudget() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(0);
        KualiDecimal budgetTotal = new KualiDecimal(150);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 200);
        assertEquals(new KualiDecimal(150), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateOneInvoiceNoPreviousAmountUnderBudget() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(0);
        KualiDecimal budgetTotal = new KualiDecimal(150);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 110);
        assertEquals(new KualiDecimal(110), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
    }
    
    @Test
    public void testCinvProrateOneInvoiceNoPreviousAmountOnBudget() throws Exception {
        KualiDecimal totalAmountBilledToDate = new KualiDecimal(0);
        KualiDecimal budgetTotal = new KualiDecimal(150);
        
        prepareCinvDocumentAndProrate(totalAmountBilledToDate, budgetTotal, 150);
        assertEquals(new KualiDecimal(150), contractsGrantsInvoiceDocument.getInvoiceDetails().get(0).getInvoiceAmount());
    }
    
    
    private void prepareCinvDocumentAndProrate(KualiDecimal totalAmountBilledToDate, KualiDecimal budgetTotal, double... invoiceAmounts) throws Exception {
        PowerMockito.doReturn(totalAmountBilledToDate).when(cuContractsGrantsInvoiceDocumentServiceImpl, "getAwardBilledToDateAmount", Mockito.any());
        PowerMockito.doReturn(totalAmountBilledToDate).when(cuContractsGrantsInvoiceDocumentServiceImpl, "getOtherTotalBilledForAwardPeriod", Mockito.any());
        
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        contractsGrantsInvoiceDocument = PowerMockito.spy(new ContractsGrantsInvoiceDocument());
        PowerMockito.doReturn(true).when(contractsGrantsInvoiceDocument, "isCorrectionDocument");
        
        Award award = new Award();
        
        AwardExtendedAttribute attribute = new AwardExtendedAttribute();
        attribute.setBudgetTotalAmount(budgetTotal);
        
        award.setExtension(attribute);
        
        InvoiceGeneralDetail invoiceGeneralDetail = Mockito.mock(InvoiceGeneralDetail.class);
        Mockito.when(invoiceGeneralDetail.getTotalAmountBilledToDate()).thenReturn(totalAmountBilledToDate);
        Mockito.when(invoiceGeneralDetail.getTotalPreviouslyBilled()).thenReturn(totalAmountBilledToDate);
        Mockito.when(invoiceGeneralDetail.getAward()).thenReturn(award);
        
        for (double invoiceAmount : invoiceAmounts) {
            ContractsGrantsInvoiceDetail detail = new ContractsGrantsInvoiceDetail();
            detail.setInvoiceAmount(new KualiDecimal(invoiceAmount));
            detail.setCategoryCode("tst");
            contractsGrantsInvoiceDocument.getInvoiceDetails().add(detail);
        }
        
        contractsGrantsInvoiceDocument.setInvoiceGeneralDetail(invoiceGeneralDetail);
        
        cuContractsGrantsInvoiceDocumentServiceImpl.prorateBill(contractsGrantsInvoiceDocument);
    }

    
}
