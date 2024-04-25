package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

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

	private static final Logger LOG = LogManager.getLogger(ConcurStandardAccountingExtractCollectorBatchBuilder.class);

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
            if (concurStandardAccountingExtractCashAdvanceService.isCashAdvanceToBeAppliedToReimbursement(saeLine)) {
                reportCashAdvance(saeLine);
            }
            if (shouldProcessLine(saeLine)
                    && concurSAEValidationService.validateConcurStandardAccountingExtractDetailLineForCollector(saeLine, reportData)) {
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
        
        consolidateOriginEntries();
        
        Integer totalRecords = Integer.valueOf(collectorBatch.getOriginEntries().size());
        KualiDecimal totalDebitAmount = collectorBatch.getOriginEntries()
                .stream()
                .filter(this::isDebitEntry)
                .map(OriginEntryFull::getTransactionLedgerEntryAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
        
        collectorBatch.setTotalRecords(totalRecords);
        collectorBatch.setTotalAmount(totalDebitAmount);
    }
    
    private void consolidateOriginEntries() {
        List<OriginEntryFull> consolidatedOriginEntries = new ArrayList<OriginEntryFull>();
        LOG.info("consolidateOriginEntries, starting number of origin entries: "  + collectorBatch.getOriginEntries().size());
        boolean transactionsWereConsolidated = false;
        for (OriginEntryFull entry : collectorBatch.getOriginEntries()) {
            OriginEntryFull matchedEntry = findMatchingOriginEntryFull(consolidatedOriginEntries, entry);
            if (matchedEntry == null) {
                consolidatedOriginEntries.add(entry);
            } else {
                LOG.debug("consolidateOriginEntries, found a transaction to consolidate: " + matchedEntry);
                transactionsWereConsolidated = true;
                KualiDecimal newTotal = matchedEntry.getTransactionLedgerEntryAmount().add(entry.getTransactionLedgerEntryAmount());
                matchedEntry.setTransactionLedgerEntryAmount(newTotal);
            }
        }

        if (transactionsWereConsolidated) {
            Map<String, MutableInt> nextSequenceNumbers = new HashMap<>();
            for (OriginEntryFull entry : consolidatedOriginEntries) {
                MutableInt nextSequenceNumber = nextSequenceNumbers.computeIfAbsent(entry.getDocumentNumber(),
                        key -> new MutableInt(0));
                nextSequenceNumber.increment();
                entry.setTransactionLedgerEntrySequenceNumber(nextSequenceNumber.toInteger());
            }
            collectorBatch.setOriginEntries(consolidatedOriginEntries);
        }
        LOG.info("consolidateOriginEntries, consolidated number of origin entries: " + consolidatedOriginEntries.size());
    }

    private OriginEntryFull findMatchingOriginEntryFull(List<OriginEntryFull> entries, OriginEntryFull searchEntry) {
        for (OriginEntryFull entry : entries) {
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(entry.getAccountNumber(), searchEntry.getAccountNumber());
            eb.append(entry.getDocumentNumber(), searchEntry.getDocumentNumber());
            eb.append(entry.getReferenceFinancialDocumentNumber(), searchEntry.getReferenceFinancialDocumentNumber());
            eb.append(entry.getReferenceFinancialDocumentTypeCode(), searchEntry.getReferenceFinancialDocumentTypeCode());
            eb.append(entry.getFinancialDocumentReversalDate(), searchEntry.getFinancialDocumentReversalDate());
            eb.append(entry.getFinancialDocumentTypeCode(), searchEntry.getFinancialDocumentTypeCode());
            eb.append(entry.getFinancialBalanceTypeCode(), searchEntry.getFinancialBalanceTypeCode());
            eb.append(entry.getChartOfAccountsCode(), searchEntry.getChartOfAccountsCode());
            eb.append(entry.getFinancialObjectTypeCode(), searchEntry.getFinancialObjectTypeCode());
            eb.append(entry.getFinancialObjectCode(), searchEntry.getFinancialObjectCode());
            eb.append(entry.getFinancialSubObjectCode(), searchEntry.getFinancialSubObjectCode());
            eb.append(entry.getFinancialSystemOriginationCode(), searchEntry.getFinancialSystemOriginationCode());
            eb.append(entry.getReferenceFinancialSystemOriginationCode(), searchEntry.getReferenceFinancialSystemOriginationCode());
            eb.append(entry.getOrganizationDocumentNumber(), searchEntry.getOrganizationDocumentNumber());
            eb.append(entry.getOrganizationReferenceId(), searchEntry.getOrganizationReferenceId());
            eb.append(entry.getProjectCode(), searchEntry.getProjectCode());
            eb.append(entry.getSubAccountNumber(), searchEntry.getSubAccountNumber());
            eb.append(entry.getTransactionDate(), searchEntry.getTransactionDate());
            eb.append(entry.getTransactionDebitCreditCode(), searchEntry.getTransactionDebitCreditCode());
            eb.append(entry.getTransactionEncumbranceUpdateCode(), searchEntry.getTransactionEncumbranceUpdateCode());
            eb.append(entry.getTransactionLedgerEntryDescription(), searchEntry.getTransactionLedgerEntryDescription());
            eb.append(entry.getUniversityFiscalPeriodCode(), searchEntry.getUniversityFiscalPeriodCode());
            eb.append(entry.getUniversityFiscalYear(), searchEntry.getUniversityFiscalYear());
            eb.append(entry.isTransactionScrubberOffsetGenerationIndicator(), searchEntry.isTransactionScrubberOffsetGenerationIndicator());

            if (eb.isEquals()) {
                return entry;
            }
        }
        return null;
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
                return true;
            case ConcurConstants.PAYMENT_CODE_PSEUDO :
                if (concurStandardAccountingExtractCashAdvanceService.isAtmCashAdvanceLineWithUnusedAmount(saeLine)) {
                    return true;
                } else {
                    reportBypassOfLineWithPseudoPaymentCode(saeLine);
                    reportUnprocessedLine(saeLine, "The line has the Pseudo (XXXX) payment code", false);
                    return false;
                }
            case StringUtils.EMPTY :
                reportBypassOfCashAdvanceRequestLine(saeLine);
                reportUnprocessedLine(saeLine, "The line detected as cash advance request. Should have been processed by first batch job step.",
                        false);
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
    
    protected void reportBypassOfCashAdvanceRequestLine(ConcurStandardAccountingExtractDetailLine saeLine) {
        updateStatisticFromLineData(ConcurStandardAccountingExtractBatchReportData::getCashAdvanceRequestsBypassed, saeLine);
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
        reportUnprocessedLine(saeLine, errorMessage, true);
    }

    protected void reportUnprocessedLine(ConcurStandardAccountingExtractDetailLine saeLine, String errorMessage, boolean reportableAsLineLevelValidationError) {
        ConcurBatchReportLineValidationErrorItem errorItem = new ConcurBatchReportLineValidationErrorItem(saeLine, errorMessage,
                reportableAsLineLevelValidationError);

        reportData.addValidationErrorFileLine(errorItem);
    }

}
