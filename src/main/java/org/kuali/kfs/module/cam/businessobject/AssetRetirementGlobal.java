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
package org.kuali.kfs.module.cam.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.document.service.AssetPaymentService;
import org.kuali.kfs.module.cam.document.service.AssetRetirementService;
import org.kuali.kfs.module.cam.document.service.PaymentSummaryService;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.PostalCode;
import org.kuali.kfs.sys.businessobject.State;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * CU Customization: 2021-01-28 version of this file with 2021-09-23 patch changes for FINP-7837 added.
 * This overlay file should be removed when we upgrade to the 2021-09-23 financials patch.
 */

public class AssetRetirementGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {

    protected String documentNumber;
    protected Long mergedTargetCapitalAssetNumber;
    protected String mergedTargetCapitalAssetDescription;
    protected String retirementReasonCode;
    protected String retirementChartOfAccountsCode;
    protected String retirementAccountNumber;
    protected String retirementContactName;
    protected String retirementInstitutionName;
    protected String retirementStreetAddress;
    protected String retirementCityName;
    protected String retirementStateCode;
    protected String retirementZipCode;
    protected String retirementCountryCode;
    protected String retirementPhoneNumber;
    protected KualiDecimal estimatedSellingPrice;
    protected KualiDecimal salePrice;
    protected String cashReceiptFinancialDocumentNumber;
    protected KualiDecimal handlingFeeAmount;
    protected KualiDecimal preventiveMaintenanceAmount;
    protected String buyerDescription;
    protected String paidCaseNumber;
    // persistent relationship
    protected Date retirementDate;
    protected Asset mergedTargetCapitalAsset;
    protected AssetRetirementReason retirementReason;
    protected DocumentHeader documentHeader;
    protected List<AssetRetirementGlobalDetail> assetRetirementGlobalDetails;
    protected Account retirementAccount;
    protected Chart retirementChartOfAccounts;
    protected DocumentHeader cashReceiptFinancialDocument;
    protected Country retirementCountry;
    protected State retirementState;
    protected PostalCode postalZipCode;

    protected List<GeneralLedgerPendingEntry> generalLedgerPendingEntries;

    protected Integer postingYear;
    protected String postingPeriodCode;
    protected AccountingPeriod accountingPeriod;
    protected static transient AccountingPeriodService accountingPeriodService;

    // Non-persistent
    protected KualiDecimal calculatedTotal;

    public AssetRetirementGlobal() {
        this.assetRetirementGlobalDetails = new ArrayList<>();
        this.generalLedgerPendingEntries = new ArrayList<>();
    }

    @Override
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        return null;
    }

    @Override
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
        AssetRetirementService retirementService = SpringContext.getBean(AssetRetirementService.class);

        List<PersistableBusinessObject> persistables = new ArrayList<>();

        if (retirementService.isAssetRetiredByMerged(this) && mergedTargetCapitalAsset != null) {
            setMergeObjectsForPersist(persistables, retirementService);
        }

        for (AssetRetirementGlobalDetail detail : assetRetirementGlobalDetails) {
            // Start: FINP-7837 changes
            // do a deep copy so the source asset isn't changed and subsequent calls to this method return the same list
            // of persistable BOs; otherwise an OptimisticLockException can occur when blanket approving an
            // asset retirement global
            setAssetForPersist((Asset) ObjectUtils.deepCopy(detail.getAsset()), persistables, retirementService);
            // End: FINP-7837 changes
        }

        return persistables;
    }

    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
        List<Collection<PersistableBusinessObject>> managedList = super.buildListOfDeletionAwareLists();
        managedList.add(new ArrayList<>(getAssetRetirementGlobalDetails()));
        return managedList;
    }

    /**
     * This method set asset fields for update
     *
     * @param persistables
     */
    protected void setAssetForPersist(Asset asset, List<PersistableBusinessObject> persistables,
            AssetRetirementService retirementService) {
        UniversityDateService universityDateService = SpringContext.getBean(UniversityDateService.class);

        // load the object by key
        asset.setInventoryStatusCode(CamsConstants.InventoryStatusCode.CAPITAL_ASSET_RETIRED);
        asset.setRetirementReasonCode(retirementReasonCode);

        // set retirement fiscal year and period code into asset
        UniversityDate currentUniversityDate = universityDateService.getCurrentUniversityDate();
        if (ObjectUtils.isNotNull(currentUniversityDate)) {
            asset.setRetirementFiscalYear(universityDateService.getCurrentUniversityDate().getUniversityFiscalYear());
            asset.setRetirementPeriodCode(universityDateService.getCurrentUniversityDate()
                    .getUniversityFiscalAccountingPeriod());
        }

        if (retirementService.isAssetRetiredByTheft(this)
                && StringUtils.isNotBlank(this.getPaidCaseNumber())) {
            asset.setCampusPoliceDepartmentCaseNumber(this.getPaidCaseNumber());
        } else if (retirementService.isAssetRetiredBySold(this)
                || retirementService.isAssetRetiredByAuction(this)) {
            asset.setRetirementChartOfAccountsCode(this.getRetirementChartOfAccountsCode());
            asset.setRetirementAccountNumber(this.getRetirementAccountNumber());
            asset.setCashReceiptFinancialDocumentNumber(this.getCashReceiptFinancialDocumentNumber());
            asset.setSalePrice(this.getSalePrice());
            asset.setEstimatedSellingPrice(this.getEstimatedSellingPrice());
        } else if (retirementService.isAssetRetiredByMerged(this)) {
            asset.setTotalCostAmount(KualiDecimal.ZERO);
            asset.setSalvageAmount(KualiDecimal.ZERO);
        } else if (retirementService.isAssetRetiredByExternalTransferOrGift(this)) {
            persistables.add(setOffCampusLocationObjectsForPersist(asset));
        }
        asset.setLastInventoryDate(new Timestamp(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate()
                .getTime()));
        persistables.add(asset);
    }

    /**
     * This method set off campus location for persist
     *
     * @param asset Asset to populate AssetLocation
     * @return the AssetLocation.
     */
    protected AssetLocation setOffCampusLocationObjectsForPersist(Asset asset) {
        AssetLocation offCampusLocation = new AssetLocation();
        offCampusLocation.setCapitalAssetNumber(asset.getCapitalAssetNumber());
        offCampusLocation.setAssetLocationTypeCode(CamsConstants.AssetLocationTypeCode.RETIREMENT);
        offCampusLocation = (AssetLocation) SpringContext.getBean(BusinessObjectService.class)
                .retrieve(offCampusLocation);
        if (offCampusLocation == null) {
            offCampusLocation = new AssetLocation();
            offCampusLocation.setCapitalAssetNumber(asset.getCapitalAssetNumber());
            offCampusLocation.setAssetLocationTypeCode(CamsConstants.AssetLocationTypeCode.RETIREMENT);
            asset.getAssetLocations().add(offCampusLocation);
        }

        offCampusLocation.setAssetLocationContactName(this.getRetirementContactName());
        offCampusLocation.setAssetLocationInstitutionName(this.getRetirementInstitutionName());
        offCampusLocation.setAssetLocationPhoneNumber(this.getRetirementPhoneNumber());
        offCampusLocation.setAssetLocationStreetAddress(this.getRetirementStreetAddress());
        offCampusLocation.setAssetLocationCityName(this.getRetirementCityName());
        offCampusLocation.setAssetLocationStateCode(this.getRetirementStateCode());
        offCampusLocation.setAssetLocationCountryCode(this.getRetirementCountryCode());
        offCampusLocation.setAssetLocationZipCode(this.getRetirementZipCode());

        return offCampusLocation;
    }

    /**
     * This method set target payment and source payment; set target/source asset salvageAmount/totalCostAmount
     *
     * @param persistables
     */
    protected void setMergeObjectsForPersist(List<PersistableBusinessObject> persistables,
            AssetRetirementService retirementService) {
        PaymentSummaryService paymentSummaryService = SpringContext.getBean(PaymentSummaryService.class);
        AssetPaymentService assetPaymentService = SpringContext.getBean(AssetPaymentService.class);

        Integer maxTargetSequenceNo = assetPaymentService.getMaxSequenceNumber(mergedTargetCapitalAssetNumber);

        KualiDecimal salvageAmount = KualiDecimal.ZERO;
        KualiDecimal totalCostAmount = KualiDecimal.ZERO;
        Asset sourceAsset;

        // update for each merge source asset
        for (AssetRetirementGlobalDetail detail : getAssetRetirementGlobalDetails()) {
            detail.refreshReferenceObject(CamsPropertyConstants.AssetRetirementGlobalDetail.ASSET);
            sourceAsset = detail.getAsset();

            totalCostAmount = totalCostAmount.add(paymentSummaryService.calculatePaymentTotalCost(sourceAsset));
            salvageAmount = salvageAmount.add(sourceAsset.getSalvageAmount());

            retirementService.generateOffsetPaymentsForEachSource(sourceAsset, persistables, detail.getDocumentNumber());
            maxTargetSequenceNo = retirementService.generateNewPaymentForTarget(mergedTargetCapitalAsset, sourceAsset,
                    persistables, maxTargetSequenceNo, detail.getDocumentNumber());

        }
        KualiDecimal mergedTargetSalvageAmount = mergedTargetCapitalAsset.getSalvageAmount() != null ?
                mergedTargetCapitalAsset.getSalvageAmount() : KualiDecimal.ZERO;

        // update merged target asset
        mergedTargetCapitalAsset.setTotalCostAmount(totalCostAmount.add(paymentSummaryService
                .calculatePaymentTotalCost(mergedTargetCapitalAsset)));
        mergedTargetCapitalAsset.setSalvageAmount(salvageAmount.add(mergedTargetSalvageAmount));
        mergedTargetCapitalAsset.setLastInventoryDate(new Timestamp(SpringContext.getBean(DateTimeService.class)
                .getCurrentSqlDate().getTime()));
        mergedTargetCapitalAsset.setCapitalAssetDescription(this.getMergedTargetCapitalAssetDescription());
        persistables.add(mergedTargetCapitalAsset);
    }

    @Override
    public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
        return getAssetRetirementGlobalDetails();
    }

    @Override
    public boolean isPersistable() {
        return true;
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Long getMergedTargetCapitalAssetNumber() {
        return mergedTargetCapitalAssetNumber;
    }

    public void setMergedTargetCapitalAssetNumber(Long mergedTargetCapitalAssetNumber) {
        this.mergedTargetCapitalAssetNumber = mergedTargetCapitalAssetNumber;
    }

    public String getRetirementReasonCode() {
        return retirementReasonCode;
    }

    public void setRetirementReasonCode(String retirementReasonCode) {
        this.retirementReasonCode = retirementReasonCode;
    }

    public Date getRetirementDate() {
        return retirementDate;
    }

    public void setRetirementDate(Date retirementDate) {
        this.retirementDate = retirementDate;
    }

    public Asset getMergedTargetCapitalAsset() {
        return mergedTargetCapitalAsset;
    }

    /**
     * @deprecated
     */
    public void setMergedTargetCapitalAsset(Asset mergedTargetCapitalAsset) {
        this.mergedTargetCapitalAsset = mergedTargetCapitalAsset;
    }

    public AssetRetirementReason getRetirementReason() {
        return retirementReason;
    }

    /**
     * @deprecated
     */
    public void setRetirementReason(AssetRetirementReason retirementReason) {
        this.retirementReason = retirementReason;
    }

    public DocumentHeader getDocumentHeader() {
        return documentHeader;
    }

    public void setDocumentHeader(DocumentHeader documentHeader) {
        this.documentHeader = documentHeader;
    }

    public List<AssetRetirementGlobalDetail> getAssetRetirementGlobalDetails() {
        return assetRetirementGlobalDetails;
    }

    public void setAssetRetirementGlobalDetails(List<AssetRetirementGlobalDetail> assetRetirementGlobalDetails) {
        this.assetRetirementGlobalDetails = assetRetirementGlobalDetails;
    }

    public List<GeneralLedgerPendingEntry> getGeneralLedgerPendingEntries() {
        return generalLedgerPendingEntries;
    }

    public void setGeneralLedgerPendingEntries(List<GeneralLedgerPendingEntry> glPendingEntries) {
        this.generalLedgerPendingEntries = glPendingEntries;
    }

    public String getMergedTargetCapitalAssetDescription() {
        return mergedTargetCapitalAssetDescription;
    }

    public void setMergedTargetCapitalAssetDescription(String mergedTargetCapitalAssetDescription) {
        this.mergedTargetCapitalAssetDescription = mergedTargetCapitalAssetDescription;
    }

    public String getRetirementChartOfAccountsCode() {
        return retirementChartOfAccountsCode;
    }

    public void setRetirementChartOfAccountsCode(String retirementChartOfAccountsCode) {
        this.retirementChartOfAccountsCode = retirementChartOfAccountsCode;
    }

    public String getRetirementAccountNumber() {
        return retirementAccountNumber;
    }

    public void setRetirementAccountNumber(String retirementAccountNumber) {
        this.retirementAccountNumber = retirementAccountNumber;
    }

    public String getRetirementContactName() {
        return retirementContactName;
    }

    public void setRetirementContactName(String retirementContactName) {
        this.retirementContactName = retirementContactName;
    }

    public String getRetirementInstitutionName() {
        return retirementInstitutionName;
    }

    public void setRetirementInstitutionName(String retirementInstitutionName) {
        this.retirementInstitutionName = retirementInstitutionName;
    }

    public String getRetirementStreetAddress() {
        return retirementStreetAddress;
    }

    public void setRetirementStreetAddress(String retirementStreetAddress) {
        this.retirementStreetAddress = retirementStreetAddress;
    }

    public String getRetirementCityName() {
        return retirementCityName;
    }

    public void setRetirementCityName(String retirementCityName) {
        this.retirementCityName = retirementCityName;
    }

    public String getRetirementStateCode() {
        return retirementStateCode;
    }

    public void setRetirementStateCode(String retirementStateCode) {
        this.retirementStateCode = retirementStateCode;
    }

    public String getRetirementZipCode() {
        return retirementZipCode;
    }

    public void setRetirementZipCode(String retirementZipCode) {
        this.retirementZipCode = retirementZipCode;
    }

    public PostalCode getPostalZipCode() {
        return postalZipCode;
    }

    public void setPostalZipCode(PostalCode postalZipCode) {
        this.postalZipCode = postalZipCode;
    }

    public String getRetirementCountryCode() {
        return retirementCountryCode;
    }

    public void setRetirementCountryCode(String retirementCountryCode) {
        this.retirementCountryCode = retirementCountryCode;
    }

    public String getRetirementPhoneNumber() {
        return retirementPhoneNumber;
    }

    public void setRetirementPhoneNumber(String retirementPhoneNumber) {
        this.retirementPhoneNumber = retirementPhoneNumber;
    }

    public KualiDecimal getEstimatedSellingPrice() {
        return estimatedSellingPrice;
    }

    public void setEstimatedSellingPrice(KualiDecimal estimatedSellingPrice) {
        this.estimatedSellingPrice = estimatedSellingPrice;
    }

    public KualiDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(KualiDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getCashReceiptFinancialDocumentNumber() {
        return cashReceiptFinancialDocumentNumber;
    }

    public void setCashReceiptFinancialDocumentNumber(String cashReceiptFinancialDocumentNumber) {
        this.cashReceiptFinancialDocumentNumber = cashReceiptFinancialDocumentNumber;
    }

    public KualiDecimal getHandlingFeeAmount() {
        return handlingFeeAmount;
    }

    public void setHandlingFeeAmount(KualiDecimal handlingFeeAmount) {
        this.handlingFeeAmount = handlingFeeAmount;
    }

    public KualiDecimal getPreventiveMaintenanceAmount() {
        return preventiveMaintenanceAmount;
    }

    public void setPreventiveMaintenanceAmount(KualiDecimal preventiveMaintenanceAmount) {
        this.preventiveMaintenanceAmount = preventiveMaintenanceAmount;
    }

    public String getBuyerDescription() {
        return buyerDescription;
    }

    public void setBuyerDescription(String buyerDescription) {
        this.buyerDescription = buyerDescription;
    }

    public String getPaidCaseNumber() {
        return paidCaseNumber;
    }

    public void setPaidCaseNumber(String paidCaseNumber) {
        this.paidCaseNumber = paidCaseNumber;
    }

    public Chart getRetirementChartOfAccounts() {
        return retirementChartOfAccounts;
    }

    /**
     * @deprecated
     */
    public void setRetirementChartOfAccounts(Chart retirementChartOfAccounts) {
        this.retirementChartOfAccounts = retirementChartOfAccounts;
    }

    public Account getRetirementAccount() {
        return retirementAccount;
    }

    /**
     * @deprecated
     */
    public void setRetirementAccount(Account retirementAccount) {
        this.retirementAccount = retirementAccount;
    }

    public DocumentHeader getCashReceiptFinancialDocument() {
        return cashReceiptFinancialDocument;
    }

    /**
     * @deprecated
     */
    public void setCashReceiptFinancialDocument(DocumentHeader cashReceiptFinancialDocument) {
        this.cashReceiptFinancialDocument = cashReceiptFinancialDocument;
    }

    public Country getRetirementCountry() {
        return retirementCountry;
    }

    /**
     * @deprecated
     */
    public void setRetirementCountry(Country retirementCountry) {
        this.retirementCountry = retirementCountry;
    }

    public State getRetirementState() {
        return retirementState;
    }

    /**
     * @deprecated
     */
    public void setRetirementState(State retirementState) {
        this.retirementState = retirementState;
    }

    public KualiDecimal getCalculatedTotal() {
        this.calculatedTotal = KualiDecimal.ZERO;
        if (this.handlingFeeAmount != null) {
            this.calculatedTotal = calculatedTotal.add(this.handlingFeeAmount);
        }
        if (this.preventiveMaintenanceAmount != null) {
            this.calculatedTotal = calculatedTotal.add(this.preventiveMaintenanceAmount);
        }
        if (this.salePrice != null) {
            this.calculatedTotal = calculatedTotal.add(this.salePrice);
        }
        return calculatedTotal;
    }

    public Integer getPostingYear() {
        return postingYear;
    }

    public void setPostingYear(Integer postingYear) {
        this.postingYear = postingYear;
    }

    public static AccountingPeriodService getAccountingPeriodService() {
        if (accountingPeriodService == null) {
            accountingPeriodService = SpringContext.getBean(AccountingPeriodService.class);
        }
        return accountingPeriodService;
    }

    /**
     * Creates a composite of postingPeriodCode and postingyear.
     *
     * @return composite or an empty string if either postingPeriodCode or postingYear is null
     */
    public String getAccountingPeriodCompositeString() {
        if (postingPeriodCode == null || postingYear == null) {
            return "";
        }
        return postingPeriodCode + postingYear;
    }

    public void setAccountingPeriodCompositeString(String accountingPeriodString) {
        if (StringUtils.isNotBlank(accountingPeriodString)) {
            String period = StringUtils.left(accountingPeriodString, 2);
            Integer year = Integer.valueOf(StringUtils.right(accountingPeriodString, 4));
            AccountingPeriod accountingPeriod = getAccountingPeriodService().getByPeriod(period, year);
            setAccountingPeriod(accountingPeriod);
        }
    }

    public void setAccountingPeriodCompositeString(AccountingPeriod accountingPeriod) {
        setAccountingPeriod(accountingPeriod);
    }

    public String getPostingPeriodCode() {
        return postingPeriodCode;
    }

    public void setPostingPeriodCode(String postingPeriodCode) {
        this.postingPeriodCode = postingPeriodCode;
    }

    /**
     * Set postingYear and postingPeriodCode
     *
     * @param accountingPeriod
     */
    public void setAccountingPeriod(AccountingPeriod accountingPeriod) {
        this.accountingPeriod = accountingPeriod;
        if (ObjectUtils.isNotNull(accountingPeriod)) {
            setPostingYear(accountingPeriod.getUniversityFiscalYear());
            setPostingPeriodCode(accountingPeriod.getUniversityFiscalPeriodCode());
        }
    }

    public AccountingPeriod getAccountingPeriod() {
        return accountingPeriod;
    }

}
