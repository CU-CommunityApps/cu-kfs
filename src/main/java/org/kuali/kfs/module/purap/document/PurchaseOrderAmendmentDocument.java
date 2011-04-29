/*
 * Copyright 2007 The Kuali Foundation
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

import static org.kuali.kfs.sys.KFSConstants.GL_DEBIT_CODE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.service.SufficientFundsService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurapDocTypeCodes;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.service.ReceivingService;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SufficientFundsItem;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.rule.event.KualiDocumentEvent;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

/**
 * Purchase Order Amendment Document
 */
public class PurchaseOrderAmendmentDocument extends PurchaseOrderDocument {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PurchaseOrderAmendmentDocument.class);

    boolean newUnorderedItem = false; //Used for routing
    String receivingDeliveryCampusCode; //Used for routing
    
    /**
     * Default constructor.
     */
    public PurchaseOrderAmendmentDocument() {
        super();
    }

    /**
     * General Ledger pending entries are not created on save for this document. They are created when the document has been finally
     * processed. Overriding this method so that entries are not created yet.
     * 
     * @see org.kuali.kfs.module.purap.document.PurchaseOrderDocument#prepareForSave(org.kuali.rice.kns.rule.event.KualiDocumentEvent)
     */
    @Override
    public void prepareForSave(KualiDocumentEvent event) {
        LOG.info("prepareForSave(KualiDocumentEvent) do not create gl entries");
        setSourceAccountingLines(new ArrayList());
        setGeneralLedgerPendingEntries(new ArrayList());
        customPrepareForSave(event);
    }

    @Override
    public List<Long> getWorkflowEngineDocumentIdsToLock() {
        return super.getWorkflowEngineDocumentIdsToLock();
    }

    /**
     * When Purchase Order Amendment document has been Processed through Workflow, the general ledger entries are created and the PO
     * status remains "OPEN".
     * 
     * @see org.kuali.kfs.module.purap.document.PurchaseOrderDocument#doRouteStatusChange()
     */
   @Override
	public void doRouteStatusChange(DocumentRouteStatusChangeDTO statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        // DOCUMENT PROCESSED
        if (getDocumentHeader().getWorkflowDocument().stateIsProcessed()) {
            // generate GL entries
            SpringContext.getBean(PurapGeneralLedgerService.class).generateEntriesApproveAmendPurchaseOrder(this);

            // set purap status 
//            SpringContext.getBean(PurapService.class).updateStatus(this, PurapConstants.PurchaseOrderStatuses.OPEN);
            // updated to set status to PENDING_CXML so the document will route to SciQuest for handling
            SpringContext.getBean(PurapService.class).updateStatus(this, PurchaseOrderStatuses.PENDING_CXML);

            // update vendor commodity code by automatically spawning vendor maintenance document
	        SpringContext.getBean(PurchaseOrderService.class).updateVendorCommodityCode(this);
          
            // update indicators
            SpringContext.getBean(PurchaseOrderService.class).completePurchaseOrderAmendment(this);

        }
        // DOCUMENT DISAPPROVED
        else if (getDocumentHeader().getWorkflowDocument().stateIsDisapproved()) {
            SpringContext.getBean(PurchaseOrderService.class).setCurrentAndPendingIndicatorsForDisapprovedChangePODocuments(this);
            SpringContext.getBean(PurapService.class).saveDocumentNoValidation(this);
        }
        // DOCUMENT CANCELED
        else if (getDocumentHeader().getWorkflowDocument().stateIsCanceled()) {
            SpringContext.getBean(PurchaseOrderService.class).setCurrentAndPendingIndicatorsForCancelledChangePODocuments(this);
            SpringContext.getBean(PurapService.class).saveDocumentNoValidation(this);
        }
   }

   /**
    * @see org.kuali.module.purap.rules.PurapAccountingDocumentRuleBase#customizeExplicitGeneralLedgerPendingEntry(org.kuali.kfs.sys.document.AccountingDocument,
    *      org.kuali.kfs.sys.businessobject.AccountingLine, org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry)
    */
   @Override
   public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
       super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);

       SpringContext.getBean(PurapGeneralLedgerService.class).customizeGeneralLedgerPendingEntry(this, (AccountingLine)postable, explicitEntry, getPurapDocumentIdentifier(), GL_DEBIT_CODE, PurapDocTypeCodes.PO_DOCUMENT, true);

       // don't think i should have to override this, but default isn't getting the right PO doc
       explicitEntry.setFinancialDocumentTypeCode(PurapDocTypeCodes.PO_AMENDMENT_DOCUMENT);
       explicitEntry.setFinancialDocumentApprovedCode(KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.APPROVED);
   }

   @Override
   public List<GeneralLedgerPendingEntrySourceDetail> getGeneralLedgerPendingEntrySourceDetails() {
       List<GeneralLedgerPendingEntrySourceDetail> accountingLines = new ArrayList<GeneralLedgerPendingEntrySourceDetail>();
       if (getGlOnlySourceAccountingLines() != null) {
           Iterator iter = getGlOnlySourceAccountingLines().iterator();
           while (iter.hasNext()) {
               accountingLines.add((GeneralLedgerPendingEntrySourceDetail) iter.next());
           }
       }
       return accountingLines;
   }

   
   @Override
   public void populateDocumentForRouting() {
       newUnorderedItem = SpringContext.getBean(PurchaseOrderService.class).hasNewUnorderedItem(this);
       receivingDeliveryCampusCode = SpringContext.getBean(ReceivingService.class).getReceivingDeliveryCampusCode(this);
       super.populateDocumentForRouting();
   }

    public boolean isNewUnorderedItem() {
    	return newUnorderedItem;
    }
    
    public void setNewUnorderedItem(boolean newUnorderedItem) {
        this.newUnorderedItem = newUnorderedItem;
    }

    public String getReceivingDeliveryCampusCode() {
        return receivingDeliveryCampusCode;
    }

    public void setReceivingDeliveryCampusCode(String receivingDeliveryCampusCode) {
        this.receivingDeliveryCampusCode = receivingDeliveryCampusCode;
    }
    
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
//        if (nodeName.equals("ReplicateRequisitionRouting")) return 
//
//		I think the "isMissingAccountingLines" method will cause document to route to account node
//		where fiscal officer needs to add accounting lines    	
//    	
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
        KualiDecimal automaticPurchaseOrderDefaultLimit = new KualiDecimal(parameterService.getParameterValue(RequisitionDocument.class, PurapParameterConstants.AUTOMATIC_PURCHASE_ORDER_DEFAULT_LIMIT_AMOUNT));
        return ((ObjectUtils.isNull(automaticPurchaseOrderDefaultLimit)) || (automaticPurchaseOrderDefaultLimit.compareTo(this.getTotalDollarAmount()) < 0));
    }

    
    protected boolean isSeparationOfDutiesReviewRequired() {
        try {
            Set<Person> priorApprovers = getDocumentHeader().getWorkflowDocument().getAllPriorApprovers();
            // if there are more than 0 prior approvers which means there had been at least another approver than the current approver
            // then no need for separation of duties
            if (priorApprovers.size() > 0) {
                return false;
            }
        }catch (WorkflowException we) {
            LOG.error("Exception while attempting to retrieve all prior approvers from workflow: " + we);
        }
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        KualiDecimal maxAllowedAmount = new KualiDecimal(parameterService.getParameterValue(RequisitionDocument.class, PurapParameterConstants.SEPARATION_OF_DUTIES_DOLLAR_AMOUNT));
        // if app param amount is greater than or equal to documentTotalAmount... no need for separation of duties
        KualiDecimal totalAmount = documentHeader.getFinancialDocumentTotalAmount();
        if (ObjectUtils.isNotNull(maxAllowedAmount) && ObjectUtils.isNotNull(totalAmount) && (maxAllowedAmount.compareTo(totalAmount) >= 0)) {
            return false;
        }
        else {
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

}
