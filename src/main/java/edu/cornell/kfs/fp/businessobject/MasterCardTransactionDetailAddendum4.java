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

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailAddendum4FieldUtil;

/**
 * Adds the fields needed on the record type 5000 addendum 4 (Rental Car) records.
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendum4 extends MasterCardTransactionDetailAddendumBase
{
    private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendum4.class);

    protected String filler1;
    protected String filler2;
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
    protected String filler3;

    /**
     * Constructor creates a new MasterCardTransactionDetail
     */
    public MasterCardTransactionDetailAddendum4()
    {
        super();

        this.dailyRentalRate = new KualiDecimal(0);
        this.insuranceCharges = new KualiDecimal(0);
        this.adjustedAmount = new KualiDecimal(0);
    }

    /**
     * Parses inputRecord setting class attributes. Dates and currency are converted into respective java objects.
     *
     * @param inputRecord
     */
    public void parseInput(String inputRecord)
    {
        rentalAgreementNumber = getValue(inputRecord, CuFPPropertyConstants.RENTAL_AGREEMENT_NUMBER, CuFPPropertyConstants.RENTER_NAME);
        renterName = getValue(inputRecord, CuFPPropertyConstants.RENTER_NAME, CuFPPropertyConstants.RENTAL_RETURN_CITY);
        rentalReturnCity = getValue(inputRecord, CuFPPropertyConstants.RENTAL_RETURN_CITY, CuFPPropertyConstants.RENTAL_RETURN_STATE);
        rentalReturnState = getValue(inputRecord, CuFPPropertyConstants.RENTAL_RETURN_STATE, CuFPPropertyConstants.RENTAL_RETURN_COUNTRY);
        rentalReturnCountry = getValue(inputRecord, CuFPPropertyConstants.RENTAL_RETURN_COUNTRY, CuFPPropertyConstants.RENTAL_RETURN_DATE);
        setRentalReturnDate(getValue(inputRecord, CuFPPropertyConstants.RENTAL_RETURN_DATE, CuFPPropertyConstants.RETURN_LOCATION_ID));
        returnLocationId = getValue(inputRecord, CuFPPropertyConstants.RETURN_LOCATION_ID, CuFPPropertyConstants.CUSTOMER_SERVICE_NUMBER);
        customerServiceNumber = getValue(inputRecord, CuFPPropertyConstants.CUSTOMER_SERVICE_NUMBER, CuFPPropertyConstants.RENTAL_CLASS);
        rentalClass = getValue(inputRecord, CuFPPropertyConstants.RENTAL_CLASS, CuFPPropertyConstants.DAILY_RENTAL_RATE);
        setDailyRentalRate(getValue(inputRecord, CuFPPropertyConstants.DAILY_RENTAL_RATE, CuFPPropertyConstants.RATE_PER_MILE));
        setRatePerMile(getValue(inputRecord, CuFPPropertyConstants.RATE_PER_MILE, CuFPPropertyConstants.TOTAL_MILES));
        totalMiles = new Integer(getValue(inputRecord, CuFPPropertyConstants.TOTAL_MILES, CuFPPropertyConstants.MAX_FREE_MILES));
        maxFreeMiles = new Integer(getValue(inputRecord, CuFPPropertyConstants.MAX_FREE_MILES, CuFPPropertyConstants.INSURANCE_IND));
        insuranceIndicator = getValue(inputRecord, CuFPPropertyConstants.INSURANCE_IND, CuFPPropertyConstants.INSURANCE_CHARGES);
        setInsuranceCharges(getValue(inputRecord, CuFPPropertyConstants.INSURANCE_CHARGES, CuFPPropertyConstants.ADJUSTED_AMOUNT_IND));
        adjustedAmountIndicator = getValue(inputRecord, CuFPPropertyConstants.ADJUSTED_AMOUNT_IND, CuFPPropertyConstants.ADJUSTED_AMOUNT);
        setAdjustedAmount(getValue(inputRecord, CuFPPropertyConstants.ADJUSTED_AMOUNT, CuFPPropertyConstants.PROGRAM_CODE));
        programCode = getValue(inputRecord, CuFPPropertyConstants.PROGRAM_CODE, CuFPPropertyConstants.CHECKOUT_DATE);
        setCheckoutDate(getValue(inputRecord, CuFPPropertyConstants.CHECKOUT_DATE, CuFPPropertyConstants.FILLER3));
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

    public String getRentalReturnDateString()
    {
        return getDateString(rentalReturnDate);
    }

    public void setRentalReturnDate(Date rentalReturnDate)
    {
        this.rentalReturnDate = rentalReturnDate;
    }

    public void setRentalReturnDate(String rentalReturnDate)
    {
        try
        {
            this.rentalReturnDate = inputDateFormatter.parse(rentalReturnDate);
        }
        catch (ParseException e)
        {
            LOG.warn("could not parse rental return date " + rentalReturnDate + ": " + e);
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
        this.dailyRentalRate = convertStringToKualiDecimal(dailyRentalRate);
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
        this.ratePerMile = convertStringToKualiDecimal(ratePerMile);
    }

    public Integer getTotalMiles()
    {
        return totalMiles;
    }

    public void setTotalMiles(Integer totalMiles)
    {
        this.totalMiles = totalMiles;
    }

    public Integer getMaxFreeMiles()
    {
        return maxFreeMiles;
    }

    public void setMaxFreeMiles(Integer maxFreeMiles)
    {
        this.maxFreeMiles = maxFreeMiles;
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
        this.insuranceCharges = convertStringToKualiDecimal(insuranceCharges);
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
        this.adjustedAmount = convertStringToKualiDecimal(adjustedAmount);
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

    public String getCheckoutDateString()
    {
        return getDateString(checkoutDate);
    }

    public void setCheckoutDate(Date checkoutDate)
    {
        this.checkoutDate = checkoutDate;
    }

    public void setCheckoutDate(String checkoutDate)
    {
        try
        {
            this.checkoutDate = inputDateFormatter.parse(checkoutDate);
        }
        catch (ParseException e)
        {
            LOG.warn("could not parse checkout date " + checkoutDate + ": " + e);
        }
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
     * Returns the BusinessObjectStringParserFieldUtils for addendum type 4 records.
     *
     * @return instance of MasterCardTransactionDetailAddendum4FieldUtil
     * @see MasterCardTransactionDetailFieldUtil.getFieldUtil()
     */
    @Override
    protected BusinessObjectStringParserFieldUtils getFieldUtil()
    {
        if (mctdFieldUtil == null)
        {
            mctdFieldUtil = new MasterCardTransactionDetailAddendum4FieldUtil();
        }
        return mctdFieldUtil;
    }

}
