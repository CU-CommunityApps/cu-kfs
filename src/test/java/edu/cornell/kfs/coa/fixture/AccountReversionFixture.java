package edu.cornell.kfs.coa.fixture;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.context.IntegTestUtils;

import edu.cornell.kfs.coa.businessobject.AccountReversion;

public enum AccountReversionFixture {

	ACCOUNT_REVERSION_GOOD("G254700", 2014, "IT", "IT", "G104895", false, "IT", "G104895"),
	ACCOUNT_REVERSION_UPLOAD("1008000", "IT", "IT", "B023005", false, "IT", "B023005");
	
    public final String accountNumber;
    public final Integer universityFiscalYear;
    public final String chartOfAccountsCode;
    public final String budgetReversionChartOfAccountsCode;
    public final String budgetReversionAccountNumber;
    public final boolean carryForwardByObjectCodeIndicator;
    public final String cashReversionFinancialChartOfAccountsCode;
    public final String cashReversionAccountNumber;

    private  AccountReversionFixture(String accountNumber, String chartOfAccountsCode, 
            String budgetReversionChartOfAccountsCode, String budgetReversionAccountNumber, boolean carryForwardByObjectCodeIndicator,
            String cashReversionFinancialChartOfAccountsCode, String cashReversionAccountNumber) {
        this.accountNumber = accountNumber;
        this.universityFiscalYear = Integer.parseInt(IntegTestUtils.getParameterService().getParameterValueAsString("KFS-COA", "Reversion", "ACCOUNT_REVERSION_FISCAL_YEAR"));;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.budgetReversionChartOfAccountsCode = budgetReversionChartOfAccountsCode;
        this.budgetReversionAccountNumber = budgetReversionAccountNumber;
        this.carryForwardByObjectCodeIndicator = carryForwardByObjectCodeIndicator;
        this.cashReversionFinancialChartOfAccountsCode = cashReversionFinancialChartOfAccountsCode;
        this.cashReversionAccountNumber = cashReversionAccountNumber;
          
    }
    
    private  AccountReversionFixture(String accountNumber, Integer universityFiscalYear, String chartOfAccountsCode, 
			String budgetReversionChartOfAccountsCode, String budgetReversionAccountNumber, boolean carryForwardByObjectCodeIndicator,
			String cashReversionFinancialChartOfAccountsCode, String cashReversionAccountNumber) {
        this.accountNumber = accountNumber;
        this.universityFiscalYear = universityFiscalYear;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.budgetReversionChartOfAccountsCode = budgetReversionChartOfAccountsCode;
        this.budgetReversionAccountNumber = budgetReversionAccountNumber;
        this.carryForwardByObjectCodeIndicator = carryForwardByObjectCodeIndicator;
        this.cashReversionFinancialChartOfAccountsCode = cashReversionFinancialChartOfAccountsCode;
        this.cashReversionAccountNumber = cashReversionAccountNumber;
	}
	
   public AccountReversion createAccountReversion() {
	   AccountReversion accountReversion = new AccountReversion();
	   accountReversion.setAccountNumber(this.accountNumber);
	   accountReversion.setUniversityFiscalYear(this.universityFiscalYear);
	   accountReversion.setChartOfAccountsCode(this.chartOfAccountsCode);
	   accountReversion.setBudgetReversionChartOfAccountsCode(this.budgetReversionChartOfAccountsCode);
	   accountReversion.setBudgetReversionAccountNumber(this.budgetReversionAccountNumber);
	   accountReversion.setCarryForwardByObjectCodeIndicator(this.carryForwardByObjectCodeIndicator);
	   accountReversion.setCashReversionFinancialChartOfAccountsCode(this.cashReversionFinancialChartOfAccountsCode);
	   accountReversion.setCashReversionAccountNumber(this.cashReversionAccountNumber);
       return accountReversion;
    }
 
    public AccountReversion createAccountReversion(BusinessObjectService businessObjectService) {
        return (AccountReversion) businessObjectService.retrieve(this.createAccountReversion());
    }
}
