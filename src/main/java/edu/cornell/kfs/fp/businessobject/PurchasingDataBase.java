/*
 * Copyright 2016 The Kuali Foundation.
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
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class PurchasingDataBase extends USBankAddendumRecord {
    private static final long serialVersionUID = 1L;
    private Integer purchasingDataId;
    private String accountNumber;
    private String tsysTranCode;
    private String itemCommodityCode;
    private String merchantOrderNumber;
    private KualiDecimal discountAmount;
    private KualiDecimal freightShippingAmount;
    private KualiDecimal dutyAmount;
    private String destinationPostalZipCode;
    private String shipFromPostalZipCode;
    private String destinationCountryCode;
    private String uniqueVATInvoice;
    private Date orderDate;
    private String itemDescriptor;
    private Integer quantity;
    private String unitOfMeasure;
    private KualiDecimal unitCost;
    private String typeOfSupply;
    private String serviceIdentifier;
    private String messageIdentifier;
    private Integer itemSequenceNumber;
    private Integer lineItemDetailIndicator;
    

    public PurchasingDataBase() {
      super();

      this.discountAmount = new KualiDecimal(0);
      this.freightShippingAmount = new KualiDecimal(0);
      this.dutyAmount = new KualiDecimal(0);
      this.unitCost = new KualiDecimal(0);
    }

    /**
     * @return the purchasingDataId
     */
    public Integer getPurchasingDataId() {
      return purchasingDataId;
    }

    /**
     * @param purchasingDataId the purchasingDataId to set
     */
    public void setPurchasingDataId(Integer purchasingDataId) {
      this.purchasingDataId = purchasingDataId;
    }
    
    /**
     * @return the accountNumber
     */
    public String getAccountNumber() {
      return accountNumber;
    }

    /**
     * @param accountNumber the accountNumber to set
     */
    public void setAccountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
    }

    /**
     * @return the tsysTranCode
     */
    public String getTsysTranCode() {
      return tsysTranCode;
    }

    /**
     * @param tsysTranCode the tsysTranCode to set
     */
    public void setTsysTranCode(String tsysTranCode) {
      this.tsysTranCode = tsysTranCode;
    }

    /**
     * @return the itemCommodityCode
     */
    public String getItemCommodityCode() {
      return itemCommodityCode;
    }

    /**
     * @param itemCommodityCode the itemCommodityCode to set
     */
    public void setItemCommodityCode(String itemCommodityCode) {
      this.itemCommodityCode = itemCommodityCode;
    }

    /**
     * @return the merchantOrderNumber
     */
    public String getMerchantOrderNumber() {
      return merchantOrderNumber;
    }

    /**
     * @param merchantOrderNumber the merchantOrderNumber to set
     */
    public void setMerchantOrderNumber(String merchantOrderNumber) {
      this.merchantOrderNumber = merchantOrderNumber;
    }

    /**
     * @return the discountAmount
     */
    public KualiDecimal getDiscountAmount() {
      return discountAmount;
    }

    /**
     * @param discountAmount the discountAmount to set
     */
    public void setDiscountAmount(KualiDecimal discountAmount) {
      this.discountAmount = discountAmount;
    }

    /**
     * @param discountAmount the discountAmount to set
     */
    public void setDiscountAmount(String discountAmount) {
      if (StringUtils.isNotBlank(discountAmount)) {
        this.discountAmount = new KualiDecimal(discountAmount);
      } else {
        this.discountAmount = KualiDecimal.ZERO;
      }
    }

    /**
     * @return the freightShippingAmount
     */
    public KualiDecimal getFreightShippingAmount() {
      return freightShippingAmount;
    }

    /**
     * @param freightShippingAmount the freightShippingAmount to set
     */
    public void setFreightShippingAmount(String freightShippingAmount) {
      if (StringUtils.isNotBlank(freightShippingAmount)) {
        this.freightShippingAmount = new KualiDecimal(freightShippingAmount);
      } else {
        this.freightShippingAmount = KualiDecimal.ZERO;
      }
    }

    /**
     * @param freightShippingAmount the freightShippingAmount to set
     */
    public void setFreightShippingAmount(KualiDecimal freightShippingAmount) {
      this.freightShippingAmount = freightShippingAmount;
    }

    /**
     * @return the dutyAmount
     */
    public KualiDecimal getDutyAmount() {
      return dutyAmount;
    }

    /**
     * @param dutyAmount the dutyAmount to set
     */
    public void setDutyAmount(KualiDecimal dutyAmount) {
      this.dutyAmount = dutyAmount;
    }

    /**
     * @param freightShippingAmount the freightShippingAmount to set
     */
    public void setDutyAmount(String dutyAmount) {
      if (StringUtils.isNotBlank(dutyAmount)) {
        this.dutyAmount = new KualiDecimal(dutyAmount);
      } else {
        this.dutyAmount = KualiDecimal.ZERO;
      }
    }

    /**
     * @return the destinationPostalZipCode
     */
    public String getDestinationPostalZipCode() {
      return destinationPostalZipCode;
    }

    /**
     * @param destinationPostalZipCode the destinationPostalZipCode to set
     */
    public void setDestinationPostalZipCode(String destinationPostalZipCode) {
      this.destinationPostalZipCode = destinationPostalZipCode;
    }

    /**
     * @return the shipFromPostalZipCode
     */
    public String getShipFromPostalZipCode() {
      return shipFromPostalZipCode;
    }

    /**
     * @param shipFromPostalZipCode the shipFromPostalZipCode to set
     */
    public void setShipFromPostalZipCode(String shipFromPostalZipCode) {
      this.shipFromPostalZipCode = shipFromPostalZipCode;
    }

    /**
     * @return the destinationCountryCode
     */
    public String getDestinationCountryCode() {
      return destinationCountryCode;
    }

    /**
     * @param destinationCountryCode the destinationCountryCode to set
     */
    public void setDestinationCountryCode(String destinationCountryCode) {
      this.destinationCountryCode = destinationCountryCode;
    }

    /**
     * @return the uniqueVATInvoice
     */
    public String getUniqueVATInvoice() {
      return uniqueVATInvoice;
    }

    /**
     * @param uniqueVATInvoice the uniqueVATInvoice to set
     */
    public void setUniqueVATInvoice(String uniqueVATInvoice) {
      this.uniqueVATInvoice = uniqueVATInvoice;
    }

    /**
     * @return the orderDate
     */
    public Date getOrderDate() {
      return orderDate;
    }

    /**
     * @param orderDate the orderDate to set
     */
    public void setOrderDate(Date orderDate) {
      this.orderDate = orderDate;
    }

    /**
     * @return the itemDescriptor
     */
    public String getItemDescriptor() {
      return itemDescriptor;
    }

    /**
     * @param itemDescriptor the itemDescriptor to set
     */
    public void setItemDescriptor(String itemDescriptor) {
      this.itemDescriptor = itemDescriptor;
    }

    /**
     * @return the quantity
     */
    public Integer getQuantity() {
      return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(Integer quantity) {
      this.quantity = quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(String quantity) {
      if (StringUtils.isNotBlank(quantity)) {
        this.quantity = Integer.valueOf(quantity);
      } else {
        this.quantity = 0;
      }
    }

    /**
     * @return the unitOfMeasure
     */
    public String getUnitOfMeasure() {
      return unitOfMeasure;
    }

    /**
     * @param unitOfMeasure the unitOfMeasure to set
     */
    public void setUnitOfMeasure(String unitOfMeasure) {
      this.unitOfMeasure = unitOfMeasure;
    }

    /**
     * @return the unitCost
     */
    public KualiDecimal getUnitCost() {
      return unitCost;
    }

    /**
     * @param unitCost the unitCost to set
     */
    public void setUnitCost(KualiDecimal unitCost) {
      this.unitCost = unitCost;
    }

    /**
     * @param unitCost the unitCost to set
     */
    public void setUnitCost(String unitCost) {
      if (StringUtils.isNotBlank(unitCost)) {
        this.unitCost = new KualiDecimal(unitCost);
      } else {
        this.unitCost = KualiDecimal.ZERO;
      }
    }

    /**
     * @return the typeOfSupply
     */
    public String getTypeOfSupply() {
      return typeOfSupply;
    }

    /**
     * @param typeOfSupply the typeOfSupply to set
     */
    public void setTypeOfSupply(String typeOfSupply) {
      this.typeOfSupply = typeOfSupply;
    }

    /**
     * @return the serviceIdentifier
     */
    public String getServiceIdentifier() {
      return serviceIdentifier;
    }

    /**
     * @param serviceIdentifier the serviceIdentifier to set
     */
    public void setServiceIdentifier(String serviceIdentifier) {
      this.serviceIdentifier = serviceIdentifier;
    }

    /**
     * @return the messageIdentifier
     */
    public String getMessageIdentifier() {
      return messageIdentifier;
    }

    /**
     * @param messageIdentifier the messageIdentifier to set
     */
    public void setMessageIdentifier(String messageIdentifier) {
      this.messageIdentifier = messageIdentifier;
    }

    /**
     * @return the itemSequenceNumber
     */
    public Integer getItemSequenceNumber() {
      return itemSequenceNumber;
    }

    /**
     * @param itemSequenceNumber the itemSequenceNumber to set
     */
    public void setItemSequenceNumber(Integer itemSequenceNumber) {
      this.itemSequenceNumber = itemSequenceNumber;
    }

    /**
     * @param itemSequenceNumber the itemSequenceNumber to set
     */
    public void setItemSequenceNumber(String itemSequenceNumber) {
      if (StringUtils.isNotBlank(itemSequenceNumber)) {
        this.itemSequenceNumber = Integer.valueOf(itemSequenceNumber);
      } else {
        this.itemSequenceNumber = 0;
      }
    }

    /**
     * @return the lineItemDetailIndicator
     */
    public Integer getLineItemDetailIndicator() {
      return lineItemDetailIndicator;
    }

    /**
     * Documentation indicates that this must be one of two values:
     * 1=Last line item detail record
     * 2=Other line item detail record
     * 
     * However, review of the file indicates that we actually observe:
     * 0=Last line item detail record
     * 1=Other line item detail record
     * 
     * @param lineItemDetailIndicator the lineItemDetailIndicator to set
     */
    public void setLineItemDetailIndicator(Integer lineItemDetailIndicator) {
      this.lineItemDetailIndicator = lineItemDetailIndicator;  
    }

    /**
     * @param lineItemDetailIndicator the lineItemDetailIndicator to set
     */
    public void setLineItemDetailIndicator(String lineItemDetailIndicator) {
      this.setLineItemDetailIndicator(Integer.valueOf(lineItemDetailIndicator));
    }
    
    /**
     * Parses a supposed Type 50 record addendum line
     * 
     * @param line The current line
     * @param lineCount The current line number
     * @throws ParseException When there is a string parsing error or a missing required field
     */
    public void parse(String line, int lineCount) throws ParseException {
      // Lines with a * indicate data that the documentation states will be available in the future
      this.setRecordId(USBankRecordFieldUtils.extractNormalizedString(line, 0, 2, true, lineCount));
      this.setAccountNumber(USBankRecordFieldUtils.extractNormalizedString(line, 2, 18, true, lineCount));
      this.setTsysTranCode(USBankRecordFieldUtils.extractNormalizedString(line, 18, 22, true, lineCount));
      this.setItemCommodityCode(USBankRecordFieldUtils.extractNormalizedString(line, 22, 37));
      this.setMerchantOrderNumber(USBankRecordFieldUtils.extractNormalizedString(line, 37, 49)); // *
      this.setDiscountAmount(USBankRecordFieldUtils.extractNormalizedString(line, 49, 61));
      this.setFreightShippingAmount(USBankRecordFieldUtils.extractNormalizedString(line, 61, 73));
      this.setDutyAmount(USBankRecordFieldUtils.extractNormalizedString(line, 73, 85));
      this.setDestinationPostalZipCode(USBankRecordFieldUtils.extractNormalizedString(line, 85, 94));
      this.setShipFromPostalZipCode(USBankRecordFieldUtils.extractNormalizedString(line, 94, 103));
      this.setDestinationCountryCode(USBankRecordFieldUtils.extractNormalizedString(line, 103, 106));
      this.setUniqueVATInvoice(USBankRecordFieldUtils.extractNormalizedString(line, 106, 121)); // typically not included in the US
      this.setOrderDate(USBankRecordFieldUtils.extractDate(line, 121, 129, lineCount));
      this.setItemDescriptor(USBankRecordFieldUtils.extractNormalizedString(line, 129, 155));
      this.setQuantity(USBankRecordFieldUtils.extractNormalizedString(line, 155, 165));
      this.setUnitOfMeasure(USBankRecordFieldUtils.extractNormalizedString(line, 165, 175));
      this.setUnitCost(USBankRecordFieldUtils.extractNormalizedString(line, 175, 187));
      this.setTypeOfSupply(USBankRecordFieldUtils.extractNormalizedString(line, 187, 189)); // *
      this.setServiceIdentifier(USBankRecordFieldUtils.extractNormalizedString(line, 189, 195)); // *
      
      // Message Identifier should "match that of the related draft data transaction's Message Identifier field." 
      // Not requiring it for now because it doesn't seem like we process draft data...
      this.setMessageIdentifier(USBankRecordFieldUtils.extractNormalizedString(line, 195, 210));
      
      this.setItemSequenceNumber(USBankRecordFieldUtils.extractNormalizedString(line, 210, 213));
      this.setLineItemDetailIndicator(USBankRecordFieldUtils.extractNormalizedString(line, 213, 214));
    }
    
}
