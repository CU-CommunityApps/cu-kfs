/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.sys.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.service.DictionaryValidationService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.PaymentSource;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.PaymentSourceWireTransferExtendedAttribute;

/*
 * CU Customization: Added required-field handling for Correspondent Bank fields.
 */
@SuppressWarnings("deprecation")
public class PaymentSourceWireTransferValidation extends GenericValidation {

    private static final Logger LOG = LogManager.getLogger();

    static final String BANK_COUNTRY_CODE = "bankCountryCode";
    static final String PAYEE_ACCOUNT_NAME = "payeeAccountName";

    private String checkAttachmentPropertyPath =
            KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.DISB_VCHR_ATTACHMENT_CODE;
    private AccountingDocument accountingDocumentForValidation;
    private DataDictionaryService dataDictionaryService;
    private DictionaryValidationService dictionaryValidationService;

    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        LOG.debug("validate start");

        final PaymentSource document = (PaymentSource) accountingDocumentForValidation;
        final PaymentSourceWireTransfer wireTransfer = document.getWireTransfer();

        if (!KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equals(document.getPaymentMethodCode())) {
            return true;
        }

        final MessageMap errors = GlobalVariables.getMessageMap();
        errors.addToErrorPath(KFSPropertyConstants.DOCUMENT);
        errors.addToErrorPath(KFSPropertyConstants.PAYMENT_SOURCE_WIRE_TRANSFER);

        boolean isValid = true;
        if (shouldFieldsBeRequired()) {
            isValid &= validateRequired(wireTransfer.getBankName(), KFSPropertyConstants.BANK_NAME);
            isValid &= validateRequired(wireTransfer.getBankCityName(), KFSPropertyConstants.BANK_CITY_NAME);
            isValid &= validateRequired(wireTransfer.getBankCountryCode(), BANK_COUNTRY_CODE);
            isValid &= validateRequired(wireTransfer.getPayeeAccountName(), PAYEE_ACCOUNT_NAME);
            isValid &= validateRequired(
                    wireTransfer.getPayeeAccountNumber(),
                    KFSPropertyConstants.PAYEE_ACCOUNT_NUMBER);
            isValid &= validateRequired(wireTransfer.getCurrencyTypeCode(), KFSPropertyConstants.CURRENCY_TYPE_CODE);
            isValid &= validateRequired(wireTransfer.getCurrencyTypeName(), KFSPropertyConstants.CURRENCY_TYPE_NAME);
            // CU Customization: Added section for validating Correspondent Bank fields.
            final PaymentSourceWireTransferExtendedAttribute wireTransferExtension
                    = (PaymentSourceWireTransferExtendedAttribute) wireTransfer.getExtension();
            if (ObjectUtils.isNotNull(wireTransferExtension)
                    && shouldCorrespondentBankFieldsBeRequired(wireTransferExtension)) {
                errors.addToErrorPath(KFSPropertyConstants.EXTENSION);

                isValid &= validateRequiredForCorrespondentBank(wireTransferExtension.getCorrespondentBankName(),
                        CUKFSPropertyConstants.CORRESPONDENT_BANK_NAME);
                isValid &= validateRequiredForCorrespondentBank(wireTransferExtension.getCorrespondentBankCityName(),
                        CUKFSPropertyConstants.CORRESPONDENT_BANK_CITY_NAME);
                isValid &= validateRequiredForCorrespondentBank(wireTransferExtension.getCorrespondentBankCountryCode(),
                        CUKFSPropertyConstants.CORRESPONDENT_BANK_COUNTRY_CODE);
                isValid &= validateRequiredForCorrespondentBank(wireTransferExtension.getCorrespondentBankSwiftCode(),
                        CUKFSPropertyConstants.CORRESPONDENT_BANK_SWIFT_CODE);

                errors.removeFromErrorPath(KFSPropertyConstants.EXTENSION);
            }
        }
        dictionaryValidationService.validateBusinessObject(wireTransfer);

        // The remaining checks are applied in any case as they validation interactions between the properties

        if (KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(wireTransfer.getBankCountryCode())
                && StringUtils.isBlank(wireTransfer.getBankRoutingNumber())) {
            errors.putError(KFSPropertyConstants.BANK_ROUTING_NUMBER,
                    KFSKeyConstants.ERROR_PAYMENT_SOURCE_BANK_ROUTING_NUMBER);
            isValid = false;
        }

        if (KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(wireTransfer.getBankCountryCode())
                && StringUtils.isBlank(wireTransfer.getBankStateCode())) {
            errors.putError(KFSPropertyConstants.BANK_STATE_CODE, KFSKeyConstants.ERROR_REQUIRED, "Bank State");
            isValid = false;
        }

        /* cannot have attachment checked for wire transfer */
        if (document.hasAttachment()) {
            errors.putErrorWithoutFullErrorPath(
                    checkAttachmentPropertyPath,
                    KFSKeyConstants.ERROR_PAYMENT_SOURCE_WIRE_ATTACHMENT);
            isValid = false;
        }

        errors.removeFromErrorPath(KFSPropertyConstants.PAYMENT_SOURCE_WIRE_TRANSFER);
        errors.removeFromErrorPath(KFSPropertyConstants.DOCUMENT);

        return isValid;
    }

    private boolean validateRequired(final String value, final String attributeName) {
        if (StringUtils.isBlank(value)) {
            GlobalVariables.getMessageMap().putError(
                    attributeName,
                    KFSKeyConstants.ERROR_REQUIRED,
                    dataDictionaryService.getAttributeLabel(PaymentSourceWireTransfer.class, attributeName));
            return false;
        }
        return true;
    }

    /**
     * This method is invoked by {@link #validate(AttributedDocumentEvent)} in order to determine if we should check
     * that certain fields are not blank on the wire transfer.  This allows subclasses to bypass that check.
     * @return  true if the fields should be checked, false otherwise
     */
    protected boolean shouldFieldsBeRequired() {
        return true;
    }

    /*
     * CU Customization: Added method for checking whether Correspondent Bank fields should be required
     * (which is the case when one or more Correspondent Bank fields have been filled out).
     */
    private boolean shouldCorrespondentBankFieldsBeRequired(
            final PaymentSourceWireTransferExtendedAttribute wireTransferExtension) {
        return !StringUtils.isAllBlank(
                wireTransferExtension.getCorrespondentBankName(),
                wireTransferExtension.getCorrespondentBankAddress(),
                wireTransferExtension.getCorrespondentBankCityName(),
                wireTransferExtension.getCorrespondentBankStateCode(),
                wireTransferExtension.getCorrespondentBankProvince(),
                wireTransferExtension.getCorrespondentBankCountryCode(),
                wireTransferExtension.getCorrespondentBankRoutingNumber(),
                wireTransferExtension.getCorrespondentBankAccountNumber(),
                wireTransferExtension.getCorrespondentBankSwiftCode()
        );
    }

    /*
     * CU Customization: Added method for checking whether a required Correspondent Bank field has been filled in.
     */
    private boolean validateRequiredForCorrespondentBank(final String value, final String attributeName) {
        if (StringUtils.isBlank(value)) {
            GlobalVariables.getMessageMap().putError(
                    attributeName,
                    CUKFSKeyConstants.ERROR_REQUIRED_CORRESPONDENT_BANK,
                    dataDictionaryService.getAttributeLabel(
                            PaymentSourceWireTransferExtendedAttribute.class, attributeName));
            return false;
        }
        return true;
    }

    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    public void setCheckAttachmentPropertyPath(final String checkAttachmentPropertyPath) {
        this.checkAttachmentPropertyPath = checkAttachmentPropertyPath;
    }

    public void setAccountingDocumentForValidation(final AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setDictionaryValidationService(final DictionaryValidationService dictionaryValidationService) {
        this.dictionaryValidationService = dictionaryValidationService;
    }
}
