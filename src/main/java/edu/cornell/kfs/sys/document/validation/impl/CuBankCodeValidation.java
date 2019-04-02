package edu.cornell.kfs.sys.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.impl.BankCodeValidation;
import org.kuali.kfs.sys.service.BankService;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuBankCodeValidation{

	protected static volatile BankService bankService;
    private static CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;

    public static boolean validate(Document document, String bankCode, String bankCodeProperty, boolean requireDeposit, boolean requireDisbursement) {
        if (document != null && !getBankService().isBankSpecificationEnabledForDocument(document.getClass())) {
            return true;
        }
        return validate(bankCode, bankCodeProperty, requireDeposit, requireDisbursement);
    }

    public static boolean validate(String bankCode, String bankCodeProperty, boolean requireDeposit, boolean requireDisbursement) {
        return validate(bankCode, bankCodeProperty, null, requireDeposit, requireDisbursement);
    }

    public static boolean validate(String bankCode, String bankCodeProperty, String paymentMethodCode, boolean requireDeposit, boolean requireDisbursement) {
        if (!getBankService().isBankSpecificationEnabled()) {
            return true;
        }

        boolean defaultValidationSucceeds = BankCodeValidation.validate(bankCode, bankCodeProperty, requireDeposit, requireDisbursement);
        if (!defaultValidationSucceeds) {
            return false;
        }

        if (StringUtils.isNotBlank(paymentMethodCode) && !checkBankCodePopulation(bankCode, paymentMethodCode, bankCodeProperty)) {
            return false;
        }

        return true;
    }

    public static boolean doesBankCodeNeedToBePopulated(String paymentMethodCode) {
        return ObjectUtils.isNotNull(getPaymentMethodGeneralLedgerPendingEntryService().getBankForPaymentMethod(paymentMethodCode));
    }

    public static boolean checkBankCodePopulation(String bankCode, String paymentMethodCode, String bankCodeProperty) {
        if (doesBankCodeNeedToBePopulated(paymentMethodCode)) {
            if (StringUtils.isBlank(bankCode)) {
                GlobalVariables.getMessageMap().putError(bankCodeProperty, CUKFSKeyConstants.ERROR_BANK_REQUIRED_PER_PAYMENT_METHOD, paymentMethodCode);
                return false;
            } else if (!doesBankExist(bankCode)) {
                GlobalVariables.getMessageMap().putError(bankCodeProperty, CUKFSKeyConstants.ERROR_BANK_INVALID, bankCode);
                return false;
            }
        } else if (StringUtils.isNotBlank(bankCode) && !doesBankExist(bankCode)) {
            GlobalVariables.getMessageMap().putError(bankCodeProperty, CUKFSKeyConstants.ERROR_BANK_INVALID, bankCode);
            return false;
        }
        return true;
    }

    protected static boolean doesBankExist(String bankCode) {
        return ObjectUtils.isNotNull(getBankService().getByPrimaryId(bankCode));
    }

    protected static CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        if (paymentMethodGeneralLedgerPendingEntryService == null) {
            paymentMethodGeneralLedgerPendingEntryService = SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class);
        }
        return paymentMethodGeneralLedgerPendingEntryService;
    }
    
    /**
     * @return the default implementation of the BankService
     */
    protected static BankService getBankService() {
        if (bankService == null) {
            bankService = SpringContext.getBean(BankService.class);
        }
        return bankService;
    }
    
    public static void setBankService(BankService bankService) {
        CuBankCodeValidation.bankService = bankService;
    }

}
