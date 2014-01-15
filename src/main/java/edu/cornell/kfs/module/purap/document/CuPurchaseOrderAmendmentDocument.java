package edu.cornell.kfs.module.purap.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.service.SufficientFundsService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.sys.businessobject.SufficientFundsItem;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.workflow.service.WorkflowDocumentService;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import org.kuali.rice.krad.workflow.service.WorkflowDocumentService;

public class CuPurchaseOrderAmendmentDocument extends PurchaseOrderAmendmentDocument {

    boolean newUnorderedItem = false;
    // KFSPTS-1769
    boolean spawnPoa = false; 
    //Auto Generating POA
    
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        try {
            // DOCUMENT PROCESSED
            if (this.getFinancialSystemDocumentHeader().getWorkflowDocument().isProcessed()) {
                // generate GL entries
                SpringContext.getBean(PurapGeneralLedgerService.class).generateEntriesApproveAmendPurchaseOrder(this);
            
            // if gl entries created(means there is amount change) for amend purchase order send an FYI to all fiscal officers
                if (getGlOnlySourceAccountingLines() != null && !getGlOnlySourceAccountingLines().isEmpty()) {
                    SpringContext.getBean(PurchaseOrderService.class).sendFyiForGLEntries(this);
                }

                    // update indicators 
                SpringContext.getBean(PurchaseOrderService.class).completePurchaseOrderAmendment(this);

                // update vendor commodity code by automatically spawning vendor maintenance document
                SpringContext.getBean(PurchaseOrderService.class).updateVendorCommodityCode(this);
                
                // for app doc status
                updateAndSaveAppDocStatus(CUPurapConstants.PurchaseOrderStatuses.PENDING_CXML);
            } else if (this.getFinancialSystemDocumentHeader().getWorkflowDocument().isDisapproved()) {
                SpringContext.getBean(PurchaseOrderService.class).setCurrentAndPendingIndicatorsForDisapprovedChangePODocuments(this);
                SpringContext.getBean(PurapService.class).saveDocumentNoValidation(this);
                
                // for app doc status
                try {
                    String nodeName = SpringContext.getBean(WorkflowDocumentService.class)
                            .getCurrentRouteLevelName(this.getFinancialSystemDocumentHeader().getWorkflowDocument());
                    String reqStatus = PurapConstants.PurchaseOrderStatuses.getPurchaseOrderAppDocDisapproveStatuses().get(nodeName);
                    updateAndSaveAppDocStatus(PurapConstants.PurchaseOrderStatuses.getPurchaseOrderAppDocDisapproveStatuses().get(reqStatus));
                    
                    RequisitionDocument req = getPurApSourceDocumentIfPossible();
                    appSpecificRouteDocumentToUser(this.getFinancialSystemDocumentHeader().getWorkflowDocument(),
                            req.getFinancialSystemDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId(),
                            "Notification of Order Disapproval for Requisition " + req.getPurapDocumentIdentifier()
                            + "(document id " + req.getDocumentNumber() + ")", "Requisition Routed By User");
                    return;
                    
                } catch (WorkflowException e) {
                    logAndThrowRuntimeException("Error saving routing data while saving App Doc Status " + getDocumentNumber(), e);
                }
            }
            // DOCUMENT CANCELED
            else if (this.getFinancialSystemDocumentHeader().getWorkflowDocument().isCanceled()) {
                SpringContext.getBean(PurchaseOrderService.class).setCurrentAndPendingIndicatorsForCancelledChangePODocuments(this);

                // for app doc status
                updateAndSaveAppDocStatus(PurapConstants.PurchaseOrderStatuses.APPDOC_CANCELLED);                
            }
        } catch (WorkflowException e) {
            logAndThrowRuntimeException("Error saving routing data while saving document with id " + getDocumentNumber(), e);
        }
   }
    
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(PurapWorkflowConstants.HAS_ACCOUNTING_LINES)) return !isMissingAccountingLines();
        if (nodeName.equals(PurapWorkflowConstants.AMOUNT_REQUIRES_SEPARATION_OF_DUTIES_REVIEW_SPLIT)) return isSeparationOfDutiesReviewRequired();
        if (nodeName.equals(PurapWorkflowConstants.AWARD_REVIEW_REQUIRED)) return isAwardReviewRequired();
        if (nodeName.equals(PurapWorkflowConstants.HAS_NEW_UNORDERED_ITEMS)) return isNewUnorderedItem();
        if (nodeName.equals(PurapWorkflowConstants.CONTRACT_MANAGEMENT_REVIEW_REQUIRED )) return isContractManagementReviewRequired();
        if (nodeName.equals(PurapWorkflowConstants.BUDGET_REVIEW_REQUIRED )) return isContractManagementReviewRequired();
        if (nodeName.equals(PurapWorkflowConstants.VENDOR_IS_EMPLOYEE_OR_NON_RESIDENT_ALIEN)) return isVendorEmployeeOrNonResidentAlien();
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \""+nodeName+"\"");
    }
    
    protected boolean isMissingAccountingLines() {
        for (Iterator iterator = getItems().iterator(); iterator.hasNext();) {
            PurchaseOrderItem item = (PurchaseOrderItem) iterator.next();
            if (item.isConsideredEntered() && item.isAccountListEmpty()) {
                return true;
            }
        }
        
        return false;
    }

    protected boolean isContractManagementReviewRequired() {
//        KualiDecimal internalPurchasingLimit = SpringContext.getBean(PurchaseOrderService.class).getInternalPurchasingDollarLimit(this);
//        return ((ObjectUtils.isNull(internalPurchasingLimit)) || (internalPurchasingLimit.compareTo(this.getTotalDollarAmount()) < 0));
        
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        KualiDecimal automaticPurchaseOrderDefaultLimit = new KualiDecimal(parameterService
                .getParameterValueAsString(RequisitionDocument.class, PurapParameterConstants.AUTOMATIC_PURCHASE_ORDER_DEFAULT_LIMIT_AMOUNT));
        return (ObjectUtils.isNull(automaticPurchaseOrderDefaultLimit)) || (automaticPurchaseOrderDefaultLimit.compareTo(this.getTotalDollarAmount()) < 0);
    }

    
    protected boolean isSeparationOfDutiesReviewRequired() {
        try {            
            Set<Person> priorApprovers = this.getAllPriorApprovers();
            // if there are more than 0 prior approvers which means there had been at least another approver than the current approver
            // then no need for separation of duties
            if (priorApprovers.size() > 0) {
                return false;
            }
        } catch (WorkflowException we) {
            LOG.error("Exception while attempting to retrieve all prior approvers from workflow: " + we);
        }
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        KualiDecimal maxAllowedAmount = new KualiDecimal(parameterService
                .getParameterValueAsString(RequisitionDocument.class, PurapParameterConstants.SEPARATION_OF_DUTIES_DOLLAR_AMOUNT));
        // if app param amount is greater than or equal to documentTotalAmount... no need for separation of duties
        KualiDecimal totalAmount = getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount();
        if (ObjectUtils.isNotNull(maxAllowedAmount) && ObjectUtils.isNotNull(totalAmount) && (maxAllowedAmount.compareTo(totalAmount) >= 0)) {
            return false;
        } else {
            return true;
        }
    }

    protected boolean isBudgetReviewRequired() {
        boolean alwaysRoutes = true;
        String documentHeaderId = null;
        String currentXpathExpression = null;
        String documentFiscalYearString = this.getPostingYear().toString();

        // if document's fiscal year is less than or equal to the current fiscal year
        if (SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear().compareTo(Integer.valueOf(documentFiscalYearString)) >= 0) {
            // get list of sufficientfundItems
            List<SufficientFundsItem> fundsItems = SpringContext.getBean(SufficientFundsService.class).checkSufficientFunds(getPendingLedgerEntriesForSufficientFundsChecking());
                for (SufficientFundsItem fundsItem : fundsItems) {
                    if (this.getChartOfAccountsCode().equalsIgnoreCase(fundsItem.getAccount().getChartOfAccountsCode())) {
                    LOG.debug("Chart code of rule extension matches chart code of at least one Sufficient Funds Item");
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean isVendorEmployeeOrNonResidentAlien() {
        Integer vendorHeaderGeneratedIdentifier = this.getVendorHeaderGeneratedIdentifier();
        if (vendorHeaderGeneratedIdentifier!=null) {
            String vendorHeaderGeneratedId = this.getVendorHeaderGeneratedIdentifier().toString();
            if (StringUtils.isBlank(vendorHeaderGeneratedId)) {
                // no vendor header id so can't check for proper tax routing
                return false;
            }
            VendorService vendorService = SpringContext.getBean(VendorService.class);
            boolean routeDocumentAsEmployeeVendor = vendorService.isVendorInstitutionEmployee(Integer.valueOf(vendorHeaderGeneratedId));
            boolean routeDocumentAsForeignVendor = vendorService.isVendorForeign(Integer.valueOf(vendorHeaderGeneratedId));
            if ((!routeDocumentAsEmployeeVendor) && (!routeDocumentAsForeignVendor)) {
                // no need to route
                return false;
            }
    
            return true;
        }
        return false;
    }

    public boolean isSpawnPoa() {
        return spawnPoa;
    }

    public void setSpawnPoa(boolean spawnPoa) {
        this.spawnPoa = spawnPoa;
    }



    // KFSPTS-1705
    protected boolean shouldAdhocFyi() {
        Collection<String> excludeList = new ArrayList<String>();
        if (SpringContext.getBean(ParameterService.class).parameterExists(PurchaseOrderDocument.class, CUPurapParameterConstants.PO_NOTIFY_EXCLUSIONS)) {
            excludeList = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(PurchaseOrderDocument.class, CUPurapParameterConstants.PO_NOTIFY_EXCLUSIONS);
        }

        if ((getDocumentHeader().getWorkflowDocument().isFinal() || getDocumentHeader().getWorkflowDocument().isDisapproved()) && !excludeList.contains(getRequisitionSourceCode()) ) {
            return true;
        }
        return false;
    }
    
    public Set<Person> getAllPriorApprovers() throws WorkflowException {
        PersonService personService = KimApiServiceLocator.getPersonService();
         List<ActionTaken> actionsTaken = this.getFinancialSystemDocumentHeader().getWorkflowDocument().getActionsTaken();
        Set<String> principalIds = new HashSet<String>();
        Set<Person> persons = new HashSet<Person>();

        for (ActionTaken actionTaken : actionsTaken) {
            if (KewApiConstants.ACTION_TAKEN_APPROVED_CD.equals(actionTaken.getActionTaken().getCode())) {
                String principalId = actionTaken.getPrincipalId();
                if (!principalIds.contains(principalId)) {
                    principalIds.add(principalId);
                    persons.add(personService.getPerson(principalId));
                }
            }
        }
        return persons;
    }

}
