package edu.cornell.kfs.module.ar.document.validation.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.krad.document.DocumentBase;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContractsGrantsInvoiceDocument.class})
@PowerMockIgnore({"javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*"}) 
public class CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryTest {
    
    private CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategory suspensionCategory;
    private ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument;
    
    private static final KualiDecimal STANDARD_BUDGET_TOTAL = new KualiDecimal(50);

    @Before
    public void setUp() throws Exception {
        suspensionCategory = new CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategory();
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
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        contractsGrantsInvoiceDocument = PowerMockito.spy(new ContractsGrantsInvoiceDocument());
        
        Award award = new Award();
        
        AwardExtendedAttribute attribute = new AwardExtendedAttribute();
        attribute.setBudgetTotalAmount(budgetTotal);
        
        award.setExtension(attribute);
        
        InvoiceGeneralDetail invoiceGeneralDetail = Mockito.mock(InvoiceGeneralDetail.class);
        Mockito.when(invoiceGeneralDetail.getTotalAmountBilledToDate()).thenReturn(totalAmountBilledToDate);
        Mockito.when(invoiceGeneralDetail.getAward()).thenReturn(award);
        
        contractsGrantsInvoiceDocument.setInvoiceGeneralDetail(invoiceGeneralDetail);
    }

}
