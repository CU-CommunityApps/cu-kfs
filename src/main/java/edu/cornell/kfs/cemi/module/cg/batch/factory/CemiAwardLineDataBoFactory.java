package edu.cornell.kfs.cemi.module.cg.batch.factory;

import java.sql.Date;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyAccountSubAccountDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLineDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableMaps;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CemiAwardLineDataBoFactory {
    
    private Award award;
    private AwardExtendedAttribute awardExtendedAttribute;
    private CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes;
    private String awardOrgCode;
    private boolean useSubAccount;
    private int awardLineDataRowId;
    private int awardLineDataLineNumber;
    private String jobRunDateString;
    private DateTimeService dateTimeService;
    private CemiAwardTranslateTableMaps allAwardTranslateTableMaps;
    private boolean maskSensitiveData;
    
    public static CemiAwardLineDataBo createEmptyCemiAwardLineDataBo() {
        CemiAwardLineDataBo emptyCemiAwardLineDataBo = new CemiAwardLineDataBo();
        emptyCemiAwardLineDataBo.setProposalNumberUsedForDataRow(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setChartOfAccountsCode(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAccountNumber(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataRowId(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setReceivableContractLine(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setReceivableContractLineReferenceId(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineNumber(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setIntercompanyAffiliate(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setRevenueCategory(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataAwardLifecycleStatus(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineType(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setSpendRestriction(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineItemDescriptionOverride(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setDeferredRevenue(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineStatus(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDocumentStatus(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setPrimaryGrant(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineCfdaNumber(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setGrantId(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineAmount(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setRateAgreement(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setCostRateType(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setException(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setRevenueAllocationProfile(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setDelete(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setBasisLimit(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setBasisLimitId(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setBasisLimitName(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setBasisLimitAmount(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineStartDate(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineEndDate(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDescription(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineInvoiceMemoOverride(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataCostCenter(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataFund(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineDataFunction(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineSalaryCap(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setAwardLineSalaryCapOverride(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setSubrecipient(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineFederalAwardIdNumber(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setLineBillingNotes(CemiBaseConstants.EMPTY_STRING);
        emptyCemiAwardLineDataBo.setRevenueRecognitionLineNotes(CemiBaseConstants.EMPTY_STRING);
        return emptyCemiAwardLineDataBo;
    }
 
    public static CemiAwardLineDataBo createCemiAwardLineDataBoFrom(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute,
            final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes, final String awardOrgCode,
            final boolean useSubAccount, final int awardLineDataRowId, final int awardLineDataLineNumber,
            final String jobRunDateString, final DateTimeService dateTimeService,
            final CemiAwardTranslateTableMaps allAwardTranslateTableMaps, final boolean maskSensitiveData) {
        final CemiAwardLineDataBoFactory factory = new CemiAwardLineDataBoFactory(award, awardExtendedAttribute,
                accountSubAccountAttributes, awardOrgCode, useSubAccount, awardLineDataRowId, awardLineDataLineNumber,
                jobRunDateString, dateTimeService, allAwardTranslateTableMaps, maskSensitiveData);
        return factory.createCemiAwardLineDataBo();
    }
    
    public CemiAwardLineDataBoFactory(final Award award, final AwardExtendedAttribute awardExtendedAttribute,
            final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes, final String awardOrgCode,
            final boolean useSubAccount, final int awardLineDataRowId, final int awardLineDataLineNumber,
            final String jobRunDateString, final DateTimeService dateTimeService,
            final CemiAwardTranslateTableMaps allAwardTranslateTableMaps, final boolean maskSensitiveData) {
        Validate.notNull(award, "award cannot be null for CemiAwardLineDataBoFactory");
        Validate.notNull(awardExtendedAttribute, "awardExtendedAttribute cannot be null for CemiAwardLineDataBoFactory");
        Validate.notBlank(awardOrgCode, "awardOrgCode cannot be blank for CemiAwardLineDataBoFactory");
        Validate.notNull(accountSubAccountAttributes, "accountSubAccountAttributes cannot be null for CemiAwardLineDataBoFactory");
        Validate.notNull(allAwardTranslateTableMaps, "allAwardTranslateTableMaps cannot be null for CemiAwardLineDataBoFactory");
        Validate.notBlank(jobRunDateString, "jobRunDateString cannot be blank for CemiAwardLineDataBoFactory");
        Validate.notNull(dateTimeService, "dateTimeService cannot be null for CemiAwardLineDataBoFactory");
        this.award = award;
        this.awardExtendedAttribute = awardExtendedAttribute;
        this.accountSubAccountAttributes = accountSubAccountAttributes;
        this.awardOrgCode = awardOrgCode;
        this.useSubAccount = useSubAccount;
        this.awardLineDataRowId = awardLineDataRowId;
        this.awardLineDataLineNumber = awardLineDataLineNumber;
        this.jobRunDateString = jobRunDateString;
        this.dateTimeService = dateTimeService;
        this.allAwardTranslateTableMaps = allAwardTranslateTableMaps;
        this.maskSensitiveData = maskSensitiveData;
    }
    
    private CemiAwardLineDataBo createCemiAwardLineDataBo() {
        CemiAwardLineDataBo awardLineDataBo = new CemiAwardLineDataBo();
        
        final String proposalNumberString = setToEmptyStringWhenValueIsBlank(award.getProposalNumber());
        final String chartOfAccountsCodeString = determineChartOfAccountsCode(accountSubAccountAttributes, useSubAccount);
        final String accountNumberString = determineAccountNumber(accountSubAccountAttributes, useSubAccount);
        final String awardLineDataRowIdString = convertIntToString(awardLineDataRowId);
        
        // accountNumberString above cannot be used here. We are creating a unique key and need to have the account
        // associated to the sub-account when we are generating line for a sub-account.
        final String receivableContractLineReferenceIdString = buildReceivableContractLineReferenceId(proposalNumberString, accountSubAccountAttributes, useSubAccount);
        
        final String awardLineDataLineNumberString = convertIntToString(awardLineDataLineNumber);

        final String awardLineStatusString = determineLineStatus(accountSubAccountAttributes, useSubAccount);
        
        final String awardInstrumentTypeCodeString = setToEmptyStringWhenValueIsBlank(award.getInstrumentTypeCode());
        final String lineTypeString = determineTranslationValueForLineType(allAwardTranslateTableMaps.getAwardLineTypesMap(), awardInstrumentTypeCodeString);
        
        final String awardLineStartDateString = determineFormattedDate(awardExtendedAttribute.getBudgetBeginningDate());
        final String awardLineEndDateString = determineFormattedDate(awardExtendedAttribute.getBudgetEndingDate());
        
        final String awardLineCfdaNumberString = setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getAccountCgCfdaNumber());
        
//FIXME functioal want KFS awardOrgCode for now, use value as key for look up into FDM cost center translate table when it is provided
        final String awardLineLineDataCostCenterString = determineCostCenter(awardOrgCode);
        
//FIXME mapping prior to unit test defauls was: need 2 xlate tables, complicated logic for specific sub-fund value
        final String awardLineDataFundString = CemiAwardConstants.DEFAULT_FUND;
        
//FIXME Program renamed to Function for unit test build with no mapping
        final String awardLineDataFunctionString = CemiAwardConstants.DEFAULT_FUNCTION;

//FIXME needed logic is acct control acct then set Y/N constant
        final String awardLineDataPrimaryGrantString = CemiBaseConstants.EMPTY_STRING;
        
//FIXME very complicated GL amount summation
        final String awardLineDataAmountString = CemiBaseConstants.EMPTY_STRING;
        
//FIXME LATER, no translate table mapping for any of these attribute
        final String awardLineDataRateAgreementString = CemiBaseConstants.EMPTY_STRING;
        
        final String awardLineDataCostRateTypeString = CemiBaseConstants.EMPTY_STRING;
        final String awardLineDataRevenueAllocationProfileString = CemiBaseConstants.EMPTY_STRING;
        
//FIXME complicated logic for these related items once fully defined
        final String awardLineDataBasisLimitIdString = CemiBaseConstants.EMPTY_STRING;
        final String awardLineDataBasisLimitNameString = CemiBaseConstants.EMPTY_STRING;
        final String awardLineDataBasisLimitAmountString = CemiBaseConstants.EMPTY_STRING;
        
//FIXME needs to link to supplierID
        final String awardLineDataSubrecipientString = CemiBaseConstants.EMPTY_STRING;
        
//FIXME get need 4 logic values base on accounts/sub-account for the award line & translate tables passed in
        final String awardLineDataAwardLifecycleStatusString = CemiBaseConstants.EMPTY_STRING;;
        
        awardLineDataBo.setProposalNumberUsedForDataRow(proposalNumberString);
        awardLineDataBo.setChartOfAccountsCode(chartOfAccountsCodeString);
        awardLineDataBo.setAccountNumber(accountNumberString);
        awardLineDataBo.setAwardLineDataRowId(awardLineDataRowIdString);
        awardLineDataBo.setReceivableContractLine(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setReceivableContractLineReferenceId(receivableContractLineReferenceIdString);
        awardLineDataBo.setLineNumber(awardLineDataLineNumberString);
        awardLineDataBo.setIntercompanyAffiliate(CemiAwardConstants.COMPANY_CORNELL_UNIVERSITY_MAIN_CAMPUS);
        awardLineDataBo.setRevenueCategory(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setAwardLineDataAwardLifecycleStatus(awardLineDataAwardLifecycleStatusString);
        awardLineDataBo.setLineType(lineTypeString);
        awardLineDataBo.setSpendRestriction(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setLineItemDescriptionOverride(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setDeferredRevenue(CemiAwardConstants.NO);
        awardLineDataBo.setLineStatus(awardLineStatusString);
        awardLineDataBo.setAwardLineDocumentStatus(CemiAwardConstants.ACTIVE);
        awardLineDataBo.setPrimaryGrant(awardLineDataPrimaryGrantString);
        awardLineDataBo.setLineCfdaNumber(awardLineCfdaNumberString);
        awardLineDataBo.setGrantId(accountNumberString);
        awardLineDataBo.setLineAmount(awardLineDataAmountString);
        awardLineDataBo.setRateAgreement(awardLineDataRateAgreementString);
        awardLineDataBo.setCostRateType(awardLineDataCostRateTypeString);
        awardLineDataBo.setException(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setRevenueAllocationProfile(awardLineDataRevenueAllocationProfileString);
        awardLineDataBo.setDelete(CemiAwardConstants.NO);
        awardLineDataBo.setBasisLimit(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setBasisLimitId(awardLineDataBasisLimitIdString);
        awardLineDataBo.setBasisLimitName(awardLineDataBasisLimitNameString);
        awardLineDataBo.setBasisLimitAmount(awardLineDataBasisLimitAmountString);
        awardLineDataBo.setAwardLineStartDate(awardLineStartDateString);
        awardLineDataBo.setAwardLineEndDate(awardLineEndDateString);
        awardLineDataBo.setAwardLineDescription(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setLineInvoiceMemoOverride(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setAwardLineDataCostCenter(awardLineLineDataCostCenterString);
        awardLineDataBo.setAwardLineDataFund(awardLineDataFundString);
        awardLineDataBo.setAwardLineDataFunction(awardLineDataFunctionString);
        awardLineDataBo.setAwardLineSalaryCap(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setAwardLineSalaryCapOverride(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setSubrecipient(awardLineDataSubrecipientString);
        awardLineDataBo.setLineFederalAwardIdNumber(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setLineBillingNotes(CemiBaseConstants.EMPTY_STRING);
        awardLineDataBo.setRevenueRecognitionLineNotes(CemiBaseConstants.EMPTY_STRING);
        return awardLineDataBo;
    }
    
    private String convertIntToString(int value) {
        return String.valueOf(value);
    }
    
    private String setToEmptyStringWhenValueIsBlank(String value) {
        return StringUtils.defaultIfBlank(value, CemiBaseConstants.EMPTY_STRING);
    }
    
    private String determineChartOfAccountsCode(final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes,
            final boolean useSubAccount) {
        if (useSubAccount) {
            return setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getChartOfAccountsCode());
        }
        return setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getChartOfAccountsCode());
    }
    
    private String determineAccountNumber(final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes,
            final boolean useSubAccount) {
        if (useSubAccount) {
            return setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getSubAccountNumber());
        }
        return setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getAccountNumber());
    }
    
    private String buildReceivableContractLineReferenceId(final String awardProposalNumberString,
            final CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes, final boolean useSubAccount) {
        if (useSubAccount) {
            return MessageFormat.format(CemiAwardConstants.RECEIVABLE_CONTRACT_LINE_REFERENCE_ID_SUB_ACCOUNT_FORMAT,
                    awardProposalNumberString,
                    setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getAccountNumber()),
                    setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getSubAccountNumber()));
        }
        return MessageFormat.format(CemiAwardConstants.RECEIVABLE_CONTRACT_LINE_REFERENCE_ID_ACCOUNT_FORMAT,
                awardProposalNumberString,
                setToEmptyStringWhenValueIsBlank(accountSubAccountAttributes.getAccountNumber()));
    }
    
    private String determineTranslationValueForLineType(Map<String, String> translationMap, String codeToUseForLookup) {
        return translationMap.getOrDefault(codeToUseForLookup, CemiAwardConstants.KFS_FIX);
    }
    
    private String determineFormattedDate(Date dateToFormat) {
        return ObjectUtils.isNotNull(dateToFormat)
                ? dateTimeService.toString(dateToFormat, CemiBaseConstants.DATE_FORMAT_yyyy_MM_dd)
                        : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String determineLineStatus(CemiAwardLegacyAccountSubAccountDataBo accountSubAccountAttributes, boolean useSubAccount) {
        //Coded to this mapping spec: Account use open/close flag, subaccount use inactive/active indicator
        if (useSubAccount) {
            return accountSubAccountAttributes.isSubAccountActive()? CemiAwardConstants.OPEN : CemiAwardConstants.CLOSED;
        }
        return accountSubAccountAttributes.isAccountAccountClosedIndicator() ? CemiAwardConstants.CLOSED : CemiAwardConstants.OPEN;
    }
    
    private String determineCostCenter(String awardOrgCode) {
        return StringUtils.isNotBlank(awardOrgCode) ? awardOrgCode : CemiBaseConstants.EMPTY_STRING;
    }
}
