package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyAccountSubAccountDataBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiAwardBudgetDataBoFactory {
    
    private Award award;
    private CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes;
    private boolean useSubAccount;
    private int awardLineDataRowId;
    private int awardLineDataLineNumber;
    private int specialConditionDataRowId;
    private int budgetDataRowId;
    private String jobRunDateString;
    private boolean maskSensitiveData;
    
    public static CemiAwardBudgetDataBo createEmptyCemiAwardBudgetDataBo() {
        CemiAwardBudgetDataBo emptyBo = new CemiAwardBudgetDataBo();
        emptyBo.setProposalNumberUsedForDataRow(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setChartOfAccountsCode(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAccountNumber(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAwardLineDataRowId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionDataRowId(CemiBaseConstants.EMPTY_STRING);
        
        emptyBo.setAwardBudgetDataRowId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setDefaultBudgetStructure(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setDefaultBudgetType(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setDefaultBalancedAmendment(CemiBaseConstants.EMPTY_STRING);
        return emptyBo;
    }
    
    public static CemiAwardBudgetDataBo createCemiAwardBudgetDataBoFrom(final Award award,
            final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes, boolean useSubAccount,
            final int awardLineDataRowId, final int awardLineDataLineNumber, final int specialConditionDataRowId,
            final int budgetDataRowId, final String jobRunDateString, final boolean maskSensitiveData) {
        final CemiAwardBudgetDataBoFactory factory = new CemiAwardBudgetDataBoFactory(award,
                accountSubAccountAttributes, useSubAccount, awardLineDataRowId, awardLineDataLineNumber,
                specialConditionDataRowId, budgetDataRowId, jobRunDateString, maskSensitiveData);
        return factory.createCemiAwardBudgetDataBo();
    }
    
    public CemiAwardBudgetDataBoFactory(final Award award, final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes,
            final boolean useSubAccount, final int awardLineDataRowId, final int awardLineDataLineNumber,
            final int specialConditionDataRowId, final int budgetDataRowId, final String jobRunDateString, final boolean maskSensitiveData) {
        Validate.notNull(award, "award cannot be null for CemiAwardBudgetDataBoFactory");
        Validate.notNull(accountSubAccountAttributes, "accountSubAccountAttributes cannot be null for CemiAwardBudgetDataBoFactory");
        Validate.notBlank(jobRunDateString, "jobRunDateString cannot be blank for CemiAwardBudgetDataBoFactory");
        this.award = award;
        this.accountSubAccountAttributes = accountSubAccountAttributes;
        this.useSubAccount = useSubAccount;
        this.awardLineDataRowId = awardLineDataRowId;
        this.awardLineDataLineNumber = awardLineDataLineNumber;
        this.specialConditionDataRowId = specialConditionDataRowId;
        this.budgetDataRowId = budgetDataRowId;
        this.jobRunDateString = jobRunDateString;
        this.maskSensitiveData = maskSensitiveData;
    }
    
    private CemiAwardBudgetDataBo createCemiAwardBudgetDataBo() {
        CemiAwardBudgetDataBo awardBudgetDataBo = new CemiAwardBudgetDataBo();
        
        final String proposalNumberString = setToEmptyStringWhenValueIsBlank(award.getProposalNumber());
        final String chartOfAccountsCodeString = determineChartOfAccountsCode(accountSubAccountAttributes, useSubAccount);
        final String accountNumberString = determineAccountNumber(accountSubAccountAttributes, useSubAccount);
        final String awardLineDataRowIdString = convertIntToString(awardLineDataRowId);
        final String awardLineDataLineNumberString = convertIntToString(awardLineDataLineNumber);
        final String specialConditionDataRowIdString = convertIntToString(specialConditionDataRowId);
        final String budgetDataRowIdString = convertIntToString(budgetDataRowId);
        
        awardBudgetDataBo.setProposalNumberUsedForDataRow(proposalNumberString);
        awardBudgetDataBo.setChartOfAccountsCode(chartOfAccountsCodeString);
        awardBudgetDataBo.setAccountNumber(accountNumberString);
        awardBudgetDataBo.setAwardLineDataRowId(awardLineDataRowIdString);
        awardBudgetDataBo.setAwardLineDataLineNumber(awardLineDataLineNumberString);
        awardBudgetDataBo.setSpecialConditionDataRowId(specialConditionDataRowIdString);
        
        awardBudgetDataBo.setAwardBudgetDataRowId(budgetDataRowIdString);
        awardBudgetDataBo.setDefaultBudgetStructure(CemiAwardConstants.AWARD);
        awardBudgetDataBo.setDefaultBudgetType(CemiAwardConstants.AWARD);
        awardBudgetDataBo.setDefaultBalancedAmendment(CemiAwardConstants.NO);
        return awardBudgetDataBo;
    }
 
    private String determineChartOfAccountsCode(CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes,
            boolean useSubAccount) {
        if (useSubAccount) {
            return setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getChartOfAccountsCode());
        }
        return setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getChartOfAccountsCode());
    }

    private String determineAccountNumber(CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes,
            boolean useSubAccount) {
        if (useSubAccount) {
            return setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getSubAccountNumber());
        }
        return setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getAccountNumber());
    }

    private String setToEmptyStringWhenValueIsBlank(String value) {
        return StringUtils.defaultIfBlank(value, CemiBaseConstants.EMPTY_STRING);
    }

    private String convertIntToString(int value) {
        return String.valueOf(value);
    }
    
}

