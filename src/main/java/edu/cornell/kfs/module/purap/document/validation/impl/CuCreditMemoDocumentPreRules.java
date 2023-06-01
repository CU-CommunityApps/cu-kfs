package edu.cornell.kfs.module.purap.document.validation.impl;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.validation.impl.CreditMemoDocumentPreRules;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;

import org.kuali.kfs.sys.businessobject.PaymentMethod;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.businessobject.CreditMemoWireTransfer;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.sys.CUKFSKeyConstants;


public class CuCreditMemoDocumentPreRules extends CreditMemoDocumentPreRules {
    
    @Override
    public boolean doPrompts(Document document){

        boolean preRulesOK = true;
        
        // KFSUPGRADE-779
        preRulesOK &= checkWireTransferTabState((VendorCreditMemoDocument) document);

        AccountsPayableDocument accountsPayableDocument = (AccountsPayableDocument) document;

        // Check if the total does not match the submitted credit if the document hasn't been completed.
        if (!SpringContext.getBean(PurapService.class).isFullDocumentEntryCompleted(accountsPayableDocument)) {
            preRulesOK = confirmInvoiceNoMatchOverride(accountsPayableDocument);
        }
        else if (SpringContext.getBean(PurapService.class).isFullDocumentEntryCompleted(accountsPayableDocument)) {
            // if past full document entry complete, then set override to true to skip validation
            accountsPayableDocument.setUnmatchedOverride(true);
        }

        return preRulesOK;
    }
	
	@SuppressWarnings("deprecation")
	@Override
    public boolean confirmInvoiceNoMatchOverride(AccountsPayableDocument accountsPayableDocument) {
    	// Check if the total does not match the submitted credit. 
    	GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.DOCUMENT);

    	
    	 // If the values are mismatched, suppress the prompt and post the error on the screen. 
         // Do not allow to override unmatched credit.
        if (validateInvoiceTotalsAreMismatched(accountsPayableDocument)) {
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.GRAND_TOTAL, PurapKeyConstants.ERROR_CREDIT_MEMO_INVOICE_AMOUNT_NONMATCH);
            event.setActionForwardName(KFSConstants.MAPPING_BASIC);
            return false;
        }    
          return true;  
    }
	
	// KFSUPGRADE-779
	protected boolean checkWireTransferTabState(VendorCreditMemoDocument cmDocument) {
        boolean tabStatesOK = true;

        CreditMemoWireTransfer cmWireTransfer = ((CuVendorCreditMemoDocument)cmDocument).getCmWireTransfer();

        // if payment method is CHECK and wire tab contains data, ask user to clear tab
        if (!StringUtils.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE, ((CuVendorCreditMemoDocument)cmDocument).getPaymentMethodCode()) && hasWireTransferValues(cmWireTransfer)) {
            String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.QUESTION_CLEAR_UNNEEDED_CM_WIRW_TAB);

            Object[] args = { CUPurapConstants.PAYMENT_METHOD, ((CuVendorCreditMemoDocument)cmDocument).getPaymentMethodCode(), CUPurapConstants.WIRE_TRANSFER, KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE };
            questionText = MessageFormat.format(questionText, args);

            boolean clearTab = super.askOrAnalyzeYesNoQuestion(KFSConstants.DisbursementVoucherDocumentConstants.CLEAR_WIRE_TRANSFER_TAB_QUESTION_ID, questionText);
            if (clearTab) {
                // NOTE: Can't replace with new instance because Foreign Draft uses same object
                clearWireTransferValues(cmWireTransfer);
            }
            else {
                // return to document if the user doesn't want to clear the Wire Transfer tab
                super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
                tabStatesOK = false;
            }
        }

        return tabStatesOK;
    }

    protected boolean hasWireTransferValues(CreditMemoWireTransfer cmWireTransfer) {
        boolean hasValues = false;

        // Checks each explicit field in the tab for user entered values
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmAutomatedClearingHouseProfileNumber());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmBankName());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmBankRoutingNumber());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmBankCityName());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmBankStateCode());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmBankCountryCode());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmPayeeAccountNumber());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmAttentionLineText());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmCurrencyTypeName());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmAdditionalWireText());
        hasValues |= StringUtils.isNotBlank(cmWireTransfer.getCmPayeeAccountName());

        return hasValues;
    }
    
    protected void clearWireTransferValues(CreditMemoWireTransfer cmWireTransfer) {
    	cmWireTransfer.setCmAutomatedClearingHouseProfileNumber(null);
    	cmWireTransfer.setCmBankName(null);
    	cmWireTransfer.setCmBankRoutingNumber(null);
    	cmWireTransfer.setCmBankCityName(null);
    	cmWireTransfer.setCmBankStateCode(null);
    	cmWireTransfer.setCmBankCountryCode(null);
    	cmWireTransfer.setCmPayeeAccountNumber(null);
    	cmWireTransfer.setCmAttentionLineText(null);
    	cmWireTransfer.setCmCurrencyTypeName(null);
    	cmWireTransfer.setCmAdditionalWireText(null);
    	cmWireTransfer.setCmPayeeAccountName(null);
    }
    
   // end KFSUPGRADE-779
    
  }
