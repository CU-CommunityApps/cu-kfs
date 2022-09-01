/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.cam.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsParameterConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.batch.AssetDepreciationStep;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetAcquisitionType;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetGlpeSourceDetail;
import org.kuali.kfs.module.cam.businessobject.AssetLocation;
import org.kuali.kfs.module.cam.businessobject.AssetObjectCode;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.kfs.module.cam.businessobject.AssetPaymentDetail;
import org.kuali.kfs.module.cam.document.gl.CamsGeneralLedgerPendingEntrySourceBase;
import org.kuali.kfs.module.cam.document.service.AssetGlobalService;
import org.kuali.kfs.module.cam.document.service.AssetObjectCodeService;
import org.kuali.kfs.module.cam.document.service.AssetPaymentService;
import org.kuali.kfs.module.cam.document.service.AssetService;
import org.kuali.kfs.module.cam.document.service.PaymentSummaryService;
import org.kuali.kfs.module.cam.util.AssetSeparatePaymentDistributor;
import org.kuali.kfs.module.cam.util.KualiDecimalUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/*
 * CU Customization: 2021-01-28 version of this file with 2021-09-23 patch changes for FINP-7837 added.
 * This overlay file should be removed when we upgrade to the 2021-09-23 financials patch.
 */

public class AssetGlobalServiceImpl implements AssetGlobalService {

    protected enum AmountCategory {
        PAYMENT {
            @Override
            void setParams(AssetGlpeSourceDetail postable, AssetPaymentDetail assetPaymentDetail,
                    AssetObjectCode assetObjectCode, OffsetDefinition offsetDefinition,
                    AssetAcquisitionType acquisitionType) {
                postable.setPayment(true);
                postable.setFinancialDocumentLineDescription(CamsConstants.AssetGlobal.LINE_DESCRIPTION_PAYMENT);
                postable.setFinancialObjectCode(assetPaymentDetail.getFinancialObjectCode());
                postable.setObjectCode(assetPaymentDetail.getObjectCode());
            }

        },
        PAYMENT_OFFSET {
            @Override
            void setParams(AssetGlpeSourceDetail postable, AssetPaymentDetail assetPaymentDetail,
                    AssetObjectCode assetObjectCode, OffsetDefinition offsetDefinition,
                    AssetAcquisitionType acquisitionType) {
                postable.setPaymentOffset(true);
                postable.setFinancialDocumentLineDescription(CamsConstants.AssetGlobal.LINE_DESCRIPTION_PAYMENT_OFFSET);
                postable.setFinancialObjectCode(acquisitionType.getIncomeAssetObjectCode());
                postable.setObjectCode(SpringContext.getBean(ObjectCodeService.class).getByPrimaryId(assetPaymentDetail.getPostingYear(), assetPaymentDetail.getChartOfAccountsCode(), acquisitionType.getIncomeAssetObjectCode()));
            }
        };

        abstract void setParams(AssetGlpeSourceDetail postable, AssetPaymentDetail assetPaymentDetail,
                AssetObjectCode assetObjectCode, OffsetDefinition offsetDefinition,
                AssetAcquisitionType acquisitionType);
    }

    protected ParameterService parameterService;
    protected AssetService assetService;
    protected UniversityDateService universityDateService;
    protected AssetObjectCodeService assetObjectCodeService;
    protected BusinessObjectService businessObjectService;
    protected AssetPaymentService assetPaymentService;
    protected PaymentSummaryService paymentSummaryService;
    protected DateTimeService dateTimeService;
    protected ObjectCodeService objectCodeService;
    protected OffsetDefinitionService offsetDefinitionService;

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Creates an instance of AssetGlpeSourceDetail depending on the source flag
     *
     * @param document           University Date Service
     * @param assetPaymentDetail Payment record for which postable is created
     * @param amountCategory
     * @return GL Postable source detail
     */
    protected AssetGlpeSourceDetail createAssetGlpePostable(AssetGlobal document,
            AssetPaymentDetail assetPaymentDetail, AmountCategory amountCategory) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start - createAssetGlpePostable (" + document.getDocumentNumber() + "-" +
                    assetPaymentDetail.getAccountNumber() + ")");
        }
        AssetGlpeSourceDetail postable = new AssetGlpeSourceDetail();

        assetPaymentDetail.refreshReferenceObject(CamsPropertyConstants.AssetPaymentDetail.ACCOUNT);
        postable.setAccount(assetPaymentDetail.getAccount());

        postable.setAmount(assetPaymentDetail.getAmount());
        postable.setAccountNumber(assetPaymentDetail.getAccountNumber());
        postable.setBalanceTypeCode(CamsConstants.Postable.GL_BALANCE_TYPE_CODE_AC);
        postable.setChartOfAccountsCode(assetPaymentDetail.getChartOfAccountsCode());
        postable.setDocumentNumber(document.getDocumentNumber());
        postable.setFinancialSubObjectCode(assetPaymentDetail.getFinancialSubObjectCode());
        postable.setPostingYear(assetPaymentDetail.getPostingYear());
        postable.setPostingPeriodCode(assetPaymentDetail.getPostingPeriodCode());
        postable.setProjectCode(assetPaymentDetail.getProjectCode());
        postable.setSubAccountNumber(assetPaymentDetail.getSubAccountNumber());
        postable.setOrganizationReferenceId(assetPaymentDetail.getOrganizationReferenceId());

        assetPaymentDetail.refreshReferenceObject(CamsPropertyConstants.AssetPaymentDetail.OBJECT_CODE);
        ObjectCode objectCode = objectCodeService.getByPrimaryIdForCurrentYear(assetPaymentDetail.getChartOfAccountsCode(), assetPaymentDetail.getFinancialObjectCode());

        AssetObjectCode assetObjectCode = assetObjectCodeService.findAssetObjectCode(assetPaymentDetail.getChartOfAccountsCode(), objectCode.getFinancialObjectSubTypeCode());

        OffsetDefinition offsetDefinition = offsetDefinitionService.getByPrimaryId(universityDateService.getCurrentFiscalYear(), assetPaymentDetail.getChartOfAccountsCode(), CamsConstants.AssetTransfer.DOCUMENT_TYPE_CODE, CamsConstants.Postable.GL_BALANCE_TYPE_CODE_AC);
        document.refreshReferenceObject(CamsPropertyConstants.AssetGlobal.ACQUISITION_TYPE);
        amountCategory.setParams(postable, assetPaymentDetail, assetObjectCode, offsetDefinition, document.getAcquisitionType());
        if (LOG.isDebugEnabled()) {
            LOG.debug("End - createAssetGlpePostable(" + document.getDocumentNumber() + "-" + assetPaymentDetail.getAccountNumber() + "-" + ")");
        }
        return postable;
    }

    @Override
    public void createGLPostables(AssetGlobal assetGlobal, CamsGeneralLedgerPendingEntrySourceBase assetGlobalGlPoster) {
        List<AssetPaymentDetail> assetPaymentDetails = assetGlobal.getAssetPaymentDetails();

        for (AssetPaymentDetail assetPaymentDetail : assetPaymentDetails) {
            if (isPaymentFinancialObjectActive(assetPaymentDetail)) {
                KualiDecimal accountChargeAmount = assetPaymentDetail.getAmount();
                if (accountChargeAmount != null && !accountChargeAmount.isZero()) {
                    assetGlobalGlPoster.getGeneralLedgerPendingEntrySourceDetails().add(createAssetGlpePostable(assetGlobal, assetPaymentDetail, AmountCategory.PAYMENT));
                    assetGlobalGlPoster.getGeneralLedgerPendingEntrySourceDetails().add(createAssetGlpePostable(assetGlobal, assetPaymentDetail, AmountCategory.PAYMENT_OFFSET));
                }
            }
        }
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setAssetObjectCodeService(AssetObjectCodeService assetObjectCodeService) {
        this.assetObjectCodeService = assetObjectCodeService;
    }

    public void setAssetPaymentService(AssetPaymentService assetPaymentService) {
        this.assetPaymentService = assetPaymentService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    /**
     * We need to calculate asset total amount from the sum of its payments. Otherwise, the asset total amount could
     * mismatch with the sum of payments.
     */
    @Override
    public KualiDecimal totalPaymentByAsset(AssetGlobal assetGlobal, boolean lastEntry) {
        KualiDecimal assetTotalAmount = KualiDecimal.ZERO;
        List<AssetPaymentDetail> assetPaymentDetails = assetGlobal.getAssetPaymentDetails();
        int numberOfTotalAsset = assetGlobal.getAssetGlobalDetails().size();
        if (numberOfTotalAsset > 0) {
            for (AssetPaymentDetail assetPaymentDetail : assetPaymentDetails) {
                KualiDecimal assetPaymentUnitCost = assetPaymentDetail.getAmount().divide(new KualiDecimal(numberOfTotalAsset));
                if (lastEntry) {
                    assetPaymentUnitCost = assetPaymentDetail.getAmount().subtract(assetPaymentUnitCost.multiply(new KualiDecimal(numberOfTotalAsset - 1)));
                }
                assetTotalAmount = assetTotalAmount.add(assetPaymentUnitCost);
            }
        }

        return assetTotalAmount;
    }

    @Override
    public boolean existsInGroup(String groupName, String memberName) {
        if (StringUtils.isBlank(groupName) || StringUtils.isBlank(memberName)) {
            return false;
        }
        return Arrays.asList(groupName.split(";")).contains(memberName);
    }

    protected boolean isPaymentFinancialObjectActive(AssetPaymentDetail assetPayment) {
        ObjectCode financialObjectCode = objectCodeService.getByPrimaryIdForCurrentYear(assetPayment.getChartOfAccountsCode(), assetPayment.getFinancialObjectCode());
        if (financialObjectCode != null) {
            return financialObjectCode.isActive();
        }
        return false;
    }

    public void setAssetService(AssetService assetService) {
        this.assetService = assetService;
    }

    @Override
    public boolean isAssetSeparate(AssetGlobal assetGlobal) {
        return ObjectUtils.isNotNull(assetGlobal.getFinancialDocumentTypeCode()) && assetGlobal
                .getFinancialDocumentTypeCode().equals(CamsConstants.PaymentDocumentTypeCodes.ASSET_GLOBAL_SEPARATE);
    }

    @Override
    public boolean isAssetSeparateByPayment(AssetGlobal assetGlobal) {
        return this.isAssetSeparate(assetGlobal) && ObjectUtils.isNotNull(
                assetGlobal.getSeparateSourcePaymentSequenceNumber());
    }

    /**
     * Add and return the total amount for all unique assets created.
     *
     * @param assetGlobal
     * @return Returns the total separate source amount
     */
    @Override
    public KualiDecimal getUniqueAssetsTotalAmount(AssetGlobal assetGlobal) {
        KualiDecimal totalAmount = KualiDecimal.ZERO;
        // add new asset location
        for (AssetGlobalDetail assetSharedDetail : assetGlobal.getAssetSharedDetails()) {
            // existing
            for (AssetGlobalDetail assetGlobalUniqueDetail : assetSharedDetail.getAssetGlobalUniqueDetails()) {
                KualiDecimal separateSourceAmount = assetGlobalUniqueDetail.getSeparateSourceAmount();
                if (separateSourceAmount != null) {
                    totalAmount = totalAmount.add(separateSourceAmount);
                }
            }
        }
        return totalAmount;
    }

    @Override
    public List<PersistableBusinessObject> getCreateNewAssets(AssetGlobal assetGlobal) {
        List<PersistableBusinessObject> persistables = new ArrayList<>();

        // create new assets with inner loop handling payments
        Iterator<AssetGlobalDetail> assetGlobalDetailsIterator = assetGlobal.getAssetGlobalDetails().iterator();
        for (int assetGlobalDetailsIndex = 0; assetGlobalDetailsIterator.hasNext(); assetGlobalDetailsIndex++) {
            AssetGlobalDetail assetGlobalDetail = assetGlobalDetailsIterator.next();

            // Create the asset with most fields set as required
            Asset asset = setupAsset(assetGlobal, assetGlobalDetail, false);

            // track total cost of all payments for this assetGlobalDetail
            KualiDecimal paymentsAccountChargeAmount = new KualiDecimal(0);

            // take care of all the payments for this asset
            for (AssetPaymentDetail assetPaymentDetail : assetGlobal.getAssetPaymentDetails()) {
                AssetPayment assetPayment = setupCreateNewAssetPayment(assetGlobalDetail.getCapitalAssetNumber(), assetGlobal.getAcquisitionTypeCode(), assetGlobal.getAssetGlobalDetails().size(), assetGlobalDetailsIndex, assetPaymentDetail);

                paymentsAccountChargeAmount = paymentsAccountChargeAmount.add(assetPayment.getAccountChargeAmount());

                asset.getAssetPayments().add(assetPayment);
            }

            // set the amount generically. Note for separate this should equal assetGlobalDetail.getSeparateSourceAmount()
            asset.setTotalCostAmount(paymentsAccountChargeAmount);

            persistables.add(asset);
        }

        return persistables;
    }

    @Override
    public List<PersistableBusinessObject> getSeparateAssets(AssetGlobal assetGlobal) {
        // Start: FINP-7837 changes
        // set the source asset amounts properly (do a deep copy so the source asset isn't changed and subsequent
        // calls to this method return the same list of persistable BOs; otherwise an OptimisticLockException can
        // occur when blanket approving an asset global separate)
        Asset separateSourceCapitalAsset = (Asset) ObjectUtils.deepCopy(assetGlobal.getSeparateSourceCapitalAsset());
        // End: FINP-7837 changes
        List<AssetPayment> sourcePayments = new ArrayList<>();

        for (AssetPayment assetPayment : separateSourceCapitalAsset.getAssetPayments()) {
            if (!this.isAssetSeparateByPayment(assetGlobal)) {
                sourcePayments.add(assetPayment);
            } else if (assetPayment.getPaymentSequenceNumber().equals(assetGlobal.getSeparateSourcePaymentSequenceNumber())) {
                // If this is separate by payment, then only add the payment that we are interested in
                sourcePayments.add(assetPayment);
                break;
            }
        }

        List<Asset> newAssets = new ArrayList<>();
        // create new assets with inner loop handling payments
        for (AssetGlobalDetail assetGlobalDetail : assetGlobal.getAssetGlobalDetails()) {
            newAssets.add(setupAsset(assetGlobal, assetGlobalDetail, true));
        }
        // adjust source asset amounts
        KualiDecimal divisor = assetGlobal.getSeparateSourceCapitalAsset().getTotalCostAmount();
        if (divisor.isZero()) {
            separateSourceCapitalAsset.setSalvageAmount(KualiDecimal.ZERO);
            separateSourceCapitalAsset.setReplacementAmount(KualiDecimal.ZERO);
            separateSourceCapitalAsset.setFabricationEstimatedTotalAmount(KualiDecimal.ZERO);
        } else {
            double separateRatio = 1 - assetGlobal.getSeparateSourceTotalAmount().doubleValue() / divisor.doubleValue();
            separateSourceCapitalAsset.setSalvageAmount(KualiDecimalUtils
                    .safeMultiply(assetGlobal.getSeparateSourceCapitalAsset().getSalvageAmount(), separateRatio));
            separateSourceCapitalAsset.setReplacementAmount(KualiDecimalUtils
                    .safeMultiply(assetGlobal.getSeparateSourceCapitalAsset().getReplacementAmount(), separateRatio));
            separateSourceCapitalAsset.setFabricationEstimatedTotalAmount(KualiDecimalUtils
                    .safeMultiply(assetGlobal.getSeparateSourceCapitalAsset().getFabricationEstimatedTotalAmount(),
                            separateRatio));
        }

        Integer maxSequenceNumber = assetPaymentService.getMaxSequenceNumber(separateSourceCapitalAsset.getCapitalAssetNumber());
        // Add to the save list
        AssetSeparatePaymentDistributor distributor = new AssetSeparatePaymentDistributor(separateSourceCapitalAsset,
                sourcePayments, maxSequenceNumber, assetGlobal, newAssets);
        distributor.distribute();
        // re-compute the source total cost amount after split
        separateSourceCapitalAsset.setTotalCostAmount(paymentSummaryService.calculatePaymentTotalCost(separateSourceCapitalAsset));
        List<PersistableBusinessObject> persistables = new ArrayList<>();
        persistables.add(separateSourceCapitalAsset);
        persistables.addAll(newAssets);
        return persistables;
    }

    /**
     * Creates a new Asset appropriate for either create new or separate. This does not create the payments for this
     * asset.
     *
     * @param assetGlobal       containing data for the asset to be created
     * @param assetGlobalDetail containing data for the asset to be created
     * @param separate          indicating whether this is a separate and asset or not
     * @return set of assets appropriate for this creation without payments
     */
    protected Asset setupAsset(AssetGlobal assetGlobal, AssetGlobalDetail assetGlobalDetail, boolean separate) {
        Asset asset = new Asset(assetGlobal, assetGlobalDetail, separate);

        // set financialObjectSubTypeCode per first payment entry if one exists
        if (!assetGlobal.getAssetPaymentDetails().isEmpty()
                && ObjectUtils.isNotNull(assetGlobal.getAssetPaymentDetails().get(0).getObjectCode())) {
            AssetPaymentDetail assetPaymentDetail = assetGlobal.getAssetPaymentDetails().get(0);

            ObjectCode objectCode = objectCodeService.getByPrimaryIdForCurrentYear(assetPaymentDetail.getChartOfAccountsCode(), assetPaymentDetail.getFinancialObjectCode());

            asset.setFinancialObjectSubTypeCode(objectCode.getFinancialObjectSubTypeCode());
        }

        // create off campus location for each asset only if it was filled in
        boolean offCampus = StringUtils.isNotBlank(assetGlobalDetail.getOffCampusName())
                || StringUtils.isNotBlank(assetGlobalDetail.getOffCampusAddress())
                || StringUtils.isNotBlank(assetGlobalDetail.getOffCampusCityName())
                || StringUtils.isNotBlank(assetGlobalDetail.getOffCampusStateCode())
                || StringUtils.isNotBlank(assetGlobalDetail.getOffCampusZipCode())
                || StringUtils.isNotBlank(assetGlobalDetail.getOffCampusCountryCode());
        if (offCampus) {
            setupAssetLocationOffCampus(assetGlobalDetail, asset);
        }

        // set specific values for new assets if document is Asset Separate
        if (separate) {
            KualiDecimal divisor = assetGlobal.getSeparateSourceCapitalAsset().getTotalCostAmount();
            if (divisor.isZero()) {
                asset.setSalvageAmount(KualiDecimal.ZERO);
                asset.setReplacementAmount(KualiDecimal.ZERO);
                asset.setFabricationEstimatedTotalAmount(KualiDecimal.ZERO);
            } else {
                double separateRatio = assetGlobalDetail.getSeparateSourceAmount().doubleValue() / divisor.doubleValue();
                asset.setSalvageAmount(KualiDecimalUtils
                        .safeMultiply(assetGlobal.getSeparateSourceCapitalAsset().getSalvageAmount(), separateRatio));
                asset.setReplacementAmount(KualiDecimalUtils
                        .safeMultiply(assetGlobal.getSeparateSourceCapitalAsset().getReplacementAmount(),
                                separateRatio));
                asset.setFabricationEstimatedTotalAmount(KualiDecimalUtils
                        .safeMultiply(assetGlobal.getSeparateSourceCapitalAsset().getFabricationEstimatedTotalAmount(),
                                separateRatio));
            }
            Date lastInventoryDate = assetGlobal.getLastInventoryDate();
            if (lastInventoryDate != null) {
                asset.setLastInventoryDate(new Timestamp(lastInventoryDate.getTime()));
            }
        }

        return asset;
    }

    /**
     * Off campus location appropriately set for this asset.
     *
     * @param assetGlobalDetail containing data for the location
     * @param asset             object that location is set in
     */
    protected void setupAssetLocationOffCampus(AssetGlobalDetail assetGlobalDetail, Asset asset) {
        // We are not checking if it already exists since on a new asset it can't
        AssetLocation offCampusAssetLocation = new AssetLocation();
        offCampusAssetLocation.setCapitalAssetNumber(asset.getCapitalAssetNumber());
        offCampusAssetLocation.setAssetLocationTypeCode(CamsConstants.AssetLocationTypeCode.OFF_CAMPUS);
        asset.getAssetLocations().add(offCampusAssetLocation);

        // Set the location fields either way
        offCampusAssetLocation.setAssetLocationContactName(assetGlobalDetail.getOffCampusName());
        offCampusAssetLocation.setAssetLocationContactIdentifier(assetGlobalDetail.getRepresentativeUniversalIdentifier());
        offCampusAssetLocation.setAssetLocationInstitutionName(assetGlobalDetail.getAssetRepresentative().getPrimaryDepartmentCode());
        offCampusAssetLocation.setAssetLocationStreetAddress(assetGlobalDetail.getOffCampusAddress());
        offCampusAssetLocation.setAssetLocationCityName(assetGlobalDetail.getOffCampusCityName());
        offCampusAssetLocation.setAssetLocationStateCode(assetGlobalDetail.getOffCampusStateCode());
        offCampusAssetLocation.setAssetLocationCountryCode(assetGlobalDetail.getOffCampusCountryCode());
        offCampusAssetLocation.setAssetLocationZipCode(assetGlobalDetail.getOffCampusZipCode());

        // There is no phone number field on Asset Global... odd...
        offCampusAssetLocation.setAssetLocationPhoneNumber(null);
    }

    /**
     * Creates a payment for an asset in create new mode.
     *
     * @param capitalAssetNumber      to use for the payment
     * @param acquisitionTypeCode     for logic in determining how dates are to be set
     * @param assetGlobalDetailsSize  for logic in determining depreciation amounts (if asset is depreciable)
     * @param assetGlobalDetailsIndex for logic in determining depreciation amounts (if asset is depreciable)
     * @param assetPaymentDetail      containing data for the payment
     * @return payment for an asset in create new
     */
    protected AssetPayment setupCreateNewAssetPayment(Long capitalAssetNumber, String acquisitionTypeCode,
            int assetGlobalDetailsSize, int assetGlobalDetailsIndex, AssetPaymentDetail assetPaymentDetail) {
        AssetPayment assetPayment = new AssetPayment(assetPaymentDetail, acquisitionTypeCode);
        assetPayment.setCapitalAssetNumber(capitalAssetNumber);
        assetPayment.setPaymentSequenceNumber(assetPaymentDetail.getSequenceNumber());
        assetPayment.setTransferPaymentCode(CamsConstants.AssetPayment.TRANSFER_PAYMENT_CODE_N);
        // Running this every time of the loop is inefficient, could be put into HashMap
        KualiDecimal[] amountBuckets = KualiDecimalUtils.allocateByQuantity(assetPaymentDetail.getAmount(), assetGlobalDetailsSize);

        assetPayment.setAccountChargeAmount(amountBuckets[assetGlobalDetailsIndex]);
        ObjectCode objectCode = objectCodeService.getByPrimaryIdForCurrentYear(assetPayment.getChartOfAccountsCode(), assetPayment.getFinancialObjectCode());

        boolean isDepreciablePayment = ObjectUtils.isNotNull(assetPaymentDetail.getObjectCode())
                && !Arrays.asList(parameterService.getParameterValueAsString(AssetDepreciationStep.class,
                    CamsParameterConstants.NON_DEPRECIABLE_FEDERALLY_OWNED_OBJECT_SUB_TYPES).split(";")).contains(objectCode.getFinancialObjectSubTypeCode());
        if (isDepreciablePayment) {
            assetPayment.setPrimaryDepreciationBaseAmount(amountBuckets[assetGlobalDetailsIndex]);
        } else {
            assetPayment.setPrimaryDepreciationBaseAmount(KualiDecimal.ZERO);
        }

        return assetPayment;
    }

    public void setPaymentSummaryService(PaymentSummaryService paymentSummaryService) {
        this.paymentSummaryService = paymentSummaryService;
    }

    /**
     * @return the parameter value for the new acquisition type code
     */
    @Override
    public String getNewAcquisitionTypeCode() {
        return parameterService.getParameterValueAsString(AssetGlobal.class,
            CamsParameterConstants.NEW_ACQUISITION_CODE_PARAM);
    }

    /**
     * @return the parameter value for the capital object acquisition code group
     */
    @Override
    public String getCapitalObjectAcquisitionCodeGroup() {
        return parameterService.getParameterValueAsString(AssetGlobal.class,
            CamsParameterConstants.CAPITAL_OBJECT_ACQUISITION_CODE_PARAM);
    }

    /**
     * @return the parameter value for the not new acquisition code group
     */
    @Override
    public String getNonNewAcquisitionCodeGroup() {
        return parameterService.getParameterValueAsString(AssetGlobal.class,
            CamsParameterConstants.NON_NEW_ACQUISITION_GROUP_PARAM);
    }

    @Override
    public String getFiscalYearEndDayAndMonth() {
        String yearEndDateAndMonth = parameterService.getParameterValueAsString(KfsParameterConstants.CAPITAL_ASSETS_ALL.class, CamsParameterConstants.FISCAL_YEAR_END);
        return yearEndDateAndMonth.substring(0, 2).concat("/").concat(yearEndDateAndMonth.substring(2, 4));
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    public void setOffsetDefinitionService(OffsetDefinitionService offsetDefinitionService) {
        this.offsetDefinitionService = offsetDefinitionService;
    }
}
