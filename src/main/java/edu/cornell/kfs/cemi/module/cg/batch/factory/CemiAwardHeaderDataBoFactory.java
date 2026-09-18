package edu.cornell.kfs.cemi.module.cg.batch.factory;

import java.sql.Date;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardScheduleConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardHeaderDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyAccountSubAccountDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyNovelutionBo;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableMaps;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

@SuppressWarnings("deprecation")
public class CemiAwardHeaderDataBoFactory {
    
    private Award award;
    private AwardExtendedAttribute awardExtendedAttribute;
    private Iterator<CemiAwardLegacyAccountSubAccountDataBo> allAccountsWithSubAccountsIterator;
    private String awardOrgCode;
    private CemiAwardLegacyNovelutionBo awardNovelutionAttributes;
    private String jobRunDateString;
    private CemiAwardExtractDao cemiAwardExtractDao;
    private DateTimeService dateTimeService;
    private CemiAwardTranslateTableMaps allAwardTranslateTableMaps;
    private boolean maskSensitiveData = true;

    public static CemiAwardHeaderDataBo createAwardHeaderDataBoFrom(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute,
            final Iterator<CemiAwardLegacyAccountSubAccountDataBo> allAccountsWithSubAccountsIterator,
            final String awardOrgCode, final CemiAwardLegacyNovelutionBo awardNovelutionAttributes,
            final String jobRunDateString, final DateTimeService dateTimeService,
            final CemiAwardExtractDao cemiAwardExtractDao, final CemiAwardTranslateTableMaps allAwardTranslateTableMaps,
            final boolean maskSensitiveData) {
        final CemiAwardHeaderDataBoFactory factory = new CemiAwardHeaderDataBoFactory(award, awardExtendedAttribute,
                allAccountsWithSubAccountsIterator, awardOrgCode, awardNovelutionAttributes, jobRunDateString,
                dateTimeService, cemiAwardExtractDao, allAwardTranslateTableMaps, maskSensitiveData);
        return factory.createCemiAwardHeaderDataBo();
    }
    
    public CemiAwardHeaderDataBoFactory (final Award award, final AwardExtendedAttribute awardExtendedAttribute,
            final Iterator<CemiAwardLegacyAccountSubAccountDataBo> allAccountsWithSubAccountsIterator,
            final String awardOrgCode, final CemiAwardLegacyNovelutionBo awardNovelutionAttributes, 
            final String jobRunDateString, final DateTimeService dateTimeService, final CemiAwardExtractDao cemiAwardExtractDao,
            final CemiAwardTranslateTableMaps allAwardTranslateTableMaps, final boolean maskSensitiveData) {
        Validate.notNull(award, "award cannot be null for CemiAwardHeaderDataBoFactory");
        Validate.notNull(awardExtendedAttribute, "awardExtendedAttribute cannot be null for CemiAwardHeaderDataBoFactory");
        Validate.notNull(allAccountsWithSubAccountsIterator, "allAccountsWithSubAccountsIterator cannot be null for CemiAwardHeaderDataBoFactory");
        Validate.notNull(awardOrgCode, "awardOrgCode cannot be null for CemiAwardHeaderDataBoFactory");
        Validate.notNull(awardNovelutionAttributes, "awardNovelutionAttributes cannot be null for CemiAwardHeaderDataBoFactory");
        Validate.notNull(allAwardTranslateTableMaps, "allAwardTranslateTableMaps cannot be null for CemiAwardHeaderDataBoFactory");
        Validate.notNull(jobRunDateString, "jobRunDateString cannot be null for CemiAwardHeaderDataBoFactory");
        Validate.notNull(dateTimeService, "dateTimeService cannot be null for CemiAwardHeaderDataBoFactory");
        this.award = award;
        this.awardExtendedAttribute = awardExtendedAttribute;
        this.allAccountsWithSubAccountsIterator = allAccountsWithSubAccountsIterator;
        this.awardOrgCode = awardOrgCode;
        this.awardNovelutionAttributes = awardNovelutionAttributes;
        this.jobRunDateString = jobRunDateString;
        this.dateTimeService = dateTimeService;
        this.cemiAwardExtractDao = cemiAwardExtractDao;
        this.allAwardTranslateTableMaps = allAwardTranslateTableMaps;
        this.maskSensitiveData = maskSensitiveData;
    }
     
    private CemiAwardHeaderDataBo createCemiAwardHeaderDataBo() {
        
        final CemiAwardHeaderDataBo awardHeaderBo = new CemiAwardHeaderDataBo();

        final String proposalNumber = setToEmptyStringWhenValueIsBlank(award.getProposalNumber());
        final String rowSpreadsheetKey = buildSpreadsheetKey(proposalNumber);
        
        final String company = determineCompany();
        
        final String sponsorAwardRefernceNumber = setToEmptyStringWhenValueIsBlank(award.getGrantNumber());
        final String awardProjectTitle = setToEmptyStringWhenValueIsBlank(award.getAwardProjectTitle());
        final String awardEffectiveDate = determineFormattedDate(award.getAwardBeginningDate());
        
        final String awardSignedDate = determineAwardSignedDate(awardNovelutionAttributes.getAwardSignedDate());
        
        final String awardType = determineTranslationValueFor(allAwardTranslateTableMaps.getSponsorAwardTypesMap(), award.getGrantDescriptionCode());
        final String purposeCode = determineTranslationValueFor(allAwardTranslateTableMaps.getAwardPurposeMap(), award.getAwardPurposeCode());
        
//FIXME  final String awardGroup = CemiAwardTranslateTableFactory.translateToAwardGroup();  //config workbook values not mapped yet
        final String awardGroup = CemiBaseConstants.EMPTY_STRING;
        
//FIXME mapping prior to unit test defaults was: final String awardCostCenter = determineCostCenter(awardOrgCode);
        final String awardCostCenterString = CemiAwardConstants.DEFAULT_COST_CENTER;
        
//FIXME mapping prior to unit test defaults was: build allAwardTranslateTableMaps.getAwardFundMap() 
//      Get all accounts on award, make sure all have same sub-fund group code, then use that subfundgroupcode for very complicate/hardcoded translationtable lookup
        final String awardFundString = CemiAwardConstants.DEFAULT_FUND;
        
///FIXME mapping prior to unit test defaults was: build allAwardTranslateTableMaps for Program, program renamed to function
        final String awardFunctionString = CemiAwardConstants.DEFAULT_FUNCTION;
        
//FIXME  final String awardContractOwner = determineEmployeeByCgAccountResponsibilityId(); //employee needs to map to Hire file employeeId, CGAccountResponsibilityId in RASS, mainAccount#, contractAccount#
        final String awardContractOwner = CemiBaseConstants.EMPTY_STRING;
        
//FIXME  three values have many-to-many, need primary award account for additional logic to get transformed-hardcoded lookup value
        final String awardLifecycleStatus = determineTranslationValueForAwardHeaderLifeCycleStatus(allAwardTranslateTableMaps.getAwardLineLifecycleStatusMap(), award.getAwardStatusCode()); 
                
//FIXME  needs to be Sponsor file SponsorId for the passThroughAgencyNumber
        final String sponsorIdForPassThroughAgency = determineSponsorIdForPassThroughAgency(award.getFederalPassThroughAgencyNumber());
        final String subAward = determineSubAward(sponsorIdForPassThroughAgency);
        
//FIXME needs to be Sponsor file sponsorId for the agency
        final String sponsorIdForAgency = determineSponsorIdForAgency(award.getAgencyNumber()); 

//FIXME  final String letterOfCredit = determineTranslationValueFor(allAwardTranslateTableMaps.getLetterOfCreditMap(), award.getLetterOfCreditFundGroupCode()); //config workbook values not mapped yet
        final String letterOfCredit = CemiBaseConstants.EMPTY_STRING;
                
        final String paymentType = determinePaymentType(letterOfCredit);
        final String letterOfCreditDocumentId = setToEmptyStringWhenValueIsBlank(awardExtendedAttribute.getLocAccountId());
        
        final KualiDecimal sponsorDirectCostAmount = award.getAwardDirectCostAmount();
        final KualiDecimal sponsorFacilitiesAndAdministrationAmount = award.getAwardIndirectCostAmount();
        final String sponsorDirectCostAmountString = convertKualiDecimalToString(sponsorDirectCostAmount);
        final String sponsorFacilitiesAndAdministrationAmountString = convertKualiDecimalToString(sponsorFacilitiesAndAdministrationAmount);
        final String zeroAmountAward = determineZeroAmountAward(sponsorDirectCostAmount, sponsorFacilitiesAndAdministrationAmount);
        
        final String cleanedCostShareTotalAmountString = removeFormattingFromNovelutionMoneyValueString(awardNovelutionAttributes.getCostShareTotalAmount());
        final String costShareRequiredBySponsor = determineCostShareRequiredBySponsor(cleanedCostShareTotalAmountString);
        final String authorizedAmountString = convertKualiDecimalToString(awardExtendedAttribute.getBudgetTotalAmount());
        final String cleanedAnticipatedSponsorDirectCostAmountString = removeFormattingFromNovelutionMoneyValueString(awardNovelutionAttributes.getAnticipatedSponsorDirectCostAmount());
        final String cleanedAnticipatedFacilitiesAndAdministrationAmountString = removeFormattingFromNovelutionMoneyValueString(awardNovelutionAttributes.getAnticipatedFacilitiesAndAdministrationAmount());
        final String awardSchedule = determineAwardScheduleReferenceId(proposalNumber);
        final String federalAwardIdNumberString = determineFederalAwardIdNumber(awardNovelutionAttributes.getFederalAwardIdNumber());

//FIXME final String cfdaNumberString = determineCfdaNumber(awardNovelutionAttributes.awardCFDANumber | Contract control account CFDA number)
        final String cfdaNumberString = CemiBaseConstants.EMPTY_STRING;
        
        awardHeaderBo.setProposalNumberUsedForDataRow(proposalNumber);

        awardHeaderBo.setSpreadsheetKey(rowSpreadsheetKey);
        awardHeaderBo.setAddOnly(CemiBaseConstants.YES);
        awardHeaderBo.setAward(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setAutoComplete(CemiBaseConstants.YES);
        awardHeaderBo.setAwardReferenceId(rowSpreadsheetKey);
        awardHeaderBo.setAwardNumber(rowSpreadsheetKey);
        awardHeaderBo.setVersion(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setCompany(company);
        awardHeaderBo.setSponsorAwardReferenceNumber(sponsorAwardRefernceNumber);
        awardHeaderBo.setAwardName(awardProjectTitle);
        awardHeaderBo.setAwardDescription(awardProjectTitle);
        awardHeaderBo.setAwardEffectiveDate(awardEffectiveDate);
        awardHeaderBo.setAwardSignedDate(awardSignedDate);
        awardHeaderBo.setAwardType(awardType);
        awardHeaderBo.setPurposeCode(purposeCode);
        awardHeaderBo.setPaymentTerms(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setInstitutionalId(proposalNumber);
        awardHeaderBo.setAwardSalaryCapDefault(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setSpendRestrictionDefault(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setRelatedAward(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setAwardGroup(awardGroup);
        awardHeaderBo.setAwardCostCenter(awardCostCenterString);
        awardHeaderBo.setAwardFund(awardFundString);
        awardHeaderBo.setAwardFunction(awardFunctionString);
        awardHeaderBo.setAwardContractOwner(awardContractOwner);
        awardHeaderBo.setAwardLifecycleStatus(awardLifecycleStatus);
        awardHeaderBo.setSubAward(subAward);
        awardHeaderBo.setPrimeSponsor(sponsorIdForPassThroughAgency);
        awardHeaderBo.setSponsor(sponsorIdForAgency);
        awardHeaderBo.setBillToSponsor(sponsorIdForAgency);
        awardHeaderBo.setPaymentType(paymentType);
        awardHeaderBo.setLetterOfCredit(letterOfCredit);
        awardHeaderBo.setLetterOfCreditDocumentId(letterOfCreditDocumentId);
        awardHeaderBo.setAwardSequenceBillingActiveReference(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setAwardBillingSequenceNumberFormatSyntaxReference(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setCurrentAwardBillingSequenceNumberUsedReference(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setAwardBillingSequenceGeneratorRule(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setCurrency(CemiAwardConstants.CURRENCY_USD);
        awardHeaderBo.setZeroAmountAward(zeroAmountAward);
        awardHeaderBo.setSponsorDirectCostAmount(sponsorDirectCostAmountString);
        awardHeaderBo.setSponsorFacilitiesAndAdministrationAmount(sponsorFacilitiesAndAdministrationAmountString);
        awardHeaderBo.setCostShareTotalAmount(cleanedCostShareTotalAmountString);
        awardHeaderBo.setAuthorizedAmount(authorizedAmountString);
        awardHeaderBo.setBillingLimitOverride(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setCostShareRequiredBySponsor(costShareRequiredBySponsor);
        awardHeaderBo.setAnticipatedSponsorDirectCostAmount(cleanedAnticipatedSponsorDirectCostAmountString);
        awardHeaderBo.setAnticipatedFacilitiesAndAdministrationAmount(cleanedAnticipatedFacilitiesAndAdministrationAmountString);
        awardHeaderBo.setAwardSchedule(awardSchedule);
        awardHeaderBo.setFederalAwardIdNumber(federalAwardIdNumberString);
        awardHeaderBo.setCfdaNumber(cfdaNumberString);
        awardHeaderBo.setProposalId(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setProposalVersion(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setOriginalProposal(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setAwardAmendmentReason(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setAwardAmendmentEffectiveDate(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setAwardNotes(CemiBaseConstants.EMPTY_STRING);
        awardHeaderBo.setBillingNotes(CemiBaseConstants.EMPTY_STRING);

        return awardHeaderBo;
    }

    private static String buildSpreadsheetKey(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
    }
    
    // Original mapping requirement was: Get all accounts on award, find control account, map account type to company
    // Changed to hard coded value requirement of C001
    private String determineCompany() {
        return CemiAwardConstants.COMPANY_CORNELL_UNIVERISY_MAIN_CAMPUS;
    }
    
    private String determineSubAward(String primeSponsor) {
      return StringUtils.isNotBlank(primeSponsor) ? CemiBaseConstants.YES : CemiAwardConstants.NO;
    }
    
    private String determinePaymentType(String letterOfCredit) {
        return StringUtils.isNotBlank(letterOfCredit) ? CemiAwardConstants.PAYMENT_TYPE_EFT : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String determineFormattedDate(Date dateToFormat) {
        return ObjectUtils.isNotNull(dateToFormat)
                ? dateTimeService.toString(dateToFormat, CemiBaseConstants.DATE_FORMAT_yyyy_MM_dd)
                        : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String convertKualiDecimalToString(KualiDecimal valueToConvert) {
        return ObjectUtils.isNotNull(valueToConvert) ? valueToConvert.toString() : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String determineZeroAmountAward(KualiDecimal sponsorDirectCostAmount, KualiDecimal sponsorFacilitiesAndAdministrationAmount) {
        KualiDecimal directAmount = ObjectUtils.isNotNull(sponsorDirectCostAmount) 
                ? sponsorDirectCostAmount : KualiDecimal.ZERO;
        KualiDecimal indirectAmount = ObjectUtils.isNotNull(sponsorFacilitiesAndAdministrationAmount) 
                ? sponsorFacilitiesAndAdministrationAmount : KualiDecimal.ZERO;
        KualiDecimal result = directAmount.add(indirectAmount);
        return result.isGreaterThan(KualiDecimal.ZERO) ? CemiAwardConstants.NO : CemiBaseConstants.YES;
    }

    private String removeFormattingFromNovelutionMoneyValueString(String valueToClean) {
        // Remove leading dollar sign and any existing commas
        char dollarSignChar = '$';
        char commaChar = ',';
        String cleanedAmountString = valueToClean.chars()
                                     .filter(c -> c != dollarSignChar)
                                     .filter(c -> c != commaChar)
                                     .mapToObj(c -> String.valueOf((char) c))
                                     .collect(Collectors.joining());
        return cleanedAmountString;
    }
    
    private String determineCostShareRequiredBySponsor(String costShareTotalAmountString) {
        String required = CemiAwardConstants.NO;
        if (StringUtils.isBlank(costShareTotalAmountString)) {
            return required;
        } 
        String cleanedAmountString = removeFormattingFromNovelutionMoneyValueString(costShareTotalAmountString);
        float amount = Float.parseFloat(cleanedAmountString);
        if (amount != 0) {
            required = CemiBaseConstants.YES;
        }
        return required;
    }
    
    private String determineAwardScheduleReferenceId(String proposalNumber) {
        String referenceId = CemiBaseConstants.EMPTY_STRING;
        
        String awardExtractionBuiltAwardScheduleReferenceId = 
                MessageFormat.format(CemiAwardScheduleConstants.SPREADSHEET_KEY_FORMAT, proposalNumber);
        
        if (cemiAwardExtractDao.awardScheduleContainsAwardExtractBuiltReferenceId(awardExtractionBuiltAwardScheduleReferenceId)) {
            referenceId = awardExtractionBuiltAwardScheduleReferenceId;
        }
        return referenceId;
    }
    
    private String determineFederalAwardIdNumber(String novelutionFederalAwardIdNumber) {
        if (StringUtils.isBlank(novelutionFederalAwardIdNumber) ||
            novelutionFederalAwardIdNumber.equalsIgnoreCase(CemiAwardConstants.NULL)) { 
                return  CemiBaseConstants.EMPTY_STRING;
        }
        return novelutionFederalAwardIdNumber;
    }
    
    private String setToEmptyStringWhenValueIsBlank(String value) {
        return StringUtils.isNotBlank(value) ? value : CemiBaseConstants.EMPTY_STRING;
    }

    private String determineTranslationValueFor(Map<String, String> translationMap, String codeToUseForLookup) {
        String valueFound = translationMap.get(codeToUseForLookup);
        return StringUtils.isNotBlank(valueFound) ? valueFound : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String determineTranslationValueForAwardHeaderLifeCycleStatus(Map<String, String> translationMap, String codeToUseForLookup) {
        if (StringUtils.isBlank(codeToUseForLookup)) {
            return CemiBaseConstants.EMPTY_STRING;
        } else if ( codeToUseForLookup.equalsIgnoreCase(CemiAwardConstants.AS) ||
                    codeToUseForLookup.equalsIgnoreCase(CemiAwardConstants.NF) ||
                    codeToUseForLookup.equalsIgnoreCase(CemiAwardConstants.PO) ) {
            return codeToUseForLookup;
        }
        String valueFound = translationMap.get(codeToUseForLookup);
        return StringUtils.isNotBlank(valueFound) ? valueFound : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String determineCostCenter(String awardOrgCode) {
        return StringUtils.isNotBlank(awardOrgCode) ? awardOrgCode : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String determineAwardSignedDate(String awardSignedDate) {
        if (StringUtils.isBlank(awardSignedDate) || awardSignedDate.equalsIgnoreCase(CemiAwardConstants.NULL)) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        return awardSignedDate;
    }
    
    private String determineSponsorIdForAgency(String agencyNumber) {
        return StringUtils.isNotBlank(agencyNumber) ? agencyNumber : CemiBaseConstants.EMPTY_STRING;
    }
    
    private String determineSponsorIdForPassThroughAgency(String passThroughAgencyNumber) {
        return StringUtils.isNotBlank(passThroughAgencyNumber) ? passThroughAgencyNumber : CemiBaseConstants.EMPTY_STRING;
    }
    
}
