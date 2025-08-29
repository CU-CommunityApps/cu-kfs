package edu.cornell.kfs.module.purap.document.validation.impl;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PaymentRequestTaxAreaValidation;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;

public class CuPaymentRequestTaxAreaValidation extends PaymentRequestTaxAreaValidation {
	
    protected boolean validateTaxRates(final PaymentRequestDocument preq) {
        boolean valid = true;
        final String code = preq.getTaxClassificationCode();
        final BigDecimal fedrate = preq.getTaxFederalPercent();
        final BigDecimal strate = preq.getTaxStatePercent();
        final MessageMap errorMap = GlobalVariables.getMessageMap();

        // only test the cases when income class and tax rates aren't empty/N
        if (StringUtils.isEmpty(code) || StringUtils.equalsIgnoreCase(code, "N") || fedrate == null || strate == null) {
            return true;
        }

        // validate that the federal and state tax rates are among the allowed set
        final ArrayList<BigDecimal> fedrates = retrieveTaxRates(code, "F"); //(ArrayList<BigDecimal>) federalTaxRates.get(code);
        if (!listContainsValue(fedrates, fedrate)) {
            valid = false;
            errorMap.putError(PurapPropertyConstants.TAX_FEDERAL_PERCENT, PurapKeyConstants.ERROR_PAYMENT_REQUEST_TAX_FIELD_VALUE_INVALID_IF, PurapPropertyConstants.TAX_CLASSIFICATION_CODE, PurapPropertyConstants.TAX_FEDERAL_PERCENT);
        }
        final ArrayList<BigDecimal> strates = retrieveTaxRates(code, "S"); //(ArrayList<BigDecimal>) stateTaxRates.get(code);
        if (!listContainsValue(strates, strate)) {
            valid = false;
            errorMap.putError(PurapPropertyConstants.TAX_STATE_PERCENT, PurapKeyConstants.ERROR_PAYMENT_REQUEST_TAX_FIELD_VALUE_INVALID_IF, PurapPropertyConstants.TAX_CLASSIFICATION_CODE, PurapPropertyConstants.TAX_STATE_PERCENT);
        }

        // validate that the federal and state tax rate abide to certain relationship
        if (fedrate.compareTo(BigDecimal.ZERO) == 0 && strate.compareTo(BigDecimal.ZERO) != 0) {
            valid = false;
            errorMap.putError(PurapPropertyConstants.TAX_STATE_PERCENT, PurapKeyConstants.ERROR_PAYMENT_REQUEST_TAX_RATE_MUST_ZERO_IF, PurapPropertyConstants.TAX_FEDERAL_PERCENT, PurapPropertyConstants.TAX_STATE_PERCENT);
        }
        // KFSUPGRADE-779
//        boolean hasstrate = "F".equalsIgnoreCase(code) || "A".equalsIgnoreCase(code) || "O".equalsIgnoreCase(code);
//        if (fedrate.compareTo(new BigDecimal(0)) > 0 && strate.compareTo(new BigDecimal(0)) <= 0 && hasstrate) {
//            valid = false;
//            errorMap.putError(PurapPropertyConstants.TAX_STATE_PERCENT, PurapKeyConstants.ERROR_PAYMENT_REQUEST_TAX_RATE_MUST_NOT_ZERO_IF, PurapPropertyConstants.TAX_FEDERAL_PERCENT, PurapPropertyConstants.TAX_STATE_PERCENT);
//        }

        return valid;
    }

}
