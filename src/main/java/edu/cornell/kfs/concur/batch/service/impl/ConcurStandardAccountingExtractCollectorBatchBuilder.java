package edu.cornell.kfs.concur.batch.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
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
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
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

    protected static final int MIN_BATCH_SEQUENCE_NUMBER = 0;
    protected static final int MAX_BATCH_SEQUENCE_NUMBER = 9;
    protected static final String PENDING_CLIENT_MESSAGE = "Line has the \"Pending Client\" object code";

    protected final String docTypeCode;
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
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;
    protected ConfigurationService configurationService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected OptionsService optionsService;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;
    protected ConcurStandardAccountingExtractValidationService concurSAEValidationService;
    protected Function<String,String> concurParameterGetter;
    protected ConcurStandardAccountingExtractBatchReportData reportData;
    protected ConcurDetailLineGroupForCollectorHelper collectorHelper;
    protected int batchSequenceNumber;

    /**
     * @throws IllegalArgumentException if any arguments are null.
     */
    public ConcurStandardAccountingExtractCollectorBatchBuilder(
            ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService,
            ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService,
            ConfigurationService configurationService, ConcurBatchUtilityService concurBatchUtilityService, OptionsService optionsService,
            UniversityDateService universityDateService, DateTimeService dateTimeService,
            ConcurStandardAccountingExtractValidationService concurSAEValidationService, Function<String,String> concurParameterGetter) {
        if (concurRequestedCashAdvanceService == null) {
            throw new IllegalArgumentException("concurRequestedCashAdvanceService cannot be null");
        } else if (concurStandardAccountingExtractCashAdvanceService == null) {
            throw new IllegalArgumentException("concurStandardAccountingExtractCashAdvanceService cannot be null");
        } else if (configurationService == null) {
            throw new IllegalArgumentException("configurationService cannot be null");
        } else if (concurBatchUtilityService == null) {
            throw new IllegalArgumentException("concurBatchUtilityService cannot be null");
        } else if (optionsService == null) {
            throw new IllegalArgumentException("optionsService cannot be null");
        } else if (universityDateService == null) {
            throw new IllegalArgumentException("universityDateService cannot be null");
        } else if (dateTimeService == null) {
            throw new IllegalArgumentException("dateTimeService cannot be null");
        } else if (concurSAEValidationService == null) {
            throw new IllegalArgumentException("saeValidationService cannot be null");
        } else if (concurParameterGetter == null) {
            throw new IllegalArgumentException("concurParameterGetter cannot be null");
        }
        
        this.concurRequestedCashAdvanceService = concurRequestedCashAdvanceService;
        this.concurStandardAccountingExtractCashAdvanceService = concurStandardAccountingExtractCashAdvanceService;
        this.configurationService = configurationService;
        this.concurBatchUtilityService = concurBatchUtilityService;
        this.optionsService = optionsService;
        this.universityDateService = universityDateService;
        this.dateTimeService = dateTimeService;
        this.concurSAEValidationService = concurSAEValidationService;
        this.concurParameterGetter = concurParameterGetter;
        
        this.docTypeCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DOCUMENT_TYPE);
        this.chartCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CHART_CODE);
        this.highestLevelOrgCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_HIGHEST_LEVEL_ORG_CODE);
        this.departmentName = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DEPARTMENT_NAME);
        this.campusCode = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CAMPUS_CODE);
        this.campusAddress = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CAMPUS_ADDRESS);
        this.notificationEmail = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_EMAIL);
        this.notificationPerson = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_PERSON);
        this.notificationPhone = concurParameterGetter.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_PHONE);
        
        resetBuilderForNextRun();
    }

    protected void resetBuilderForNextRun() {
        this.collectorBatch = new CollectorBatch();
        this.lineGroups = new LinkedHashMap<>();
        this.collectorHelper = null;
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
            initializeCollectorHelperFromCollectorBatch();
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

    protected void initializeCollectorHelperFromCollectorBatch() {
        String actualFinancialBalanceTypeCode = getActualFinancialBalanceTypeCodeForCollectorBatch();
        this.collectorHelper = new ConcurDetailLineGroupForCollectorHelper(
                actualFinancialBalanceTypeCode, collectorBatch.getTransmissionDate(),
                concurRequestedCashAdvanceService, concurStandardAccountingExtractCashAdvanceService,
                configurationService, concurBatchUtilityService, dateTimeService, this::getDashValueForProperty, concurParameterGetter);
    }

    protected String getActualFinancialBalanceTypeCodeForCollectorBatch() {
        Integer fiscalYear = Integer.valueOf(collectorBatch.getUniversityFiscalYear());
        SystemOptions fiscalYearOptions = optionsService.getOptions(fiscalYear);
        return fiscalYearOptions.getActualFinancialBalanceTypeCd();
    }

    protected void groupLines(List<ConcurStandardAccountingExtractDetailLine> saeLines) {
        for (ConcurStandardAccountingExtractDetailLine saeLine : saeLines) {
            if (concurStandardAccountingExtractCashAdvanceService.isCashAdvanceLine(saeLine)) {
                reportCashAdvance(saeLine);
            }
            if (shouldProcessLine(saeLine)
                    && concurSAEValidationService.validateConcurStandardAccountingExtractDetailLine(saeLine, reportData)) {
                if (Boolean.TRUE.equals(saeLine.getJournalAccountCodeOverridden())) {
                    reportPendingClientLine(saeLine);
                }
                ConcurDetailLineGroupForCollector lineGroup = lineGroups.computeIfAbsent(
                        saeLine.getReportId(), (reportId) -> new ConcurDetailLineGroupForCollector(reportId, collectorHelper));
                lineGroup.addDetailLine(saeLine);
            }
        }
    }

    protected void updateCollectorBatchWithOriginEntries() {
        for (ConcurDetailLineGroupForCollector lineGroup : lineGroups.values()) {
            if (lineGroup.hasNoErrors()) {
                lineGroup.buildAndAddOriginEntries(collectorBatch::addOriginEntry);
            } else {
                lineGroup.reportErrors(this::reportUnprocessedLine);
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

    protected boolean shouldProcessLine(ConcurStandardAccountingExtractDetailLine saeLine) {
        String paymentCode = saeLine.getPaymentCode() != null ? saeLine.getPaymentCode() : StringUtils.EMPTY;
        switch (paymentCode) {
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

    protected String getDashValueForProperty(String propertyName) {
        switch (propertyName) {
            case KFSPropertyConstants.SUB_ACCOUNT_NUMBER :
                return KFSConstants.getDashSubAccountNumber();
            case KFSPropertyConstants.SUB_OBJECT_CODE :
                return KFSConstants.getDashFinancialSubObjectCode();
            case KFSPropertyConstants.PROJECT_CODE :
                return KFSConstants.getDashProjectCode();
            default :
                return StringUtils.EMPTY;
        }
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
        ConcurBatchReportMissingObjectCodeItem pendingClientItem = new ConcurBatchReportMissingObjectCodeItem(saeLine, PENDING_CLIENT_MESSAGE);
        reportData.addPendingClientObjectCodeLine(pendingClientItem);
    }

    protected void reportUnprocessedLine(ConcurStandardAccountingExtractDetailLine saeLine, String errorMessage) {
        ConcurBatchReportLineValidationErrorItem errorItem = new ConcurBatchReportLineValidationErrorItem(saeLine, errorMessage);
        reportData.addValidationErrorFileLine(errorItem);
    }

}
