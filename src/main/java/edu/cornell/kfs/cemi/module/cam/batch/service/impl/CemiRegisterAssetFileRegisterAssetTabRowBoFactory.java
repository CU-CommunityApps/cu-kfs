package edu.cornell.kfs.cemi.module.cam.batch.service.impl;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.kfs.module.cam.businessobject.AssetType;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cam.CemiRegisterAssetConstants;
import edu.cornell.kfs.cemi.module.cam.batch.businessobject.CemiRegisterAssetFileRegisterAssetTabRowBo;
import edu.cornell.kfs.cemi.module.cam.batch.translatetable.CemiRegisterAssetTranslateTableMaps;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.module.cam.businessobject.AssetExtension;

// The factory class deals with converting the legacy data values to the new data value representation.
// Depending upon how the data needs to be placed in the data extraction file, multiple business object factories
// may be required to transform the information, one per business object type.
// Ensure that your classes, attributes, method names, business object factories and business objects have
// meaningful names to make the code self documenting.

@SuppressWarnings("deprecation")
public class CemiRegisterAssetFileRegisterAssetTabRowBoFactory {

    private Asset asset;
    private AssetExtension assetExtendedAttribute;
    private String jobRunDateString;
    private DateTimeService dateTimeService;
    private boolean maskSensitiveData = true; // Initialize default processing to mask in the event
                                              // KFS system parameter has not been created.
    private CemiRegisterAssetTranslateTableMaps allRegisterAssetTranslateTableMaps;

    public CemiRegisterAssetFileRegisterAssetTabRowBoFactory(final Asset asset,
            final AssetExtension assetExtendedAttribute, final String jobRunDateString,
            final DateTimeService dateTimeService, final boolean maskSensitiveData,
            final CemiRegisterAssetTranslateTableMaps allRegisterAssetTranslateTableMaps) {
        this.asset = asset;
        this.assetExtendedAttribute = assetExtendedAttribute;
        this.jobRunDateString = jobRunDateString;
        this.dateTimeService = dateTimeService;
        this.maskSensitiveData = maskSensitiveData;
        this.allRegisterAssetTranslateTableMaps = allRegisterAssetTranslateTableMaps;
    }

    public CemiRegisterAssetFileRegisterAssetTabRowBo createCemiRegisterAssetFileRegisterAssetTabRowBo() {
        Validate.validState(asset != null, "Asset cannot be null.");
        Validate.validState(assetExtendedAttribute != null, "AssetExtension cannot be null.");
        Validate.validState(jobRunDateString != null, "jobRunDateString cannot be null.");
        Validate.validState(dateTimeService != null, "DateTimeService cannot be null.");

        final CemiRegisterAssetFileRegisterAssetTabRowBo registerAssetTabDataRow = new CemiRegisterAssetFileRegisterAssetTabRowBo();

        registerAssetTabDataRow.setBusinessAssetNumber(determineBusinessAssetNumber(asset.getCapitalAssetNumber()));
        registerAssetTabDataRow.setBusinessAssetID(determineBusinessAssetID(asset.getCapitalAssetNumber()));
        registerAssetTabDataRow.setCompany(determineCompany(asset));
        registerAssetTabDataRow.setBusinessAssetName(determineBusinessAssetName(asset));
        registerAssetTabDataRow.setBusinessAssetDescription(determineBusinessAssetDescription(asset));
        registerAssetTabDataRow.setSpendCategory(determineSpendCategory(asset));
        registerAssetTabDataRow.setAccountingTreatment(determineAccountingTreatment(asset));
        registerAssetTabDataRow.setAcquisitionMethod(determineAcquisitionMethod(asset));
        registerAssetTabDataRow.setMemo(determineMemo(asset));
        registerAssetTabDataRow.setAcquisitionCost(determineAcquisitionCost(asset));
        registerAssetTabDataRow.setResidualValue(determineResidualValue(asset));
        registerAssetTabDataRow.setFairMarketValue(determineFairMarketValue(asset));
        registerAssetTabDataRow.setQuantity(determineQuantity(asset));
        registerAssetTabDataRow.setWorktagType1(determineWorktagType1(asset));
        registerAssetTabDataRow.setWorktagValue1(determineWorktagValue1(asset));
        registerAssetTabDataRow.setWorktagType2(determineWorktagType2(asset));
        registerAssetTabDataRow.setWorktagValue2(determineWorktagValue2(asset));
        registerAssetTabDataRow.setWorktagType3(determineWorktagType3(asset));
        registerAssetTabDataRow.setWorktagValue3(determineWorktagValue3(asset));
        registerAssetTabDataRow.setDateAcquired(determineDateAcquired(asset));
        registerAssetTabDataRow.setDatePlacedInService(determineDatePlacedInService(asset));
        registerAssetTabDataRow.setLocation(determineLocaltion(asset));
        registerAssetTabDataRow.setAssetIdentifier(determineAssetIdentifier(asset));
        registerAssetTabDataRow.setSerialNumber(determineSerialNumber(asset));
        registerAssetTabDataRow.setManufacturer(determineManufacturer(asset));
        registerAssetTabDataRow.setAssetClass(determineAssetClass(asset));
        registerAssetTabDataRow.setAssetType(determineAssetType(asset));
        registerAssetTabDataRow.setCoordinatingCostCenter(determineCoordinatingCostCenter(asset));
        registerAssetTabDataRow.setAssetCoordinator(determineAssetCoordinator(asset));
        registerAssetTabDataRow.setPoNumber(determinePoNumber(asset));
        registerAssetTabDataRow.setDepreciationProfileOverride(determineDepreciationProfileOverride(asset));
        registerAssetTabDataRow.setDepreciationMethodOverride(determineDepreciationMethodOverride(asset));
        registerAssetTabDataRow.setDepreciationPercentOverride(determineDepreciationPercentOverride(asset));
        registerAssetTabDataRow.setUsefulLifeInPeriodsOverride(determineUsefulLifeInPeriodsOverride(asset));
        registerAssetTabDataRow.setDepreciationThresholdOverride(determineDepreciationThresholdOverride(asset));
        registerAssetTabDataRow.setRemainingDepreciationPeriods(determineRemainingDepreciationPeriods(asset));
        registerAssetTabDataRow.setAccumulatedDepreciation(determineAccumulatedDepreciation(asset));

        return registerAssetTabDataRow;
    }

    private String determineBusinessAssetNumber(Long businessAssetNumber) {
        return businessAssetNumber != null ? businessAssetNumber.toString() : KFSConstants.EMPTY_STRING;
    }

    private String determineBusinessAssetID(Long businessAssetNumber) {
        return businessAssetNumber != null ? businessAssetNumber.toString() : KFSConstants.EMPTY_STRING;
    }

    // Determined based on Asset Account Type Code.
    // Account Type code comes from : Organization Owner Organization Code ->
    // Organization Plant Account Number: -> Account Type Code
    // If Account Type code is CC then Company is C001. (Account Type code EN, TC,
    // JI all map to EN) All other Account Type codes map to Company C002.
    private String determineCompany(Asset asset) {
        String accountType = asset.getOrganizationOwnerAccount().getOrganization().getCampusPlantAccount()
                .getAccountTypeCode();
        if (CemiRegisterAssetConstants.AccountType.CC.equalsIgnoreCase(accountType)) {
            return CemiRegisterAssetConstants.WorkdayCompany.STATUTORY;
        } else {
            return CemiRegisterAssetConstants.WorkdayCompany.ENDOWED;
        }

    }

    private String determineBusinessAssetName(Asset asset) {
        return StringUtils.isNotBlank(asset.getManufacturerModelNumber()) ? asset.getManufacturerModelNumber()
                : KFSConstants.EMPTY_STRING;
    }

    private String determineBusinessAssetDescription(Asset asset) {
        return StringUtils.isNotBlank(asset.getCapitalAssetDescription()) ? asset.getCapitalAssetDescription()
                : KFSConstants.EMPTY_STRING;
    }

    // TODO: This is currently using a provided default value. At a future date,
    // update this method to use a Spend Category mapping table or API.
    private String determineSpendCategory(Asset asset) {
        return CemiRegisterAssetConstants.DEFAULT_ITHACA_SPEND_CATEGORY;
    }

    private String determineAccountingTreatment(Asset asset) {
        String accountingTreatment = KFSConstants.EMPTY_STRING;
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        if (StringUtils.isNotBlank(assetTypeCode)) {
            String wdAssetTypeCode = allRegisterAssetTranslateTableMaps.getAssetTypeMap().get(assetTypeCode);
            if (StringUtils.isNotBlank(wdAssetTypeCode)) {
                accountingTreatment = allRegisterAssetTranslateTableMaps.getAccountingTreatmentap()
                        .get(wdAssetTypeCode);
            }
        }
        return accountingTreatment;
    }

    //    If GIK (Gift), Transfer (Transferred from other university or federal) and GFE = Other
    //            Purchased (New) = PURCHASED
    private String determineAcquisitionMethod(Asset asset) {
        String assetAcquisitionType = asset.getAcquisitionTypeCode();

        if (CemiRegisterAssetConstants.OTHER_ACQUISITION_TYPE_CODES.contains(assetAcquisitionType)) {
            return CemiRegisterAssetConstants.ACQUISITION_METHOD_OTHER;
        }
        if (CemiRegisterAssetConstants.PURCHASED_ACQUISITION_TYPE_CODES.contains(assetAcquisitionType)) {
            return CemiRegisterAssetConstants.ACQUISITION_METHOD_PURCHASED;
        }
        return KFSConstants.EMPTY_STRING;
    }

    private String determineMemo(Asset asset) {
        return StringUtils.isNotBlank(asset.getAcquisitionTypeCode()) ? asset.getAcquisitionTypeCode()
                : KFSConstants.EMPTY_STRING;
    }

    private String determineAcquisitionCost(Asset asset) {
        return asset.getTotalCostAmount() != null ? String.valueOf(asset.getTotalCostAmount())
                : KFSConstants.EMPTY_STRING;
    }

    private String determineResidualValue(Asset asset) {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineFairMarketValue(Asset asset) {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineQuantity(Asset asset) {
        return CemiRegisterAssetConstants.NUMERIC_ONE;
    }

    private String determineWorktagType1(Asset asset) {
        return CemiRegisterAssetConstants.COST_CENTER_ID;
    }

    // TODO: This is currently using a provided default value. At a future date,
    // update this method to use a Cost Center mapping table or API.
    private String determineWorktagValue1(Asset asset) {
        return CemiRegisterAssetConstants.DEFAULT_ITHACA_COST_CENTER;
    }

    private String determineWorktagType2(Asset asset) {
        return CemiRegisterAssetConstants.FUND_ID;
    }

    // TODO: This is currently using a provided default value. At a future date,
    // update this method to use a Fund mapping table or API.
    private String determineWorktagValue2(Asset asset) {
        return CemiRegisterAssetConstants.DEFAULT_ITHACA_FUND;
    }

    private String determineWorktagType3(Asset asset) {
        return CemiRegisterAssetConstants.PROGRAM_ID;
    }

    private String determineWorktagValue3(Asset asset) {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineDateAcquired(Asset asset) {
        return determineFormattedDate(asset.getCreateDate());
    }

    private String determineDatePlacedInService(Asset asset) {
        return determineFormattedDate(asset.getCapitalAssetInServiceDate());
    }

    private String determineLocaltion(Asset asset) {
        return CemiRegisterAssetConstants.CORNELL_UNIVERSITY_ITHACA;
    }

    private String determineAssetIdentifier(Asset asset) {
        return StringUtils.isNotBlank(asset.getCampusTagNumber()) ? asset.getCampusTagNumber()
                : KFSConstants.EMPTY_STRING;
    }

    private String determineSerialNumber(Asset asset) {
        return StringUtils.isNotBlank(asset.getSerialNumber()) ? asset.getSerialNumber() : KFSConstants.EMPTY_STRING;
    }

    private String determineManufacturer(Asset asset) {
        return StringUtils.isNotBlank(asset.getManufacturerName()) ? asset.getManufacturerName()
                : KFSConstants.EMPTY_STRING;
    }

    // TBD
    private String determineAssetClass(Asset asset) {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        String assetClass = KFSConstants.EMPTY_STRING;
        if (StringUtils.isNotBlank(assetTypeCode)) {
            assetClass = allRegisterAssetTranslateTableMaps.getAssetClassMap().get(assetTypeCode);
        }
        return assetClass;
    }

    private String determineAssetType(Asset asset) {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        String wdAssetType = KFSConstants.EMPTY_STRING;
        if (StringUtils.isNotBlank(assetTypeCode)) {
            wdAssetType = allRegisterAssetTranslateTableMaps.getAssetTypeMap().get(assetTypeCode);
        }
        return wdAssetType;
    }

    // TODO: This is currently using a provided default value. At a future date,
    // update this method to use a Cost Center mapping table or API.
    private String determineCoordinatingCostCenter(Asset asset) {
        String assetOwnerOrganization = asset.getOrganizationOwnerAccountNumber();
        return CemiRegisterAssetConstants.DEFAULT_ITHACA_COST_CENTER;
    }

    private String determineAssetCoordinator(Asset asset) {
        String assetRepresentativeName = asset.getAssetRepresentative().getPrincipalName();
        return StringUtils.isNotBlank(assetRepresentativeName) ? assetRepresentativeName : KFSConstants.EMPTY_STRING;
    }

    private String determinePoNumber(Asset asset) {
        List<AssetPayment> assetPayments = asset.getAssetPayments();
        Set<String> poNumbers = new HashSet<String>();
        if (CollectionUtils.isEmpty(assetPayments)) {
            return KFSConstants.EMPTY_STRING;
        } else {
            for (AssetPayment assetPayment : assetPayments) {
                String poNumber = assetPayment.getPurchaseOrderNumber();
                if (StringUtils.isNotBlank(poNumber)) {
                    poNumbers.add(poNumber);
                }
            }
            return StringUtils.join(poNumbers, KFSConstants.BLANK_SPACE);
        }
    }

    private String determineDepreciationProfileOverride(Asset asset) {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        String depreciationProfile = KFSConstants.EMPTY_STRING;
        if (StringUtils.isNotBlank(assetTypeCode)) {
            depreciationProfile = allRegisterAssetTranslateTableMaps.getDepreciationProfileMap().get(assetTypeCode);
        }
        return depreciationProfile;
    }

    private String determineDepreciationMethodOverride(Asset asset) {
        return CemiRegisterAssetConstants.STRAIGHT_LINE;
    }

    private String determineDepreciationPercentOverride(Asset asset) {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineUsefulLifeInPeriodsOverride(Asset asset) {
        AssetType assetType = asset.getCapitalAssetType();
        if (ObjectUtils.isNotNull(assetType)) {
            Integer assetDepreciation = asset.getCapitalAssetType().getDepreciableLifeLimit();
            if (assetDepreciation != null) {
                return String.valueOf(assetDepreciation * 12);
            }
        }
        return KFSConstants.EMPTY_STRING;
    }

    private String determineDepreciationThresholdOverride(Asset asset) {
        return CemiRegisterAssetConstants.DEPRECATION_START_DATE;
    }

    private String determineRemainingDepreciationPeriods(Asset asset) {
        return CemiRegisterAssetConstants.NUMERIC_ONE;
    }

    private String determineAccumulatedDepreciation(Asset asset) {
        KualiDecimal accumulateDepreciation = asset.getAccumulatedDepreciation();
        if (ObjectUtils.isNotNull(accumulateDepreciation)) {
            return accumulateDepreciation.toString();
        }
        return KFSConstants.EMPTY_STRING;
    }

    private String determineFormattedDate(Date dateToFormat) {
        return ObjectUtils.isNotNull(dateToFormat)
                ? dateTimeService.toString(dateToFormat, CemiBaseConstants.DATE_FORMAT_yyyy_MM_dd)
                : KFSConstants.EMPTY_STRING;
    }

}
