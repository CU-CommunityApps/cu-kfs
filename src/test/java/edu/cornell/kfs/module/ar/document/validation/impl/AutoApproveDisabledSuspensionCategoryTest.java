package edu.cornell.kfs.module.ar.document.validation.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.krad.document.DocumentBase;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContractsGrantsInvoiceDocument.class})
public class AutoApproveDisabledSuspensionCategoryTest {
    
    private AutoApproveDisabledSuspensionCategory autoApproveDisabledSuspensionCategory;
    private ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument;

    @Before
    public void setUp() throws Exception {
        Logger.getLogger(AutoApproveDisabledSuspensionCategory.class.getName()).setLevel(Level.DEBUG);
        autoApproveDisabledSuspensionCategory = new AutoApproveDisabledSuspensionCategory();
    }

    @After
    public void tearDown() throws Exception {
        autoApproveDisabledSuspensionCategory = null;
    }

    @Test
    public void testShouldSuspendAutoApproveOff() {
        prepareContractsGrantsInvoiceDocument(false, "Auto approve turned off for testing purposees.");
        assertTrue(autoApproveDisabledSuspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testShouldSuspendAutoApproveOffNullAutoApproveReason() {
        prepareContractsGrantsInvoiceDocument(false, null);
        assertTrue(autoApproveDisabledSuspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testShouldSuspendAutoApproveOffEmptyAutoApproveReason() {
        prepareContractsGrantsInvoiceDocument(false, StringUtils.EMPTY);
        assertTrue(autoApproveDisabledSuspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    @Test
    public void testShouldSuspendAutoApproveOn() {
        prepareContractsGrantsInvoiceDocument(true, StringUtils.EMPTY);
        assertFalse(autoApproveDisabledSuspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument));
    }
    
    private void prepareContractsGrantsInvoiceDocument(boolean autoApproveIndicator, String autoApproveReason) {
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        contractsGrantsInvoiceDocument = PowerMockito.spy(new ContractsGrantsInvoiceDocument());
        
        Award award = new Award();
        award.setAutoApproveIndicator(autoApproveIndicator);

        AwardExtendedAttribute attribute = new AwardExtendedAttribute();
        attribute.setAutoApproveReason(autoApproveReason);
        
        award.setExtension(attribute);
        
        InvoiceGeneralDetail invoiceGeneralDetail = Mockito.mock(InvoiceGeneralDetail.class);
        Mockito.when(invoiceGeneralDetail.getAward()).thenReturn(award);
        
        contractsGrantsInvoiceDocument.setInvoiceGeneralDetail(invoiceGeneralDetail);
    }

}
