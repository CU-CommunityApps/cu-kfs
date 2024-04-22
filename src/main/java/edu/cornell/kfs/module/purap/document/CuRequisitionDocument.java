package edu.cornell.kfs.module.purap.document;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.RequisitionStatuses;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
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
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteLevelChange;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants;
import edu.cornell.kfs.module.purap.document.service.CuPurapService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.NoteExtendedAttribute;

@NAMESPACE(namespace = KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE)
@COMPONENT(component = "Requisition")
public class CuRequisitionDocument extends RequisitionDocument {

    private PermissionService permissionService;
    
   // KFSPTS-985, KFSUPGRADE-75
    public boolean isIntegratedWithFavoriteAccount() {
        return true;
    }

    @Override
    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(PurapWorkflowConstants.AWARD_REVIEW_REQUIRED)) return isAwardReviewRequired();
        if (nodeName.equals(CUPurapWorkflowConstants.B2B_AUTO_PURCHASE_ORDER)) { 
            final boolean isB2BAutoPurchaseOrder =  isB2BAutoPurchaseOrder();
            if(isB2BAutoPurchaseOrder) this.paymentRequestPositiveApprovalIndicator=true;
            return isB2BAutoPurchaseOrder;
        }
        
        
        
        return super.answerSplitNodeQuestion(nodeName);
        
    }

    /**
     * Overridden to unmask name and phone number. This will be able to be removed once this fix is in the base code.
     * 
     * Also overridden to use the alternate CuPurapService method for setting the APO limit.
     */
    @Override
    public void initiateDocument() {
        super.initiateDocument();

        final Person currentUser = GlobalVariables.getUserSession().getPerson();
        setDeliveryToName(currentUser.getName());
        setDeliveryToPhoneNumber(SpringContext.getBean(PhoneNumberService.class).formatNumberIfPossible(currentUser.getPhoneNumber()));
        setRequestorPersonName(currentUser.getName());
        setRequestorPersonPhoneNumber(SpringContext.getBean(PhoneNumberService.class).formatNumberIfPossible(currentUser.getPhoneNumber()));
        
        setOrganizationAutomaticPurchaseOrderLimit(getPurapService().getApoLimit(this));
    }

    protected boolean isB2BAutoPurchaseOrder() {
        boolean returnValue = false;
 
        final VendorDetail vendorDetail = SpringContext.getBean(VendorService.class)
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
        
        final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        final String autoPOAmountString = new String(parameterService.getParameterValueAsString(RequisitionDocument.class,
                CUPurapParameterConstants.B2B_TOTAL_AMOUNT_FOR_AUTO_PO));
        KualiDecimal autoPOAmount = new KualiDecimal(autoPOAmountString);
        // KFSPTS-1625
        
        String routedBy = getRoutedByPrincipalId();
        if (StringUtils.isBlank(routedBy)) {
        	routedBy = this.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        }
        if (KimApiServiceLocator.getPermissionService().hasPermission( routedBy,
                CUKFSConstants.ParameterNamespaces.PURCHASING, CUPurapConstants.B2B_HIGHER_LIMIT_PERMISSION)) {
            
        	String paramVal = parameterService.getParameterValueAsString(RequisitionDocument.class,
                    CUPurapParameterConstants.B2B_TOTAL_AMOUNT_FOR_SUPER_USER_AUTO_PO);
            if (StringUtils.isNotBlank(paramVal)) {
                autoPOAmount = new KualiDecimal(paramVal);
            }
        }
        final RequisitionDocument document = (RequisitionDocument) (SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(this.getDocumentNumber()));
        final KualiDecimal totalAmount = document.getDocumentHeader().getFinancialDocumentTotalAmount();
        if (ObjectUtils.isNotNull(autoPOAmount) && ObjectUtils.isNotNull(totalAmount) && (autoPOAmount.compareTo(totalAmount) >= 0)) {  
            returnValue = true;
               
        } else {
            returnValue =  false;
        }
        return returnValue;
    }
    
    public String getRoutedByPrincipalId() {
        final DocumentService documentService = SpringContext.getBean(DocumentService.class);
        final RequisitionDocument document = (RequisitionDocument) documentService.getByDocumentHeaderId(this.getDocumentNumber());
        final String principalId = document.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId();

        return principalId;
    }
    
    @Override
    public void toCopy() throws ValidationException {
        super.toCopy();
        this.setObjectId(null);
        this.setOrganizationAutomaticPurchaseOrderLimit(getPurapService().getApoLimit(this));
    }
    
    /**
     * toCopyFromGateway
     */
    
    public void toCopyFromGateway() throws WorkflowException, ValidationException {
        //no validation for the KFS copy requisition rules:
        
        final String sourceDocumentHeaderId = getDocumentNumber();
        setNewDocumentHeader();
        
        getDocumentHeader().setDocumentTemplateNumber(sourceDocumentHeaderId);
        // Clear out existing notes.
        if (getNotes() != null) {
            getNotes().clear();
        }

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
        setObjectId(null);
        
        // Clear related views
        setAccountsPayablePurchasingDocumentLinkIdentifier(null);
        setRelatedViews(null);
        
        final Person currentUser = GlobalVariables.getUserSession().getPerson();
        final ChartOrgHolder purapChartOrg = SpringContext.getBean(FinancialSystemUserService.class)
                .getPrimaryOrganization(currentUser, PurapConstants.PURAP_NAMESPACE);
        setPurapDocumentIdentifier(null);

        // Set req status to INPR.
        //for app doc status
        updateAndSaveAppDocStatus(RequisitionStatuses.APPDOC_IN_PROCESS);

        // Set fields from the user.
        if (ObjectUtils.isNotNull(purapChartOrg)) {
            setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
            setOrganizationCode(purapChartOrg.getOrganizationCode());
        }
        setPostingYear(SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear());

        boolean activeVendor = true;
        boolean activeContract = true;
        final Date today =  SpringContext.getBean(DateTimeService.class).getCurrentDate();
        VendorContract vendorContract = new VendorContract();
        vendorContract.setVendorContractGeneratedIdentifier(this.getVendorContractGeneratedIdentifier());
        final Map keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(vendorContract);
        vendorContract = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(VendorContract.class, keys);
        if (!(vendorContract != null && today.after(vendorContract.getVendorContractBeginningDate())
                && today.before(vendorContract.getVendorContractEndDate()))) {
            activeContract = false;
        }

        final VendorDetail vendorDetail = SpringContext.getBean(VendorService.class)
                .getVendorDetail(this.getVendorHeaderGeneratedIdentifier(), this.getVendorDetailAssignedIdentifier());
        if (!(vendorDetail != null && vendorDetail.isActiveIndicator())) {
            activeVendor = false;
        }

        //KFSPTS-916 : need vendor address key for business rules and only way to get it is to retrieve the default PO address for the vendor.
        if (vendorDetail != null) {         
            final VendorAddress vendorAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(getVendorHeaderGeneratedIdentifier(),
                    getVendorDetailAssignedIdentifier(), VendorConstants.AddressTypes.PURCHASE_ORDER, "");
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
            setVendorContractGeneratedIdentifier(null);
        }
        if (!activeContract) {
            setVendorContractGeneratedIdentifier(null);
        }

        // These fields should not be set in this method; force to be null
        setOrganizationAutomaticPurchaseOrderLimit(null);
        setPurchaseOrderAutomaticIndicator(false);

        for (final Iterator iter = this.getItems().iterator(); iter.hasNext();) {
            final RequisitionItem item = (RequisitionItem) iter.next();
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
        setOrganizationAutomaticPurchaseOrderLimit(getPurapService().getApoLimit(this));
        clearCapitalAssetFields();
        SpringContext.getBean(PurapService.class).clearTax(this, this.isUseTaxIndicator());
        
        refreshNonUpdateableReferences();
    }
    
    @Override
    public boolean isSensitive() {
        boolean isSensitive =  super.isSensitive();

        for (final PurchasingItemBase item : (List<PurchasingItemBase>)this.getItems()) {
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
        
        getAccountsForRouting();
        final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        
        // If the award amount is less than the threshold, then there's no reason to review the object codes, return false
        if (!isAwardAmountGreaterThanThreshold()) {
            return requiresAwardReview;
        }
        
        for (final PurApItem item : (List<PurApItem>) this.getItems()) {
            for (final PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
                
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

    protected boolean isObjectCodeAllowedForAwardRouting(
            final PurApAccountingLine accountingLine, final ParameterService parameterService) {
        if (ObjectUtils.isNull(accountingLine.getObjectCode())) {
            return false;
        }

        // make sure object code is active
        if (!accountingLine.getObjectCode().isFinancialObjectActiveCode()) {
            return false;
        }

        final String chartCode = accountingLine.getChartOfAccountsCode();
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
        final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        final String dollarThreshold = parameterService.getParameterValueAsString("KFS-PURAP", "Requisition", DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW);
        final KualiDecimal dollarThresholdDecimal = new KualiDecimal(dollarThreshold);
        if (this.getTotalPreTaxDollarAmount().isGreaterEqual(dollarThresholdDecimal)) {
            return true;
        }               
        return false;
    }
    
    public List<Account> getAccountsForAwardRouting() {
        final List<Account> accounts = new ArrayList<Account>();
        
        final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        for (final PurApItem item : (List<PurApItem>) this.getItems()) {
            for (final PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
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
    
    /**
     * @see org.kuali.kfs.module.purap.document.RequisitionDocument#doRouteLevelChange(org.kuali.kfs.kew.framework.postprocessor.DocumentRouteLevelChange)
     */
    @Override
    public void doRouteLevelChange(final DocumentRouteLevelChange change) {
    	super.doRouteLevelChange(change);
    	
    	// if route node is CommodityAPO change to app doc status Awaiting Commodity Review
		final String nodeName = change.getNewNodeName();
		
		if (RequisitionStatuses.NODE_COMMODITY_CODE_APO_REVIEW.equalsIgnoreCase(nodeName)) {
			if (!RequisitionStatuses.APPDOC_AWAIT_COMMODITY_CODE_REVIEW.equals(this.getApplicationDocumentStatus())) {
				this.updateAndSaveAppDocStatus(RequisitionStatuses.APPDOC_AWAIT_COMMODITY_CODE_REVIEW);
            }    
		}    	
    }

    /**
     * Overridden to also pre-populate note IDs on unsaved notes lacking attachments,
     * to avoid persistence problems with note extended attributes. This is necessary
     * because of the OJB behavior of trying to persist the 1-1 reference object
     * prior to the parent object, which can interfere with saving auto-generated
     * notes (like "copied from document" notes).
     *
     * @see org.kuali.kfs.module.purap.document.PurchasingDocumentBase#prepareForSave(org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent)
     */
    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
        super.prepareForSave(event);
        final SequenceAccessorService sequenceAccessorService = SpringContext.getBean(SequenceAccessorService.class);
        for (final Note note : getNotes()) {
            if (note.getNoteIdentifier() == null && ObjectUtils.isNull(note.getAttachment())) {
                // Pre-populate IDs on unsaved notes without attachments, as well as their extended attributes.
                final Long newNoteId = sequenceAccessorService.getNextAvailableSequenceNumber(CUKFSConstants.NOTE_SEQUENCE_NAME);
                note.setNoteIdentifier(newNoteId);
                ((NoteExtendedAttribute) note.getExtension()).setNoteIdentifier(newNoteId);
            }
        }
    }

    @Override
    public Date getCreateDateForResult() {
        return getCreateDate();
    }

    protected CuPurapService getPurapService() {
        return SpringContext.getBean(CuPurapService.class);
    }

}


