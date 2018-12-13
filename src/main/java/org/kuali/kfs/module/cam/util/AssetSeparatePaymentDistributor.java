/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2018 Kuali, Inc.
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
package org.kuali.kfs.module.cam.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class is a calculator which will distribute the amounts and balance them by ratio. Inputs received are
 * <li>Source Asset</li>
 * <li>Source Payments</li>
 * <li>Current max of payment number used by source Asset</li>
 * <li>AssetGlobal Document performing the separate action</li>
 * <li>List of new assets to be created for this separate request document</li>
 * Logic is best explained as below
 * <li>Compute the ratio of amounts to be removed from source payments</li>
 * <li>Compute the ratio by which each new asset should receive the allocated amount</li>
 * <li>Separate the allocate amount from the source payment using ratio computed above</li>
 * <li>Apply the allocate amount by ratio to each new asset</li>
 * <li>Adjust the last payment to round against the source from which split is done</li>
 * <li>Adjust the account charge amount of each asset by rounding the last payment with reference to user input
 * separate amount</li>
 * <li>Create offset payments for the source asset</li>
 * <li>Compute accumulated depreciation amount for each payment, including offsets</li>
 */
public class AssetSeparatePaymentDistributor {

    private Asset sourceAsset;
    private AssetGlobal assetGlobal;
    private List<Asset> newAssets;
    private List<AssetPayment> sourcePayments;
    private List<AssetPayment> separatedPayments = new ArrayList<>();
    private List<AssetPayment> offsetPayments = new ArrayList<>();
    private List<AssetPayment> remainingPayments = new ArrayList<>();
    private HashMap<Long, KualiDecimal> totalByAsset = new HashMap<>();
    private HashMap<Integer, List<AssetPayment>> paymentSplitMap = new HashMap<>();
    private double[] assetAllocateRatios;
    private double separateRatio;
    private double retainRatio;
    private Integer maxPaymentSeqNo;
    private static PropertyDescriptor[] assetPaymentProperties = PropertyUtils.getPropertyDescriptors(AssetPayment.class);


    /**
     * Constructs a AssetSeparatePaymentDistributor.java.
     *
     * @param sourceAsset     Source Asset
     * @param sourcePayments  Source Payments
     * @param maxPaymentSeqNo Current max of payment number used by source Asset
     * @param assetGlobal     AssetGlobal Document performing the separate action
     * @param newAssets       List of new assets to be created for this separate request document
     */
    public AssetSeparatePaymentDistributor(Asset sourceAsset, List<AssetPayment> sourcePayments,
            Integer maxPaymentSeqNo, AssetGlobal assetGlobal, List<Asset> newAssets) {
        super();
        this.sourceAsset = sourceAsset;
        this.sourcePayments = sourcePayments;
        this.maxPaymentSeqNo = maxPaymentSeqNo;
        this.assetGlobal = assetGlobal;
        this.newAssets = newAssets;
    }

    public void distribute() {
        KualiDecimal totalSourceAmount = this.assetGlobal.getTotalCostAmount();
        KualiDecimal totalSeparateAmount = this.assetGlobal.getSeparateSourceTotalAmount();
        KualiDecimal remainingAmount = totalSourceAmount.subtract(totalSeparateAmount);
        // Compute separate ratio
        separateRatio = totalSeparateAmount.doubleValue() / totalSourceAmount.doubleValue();
        // Compute the retained ratio
        retainRatio = remainingAmount.doubleValue() / totalSourceAmount.doubleValue();
        List<AssetGlobalDetail> assetGlobalDetails = this.assetGlobal.getAssetGlobalDetails();
        int size = assetGlobalDetails.size();
        assetAllocateRatios = new double[size];
        AssetGlobalDetail assetGlobalDetail;
        // Compute ratio by each asset
        for (int i = 0; i < size; i++) {
            assetGlobalDetail = assetGlobalDetails.get(i);
            Long capitalAssetNumber = assetGlobalDetail.getCapitalAssetNumber();
            totalByAsset.put(capitalAssetNumber, KualiDecimal.ZERO);
            assetAllocateRatios[i] =
                    assetGlobalDetail.getSeparateSourceAmount().doubleValue() / totalSeparateAmount.doubleValue();
        }
        // Prepare the source and offset payments for split
        prepareSourcePaymentsForSplit();
        // Distribute payments by ratio
        allocatePaymentAmountsByRatio();
        // Round and balance by each payment line
        roundPaymentAmounts();
        // Round and balance by separate source amount
        roundAccountChargeAmount();
        // create offset payments
        createOffsetPayments();
    }


    /**
     * Split the amount to be assigned from the source payments
     */
    private void prepareSourcePaymentsForSplit() {
        // Call the allocate with ratio for each payments
        for (AssetPayment assetPayment : this.sourcePayments) {
 //           if (assetPayment.getAccountChargeAmount() != null && assetPayment.getAccountChargeAmount().isNonZero()) {
            // KFSUPGRADE-929.  some payment has 0 charge amount but with depr amount
            if (assetPayment.getAccountChargeAmount() != null) {
                // Separate amount
                AssetPayment separatePayment = new AssetPayment();
                ObjectValueUtils.copySimpleProperties(assetPayment, separatePayment);
                this.separatedPayments.add(separatePayment);

                // Remaining amount
                AssetPayment remainingPayment = new AssetPayment();
                ObjectValueUtils.copySimpleProperties(assetPayment, remainingPayment);
                this.remainingPayments.add(remainingPayment);

                applyRatioToPaymentAmounts(assetPayment, new AssetPayment[]{separatePayment, remainingPayment}, new double[]{separateRatio, retainRatio});
            }
        }
    }

    /**
     * Creates offset payment by copying and negating the separated payments
     */
    private void createOffsetPayments() {
        // create offset payment by negating the amount fields
        for (AssetPayment separatePayment : this.separatedPayments) {
            AssetPayment offsetPayment = new AssetPayment();
            ObjectValueUtils.copySimpleProperties(separatePayment, offsetPayment);
            try {
                negatePaymentAmounts(offsetPayment);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            offsetPayment.setDocumentNumber(assetGlobal.getDocumentNumber());
            offsetPayment.setFinancialDocumentTypeCode(CamsConstants.PaymentDocumentTypeCodes.ASSET_GLOBAL_SEPARATE);
            offsetPayment.setVersionNumber(null);
            offsetPayment.setObjectId(null);
            offsetPayment.setPaymentSequenceNumber(++maxPaymentSeqNo);
            this.offsetPayments.add(offsetPayment);
        }
        this.sourceAsset.getAssetPayments().addAll(this.offsetPayments);
    }

    /**
     * Applies the asset allocate ratio for each payment line to be created and adds to the new asset. In addition it keeps track of
     * how amount is consumed by each asset and how each payment is being split
     */
    private void allocatePaymentAmountsByRatio() {
        int index = 0;
        for (AssetPayment source : this.separatedPayments) {

            // for each source payment, create target payments by ratio
            AssetPayment[] targets = new AssetPayment[assetAllocateRatios.length];
            for (int j = 0; j < assetAllocateRatios.length; j++) {
                AssetPayment newPayment = new AssetPayment();
                ObjectValueUtils.copySimpleProperties(source, newPayment);
                Asset currentAsset = this.newAssets.get(j);
                Long capitalAssetNumber = currentAsset.getCapitalAssetNumber();
                newPayment.setCapitalAssetNumber(capitalAssetNumber);
                newPayment.setDocumentNumber(assetGlobal.getDocumentNumber());
                newPayment.setFinancialDocumentTypeCode(CamsConstants.PaymentDocumentTypeCodes.ASSET_GLOBAL_SEPARATE);
                targets[j] = newPayment;
                newPayment.setVersionNumber(null);
                newPayment.setObjectId(null);
                currentAsset.getAssetPayments().add(index, newPayment);
            }
            applyRatioToPaymentAmounts(source, targets, assetAllocateRatios);

            // keep track of split happened for the source
            this.paymentSplitMap.put(source.getPaymentSequenceNumber(), Arrays.asList(targets));

            // keep track of total amount by asset
            for (int j = 0; j < targets.length; j++) {
                Asset currentAsset = this.newAssets.get(j);
                Long capitalAssetNumber = currentAsset.getCapitalAssetNumber();
                this.totalByAsset.put(capitalAssetNumber, this.totalByAsset.get(capitalAssetNumber).add(targets[j].getAccountChargeAmount()));
            }
            index++;
        }
    }

    /**
     * Rounds the last payment by adjusting the amounts against source amount
     */
    private void roundPaymentAmounts() {
        for (AssetPayment separatedPayment : this.separatedPayments) {
            applyBalanceToPaymentAmounts(separatedPayment,
                    this.paymentSplitMap.get(separatedPayment.getPaymentSequenceNumber()));
        }
    }

    /**
     * Rounds the last payment by adjusting the amount compared against separate source amount and copies account charge amount to
     * primary depreciation base amount if not zero
     */
    private void roundAccountChargeAmount() {
        for (int j = 0; j < this.newAssets.size(); j++) {
            Asset currentAsset = this.newAssets.get(j);
            AssetGlobalDetail detail = this.assetGlobal.getAssetGlobalDetails().get(j);
            AssetPayment lastPayment = currentAsset.getAssetPayments().get(currentAsset.getAssetPayments().size() - 1);
            KualiDecimal totalForAsset = this.totalByAsset.get(currentAsset.getCapitalAssetNumber());
            KualiDecimal diff = detail.getSeparateSourceAmount().subtract(totalForAsset);
            lastPayment.setAccountChargeAmount(lastPayment.getAccountChargeAmount().add(diff));
            currentAsset.setTotalCostAmount(totalForAsset.add(diff));
            AssetPayment lastSource = this.separatedPayments.get(this.separatedPayments.size() - 1);
            lastSource.setAccountChargeAmount(lastSource.getAccountChargeAmount().add(diff));
            // adjust primary depreciation base amount, same as account charge amount
            // KFSUPGRADE-929 : for payments that are 0 amount but not non-zero depr amount
            // TODO : need more testing
            if (lastPayment.getPrimaryDepreciationBaseAmount() != null && lastPayment.getPrimaryDepreciationBaseAmount().isNonZero()) {
                if (lastPayment.getAccountChargeAmount().isNonZero()
                || lastPayment.getAccumulatedPrimaryDepreciationAmount() == null || lastPayment.getAccumulatedPrimaryDepreciationAmount().isZero()) {
                   lastPayment.setPrimaryDepreciationBaseAmount(lastPayment.getAccountChargeAmount());
                   lastSource.setPrimaryDepreciationBaseAmount(lastSource.getAccountChargeAmount());
                }
            }
        }
    }

    /**
     * Utility method which can take one payment and distribute its amount by ratio to the target payments
     *
     * @param source  Source Payment
     * @param targets Target Payment
     * @param ratios  Ratio to be applied for each target
     */
    private void applyRatioToPaymentAmounts(AssetPayment source, AssetPayment[] targets, double[] ratios) {
        try {
            for (PropertyDescriptor propertyDescriptor : assetPaymentProperties) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && propertyDescriptor.getPropertyType() != null
                        && KualiDecimal.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    KualiDecimal amount = (KualiDecimal) readMethod.invoke(source);
                    if (amount != null && amount.isNonZero()) {
                        KualiDecimal[] ratioAmounts = KualiDecimalUtils.allocateByRatio(amount, ratios);
                        Method writeMethod = propertyDescriptor.getWriteMethod();
                        if (writeMethod != null) {
                            for (int i = 0; i < ratioAmounts.length; i++) {
                                writeMethod.invoke(targets[i], ratioAmounts[i]);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility method which can compute the difference between source amount and consumed amounts, then will adjust the last amount
     *
     * @param source       Source payments
     * @param consumedList Consumed Payments
     */
    private void applyBalanceToPaymentAmounts(AssetPayment source, List<AssetPayment> consumedList) {
        try {
            for (PropertyDescriptor propertyDescriptor : assetPaymentProperties) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && propertyDescriptor.getPropertyType() != null
                        && KualiDecimal.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    KualiDecimal amount = (KualiDecimal) readMethod.invoke(source);
                    if (amount != null && amount.isNonZero()) {
                        Method writeMethod = propertyDescriptor.getWriteMethod();
                        KualiDecimal consumedAmount = KualiDecimal.ZERO;
                        KualiDecimal currAmt = KualiDecimal.ZERO;
                        if (writeMethod != null) {
                            for (AssetPayment aConsumedList : consumedList) {
                                currAmt = (KualiDecimal) readMethod.invoke(aConsumedList);
                                consumedAmount = consumedAmount.add(currAmt != null ? currAmt : KualiDecimal.ZERO);
                            }
                        }
                        if (!consumedAmount.equals(amount)) {
                            AssetPayment lastPayment = consumedList.get(consumedList.size() - 1);
                            writeMethod.invoke(lastPayment, currAmt.add(amount.subtract(consumedAmount)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility method which will negate the payment amounts for a given payment
     *
     * @param assetPayment Payment to be negated
     */
    public void negatePaymentAmounts(AssetPayment assetPayment) {
        try {
            for (PropertyDescriptor propertyDescriptor : assetPaymentProperties) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && propertyDescriptor.getPropertyType() != null && KualiDecimal.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    KualiDecimal amount = (KualiDecimal) readMethod.invoke(assetPayment);
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    if (writeMethod != null && amount != null) {
                        writeMethod.invoke(assetPayment, (amount.negated()));
                    }

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sums up YTD values and Previous Year value to decide accumulated depreciation amount
     */
    private void computeAccumulatedDepreciationAmount() {
        KualiDecimal previousYearAmount;
        for (Asset asset : this.newAssets) {
            List<AssetPayment> assetPayments = asset.getAssetPayments();
            for (AssetPayment currPayment : assetPayments) {
                previousYearAmount = currPayment.getPreviousYearPrimaryDepreciationAmount();
                previousYearAmount = previousYearAmount == null ? KualiDecimal.ZERO : previousYearAmount;
                KualiDecimal computedAmount = previousYearAmount.add(sumPeriodicDepreciationAmounts(currPayment));
                if (computedAmount.isNonZero()) {
                    currPayment.setAccumulatedPrimaryDepreciationAmount(computedAmount);
                }
            }
        }
        for (AssetPayment currPayment : this.offsetPayments) {
            previousYearAmount = currPayment.getPreviousYearPrimaryDepreciationAmount();
            previousYearAmount = previousYearAmount == null ? KualiDecimal.ZERO : previousYearAmount;
            KualiDecimal computedAmount = previousYearAmount.add(sumPeriodicDepreciationAmounts(currPayment));
            if (computedAmount.isNonZero()) {
                currPayment.setAccumulatedPrimaryDepreciationAmount(computedAmount);
            }
        }
    }

    /**
     * Sums up periodic amounts for a payment
     *
     * @param currPayment Payment
     * @return Sum of payment
     */
    public static KualiDecimal sumPeriodicDepreciationAmounts(AssetPayment currPayment) {
        KualiDecimal ytdAmount = KualiDecimal.ZERO;
        try {
            for (PropertyDescriptor propertyDescriptor : assetPaymentProperties) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && Pattern.matches(CamsConstants.GET_PERIOD_DEPRECIATION_AMOUNT_REGEX,
                        readMethod.getName().toLowerCase()) && propertyDescriptor
                        .getPropertyType() != null && KualiDecimal.class
                        .isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    KualiDecimal amount = (KualiDecimal) readMethod.invoke(currPayment);
                    if (amount != null) {
                        ytdAmount = ytdAmount.add(amount);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ytdAmount;
    }

    public List<AssetPayment> getRemainingPayments() {
        return remainingPayments;
    }

    public void setRemainingPayments(List<AssetPayment> remainingPayments) {
        this.remainingPayments = remainingPayments;
    }

    public List<AssetPayment> getOffsetPayments() {
        return offsetPayments;
    }

    public void setOffsetPayments(List<AssetPayment> offsetPayments) {
        this.offsetPayments = offsetPayments;
    }

    public List<AssetPayment> getSeparatedPayments() {
        return separatedPayments;
    }

    public void setSeparatedPayments(List<AssetPayment> separatedPayments) {
        this.separatedPayments = separatedPayments;
    }

    public AssetGlobal getAssetGlobal() {
        return assetGlobal;
    }

    public void setAssetGlobal(AssetGlobal assetGlobal) {
        this.assetGlobal = assetGlobal;
    }

    public List<Asset> getNewAssets() {
        return newAssets;
    }

    public void setNewAssets(List<Asset> newAssets) {
        this.newAssets = newAssets;
    }

    public double[] getAssetAllocateRatios() {
        return assetAllocateRatios;
    }

    public void setAssetAllocateRatios(double[] assetAllocateRatios) {
        this.assetAllocateRatios = assetAllocateRatios;
    }

    public double getSeparateRatio() {
        return separateRatio;
    }

    public void setSeparateRatio(double separateRatio) {
        this.separateRatio = separateRatio;
    }

    public double getRetainRatio() {
        return retainRatio;
    }

    public void setRetainRatio(double retainRatio) {
        this.retainRatio = retainRatio;
    }

    public List<AssetPayment> getSourcePayments() {
        return sourcePayments;
    }

    public void setSourcePayments(List<AssetPayment> sourcePayments) {
        this.sourcePayments = sourcePayments;
    }
}
