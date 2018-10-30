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
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.ar.CuArConstants;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.module.cg.businessobject.CuAward;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CuAward.class, ContractsGrantsInvoiceDocument.class})
public class CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategoryTest {
    
    private CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategory suspensionCategory;
    private ParameterService parameterService;
    private ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument;

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
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(25));
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByBudgetAmountEqualBudget() {
        configureParameterServiceForSuspensionCheck(Boolean.TRUE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(50));
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByBudgetAmountMoreThanBudget() {
        configureParameterServiceForSuspensionCheck(Boolean.TRUE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(55));
        assertTrue(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByAwardTotalAmountLessThanBudget() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(25));
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByAwardTotalAmountLessThanAwardTotal() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(55));
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByAwardTotalAmountEqualAwardTotal() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(100));
        assertFalse(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testSuspensionByAwardTotalAmountMoreThanAwardTotal() {
        configureParameterServiceForSuspensionCheck(Boolean.FALSE);
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(105));
        assertTrue(suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    
    private void configureParameterServiceForSuspensionCheck(Boolean value) {
        Mockito.when(parameterService.getParameterValueAsBoolean(ArConstants.AR_NAMESPACE_CODE, 
                ArConstants.CONTRACTS_GRANTS_INVOICE_COMPONENT, CuArConstants.CG_INVOICE_AMT_BILLED_SUSPENSION_BY_BUDGET_TOTAL, Boolean.TRUE.booleanValue())).thenReturn(value);
        suspensionCategory.setParameterService(parameterService);
    }
    
    private void prepareContractsGrantsInvoiceDocument(KualiDecimal totalAmountBilledToDate) {
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        contractsGrantsInvoiceDocument = PowerMockito.spy(new ContractsGrantsInvoiceDocument());
        
        CuAward award = new CuAward();
        award.setAwardIndirectCostAmount(new KualiDecimal(50));
        award.setAwardDirectCostAmount(new KualiDecimal(50));
        
        AwardExtendedAttribute attribute = new AwardExtendedAttribute();
        attribute.setBudgetTotalAmount(new KualiDecimal(50));
        
        award.setExtension(attribute);
        
        InvoiceGeneralDetail invoiceGeneralDetail = Mockito.mock(InvoiceGeneralDetail.class);
        Mockito.when(invoiceGeneralDetail.getTotalAmountBilledToDate()).thenReturn(totalAmountBilledToDate);
        Mockito.when(invoiceGeneralDetail.getAward()).thenReturn(award);
        
        contractsGrantsInvoiceDocument.setInvoiceGeneralDetail(invoiceGeneralDetail);
    }

}
