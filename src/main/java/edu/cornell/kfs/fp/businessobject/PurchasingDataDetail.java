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

public class PurchasingDataDetail extends PurchasingDataBase {
    private static final long serialVersionUID = 1L;
    private String documentNumber;
    private Integer financialDocumentTransactionLineNumber;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getFinancialDocumentTransactionLineNumber() {
        return financialDocumentTransactionLineNumber;
    }

    public void setFinancialDocumentTransactionLineNumber(Integer financialDocumentTransactionLineNumber) {
        this.financialDocumentTransactionLineNumber = financialDocumentTransactionLineNumber;
    }

    public void setFinancialDocumentTransactionLineNumber(String financialDocumentTransactionLineNumber) {
        this.financialDocumentTransactionLineNumber = Integer.valueOf(financialDocumentTransactionLineNumber);
    }

    public void populateFromRecord(PurchasingDataRecord record) {
      this.setAccountNumber(record.getAccountNumber());
      this.setTsysTranCode(record.getTsysTranCode());
      this.setItemCommodityCode(record.getItemCommodityCode());
      this.setMerchantOrderNumber(record.getMerchantOrderNumber());
      this.setDiscountAmount(record.getDiscountAmount());
      this.setFreightShippingAmount(record.getFreightShippingAmount());
      this.setDutyAmount(record.getDutyAmount());
      this.setDestinationPostalZipCode(record.getDestinationPostalZipCode());
      this.setShipFromPostalZipCode(record.getShipFromPostalZipCode());
      this.setDestinationCountryCode(record.getDestinationCountryCode());
      this.setUniqueVATInvoice(record.getUniqueVATInvoice());
      this.setOrderDate(record.getOrderDate());
      this.setItemDescriptor(record.getItemDescriptor());
      this.setQuantity(record.getQuantity());
      this.setUnitOfMeasure(record.getUnitOfMeasure());
      this.setUnitCost(record.getUnitCost());
      this.setTypeOfSupply(record.getTypeOfSupply());
      this.setServiceIdentifier(record.getServiceIdentifier());
      this.setMessageIdentifier(record.getMessageIdentifier());
      this.setItemSequenceNumber(record.getItemSequenceNumber());
      this.setLineItemDetailIndicator(record.getLineItemDetailIndicator());
    }

}
