

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

package edu.cornell.kfs.fp.document.validation.impl;

 

import static org.kuali.kfs.sys.KFSConstants.AMOUNT_PROPERTY_NAME;
import static org.kuali.kfs.sys.KFSConstants.GL_DEBIT_CODE;
import static org.kuali.kfs.sys.KFSKeyConstants.ERROR_ZERO_AMOUNT;
import static org.kuali.kfs.sys.KFSKeyConstants.ERROR_ZERO_OR_NEGATIVE_AMOUNT;
import static org.kuali.kfs.sys.KFSKeyConstants.JournalVoucher.ERROR_NEGATIVE_NON_BUDGET_AMOUNTS;
import static org.kuali.kfs.sys.KFSPropertyConstants.BALANCE_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.JournalVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.JournalVoucherAccountingLineAmountValidation;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

 

/**

 * The Journal Voucher's version of the accounting line amount validation

 */

public class CuJournalVoucherAccountingLineAmountValidation extends JournalVoucherAccountingLineAmountValidation {

   /**

     * 

     * Accounting lines for Journal Vouchers can be positive or negative, just not "$0.00".  

     * 

     * Additionally, accounting lines cannot have negative dollar amounts if the balance type of the 

     * journal voucher allows for general ledger pending entry offset generation or the balance type 

     * is not a budget type code.

     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)

     */

    public boolean validate(AttributedDocumentEvent event) {

        KualiDecimal amount = getAccountingLineForValidation().getAmount();
        getJournalVoucherForValidation().refreshReferenceObject(BALANCE_TYPE);

        if (getJournalVoucherForValidation().getBalanceType().isFinancialOffsetGenerationIndicator()) {
        // check for negative or zero amounts
            
            if (amount.isZero()) { 
            // if 0

                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(buildMessageMapKeyPathForDebitCreditAmount(true),
                        ERROR_ZERO_OR_NEGATIVE_AMOUNT, "an accounting line");

                GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(buildMessageMapKeyPathForDebitCreditAmount(false),
                        ERROR_ZERO_OR_NEGATIVE_AMOUNT, "an accounting line");

 

               return false;

           }

           else if (amount.isNegative()) { // entered a negative number

   String debitCreditCode = getAccountingLineForValidation().getDebitCreditCode();

   if (StringUtils.isNotBlank(debitCreditCode) && GL_DEBIT_CODE.equals(debitCreditCode)) {

       GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(buildMessageMapKeyPathForDebitCreditAmount(true), ERROR_ZERO_OR_NEGATIVE_AMOUNT, "an accounting line");

   }

   else {

       GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(buildMessageMapKeyPathForDebitCreditAmount(false), ERROR_ZERO_OR_NEGATIVE_AMOUNT, "an accounting line");

               }

 

               return false;

           }

       }

       else {

           // Check for zero amounts

   if (amount.isZero()) { // amount == 0

   GlobalVariables.getMessageMap().putError(AMOUNT_PROPERTY_NAME, ERROR_ZERO_AMOUNT, "an accounting line");

               return false;

           }

           else if (amount.isNegative()) {

               if (!allowNegativeAmounts(getAccountingLineForValidation())) {

                   GlobalVariables.getMessageMap().putError(AMOUNT_PROPERTY_NAME, ERROR_NEGATIVE_NON_BUDGET_AMOUNTS);

               }

           }

       }

 

       return true;

   }

   

   /**

 * This method retrieves the parameter values that define the allowable balance type codes and determines if negative amounts

 * are allowed for the associated accounting line.

 * 

 * @param acctLine The accounting line which will be used to determine if negative amounts are allowed.

 * @return True if the accounting line has a balance type found in the associated parameter, false otherwise.

 */

   private boolean allowNegativeAmounts(AccountingLine acctLine) {

       List<String> budgetTypes = new ArrayList<String>(SpringContext.getBean(ParameterService.class).getParameterValuesAsString(JournalVoucherDocument.class, CUKFSParameterKeyConstants.FpParameterConstants.FP_ALLOWED_BUDGET_BALANCE_TYPES));

       return budgetTypes.contains(acctLine.getBalanceTypeCode());

   }

}

