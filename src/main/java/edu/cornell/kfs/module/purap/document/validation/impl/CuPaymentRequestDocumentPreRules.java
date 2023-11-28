package edu.cornell.kfs.module.purap.document.validation.impl;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PaymentRequestDocumentPreRules;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.businessobject.PaymentRequestWireTransfer;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.pdp.service.CuCheckStubService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuPaymentRequestDocumentPreRules extends PaymentRequestDocumentPreRules {

    private CuCheckStubService cuCheckStubService;

	@Override
	public boolean doPrompts(final Document document) {
		boolean preRulesOK = true;
		final PaymentRequestDocument preq = (PaymentRequestDocument) document;
		
		preRulesOK &= checkWireTransferTabState(preq);
		preRulesOK &= getCuCheckStubService().performPreRulesValidationOfIso20022CheckStubLength(document, this);
		preRulesOK &= super.doPrompts(document);
		return preRulesOK;
	}
	
	 protected boolean checkWireTransferTabState(final PaymentRequestDocument preqDocument) {
	        boolean tabStatesOK = true;

	        final PaymentRequestWireTransfer preqWireTransfer = ((CuPaymentRequestDocument)preqDocument).getPreqWireTransfer();

	        // if payment method is CHECK and wire tab contains data, ask user to clear tab
	        if (!StringUtils.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE, ((CuPaymentRequestDocument)preqDocument).getPaymentMethodCode()) && hasWireTransferValues(preqWireTransfer)) {
	            String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.QUESTION_CLEAR_UNNEEDED_WIRW_TAB);

	            final Object[] args = { "payment method", ((CuPaymentRequestDocument)preqDocument).getPaymentMethodCode(), "Wire Transfer", KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE };
	            questionText = MessageFormat.format(questionText, args);

	            final boolean clearTab = super.askOrAnalyzeYesNoQuestion(KFSConstants.DisbursementVoucherDocumentConstants.CLEAR_WIRE_TRANSFER_TAB_QUESTION_ID, questionText);
	            if (clearTab) {
	                // NOTE: Can't replace with new instance because Foreign Draft uses same object
	                clearWireTransferValues(preqWireTransfer);
	            }
	            else {
	                // return to document if the user doesn't want to clear the Wire Transfer tab
	                super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
	                tabStatesOK = false;
	            }
	        }

	        return tabStatesOK;
	    }

	    protected boolean hasWireTransferValues(final PaymentRequestWireTransfer preqWireTransfer) {
	        boolean hasValues = false;

	        // Checks each explicit field in the tab for user entered values
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqAutomatedClearingHouseProfileNumber());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankName());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankRoutingNumber());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankCityName());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankStateCode());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankCountryCode());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqPayeeAccountNumber());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqAttentionLineText());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqCurrencyTypeName());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqAdditionalWireText());
	        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqPayeeAccountName());

	        return hasValues;
	    }
	    protected void clearWireTransferValues(final PaymentRequestWireTransfer preqWireTransfer) {
	        preqWireTransfer.setPreqAutomatedClearingHouseProfileNumber(null);
	        preqWireTransfer.setPreqBankName(null);
	        preqWireTransfer.setPreqBankRoutingNumber(null);
	        preqWireTransfer.setPreqBankCityName(null);
	        preqWireTransfer.setPreqBankStateCode(null);
	        preqWireTransfer.setPreqBankCountryCode(null);
	        preqWireTransfer.setPreqPayeeAccountNumber(null);
	        preqWireTransfer.setPreqAttentionLineText(null);
	        preqWireTransfer.setPreqCurrencyTypeName(null);
	        preqWireTransfer.setPreqAdditionalWireText(null);
	        preqWireTransfer.setPreqPayeeAccountName(null);
	    }

    public CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }

}
