package edu.cornell.kfs.sys.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.sys.service.CUBankService;

@ConfigureContext(session = ccs1)
public class CuBankServiceImplIntegTest extends KualiIntegTestBase {
    private CUBankService cUBankService;
    private ParameterService parameterService;
    private String bankCodesMap;
    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        cUBankService = SpringContext.getBean(CUBankService.class);
    }

    /*
     * test doc type is null or empty
     * 
     */
    public void testGetDefaultBankByDocType_blankDocType() {
        try {
            cUBankService.getDefaultBankByDocType(null);
            assertTrue("should get RuntimeException with blank doc type code", false);            
        } catch (RuntimeException re) {
            assertTrue("should get RuntimeException with blank doc type code", true);            
        }

        try {
            cUBankService.getDefaultBankByDocType("");
            assertTrue("should get RuntimeException with blank doc type code", false);            
        } catch (RuntimeException re) {
            assertTrue("should get RuntimeException with blank doc type code", true);            
        }
   }
    
    /*
     * test document type that has bank code set up in parameter
     */
    public void testGetDefaultBankByDocType_valid() {
        parameterService = SpringContext.getBean(ParameterService.class);
        bankCodesMap = parameterService.getParameterValueAsString(Bank.class, KFSParameterKeyConstants.DEFAULT_BANK_BY_DOCUMENT_TYPE);
        if (StringUtils.isNotBlank(bankCodesMap)) {
            for (String docTypeBankCode : bankCodesMap.split(";")) {
                String[] docTypeBankCodePair= docTypeBankCode.split("=");
                Bank bank = cUBankService.getDefaultBankByDocType(docTypeBankCodePair[0]);
                assertTrue("should get valid bank code for " + docTypeBankCodePair[0], StringUtils.equals(docTypeBankCodePair[1], bank.getBankCode()));   
               
            }

        }
   }

    /*
     * test doc type don't have bank code
     */
    public void testGetDefaultBankByDocType_invalid() {
        Bank bank = cUBankService.getDefaultBankByDocType("PVEN");
        assertTrue("should not get bank code for PVEN", bank == null);   
        bank = cUBankService.getDefaultBankByDocType("XYZ");
        assertTrue("should not get bank code for XYZ", bank == null);   
   }



}
