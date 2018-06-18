/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherDocumentPreRules;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * Checks warnings and prompt conditions for dv document.
 */
public class CuDisbursementVoucherDocumentPreRules extends DisbursementVoucherDocumentPreRules {

    /**
     * Executes pre-rules for Disbursement Voucher Document
     *
     * @param document submitted document
     * @return true if pre-rules execute successfully
     * @see org.kuali.kfs.kns.rules.PromptBeforeValidationBase#doRules(org.kuali.kfs.kns.document.MaintenanceDocument)
     */
    @Override
    public boolean doPrompts(Document document) {
        boolean preRulesOK = super.doPrompts(document);

        setIncomeClassNonReportableForForeignVendorWithNoTaxReviewRequired(document);

        return preRulesOK;
    }

	private void setIncomeClassNonReportableForForeignVendorWithNoTaxReviewRequired(Document document) {
		DisbursementVoucherDocument dvDoc = (DisbursementVoucherDocument) document;
		DisbursementVoucherPayeeDetail dvPayeeDetail = dvDoc.getDvPayeeDetail();

		String payeeTypeCode = dvPayeeDetail.getDisbursementVoucherPayeeTypeCode();
		String paymentReasonCode = dvPayeeDetail.getDisbVchrPaymentReasonCode();
		Integer vendorHeaderId = dvPayeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger();

		if (getCuDisbursementVoucherTaxService().isForeignVendorAndTaxReviewNotRequired(payeeTypeCode,paymentReasonCode, vendorHeaderId)) {
			dvDoc.getDvNonResidentAlienTax().setIncomeClassCode(DisbursementVoucherConstants.NRA_TAX_INCOME_CLASS_NON_REPORTABLE);
		}
	}

	protected CuDisbursementVoucherTaxService getCuDisbursementVoucherTaxService(){
		return SpringContext.getBean(CuDisbursementVoucherTaxService.class);
	}

	/**
     * This method returns true if the state of all the tabs is valid, false otherwise.
     *
     * @param dvDocument submitted disbursement voucher document
     * @return Returns true if the state of all the tabs is valid, false otherwise.
     */
    @SuppressWarnings("deprecation")
    protected boolean checkWireTransferTabState(DisbursementVoucherDocument dvDocument) {
        boolean tabStatesOK = true;

        PaymentSourceWireTransfer dvWireTransfer = dvDocument.getWireTransfer();

        // if payment method is CHECK and wire tab contains data, ask user to clear tab
        if ((StringUtils.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK,
                dvDocument.getDisbVchrPaymentMethodCode()) || StringUtils.equals(
                KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT,
                dvDocument.getDisbVchrPaymentMethodCode())) && hasWireTransferValues(dvWireTransfer)) {
            String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                    CUKFSKeyConstants.QUESTION_CLEAR_UNNEEDED_WIRE_TAB);
            
            boolean clearTab = super.askOrAnalyzeYesNoQuestion(
                    KFSConstants.DisbursementVoucherDocumentConstants.CLEAR_WIRE_TRANSFER_TAB_QUESTION_ID,
                    questionText);
            if (clearTab) {
                // NOTE: Can't replace with new instance because Foreign Draft uses same object
                clearWireTransferValues(dvWireTransfer);
            } else {
                // return to document if the user doesn't want to clear the Wire Transfer tab
                super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
                tabStatesOK = false;
            }
        }

        return tabStatesOK;
    }

}
