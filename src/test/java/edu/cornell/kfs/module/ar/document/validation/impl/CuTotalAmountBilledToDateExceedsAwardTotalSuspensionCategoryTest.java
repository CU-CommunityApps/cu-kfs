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
        configureParameterServiceForSuspensionCheck(new Boolean(true));
        assertTrue(suspensionCategory.shouldValidateOnBudgetTotal());
    }
    
    @Test
    public void shouldValidateOnBudgetTotalForNullParam() {
        configureParameterServiceForSuspensionCheck(null);
        assertTrue(suspensionCategory.shouldValidateOnBudgetTotal());
    }
    
    @Test
    public void shouldValidateOnBudgetTotalForFalseParam() {
        configureParameterServiceForSuspensionCheck(new Boolean(false));
        assertFalse(suspensionCategory.shouldValidateOnBudgetTotal());
    }
    
    @Test
    public void testNoSespenseBudgetTotal() {
        configureParameterServiceForSuspensionCheck(new Boolean(true));
        
    }
    
    
    private void configureParameterServiceForSuspensionCheck(Boolean value) {
        Mockito.when(parameterService.getParameterValueAsBoolean(ArConstants.AR_NAMESPACE_CODE, 
                ArConstants.CONTRACTS_GRANTS_INVOICE_COMPONENT, CuArConstants.CG_INVOICE_AMT_BILLED_SUSPENION_BY_BUDGET_TOTAL)).thenReturn(value);
        suspensionCategory.setParameterService(parameterService);
    }
    
    private void prepareContractsGrantsInvoiceDocument(KualiDecimal awardTotal, KualiDecimal budgetTotal) {
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        contractsGrantsInvoiceDocument = PowerMockito.spy(new ContractsGrantsInvoiceDocument());
        
        InvoiceGeneralDetail invoiceGeneralDetail = new InvoiceGeneralDetail();
        
        CuAward award = PowerMockito.spy(new CuAward());
        award.setAwardTotalAmount(awardTotal);
        
        AwardExtendedAttribute attribute = new AwardExtendedAttribute();
        attribute.setBudgetTotalAmount(budgetTotal);
        
        award.setExtension(attribute);
        
        invoiceGeneralDetail.setAward(award);
        
        contractsGrantsInvoiceDocument.setInvoiceGeneralDetail(invoiceGeneralDetail);
    }

}
