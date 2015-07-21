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

public class LodgingBase extends PersistableBusinessObjectBase
{
	private static final long serialVersionUID = 1L;
	private Integer lodgingId;
    private Date arrivalDate;
    private Date departureDate;
    private String folioNumber;
    private String propertyPhoneNumber;
    private String customerServiceNumber;
    private KualiDecimal roomRate;
    private KualiDecimal roomTax;
    private String programCode;
    private KualiDecimal telephoneCharges;
    private KualiDecimal roomService;
    private KualiDecimal barCharges;
    private KualiDecimal giftShopCharges;
    private KualiDecimal laundryCharges;
    private String otherServicesCode;
    private KualiDecimal otherServicesCharges;
    private String billingAdjustmentIndicator;
    private KualiDecimal billingAdjustmentAmount;

    public LodgingBase()
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


    public Date getArrivalDate()
    {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate)
    {
        this.arrivalDate = arrivalDate;
    }

    public void setArrivalDate(String arrivalDate)
    {
        if (StringUtils.isNotBlank(arrivalDate))
        {
            this.arrivalDate = (Date) (new SqlDateConverter()).convert(Date.class, arrivalDate);
        }
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
        if (StringUtils.isNotBlank(roomRate))
        {
            this.roomRate = new KualiDecimal(roomRate);
        }
        else
        {
            this.roomRate = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(roomTax))
        {
            this.roomTax = new KualiDecimal(roomTax);
        }
        else
        {
            this.roomTax = KualiDecimal.ZERO;
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
        if (StringUtils.isNotBlank(telephoneCharges))
        {
            this.telephoneCharges = new KualiDecimal(telephoneCharges);
        }
        else
        {
            this.telephoneCharges = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(roomService))
        {
            this.roomService = new KualiDecimal(roomService);
        }
        else
        {
            this.roomService = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(barCharges))
        {
            this.barCharges = new KualiDecimal(barCharges);
        }
        else
        {
            this.barCharges = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(giftShopCharges))
        {
            this.giftShopCharges = new KualiDecimal(giftShopCharges);
        }
        else
        {
            this.giftShopCharges = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(laundryCharges))
        {
            this.laundryCharges = new KualiDecimal(laundryCharges);
        }
        else
        {
            this.laundryCharges = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(otherServicesCharges))
        {
            this.otherServicesCharges = new KualiDecimal(otherServicesCharges);
        }
        else
        {
            this.otherServicesCharges = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(billingAdjustmentAmount))
        {
            this.billingAdjustmentAmount = new KualiDecimal(billingAdjustmentAmount);
        }
        else
        {
            this.billingAdjustmentAmount = KualiDecimal.ZERO;
        }
    }

    public String getPropertyPhoneNumber()
    {
        return propertyPhoneNumber;
    }

    public void setPropertyPhoneNumber(String propertyPhoneNumber)
    {
        this.propertyPhoneNumber = propertyPhoneNumber;
    }

    public Integer getLodgingId()
    {
        return lodgingId;
    }

    public void setLodgingId(Integer lodgingId)
    {
        this.lodgingId = lodgingId;
    }

}
