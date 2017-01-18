package edu.cornell.kfs.concur.services;

import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.SubObjectCodeService;

public class MockSubObjectCodeService implements SubObjectCodeService {

    @Override
    public SubObjectCode getByPrimaryId(Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber, String financialObjectCode, String financialSubObjectCode) {
        return null;
    }

    @Override
    public SubObjectCode getByPrimaryIdForCurrentYear(String chartOfAccountsCode, String accountNumber, String financialObjectCode, String financialSubObjectCode) {
        SubObjectCode subObjectCode = null;
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.VALID_ACCT_NBR.equalsIgnoreCase(accountNumber) && ConcurAccountValidationTestConstants.VALID_OBJ_CD.equalsIgnoreCase(financialObjectCode) && ConcurAccountValidationTestConstants.VALID_SUB_OBJECT.equalsIgnoreCase(financialSubObjectCode)) {
            subObjectCode = createSubObjectCode(chartOfAccountsCode, accountNumber, financialObjectCode, financialSubObjectCode);
            subObjectCode.setActive(true);
        }
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.VALID_ACCT_NBR.equalsIgnoreCase(accountNumber) && ConcurAccountValidationTestConstants.VALID_OBJ_CD.equalsIgnoreCase(financialObjectCode) && ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT.equalsIgnoreCase(financialSubObjectCode)) {
            subObjectCode = createSubObjectCode(chartOfAccountsCode, accountNumber, financialObjectCode, financialSubObjectCode);
            subObjectCode.setActive(false);
        }

        return subObjectCode;
    }
    
    private SubObjectCode createSubObjectCode(String chartOfAccountsCode, String accountNumber, String financialObjectCode, String financialSubObjectCode) {
        SubObjectCode subObjectCode = new SubObjectCode();
        subObjectCode.setChartOfAccountsCode(chartOfAccountsCode);
        subObjectCode.setAccountNumber(accountNumber);
        subObjectCode.setFinancialObjectCode(financialObjectCode);
        subObjectCode.setFinancialSubObjectCode(financialSubObjectCode);
        return subObjectCode;
    }

}
