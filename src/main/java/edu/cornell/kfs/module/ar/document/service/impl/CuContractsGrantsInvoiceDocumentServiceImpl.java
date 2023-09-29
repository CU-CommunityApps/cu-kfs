package edu.cornell.kfs.module.ar.document.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.integration.cg.CGIntegrationConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceDetailAccountObjectCode;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.impl.ContractsGrantsInvoiceDocumentServiceImpl;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.ar.CuArParameterKeyConstants;
import edu.cornell.kfs.module.ar.CuArPropertyConstants;
import edu.cornell.kfs.module.ar.businessobject.CustomerExtendedAttribute;
import edu.cornell.kfs.module.ar.document.service.CuContractsGrantsInvoiceDocumentService;
import edu.cornell.kfs.module.cg.businessobject.AwardAccountExtendedAttribute;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuContractsGrantsInvoiceDocumentServiceImpl extends ContractsGrantsInvoiceDocumentServiceImpl implements CuContractsGrantsInvoiceDocumentService {
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Add institution specific parameters to the template parameters
     */
    @Override
    protected Map<String, String> getTemplateParameterList(final ContractsGrantsInvoiceDocument document) {
        final Map<String, String> templateParameters = new HashMap<String, String>();
        templateParameters.putAll(super.getTemplateParameterList(document));
        
        final Map<String, String> localParameterMap = getInstitutionTemplateParameters(document);
        
        if(!localParameterMap.isEmpty()) {
            templateParameters.putAll(localParameterMap);
        }
              
        return templateParameters;
    }

    protected Map<String, String> getInstitutionTemplateParameters(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> localParameterMap =  new HashMap<String, Object>();
        
        if (document.getInvoiceGeneralDetail().isFinalBillIndicator()) {
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.FINAL_BILL, CUKFSConstants.CAPITAL_X);
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.PARTIAL_BILL, StringUtils.EMPTY);
        } else {
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.FINAL_BILL, StringUtils.EMPTY);
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.PARTIAL_BILL, CUKFSConstants.CAPITAL_X);
        }
        
        ContractsGrantsInvoiceDetail totalCostInvoiceDetail = document.getTotalCostInvoiceDetail();
        if (ObjectUtils.isNotNull(totalCostInvoiceDetail)) {
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.TOTAL_PROGRAM_OUTLAYS_TO_DATE,
                    totalCostInvoiceDetail.getTotalAmountBilledToDate().add(document.getInvoiceGeneralDetail().getCostShareAmount()));
        }
        
        
        setAwardExtendedAttributeValuesInParameterMap(document, localParameterMap);
        setPurchaseOrderNumberFieldInParameterMap(document, localParameterMap);
        
        if (!localParameterMap.isEmpty()) {
            LOG.debug("getInstitutionTemplateParameters, there were local parameters, these will be in the returned map.");
        }
        
        return localParameterMap.keySet().stream()
                .collect(Collectors.toMap(key -> key, key -> stringifyValue(localParameterMap.get(key)), (a, b) -> b));
    }
    
    protected void setAwardExtendedAttributeValuesInParameterMap(ContractsGrantsInvoiceDocument document, Map<String, Object> localParameterMap) {
        Award award = (Award) document.getInvoiceGeneralDetail().getAward();
        AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
        if (ObjectUtils.isNotNull(awardExtension)) {
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_BUDGET_START_DATE,
                    awardExtension.getBudgetBeginningDate());
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_BUDGET_END_DATE, awardExtension.getBudgetEndingDate());
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_BUDGET_TOTAL, awardExtension.getBudgetTotalAmount());
            localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_PRIME_AGREEMENT_NUMBER,
                    awardExtension.getPrimeAgreementNumber());
        }
    }

    protected void setPurchaseOrderNumberFieldInParameterMap(ContractsGrantsInvoiceDocument document, Map<String, Object> localParameterMap) {
        Award award = (Award) document.getInvoiceGeneralDetail().getAward();
        String invoiceOptionCode = award.getInvoicingOptionCode();
        String purchaseOrderNumber = StringUtils.EMPTY;

        if (StringUtils.equals(invoiceOptionCode, CGIntegrationConstants.AwardInvoicingOption.Types.AWARD.getCode())) {
            purchaseOrderNumber = findPurchaseOrderNumberForInvoiceOptionAward(award);
        } else if (StringUtils.equals(invoiceOptionCode, CGIntegrationConstants.AwardInvoicingOption.Types.ACCOUNT.getCode())) {
            purchaseOrderNumber = findPurchaseOrderNumberForInvoiceOptionAccount(document);
        } else if (StringUtils.equals(invoiceOptionCode, CGIntegrationConstants.AwardInvoicingOption.Types.CONTRACT_CONTROL.getCode())
                || StringUtils.equals(invoiceOptionCode, CGIntegrationConstants.AwardInvoicingOption.Types.SCHEDULE.getCode())) {
            purchaseOrderNumber = findPurchaseOrderNumberForInvoiceOptionContractControlAndScheduled(document);
        }
        
        if (StringUtils.isBlank(purchaseOrderNumber)) {
            LOG.error("setPurchaseOrderNumberFieldInParameterMap, unable to find the purchase order number for document " + document.getDocumentNumber());
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPurchaseOrderNumberFieldInParameterMap, for document " + document.getDocumentNumber() + " with an invoice option code of "
                    + invoiceOptionCode + " the purchase order number is " + purchaseOrderNumber);
        }

        localParameterMap.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_PURCHASE_ORDER_NBR, purchaseOrderNumber);
    }

    protected String findPurchaseOrderNumberForInvoiceOptionAward(Award award) {
        String purchaseOrderNumber = StringUtils.EMPTY;
        AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
        if (ObjectUtils.isNotNull(awardExtension)) {
            purchaseOrderNumber = awardExtension.getPurchaseOrderNumber();
        }
        return purchaseOrderNumber;
    }

    protected String findPurchaseOrderNumberForInvoiceOptionAccount(ContractsGrantsInvoiceDocument document) {
        String purchaseOrderNumber = StringUtils.EMPTY;
        List<InvoiceAccountDetail> invoiceAccountDetails = document.getAccountDetails();
        if (CollectionUtils.isNotEmpty(invoiceAccountDetails)) {
            Account invoiceAccount = invoiceAccountDetails.get(0).getAccount();
            AwardAccountExtendedAttribute awardAccountExtension = findAwardAccountExtendedAttribute(invoiceAccountDetails.get(0).getProposalNumber(),
                    invoiceAccount.getChartOfAccountsCode(), invoiceAccount.getAccountNumber());
            if (ObjectUtils.isNotNull(awardAccountExtension)) {
                purchaseOrderNumber = awardAccountExtension.getAccountPurchaseOrderNumber();
            }
        }
        return purchaseOrderNumber;
    }

    protected AwardAccountExtendedAttribute findAwardAccountExtendedAttribute(String proposalNumber, String chartOfAccountsCode, String accountNumber) {
        Map<String, Object> primaryKeys = new HashMap<String, Object>();
        primaryKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        primaryKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        primaryKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        AwardAccountExtendedAttribute awardAccountExtension = businessObjectService.findByPrimaryKey(AwardAccountExtendedAttribute.class, primaryKeys);
        return awardAccountExtension;
    }

    protected String findPurchaseOrderNumberForInvoiceOptionContractControlAndScheduled(ContractsGrantsInvoiceDocument document) {
        String purchaseOrderNumber = StringUtils.EMPTY;
        List<InvoiceAccountDetail> invoiceAccountDetails = document.getAccountDetails();
        if (CollectionUtils.isNotEmpty(invoiceAccountDetails)) {
            Account contractControlAccount = determineContractControlAccount(invoiceAccountDetails.get(0));
            if (ObjectUtils.isNotNull(contractControlAccount)) {
                AwardAccountExtendedAttribute awardAccountExtension = findAwardAccountExtendedAttribute(invoiceAccountDetails.get(0).getProposalNumber(),
                        contractControlAccount.getChartOfAccountsCode(), contractControlAccount.getAccountNumber());
                if (ObjectUtils.isNotNull(awardAccountExtension)) {
                    purchaseOrderNumber = awardAccountExtension.getAccountPurchaseOrderNumber();
                }
            }
        }
        return purchaseOrderNumber;
    }
    
    @Override
    public void prorateBill(final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        LOG.debug("prorateBill, entering");
        KualiDecimal totalCost = new KualiDecimal(0); // Amount to be billed on
                                                      // this invoice
        // must iterate through the invoice details because the user might have
        // manually changed the value
        for (final ContractsGrantsInvoiceDetail invD : contractsGrantsInvoiceDocument.getInvoiceDetails()) {
            totalCost = totalCost.add(invD.getInvoiceAmount());
        }
        
        // Total Billed so far
        final KualiDecimal billedTotalCost = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail()
                .getTotalPreviouslyBilled();

        // CU Customization, use award budget total, and not the award total
        // KualiDecimal accountAwardTotal = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAwardTotal();
        
        Award award = (Award) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
        final KualiDecimal accountAwardTotal = awardExtension.getBudgetTotalAmount();
        
        if (accountAwardTotal.subtract(billedTotalCost).isGreaterEqual(new KualiDecimal(0))) {
            final KualiDecimal amountEligibleForBilling = accountAwardTotal.subtract(billedTotalCost);
            // only recalculate if the current invoice is over what's billable.
            
            if (totalCost.isGreaterThan(amountEligibleForBilling)) {
                // use BigDecimal because percentage should not have only a
                // scale of 2, we need more for accuracy
                final BigDecimal percentage = amountEligibleForBilling.bigDecimalValue().divide(totalCost.bigDecimalValue(),
                        10, RoundingMode.HALF_DOWN);
                // use to check if rounding has left a few cents off
                KualiDecimal amountToBill = new KualiDecimal(0);

                ContractsGrantsInvoiceDetail largestCostCategory = null;
                BigDecimal largestAmount = BigDecimal.ZERO;
                for (final ContractsGrantsInvoiceDetail invD : contractsGrantsInvoiceDocument.getInvoiceDetails()) {
                    final BigDecimal newValue = invD.getInvoiceAmount().bigDecimalValue().multiply(percentage);
                    final KualiDecimal newKualiDecimalValue = new KualiDecimal(newValue.setScale(2, RoundingMode.DOWN));
                    invD.setInvoiceAmount(newKualiDecimalValue);
                    amountToBill = amountToBill.add(newKualiDecimalValue);
                    if (newValue.compareTo(largestAmount) > 0) {
                        largestAmount = newKualiDecimalValue.bigDecimalValue();
                        largestCostCategory = invD;
                    }
                }
                if (!amountToBill.equals(amountEligibleForBilling)) {
                    final KualiDecimal remaining = amountEligibleForBilling.subtract(amountToBill);
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
    public void recalculateObjectCodeByCategory(
            final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument,
            final ContractsGrantsInvoiceDetail invoiceDetail, KualiDecimal total,
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        super.recalculateObjectCodeByCategory(contractsGrantsInvoiceDocument, invoiceDetail, total, invoiceDetailAccountObjectCodes);
    }
    
    /*
     * CUMod: KFSPTS-14929
     */
    @Override
    protected String determineContractControlAccountNumber(final ContractsGrantsInvoiceDocument document) {
        final List<InvoiceAccountDetail> accountDetails = document.getAccountDetails();
        if (CollectionUtils.isNotEmpty(accountDetails)) {
            Account contractControlAccount = determineContractControlAccount(accountDetails.get(0));
            if (ObjectUtils.isNotNull(contractControlAccount) 
                    && StringUtils.isNotBlank(contractControlAccount.getAccountNumber())) {
                return contractControlAccount.getAccountNumber();
            }
        }
        return null;
    }
    
    /*
     * CUMod: KFSPTS-14929
     * 
     * Part of the logic to determine contract control account was obtained from FINP-4726.
     */
    @Override
    public Account determineContractControlAccount(InvoiceAccountDetail invoiceAccountDetail) {
        Account contractControlAccount = null;
        Account account = invoiceAccountDetail.getAccount();
        if (ObjectUtils.isNull(account)) {
            invoiceAccountDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
            account = invoiceAccountDetail.getAccount();
        }
        if (ObjectUtils.isNotNull(account)) {
            contractControlAccount = account.getContractControlAccount();
            if (ObjectUtils.isNull(contractControlAccount)) {
                account.refreshReferenceObject(KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT);
                contractControlAccount = account.getContractControlAccount();
            }
        }
        return contractControlAccount;
    }
    
    /*
     * CUMod: KFSPTS-14929
     */
    @Override
    protected CustomerInvoiceDetail createSourceAccountingLinesByContractControlAccount(
            final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        String coaCode = null;
        String accountNumber = null;
        SubFundGroup subFundGroup = null;

        final List<InvoiceAccountDetail> accountDetails = contractsGrantsInvoiceDocument.getAccountDetails();
        if (CollectionUtils.isNotEmpty(accountDetails)) {
            final Account contractControlAccount = determineContractControlAccount(accountDetails.get(0));
            if (ObjectUtils.isNotNull(contractControlAccount)) {
                coaCode = contractControlAccount.getChartOfAccountsCode();
                accountNumber = contractControlAccount.getAccountNumber();
                subFundGroup = contractControlAccount.getSubFundGroup();
                if (ObjectUtils.isNull(subFundGroup)) {
                    contractControlAccount.refreshReferenceObject(KFSPropertyConstants.SUB_FUND_GROUP);
                    subFundGroup = contractControlAccount.getSubFundGroup();
                }
            }
        }

        return createSourceAccountingLine(contractsGrantsInvoiceDocument.getDocumentNumber(),
                coaCode, accountNumber, subFundGroup, getTotalAmountForInvoice(contractsGrantsInvoiceDocument), 1);
    }

    /*
     * CUMod: KFSPTS-15342
     * 
     * Overridden to also update the invoice due date.
     * 
     * NOTE: There isn't an easy way to hook in the invoice-due-date updates independently of this
     * without overlaying the CINV document class. (This method only appears to be called when CINV docs
     * enter PROCESSED status, so hooking in here is okay for now.) If ContractsGrantsInvoiceDocument
     * gets updated to make it easier to hook in custom PROCESSED-status handling, then we should rework
     * this customization.
     */
    @Override
    public void updateLastBilledDate(final ContractsGrantsInvoiceDocument document) {
        super.updateLastBilledDate(document);
        setInvoiceDueDateBasedOnNetTermsAndCurrentDate(document);
    }

    /*
     * CUMod: KFSPTS-15342
     */
    @Override
    public void setInvoiceDueDateBasedOnNetTermsAndCurrentDate(ContractsGrantsInvoiceDocument document) {
        Date invoiceDueDate = calculateInvoiceDueDate(document);
        document.setInvoiceDueDate(invoiceDueDate);
    }

    /*
     * CUMod: KFSPTS-15342
     */
    protected Date calculateInvoiceDueDate(ContractsGrantsInvoiceDocument document) {
        Calendar calendar = dateTimeService.getCurrentCalendar();
        Optional<Integer> customerNetTerms = getCustomerNetTerms(document);
        int netTermsInDays;
        
        if (customerNetTerms.isPresent()) {
            netTermsInDays = customerNetTerms.get();
        } else {
            String defaultNetTerms = parameterService.getParameterValueAsString(
                    ArConstants.AR_NAMESPACE_CODE, ArConstants.CUSTOMER_COMPONENT, CuArParameterKeyConstants.CG_INVOICE_TERMS_DUE_DATE);
            if (StringUtils.isBlank(defaultNetTerms)) {
                throw new RuntimeException("Could not find a net terms value for parameter " + CuArParameterKeyConstants.CG_INVOICE_TERMS_DUE_DATE);
            }
            
            try {
                netTermsInDays = Integer.parseInt(defaultNetTerms);
            } catch (NumberFormatException e) {
                LOG.error("calculateInvoiceDueDate, Net terms parameter value is malformed", e);
                throw new RuntimeException("Net terms value is malformed for parameter " + CuArParameterKeyConstants.CG_INVOICE_TERMS_DUE_DATE);
            }
        }
        
        calendar.add(Calendar.DATE, netTermsInDays);
        return new Date(calendar.getTimeInMillis());
    }

    /*
     * CUMod: KFSPTS-15342
     */
    protected Optional<Integer> getCustomerNetTerms(ContractsGrantsInvoiceDocument document) {
        Customer customer = null;
        
        AccountsReceivableDocumentHeader documentHeader = document.getAccountsReceivableDocumentHeader();
        if (ObjectUtils.isNotNull(documentHeader)) {
            documentHeader.refreshReferenceObject(ArPropertyConstants.CustomerInvoiceDocumentFields.CUSTOMER);
            customer = documentHeader.getCustomer();
        }
        
        if (ObjectUtils.isNotNull(customer)) {
            CustomerExtendedAttribute customerExtension = (CustomerExtendedAttribute) customer.getExtension();
            if (ObjectUtils.isNotNull(customerExtension)) {
                return Optional.ofNullable(customerExtension.getNetTermsInDays());
            }
        }
        
        return Optional.empty();
    }

}
