package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportMissingObjectCodeItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportSummaryItem;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

/**
 * Helper class for converting parsed SAE file data
 * into GL CollectorBatch objects, which in turn will contain
 * OriginEntryFull objects derived from the SAE detail lines.
 * The CollectorBatch objects will not have a record type
 * configured; the service that serializes the objects
 * to a flat file will handle populating the record type
 * in the text output.
 * 
 * This builder is NOT thread-safe; external synchronization
 * is required if one instance is used by multiple threads.
 */
public class ConcurStandardAccountingExtractCollectorBatchBuilder {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractCollectorBatchBuilder.class);

    protected static final String FAKE_OBJECT_CODE_PREFIX = "?NONE?";
    protected static final int INITIAL_BUILDER_SIZE = 50;
    protected static final int REPORT_ID_LENGTH_FOR_DOC_NUMBER = 10;
    protected static final int NAME_LENGTH_FOR_DESCRIPTION = 14;
    protected static final int MIN_BATCH_SEQUENCE_NUMBER = 0;
    protected static final int MAX_BATCH_SEQUENCE_NUMBER = 9;
    protected static final char PIPE_CHAR = '|';
    protected static final char COMMA_CHAR = ',';

    protected final String docTypeCode;
    protected final String systemOriginationCode;
    protected final String chartCode;
    protected final String highestLevelOrgCode;
    protected final String departmentName;
    protected final String campusCode;
    protected final String campusAddress;
    protected final String notificationEmail;
    protected final String notificationPerson;
    protected final String notificationPhone;
    protected final String prepaidOffsetAccountNumber;
    protected final String prepaidOffsetObjectCode;
    protected final String cashOffsetObjectCode;

    protected CollectorBatch collectorBatch;
    protected Map<String,DetailLineGroup> lineGroups;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;
    protected ConcurStandardAccountingExtractValidationService concurSAEValidationService;
    protected ConcurStandardAccountingExtractBatchReportData reportData;
    protected int batchSequenceNumber;
    protected int nextFakeObjectCode;
    protected Map<String,MutableInt> nextTransactionSequenceNumbers;

    public ConcurStandardAccountingExtractCollectorBatchBuilder(
            UniversityDateService universityDateService, DateTimeService dateTimeService,
            ConcurStandardAccountingExtractValidationService concurSAEValidationService, Function<String,String> parameterService) {
        if (universityDateService == null) {
            throw new IllegalArgumentException("universityDateService cannot be null");
        } else if (dateTimeService == null) {
            throw new IllegalArgumentException("dateTimeService cannot be null");
        } else if (concurSAEValidationService == null) {
            throw new IllegalArgumentException("saeValidationService cannot be null");
        } else if (parameterService == null) {
            throw new IllegalArgumentException("propertyFinder cannot be null");
        }
        
        this.universityDateService = universityDateService;
        this.dateTimeService = dateTimeService;
        this.concurSAEValidationService = concurSAEValidationService;
        
        this.docTypeCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DOCUMENT_TYPE);
        this.systemOriginationCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_SYSTEM_ORIGINATION_CODE);
        this.chartCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CHART_CODE);
        this.highestLevelOrgCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_HIGHEST_LEVEL_ORG_CODE);
        this.departmentName = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DEPARTMENT_NAME);
        this.campusCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CAMPUS_CODE);
        this.campusAddress = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CAMPUS_ADDRESS);
        this.notificationEmail = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_EMAIL);
        this.notificationPerson = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_PERSON);
        this.notificationPhone = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_PHONE);
        this.prepaidOffsetAccountNumber = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER);
        this.prepaidOffsetObjectCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_OBJECT_CODE);
        this.cashOffsetObjectCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CASH_OFFSET_OBJECT_CODE);
    }

    public void reset() {
        this.collectorBatch = new CollectorBatch();
        this.lineGroups = new LinkedHashMap<>();
        this.nextFakeObjectCode = 1;
        this.nextTransactionSequenceNumbers = new HashMap<>();
    }

    public CollectorBatch buildCollectorBatchFromStandardAccountingExtract(int nextBatchSequenceNumber,
            ConcurStandardAccountingExtractFile saeFileContents, ConcurStandardAccountingExtractBatchReportData newReportData) {
        this.batchSequenceNumber = nextBatchSequenceNumber;
        this.reportData = newReportData;
        reset();
        
        try {
            LOG.info("Generating Collector data from SAE file: " + saeFileContents.getOriginalFileName());
            
            if (batchSequenceNumber < MIN_BATCH_SEQUENCE_NUMBER || batchSequenceNumber > MAX_BATCH_SEQUENCE_NUMBER) {
                throw new IllegalArgumentException("Batch Sequence Number should have been an integer between 0 and 9");
            } else if (reportData == null) {
                throw new IllegalArgumentException("Batch Report Data POJO cannot be null");
            }
            
            updateCollectorBatchHeaderFields(saeFileContents);
            groupLines(saeFileContents.getConcurStandardAccountingExtractDetailLines());
            updateCollectorBatchWithOriginEntries();
            
            LOG.info("Finished generating collector data from SAE file: " + saeFileContents.getOriginalFileName());
            LOG.info("Total GL Entry Count: " + collectorBatch.getTotalRecords());
            LOG.info("Total Amount: " + collectorBatch.getTotalAmount());
        } catch (Exception e) {
            LOG.error("Error encountered while generating Collector data from SAE file", e);
            return null;
        }
        
        return collectorBatch;
    }

    protected void updateCollectorBatchHeaderFields(ConcurStandardAccountingExtractFile saeFileContents) {
        Integer fiscalYear = universityDateService.getFiscalYear(saeFileContents.getBatchDate());
        if (fiscalYear == null) {
            throw new RuntimeException("No fiscal year found for batch date: " + saeFileContents.getBatchDate());
        }
        
        collectorBatch.setUniversityFiscalYear(fiscalYear.toString());
        collectorBatch.setChartOfAccountsCode(chartCode);
        collectorBatch.setOrganizationCode(highestLevelOrgCode);
        collectorBatch.setTransmissionDate(saeFileContents.getBatchDate());
        collectorBatch.setBatchSequenceNumber(Integer.valueOf(batchSequenceNumber));
        collectorBatch.setCampusCode(campusCode);
        collectorBatch.setMailingAddress(campusAddress);
        collectorBatch.setDepartmentName(departmentName);
        collectorBatch.setEmailAddress(notificationEmail);
        collectorBatch.setPersonUserID(notificationPerson);
        collectorBatch.setPhoneNumber(notificationPhone);
    }

    protected void groupLines(List<ConcurStandardAccountingExtractDetailLine> saeLines) {
        for (ConcurStandardAccountingExtractDetailLine saeLine : saeLines) {
            String itemKey = buildItemKey(saeLine);
            if (concurSAEValidationService.validateConcurStandardAccountingExtractDetailLine(saeLine)
                    && shouldProcessLine(itemKey, saeLine)) {
                DetailLineGroup lineGroup = lineGroups.computeIfAbsent(
                        itemKey, (newKey) -> new DetailLineGroup(saeLine.getReportId(), buildOriginEntryForExtractedLine(newKey, saeLine)));
                lineGroup.addDetailLine(saeLine);
            } else {
                reportUnprocessedLine(saeLine);
            }
        }
    }

    protected void updateCollectorBatchWithOriginEntries() {
        for (DetailLineGroup lineGroup : lineGroups.values()) {
            for (OriginEntryFull originEntry : lineGroup.buildOriginEntries()) {
                originEntry.setTransactionLedgerEntrySequenceNumber(
                        getNextTransactionSequenceNumber(lineGroup.getReportId()));
                collectorBatch.addOriginEntry(originEntry);
            }
        }
        
        Integer totalRecords = Integer.valueOf(collectorBatch.getOriginEntries().size());
        KualiDecimal totalAmount = collectorBatch.getOriginEntries()
                .stream()
                .map(OriginEntryFull::getTransactionLedgerEntryAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
        
        collectorBatch.setTotalRecords(totalRecords);
        collectorBatch.setTotalAmount(totalAmount);
    }

    protected boolean shouldProcessLine(String itemKey, ConcurStandardAccountingExtractDetailLine saeLine) {
        switch (saeLine.getPaymentCode()) {
            case ConcurConstants.PAYMENT_CODE_CASH :
                return true;
            case ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID :
                reportCorporateCardPayment(saeLine);
                return true;
            case ConcurConstants.PAYMENT_CODE_PRE_PAID_OR_OTHER :
                LOG.warn("Found a transaction with a Pre-Paid/Other payment code; it will not be processed. Item key: " + itemKey);
                return false;
            case ConcurConstants.PAYMENT_CODE_PSEUDO :
                LOG.warn("Found a transaction with a Pseudo payment code; it will not be processed. Item key: " + itemKey);
                reportBypassOfLineWithPseudoPaymentCode(saeLine);
                return false;
            default :
                LOG.warn("Found a transaction with an unknown payment code; it will not be processed. Item key: " + itemKey);
                return false;
        }
    }

    protected String buildItemKey(ConcurStandardAccountingExtractDetailLine saeLine) {
        String objectCodeForKey = saeLine.getJournalAccountCode();
        if (Boolean.TRUE.equals(saeLine.getJournalAccountCodeOverridden())) {
            objectCodeForKey = buildFakeObjectCodeToLeaveDetailLineUnmerged();
        }
        
        return new StringBuilder(INITIAL_BUILDER_SIZE)
                .append(saeLine.getReportId())
                .append(PIPE_CHAR).append(makeEmptyIfBlank(saeLine.getChartOfAccountsCode()))
                .append(PIPE_CHAR).append(makeEmptyIfBlank(saeLine.getAccountNumber()))
                .append(PIPE_CHAR).append(StringUtils.defaultIfBlank(saeLine.getSubAccountNumber(), getDashSubAccountNumber()))
                .append(PIPE_CHAR).append(makeEmptyIfBlank(objectCodeForKey))
                .append(PIPE_CHAR).append(StringUtils.defaultIfBlank(saeLine.getSubObjectCode(), getDashSubObjectCode()))
                .append(PIPE_CHAR).append(StringUtils.defaultIfBlank(saeLine.getProjectCode(), getDashProjectCode()))
                .append(PIPE_CHAR).append(makeEmptyIfBlank(saeLine.getOrgRefId()))
                .toString();
    }

    protected String makeEmptyIfBlank(String value) {
        return StringUtils.defaultIfBlank(value, StringUtils.EMPTY);
    }

    protected String buildFakeObjectCodeToLeaveDetailLineUnmerged() {
        return FAKE_OBJECT_CODE_PREFIX + (nextFakeObjectCode++);
    }

    protected OriginEntryFull buildOriginEntryForExtractedLine(String itemKey, ConcurStandardAccountingExtractDetailLine saeLine) {
        OriginEntryFull originEntry = new OriginEntryFull();
        
        // Default constructor sets fiscal year to zero; need to forcibly clear it to allow auto-setup by the Poster, as per the spec.
        originEntry.setUniversityFiscalYear(null);
        
        originEntry.setChartOfAccountsCode(saeLine.getChartOfAccountsCode());
        originEntry.setAccountNumber(saeLine.getAccountNumber());
        originEntry.setSubAccountNumber(
                StringUtils.defaultIfBlank(saeLine.getSubAccountNumber(), getDashSubAccountNumber()));
        originEntry.setFinancialObjectCode(saeLine.getJournalAccountCode());
        originEntry.setFinancialSubObjectCode(
                StringUtils.defaultIfBlank(saeLine.getSubObjectCode(), getDashSubObjectCode()));
        originEntry.setProjectCode(
                StringUtils.defaultIfBlank(saeLine.getProjectCode(), getDashProjectCode()));
        originEntry.setOrganizationReferenceId(
                StringUtils.defaultIfBlank(saeLine.getOrgRefId(), StringUtils.EMPTY));
        
        originEntry.setFinancialDocumentTypeCode(docTypeCode);
        originEntry.setFinancialSystemOriginationCode(systemOriginationCode);
        originEntry.setDocumentNumber(buildDocumentNumber(saeLine));
        originEntry.setTransactionLedgerEntryDescription(buildTransactionDescription(saeLine));
        originEntry.setTransactionDate(collectorBatch.getTransmissionDate());
        originEntry.setTransactionLedgerEntryAmount(KualiDecimal.ZERO);
        
        return originEntry;
    }

    protected String getDashSubAccountNumber() {
        return KFSConstants.getDashSubAccountNumber();
    }

    protected String getDashSubObjectCode() {
        return KFSConstants.getDashFinancialSubObjectCode();
    }

    protected String getDashProjectCode() {
        return KFSConstants.getDashProjectCode();
    }

    protected Integer getNextTransactionSequenceNumber(String reportId) {
        MutableInt nextSequenceNumber = nextTransactionSequenceNumbers.computeIfAbsent(
                reportId, (key) -> new MutableInt(0));
        nextSequenceNumber.increment();
        return nextSequenceNumber.toInteger();
    }

    protected String buildDocumentNumber(ConcurStandardAccountingExtractDetailLine saeLine) {
        return docTypeCode + StringUtils.left(saeLine.getReportId(), REPORT_ID_LENGTH_FOR_DOC_NUMBER);
    }

    protected String buildTransactionDescription(ConcurStandardAccountingExtractDetailLine saeLine) {
        String formattedEndDate = dateTimeService.toString(saeLine.getReportEndDate(), ConcurConstants.DATE_FORMAT);
        
        return new StringBuilder(INITIAL_BUILDER_SIZE)
                .append(StringUtils.left(saeLine.getEmployeeLastName(), NAME_LENGTH_FOR_DESCRIPTION))
                .append(COMMA_CHAR).append(StringUtils.left(saeLine.getEmployeeFirstName(), NAME_LENGTH_FOR_DESCRIPTION))
                .append(COMMA_CHAR).append(formattedEndDate)
                .toString();
    }

    protected void reportCashAdvance(ConcurStandardAccountingExtractDetailLine saeLine) {
        updateStatisticFromLineData(ConcurStandardAccountingExtractBatchReportData::getCashAdvancesRelatedToExpenseReports, saeLine);
    }

    protected void reportCorporateCardPayment(ConcurStandardAccountingExtractDetailLine saeLine) {
        updateStatisticFromLineData(ConcurStandardAccountingExtractBatchReportData::getExpensesPaidOnCorporateCard, saeLine);
    }

    protected void reportBypassOfLineWithPseudoPaymentCode(ConcurStandardAccountingExtractDetailLine saeLine) {
        updateStatisticFromLineData(ConcurStandardAccountingExtractBatchReportData::getTransactionsBypassed, saeLine);
    }

    protected void updateStatisticFromLineData(
            Function<ConcurStandardAccountingExtractBatchReportData,ConcurBatchReportSummaryItem> summaryItemGetter,
            ConcurStandardAccountingExtractDetailLine saeLine) {
        ConcurBatchReportSummaryItem summaryItem = summaryItemGetter.apply(reportData);
        summaryItem.setRecordCount(summaryItem.getRecordCount() + 1);
        summaryItem.setDollarAmount(summaryItem.getDollarAmount().add(saeLine.getJournalAmount().abs()));
    }

    protected void reportPendingClientLine(ConcurStandardAccountingExtractDetailLine saeLine) {
        ConcurBatchReportMissingObjectCodeItem pendingClientItem = buildPreConfiguredErrorItem(
                ConcurBatchReportMissingObjectCodeItem::new, saeLine);
        pendingClientItem.setPolicyName(saeLine.getPolicy());
        pendingClientItem.setExpenseTypeName(saeLine.getExpenseType());
        reportData.addPendingClientObjectCodeLine(pendingClientItem);
    }

    protected void reportUnprocessedLine(ConcurStandardAccountingExtractDetailLine saeLine) {
        reportData.addValidationErrorFileLine(
                buildPreConfiguredErrorItem(ConcurBatchReportLineValidationErrorItem::new, saeLine));
    }

    protected <T extends ConcurBatchReportLineValidationErrorItem> T buildPreConfiguredErrorItem(
            Supplier<T> errorItemSupplier, ConcurStandardAccountingExtractDetailLine saeLine) {
        T errorItem = errorItemSupplier.get();
        errorItem.setReportId(saeLine.getReportId());
        errorItem.setEmployeeId(saeLine.getEmployeeId());
        errorItem.setLastName(saeLine.getEmployeeLastName());
        errorItem.setFirstName(saeLine.getEmployeeFirstName());
        errorItem.setMiddleInitial(saeLine.getEmployeeMiddleInitital());
        return errorItem;
    }

    /**
     * Internal helper class for encapsulating related SAE transactions,
     * and for generating Collector OriginEntryFull lines from them.
     */
    protected class DetailLineGroup {
        protected String reportId;
        protected List<ConcurStandardAccountingExtractDetailLine> detailLines;
        protected OriginEntryFull baseEntry;
        protected Map<String,KualiDecimal> paymentCodeAmounts;
        
        public DetailLineGroup(String reportId, OriginEntryFull baseEntry) {
            this.reportId = reportId;
            this.detailLines = new ArrayList<>();
            this.baseEntry = baseEntry;
            this.paymentCodeAmounts = new HashMap<>();
        }
        
        public String getReportId() {
            return reportId;
        }
        
        public void addDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
            detailLines.add(detailLine);
            paymentCodeAmounts.merge(detailLine.getPaymentCode(), detailLine.getJournalAmount(), KualiDecimal::add);
        }
        
        public List<OriginEntryFull> buildOriginEntries() {
            KualiDecimal cashAmount = paymentCodeAmounts.getOrDefault(ConcurConstants.PAYMENT_CODE_CASH, KualiDecimal.ZERO);
            KualiDecimal corporateCardAmount = paymentCodeAmounts.getOrDefault(
                    ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID, KualiDecimal.ZERO);
            KualiDecimal totalAmount = cashAmount.add(corporateCardAmount);
            
            List<OriginEntryFull> originEntries = new ArrayList<>();
            if (totalAmount.isNonZero()) {
                originEntries.add(buildOriginEntry(totalAmount));
            } else {
                if (corporateCardAmount.isNonZero()) {
                    originEntries.add(buildOriginEntry(corporateCardAmount));
                }
                if (cashAmount.isNonZero()) {
                    originEntries.add(buildOriginEntry(cashAmount));
                }
            }
            
            if (corporateCardAmount.isNonZero()) {
                originEntries.add(
                        buildOffsetOriginEntry(corporateCardAmount, this::configureOriginEntryForCorporateCardOffset));
            }
            if (cashAmount.isNonZero()) {
                originEntries.add(
                        buildOffsetOriginEntry(cashAmount, this::configureOriginEntryForCashOffset));
            }
            
            return originEntries;
        }

        protected void configureOriginEntryForCorporateCardOffset(OriginEntryFull originEntry) {
            originEntry.setChartOfAccountsCode(chartCode);
            originEntry.setAccountNumber(prepaidOffsetAccountNumber);
            originEntry.setSubAccountNumber(getDashSubAccountNumber());
            originEntry.setFinancialObjectCode(prepaidOffsetObjectCode);
            originEntry.setFinancialSubObjectCode(getDashSubObjectCode());
        }

        protected void configureOriginEntryForCashOffset(OriginEntryFull originEntry) {
            originEntry.setFinancialObjectCode(cashOffsetObjectCode);
            originEntry.setFinancialSubObjectCode(getDashSubObjectCode());
        }

        protected OriginEntryFull buildOffsetOriginEntry(KualiDecimal amount, Consumer<OriginEntryFull> originEntryConfigurer) {
            KualiDecimal offsetAmount = amount.negated();
            return buildOriginEntry(offsetAmount, originEntryConfigurer);
        }
        
        protected OriginEntryFull buildOriginEntry(KualiDecimal amount) {
            return buildOriginEntry(amount, (originEntry) -> {});
        }

        protected OriginEntryFull buildOriginEntry(KualiDecimal amount, Consumer<OriginEntryFull> originEntryConfigurer) {
            OriginEntryFull originEntry = new OriginEntryFull(baseEntry);
            String debitCreditCode = getGeneralLedgerDebitCreditCode(amount);
            
            originEntry.setTransactionDebitCreditCode(debitCreditCode);
            originEntry.setTransactionLedgerEntryAmount(amount.abs());
            originEntryConfigurer.accept(originEntry);
            
            return originEntry;
        }

        protected String getGeneralLedgerDebitCreditCode(KualiDecimal amount) {
            if (amount.isPositive()) {
                return KFSConstants.GL_DEBIT_CODE;
            } else if (amount.isNegative()) {
                return KFSConstants.GL_CREDIT_CODE;
            } else {
                throw new IllegalArgumentException("amount cannot be zero");
            }
        }
    }

}
