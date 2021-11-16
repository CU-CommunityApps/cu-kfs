/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.ar.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerInvoiceDetail;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceItemCode;
import org.kuali.kfs.module.ar.businessobject.OrganizationAccountingDefault;
import org.kuali.kfs.module.ar.businessobject.SystemInformation;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.AccountsReceivablePendingEntryService;
import org.kuali.kfs.module.ar.document.service.AccountsReceivableTaxService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDetailService;
import org.kuali.kfs.module.ar.document.service.InvoicePaidAppliedService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.TaxService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Cornell Customization: backport redis*/
@Transactional
public class CustomerInvoiceDetailServiceImpl implements CustomerInvoiceDetailService {
    protected AccountsReceivablePendingEntryService accountsReceivablePendingEntryService;
    protected DateTimeService dateTimeService;
    protected UniversityDateService universityDateService;
    protected BusinessObjectService businessObjectService;
    protected FinancialSystemUserService financialSystemUserService;
    protected ParameterService parameterService;
    protected InvoicePaidAppliedService invoicePaidAppliedService;
    protected AccountsReceivableTaxService accountsReceivableTaxService;
    protected TaxService taxService;

    @Override
    public CustomerInvoiceDetail getCustomerInvoiceDetailFromOrganizationAccountingDefault(Integer universityFiscalYear, String chartOfAccountsCode, String organizationCode) {
        CustomerInvoiceDetail customerInvoiceDetail = new CustomerInvoiceDetail();

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("universityFiscalYear", universityFiscalYear);
        criteria.put("chartOfAccountsCode", chartOfAccountsCode);
        criteria.put("organizationCode", organizationCode);

        OrganizationAccountingDefault organizationAccountingDefault = businessObjectService.findByPrimaryKey(OrganizationAccountingDefault.class, criteria);
        if (ObjectUtils.isNotNull(organizationAccountingDefault)) {
            customerInvoiceDetail.setChartOfAccountsCode(organizationAccountingDefault.getDefaultInvoiceChartOfAccountsCode());
            customerInvoiceDetail.setAccountNumber(organizationAccountingDefault.getDefaultInvoiceAccountNumber());
            customerInvoiceDetail.setSubAccountNumber(organizationAccountingDefault.getDefaultInvoiceSubAccountNumber());
            customerInvoiceDetail.setFinancialObjectCode(organizationAccountingDefault.getDefaultInvoiceFinancialObjectCode());
            customerInvoiceDetail.setFinancialSubObjectCode(organizationAccountingDefault.getDefaultInvoiceFinancialSubObjectCode());
            customerInvoiceDetail.setProjectCode(organizationAccountingDefault.getDefaultInvoiceProjectCode());
            customerInvoiceDetail.setOrganizationReferenceId(organizationAccountingDefault.getDefaultInvoiceOrganizationReferenceIdentifier());
        }

        customerInvoiceDetail.setInvoiceItemTaxAmount(new KualiDecimal(0.00));
        customerInvoiceDetail.setInvoiceItemQuantity(new BigDecimal(1));
        customerInvoiceDetail.setInvoiceItemUnitOfMeasureCode(ArConstants.CUSTOMER_INVOICE_DETAIL_UOM_DEFAULT);
        customerInvoiceDetail.setTaxableIndicator(false);

        return customerInvoiceDetail;
    }

    @Override
    public CustomerInvoiceDetail getCustomerInvoiceDetailFromOrganizationAccountingDefaultForCurrentYear() {
        Integer currentUniversityFiscalYear = universityDateService.getCurrentFiscalYear();
        ChartOrgHolder currentUser = financialSystemUserService.getPrimaryOrganization(GlobalVariables.getUserSession().getPerson(), ArConstants.AR_NAMESPACE_CODE);
        return getCustomerInvoiceDetailFromOrganizationAccountingDefault(currentUniversityFiscalYear, currentUser.getChartOfAccountsCode(), currentUser.getOrganizationCode());
    }

    @Override
    public CustomerInvoiceDetail getCustomerInvoiceDetailFromCustomerInvoiceItemCode(String invoiceItemCode, String chartOfAccountsCode, String organizationCode) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put("invoiceItemCode", invoiceItemCode);
        criteria.put("chartOfAccountsCode", chartOfAccountsCode);
        criteria.put("organizationCode", organizationCode);
        CustomerInvoiceItemCode customerInvoiceItemCode = businessObjectService.findByPrimaryKey(CustomerInvoiceItemCode.class, criteria);

        CustomerInvoiceDetail customerInvoiceDetail = null;
        if (ObjectUtils.isNotNull(customerInvoiceItemCode)) {
            customerInvoiceDetail = new CustomerInvoiceDetail();
            customerInvoiceDetail.setChartOfAccountsCode(customerInvoiceItemCode.getDefaultInvoiceChartOfAccountsCode());
            customerInvoiceDetail.setAccountNumber(customerInvoiceItemCode.getDefaultInvoiceAccountNumber());
            customerInvoiceDetail.setSubAccountNumber(customerInvoiceItemCode.getDefaultInvoiceSubAccountNumber());
            customerInvoiceDetail.setFinancialObjectCode(customerInvoiceItemCode.getDefaultInvoiceFinancialObjectCode());
            customerInvoiceDetail.setFinancialSubObjectCode(customerInvoiceItemCode.getDefaultInvoiceFinancialSubObjectCode());
            customerInvoiceDetail.setProjectCode(customerInvoiceItemCode.getDefaultInvoiceProjectCode());

            customerInvoiceDetail.setOrganizationReferenceId(customerInvoiceItemCode.getDefaultInvoiceOrganizationReferenceIdentifier());
            customerInvoiceDetail.setInvoiceItemCode(customerInvoiceItemCode.getInvoiceItemCode());
            customerInvoiceDetail.setInvoiceItemDescription(customerInvoiceItemCode.getInvoiceItemDescription());
            customerInvoiceDetail.setInvoiceItemUnitPrice(customerInvoiceItemCode.getItemDefaultPrice());
            customerInvoiceDetail.setInvoiceItemUnitOfMeasureCode(customerInvoiceItemCode.getDefaultUnitOfMeasureCode());
            customerInvoiceDetail.setInvoiceItemQuantity(customerInvoiceItemCode.getItemDefaultQuantity());
            customerInvoiceDetail.setTaxableIndicator(customerInvoiceItemCode.isTaxableIndicator());

            // TODO set sales tax accordingly
            customerInvoiceDetail.setInvoiceItemTaxAmount(new KualiDecimal(0.00));

            // set amount = unit price * quantity
            customerInvoiceDetail.updateAmountBasedOnQuantityAndUnitPrice();
        }

        return customerInvoiceDetail;
    }

    @Override
    public List<String> getCustomerInvoiceDocumentNumbersByAccountNumber(String accountNumber) {
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("accountNumber", accountNumber);

        Collection<CustomerInvoiceDetail> customerInvoiceDetails = businessObjectService.findMatching(CustomerInvoiceDetail.class, fieldValues);
        List<String> docNumbers = new ArrayList<>();
        for (CustomerInvoiceDetail detail : customerInvoiceDetails) {
            docNumbers.add(detail.getDocumentNumber());
        }

        return docNumbers;
    }

    @Override
    public CustomerInvoiceDetail getCustomerInvoiceDetailFromCustomerInvoiceItemCodeForCurrentUser(String invoiceItemCode) {
        ChartOrgHolder currentUser = financialSystemUserService.getPrimaryOrganization(GlobalVariables.getUserSession().getPerson(), ArConstants.AR_NAMESPACE_CODE);
        return getCustomerInvoiceDetailFromCustomerInvoiceItemCode(invoiceItemCode, currentUser.getChartOfAccountsCode(), currentUser.getOrganizationCode());
    }

    @Override
    public CustomerInvoiceDetail getDiscountCustomerInvoiceDetail(CustomerInvoiceDetail customerInvoiceDetail, Integer universityFiscalYear, String chartOfAccountsCode, String organizationCode) {
        CustomerInvoiceDetail discountCustomerInvoiceDetail = (CustomerInvoiceDetail) ObjectUtils.deepCopy(customerInvoiceDetail);
        discountCustomerInvoiceDetail.setInvoiceItemUnitPriceToNegative();
        discountCustomerInvoiceDetail.updateAmountBasedOnQuantityAndUnitPrice();
        discountCustomerInvoiceDetail.setInvoiceItemDescription(ArConstants.DISCOUNT_PREFIX + StringUtils.trimToEmpty(customerInvoiceDetail.getInvoiceItemDescription()));

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("universityFiscalYear", universityFiscalYear);
        criteria.put("processingChartOfAccountCode", chartOfAccountsCode);
        criteria.put("processingOrganizationCode", organizationCode);

        SystemInformation systemInformation = businessObjectService.findByPrimaryKey(SystemInformation.class, criteria);
        if (ObjectUtils.isNotNull(systemInformation)) {
            discountCustomerInvoiceDetail.setFinancialObjectCode(systemInformation.getDiscountObjectCode());
        }

        return discountCustomerInvoiceDetail;
    }

    @Override
    public CustomerInvoiceDetail getDiscountCustomerInvoiceDetailForCurrentYear(CustomerInvoiceDetail customerInvoiceDetail, CustomerInvoiceDocument customerInvoiceDocument) {
        Integer currentUniversityFiscalYear = universityDateService.getCurrentFiscalYear();
        String processingChartOfAccountsCode = customerInvoiceDocument.getAccountsReceivableDocumentHeader().getProcessingChartOfAccountCode();
        String processingOrganizationCode = customerInvoiceDocument.getAccountsReceivableDocumentHeader().getProcessingOrganizationCode();
        return getDiscountCustomerInvoiceDetail(customerInvoiceDetail, currentUniversityFiscalYear, processingChartOfAccountsCode, processingOrganizationCode);
    }

    @Override
    public void recalculateCustomerInvoiceDetail(CustomerInvoiceDocument customerInvoiceDocument, CustomerInvoiceDetail customerInvoiceDetail) {
        // make sure amounts are negative when they are supposed to be
        if (!customerInvoiceDocument.isInvoiceReversal() && customerInvoiceDetail.isDiscountLine()) {
            customerInvoiceDetail.setInvoiceItemUnitPriceToNegative();
        } else if (customerInvoiceDocument.isInvoiceReversal() && !customerInvoiceDetail.isDiscountLine()) {
            customerInvoiceDetail.setInvoiceItemUnitPriceToNegative();
        }
        KualiDecimal pretaxAmount = customerInvoiceDetail.getInvoiceItemPreTaxAmount();

        KualiDecimal taxAmount = KualiDecimal.ZERO;
        if (accountsReceivableTaxService.isCustomerInvoiceDetailTaxable(customerInvoiceDocument, customerInvoiceDetail)) {
            String postalCode = accountsReceivableTaxService.getPostalCodeForTaxation(customerInvoiceDocument);
            taxAmount = taxService.getTotalSalesTaxAmount(dateTimeService.getCurrentSqlDate(), postalCode, pretaxAmount);
        }

        customerInvoiceDetail.setInvoiceItemTaxAmount(taxAmount);
        customerInvoiceDetail.setAmount(taxAmount.add(pretaxAmount));
    }

    @Override
    public void updateAccountsForCorrespondingDiscount(CustomerInvoiceDetail parent) {
        CustomerInvoiceDetail discount = parent.getDiscountCustomerInvoiceDetail();
        if (ObjectUtils.isNotNull(discount)) {
            if (StringUtils.isNotEmpty(parent.getAccountNumber())) {
                discount.setAccountNumber(parent.getAccountNumber());
            }
            if (StringUtils.isNotEmpty(parent.getSubAccountNumber())) {
                discount.setSubAccountNumber(parent.getSubAccountNumber());
            }
        }
    }

    @Override
    public CustomerInvoiceDetail getCustomerInvoiceDetail(String documentNumber, Integer sequenceNumber) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.DOCUMENT_NUMBER, documentNumber);
        criteria.put("sequenceNumber", sequenceNumber.toString());

        return businessObjectService.findByPrimaryKey(CustomerInvoiceDetail.class, criteria);
    }

    @Override
    public Collection<CustomerInvoiceDetail> getCustomerInvoiceDetailsForInvoice(String customerInvoiceDocumentNumber) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.DOCUMENT_NUMBER, customerInvoiceDocumentNumber);

        return businessObjectService.findMatching(CustomerInvoiceDetail.class, criteria);
    }

    @Override
    public Collection<CustomerInvoiceDetail> getCustomerInvoiceDetailsForInvoice(CustomerInvoiceDocument customerInvoiceDocument) {
        if (null == customerInvoiceDocument) {
            return new ArrayList<>();
        }
        return getCustomerInvoiceDetailsForInvoice(customerInvoiceDocument.getDocumentNumber());
    }

    @Override
    @Cacheable(cacheNames = AccountsReceivableCustomerInvoiceDetail.CACHE_NAME, key = "'{getCustomerInvoiceDetailsForInvoiceWithCaching}'+#p0")
    public Collection<CustomerInvoiceDetail> getCustomerInvoiceDetailsForInvoiceWithCaching(String customerInvoiceDocumentNumber) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.DOCUMENT_NUMBER, customerInvoiceDocumentNumber);

        return businessObjectService.findMatching(CustomerInvoiceDetail.class, criteria);
    }

    @Override
    public void updateAccountsReceivableObjectCode(CustomerInvoiceDetail customerInvoiceDetail) {
        customerInvoiceDetail.setAccountsReceivableObjectCode(getAccountsReceivablePendingEntryService().getAccountsReceivableObjectCode(customerInvoiceDetail));
    }

    @Override
    public void prepareCustomerInvoiceDetailForAdd(CustomerInvoiceDetail customerInvoiceDetail, CustomerInvoiceDocument customerInvoiceDocument) {
        recalculateCustomerInvoiceDetail(customerInvoiceDocument, customerInvoiceDetail);
        updateAccountsReceivableObjectCode(customerInvoiceDetail);
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public UniversityDateService getUniversityDateService() {
        return universityDateService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setInvoicePaidAppliedService(InvoicePaidAppliedService invoicePaidAppliedService) {
        this.invoicePaidAppliedService = invoicePaidAppliedService;
    }

    public InvoicePaidAppliedService getInvoicePaidAppliedService() {
        return invoicePaidAppliedService;
    }

    public TaxService getTaxService() {
        return taxService;
    }

    public void setTaxService(TaxService taxService) {
        this.taxService = taxService;
    }

    public AccountsReceivableTaxService getAccountsReceivableTaxService() {
        return accountsReceivableTaxService;
    }

    public void setAccountsReceivableTaxService(AccountsReceivableTaxService accountsReceivableTaxService) {
        this.accountsReceivableTaxService = accountsReceivableTaxService;
    }

    public FinancialSystemUserService getFinancialSystemUserService() {
        return financialSystemUserService;
    }

    public void setFinancialSystemUserService(FinancialSystemUserService financialSystemUserService) {
        this.financialSystemUserService = financialSystemUserService;
    }

    public AccountsReceivablePendingEntryService getAccountsReceivablePendingEntryService() {
        return accountsReceivablePendingEntryService;
    }

    public void setAccountsReceivablePendingEntryService(AccountsReceivablePendingEntryService accountsReceivablePendingEntryService) {
        this.accountsReceivablePendingEntryService = accountsReceivablePendingEntryService;
    }
}
