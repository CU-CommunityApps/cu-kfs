/*
 * Copyright 2006 The Kuali Foundation
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

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;

/**
 * This class is used to represent a procurement card transaction detail business object.
 */
public class ProcurementCardTransactionDetailExtension extends PersistableBusinessObjectExtensionBase {

	private static final long serialVersionUID = 1L;
	private String documentNumber;
    private Integer financialDocumentTransactionLineNumber;
    private String customerCode;
    
    // list of pcard data
    private List<ProcurementCardDetail> pcardDetails;
    private List<ProcurementCardUserAmountDetail> pcardUserAmountDetails;
    private List<PassengerTransportDetail> passengerTransportDetails;
    private List<PassengerTransportLegDetail> passengerTransportLegDetails;
    private List<LodgingDetail> lodgingDetails;
    private List<RentalCarDetail> rentalCarDetails;
    private List<ProcurementCardGenericDetail> pcardGenericDetails;
    private List<FuelDetail> fuelDetails;
    private List<NonFuelDetail> nonFuelDetails;
    
    public ProcurementCardTransactionDetailExtension()
    {
        super();
        
        pcardDetails = new ArrayList<ProcurementCardDetail>();
        pcardUserAmountDetails = new ArrayList<ProcurementCardUserAmountDetail>();
        passengerTransportDetails = new ArrayList<PassengerTransportDetail>();
        passengerTransportLegDetails = new ArrayList<PassengerTransportLegDetail>();
        lodgingDetails = new ArrayList<LodgingDetail>();
        rentalCarDetails = new ArrayList<RentalCarDetail>();
        pcardGenericDetails = new ArrayList<ProcurementCardGenericDetail>();
        fuelDetails = new ArrayList<FuelDetail>();
        nonFuelDetails = new ArrayList<NonFuelDetail>();
    }
    
    public String getDocumentNumber()
    {
        return documentNumber;
    }
    public void setDocumentNumber(String documentNumber)
    {
        this.documentNumber = documentNumber;
    }
    public Integer getFinancialDocumentTransactionLineNumber()
    {
        return financialDocumentTransactionLineNumber;
    }
    public void setFinancialDocumentTransactionLineNumber(Integer financialDocumentTransactionLineNumber)
    {
        this.financialDocumentTransactionLineNumber = financialDocumentTransactionLineNumber;
    }
    public String getCustomerCode()
    {
        return customerCode;
    }
    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }
    public List<ProcurementCardDetail> getPcardDetails()
    {
        return pcardDetails;
    }
    public void setPcardDetails(List<ProcurementCardDetail> pcardDetails)
    {
        this.pcardDetails = pcardDetails;
    }

    public List<ProcurementCardUserAmountDetail> getPcardUserAmountDetails()
    {
        return pcardUserAmountDetails;
    }

    public void setPcardUserAmountDetails(List<ProcurementCardUserAmountDetail> pcardUserAmountDetails)
    {
        this.pcardUserAmountDetails = pcardUserAmountDetails;
    }

    public List<PassengerTransportDetail> getPassengerTransportDetails()
    {
        return passengerTransportDetails;
    }

    public void setPassengerTransportDetails(List<PassengerTransportDetail> passengerTransportDetails)
    {
        this.passengerTransportDetails = passengerTransportDetails;
    }

    public List<PassengerTransportLegDetail> getPassengerTransportLegDetails()
    {
        return passengerTransportLegDetails;
    }

    public void setPassengerTransportLegDetails(List<PassengerTransportLegDetail> passengerTransportLegDetails)
    {
        this.passengerTransportLegDetails = passengerTransportLegDetails;
    }

    public List<LodgingDetail> getLodgingDetails()
    {
        return lodgingDetails;
    }

    public void setLodgingDetails(List<LodgingDetail> lodgingDetails)
    {
        this.lodgingDetails = lodgingDetails;
    }

    public List<RentalCarDetail> getRentalCarDetails()
    {
        return rentalCarDetails;
    }

    public void setRentalCarDetails(List<RentalCarDetail> rentalCarDetails)
    {
        this.rentalCarDetails = rentalCarDetails;
    }

    public List<ProcurementCardGenericDetail> getPcardGenericDetails()
    {
        return pcardGenericDetails;
    }

    public void setPcardGenericDetails(List<ProcurementCardGenericDetail> pcardGenericDetails)
    {
        this.pcardGenericDetails = pcardGenericDetails;
    }

    public List<FuelDetail> getFuelDetails()
    {
        return fuelDetails;
    }

    public void setFuelDetails(List<FuelDetail> fuelDetails)
    {
        this.fuelDetails = fuelDetails;
    }

    public List<NonFuelDetail> getNonFuelDetails()
    {
        return nonFuelDetails;
    }

    public void setNonFuelDetails(List<NonFuelDetail> nonFuelDetails)
    {
        this.nonFuelDetails = nonFuelDetails;
    }
}
