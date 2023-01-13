/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.module.purap.document;

import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.sql.Date;
import java.util.List;

/**
 * Interface for Purchasing-Accounts Payable Documents.
 */
public interface PurchasingAccountsPayableDocument extends AccountingDocument, PurapItemOperations {

    /**
     * @return true if posting year on document is set to use NEXT fiscal year. If set to anything besides NEXT, then
     *         return false.
     */
    boolean isPostingYearNext();

    /**
     * @return true if posting year on document is set to use PRIOR fiscal year. If set to anything besides PRIOR, then
     *         return false.
     */
    boolean isPostingYearPrior();

    /**
     * @return If posting year on document is set to use NEXT fiscal year, then return NEXT. If set to anything besides
     *         NEXT, then return CURRENT fiscal year.  This is assuming that the system does not allow the user to set
     *         a posting year beyond NEXT.
     */
    Integer getPostingYearNextOrCurrent();

    @Override
    Class getItemClass();

    /**
     * @return the source of this document if exists, else null.
     */
    PurchasingAccountsPayableDocument getPurApSourceDocumentIfPossible();

    /**
     * @return the label of the document source if exists, else null.
     */
    String getPurApSourceDocumentLabelIfPossible();

    /**
     * @param nodeName the name of the specified node.
     * @return true if this document is stopped in the specified route node.
     */
    boolean isDocumentStoppedInRouteNode(String nodeName);

    /**
     * Adds the specified item to this document.
     *
     * @param item the specified item to add.
     */
    void addItem(PurApItem item);

    /**
     * Deletes the specified item from this document.
     *
     * @param lineNum the specified item to delete.
     */
    void deleteItem(int lineNum);

    /**
     * Renumbers the item starting from the specified index.
     *
     * @param start the index of the starting item to be renumbered.
     */
    void renumberItems(int start);

    /**
     * Swaps the specified two items based on their item line numbers (which are one higher than the item positions in
     * the list).
     *
     * @param position1 the position of the first item
     * @param position2 the position of the second item
     */
    void itemSwap(int position1, int position2);

    /**
     * Determines the item line position if the user did not specify the line number on an above the line items before
     * clicking on the add button. It subtracts the number of the below the line items on the list with the total item
     * list size.
     *
     * @return the item line position of the last (highest) line number of above the line items.
     */
    int getItemLinePosition();

    /**
     * @param pos the specified index.
     * @return the item at the specified index.
     */
    @Override
    PurApItem getItem(int pos);

    /**
     * @return a list of below the line item types.
     */
    String[] getBelowTheLineTypes();

    /**
     * @return the total dollar amount of all items.
     */
    KualiDecimal getTotalDollarAmount();

    /**
     * @param totalDollarAmount the specified total dollar amount to set.
     */
    void setTotalDollarAmount(KualiDecimal totalDollarAmount);

    /**
     * @param excludedTypes the types of items to be excluded.
     * @return the total dollar amount with the specified item types excluded.
     */
    KualiDecimal getTotalDollarAmountAllItems(String[] excludedTypes);

    KualiDecimal getTotalDollarAmountAboveLineItems();

    /**
     * @return the pre tax total dollar amount of all items.
     */
    KualiDecimal getTotalPreTaxDollarAmount();

    /**
     * @param totalDollarAmount the specified total dollar amount to set.
     */
    void setTotalPreTaxDollarAmount(KualiDecimal totalDollarAmount);

    /**
     * @param excludedTypes the types of items to be excluded.
     * @return the pre tax total dollar amount with the specified item types excluded.
     */
    KualiDecimal getTotalPreTaxDollarAmountAllItems(String[] excludedTypes);

    KualiDecimal getTotalTaxAmount();

    void setTotalTaxAmount(KualiDecimal amount);

    KualiDecimal getTotalTaxAmountAllItems(String[] excludedTypes);

    KualiDecimal getTotalTaxAmountAboveLineItems();

    KualiDecimal getTotalTaxAmountAboveLineItems(String[] excludedTypes);

    KualiDecimal getTotalTaxAmountWithExclusions(String[] excludedTypes, boolean includeBelowTheLine);

    /**
     * @param vendorAddress vendor address fields to set based on a given VendorAddress.
     */
    void templateVendorAddress(VendorAddress vendorAddress);

    Country getVendorCountry();

    VendorDetail getVendorDetail();

    @Override
    List<PurApItem> getItems();

    @Override
    void setItems(List<PurApItem> items);

    String getVendorNumber();

    void setVendorNumber(String vendorNumber);

    Integer getVendorHeaderGeneratedIdentifier();

    void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier);

    Integer getVendorDetailAssignedIdentifier();

    void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier);

    String getVendorCustomerNumber();

    void setVendorCustomerNumber(String vendorCustomerNumber);

    Integer getPurapDocumentIdentifier();

    void setPurapDocumentIdentifier(Integer identifier);

    @Override
    String getApplicationDocumentStatus();

    @Override
    void setApplicationDocumentStatus(String appDocStatus);

    String getVendorCityName();

    void setVendorCityName(String vendorCityName);

    String getVendorCountryCode();

    void setVendorCountryCode(String vendorCountryCode);

    String getVendorLine1Address();

    void setVendorLine1Address(String vendorLine1Address);

    String getVendorLine2Address();

    void setVendorLine2Address(String vendorLine2Address);

    String getVendorName();

    void setVendorName(String vendorName);

    String getVendorPostalCode();

    void setVendorPostalCode(String vendorPostalCode);

    String getVendorStateCode();

    void setVendorStateCode(String vendorStateCode);

    String getVendorAddressInternationalProvinceName();

    void setVendorAddressInternationalProvinceName(String vendorAddressInternationalProvinceName);

    Integer getAccountsPayablePurchasingDocumentLinkIdentifier();

    void setAccountsPayablePurchasingDocumentLinkIdentifier(Integer accountsPayablePurchasingDocumentLinkIdentifier);

    Integer getVendorAddressGeneratedIdentifier();

    void setVendorAddressGeneratedIdentifier(Integer vendorAddressGeneratedIdentifier);

    boolean isUseTaxIndicator();

    void setUseTaxIndicator(boolean useTaxIndicator);

    void fixItemReferences();

    Date getTransactionTaxDate();

    PurApItem getTradeInItem();

    KualiDecimal getTotalDollarAmountForTradeIn();

    List<PurApItem> getTradeInItems();

    /**
     * Always returns true.
     * This method is needed here because it's called by some tag files shared with PurAp documents.
     *
     * @return true.
     */
    boolean getIsATypeOfPurAPRecDoc();

    /**
     * @return true if the document is a type of PurchasingDocument.
     */
    boolean getIsATypeOfPurDoc();

    /**
     * @return true if the document is a type of PurchaseOrderDocument (including its subclass documents).
     */
    boolean getIsATypeOfPODoc();

    /**
     * @return true if the document is a PurchaseOrderDocument (excluding its subclass documents).
     */
    boolean getIsPODoc();

    /**
     * @return true if the document is a RequisitionDocument.
     */
    boolean getIsReqsDoc();

    /**
     * @return whether the inquiry links should be rendered for Object Code and Sub Object Code.
     */
    boolean isInquiryRendered();

    boolean shouldGiveErrorForEmptyAccountsProration();

    boolean isCalculated();

    void setCalculated(boolean calculated);

    // KFSUPGRADE-346
    boolean isSensitive();
}
