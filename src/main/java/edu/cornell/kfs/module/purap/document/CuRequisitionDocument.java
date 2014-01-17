package edu.cornell.kfs.module.purap.document;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.exception.ValidationException;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.PersistenceService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

@NAMESPACE(namespace = KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE)
@COMPONENT(component = "Requisition")
public class CuRequisitionDocument extends RequisitionDocument {

    protected String vendorEmailAddress;
    private PermissionService permissionService;
    
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(CUPurapWorkflowConstants.AWARD_REVIEW_REQUIRED)) return isAwardReviewRequired();
        if (nodeName.equals(CUPurapWorkflowConstants.B2B_AUTO_PURCHASE_ORDER)) { 
            boolean isB2BAutoPurchaseOrder =  isB2BAutoPurchaseOrder();
            if(isB2BAutoPurchaseOrder) this.paymentRequestPositiveApprovalIndicator=true;
            return isB2BAutoPurchaseOrder;
        }
        return super.answerSplitNodeQuestion(nodeName);
        
    }
    
    protected boolean isB2BAutoPurchaseOrder() {
        boolean returnValue = false;
 
        VendorDetail vendorDetail = SpringContext.getBean(VendorService.class)
                .getVendorDetail(this.getVendorHeaderGeneratedIdentifier(), this.getVendorDetailAssignedIdentifier());
        if (vendorDetail != null) {
            if (vendorDetail.isB2BVendor()) {
                returnValue = isB2BTotalAmountForAutoPO();
            } else {
                returnValue =  false;
            }
            return returnValue;
        }
        return false;
    }
    
    protected boolean isB2BTotalAmountForAutoPO() {
        boolean returnValue = false;
        
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        String autoPOAmountString = new String(parameterService.getParameterValueAsString(RequisitionDocument.class,
                CUPurapParameterConstants.B2B_TOTAL_AMOUNT_FOR_AUTO_PO));
        KualiDecimal autoPOAmount = new KualiDecimal(autoPOAmountString);
        // KFSPTS-1625
         
        if (KimApiServiceLocator.getPermissionService().hasPermission(
                getRoutedByPrincipalId(), CUKFSConstants.ParameterNamespaces.PURCHASING, CUPurapConstants.B2B_HIGHER_LIMIT_PERMISSION)) {
            String paramVal = parameterService.getParameterValueAsString(RequisitionDocument.class,
                    CUPurapParameterConstants.B2B_TOTAL_AMOUNT_FOR_SUPER_USER_AUTO_PO);
            if (StringUtils.isNotBlank(paramVal)) {
                autoPOAmount = new KualiDecimal(paramVal);
            }
        }
        RequisitionDocument document = null;
        try {
            document = (RequisitionDocument) (SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(this.getDocumentNumber()));
        } catch (WorkflowException we) {            
        }
        KualiDecimal totalAmount = document.getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount();
        if (ObjectUtils.isNotNull(autoPOAmount) && ObjectUtils.isNotNull(totalAmount) && (autoPOAmount.compareTo(totalAmount) >= 0)) {
            returnValue = true;
        } else {
            returnValue =  false;
        }
        return returnValue;
    }
    
    public String getRoutedByPrincipalId() {
        DocumentService documentService = SpringContext.getBean(DocumentService.class);
        RequisitionDocument document = null;
        String principalId = null;
        try {
            document = (RequisitionDocument) documentService.getByDocumentHeaderId(this.getDocumentNumber());
            principalId = document.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId();
        } catch (WorkflowException we) {

        }
        return principalId;
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
        getNotes();
                
        getDocumentHeader().setDocumentTemplateNumber(sourceDocumentHeaderId);

        addCopyErrorDocumentNote("copied from document " + sourceDocumentHeaderId);   
        
       //--- LedgerPostingDocumentBase:
        setAccountingPeriod(retrieveCurrentAccountingPeriod());
        //--GeneralLedgerPostingDocumentBase:
        getGeneralLedgerPendingEntries().clear();
        //--AccountingDocumentBase:
        copyAccountingLines(false);
        updatePostingYearForAccountingLines(getSourceAccountingLines());
        updatePostingYearForAccountingLines(getTargetAccountingLines());
        
        //--RequisitionDocument:
        
        // Clear related views
        this.setAccountsPayablePurchasingDocumentLinkIdentifier(null);
        this.setRelatedViews(null);
        
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        ChartOrgHolder purapChartOrg = SpringContext.getBean(FinancialSystemUserService.class)
                .getPrimaryOrganization(currentUser, PurapConstants.PURAP_NAMESPACE);
        this.setPurapDocumentIdentifier(null);

        // Set req status to INPR.
        this.setStatusCode(PurapConstants.RequisitionStatuses.APPDOC_IN_PROCESS);

        // Set fields from the user.
        if (ObjectUtils.isNotNull(purapChartOrg)) {
            this.setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
            this.setOrganizationCode(purapChartOrg.getOrganizationCode());
        }
        this.setPostingYear(SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear());

        boolean activeVendor = true;
        boolean activeContract = true;
        Date today =  SpringContext.getBean(DateTimeService.class).getCurrentDate();
        VendorContract vendorContract = new VendorContract();
        vendorContract.setVendorContractGeneratedIdentifier(this.getVendorContractGeneratedIdentifier());
        Map keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(vendorContract);
        vendorContract = (VendorContract) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(VendorContract.class, keys);
        if (!(vendorContract != null && today.after(vendorContract.getVendorContractBeginningDate())
                && today.before(vendorContract.getVendorContractEndDate()))) {
            activeContract = false;
        }

        VendorDetail vendorDetail = SpringContext.getBean(VendorService.class)
                .getVendorDetail(this.getVendorHeaderGeneratedIdentifier(), this.getVendorDetailAssignedIdentifier());
        if (!(vendorDetail != null && vendorDetail.isActiveIndicator())) {
            activeVendor = false;
        }

        //KFSPTS-916 : need vendor address key for business rules and only way to get it is to retrieve the default PO address for the vendor.
        if (vendorDetail != null) {         
            VendorAddress vendorAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(this.getVendorHeaderGeneratedIdentifier(),
                    this.getVendorDetailAssignedIdentifier(), VendorConstants.AddressTypes.PURCHASE_ORDER, "");
            if (vendorAddress != null) {
                super.templateVendorAddress(vendorAddress);   
            }
        }

        // B2B - only copy if contract and vendor are both active (throw separate errors to print to screen)
        if (this.getRequisitionSourceCode().equals(PurapConstants.RequisitionSources.B2B)) {
            if (!activeContract) {
              //--  throw new ValidationException(PurapKeyConstants.ERROR_REQ_COPY_EXPIRED_CONTRACT);
            }
            if (!activeVendor) {
               //-- throw new ValidationException(PurapKeyConstants.ERROR_REQ_COPY_INACTIVE_VENDOR);
            }
        }

        if (!activeVendor) {
            this.setVendorContractGeneratedIdentifier(null);
        }
        if (!activeContract) {
            this.setVendorContractGeneratedIdentifier(null);
        }

        // These fields should not be set in this method; force to be null
        this.setOrganizationAutomaticPurchaseOrderLimit(null);
        this.setPurchaseOrderAutomaticIndicator(false);

        // Fill the BO Notes with an empty List.
        this.setNotes(new ArrayList());

        for (Iterator iter = this.getItems().iterator(); iter.hasNext();) {
            RequisitionItem item = (RequisitionItem) iter.next();
            item.setPurapDocumentIdentifier(null);
            item.setItemIdentifier(null);
            for (Iterator acctIter = item.getSourceAccountingLines().iterator(); acctIter.hasNext();) {
                RequisitionAccount account = (RequisitionAccount) acctIter.next();
                account.setAccountIdentifier(null);
                account.setItemIdentifier(null);
            }
        }

        if (!PurapConstants.RequisitionSources.B2B.equals(this.getRequisitionSourceCode())) {
            SpringContext.getBean(PurapService.class).addBelowLineItems(this);
        }
        this.setOrganizationAutomaticPurchaseOrderLimit(SpringContext.getBean(PurapService.class)
                .getApoLimit(this.getVendorContractGeneratedIdentifier(), this.getChartOfAccountsCode(), this.getOrganizationCode()));
        clearCapitalAssetFields();
        SpringContext.getBean(PurapService.class).clearTax(this, this.isUseTaxIndicator());
        
        this.refreshNonUpdateableReferences();
    }
    
    @Override
    public boolean isSensitive() {
        boolean isSensitive =  super.isSensitive();

        for (PurchasingItemBase item : (List<PurchasingItemBase>)this.getItems()) {
            if (item.getCommodityCode() != null 
                    && item.getCommodityCode().getSensitiveDataCode() != null 
                    && item.getCommodityCode().getSensitiveDataCode().length() != 0) {
                isSensitive |= true;
            }
        }
        return isSensitive;
    }
    
    public static final String DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW = "DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW";

    protected boolean isAwardReviewRequired() {
        boolean requiresAwardReview = false;
        
        this.getAccountsForRouting();
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        
        // If the award amount is less than the threshold, then there's no reason to review the object codes, return false
        if (!isAwardAmountGreaterThanThreshold()) {
            return requiresAwardReview;
        }
        
        for (PurApItem item : (List<PurApItem>) this.getItems()) {
            for (PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
                
                requiresAwardReview = isObjectCodeAllowedForAwardRouting(accountingLine, parameterService);
                // We should return true as soon as we have at least one objectCodeAllowed=true so that the PO will stop at Award
                // level.
                if (requiresAwardReview) {
                    return requiresAwardReview;
                }

            }
        }
        return requiresAwardReview;        
    }

    protected boolean isObjectCodeAllowedForAwardRouting(PurApAccountingLine accountingLine, ParameterService parameterService) {
        if (ObjectUtils.isNull(accountingLine.getObjectCode())) {
            return false;
        }

        // make sure object code is active
        if (!accountingLine.getObjectCode().isFinancialObjectActiveCode()) {
            return false;
        }

        String chartCode = accountingLine.getChartOfAccountsCode();
        // check object level is in permitted list for award routing
        boolean objectCodeAllowed = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(PurchaseOrderDocument.class,
                PurapParameterConstants.CG_ROUTE_OBJECT_LEVELS_BY_CHART, PurapParameterConstants.NO_CG_ROUTE_OBJECT_LEVELS_BY_CHART,
                chartCode, accountingLine.getObjectCode().getFinancialObjectLevelCode()).evaluationSucceeds();

        if (!objectCodeAllowed) {
            // If the object level is not permitting for award routing, then we need to also
            // check object code is in permitted list for award routing
            objectCodeAllowed = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(PurchaseOrderDocument.class,
                    PurapParameterConstants.CG_ROUTE_OBJECT_CODES_BY_CHART, PurapParameterConstants.NO_CG_ROUTE_OBJECT_CODES_BY_CHART,
                    chartCode, accountingLine.getFinancialObjectCode()).evaluationSucceeds();
        }
        return objectCodeAllowed;
    }

    protected boolean isAwardAmountGreaterThanThreshold() {
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        String dollarThreshold = parameterService.getParameterValueAsString("KFS-PURAP", "Requisition", DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW);
        KualiDecimal dollarThresholdDecimal = new KualiDecimal(dollarThreshold);
        if (this.getTotalPreTaxDollarAmount().isGreaterEqual(dollarThresholdDecimal)) {
            return true;
        }               
        return false;
    }
    
    public List<Account> getAccountsForAwardRouting() {
        List<Account> accounts = new ArrayList<Account>();
        
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        for (PurApItem item : (List<PurApItem>) this.getItems()) {
            for (PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
                if (isObjectCodeAllowedForAwardRouting(accountingLine, parameterService)) {
                    if (ObjectUtils.isNull(accountingLine.getAccount())) {
                        accountingLine.refreshReferenceObject("account");
                    }
                    if (accountingLine.getAccount() != null && !accounts.contains(accountingLine.getAccount())) {
                        accounts.add(accountingLine.getAccount());
                    }
                }
            }
        }
        return accounts;
    }
        
}


