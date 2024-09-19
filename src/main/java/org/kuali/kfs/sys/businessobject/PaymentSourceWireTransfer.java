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
package org.kuali.kfs.sys.businessobject;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.sys.businessobject.PaymentSourceWireTransferExtendedAttribute;

/**
 * Cornell Customizations: 
 *     Added method toStringMapper to KualiCo 2024-04-19 version of this class
 *     to support credit memo payment method additional data processing.
 *     
 *     Add extended attribute to the business object when the extended attribute does not exist.
 *     Set table primary key of extended attribute to same primary key value as main business object
 *     when it is null. Both will prevent stacktrace when insert of extension is attempted.
 */

/**
 * This class is used to represent a disbursement voucher wire transfer.
 */
public class PaymentSourceWireTransfer extends PersistableBusinessObjectBase {

    private String documentNumber;
    private String bankName;
    private String bankRoutingNumber;
    private String bankCityName;
    private String bankStateCode;
    private String bankCountryCode;
    private String attentionLineText;
    private String additionalWireText;
    private String payeeAccountNumber;
    private String currencyTypeName;
    private String currencyTypeCode;
    private boolean wireTransferFeeWaiverIndicator;
    private String payeeAccountName;
    private String automatedClearingHouseProfileNumber;
    private String foreignCurrencyTypeName;
    private String foreignCurrencyTypeCode;

    public PaymentSourceWireTransfer() {
        wireTransferFeeWaiverIndicator = false;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(final String disbursementVoucherBankName) {
        bankName = disbursementVoucherBankName;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(final String disbVchrBankRoutingNumber) {
        bankRoutingNumber = disbVchrBankRoutingNumber;
    }

    public String getBankCityName() {
        return bankCityName;
    }

    public void setBankCityName(final String disbVchrBankCityName) {
        bankCityName = disbVchrBankCityName;
    }

    public String getBankStateCode() {
        return bankStateCode;
    }

    public void setBankStateCode(final String disbVchrBankStateCode) {
        bankStateCode = disbVchrBankStateCode;
    }

    public String getBankCountryCode() {
        return bankCountryCode;
    }

    public void setBankCountryCode(final String disbVchrBankCountryCode) {
        bankCountryCode = disbVchrBankCountryCode;
    }

    public String getAttentionLineText() {
        return attentionLineText;
    }

    public void setAttentionLineText(final String disbVchrAttentionLineText) {
        attentionLineText = disbVchrAttentionLineText;
    }

    public String getAdditionalWireText() {
        return additionalWireText;
    }

    public void setAdditionalWireText(final String disbVchrAdditionalWireText) {
        additionalWireText = disbVchrAdditionalWireText;
    }

    public String getPayeeAccountNumber() {
        return payeeAccountNumber;
    }

    public void setPayeeAccountNumber(final String disbVchrPayeeAccountNumber) {
        payeeAccountNumber = disbVchrPayeeAccountNumber;
    }

    public String getCurrencyTypeName() {
        return currencyTypeName;
    }

    public void setCurrencyTypeName(final String disbVchrCurrencyTypeName) {
        currencyTypeName = disbVchrCurrencyTypeName;
    }

    /**
     * Gets the foreignCurrencyTypeName attribute. This field is here because the currency type field is
     * presented in different places on screen, and value conflicts occur unless we have an alias.
     *
     * @return Returns the foreignCurrencyTypeName
     */
    public String getForeignCurrencyTypeName() {
        return foreignCurrencyTypeName;
    }

    /**
     * Sets the foreignCurrencyTypeName attribute. This field is here because the currency type field is
     * presented in different places on screen, and value conflicts occur unless we have an alias.
     *
     * @param disbursementVoucherForeignCurrencyTypeName The foreignCurrencyTypeName to set.
     */
    public void setForeignCurrencyTypeName(final String disbursementVoucherForeignCurrencyTypeName) {
        foreignCurrencyTypeName = disbursementVoucherForeignCurrencyTypeName;
    }

    public String getCurrencyTypeCode() {
        return currencyTypeCode;
    }

    public void setCurrencyTypeCode(final String disbVchrCurrencyTypeCode) {
        currencyTypeCode = disbVchrCurrencyTypeCode;
    }

    /**
     * Gets the foreignCurrencyTypeCode attribute. This field is here because the currency type field is
     * presented in different places on screen, and value conflicts occur unless we have an alias.
     *
     * @return Returns the foreignCurrencyTypeCode
     */
    public String getForeignCurrencyTypeCode() {
        return foreignCurrencyTypeCode;
    }

    /**
     * Sets the foreignCurrencyTypeCode attribute. This field is here because the currency type field is
     * presented in different places on screen, and value conflicts occur unless we have an alias.
     *
     * @param disbursementVoucherForeignCurrencyTypeCode The foreignCurrencyTypeCode to set.
     */
    public void setForeignCurrencyTypeCode(final String disbursementVoucherForeignCurrencyTypeCode) {
        foreignCurrencyTypeCode = disbursementVoucherForeignCurrencyTypeCode;
    }

    public boolean isWireTransferFeeWaiverIndicator() {
        return wireTransferFeeWaiverIndicator;
    }

    public void setWireTransferFeeWaiverIndicator(final boolean disbursementVoucherWireTransferFeeWaiverIndicator) {
        wireTransferFeeWaiverIndicator = disbursementVoucherWireTransferFeeWaiverIndicator;
    }

    public String getPayeeAccountName() {
        return payeeAccountName;
    }

    public void setPayeeAccountName(final String disbursementVoucherPayeeAccountName) {
        payeeAccountName = disbursementVoucherPayeeAccountName;
    }

    public String getAutomatedClearingHouseProfileNumber() {
        return automatedClearingHouseProfileNumber;
    }

    public void setAutomatedClearingHouseProfileNumber(final String disbursementVoucherAutomatedClearingHouseProfileNumber) {
        automatedClearingHouseProfileNumber = disbursementVoucherAutomatedClearingHouseProfileNumber;
    }

    public void setDisbVchrForeignBankIndicatorName(final String name) {
    }
    
    /*
     * Cornell Customization: 
     *     Added method toStringMapper to KualiCo 2024-04-19 version of this class
     *     to support credit memo payment method additional data processing.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put(KFSPropertyConstants.DOCUMENT_NUMBER, this.documentNumber);
        return m;
    }
    
    /*
     * Cornell Customization:
     *    Add extended attribute to the business object when the extended attribute does not exist.
     *    Set table primary key of extended attribute to same primary key value as main business object
     *    when it is null. Both will prevent stacktrace when insert of extension is attempted.
     */
    @Override
    public void beforeInsert() {
        super.beforeInsert();
        if (ObjectUtils.isNull(this.getExtension())) {
            PaymentSourceWireTransferExtendedAttribute paymentSourceWireTransferExtendedAttribute = new PaymentSourceWireTransferExtendedAttribute();
            paymentSourceWireTransferExtendedAttribute.setDocumentNumber(this.getDocumentNumber());
            this.setExtension(paymentSourceWireTransferExtendedAttribute);
        }
        PaymentSourceWireTransferExtendedAttribute boExtension = (PaymentSourceWireTransferExtendedAttribute)(this.getExtension());
        if (StringUtils.isBlank(boExtension.getDocumentNumber())) {
            boExtension.setDocumentNumber(this.getDocumentNumber());
        }
    }

}
