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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.TransientBusinessObjectBase;

import edu.cornell.kfs.fp.CuFPPropertyConstants;

/**
 * Temporary object to hold record type 5000 data from the mastercard file, along with card holder info. Used to generate the PCDO
 * XML file.
 * 
 * @author Dave Raines
 * @version $Revision$
 */
public class ProcurementCardUploadDocument extends TransientBusinessObjectBase
{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(ProcurementCardUploadDocument.class);

	private KualiDecimal financialDocTotalAmount;
	private String debitCreditCode;
	private ProcurementCardHolderDetail cardHolder;
	private Date transactionDate;
	private String transactionRefNumber;
	private String transMerchantCategoryCode;
	private Date transPostingDate;
	private String origCurrencyCode;
	private KualiDecimal origCurrencyAmount;
    private String customerCode;
	private KualiDecimal transSalesTaxAmount;
	private String vendorName;
	private String vendorAddress1;
	private String vendorAddress2;
	private String vendorCity;
	private String vendorState;
	
    protected List<MasterCardTransactionDetailAddendum1> pcardDetails;
    protected List<MasterCardTransactionDetailAddendum11> pcardUserAmountDetails;
    protected List<MasterCardTransactionDetailAddendum2> passengerTransportDetails;
    protected List<MasterCardTransactionDetailAddendum21> passengerTransportLegDetails;
    protected List<MasterCardTransactionDetailAddendum3> lodgingDetails;
    protected List<MasterCardTransactionDetailAddendum4> rentalCarDetails;
    protected List<MasterCardTransactionDetailAddendum5> genericDetails;
    protected List<MasterCardTransactionDetailAddendum6> fuelDetails;
    protected List<MasterCardTransactionDetailAddendum61> nonFuelDetails;

	private DateFormat inputDateFormatter;
	private DateFormat outputDateFormatter;

	public ProcurementCardUploadDocument()
	{
		String inputDatePattern = "yyyyMMdd";
		String outputDatePattern = "yyyy-MM-dd";
		inputDateFormatter = new SimpleDateFormat(inputDatePattern);
		outputDateFormatter = new SimpleDateFormat(outputDatePattern);
		
        pcardDetails = new ArrayList<MasterCardTransactionDetailAddendum1>();
        pcardUserAmountDetails = new ArrayList<MasterCardTransactionDetailAddendum11>();
        passengerTransportDetails = new ArrayList<MasterCardTransactionDetailAddendum2>();
        passengerTransportLegDetails = new ArrayList<MasterCardTransactionDetailAddendum21>();
        lodgingDetails = new ArrayList<MasterCardTransactionDetailAddendum3>();
        rentalCarDetails = new ArrayList<MasterCardTransactionDetailAddendum4>();
        genericDetails = new ArrayList<MasterCardTransactionDetailAddendum5>();
        fuelDetails = new ArrayList<MasterCardTransactionDetailAddendum6>();
        nonFuelDetails = new ArrayList<MasterCardTransactionDetailAddendum61>();

	}

	/**
	 * returns the credit card number
	 */
	public String getTransactionCreditCardNumber()
	{
		return this.cardHolder.getCreditCardNumber();
	}

	/**
	 * returns the credit card number
	 */
	public String getMaskedTransactionCreditCardNumber()
	{
		String creditCardNumber = "";

		if (this.cardHolder != null)
		{
			creditCardNumber = this.cardHolder.getCreditCardNumber();
		}

		int length = creditCardNumber.length();
		String suffix = StringUtils.substring(creditCardNumber, length - 4);
		return "**********" + suffix;
	}

	/**
	 * returns the transaction amount
	 */
	public KualiDecimal getFinancialDocTotalAmountAsKualiDecimal()
	{
		return CuFPPropertyConstants.CREDIT_CODE.equals(debitCreditCode) ? 
				financialDocTotalAmount.multiply(new KualiDecimal(-1), true) : financialDocTotalAmount;
	}

	/**
	 * returns the transaction amount
	 */
	public String getFinancialDocTotalAmount()
	{
		return financialDocTotalAmount != null ? financialDocTotalAmount.toString() : null;
	}

	/**
	 * sets the transaction amount
	 */
	public void setFinancialDocTotalAmount(KualiDecimal financialDocTotalAmount)
	{
		this.financialDocTotalAmount = financialDocTotalAmount;
	}

	/**
	 * sets the transaction amount from a String value. It is assumed that the string is 16 bytes, with 4 implied decimal places.
	 */
	public void setFinancialDocTotalAmount(String financialDocTotalAmount)
	{
		if (NumberUtils.isDigits(financialDocTotalAmount))
		{
			this.financialDocTotalAmount = 
					new KualiDecimal(Integer.valueOf(financialDocTotalAmount).doubleValue() / 10000);
		}
	}

	/**
	 * returns the debitCredit code
	 */
	public String getDebitCreditCode()
	{
		return debitCreditCode;
	}

	/**
	 * sets the debitCredit code
	 */
	public void setDebitCreditCode(String debitCreditCode)
	{
		this.debitCreditCode = debitCreditCode;
	}

	/**
	 * returns the chart of accounts code
	 */
	public String getChartOfAccountsCode()
	{
		return this.cardHolder.getChartOfAccountsCode();
	}

	/**
	 * returns the account number
	 */
	public String getAccountNumber()
	{
		return this.cardHolder.getAccountNumber();
	}

	/**
	 * returns the financial object code
	 */
	public String getFinancialObjectCode()
	{
		return this.cardHolder.getFinancialObjectCode();
	}

	/**
     * 
     */
	public ProcurementCardHolderDetail getCardHolder()
	{
		return cardHolder;
	}

	public String getCardHolderName()
	{
		return this.cardHolder.getCardHolderName();
	}

	public String getCardHolderAltName()
	{
		return this.cardHolder.getCardHolderAlternateName();
	}

	public String getCardHolderAddress1()
	{
		return this.cardHolder.getCardHolderLine1Address();
	}

	public String getCardHolderAddress2()
	{
		return this.cardHolder.getCardHolderLine2Address();
	}

	public String getCardHolderCity()
	{
		return this.cardHolder.getCardHolderCityName();
	}

	public String getCardHolderState()
	{
		return this.cardHolder.getCardHolderStateCode();
	}

	public String getCardHolderZip()
	{
		return this.cardHolder.getCardHolderZipCode();
	}

	public String getCardHolderPhone()
	{
		return this.cardHolder.getCardHolderWorkPhoneNumber();
	}

	public String getCardLimit()
	{
		KualiDecimal cardLimit = this.cardHolder.getCardLimit();
		return cardLimit != null ? cardLimit.toString() : null;
	}

	/**
	 * sets the ProcurementCardHolderDetail object
	 */
	public void setCardHolder(ProcurementCardHolderDetail cardHolder)
	{
		this.cardHolder = cardHolder;
	}

	/**
	 * returns the transaction date
	 */
	public String getTransactionDate()
	{
		return transactionDate != null ? outputDateFormatter.format(transactionDate) : null;
	}

	/**
	 * sets the transaction date
	 */
	public void setTransactionDate(Date transactionDate)
	{
		this.transactionDate = transactionDate;
	}

	/**
	 * sets the transaction date from a String in YYYYMMDD format
	 */
	public void setTransactionDate(String transactionDate)
	{
		try
		{
			this.transactionDate = inputDateFormatter.parse(transactionDate);
		}
		catch (ParseException e)
		{
			LOG.warn("could not parse transaction date: " + transactionDate);
		}
	}

	/**
	 * returns the transaction reference number
	 */
	public String getTransactionRefNumber()
	{
		return transactionRefNumber;
	}

	/**
	 * Sets the transaction reference number
	 */
	public void setTransactionRefNumber(String transactionRefNumber)
	{
		this.transactionRefNumber = transactionRefNumber;
	}

	/**
	 * returns the merchant category code
	 */
	public String getTransMerchantCategoryCode()
	{
		return transMerchantCategoryCode;
	}

	/**
	 * sets the merchant category code
	 */
	public void setTransMerchantCategoryCode(String transMerchantCategoryCode)
	{
		this.transMerchantCategoryCode = transMerchantCategoryCode;
	}

	/**
	 * returns the transaction posting date
	 */
	public String getTransPostingDate()
	{
		return transPostingDate != null ? outputDateFormatter.format(transPostingDate) : null;
	}

	/**
	 * sets the posting date
	 */
	public void setTransPostingDate(Date transPostingDate)
	{
		this.transPostingDate = transPostingDate;
	}

	/**
	 * sets the posting date from a String in YYYYMMDD format
	 */
	public void setTransPostingDate(String transPostingDate)
	{
		try
		{
			this.transPostingDate = inputDateFormatter.parse(transPostingDate);
		}
		catch (ParseException e)
		{
			LOG.warn("could not parse posting date: " + transPostingDate);
		}
	}

	/**
	 * returns the original currency code
	 */
	public String getOrigCurrencyCode()
	{
		return origCurrencyCode;
	}

	/**
	 * sets the original currency code
	 */
	public void setOrigCurrencyCode(String origCurrencyCode)
	{
		this.origCurrencyCode = origCurrencyCode;
	}

	/**
	 * returns the original currency amount
	 */
	public String getOrigCurrencyAmount()
	{
		return origCurrencyAmount != null ? origCurrencyAmount.toString() : null;
	}

	/**
	 * sets the original currency amount
	 */
	public void setOrigCurrencyAmount(KualiDecimal origCurrencyAmount)
	{
		this.origCurrencyAmount = origCurrencyAmount;
	}

	/**
	 * sets the original currency amount from a String value. It is assumed that the string is 16 bytes, with 4 implied decimal
	 * places.
	 */
	public void setOrigCurrencyAmount(String origCurrencyAmount)
	{
		if (NumberUtils.isDigits(origCurrencyAmount))
		{
			this.origCurrencyAmount = new KualiDecimal(Integer.valueOf(origCurrencyAmount).doubleValue() / 10000);
		}
	}

	/**
	 * returns the sales tax amount
	 */
	public String getTransSalesTaxAmount()
	{
		return transSalesTaxAmount != null ? transSalesTaxAmount.toString() : null;
	}

	/**
	 * sets the sales tax amount.
	 */
	public void setTransSalesTaxAmount(KualiDecimal transSalesTaxAmount)
	{
		this.transSalesTaxAmount = transSalesTaxAmount;
	}

	/**
	 * sets the sales tax amount from a String value. It is assumed that the string is 16 bytes, with 4 implied decimal places.
	 */
	public void setTransSalesTaxAmount(String transSalesTaxAmount)
	{
		if (NumberUtils.isDigits(transSalesTaxAmount))
		{
			this.transSalesTaxAmount = new KualiDecimal(Integer.valueOf(transSalesTaxAmount).doubleValue() / 10000);
		}
	}

	/**
	 * returns the merchant name
	 */
	public String getVendorName()
	{
		return vendorName;
	}

	/**
	 * sets the merchant name
	 */
	public void setVendorName(String vendorName)
	{
		this.vendorName = vendorName;
	}

	/**
	 * returns the merchant address
	 */
	public String getVendorAddress1()
	{
		return vendorAddress1;
	}

	/**
	 * sets the merchant address
	 */
	public void setVendorAddress1(String vendorAddress1)
	{
		this.vendorAddress1 = vendorAddress1;
	}

	/**
	 * not used
	 */
	public String getVendorAddress2()
	{
		return vendorAddress2;
	}

	/**
	 * not used
	 */
	public void setVendorAddress2(String vendorAddress2)
	{
		this.vendorAddress2 = vendorAddress2;
	}

	/**
	 * returns the merchant city
	 */
	public String getVendorCity()
	{
		return vendorCity;
	}

	/**
	 * sets the merchant city
	 */
	public void setVendorCity(String vendorCity)
	{
		this.vendorCity = vendorCity;
	}

	/**
	 * returns the merchant state
	 */
	public String getVendorState()
	{
		return vendorState;
	}

	/**
	 * sets the merchant state
	 */
	public void setVendorState(String vendorState)
	{
		this.vendorState = vendorState;
	}

    public List<MasterCardTransactionDetailAddendum1> getPcardDetails()
    {
        return pcardDetails;
    }

    public void setPcardDetails(List<MasterCardTransactionDetailAddendum1> pcardDetails)
    {
        this.pcardDetails = pcardDetails;
    }

    public String getCustomerCode()
    {
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    public List<MasterCardTransactionDetailAddendum2> getPassengerTransportDetails()
    {
        return passengerTransportDetails;
    }

    public void setPassengerTransportDetails(List<MasterCardTransactionDetailAddendum2> passengerTransportDetails)
    {
        this.passengerTransportDetails = passengerTransportDetails;
    }

    public List<MasterCardTransactionDetailAddendum21> getPassengerTransportLegDetails()
    {
        return passengerTransportLegDetails;
    }

    public void setPassengerTransportLegDetails(List<MasterCardTransactionDetailAddendum21> passengerTransportLegDetails)
    {
        this.passengerTransportLegDetails = passengerTransportLegDetails;
    }

    public List<MasterCardTransactionDetailAddendum3> getLodgingDetails()
    {
        return lodgingDetails;
    }

    public void setLodgingDetails(List<MasterCardTransactionDetailAddendum3> lodgingDetails)
    {
        this.lodgingDetails = lodgingDetails;
    }

    public List<MasterCardTransactionDetailAddendum4> getRentalCarDetails()
    {
        return rentalCarDetails;
    }

    public void setRentalCarDetails(List<MasterCardTransactionDetailAddendum4> rentalCarDetails)
    {
        this.rentalCarDetails = rentalCarDetails;
    }

    public List<MasterCardTransactionDetailAddendum5> getGenericDetails()
    {
        return genericDetails;
    }

    public void setGenericDetails(List<MasterCardTransactionDetailAddendum5> genericDetails)
    {
        this.genericDetails = genericDetails;
    }

    public List<MasterCardTransactionDetailAddendum6> getFuelDetails()
    {
        return fuelDetails;
    }

    public void setFuelDetails(List<MasterCardTransactionDetailAddendum6> fuelDetails)
    {
        this.fuelDetails = fuelDetails;
    }

    public List<MasterCardTransactionDetailAddendum61> getNonFuelDetails()
    {
        return nonFuelDetails;
    }

    public void setNonFuelDetails(List<MasterCardTransactionDetailAddendum61> nonFuelDetails)
    {
        this.nonFuelDetails = nonFuelDetails;
    }

    public List<MasterCardTransactionDetailAddendum11> getPcardUserAmountDetails()
    {
        return pcardUserAmountDetails;
    }

    public void setPcardUserAmountDetails(List<MasterCardTransactionDetailAddendum11> pcardUserAmountDetails)
    {
        this.pcardUserAmountDetails = pcardUserAmountDetails;
    }

}
