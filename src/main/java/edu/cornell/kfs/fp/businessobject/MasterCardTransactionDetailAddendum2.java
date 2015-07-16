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
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailAddendum2FieldUtil;

/**
 * Adds the fields needed on the record type 5000 addendum 2 (Passenger Transport) records.
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendum2 extends MasterCardTransactionDetailAddendumBase
{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendum2.class);

    protected String filler1;
    protected String filler2;
    protected String passengerName;
    protected Date departureDate;
    protected String airportCode;
    protected String travelAgencyCode;
    protected String travelAgencyName;
    protected String ticketNumber;
    protected String customerCode;
    protected Date issueDate;
    protected String issuingCarrier;
    protected KualiDecimal totalFare;
    protected KualiDecimal totalFees;
    protected KualiDecimal totalTaxes;
    protected String filler3;

    /**
     * Constructor creates a new MasterCardTransactionDetail
     */
    public MasterCardTransactionDetailAddendum2()
    {
        super();

        this.totalFare = new KualiDecimal(0);
        this.totalFees = new KualiDecimal(0);
        this.totalTaxes = new KualiDecimal(0);
    }


    /**
     * Parses inputRecord setting class attributes. Dates and currency are converted into respective java objects.
     *
     * @param inputRecord
     */
    public void parseInput(String inputRecord)
    {
        passengerName = getValue(inputRecord, CuFPPropertyConstants.PASSENGER_NAME, CuFPPropertyConstants.DEPARTURE_DATE);
        setDepartureDate(getValue(inputRecord, CuFPPropertyConstants.DEPARTURE_DATE, CuFPPropertyConstants.AIRPORT_CODE));
        airportCode = getValue(inputRecord, CuFPPropertyConstants.AIRPORT_CODE, CuFPPropertyConstants.TRAVEL_AGENCY_CODE);
        travelAgencyCode = getValue(inputRecord, CuFPPropertyConstants.TRAVEL_AGENCY_CODE, CuFPPropertyConstants.TRAVEL_AGENCY_NAME);
        travelAgencyName = getValue(inputRecord, CuFPPropertyConstants.TRAVEL_AGENCY_NAME, CuFPPropertyConstants.TICKET_NUMBER);
        ticketNumber = getValue(inputRecord, CuFPPropertyConstants.TICKET_NUMBER, CuFPPropertyConstants.CUSTOMER_CODE);
        customerCode = getValue(inputRecord, CuFPPropertyConstants.CUSTOMER_CODE, CuFPPropertyConstants.ISSUE_DATE);
        setIssueDate(getValue(inputRecord, CuFPPropertyConstants.ISSUE_DATE, CuFPPropertyConstants.ISSUING_CARRIER));
        issuingCarrier = getValue(inputRecord, CuFPPropertyConstants.ISSUING_CARRIER, CuFPPropertyConstants.TOTAL_FARE);
        setTotalFare(getValue(inputRecord, CuFPPropertyConstants.TOTAL_FARE, CuFPPropertyConstants.TOTAL_FEES));
        setTotalFees(getValue(inputRecord, CuFPPropertyConstants.TOTAL_FEES, CuFPPropertyConstants.TOTAL_TAXES));
        setTotalTaxes(getValue(inputRecord, CuFPPropertyConstants.TOTAL_TAXES, CuFPPropertyConstants.FILLER3));
    }

    public String getPassengerName()
    {
        return passengerName;
    }

    public void setPassengerName(String passengerName)
    {
        this.passengerName = passengerName;
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

    public String getAirportCode()
    {
        return airportCode;
    }

    public void setAirportCode(String airportCode)
    {
        this.airportCode = airportCode;
    }

    public String getTravelAgencyCode()
    {
        return travelAgencyCode;
    }

    public void setTravelAgencyCode(String travelAgencyCode)
    {
        this.travelAgencyCode = travelAgencyCode;
    }

    public String getTravelAgencyName()
    {
        return travelAgencyName;
    }

    public void setTravelAgencyName(String travelAgencyName)
    {
        this.travelAgencyName = travelAgencyName;
    }

    public String getTicketNumber()
    {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber)
    {
        this.ticketNumber = ticketNumber;
    }

    public String getCustomerCode()
    {
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    public Date getIssueDate()
    {
        return issueDate;
    }

    public String getIssueDateString()
    {
        return getDateString(issueDate);
    }

    public void setIssueDate(Date issueDate)
    {
        this.issueDate = issueDate;
    }

    public void setIssueDate(String issueDate)
    {
        try
        {
            this.issueDate = inputDateFormatter.parse(issueDate);
        }
        catch (ParseException e)
        {
            LOG.warn("could not parse issue date " + issueDate + ": " + e);
        }
    }

    public String getIssuingCarrier()
    {
        return issuingCarrier;
    }

    public void setIssuingCarrier(String issuingCarrier)
    {
        this.issuingCarrier = issuingCarrier;
    }

    public KualiDecimal getTotalFare()
    {
        return totalFare;
    }

    public void setTotalFare(KualiDecimal totalFare)
    {
        this.totalFare = totalFare;
    }

    public void setTotalFare(String totalFare)
    {
        this.totalFare = convertStringToKualiDecimal(totalFare);

    }

    public KualiDecimal getTotalFees()
    {
        return totalFees;
    }

    public void setTotalFees(KualiDecimal totalFees)
    {
        this.totalFees = totalFees;
    }

    public void setTotalFees(String totalFees)
    {
        this.totalFees = convertStringToKualiDecimal(totalFees);

    }

    public KualiDecimal getTotalTaxes()
    {
        return totalTaxes;
    }

    public void setTotalTaxes(KualiDecimal totalTaxes)
    {
        this.totalTaxes = totalTaxes;
    }

    public void setTotalTaxes(String totalTaxes)
    {
        this.totalTaxes = convertStringToKualiDecimal(totalTaxes);
    }

    /**
     * Returns the BusinessObjectStringParserFieldUtils for addendum type 2 records.
     *
     * @return instance of MasterCardTransactionDetailAddendum2FieldUtil
     * @see MasterCardTransactionDetailFieldUtil.getFieldUtil()
     */

    @Override
    protected BusinessObjectStringParserFieldUtils getFieldUtil()
    {
        if (mctdFieldUtil == null)
        {
            mctdFieldUtil = new MasterCardTransactionDetailAddendum2FieldUtil();
        }
        return mctdFieldUtil;
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

    public String getFiller3()
    {
        return filler3;
    }

    public void setFiller3(String filler3)
    {
        this.filler3 = filler3;
    }

}
