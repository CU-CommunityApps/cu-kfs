package edu.cornell.kfs.fp.document.validation.impl;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherAccountingLineTotalsValidation;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.gl.service.ScheduledAccountingLineService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class CuDisbursementVoucherAccountingLineTotalsValidation extends DisbursementVoucherAccountingLineTotalsValidation {
	private static final Logger LOG = LogManager.getLogger();
	protected transient ScheduledAccountingLineService scheduledAccountingLineService;

    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        LOG.debug("validate start");

        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) event.getDocument();


        final Person financialSystemUser = GlobalVariables.getUserSession().getPerson();
        final Set<String> currentEditModes = getEditModesFromDocument(dvDocument, financialSystemUser);

        // amounts can only decrease
        final List<String> candidateEditModes = getCandidateEditModes();
        if (hasRequiredEditMode(currentEditModes, candidateEditModes)) {

            //users in foreign or wire workgroup can increase or decrease amounts because of currency conversion
            final List<String> foreignDraftAndWireTransferEditModes = getForeignDraftAndWireTransferEditModes(dvDocument);
            if (!hasRequiredEditMode(currentEditModes, foreignDraftAndWireTransferEditModes)) {
                final DisbursementVoucherDocument persistedDocument = (DisbursementVoucherDocument) retrievePersistedDocument(dvDocument);
                if (persistedDocument == null) {
                    handleNonExistentDocumentWhenApproving(dvDocument);
                    return true;
                }
                // KFSMI- 5183
                if (persistedDocument.getDocumentHeader().getWorkflowDocument().isSaved() && persistedDocument
                        .getDisbVchrCheckTotalAmount().isZero()) {
                    return true;
                }

                // check total cannot decrease
                if (!persistedDocument.getDocumentHeader().getWorkflowDocument()
                        .isCompletionRequested() && !persistedDocument.getDisbVchrCheckTotalAmount().equals(
                        dvDocument.getDisbVchrCheckTotalAmount())) {
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
            if(!isAccountingLineEndDateValid(recurringDV)) {
                return false;
            }
        }

       return super.validate(event);
    }
    
    private boolean doesAccountingLineTotalEqualDVTotalDollarAmount(final RecurringDisbursementVoucherDocument recurringDV) {
        KualiDecimal calculatedTotal = KualiDecimal.ZERO;
        for (final Object accountingLine : recurringDV.getSourceAccountingLines()) {
            final ScheduledSourceAccountingLine scheduledAccountingLine = (ScheduledSourceAccountingLine)accountingLine;	
            calculatedTotal = calculatedTotal.add(scheduledAccountingLine.getAmount());
        }
        return  calculatedTotal.equals(recurringDV.getTotalDollarAmount());
    }

    private boolean isAccountingLineEndDateValid(final RecurringDisbursementVoucherDocument recurringDV) {
        boolean valid = true;
        int counter = 0;
        final Date maximumScheduledAccountingLineEndDate = getScheduledAccountingLineService().getMaximumScheduledAccountingLineEndDate();
        final SimpleDateFormat dateFormater = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
        for (final Object accountingLine : recurringDV.getSourceAccountingLines()) {
            final ScheduledSourceAccountingLine scheduledAccountingLine = (ScheduledSourceAccountingLine)accountingLine;
            scheduledAccountingLine.setEndDate(getScheduledAccountingLineService().generateEndDate(scheduledAccountingLine));
            if (maximumScheduledAccountingLineEndDate.before(scheduledAccountingLine.getEndDate())) {
                valid = false;
                GlobalVariables.getMessageMap().putError(buildTransactionCountFieldName(counter), CUKFSKeyConstants.ERROR_RCDV_END_DATE_PASSED_MAX_END_DATE, 
                        dateFormater.format(scheduledAccountingLine.getEndDate()), dateFormater.format(maximumScheduledAccountingLineEndDate));
            }
            counter++;
        }
        return valid;

    }

    private String buildTransactionCountFieldName(final int sourceAccountingLineListElement) {
        return AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX + KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME + 
                KFSConstants.SQUARE_BRACKET_LEFT + sourceAccountingLineListElement + KFSConstants.SQUARE_BRACKET_RIGHT + 
                AccountingDocumentRuleBaseConstants.ERROR_PATH.DELIMITER + CUKFSPropertyConstants.RECURRING_DV_PARTIAL_TRANSACTION_COUNT_FIELD_NAME;
    }

    protected ScheduledAccountingLineService getScheduledAccountingLineService() {
        if (scheduledAccountingLineService == null) {
            scheduledAccountingLineService =  SpringContext.getBean(ScheduledAccountingLineService.class);
        }
        return scheduledAccountingLineService;
    }

    public void setScheduledAccountingLineService(final ScheduledAccountingLineService scheduledAccountingLineService) {
        this.scheduledAccountingLineService = scheduledAccountingLineService;
    }

}
