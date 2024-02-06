package edu.cornell.kfs.module.purap.document;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderView;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.util.PurApRelatedViews;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineParser;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.SufficientFundsItem;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.document.Copyable;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.Country;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class IWantDocument extends FinancialSystemTransactionalDocumentBase implements Copyable, PurchasingAccountsPayableDocument, AmountTotaling {

    private static final long serialVersionUID = 1L;

    private String step;

    private String initiatorNetID;
    private String initiatorName;
    private String initiatorEmailAddress;
    private String initiatorPhoneNumber;
    private String initiatorAddress;

    private boolean sameAsInitiator;
    private String deliverToNetID;
    private String deliverToName;
    private String deliverToEmailAddress;
    private String deliverToPhoneNumber;
    private String deliverToAddress;

    // Vendor Data
    private String vendorNumber;
    private Integer vendorDetailAssignedIdentifier;
    private Integer vendorHeaderGeneratedIdentifier;
    private String vendorCustomerNumber;
    private String vendorName;
    private String vendorAttentionName;
    private String vendorLine1Address;
    private String vendorLine2Address;
    private String vendorStateCode;
    private String vendorAddressInternationalProvinceName;
    private String vendorPostalCode;
    private String vendorCityName;
    private String vendorCountryCode;
    private String vendorAddress;
    private String vendorPhoneNumber;
    private String vendorWebURL;
    private String vendorFaxNumber;
    private String vendorEmail;
    private String vendorDescription;

    private String collegeLevelOrganization;
    private String departmentLevelOrganization;

    private boolean useCollegeAndDepartmentAsDefault;

    private boolean setDeliverToInfoAsDefault;

    private String attachmentDescription;
    private String noteLabel;
    private String noteTextOption;

    private String completeOption;
    private boolean completed;
    private String processorNetId;
    private String processorName;

    // routing fields
    private String routingChart;
    private String routingOrganization;

    //adhoc routing
    private String currentRouteToNetId;

    //Items
    private List<IWantItem> items;
    private List<IWantAccount> accounts;

    // Account Description free form field
    private String accountDescriptionTxt;

    // Checkbox that tells whether documentation has been attached to this edoc
    private boolean documentationAttached;

    //Notes

    // Comments/special instructions free form text
    private String commentsAndSpecialInstructions;

    // Goods versus Services
    private boolean goods;

    //Services related fields

    // Will service be performed on Campus: yes/no drop down box
    // Need to store as a String here due to drop-down usage; database already stores it as such.
    private String servicePerformedOnCampus;

    private String explanation;

    private KualiDecimal internalPurchasingLimit;

    private VendorDetail vendorDetail;

    // ID of associated reqs document, if any.
    private String reqsDocId;
    
    // ID of associated DV document, if any.
    private String dvDocId;
    
    // Copied this property from the base PURAP doc class, but made it private instead.
    private Integer accountsPayablePurchasingDocumentLinkIdentifier;
    
    // The selected line's ID from the Favorite Accounts drop-down, if any; not persisted.
    private Integer favoriteAccountLineIdentifier;
    
    // Copied this property from the base PURAP doc class, but made it private instead; not persisted.
    private transient PurApRelatedViews relatedViews;

    // The three fields below are for lookup purposes only; they are not persisted.
    private transient Chart collegeChartForSearch;
    private transient Organization collegeOrgForSearch;
    private transient Organization departmentOrgForSearch;
    
    // The difference between the item and account totals; not persisted.
    private KualiDecimal itemAndAccountDifference;
    
    public IWantDocument() {
        super();
        items = new ArrayList<IWantItem>();
        accounts = new ArrayList<IWantAccount>();
        servicePerformedOnCampus = KFSConstants.ParameterValues.NO;
    }

    @Override
    public String getVendorName() {
        return vendorName;
    }

    @Override
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorWebURL() {
        return vendorWebURL;
    }

    public void setVendorWebURL(String vendorWebURL) {
        this.vendorWebURL = vendorWebURL;
    }

    public String getAccountDescriptionTxt() {
        return accountDescriptionTxt;
    }

    public void setAccountDescriptionTxt(String accountDescriptionTxt) {
        this.accountDescriptionTxt = accountDescriptionTxt;
    }

    public boolean isDocumentationAttached() {
        return documentationAttached;
    }

    public void setDocumentationAttached(boolean documentationAttached) {
        this.documentationAttached = documentationAttached;
    }

    public String getCommentsAndSpecialInstructions() {
        return commentsAndSpecialInstructions;
    }

    public void setCommentsAndSpecialInstructions(String commentsAndSpecialInstructions) {
        this.commentsAndSpecialInstructions = commentsAndSpecialInstructions;
    }

    public boolean isGoods() {
        return goods;
    }

    public void setGoods(boolean goods) {
        this.goods = goods;
    }

    public String getServicePerformedOnCampus() {
        return servicePerformedOnCampus;
    }

    public void setServicePerformedOnCampus(String servicePerformedOnCampus) {
        this.servicePerformedOnCampus = servicePerformedOnCampus;
    }

    /**
     * Returns the "servicePerformedOnCampus" property as a boolean, in case
     * we ever need to retrieve it as such.
     */
    public boolean serviceIsPerformedOnCampus() {
        return KFSConstants.ParameterValues.YES.equals(servicePerformedOnCampus);
    }

    public String getInitiatorNetID() {
        return initiatorNetID;
    }

    public void setInitiatorNetID(String initiatorNetID) {
        this.initiatorNetID = initiatorNetID;
    }
    
    /**
     * Returns the initiatorNetID with wildcards on either side, or
     * the current initiatorNetID if it is blank.
     */
    public String getInitiatorNetIDForLookup() {
        if (StringUtils.isNotBlank(initiatorNetID)) {
            return KFSConstants.WILDCARD_CHARACTER+ initiatorNetID + KFSConstants.WILDCARD_CHARACTER;
        }
        return initiatorNetID;
    }

    public String getInitiatorName() {
        return initiatorName;
    }

    public void setInitiatorName(String initiatorName) {
        this.initiatorName = initiatorName;
    }

    public String getInitiatorEmailAddress() {
        return initiatorEmailAddress;
    }

    public void setInitiatorEmailAddress(String initiatorEmailAddress) {
        this.initiatorEmailAddress = initiatorEmailAddress;
    }

    public String getInitiatorPhoneNumber() {
        return initiatorPhoneNumber;
    }

    public void setInitiatorPhoneNumber(String initiatorPhoneNumber) {
        this.initiatorPhoneNumber = initiatorPhoneNumber;
    }

    public String getDeliverToNetID() {
        return deliverToNetID;
    }

    public void setDeliverToNetID(String deliverToNetID) {
        this.deliverToNetID = deliverToNetID;
    }
    
    /**
     * Returns the deliverToNetID with wildcards on either side, or
     * the current deliverToNetID value if it is blank.
     */
    public String getDeliverToNetIDForLookup() {
        if (StringUtils.isNotBlank(deliverToNetID)) {
            return KFSConstants.WILDCARD_CHARACTER + deliverToNetID + KFSConstants.WILDCARD_CHARACTER;
        }
        return deliverToNetID;
    }

    public String getDeliverToName() {
        return deliverToName;
    }

    public void setDeliverToName(String deliverToName) {
        this.deliverToName = deliverToName;
    }

    public String getDeliverToEmailAddress() {
        return deliverToEmailAddress;
    }

    public void setDeliverToEmailAddress(String deliverToEmailAddress) {
        this.deliverToEmailAddress = deliverToEmailAddress;
    }

    public String getDeliverToPhoneNumber() {
        return deliverToPhoneNumber;
    }

    public void setDeliverToPhoneNumber(String deliverToPhoneNumber) {
        this.deliverToPhoneNumber = deliverToPhoneNumber;
    }

    public String getDeliverToAddress() {
        return deliverToAddress;
    }

    public void setDeliverToAddress(String deliverToAddress) {
        this.deliverToAddress = deliverToAddress;
    }

    @Override
    public String getVendorNumber() {
        return vendorNumber;
    }

    @Override
    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    @Override
    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }

    public void setVendorDetail(VendorDetail vendorDetail) {
        this.vendorDetail = vendorDetail;
    }

    @Override
    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    @Override
    public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    @Override
    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    @Override
    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    @Override
    public String getVendorCustomerNumber() {
        return vendorCustomerNumber;
    }

    @Override
    public void setVendorCustomerNumber(String vendorCustomerNumber) {
        this.vendorCustomerNumber = vendorCustomerNumber;
    }

    public String getVendorAttentionName() {
        return vendorAttentionName;
    }

    public void setVendorAttentionName(String vendorAttentionName) {
        this.vendorAttentionName = vendorAttentionName;
    }

    @Override
    public String getVendorLine1Address() {
        return vendorLine1Address;
    }

    @Override
    public void setVendorLine1Address(String vendorLine1Address) {
        this.vendorLine1Address = vendorLine1Address;
    }

    @Override
    public String getVendorLine2Address() {
        return vendorLine2Address;
    }

    @Override
    public void setVendorLine2Address(String vendorLine2Address) {
        this.vendorLine2Address = vendorLine2Address;
    }

    @Override
    public String getVendorStateCode() {
        return vendorStateCode;
    }

    @Override
    public void setVendorStateCode(String vendorStateCode) {
        this.vendorStateCode = vendorStateCode;
    }

    @Override
    public String getVendorAddressInternationalProvinceName() {
        return vendorAddressInternationalProvinceName;
    }

    @Override
    public void setVendorAddressInternationalProvinceName(String vendorAddressInternationalProvinceName) {
        this.vendorAddressInternationalProvinceName = vendorAddressInternationalProvinceName;
    }

    @Override
    public String getVendorPostalCode() {
        return vendorPostalCode;
    }

    @Override
    public void setVendorPostalCode(String vendorPostalCode) {
        this.vendorPostalCode = vendorPostalCode;
    }

    @Override
    public String getVendorCityName() {
        return vendorCityName;
    }

    @Override
    public void setVendorCityName(String vendorCityName) {
        this.vendorCityName = vendorCityName;
    }

    @Override
    public String getVendorCountryCode() {
        return vendorCountryCode;
    }

    @Override
    public void setVendorCountryCode(String vendorCountryCode) {
        this.vendorCountryCode = vendorCountryCode;
    }

    public String getVendorPhoneNumber() {
        return vendorPhoneNumber;
    }

    public void setVendorPhoneNumber(String vendorPhoneNumber) {
        this.vendorPhoneNumber = vendorPhoneNumber;
    }

    public String getVendorFaxNumber() {
        return vendorFaxNumber;
    }

    public void setVendorFaxNumber(String vendorFaxNumber) {
        this.vendorFaxNumber = vendorFaxNumber;
    }

    public boolean isSameAsInitiator() {
        return sameAsInitiator;
    }

    public void setSameAsInitiator(boolean sameAsInitiator) {
        this.sameAsInitiator = sameAsInitiator;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getItemClass() {
        return IWantItem.class;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List getItems() {
        return items;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setItems(List items) {
        this.items = items;
    }

    public KualiDecimal getInternalPurchasingLimit() {
        return internalPurchasingLimit;
    }

    public void setInternalPurchasingLimit(KualiDecimal internalPurchasingLimit) {
        this.internalPurchasingLimit = internalPurchasingLimit;
    }

    public String getVendorAddress() {
        return vendorAddress;
    }

    public void setVendorAddress(String vendorAddress) {
        this.vendorAddress = vendorAddress;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        if (this.getDocumentHeader() != null) {
            this.explanation = this.getDocumentHeader().getExplanation();
        }
    }

    public String getVendorEmail() {
        return vendorEmail;
    }

    public void setVendorEmail(String vendorEmail) {
        this.vendorEmail = vendorEmail;
    }

    public String getInitiatorAddress() {
        return initiatorAddress;
    }

    public void setInitiatorAddress(String initiatorAddress) {
        this.initiatorAddress = initiatorAddress;
    }

    @Override
    public void addItem(PurApItem purapItem) {
        IWantItem item = (IWantItem) purapItem;
        item.refreshReferenceObject(PurapPropertyConstants.COMMODITY_CODE);
        int itemLinePosition = getItemLinePosition();
        if (ObjectUtils.isNotNull(item.getItemLineNumber()) && (item.getItemLineNumber() > 0)
                && (item.getItemLineNumber() <= itemLinePosition)) {
            itemLinePosition = item.getItemLineNumber().intValue() - 1;
        }

        item.setiWantDocumentNumber(this.getDocumentNumber());
        item.setiWantDocument(this);

        items.add(itemLinePosition, item);
        renumberItems(itemLinePosition);

    }

    @Override
    public void renumberItems(int start) {
        for (int i = start; i < items.size(); i++) {
            IWantItem item = (IWantItem) items.get(i);

            item.setItemLineNumber(new Integer(i + 1));

        }
    }

    public void addAccount(IWantAccount account) {
        //item.refreshReferenceObject(PurapPropertyConstants.COMMODITY_CODE);
        //        int itemLinePosition = getItemLinePosition();
        //        if (ObjectUtils.isNotNull(account.getItemLineNumber()) && (account.getItemLineNumber() > 0)
        //                && (account.getItemLineNumber() <= itemLinePosition)) {
        //            itemLinePosition = account.getItemLineNumber().intValue() - 1;
        //        }

        account.setDocumentNumber(this.getDocumentNumber());
        //  account.setDocument(this);

        accounts.add(account);
        //        renumberItems(itemLinePosition);
    }

    @Override
    public int getItemLinePosition() {
        return items.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public IWantItem getItem(int index) {
        if (index >= items.size()) {
            for (int i = items.size(); i <= index; i++) {
                items.add(new IWantItem());
            }
        }
        return items.get(index);
    }

    public IWantAccount getAccount(int index) {
        if (index >= accounts.size()) {
            for (int i = accounts.size(); i <= index; i++) {
                accounts.add(new IWantAccount());
            }
        }
        return accounts.get(index);
    }

    @Override
    public void deleteItem(int lineNum) {
        if (items.remove(lineNum) == null) {
            // throw error here
        }
        renumberItems(lineNum);
    }

    public void deleteAccount(int lineNum) {
        if (accounts.remove(lineNum) == null) {
            // throw error here
        }
    }

    /**
     * Calculates the total dollar amount
     * 
     * @return
     */
    @Override
    public KualiDecimal getTotalDollarAmount() {

        KualiDecimal totalDollarAmount = KualiDecimal.ZERO;
        KualiDecimal itemTotal = KualiDecimal.ZERO;

        for (IWantItem item : items) {

            if (ObjectUtils.isNull(item.getItemQuantity())) {
                item.setItemQuantity(new KualiDecimal(1));
            }
            if (ObjectUtils.isNull(item.getItemUnitPrice())) {
                item.setItemUnitPrice(BigDecimal.ZERO);
            }

            itemTotal = new KualiDecimal(item.getItemQuantity().bigDecimalValue().multiply(item.getItemUnitPrice()));

            totalDollarAmount = totalDollarAmount.add(itemTotal);
        }

        return totalDollarAmount;
    }

    public String getCollegeLevelOrganization() {
        return collegeLevelOrganization;
    }

    public void setCollegeLevelOrganization(String collegeLevelOrganization) {
        this.collegeLevelOrganization = collegeLevelOrganization;
    }

    public String getDepartmentLevelOrganization() {
        return departmentLevelOrganization;
    }

    public void setDepartmentLevelOrganization(String departmentLevelOrganization) {
        this.departmentLevelOrganization = departmentLevelOrganization;
    }

    public boolean isUseCollegeAndDepartmentAsDefault() {
        return useCollegeAndDepartmentAsDefault;
    }

    public void setUseCollegeAndDepartmentAsDefault(boolean useCollegeAndDepartmentAsDefault) {
        this.useCollegeAndDepartmentAsDefault = useCollegeAndDepartmentAsDefault;
    }

    public String getVendorDescription() {
        return vendorDescription;
    }

    public void setVendorDescription(String vendorDescription) {
        this.vendorDescription = vendorDescription;
    }

    public List<IWantAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<IWantAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String getDocumentTitle() {

        return super.getDocumentTitle();
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getAttachmentDescription() {
        return attachmentDescription;
    }

    public void setAttachmentDescription(String attachmentDescription) {
        this.attachmentDescription = attachmentDescription;
    }

    public String getRoutingChart() {
        if (StringUtils.isNotEmpty(collegeLevelOrganization)) {
            routingChart = collegeLevelOrganization.substring(0, collegeLevelOrganization.lastIndexOf('-'));
        }
        return routingChart;
    }

    public void setRoutingChart(String routingChart) {

        this.routingChart = routingChart;
    }

    public String getRoutingOrganization() {
        if (StringUtils.isNotEmpty(departmentLevelOrganization)) {
            this.routingOrganization = departmentLevelOrganization;
        } else if (StringUtils.isNotEmpty(collegeLevelOrganization)) {
            routingOrganization = collegeLevelOrganization.substring(collegeLevelOrganization.lastIndexOf('-') + 1,
                    collegeLevelOrganization.length());
        }
        return routingOrganization;
    }

    public void setRoutingOrganization(String routingOrganization) {
        this.routingOrganization = routingOrganization;
    }

    public String getCollegeLevelChartForSearch() {
        return getRoutingChart();
    }
    
    public String getCollegeLevelOrgCodeForSearch() {
        if (StringUtils.isNotEmpty(collegeLevelOrganization)) {
            return collegeLevelOrganization.substring(collegeLevelOrganization.lastIndexOf('-') + 1,
                    collegeLevelOrganization.length());
        }
        return collegeLevelOrganization;
    }
    
    public String getDepartmentLevelOrgCodeForSearch() {
        return getDepartmentLevelOrganization();
    }
    
    /**
     * toCopyFromGateway
     */
    public void toCopyFromGateway() throws WorkflowException, ValidationException {
        //no validation for the KFS copy requisition rules:
        //  if (!this.getAllowsCopy()) {
        //      throw new IllegalStateException(this.getClass().getName() + " does not support document-level copying");
        // }

        String sourceDocumentHeaderId = getDocumentNumber();
        setNewDocumentHeader();

        //getNotes();
        getDocumentHeader().setDocumentTemplateNumber(sourceDocumentHeaderId);
        // Clear out existing notes.
        if (getNotes() != null) {
            getNotes().clear();
        }

        addCopyErrorDocumentNote("copied from document " + sourceDocumentHeaderId);

        copyItemsAndAccounts();
        
        setAccountsPayablePurchasingDocumentLinkIdentifier(null);
        setReqsDocId(null);
        setDvDocId(null);
        
        this.completeOption = null;
        this.completed = false;
    }

    @Override
    public void toCopy() throws IllegalStateException {
        super.toCopy();

        copyItemsAndAccounts();

        setAccountsPayablePurchasingDocumentLinkIdentifier(null);
        setReqsDocId(null);
        setDvDocId(null);

        this.completeOption = null;
        this.completed = false;
    }

    /**
     * Create copies of the items and accounts lists upon document copy.
     */
    private void copyItemsAndAccounts() {
        List<IWantItem> newItems = new ArrayList<IWantItem>();
        List<IWantAccount> newAccounts = new ArrayList<IWantAccount>();

        if (items != null && !items.isEmpty()) {
            for (IWantItem item : items) {
                newItems.add(IWantItem.createCopy(item));
            }
        }

        if (accounts != null && !accounts.isEmpty()) {
            for (IWantAccount account : accounts) {
                newAccounts.add(IWantAccount.createCopy(account));
            }
        }

        this.items = newItems;
        this.accounts = newAccounts;
    }

    /**
     * Computes the accounting lines total amount
     * 
     * @return
     */
    public KualiDecimal getAccountingLinesTotal() {
        KualiDecimal totalDollarAmount = KualiDecimal.ZERO;
        KualiDecimal accountTotal = KualiDecimal.ZERO;

        for (IWantAccount accountLine : accounts) {

            // if amount
            if (CUPurapConstants.AMOUNT.equalsIgnoreCase(accountLine.getUseAmountOrPercent())) {
                if (accountLine.getAmountOrPercent() != null) {
                    accountTotal = accountLine.getAmountOrPercent();
                } else {
                    accountTotal = KualiDecimal.ZERO;
                }
            }

            //if percent
            if (CUPurapConstants.PERCENT.equalsIgnoreCase(accountLine.getUseAmountOrPercent())) {
                if (accountLine.getAmountOrPercent() != null) {
                    if (totalDollarAmount != null) {

                        accountTotal = (accountLine.getAmountOrPercent().multiply(getTotalDollarAmount()))
                                .divide(new KualiDecimal(100));
                    } else {
                        accountTotal = KualiDecimal.ZERO;
                    }
                } else {
                    accountTotal = KualiDecimal.ZERO;
                }
            }

            totalDollarAmount = totalDollarAmount.add(accountTotal);
        }

        return totalDollarAmount;
    }

    public void setAccountingLinesTotal(KualiDecimal accountingLinesTotal) {
        // Do nothing.
    }

    public String getNoteLabel() {
        return noteLabel;
    }

    public void setNoteLabel(String noteLabel) {
        this.noteLabel = noteLabel;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        return KRADConstants.YES_INDICATOR_VALUE.equalsIgnoreCase(completeOption);
    }

    public String getProcessorNetId() {
        return processorNetId;
    }

    public void setProcessorNetId(String processorNetId) {
        this.processorNetId = processorNetId;
    }

    public String getProcessorNetIDForLookup() {
        if (StringUtils.isNotBlank(processorNetId)) {
            return KFSConstants.WILDCARD_CHARACTER + processorNetId + KFSConstants.WILDCARD_CHARACTER;
        }
        return processorNetId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }
    
    public String getCurrentRouteToNetId() {
        return currentRouteToNetId;
    }

    public void setCurrentRouteToNetId(String currentRouteToNetId) {
        this.currentRouteToNetId = currentRouteToNetId;
    }

    public String getCompleteOption() {
        return completeOption;
    }

    public void setCompleteOption(String completeOption) {
        this.completeOption = completeOption;
    }

    public boolean isSetDeliverToInfoAsDefault() {
        return setDeliverToInfoAsDefault;
    }

    public void setSetDeliverToInfoAsDefault(boolean setDeliverToInfoAsDefault) {
        this.setDeliverToInfoAsDefault = setDeliverToInfoAsDefault;
    }

    public String getReqsDocId() {
        return reqsDocId;
    }
    
    public void setReqsDocId(String reqsDocId) {
        this.reqsDocId = reqsDocId;
    }

    /**
     * Gets the difference between the item total amounts and the account total amounts.
     * Re-computes and stores this difference upon invocation.
     */
    public KualiDecimal getItemAndAccountDifference() {
        KualiDecimal itemTotal = getTotalDollarAmount();
        KualiDecimal accountTotal = getAccountingLinesTotal();
        itemAndAccountDifference = itemTotal.subtract(accountTotal);
        return itemAndAccountDifference;
    }

    public void setItemAndAccountDifference(KualiDecimal itemAndAccountDifference) {
        this.itemAndAccountDifference = itemAndAccountDifference;
    }

    /**
     * KFSPTS-1961:
     * We need to add the items and accounts lists to the deletion-aware-lists collection
     * to ensure that element deletion functions correctly.
     * 
     * @see org.kuali.kfs.kns.bo.PersistableBusinessObjectBase#buildListOfDeletionAwareLists()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List buildListOfDeletionAwareLists() {
        List deletionAwareLists = super.buildListOfDeletionAwareLists();
        deletionAwareLists.add(items);
        deletionAwareLists.add(accounts);
        return deletionAwareLists;
    }
    
    /**
     * Override this method to send out an email to the initiator when the document reached the final status.
     * 
     * @see org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase#doRouteStatusChange(org.kuali.kfs.kew.dto.DocumentRouteStatusChangeDTO)
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        
        super.doRouteStatusChange(statusChangeEvent);

        if (getDocumentHeader().getWorkflowDocument().isFinal()) {

            IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
            iWantDocumentService.sendDocumentFinalizedMessage(this);
        }

    }

    // The following link identifier getter and setter are needed for viewing the IWNT with other related PURAP docs.

    @Override
    public Integer getAccountsPayablePurchasingDocumentLinkIdentifier() {
        return accountsPayablePurchasingDocumentLinkIdentifier;
    }

    @Override
    public void setAccountsPayablePurchasingDocumentLinkIdentifier(Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        this.accountsPayablePurchasingDocumentLinkIdentifier = accountsPayablePurchasingDocumentLinkIdentifier;
    }

    // Copied the getter and setter below from PurchasingAccountsPayableDocumentBase.

    public PurApRelatedViews getRelatedViews() {
        if (relatedViews == null) {
            relatedViews = new PurApRelatedViews(this.documentNumber, this.accountsPayablePurchasingDocumentLinkIdentifier);
        }
        return relatedViews;
    }

    public void setRelatedViews(PurApRelatedViews relatedViews) {
        this.relatedViews = relatedViews;
    }

    // Copied the method below from PurchasingAccountsPayableDocumentBase.
    
    public boolean getNeedWarningRelatedPOs() {
        List<PurchaseOrderView> poViews = getRelatedViews().getRelatedPurchaseOrderViews();
        for (PurchaseOrderView poView : poViews) {
            if (poView.getNeedWarning()) {
                return true;
            }
        }
        return false;
    }

    // Added this method since the relatedDocuments.tag file relies on it when getIsReqsDoc() and getIsATypeOfPODoc() return false.

    public Integer getPurchaseOrderIdentifier() {
        return null;
    }

    // The three properties below are for lookup purposes only, and are not persisted.

    public Chart getCollegeChartForSearch() {
        return collegeChartForSearch;
    }

    public void setCollegeChartForSearch(Chart collegeChartForSearch) {
        this.collegeChartForSearch = collegeChartForSearch;
    }

    public Organization getCollegeOrgForSearch() {
        return collegeOrgForSearch;
    }

    public void setCollegeOrgForSearch(Organization collegeOrgForSearch) {
        this.collegeOrgForSearch = collegeOrgForSearch;
    }

    public Organization getDepartmentOrgForSearch() {
        return departmentOrgForSearch;
    }

    public void setDepartmentOrgForSearch(Organization departmentOrgForSearch) {
        this.departmentOrgForSearch = departmentOrgForSearch;
    }

    /*
     * ================================================================================
     * NOTE:
     * We only added the methods below to comply with the PURAP document interface and its super-interfaces.
     * Most of them just return null/empty values or are no-ops.
     * ================================================================================
     */

    @Override
    public List<SufficientFundsItem> checkSufficientFunds() {
        return Collections.emptyList();
    }

    @Override
    public List<GeneralLedgerPendingEntry> getGeneralLedgerPendingEntries() {
        return Collections.emptyList();
    }

    @Override
    public GeneralLedgerPendingEntry getGeneralLedgerPendingEntry(int arg0) {
        return null;
    }

    @Override
    public List<GeneralLedgerPendingEntry> getPendingLedgerEntriesForSufficientFundsChecking() {
        return Collections.emptyList();
    }

    @Override
    public void setGeneralLedgerPendingEntries(List<GeneralLedgerPendingEntry> arg0) {
        // Do nothing.
    }

    @Override
    public AccountingPeriod getAccountingPeriod() {
        return null;
    }

    @Override
    public String getPostingPeriodCode() {
        return null;
    }

    @Override
    public Integer getPostingYear() {
        return null;
    }

    @Override
    public void setAccountingPeriod(AccountingPeriod arg0) {
        // Do nothing.
    }

    @Override
    public void setPostingPeriodCode(String arg0) {
        // Do nothing.
    }

    @Override
    public void setPostingYear(Integer arg0) {
        // Do nothing.
    }

    @Override
    public void addPendingEntry(GeneralLedgerPendingEntry arg0) {
        // Do nothing.
    }

    @Override
    public void clearAnyGeneralLedgerPendingEntries() {
        // Do nothing.
    }

    @Override
    public boolean generateDocumentGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySequenceHelper arg0) {
        return false;
    }

    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail arg0, GeneralLedgerPendingEntrySequenceHelper arg1) {
        return false;
    }

    @Override
    public String getFinancialDocumentTypeCode() {
        return CUPurapConstants.IWNT_DOC_TYPE;
    }

    @Override
    public KualiDecimal getGeneralLedgerPendingEntryAmountForDetail(GeneralLedgerPendingEntrySourceDetail arg0) {
        return null;
    }

    @Override
    public List<GeneralLedgerPendingEntrySourceDetail> getGeneralLedgerPendingEntrySourceDetails() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail arg0) {
        return false;
    }

    @Override
    public void addSourceAccountingLine(SourceAccountingLine arg0) {
        // Do nothing.
    }

    @Override
    public void addTargetAccountingLine(TargetAccountingLine arg0) {
        // Do nothing.
    }

    @Override
    public AccountingLineParser getAccountingLineParser() {
        return null;
    }

    @Override
    public Class<? extends AccountingDocument> getDocumentClassForAccountingLineValueAllowedValidation() {
        return null;
    }

    @Override
    public Integer getNextSourceLineNumber() {
        return null;
    }

    @Override
    public Integer getNextTargetLineNumber() {
        return null;
    }

    @Override
    public SourceAccountingLine getSourceAccountingLine(int arg0) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getSourceAccountingLineClass() {
        return null;
    }

    @Override
    public String getSourceAccountingLineEntryName() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getSourceAccountingLines() {
        return Collections.emptyList();
    }

    @Override
    public String getSourceAccountingLinesSectionTitle() {
        return null;
    }

    @Override
    public KualiDecimal getSourceTotal() {
        return null;
    }

    @Override
    public TargetAccountingLine getTargetAccountingLine(int arg0) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getTargetAccountingLineClass() {
        return null;
    }

    @Override
    public String getTargetAccountingLineEntryName() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getTargetAccountingLines() {
        return Collections.emptyList();
    }

    @Override
    public String getTargetAccountingLinesSectionTitle() {
        return null;
    }

    @Override
    public KualiDecimal getTargetTotal() {
        return null;
    }

    // Copied the AccountingDocumentBase version of this method.
    @Override
    public boolean isDocumentFinalOrProcessed() {
        boolean isDocumentFinalOrProcessed = false;
        if (ObjectUtils.isNotNull(getDocumentHeader().getDocumentNumber())) {
            if (getDocumentHeader().hasWorkflowDocument()) {
                if (getDocumentHeader().getWorkflowDocument().isFinal() || getDocumentHeader().getWorkflowDocument().isProcessed()) {
                    isDocumentFinalOrProcessed = true;
                }
            }
           
        }

        return isDocumentFinalOrProcessed;
    }

    @Override
    public void setNextSourceLineNumber(Integer arg0) {
        // Do nothing.
    }

    @Override
    public void setNextTargetLineNumber(Integer arg0) {
        // Do nothing.
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setSourceAccountingLines(List arg0) {
        // Do nothing.
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setTargetAccountingLines(List arg0) {
        // Do nothing.
    }

    @Override
    public boolean isPostingYearNext() {
        return false;
    }

    @Override
    public boolean isPostingYearPrior() {
        return false;
    }

    @Override
    public Integer getPostingYearNextOrCurrent() {
        return null;
    }

    @Override
    public PurchasingAccountsPayableDocument getPurApSourceDocumentIfPossible() {
        return null;
    }

    @Override
    public String getPurApSourceDocumentLabelIfPossible() {
        return null;
    }

    @Override
    public boolean isDocumentStoppedInRouteNode(String nodeName) {
        return false;
    }

    @Override
    public void itemSwap(int position1, int position2) {
        // Do nothing.
    }

    @Override
    public String[] getBelowTheLineTypes() {
        return null;
    }

    @Override
    public void setTotalDollarAmount(KualiDecimal totalDollarAmount) {
        // Do nothing.
    }

    @Override
    public KualiDecimal getTotalDollarAmountAllItems(String[] excludedTypes) {
        return null;
    }

    @Override
    public KualiDecimal getTotalDollarAmountAboveLineItems() {
        return null;
    }

    @Override
    public KualiDecimal getTotalPreTaxDollarAmount() {
        return null;
    }

    @Override
    public void setTotalPreTaxDollarAmount(KualiDecimal totalDollarAmount) {
        // Do nothing.
    }

    @Override
    public KualiDecimal getTotalPreTaxDollarAmountAllItems(String[] excludedTypes) {
        return null;
    }

    @Override
    public KualiDecimal getTotalTaxAmount() {
        return null;
    }

    @Override
    public void setTotalTaxAmount(KualiDecimal amount) {
        // Do nothing.
    }

    @Override
    public KualiDecimal getTotalTaxAmountAllItems(String[] excludedTypes) {
        return null;
    }

    @Override
    public KualiDecimal getTotalTaxAmountAboveLineItems() {
        return null;
    }

    @Override
    public KualiDecimal getTotalTaxAmountAboveLineItems(String[] excludedTypes) {
        return null;
    }

    @Override
    public KualiDecimal getTotalTaxAmountWithExclusions(String[] excludedTypes, boolean includeBelowTheLine) {
        return null;
    }

    @Override
    public void templateVendorAddress(VendorAddress vendorAddress) {
        // Do nothing.
    }

    @Override
    public Country getVendorCountry() {
        return null;
    }

    @Override
    public Integer getPurapDocumentIdentifier() {
        return null;
    }

    @Override
    public void setPurapDocumentIdentifier(Integer identifier) {
        // Do nothing.
    }

    @Override
    public Integer getVendorAddressGeneratedIdentifier() {
        return null;
    }

    @Override
    public void setVendorAddressGeneratedIdentifier(Integer vendorAddressGeneratedIdentifier) {
        // Do nothing.
    }

    @Override
    public boolean isUseTaxIndicator() {
        return false;
    }

    @Override
    public void setUseTaxIndicator(boolean useTaxIndicator) {
        // Do nothing.
    }

    @Override
    public void fixItemReferences() {
        // Do nothing.
    }

    @Override
    public Date getTransactionTaxDate() {
        return null;
    }

    @Override
    public PurApItem getTradeInItem() {
        return null;
    }

    @Override
    public KualiDecimal getTotalDollarAmountForTradeIn() {
        return null;
    }

    @Override
    public List<PurApItem> getTradeInItems() {
        return Collections.emptyList();
    }

    @Override
    public boolean getIsATypeOfPurAPRecDoc() {
        return false;
    }

    @Override
    public boolean getIsATypeOfPurDoc() {
        return false;
    }

    @Override
    public boolean getIsATypeOfPODoc() {
        return false;
    }

    @Override
    public boolean getIsPODoc() {
        return false;
    }

    @Override
    public boolean getIsReqsDoc() {
        return false;
    }

    @Override
    public boolean isInquiryRendered() {
        return false;
    }

    @Override
    public boolean shouldGiveErrorForEmptyAccountsProration() {
        return false;
    }

    @Override
    public boolean isCalculated() {
        return false;
    }

    @Override
    public void setCalculated(boolean calculated) {
        // Do nothing.
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    public String getDvDocId() {
        return dvDocId;
    }

    public void setDvDocId(String dvDocId) {
        this.dvDocId = dvDocId;
    }
    
    public String getDvUrl() {
        return SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY) + "/DocHandler.do?docId=" + getDvDocId() + "&command=displayDocSearchView";
    }
    
    public Integer getFavoriteAccountLineIdentifier() {
        return favoriteAccountLineIdentifier;
    }
    
    public void setFavoriteAccountLineIdentifier(Integer favoriteAccountLineIdentifier) {
        this.favoriteAccountLineIdentifier = favoriteAccountLineIdentifier;
    }
    
    public String getDvDocumentLabel() throws WorkflowException{
        return SpringContext.getBean(DataDictionaryService.class).getDocumentLabelByTypeName("DV");     
    }

    public String getNoteTextOption() {
        return noteTextOption;
    }

    public void setNoteTextOption(String noteTextOption) {
        this.noteTextOption = noteTextOption;
    }

}
