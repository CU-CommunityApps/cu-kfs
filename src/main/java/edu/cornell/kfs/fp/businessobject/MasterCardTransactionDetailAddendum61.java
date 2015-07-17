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

import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailAddendum61FieldUtil;


/**
 * Adds the fields needed on the record type 5000 addendum 61 (Non Fuel) records.
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendum61 extends MasterCardTransactionDetailAddendumBase
{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendum61.class);

    protected String filler1;
    protected String filler2;
    protected String itemProductCode;
    protected String itemDescription;
    protected Integer itemQuantity;
    protected String itemUnitOfMeasure;
    protected KualiDecimal extendedItemAmount;
    protected String discountIndicator;
    protected KualiDecimal discountAmount;
    protected String netGrossIndicator;
    protected BigDecimal taxRateApplied;
    protected String taxTypeApplied;
    protected KualiDecimal taxAmount;
    protected String debitCreditInd;
    protected String alternateTaxIdentifier;
    protected String filler3;

    /**
     * Constructor creates a new MasterCardTransactionDetail
     */
    public MasterCardTransactionDetailAddendum61()
    {
        super();

        this.extendedItemAmount = new KualiDecimal(0);
        this.discountAmount = new KualiDecimal(0);
        this.taxRateApplied = new BigDecimal(0).setScale(5);
        this.taxAmount = new KualiDecimal(0);
    }

    /**
     * Parses inputRecord setting class attributes. Dates and currency are converted into respective java objects.
     *
     * @param inputRecord
     */
    public void parseInput(String inputRecord)
    {
        itemProductCode = getValue(inputRecord, CuFPPropertyConstants.ITEM_PRODUCT_CODE, CuFPPropertyConstants.ITEM_DESCRIPTION);
        itemDescription = getValue(inputRecord, CuFPPropertyConstants.ITEM_DESCRIPTION, CuFPPropertyConstants.ITEM_QUANTITY);
        itemQuantity = new Integer(getValue(inputRecord, CuFPPropertyConstants.ITEM_QUANTITY, CuFPPropertyConstants.ITEM_UOM));
        itemUnitOfMeasure = getValue(inputRecord, CuFPPropertyConstants.ITEM_UOM, CuFPPropertyConstants.EXT_ITEM_AMOUNT);
        setExtendedItemAmount(getValue(inputRecord, CuFPPropertyConstants.EXT_ITEM_AMOUNT, CuFPPropertyConstants.DISCOUNT_IND));
        discountIndicator = getValue(inputRecord, CuFPPropertyConstants.DISCOUNT_IND, CuFPPropertyConstants.DISCOUNT_AMOUNT);
        setDiscountAmount(getValue(inputRecord, CuFPPropertyConstants.DISCOUNT_AMOUNT, CuFPPropertyConstants.NET_GROSS_IND));
        netGrossIndicator = getValue(inputRecord, CuFPPropertyConstants.NET_GROSS_IND, CuFPPropertyConstants.TAX_RATE_APPLIED);
        setTaxRateApplied(getValue(inputRecord, CuFPPropertyConstants.TAX_RATE_APPLIED, CuFPPropertyConstants.TAX_TYPE_APPLIED));
        taxTypeApplied = getValue(inputRecord, CuFPPropertyConstants.TAX_TYPE_APPLIED, CuFPPropertyConstants.TAX_AMOUNT);
        setTaxAmount(getValue(inputRecord, CuFPPropertyConstants.TAX_AMOUNT, CuFPPropertyConstants.DEBIT_CREDIT_IND));
        debitCreditInd = getValue(inputRecord, CuFPPropertyConstants.DEBIT_CREDIT_IND, CuFPPropertyConstants.ALTERNATE_TAX_ID);
        alternateTaxIdentifier = getValue(inputRecord, CuFPPropertyConstants.ALTERNATE_TAX_ID, CuFPPropertyConstants.FILLER3);
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

    public String getItemProductCode()
    {
        return itemProductCode;
    }

    public void setItemProductCode(String itemProductCode)
    {
        this.itemProductCode = itemProductCode;
    }

    public String getItemDescription()
    {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription)
    {
        this.itemDescription = itemDescription;
    }

    public Integer getItemQuantity()
    {
        return itemQuantity;
    }

    public void setItemQuantity(Integer itemQuantity)
    {
        this.itemQuantity = itemQuantity;
    }

    public String getItemUnitOfMeasure()
    {
        return itemUnitOfMeasure;
    }

    public void setItemUnitOfMeasure(String itemUnitOfMeasure)
    {
        this.itemUnitOfMeasure = itemUnitOfMeasure;
    }

    public KualiDecimal getExtendedItemAmount()
    {
        return extendedItemAmount;
    }

    public void setExtendedItemAmount(KualiDecimal extendedItemAmount)
    {
        this.extendedItemAmount = extendedItemAmount;
    }

    public void setExtendedItemAmount(String extendedItemAmount)
    {
        this.extendedItemAmount = convertStringToKualiDecimal(extendedItemAmount);
    }

    public String getDiscountIndicator()
    {
        return discountIndicator;
    }

    public void setDiscountIndicator(String discountIndicator)
    {
        this.discountIndicator = discountIndicator;
    }

    public KualiDecimal getDiscountAmount()
    {
        return discountAmount;
    }

    public void setDiscountAmount(KualiDecimal discountAmount)
    {
        this.discountAmount = discountAmount;
    }

    public void setDiscountAmount(String discountAmount)
    {
        this.discountAmount = convertStringToKualiDecimal(discountAmount);
    }

    public String getNetGrossIndicator()
    {
        return netGrossIndicator;
    }

    public void setNetGrossIndicator(String netGrossIndicator)
    {
        this.netGrossIndicator = netGrossIndicator;
    }

    public BigDecimal getTaxRateApplied()
    {
        return taxRateApplied;
    }

    public void setTaxRateApplied(BigDecimal taxRateApplied)
    {
        this.taxRateApplied = taxRateApplied;
    }

    public void setTaxRateApplied(String taxRateApplied)
    {
        this.taxRateApplied = convertStringToBigDecimal(taxRateApplied);
    }

    public String getTaxTypeApplied()
    {
        return taxTypeApplied;
    }

    public void setTaxTypeApplied(String taxTypeApplied)
    {
        this.taxTypeApplied = taxTypeApplied;
    }

    public KualiDecimal getTaxAmount()
    {
        return taxAmount;
    }

    public void setTaxAmount(KualiDecimal taxAmount)
    {
        this.taxAmount = taxAmount;
    }

    public void setTaxAmount(String taxAmount)
    {
        this.taxAmount = convertStringToKualiDecimal(taxAmount);
    }

    public String getDebitCreditInd()
    {
        return debitCreditInd;
    }

    public void setDebitCreditInd(String debitCreditInd)
    {
        this.debitCreditInd = debitCreditInd;
    }

    public String getAlternateTaxIdentifier()
    {
        return alternateTaxIdentifier;
    }

    public void setAlternateTaxIdentifier(String alternateTaxIdentifier)
    {
        this.alternateTaxIdentifier = alternateTaxIdentifier;
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
     * Returns the BusinessObjectStringParserFieldUtils for addendum type 61 records.
     *
     * @return instance of MasterCardTransactionDetailAddendum61FieldUtil
     * @see MasterCardTransactionDetailFieldUtil.getFieldUtil()
     */
    @Override
    protected BusinessObjectStringParserFieldUtils getFieldUtil()
    {
        if (mctdFieldUtil == null)
        {
            mctdFieldUtil = new MasterCardTransactionDetailAddendum61FieldUtil();
        }
        return mctdFieldUtil;
    }

}
