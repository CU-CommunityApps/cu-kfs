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

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

public class FuelBase extends PersistableBusinessObjectBase
{
	private static final long serialVersionUID = 1L;
	private Integer fuelId;
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

    public FuelBase()
    {
        super();

        this.motorFuelUnitPrice = new KualiDecimal(0);
        this.motorFuelSaleAmount = new KualiDecimal(0);
        this.couponDiscountAmount = new KualiDecimal(0);
        this.taxExemptAmount = new KualiDecimal(0);
        this.taxAmount1 = new KualiDecimal(0);
        this.taxAmount2 = new KualiDecimal(0);
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
        if (StringUtils.isNotBlank(motorFuelUnitPrice))
        {
            this.motorFuelUnitPrice = new KualiDecimal(motorFuelUnitPrice);
        }
        else
        {
            this.motorFuelUnitPrice = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(motorFuelQuantity))
        {
            this.motorFuelQuantity = new BigDecimal(motorFuelQuantity);
        }
        else
        {
            this.motorFuelQuantity = BigDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(motorFuelSaleAmount))
        {
            this.motorFuelSaleAmount = new KualiDecimal(motorFuelSaleAmount);
        }
        else
        {
            this.motorFuelSaleAmount = KualiDecimal.ZERO;
        }
    }

    public Integer getOdomoterReading()
    {
        return odomoterReading;
    }

    public void setOdomoterReading(Integer odomoterReading)
    {
        this.odomoterReading = odomoterReading;
    }

    public void setOdomoterReading(String odomoterReading)
    {
        if (StringUtils.isNotBlank(odomoterReading))
        {
            this.odomoterReading = new Integer(odomoterReading);
        }
        else
        {
            this.odomoterReading = new Integer(0);
        }
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
        if (StringUtils.isNotBlank(couponDiscountAmount))
        {
            this.couponDiscountAmount = new KualiDecimal(couponDiscountAmount);
        }
        else
        {
            this.couponDiscountAmount = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(taxExemptAmount))
        {
            this.taxExemptAmount = new KualiDecimal(taxExemptAmount);
        }
        else
        {
            this.taxExemptAmount = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(taxAmount1))
        {
            this.taxAmount1 = new KualiDecimal(taxAmount1);
        }
        else
        {
            this.taxAmount1 = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(taxAmount2))
        {
            this.taxAmount2 = new KualiDecimal(taxAmount2);
        }
        else
        {
            this.taxAmount2 = KualiDecimal.ZERO;
        }
    }

    public Integer getFuelId()
    {
        return fuelId;
    }

    public void setFuelId(Integer fuelId)
    {
        this.fuelId = fuelId;
    }

}
