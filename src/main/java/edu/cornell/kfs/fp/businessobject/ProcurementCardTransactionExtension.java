/*
 * Copyright 2005-2006 The Kuali Foundation
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
 * This class is used to represent a procurement card transaction business object.
 */
public class ProcurementCardTransactionExtension extends PersistableBusinessObjectExtensionBase
{
	private static final long serialVersionUID = 1L;
	private Integer transactionSequenceRowNumber;
	private String customerCode;
	private List<ProcurementCardRecord> pcardRecords;
	private List<ProcurementCardUserAmountRecord> pcardUserAmountRecords;
	private List<PassengerTransportRecord> passengerTransportRecords;
	private List<PassengerTransportLegRecord> passengerTransportLegRecords;
	private List<LodgingRecord> lodgingRecords;
	private List<RentalCarRecord> rentalCarRecords;
	private List<ProcurementCardGenericRecord> pcardGenericRecords;
	private List<FuelRecord> fuelRecords;
	private List<NonFuelRecord> nonFuelRecords;

	public ProcurementCardTransactionExtension()
	{
		super();

		pcardRecords = new ArrayList<ProcurementCardRecord>();
		pcardUserAmountRecords = new ArrayList<ProcurementCardUserAmountRecord>();
		passengerTransportRecords = new ArrayList<PassengerTransportRecord>();
		passengerTransportLegRecords = new ArrayList<PassengerTransportLegRecord>();
		lodgingRecords = new ArrayList<LodgingRecord>();
		rentalCarRecords = new ArrayList<RentalCarRecord>();
		pcardGenericRecords = new ArrayList<ProcurementCardGenericRecord>();
		fuelRecords = new ArrayList<FuelRecord>();
		nonFuelRecords = new ArrayList<NonFuelRecord>();
	}

	public String getCustomerCode()
	{
		return customerCode;
	}

	public void setCustomerCode(String customerCode)
	{
		this.customerCode = customerCode;
	}

	public List<ProcurementCardRecord> getPcardRecords()
	{
		return pcardRecords;
	}

	public void setPcardRecords(List<ProcurementCardRecord> pcardRecords)
	{
		this.pcardRecords = pcardRecords;
	}

	public Integer getTransactionSequenceRowNumber()
	{
		return transactionSequenceRowNumber;
	}

	public void setTransactionSequenceRowNumber(Integer transactionSequenceRowNumber)
	{
		this.transactionSequenceRowNumber = transactionSequenceRowNumber;
	}

	@Override
	public List buildListOfDeletionAwareLists()
	{
		List managedLists = super.buildListOfDeletionAwareLists();
		managedLists.add(getPcardRecords());
		return managedLists;
	}

	public List<ProcurementCardUserAmountRecord> getPcardUserAmountRecords()
	{
		return pcardUserAmountRecords;
	}

	public void setPcardUserAmountRecords(List<ProcurementCardUserAmountRecord> pcardUserAmountRecords)
	{
		this.pcardUserAmountRecords = pcardUserAmountRecords;
	}

	public List<PassengerTransportRecord> getPassengerTransportRecords()
	{
		return passengerTransportRecords;
	}

	public void setPassengerTransportRecords(List<PassengerTransportRecord> passengerTransportRecords)
	{
		this.passengerTransportRecords = passengerTransportRecords;
	}

	public List<PassengerTransportLegRecord> getPassengerTransportLegRecords()
	{
		return passengerTransportLegRecords;
	}

	public void setPassengerTransportLegRecords(List<PassengerTransportLegRecord> passengerTransportLegRecords)
	{
		this.passengerTransportLegRecords = passengerTransportLegRecords;
	}

	public List<LodgingRecord> getLodgingRecords()
	{
		return lodgingRecords;
	}

	public void setLodgingRecords(List<LodgingRecord> lodgingRecords)
	{
		this.lodgingRecords = lodgingRecords;
	}

	public List<RentalCarRecord> getRentalCarRecords()
	{
		return rentalCarRecords;
	}

	public void setRentalCarRecords(List<RentalCarRecord> rentalCarRecords)
	{
		this.rentalCarRecords = rentalCarRecords;
	}

	public List<ProcurementCardGenericRecord> getPcardGenericRecords()
	{
		return pcardGenericRecords;
	}

	public void setPcardGenericRecords(List<ProcurementCardGenericRecord> pcardGenericRecords)
	{
		this.pcardGenericRecords = pcardGenericRecords;
	}

	public List<FuelRecord> getFuelRecords()
	{
		return fuelRecords;
	}

	public void setFuelRecords(List<FuelRecord> fuelRecords)
	{
		this.fuelRecords = fuelRecords;
	}

	public List<NonFuelRecord> getNonFuelRecords()
	{
		return nonFuelRecords;
	}

	public void setNonFuelRecords(List<NonFuelRecord> nonFuelRecords)
	{
		this.nonFuelRecords = nonFuelRecords;
	}

}
