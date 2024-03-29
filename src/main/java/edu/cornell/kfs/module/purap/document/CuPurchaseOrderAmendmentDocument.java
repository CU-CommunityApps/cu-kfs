package edu.cornell.kfs.module.purap.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;

@NAMESPACE(namespace = KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE)
@COMPONENT(component = "PurchaseOrderAmendment")
public class CuPurchaseOrderAmendmentDocument extends PurchaseOrderAmendmentDocument {
	private static final Logger LOG = LogManager.getLogger(CuPurchaseOrderAmendmentDocument.class);
    // KFSPTS-1769
    boolean spawnPoa = false; //Auto Generating POA

	public boolean isSpawnPoa() {
		return spawnPoa;
	}

	public void setSpawnPoa(final boolean spawnPoa) {
		this.spawnPoa = spawnPoa;
	}


    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
//      if (nodeName.equals("ReplicateRequisitionRouting")) return 
//
//		I think the "isMissingAccountingLines" method will cause document to route to account node
//		where fiscal officer needs to add accounting lines    	
//  	
      if (nodeName.equals(PurapWorkflowConstants.HAS_ACCOUNTING_LINES)) return !isMissingAccountingLines();
      if (nodeName.equals(PurapWorkflowConstants.AMOUNT_REQUIRES_SEPARATION_OF_DUTIES_REVIEW_SPLIT)) return isSeparationOfDutiesReviewRequired();
      if (nodeName.equals(PurapWorkflowConstants.HAS_NEW_UNORDERED_ITEMS)) return isNewUnorderedItem();
      if (nodeName.equals(PurapWorkflowConstants.CONTRACT_MANAGEMENT_REVIEW_REQUIRED )) return isContractManagementReviewRequired();
      if (nodeName.equals(PurapWorkflowConstants.BUDGET_REVIEW_REQUIRED )) return isContractManagementReviewRequired();
      if (nodeName.equals(PurapWorkflowConstants.VENDOR_IS_EMPLOYEE_OR_NONRESIDENT)) return isVendorEmployeeOrNonresident();
      throw new UnsupportedOperationException("Cannot answer split question for this node you call \""+nodeName+"\"");
  } 

    protected boolean isMissingAccountingLines() {
        for (final Iterator iterator = getItems().iterator(); iterator.hasNext();) {
            final PurchaseOrderItem item = (PurchaseOrderItem) iterator.next();
            if (item.isConsideredEntered() && item.isAccountListEmpty()) {
                return true;
            }
        }
        
        return false;
    }

    protected boolean isSeparationOfDutiesReviewRequired() {
        try {
        	Set<Person> priorApprovers = getAllPriorApprovers();
            // if there are more than 0 prior approvers which means there had been at least another approver than the current approver
            // then no need for separation of duties
            if (priorApprovers.size() > 0) {
                return false;
            }
        }catch (WorkflowException we) {
            LOG.error("Exception while attempting to retrieve all prior approvers from workflow: " + we);
        }
        final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        final KualiDecimal maxAllowedAmount = new KualiDecimal(parameterService.getParameterValueAsString(RequisitionDocument.class, PurapParameterConstants.SEPARATION_OF_DUTIES_DOLLAR_AMOUNT));
        // if app param amount is greater than or equal to documentTotalAmount... no need for separation of duties
        final KualiDecimal totalAmount = getDocumentHeader().getFinancialDocumentTotalAmount();
        if (ObjectUtils.isNotNull(maxAllowedAmount) && ObjectUtils.isNotNull(totalAmount) && (maxAllowedAmount.compareTo(totalAmount) >= 0)) {
            return false;
        }
        else {
            return true;
        }
    }


    public Set<Person> getAllPriorApprovers() throws WorkflowException {
        final PersonService personService = KimApiServiceLocator.getPersonService();
         List<ActionTaken> actionsTaken = this.getDocumentHeader().getWorkflowDocument().getActionsTaken();
        final Set<String> principalIds = new HashSet<String>();
        final Set<Person> persons = new HashSet<Person>();

        for (final ActionTaken actionTaken : actionsTaken) {
            if (KewApiConstants.ACTION_TAKEN_APPROVED_CD.equals(actionTaken.getActionTaken())) {
                final String principalId = actionTaken.getPrincipalId();
                if (!principalIds.contains(principalId)) {
                    principalIds.add(principalId);
                    persons.add(personService.getPerson(principalId));
                }
            }
        }
        return persons;
    }

    // KFSUPGRADE-339
    protected boolean isContractManagementReviewRequired() {
        final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        final String apoLimitParameterValue = parameterService.getParameterValueAsString(
                PurapConstants.PURAP_NAMESPACE,
                PurapParameterConstants.Components.PURCHASE_ORDER,
                PurapParameterConstants.APO_LIMIT
        );
        final KualiDecimal automaticPurchaseOrderDefaultLimit = new KualiDecimal(apoLimitParameterValue);

        return ((ObjectUtils.isNull(automaticPurchaseOrderDefaultLimit)) || (automaticPurchaseOrderDefaultLimit.compareTo(this.getTotalDollarAmount()) < 0));
    }

    public PaymentTermType getVendorPaymentTerms() {
        if (ObjectUtils.isNull(vendorPaymentTerms) || ObjectUtils.isNull(vendorPaymentTerms.getVendorPaymentTermsCode())) {
            this.refreshReferenceObject("vendorPaymentTerms");
        }
        return vendorPaymentTerms;
    }

    protected boolean shouldAdhocFyi() {
        Collection<String> excludeList = new ArrayList<String>();
        if (SpringContext.getBean(ParameterService.class).parameterExists(PurchaseOrderDocument.class, PurapParameterConstants.PO_NOTIFY_EXCLUSIONS)) {
            excludeList = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(PurchaseOrderDocument.class, PurapParameterConstants.PO_NOTIFY_EXCLUSIONS);
        }

     // CU want to exclude B2B for all FYI
        if ((getDocumentHeader().getWorkflowDocument().isFinal() || getDocumentHeader().getWorkflowDocument().isDisapproved()) && !excludeList.contains(getRequisitionSourceCode()) ) {
            return true;
        }
        return false;
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);
        if (getDocumentHeader().getWorkflowDocument().isDisapproved()) {
            final String nodeName = SpringContext.getBean(WorkflowDocumentService.class).getCurrentRouteLevelName(this.getDocumentHeader().getWorkflowDocument());
            final String disapprovalStatus = PurchaseOrderStatuses.getPurchaseOrderAppDocDisapproveStatuses().get(nodeName);
            updateAndSaveAppDocStatus(disapprovalStatus);
        }
    }

}
