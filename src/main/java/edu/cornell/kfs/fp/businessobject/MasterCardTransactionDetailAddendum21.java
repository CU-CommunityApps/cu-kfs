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
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailAddendum21FieldUtil;

/**
 * Adds the fields needed on the record type 5000 addendum 21 (Passenger Transport Leg) records.
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendum21 extends MasterCardTransactionDetailAddendumBase
{
	private static final long serialVersionUID = 1L;

	private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendum21.class);

    protected String filler1;
    protected String filler2;
    protected String tripLegNumber;
    protected String carrierCode;
    protected String serviceClass;
    protected String stopOverCode;
    protected String cityOfOrigin;
    protected String conjunctionTicket;
    protected Date travelDate;
    protected String exchangeTicket;
    protected String couponNumber;
    protected String cityOfDestination;
    protected String fareBaseCode;
    protected String flightNumber;
    protected String departureTime;
    protected String departureTimeSegment;
    protected String arrivalTime;
    protected String arrivalTimeSegment;
    protected KualiDecimal fare;
    protected KualiDecimal fee;
    protected KualiDecimal taxes;
    protected String endorsements;
    protected String filler3;

    /**
     * Constructor creates a new MasterCardTransactionDetail
     */
    public MasterCardTransactionDetailAddendum21()
    {
        super();

        this.fare = new KualiDecimal(0);
        this.fee = new KualiDecimal(0);
        this.taxes = new KualiDecimal(0);
    }


    /**
     * Parses inputRecord setting class attributes. Dates and currency are converted into respective java objects.
     *
     * @param inputRecord
     */
    public void parseInput(String inputRecord)
    {
        tripLegNumber = getValue(inputRecord, CuFPPropertyConstants.TRIP_LEG_NUMBER, CuFPPropertyConstants.CARRIER_CODE);
        carrierCode = getValue(inputRecord, CuFPPropertyConstants.CARRIER_CODE, CuFPPropertyConstants.SERVICE_CLASS);
        serviceClass = getValue(inputRecord, CuFPPropertyConstants.SERVICE_CLASS, CuFPPropertyConstants.STOP_OVER_CODE);
        stopOverCode = getValue(inputRecord, CuFPPropertyConstants.STOP_OVER_CODE, CuFPPropertyConstants.CITY_OF_ORIGIN);
        cityOfOrigin = getValue(inputRecord, CuFPPropertyConstants.CITY_OF_ORIGIN, CuFPPropertyConstants.CONJUNCTION_TICKET);
        conjunctionTicket = getValue(inputRecord, CuFPPropertyConstants.CONJUNCTION_TICKET, CuFPPropertyConstants.TRAVEL_DATE);
        setTravelDate(getValue(inputRecord, CuFPPropertyConstants.TRAVEL_DATE, CuFPPropertyConstants.EXCHANGE_TICKET));
        exchangeTicket = getValue(inputRecord, CuFPPropertyConstants.EXCHANGE_TICKET, CuFPPropertyConstants.COUPON_NUMBER);
        couponNumber = getValue(inputRecord, CuFPPropertyConstants.COUPON_NUMBER, CuFPPropertyConstants.CITY_OF_DESTINATION);
        cityOfDestination = getValue(inputRecord, CuFPPropertyConstants.CITY_OF_DESTINATION, CuFPPropertyConstants.FARE_BASE_CODE);
        fareBaseCode = getValue(inputRecord, CuFPPropertyConstants.FARE_BASE_CODE, CuFPPropertyConstants.FLIGHT_NUMBER);
        flightNumber = getValue(inputRecord, CuFPPropertyConstants.FLIGHT_NUMBER, CuFPPropertyConstants.DEPARTURE_TIME);
        departureTime = getValue(inputRecord, CuFPPropertyConstants.DEPARTURE_TIME, CuFPPropertyConstants.DEPARTURE_TIME_SEGMENT);
        departureTimeSegment = getValue(inputRecord, CuFPPropertyConstants.DEPARTURE_TIME_SEGMENT, CuFPPropertyConstants.ARRIVAL_TIME);
        arrivalTime = getValue(inputRecord, CuFPPropertyConstants.ARRIVAL_TIME, CuFPPropertyConstants.ARRIVAL_TIME_SEGMENT);
        arrivalTimeSegment = getValue(inputRecord, CuFPPropertyConstants.ARRIVAL_TIME_SEGMENT, CuFPPropertyConstants.FARE);
        setFare(getValue(inputRecord, CuFPPropertyConstants.FARE, CuFPPropertyConstants.FEE));
        setFee(getValue(inputRecord, CuFPPropertyConstants.FEE, CuFPPropertyConstants.TAXES));
        setTaxes(getValue(inputRecord, CuFPPropertyConstants.TAXES, CuFPPropertyConstants.ENDORSEMENTS));
        endorsements = getValue(inputRecord, CuFPPropertyConstants.ENDORSEMENTS, CuFPPropertyConstants.FILLER3);
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

    public String getTripLegNumber()
    {
        return tripLegNumber;
    }

    public void setTripLegNumber(String tripLegNumber)
    {
        this.tripLegNumber = tripLegNumber;
    }

    public String getCarrierCode()
    {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode)
    {
        this.carrierCode = carrierCode;
    }

    public String getServiceClass()
    {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass)
    {
        this.serviceClass = serviceClass;
    }

    public String getStopOverCode()
    {
        return stopOverCode;
    }

    public void setStopOverCode(String stopOverCode)
    {
        this.stopOverCode = stopOverCode;
    }

    public String getCityOfOrigin()
    {
        return cityOfOrigin;
    }

    public void setCityOfOrigin(String cityOfOrigin)
    {
        this.cityOfOrigin = cityOfOrigin;
    }

    public String getConjunctionTicket()
    {
        return conjunctionTicket;
    }

    public void setConjunctionTicket(String conjunctionTicket)
    {
        this.conjunctionTicket = conjunctionTicket;
    }

    public Date getTravelDate()
    {
        return travelDate;
    }

    public String getTravelDateString()
    {
        return getDateString(travelDate);
    }

    public void setTravelDate(Date travelDate)
    {
        this.travelDate = travelDate;
    }

    public void setTravelDate(String travelDate)
    {
        try
        {
            this.travelDate = inputDateFormatter.parse(travelDate);
        }
        catch (ParseException e)
        {
            LOG.warn("could not parse travel date " + travelDate + ": " + e);
        }
    }

    public String getExchangeTicket()
    {
        return exchangeTicket;
    }

    public void setExchangeTicket(String exchangeTicket)
    {
        this.exchangeTicket = exchangeTicket;
    }

    public String getCouponNumber()
    {
        return couponNumber;
    }

    public void setCouponNumber(String couponNumber)
    {
        this.couponNumber = couponNumber;
    }

    public String getCityOfDestination()
    {
        return cityOfDestination;
    }

    public void setCityOfDestination(String cityOfDestination)
    {
        this.cityOfDestination = cityOfDestination;
    }

    public String getFareBaseCode()
    {
        return fareBaseCode;
    }

    public void setFareBaseCode(String fareBaseCode)
    {
        this.fareBaseCode = fareBaseCode;
    }

    public String getFlightNumber()
    {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber)
    {
        this.flightNumber = flightNumber;
    }

    public String getDepartureTime()
    {
        return departureTime;
    }

    public void setDepartureTime(String departureTime)
    {
        this.departureTime = departureTime;
    }

    public String getDepartureTimeSegment()
    {
        return departureTimeSegment;
    }

    public void setDepartureTimeSegment(String departureTimeSegment)
    {
        this.departureTimeSegment = departureTimeSegment;
    }

    public String getArrivalTime()
    {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime)
    {
        this.arrivalTime = arrivalTime;
    }

    public String getArrivalTimeSegment()
    {
        return arrivalTimeSegment;
    }

    public void setArrivalTimeSegment(String arrivalTimeSegment)
    {
        this.arrivalTimeSegment = arrivalTimeSegment;
    }

    public KualiDecimal getFare()
    {
        return fare;
    }

    public void setFare(KualiDecimal fare)
    {
        this.fare = fare;
    }

    public void setFare(String fare)
    {
        this.fare = convertStringToKualiDecimal(fare);
    }

    public KualiDecimal getFee()
    {
        return fee;
    }

    public void setFee(KualiDecimal fee)
    {
        this.fee = fee;
    }

    public void setFee(String fee)
    {
        this.fee = convertStringToKualiDecimal(fee);
    }

    public KualiDecimal getTaxes()
    {
        return taxes;
    }

    public void setTaxes(KualiDecimal taxes)
    {
        this.taxes = taxes;
    }

    public void setTaxes(String taxes)
    {
        this.taxes = convertStringToKualiDecimal(taxes);
    }

    public String getEndorsements()
    {
        return endorsements;
    }

    public void setEndorsements(String endorsements)
    {
        this.endorsements = endorsements;
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
     * Returns the BusinessObjectStringParserFieldUtils for addendum type 21 records.
     *
     * @return instance of MasterCardTransactionDetailAddendum21FieldUtil
     * @see MasterCardTransactionDetailFieldUtil.getFieldUtil()
     */
    @Override
    protected BusinessObjectStringParserFieldUtils getFieldUtil()
    {
        if (mctdFieldUtil == null)
        {
            mctdFieldUtil = new MasterCardTransactionDetailAddendum21FieldUtil();
        }
        return mctdFieldUtil;
    }

}
