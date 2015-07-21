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
package edu.cornell.kfs.fp.batch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.fp.batch.ProcurementCardInputFileType;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum1;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum11;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum2;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum21;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum3;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum4;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum5;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum6;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum61;
import edu.cornell.kfs.fp.businessobject.ProcurementCardUploadDocument;


/**
 * Generates the Procurement Card Document XML file
 */
public class MasterCardOutputXmlFileType
{
	private static Logger LOG = Logger.getLogger(MasterCardOutputXmlFileType.class);
	
	private String proCardFilename;
	private String defaultNamespace;
	private String xsiNamespace;
	private ProcurementCardInputFileType procurementCardOutputFileType;
	private boolean useFormatter;

	public MasterCardOutputXmlFileType()
	{
		// no-arg constructor
	}
	  
	/**
	 * Creates XML file for procurement card transaction.
	 * 
	 * @param pcTransactionFile name of file to write
	 * @return file name of the output XML file
	 */
	public String writeXmlDocument(List<ProcurementCardUploadDocument> transactionList)
	throws IOException
	{
		Element root = new Element("transactions", defaultNamespace);
		root.addNamespaceDeclaration("xsi", xsiNamespace);
		int transactionCounter = 0;
		
		Document document = new Document(root);
		
		for (ProcurementCardUploadDocument pcUploadDoc : transactionList)
		{
			addTransactionElement(pcUploadDoc, root);
			transactionCounter++;
		}
		
		String proCardFileName = getOutputFilename();
		FileOutputStream fos = null;
		
		try
		{
			fos = new FileOutputStream(proCardFileName);
			
			if (useFormatter)
			{
				Serializer serializer = new Serializer(fos);
				serializer.setIndent(2);
				serializer.write(document);
				serializer.flush();
			}
			else
			{
				fos.write(document.toXML().getBytes());
			}
			LOG.info("Wrote " + proCardFileName + " file with " + transactionCounter + " transaction elements.");
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException e)
				{
					LOG.warn("Failed to close output filestream.");
				}
			}
		}
		
		return proCardFileName;
	}
    
	/**
	 * 
	 */
	public String getProCardFilename()
	{
		return proCardFilename;
	}

	/**
	 * 
	 */
	public void setProCardFilename(String proCardFilename)
	{
		this.proCardFilename = proCardFilename;
	}

	/**
	 * 
	 */
	public String getDefaultNamespace()
	{
		return defaultNamespace;
	}

	/**
	 * 
	 */
	public void setDefaultNamespace(String defaultNamespace)
	{
		this.defaultNamespace = defaultNamespace;
	}

	/**
	 * 
	 */
	public String getXsiNamespace()
	{
		return xsiNamespace;
	}

	/**
	 * 
	 */
	public void setXsiNamespace(String xsiNamespace)
	{
		this.xsiNamespace = xsiNamespace;
	}

	/**
	 * 
	 */
	public ProcurementCardInputFileType getProcurementCardOutputFileType()
	{
		return procurementCardOutputFileType;
	}

	/**
	 * 
	 */
	public void setProcurementCardOutputFileType(ProcurementCardInputFileType procurementCardOutputFileType)
	{
		this.procurementCardOutputFileType = procurementCardOutputFileType;
	}

    /**
	 * 
	 */
	public void setUseFormatter(String useFormatter)
	{
		this.useFormatter = Boolean.valueOf(useFormatter);
	}
	 
	/**
	 * Generates path name of output file.
	 * 
	 * @return String full pathname of output procurement card file
	 */
	private String getOutputFilename()
	{
		String extension = procurementCardOutputFileType.getFileExtension();
		String directory = procurementCardOutputFileType.getDirectoryPath();
		DateTimeService dateTimeService = procurementCardOutputFileType.getDateTimeService();
		String timestamp = dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate());
		String pcTransactionFileName = directory + proCardFilename + '_' + timestamp + '.' + extension;
		
		return pcTransactionFileName;
	}
	
	/**
	 * adds transaction element to the root node of the document.
	 * 
	 * @param pcUploadDoc
	 * @param rootElement
	 */
	private void addTransactionElement(ProcurementCardUploadDocument pcUploadDoc, Element rootElement)
	{
		Element transaction = new Element("transaction", defaultNamespace);
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CREDIT_CARD_NUMBER, pcUploadDoc.getTransactionCreditCardNumber());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_TOTAL_AMOUNT,       pcUploadDoc.getFinancialDocTotalAmount());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_DEBIT_CREDIT,       pcUploadDoc.getDebitCreditCode());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_COA,                pcUploadDoc.getChartOfAccountsCode());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_ACCT_NUMBER,        pcUploadDoc.getAccountNumber());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_OBJ_CODE,           pcUploadDoc.getFinancialObjectCode());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CARD_NAME,          pcUploadDoc.getCardHolderName());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_TRANS_DATE,         pcUploadDoc.getTransactionDate());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_TRANS_REF_NUMBER,   pcUploadDoc.getTransactionRefNumber());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CAT_CODE,           pcUploadDoc.getTransMerchantCategoryCode());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_POSTING_DATE,       pcUploadDoc.getTransPostingDate());
        addElement(transaction, CuFPPropertyConstants.ELEMENT_ORIG_CUR_CODE,      pcUploadDoc.getOrigCurrencyCode());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_ORIG_CUR_AMOUNT,    pcUploadDoc.getOrigCurrencyAmount());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_SALES_TAX,          pcUploadDoc.getTransSalesTaxAmount());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_VENDOR_NAME,        pcUploadDoc.getVendorName());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_VENDOR_ADDR1,       pcUploadDoc.getVendorAddress1());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_VENDOR_CITY,        pcUploadDoc.getVendorCity());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_VENDOR_STATE,       pcUploadDoc.getVendorState());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CC_ADDR1,           pcUploadDoc.getCardHolderAddress1());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CC_ADDR2,           pcUploadDoc.getCardHolderAddress2());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CC_CITY,            pcUploadDoc.getCardHolderCity());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CC_STATE,           pcUploadDoc.getCardHolderState());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CC_ZIP,             pcUploadDoc.getCardHolderZip());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CC_PHONE,           pcUploadDoc.getCardHolderPhone());
		addElement(transaction, CuFPPropertyConstants.ELEMENT_CARD_LIMIT,         pcUploadDoc.getCardLimit());

		// Add the level 3 information to the procard file
	    Element transactionDetails = new Element("transactionDetails", defaultNamespace);
        addElement(transactionDetails, CuFPPropertyConstants.CUSTOMER_CODE, pcUploadDoc.getCustomerCode());
	    addPcardElements(transactionDetails, pcUploadDoc);
        addPcardUserAmountElements(transactionDetails, pcUploadDoc);
        addPassengerTransportElements(transactionDetails, pcUploadDoc);
        addPassengerTransportLegElements(transactionDetails, pcUploadDoc);
        addLodgingElements(transactionDetails, pcUploadDoc);
        addRentalCarElements(transactionDetails, pcUploadDoc);
        addGenericElements(transactionDetails, pcUploadDoc);
        addFuelElements(transactionDetails, pcUploadDoc);
        addNonFuelElements(transactionDetails, pcUploadDoc);
	    transaction.appendChild(transactionDetails);
		
		rootElement.appendChild(transaction);
	} 

	/**
	 * This method adds the Purchasing Card information (Addendum 1), if any exists.
	 * 
	 * @param transactionDetails
	 * @param pcUploadDoc
	 */
    private void addPcardElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getPcardDetails() != null && pcUploadDoc.getPcardDetails().size() > 0) 
        {
            Element pcardDetailsElement = new Element("pcardDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum1 pcard : pcUploadDoc.getPcardDetails()) 
            {
                Element pcardElement = new Element("pcard", defaultNamespace);
                addElement(pcardElement, CuFPPropertyConstants.PRODUCT_CODE, pcard.getProductCode());
                addElement(pcardElement, CuFPPropertyConstants.ITEM_DESCRIPTION, pcard.getItemDescription());
                addElement(pcardElement, CuFPPropertyConstants.ITEM_QUANTITY, pcard.getItemQuantity().toString());
                addElement(pcardElement, CuFPPropertyConstants.ITEM_UOM, pcard.getItemUnitOfMeasure());
                addElement(pcardElement, CuFPPropertyConstants.EXT_ITEM_AMOUNT, pcard.getExtendedItemAmount().toString());
                addElement(pcardElement, CuFPPropertyConstants.DEBIT_CREDIT_IND, pcard.getDebitCreditInd());
                addElement(pcardElement, CuFPPropertyConstants.NET_GROSS_IND, pcard.getNetGrossIndicator());
                addElement(pcardElement, CuFPPropertyConstants.TAX_RATE_APPLIED, pcard.getTaxRateApplied().toString());
                addElement(pcardElement, CuFPPropertyConstants.TAX_TYPE_APPLIED, pcard.getTaxTypeApplied());
                addElement(pcardElement, CuFPPropertyConstants.TAX_AMOUNT, pcard.getTaxAmount().toString());
                addElement(pcardElement, CuFPPropertyConstants.DISCOUNT_IND, pcard.getDiscountIndicator());
                addElement(pcardElement, CuFPPropertyConstants.DISCOUNT_AMOUNT, pcard.getDiscountAmount().toString());
                pcardDetailsElement.appendChild(pcardElement);
            }
            transactionDetails.appendChild(pcardDetailsElement);
        }
       
    }

    /**
     * This method adds the Purchasing Card User Amount information (Addendum 11), if any exists.
     * 
     * @param transactionDetails
     * @param pcUploadDoc
     */
    private void addPcardUserAmountElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getPcardUserAmountDetails() != null && pcUploadDoc.getPcardUserAmountDetails().size() > 0) 
        {
            Element pcardUserAmountDetailsElement = new Element("pcardUserAmountDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum11 pcardUserAmount : pcUploadDoc.getPcardUserAmountDetails()) 
            {
                Element pcardUserAmountElement = new Element("pcardUserAmount", defaultNamespace);
                addElement(pcardUserAmountElement, CuFPPropertyConstants.USER_AMOUNT, pcardUserAmount.getUserAmount().toString());
                pcardUserAmountDetailsElement.appendChild(pcardUserAmountElement);
            }
            transactionDetails.appendChild(pcardUserAmountDetailsElement);
        }
        
    }

    /**
     * This method adds the Passenger Transport information (Addendum 2), if any exists.
     * 
     * @param transactionDetails
     * @param pcUploadDoc
     */
    private void addPassengerTransportElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getPassengerTransportDetails() != null && pcUploadDoc.getPassengerTransportDetails().size() > 0) 
        {
            Element passengerTransportDetailsElement = new Element("passengerTransportDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum2 passengerTransport : pcUploadDoc.getPassengerTransportDetails()) 
            {
                Element passengerTransportElement = new Element("passengerTransport", defaultNamespace);
                addElement(passengerTransportElement, CuFPPropertyConstants.PASSENGER_NAME, passengerTransport.getPassengerName());
                addElement(passengerTransportElement, CuFPPropertyConstants.DEPARTURE_DATE, passengerTransport.getDepartureDateString());
                addElement(passengerTransportElement, CuFPPropertyConstants.AIRPORT_CODE, passengerTransport.getAirportCode());
                addElement(passengerTransportElement, CuFPPropertyConstants.TRAVEL_AGENCY_CODE, passengerTransport.getTravelAgencyCode());
                addElement(passengerTransportElement, CuFPPropertyConstants.TRAVEL_AGENCY_NAME, passengerTransport.getTravelAgencyName());
                addElement(passengerTransportElement, CuFPPropertyConstants.TICKET_NUMBER, passengerTransport.getTicketNumber());
                addElement(passengerTransportElement, CuFPPropertyConstants.CUSTOMER_CODE, passengerTransport.getCustomerCode());
                addElement(passengerTransportElement, CuFPPropertyConstants.ISSUE_DATE, passengerTransport.getIssueDateString());
                addElement(passengerTransportElement, CuFPPropertyConstants.ISSUING_CARRIER, passengerTransport.getIssuingCarrier());
                addElement(passengerTransportElement, CuFPPropertyConstants.TOTAL_FARE, passengerTransport.getTotalFare().toString());
                addElement(passengerTransportElement, CuFPPropertyConstants.TOTAL_FEES, passengerTransport.getTotalFees().toString());
                addElement(passengerTransportElement, CuFPPropertyConstants.TOTAL_TAXES, passengerTransport.getTotalTaxes().toString());
                passengerTransportDetailsElement.appendChild(passengerTransportElement);
            }
            transactionDetails.appendChild(passengerTransportDetailsElement);
        }
    }

    /**
     * This method adds the Passenger Transport Leg information (Addendum 21), if any exists.
     * 
     * @param transactionDetails
     * @param pcUploadDoc
     */
    private void addPassengerTransportLegElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getPassengerTransportLegDetails() != null && pcUploadDoc.getPassengerTransportLegDetails().size() > 0) 
        {
            Element passengerTransportLegDetailsElement = new Element("passengerTransportLegDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum21 passengerTransportLeg : pcUploadDoc.getPassengerTransportLegDetails()) 
            {
                Element passengerTransportLegElement = new Element("passengerTransportLeg", defaultNamespace);
                addElement(passengerTransportLegElement, CuFPPropertyConstants.TRIP_LEG_NUMBER, passengerTransportLeg.getTripLegNumber());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.CARRIER_CODE, passengerTransportLeg.getCarrierCode());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.SERVICE_CLASS, passengerTransportLeg.getServiceClass());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.STOP_OVER_CODE, passengerTransportLeg.getStopOverCode());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.CITY_OF_ORIGIN, passengerTransportLeg.getCityOfOrigin());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.CONJUNCTION_TICKET, passengerTransportLeg.getConjunctionTicket());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.TRAVEL_DATE, passengerTransportLeg.getTravelDateString());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.EXCHANGE_TICKET, passengerTransportLeg.getExchangeTicket());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.COUPON_NUMBER, passengerTransportLeg.getCouponNumber());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.CITY_OF_DESTINATION, passengerTransportLeg.getCityOfDestination());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.FARE_BASE_CODE, passengerTransportLeg.getFareBaseCode());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.FLIGHT_NUMBER, passengerTransportLeg.getFlightNumber());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.DEPARTURE_TIME, passengerTransportLeg.getDepartureTime());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.DEPARTURE_TIME_SEGMENT, passengerTransportLeg.getDepartureTimeSegment());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.ARRIVAL_TIME, passengerTransportLeg.getArrivalTime());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.ARRIVAL_TIME_SEGMENT, passengerTransportLeg.getArrivalTimeSegment());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.FARE, passengerTransportLeg.getFare().toString());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.FEE, passengerTransportLeg.getFee().toString());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.TAXES, passengerTransportLeg.getTaxes().toString());
                addElement(passengerTransportLegElement, CuFPPropertyConstants.ENDORSEMENTS, passengerTransportLeg.getEndorsements());
                passengerTransportLegDetailsElement.appendChild(passengerTransportLegElement);
            }
            transactionDetails.appendChild(passengerTransportLegDetailsElement);
        }
    }

    /**
     * This method adds the Lodging information (Addendum 3), if any exists.
     * 
     * @param transactionDetails
     * @param pcUploadDoc
     */
    private void addLodgingElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getLodgingDetails() != null && pcUploadDoc.getLodgingDetails().size() > 0) 
        {
            Element lodgingDetailsElement = new Element("lodgingDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum3 lodging : pcUploadDoc.getLodgingDetails()) 
            {
                Element lodgingElement = new Element("lodging", defaultNamespace);
                addElement(lodgingElement, CuFPPropertyConstants.ARRIVAL_DATE, lodging.getArrivalDateString());
                addElement(lodgingElement, CuFPPropertyConstants.DEPARTURE_DATE, lodging.getDepartureDateString());
                addElement(lodgingElement, CuFPPropertyConstants.FOLIO_NUMBER, lodging.getFolioNumber());
                addElement(lodgingElement, CuFPPropertyConstants.PROPERTY_PHONE_NUMBER, lodging.getPropertyPhoneNumber());
                addElement(lodgingElement, CuFPPropertyConstants.CUSTOMER_SERVICE_NUMBER, lodging.getCustomerServiceNumber());
                addElement(lodgingElement, CuFPPropertyConstants.ROOM_RATE, lodging.getRoomRate().toString());
                addElement(lodgingElement, CuFPPropertyConstants.ROOM_TAX, lodging.getRoomTax().toString());
                addElement(lodgingElement, CuFPPropertyConstants.PROGRAM_CODE, lodging.getProgramCode());
                addElement(lodgingElement, CuFPPropertyConstants.TELEPHONE_CHARGES, lodging.getTelephoneCharges().toString());
                addElement(lodgingElement, CuFPPropertyConstants.ROOM_SERVICE, lodging.getRoomService().toString());
                addElement(lodgingElement, CuFPPropertyConstants.BAR_CHARGES, lodging.getBarCharges().toString());
                addElement(lodgingElement, CuFPPropertyConstants.GIFT_SHOP_CHARGES, lodging.getGiftShopCharges().toString());
                addElement(lodgingElement, CuFPPropertyConstants.LAUNDRY_CHARGES, lodging.getLaundryCharges().toString());
                addElement(lodgingElement, CuFPPropertyConstants.OTHER_SERVICES_CODE, lodging.getOtherServicesCode());
                addElement(lodgingElement, CuFPPropertyConstants.OTHER_SERVICES_CHARGES, lodging.getOtherServicesCharges().toString());
                addElement(lodgingElement, CuFPPropertyConstants.BILLING_ADJUSTMENT_IND, lodging.getBillingAdjustmentIndicator());
                addElement(lodgingElement, CuFPPropertyConstants.BILLING_ADJUSTMENT_AMOUNT, lodging.getBillingAdjustmentAmount().toString());
                lodgingDetailsElement.appendChild(lodgingElement);
            }
            transactionDetails.appendChild(lodgingDetailsElement);
        }
    }

    /**
     * This method adds the Rental Car information (Addendum 4), if any exists.
     * 
     * @param transactionDetails
     * @param pcUploadDoc
     */
    private void addRentalCarElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getRentalCarDetails() != null && pcUploadDoc.getRentalCarDetails().size() > 0) 
        {
            Element rentalCarDetailsElement = new Element("rentalCarDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum4 rentalCar : pcUploadDoc.getRentalCarDetails()) 
            {
                Element rentalCarElement = new Element("rentalCar", defaultNamespace);
                addElement(rentalCarElement, CuFPPropertyConstants.RENTAL_AGREEMENT_NUMBER, rentalCar.getRentalAgreementNumber());
                addElement(rentalCarElement, CuFPPropertyConstants.RENTER_NAME, rentalCar.getRenterName());
                addElement(rentalCarElement, CuFPPropertyConstants.RENTAL_RETURN_CITY, rentalCar.getRentalReturnCity());
                addElement(rentalCarElement, CuFPPropertyConstants.RENTAL_RETURN_STATE, rentalCar.getRentalReturnState());
                addElement(rentalCarElement, CuFPPropertyConstants.RENTAL_RETURN_COUNTRY, rentalCar.getRentalReturnCountry());
                addElement(rentalCarElement, CuFPPropertyConstants.RENTAL_RETURN_DATE, rentalCar.getRentalReturnDateString());
                addElement(rentalCarElement, CuFPPropertyConstants.RETURN_LOCATION_ID, rentalCar.getReturnLocationId());
                addElement(rentalCarElement, CuFPPropertyConstants.CUSTOMER_SERVICE_NUMBER, rentalCar.getCustomerServiceNumber());
                addElement(rentalCarElement, CuFPPropertyConstants.RENTAL_CLASS, rentalCar.getRentalClass());
                addElement(rentalCarElement, CuFPPropertyConstants.DAILY_RENTAL_RATE, rentalCar.getDailyRentalRate().toString());
                addElement(rentalCarElement, CuFPPropertyConstants.RATE_PER_MILE, rentalCar.getRatePerMile().toString());
                addElement(rentalCarElement, CuFPPropertyConstants.TOTAL_MILES, rentalCar.getTotalMiles().toString());
                addElement(rentalCarElement, CuFPPropertyConstants.MAX_FREE_MILES, rentalCar.getMaxFreeMiles().toString());
                addElement(rentalCarElement, CuFPPropertyConstants.INSURANCE_IND, rentalCar.getInsuranceIndicator());
                addElement(rentalCarElement, CuFPPropertyConstants.INSURANCE_CHARGES, rentalCar.getInsuranceCharges().toString());
                addElement(rentalCarElement, CuFPPropertyConstants.ADJUSTED_AMOUNT_IND, rentalCar.getAdjustedAmountIndicator());
                addElement(rentalCarElement, CuFPPropertyConstants.ADJUSTED_AMOUNT, rentalCar.getAdjustedAmount().toString());
                addElement(rentalCarElement, CuFPPropertyConstants.PROGRAM_CODE, rentalCar.getProgramCode());
                addElement(rentalCarElement, CuFPPropertyConstants.CHECKOUT_DATE, rentalCar.getCheckoutDateString());
                rentalCarDetailsElement.appendChild(rentalCarElement);
            }
            transactionDetails.appendChild(rentalCarDetailsElement);
        }
    }

    /**
     * This method adds the Generic information (Addendum 5), if any exists.
     * 
     * @param transactionDetails
     * @param pcUploadDoc
     */
    private void addGenericElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getGenericDetails() != null && pcUploadDoc.getGenericDetails().size() > 0) 
        {
            Element genericDetailsElement = new Element("genericDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum5 generic : pcUploadDoc.getGenericDetails()) 
            {
                Element genericElement = new Element("generic", defaultNamespace);
                addElement(genericElement, CuFPPropertyConstants.GENERIC_ADDEDNUM_DATA, generic.getGenericAddendumData());
                genericDetailsElement.appendChild(genericElement);
            }
            transactionDetails.appendChild(genericDetailsElement);
        }
    }

    /**
     * This method adds the Fuel information (Addendum 6), if any exists.
     * 
     * @param transactionDetails
     * @param pcUploadDoc
     */
    private void addFuelElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getFuelDetails() != null && pcUploadDoc.getFuelDetails().size() > 0) 
        {
            Element fuelDetailsElement = new Element("fuelDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum6 fuel : pcUploadDoc.getFuelDetails()) 
            {
                Element fuelElement = new Element("fuel", defaultNamespace);
                addElement(fuelElement, CuFPPropertyConstants.OIL_COMPANY_BRAND, fuel.getOilCompanyBrand());
                addElement(fuelElement, CuFPPropertyConstants.MERCHANT_STREET_ADDRESS, fuel.getMerchantStreetAddress());
                addElement(fuelElement, CuFPPropertyConstants.MERCHANT_POSTAL_CODE, fuel.getMerchantPostalCode());
                addElement(fuelElement, CuFPPropertyConstants.TIME_OF_PURCHASE, fuel.getTimeOfPurchase());
                addElement(fuelElement, CuFPPropertyConstants.MOTOR_FUEL_SERVICE_TYPE, fuel.getMotorFuelServiceType());
                addElement(fuelElement, CuFPPropertyConstants.MOTOR_FUEL_PRODUCT_CODE, fuel.getMotorFuelProductCode());
                addElement(fuelElement, CuFPPropertyConstants.MOTOR_FUEL_UNIT_PRICE, fuel.getMotorFuelUnitPrice().toString());
                addElement(fuelElement, CuFPPropertyConstants.MOTOR_FUEL_UOM, fuel.getMotorFuelUnitOfMeasure());
                addElement(fuelElement, CuFPPropertyConstants.MOTOR_FUEL_QUANTITY, fuel.getMotorFuelQuantity().toString());
                addElement(fuelElement, CuFPPropertyConstants.MOTOR_FUEL_SALE_AMOUNT, fuel.getMotorFuelSaleAmount().toString());
                addElement(fuelElement, CuFPPropertyConstants.ODOMETER_READING, fuel.getOdomoterReading().toString());
                addElement(fuelElement, CuFPPropertyConstants.VEHICLE_NUMBER, fuel.getVehicleNumber());
                addElement(fuelElement, CuFPPropertyConstants.DRIVER_NUMBER, fuel.getDriverNumber());
                addElement(fuelElement, CuFPPropertyConstants.MAGNETIC_STRIPE_PRODUCT_TYPE_CODE, fuel.getMagneticStripeProductTypeCode());
                addElement(fuelElement, CuFPPropertyConstants.COUPON_DISCOUNT_AMOUNT, fuel.getCouponDiscountAmount().toString());
                addElement(fuelElement, CuFPPropertyConstants.TAX_EXEMPT_AMOUNT, fuel.getTaxExemptAmount().toString());
                addElement(fuelElement, CuFPPropertyConstants.TAX_AMOUNT1, fuel.getTaxAmount1().toString());
                addElement(fuelElement, CuFPPropertyConstants.TAX_AMOUNT2, fuel.getTaxAmount2().toString());
                fuelDetailsElement.appendChild(fuelElement);
            }
            transactionDetails.appendChild(fuelDetailsElement);
        }
    }

    /**
     * This method adds the Non Fuel information (Addendum 61), if any exists.
     * 
     * @param transactionDetails
     * @param pcUploadDoc
     */
    private void addNonFuelElements(Element transactionDetails, ProcurementCardUploadDocument pcUploadDoc)
    {
        if (pcUploadDoc.getNonFuelDetails() != null && pcUploadDoc.getNonFuelDetails().size() > 0) 
        {
            Element nonFuelDetailsElement = new Element("nonFuelDetails", defaultNamespace);
            for (MasterCardTransactionDetailAddendum61 nonFuel : pcUploadDoc.getNonFuelDetails()) 
            {
                Element nonFuelElement = new Element("nonFuel", defaultNamespace);
                addElement(nonFuelElement, CuFPPropertyConstants.ITEM_PRODUCT_CODE, nonFuel.getItemProductCode());
                addElement(nonFuelElement, CuFPPropertyConstants.ITEM_DESCRIPTION, nonFuel.getItemDescription());
                addElement(nonFuelElement, CuFPPropertyConstants.ITEM_QUANTITY, nonFuel.getItemQuantity().toString());
                addElement(nonFuelElement, CuFPPropertyConstants.ITEM_UOM, nonFuel.getItemUnitOfMeasure());
                addElement(nonFuelElement, CuFPPropertyConstants.EXT_ITEM_AMOUNT, nonFuel.getExtendedItemAmount().toString());
                addElement(nonFuelElement, CuFPPropertyConstants.DISCOUNT_IND, nonFuel.getDiscountIndicator());
                addElement(nonFuelElement, CuFPPropertyConstants.DISCOUNT_AMOUNT, nonFuel.getDiscountAmount().toString());
                addElement(nonFuelElement, CuFPPropertyConstants.NET_GROSS_IND, nonFuel.getNetGrossIndicator());
                addElement(nonFuelElement, CuFPPropertyConstants.TAX_RATE_APPLIED, nonFuel.getTaxRateApplied().toString());
                addElement(nonFuelElement, CuFPPropertyConstants.TAX_TYPE_APPLIED, nonFuel.getTaxTypeApplied());
                addElement(nonFuelElement, CuFPPropertyConstants.TAX_AMOUNT, nonFuel.getTaxAmount().toString());
                addElement(nonFuelElement, CuFPPropertyConstants.DEBIT_CREDIT_IND, nonFuel.getDebitCreditInd());
                addElement(nonFuelElement, CuFPPropertyConstants.ALTERNATE_TAX_ID, nonFuel.getAlternateTaxIdentifier());
                nonFuelDetailsElement.appendChild(nonFuelElement);
            }
            transactionDetails.appendChild(nonFuelDetailsElement);
        }        
    }

    /**
	 * Adding null text throws IllegalArgumentException so if value is null then
	 * do not add element.
	 * 
	 * @param transaction - transaction element
	 * @param nodeName - element name
	 * @param value - element value
	 */
	private void addElement(Element transaction, String nodeName, String value)
	{
		if (value != null)
		{
			Element node = new Element(nodeName, defaultNamespace);
			node.appendChild(value);
			transaction.appendChild(node);
		}
	}
	
}
