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
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailAddendum3FieldUtil;

/**
 * Adds the fields needed on the record type 5000 addendum 3 (Lodging) records.
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendum3 extends MasterCardTransactionDetailAddendumBase
{
	private static final long serialVersionUID = 1L;

	private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendum3.class);

    protected String filler1;
    protected String filler2;
    protected Date arrivalDate;
    protected Date departureDate;
    protected String folioNumber;
    protected String propertyPhoneNumber;
    protected String customerServiceNumber;
    protected KualiDecimal roomRate;
    protected KualiDecimal roomTax;
    protected String programCode;
    protected KualiDecimal telephoneCharges;
    protected KualiDecimal roomService;
    protected KualiDecimal barCharges;
    protected KualiDecimal giftShopCharges;
    protected KualiDecimal laundryCharges;
    protected String otherServicesCode;
    protected KualiDecimal otherServicesCharges;
    protected String billingAdjustmentIndicator;
    protected KualiDecimal billingAdjustmentAmount;
    protected String filler3;

    /**
     * Constructor creates a new MasterCardTransactionDetail
     */
    public MasterCardTransactionDetailAddendum3()
    {
        super();

        this.roomRate = new KualiDecimal(0);
        this.roomTax = new KualiDecimal(0);
        this.telephoneCharges = new KualiDecimal(0);
        this.roomService = new KualiDecimal(0);
        this.barCharges = new KualiDecimal(0);
        this.giftShopCharges = new KualiDecimal(0);
        this.laundryCharges = new KualiDecimal(0);
        this.otherServicesCharges = new KualiDecimal(0);
        this.billingAdjustmentAmount = new KualiDecimal(0);
    }


    /**
     * Parses inputRecord setting class attributes. Dates and currency are converted into respective java objects.
     *
     * @param inputRecord
     */
    public void parseInput(String inputRecord)
    {
        setArrivalDate(getValue(inputRecord, CuFPPropertyConstants.ARRIVAL_DATE, CuFPPropertyConstants.DEPARTURE_DATE));
        setDepartureDate(getValue(inputRecord, CuFPPropertyConstants.DEPARTURE_DATE, CuFPPropertyConstants.FOLIO_NUMBER));
        folioNumber = getValue(inputRecord, CuFPPropertyConstants.FOLIO_NUMBER, CuFPPropertyConstants.PROPERTY_PHONE_NUMBER);
        propertyPhoneNumber = getValue(inputRecord, CuFPPropertyConstants.PROPERTY_PHONE_NUMBER, CuFPPropertyConstants.CUSTOMER_SERVICE_NUMBER);
        customerServiceNumber = getValue(inputRecord, CuFPPropertyConstants.CUSTOMER_SERVICE_NUMBER, CuFPPropertyConstants.ROOM_RATE);
        setRoomRate(getValue(inputRecord, CuFPPropertyConstants.ROOM_RATE, CuFPPropertyConstants.ROOM_TAX));
        setRoomTax(getValue(inputRecord, CuFPPropertyConstants.ROOM_TAX, CuFPPropertyConstants.PROGRAM_CODE));
        programCode = getValue(inputRecord, CuFPPropertyConstants.PROGRAM_CODE, CuFPPropertyConstants.TELEPHONE_CHARGES);
        setTelephoneCharges(getValue(inputRecord, CuFPPropertyConstants.TELEPHONE_CHARGES, CuFPPropertyConstants.ROOM_SERVICE));
        setRoomService(getValue(inputRecord, CuFPPropertyConstants.ROOM_SERVICE, CuFPPropertyConstants.BAR_CHARGES));
        setBarCharges(getValue(inputRecord, CuFPPropertyConstants.BAR_CHARGES, CuFPPropertyConstants.GIFT_SHOP_CHARGES));
        setGiftShopCharges(getValue(inputRecord, CuFPPropertyConstants.GIFT_SHOP_CHARGES, CuFPPropertyConstants.LAUNDRY_CHARGES));
        setLaundryCharges(getValue(inputRecord, CuFPPropertyConstants.LAUNDRY_CHARGES, CuFPPropertyConstants.OTHER_SERVICES_CODE));
        otherServicesCode = getValue(inputRecord, CuFPPropertyConstants.OTHER_SERVICES_CODE, CuFPPropertyConstants.OTHER_SERVICES_CHARGES);
        setOtherServicesCharges(getValue(inputRecord, CuFPPropertyConstants.OTHER_SERVICES_CHARGES, CuFPPropertyConstants.BILLING_ADJUSTMENT_IND));
        billingAdjustmentIndicator = getValue(inputRecord, CuFPPropertyConstants.BILLING_ADJUSTMENT_IND, CuFPPropertyConstants.BILLING_ADJUSTMENT_AMOUNT);
        setBillingAdjustmentAmount(getValue(inputRecord, CuFPPropertyConstants.BILLING_ADJUSTMENT_AMOUNT, CuFPPropertyConstants.FILLER3));
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

    public Date getArrivalDate()
    {
        return arrivalDate;
    }

    public String getArrivalDateString()
    {
        return getDateString(arrivalDate);
    }

    public void setArrivalDate(Date arrivalDate)
    {
        this.arrivalDate = arrivalDate;
    }

    public void setArrivalDate(String arrivalDate)
    {
        try
        {
            this.arrivalDate = inputDateFormatter.parse(arrivalDate);
        }
        catch (ParseException e)
        {
            LOG.warn("could not parse arrival date " + arrivalDate + ": " + e);
        }
    }

    public Date getDepartureDate()
    {
        return departureDate;
    }

    public String getDepartureDateString()
    {
        return getDateString(departureDate);
    }

    public void setDepartureDate(Date departureDate)
    {
        this.departureDate = departureDate;
    }

    public void setDepartureDate(String departureDate)
    {
        try
        {
            this.departureDate = inputDateFormatter.parse(departureDate);
        }
        catch (ParseException e)
        {
            LOG.warn("could not parse departure date " + departureDate + ": " + e);
        }
    }

    public String getFolioNumber()
    {
        return folioNumber;
    }

    public void setFolioNumber(String folioNumber)
    {
        this.folioNumber = folioNumber;
    }

    public String getCustomerServiceNumber()
    {
        return customerServiceNumber;
    }

    public void setCustomerServiceNumber(String customerServiceNumber)
    {
        this.customerServiceNumber = customerServiceNumber;
    }

    public KualiDecimal getRoomRate()
    {
        return roomRate;
    }

    public void setRoomRate(KualiDecimal roomRate)
    {
        this.roomRate = roomRate;
    }

    public void setRoomRate(String roomRate)
    {
        this.roomRate = convertStringToKualiDecimal(roomRate);
    }

    public KualiDecimal getRoomTax()
    {
        return roomTax;
    }

    public void setRoomTax(KualiDecimal roomTax)
    {
        this.roomTax = roomTax;
    }

    public void setRoomTax(String roomTax)
    {
        this.roomTax = convertStringToKualiDecimal(roomTax);
    }

    public String getProgramCode()
    {
        return programCode;
    }

    public void setProgramCode(String programCode)
    {
        this.programCode = programCode;
    }

    public KualiDecimal getTelephoneCharges()
    {
        return telephoneCharges;
    }

    public void setTelephoneCharges(KualiDecimal telephoneCharges)
    {
        this.telephoneCharges = telephoneCharges;
    }

    public void setTelephoneCharges(String telephoneCharges)
    {
        this.telephoneCharges = convertStringToKualiDecimal(telephoneCharges);
    }

    public KualiDecimal getRoomService()
    {
        return roomService;
    }

    public void setRoomService(KualiDecimal roomService)
    {
        this.roomService = roomService;
    }

    public void setRoomService(String roomService)
    {
        this.roomService = convertStringToKualiDecimal(roomService);
    }

    public KualiDecimal getBarCharges()
    {
        return barCharges;
    }

    public void setBarCharges(KualiDecimal barCharges)
    {
        this.barCharges = barCharges;
    }

    public void setBarCharges(String barCharges)
    {
        this.barCharges = convertStringToKualiDecimal(barCharges);
    }

    public KualiDecimal getGiftShopCharges()
    {
        return giftShopCharges;
    }

    public void setGiftShopCharges(KualiDecimal giftShopCharges)
    {
        this.giftShopCharges = giftShopCharges;
    }

    public void setGiftShopCharges(String giftShopCharges)
    {
        this.giftShopCharges = convertStringToKualiDecimal(giftShopCharges);
    }

    public KualiDecimal getLaundryCharges()
    {
        return laundryCharges;
    }

    public void setLaundryCharges(KualiDecimal laundryCharges)
    {
        this.laundryCharges = laundryCharges;
    }

    public void setLaundryCharges(String laundryCharges)
    {
        this.laundryCharges = convertStringToKualiDecimal(laundryCharges);
    }

    public String getOtherServicesCode()
    {
        return otherServicesCode;
    }

    public void setOtherServicesCode(String otherServicesCode)
    {
        this.otherServicesCode = otherServicesCode;
    }

    public KualiDecimal getOtherServicesCharges()
    {
        return otherServicesCharges;
    }

    public void setOtherServicesCharges(KualiDecimal otherServicesCharges)
    {
        this.otherServicesCharges = otherServicesCharges;
    }

    public void setOtherServicesCharges(String otherServicesCharges)
    {
        this.otherServicesCharges = convertStringToKualiDecimal(otherServicesCharges);
    }

    public String getBillingAdjustmentIndicator()
    {
        return billingAdjustmentIndicator;
    }

    public void setBillingAdjustmentIndicator(String billingAdjustmentIndicator)
    {
        this.billingAdjustmentIndicator = billingAdjustmentIndicator;
    }

    public KualiDecimal getBillingAdjustmentAmount()
    {
        return billingAdjustmentAmount;
    }

    public void setBillingAdjustmentAmount(KualiDecimal billingAdjustmentAmount)
    {
        this.billingAdjustmentAmount = billingAdjustmentAmount;
    }

    public void setBillingAdjustmentAmount(String billingAdjustmentAmount)
    {
        this.billingAdjustmentAmount = convertStringToKualiDecimal(billingAdjustmentAmount);
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
     * Returns the BusinessObjectStringParserFieldUtils for addendum type 3 records.
     *
     * @return instance of MasterCardTransactionDetailAddendum3FieldUtil
     * @see MasterCardTransactionDetailFieldUtil.getFieldUtil()
     */
    @Override
    protected BusinessObjectStringParserFieldUtils getFieldUtil()
    {
        if (mctdFieldUtil == null)
        {
            mctdFieldUtil = new MasterCardTransactionDetailAddendum3FieldUtil();
        }
        return mctdFieldUtil;
    }

    public String getPropertyPhoneNumber()
    {
        return propertyPhoneNumber;
    }

    public void setPropertyPhoneNumber(String propertyPhoneNumber)
    {
        this.propertyPhoneNumber = propertyPhoneNumber;
    }

}
