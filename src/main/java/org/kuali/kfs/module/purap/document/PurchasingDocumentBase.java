/*
 * Copyright 2006 The Kuali Foundation
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
package org.kuali.kfs.module.purap.document;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.integration.purap.CapitalAssetLocation;
import org.kuali.kfs.integration.purap.CapitalAssetSystem;
import org.kuali.kfs.integration.purap.ItemCapitalAsset;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurapFundingSources;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.BillingAddress;
import org.kuali.kfs.module.purap.businessobject.CapitalAssetSystemState;
import org.kuali.kfs.module.purap.businessobject.CapitalAssetSystemType;
import org.kuali.kfs.module.purap.businessobject.DeliveryRequiredDateReason;
import org.kuali.kfs.module.purap.businessobject.FundingSource;
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
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.CampusParameter;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.businessobject.PurchaseOrderCostSource;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kns.bo.Country;
import org.kuali.rice.kns.rule.event.ApproveDocumentEvent;
import org.kuali.rice.kns.rule.event.KualiDocumentEvent;
import org.kuali.rice.kns.rule.event.RouteDocumentEvent;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.CountryService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.util.TypedArrayList;

/**
 * Base class for Purchasing Documents.
 */
public abstract class PurchasingDocumentBase extends PurchasingAccountsPayableDocumentBase implements PurchasingDocument {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PurchasingDocumentBase.class);

    // SHARED FIELDS BETWEEN REQUISITION AND PURCHASE ORDER
    protected String documentFundingSourceCode;
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
//KFSPTS-1639    protected String vendorEmailAddress;					//KFSPTS-1458
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
    protected String receivingName;
    protected String receivingLine1Address;
    protected String receivingLine2Address;
    protected String receivingCityName;
    protected String receivingStateCode;
    protected String receivingPostalCode;
    protected String receivingCountryCode;
    protected boolean addressToVendorIndicator; // if true, use receiving address
    protected String externalOrganizationB2bSupplierIdentifier;
    protected boolean purchaseOrderAutomaticIndicator;
    protected String vendorPaymentTermsCode;
    protected String vendorShippingTitleCode;
    protected String vendorShippingPaymentTermsCode;
    protected String capitalAssetSystemTypeCode;
    protected String capitalAssetSystemStateCode;

    // NOT PERSISTED IN DB
    protected String supplierDiversityLabel;
    protected String vendorContactsLabel;

    // REFERENCE OBJECTS
    protected FundingSource fundingSource;
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
   
    /**
     * Default Constructor.
     */
    public PurchasingDocumentBase() {
        super();
        
        purchasingCapitalAssetItems = new TypedArrayList(getPurchasingCapitalAssetItemClass());
        purchasingCapitalAssetSystems = new TypedArrayList(getPurchasingCapitalAssetSystemClass());
    }

    public abstract PurchasingDocumentSpecificService getDocumentSpecificService();

    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingDocument#templateVendorDetail(org.kuali.kfs.vnd.businessobject.VendorDetail)
     */
    public void templateVendorDetail(VendorDetail vendorDetail) {
        if (ObjectUtils.isNotNull(vendorDetail)) {
            this.setVendorDetail(vendorDetail);
            this.setVendorName(vendorDetail.getVendorName());
            this.setVendorShippingTitleCode(vendorDetail.getVendorShippingTitleCode());
            this.setVendorPaymentTermsCode(vendorDetail.getVendorPaymentTermsCode());
            this.setVendorShippingPaymentTermsCode(vendorDetail.getVendorShippingPaymentTermsCode());
            this.setVendorCustomerNumber("");
        }
    } 
    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingDocument#templateVendorContract(org.kuali.kfs.vnd.businessobject.VendorContract)
     */
    public void templateVendorContract(VendorContract vendorContract) {
        if (ObjectUtils.isNotNull(vendorContract)) {
            this.setVendorContract(vendorContract);
            this.setVendorContractGeneratedIdentifier(vendorContract.getVendorContractGeneratedIdentifier());
            this.setVendorShippingTitleCode(vendorContract.getVendorShippingTitleCode());
            this.setVendorPaymentTermsCode(vendorContract.getVendorPaymentTermsCode());
            this.setVendorShippingPaymentTermsCode(vendorContract.getVendorShippingPaymentTermsCode());
            this.setPurchaseOrderCostSourceCode(vendorContract.getPurchaseOrderCostSourceCode());
        }
    }

    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#templateVendorAddress(org.kuali.kfs.vnd.businessobject.VendorAddress)
     */
    public void templateVendorAddress(VendorAddress vendorAddress) {
        super.templateVendorAddress(vendorAddress);
        if (vendorAddress != null) {
            this.setVendorFaxNumber(vendorAddress.getVendorFaxNumber());
            this.setVendorAttentionName(vendorAddress.getVendorAttentionName());
                     
            //need to save vendorAddressGeneratedIdentifier for Method of PO Transmission mod, value is null in business object when it is needed
            this.setVendorAddressGeneratedIdentifier(vendorAddress.getVendorAddressGeneratedIdentifier());            
            //Method of PO Transmission on Vendor Address should be the default when a vendor is selected.
            //set purchasing document value for po transmission method
            this.setPurchaseOrderTransmissionMethodCode(vendorAddress.getPurchaseOrderTransmissionMethodCode());

//KFSPTS-1639            
//            //KFSPTS-1458
//            this.setVendorEmailAddress(vendorAddress.getVendorAddressEmailAddress());
        }
    }

    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingDocumentBase#templateBillingAddress(org.kuali.kfs.module.purap.businessobject.BillingAddress).
     */
    public void templateBillingAddress(BillingAddress billingAddress) {
        if (ObjectUtils.isNotNull(billingAddress)) {
            this.setBillingName(billingAddress.getBillingName());
            this.setBillingLine1Address(billingAddress.getBillingLine1Address());
            this.setBillingLine2Address(billingAddress.getBillingLine2Address());
            this.setBillingCityName(billingAddress.getBillingCityName());
            this.setBillingStateCode(billingAddress.getBillingStateCode());
            this.setBillingPostalCode(billingAddress.getBillingPostalCode());
            this.setBillingCountryCode(billingAddress.getBillingCountryCode());
            this.setBillingPhoneNumber(billingAddress.getBillingPhoneNumber());
        }
    }

    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingDocumentBase#templateReceivingAddress(org.kuali.kfs.module.purap.businessobject.ReceivingAddress).
     */
    public void templateReceivingAddress(ReceivingAddress receivingAddress) {
        if (receivingAddress != null) {
            this.setReceivingName(receivingAddress.getReceivingName());
            this.setReceivingLine1Address(receivingAddress.getReceivingLine1Address());
            this.setReceivingLine2Address(receivingAddress.getReceivingLine2Address());
            this.setReceivingCityName(receivingAddress.getReceivingCityName());
            this.setReceivingStateCode(receivingAddress.getReceivingStateCode());
            this.setReceivingPostalCode(receivingAddress.getReceivingPostalCode());
            this.setReceivingCountryCode(receivingAddress.getReceivingCountryCode());            
            this.setAddressToVendorIndicator(receivingAddress.isUseReceivingIndicator());
        }
        else {
            this.setReceivingName(null);
            this.setReceivingLine1Address(null);
            this.setReceivingLine2Address(null);
            this.setReceivingCityName(null);
            this.setReceivingStateCode(null);
            this.setReceivingPostalCode(null);
            this.setReceivingCountryCode(null);            
            this.setAddressToVendorIndicator(false);            
        }
    }

    /**
     * Loads the default receiving address from database corresponding to the chart/org of this document.
     */
    public void loadReceivingAddress() {
        /*
        Map criteria = new HashMap();
        criteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, this.getChartOfAccountsCode());
        criteria.put(KFSPropertyConstants.ORGANIZATION_CODE, this.getOrganizationCode());
        criteria.put(PurapPropertyConstants.RCVNG_ADDR_DFLT_IND, true);        
        criteria.put(PurapPropertyConstants.RCVNG_ADDR_ACTIVE, true);        
        List<ReceivingAddress> addresses = (List)SpringContext.getBean(BusinessObjectService.class).findMatching(ReceivingAddress.class, criteria);
        if (addresses != null && addresses.size()>0 ) 
            this.templateReceivingAddress(addresses.get(0));        
        else // if no address is found, fill with null
            this.templateReceivingAddress(null);
        */
        String chartCode = getChartOfAccountsCode();
        String orgCode = getOrganizationCode();
        ReceivingAddress address = SpringContext.getBean(ReceivingAddressService.class).findUniqueDefaultByChartOrg(chartCode, orgCode);        
        // if default address for chart/org not found, look for chart default 
        if (address == null && orgCode != null) {
            address = SpringContext.getBean(ReceivingAddressService.class).findUniqueDefaultByChartOrg(chartCode, null);
        }
        this.templateReceivingAddress(address);
    }
    
    /**
     * Iterates through the purchasingCapitalAssetItems of the document and returns the purchasingCapitalAssetItem with the item id equal to the number given, or null if a
     * match is not found.
     * 
     * @param itemIdentifier item id to match on.
     * @return the PurchasingCapitalAssetItem if a match is found, else null.
     */
    public PurchasingCapitalAssetItem getPurchasingCapitalAssetItemByItemIdentifier(int itemIdentifier) {
        for (Iterator iter = purchasingCapitalAssetItems.iterator(); iter.hasNext();) {
            PurchasingCapitalAssetItem camsItem = (PurchasingCapitalAssetItem) iter.next();
            if (camsItem.getItemIdentifier().intValue() == itemIdentifier) {
                return camsItem;
            }
        }
        return null;
    }


    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#addItem(org.kuali.kfs.module.purap.businessobject.PurApItem)
     */
    @Override
    public void addItem(PurApItem item) {
        item.refreshReferenceObject(PurapPropertyConstants.COMMODITY_CODE);
        super.addItem(item);
    }
    
    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#populateDocumentForRouting()
     */
    @Override
    public void populateDocumentForRouting() {
        commodityCodesForRouting = new ArrayList<CommodityCode>();
    	List<PurchasingItemBase> previousPOItems = new ArrayList<PurchasingItemBase>();
    	if (this instanceof PurchaseOrderAmendmentDocument) {
    	    previousPOItems = getPreviousPoItems();
    	}
    	for (PurchasingItemBase item : (List<PurchasingItemBase>)this.getItems()) {
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
            	isFinal = poa.getDocumentHeader().getWorkflowDocument().stateIsFinal();
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

    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingDocument.getItemParser().
     */
    public ItemParser getItemParser() {
        return new ItemParserBase();
    }
    
    public String getBillingCityName() {
        return billingCityName;
    }

    public void setBillingCityName(String billingCityName) {
        this.billingCityName = billingCityName;
    }

    public String getBillingCountryCode() {
        return billingCountryCode;
    }

    public void setBillingCountryCode(String billingCountryCode) {
        this.billingCountryCode = billingCountryCode;
    }

    public String getBillingCountryName() {
        Country country = SpringContext.getBean(CountryService.class).getByPrimaryId(getBillingCountryCode());
        //if (country == null)
        //    country = SpringContext.getBean(CountryService.class).getDefaultCountry();
        if (country != null)
            return country.getPostalCountryName();
        return null;
    }
   
    public String getBillingLine1Address() {
        return billingLine1Address;
    }

    public void setBillingLine1Address(String billingLine1Address) {
        this.billingLine1Address = billingLine1Address;
    }

    public String getBillingLine2Address() {
        return billingLine2Address;
    }

    public void setBillingLine2Address(String billingLine2Address) {
        this.billingLine2Address = billingLine2Address;
    }

    public String getBillingName() {
        return billingName;
    }

    public void setBillingName(String billingName) {
        this.billingName = billingName;
    }

    public String getBillingPhoneNumber() {
        return billingPhoneNumber;
    }

    public void setBillingPhoneNumber(String billingPhoneNumber) {
        this.billingPhoneNumber = billingPhoneNumber;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    public void setBillingPostalCode(String billingPostalCode) {
        this.billingPostalCode = billingPostalCode;
    }

    public String getBillingStateCode() {
        return billingStateCode;
    }

    public void setBillingStateCode(String billingStateCode) {
        this.billingStateCode = billingStateCode;
    }

    public String getReceivingCityName() {
        return receivingCityName;
    }

    public void setReceivingCityName(String receivingCityName) {
        this.receivingCityName = receivingCityName;
    }

    public String getReceivingCountryCode() {
        return receivingCountryCode;
    }

    public void setReceivingCountryCode(String receivingCountryCode) {
        this.receivingCountryCode = receivingCountryCode;
    }

    public String getReceivingCountryName() {
        Country country = SpringContext.getBean(CountryService.class).getByPrimaryId(getReceivingCountryCode());
        //if (country == null)
        //    country = SpringContext.getBean(CountryService.class).getDefaultCountry();
        if (country != null)
            return country.getPostalCountryName();
        return null;
    }

    public String getReceivingLine1Address() {
        return receivingLine1Address;
    }

    public void setReceivingLine1Address(String receivingLine1Address) {
        this.receivingLine1Address = receivingLine1Address;
    }

    public String getReceivingLine2Address() {
        return receivingLine2Address;
    }

    public void setReceivingLine2Address(String receivingLine2Address) {
        this.receivingLine2Address = receivingLine2Address;
    }

    public String getReceivingName() {
        return receivingName;
    }

    public void setReceivingName(String receivingName) {
        this.receivingName = receivingName;
    }

    public String getReceivingPostalCode() {
        return receivingPostalCode;
    }

    public void setReceivingPostalCode(String receivingPostalCode) {
        this.receivingPostalCode = receivingPostalCode;
    }

    public String getReceivingStateCode() {
        return receivingStateCode;
    }

    public void setReceivingStateCode(String receivingStateCode) {
        this.receivingStateCode = receivingStateCode;
    }

    public boolean getAddressToVendorIndicator() {
        return addressToVendorIndicator;
    }

    public void setAddressToVendorIndicator(boolean addressToVendor) {
        this.addressToVendorIndicator = addressToVendor;
    }
    
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getDeliveryBuildingCode() {
        return deliveryBuildingCode;
    }

    public void setDeliveryBuildingCode(String deliveryBuildingCode) {
        this.deliveryBuildingCode = deliveryBuildingCode;
    }

    public String getDeliveryBuildingLine1Address() {
        return deliveryBuildingLine1Address;
    }

    public void setDeliveryBuildingLine1Address(String deliveryBuildingLine1Address) {
        this.deliveryBuildingLine1Address = deliveryBuildingLine1Address;
    }

    public String getDeliveryBuildingLine2Address() {
        return deliveryBuildingLine2Address;
    }

    public void setDeliveryBuildingLine2Address(String deliveryBuildingLine2Address) {
        this.deliveryBuildingLine2Address = deliveryBuildingLine2Address;
    }

    public String getDeliveryBuildingName() {
        return deliveryBuildingName;
    }

    public void setDeliveryBuildingName(String deliveryBuildingName) {
        this.deliveryBuildingName = deliveryBuildingName;
    }

    public boolean isDeliveryBuildingOtherIndicator() {
        return deliveryBuildingOtherIndicator;
    }

    public void setDeliveryBuildingOtherIndicator(boolean deliveryBuildingOtherIndicator) {
        this.deliveryBuildingOtherIndicator = deliveryBuildingOtherIndicator;
    }

    public String getDeliveryBuildingRoomNumber() {
        return deliveryBuildingRoomNumber;
    }

    public void setDeliveryBuildingRoomNumber(String deliveryBuildingRoomNumber) {
        this.deliveryBuildingRoomNumber = deliveryBuildingRoomNumber;
    }

    public String getDeliveryCampusCode() {
        return deliveryCampusCode;
    }

    public void setDeliveryCampusCode(String deliveryCampusCode) {
        this.deliveryCampusCode = deliveryCampusCode;
    }

    public String getDeliveryCityName() {
        return deliveryCityName;
    }

    public void setDeliveryCityName(String deliveryCityName) {
        this.deliveryCityName = deliveryCityName;
    }

    public String getDeliveryCountryCode() {
        return deliveryCountryCode;
    }

    public String getDeliveryCountryName() {
        Country country = SpringContext.getBean(CountryService.class).getByPrimaryId(getDeliveryCountryCode());
        if (country != null)
            return country.getPostalCountryName();
        return null;
    }

    public void setDeliveryCountryCode(String deliveryCountryCode) {
        this.deliveryCountryCode = deliveryCountryCode;
    }

    
    public String getDeliveryInstructionText() {
        return deliveryInstructionText;
    }

    public void setDeliveryInstructionText(String deliveryInstructionText) {
        this.deliveryInstructionText = deliveryInstructionText;
    }

    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }

    public void setDeliveryPostalCode(String deliveryPostalCode) {
        this.deliveryPostalCode = deliveryPostalCode;
    }

    public Date getDeliveryRequiredDate() {
        return deliveryRequiredDate;
    }

    public void setDeliveryRequiredDate(Date deliveryRequiredDate) {
        this.deliveryRequiredDate = deliveryRequiredDate;
    }

    public String getDeliveryRequiredDateReasonCode() {
        return deliveryRequiredDateReasonCode;
    }

    public void setDeliveryRequiredDateReasonCode(String deliveryRequiredDateReasonCode) {
        this.deliveryRequiredDateReasonCode = deliveryRequiredDateReasonCode;
    }

    public String getDeliveryStateCode() {
        return deliveryStateCode;
    }

    public void setDeliveryStateCode(String deliveryStateCode) {
        this.deliveryStateCode = deliveryStateCode;
    }

    public String getDeliveryToEmailAddress() {
        return deliveryToEmailAddress;
    }

    public void setDeliveryToEmailAddress(String deliveryToEmailAddress) {
        this.deliveryToEmailAddress = deliveryToEmailAddress;
    }

    public String getDeliveryToName() {
        return deliveryToName;
    }

    public void setDeliveryToName(String deliveryToName) {
        this.deliveryToName = deliveryToName;
    }

    public String getDeliveryToPhoneNumber() {
        return deliveryToPhoneNumber;
    }

    public void setDeliveryToPhoneNumber(String deliveryToPhoneNumber) {
        this.deliveryToPhoneNumber = deliveryToPhoneNumber;
    }

    public String getExternalOrganizationB2bSupplierIdentifier() {
        return externalOrganizationB2bSupplierIdentifier;
    }

    public void setExternalOrganizationB2bSupplierIdentifier(String externalOrganizationB2bSupplierIdentifier) {
        this.externalOrganizationB2bSupplierIdentifier = externalOrganizationB2bSupplierIdentifier;
    }

    public String getDocumentFundingSourceCode() {
        return documentFundingSourceCode;
    }

    public void setDocumentFundingSourceCode(String documentFundingSourceCode) {
        this.documentFundingSourceCode = documentFundingSourceCode;
    }

    public String getInstitutionContactEmailAddress() {
        return institutionContactEmailAddress;
    }

    public void setInstitutionContactEmailAddress(String institutionContactEmailAddress) {
        this.institutionContactEmailAddress = institutionContactEmailAddress;
    }

    public String getInstitutionContactName() {
        return institutionContactName;
    }

    public void setInstitutionContactName(String institutionContactName) {
        this.institutionContactName = institutionContactName;
    }

    public String getInstitutionContactPhoneNumber() {
        return institutionContactPhoneNumber;
    }

    public void setInstitutionContactPhoneNumber(String institutionContactPhoneNumber) {
        this.institutionContactPhoneNumber = institutionContactPhoneNumber;
    }

    public String getNonInstitutionFundAccountNumber() {
        return nonInstitutionFundAccountNumber;
    }

    public void setNonInstitutionFundAccountNumber(String nonInstitutionFundAccountNumber) {
        this.nonInstitutionFundAccountNumber = nonInstitutionFundAccountNumber;
    }

    public String getNonInstitutionFundChartOfAccountsCode() {
        return nonInstitutionFundChartOfAccountsCode;
    }

    public void setNonInstitutionFundChartOfAccountsCode(String nonInstitutionFundChartOfAccountsCode) {
        this.nonInstitutionFundChartOfAccountsCode = nonInstitutionFundChartOfAccountsCode;
    }

    public String getNonInstitutionFundOrganizationCode() {
        return nonInstitutionFundOrganizationCode;
    }

    public void setNonInstitutionFundOrganizationCode(String nonInstitutionFundOrganizationCode) {
        this.nonInstitutionFundOrganizationCode = nonInstitutionFundOrganizationCode;
    }

    public String getNonInstitutionFundOrgChartOfAccountsCode() {
        return nonInstitutionFundOrgChartOfAccountsCode;
    }

    public void setNonInstitutionFundOrgChartOfAccountsCode(String nonInstitutionFundOrgChartOfAccountsCode) {
        this.nonInstitutionFundOrgChartOfAccountsCode = nonInstitutionFundOrgChartOfAccountsCode;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public boolean getPurchaseOrderAutomaticIndicator() {
        return purchaseOrderAutomaticIndicator;
    }

    public void setPurchaseOrderAutomaticIndicator(boolean purchaseOrderAutomaticIndicator) {
        this.purchaseOrderAutomaticIndicator = purchaseOrderAutomaticIndicator;
    }

    public Date getPurchaseOrderBeginDate() {
        return purchaseOrderBeginDate;
    }

    public void setPurchaseOrderBeginDate(Date purchaseOrderBeginDate) {
        this.purchaseOrderBeginDate = purchaseOrderBeginDate;
    }

    public String getPurchaseOrderCostSourceCode() {
        return purchaseOrderCostSourceCode;
    }

    public void setPurchaseOrderCostSourceCode(String purchaseOrderCostSourceCode) {
        this.purchaseOrderCostSourceCode = purchaseOrderCostSourceCode;
    }

    public Date getPurchaseOrderEndDate() {
        return purchaseOrderEndDate;
    }

    public void setPurchaseOrderEndDate(Date purchaseOrderEndDate) {
        this.purchaseOrderEndDate = purchaseOrderEndDate;
    }

    public KualiDecimal getPurchaseOrderTotalLimit() {
        return purchaseOrderTotalLimit;
    }

    public void setPurchaseOrderTotalLimit(KualiDecimal purchaseOrderTotalLimit) {
        this.purchaseOrderTotalLimit = purchaseOrderTotalLimit;
    }

    public String getPurchaseOrderTransmissionMethodCode() {       
        return purchaseOrderTransmissionMethodCode;
    }

    public void setPurchaseOrderTransmissionMethodCode(String purchaseOrderTransmissionMethodCode) {
        this.purchaseOrderTransmissionMethodCode = purchaseOrderTransmissionMethodCode;
    }

    public String getRecurringPaymentTypeCode() {
        return recurringPaymentTypeCode;
    }

    public void setRecurringPaymentTypeCode(String recurringPaymentTypeCode) {
        this.recurringPaymentTypeCode = recurringPaymentTypeCode;
    }

    public String getRequestorPersonEmailAddress() {
        return requestorPersonEmailAddress;
    }

    public void setRequestorPersonEmailAddress(String requestorPersonEmailAddress) {
        this.requestorPersonEmailAddress = requestorPersonEmailAddress;
    }

    public String getRequestorPersonName() {
        return requestorPersonName;
    }

    public void setRequestorPersonName(String requestorPersonName) {
        this.requestorPersonName = requestorPersonName;
    }

    public String getRequestorPersonPhoneNumber() {
        return requestorPersonPhoneNumber;
    }

    public void setRequestorPersonPhoneNumber(String requestorPersonPhoneNumber) {
        this.requestorPersonPhoneNumber = requestorPersonPhoneNumber;
    }

    public String getRequisitionSourceCode() {
        return requisitionSourceCode;
    }

    public void setRequisitionSourceCode(String requisitionSourceCode) {
        this.requisitionSourceCode = requisitionSourceCode;
    }

    public String getVendorContactsLabel() {
        return vendorContactsLabel;
    }

    public void setVendorContactsLabel(String vendorContactsLabel) {
        this.vendorContactsLabel = vendorContactsLabel;
    }

    public VendorContract getVendorContract() {
        if (ObjectUtils.isNull(vendorContract))
            refreshReferenceObject(PurapPropertyConstants.VENDOR_CONTRACT);
        return vendorContract;
    }

    public void setVendorContract(VendorContract vendorContract) {
        this.vendorContract = vendorContract;
    }

    public Integer getVendorContractGeneratedIdentifier() {
        return vendorContractGeneratedIdentifier;
    }

    public void setVendorContractGeneratedIdentifier(Integer vendorContractGeneratedIdentifier) {
        this.vendorContractGeneratedIdentifier = vendorContractGeneratedIdentifier;
    }

    public String getVendorContractName() {
        getVendorContract();
        if (ObjectUtils.isNull(vendorContract)) {
            return "";
        }
        else {
            return vendorContract.getVendorContractName();
        }
    }

    public String getVendorFaxNumber() {
        return vendorFaxNumber;
    }

    public void setVendorFaxNumber(String vendorFaxNumber) {
        this.vendorFaxNumber = vendorFaxNumber;
    }

//KFSPTS-1639    
//    //KFSPTS-1458
//    public String getVendorEmailAddress() {
//        return vendorEmailAddress;
//    }
//
//    //KFSPTS-1458
//    public void setVendorEmailAddress(String vendorEmailAddress) {
//        this.vendorEmailAddress = vendorEmailAddress;
//    }

    public String getVendorNoteText() {
        return vendorNoteText;
    }

    public void setVendorNoteText(String vendorNoteText) {
        this.vendorNoteText = vendorNoteText;
    }

    public String getVendorPaymentTermsCode() {
        return vendorPaymentTermsCode;
    }

    public void setVendorPaymentTermsCode(String vendorPaymentTermsCode) {
        this.vendorPaymentTermsCode = vendorPaymentTermsCode;
    }

    public String getVendorPhoneNumber() {
        return vendorPhoneNumber;
    }

    public void setVendorPhoneNumber(String vendorPhoneNumber) {
        this.vendorPhoneNumber = vendorPhoneNumber;
    }

    public Boolean getVendorRestrictedIndicator() {
        return vendorRestrictedIndicator;
    }

    public void setVendorRestrictedIndicator(Boolean vendorRestrictedIndicator) {
        this.vendorRestrictedIndicator = vendorRestrictedIndicator;
    }

    public String getVendorShippingPaymentTermsCode() {
        return vendorShippingPaymentTermsCode;
    }

    public void setVendorShippingPaymentTermsCode(String vendorShippingPaymentTermsCode) {
        this.vendorShippingPaymentTermsCode = vendorShippingPaymentTermsCode;
    }

    public String getVendorShippingTitleCode() {
        return vendorShippingTitleCode;
    }

    public void setVendorShippingTitleCode(String vendorShippingTitleCode) {
        this.vendorShippingTitleCode = vendorShippingTitleCode;
    }

    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    public CampusParameter getDeliveryCampus() {
        return deliveryCampus;
    }

    public DeliveryRequiredDateReason getDeliveryRequiredDateReason() {
        return deliveryRequiredDateReason;
    }

    public FundingSource getFundingSource() {
    	this.refreshReferenceObject(PurapPropertyConstants.FUNDING_SOURCE);
        return fundingSource;
    }

    public Account getNonInstitutionFundAccount() {
        return nonInstitutionFundAccount;
    }

    public Chart getNonInstitutionFundChartOfAccounts() {
        return nonInstitutionFundChartOfAccounts;
    }

    public Organization getNonInstitutionFundOrganization() {
        return nonInstitutionFundOrganization;
    }

    public Chart getNonInstitutionFundOrgChartOfAccounts() {
        return nonInstitutionFundOrgChartOfAccounts;
    }

    public Organization getOrganization() {
        return organization;
    }

    public PurchaseOrderTransmissionMethod getPurchaseOrderTransmissionMethod() {
        return purchaseOrderTransmissionMethod;
    }

    public RecurringPaymentType getRecurringPaymentType() {
        return recurringPaymentType;
    }

    public RequisitionSource getRequisitionSource() {
        return requisitionSource;
    }

    public String getSupplierDiversityLabel() {
        return supplierDiversityLabel;
    }

    public PurchaseOrderCostSource getPurchaseOrderCostSource() {
        if (ObjectUtils.isNull(purchaseOrderCostSource))
            refreshReferenceObject(PurapPropertyConstants.PURCHASE_ORDER_COST_SOURCE);
        return purchaseOrderCostSource;
    }

    /**
     * @deprecated
     */
    public void setChartOfAccounts(Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    /**
     * @deprecated
     */
    public void setDeliveryCampus(CampusParameter deliveryCampus) {
        this.deliveryCampus = deliveryCampus;
    }

    /**
     * @deprecated
     */
    public void setDeliveryRequiredDateReason(DeliveryRequiredDateReason deliveryRequiredDateReason) {
        this.deliveryRequiredDateReason = deliveryRequiredDateReason;
    }

    /**
     * @deprecated
     */
    public void setFundingSource(FundingSource fundingSource) {
        this.fundingSource = fundingSource;
    }

    /**
     * @deprecated
     */
    public void setNonInstitutionFundAccount(Account nonInstitutionFundAccount) {
        this.nonInstitutionFundAccount = nonInstitutionFundAccount;
    }

    /**
     * @deprecated
     */
    public void setNonInstitutionFundChartOfAccounts(Chart nonInstitutionFundChartOfAccounts) {
        this.nonInstitutionFundChartOfAccounts = nonInstitutionFundChartOfAccounts;
    }

    /**
     * @deprecated
     */
    public void setNonInstitutionFundOrganization(Organization nonInstitutionFundOrganization) {
        this.nonInstitutionFundOrganization = nonInstitutionFundOrganization;
    }

    /**
     * @deprecated
     */
    public void setNonInstitutionFundOrgChartOfAccounts(Chart nonInstitutionFundOrgChartOfAccounts) {
        this.nonInstitutionFundOrgChartOfAccounts = nonInstitutionFundOrgChartOfAccounts;
    }

    /**
     * @deprecated
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * @deprecated
     */
    public void setPurchaseOrderCostSource(PurchaseOrderCostSource purchaseOrderCostSource) {
        this.purchaseOrderCostSource = purchaseOrderCostSource;
    }

    /**
     * @deprecated
     */
    public void setPurchaseOrderTransmissionMethod(PurchaseOrderTransmissionMethod purchaseOrderTransmissionMethod) {
        this.purchaseOrderTransmissionMethod = purchaseOrderTransmissionMethod;
    }

    /**
     * @deprecated
     */
    public void setRecurringPaymentType(RecurringPaymentType recurringPaymentType) {
        this.recurringPaymentType = recurringPaymentType;
    }

    /**
     * @deprecated
     */
    public void setRequisitionSource(RequisitionSource requisitionSource) {
        this.requisitionSource = requisitionSource;
    }

    /**
     * Gets the receivingDocumentRequiredIndicator attribute. 
     * @return Returns the receivingDocumentRequiredIndicator.
     */
    public boolean isReceivingDocumentRequiredIndicator() {
        return receivingDocumentRequiredIndicator;
    }
    
    /**
     * Sets the receivingDocumentRequiredIndicator attribute value.
     * @param receivingDocumentRequiredIndicator The receivingDocumentRequiredIndicator to set.
     */
    public void setReceivingDocumentRequiredIndicator(boolean receivingDocumentRequiredIndicator) {
        this.receivingDocumentRequiredIndicator = receivingDocumentRequiredIndicator;
    }

    public boolean isPaymentRequestPositiveApprovalIndicator() {
        return paymentRequestPositiveApprovalIndicator;
    }

    public void setPaymentRequestPositiveApprovalIndicator(boolean paymentRequestPositiveApprovalIndicator) {
        this.paymentRequestPositiveApprovalIndicator = paymentRequestPositiveApprovalIndicator;
    }

    public List<CommodityCode> getCommodityCodesForRouting() {
        return commodityCodesForRouting;
    }

    public void setCommodityCodesForRouting(List<CommodityCode> commodityCodesForRouting) {
        this.commodityCodesForRouting = commodityCodesForRouting;
    }

    public String getCapitalAssetSystemTypeCode() {
        return capitalAssetSystemTypeCode;
    }

    public void setCapitalAssetSystemTypeCode(String capitalAssetSystemTypeCode) {
        this.capitalAssetSystemTypeCode = capitalAssetSystemTypeCode;
    }

    public String getCapitalAssetSystemStateCode() {
        return capitalAssetSystemStateCode;
    }

    public void setCapitalAssetSystemStateCode(String capitalAssetSystemStateCode) {
        this.capitalAssetSystemStateCode = capitalAssetSystemStateCode;
    }

    public CapitalAssetSystemType getCapitalAssetSystemType() {
        if(ObjectUtils.isNull(capitalAssetSystemType)){
            this.refreshReferenceObject("capitalAssetSystemType");
        }
        return capitalAssetSystemType;
    }

    public void setCapitalAssetSystemType(CapitalAssetSystemType capitalAssetSystemType) {
        this.capitalAssetSystemType = capitalAssetSystemType;
    }

    public CapitalAssetSystemState getCapitalAssetSystemState() {
        if(ObjectUtils.isNull(capitalAssetSystemState)){
            this.refreshReferenceObject("capitalAssetSystemState");
        }
        return capitalAssetSystemState;
    }

    public void setCapitalAssetSystemState(CapitalAssetSystemState capitalAssetSystemState) {
        this.capitalAssetSystemState = capitalAssetSystemState;
    }

    public List<CapitalAssetSystem> getPurchasingCapitalAssetSystems() {
        return purchasingCapitalAssetSystems;
    }

    public void setPurchasingCapitalAssetSystems(List<CapitalAssetSystem> purchasingCapitalAssetSystems) {
        this.purchasingCapitalAssetSystems = purchasingCapitalAssetSystems;
    }

    public List<PurchasingCapitalAssetItem> getPurchasingCapitalAssetItems() {
        return purchasingCapitalAssetItems;
    }

    public void setPurchasingCapitalAssetItems(List<PurchasingCapitalAssetItem> purchasingCapitalAssetItems) {
        this.purchasingCapitalAssetItems = purchasingCapitalAssetItems;
    }
    
    public abstract Class getPurchasingCapitalAssetItemClass();

    public abstract Class getPurchasingCapitalAssetSystemClass();
    
    public PurchasingItem getPurchasingItem(Integer itemIdentifier){
        
        if(ObjectUtils.isNull(itemIdentifier)) return null;
        
        PurchasingItem item = null;
                
        for(PurchasingItem pi: (List<PurchasingItem>)this.getItems()){
            if(itemIdentifier.equals(pi.getItemIdentifier())){
                item = pi;
                break;
            }
        }
        
        return item;
    }
    
    public PurchasingCapitalAssetItem getPurchasingCapitalAssetItem(Integer itemIdentifier){

        if(ObjectUtils.isNull(itemIdentifier)) return null;
        
        PurchasingCapitalAssetItem item = null;
        
        for(PurchasingCapitalAssetItem pcai: this.getPurchasingCapitalAssetItems()){
            if(itemIdentifier.equals(pcai.getItemIdentifier())){
                item = pcai;
                break;
            }
        }
        
        return item;
    }
    
    /**
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#buildListOfDeletionAwareLists()
     */
    @Override
    public List buildListOfDeletionAwareLists() {
        List managedLists = super.buildListOfDeletionAwareLists();
        if (allowDeleteAwareCollection) {
            List<ItemCapitalAsset> assetLists = new ArrayList<ItemCapitalAsset>();
            List<CapitalAssetLocation>capitalAssetLocationLists = new ArrayList<CapitalAssetLocation>();
            if (StringUtils.equals(this.getCapitalAssetSystemTypeCode(),PurapConstants.CapitalAssetSystemTypes.INDIVIDUAL)) {
                for (PurchasingCapitalAssetItem capitalAssetItem : this.getPurchasingCapitalAssetItems()) {
                    //We only need to add the itemCapitalAssets to assetLists if the system is not null, otherwise 
                    //just let the assetLists be empty ArrayList.
                    if (capitalAssetItem.getPurchasingCapitalAssetSystem() != null) {
                        assetLists.addAll(capitalAssetItem.getPurchasingCapitalAssetSystem().getItemCapitalAssets());
                        capitalAssetLocationLists.addAll(capitalAssetItem.getPurchasingCapitalAssetSystem().getCapitalAssetLocations());
                    }
                }
            }
            else {
                for (CapitalAssetSystem system : this.getPurchasingCapitalAssetSystems()) {
                    assetLists.addAll(system.getItemCapitalAssets());
                    capitalAssetLocationLists.addAll(system.getCapitalAssetLocations());
                }
            }
            managedLists.add(assetLists);
            managedLists.add(capitalAssetLocationLists);
            managedLists.add(this.getPurchasingCapitalAssetSystems());
            managedLists.add(this.getPurchasingCapitalAssetItems());
        }
        return managedLists;
    }
    
    /**
     * Overrides the method in PurchasingAccountsPayableDocumentBase to remove the
     * purchasingCapitalAssetSystem when the system type is either ONE or MULT.
     * 
     * @see org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase#prepareForSave(org.kuali.rice.kns.rule.event.KualiDocumentEvent)
     */
    @Override
    public void prepareForSave(KualiDocumentEvent event) {
        super.prepareForSave(event);
        if (StringUtils.isNotBlank(this.getCapitalAssetSystemTypeCode())) {
            if (this.getCapitalAssetSystemTypeCode().equals(PurapConstants.CapitalAssetSystemTypes.ONE_SYSTEM) || this.getCapitalAssetSystemTypeCode().equals(PurapConstants.CapitalAssetSystemTypes.MULTIPLE)) {
                //If the system state is ONE or MULT, we have to remove all the systems on the items because it's not applicable.
                for (PurchasingCapitalAssetItem camsItem : this.getPurchasingCapitalAssetItems()) {
                    camsItem.setPurchasingCapitalAssetSystem(null);
                }
            }
        }
        if (event instanceof RouteDocumentEvent || event instanceof ApproveDocumentEvent) {

            boolean defaultUseTaxIndicatorValue = SpringContext.getBean(PurchasingService.class).getDefaultUseTaxIndicatorValue(this);
            SpringContext.getBean(PurapService.class).updateUseTaxIndicator(this, defaultUseTaxIndicatorValue);
        }
        
        checkForFederalAccount();
    }
    
    /**
     * Check for Federal Accounts, and if any are found, set Funding Source to Federal, 
     * otherwise set Funding Source to default.
     */
    private void checkForFederalAccount() {
    	boolean federalFunding = false;
    	
    	for (SourceAccountingLine sourceAccountingLine : (List<SourceAccountingLine>)this.getSourceAccountingLines()) {
    		// On the PO, the sourceAccountingLine.account wasn't always populated even when we have an account number, so
    	    // we refresh the reference object so we can check for CFDA number
    	    if (ObjectUtils.isNotNull(sourceAccountingLine.getAccountNumber()) && ObjectUtils.isNull(sourceAccountingLine.getAccount().getAccountNumber())) {
    			sourceAccountingLine.refreshReferenceObject("account");
    		}
			if (ObjectUtils.isNotNull(sourceAccountingLine.getAccount().getAccountCfdaNumber())) {
				federalFunding = true;
				break;
			}
		}
    	
    	if (federalFunding) {
			this.setDocumentFundingSourceCode(PurapFundingSources.FEDERAL_FUNDING_SOURCE);
    	} else {
            this.setDocumentFundingSourceCode(SpringContext.getBean(ParameterService.class).getParameterValue(RequisitionDocument.class, PurapParameterConstants.DEFAULT_FUNDING_SOURCE));
    	}
    }

	public Date getTransactionTaxDate() {
        return SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
    }

    public void clearCapitalAssetFields() {
        this.setPurchasingCapitalAssetItems(new TypedArrayList(getPurchasingCapitalAssetItemClass()));
        this.setPurchasingCapitalAssetSystems(new TypedArrayList(getPurchasingCapitalAssetSystemClass()));
        this.setCapitalAssetSystemStateCode(null);
        this.setCapitalAssetSystemTypeCode(null);
        this.setCapitalAssetSystemState(null);
        this.setCapitalAssetSystemType(null);
        
    }

    /**
     * @return the payment request positive approval indicator
     */
    public boolean getPaymentRequestPositiveApprovalIndicatorForSearching() {
        return paymentRequestPositiveApprovalIndicator;
    }
    
    /**
     * @return the receiving document required indicator
     */
    public boolean getReceivingDocumentRequiredIndicatorForSearching() {
        return receivingDocumentRequiredIndicator;
    }
    
    public String getDocumentChartOfAccountsCodeForSearching(){
        return chartOfAccountsCode;
    }
    
    public String getDocumentOrganizationCodeForSearching(){
        return organizationCode;
    }
    
    public boolean shouldGiveErrorForEmptyAccountsProration() {
        return true;
    }
    
    public String getChartAndOrgCodeForResult(){
        return getChartOfAccountsCode() + "-" + getOrganizationCode();
    }
    
    public String getDeliveryCampusCodeForSearch(){
        return getDeliveryCampusCode();
    }
    
    public boolean getHasB2BVendor() {
        if (getVendorHeaderGeneratedIdentifier() != null) {
            refreshReferenceObject(VendorPropertyConstants.VENDOR_DETAIL);
            String campusCode = GlobalVariables.getUserSession().getPerson().getCampusCode();
            VendorDetail vendorDetail = getVendorDetail();
            if (vendorDetail == null || StringUtils.isEmpty(campusCode))
                return false; // this should never happen
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
    

}
