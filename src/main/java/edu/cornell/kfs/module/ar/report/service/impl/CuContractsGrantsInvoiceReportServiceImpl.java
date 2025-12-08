package edu.cornell.kfs.module.ar.report.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRateDetail;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.report.service.impl.ContractsGrantsInvoiceReportServiceImpl;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;

import edu.cornell.kfs.module.ar.service.CuContractsGrantsBillingUtilityService;
import edu.cornell.kfs.module.ar.service.CuContractsGrantsInvoiceCreateDocumentService;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

/* Cornell Customized class created for KFSPTS-33340 */

public class CuContractsGrantsInvoiceReportServiceImpl extends ContractsGrantsInvoiceReportServiceImpl {
    
    protected CuContractsGrantsInvoiceCreateDocumentService contractsGrantsInvoiceCreateDocumentService;
    protected CuContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService;
    
    @Override
    protected KualiDecimal getCashReceipts(Award award) {
        KualiDecimal cashReceipt = KualiDecimal.ZERO;
        final KualiDecimal payments = contractsGrantsInvoiceDocumentService.calculateTotalPaymentsToDateByAward(award);
        cashReceipt = payments;
        return cashReceipt;
    }
    
    @Override
    /* KFSPTS-33340: Cornell Customization of base code method of same name. */
    /**
     * This method is used to populate the replacement list to replace values from pdf template to actual values for
     * Federal Form 425.
     *
     * @param award
     * @param reportingPeriod
     * @param year
     */
    protected void populateListByAward(
            final Award award,
            final String reportingPeriod,
            final String year,
            final Map<String, String> replacementList
    ) {
        KualiDecimal cashDisbursement = KualiDecimal.ZERO;
        final SystemOptions systemOption = optionsService.getCurrentYearOptions();
        
        /* Cornell customization - KFSPTS-33340 */
        ContractsGrantsInvoiceDocument cinv = contractsGrantsInvoiceCreateDocumentService.createCINVForReport(award);

        for (final AwardAccount awardAccount : award.getActiveAwardAccounts()) {
            int index = 0;
            KualiDecimal baseSum = KualiDecimal.ZERO;
            KualiDecimal amountSum = KualiDecimal.ZERO;
            cashDisbursement =
                    cashDisbursement.add(contractsGrantsInvoiceDocumentService.getBudgetAndActualsForAwardAccount(
                            awardAccount,
                            systemOption.getActualFinancialBalanceTypeCd()
                    ));
            if (ObjectUtils.isNotNull(awardAccount.getAccount().getFinancialIcrSeriesIdentifier())
                && ObjectUtils.isNotNull(awardAccount.getAccount().getAcctIndirectCostRcvyTypeCd())) {
                index++;
                /* Cornell customization - KFSPTS-33340
                 * No substituion data value needed as it is hardcoded on the CU customized template.
                 */
                //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                //        ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_TYPE + "_" + index,
                //        awardAccount.getAccount().getAcctIndirectCostRcvyTypeCd()
                //);
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_RATE + "_" + index,
                        awardAccount.getAccount().getFinancialIcrSeriesIdentifier()
                );
                if (ObjectUtils.isNotNull(awardAccount.getAccount().getAccountEffectiveDate())) {
                    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                            ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_PERIOD_FROM + "_" + index,
                            getDateTimeService().toDateString(awardAccount.getAccount().getAccountEffectiveDate())
                    );
                }
                /* Cornell customization - KFSPTS-33340 */
                if (ObjectUtils.isNotNull(awardAccount.getAccount().getAccountExpirationDate())) {
                    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                            ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_PERIOD_TO + "_" + index,
                            getReportingPeriodEndDate(reportingPeriod, year)
                    );
                }
                /* Cornell customization - KFSPTS-33340 */
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_BASE + "_" + index,
                        contractsGrantsBillingUtilityService.formatForCurrency(cinv.getTotalDirectCostInvoiceDetail().getTotalAmountBilledToDate(), false)
                );
                final Map<String, Object> key = new HashMap<>();
                key.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
                key.put(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER,
                        awardAccount.getAccount().getFinancialIcrSeriesIdentifier()
                );
                key.put(KFSPropertyConstants.ACTIVE, true);
                key.put(KFSPropertyConstants.TRANSACTION_DEBIT_INDICATOR, KFSConstants.GL_DEBIT_CODE);
                final List<IndirectCostRecoveryRateDetail> icrDetail =
                        (List<IndirectCostRecoveryRateDetail>) businessObjectService.findMatchingOrderBy(
                                IndirectCostRecoveryRateDetail.class,
                                key,
                                KFSPropertyConstants.AWARD_INDR_COST_RCVY_ENTRY_NBR,
                                false
                        );
                if (CollectionUtils.isNotEmpty(icrDetail)) {
                    final KualiDecimal rate = new KualiDecimal(icrDetail.get(0).getAwardIndrCostRcvyRatePct());
                    if (ObjectUtils.isNotNull(rate)) {
                        /* Cornell customization - KFSPTS-33340 */
                        //final KualiDecimal ONE_HUNDRED = new KualiDecimal(100);
                        /* Cornell customization - KFSPTS-33340 */
                        final KualiDecimal indirectExpenseAmount = cinv.getTotalIndirectCostInvoiceDetail().getTotalAmountBilledToDate();
                        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                                ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_AMOUNT + "_" + index,
                                contractsGrantsBillingUtilityService.formatForCurrency(indirectExpenseAmount, false)
                        );
                        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                                ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_FEDERAL + "_" + index,
                                contractsGrantsBillingUtilityService.formatForCurrency(indirectExpenseAmount, false)
                        );
                        amountSum = amountSum.add(indirectExpenseAmount);
                    }
                }
                /* Cornell customization - KFSPTS-33340 */
                baseSum = baseSum.add(cinv.getTotalDirectCostInvoiceDetail().getTotalAmountBilledToDate());
            }
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_BASE_SUM,
                    contractsGrantsBillingUtilityService.formatForCurrency(baseSum, false)
            );
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_AMOUNT_SUM,
                    contractsGrantsBillingUtilityService.formatForCurrency(amountSum, false)
            );
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_FEDERAL_SUM,
                    contractsGrantsBillingUtilityService.formatForCurrency(amountSum, false)
            );
        }
        /* Cornell customization - KFSPTS-33340
         * No substituion data value needed as it is hardcoded on the CU customized template.
         */
        //final SystemInformation sysInfo = retrieveSystemInformationForAward(award, year);
        //if (ObjectUtils.isNotNull(sysInfo)) {
        //    final String address = concatenateAddressFromSystemInformation(sysInfo);
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //            ArPropertyConstants.FederalFormReportFields.RECIPIENT_ORGANIZATION,
        //            address
        //    );
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //            ArPropertyConstants.FederalFormReportFields.ZWEI,
        //            sysInfo.getUniversityFederalEmployerIdentificationNumber());
        //
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.STREET1, sysInfo.getOrganizationRemitToLine1StreetAddress());
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.STREET2, sysInfo.getOrganizationRemitToLine2StreetAddress());
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.CITY, sysInfo.getOrganizationRemitToCityName());
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.STATE, sysInfo.getOrganizationRemitToStateCode());
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.ZIP, sysInfo.getOrganizationRemitToZipCode());
        //    if (ObjectUtils.isNotNull(sysInfo.getOrganizationRemitToCounty())) {
        //        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.COUNTY, sysInfo.getOrganizationRemitToCounty().getName());
        //    }
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.UEI, sysInfo.getUniqueEntityId());
        //}

        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.FEDERAL_AGENCY,
                award.getAgency().getFullName()
        );
        /* Cornell customization - KFSPTS-33340 */
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.FEDERAL_GRANT_NUMBER,
                award.getProposal().getGrantNumber()
        );
        if (CollectionUtils.isNotEmpty(award.getActiveAwardAccounts())) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.RECIPIENT_ACCOUNT_NUMBER,
                    award.getActiveAwardAccounts().get(0).getAccountNumber()
            );
        }
        /* Cornell customization - KFSPTS-33340 */
        AwardExtendedAttribute awardExtendedAttribute = ((AwardExtendedAttribute)((Award)award).getExtension());
        if (ObjectUtils.isNotNull(awardExtendedAttribute.getBudgetBeginningDate())) {
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.GRANT_PERIOD_FROM,
                    getDateTimeService().toDateString(awardExtendedAttribute.getBudgetBeginningDate())
            );
        }
        if (ObjectUtils.isNotNull(awardExtendedAttribute.getBudgetEndingDate())) {
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.GRANT_PERIOD_TO,
                    getDateTimeService().toDateString(awardExtendedAttribute.getBudgetEndingDate())
            );
        }
        /* Cornell customization - KFSPTS-33340 */
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.CASH_RECEIPTS,
                contractsGrantsBillingUtilityService.formatForCurrency(getCashReceipts(award), false)
        );
        /* Cornell customization - KFSPTS-33340 */
        KualiDecimal expenditures = KualiDecimal.ZERO;
        if (ObjectUtils.isNotNull(cashDisbursement)) {
            expenditures = cashDisbursement;
        }
        /* Cornell customization - KFSPTS-33340 */
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.TOTAL_FEDERAL_FUNDS_AUTHORIZED,
                contractsGrantsBillingUtilityService.formatForCurrency(awardExtendedAttribute.getBudgetTotalAmount(), false)
        );

        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.REPORTING_PERIOD_END_DATE,
                getReportingPeriodEndDate(reportingPeriod, year)
        );
        if (ObjectUtils.isNotNull(cashDisbursement)) {
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.CASH_DISBURSEMENTS,
                    contractsGrantsBillingUtilityService.formatForCurrency(cashDisbursement, false)
            );
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.CASH_ON_HAND,
                    contractsGrantsBillingUtilityService.formatForCurrency(getCashReceipts(award).subtract(
                            cashDisbursement), false)
            );
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.FEDERAL_SHARE_OF_EXPENDITURES,
                    contractsGrantsBillingUtilityService.formatForCurrency(cashDisbursement, false)
            );
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.TOTAL_FEDERAL_SHARE,
                    contractsGrantsBillingUtilityService.formatForCurrency(cashDisbursement, false)
            );
            /* Cornell customization - KFSPTS-33340 */
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.UNOBLIGATED_BALANCE_OF_FEDERAL_FUNDS,
                    contractsGrantsBillingUtilityService.formatForCurrency(awardExtendedAttribute.getBudgetTotalAmount()
                            .subtract(expenditures), false)
            );
        }
        /* Cornell customization - KFSPTS-33340 */
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.FEDERAL_SHARE_OF_UNLIQUIDATED_OBLIGATION,
                contractsGrantsBillingUtilityService.formatForCurrency(KualiDecimal.ZERO, false)
        );

        /* Cornell customization - KFSPTS-33340
         * No substituion data value needed as it is hardcoded on the CU customized template.
         */
        //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //        ArPropertyConstants.FederalFormReportFields.TOTAL_FEDERAL_INCOME_EARNED,
        //        KFSConstants.EMPTY_STRING
        //);
        //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //        ArPropertyConstants.FederalFormReportFields.INCOME_EXPENDED_DEDUCATION_ALTERNATIVE,
        //        KFSConstants.EMPTY_STRING
        //);
        //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //        ArPropertyConstants.FederalFormReportFields.INCOME_EXPENDED_ADDITION_ALTERNATIVE,
        //        KFSConstants.EMPTY_STRING
        //);
        //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //        ArPropertyConstants.FederalFormReportFields.UNEXPECTED_PROGRAM_INCOME,
        //        KFSConstants.EMPTY_STRING
        //);
        //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //        ArPropertyConstants.FederalFormReportFields.NAME,
        //        KFSConstants.EMPTY_STRING
        //);
        //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //        ArPropertyConstants.FederalFormReportFields.TELEPHONE,
        //        KFSConstants.EMPTY_STRING
        //);
        //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //        ArPropertyConstants.FederalFormReportFields.EMAIL_ADDRESS,
        //        KFSConstants.EMPTY_STRING
        //);
        //contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //        ArPropertyConstants.FederalFormReportFields.DATE_REPORT_SUBMITTED,
        //        getDateTimeService().toDateString(getDateTimeService().getCurrentDate())
        //);
        if (ArConstants.QUARTER1.equals(reportingPeriod) || ArConstants.QUARTER2.equals(reportingPeriod)
            || ArConstants.QUARTER3.equals(reportingPeriod) || ArConstants.QUARTER4.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.QUARTERLY,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.SEMI_ANNUAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.SEMI_ANNUAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.ANNUAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.ANNUAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.FINAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.FINAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        /* Cornell customization - KFSPTS-33340
         * No substituion data value needed as it is hardcoded on the CU customized template.
         */
        //final String accountingBasis = parameterService.getParameterValueAsString(ArConstants.AR_NAMESPACE_CODE,
        //        ArParameterConstants.Components.FEDERAL_FINANCIAL_REPORT,
        //        ArParameterConstants.ACCOUNTING_BASIS
        //);
        //if (ArConstants.BASIS_OF_ACCOUNTING_CASH.equals(accountingBasis)) {
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //            ArPropertyConstants.FederalFormReportFields.CASH,
        //            KFSConstants.OptionLabels.YES
        //    );
        //}
        //if (ArConstants.BASIS_OF_ACCOUNTING_ACCRUAL.equals(accountingBasis)) {
        //    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
        //            ArPropertyConstants.FederalFormReportFields.ACCRUAL,
        //            KFSConstants.OptionLabels.YES
        //    );
        //}
    }
    
    public CuContractsGrantsInvoiceCreateDocumentService getContractsGrantsInvoiceCreateDocumentService() {
        return contractsGrantsInvoiceCreateDocumentService;
    }

    public void setContractsGrantsInvoiceCreateDocumentService(
            CuContractsGrantsInvoiceCreateDocumentService contractsGrantsInvoiceCreateDocumentService) {
        this.contractsGrantsInvoiceCreateDocumentService = contractsGrantsInvoiceCreateDocumentService;
    }

    public CuContractsGrantsBillingUtilityService getContractsGrantsBillingUtilityService() {
        return contractsGrantsBillingUtilityService;
    }

    public void setContractsGrantsBillingUtilityService(
            CuContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService) {
        this.contractsGrantsBillingUtilityService = contractsGrantsBillingUtilityService;
    }

}
