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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.BusinessObjectBase;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailFieldUtil;

/**
 * Business object to hold the master card procurement transaction data from the flat file input. This object is not persisted in
 * the database but is required to be a BusinessObject so that it can use the DataDictionary service to assist in parsing the flat
 * file input.
 *
 * @see MasterCardTransactionDetailAddendum7
 *
 * @author Dave Raines
 * @version $Revision$
 */
public class MasterCardTransactionDetail extends BusinessObjectBase
{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(MasterCardTransactionDetail.class);
	protected BusinessObjectStringParserFieldUtils mctdFieldUtil;
	protected DateFormat inputDateFormatter;

	protected String recordType;
	protected String addendumType;
	protected String creditCardNumber;
	protected String transactionRefNumber;
	protected String debitCreditInd;
	protected KualiDecimal transactionAmount;
	protected Date postingDate;
	protected Date transactionDate;
	protected String merchantName;
	protected String merchantAddress;
	protected String merchantCity;
	protected String merchantState;
	protected String merchantCountry;
	protected String categoryCode;
	protected KualiDecimal origCurrencyAmount;
	protected String origCurrencyCode;
	protected String customerCode;
	protected KualiDecimal salesTaxAmount;
	protected String corporationName;

	protected String filler1;
	protected String filler2;
	protected String filler3;
	protected String filler4;
	protected String filler5;
	protected String filler6;
	protected String filler7;

    protected List<MasterCardTransactionDetailAddendum1> addendum1Details;
    protected List<MasterCardTransactionDetailAddendum11> addendum11Details;
    protected List<MasterCardTransactionDetailAddendum2> addendum2Details;
    protected List<MasterCardTransactionDetailAddendum21> addendum21Details;
    protected List<MasterCardTransactionDetailAddendum3> addendum3Details;
    protected List<MasterCardTransactionDetailAddendum4> addendum4Details;
    protected List<MasterCardTransactionDetailAddendum5> addendum5Details;
    protected List<MasterCardTransactionDetailAddendum6> addendum6Details;
    protected List<MasterCardTransactionDetailAddendum61> addendum61Details;

	/**
	 * Constructor creates a new MasterCardTransactionDetail
	 */
	public MasterCardTransactionDetail()
	{
		super();
		String inputDatePattern = "yyyyMMdd";
		inputDateFormatter = new SimpleDateFormat(inputDatePattern);
		this.transactionAmount  = new KualiDecimal(0);
		this.origCurrencyAmount = new KualiDecimal(0);
		this.salesTaxAmount     = new KualiDecimal(0);

        addendum1Details = new ArrayList<MasterCardTransactionDetailAddendum1>();
        addendum11Details = new ArrayList<MasterCardTransactionDetailAddendum11>();
        addendum2Details = new ArrayList<MasterCardTransactionDetailAddendum2>();
        addendum21Details = new ArrayList<MasterCardTransactionDetailAddendum21>();
        addendum3Details = new ArrayList<MasterCardTransactionDetailAddendum3>();
        addendum4Details = new ArrayList<MasterCardTransactionDetailAddendum4>();
        addendum5Details = new ArrayList<MasterCardTransactionDetailAddendum5>();
        addendum6Details = new ArrayList<MasterCardTransactionDetailAddendum6>();
        addendum61Details = new ArrayList<MasterCardTransactionDetailAddendum61>();
	}

	/**
	 * Parses inputRecord setting class attributes. Dates and currency are converted into respective java objects.
	 *
	 * @param inputRecord
	 */
	public void parseInput(String inputRecord)
	{
		addendumType         = getValue(inputRecord, CuFPPropertyConstants.ADDENDUM_TYPE, CuFPPropertyConstants.FILLER2);
		creditCardNumber     = getValue(inputRecord, CuFPPropertyConstants.CREDIT_CARD_NUMBER, CuFPPropertyConstants.FILLER3);
		transactionRefNumber = getValue(inputRecord, CuFPPropertyConstants.TRANS_REF_NUMBER, CuFPPropertyConstants.FILLER4);
		debitCreditInd       = getValue(inputRecord, CuFPPropertyConstants.DEBIT_CREDIT_IND, CuFPPropertyConstants.TRANSACTION_AMOUNT);

		setTransactionAmount(getValue(inputRecord, CuFPPropertyConstants.TRANSACTION_AMOUNT, CuFPPropertyConstants.POSTING_DATE));
		setPostingDate(      getValue(inputRecord, CuFPPropertyConstants.POSTING_DATE, CuFPPropertyConstants.TRANSACTION_DATE));
		setTransactionDate(  getValue(inputRecord, CuFPPropertyConstants.TRANSACTION_DATE, CuFPPropertyConstants.FILLER5));

		merchantName     = getValue(inputRecord, CuFPPropertyConstants.MERCHANT_NAME, CuFPPropertyConstants.MERCHANT_CITY);
		merchantCity     = getValue(inputRecord, CuFPPropertyConstants.MERCHANT_CITY, CuFPPropertyConstants.MERCHANT_STATE);
		merchantState    = getValue(inputRecord, CuFPPropertyConstants.MERCHANT_STATE, CuFPPropertyConstants.MERCHANT_COUNTRY);
		categoryCode     = getValue(inputRecord, CuFPPropertyConstants.CATEGORY_CODE, CuFPPropertyConstants.ORIG_CURRENCY_AMOUNT);
		origCurrencyCode = getValue(inputRecord, CuFPPropertyConstants.ORIG_CURRENCY_CODE, CuFPPropertyConstants.FILLER6);
        customerCode     = getValue(inputRecord, CuFPPropertyConstants.CUSTOMER_CODE, CuFPPropertyConstants.SALES_TAX_AMOUNT);

		setOrigCurrencyAmount(getValue(inputRecord, CuFPPropertyConstants.ORIG_CURRENCY_AMOUNT, CuFPPropertyConstants.ORIG_CURRENCY_CODE));
		setSalesTaxAmount(    getValue(inputRecord, CuFPPropertyConstants.SALES_TAX_AMOUNT, CuFPPropertyConstants.FILLER7));
	}

	/**
	 *
	 */
	public String getRecordType()
	{
		return recordType;
	}

	/**
	 *
	 */
	public void setRecordType(String recordType)
	{
		this.recordType = recordType;
	}

	/**
	 *
	 */
	public String getAddendumType()
	{
		return addendumType;
	}

	/**
	 *
	 */
	public void setAddendumType(String addendumType)
	{
		this.addendumType = addendumType;
	}

	/**
     *
     */
	public String getCreditCardNumber()
	{
		return creditCardNumber;
	}

	/**
	 * returns the credit card number
	 */
	public String getMaskedTransactionCreditCardNumber()
	{
		int length = creditCardNumber.length();
		String suffix = StringUtils.substring(creditCardNumber, length - 4);
		return "**********" + suffix;
	}

	/**
	 *
	 */
	public void setCreditCardNumber(String creditCardNumber)
	{
		this.creditCardNumber = creditCardNumber;
	}

	/**
	 *
	 */
	public String getTransactionRefNumber()
	{
		return transactionRefNumber;
	}

	/**
	 *
	 */
	public void setTransactionRefNumber(String transactionRefNumber)
	{
		this.transactionRefNumber = transactionRefNumber;
	}

	/**
	 *
	 */
	public String getDebitCreditInd()
	{
		return debitCreditInd;
	}

	/**
	 *
	 */
	public void setDebitCreditInd(String debitCreditInd)
	{
		this.debitCreditInd = debitCreditInd;
	}

	/**
	 *
	 */
	public KualiDecimal getTransactionAmount()
	{
		return transactionAmount;
	}

	/**
	 *
	 */
	public void setTransactionAmount(KualiDecimal transactionAmount)
	{
		this.transactionAmount = transactionAmount;
	}

	/**
	 *
	 */
	public void setTransactionAmount(String transactionAmount)
	{
		if (NumberUtils.isDigits(transactionAmount))
		{
			this.transactionAmount = new KualiDecimal(Double.valueOf(transactionAmount) / 10000);
		}
		else
		{
			this.transactionAmount = new KualiDecimal(0);
		}
	}

	/**
	 *
	 */
	public Date getPostingDate()
	{
		return postingDate;
	}

	/**
	 *
	 */
	public void setPostingDate(Date postingDate)
	{
		this.postingDate = postingDate;
	}

	/**
	 *
	 */
	public void setPostingDate(String postingDate)
	{
		try
		{
			this.postingDate = inputDateFormatter.parse(postingDate);
		}
		catch (ParseException e)
		{
			LOG.warn("could not parse posting date " + postingDate + ": " + e);
		}
	}

	/**
	 *
	 */
	public Date getTransactionDate()
	{
		return transactionDate;
	}

	/**
	 *
	 */
	public void setTransactionDate(String transactionDate)
	{
		try
		{
			this.transactionDate = inputDateFormatter.parse(transactionDate);
		}
		catch (ParseException e)
		{
			LOG.warn("could not parse transaction date " + transactionDate + ": " + e);
		}
	}

	/**
	 *
	 */
	public void setTransactionDate(Date transactionDate)
	{
		this.transactionDate = transactionDate;
	}


	/**
	 *
	 */
	public String getMerchantName()
	{
		return merchantName;
	}

	/**
	 *
	 */
	public void setMerchantName(String merchantName)
	{
		this.merchantName = merchantName;
	}

	/**
	 *
	 */
	public String getMerchantAddress()
	{
		return merchantAddress;
	}

	/**
	 *
	 */
	public void setMerchantAddress(String merchantAddress)
	{
		this.merchantAddress = merchantAddress;
	}

	/**
	 *
	 */
	public String getMerchantCity()
	{
		return merchantCity;
	}

	/**
	 *
	 */
	public void setMerchantCity(String merchantCity)
	{
		this.merchantCity = merchantCity;
	}

	/**
	 *
	 */
	public String getMerchantState()
	{
		return merchantState;
	}

	/**
	 *
	 */
	public void setMerchantState(String merchantState)
	{
		this.merchantState = merchantState;
	}

	/**
	 *
	 */
	public String getMerchantCountry()
	{
		return merchantCountry;
	}

	/**
	 *
	 */
	public void setMerchantCountry(String merchantCountry)
	{
		this.merchantCountry = merchantCountry;
	}

	/**
	 *
	 */
	public String getCategoryCode()
	{
		return categoryCode;
	}

	/**
	 *
	 */
	public void setCategoryCode(String categoryCode)
	{
		this.categoryCode = categoryCode;
	}

	/**
	 *
	 */
	public KualiDecimal getOrigCurrencyAmount()
	{
		return origCurrencyAmount;
	}

	/**
	 *
	 */
	public void setOrigCurrencyAmount(KualiDecimal origCurrencyAmount)
	{
		this.origCurrencyAmount = origCurrencyAmount;
	}

	/**
	 *
	 */
	public void setOrigCurrencyAmount(String origCurrencyAmount)
	{
		if (NumberUtils.isDigits(origCurrencyAmount))
		{
			this.origCurrencyAmount = new KualiDecimal(Double.valueOf(origCurrencyAmount) / 10000);
		}
		else
		{
			this.origCurrencyAmount = new KualiDecimal(0);
		}
	}

	/**
	 *
	 */
	public String getOrigCurrencyCode()
	{
		return origCurrencyCode;
	}

	/**
	 *
	 */
	public void setOrigCurrencyCode(String origCurrencyCode)
	{
		this.origCurrencyCode = origCurrencyCode;
	}

	public String getCustomerCode()
	{
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    /**
	 *
	 */
	public KualiDecimal getSalesTaxAmount()
	{
		return salesTaxAmount;
	}

	/**
	 *
	 */
	public void setSalesTaxAmount(KualiDecimal salesTaxAmount)
	{
		this.salesTaxAmount = salesTaxAmount;
	}

	/**
	 *
	 */
	public void setSalesTaxAmount(String salesTaxAmount)
	{
		if (NumberUtils.isDigits(salesTaxAmount))
		{
			this.salesTaxAmount = new KualiDecimal(Double.valueOf(salesTaxAmount) / 10000);
		}
		else
		{
			this.salesTaxAmount = new KualiDecimal(0);
		}
	}

	/**
	 *
	 */
	public String getCorporationName()
	{
		return corporationName;
	}

	/**
	 *
	 */
	public void setCorporationName(String corporationName)
	{
		this.corporationName = corporationName;
	}

	/**
	 *
	 */
	public String getFiller1()
	{
		return filler1;
	}

	/**
	 *
	 */
	public String getFiller2()
	{
		return filler2;
	}

	/**
	 *
	 */
	public String getFiller3()
	{
		return filler3;
	}

	/**
	 *
	 */
	public String getFiller4()
	{
		return filler4;
	}

	/**
	 *
	 */
	public String getFiller5()
	{
		return filler5;
	}

	/**
	 *
	 */
	public String getFiller6()
	{
		return filler6;
	}

	/**
	 *
	 */
	public String getFiller7()
	{
		return filler7;
	}

	/**
	 * @see org.kuali.rice.krad.bo.BusinessObject#refresh()
	 */

	@Override
    public void refresh()
	{
		// not used
	}

	/**
	 * Uses the MasterCardTransactionDetailFieldUtil class to obtain start and end positions of propertyName and extracts the value
	 * for propertyName from the input record
	 *
	 * @param input 5000 addendum 0 input line from master card file
	 * @param propertyName the name of the property to extract
	 * @param nextPropertyName the property immediately following propertyName in the file
	 * @return value of property propertyName
	 */
	protected String getValue(String input, String propertyName, String nextPropertyName)
	{
		BusinessObjectStringParserFieldUtils fieldUtil = getFieldUtil();
		final Map<String, Integer> pMap = fieldUtil.getFieldBeginningPositionMap();
		int startIndex = pMap.get(propertyName);
		int endIndex = pMap.get(nextPropertyName);
		return input != null ? StringUtils.substring(input, startIndex, endIndex).trim() : null;
	}

	/**
	 * Returns the BusinessObjectStringParserFieldUtils for addendum type 0 records.
	 *
	 * @return MasterCardTransactionDetailFieldUtil
	 */
	protected BusinessObjectStringParserFieldUtils getFieldUtil()
	{
		if (mctdFieldUtil == null)
		{
			mctdFieldUtil = new MasterCardTransactionDetailFieldUtil();
		}

		return mctdFieldUtil;
	}

	/**
	 * This method converts a String to a KualiDecimal
	 *
	 * @param decimal
	 * @return
	 */
	protected KualiDecimal convertStringToKualiDecimal(String decimal)
	{
	    KualiDecimal convertedDecimal;
        if (NumberUtils.isDigits(decimal))
        {
            convertedDecimal = new KualiDecimal(Double.valueOf(decimal) / 10000);
        }
        else
        {
            convertedDecimal = new KualiDecimal(0);
        }
        return convertedDecimal;
	}


	/**
	 * This method converts a String to a BigDecimal
	 *
	 * @param decimal
	 * @return
	 */
	protected BigDecimal convertStringToBigDecimal(String decimal)
    {
        BigDecimal convertedDecimal;
        if (NumberUtils.isDigits(decimal))
        {
            convertedDecimal = new BigDecimal(Double.valueOf(decimal) / 100000).setScale(5);
        }
        else
        {
            convertedDecimal = new BigDecimal(0).setScale(5);
        }
        return convertedDecimal;
    }

    public List<MasterCardTransactionDetailAddendum1> getAddendum1Details()
    {
        return addendum1Details;
    }

    public void setAddendum1Details(List<MasterCardTransactionDetailAddendum1> addendum1Details)
    {
        this.addendum1Details = addendum1Details;
    }

    public void addAddendum1Details(MasterCardTransactionDetailAddendum1 addendum1) {
        this.addendum1Details.add(addendum1);
    }

    public List<MasterCardTransactionDetailAddendum2> getAddendum2Details()
    {
        return addendum2Details;
    }

    public void setAddendum2Details(List<MasterCardTransactionDetailAddendum2> addendum2Details)
    {
        this.addendum2Details = addendum2Details;
    }

    public void addAddendum2Details(MasterCardTransactionDetailAddendum2 addendum2) {
        this.addendum2Details.add(addendum2);
    }

    public List<MasterCardTransactionDetailAddendum21> getAddendum21Details()
    {
        return addendum21Details;
    }

    public void setAddendum21Details(List<MasterCardTransactionDetailAddendum21> addendum21Details)
    {
        this.addendum21Details = addendum21Details;
    }

    public void addAddendum21Details(MasterCardTransactionDetailAddendum21 addendum21) {
        this.addendum21Details.add(addendum21);
    }

    public List<MasterCardTransactionDetailAddendum3> getAddendum3Details()
    {
        return addendum3Details;
    }

    public void setAddendum3Details(List<MasterCardTransactionDetailAddendum3> addendum3Details)
    {
        this.addendum3Details = addendum3Details;
    }

    public void addAddendum3Details(MasterCardTransactionDetailAddendum3 addendum3) {
        this.addendum3Details.add(addendum3);
    }

    public List<MasterCardTransactionDetailAddendum4> getAddendum4Details()
    {
        return addendum4Details;
    }

    public void setAddendum4Details(List<MasterCardTransactionDetailAddendum4> addendum4Details)
    {
        this.addendum4Details = addendum4Details;
    }

    public void addAddendum4Details(MasterCardTransactionDetailAddendum4 addendum4) {
        this.addendum4Details.add(addendum4);
    }

    public List<MasterCardTransactionDetailAddendum5> getAddendum5Details()
    {
        return addendum5Details;
    }

    public void setAddendum5Details(List<MasterCardTransactionDetailAddendum5> addendum5Details)
    {
        this.addendum5Details = addendum5Details;
    }

    public void addAddendum5Details(MasterCardTransactionDetailAddendum5 addendum5) {
        this.addendum5Details.add(addendum5);
    }

    public List<MasterCardTransactionDetailAddendum6> getAddendum6Details()
    {
        return addendum6Details;
    }

    public void setAddendum6Details(List<MasterCardTransactionDetailAddendum6> addendum6Details)
    {
        this.addendum6Details = addendum6Details;
    }

    public void addAddendum6Details(MasterCardTransactionDetailAddendum6 addendum6) {
        this.addendum6Details.add(addendum6);
    }

    public List<MasterCardTransactionDetailAddendum61> getAddendum61Details()
    {
        return addendum61Details;
    }

    public void setAddendum61Details(List<MasterCardTransactionDetailAddendum61> addendum61Details)
    {
        this.addendum61Details = addendum61Details;
    }

    public void addAddendum61Details(MasterCardTransactionDetailAddendum61 addendum61) {
        this.addendum61Details.add(addendum61);
    }

    public List<MasterCardTransactionDetailAddendum11> getAddendum11Details()
    {
        return addendum11Details;
    }

    public void setAddendum11Details(List<MasterCardTransactionDetailAddendum11> addendum11Details)
    {
        this.addendum11Details = addendum11Details;
    }

    public void addAddendum11Details(MasterCardTransactionDetailAddendum11 addendum11) {
        this.addendum11Details.add(addendum11);
    }

}
