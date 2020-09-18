package edu.cornell.kfs.concur.services;

import java.util.List;

import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;

public class MockObjectCodeService implements ObjectCodeService {

    @Override
    public ObjectCode getByPrimaryId(Integer universityFiscalYear, String chartOfAccountsCode, String financialObjectCode) {
        return null;
    }

    @Override
    public ObjectCode getByPrimaryIdWithCaching(Integer universityFiscalYear, String chartOfAccountsCode, String financialObjectCode) {
        return null;
    }

    @Override
    public ObjectCode getByPrimaryIdForCurrentYear(String chartOfAccountsCode, String financialObjectCode) {
        ObjectCode objectCode = null;
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.VALID_OBJ_CD.equalsIgnoreCase(financialObjectCode)) {
            objectCode = createObjectCode(chartOfAccountsCode, financialObjectCode);
            objectCode.setActive(true);
        }
        
        if (ConcurAccountValidationTestConstants.VALID_CHART.equalsIgnoreCase(chartOfAccountsCode) && ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD.equalsIgnoreCase(financialObjectCode)) {
            objectCode = createObjectCode(chartOfAccountsCode, financialObjectCode);
            objectCode.setActive(false);
        }
        return objectCode;
    }
    
    private ObjectCode createObjectCode(String chartOfAccountsCode, String financialObjectCode) {
        ObjectCode objectCode = new ObjectCode();
        objectCode.setChartOfAccountsCode(chartOfAccountsCode);
        objectCode.setFinancialObjectCode(financialObjectCode);
        return objectCode;
    }

    @Override
    public List getYearList(String chartOfAccountsCode, String financialObjectCode) {
        return null;
    }

    @Override
    public ObjectCode getByPrimaryIdForLatestValidYear(String chartOfAccountsCode, String financialObjectCode) {
        return null;
    }

    @Override
    public String getObjectCodeNamesByCharts(Integer universityFiscalYear, String[] chartOfAccountCodes, String financialObjectCode) {
        return null;
    }

    @Override
    public boolean doesObjectConsolidationContainObjectCode(String chartOfAccountsCode, String consolidationCode, String objectChartOfAccountsCode, String objectCode) {
        return false;
    }

    @Override
    public boolean doesObjectLevelContainObjectCode(String chartOfAccountsCode, String levelCode, String objectChartOfAccountsCode, String objectCode) {
        return false;
    }

    @Override
    public List<ObjectCode> getObjectCodesByLevelIds(List<String> levelCodes) {
        return null;
    }

}
