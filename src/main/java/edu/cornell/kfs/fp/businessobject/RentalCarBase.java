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

import java.sql.Date;

import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.lang.StringUtils;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

public class RentalCarBase extends PersistableBusinessObjectBase
{
	private static final long serialVersionUID = 1L;
    private Integer rentalCarId;
    protected String rentalAgreementNumber;
    protected String renterName;
    protected String rentalReturnCity;
    protected String rentalReturnState;
    protected String rentalReturnCountry;
    protected Date rentalReturnDate;
    protected String returnLocationId;
    protected String customerServiceNumber;
    protected String rentalClass;
    protected KualiDecimal dailyRentalRate;
    protected KualiDecimal ratePerMile;
    protected Integer totalMiles;
    protected Integer maxFreeMiles;
    protected String insuranceIndicator;
    protected KualiDecimal insuranceCharges;
    protected String adjustedAmountIndicator;
    protected KualiDecimal adjustedAmount;
    protected String programCode;
    protected Date checkoutDate;

    public RentalCarBase()
    {
        super();

        this.totalMiles = new Integer(0);
        this.maxFreeMiles = new Integer(0);
        this.dailyRentalRate = new KualiDecimal(0);
        this.ratePerMile = new KualiDecimal(0);
        this.insuranceCharges = new KualiDecimal(0);
        this.adjustedAmount = new KualiDecimal(0);
    }


    public String getRentalAgreementNumber()
    {
        return rentalAgreementNumber;
    }

    public void setRentalAgreementNumber(String rentalAgreementNumber)
    {
        this.rentalAgreementNumber = rentalAgreementNumber;
    }

    public String getRenterName()
    {
        return renterName;
    }

    public void setRenterName(String renterName)
    {
        this.renterName = renterName;
    }

    public String getRentalReturnCity()
    {
        return rentalReturnCity;
    }

    public void setRentalReturnCity(String rentalReturnCity)
    {
        this.rentalReturnCity = rentalReturnCity;
    }

    public String getRentalReturnState()
    {
        return rentalReturnState;
    }

    public void setRentalReturnState(String rentalReturnState)
    {
        this.rentalReturnState = rentalReturnState;
    }

    public String getRentalReturnCountry()
    {
        return rentalReturnCountry;
    }

    public void setRentalReturnCountry(String rentalReturnCountry)
    {
        this.rentalReturnCountry = rentalReturnCountry;
    }

    public Date getRentalReturnDate()
    {
        return rentalReturnDate;
    }

    public void setRentalReturnDate(Date rentalReturnDate)
    {
        this.rentalReturnDate = rentalReturnDate;
    }

    public void setRentalReturnDate(String rentalReturnDate)
    {
        if (StringUtils.isNotBlank(rentalReturnDate))
        {
            this.rentalReturnDate = (Date) (new SqlDateConverter()).convert(Date.class, rentalReturnDate);
        }
    }

    public String getReturnLocationId()
    {
        return returnLocationId;
    }

    public void setReturnLocationId(String returnLocationId)
    {
        this.returnLocationId = returnLocationId;
    }

    public String getCustomerServiceNumber()
    {
        return customerServiceNumber;
    }

    public void setCustomerServiceNumber(String customerServiceNumber)
    {
        this.customerServiceNumber = customerServiceNumber;
    }

    public String getRentalClass()
    {
        return rentalClass;
    }

    public void setRentalClass(String rentalClass)
    {
        this.rentalClass = rentalClass;
    }

    public KualiDecimal getDailyRentalRate()
    {
        return dailyRentalRate;
    }

    public void setDailyRentalRate(KualiDecimal dailyRentalRate)
    {
        this.dailyRentalRate = dailyRentalRate;
    }

    public void setDailyRentalRate(String dailyRentalRate)
    {
        if (StringUtils.isNotBlank(dailyRentalRate))
        {
            this.dailyRentalRate = new KualiDecimal(dailyRentalRate);
        }
        else
        {
            this.dailyRentalRate = KualiDecimal.ZERO;
        }
    }

    public KualiDecimal getRatePerMile()
    {
        return ratePerMile;
    }

    public void setRatePerMile(KualiDecimal ratePerMile)
    {
        this.ratePerMile = ratePerMile;
    }

    public void setRatePerMile(String ratePerMile)
    {
        if (StringUtils.isNotBlank(ratePerMile))
        {
            this.ratePerMile = new KualiDecimal(ratePerMile);
        }
        else
        {
            this.ratePerMile = KualiDecimal.ZERO;
        }
    }

    public Integer getTotalMiles()
    {
        return totalMiles;
    }

    public void setTotalMiles(Integer totalMiles)
    {
        this.totalMiles = totalMiles;
    }

    public void setTotalMiles(String totalMiles)
    {
        if (StringUtils.isNotBlank(totalMiles))
        {
            this.totalMiles = new Integer(totalMiles);
        }
        else
        {
            this.totalMiles = new Integer(0);
        }
    }

    public Integer getMaxFreeMiles()
    {
        return maxFreeMiles;
    }

    public void setMaxFreeMiles(Integer maxFreeMiles)
    {
        this.maxFreeMiles = maxFreeMiles;
    }

    public void setMaxFreeMiles(String maxFreeMiles)
    {
        if (StringUtils.isNotBlank(maxFreeMiles))
        {
            this.maxFreeMiles = new Integer(maxFreeMiles);
        }
        else
        {
            this.maxFreeMiles = new Integer(0);
        }
    }

    public String getInsuranceIndicator()
    {
        return insuranceIndicator;
    }

    public void setInsuranceIndicator(String insuranceIndicator)
    {
        this.insuranceIndicator = insuranceIndicator;
    }

    public KualiDecimal getInsuranceCharges()
    {
        return insuranceCharges;
    }

    public void setInsuranceCharges(KualiDecimal insuranceCharges)
    {
        this.insuranceCharges = insuranceCharges;
    }

    public void setInsuranceCharges(String insuranceCharges)
    {
        if (StringUtils.isNotBlank(insuranceCharges))
        {
            this.insuranceCharges = new KualiDecimal(insuranceCharges);
        }
        else
        {
            this.insuranceCharges = KualiDecimal.ZERO;
        }
    }

    public String getAdjustedAmountIndicator()
    {
        return adjustedAmountIndicator;
    }

    public void setAdjustedAmountIndicator(String adjustedAmountIndicator)
    {
        this.adjustedAmountIndicator = adjustedAmountIndicator;
    }

    public KualiDecimal getAdjustedAmount()
    {
        return adjustedAmount;
    }

    public void setAdjustedAmount(KualiDecimal adjustedAmount)
    {
        this.adjustedAmount = adjustedAmount;
    }

    public void setAdjustedAmount(String adjustedAmount)
    {
        if (StringUtils.isNotBlank(adjustedAmount))
        {
            this.adjustedAmount = new KualiDecimal(adjustedAmount);
        }
        else
        {
            this.adjustedAmount = KualiDecimal.ZERO;
        }
    }

    public String getProgramCode()
    {
        return programCode;
    }

    public void setProgramCode(String programCode)
    {
        this.programCode = programCode;
    }

    public Date getCheckoutDate()
    {
        return checkoutDate;
    }

    public void setCheckoutDate(Date checkoutDate)
    {
        this.checkoutDate = checkoutDate;
    }

    public void setCheckoutDate(String checkoutDate)
    {
        if (StringUtils.isNotBlank(checkoutDate))
        {
            this.checkoutDate = (Date) (new SqlDateConverter()).convert(Date.class, checkoutDate);
        }
    }

    public Integer getRentalCarId()
    {
        return rentalCarId;
    }

    public void setRentalCarId(Integer rentalCarId)
    {
        this.rentalCarId = rentalCarId;
    }

}
