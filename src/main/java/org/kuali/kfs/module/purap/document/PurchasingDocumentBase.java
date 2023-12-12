/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.purap.document;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.integration.purap.CapitalAssetSystem;
import org.kuali.kfs.krad.rules.rule.event.ApproveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.BillingAddress;
import org.kuali.kfs.module.purap.businessobject.CapitalAssetSystemState;
import org.kuali.kfs.module.purap.businessobject.CapitalAssetSystemType;
import org.kuali.kfs.module.purap.businessobject.DeliveryRequiredDateReason;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderTransmissionMethod;
import org.kuali.kfs.module.purap.businessobject.PurchasingCapitalAssetItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.businessobject.ReceivingAddress;
import org.kuali.kfs.module.purap.businessobject.RecurringPaymentType;
import org.kuali.kfs.module.purap.businessobject.RequisitionSource;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchasingDocumentSpecificService;
import org.kuali.kfs.module.purap.document.service.PurchasingService;
import org.kuali.kfs.module.purap.document.service.ReceivingAddressService;
import org.kuali.kfs.module.purap.util.ItemParser;
import org.kuali.kfs.module.purap.util.ItemParserBase;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.LocationService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.CampusParameter;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.businessobject.PurchaseOrderCostSource;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Base class for Purchasing Documents.
 */
public abstract class PurchasingDocumentBase extends PurchasingAccountsPayableDocumentBase implements
        PurchasingDocument {

    protected String requisitionSourceCode;
    protected String purchaseOrderTransmissionMethodCode;
    protected String purchaseOrderCostSourceCode;
    protected String deliveryRequiredDateReasonCode;
    protected String recurringPaymentTypeCode;
    protected String chartOfAccountsCode;
    protected String organizationCode;
    protected String deliveryCampusCode;
    protected KualiDecimal purchaseOrderTotalLimit;
    protected Boolean vendorRestrictedIndicator;
    protected String vendorPhoneNumber;
    protected String vendorFaxNumber;
    protected Integer vendorContractGeneratedIdentifier;
    protected String vendorNoteText;
    protected String requestorPersonName;
    protected String requestorPersonEmailAddress;
    protected String requestorPersonPhoneNumber;
    protected String nonInstitutionFundOrgChartOfAccountsCode;
    protected String nonInstitutionFundOrganizationCode;
    protected String nonInstitutionFundChartOfAccountsCode;
    protected String nonInstitutionFundAccountNumber;
    protected boolean deliveryBuildingOtherIndicator;
    protected String deliveryBuildingCode;
    protected String deliveryBuildingName;
    protected String deliveryBuildingRoomNumber;
    protected String deliveryBuildingLine1Address;
    protected String deliveryBuildingLine2Address;
    protected String deliveryCityName;
    protected String deliveryStateCode;
    protected String deliveryPostalCode;
    protected String deliveryCountryCode;
    protected String deliveryToName;
    protected String deliveryToEmailAddress;
    protected String deliveryToPhoneNumber;
    protected Date deliveryRequiredDate;
    protected String deliveryInstructionText;
    protected Date purchaseOrderBeginDate;
    protected Date purchaseOrderEndDate;
    protected String institutionContactName;
    protected String institutionContactPhoneNumber;
    protected String institutionContactEmailAddress;
    protected String billingName;
    protected String billingLine1Address;
    protected String billingLine2Address;
    protected String billingCityName;
    protected String billingStateCode;
    protected String billingPostalCode;
    protected String billingCountryCode;
    protected String billingPhoneNumber;
    protected String billingEmailAddress;
    protected String receivingName;
    protected String receivingLine1Address;
    protected String receivingLine2Address;
    protected String receivingCityName;
    protected String receivingStateCode;
    protected String receivingPostalCode;
    protected String receivingCountryCode;
    // if true, use receiving address
    protected boolean addressToVendorIndicator;
    protected String externalOrganizationB2bSupplierIdentifier;
    protected boolean purchaseOrderAutomaticIndicator;
    protected String vendorPaymentTermsCode;
    protected String vendorShippingTitleCode;
    protected String vendorShippingPaymentTermsCode;
    protected String capitalAssetSystemTypeCode;
    protected String capitalAssetSystemStateCode;
    protected String justification;

    // NOT PERSISTED IN DB
    protected String supplierDiversityLabel;
    protected String vendorContactsLabel;

    protected RequisitionSource requisitionSource;
    protected PurchaseOrderTransmissionMethod purchaseOrderTransmissionMethod;
    protected PurchaseOrderCostSource purchaseOrderCostSource;
    protected DeliveryRequiredDateReason deliveryRequiredDateReason;
    protected RecurringPaymentType recurringPaymentType;
    protected Organization organization;
    protected Chart chartOfAccounts;
    protected CampusParameter deliveryCampus;
    protected Chart nonInstitutionFundOrgChartOfAccounts;
    protected Organization nonInstitutionFundOrganization;
    protected Account nonInstitutionFundAccount;
    protected Chart nonInstitutionFundChartOfAccounts;
    protected VendorContract vendorContract;
    protected CapitalAssetSystemType capitalAssetSystemType;
    protected CapitalAssetSystemState capitalAssetSystemState;
    protected List<CapitalAssetSystem> purchasingCapitalAssetSystems;
    protected List<PurchasingCapitalAssetItem> purchasingCapitalAssetItems;

    protected boolean receivingDocumentRequiredIndicator;
    protected boolean paymentRequestPositiveApprovalIndicator;

    protected List<CommodityCode> commodityCodesForRouting;
    // KFSPTS-985 : this is for setdistribution
    private Integer favoriteAccountLineIdentifier;

    public PurchasingDocumentBase() {
        super();

        purchasingCapitalAssetItems = new ArrayList<>();
        purchasingCapitalAssetSystems = new ArrayList<>();
    }

    @Override
    public abstract PurchasingDocumentSpecificService getDocumentSpecificService();

    @Override
    public void templateVendorDetail(final VendorDetail vendorDetail) {
        if (ObjectUtils.isNotNull(vendorDetail)) {
            setVendorDetail(vendorDetail);
            setVendorName(vendorDetail.getVendorName());
            setVendorShippingTitleCode(vendorDetail.getVendorShippingTitleCode());
            setVendorPaymentTermsCode(vendorDetail.getVendorPaymentTermsCode());
            setVendorShippingPaymentTermsCode(vendorDetail.getVendorShippingPaymentTermsCode());
            setVendorCustomerNumber("");
        }
    }

    @Override
    public void templateVendorContract(final VendorContract vendorContract) {
        if (ObjectUtils.isNotNull(vendorContract)) {
            setVendorContract(vendorContract);
            setVendorContractGeneratedIdentifier(vendorContract.getVendorContractGeneratedIdentifier());
            setVendorShippingTitleCode(vendorContract.getVendorShippingTitleCode());
            setVendorPaymentTermsCode(vendorContract.getVendorPaymentTermsCode());
            setVendorShippingPaymentTermsCode(vendorContract.getVendorShippingPaymentTermsCode());
            setPurchaseOrderCostSourceCode(vendorContract.getPurchaseOrderCostSourceCode());
        }
    }

    @Override
    public void templateVendorAddress(final VendorAddress vendorAddress) {
        super.templateVendorAddress(vendorAddress);
        if (vendorAddress != null) {
            setVendorFaxNumber(vendorAddress.getVendorFaxNumber());
            setVendorAttentionName(vendorAddress.getVendorAttentionName());
            // KFSUPGRADE-348 : CU enhancement
            //need to save vendorAddressGeneratedIdentifier for Method of PO Transmission mod, value is null in business object when it is needed
            setVendorAddressGeneratedIdentifier(vendorAddress.getVendorAddressGeneratedIdentifier());            
            //Method of PO Transmission on Vendor Address should be the default when a vendor is selected.
            //set purchasing document value for po transmission method
            setPurchaseOrderTransmissionMethodCode(((CuVendorAddressExtension)vendorAddress.getExtension()).getPurchaseOrderTransmissionMethodCode());
            // end CU enhancement
        }
    }

    @Override
    public void templateBillingAddress(final BillingAddress billingAddress) {
        if (ObjectUtils.isNotNull(billingAddress)) {
            setBillingName(billingAddress.getBillingName());
            setBillingLine1Address(billingAddress.getBillingLine1Address());
            setBillingLine2Address(billingAddress.getBillingLine2Address());
            setBillingCityName(billingAddress.getBillingCityName());
            setBillingStateCode(billingAddress.getBillingStateCode());
            setBillingPostalCode(billingAddress.getBillingPostalCode());
            setBillingCountryCode(billingAddress.getBillingCountryCode());
            setBillingPhoneNumber(billingAddress.getBillingPhoneNumber());
            setBillingEmailAddress(billingAddress.getBillingEmailAddress());
        }
    }

    @Override
    public void templateReceivingAddress(final ReceivingAddress receivingAddress) {
        if (receivingAddress != null) {
            setReceivingName(receivingAddress.getReceivingName());
            setReceivingLine1Address(receivingAddress.getReceivingLine1Address());
            setReceivingLine2Address(receivingAddress.getReceivingLine2Address());
            setReceivingCityName(receivingAddress.getReceivingCityName());
            setReceivingStateCode(receivingAddress.getReceivingStateCode());
            setReceivingPostalCode(receivingAddress.getReceivingPostalCode());
            setReceivingCountryCode(receivingAddress.getReceivingCountryCode());
            setAddressToVendorIndicator(receivingAddress.isUseReceivingIndicator());
        } else {
            setReceivingName(null);
            setReceivingLine1Address(null);
            setReceivingLine2Address(null);
            setReceivingCityName(null);
            setReceivingStateCode(null);
            setReceivingPostalCode(null);
            setReceivingCountryCode(null);
            setAddressToVendorIndicator(false);
        }
    }

    /**
     * Loads the default receiving address from database corresponding to the chart/org of this document.
     */
    @Override
    public void loadReceivingAddress() {
        final String chartCode = getChartOfAccountsCode();
        final String orgCode = getOrganizationCode();
        ReceivingAddress address = SpringContext.getBean(ReceivingAddressService.class)
                .findUniqueDefaultByChartOrg(chartCode, orgCode);
        // if default address for chart/org not found, look for chart default
        if (address == null && orgCode != null) {
            address = SpringContext.getBean(ReceivingAddressService.class).findUniqueDefaultByChartOrg(chartCode, null);
        }
        templateReceivingAddress(address);
    }

    /**
     * Iterates through the purchasingCapitalAssetItems of the document and returns the purchasingCapitalAssetItem
     * with the item id equal to the number given, or null if a match is not found.
     *
     * @param itemIdentifier item id to match on.
     * @return the PurchasingCapitalAssetItem if a match is found, else null.
     */
    public PurchasingCapitalAssetItem getPurchasingCapitalAssetItemByItemIdentifier(final int itemIdentifier) {
        for (final PurchasingCapitalAssetItem camsItem : purchasingCapitalAssetItems) {
            if (camsItem.getItemIdentifier() == itemIdentifier) {
                return camsItem;
            }
        }
        return null;
    }

    @Override
    public void addItem(final PurApItem item) {
        item.refreshReferenceObject(PurapPropertyConstants.COMMODITY_CODE);
        super.addItem(item);
    }

    @Override
    public void deleteItem(final int lineNum) {
        // remove associated asset items
        final PurApItem item = items.get(lineNum);
        if (ObjectUtils.isNotNull(item) && item.getItemIdentifier() != null) {
            final PurchasingCapitalAssetItem purchasingCapitalAssetItem =
                    getPurchasingCapitalAssetItemByItemIdentifier(item.getItemIdentifier());

            if (ObjectUtils.isNotNull(purchasingCapitalAssetItem)) {
                getPurchasingCapitalAssetItems().remove(purchasingCapitalAssetItem);
            }
            // no more capital asset items, clear cap asset fields
            if (getPurchasingCapitalAssetItems().size() == 0) {
                clearCapitalAssetFields();
            }
        }
        super.deleteItem(lineNum);
    }

    @Override
    public void populateDocumentForRouting() {
        commodityCodesForRouting = new ArrayList<>();
        List<PurchasingItemBase> previousPOItems = new ArrayList<PurchasingItemBase>();
        if (this instanceof PurchaseOrderAmendmentDocument) {
            previousPOItems = getPreviousPoItems();
        }
        for (final PurchasingItemBase item : (List<PurchasingItemBase>)this.getItems()) {
            // KFSPTS-1973
           if (item.getCommodityCode() != null && !commodityCodesForRouting.contains(item.getCommodityCode())) {
               if (this instanceof PurchaseOrderAmendmentDocument) {
                     if (includeItem(item, previousPOItems)) {
                         commodityCodesForRouting.add(item.getCommodityCode());
                     }
               } else {
                   commodityCodesForRouting.add(item.getCommodityCode());
               }
            }
        }
        super.populateDocumentForRouting();
    }

    // KFSPTS-1973 :  check POA item and see if it need to be included to commodity route validation
    
    /*
     * get all PO with this po#
     */
    private List<PurchaseOrderDocument> getPurchaseOrders() {
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put(PurapPropertyConstants.PURAP_DOC_ID, getPurapDocumentIdentifier());
        return (List<PurchaseOrderDocument>)SpringContext.getBean(BusinessObjectService.class).findMatching(PurchaseOrderDocument.class, fieldValues);
    
    }
    
    /*
     * loop thru the pos and find the one, which is 'final', precedes this one.
     */
    private List<PurchasingItemBase> getPreviousPoItems() {
        
        List<PurchaseOrderDocument> pos = getPurchaseOrders();
        int currDocNumber = Integer.parseInt(getDocumentNumber());
        int oldestNumber = 0;
        PurchaseOrderDocument selectedPo = (PurchaseOrderDocument)this;
        // try to find the po that is being amended by this po
        for (PurchaseOrderDocument po : pos) {
            if (Integer.parseInt(po.getDocumentNumber()) > oldestNumber && Integer.parseInt(po.getDocumentNumber()) < currDocNumber &&isDocumentFinal(po.getDocumentNumber())) {
                oldestNumber = Integer.parseInt(po.getDocumentNumber());
                selectedPo = po;
            }
        }
        return selectedPo.getItems();
    }
    
    /*
     * is document final
     */
    private boolean isDocumentFinal(String docNumber) {
        boolean isFinal = true;
        try {
            PurchaseOrderDocument poa = (PurchaseOrderDocument)SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(docNumber);
            if (poa != null && poa.getDocumentHeader().getWorkflowDocument() != null) {
                isFinal = poa.getDocumentHeader().getWorkflowDocument().isFinal();
            }
        } catch (Exception e) {
            isFinal = false;
        }

        return isFinal;
    }
    
    /*
     * validate if item needs to be included in 'commodity' codes check
     */
    private boolean includeItem(PurchasingItemBase item, List<PurchasingItemBase> previousPOItems) {
        boolean isNotChanged = true;
        boolean isNew = true;
        for (PurchasingItemBase prevItem : previousPOItems) {
            if (item.getItemLineNumber().equals(prevItem.getItemLineNumber())) {
                isNew = false;
                isNotChanged &= isItemNotChanged(item, prevItem);
            }
        }
        return isNew || !isNotChanged;
    }
    
    /*
     * check if item detail has been changed or is it a new item
     */
    private boolean isItemNotChanged(PurchasingItemBase item, PurchasingItemBase prevItem) {
        boolean isNotChanged = true;
        isNotChanged &= StringUtils.equals(item.getItemUnitOfMeasureCode(), prevItem.getItemUnitOfMeasureCode());
        if (item.getItemQuantity() != null && prevItem.getItemQuantity() != null) {
           isNotChanged &= item.getItemQuantity().equals(prevItem.getItemQuantity());
        }
        if (item.getItemUnitPrice() != null && prevItem.getItemUnitPrice() != null) {
            isNotChanged &= item.getItemUnitPrice().equals(prevItem.getItemUnitPrice());
        }
        isNotChanged &= StringUtils.equals(item.getPurchasingCommodityCode(), prevItem.getPurchasingCommodityCode());
        if (isNotChanged) {
            isNotChanged &= !isAccountingLineChanged(item,prevItem);
        }
        if (isNotChanged) {
            // need to do this because account might be deleted.
            isNotChanged &= !isAccountingLineChanged(prevItem,item);
        }
        return isNotChanged;
    }
    
    /*
     * check if the accounting line of the item has been changed
     */
    private boolean isAccountingLineChanged(PurchasingItemBase item, PurchasingItemBase prevItem) {
        boolean isChanged = false;
        for (PurApAccountingLine acctLine : item.getSourceAccountingLines()) {
            boolean acctLineFound = false;
            for (PurApAccountingLine prevAcctLine : prevItem.getSourceAccountingLines()) {
                boolean isMatched = true;
                isMatched &= StringUtils.equals(acctLine.getChartOfAccountsCode(), prevAcctLine.getChartOfAccountsCode());
                isMatched &= StringUtils.equals(acctLine.getAccountNumber(), prevAcctLine.getAccountNumber());
                isMatched &= StringUtils.equals(acctLine.getFinancialObjectCode(), prevAcctLine.getFinancialObjectCode());
                isMatched &= StringUtils.equals(acctLine.getFinancialSubObjectCode(), prevAcctLine.getFinancialSubObjectCode());
                isMatched &= StringUtils.equals(acctLine.getSubAccountNumber(), prevAcctLine.getSubAccountNumber());
                isMatched &= StringUtils.equals(acctLine.getOrganizationReferenceId(), prevAcctLine.getOrganizationReferenceId());
                isMatched &= StringUtils.equals(acctLine.getProjectCode(), prevAcctLine.getProjectCode());
                isMatched &= acctLine.getAccountLinePercent().equals(prevAcctLine.getAccountLinePercent());
                if (isMatched) {
                    acctLineFound = true;
                    break;
                }
            }
            if (!acctLineFound) {
                // no acct line matched, then it meant that something changed or this is a new line.  
                isChanged = true;
                break;
            }
        }
        return isChanged;
    }
 
    // end KFSPTS-1973

    // GETTERS AND SETTERS

    @Override
    public ItemParser getItemParser() {
        return new ItemParserBase();
    }

    /**
     * Decides whether receivingDocumentRequiredIndicator functionality shall be enabled according to the controlling
     * parameter.
     */
    public boolean isEnableReceivingDocumentRequiredIndicator() {
        return SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(
                KfsParameterConstants.PURCHASING_DOCUMENT.class,
                PurapParameterConstants.RECEIVING_REQUIRED_IND);
    }

    /**
     * Decides whether paymentRequestPositiveApprovalIndicator functionality shall be enabled according to the
     * controlling parameter.
     */
    public boolean isEnablePaymentRequestPositiveApprovalIndicator() {
        return SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(
                KfsParameterConstants.PURCHASING_DOCUMENT.class,
                PurapParameterConstants.PAYMENT_REQUEST_POSITIVE_APPROVAL_IND);
    }

    @Override
    public String getBillingCityName() {
        return billingCityName;
    }

    @Override
    public void setBillingCityName(final String billingCityName) {
        this.billingCityName = billingCityName;
    }

    @Override
    public String getBillingCountryCode() {
        return billingCountryCode;
    }

    @Override
    public void setBillingCountryCode(final String billingCountryCode) {
        this.billingCountryCode = billingCountryCode;
    }

    @Override
    public String getBillingCountryName() {
        if (StringUtils.isNotBlank(getBillingCountryCode())) {
            final Country country = SpringContext.getBean(LocationService.class, "locationService-fin")
                    .getCountry(getBillingCountryCode());
            if (country != null) {
                return country.getName();
            }
        }
        return null;
    }

    @Override
    public String getBillingLine1Address() {
        return billingLine1Address;
    }

    @Override
    public void setBillingLine1Address(final String billingLine1Address) {
        this.billingLine1Address = billingLine1Address;
    }

    @Override
    public String getBillingLine2Address() {
        return billingLine2Address;
    }

    @Override
    public void setBillingLine2Address(final String billingLine2Address) {
        this.billingLine2Address = billingLine2Address;
    }

    @Override
    public String getBillingName() {
        return billingName;
    }

    @Override
    public void setBillingName(final String billingName) {
        this.billingName = billingName;
    }

    @Override
    public String getBillingPhoneNumber() {
        return billingPhoneNumber;
    }

    @Override
    public void setBillingPhoneNumber(final String billingPhoneNumber) {
        this.billingPhoneNumber = billingPhoneNumber;
    }

    public String getBillingEmailAddress() {
        return billingEmailAddress;
    }

    public void setBillingEmailAddress(final String billingEmailAddress) {
        this.billingEmailAddress = billingEmailAddress;
    }

    @Override
    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    @Override
    public void setBillingPostalCode(final String billingPostalCode) {
        this.billingPostalCode = billingPostalCode;
    }

    @Override
    public String getBillingStateCode() {
        return billingStateCode;
    }

    @Override
    public void setBillingStateCode(final String billingStateCode) {
        this.billingStateCode = billingStateCode;
    }

    @Override
    public String getReceivingCityName() {
        return receivingCityName;
    }

    @Override
    public void setReceivingCityName(final String receivingCityName) {
        this.receivingCityName = receivingCityName;
    }

    @Override
    public String getReceivingCountryCode() {
        return receivingCountryCode;
    }

    @Override
    public void setReceivingCountryCode(final String receivingCountryCode) {
        this.receivingCountryCode = receivingCountryCode;
    }

    @Override
    public String getReceivingCountryName() {
        if (StringUtils.isNotBlank(getReceivingCountryCode())) {
            final Country country = SpringContext.getBean(LocationService.class, "locationService-fin")
                    .getCountry(getReceivingCountryCode());
            if (country != null) {
                return country.getName();
            }
        }
        return null;
    }

    @Override
    public String getReceivingLine1Address() {
        return receivingLine1Address;
    }

    @Override
    public void setReceivingLine1Address(final String receivingLine1Address) {
        this.receivingLine1Address = receivingLine1Address;
    }

    @Override
    public String getReceivingLine2Address() {
        return receivingLine2Address;
    }

    @Override
    public void setReceivingLine2Address(final String receivingLine2Address) {
        this.receivingLine2Address = receivingLine2Address;
    }

    @Override
    public String getReceivingName() {
        return receivingName;
    }

    @Override
    public void setReceivingName(final String receivingName) {
        this.receivingName = receivingName;
    }

    @Override
    public String getReceivingPostalCode() {
        return receivingPostalCode;
    }

    @Override
    public void setReceivingPostalCode(final String receivingPostalCode) {
        this.receivingPostalCode = receivingPostalCode;
    }

    @Override
    public String getReceivingStateCode() {
        return receivingStateCode;
    }

    @Override
    public void setReceivingStateCode(final String receivingStateCode) {
        this.receivingStateCode = receivingStateCode;
    }

    @Override
    public boolean getAddressToVendorIndicator() {
        return addressToVendorIndicator;
    }

    @Override
    public void setAddressToVendorIndicator(final boolean addressToVendor) {
        addressToVendorIndicator = addressToVendor;
    }

    @Override
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    @Override
    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    @Override
    public String getDeliveryBuildingCode() {
        return deliveryBuildingCode;
    }

    @Override
    public void setDeliveryBuildingCode(final String deliveryBuildingCode) {
        this.deliveryBuildingCode = deliveryBuildingCode != null ? deliveryBuildingCode.toUpperCase(Locale.US) : null;
    }

    @Override
    public String getDeliveryBuildingLine1Address() {
        return deliveryBuildingLine1Address;
    }

    @Override
    public void setDeliveryBuildingLine1Address(final String deliveryBuildingLine1Address) {
        this.deliveryBuildingLine1Address = deliveryBuildingLine1Address;
    }

    @Override
    public String getDeliveryBuildingLine2Address() {
        return deliveryBuildingLine2Address;
    }

    @Override
    public void setDeliveryBuildingLine2Address(final String deliveryBuildingLine2Address) {
        this.deliveryBuildingLine2Address = deliveryBuildingLine2Address;
    }

    @Override
    public String getDeliveryBuildingName() {
        return deliveryBuildingName;
    }

    @Override
    public void setDeliveryBuildingName(final String deliveryBuildingName) {
        this.deliveryBuildingName = deliveryBuildingName;
    }

    @Override
    public boolean isDeliveryBuildingOtherIndicator() {
        return deliveryBuildingOtherIndicator;
    }

    @Override
    public void setDeliveryBuildingOtherIndicator(final boolean deliveryBuildingOtherIndicator) {
        this.deliveryBuildingOtherIndicator = deliveryBuildingOtherIndicator;
    }

    @Override
    public String getDeliveryBuildingRoomNumber() {
        return deliveryBuildingRoomNumber;
    }

    @Override
    public void setDeliveryBuildingRoomNumber(final String deliveryBuildingRoomNumber) {
        this.deliveryBuildingRoomNumber = deliveryBuildingRoomNumber;
    }

    @Override
    public String getDeliveryCampusCode() {
        return deliveryCampusCode;
    }

    @Override
    public void setDeliveryCampusCode(final String deliveryCampusCode) {
        this.deliveryCampusCode = deliveryCampusCode;
    }

    @Override
    public String getDeliveryCityName() {
        return deliveryCityName;
    }

    @Override
    public void setDeliveryCityName(final String deliveryCityName) {
        this.deliveryCityName = deliveryCityName;
    }

    @Override
    public String getDeliveryCountryCode() {
        return deliveryCountryCode;
    }

    @Override
    public String getDeliveryCountryName() {
        if (StringUtils.isNotBlank(getDeliveryCountryCode())) {
            final Country country = SpringContext.getBean(LocationService.class, "locationService-fin")
                    .getCountry(getDeliveryCountryCode());
            if (country != null) {
                return country.getName();
            }
        }
        return null;
    }

    @Override
    public void setDeliveryCountryCode(final String deliveryCountryCode) {
        this.deliveryCountryCode = deliveryCountryCode;
    }

    @Override
    public String getDeliveryInstructionText() {
        return deliveryInstructionText;
    }

    @Override
    public void setDeliveryInstructionText(final String deliveryInstructionText) {
        this.deliveryInstructionText = deliveryInstructionText;
    }

    @Override
    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }

    @Override
    public void setDeliveryPostalCode(final String deliveryPostalCode) {
        this.deliveryPostalCode = deliveryPostalCode;
    }

    @Override
    public Date getDeliveryRequiredDate() {
        return deliveryRequiredDate;
    }

    @Override
    public void setDeliveryRequiredDate(final Date deliveryRequiredDate) {
        this.deliveryRequiredDate = deliveryRequiredDate;
    }

    @Override
    public String getDeliveryRequiredDateReasonCode() {
        return deliveryRequiredDateReasonCode;
    }

    @Override
    public void setDeliveryRequiredDateReasonCode(final String deliveryRequiredDateReasonCode) {
        this.deliveryRequiredDateReasonCode = deliveryRequiredDateReasonCode;
    }

    @Override
    public String getDeliveryStateCode() {
        return deliveryStateCode;
    }

    @Override
    public void setDeliveryStateCode(final String deliveryStateCode) {
        this.deliveryStateCode = deliveryStateCode;
    }

    @Override
    public String getDeliveryToEmailAddress() {
        return deliveryToEmailAddress;
    }

    @Override
    public void setDeliveryToEmailAddress(final String deliveryToEmailAddress) {
        this.deliveryToEmailAddress = deliveryToEmailAddress;
    }

    @Override
    public String getDeliveryToName() {
        return deliveryToName;
    }

    @Override
    public void setDeliveryToName(final String deliveryToName) {
        this.deliveryToName = deliveryToName;
    }

    @Override
    public String getDeliveryToPhoneNumber() {
        return deliveryToPhoneNumber;
    }

    @Override
    public void setDeliveryToPhoneNumber(final String deliveryToPhoneNumber) {
        this.deliveryToPhoneNumber = deliveryToPhoneNumber;
    }

    @Override
    public String getExternalOrganizationB2bSupplierIdentifier() {
        return externalOrganizationB2bSupplierIdentifier;
    }

    @Override
    public void setExternalOrganizationB2bSupplierIdentifier(final String externalOrganizationB2bSupplierIdentifier) {
        this.externalOrganizationB2bSupplierIdentifier = externalOrganizationB2bSupplierIdentifier;
    }

    @Override
    public String getInstitutionContactEmailAddress() {
        return institutionContactEmailAddress;
    }

    @Override
    public void setInstitutionContactEmailAddress(final String institutionContactEmailAddress) {
        this.institutionContactEmailAddress = institutionContactEmailAddress;
    }

    @Override
    public String getInstitutionContactName() {
        return institutionContactName;
    }

    @Override
    public void setInstitutionContactName(final String institutionContactName) {
        this.institutionContactName = institutionContactName;
    }

    @Override
    public String getInstitutionContactPhoneNumber() {
        return institutionContactPhoneNumber;
    }

    @Override
    public void setInstitutionContactPhoneNumber(final String institutionContactPhoneNumber) {
        this.institutionContactPhoneNumber = institutionContactPhoneNumber;
    }

    @Override
    public String getNonInstitutionFundAccountNumber() {
        return nonInstitutionFundAccountNumber;
    }

    @Override
    public void setNonInstitutionFundAccountNumber(final String nonInstitutionFundAccountNumber) {
        this.nonInstitutionFundAccountNumber = nonInstitutionFundAccountNumber;
    }

    @Override
    public String getNonInstitutionFundChartOfAccountsCode() {
        return nonInstitutionFundChartOfAccountsCode;
    }

    @Override
    public void setNonInstitutionFundChartOfAccountsCode(final String nonInstitutionFundChartOfAccountsCode) {
        this.nonInstitutionFundChartOfAccountsCode = nonInstitutionFundChartOfAccountsCode;
    }

    @Override
    public String getNonInstitutionFundOrganizationCode() {
        return nonInstitutionFundOrganizationCode;
    }

    @Override
    public void setNonInstitutionFundOrganizationCode(final String nonInstitutionFundOrganizationCode) {
        this.nonInstitutionFundOrganizationCode = nonInstitutionFundOrganizationCode;
    }

    @Override
    public String getNonInstitutionFundOrgChartOfAccountsCode() {
        return nonInstitutionFundOrgChartOfAccountsCode;
    }

    @Override
    public void setNonInstitutionFundOrgChartOfAccountsCode(final String nonInstitutionFundOrgChartOfAccountsCode) {
        this.nonInstitutionFundOrgChartOfAccountsCode = nonInstitutionFundOrgChartOfAccountsCode;
    }

    @Override
    public String getOrganizationCode() {
        return organizationCode;
    }

    @Override
    public void setOrganizationCode(final String organizationCode) {
        this.organizationCode = organizationCode;
    }

    @Override
    public boolean getPurchaseOrderAutomaticIndicator() {
        return purchaseOrderAutomaticIndicator;
    }

    @Override
    public void setPurchaseOrderAutomaticIndicator(final boolean purchaseOrderAutomaticIndicator) {
        this.purchaseOrderAutomaticIndicator = purchaseOrderAutomaticIndicator;
    }

    @Override
    public Date getPurchaseOrderBeginDate() {
        return purchaseOrderBeginDate;
    }

    @Override
    public void setPurchaseOrderBeginDate(final Date purchaseOrderBeginDate) {
        this.purchaseOrderBeginDate = purchaseOrderBeginDate;
    }

    @Override
    public String getPurchaseOrderCostSourceCode() {
        return purchaseOrderCostSourceCode;
    }

    @Override
    public void setPurchaseOrderCostSourceCode(final String purchaseOrderCostSourceCode) {
        this.purchaseOrderCostSourceCode = purchaseOrderCostSourceCode;
    }

    @Override
    public Date getPurchaseOrderEndDate() {
        return purchaseOrderEndDate;
    }

    @Override
    public void setPurchaseOrderEndDate(final Date purchaseOrderEndDate) {
        this.purchaseOrderEndDate = purchaseOrderEndDate;
    }

    @Override
    public KualiDecimal getPurchaseOrderTotalLimit() {
        return purchaseOrderTotalLimit;
    }

    @Override
    public void setPurchaseOrderTotalLimit(final KualiDecimal purchaseOrderTotalLimit) {
        this.purchaseOrderTotalLimit = purchaseOrderTotalLimit;
    }

    @Override
    public String getPurchaseOrderTransmissionMethodCode() {
        return purchaseOrderTransmissionMethodCode;
    }

    @Override
    public void setPurchaseOrderTransmissionMethodCode(final String purchaseOrderTransmissionMethodCode) {
        this.purchaseOrderTransmissionMethodCode = purchaseOrderTransmissionMethodCode;
    }

    @Override
    public String getRecurringPaymentTypeCode() {
        return recurringPaymentTypeCode;
    }

    @Override
    public void setRecurringPaymentTypeCode(final String recurringPaymentTypeCode) {
        this.recurringPaymentTypeCode = recurringPaymentTypeCode;
    }

    @Override
    public String getRequestorPersonEmailAddress() {
        return requestorPersonEmailAddress;
    }

    @Override
    public void setRequestorPersonEmailAddress(final String requestorPersonEmailAddress) {
        this.requestorPersonEmailAddress = requestorPersonEmailAddress;
    }

    @Override
    public String getRequestorPersonName() {
        return requestorPersonName;
    }

    @Override
    public void setRequestorPersonName(final String requestorPersonName) {
        this.requestorPersonName = requestorPersonName;
    }

    @Override
    public String getRequestorPersonPhoneNumber() {
        return requestorPersonPhoneNumber;
    }

    @Override
    public void setRequestorPersonPhoneNumber(final String requestorPersonPhoneNumber) {
        this.requestorPersonPhoneNumber = requestorPersonPhoneNumber;
    }

    @Override
    public String getRequisitionSourceCode() {
        return requisitionSourceCode;
    }

    @Override
    public void setRequisitionSourceCode(final String requisitionSourceCode) {
        this.requisitionSourceCode = requisitionSourceCode;
    }

    public String getVendorContactsLabel() {
        return vendorContactsLabel;
    }

    public void setVendorContactsLabel(final String vendorContactsLabel) {
        this.vendorContactsLabel = vendorContactsLabel;
    }

    public VendorContract getVendorContract() {
        if (ObjectUtils.isNull(vendorContract)) {
            refreshReferenceObject(PurapPropertyConstants.VENDOR_CONTRACT);
        }
        return vendorContract;
    }

    public void setVendorContract(final VendorContract vendorContract) {
        this.vendorContract = vendorContract;
    }

    @Override
    public Integer getVendorContractGeneratedIdentifier() {
        return vendorContractGeneratedIdentifier;
    }

    @Override
    public void setVendorContractGeneratedIdentifier(final Integer vendorContractGeneratedIdentifier) {
        this.vendorContractGeneratedIdentifier = vendorContractGeneratedIdentifier;
    }

    public String getVendorContractName() {
        getVendorContract();
        if (ObjectUtils.isNull(vendorContract)) {
            return "";
        } else {
            return vendorContract.getVendorContractName();
        }
    }

    @Override
    public String getVendorFaxNumber() {
        return vendorFaxNumber;
    }

    @Override
    public void setVendorFaxNumber(final String vendorFaxNumber) {
        this.vendorFaxNumber = vendorFaxNumber;
    }

    @Override
    public String getVendorNoteText() {
        return vendorNoteText;
    }

    @Override
    public void setVendorNoteText(final String vendorNoteText) {
        this.vendorNoteText = vendorNoteText;
    }

    @Override
    public String getVendorPaymentTermsCode() {
        return vendorPaymentTermsCode;
    }

    @Override
    public void setVendorPaymentTermsCode(final String vendorPaymentTermsCode) {
        this.vendorPaymentTermsCode = vendorPaymentTermsCode;
    }

    @Override
    public String getVendorPhoneNumber() {
        return vendorPhoneNumber;
    }

    @Override
    public void setVendorPhoneNumber(final String vendorPhoneNumber) {
        this.vendorPhoneNumber = vendorPhoneNumber;
    }

    @Override
    public Boolean getVendorRestrictedIndicator() {
        return vendorRestrictedIndicator;
    }

    @Override
    public void setVendorRestrictedIndicator(final Boolean vendorRestrictedIndicator) {
        this.vendorRestrictedIndicator = vendorRestrictedIndicator;
    }

    @Override
    public String getVendorShippingPaymentTermsCode() {
        return vendorShippingPaymentTermsCode;
    }

    @Override
    public void setVendorShippingPaymentTermsCode(final String vendorShippingPaymentTermsCode) {
        this.vendorShippingPaymentTermsCode = vendorShippingPaymentTermsCode;
    }

    @Override
    public String getVendorShippingTitleCode() {
        return vendorShippingTitleCode;
    }

    @Override
    public void setVendorShippingTitleCode(final String vendorShippingTitleCode) {
        this.vendorShippingTitleCode = vendorShippingTitleCode;
    }

    @Override
    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    @Override
    public CampusParameter getDeliveryCampus() {
        return deliveryCampus;
    }

    @Override
    public DeliveryRequiredDateReason getDeliveryRequiredDateReason() {
        return deliveryRequiredDateReason;
    }

    @Override
    public Account getNonInstitutionFundAccount() {
        return nonInstitutionFundAccount;
    }

    @Override
    public Chart getNonInstitutionFundChartOfAccounts() {
        return nonInstitutionFundChartOfAccounts;
    }

    @Override
    public Organization getNonInstitutionFundOrganization() {
        return nonInstitutionFundOrganization;
    }

    @Override
    public Chart getNonInstitutionFundOrgChartOfAccounts() {
        return nonInstitutionFundOrgChartOfAccounts;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public PurchaseOrderTransmissionMethod getPurchaseOrderTransmissionMethod() {
        return purchaseOrderTransmissionMethod;
    }

    @Override
    public RecurringPaymentType getRecurringPaymentType() {
        return recurringPaymentType;
    }

    @Override
    public RequisitionSource getRequisitionSource() {
        return requisitionSource;
    }

    public String getSupplierDiversityLabel() {
        return supplierDiversityLabel;
    }

    @Override
    public PurchaseOrderCostSource getPurchaseOrderCostSource() {
        if (ObjectUtils.isNull(purchaseOrderCostSource)) {
            refreshReferenceObject(PurapPropertyConstants.PURCHASE_ORDER_COST_SOURCE);
        }
        return purchaseOrderCostSource;
    }

    @Deprecated
    @Override
    public void setChartOfAccounts(final Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    @Deprecated
    @Override
    public void setDeliveryCampus(final CampusParameter deliveryCampus) {
        this.deliveryCampus = deliveryCampus;
    }

    @Deprecated
    @Override
    public void setDeliveryRequiredDateReason(final DeliveryRequiredDateReason deliveryRequiredDateReason) {
        this.deliveryRequiredDateReason = deliveryRequiredDateReason;
    }

    @Deprecated
    @Override
    public void setNonInstitutionFundAccount(final Account nonInstitutionFundAccount) {
        this.nonInstitutionFundAccount = nonInstitutionFundAccount;
    }

    @Deprecated
    @Override
    public void setNonInstitutionFundChartOfAccounts(final Chart nonInstitutionFundChartOfAccounts) {
        this.nonInstitutionFundChartOfAccounts = nonInstitutionFundChartOfAccounts;
    }

    @Deprecated
    @Override
    public void setNonInstitutionFundOrganization(final Organization nonInstitutionFundOrganization) {
        this.nonInstitutionFundOrganization = nonInstitutionFundOrganization;
    }

    @Deprecated
    @Override
    public void setNonInstitutionFundOrgChartOfAccounts(final Chart nonInstitutionFundOrgChartOfAccounts) {
        this.nonInstitutionFundOrgChartOfAccounts = nonInstitutionFundOrgChartOfAccounts;
    }

    @Deprecated
    @Override
    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    @Deprecated
    @Override
    public void setPurchaseOrderCostSource(final PurchaseOrderCostSource purchaseOrderCostSource) {
        this.purchaseOrderCostSource = purchaseOrderCostSource;
    }

    @Deprecated
    @Override
    public void setPurchaseOrderTransmissionMethod(final PurchaseOrderTransmissionMethod purchaseOrderTransmissionMethod) {
        this.purchaseOrderTransmissionMethod = purchaseOrderTransmissionMethod;
    }

    @Deprecated
    @Override
    public void setRecurringPaymentType(final RecurringPaymentType recurringPaymentType) {
        this.recurringPaymentType = recurringPaymentType;
    }

    @Deprecated
    @Override
    public void setRequisitionSource(final RequisitionSource requisitionSource) {
        this.requisitionSource = requisitionSource;
    }

    @Override
    public boolean isReceivingDocumentRequiredIndicator() {
        return receivingDocumentRequiredIndicator;
    }

    @Override
    public void setReceivingDocumentRequiredIndicator(final boolean receivingDocumentRequiredIndicator) {
        // if receivingDocumentRequiredIndicator functionality is disabled, always set it to false, overriding the
        // passed-in value
        if (!isEnableReceivingDocumentRequiredIndicator()) {
            this.receivingDocumentRequiredIndicator = false;
        } else {
            this.receivingDocumentRequiredIndicator = receivingDocumentRequiredIndicator;
        }
    }

    @Override
    public boolean isPaymentRequestPositiveApprovalIndicator() {
        return paymentRequestPositiveApprovalIndicator;
    }

    @Override
    public void setPaymentRequestPositiveApprovalIndicator(final boolean paymentRequestPositiveApprovalIndicator) {
        // if paymentRequestPositiveApprovalIndicator functionality is disabled, always set it to false, overriding the
        // passed-in value
        if (!isEnablePaymentRequestPositiveApprovalIndicator()) {
            this.paymentRequestPositiveApprovalIndicator = false;
        } else {
            this.paymentRequestPositiveApprovalIndicator = paymentRequestPositiveApprovalIndicator;
        }
    }

    public List<CommodityCode> getCommodityCodesForRouting() {
        return commodityCodesForRouting;
    }

    public void setCommodityCodesForRouting(final List<CommodityCode> commodityCodesForRouting) {
        this.commodityCodesForRouting = commodityCodesForRouting;
    }

    @Override
    public String getCapitalAssetSystemTypeCode() {
        return capitalAssetSystemTypeCode;
    }

    @Override
    public void setCapitalAssetSystemTypeCode(final String capitalAssetSystemTypeCode) {
        this.capitalAssetSystemTypeCode = capitalAssetSystemTypeCode;
    }

    @Override
    public String getCapitalAssetSystemStateCode() {
        return capitalAssetSystemStateCode;
    }

    @Override
    public void setCapitalAssetSystemStateCode(final String capitalAssetSystemStateCode) {
        this.capitalAssetSystemStateCode = capitalAssetSystemStateCode;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    @Override
    public CapitalAssetSystemType getCapitalAssetSystemType() {
        if (ObjectUtils.isNull(capitalAssetSystemType)) {
            refreshReferenceObject(PurapPropertyConstants.CAPITAL_ASSET_SYSTEM_TYPE);
        }
        return capitalAssetSystemType;
    }

    @Override
    public void setCapitalAssetSystemType(final CapitalAssetSystemType capitalAssetSystemType) {
        this.capitalAssetSystemType = capitalAssetSystemType;
    }

    @Override
    public CapitalAssetSystemState getCapitalAssetSystemState() {
        if (ObjectUtils.isNull(capitalAssetSystemState)) {
            refreshReferenceObject(PurapPropertyConstants.CAPITAL_ASSET_SYSTEM_STATE);
        }
        return capitalAssetSystemState;
    }

    @Override
    public void setCapitalAssetSystemState(final CapitalAssetSystemState capitalAssetSystemState) {
        this.capitalAssetSystemState = capitalAssetSystemState;
    }

    @Override
    public List<CapitalAssetSystem> getPurchasingCapitalAssetSystems() {
        return purchasingCapitalAssetSystems;
    }

    @Override
    public void setPurchasingCapitalAssetSystems(final List<CapitalAssetSystem> purchasingCapitalAssetSystems) {
        this.purchasingCapitalAssetSystems = purchasingCapitalAssetSystems;
    }

    @Override
    public List<PurchasingCapitalAssetItem> getPurchasingCapitalAssetItems() {
        return purchasingCapitalAssetItems;
    }

    @Override
    public void setPurchasingCapitalAssetItems(final List<PurchasingCapitalAssetItem> purchasingCapitalAssetItems) {
        this.purchasingCapitalAssetItems = purchasingCapitalAssetItems;
    }

    @Override
    public abstract Class getPurchasingCapitalAssetItemClass();

    @Override
    public abstract Class getPurchasingCapitalAssetSystemClass();

    @Override
    public PurchasingItem getPurchasingItem(final Integer itemIdentifier) {

        if (ObjectUtils.isNull(itemIdentifier)) {
            return null;
        }

        PurchasingItem item = null;

        for (final PurchasingItem pi : (List<PurchasingItem>) getItems()) {
            if (itemIdentifier.equals(pi.getItemIdentifier())) {
                item = pi;
                break;
            }
        }

        return item;
    }

    @Override
    public PurchasingCapitalAssetItem getPurchasingCapitalAssetItem(final Integer itemIdentifier) {
        if (ObjectUtils.isNull(itemIdentifier)) {
            return null;
        }

        PurchasingCapitalAssetItem item = null;

        for (final PurchasingCapitalAssetItem pcai : getPurchasingCapitalAssetItems()) {
            if (itemIdentifier.equals(pcai.getItemIdentifier())) {
                item = pcai;
                break;
            }
        }

        return item;
    }

    @Override
    public List buildListOfDeletionAwareLists() {
        final List managedLists = new ArrayList<List>();
        managedLists.add(getDeletionAwareAccountingLines());
        managedLists.add(getDeletionAwareUseTaxItems());
        if (allowDeleteAwareCollection) {
            managedLists.add(getPurchasingCapitalAssetSystems());
            managedLists.add(getPurchasingCapitalAssetItems());
            managedLists.add(getItems());
        }
        return managedLists;
    }

    /**
     * Overrides the method in PurchasingAccountsPayableDocumentBase to remove the
     * purchasingCapitalAssetSystem when the system type is either ONE or MULT.
     */
    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
        super.prepareForSave(event);
        if (StringUtils.isNotBlank(getCapitalAssetSystemTypeCode())) {
            if (getCapitalAssetSystemTypeCode().equals(PurapConstants.CapitalAssetSystemTypes.ONE_SYSTEM)
                || getCapitalAssetSystemTypeCode().equals(PurapConstants.CapitalAssetSystemTypes.MULTIPLE)) {
                // If the system state is ONE or MULT, we have to remove all the systems on the items because it's not
                // applicable.
                for (final PurchasingCapitalAssetItem camsItem : getPurchasingCapitalAssetItems()) {
                    camsItem.setPurchasingCapitalAssetSystem(null);
                }
            }
        }
        if (event instanceof RouteDocumentEvent || event instanceof ApproveDocumentEvent) {

            final boolean defaultUseTaxIndicatorValue = SpringContext.getBean(PurchasingService.class)
                    .getDefaultUseTaxIndicatorValue(this);
            SpringContext.getBean(PurapService.class).updateUseTaxIndicator(this,
                    defaultUseTaxIndicatorValue);
        }
    }

    @Override
    public Date getTransactionTaxDate() {
        return SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
    }

    @Override
    public void clearCapitalAssetFields() {
        getPurchasingCapitalAssetItems().clear();
        getPurchasingCapitalAssetSystems().clear();
        setCapitalAssetSystemStateCode(null);
        setCapitalAssetSystemTypeCode(null);
        setCapitalAssetSystemState(null);
        setCapitalAssetSystemType(null);
    }

    public boolean getPaymentRequestPositiveApprovalIndicatorForSearching() {
        return paymentRequestPositiveApprovalIndicator;
    }

    public boolean getReceivingDocumentRequiredIndicatorForSearching() {
        return receivingDocumentRequiredIndicator;
    }

    public String getDocumentChartOfAccountsCodeForSearching() {
        return chartOfAccountsCode;
    }

    public String getDocumentOrganizationCodeForSearching() {
        return organizationCode;
    }

    @Override
    public boolean shouldGiveErrorForEmptyAccountsProration() {
        return true;
    }

    public String getChartAndOrgCodeForResult() {
        return getChartOfAccountsCode() + "-" + getOrganizationCode();
    }

    public String getDeliveryCampusCodeForSearch() {
        return getDeliveryCampusCode();
    }

    public boolean getHasB2BVendor() {
        if (getVendorHeaderGeneratedIdentifier() != null) {
            refreshReferenceObject(VendorPropertyConstants.VENDOR_DETAIL);
            final String campusCode = GlobalVariables.getUserSession().getPerson().getCampusCode();
            final VendorDetail vendorDetail = getVendorDetail();
            if (vendorDetail == null || StringUtils.isEmpty(campusCode)) {
                // this should never happen
                return false;
            }
            return SpringContext.getBean(VendorService.class).getVendorB2BContract(vendorDetail, campusCode) != null;
        }
        return false;
    }
    
    /*
     * KFSPTS-985
     */
     public Integer getFavoriteAccountLineIdentifier() {
        return favoriteAccountLineIdentifier;
    }

    public void setFavoriteAccountLineIdentifier(
            Integer favoriteAccountLineIdentifier) {
        this.favoriteAccountLineIdentifier = favoriteAccountLineIdentifier;
    }    
     
    // KFSPTS-985, KFSUPGRADE-75
    public boolean isIntegratedWithFavoriteAccount() {
        return true;
    }


}
