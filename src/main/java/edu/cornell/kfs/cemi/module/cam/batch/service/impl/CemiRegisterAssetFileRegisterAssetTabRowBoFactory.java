package edu.cornell.kfs.cemi.module.cam.batch.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.impl.identity.Person;
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
        Validate.validState(allRegisterAssetTranslateTableMaps != null, "allRegisterAssetTranslateTableMaps cannot be null.");

        final CemiRegisterAssetFileRegisterAssetTabRowBo registerAssetTabDataRow = new CemiRegisterAssetFileRegisterAssetTabRowBo();

        registerAssetTabDataRow.setBusinessAssetNumber(determineBusinessAssetNumber(asset.getCapitalAssetNumber()));
        registerAssetTabDataRow.setBusinessAssetID(determineBusinessAssetID(asset.getCapitalAssetNumber()));
        registerAssetTabDataRow.setCompany(determineCompany());
        registerAssetTabDataRow.setBusinessAssetName(determineBusinessAssetName());
        registerAssetTabDataRow.setBusinessAssetDescription(determineBusinessAssetDescription());
        registerAssetTabDataRow.setSpendCategory(determineSpendCategory());
        registerAssetTabDataRow.setAccountingTreatment(determineAccountingTreatment());
        registerAssetTabDataRow.setAcquisitionMethod(determineAcquisitionMethod());
        registerAssetTabDataRow.setMemo(determineMemo());
        registerAssetTabDataRow.setAcquisitionCost(determineAcquisitionCost());
        registerAssetTabDataRow.setResidualValue(determineResidualValue());
        registerAssetTabDataRow.setFairMarketValue(determineFairMarketValue());
        registerAssetTabDataRow.setQuantity(determineQuantity());
        registerAssetTabDataRow.setWorktagType1(determineWorktagType1());
        registerAssetTabDataRow.setWorktagValue1(determineWorktagValue1());
        registerAssetTabDataRow.setWorktagType2(determineWorktagType2());
        registerAssetTabDataRow.setWorktagValue2(determineWorktagValue2());
        registerAssetTabDataRow.setWorktagType3(determineWorktagType3());
        registerAssetTabDataRow.setWorktagValue3(determineWorktagValue3());
        registerAssetTabDataRow.setDateAcquired(determineDateAcquired());
        registerAssetTabDataRow.setDatePlacedInService(determineDatePlacedInService());
        registerAssetTabDataRow.setLocation(determineLocation());
        registerAssetTabDataRow.setAssetIdentifier(determineAssetIdentifier());
        registerAssetTabDataRow.setSerialNumber(determineSerialNumber());
        registerAssetTabDataRow.setManufacturer(determineManufacturer());
        registerAssetTabDataRow.setAssetClass(determineAssetClass());
        registerAssetTabDataRow.setAssetType(determineAssetType());
        registerAssetTabDataRow.setCoordinatingCostCenter(determineCoordinatingCostCenter());
        registerAssetTabDataRow.setAssetCoordinator(determineAssetCoordinator());
        registerAssetTabDataRow.setPoNumber(determinePoNumber());
        registerAssetTabDataRow.setDepreciationProfileOverride(determineDepreciationProfileOverride());
        registerAssetTabDataRow.setDepreciationMethodOverride(determineDepreciationMethodOverride());
        registerAssetTabDataRow.setDepreciationPercentOverride(determineDepreciationPercentOverride());
        registerAssetTabDataRow.setUsefulLifeInPeriodsOverride(determineUsefulLifeInPeriodsOverride());
        registerAssetTabDataRow.setDepreciationThresholdOverride(determineDepreciationThresholdOverride());
        registerAssetTabDataRow.setRemainingDepreciationPeriods(determineRemainingDepreciationPeriods());
        registerAssetTabDataRow.setAccumulatedDepreciation(determineAccumulatedDepreciation());

        return registerAssetTabDataRow;
    }

    private String determineBusinessAssetNumber(Long businessAssetNumber) {
        return businessAssetNumber != null ? businessAssetNumber.toString() : KFSConstants.EMPTY_STRING;
    }

    private String determineBusinessAssetID(Long businessAssetNumber) {
        return businessAssetNumber != null ? businessAssetNumber.toString() : KFSConstants.EMPTY_STRING;
    }

    private String determineCompany() {
        return CemiRegisterAssetConstants.WorkdayCompany.CU_MAIN_CAMPUS;
    }

    private String determineBusinessAssetName() {
        return StringUtils.defaultIfBlank(asset.getManufacturerModelNumber(), KFSConstants.EMPTY_STRING);
    }

    private String determineBusinessAssetDescription() {
        return StringUtils.defaultIfBlank(asset.getCapitalAssetDescription(), KFSConstants.EMPTY_STRING);
    }

    // TODO: This is currently using a provided default value. At a future date,
    // update this method to use a Spend Category mapping table or API.
    private String determineSpendCategory() {
        return CemiRegisterAssetConstants.DEFAULT_ITHACA_SPEND_CATEGORY;
    }

    private String determineAccountingTreatment() {
        String accountingTreatment = KFSConstants.EMPTY_STRING;
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        if (StringUtils.isNotBlank(assetTypeCode)) {
            String wdAssetTypeCode = allRegisterAssetTranslateTableMaps.getAssetTypeMap().get(assetTypeCode);
            if (StringUtils.isNotBlank(wdAssetTypeCode)) {
                accountingTreatment = allRegisterAssetTranslateTableMaps.getAccountingTreatmentMap()
                        .get(wdAssetTypeCode);
            }
        }
        return accountingTreatment;
    }

    //    If GIK (Gift), Transfer (Transferred from other university or federal) and GFE = Other
    //            Purchased (New) = PURCHASED
    private String determineAcquisitionMethod() {
        String assetAcquisitionType = asset.getAcquisitionTypeCode();

        if (CemiRegisterAssetConstants.OTHER_ACQUISITION_TYPE_CODES.contains(assetAcquisitionType)) {
            return CemiRegisterAssetConstants.ACQUISITION_METHOD_OTHER;
        }
        if (CemiRegisterAssetConstants.PURCHASED_ACQUISITION_TYPE_CODES.contains(assetAcquisitionType)) {
            return CemiRegisterAssetConstants.ACQUISITION_METHOD_PURCHASED;
        }
        return KFSConstants.EMPTY_STRING;
    }

    private String determineMemo() {
        return StringUtils.defaultIfBlank(asset.getAcquisitionTypeCode(), KFSConstants.EMPTY_STRING);
    }

    private String determineAcquisitionCost() {
        return asset.getTotalCostAmount() != null ? String.valueOf(asset.getTotalCostAmount())
                : KFSConstants.EMPTY_STRING;
    }

    private String determineResidualValue() {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineFairMarketValue() {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineQuantity() {
        return CemiRegisterAssetConstants.NUMERIC_ONE;
    }

    private String determineWorktagType1() {
        return CemiRegisterAssetConstants.COST_CENTER_ID;
    }

    // TODO: This is currently using a provided default value. At a future date,
    // update this method to use a Cost Center mapping table or API.
    private String determineWorktagValue1() {
        return CemiRegisterAssetConstants.DEFAULT_ITHACA_COST_CENTER;
    }

    private String determineWorktagType2() {
        return CemiRegisterAssetConstants.FUND_ID;
    }

    // TODO: This is currently using a provided default value. At a future date,
    // update this method to use a Fund mapping table or API.
    private String determineWorktagValue2() {
        return CemiRegisterAssetConstants.DEFAULT_ITHACA_FUND;
    }

    private String determineWorktagType3() {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineWorktagValue3() {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineDateAcquired() {
        return determineFormattedDate(asset.getCreateDate());
    }

    private String determineDatePlacedInService() {
        return determineFormattedDate(asset.getCapitalAssetInServiceDate());
    }

    private String determineLocation() {
        return CemiRegisterAssetConstants.CORNELL_UNIVERSITY_ITHACA;
    }

    private String determineAssetIdentifier() {
        return StringUtils.defaultIfBlank(asset.getCampusTagNumber(), KFSConstants.EMPTY_STRING);
    }

    private String determineSerialNumber() {
        return StringUtils.defaultIfBlank(asset.getSerialNumber(), KFSConstants.EMPTY_STRING);
    }

    private String determineManufacturer() {
        return StringUtils.defaultIfBlank(asset.getManufacturerName(), KFSConstants.EMPTY_STRING);
    }

    // TBD
    private String determineAssetClass() {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        String assetClass = KFSConstants.EMPTY_STRING;
        if (StringUtils.isNotBlank(assetTypeCode)) {
            assetClass = allRegisterAssetTranslateTableMaps.getAssetClassMap().get(assetTypeCode);
        }
        return assetClass;
    }

    private String determineAssetType() {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        String wdAssetType = KFSConstants.EMPTY_STRING;
        if (StringUtils.isNotBlank(assetTypeCode)) {
            wdAssetType = allRegisterAssetTranslateTableMaps.getAssetTypeMap().get(assetTypeCode);
        }
        return wdAssetType;
    }

    // TODO: This is currently using a provided default value. At a future date,
    // update this method to use a Cost Center mapping table or API.
    private String determineCoordinatingCostCenter() {
        String assetOwnerOrganization = asset.getOrganizationOwnerAccountNumber();
        return CemiRegisterAssetConstants.DEFAULT_ITHACA_COST_CENTER;
    }
    
    private String determineAssetCoordinator() {
        Person assetRepresentative = asset.getAssetRepresentative();
        if (ObjectUtils.isNull(assetRepresentative)) {
            return KFSConstants.EMPTY_STRING;
        }
        return StringUtils.defaultIfBlank(assetRepresentative.getPrincipalName(), KFSConstants.EMPTY_STRING);
    }
    
    private String determinePoNumber() {
        List<AssetPayment> assetPayments = asset.getAssetPayments();
        if (CollectionUtils.isEmpty(assetPayments)) {
            return KFSConstants.EMPTY_STRING;
        }

        List<AssetPayment> sortedPayments = new ArrayList<>(assetPayments);
        sortedPayments.sort(Comparator.comparing(AssetPayment::getPaymentSequenceNumber));

        Set<String> poNumbers = new LinkedHashSet<>();
        for (AssetPayment assetPayment : sortedPayments) {
            String poNumber = assetPayment.getPurchaseOrderNumber();
            if (StringUtils.isNotBlank(poNumber)) {
                poNumbers.add(poNumber);
            }
        }
        return StringUtils.join(poNumbers, KFSConstants.BLANK_SPACE);
    }

    private String determineDepreciationProfileOverride() {
        String assetTypeCode = asset.getCapitalAssetTypeCode();
        String depreciationProfile = KFSConstants.EMPTY_STRING;
        if (StringUtils.isNotBlank(assetTypeCode)) {
            depreciationProfile = allRegisterAssetTranslateTableMaps.getDepreciationProfileMap().get(assetTypeCode);
        }
        return depreciationProfile;
    }

    private String determineDepreciationMethodOverride() {
        return CemiRegisterAssetConstants.STRAIGHT_LINE;
    }

    private String determineDepreciationPercentOverride() {
        return KFSConstants.EMPTY_STRING;
    }

    private String determineUsefulLifeInPeriodsOverride() {
        AssetType assetType = asset.getCapitalAssetType();
        if (ObjectUtils.isNotNull(assetType)) {
            Integer assetDepreciation = asset.getCapitalAssetType().getDepreciableLifeLimit();
            if (assetDepreciation != null) {
                return String.valueOf(assetDepreciation * 12);
            }
        }
        return KFSConstants.EMPTY_STRING;
    }

    private String determineDepreciationThresholdOverride() {
        return CemiRegisterAssetConstants.DEPRECIATION_START_DATE;
    }

    private String determineRemainingDepreciationPeriods() {
        return CemiRegisterAssetConstants.NUMERIC_ONE;
    }

    private String determineAccumulatedDepreciation() {
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
