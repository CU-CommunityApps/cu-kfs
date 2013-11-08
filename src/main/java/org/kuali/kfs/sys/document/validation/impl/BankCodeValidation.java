/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.kfs.sys.document.validation.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;

/**
 * Performs bank code validation.
 * 
 * Modified for MOD-PA2000-01 (KITT-592) to add conditional validation based on the payment method code
 * 
 */
public class BankCodeValidation {

    protected static final String WARNING_BANK_NOT_REQUIRED = "warning.document.disbursementvoucher.bank.not.required";
    protected static final String ERROR_BANK_REQUIRED_PER_PAYMENT_METHOD = "error.document.disbursementvoucher.bank.required";
    // KFSPTS-1891 Mods
    private static CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;

    /**
     * Performs required, exists, and active validation of bank code. Also validates bank for deposit or disbursement indicator if
     * requested. .
     * 
     * @param bankCode value to validate
     * @param bankCodeProperty property to associate errors with
     * @param requireDeposit true if the bank code should support deposits
     * @param requireDisbursement true if the bank code should support disbursements
     * @return true if bank code passes all validations, false if any fail
     */
    public static boolean validate(String bankCode, String bankCodeProperty, boolean requireDeposit, boolean requireDisbursement) {
        return validate(bankCode, bankCodeProperty, null, requireDeposit, requireDisbursement);
    }
    
    /**
     * Performs required, exists, and active validation of bank code. Also validates bank for deposit or disbursement indicator if
     * requested. .
     * 
     * @param bankCode value to validate
     * @param bankCodeProperty property to associate errors with
     * @param required true if the bank code is required
     * @param requireDeposit true if the bank code should support deposits
     * @param requireDisbursement true if the bank code should support disbursements
     * @return true if bank code passes all validations, false if any fail
     */
    public static boolean validate(String bankCode, String bankCodeProperty, String paymentMethodCode, boolean requireDeposit, boolean requireDisbursement) {

        // if bank specification is not enabled, no need to validate bank code
        if (!SpringContext.getBean(BankService.class).isBankSpecificationEnabled()) {
            return true;
        }

        Bank bank = SpringContext.getBean(BankService.class).getByPrimaryId(bankCode);
        // required check
        // if the payment method code is blank, then revert to the baseline behavior
        if (StringUtils.isBlank(bankCode)) {
            String bankCodeLabel = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(Bank.class, KFSPropertyConstants.BANK_CODE);
            GlobalVariables.getMessageMap().putError(bankCodeProperty, KFSKeyConstants.ERROR_REQUIRED, bankCodeLabel);    
            return false;
        }            
        if (ObjectUtils.isNull(bank)) {
            GlobalVariables.getMessageMap().putError(bankCodeProperty, KFSKeyConstants.ERROR_DOCUMENT_BANKACCMAINT_INVALID_BANK);
            return false;
        }            

        if ( StringUtils.isBlank(paymentMethodCode) ) {
//            if (StringUtils.isBlank(bankCode)) {
//                String bankCodeLabel = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(Bank.class, KFSPropertyConstants.BANK_CODE);
//                GlobalVariables.getMessageMap().putError(bankCodeProperty, KFSKeyConstants.ERROR_REQUIRED, bankCodeLabel);    
//                return false;
//            }            
//            if (ObjectUtils.isNull(bank)) {
//                GlobalVariables.getMessageMap().putError(bankCodeProperty, KFSKeyConstants.ERROR_DOCUMENT_BANKACCMAINT_INVALID_BANK);
//                return false;
//            }            
        } else {
            if ( !checkBankCodePopulation(bankCode, paymentMethodCode, bankCodeProperty, true) ) {
                return false;
            }
        }

        
        // validate deposit
        if (bank != null && requireDeposit && !bank.isBankDepositIndicator()) {
            GlobalVariables.getMessageMap().putError(bankCodeProperty, KFSKeyConstants.Bank.ERROR_DEPOSIT_NOT_SUPPORTED);

            return false;
        }

        // validate disbursement
        if (bank != null && requireDisbursement && !bank.isBankDisbursementIndicator()) {
            GlobalVariables.getMessageMap().putError(bankCodeProperty, KFSKeyConstants.Bank.ERROR_DISBURSEMENT_NOT_SUPPORTED);

            return false;
        }

        return true;
    }

    public static boolean doesBankCodeNeedToBePopulated( String paymentMethodCode ) {
        return getPaymentMethodGeneralLedgerPendingEntryService().getBankForPaymentMethod(paymentMethodCode) != null;
    }
    
    public static boolean checkBankCodePopulation( String bankCode, String paymentMethodCode, String bankCodeProperty, boolean addMessages ) {
        boolean bankCodeNeedsPopulation = doesBankCodeNeedToBePopulated(paymentMethodCode);
        // if the payment method uses a bank code and none has been filled in (the user blanked it), throw an error
        if ( bankCodeNeedsPopulation && StringUtils.isBlank( bankCode ) ) {
            // error
            if ( addMessages ) {
                GlobalVariables.getMessageMap().putError( bankCodeProperty, ERROR_BANK_REQUIRED_PER_PAYMENT_METHOD, paymentMethodCode);
            }
            return false;
        } else if ( !bankCodeNeedsPopulation && StringUtils.isNotBlank( bankCode ) ) {
            // if the bank code on the document is not blank but no bank code is specified for the payment method, blank and warn the user.
            if ( addMessages ) {
                GlobalVariables.getMessageMap().putWarning( bankCodeProperty, WARNING_BANK_NOT_REQUIRED, paymentMethodCode);
            }
        }
        return true;
    }

    protected static CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        if ( paymentMethodGeneralLedgerPendingEntryService == null ) {
            paymentMethodGeneralLedgerPendingEntryService = SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class);
        }
        return paymentMethodGeneralLedgerPendingEntryService;
    }
    
}
