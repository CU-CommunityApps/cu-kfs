package edu.cornell.kfs.cemi.module.cam.batch.service.impl;

import java.sql.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cam.CemiRegisterAssetConstants;
import edu.cornell.kfs.cemi.module.cam.batch.businessobject.CemiRegisterAssetFileRegisterAssetTabRowBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.module.cam.businessobject.AssetExtension;
import io.jsonwebtoken.lang.Collections;

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

    public CemiRegisterAssetFileRegisterAssetTabRowBoFactory(final Asset asset,
            final AssetExtension assetExtendedAttribute, final String jobRunDateString,
            final DateTimeService dateTimeService, final boolean maskSensitiveData) {
        this.asset = asset;
        this.assetExtendedAttribute = assetExtendedAttribute;
        this.jobRunDateString = jobRunDateString;
        this.dateTimeService = dateTimeService;
        this.maskSensitiveData = maskSensitiveData;
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
        registerAssetTabDataRow.setSpendCategory(determineSpendCategory(asset)); // TBD
        registerAssetTabDataRow.setAccountingTreatment(determineAccountingTreatment(asset)); // TBD
        registerAssetTabDataRow.setAcquisitionMethod(determineAcquisitionMethod(asset));
        registerAssetTabDataRow.setMemo(determineMemo(asset));
        registerAssetTabDataRow.setAcquisitionCost(determineAcquisitionCost(asset));
        registerAssetTabDataRow.setResidualValue(determineResidualValue(asset));
        registerAssetTabDataRow.setFairMarketValue(determineFairMarketValue(asset));
        registerAssetTabDataRow.setQuantity(determineQuantity(asset));
        registerAssetTabDataRow.setWorktagType1(determineWorktagType1(asset));
        registerAssetTabDataRow.setWorktagValue1(determineWorktagValue1(asset)); // TBD
        registerAssetTabDataRow.setWorktagType2(determineWorktagType2(asset));
        registerAssetTabDataRow.setWorktagValue2(determineWorktagValue2(asset)); // TBD
        registerAssetTabDataRow.setWorktagType3(determineWorktagType3(asset));
        registerAssetTabDataRow.setWorktagValue3(determineWorktagValue3(asset));
        registerAssetTabDataRow.setDateAcquired(determineDateAcquired(asset));
        registerAssetTabDataRow.setDatePlacedInService(determineDatePlacedInService(asset));
        registerAssetTabDataRow.setLocation(determineLocaltion(asset));
        registerAssetTabDataRow.setAssetIdentifier(determineAssetIdentifier(asset));
        registerAssetTabDataRow.setSerialNumber(determineSerialNumber(asset));
        registerAssetTabDataRow.setManufacturer(determineManufacturer(asset));
        registerAssetTabDataRow.setAssetClass(determineAssetClass(asset)); // TBD
        registerAssetTabDataRow.setAssetType(determineAssetType(asset)); // TBD
        registerAssetTabDataRow.setCoordinatingCostCenter(determineCoordinatingCostCenter(asset)); // TBD
        registerAssetTabDataRow.setAssetCoordinator(determineAssetCoordinator(asset)); // TBD
        registerAssetTabDataRow.setPoNumber(determinePoNumber(asset));// TBD
        registerAssetTabDataRow.setDepreciationProfileOverride(determineDepreciationProfileOverride(asset)); // TBD
        registerAssetTabDataRow.setDepreciationMethodOverride(determineDepreciationMethodOverride(asset));
        registerAssetTabDataRow.setDepreciationPercentOverride(determineDepreciationPercentOverride(asset));
        registerAssetTabDataRow
                .setUsefulLifeInPeriodsOverride(determineUsefulLifeInPeriodsOverride(registerAssetTabDataRow));// TBD
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

//    Account Type Code: > comes from :Organization Owner Organization Code -> Organization Plant Account Number: -> Account Type Code:
//
//        CC -> C01
//
//        EN
//        TC
//        JI 
//        All map to EN
//        All others C02
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
        return !StringUtils.isBlank(asset.getManufacturerModelNumber()) ? asset.getManufacturerModelNumber()
                : KFSConstants.EMPTY_STRING;
    }

    private String determineBusinessAssetDescription(Asset asset) {
        return !StringUtils.isBlank(asset.getCapitalAssetDescription()) ? asset.getCapitalAssetDescription()
                : KFSConstants.EMPTY_STRING;
    }

    // TBD
    private String determineSpendCategory(Asset asset) {
        return KFSConstants.EMPTY_STRING;
    }

    // TBD
    private String determineAccountingTreatment(Asset asset) {
        return KFSConstants.EMPTY_STRING;
    }

//    If GIK (Gift), Transfer (Transferred from other university or federal) and GFE = Other
//            Purchased (New) = PURCHASED
    private String determineAcquisitionMethod(Asset asset) {
        String assetAcquisitionType = asset.getAcquisitionTypeCode();
        List<String> otherAcquisitionTypes = List.of("G", "T", "Y");
        List<String> purchasedAcquisitionTypes = List.of("N");
        if (otherAcquisitionTypes.contains(assetAcquisitionType)) {
            return "OTHER";
        }
        if (purchasedAcquisitionTypes.contains(assetAcquisitionType)) {
            return "PURCHASED";
        }
        return KFSConstants.EMPTY_STRING;
    }

    private String determineMemo(Asset asset) {
        return !StringUtils.isBlank(asset.getAcquisitionTypeCode()) ? asset.getAcquisitionTypeCode()
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

    // TBD
    private String determineWorktagValue1(Asset asset) {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineWorktagType2(Asset asset) {
        return CemiRegisterAssetConstants.FUND_ID;
    }

    // TBD
    private String determineWorktagValue2(Asset asset) {
        return KFSConstants.EMPTY_STRING;
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
        return !StringUtils.isBlank(asset.getCampusTagNumber()) ? asset.getCampusTagNumber()
                : KFSConstants.EMPTY_STRING;
    }

    private String determineSerialNumber(Asset asset) {
        return !StringUtils.isBlank(asset.getSerialNumber()) ? asset.getSerialNumber() : KFSConstants.EMPTY_STRING;
    }

    private String determineManufacturer(Asset asset) {
        return !StringUtils.isBlank(asset.getManufacturerName()) ? asset.getManufacturerName()
                : KFSConstants.EMPTY_STRING;
    }

    // TBD
    private String determineAssetClass(Asset asset) {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        return KFSConstants.EMPTY_STRING;
    }

    private String determineAssetType(Asset asset) {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        return KFSConstants.EMPTY_STRING;
    }

    // TBD
    private String determineCoordinatingCostCenter(Asset asset) {
        String assetOwnerOrganization = asset.getOrganizationOwnerAccountNumber();
        return KFSConstants.EMPTY_STRING;
    }

    // TBD clarify if this is name or net id
    private String determineAssetCoordinator(Asset asset) {
        String assetRepresentativeName = asset.getAssetRepresentative().getPrincipalName();
        return !StringUtils.isBlank(assetRepresentativeName) ? assetRepresentativeName : KFSConstants.EMPTY_STRING;
    }

    // TBD confirm which po number to use
    private String determinePoNumber(Asset asset) {
        List<AssetPayment> assetPayments = asset.getAssetPayments();
        if (Collections.isEmpty(assetPayments)) {
            return KFSConstants.EMPTY_STRING;
        } else {
            String poNumber = assetPayments.get(0).getPurchaseOrderNumber();
            return !StringUtils.isBlank(poNumber) ? poNumber : KFSConstants.EMPTY_STRING;
        }
    }

    // TBD
    private String determineDepreciationProfileOverride(Asset asset) {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        return KFSConstants.EMPTY_STRING;
    }

    private String determineDepreciationMethodOverride(Asset asset) {
        return CemiRegisterAssetConstants.STRAIGHT_LINE;
    }

    private String determineDepreciationPercentOverride(Asset asset) {
        return KFSConstants.EMPTY_STRING;
    }

    // TBD
    private String determineUsefulLifeInPeriodsOverride(
            CemiRegisterAssetFileRegisterAssetTabRowBo registerAssetTabRowBo) {
        if(StringUtils.isNotBlank(registerAssetTabRowBo.getDepreciationProfileOverride())) {
        Integer depreciationProfileOverride = Integer.valueOf(registerAssetTabRowBo.getDepreciationProfileOverride());
        return String.valueOf(depreciationProfileOverride * 12);
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
        return KFSConstants.EMPTY_STRING;
    }

    private String determineFormattedDate(Date dateToFormat) {
        return ObjectUtils.isNotNull(dateToFormat)
                ? dateTimeService.toString(dateToFormat, CemiBaseConstants.DATE_FORMAT_yyyy_MM_dd)
                : KFSConstants.EMPTY_STRING;
    }

}
