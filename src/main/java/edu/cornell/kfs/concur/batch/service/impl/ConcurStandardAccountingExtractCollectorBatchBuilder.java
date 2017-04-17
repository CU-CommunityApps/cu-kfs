package edu.cornell.kfs.concur.batch.service.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.service.OptionsService;
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
    protected static final String PENDING_CLIENT_MESSAGE = "Line has the \"Pending Client\" object code";

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

    protected CollectorBatch collectorBatch;
    protected Map<String,ConcurDetailLineGroupForCollector> lineGroups;
    protected OptionsService optionsService;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;
    protected ConcurStandardAccountingExtractValidationService concurSAEValidationService;
    protected Function<String,String> parameterService;
    protected ConcurStandardAccountingExtractBatchReportData reportData;
    protected int batchSequenceNumber;
    protected int nextFakeObjectCode;
    protected Map<String,MutableInt> nextTransactionSequenceNumbers;

    /**
     * @throws IllegalArgumentException if any arguments are null.
     */
    public ConcurStandardAccountingExtractCollectorBatchBuilder(
            OptionsService optionsService,
            UniversityDateService universityDateService, DateTimeService dateTimeService,
            ConcurStandardAccountingExtractValidationService concurSAEValidationService, Function<String,String> parameterService) {
        if (optionsService == null) {
            throw new IllegalArgumentException("optionsService cannot be null");
        } else if (universityDateService == null) {
            throw new IllegalArgumentException("universityDateService cannot be null");
        } else if (dateTimeService == null) {
            throw new IllegalArgumentException("dateTimeService cannot be null");
        } else if (concurSAEValidationService == null) {
            throw new IllegalArgumentException("saeValidationService cannot be null");
        } else if (parameterService == null) {
            throw new IllegalArgumentException("propertyFinder cannot be null");
        }
        
        this.optionsService = optionsService;
        this.universityDateService = universityDateService;
        this.dateTimeService = dateTimeService;
        this.concurSAEValidationService = concurSAEValidationService;
        this.parameterService = parameterService;
        
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
        
        resetBuilderForNextRun();
    }

    protected void resetBuilderForNextRun() {
        this.collectorBatch = new CollectorBatch();
        this.lineGroups = new LinkedHashMap<>();
        this.nextFakeObjectCode = 1;
        this.nextTransactionSequenceNumbers = new HashMap<>();
        this.batchSequenceNumber = 0;
        this.reportData = null;
    }

    /**
     * @throws IllegalArgumentException if any arguments are null.
     */
    public CollectorBatch buildCollectorBatchFromStandardAccountingExtract(int nextBatchSequenceNumber,
            ConcurStandardAccountingExtractFile saeFileContents, ConcurStandardAccountingExtractBatchReportData newReportData) {
        if (saeFileContents == null) {
            throw new IllegalArgumentException("saeFileContents cannot be null");
        } else if (newReportData == null) {
            throw new IllegalArgumentException("newReportData cannot be null");
        }
        this.batchSequenceNumber = nextBatchSequenceNumber;
        this.reportData = newReportData;
        CollectorBatch result;
        
        try {
            LOG.info("buildCollectorBatchFromStandardAccountingExtract(): Generating Collector data from SAE file: "
                    + saeFileContents.getOriginalFileName());
            
            if (batchSequenceNumber < MIN_BATCH_SEQUENCE_NUMBER || batchSequenceNumber > MAX_BATCH_SEQUENCE_NUMBER) {
                throw new IllegalArgumentException("Batch Sequence Number should have been an integer between "
                        + MIN_BATCH_SEQUENCE_NUMBER + " and " + MAX_BATCH_SEQUENCE_NUMBER);
            }
            
            updateCollectorBatchHeaderFields(saeFileContents);
            groupLines(saeFileContents.getConcurStandardAccountingExtractDetailLines());
            updateCollectorBatchWithOriginEntries();
            
            LOG.info("buildCollectorBatchFromStandardAccountingExtract(): Finished generating Collector data from SAE file: "
                    + saeFileContents.getOriginalFileName());
            LOG.info("buildCollectorBatchFromStandardAccountingExtract(): Total GL Entry Count: " + collectorBatch.getTotalRecords());
            LOG.info("buildCollectorBatchFromStandardAccountingExtract(): Total Debit Amount: " + collectorBatch.getTotalAmount());
            result = collectorBatch;
        } catch (Exception e) {
            LOG.error("buildCollectorBatchFromStandardAccountingExtract(): Error encountered while generating Collector data from SAE file", e);
            reportData.addHeaderValidationError("Error processing SAE-to-Collector feed: " + e.getMessage());
            result = null;
        } finally {
            resetBuilderForNextRun();
        }
        
        return result;
    }

    /**
     * @throws RuntimeException if universityDateService cannot find a fiscal year for the SAE file's batch date.
     */
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
            if (shouldProcessLine(itemKey, saeLine)
                    && concurSAEValidationService.validateConcurStandardAccountingExtractDetailLine(saeLine, reportData)) {
                if (Boolean.TRUE.equals(saeLine.getJournalAccountCodeOverridden())) {
                    reportPendingClientLine(saeLine);
                }
                ConcurDetailLineGroupForCollector lineGroup = lineGroups.computeIfAbsent(
                        itemKey, (newKey) -> new ConcurDetailLineGroupForCollector(
                                saeLine.getReportId(), buildOriginEntryForExtractedLine(newKey, saeLine),
                                parameterService, this::getDashValueForProperty));
                lineGroup.addDetailLine(saeLine);
            }
        }
    }

    protected void updateCollectorBatchWithOriginEntries() {
        SystemOptions options = optionsService.getCurrentYearOptions();
        String actualFinancialBalanceTypeCode = options.getActualFinancialBalanceTypeCd();
        
        for (ConcurDetailLineGroupForCollector lineGroup : lineGroups.values()) {
            for (OriginEntryFull originEntry : lineGroup.buildOriginEntries()) {
                originEntry.setFinancialBalanceTypeCode(actualFinancialBalanceTypeCode);
                originEntry.setTransactionLedgerEntrySequenceNumber(
                        getNextTransactionSequenceNumber(lineGroup.getReportId()));
                collectorBatch.addOriginEntry(originEntry);
            }
        }
        
        Integer totalRecords = Integer.valueOf(collectorBatch.getOriginEntries().size());
        KualiDecimal totalDebitAmount = collectorBatch.getOriginEntries()
                .stream()
                .filter(this::isDebitEntry)
                .map(OriginEntryFull::getTransactionLedgerEntryAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
        
        collectorBatch.setTotalRecords(totalRecords);
        collectorBatch.setTotalAmount(totalDebitAmount);
    }

    protected boolean isDebitEntry(OriginEntryFull originEntry) {
        return StringUtils.equals(KFSConstants.GL_DEBIT_CODE, originEntry.getTransactionDebitCreditCode());
    }

    protected boolean shouldProcessLine(String itemKey, ConcurStandardAccountingExtractDetailLine saeLine) {
        switch (saeLine.getPaymentCode()) {
            case ConcurConstants.PAYMENT_CODE_CASH :
                return true;
            case ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID :
                reportCorporateCardPayment(saeLine);
                return true;
            case ConcurConstants.PAYMENT_CODE_PRE_PAID_OR_OTHER :
                reportUnprocessedLine(saeLine, "The line has the Pre-Paid/Other (COPD) payment code");
                return false;
            case ConcurConstants.PAYMENT_CODE_PSEUDO :
                reportBypassOfLineWithPseudoPaymentCode(saeLine);
                reportUnprocessedLine(saeLine, "The line has the Pseudo (XXXX) payment code");
                return false;
            default :
                reportUnprocessedLine(saeLine, "The line has an unrecognized payment code");
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

    protected String getDashValueForProperty(String propertyName) {
        switch (propertyName) {
            case KFSPropertyConstants.SUB_ACCOUNT_NUMBER :
                return getDashSubAccountNumber();
            case KFSPropertyConstants.SUB_OBJECT_CODE :
                return getDashSubObjectCode();
            case KFSPropertyConstants.PROJECT_CODE :
                return getDashProjectCode();
            default :
                return StringUtils.EMPTY;
        }
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
        summaryItem.setDollarAmount(summaryItem.getDollarAmount().add(saeLine.getJournalAmount()));
    }

    protected void reportPendingClientLine(ConcurStandardAccountingExtractDetailLine saeLine) {
        ConcurBatchReportMissingObjectCodeItem pendingClientItem = new ConcurBatchReportMissingObjectCodeItem(
                saeLine.getReportId(),
                saeLine.getEmployeeId(),
                saeLine.getEmployeeLastName(),
                saeLine.getEmployeeFirstName(),
                saeLine.getEmployeeMiddleInitital(),
                PENDING_CLIENT_MESSAGE,
                saeLine.getPolicy(),
                saeLine.getExpenseType());
        reportData.addPendingClientObjectCodeLine(pendingClientItem);
    }

    protected void reportUnprocessedLine(ConcurStandardAccountingExtractDetailLine saeLine, String errorMessage) {
        ConcurBatchReportLineValidationErrorItem errorItem = new ConcurBatchReportLineValidationErrorItem(
                saeLine.getReportId(),
                saeLine.getEmployeeId(),
                saeLine.getEmployeeLastName(),
                saeLine.getEmployeeFirstName(),
                saeLine.getEmployeeMiddleInitital(),
                errorMessage);
        reportData.addValidationErrorFileLine(errorItem);
    }

}
