package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.mockito.Mockito;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.ProcurementCardSkippedTransaction;

public class ProcurementCardSkippedTransactionEmailServiceImplTest {
    
    private ProcurementCardSkippedTransactionEmailServiceImpl procurementCardSkippedTransactionEmailServiceImpl;

    @Before
    public void setUp() throws Exception {
        procurementCardSkippedTransactionEmailServiceImpl = new ProcurementCardSkippedTransactionEmailServiceImpl();
        
        ParameterService parameterService = Mockito.mock(ParameterService.class);
        
        String emailMessageTemplate = "The {0} bank file had the following transactions skipped:";
        Mockito.when(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                KFSConstants.ProcurementCardParameters.PCARD_BATCH_LOAD_STEP, CuFPParameterConstants.ProcurementCardDocument.CARD_TRANSACTIONS_SKIPPED_EMAIL_BODY_TEMPLATE)).
        thenReturn(emailMessageTemplate);
        procurementCardSkippedTransactionEmailServiceImpl.setParameterService(parameterService);
        
        procurementCardSkippedTransactionEmailServiceImpl.setBankFileType("CBCP");
    }

    @After
    public void tearDown() throws Exception {
        procurementCardSkippedTransactionEmailServiceImpl = null;
    }

    @Test
    public void testBuildEmailMessageNoSkipped() {
        List<ProcurementCardSkippedTransaction> skippedTransactions = new ArrayList<ProcurementCardSkippedTransaction>();
        String actualMessage = procurementCardSkippedTransactionEmailServiceImpl.buildEmailMessage(skippedTransactions);
        
        String expectedMessage = "The CBCP bank file had the following transactions skipped:\n\n";
        
        assertEquals(expectedMessage, actualMessage);
    }
    
    @Test
    public void testBuildEmailMessageOneSkipped() {
        List<ProcurementCardSkippedTransaction> skippedTransactions = new ArrayList<ProcurementCardSkippedTransaction>();
        
        ProcurementCardSkippedTransaction skipped = new ProcurementCardSkippedTransaction();
        skipped.setCardHolderName("Jane Doe");
        skipped.setFileLineNumber(101);
        skipped.setTransactionAmount(new KualiDecimal(50.00));
        skippedTransactions.add(skipped);
        
        String actualMessage = procurementCardSkippedTransactionEmailServiceImpl.buildEmailMessage(skippedTransactions);
        
        String expectedMessage = "The CBCP bank file had the following transactions skipped:\n\nFile Line Number: 101 Card Holder Name: Jane Doe Transaction Amount: 50.00\n";
        
        assertEquals(expectedMessage, actualMessage);
    }

}
