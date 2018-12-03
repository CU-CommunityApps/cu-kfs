package edu.cornell.kfs.module.ar.document.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.krad.document.DocumentBase;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.fixture.ARAwardMockFixture;
import org.kuali.kfs.module.ar.fixture.ContractsGrantsInvoiceDetailFixture;
import org.kuali.kfs.module.ar.fixture.ContractsGrantsInvoiceDocumentFixture;
import org.kuali.kfs.module.ar.fixture.InvoiceAccountDetailFixture;
import org.kuali.kfs.module.ar.fixture.InvoiceGeneralDetailFixture;
import org.kuali.kfs.module.ar.service.impl.ContractsGrantsInvoiceCreateDocumentServiceImpl;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContractsGrantsInvoiceDocument.class})
public class CuContractsGrantsInvoiceDocumentServiceImpTest {
    
    private CuContractsGrantsInvoiceDocumentServiceImpl cuContractsGrantsInvoiceDocumentServiceImpl;
    private ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument;

    @Before
    public void setUp() throws Exception {
        cuContractsGrantsInvoiceDocumentServiceImpl = new CuContractsGrantsInvoiceDocumentServiceImpl();
        
    }

    @After
    public void tearDown() throws Exception {
        cuContractsGrantsInvoiceDocumentServiceImpl = null;
        contractsGrantsInvoiceDocument = null;
    }
    
    @Test
    public void testCinvProrate() {
        prepareContractsGrantsInvoiceDocument(new KualiDecimal(50), new KualiDecimal(150));
        cuContractsGrantsInvoiceDocumentServiceImpl.prorateBill(contractsGrantsInvoiceDocument);
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
        Mockito.when(invoiceGeneralDetail.getTotalPreviouslyBilled()).thenReturn(totalAmountBilledToDate);
        Mockito.when(invoiceGeneralDetail.getAward()).thenReturn(award);
        
        
        contractsGrantsInvoiceDocument.setInvoiceGeneralDetail(invoiceGeneralDetail);
    }

    
}
