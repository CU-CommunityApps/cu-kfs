/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Purap Accounting Line Base Business Object.
 */
public abstract class PurApAccountingLineBase extends SourceAccountingLine implements PurApAccountingLine, Comparable {

    protected Integer accountIdentifier;
    private Integer itemIdentifier;
    private BigDecimal accountLinePercent;
    //stored in DB only for PREQ and CM Account History
    private String postingPeriodCode;
    // not stored in DB; needed for disencumbrances and such
    private KualiDecimal alternateAmountForGLEntryCreation;
    public Integer purApSequenceNumber;

    private PurApItem purapItem;
    // KFSPTS-2200
    private boolean discountTradeIn;
    
   /**
     * Default constructor
     */
    public PurApAccountingLineBase() {
        super();
        setSequenceNumber(0);
        setAmount(null);
    }

    @Override
    public Integer getAccountIdentifier() {
        return accountIdentifier;
    }

    @Override
    public void setAccountIdentifier(final Integer requisitionAccountIdentifier) {
        accountIdentifier = requisitionAccountIdentifier;
    }

    @Override
    public Integer getItemIdentifier() {
        return itemIdentifier;
    }

    @Override
    public void setItemIdentifier(final Integer requisitionItemIdentifier) {
        itemIdentifier = requisitionItemIdentifier;
    }

    @Override
    public BigDecimal getAccountLinePercent() {
        if (accountLinePercent != null) {
            accountLinePercent = accountLinePercent.setScale(2, BigDecimal.ROUND_HALF_UP);
            return accountLinePercent;
        } else {
            return BigDecimal.ZERO.setScale(2, 2);
        }
    }

    @Override
    public void setAccountLinePercent(final BigDecimal accountLinePercent) {
        this.accountLinePercent = accountLinePercent;
    }

    @Override
    public boolean isEmpty() {
        return !(StringUtils.isNotEmpty(getAccountNumber())
                || StringUtils.isNotEmpty(getChartOfAccountsCode())
                || StringUtils.isNotEmpty(getFinancialObjectCode())
                || StringUtils.isNotEmpty(getFinancialSubObjectCode())
                || StringUtils.isNotEmpty(getOrganizationReferenceId())
                || StringUtils.isNotEmpty(getProjectCode())
                || StringUtils.isNotEmpty(getSubAccountNumber())
                || ObjectUtils.isNotNull(getAccountLinePercent()));
    }

    @Override
    public PurApAccountingLine createBlankAmountsCopy() {
        final PurApAccountingLine newAccount = (PurApAccountingLine) ObjectUtils.deepCopy(this);
        newAccount.setSequenceNumber(0);
        newAccount.setAccountLinePercent(null);
        newAccount.setAmount(null);
        return newAccount;
    }

    @Override
    public boolean accountStringsAreEqual(final SourceAccountingLine accountingLine) {
        if (accountingLine == null) {
            return false;
        }
        return new EqualsBuilder().append(getChartOfAccountsCode(), accountingLine.getChartOfAccountsCode())
                .append(getAccountNumber(), accountingLine.getAccountNumber())
                .append(getSubAccountNumber(), accountingLine.getSubAccountNumber())
                .append(getFinancialObjectCode(), accountingLine.getFinancialObjectCode())
                .append(getFinancialSubObjectCode(), accountingLine.getFinancialSubObjectCode())
                .append(getProjectCode(), accountingLine.getProjectCode())
                .append(getOrganizationReferenceId(), accountingLine.getOrganizationReferenceId())
            .isEquals();
    }

    @Override
    public boolean accountStringsAreEqual(final PurApAccountingLine accountingLine) {
        return accountStringsAreEqual((SourceAccountingLine) accountingLine);

    }

    @Override
    public SourceAccountingLine generateSourceAccountingLine() {
        // the fields here should probably match method 'accountStringsAreEqual' above
        final SourceAccountingLine sourceLine = new SourceAccountingLine();
        sourceLine.setChartOfAccountsCode(getChartOfAccountsCode());
        sourceLine.setAccountNumber(getAccountNumber());
        sourceLine.setSubAccountNumber(getSubAccountNumber());
        sourceLine.setFinancialObjectCode(getFinancialObjectCode());
        sourceLine.setFinancialSubObjectCode(getFinancialSubObjectCode());
        sourceLine.setProjectCode(getProjectCode());
        sourceLine.setOrganizationReferenceId(getOrganizationReferenceId());
        sourceLine.setAmount(getAmount());
        sourceLine.setSequenceNumber(getSequenceNumber());
        return sourceLine;
    }

    @Override
    public int compareTo(final Object arg0) {
        if (arg0 instanceof PurApAccountingLine) {
            final PurApAccountingLine account = (PurApAccountingLine) arg0;
            return getString().compareTo(account.getString());
        }
        return -1;
    }

    @Override
    public String getString() {
        return getChartOfAccountsCode() + "~" + getAccountNumber() + "~" + getSubAccountNumber() + "~" +
                getFinancialObjectCode() + "~" + getFinancialSubObjectCode() + "~" + getProjectCode() + "~" +
                getOrganizationReferenceId();
    }

    @Override
    public KualiDecimal getAlternateAmountForGLEntryCreation() {
        return alternateAmountForGLEntryCreation;
    }

    @Override
    public void setAlternateAmountForGLEntryCreation(final KualiDecimal alternateAmount) {
        alternateAmountForGLEntryCreation = alternateAmount;
    }

    @Override
    public Integer getSequenceNumber() {
        return getAccountIdentifier();
    }

    @Override
    public void copyFrom(final AccountingLine other) {
        super.copyFrom(other);

        if (other instanceof PurApAccountingLine) {
            final PurApAccountingLine purapOther = (PurApAccountingLine) other;

            // Need to fix accountIdentifier and sequenceNumber since they are crossed in the getter in purap
            // i.e. getSequenceNumber() actually returns accountIdentifier, while getPurApSequenceNumber() returns
            // the original sequenceNumber. Without this fix, this.sequenceNumber will be set as
            // other.AccountIdentifier, while this.accountIdentifier will remain unpopulated or it was; this is not
            // what we want; and if this method were used during comparison, such as being called by isLike(), then
            // error will occur.
            setAccountIdentifier(purapOther.getAccountIdentifier());
            setSequenceNumber(purapOther.getPurApSequenceNumber());

            setAccountLinePercent(purapOther.getAccountLinePercent());
            setAmount(purapOther.getAmount());
            setAlternateAmountForGLEntryCreation(purapOther.getAlternateAmountForGLEntryCreation());
        }
    }

    @Override
    public void refreshNonUpdateableReferences() {
        //hold onto item reference if there without itemId
        PurApItem item = null;
        final PurApItem tempItem = getPurapItem();
        if (tempItem != null && tempItem.getItemIdentifier() != null) {
            item = tempItem;
        }
        super.refreshNonUpdateableReferences();
        if (ObjectUtils.isNotNull(item)) {
            setPurapItem(item);
        }
    }

    @Override
    public <T extends PurApItem> T getPurapItem() {
        return (T) purapItem;
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public void setPurapItem(final PurApItem item) {
        purapItem = item;
    }

    @Override
    public String getPostingPeriodCode() {
        return postingPeriodCode;
    }

    @Override
    public void setPostingPeriodCode(final String postingPeriodCode) {
        this.postingPeriodCode = postingPeriodCode;
    }

    /**
     * Overridden to use purap doc identifier, rather than document number
     */
    @Override
    public Map getValuesMap() {
        final Map valuesMap = super.getValuesMap();
        // remove document number
        valuesMap.remove(KFSPropertyConstants.DOCUMENT_NUMBER);
        return valuesMap;
    }

    @Override
    public Integer getPurApSequenceNumber() {
        return super.getSequenceNumber();
    }
    
    public boolean isDiscountTradeIn() {
        return discountTradeIn;
    }

    public void setDiscountTradeIn(boolean discountTradeIn) {
        this.discountTradeIn = discountTradeIn;
    }

    @Override
    public String toString() {
        String acctLineString =  super.toString();
        if (StringUtils.isNotBlank(organizationReferenceId)) {
            acctLineString = acctLineString + ", " + organizationReferenceId;

        }
        
        return acctLineString;
    }
 
}
