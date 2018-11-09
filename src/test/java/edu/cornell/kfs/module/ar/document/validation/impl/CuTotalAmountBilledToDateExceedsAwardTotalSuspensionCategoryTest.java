package edu.cornell.kfs.module.ar.document.validation.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.document.DocumentBase;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.ar.CuArConstants;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContractsGrantsInvoiceDocument.class})
public class CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategoryTest {
    
    private CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategory suspensionCategory;
    private ParameterService parameterService;
    private ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument;
    
    private static final KualiDecimal STANDARD_AWARD_TOTAL = new KualiDecimal(100);
    private static final KualiDecimal STANDARD_BUDGET_TOTAL = new KualiDecimal(50);

    @Before
    public void setUp() throws Exception {
        suspensionCategory = new CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategory();
        parameterService = Mockito.mock(ParameterService.class);
    }

    @After
    public void tearDown() throws Exception {
        suspensionCategory = null;
        parameterService = null;
        contractsGrantsInvoiceDocument = null;
    }

    @Test
    public void shouldValidateOnBudgetTotalForTrueParam() {
        configureParameterServiceForSuspensionCheck(Boolean.TRUE);
        assertTrue(suspensionCategory.shouldValidateOnBudgetTotal());
    }
    
    @Test
    public void shouldValidateOnBudgetTotalForFalseParam() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        assertFalse(suspensionCategory.shouldValidateOnBudgetTotal());
    }
    
    @Test
    public void testSuspensionByBudgetAmountLessThanBudget() {
        configureParameterServiceForSuspensionCheck(Boolean.TRUE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(25), STANDARD_AWARD_TOTAL, STANDARD_BUDGET_TOTAL);
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByBudgetAmountEqualBudget() {
        configureParameterServiceForSuspensionCheck(Boolean.TRUE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(50), STANDARD_AWARD_TOTAL, STANDARD_BUDGET_TOTAL);
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByBudgetAmountMoreThanBudget() {
        configureParameterServiceForSuspensionCheck(Boolean.TRUE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(55), STANDARD_AWARD_TOTAL, STANDARD_BUDGET_TOTAL);
        assertTrue(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByBudgetAmountNullBudgetTotal() {
        configureParameterServiceForSuspensionCheck(Boolean.TRUE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(1), STANDARD_AWARD_TOTAL, null);
        assertTrue(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByAwardTotalAmountLessThanBudget() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(25), STANDARD_AWARD_TOTAL, STANDARD_BUDGET_TOTAL);
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByAwardTotalAmountLessThanAwardTotal() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(55), STANDARD_AWARD_TOTAL, STANDARD_BUDGET_TOTAL);
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByAwardTotalAmountEqualAwardTotal() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(100), STANDARD_AWARD_TOTAL, STANDARD_BUDGET_TOTAL);
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByAwardTotalAmountMoreThanAwardTotal() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(105), STANDARD_AWARD_TOTAL, STANDARD_BUDGET_TOTAL);
        assertTrue(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    private void configureParameterServiceForSuspensionCheck(Boolean value) {
        Mockito.when(parameterService.getParameterValueAsBoolean(ArConstants.AR_NAMESPACE_CODE, 
                ArConstants.CONTRACTS_GRANTS_INVOICE_COMPONENT, CuArConstants.CG_INVOICE_AMT_BILLED_SUSPENSION_BY_BUDGET_TOTAL, Boolean.TRUE)).thenReturn(value);
        suspensionCategory.setParameterService(parameterService);
    }
    
    private void prepareContractsGrantsInvoiceDocument(KualiDecimal totalAmountBilledToDate, KualiDecimal awardTotal, KualiDecimal budgetTotal) {
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        contractsGrantsInvoiceDocument = PowerMockito.spy(new ContractsGrantsInvoiceDocument());
        
        Award award = new Award();
        KualiDecimal halfAwardTotal = awardTotal.divide(new KualiDecimal(2));
        award.setAwardIndirectCostAmount(halfAwardTotal);
        award.setAwardDirectCostAmount(halfAwardTotal);
        
        AwardExtendedAttribute attribute = new AwardExtendedAttribute();
        attribute.setBudgetTotalAmount(budgetTotal);
        
        award.setExtension(attribute);
        
        InvoiceGeneralDetail invoiceGeneralDetail = Mockito.mock(InvoiceGeneralDetail.class);
        Mockito.when(invoiceGeneralDetail.getTotalAmountBilledToDate()).thenReturn(totalAmountBilledToDate);
        Mockito.when(invoiceGeneralDetail.getAward()).thenReturn(award);
        
        contractsGrantsInvoiceDocument.setInvoiceGeneralDetail(invoiceGeneralDetail);
    }

}
