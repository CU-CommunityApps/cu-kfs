package edu.cornell.kfs.module.ar.document.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceDetailAccountObjectCode;
import org.kuali.kfs.module.ar.businessobject.SystemInformation;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.impl.ContractsGrantsInvoiceDocumentServiceImpl;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.util.FallbackMap;
import org.kuali.kfs.sys.util.ReflectionMap;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.ar.CuArPropertyConstants;
import edu.cornell.kfs.module.ar.report.CuPdfFormattingMap;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuContractsGrantsInvoiceDocumentServiceImpl extends ContractsGrantsInvoiceDocumentServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceDocumentServiceImpl.class);

    // Customized to default final status date to last modified date, and to wrap the results in a new CuPdfFormattingMap object.
    @Override
    /**
     * This method generated the template parameter list to populate the pdf invoices that are attached to the Document.
     * The evident goal of this method was to return practically any possible property from the given document into a
     * Map which could be stamped on any invoice PDF.  Given that, we've done some strange tricks with Map
     * implementations.  First, we wrap the document in a ReflectionMap, which means that all nested properties from
     * the document can be read via Map notation.  We still have a number of properties we want to add, though, for
     * instance for the payee and the award.  So we wrap ReflectionMap in a FallbackMap, which will treat a regular
     * HashMap and the ReflectionMap as if they were one Map for the sake of getting at least.  Finally, we wrap the map
     * of all properties into a PdfFormattingMap, which formats any values to be returned through get() into properly
     * formatted Strings.
     *
     * @param document the ContractsGrantsInvoiceDocument to convert into a Map form
     * @return a Map.  With everything.
     */
    protected Map<String, String> getTemplateParameterList(ContractsGrantsInvoiceDocument document) {
        ContractsAndGrantsBillingAward award = document.getInvoiceGeneralDetail().getAward();

        Map cinvDocMap = new ReflectionMap(document);
        Map<String, Object> parameterMap = new FallbackMap<String, Object>(cinvDocMap);

        Map<String, Object> primaryKeys = new HashMap<>();
        primaryKeys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,
                document.getAccountingPeriod().getUniversityFiscalYear());
        primaryKeys.put(KFSPropertyConstants.PROCESSING_CHART_OF_ACCT_CD,
                document.getAccountsReceivableDocumentHeader().getProcessingChartOfAccountCode());
        primaryKeys.put(KFSPropertyConstants.PROCESSING_ORGANIZATION_CODE,
                document.getAccountsReceivableDocumentHeader().getProcessingOrganizationCode());
        SystemInformation sysInfo = businessObjectService.findByPrimaryKey(SystemInformation.class, primaryKeys);
        parameterMap.put(KFSPropertyConstants.DOCUMENT_NUMBER, document.getDocumentNumber());
        if (ObjectUtils.isNotNull(document.getDocumentHeader().getWorkflowDocument().getDateCreated())) {
            parameterMap.put(KFSPropertyConstants.DATE, getDateTimeService()
                    .toDateString(document.getDocumentHeader().getWorkflowDocument().getDateCreated().toDate()));
        }
        
        setFinalStatusDate(document, parameterMap);
        
        parameterMap.put(KFSPropertyConstants.PROPOSAL_NUMBER, document.getInvoiceGeneralDetail().getProposalNumber());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.NAME,
                document.getBillingAddressName());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.ADDRESS_LINE1,
                document.getBillingLine1StreetAddress());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.ADDRESS_LINE2,
                document.getBillingLine2StreetAddress());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.CITY, document.getBillingCityName());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.STATE, document.getBillingStateCode());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.ZIPCODE, document.getBillingZipCode());
        parameterMap.put(ArPropertyConstants.ADVANCE_FLAG,
                ArConstants.BillingFrequencyValues.isPredeterminedBilling(document.getInvoiceGeneralDetail()));
        parameterMap.put(ArPropertyConstants.REIMBURSEMENT_FLAG,
                !ArConstants.BillingFrequencyValues.isPredeterminedBilling(document.getInvoiceGeneralDetail()));
        parameterMap.put(
                ArPropertyConstants.ACCOUNT_DETAILS + "." + KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT_NUMBER,
                getRecipientAccountNumber(document.getAccountDetails()));
        
        if (document.getInvoiceGeneralDetail().isFinalBillIndicator()) {
            parameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.FINAL_BILL, CUKFSConstants.CAPITAL_X);
            parameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.PARTIAL_BILL, StringUtils.EMPTY);
        } else {
            parameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.FINAL_BILL, StringUtils.EMPTY);
            parameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.PARTIAL_BILL, CUKFSConstants.CAPITAL_X);
        }
        
        if (ObjectUtils.isNotNull(sysInfo)) {
            parameterMap.put(
                    ArPropertyConstants.SYSTEM_INFORMATION + "."
                            + ArPropertyConstants.SystemInformationFields.FEIN_NUMBER,
                    sysInfo.getUniversityFederalEmployerIdentificationNumber());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." + KFSPropertyConstants.NAME,
                    sysInfo.getOrganizationRemitToAddressName());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." + KFSPropertyConstants.ADDRESS_LINE1,
                    sysInfo.getOrganizationRemitToLine1StreetAddress());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." + KFSPropertyConstants.ADDRESS_LINE2,
                    sysInfo.getOrganizationRemitToLine2StreetAddress());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." + KFSPropertyConstants.CITY,
                    sysInfo.getOrganizationRemitToCityName());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." + KFSPropertyConstants.STATE,
                    sysInfo.getOrganizationRemitToStateCode());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." + KFSPropertyConstants.ZIPCODE,
                    sysInfo.getOrganizationRemitToZipCode());
        }
        if (CollectionUtils.isNotEmpty(document.getDirectCostInvoiceDetails())) {
            ContractsGrantsInvoiceDetail firstInvoiceDetail = document.getDirectCostInvoiceDetails().get(0);

            for (int i = 0; i < document.getDirectCostInvoiceDetails().size(); i++) {
                parameterMap.put(
                        ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]."
                                + ArPropertyConstants.INVOICE_DETAIL_IDENTIFIER,
                        document.getDirectCostInvoiceDetails().get(i).getInvoiceDetailIdentifier());
                parameterMap.put(
                        ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]." + KFSPropertyConstants.DOCUMENT_NUMBER,
                        document.getDirectCostInvoiceDetails().get(i).getDocumentNumber());
                parameterMap.put(ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]." + ArPropertyConstants.CATEGORY,
                        document.getDirectCostInvoiceDetails().get(i).getCostCategory().getCategoryName());
                parameterMap.put(ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]." + ArPropertyConstants.TOTAL_BUDGET,
                        document.getDirectCostInvoiceDetails().get(i).getTotalBudget());
                parameterMap.put(
                        ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]." + ArPropertyConstants.INVOICE_AMOUNT,
                        document.getDirectCostInvoiceDetails().get(i).getInvoiceAmount());
                parameterMap.put(
                        ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]." + ArPropertyConstants.CUMULATIVE_EXPENDITURES,
                        document.getDirectCostInvoiceDetails().get(i).getCumulativeExpenditures());
                parameterMap.put(
                        ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]." + ArPropertyConstants.BUDGET_REMAINING,
                        document.getDirectCostInvoiceDetails().get(i).getBudgetRemaining());
                parameterMap.put(
                        ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]." + ArPropertyConstants.TOTAL_PREVIOUSLY_BILLED,
                        document.getDirectCostInvoiceDetails().get(i).getTotalPreviouslyBilled());
                parameterMap.put(
                        ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]."
                                + ArPropertyConstants.TOTAL_AMOUNT_BILLED_TO_DATE,
                        document.getDirectCostInvoiceDetails().get(i).getTotalAmountBilledToDate());
                parameterMap.put(
                        ArPropertyConstants.INVOICE_DETAIL + "[" + i + "]."
                                + ArPropertyConstants.AMOUNT_REMAINING_TO_BILL,
                        firstInvoiceDetail.getAmountRemainingToBill());
            }
        }
        ContractsGrantsInvoiceDetail totalDirectCostInvoiceDetail = document.getTotalDirectCostInvoiceDetail();
        if (ObjectUtils.isNotNull(totalDirectCostInvoiceDetail)) {
            parameterMap.put(
                    ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "."
                            + ArPropertyConstants.INVOICE_DETAIL_IDENTIFIER,
                    totalDirectCostInvoiceDetail.getInvoiceDetailIdentifier());
            parameterMap.put(
                    ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "." + KFSPropertyConstants.DOCUMENT_NUMBER,
                    totalDirectCostInvoiceDetail.getDocumentNumber());
            parameterMap.put(ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.CATEGORIES,
                    getConfigurationService().getPropertyValueAsString(
                            ArKeyConstants.CONTRACTS_GRANTS_INVOICE_DETAILS_DIRECT_SUBTOTAL_LABEL));
            parameterMap.put(ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.TOTAL_BUDGET,
                    totalDirectCostInvoiceDetail.getTotalBudget());
            parameterMap.put(ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.INVOICE_AMOUNT,
                    totalDirectCostInvoiceDetail.getInvoiceAmount());
            parameterMap.put(
                    ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.CUMULATIVE_EXPENDITURES,
                    totalDirectCostInvoiceDetail.getCumulativeExpenditures());
            parameterMap.put(
                    ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.BUDGET_REMAINING,
                    totalDirectCostInvoiceDetail.getBudgetRemaining());
            parameterMap.put(
                    ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.TOTAL_PREVIOUSLY_BILLED,
                    totalDirectCostInvoiceDetail.getTotalPreviouslyBilled());
            parameterMap.put(
                    ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "."
                            + ArPropertyConstants.TOTAL_AMOUNT_BILLED_TO_DATE,
                    totalDirectCostInvoiceDetail.getTotalAmountBilledToDate());
            parameterMap.put(
                    ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.AMOUNT_REMAINING_TO_BILL,
                    totalDirectCostInvoiceDetail.getAmountRemainingToBill());
        }
        ContractsGrantsInvoiceDetail totalInDirectCostInvoiceDetail = document.getTotalIndirectCostInvoiceDetail();
        if (ObjectUtils.isNotNull(totalInDirectCostInvoiceDetail)) {
            parameterMap.put(
                    ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "."
                            + ArPropertyConstants.INVOICE_DETAIL_IDENTIFIER,
                    totalInDirectCostInvoiceDetail.getInvoiceDetailIdentifier());
            parameterMap.put(
                    ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "." + KFSPropertyConstants.DOCUMENT_NUMBER,
                    totalInDirectCostInvoiceDetail.getDocumentNumber());
            parameterMap.put(ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.CATEGORIES,
                    getConfigurationService().getPropertyValueAsString(
                            ArKeyConstants.CONTRACTS_GRANTS_INVOICE_DETAILS_INDIRECT_SUBTOTAL_LABEL));
            parameterMap.put(ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.TOTAL_BUDGET,
                    totalInDirectCostInvoiceDetail.getTotalBudget());
            parameterMap.put(
                    ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.INVOICE_AMOUNT,
                    totalInDirectCostInvoiceDetail.getInvoiceAmount());
            parameterMap.put(
                    ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "."
                            + ArPropertyConstants.CUMULATIVE_EXPENDITURES,
                    totalInDirectCostInvoiceDetail.getCumulativeExpenditures());
            parameterMap.put(
                    ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "." + ArPropertyConstants.BUDGET_REMAINING,
                    totalInDirectCostInvoiceDetail.getBudgetRemaining());
            parameterMap.put(
                    ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "."
                            + ArPropertyConstants.TOTAL_PREVIOUSLY_BILLED,
                    totalInDirectCostInvoiceDetail.getTotalPreviouslyBilled());
            parameterMap.put(
                    ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "."
                            + ArPropertyConstants.TOTAL_AMOUNT_BILLED_TO_DATE,
                    totalInDirectCostInvoiceDetail.getTotalAmountBilledToDate());
            parameterMap.put(
                    ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + "."
                            + ArPropertyConstants.AMOUNT_REMAINING_TO_BILL,
                    totalInDirectCostInvoiceDetail.getAmountRemainingToBill());
        }
        ContractsGrantsInvoiceDetail totalCostInvoiceDetail = document.getTotalCostInvoiceDetail();
        if (ObjectUtils.isNotNull(totalCostInvoiceDetail)) {
            parameterMap.put(
                    ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.INVOICE_DETAIL_IDENTIFIER,
                    totalCostInvoiceDetail.getInvoiceDetailIdentifier());
            parameterMap.put(ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + KFSPropertyConstants.DOCUMENT_NUMBER,
                    totalCostInvoiceDetail.getDocumentNumber());
            parameterMap.put(ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.CATEGORIES,
                    getConfigurationService()
                            .getPropertyValueAsString(ArKeyConstants.CONTRACTS_GRANTS_INVOICE_DETAILS_TOTAL_LABEL));
            parameterMap.put(ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.TOTAL_BUDGET,
                    totalCostInvoiceDetail.getTotalBudget());
            parameterMap.put(ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.INVOICE_AMOUNT,
                    totalCostInvoiceDetail.getInvoiceAmount());
            parameterMap.put(
                    ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.CUMULATIVE_EXPENDITURES,
                    totalCostInvoiceDetail.getCumulativeExpenditures());
            parameterMap.put(ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.BUDGET_REMAINING,
                    totalCostInvoiceDetail.getBudgetRemaining());
            parameterMap.put(
                    ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.TOTAL_PREVIOUSLY_BILLED,
                    totalCostInvoiceDetail.getTotalPreviouslyBilled());
            parameterMap.put(ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.ESTIMATED_COST,
                    totalCostInvoiceDetail.getTotalPreviouslyBilled().add(totalCostInvoiceDetail.getInvoiceAmount()));
            parameterMap.put(
                    ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.TOTAL_AMOUNT_BILLED_TO_DATE,
                    totalCostInvoiceDetail.getTotalAmountBilledToDate());
            parameterMap.put(
                    ArPropertyConstants.TOTAL_INVOICE_DETAIL + "." + ArPropertyConstants.AMOUNT_REMAINING_TO_BILL,
                    totalCostInvoiceDetail.getAmountRemainingToBill());
            parameterMap.put(
                    CuArPropertyConstants.TOTAL_PROGRAM_OUTLAYS_TO_DATE,
                    totalCostInvoiceDetail.getTotalAmountBilledToDate().add(document.getInvoiceGeneralDetail().getCostShareAmount()));
        }

        if (ObjectUtils.isNotNull(award)) {
            KualiDecimal billing = getAwardBilledToDateAmount(award.getProposalNumber());
            KualiDecimal payments = calculateTotalPaymentsToDateByAward(award);
            KualiDecimal receivable = billing.subtract(payments);
            parameterMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.BILLINGS, billing);
            parameterMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.PAYMENTS, payments);
            parameterMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.RECEIVABLES, receivable);
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.PROPOSAL_NUMBER,
                    award.getProposalNumber());
            if (ObjectUtils.isNotNull(award.getAwardBeginningDate())) {
                parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_BEGINNING_DATE,
                        getDateTimeService().toDateString(award.getAwardBeginningDate()));
            }
            if (ObjectUtils.isNotNull(award.getAwardEndingDate())) {
                parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_ENDING_DATE,
                        getDateTimeService().toDateString(award.getAwardEndingDate()));
            }
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_TOTAL_AMOUNT,
                    award.getAwardTotalAmount());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_ADDENDUM_NUMBER,
                    award.getAwardAddendumNumber());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "." + ArPropertyConstants.ContractsAndGrantsBillingAwardFields
                            .AWARD_ALLOCATED_UNIVERSITY_COMPUTING_SERVICES_AMOUNT,
                    award.getAwardAllocatedUniversityComputingServicesAmount());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.FEDERAL_PASS_THROUGH_FUNDED_AMOUNT,
                    award.getFederalPassThroughFundedAmount());
            if (ObjectUtils.isNotNull(award.getAwardEntryDate())) {
                parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_ENTRY_DATE,
                        getDateTimeService().toDateString(award.getAwardEntryDate()));
            }
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AGENCY_FUTURE_1_AMOUNT,
                    award.getAgencyFuture1Amount());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AGENCY_FUTURE_2_AMOUNT,
                    award.getAgencyFuture2Amount());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AGENCY_FUTURE_3_AMOUNT,
                    award.getAgencyFuture3Amount());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_DOCUMENT_NUMBER,
                    award.getAwardDocumentNumber());
            if (ObjectUtils.isNotNull(award.getAwardLastUpdateDate())) {
                parameterMap.put(
                        KFSPropertyConstants.AWARD + "."
                                + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_LAST_UPDATE_DATE,
                        getDateTimeService().toDateString(award.getAwardLastUpdateDate()));
            }
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.FEDERAL_PASS_THROUGH_INDICATOR,
                    award.getFederalPassThroughIndicator());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.OLD_PROPOSAL_NUMBER,
                    award.getOldProposalNumber());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_DIRECT_COST_AMOUNT,
                    award.getAwardDirectCostAmount());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_INDIRECT_COST_AMOUNT,
                    award.getAwardIndirectCostAmount());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.FEDERAL_FUNDED_AMOUNT,
                    award.getFederalFundedAmount());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_CREATE_TIMESTAMP,
                    award.getAwardCreateTimestamp());
            if (ObjectUtils.isNotNull(award.getAwardClosingDate())) {
                parameterMap.put(
                        KFSPropertyConstants.AWARD + "."
                                + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_CLOSING_DATE,
                        getDateTimeService().toDateString(award.getAwardClosingDate()));
            }
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PROPOSAL_AWARD_TYPE_CODE,
                    award.getProposalAwardTypeCode());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_STATUS_CODE,
                    award.getAwardStatusCode());
            if (ObjectUtils.isNotNull(award.getLetterOfCreditFund())) {
                parameterMap.put(
                        KFSPropertyConstants.AWARD + "." + ArPropertyConstants.LETTER_OF_CREDIT_FUND_GROUP_CODE,
                        award.getLetterOfCreditFund().getLetterOfCreditFundGroupCode());
            }
            parameterMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.LETTER_OF_CREDIT_FUND_CODE,
                    award.getLetterOfCreditFundCode());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.GRANT_DESCRIPTION_CODE,
                    award.getGrantDescriptionCode());
            if (ObjectUtils.isNotNull(award.getProposal())) {
                parameterMap.put(
                        KFSPropertyConstants.AWARD + "."
                                + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.GRANT_NUMBER,
                        award.getProposal().getGrantNumber());
            }
            parameterMap.put(KFSPropertyConstants.AGENCY_NUMBER, award.getAgencyNumber());
            parameterMap.put(KFSPropertyConstants.AGENCY + "." + KFSPropertyConstants.FULL_NAME,
                    award.getAgency().getFullName());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY_NUMBER,
                    award.getFederalPassThroughAgencyNumber());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AGENCY_ANALYST_NAME,
                    award.getAgencyAnalystName());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.ANALYST_TELEPHONE_NUMBER,
                    award.getAnalystTelephoneNumber());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.BILLING_FREQUENCY_CODE,
                    award.getBillingFrequencyCode());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_PROJECT_TITLE,
                    award.getAwardProjectTitle());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_PURPOSE_CODE,
                    award.getAwardPurposeCode());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.ACTIVE, award.isActive());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.KIM_GROUP_NAMES,
                    award.getKimGroupNames());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.ROUTING_ORG,
                    award.getRoutingOrg());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.ROUTING_CHART,
                    award.getRoutingChart());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.EXCLUDED_FROM_INVOICING,
                    award.isExcludedFromInvoicing());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.ADDITIONAL_FORMS_REQUIRED,
                    award.isAdditionalFormsRequiredIndicator());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.ADDITIONAL_FORMS_DESCRIPTION,
                    award.getAdditionalFormsDescription());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.INSTRUMENT_TYPE_CODE,
                    award.getInstrumentTypeCode());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.MIN_INVOICE_AMOUNT,
                    award.getMinInvoiceAmount());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AUTO_APPROVE,
                    award.getAutoApproveIndicator());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.LOOKUP_PERSON_UNIVERSAL_IDENTIFIER,
                    award.getLookupPersonUniversalIdentifier());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.LOOKUP_PERSON,
                    award.getLookupPerson().getPrincipalName());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.USER_LOOKUP_ROLE_NAMESPACE_CODE,
                    award.getUserLookupRoleNamespaceCode());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.USER_LOOKUP_ROLE_NAME,
                    award.getUserLookupRoleName());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.FUNDING_EXPIRATION_DATE,
                    award.getFundingExpirationDate());
            parameterMap.put(
                    KFSPropertyConstants.AWARD + "."
                            + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.STOP_WORK_INDICATOR,
                    award.isStopWorkIndicator());
            parameterMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.STOP_WORK_REASON,
                    award.getStopWorkReason());
            if (ObjectUtils.isNotNull(award.getAwardPrimaryProjectDirector())) {
                parameterMap.put(
                        KFSPropertyConstants.AWARD + "." + ArConstants.AWARD_PROJECT_DIRECTOR + "."
                                + KFSPropertyConstants.NAME,
                        award.getAwardPrimaryProjectDirector().getProjectDirector().getName());
            }
            parameterMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.LETTER_OF_CREDIT_FUND_CODE,
                    award.getLetterOfCreditFundCode());
            if (ObjectUtils.isNotNull(award.getAwardPrimaryFundManager())) {
                parameterMap.put(
                        KFSPropertyConstants.AWARD + "."
                                + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PRIMARY_FUND_MANAGER + "."
                                + KFSPropertyConstants.NAME,
                        award.getAwardPrimaryFundManager().getFundManager().getName());
                parameterMap.put(
                        KFSPropertyConstants.AWARD + "."
                                + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PRIMARY_FUND_MANAGER + "."
                                + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.EMAIL,
                        award.getAwardPrimaryFundManager().getFundManager().getEmailAddress());
                parameterMap.put(
                        KFSPropertyConstants.AWARD + "."
                                + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PRIMARY_FUND_MANAGER + "."
                                + ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PHONE,
                        award.getAwardPrimaryFundManager().getFundManager().getPhoneNumber());
            }
            if (ObjectUtils.isNotNull(document.getInvoiceGeneralDetail())) {
                parameterMap.put(ArPropertyConstants.TOTAL_AMOUNT_DUE,
                        receivable.add(document.getInvoiceGeneralDetail().getTotalAmountBilledToDate()));
            }
        }
        return new CuPdfFormattingMap(parameterMap);
    }

    private void setFinalStatusDate(ContractsGrantsInvoiceDocument document, Map<String, Object> parameterMap) {
        if (ObjectUtils.isNotNull(document.getDocumentHeader().getWorkflowDocument().getDateFinalized())) {
            parameterMap.put(ArPropertyConstants.FINAL_STATUS_DATE, getDateTimeService().toDateString(document.getDocumentHeader().getWorkflowDocument().getDateFinalized().toDate()));
        } else if (ObjectUtils.isNotNull(document.getDocumentHeader().getWorkflowDocument().getDateLastModified())) {
            parameterMap.put(ArPropertyConstants.FINAL_STATUS_DATE, getDateTimeService().toDateString(document.getDocumentHeader().getWorkflowDocument().getDateLastModified().toDate()));
        }
    }
    
    @Override
    public void prorateBill(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        LOG.debug("prorateBill, entering");
        KualiDecimal totalCost = new KualiDecimal(0); // Amount to be billed on
                                                      // this invoice
        // must iterate through the invoice details because the user might have
        // manually changed the value
        for (ContractsGrantsInvoiceDetail invD : contractsGrantsInvoiceDocument.getInvoiceDetails()) {
            totalCost = totalCost.add(invD.getInvoiceAmount());
        }
        
        KualiDecimal billedTotalCost = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail()
                .getTotalPreviouslyBilled(); // Total Billed so far

        // CU Customization, use award budget total, and not the award total
        // KualiDecimal accountAwardTotal = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAwardTotal();
        
        Award award = (Award) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
        KualiDecimal accountAwardTotal = awardExtension.getBudgetTotalAmount();
        
        if (accountAwardTotal.subtract(billedTotalCost).isGreaterEqual(new KualiDecimal(0))) {
            KualiDecimal amountEligibleForBilling = accountAwardTotal.subtract(billedTotalCost);
            // only recalculate if the current invoice is over what's billable.
            
            if (totalCost.isGreaterThan(amountEligibleForBilling)) {
                // use BigDecimal because percentage should not have only a
                // scale of 2, we need more for accuracy
                BigDecimal percentage = amountEligibleForBilling.bigDecimalValue().divide(totalCost.bigDecimalValue(),
                        10, BigDecimal.ROUND_HALF_DOWN);
                // use to check if rounding has left a few cents off
                KualiDecimal amountToBill = new KualiDecimal(0);

                ContractsGrantsInvoiceDetail largestCostCategory = null;
                BigDecimal largestAmount = BigDecimal.ZERO;
                for (ContractsGrantsInvoiceDetail invD : contractsGrantsInvoiceDocument.getInvoiceDetails()) {
                    BigDecimal newValue = invD.getInvoiceAmount().bigDecimalValue().multiply(percentage);
                    KualiDecimal newKualiDecimalValue = new KualiDecimal(newValue.setScale(2, BigDecimal.ROUND_DOWN));
                    invD.setInvoiceAmount(newKualiDecimalValue);
                    amountToBill = amountToBill.add(newKualiDecimalValue);
                    if (newValue.compareTo(largestAmount) > 0) {
                        largestAmount = newKualiDecimalValue.bigDecimalValue();
                        largestCostCategory = invD;
                    }
                }
                if (!amountToBill.equals(amountEligibleForBilling)) {
                    KualiDecimal remaining = amountEligibleForBilling.subtract(amountToBill);
                    if (ObjectUtils.isNull(largestCostCategory)
                            && CollectionUtils.isNotEmpty(contractsGrantsInvoiceDocument.getInvoiceDetails())) {
                        largestCostCategory = contractsGrantsInvoiceDocument.getInvoiceDetails().get(0);
                    }
                    if (ObjectUtils.isNotNull(largestCostCategory)) {
                        largestCostCategory.setInvoiceAmount(largestCostCategory.getInvoiceAmount().add(remaining));
                    }
                }
                recalculateTotalAmountBilledToDate(contractsGrantsInvoiceDocument);
            }
        }
    }
    
    @Override
    public void recalculateObjectCodeByCategory(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument,
            ContractsGrantsInvoiceDetail invoiceDetail, KualiDecimal total,
            List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        super.recalculateObjectCodeByCategory(contractsGrantsInvoiceDocument, invoiceDetail, total, invoiceDetailAccountObjectCodes);
    }
    

}
