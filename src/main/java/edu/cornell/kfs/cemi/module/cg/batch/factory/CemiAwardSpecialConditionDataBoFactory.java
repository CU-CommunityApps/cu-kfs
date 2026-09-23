package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyAccountSubAccountDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardSpecialConditionDataBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiAwardSpecialConditionDataBoFactory {
    
    private Award award;
    private CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes;
    private boolean useSubAccount;
    private int awardLineDataRowId;
    private int awardLineDataLineNumber;
    private int specialConditionDataRowId;
    private String jobRunDateString;
    private boolean maskSensitiveData;
    
    public static CemiAwardSpecialConditionDataBo createEmptyCemiAwardSpecialConditionDataBo() {
        CemiAwardSpecialConditionDataBo emptyBo = new CemiAwardSpecialConditionDataBo();
        emptyBo.setProposalNumberUsedForDataRow(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setChartOfAccountsCode(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAccountNumber(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setAwardLineDataRowId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionDataRowId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionDataDelete(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpecialCondition(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionReferenceId(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionType(CemiBaseConstants.EMPTY_STRING);
        emptyBo.setSpecialConditionComment(CemiBaseConstants.EMPTY_STRING);
        return emptyBo;
    }
    
    public static CemiAwardSpecialConditionDataBo createCemiAwardSpecialConditionDataBoFrom(final Award award,
            final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes, boolean useSubAccount,
            final int awardLineDataRowId, final int awardLineDataLineNumber, final int specialConditionDataRowId,
            final String jobRunDateString, final boolean maskSensitiveData) {
        final CemiAwardSpecialConditionDataBoFactory factory = new CemiAwardSpecialConditionDataBoFactory(award,
                accountSubAccountAttributes, useSubAccount, awardLineDataRowId, awardLineDataLineNumber,
                specialConditionDataRowId, jobRunDateString, maskSensitiveData);
        return factory.createCemiAwardSpecialConditionDataBo();
    }
    
    public CemiAwardSpecialConditionDataBoFactory(final Award award, 
            final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes, final boolean useSubAccount,
            final int awardLineDataRowId, final int awardLineDataLineNumber, final int specialConditionDataRowId,
            final String jobRunDateString, final boolean maskSensitiveData) {
        Validate.notNull(award, "award cannot be null for CemiAwardSpecialConditionDataBoFactory");
        Validate.notNull(accountSubAccountAttributes, "accountSubAccountAttributes cannot be null for CemiAwardSpecialConditionDataBoFactory");
        Validate.notNull(awardLineDataRowId, "awardLineDataRowId cannot be null for CemiAwardSpecialConditionDataBoFactory");
        Validate.notNull(specialConditionDataRowId, "specialConditionDataRowId cannot be null for CemiAwardSpecialConditionDataBoFactory");
        Validate.notNull(jobRunDateString, "jobRunDateString cannot be null for CemiAwardSpecialConditionDataBoFactory");
        this.award = award;
        this.accountSubAccountAttributes = accountSubAccountAttributes;
        this.useSubAccount = useSubAccount;
        this.awardLineDataRowId = awardLineDataRowId;
        this.awardLineDataLineNumber = awardLineDataLineNumber;
        this.specialConditionDataRowId = specialConditionDataRowId;
        this.jobRunDateString = jobRunDateString;
        this.maskSensitiveData = maskSensitiveData;
    }
    
    private CemiAwardSpecialConditionDataBo createCemiAwardSpecialConditionDataBo() {
        CemiAwardSpecialConditionDataBo awardSpecialConditionDataBo = new CemiAwardSpecialConditionDataBo();
        
        final String proposalNumberString = setToEmptyStringWhenValueIsBlank(award.getProposalNumber());
        final String chartOfAccountsCodeString = determineChartOfAccountsCode(accountSubAccountAttributes, useSubAccount);
        final String accountNumberString = this.determineAccountNumber(accountSubAccountAttributes, useSubAccount);
        final String awardLineDataRowIdString = convertIntToString(awardLineDataRowId);
        final String awardLineDataLineNumberString = convertIntToString(awardLineDataLineNumber);
        final String specialConditionDataRowIdString = convertIntToString(specialConditionDataRowId);
        
        awardSpecialConditionDataBo.setProposalNumberUsedForDataRow(proposalNumberString);
        awardSpecialConditionDataBo.setChartOfAccountsCode(chartOfAccountsCodeString);
        awardSpecialConditionDataBo.setAccountNumber(accountNumberString);
        awardSpecialConditionDataBo.setAwardLineDataRowId(awardLineDataRowIdString);
        awardSpecialConditionDataBo.setAwardLineDataLineNumber(awardLineDataLineNumberString);

        //TODO FIXME when TDB data mappings are complete
        //awardSpecialConditionDataBo.setSpecialConditionDataRowId(specialConditionDataRowIdString); // template field name duplicated as rowId
        awardSpecialConditionDataBo.setSpecialConditionDataRowId(CemiBaseConstants.EMPTY_STRING);
        awardSpecialConditionDataBo.setSpecialConditionDataDelete(CemiBaseConstants.EMPTY_STRING);
        awardSpecialConditionDataBo.setSpecialCondition(CemiBaseConstants.EMPTY_STRING);
        awardSpecialConditionDataBo.setSpecialConditionReferenceId(CemiBaseConstants.EMPTY_STRING);
        awardSpecialConditionDataBo.setSpecialConditionType(CemiBaseConstants.EMPTY_STRING);
        awardSpecialConditionDataBo.setSpecialConditionComment(CemiBaseConstants.EMPTY_STRING);
        
        return awardSpecialConditionDataBo;
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
