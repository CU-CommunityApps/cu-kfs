package edu.cornell.kfs.coa.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobal;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.service.BusinessObjectService;

public class CuAccountGlobal extends AccountGlobal {
    
    private static final long serialVersionUID = 1L;

    protected String majorReportingCategoryCode;
    protected MajorReportingCategory majorReportingCategory;
    
    @Override
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {

        // the list of persist-ready BOs
        List<PersistableBusinessObject> persistables = new ArrayList<PersistableBusinessObject>();
    
        // walk over each change detail record
        for (AccountGlobalDetail detail : accountGlobalDetails) {
    
            // load the object by keys
            Account account = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Account.class, detail.getPrimaryKeys());
    
            // if we got a valid account, do the processing
            if (account != null) {
    
                // NOTE that the list of fields that are updated may be a subset of the total
                // number of fields in this class. This is because the class may contain a superset
                // of the fields actually used in the Global Maintenance Document.
    
                // FISCAL OFFICER
                if (StringUtils.isNotBlank(accountFiscalOfficerSystemIdentifier)) {
                    account.setAccountFiscalOfficerSystemIdentifier(accountFiscalOfficerSystemIdentifier);
                }
    
                // ACCOUNT SUPERVISOR
                if (StringUtils.isNotBlank(accountsSupervisorySystemsIdentifier)) {
                    account.setAccountsSupervisorySystemsIdentifier(accountsSupervisorySystemsIdentifier);
                }
    
                // ACCOUNT MANAGER
                if (StringUtils.isNotBlank(accountManagerSystemIdentifier)) {
                    account.setAccountManagerSystemIdentifier(accountManagerSystemIdentifier);
                }
    
                // ORGANIZATION CODE
                if (StringUtils.isNotBlank(organizationCode)) {
                    account.setOrganizationCode(organizationCode);
                }
    
                // SUB FUND GROUP CODE
                if (StringUtils.isNotBlank(subFundGroupCode)) {
                    account.setSubFundGroupCode(subFundGroupCode);
                }
    
                // CITY NAME
                if (StringUtils.isNotBlank(accountCityName)) {
                    account.setAccountCityName(accountCityName);
                }
    
                // STATE CODE
                if (StringUtils.isNotBlank(accountStateCode)) {
                    account.setAccountStateCode(accountStateCode);
                }
    
                // STREET ADDRESS
                if (StringUtils.isNotBlank(accountStreetAddress)) {
                    account.setAccountStreetAddress(accountStreetAddress);
                }
    
                // ZIP CODE
                if (StringUtils.isNotBlank(accountZipCode)) {
                    account.setAccountZipCode(accountZipCode);
                }
    
                // EXPIRATION DATE
                if (accountExpirationDate != null) {
                    account.setAccountExpirationDate(new Date(accountExpirationDate.getTime()));
                }
    
                // CONTINUATION CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(continuationFinChrtOfAcctCd)) {
                    account.setContinuationFinChrtOfAcctCd(continuationFinChrtOfAcctCd);
                }
    
                // CONTINUATION ACCOUNT NUMBER
                if (StringUtils.isNotBlank(continuationAccountNumber)) {
                    account.setContinuationAccountNumber(continuationAccountNumber);
                }
    
                // INCOME STREAM CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(incomeStreamFinancialCoaCode)) {
                    account.setIncomeStreamFinancialCoaCode(incomeStreamFinancialCoaCode);
                }
    
                // INCOME STREAM ACCOUNT NUMBER
                if (StringUtils.isNotBlank(incomeStreamAccountNumber)) {
                    account.setIncomeStreamAccountNumber(incomeStreamAccountNumber);
                }
    
                // CG CATL FED DOMESTIC ASSIST NBR
                if (StringUtils.isNotBlank(accountCfdaNumber)) {
                    account.setAccountCfdaNumber(accountCfdaNumber);
                }
    
                // FINANCIAL HIGHER ED FUNCTION CODE
                if (StringUtils.isNotBlank(financialHigherEdFunctionCd)) {
                    account.setFinancialHigherEdFunctionCd(financialHigherEdFunctionCd);
                }
    
                // SUFFICIENT FUNDS CODE
                if (StringUtils.isNotBlank(accountSufficientFundsCode)) {
                    account.setAccountSufficientFundsCode(accountSufficientFundsCode);
                }
    
                // LABOR BENEFIT RATE CATEGORY CODE
                if (StringUtils.isNotBlank(getLaborBenefitRateCategoryCode())) {
                    account.setLaborBenefitRateCategoryCode(getLaborBenefitRateCategoryCode());
                }
    
                // PENDING ACCOUNT SUFFICIENT FUNDS CODE INDICATOR
                if (pendingAcctSufficientFundsIndicator != null) {
                    account.setPendingAcctSufficientFundsIndicator(pendingAcctSufficientFundsIndicator);
                }
    
                // MAJOR REPORTING CATEGORY CODE 
                if (StringUtils.isNotBlank(majorReportingCategoryCode)) {
                    ((AccountExtendedAttribute) account.getExtension()).setMajorReportingCategoryCode(majorReportingCategoryCode);
                }
                
                
                persistables.add(account);
    
            }
        }

        return persistables;
    }
    
    public String getMajorReportingCategoryCode() {
        return majorReportingCategoryCode;
    }
    public void setMajorReportingCategoryCode(String majorReportingCategoryCode) {
        this.majorReportingCategoryCode = majorReportingCategoryCode;
    }
    public MajorReportingCategory getMajorReportingCategory() {
        return majorReportingCategory;
    }
    public void setMajorReportingCategory(
            MajorReportingCategory majorReportingCategory) {
        this.majorReportingCategory = majorReportingCategory;
    }


}
