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

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

public class NonFuelBase extends PersistableBusinessObjectBase
{

	private static final long serialVersionUID = 1L;
    private Integer nonFuelId;
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

    public NonFuelBase()
    {
        super();

        this.itemQuantity = new Integer(0);
        this.extendedItemAmount = new KualiDecimal(0);
        this.discountAmount = new KualiDecimal(0);
        this.taxRateApplied = new BigDecimal(0).setScale(5);
        this.taxAmount = new KualiDecimal(0);
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

    public void setItemQuantity(String itemQuantity)
    {
        if (StringUtils.isNotBlank(itemQuantity))
        {
            this.itemQuantity = new Integer(itemQuantity);
        }
        else
        {
            this.itemQuantity = new Integer(0);
        }
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
        if (StringUtils.isNotBlank(extendedItemAmount))
        {
            this.extendedItemAmount = new KualiDecimal(extendedItemAmount);
        }
        else
        {
            this.extendedItemAmount = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(discountAmount))
        {
            this.discountAmount = new KualiDecimal(discountAmount);
        }
        else
        {
            this.discountAmount = KualiDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(taxRateApplied))
        {
            this.taxRateApplied = new BigDecimal(taxRateApplied);
        }
        else
        {
            this.taxRateApplied = BigDecimal.ZERO;
        }
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
        if (StringUtils.isNotBlank(taxAmount))
        {
            this.taxAmount = new KualiDecimal(taxAmount);
        }
        else
        {
            this.taxAmount = KualiDecimal.ZERO;
        }
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

    public Integer getNonFuelId()
    {
        return nonFuelId;
    }

    public void setNonFuelId(Integer nonFuelId)
    {
        this.nonFuelId = nonFuelId;
    }

}
