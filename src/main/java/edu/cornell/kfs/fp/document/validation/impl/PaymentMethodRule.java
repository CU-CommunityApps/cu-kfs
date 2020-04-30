/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.fp.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.businessobject.PaymentMethod;
import edu.cornell.kfs.fp.businessobject.PaymentMethodChart;

public class PaymentMethodRule extends MaintenanceDocumentRuleBase {

    protected static final String ERROR_NO_BANK_WHEN_INTERDEPT = "error.document.paymentmethod.no.bank.when.interdept";
    protected static final String ERROR_NOT_PDP_AND_INTERDEPT = "error.document.paymentmethod.not.pdp.and.interdept";
    protected static final String ERROR_FLAGREQUIRED = "error.document.paymentmethod.flagrequired";
    protected static final String WARNING_CLEARING_OBJECT_NOTREQUIRED = "warning.document.paymentmethod.clearing.object.notrequired";
    protected static final String WARNING_CLEARING_ACCOUNT_NOTREQUIRED = "warning.document.paymentmethod.clearing.account.notrequired";
    protected static final String WARNING_CLEARING_CHART_NOTREQUIRED = "warning.document.paymentmethod.clearing.chart.notrequired";
    protected static final String ERROR_CLEARING_OBJECT_REQUIRED = "error.document.paymentmethod.clearing.object.required";
    protected static final String ERROR_CLEARING_ACCOUNT_REQUIRED = "error.document.paymentmethod.clearing.account.required";
    protected static final String ERROR_CLEARING_CHART_REQUIRED = "error.document.paymentmethod.clearing.chart.required";
    protected static final String ERROR_EFFECTIVEDATE_INPAST = "error.document.paymentmethod.effectivedate.inpast";
    protected static final String WARNING_FEE_AMOUNT_NOTREQUIRED = "warning.document.paymentmethod.fee.amount.notrequired";
    protected static final String WARNING_FEE_EXPOBJ_NOTREQUIRED = "warning.document.paymentmethod.fee.expobj.notrequired";
    protected static final String WARNING_FEE_INCOBJ_NOTREQUIRED = "warning.document.paymentmethod.fee.incobj.notrequired";
    protected static final String WARNING_FEE_ACCOUNT_NOTREQUIRED = "warning.document.paymentmethod.fee.account.notrequired";
    protected static final String WARNING_FEE_CHART_NOTREQUIRED = "warning.document.paymentmethod.fee.chart.notrequired";
    protected static final String ERROR_FEE_AMOUNT_REQUIRED = "error.document.paymentmethod.fee.amount.required";
    protected static final String ERROR_FEE_EXPOBJ_REQUIRED = "error.document.paymentmethod.fee.expobj.required";
    protected static final String ERROR_FEE_INCOBJ_REQUIRED = "error.document.paymentmethod.fee.incobj.required";
    protected static final String ERROR_FEE_ACCOUNT_REQUIRED = "error.document.paymentmethod.fee.account.required";
    protected static final String ERROR_FEE_CHART_REQUIRED = "error.document.paymentmethod.fee.chart.required";

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean continueRouting = super.processCustomRouteDocumentBusinessRules(document);
        PaymentMethod paymentMethod = (PaymentMethod)document.getNewMaintainableObject().getBusinessObject();
        // checks on the main record
        GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath("document.newMaintainableObject");
     
        // TODO : This is a hack now, foreign draft does not need this check
        if (!StringUtils.equals(PaymentMethod.PM_CODE_FOREIGN_DRAFT, paymentMethod.getPaymentMethodCode()) && !StringUtils.equals(PaymentMethod.PM_CODE_WIRE, paymentMethod.getPaymentMethodCode())) {
            continueRouting &= sanityCheckFlags( paymentMethod );
        }
        continueRouting &= checkNeedForBankCode(paymentMethod);
        
        // checks on the chart records
        for ( int i = 0; i < paymentMethod.getPaymentMethodCharts().size(); i++ ) {
            GlobalVariables.getMessageMap().addToErrorPath("paymentMethodCharts["+i+"]");
            PaymentMethodChart paymentMethodChart = paymentMethod.getPaymentMethodCharts().get(i);
            if ( paymentMethodChart.isNewCollectionRecord() ) {
                continueRouting &= isNewEffectiveDateInFuture( paymentMethod.getPaymentMethodCharts().get(i) );
                continueRouting &= checkFeeInformation( paymentMethod, paymentMethod.getPaymentMethodCharts().get(i) );
                continueRouting &= checkClearingAccountInformation( paymentMethod, paymentMethod.getPaymentMethodCharts().get(i) );
            }
            
            GlobalVariables.getMessageMap().removeFromErrorPath("paymentMethodCharts["+i+"]");
        }
        GlobalVariables.getMessageMap().removeFromErrorPath("document.newMaintainableObject");
        
        return continueRouting;
    }
    
    protected boolean isNewEffectiveDateInFuture( PaymentMethodChart paymentMethodChart ) {        
        // check if new, if so, they must have a future date
        if ( paymentMethodChart.getEffectiveDate() != null ) {
            if ( paymentMethodChart.getEffectiveDate().before( getDateTimeService().getCurrentSqlDateMidnight() ) ) {
                GlobalVariables.getMessageMap().putError("effectiveDate", ERROR_EFFECTIVEDATE_INPAST, (String[])null);            
                return false;
            }
        }
        return true;
    }
    
    protected boolean checkFeeInformation( PaymentMethod paymentMethod, PaymentMethodChart paymentMethodChart ) {
        boolean continueRouting = true;
        if ( paymentMethod.isAssessedFees() ) {
            if ( StringUtils.isBlank( paymentMethodChart.getFeeIncomeChartOfAccountsCode() ) ) {
                GlobalVariables.getMessageMap().putError("feeIncomeChartOfAccountsCode", ERROR_FEE_CHART_REQUIRED, (String[])null);
                continueRouting = false;
            }
            if ( StringUtils.isBlank( paymentMethodChart.getFeeIncomeAccountNumber() ) ) {
                GlobalVariables.getMessageMap().putError("feeIncomeAccountNumber", ERROR_FEE_ACCOUNT_REQUIRED, (String[])null);
                continueRouting = false;
            }
            if ( StringUtils.isBlank( paymentMethodChart.getFeeIncomeFinancialObjectCode() ) ) {
                GlobalVariables.getMessageMap().putError("feeIncomeFinancialObjectCode", ERROR_FEE_INCOBJ_REQUIRED, (String[])null);
                continueRouting = false;
            }
            if ( StringUtils.isBlank( paymentMethodChart.getFeeExpenseFinancialObjectCode() ) ) {
                GlobalVariables.getMessageMap().putError("feeExpenseFinancialObjectCode", ERROR_FEE_EXPOBJ_REQUIRED, (String[])null);
                continueRouting = false;
            }
            if ( paymentMethodChart.getFeeAmount() == null || paymentMethodChart.getFeeAmount().isZero() ) {
                GlobalVariables.getMessageMap().putError("feeAmount", ERROR_FEE_AMOUNT_REQUIRED, (String[])null);
                continueRouting = false;
            }
        } else {
            if ( StringUtils.isNotBlank( paymentMethodChart.getFeeIncomeChartOfAccountsCode() ) ) {
                GlobalVariables.getMessageMap().putError("feeIncomeChartOfAccountsCode", WARNING_FEE_CHART_NOTREQUIRED, (String[])null);
            }
            if ( StringUtils.isNotBlank( paymentMethodChart.getFeeIncomeAccountNumber() ) ) {
                GlobalVariables.getMessageMap().putError("feeIncomeAccountNumber", WARNING_FEE_ACCOUNT_NOTREQUIRED, (String[])null);
            }
            if ( StringUtils.isNotBlank( paymentMethodChart.getFeeIncomeFinancialObjectCode() ) ) {
                GlobalVariables.getMessageMap().putError("feeIncomeFinancialObjectCode", WARNING_FEE_INCOBJ_NOTREQUIRED, (String[])null);
            }
            if ( StringUtils.isNotBlank( paymentMethodChart.getFeeExpenseFinancialObjectCode() ) ) {
                GlobalVariables.getMessageMap().putError("feeExpenseFinancialObjectCode", WARNING_FEE_EXPOBJ_NOTREQUIRED, (String[])null);
            }
            if ( paymentMethodChart.getFeeAmount() != null && paymentMethodChart.getFeeAmount().isNonZero() ) {
                GlobalVariables.getMessageMap().putError("feeAmount", WARNING_FEE_AMOUNT_NOTREQUIRED, (String[])null);
            }
        }        
        return continueRouting;
    }

    protected boolean checkClearingAccountInformation( PaymentMethod paymentMethod, PaymentMethodChart paymentMethodChart ) {
        boolean continueRouting = true;
        if ( paymentMethod.isOffsetUsingClearingAccount() ) {
            if ( StringUtils.isEmpty( paymentMethodChart.getClearingChartOfAccountsCode() ) ) {
                GlobalVariables.getMessageMap().putError("clearingChartOfAccountsCode", ERROR_CLEARING_CHART_REQUIRED, (String[])null);
                continueRouting = false;
            }
            if ( StringUtils.isEmpty( paymentMethodChart.getClearingAccountNumber() ) ) {
                GlobalVariables.getMessageMap().putError("clearingAccountNumber", ERROR_CLEARING_ACCOUNT_REQUIRED, (String[])null);
                continueRouting = false;
            }
            if ( StringUtils.isEmpty( paymentMethodChart.getClearingFinancialObjectCode() ) ) {
                GlobalVariables.getMessageMap().putError("clearingFinancialObjectCode", ERROR_CLEARING_OBJECT_REQUIRED, (String[])null);
                continueRouting = false;
            }
        } else {
            if ( StringUtils.isNotEmpty( paymentMethodChart.getClearingChartOfAccountsCode() ) ) {
                GlobalVariables.getMessageMap().putError("clearingChartOfAccountsCode", WARNING_CLEARING_CHART_NOTREQUIRED, (String[])null);
            }
            if ( StringUtils.isNotEmpty( paymentMethodChart.getClearingAccountNumber() ) ) {
                GlobalVariables.getMessageMap().putError("clearingAccountNumber", WARNING_CLEARING_ACCOUNT_NOTREQUIRED, (String[])null);
            }
            if ( StringUtils.isNotEmpty( paymentMethodChart.getClearingFinancialObjectCode() ) ) {
                GlobalVariables.getMessageMap().putError("clearingFinancialObjectCode", WARNING_CLEARING_OBJECT_NOTREQUIRED, (String[])null);
            }
        }        
        return continueRouting;
    }
    
    @Override
    public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
        boolean continueAddingLine = true;
        GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath("document.newMaintainableObject");
        if ( line instanceof PaymentMethodChart ) {
            GlobalVariables.getMessageMap().addToErrorPath(KFSConstants.MAINTENANCE_ADD_PREFIX + collectionName );
            
            continueAddingLine &= isNewEffectiveDateInFuture( ((PaymentMethodChart)line) );
            continueAddingLine &= checkFeeInformation( (PaymentMethod)document.getNewMaintainableObject().getBusinessObject(), (PaymentMethodChart)line );
            continueAddingLine &= checkClearingAccountInformation( (PaymentMethod)document.getNewMaintainableObject().getBusinessObject(), (PaymentMethodChart)line );
            
            GlobalVariables.getMessageMap().removeFromErrorPath(KFSConstants.MAINTENANCE_ADD_PREFIX + collectionName );
        }
        GlobalVariables.getMessageMap().removeFromErrorPath("document.newMaintainableObject");
        return continueAddingLine;
    }
    
    protected boolean sanityCheckFlags( PaymentMethod paymentMethod ) {
        // ensure at least one of the following flags is set
        if ( !paymentMethod.isProcessedUsingPdp() 
                && !paymentMethod.isInterdepartmentalPayment()
                && !paymentMethod.isOffsetUsingClearingAccount() ) {
            GlobalVariables.getMessageMap().putError("processedUsingPdp",ERROR_FLAGREQUIRED,
                    getDdService().getAttributeLabel(PaymentMethod.class, "processedUsingPdp"),
                    getDdService().getAttributeLabel(PaymentMethod.class, "interdepartmentalPayment"),
                    getDdService().getAttributeLabel(PaymentMethod.class, "offsetUsingClearingAccount"));
            return false;
        // the PDP and interdepartmental flags can not both be set
        } else if ( paymentMethod.isProcessedUsingPdp() && paymentMethod.isInterdepartmentalPayment() ) {
            GlobalVariables.getMessageMap().putError("processedUsingPdp",ERROR_NOT_PDP_AND_INTERDEPT,(String[])null);
            return false;
        }
        return true;
    }
    
    protected boolean checkNeedForBankCode( PaymentMethod paymentMethod ) {
        // when interdepartmental, it doesn't make sense to have a bank code
        if ( paymentMethod.isInterdepartmentalPayment() && StringUtils.isNotBlank(paymentMethod.getBankCode()) ) {
            GlobalVariables.getMessageMap().putError("bankCode",ERROR_NO_BANK_WHEN_INTERDEPT,(String[])null);
            return false;
        }
        return true;
    }
}