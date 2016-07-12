package edu.cornell.kfs.fp.document.validation.impl;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.cxf.common.util.StringUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherAccountingLineTotalsValidation;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.event.AttributedSaveDocumentEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.businessobject.ScheduledAccountingLine;
import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuDisbursementVoucherAccountingLineTotalsValidation extends DisbursementVoucherAccountingLineTotalsValidation {

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validate start");
        }

        DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) event.getDocument();


        Person financialSystemUser = GlobalVariables.getUserSession().getPerson();
        final Set<String> currentEditModes = getEditModesFromDocument(dvDocument, financialSystemUser);

        // amounts can only decrease
        List<String> candidateEditModes = this.getCandidateEditModes();
        if (this.hasRequiredEditMode(currentEditModes, candidateEditModes)) {

            //users in foreign or wire workgroup can increase or decrease amounts because of currency conversion
            List<String> foreignDraftAndWireTransferEditModes = this.getForeignDraftAndWireTransferEditModes(dvDocument);
            if (!this.hasRequiredEditMode(currentEditModes, foreignDraftAndWireTransferEditModes)) {
                DisbursementVoucherDocument persistedDocument = (DisbursementVoucherDocument) retrievePersistedDocument(dvDocument);
                if (persistedDocument == null) {
                    handleNonExistentDocumentWhenApproving(dvDocument);
                    return true;
                }
                // KFSMI- 5183
                if (persistedDocument.getDocumentHeader().getWorkflowDocument().isSaved() && persistedDocument.getDisbVchrCheckTotalAmount().isZero()) {
                    return true;
                }

                // check total cannot decrease
                if (!persistedDocument.getDocumentHeader().getWorkflowDocument().isCompletionRequested() && (!persistedDocument.getDisbVchrCheckTotalAmount().equals(dvDocument.getDisbVchrCheckTotalAmount()))) {
                    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.DISB_VCHR_CHECK_TOTAL_AMOUNT, CUKFSKeyConstants.ERROR_DV_CHECK_TOTAL_NO_CHANGE);
                    return false;
                }
            }

            return true;
        }

        // KFSUPGRADE-848 : skip total check here for FO
        final WorkflowDocument workflowDocument = dvDocument.getDocumentHeader().getWorkflowDocument();
        final Set<String> currentRouteLevels = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(currentRouteLevels)) {
            if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.ACCOUNT)) {  
                return true; 
            }
        }
        
        if (dvDocument instanceof RecurringDisbursementVoucherDocument) {
        	RecurringDisbursementVoucherDocument recurringDV = (RecurringDisbursementVoucherDocument) dvDocument;
        	if (!doesAccountingLineTotalEqualDVTotalDollarAmount(recurringDV)) {
        		String propertyName = KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.DISB_VCHR_CHECK_TOTAL_AMOUNT;
        		GlobalVariables.getMessageMap().putError(propertyName, CUKFSKeyConstants.ERROR_DV_CHECK_TOTAL_MUST_EQUAL_ACCOUNTING_LINE_TOTAL);
        		return false;
        	}
        	if(StringUtils.isEmpty(recurringDV.getDisbVchrCheckStubText()) && !recurringDV.getSourceAccountingLines().isEmpty()) {
        		String propertyName = KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.DISB_VCHR_CHECK_STUB_TEXT;
        		GlobalVariables.getMessageMap().putError(propertyName, CUKFSKeyConstants.ERROR_DV_CHECK_STUB_REQUIRED);
        		return false;
        	}
        }

       return super.validate(event);
    }
    
    private boolean doesAccountingLineTotalEqualDVTotalDollarAmount(RecurringDisbursementVoucherDocument recurringDV) {
    	KualiDecimal calculatedTotal = KualiDecimal.ZERO;
    	for (Object accountingLine : recurringDV.getSourceAccountingLines()) {
			ScheduledSourceAccountingLine scheduledAccountingLine = (ScheduledSourceAccountingLine)accountingLine;	
			calculatedTotal = calculatedTotal.add(scheduledAccountingLine.getAmount());
    	}
    	return  calculatedTotal.equals(recurringDV.getTotalDollarAmount());
    }

}
