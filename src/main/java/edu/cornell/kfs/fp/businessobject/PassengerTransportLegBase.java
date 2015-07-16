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

public class PassengerTransportLegBase extends PersistableBusinessObjectBase
{

	private static final long serialVersionUID = 1L;
    private Integer passengerTransportLegId;
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

    public PassengerTransportLegBase()
    {
        super();

        this.fare = new KualiDecimal(0);
        this.fee = new KualiDecimal(0);
        this.taxes = new KualiDecimal(0);
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

    public void setTravelDate(Date travelDate)
    {
        this.travelDate = travelDate;
    }

    public void setTravelDate(String travelDate)
    {
        if (StringUtils.isNotBlank(travelDate))
        {
            this.travelDate = (Date) (new SqlDateConverter()).convert(Date.class, travelDate);
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
        if (StringUtils.isNotBlank(fare))
        {
            this.fare = new KualiDecimal(fare);
        }
        else
        {
            this.fare = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(fee))
        {
            this.fee = new KualiDecimal(fee);
        }
        else
        {
            this.fee = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(taxes))
        {
            this.taxes = new KualiDecimal(taxes);
        }
        else
        {
            this.taxes = KualiDecimal.ZERO;
        }
    }

    public String getEndorsements()
    {
        return endorsements;
    }

    public void setEndorsements(String endorsements)
    {
        this.endorsements = endorsements;
    }

    public Integer getPassengerTransportLegId()
    {
        return passengerTransportLegId;
    }

    public void setPassengerTransportLegId(Integer passengerTransportLegId)
    {
        this.passengerTransportLegId = passengerTransportLegId;
    }

}
