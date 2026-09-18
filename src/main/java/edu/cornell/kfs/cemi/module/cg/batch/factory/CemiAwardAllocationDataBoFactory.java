package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardAllocationDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyAccountSubAccountDataBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiAwardAllocationDataBoFactory {
    
    private Award award;
    private CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes;
    private boolean useSubAccount;
    private int awardLineDataRowId;
    private int awardLineDataLineNumber;
    private int specialConditionDataRowId;
    private int budgetDataRowId;
    private String jobRunDateString;  
    private boolean maskSensitiveData;
    
    public static CemiAwardAllocationDataBo createEmptyCemiAwardAllocationDataBo() {
        CemiAwardAllocationDataBo emptyBo = new CemiAwardAllocationDataBo();
        emptyBo.setProposalNumberUsedForDataRow(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setChartOfAccountsCode(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAccountNumber(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAwardLineDataRowId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAwardLineDataLineNumber(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionDataRowId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setBudgetDataRowId(CemiBaseConstants.EMPTY_STRING);
        
        emptyBo.setNsfCodeAllocationDataRowId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setNsfCodeAllocationDataDelete(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setNsfCodeAllocation(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setNsfCodeAllocationId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setNsfCodeAllocationPercentage(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setNsfCode(CemiBaseConstants.EMPTY_STRING);
        return emptyBo;
    }
    
    public static CemiAwardAllocationDataBo createCemiAwardAllocationDataBoFrom(final Award award,
            final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes, boolean useSubAccount,
            final int awardLineDataRowId, final int awardLineDataLineNumber, final int specialConditionDataRowId,
            final int budgetDataRowId, final String jobRunDateString, final boolean maskSensitiveData) {
        final CemiAwardAllocationDataBoFactory factory = new CemiAwardAllocationDataBoFactory(award,
                accountSubAccountAttributes, useSubAccount, awardLineDataRowId, awardLineDataLineNumber,
                specialConditionDataRowId, budgetDataRowId, jobRunDateString, maskSensitiveData);
        return factory.createCemiAwardAllocationDataBo();
    }
    
    public CemiAwardAllocationDataBoFactory(final Award award, final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes,
            final boolean useSubAccount, final int awardLineDataRowId, final int awardLineDataLineNumber,
            final int specialConditionDataRowId, final int budgetDataRowId, final String jobRunDateString, final boolean maskSensitiveData) {
        Validate.notNull(award, "award cannot be null for CemiAwardAllocationDataBoFactory");
        Validate.notNull(accountSubAccountAttributes, "accountSubAccountAttributes cannot be null for CemiAwardAllocationDataBoFactory");
        Validate.notNull(awardLineDataRowId, "awardLineDataRowId cannot be null for CemiAwardAllocationDataBoFactory");
        Validate.notNull(specialConditionDataRowId, "specialConditionDataRowId cannot be null for CemiAwardAllocationDataBoFactory");
        Validate.notNull(budgetDataRowId, "budgetDataRowId cannot be null for CemiAwardAllocationDataBoFactory");
        Validate.notNull(jobRunDateString, "jobRunDateString cannot be null for CemiAwardAllocationDataBoFactory");
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
     
    private CemiAwardAllocationDataBo createCemiAwardAllocationDataBo() {
        CemiAwardAllocationDataBo awardAllocationDataBo = new CemiAwardAllocationDataBo();
        
        final String proposalNumberString = setToEmptyStringWhenValueIsBlank(award.getProposalNumber());
        final String chartOfAccountsCode = determineCharOfAccountsCode(accountSubAccountAttributes, useSubAccount);
        final String accountNumber = this.determineAccountNumber(accountSubAccountAttributes, useSubAccount);
        final String awardLineDataRowIdString = convertIntToString(awardLineDataRowId);
        final String awardLineDataLineNumberString = convertIntToString(awardLineDataLineNumber);
        final String specialConditionDataRowIdString = convertIntToString(specialConditionDataRowId);
        final String budgetDataRowIdString = convertIntToString(budgetDataRowId);
                
        awardAllocationDataBo.setProposalNumberUsedForDataRow(proposalNumberString);
        awardAllocationDataBo.setChartOfAccountsCode(chartOfAccountsCode);
        awardAllocationDataBo.setAccountNumber(accountNumber);
        awardAllocationDataBo.setAwardLineDataRowId(awardLineDataRowIdString);
        awardAllocationDataBo.setAwardLineDataLineNumber(awardLineDataLineNumberString);
        awardAllocationDataBo.setSpecialConditionDataRowId(specialConditionDataRowIdString);
        awardAllocationDataBo.setBudgetDataRowId(budgetDataRowIdString);
        
        //TODO FIXME when TDB data mappings are complete
        awardAllocationDataBo.setNsfCodeAllocationDataRowId(CemiBaseConstants.EMPTY_STRING);
        awardAllocationDataBo.setNsfCodeAllocationDataDelete(CemiBaseConstants.EMPTY_STRING);
        awardAllocationDataBo.setNsfCodeAllocation(CemiBaseConstants.EMPTY_STRING);
        awardAllocationDataBo.setNsfCodeAllocationId(CemiBaseConstants.EMPTY_STRING);
        awardAllocationDataBo.setNsfCodeAllocationPercentage(CemiBaseConstants.EMPTY_STRING);
        awardAllocationDataBo.setNsfCode(CemiBaseConstants.EMPTY_STRING);
        return awardAllocationDataBo;
    }
    
    private String determineCharOfAccountsCode(CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes,
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
        return StringUtils.isNotBlank(value) ? value : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String convertIntToString(int value) {
        return String.valueOf(value);
    }
    
}
