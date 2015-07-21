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


public class PassengerTransportBase extends PersistableBusinessObjectBase
{

	private static final long serialVersionUID = 1L;
    private Integer passengerTransportId;
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

    public PassengerTransportBase()
    {
        super();

        this.totalFare = new KualiDecimal(0);
        this.totalFees = new KualiDecimal(0);
        this.totalTaxes = new KualiDecimal(0);
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

    public void setDepartureDate(Date departureDate)
    {
        this.departureDate = departureDate;
    }

    public void setDepartureDate(String departureDate)
    {
        if (StringUtils.isNotBlank(departureDate))
        {
            this.departureDate = (Date) (new SqlDateConverter()).convert(Date.class, departureDate);
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

    public void setIssueDate(Date issueDate)
    {
        this.issueDate = issueDate;
    }

    public void setIssueDate(String issueDate)
    {
        if (StringUtils.isNotBlank(issueDate))
        {
            this.issueDate = (Date) (new SqlDateConverter()).convert(Date.class, issueDate);
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
        if (StringUtils.isNotBlank(totalFare))
        {
            this.totalFare = new KualiDecimal(totalFare);
        }
        else
        {
            this.totalFare = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(totalFees))
        {
            this.totalFees = new KualiDecimal(totalFees);
        }
        else
        {
            this.totalFees = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(totalTaxes))
        {
            this.totalTaxes = new KualiDecimal(totalTaxes);
        }
        else
        {
            this.totalTaxes = KualiDecimal.ZERO;
        }
    }

    public Integer getPassengerTransportId()
    {
        return passengerTransportId;
    }

    public void setPassengerTransportId(Integer passengerTransportId)
    {
        this.passengerTransportId = passengerTransportId;
    }

}
