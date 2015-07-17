/*
 * Copyright 2012 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.fp.businessobject;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailAddendum6FieldUtil;

/**
 * Adds the fields needed on the record type 5000 addendum 6 (Fuel) records.
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendum6 extends MasterCardTransactionDetailAddendumBase
{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendum6.class);

    protected String filler1;
    protected String filler2;
    protected String oilCompanyBrand;
    protected String merchantStreetAddress;
    protected String merchantPostalCode;
    protected String timeOfPurchase;
    protected String motorFuelServiceType;
    protected String motorFuelProductCode;
    protected KualiDecimal motorFuelUnitPrice;
    protected String motorFuelUnitOfMeasure;
    protected BigDecimal motorFuelQuantity;
    protected KualiDecimal motorFuelSaleAmount;
    protected Integer odomoterReading;
    protected String vehicleNumber;
    protected String driverNumber;
    protected String magneticStripeProductTypeCode;
    protected KualiDecimal couponDiscountAmount;
    protected KualiDecimal taxExemptAmount;
    protected KualiDecimal taxAmount1;
    protected KualiDecimal taxAmount2;
    protected String filler3;

    /**
     * Constructor creates a new MasterCardTransactionDetail
     */
    public MasterCardTransactionDetailAddendum6()
    {
        super();

        this.motorFuelUnitPrice = new KualiDecimal(0);
        this.motorFuelSaleAmount = new KualiDecimal(0);
        this.couponDiscountAmount = new KualiDecimal(0);
        this.taxAmount1 = new KualiDecimal(0);
        this.taxAmount2 = new KualiDecimal(0);
    }


    /**
     * Parses inputRecord setting class attributes. Dates and currency are converted into respective java objects.
     *
     * @param inputRecord
     */
    public void parseInput(String inputRecord)
    {
        oilCompanyBrand = getValue(inputRecord, CuFPPropertyConstants.OIL_COMPANY_BRAND, CuFPPropertyConstants.MERCHANT_STREET_ADDRESS);
        merchantStreetAddress = getValue(inputRecord, CuFPPropertyConstants.MERCHANT_STREET_ADDRESS, CuFPPropertyConstants.MERCHANT_POSTAL_CODE);
        merchantPostalCode = getValue(inputRecord, CuFPPropertyConstants.MERCHANT_POSTAL_CODE, CuFPPropertyConstants.TIME_OF_PURCHASE);
        timeOfPurchase = getValue(inputRecord, CuFPPropertyConstants.TIME_OF_PURCHASE, CuFPPropertyConstants.MOTOR_FUEL_SERVICE_TYPE);
        motorFuelServiceType = getValue(inputRecord, CuFPPropertyConstants.MOTOR_FUEL_SERVICE_TYPE, CuFPPropertyConstants.MOTOR_FUEL_PRODUCT_CODE);
        motorFuelProductCode = getValue(inputRecord, CuFPPropertyConstants.MOTOR_FUEL_PRODUCT_CODE, CuFPPropertyConstants.MOTOR_FUEL_UNIT_PRICE);
        setMotorFuelUnitPrice(getValue(inputRecord, CuFPPropertyConstants.MOTOR_FUEL_UNIT_PRICE, CuFPPropertyConstants.MOTOR_FUEL_UOM));
        motorFuelUnitOfMeasure = getValue(inputRecord, CuFPPropertyConstants.MOTOR_FUEL_UOM, CuFPPropertyConstants.MOTOR_FUEL_QUANTITY);
        setMotorFuelQuantity(getValue(inputRecord, CuFPPropertyConstants.MOTOR_FUEL_QUANTITY, CuFPPropertyConstants.MOTOR_FUEL_SALE_AMOUNT));
        setMotorFuelSaleAmount(getValue(inputRecord, CuFPPropertyConstants.MOTOR_FUEL_SALE_AMOUNT, CuFPPropertyConstants.ODOMETER_READING));
        odomoterReading = new Integer(getValue(inputRecord, CuFPPropertyConstants.ODOMETER_READING, CuFPPropertyConstants.VEHICLE_NUMBER));
        vehicleNumber = getValue(inputRecord, CuFPPropertyConstants.VEHICLE_NUMBER, CuFPPropertyConstants.DRIVER_NUMBER);
        driverNumber = getValue(inputRecord, CuFPPropertyConstants.DRIVER_NUMBER, CuFPPropertyConstants.MAGNETIC_STRIPE_PRODUCT_TYPE_CODE);
        magneticStripeProductTypeCode = getValue(inputRecord, CuFPPropertyConstants.MAGNETIC_STRIPE_PRODUCT_TYPE_CODE, CuFPPropertyConstants.COUPON_DISCOUNT_AMOUNT);
        setCouponDiscountAmount(getValue(inputRecord, CuFPPropertyConstants.COUPON_DISCOUNT_AMOUNT, CuFPPropertyConstants.TAX_EXEMPT_AMOUNT));
        setTaxExemptAmount(getValue(inputRecord, CuFPPropertyConstants.TAX_EXEMPT_AMOUNT, CuFPPropertyConstants.TAX_AMOUNT1));
        setTaxAmount1(getValue(inputRecord, CuFPPropertyConstants.TAX_AMOUNT1, CuFPPropertyConstants.TAX_AMOUNT2));
        setTaxAmount2(getValue(inputRecord, CuFPPropertyConstants.TAX_AMOUNT2, CuFPPropertyConstants.FILLER3));
    }

    public String getFiller1()
    {
        return filler1;
    }

    public void setFiller1(String filler1)
    {
        this.filler1 = filler1;
    }

    public String getFiller2()
    {
        return filler2;
    }

    public void setFiller2(String filler2)
    {
        this.filler2 = filler2;
    }

    public String getOilCompanyBrand()
    {
        return oilCompanyBrand;
    }

    public void setOilCompanyBrand(String oilCompanyBrand)
    {
        this.oilCompanyBrand = oilCompanyBrand;
    }

    public String getMerchantStreetAddress()
    {
        return merchantStreetAddress;
    }

    public void setMerchantStreetAddress(String merchantStreetAddress)
    {
        this.merchantStreetAddress = merchantStreetAddress;
    }

    public String getMerchantPostalCode()
    {
        return merchantPostalCode;
    }

    public void setMerchantPostalCode(String merchantPostalCode)
    {
        this.merchantPostalCode = merchantPostalCode;
    }

    public String getTimeOfPurchase()
    {
        return timeOfPurchase;
    }

    public void setTimeOfPurchase(String timeOfPurchase)
    {
        this.timeOfPurchase = timeOfPurchase;
    }

    public String getMotorFuelServiceType()
    {
        return motorFuelServiceType;
    }

    public void setMotorFuelServiceType(String motorFuelServiceType)
    {
        this.motorFuelServiceType = motorFuelServiceType;
    }

    public String getMotorFuelProductCode()
    {
        return motorFuelProductCode;
    }

    public void setMotorFuelProductCode(String motorFuelProductCode)
    {
        this.motorFuelProductCode = motorFuelProductCode;
    }

    public KualiDecimal getMotorFuelUnitPrice()
    {
        return motorFuelUnitPrice;
    }

    public void setMotorFuelUnitPrice(KualiDecimal motorFuelUnitPrice)
    {
        this.motorFuelUnitPrice = motorFuelUnitPrice;
    }

    public void setMotorFuelUnitPrice(String motorFuelUnitPrice)
    {
        this.motorFuelUnitPrice = convertStringToKualiDecimal(motorFuelUnitPrice);
    }

    public String getMotorFuelUnitOfMeasure()
    {
        return motorFuelUnitOfMeasure;
    }

    public void setMotorFuelUnitOfMeasure(String motorFuelUnitOfMeasure)
    {
        this.motorFuelUnitOfMeasure = motorFuelUnitOfMeasure;
    }

    public BigDecimal getMotorFuelQuantity()
    {
        return motorFuelQuantity;
    }

    public void setMotorFuelQuantity(BigDecimal motorFuelQuantity)
    {
        this.motorFuelQuantity = motorFuelQuantity;
    }

    public void setMotorFuelQuantity(String motorFuelQuantity)
    {
        this.motorFuelQuantity = convertStringToBigDecimal(motorFuelQuantity);
    }

    public KualiDecimal getMotorFuelSaleAmount()
    {
        return motorFuelSaleAmount;
    }

    public void setMotorFuelSaleAmount(KualiDecimal motorFuelSaleAmount)
    {
        this.motorFuelSaleAmount = motorFuelSaleAmount;
    }

    public void setMotorFuelSaleAmount(String motorFuelSaleAmount)
    {
        this.motorFuelSaleAmount = convertStringToKualiDecimal(motorFuelSaleAmount);
    }

    public Integer getOdomoterReading()
    {
        return odomoterReading;
    }

    public void setOdomoterReading(Integer odomoterReading)
    {
        this.odomoterReading = odomoterReading;
    }

    public String getVehicleNumber()
    {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber)
    {
        this.vehicleNumber = vehicleNumber;
    }

    public String getDriverNumber()
    {
        return driverNumber;
    }

    public void setDriverNumber(String driverNumber)
    {
        this.driverNumber = driverNumber;
    }

    public String getMagneticStripeProductTypeCode()
    {
        return magneticStripeProductTypeCode;
    }

    public void setMagneticStripeProductTypeCode(String magneticStripeProductTypeCode)
    {
        this.magneticStripeProductTypeCode = magneticStripeProductTypeCode;
    }

    public KualiDecimal getCouponDiscountAmount()
    {
        return couponDiscountAmount;
    }

    public void setCouponDiscountAmount(KualiDecimal couponDiscountAmount)
    {
        this.couponDiscountAmount = couponDiscountAmount;
    }

    public void setCouponDiscountAmount(String couponDiscountAmount)
    {
        this.couponDiscountAmount = convertStringToKualiDecimal(couponDiscountAmount);
    }

    public KualiDecimal getTaxExemptAmount()
    {
        return taxExemptAmount;
    }

    public void setTaxExemptAmount(KualiDecimal taxExemptAmount)
    {
        this.taxExemptAmount = taxExemptAmount;
    }

    public void setTaxExemptAmount(String taxExemptAmount)
    {
        this.taxExemptAmount = convertStringToKualiDecimal(taxExemptAmount);
    }

    public KualiDecimal getTaxAmount1()
    {
        return taxAmount1;
    }

    public void setTaxAmount1(KualiDecimal taxAmount1)
    {
        this.taxAmount1 = taxAmount1;
    }

    public void setTaxAmount1(String taxAmount1)
    {
        this.taxAmount1 = convertStringToKualiDecimal(taxAmount1);
    }

    public KualiDecimal getTaxAmount2()
    {
        return taxAmount2;
    }

    public void setTaxAmount2(KualiDecimal taxAmount2)
    {
        this.taxAmount2 = taxAmount2;
    }

    public void setTaxAmount2(String taxAmount2)
    {
        this.taxAmount2 = convertStringToKualiDecimal(taxAmount2);
    }

    public String getFiller3()
    {
        return filler3;
    }

    public void setFiller3(String filler3)
    {
        this.filler3 = filler3;
    }

    /**
     * Returns the BusinessObjectStringParserFieldUtils for addendum type 6 records.
     *
     * @return instance of MasterCardTransactionDetailAddendum6FieldUtil
     * @see MasterCardTransactionDetailFieldUtil.getFieldUtil()
     */
    @Override
    protected BusinessObjectStringParserFieldUtils getFieldUtil()
    {
        if (mctdFieldUtil == null)
        {
            mctdFieldUtil = new MasterCardTransactionDetailAddendum6FieldUtil();
        }
        return mctdFieldUtil;
    }

}
