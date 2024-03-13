package edu.cornell.kfs.fp.service.impl;

import java.io.File;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.util.KfsDateUtils;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.RecurringDisbursementVoucherDocumentRoutingReportItem;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherDetail;
import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherPDPStatus;
import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.fp.dataaccess.RecurringDisbursementVoucherSearchDao;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherExtractionHelperService;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentReportService;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentRoutingService;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherPaymentMaintenanceService;
import edu.cornell.kfs.gl.service.ScheduledAccountingLineService;
import edu.emory.mathcs.backport.java.util.Collections;

public class RecurringDisbursementVoucherDocumentServiceImpl implements RecurringDisbursementVoucherDocumentService, Serializable {

    private static final long serialVersionUID = -1775346783004136197L;
    private static final Logger LOG = LogManager.getLogger(RecurringDisbursementVoucherDocumentServiceImpl.class);
    protected DataDictionaryService dataDictionaryService;
    protected DocumentService documentService;
    protected ScheduledAccountingLineService scheduledAccountingLineService;
    protected BusinessObjectService businessObjectService;
    protected RecurringDisbursementVoucherPaymentMaintenanceService recurringDisbursementVoucherPaymentMaintenanceService;
    protected AccountingPeriodService accountingPeriodService;
    protected RecurringDisbursementVoucherSearchDao recurringDisbursementVoucherSearchDao;
    protected PersonService personService;
    protected RouteHeaderService routeHeaderService;
    protected CuDisbursementVoucherExtractionHelperService cuDisbursementVoucherExtractionHelperService;
    protected NoteService noteService;
    protected RecurringDisbursementVoucherDocumentRoutingService recurringDisbursementVoucherDocumentRoutingService;
    protected RecurringDisbursementVoucherDocumentReportService recurringDisbursementVoucherDocumentReportService;

    @Override
    public void updateRecurringDisbursementVoucherDetails(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument){
        if (recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails() == null ||
                recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails().isEmpty()){
            recurringDisbursementVoucherDocument.setRecurringDisbursementVoucherDetails(new ArrayList<RecurringDisbursementVoucherDetail>());
        } else {
            resetAmounts(recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails());
        }
        int rowId = 0;
        for (Object accountingLine : recurringDisbursementVoucherDocument.getSourceAccountingLines()) {
            ScheduledSourceAccountingLine scheduledAccountingLine = (ScheduledSourceAccountingLine)accountingLine;
            TreeMap<Date, KualiDecimal> datesAndAmounts = getScheduledAccountingLineService().generateDatesAndAmounts(scheduledAccountingLine, rowId);

            for (Date date : datesAndAmounts.keySet()){
                KualiDecimal amount = datesAndAmounts.get(date);
                RecurringDisbursementVoucherDetail detail = getDetailtem(recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails(), date);
                detail.setDvCheckAmount(amount.add(detail.getDvCheckAmount()));
                detail.setRecurringDVDocumentNumber(recurringDisbursementVoucherDocument.getDocumentNumber());
                if (StringUtils.isEmpty(detail.getDvCheckStub())) {
                    detail.setDvCheckStub(recurringDisbursementVoucherDocument.getDisbVchrCheckStubText());
                }
            }
            rowId++;
        }
        recurringDisbursementVoucherDocument.setRecurringDisbursementVoucherDetails(
                removeZeroAmounts(recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails()));
        Collections.sort(recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails());
    }

    private void resetAmounts(List<RecurringDisbursementVoucherDetail> details) {
        for (RecurringDisbursementVoucherDetail detail : details) {
            detail.setDvCheckAmount(KualiDecimal.ZERO);
        }
    }

    private List<RecurringDisbursementVoucherDetail> removeZeroAmounts(List<RecurringDisbursementVoucherDetail> oldDetails) {
        List<RecurringDisbursementVoucherDetail> newDetails = new ArrayList<RecurringDisbursementVoucherDetail>();
        for (RecurringDisbursementVoucherDetail detail : oldDetails) {
            if (!detail.getDvCheckAmount().equals(KualiDecimal.ZERO)) {
                newDetails.add(detail);
            } else {
                getBusinessObjectService().delete(detail);
            }
        }
        return newDetails;
    }

    private RecurringDisbursementVoucherDetail getDetailtem(List<RecurringDisbursementVoucherDetail> details, Date date) {
        for (RecurringDisbursementVoucherDetail detail : details) {
            if (detail.getDvCheckDate().equals(date)) {
                return detail;
            }
        }
        RecurringDisbursementVoucherDetail detail = new RecurringDisbursementVoucherDetail(date, KualiDecimal.ZERO, "");
        details.add(detail);
        return detail;
    }

    @Override
    public List<DisbursementVoucherDocument> generateDisbursementDocumentsFromRecurringDV(RecurringDisbursementVoucherDocument recurringDV) throws WorkflowException {
        List<DisbursementVoucherDocument> generatedDVs = new ArrayList<DisbursementVoucherDocument>();
        int rowId = 0;
        for (Object accountingLine : recurringDV.getSourceAccountingLines()) {
            ScheduledSourceAccountingLine scheduledAccountingLine = (ScheduledSourceAccountingLine)accountingLine;
            TreeMap<Date, KualiDecimal> datesAndAmounts = getScheduledAccountingLineService().generateDatesAndAmounts(scheduledAccountingLine, rowId);

            for (Date date : datesAndAmounts.keySet()){
                KualiDecimal amount = datesAndAmounts.get(date);

                CuDisbursementVoucherDocument disbursementVoucherDocument = getCuDisbursementVoucherDocument(generatedDVs, recurringDV, date);
                disbursementVoucherDocument.setDisbVchrCheckTotalAmount(calculateDVCheckAmount(amount, disbursementVoucherDocument));

                RecurringDisbursementVoucherDetail dvDetail = getDetailtem(recurringDV.getRecurringDisbursementVoucherDetails(), date);
                dvDetail.setDvDocumentNumber(disbursementVoucherDocument.getDocumentNumber());
                disbursementVoucherDocument.setDisbVchrCheckStubText(dvDetail.getDvCheckStub());

                SourceAccountingLine line = buildSourceAccountingLine(scheduledAccountingLine);
                line.setAmount(amount);
                disbursementVoucherDocument.addSourceAccountingLine(line);

            }
            rowId++;
        }
        saveDisbursementVouchers(generatedDVs, recurringDV);
        return generatedDVs;
    }

    private CuDisbursementVoucherDocument getCuDisbursementVoucherDocument(List<DisbursementVoucherDocument> generatedDVs,
            RecurringDisbursementVoucherDocument recurringDV, Date dvCheckDate) throws WorkflowException {
        for (DisbursementVoucherDocument dv : generatedDVs) {
            if (dv.getDisbursementVoucherDueDate().equals(dvCheckDate)) {
                return (CuDisbursementVoucherDocument)dv;
            }
        }
        CuDisbursementVoucherDocument dv =  buildCuDisbursementVoucherDocument(recurringDV);
        dv.setDisbursementVoucherDueDate(dvCheckDate);
        dv.setDisbursementVoucherCheckDate(new Timestamp(dvCheckDate.getTime()));
        generatedDVs.add(dv);
        return dv;
    }

    private KualiDecimal calculateDVCheckAmount(KualiDecimal amount, CuDisbursementVoucherDocument disbursementVoucherDocument) {
        KualiDecimal calculatedAmount;
        if (disbursementVoucherDocument.getDisbVchrCheckTotalAmount() == null || disbursementVoucherDocument.getDisbVchrCheckTotalAmount().isZero()) {
            calculatedAmount = amount;
        } else {
            calculatedAmount = amount.add(disbursementVoucherDocument.getDisbVchrCheckTotalAmount());
        }
        return calculatedAmount;
    }

    private CuDisbursementVoucherDocument buildCuDisbursementVoucherDocument(RecurringDisbursementVoucherDocument recurringDV) throws WorkflowException {
        CuDisbursementVoucherDocument dv = (CuDisbursementVoucherDocument) getDocumentService().getNewDocument(CuDisbursementVoucherDocument.class);

        dv.setDisbVchrContactPersonName(recurringDV.getDisbVchrContactPersonName());
        dv.setDisbVchrContactPhoneNumber(recurringDV.getDisbVchrContactPhoneNumber());
        dv.setDisbVchrContactEmailId(recurringDV.getDisbVchrContactEmailId());
        dv.setDisbVchrAttachmentCode(recurringDV.isDisbVchrAttachmentCode());
        dv.setDisbVchrSpecialHandlingCode(recurringDV.isDisbVchrSpecialHandlingCode());
        dv.setDisbVchrForeignCurrencyInd(recurringDV.isDisbVchrForeignCurrencyInd());
        dv.setDisbursementVoucherDocumentationLocationCode(recurringDV.getDisbursementVoucherDocumentationLocationCode());
        dv.setDisbVchrCheckStubText(recurringDV.getDisbVchrCheckStubText());
        dv.setDvCheckStubOverflowCode(recurringDV.getDvCheckStubOverflowCode());
        dv.setCampusCode(recurringDV.getCampusCode());
        dv.setDisbVchrPayeeTaxControlCode(recurringDV.getDisbVchrPayeeTaxControlCode());
        dv.setDisbVchrPayeeChangedInd(recurringDV.isDisbVchrPayeeChangedInd());
        dv.setExceptionIndicator(recurringDV.isExceptionIndicator());
        dv.setDisbursementVoucherPdpStatus(recurringDV.getDisbursementVoucherPdpStatus());
        dv.setDisbVchrBankCode(recurringDV.getDisbVchrBankCode());
        dv.setPayeeAssigned(recurringDV.isPayeeAssigned());
        dv.setEditW9W8BENbox(recurringDV.isEditW9W8BENbox());
        dv.setDisbVchrPdpBankCode(recurringDV.getDisbVchrPdpBankCode());
        dv.setImmediatePaymentIndicator(recurringDV.isImmediatePaymentIndicator());
        dv.setDisbExcptAttachedIndicator(recurringDV.isDisbExcptAttachedIndicator());
        dv.setDisbVchrPayeeW9CompleteCode(recurringDV.getDisbVchrPayeeW9CompleteCode());
        dv.setDisbVchrPaymentMethodCode(recurringDV.getDisbVchrPaymentMethodCode());

        CuDisbursementVoucherPayeeDetail payeeDetail = (CuDisbursementVoucherPayeeDetail) ObjectUtils.deepCopy(recurringDV.getDvPayeeDetail());
        payeeDetail.setDocumentNumber(dv.getDocumentNumber());
        dv.setDvPayeeDetail(payeeDetail);

        PaymentSourceWireTransfer wireTransfer = (PaymentSourceWireTransfer) ObjectUtils.deepCopy(recurringDV.getWireTransfer());
        wireTransfer.setDocumentNumber(dv.getDocumentNumber());
        dv.setWireTransfer(wireTransfer);

        DisbursementVoucherNonEmployeeTravel nonEmployeeTravel = (DisbursementVoucherNonEmployeeTravel) ObjectUtils.deepCopy(recurringDV.getDvNonEmployeeTravel());
        nonEmployeeTravel.setDocumentNumber(dv.getDocumentNumber());
        dv.setDvNonEmployeeTravel(nonEmployeeTravel);

        DisbursementVoucherNonresidentTax nonresidentTax = (DisbursementVoucherNonresidentTax) ObjectUtils.deepCopy(recurringDV.getDvNonresidentTax());
        nonresidentTax.setDocumentNumber(dv.getDocumentNumber());
        dv.setDvNonresidentTax(nonresidentTax);

        DisbursementVoucherPreConferenceDetail conferenceDetail = (DisbursementVoucherPreConferenceDetail) ObjectUtils.deepCopy(recurringDV.getDvPreConferenceDetail());
        conferenceDetail.setDocumentNumber(dv.getDocumentNumber());
        dv.setDvPreConferenceDetail(conferenceDetail);

        return dv;
    }

    private SourceAccountingLine buildSourceAccountingLine(ScheduledSourceAccountingLine scheduledAccountingLine) {
        SourceAccountingLine line = new SourceAccountingLine();
        line.setAccountExpiredOverride(scheduledAccountingLine.getAccountExpiredOverride());
        line.setAccountExpiredOverrideNeeded(scheduledAccountingLine.getAccountExpiredOverrideNeeded());
        line.setAccountNumber(scheduledAccountingLine.getAccountNumber());
        line.setChartOfAccountsCode(scheduledAccountingLine.getChartOfAccountsCode());
        line.setDebitCreditCode(scheduledAccountingLine.getDebitCreditCode());
        line.setEncumbranceUpdateCode(scheduledAccountingLine.getEncumbranceUpdateCode());
        line.setFinancialDocumentLineDescription(scheduledAccountingLine.getFinancialDocumentLineDescription());
        line.setFinancialDocumentLineTypeCode(scheduledAccountingLine.getFinancialDocumentLineTypeCode());
        line.setFinancialObjectCode(scheduledAccountingLine.getFinancialObjectCode());
        line.setFinancialSubObjectCode(scheduledAccountingLine.getFinancialSubObjectCode());
        line.setNonFringeAccountOverride(scheduledAccountingLine.getNonFringeAccountOverride());
        line.setNonFringeAccountOverrideNeeded(scheduledAccountingLine.getNonFringeAccountOverrideNeeded());
        line.setOrganizationReferenceId(scheduledAccountingLine.getOrganizationReferenceId());
        line.setPostingYear(scheduledAccountingLine.getPostingYear());
        line.setProjectCode(scheduledAccountingLine.getProjectCode());
        line.setReferenceNumber(scheduledAccountingLine.getReferenceNumber());
        line.setReferenceTypeCode(scheduledAccountingLine.getReferenceTypeCode());
        line.setSalesTaxRequired(scheduledAccountingLine.isSalesTaxRequired());
        line.setSubAccountNumber(scheduledAccountingLine.getSubAccountNumber());
        return line;
    }

    private void saveDisbursementVouchers(List<DisbursementVoucherDocument> dvs, RecurringDisbursementVoucherDocument recurringDV){
        for (DisbursementVoucherDocument dv : dvs) {
            dv.getDocumentHeader().setDocumentDescription(recurringDV.getDocumentHeader().getDocumentDescription());
            dv.getDocumentHeader().setExplanation(buildDVExplanation(recurringDV));
            getDocumentService().saveDocument(dv);
            getBusinessObjectService().save(recurringDV.getRecurringDisbursementVoucherDetails());
            updateGLPEDatesAndAddRecurringDocumentLinks(dv, recurringDV.getDocumentNumber());
        }
    }
    
    private String buildDVExplanation(RecurringDisbursementVoucherDocument recurringDV) {
        StringBuilder dvExplanation = new StringBuilder();
        dvExplanation.append(CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_EXPLANATION_TO_DV_NOTE_STARTER).append(recurringDV.getDocumentNumber());
        String rcdvExplanation = recurringDV.getDocumentHeader().getExplanation();
        
        if(StringUtils.isNotBlank(rcdvExplanation)) {
            dvExplanation.append(KFSConstants.DELIMITER).append(KFSConstants.BLANK_SPACE).append(rcdvExplanation);
        }

        return truncateExplanationToMaxLength(dvExplanation.toString());
    }
    
    private String truncateExplanationToMaxLength(String dvExplanation) {
        String truncatedExplanation = dvExplanation;
        int explanationMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(), KFSPropertyConstants.EXPLANATION);

        if (dvExplanation.length() > explanationMaxLength) {
            truncatedExplanation = StringUtils.left(dvExplanation, explanationMaxLength);
        }
        
        return truncatedExplanation;
    }

    private void updateGLPEDatesAndAddRecurringDocumentLinks(DisbursementVoucherDocument dv, String recurringDisbursemntVoucherDocumentNumber) {
        for (GeneralLedgerPendingEntry glpe : dv.getGeneralLedgerPendingEntries()) {
            glpe.setTransactionDate(dv.getDisbursementVoucherDueDate());
            glpe.setReferenceFinancialDocumentTypeCode(CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_DOCUMENT_TYPE_NAME);
            glpe.setReferenceFinancialDocumentNumber(recurringDisbursemntVoucherDocumentNumber);

            AccountingPeriod ap = getAccountingPeriodService().getByDate(dv.getDisbursementVoucherDueDate());
            glpe.setAccountingPeriod(ap);
            glpe.setUniversityFiscalPeriodCode(ap.getUniversityFiscalPeriodCode());
            glpe.setUniversityFiscalYear(ap.getUniversityFiscalYear());

            getBusinessObjectService().save(glpe);
        }
    }

    @Override
    public List<RecurringDisbursementVoucherPDPStatus> findPdpStatuses(RecurringDisbursementVoucherDocument recurringDV) {
        List<RecurringDisbursementVoucherPDPStatus> pdpStatuses = new ArrayList<RecurringDisbursementVoucherPDPStatus>();
        for (RecurringDisbursementVoucherDetail detail : recurringDV.getRecurringDisbursementVoucherDetails()) {
            if (StringUtils.isNotEmpty(detail.getDvDocumentNumber())){
                DisbursementVoucherDocument disbursementVoucherDocument;
                disbursementVoucherDocument = (DisbursementVoucherDocument) getDocumentService().getByDocumentHeaderId(detail.getDvDocumentNumber());
                pdpStatuses.add(buildRecurringDisbursementVoucherPDPStatus(disbursementVoucherDocument));
            }
        }
        Collections.sort(pdpStatuses);
        return pdpStatuses;
    }

    private RecurringDisbursementVoucherPDPStatus buildRecurringDisbursementVoucherPDPStatus(DisbursementVoucherDocument disbursementVoucherDocument) {
        RecurringDisbursementVoucherPDPStatus pdpStatus = new RecurringDisbursementVoucherPDPStatus();
        pdpStatus.setDocumentNumber(disbursementVoucherDocument.getDocumentNumber());
        pdpStatus.setCancelDate(disbursementVoucherDocument.getCancelDate());
        pdpStatus.setExtractDate(disbursementVoucherDocument.getExtractDate());
        pdpStatus.setPaidDate(disbursementVoucherDocument.getPaidDate());
        pdpStatus.setPdpStatus(disbursementVoucherDocument.getDisbursementVoucherPdpStatus());
        pdpStatus.setPaymentDetailDocumentType(disbursementVoucherDocument.getPaymentDetailDocumentType());
        pdpStatus.setDueDate(disbursementVoucherDocument.getDisbursementVoucherDueDate());
        pdpStatus.setDvStatus(disbursementVoucherDocument.getDocumentHeader().getWorkflowDocument().getStatus().getLabel());
        return pdpStatus;
    }

    @Override
    public Set<String> cancelOpenPDPPayments(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument, String cancelMessage) {
        Collection<PaymentDetail> paymentDetails = findPaymentDetailsFromRecurringDisbursementVoucher(recurringDisbursementVoucherDocument);
        Set<String> canceledPaymentGroups = new HashSet<String>();
        Person loggedINPerson = GlobalVariables.getUserSession().getPerson();
        for (PaymentDetail detail : paymentDetails) {
            String paymentDetailStatus = detail.getPaymentGroup().getPaymentStatusCode();
            if (isPaymentCancelableByLoggedInUser(paymentDetailStatus)) {
                LOG.debug("cancelPDPPayments: About to Cancel " + detail.getId() + " for a reason of " + cancelMessage);
                int paymentGroupId = detail.getPaymentGroupId().intValue();
                int paymentDetailId = detail.getId().intValue();
                boolean canceledSuccessfully = getRecurringDisbursementVoucherPaymentMaintenanceService().cancelPendingPayment(
                        paymentGroupId, paymentDetailId, cancelMessage, loggedINPerson);
                if (canceledSuccessfully) {
                    canceledPaymentGroups.add(detail.getPaymentGroupId().toString());
                } else {
                    throw new RuntimeException("cancelPDPPayments: unable to cancel payment Payment Detail "+ detail.getId());
                }
            } else {
                LOG.debug("cancelPDPPayments: Not going to cancel Payment Detail "+ detail.getId());
            }
        }
        noteChangeOnRecurringDV(recurringDisbursementVoucherDocument, "The following payments were canceled: ", canceledPaymentGroups);
        return canceledPaymentGroups;
    }

    @Override
    public Collection<PaymentDetail> findPaymentDetailsFromRecurringDisbursementVoucher(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument) {
        Collection<PaymentDetail> paymentDetails = new ArrayList<PaymentDetail>();
        List<RecurringDisbursementVoucherPDPStatus> statuses = findPdpStatuses(recurringDisbursementVoucherDocument);
        for (RecurringDisbursementVoucherPDPStatus status : statuses) {
            Map fieldValues = new HashMap();
            fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_CUSTOMER_DOC_NUMBER, status.getDocumentNumber());
            paymentDetails.addAll(getBusinessObjectService().findMatching(PaymentDetail.class, fieldValues));
        }
        return paymentDetails;
    }

    @Override
    public boolean isPaymentCancelableByLoggedInUser(String paymentDetailStatus) {
        return isPaymentDetailInCancelableStatus(paymentDetailStatus) && hasCancelPermission();
    }

    private boolean isPaymentDetailInCancelableStatus(String paymentDetailStatus) {
        return StringUtils.equals(PdpConstants.PaymentStatusCodes.OPEN, paymentDetailStatus)
                || StringUtils.equals(PdpConstants.PaymentStatusCodes.HELD_CD, paymentDetailStatus)
                || StringUtils.equals(PdpConstants.PaymentStatusCodes.HELD_TAX_EMPLOYEE_CD, paymentDetailStatus)
                || StringUtils.equals(PdpConstants.PaymentStatusCodes.HELD_TAX_NONRESIDENT_CD, paymentDetailStatus)
                || StringUtils.equals(PdpConstants.PaymentStatusCodes.HELD_TAX_NONRESIDENT_EMPL_CD, paymentDetailStatus);
    }

    private void noteChangeOnRecurringDV(RecurringDisbursementVoucherDocument recurringDV, String noteText, Set<String> setOfStrings) {
        if (!setOfStrings.isEmpty()) {
            Note note = buildNoteBase();
            note.setNoteText(noteText + StringUtils.join(setOfStrings, ", "));;
            recurringDV.addNote(note);
            getDocumentService().saveDocument(recurringDV);
        }
    }

    private Note buildNoteBase() {
        Note noteBase = new Note();
        noteBase.setAuthorUniversalIdentifier(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        noteBase.setNotePostedTimestampToCurrent();
        return noteBase;
    }

    @Override
    public void autoApproveDisbursementVouchersSpawnedByRecurringDvs() {
        LOG.info("autoApproveDisbursementVouchersSpawnedByRecurringDvs: Entered.");
        int approvalCount = 0;
        int errorCount = 0;
        List<String> dvDocIdsCausingError = new ArrayList<>();
        Collection<String> dvDocIds = getRecurringDisbursementVoucherSearchDao().findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods(getCurrentFiscalPeriodEndDate());
        List<RecurringDisbursementVoucherDocumentRoutingReportItem> reportItems = new ArrayList<>(
                CollectionUtils.size(dvDocIds));
        if (CollectionUtils.isNotEmpty(dvDocIds)) {
            for (String dvDocId : dvDocIds) {
                LOG.info("autoApproveDisbursementVouchersSpawnedByRecurringDvs: Processing start for DV ID:: {}", dvDocId);
                RecurringDisbursementVoucherDocumentRoutingReportItem reportItem =
                        recurringDisbursementVoucherDocumentRoutingService
                                .autoApproveSpawnedDisbursementVoucher(dvDocId);
                reportItems.add(reportItem);
                if (reportItem.hasErrors()) {
                    errorCount++;
                    dvDocIdsCausingError.add(dvDocId);
                } else {
                    approvalCount++;
                }
            }
        } else {
            LOG.info("autoApproveDisbursementVouchersSpawnedByRecurringDvs: No DV's spwned by Recurring DV found. Nothing will be auto approved. ");
        }

        LOG.info("autoApproveDisbursementVouchersSpawnedByRecurringDvs: **************** Batch Job Processing Results ****************");
        LOG.info("autoApproveDisbursementVouchersSpawnedByRecurringDvs: Number of Disbursement Vouchers that generated Errors:: {}", errorCount);
        for (String dvDocIdCausingError : dvDocIdsCausingError) {
            LOG.info("autoApproveDisbursementVouchersSpawnedByRecurringDvs: Erroring {}", dvDocIdCausingError);
        }
        LOG.info("autoApproveDisbursementVouchersSpawnedByRecurringDvs: Number of Disbursement Vouchers successfully Blanket Approved (fully processed):: {}", approvalCount);

        if (errorCount > 0) {
            File reportFile = recurringDisbursementVoucherDocumentReportService
                    .buildDvAutoApproveErrorReport(reportItems);
            recurringDisbursementVoucherDocumentReportService.sendDvAutoApproveErrorReportEmail(reportFile);
        }
        LOG.info("autoApproveDisbursementVouchersSpawnedByRecurringDvs: Leaving.");
    }

    private Date getCurrentFiscalPeriodEndDate() {
        Date today = KfsDateUtils.convertToSqlDate(new Date(Calendar.getInstance().getTimeInMillis()));
        AccountingPeriod currentPeriod = getAccountingPeriodService().getByDate(today);
        if (currentPeriod == null) {
            return null;
        }
        else {
            return currentPeriod.getUniversityFiscalPeriodEndDate();
        }
    }

    @Override
    public Set<String> cancelSavedDisbursementVouchers(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument, final String cancelMessage) {
        Set<String> canceledDVs = new HashSet<String>();
        for (RecurringDisbursementVoucherDetail detail : recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails()) {
            if (isDVCancelable(detail.getDvDocumentNumber())) {
                cancelDVAsSystemUser(cancelMessage, detail.getDvDocumentNumber());
                canceledDVs.add(detail.getDvDocumentNumber());
            }
        }
        noteChangeOnRecurringDV(recurringDisbursementVoucherDocument, "The following disbursement vouchers were canceled: ", canceledDVs);
        return canceledDVs;
    }

    private boolean isDVCancelable(String dvDocumentNumber) {
        String dvStatus = getRouteHeaderService().getDocumentStatus(dvDocumentNumber);
        return hasCancelPermission() && StringUtils.equalsIgnoreCase(dvStatus, DocumentStatus.SAVED.getCode());
    }

    private boolean hasCancelPermission() {
        return getRecurringDisbursementVoucherPaymentMaintenanceService().hasCancelPermission(GlobalVariables.getUserSession().getPerson());
    }

    private void cancelDVAsSystemUser(final String cancelMessage, final String dvDocumentNumber) {
        try {
            GlobalVariables.doInNewGlobalVariables(new UserSession(KFSConstants.SYSTEM_USER), new Callable<Object>() {
                @Override
                public Object call() throws WorkflowException {
                    CuDisbursementVoucherDocument disbursementVoucher = (CuDisbursementVoucherDocument) getDocumentService().getByDocumentHeaderId(dvDocumentNumber);

                    Note note = buildNoteBase();
                    note.setNoteText("This DV was canceled from the recurring disbursement voucher that created it.");
                    String usersPrincipleID = GlobalVariables.getUserSession().getPrincipalId();
                    note = getNoteService().createNote(note, disbursementVoucher.getDocumentHeader(), usersPrincipleID);
                    getNoteService().save(note);

                    getDocumentService().cancelDocument(disbursementVoucher, cancelMessage);
                    return null;

                }
            });
        } catch (Exception e) {
            throw new RuntimeException("cancelDVAsSystemUser() Unable to cancel DV: " + dvDocumentNumber, e);
        }
    }

    @Override
    public Set<String> cancelDisbursementVouchersFinalizedNotExtracted(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument, String cancelMessage) {
        Set<String> canceledDVs = new HashSet<String>();

        for (RecurringDisbursementVoucherDetail detail : recurringDisbursementVoucherDocument.getRecurringDisbursementVoucherDetails()) {
            String dvDocumentNumber = detail.getDvDocumentNumber();
            if (!isDVCancelable(dvDocumentNumber)) {
                CuDisbursementVoucherDocument dv;
                dv = (CuDisbursementVoucherDocument) getDocumentService().getByDocumentHeaderId(detail.getDvDocumentNumber());
                if (isDvCancelableFromApprovedNotExtracted(dv)) {

                    Date cancelDate =  new Date (Calendar.getInstance().getTimeInMillis());
                    dv.setCancelDate(cancelDate);

                    CuDisbursementVoucherDocument cancledDV = (CuDisbursementVoucherDocument) getDocumentService().saveDocument(dv);

                    getCuDisbursementVoucherExtractionHelperService().getPaymentSourceHelperService().handleEntryCancellation(
                            cancledDV, getCuDisbursementVoucherExtractionHelperService());

                    canceledDVs.add(cancledDV.getDocumentNumber());
                }
            }
        }
        noteChangeOnRecurringDV(recurringDisbursementVoucherDocument, "The following disbursement vouchers were canceled after it was approved but before payments were created: ", canceledDVs);
        return canceledDVs;
    }

    private boolean isDvCancelableFromApprovedNotExtracted(CuDisbursementVoucherDocument dv) {
        String dvStatus = getRouteHeaderService().getDocumentStatus(dv.getDocumentNumber());
        return hasCancelPermission() && StringUtils.equals(dv.getDisbursementVoucherPdpStatus(), CuFPConstants.RecurringDisbursementVoucherDocumentConstants.PDP_PRE_EXTRACTION_STATUS)
                && StringUtils.equalsIgnoreCase(dvStatus, DocumentStatus.FINAL.getCode());
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public ScheduledAccountingLineService getScheduledAccountingLineService() {
        return scheduledAccountingLineService;
    }

    public void setScheduledAccountingLineService(ScheduledAccountingLineService scheduledAccountingLineService) {
        this.scheduledAccountingLineService = scheduledAccountingLineService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public AccountingPeriodService getAccountingPeriodService() {
        return accountingPeriodService;
    }

    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    public RecurringDisbursementVoucherPaymentMaintenanceService getRecurringDisbursementVoucherPaymentMaintenanceService() {
        return recurringDisbursementVoucherPaymentMaintenanceService;
    }

    public void setRecurringDisbursementVoucherPaymentMaintenanceService(RecurringDisbursementVoucherPaymentMaintenanceService paymentMaintenanceService) {
        this.recurringDisbursementVoucherPaymentMaintenanceService = paymentMaintenanceService;
    }

    public RouteHeaderService getRouteHeaderService() {
        return routeHeaderService;
    }

    public void setRouteHeaderService(RouteHeaderService routeHeaderService) {
        this.routeHeaderService = routeHeaderService;
    }

    public CuDisbursementVoucherExtractionHelperService getCuDisbursementVoucherExtractionHelperService() {
        return cuDisbursementVoucherExtractionHelperService;
    }

    public void setCuDisbursementVoucherExtractionHelperService(
            CuDisbursementVoucherExtractionHelperService cuDisbursementVoucherExtractionHelperService) {
        this.cuDisbursementVoucherExtractionHelperService = cuDisbursementVoucherExtractionHelperService;
    }

    protected PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
    protected RecurringDisbursementVoucherSearchDao getRecurringDisbursementVoucherSearchDao() {
        return recurringDisbursementVoucherSearchDao;
    }

    public void setRecurringDisbursementVoucherSearchDao(RecurringDisbursementVoucherSearchDao recurringDisbursementVoucherSearchDao) {
        this.recurringDisbursementVoucherSearchDao = recurringDisbursementVoucherSearchDao;
    }

    public NoteService getNoteService() {
        return noteService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setRecurringDisbursementVoucherDocumentRoutingService(
            RecurringDisbursementVoucherDocumentRoutingService recurringDisbursementVoucherDocumentRoutingService) {
        this.recurringDisbursementVoucherDocumentRoutingService = recurringDisbursementVoucherDocumentRoutingService;
    }

    public void setRecurringDisbursementVoucherDocumentReportService(
            RecurringDisbursementVoucherDocumentReportService recurringDisbursementVoucherDocumentReportService) {
        this.recurringDisbursementVoucherDocumentReportService = recurringDisbursementVoucherDocumentReportService;
    }

}