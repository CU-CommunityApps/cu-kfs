package edu.cornell.kfs.module.ar.document.validation.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.mockito.Mockito;

import edu.cornell.kfs.module.ar.document.service.impl.CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryServiceImpl;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryTest {
    
    private CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryServiceImpl suspensionCategory;
    private ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument;
    
    private static final KualiDecimal STANDARD_BUDGET_TOTAL = new KualiDecimal(50);

    @Before
    public void setUp() throws Exception {
        suspensionCategory = new CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        suspensionCategory = null;
        contractsGrantsInvoiceDocument = null;
    }
    
    @Test
    public void testSuspensionByBudgetAmountLessThanBudget() {
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(25), STANDARD_BUDGET_TOTAL);
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByBudgetAmountEqualBudget() {
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(50), STANDARD_BUDGET_TOTAL);
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByBudgetAmountMoreThanBudget() {
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(55), STANDARD_BUDGET_TOTAL);
        assertTrue(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByBudgetAmountNullBudgetTotal() {
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(1), null);
        assertTrue(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    private void prepareContractsGrantsInvoiceDocument(KualiDecimal totalAmountBilledToDate, KualiDecimal budgetTotal) {
        contractsGrantsInvoiceDocument = Mockito.spy(new ContractsGrantsInvoiceDocument());
        
        Award award = new Award();
        
        AwardExtendedAttribute attribute = new AwardExtendedAttribute();
        attribute.setBudgetTotalAmount(budgetTotal);
        
        award.setExtension(attribute);
        
        InvoiceGeneralDetail invoiceGeneralDetail = Mockito.mock(InvoiceGeneralDetail.class);
        Mockito.when(invoiceGeneralDetail.getTotalAmountBilledToDate()).thenReturn(totalAmountBilledToDate);
        Mockito.when(invoiceGeneralDetail.getAward()).thenReturn(award);
        
        Mockito.doReturn(invoiceGeneralDetail).when(contractsGrantsInvoiceDocument).getInvoiceGeneralDetail();
    }

}
