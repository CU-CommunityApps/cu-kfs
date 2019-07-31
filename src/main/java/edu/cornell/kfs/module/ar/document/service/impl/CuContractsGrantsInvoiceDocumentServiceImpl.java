package edu.cornell.kfs.module.ar.document.service.impl;

import java.math.BigDecimal;
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
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceDetailAccountObjectCode;
import org.kuali.kfs.module.ar.businessobject.OrganizationAccountingDefault;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.impl.ContractsGrantsInvoiceDocumentServiceImpl;
import org.kuali.kfs.module.ar.report.PdfFormattingMap;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.ar.CuArParameterKeyConstants;
import edu.cornell.kfs.module.ar.CuArPropertyConstants;
import edu.cornell.kfs.module.ar.businessobject.CustomerExtendedAttribute;
import edu.cornell.kfs.module.ar.document.service.CuContractsGrantsInvoiceDocumentService;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuContractsGrantsInvoiceDocumentServiceImpl extends ContractsGrantsInvoiceDocumentServiceImpl implements CuContractsGrantsInvoiceDocumentService {
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceDocumentServiceImpl.class);

    @Override
    protected Map<String, String> getTemplateParameterList(ContractsGrantsInvoiceDocument document) {
        Map<String, String> templateParameters = super.getTemplateParameterList(document);
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
        
        if (CollectionUtils.isNotEmpty(document.getDirectCostInvoiceDetails())) {
            processInvoiceDetails(document, localParameterMap);
        }
        
        if (!localParameterMap.isEmpty()) {
            LOG.debug("getTemplateParameterList, there were local parameters, adding them to the returning map.");
            templateParameters.putAll(new PdfFormattingMap(localParameterMap));
        }
        
        return templateParameters;
    }
    
    private void processInvoiceDetails(ContractsGrantsInvoiceDocument document, Map<String, Object> localParameterMap) {
        int i = 0;
        
        KualiDecimal indirectCostTotalBudget = KualiDecimal.ZERO;
        KualiDecimal indirectCostInvoiceAmount = KualiDecimal.ZERO;
        KualiDecimal indirectCosttotalAmountBilledToDate = KualiDecimal.ZERO;
        
        KualiDecimal directCostTotalBudget = KualiDecimal.ZERO;
        KualiDecimal directCostInvoiceAmount = KualiDecimal.ZERO;
        KualiDecimal directCosttotalAmountBilledToDate = KualiDecimal.ZERO;
        
        for (ContractsGrantsInvoiceDetail detail : document.getDirectCostInvoiceDetails()) {
            String thisInvoiceFieldStarter = ArPropertyConstants.INVOICE_DETAIL + "[" + i + "].";
            if (detail.getCostCategory().isIndirectCostIndicator()) {
                thisInvoiceFieldStarter = thisInvoiceFieldStarter + "MTDCY";
                indirectCostTotalBudget = indirectCostTotalBudget.add(detail.getTotalBudget());
                indirectCostInvoiceAmount = indirectCostInvoiceAmount.add(detail.getInvoiceAmount());
                indirectCosttotalAmountBilledToDate = indirectCosttotalAmountBilledToDate.add(detail.getTotalAmountBilledToDate());
            } else {
                thisInvoiceFieldStarter = thisInvoiceFieldStarter + "MTDCN";
                directCostTotalBudget = directCostTotalBudget.add(detail.getTotalBudget());
                directCostInvoiceAmount = directCostInvoiceAmount.add(detail.getInvoiceAmount());
                directCosttotalAmountBilledToDate = directCosttotalAmountBilledToDate.add(detail.getTotalAmountBilledToDate());
            }
            localParameterMap.put(thisInvoiceFieldStarter + ArPropertyConstants.CATEGORY, detail.getCostCategory().getCategoryName());
            localParameterMap.put(thisInvoiceFieldStarter + ArPropertyConstants.TOTAL_BUDGET, detail.getTotalBudget());
            localParameterMap.put(thisInvoiceFieldStarter + ArPropertyConstants.INVOICE_AMOUNT, detail.getInvoiceAmount());
            localParameterMap.put(thisInvoiceFieldStarter + ArPropertyConstants.TOTAL_AMOUNT_BILLED_TO_DATE, detail.getTotalAmountBilledToDate());
            i++;
        }
        
        localParameterMap.put("directCostInvoiceDetail.MTDCtotalBudget", directCostTotalBudget);
        localParameterMap.put("inDirectCostInvoiceDetail.totalBudget", indirectCostTotalBudget);
        localParameterMap.put("totalInvoiceDetail.MTDCtotalBudget", directCostTotalBudget.add(indirectCostTotalBudget));
        
        localParameterMap.put("directCostInvoiceDetail.MTDCinvoiceAmount", directCostInvoiceAmount);
        localParameterMap.put("inDirectCostInvoiceDetail.invoiceAmount", indirectCostInvoiceAmount);
        localParameterMap.put("totalInvoiceDetail.MTDCinvoiceAmount", directCostInvoiceAmount.add(indirectCostInvoiceAmount));
        
        localParameterMap.put("directCostInvoiceDetail.MTDCtotalAmountBilledToDate", directCosttotalAmountBilledToDate);
        localParameterMap.put("inDirectCostInvoiceDetail.totalAmountBilledToDate", indirectCosttotalAmountBilledToDate);
        localParameterMap.put("totalInvoiceDetail.MTDCtotalAmountBilledToDate", directCosttotalAmountBilledToDate.add(indirectCosttotalAmountBilledToDate));
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
    
    @Override
    protected String getRecipientAccountNumber(List<InvoiceAccountDetail> accountDetails) {
        if (CollectionUtils.isNotEmpty(accountDetails)) {
            Account contractControlAccount = determineContractControlAccount(accountDetails.get(0));
            if (ObjectUtils.isNotNull(contractControlAccount) 
                    && StringUtils.isNotBlank(contractControlAccount.getAccountNumber())) {
                return contractControlAccount.getAccountNumber();
            }
        }
        return null;
    }
    
    //Part of the logic to determine contract control account was obtained from FINP-4726.
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
    
    @Override
    protected CustomerInvoiceDetail createSourceAccountingLinesByContractControlAccount(
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument,
            final OrganizationAccountingDefault organizationAccountingDefault) {
        String coaCode = null;
        String accountNumber = null;

        List<InvoiceAccountDetail> accountDetails = contractsGrantsInvoiceDocument.getAccountDetails();
        if (CollectionUtils.isNotEmpty(accountDetails)) {
            Account contractControlAccount = determineContractControlAccount(accountDetails.get(0));
            if (ObjectUtils.isNotNull(contractControlAccount)) {
                coaCode = contractControlAccount.getChartOfAccountsCode();
                accountNumber = contractControlAccount.getAccountNumber();
            }
        }

        String objectCode = organizationAccountingDefault.getDefaultInvoiceFinancialObjectCode();

        return createSourceAccountingLine(contractsGrantsInvoiceDocument.getDocumentNumber(),
                coaCode, accountNumber, objectCode, getTotalAmountForInvoice(contractsGrantsInvoiceDocument), 1);
    }

    /**
     * Overridden to also update the invoice due date.
     * 
     * NOTE: There isn't an easy way to hook in the invoice-due-date updates independently of this
     * without overlaying the CINV document class. (This method only appears to be called when CINV docs
     * enter PROCESSED status, so hooking in here is okay for now.) If ContractsGrantsInvoiceDocument
     * gets updated to make it easier to hook in custom PROCESSED-status handling, then we should rework
     * this customization.
     */
    @Override
    public void updateLastBilledDate(ContractsGrantsInvoiceDocument document) {
        super.updateLastBilledDate(document);
        setInvoiceDueDateBasedOnNetTermsAndCurrentDate(document);
    }

    @Override
    public void setInvoiceDueDateBasedOnNetTermsAndCurrentDate(ContractsGrantsInvoiceDocument document) {
        Date invoiceDueDate = calculateInvoiceDueDate(document);
        document.setInvoiceDueDate(invoiceDueDate);
    }

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
