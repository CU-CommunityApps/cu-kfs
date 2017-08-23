package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

/**
 * Utility class for grouping together related SAE transactions, and then generating GL entries
 * and GL offset entries from them. The calling code should create one instance of this class
 * for each Report ID in the SAE file.
 */
public class ConcurDetailLineGroupForCollector {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurDetailLineGroupForCollector.class);
    
    protected static final String FAKE_OBJECT_CODE_PREFIX = "?NONE?";
    protected static final String ORPHANED_CASH_ADVANCES_KEY = "ORPHANED_CASH_ADVANCES";
    protected static final String ACCOUNTING_FIELDS_KEY_FORMAT = "%s|%s|%s|%s|%s|%s|%s";
    protected static final String TRANSACTION_DESCRIPTION_FORMAT = "%s,%s,%s";
    protected static final int REPORT_ID_LENGTH_FOR_DOC_NUMBER = 10;
    protected static final int NAME_LENGTH_FOR_DESCRIPTION = 14;

    protected String reportId;
    protected ConcurDetailLineGroupForCollectorHelper collectorHelper;
    protected Map<String, ConcurDetailLineSubGroupForCollector> consolidatedRegularLines;
    protected Map<String, List<ConcurStandardAccountingExtractDetailLine>> consolidatedCashAdvanceLines;
    protected Map<String, KualiDecimal> totalCashAdvanceAmountsByReportEntryId;
    protected Map<String, KualiDecimal> unusedCashAdvanceAmountsByReportEntryId;
    protected Map<String, ConcurRequestedCashAdvance> requestedCashAdvancesByCashAdvanceKey;
    protected Map<String, List<ConcurStandardAccountingExtractDetailLine>> consolidatedCorpCardPersonalExpenseDebits;
    protected Map<String, List<ConcurStandardAccountingExtractDetailLine>> consolidatedCorpCardPersonalExpenseCredits;
    protected boolean hasMissingRequestedCashAdvance;
    protected int nextFakeObjectCode;
    protected int nextTransactionSequenceNumber;

    public ConcurDetailLineGroupForCollector(String reportId, ConcurDetailLineGroupForCollectorHelper collectorHelper) {
        this.reportId = reportId;
        this.collectorHelper = collectorHelper;
        this.consolidatedRegularLines = new LinkedHashMap<>();
        this.consolidatedCashAdvanceLines = new LinkedHashMap<>();
        this.totalCashAdvanceAmountsByReportEntryId = new LinkedHashMap<>();
        this.unusedCashAdvanceAmountsByReportEntryId = new LinkedHashMap<>();
        this.requestedCashAdvancesByCashAdvanceKey = new LinkedHashMap<>();
        this.consolidatedCorpCardPersonalExpenseDebits = new LinkedHashMap<>();
        this.consolidatedCorpCardPersonalExpenseCredits = new LinkedHashMap<>();
        this.hasMissingRequestedCashAdvance = false;
        this.nextFakeObjectCode = 1;
        this.nextTransactionSequenceNumber = 1;
    }

    public String getReportId() {
        return reportId;
    }

    public void addDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        if (collectorHelper.isCashAdvanceLine(detailLine)) {
            LOG.info("addDetailLine, isCashAdvanceLine");
            String accountingFieldsKey = buildAccountingFieldsKeyForCashAdvance(detailLine);
            consolidatedCashAdvanceLines.computeIfAbsent(accountingFieldsKey, (key) -> new ArrayList<>())
                    .add(detailLine);
            totalCashAdvanceAmountsByReportEntryId.merge(
                    detailLine.getReportEntryId(), detailLine.getJournalAmount(), KualiDecimal::add);
        } else if (collectorHelper.lineRepresentsPersonalExpenseChargedToCorporateCard(detailLine)) {
            LOG.info("addDetailLine, lineRepresentsPersonalExpenseChargedToCorporateCard");
            addCorpCardPersonalExpenseDetailLine(detailLine);
        } else {
            LOG.info("addDetailLine, other line");
            String accountingFieldsKey = buildAccountingFieldsKey(detailLine);
            consolidatedRegularLines.computeIfAbsent(accountingFieldsKey, (key) -> new ConcurDetailLineSubGroupForCollector())
                    .addDetailLine(detailLine);
        }
    }

    protected void addCorpCardPersonalExpenseDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        String accountingFieldsKey = buildAccountingFieldsKeyForCorpCardPersonalExpense(detailLine);
        Map<String, List<ConcurStandardAccountingExtractDetailLine>> consolidatedLinesMap = getConsolidatedPersonalExpensesMapForLine(detailLine);
        consolidatedLinesMap.computeIfAbsent(accountingFieldsKey, (key) -> new ArrayList<>())
                .add(detailLine);
    }

    protected Map<String, List<ConcurStandardAccountingExtractDetailLine>> getConsolidatedPersonalExpensesMapForLine(
            ConcurStandardAccountingExtractDetailLine detailLine) {
        if (detailLine.getJournalAmount().isPositive()) {
            return getConsolidatedPersonalExpensesMapForDebitLine(detailLine);
        } else {
            return getConsolidatedPersonalExpensesMapForCreditLine(detailLine);
        }
    }

    protected Map<String, List<ConcurStandardAccountingExtractDetailLine>> getConsolidatedPersonalExpensesMapForDebitLine(
            ConcurStandardAccountingExtractDetailLine detailLine) {
        if (collectorHelper.lineRepresentsReturnOfCorporateCardPersonalExpenseToUser(detailLine)) {
            return consolidatedCorpCardPersonalExpenseCredits;
        } else {
            return consolidatedCorpCardPersonalExpenseDebits;
        }
    }

    protected Map<String, List<ConcurStandardAccountingExtractDetailLine>> getConsolidatedPersonalExpensesMapForCreditLine(
            ConcurStandardAccountingExtractDetailLine detailLine) {
        if (collectorHelper.lineRepresentsReturnOfCorporateCardPersonalExpenseToUniversity(detailLine)) {
            return consolidatedCorpCardPersonalExpenseDebits;
        } else {
            return consolidatedCorpCardPersonalExpenseCredits;
        }
    }

    protected ConcurRequestedCashAdvance getExistingRequestedCashAdvanceByCashAdvanceKey(String cashAdvanceKey) {
        ConcurRequestedCashAdvance requestedCashAdvance = collectorHelper.getRequestedCashAdvanceByCashAdvanceKey(cashAdvanceKey);
        if (ObjectUtils.isNull(requestedCashAdvance)) {
            hasMissingRequestedCashAdvance = true;
        }
        return requestedCashAdvance;
    }

    protected String buildAccountingFieldsKey(ConcurStandardAccountingExtractDetailLine detailLine) {
        if (isAtmFeeLine(detailLine)) {
            LOG.debug("buildAccountingFieldsKey, found ATM fee");
            return buildAccountingFieldsForATMFeeCashAdvance(detailLine); 
        } else {
            String objectCodeForKey = detailLine.getJournalAccountCode();
            objectCodeForKey = convertToFakeObjectCodeIfNecessary(objectCodeForKey, detailLine);
            
            return String.format(ACCOUNTING_FIELDS_KEY_FORMAT,
                    detailLine.getChartOfAccountsCode(),
                    detailLine.getAccountNumber(),
                    defaultToDashesIfBlank(detailLine.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                    objectCodeForKey,
                    defaultToDashesIfBlank(detailLine.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE),
                    defaultToDashesIfBlank(detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                    StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
        }
    }

    protected String buildAccountingFieldsKeyForCashAdvance(ConcurStandardAccountingExtractDetailLine detailLine) {
        if (isAtmCashAdvanceLine(detailLine)) {
            LOG.debug("buildAccountingFieldsKeyForCashAdvance, found ATM cash advance");
            return buildAccountingFieldsForATMCashAdvance(detailLine);

        }else {
            LOG.debug("buildAccountingFieldsKeyForCashAdvance, found a non ATM related cash advance");
            Function<? super String, ? extends ConcurRequestedCashAdvance> cashAdvanceKeyMappingFunction = this::getExistingRequestedCashAdvanceByCashAdvanceKey;
            ConcurRequestedCashAdvance requestedCashAdvance = requestedCashAdvancesByCashAdvanceKey
                    .computeIfAbsent(detailLine.getCashAdvanceKey(), cashAdvanceKeyMappingFunction);
            return buildAccountingFieldsKeyForCashAdvanceWithRequestedCashAdvance(detailLine, requestedCashAdvance);
        }
    }
    
    private String buildAccountingFieldsForATMFeeCashAdvance(ConcurStandardAccountingExtractDetailLine detailLine) {
        return String.format(ACCOUNTING_FIELDS_KEY_FORMAT, collectorHelper.atmFeeDebitChartCode,
                collectorHelper.atmFeeDebitAccountNumber, collectorHelper.atmFeeDebitSubAccountNumber,
                collectorHelper.atmFeeDebitObjectCode, KFSPropertyConstants.SUB_OBJECT_CODE,
                defaultToDashesIfBlank(detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
    }

    private String buildAccountingFieldsForATMCashAdvance(ConcurStandardAccountingExtractDetailLine detailLine) {
        return String.format(ACCOUNTING_FIELDS_KEY_FORMAT, detailLine.getReportChartOfAccountsCode(),
                detailLine.getReportAccountNumber(),
                defaultToDashesIfBlank(detailLine.getReportSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                collectorHelper.atmCashAdvanceObjectCode, KFSPropertyConstants.SUB_OBJECT_CODE,
                StringUtils.EMPTY,
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getReportOrgRefId(), StringUtils.EMPTY));
    }


    protected String buildAccountingFieldsKeyForCashAdvanceWithRequestedCashAdvance(
            ConcurStandardAccountingExtractDetailLine detailLine, ConcurRequestedCashAdvance requestedCashAdvance) {
        if (ObjectUtils.isNull(requestedCashAdvance)) {
            return ORPHANED_CASH_ADVANCES_KEY;
        }
        
        String objectCodeForKey = StringUtils.defaultIfBlank(requestedCashAdvance.getObjectCode(), detailLine.getJournalAccountCode());
        objectCodeForKey = convertToFakeObjectCodeIfNecessary(objectCodeForKey, detailLine);
        
        return String.format(ACCOUNTING_FIELDS_KEY_FORMAT,
                requestedCashAdvance.getChart(),
                requestedCashAdvance.getAccountNumber(),
                defaultToDashesIfBlank(requestedCashAdvance.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                objectCodeForKey,
                defaultToDashesIfBlank(requestedCashAdvance.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(requestedCashAdvance.getProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(requestedCashAdvance.getOrgRefId(), StringUtils.EMPTY));
    }

    protected String buildAccountingFieldsKeyForCorpCardPersonalExpense(ConcurStandardAccountingExtractDetailLine detailLine) {
        String objectCodeForKey = detailLine.getJournalAccountCode();
        objectCodeForKey = convertToFakeObjectCodeIfNecessary(objectCodeForKey, detailLine);
        
        return String.format(ACCOUNTING_FIELDS_KEY_FORMAT,
                detailLine.getReportChartOfAccountsCode(),
                detailLine.getReportAccountNumber(),
                defaultToDashesIfBlank(detailLine.getReportSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                objectCodeForKey,
                defaultToDashesIfBlank(detailLine.getReportSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getReportOrgRefId(), StringUtils.EMPTY));
    }

    protected String convertToFakeObjectCodeIfNecessary(String objectCode, ConcurStandardAccountingExtractDetailLine detailLine) {
        if (Boolean.TRUE.equals(detailLine.getJournalAccountCodeOverridden())) {
            return buildFakeObjectCodeToLeaveDetailLineUnmerged();
        } else {
            return objectCode;
        }
    }

    protected String buildFakeObjectCodeToLeaveDetailLineUnmerged() {
        return FAKE_OBJECT_CODE_PREFIX + nextFakeObjectCode++;
    }

    protected String defaultToDashesIfBlank(String value, String propertyName) {
        return StringUtils.defaultIfBlank(value, collectorHelper.getDashOnlyPropertyValue(propertyName));
    }

    public boolean hasNoErrors() {
        return !hasMissingRequestedCashAdvance;
    }

    public void reportErrors(BiConsumer<ConcurStandardAccountingExtractDetailLine, String> lineErrorReporter) {
        reportAllLinesForGroupAsFailuresDueToOrphanedCashAdvanceLines(lineErrorReporter);
    }

    public void buildAndAddOriginEntries(Consumer<OriginEntryFull> entryConsumer) {
        nextTransactionSequenceNumber = 1;
        unusedCashAdvanceAmountsByReportEntryId.clear();
        unusedCashAdvanceAmountsByReportEntryId.putAll(totalCashAdvanceAmountsByReportEntryId);
        
        for (ConcurDetailLineSubGroupForCollector subGroup : consolidatedRegularLines.values()) {
            addOriginEntriesForCorporateCardLines(entryConsumer, subGroup.getCorporateCardLines());
            addOriginEntriesForCashLines(entryConsumer, subGroup.getCashLines());
        }
        
        for (List<ConcurStandardAccountingExtractDetailLine> cashAdvanceSubGroup : consolidatedCashAdvanceLines.values()) {
            addOriginEntriesForCashAdvanceLines(entryConsumer, cashAdvanceSubGroup);
        }
        
        addOriginEntriesForCorpCardPersonalExpenseDebitLines(entryConsumer);
        addOriginEntriesForCorpCardPersonalExpenseCreditLines(entryConsumer);
    }

    protected void addOriginEntriesForCashLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> cashLines) {
        addOriginEntriesForLines(entryConsumer, cashLines, this::buildOriginEntryForPaymentOffset);
    }

    protected void addOriginEntriesForCorporateCardLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> corporateCardLines) {
        addOriginEntriesForLines(entryConsumer, corporateCardLines, this::buildOriginEntryForCorporateCardOffset);
    }

    protected void addOriginEntriesForCashAdvanceLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> cashAdvanceLines) {
        addOriginEntriesForLines(entryConsumer, cashAdvanceLines, this::doNotBuildAnyOriginEntriesForCashAdvanceOffset);
    }

    /**
     * @param entryConsumer The Consumer to add the generated origin entries to; won't be called if detailLines is empty or has zero-sum lines.
     * @param detailLines The SAE lines to generate a single origin entry from; may be empty.
     * @param offsetGenerator A BiFunction that may create an offset entry using the generated regular entry and the detailLines List.
     */
    protected void addOriginEntriesForLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> detailLines,
            BiFunction<OriginEntryFull, List<ConcurStandardAccountingExtractDetailLine>, Optional<OriginEntryFull>> offsetGenerator) {
        if (CollectionUtils.isEmpty(detailLines)) {
            return;
        }
        
        KualiDecimal totalAmount = calculateTotalAmountForLines(detailLines);
        if (totalAmount.isNonZero()) {
            ConcurStandardAccountingExtractDetailLine firstDetailLine = detailLines.get(0);
            OriginEntryFull originEntry = buildOriginEntry(firstDetailLine, totalAmount);
            entryConsumer.accept(originEntry);
            
            Optional<OriginEntryFull> optionalOffsetEntry = offsetGenerator.apply(originEntry, detailLines);
            if (optionalOffsetEntry.isPresent()) {
                entryConsumer.accept(optionalOffsetEntry.get());
            }
        }
    }

    protected KualiDecimal calculateTotalAmountForLines(List<ConcurStandardAccountingExtractDetailLine> detailLines) {
        return detailLines.stream()
                .map(ConcurStandardAccountingExtractDetailLine::getJournalAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
    }

    protected void addOriginEntriesForCorpCardPersonalExpenseCreditLines(Consumer<OriginEntryFull> entryConsumer) {
        if (consolidatedCorpCardPersonalExpenseCredits.isEmpty()) {
            return;
        }
        
        for (List<ConcurStandardAccountingExtractDetailLine> corpCardPersonalCredits : consolidatedCorpCardPersonalExpenseCredits.values()) {
            KualiDecimal totalSubGroupAmount = calculateTotalAmountForLines(corpCardPersonalCredits);
            if (totalSubGroupAmount.isNonZero()) {
                ConcurStandardAccountingExtractDetailLine firstCreditLine = corpCardPersonalCredits.get(0);
                addOriginEntryForCorpCardPersonalExpense(
                        entryConsumer, firstCreditLine, collectorHelper.getPrepaidOffsetChartCode(),
                        collectorHelper.getPrepaidOffsetAccountNumber(), StringUtils.EMPTY,
                        collectorHelper.getPrepaidOffsetObjectCode(), totalSubGroupAmount);
            }
        }
    }

    protected void addOriginEntriesForCorpCardPersonalExpenseDebitLines(Consumer<OriginEntryFull> entryConsumer) {
        if (consolidatedCorpCardPersonalExpenseDebits.isEmpty()) {
            return;
        }
        
        KualiDecimal netCashAmount = calculateTotalCashAmountNotOffsetByCashAdvances();
        KualiDecimal currentReimbursableCashAmount = netCashAmount;
        
        for (List<ConcurStandardAccountingExtractDetailLine> corpCardPersonalDebits : consolidatedCorpCardPersonalExpenseDebits.values()) {
            KualiDecimal totalSubGroupAmount = calculateTotalAmountForLines(corpCardPersonalDebits);
            if (totalSubGroupAmount.isZero()) {
                continue;
            }
            
            KualiDecimal newReimbursableCashAmount = currentReimbursableCashAmount.subtract(totalSubGroupAmount);
            KualiDecimal paymentOffsetAdjustment = calculatePaymentOffsetAdjustmentForCorpCardPersonalExpenseSubGroup(
                    totalSubGroupAmount, currentReimbursableCashAmount, newReimbursableCashAmount);
            KualiDecimal personalOffsetAdjustment = calculatePersonalOffsetAdjustmentForCorpCardPersonalExpenseSubGroup(
                    totalSubGroupAmount, currentReimbursableCashAmount, newReimbursableCashAmount);
            
            addOriginEntriesForCorpCardPersonalExpenseDebitLinesIfNecessary(
                    entryConsumer, corpCardPersonalDebits, paymentOffsetAdjustment, personalOffsetAdjustment);
            
            currentReimbursableCashAmount = newReimbursableCashAmount;
        }
    }

    protected KualiDecimal calculatePaymentOffsetAdjustmentForCorpCardPersonalExpenseSubGroup(
            KualiDecimal totalSubGroupAmount, KualiDecimal oldReimbursableCashAmount, KualiDecimal newReimbursableCashAmount) {
        if (newReimbursableCashAmount.isNegative()) {
            if (oldReimbursableCashAmount.isPositive()) {
                return oldReimbursableCashAmount;
            } else {
                return KualiDecimal.ZERO;
            }
        } else {
            return totalSubGroupAmount;
        }
    }

    protected KualiDecimal calculatePersonalOffsetAdjustmentForCorpCardPersonalExpenseSubGroup(
            KualiDecimal totalSubGroupAmount, KualiDecimal oldReimbursableCashAmount, KualiDecimal newReimbursableCashAmount) {
        if (newReimbursableCashAmount.isNegative()) {
            if (oldReimbursableCashAmount.isPositive()) {
                return newReimbursableCashAmount.negated();
            } else {
                return totalSubGroupAmount;
            }
        } else {
            return KualiDecimal.ZERO;
        }
    }

    protected void addOriginEntriesForCorpCardPersonalExpenseDebitLinesIfNecessary(Consumer<OriginEntryFull> entryConsumer,
            List<ConcurStandardAccountingExtractDetailLine> debitLines, KualiDecimal paymentOffsetAdjustment, KualiDecimal personalOffsetAdjustment) {
        ConcurStandardAccountingExtractDetailLine firstDebitLine = debitLines.get(0);
        if (paymentOffsetAdjustment.isNonZero()) {
            addOriginEntryForCorpCardPersonalExpense(
                    entryConsumer, firstDebitLine, collectorHelper.getPaymentOffsetObjectCode(), paymentOffsetAdjustment);
        }
        if (personalOffsetAdjustment.isNonZero()) {
            addOriginEntryForCorpCardPersonalExpense(
                    entryConsumer, firstDebitLine, collectorHelper.getPersonalOffsetObjectCode(), personalOffsetAdjustment);
        }
    }

    protected void addOriginEntryForCorpCardPersonalExpense(
            Consumer<OriginEntryFull> entryConsumer, ConcurStandardAccountingExtractDetailLine detailLine, String objectCode, KualiDecimal amount) {
        addOriginEntryForCorpCardPersonalExpense(entryConsumer, detailLine, detailLine.getReportChartOfAccountsCode(),
                detailLine.getReportAccountNumber(), detailLine.getReportSubAccountNumber(), objectCode, amount);
    }

    protected void addOriginEntryForCorpCardPersonalExpense(
            Consumer<OriginEntryFull> entryConsumer, ConcurStandardAccountingExtractDetailLine detailLine,
            String chartCode, String accountNumber, String subAccountNumber, String objectCode, KualiDecimal amount) {
        OriginEntryFull originEntry = buildCorpCardPersonalExpenseOriginEntry(
                detailLine, chartCode, accountNumber, subAccountNumber, objectCode, StringUtils.EMPTY, amount);
        entryConsumer.accept(originEntry);
    }

    protected KualiDecimal calculateTotalCashAmountNotOffsetByCashAdvances() {
        List<ConcurStandardAccountingExtractDetailLine> allCashLines = consolidatedRegularLines.values()
                .stream()
                .flatMap(ConcurDetailLineSubGroupForCollector::getCashLinesAsStream)
                .collect(Collectors.toCollection(ArrayList::new));
        
        KualiDecimal totalCashAmount = calculateTotalAmountForLines(allCashLines);
        KualiDecimal totalCashAdvanceAmount = calculateTotalAmountForCashAdvanceLinesReferencedByRegularLines(allCashLines);
        
        return totalCashAmount.add(totalCashAdvanceAmount);
    }

    protected Optional<OriginEntryFull> buildOriginEntryForPaymentOffset(
            OriginEntryFull cashEntry, List<ConcurStandardAccountingExtractDetailLine> cashLines) {
        KualiDecimal cashAmount = getSignedAmountFromOriginEntry(cashEntry);
        KualiDecimal cashAdvanceAmount = calculateAndUpdateUsableAmountForCashAdvanceLinesReferencedByRegularLines(cashLines);
        KualiDecimal cashAmountToOffset = cashAmount.add(cashAdvanceAmount);
        if (cashAmountToOffset.isZero()) {
            return Optional.empty();
        }
        
        OriginEntryFull offsetEntry = buildOffsetOriginEntry(cashEntry, cashAmountToOffset);
        offsetEntry.setFinancialObjectCode(collectorHelper.getPaymentOffsetObjectCode());
        offsetEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        
        return Optional.of(offsetEntry);
    }

    protected KualiDecimal calculateTotalAmountForCashAdvanceLinesReferencedByRegularLines(
            List<ConcurStandardAccountingExtractDetailLine> regularDetailLines) {
        if (totalCashAdvanceAmountsByReportEntryId.isEmpty()) {
            return KualiDecimal.ZERO;
        }
        
        KualiDecimal totalAmount = KualiDecimal.ZERO;
        String[] reportEntryIds = regularDetailLines.stream()
                .map(ConcurStandardAccountingExtractDetailLine::getReportEntryId)
                .distinct()
                .toArray(String[]::new);
        
        for (String reportEntryId : reportEntryIds) {
            KualiDecimal subTotalAmount = totalCashAdvanceAmountsByReportEntryId.get(reportEntryId);
            if (subTotalAmount != null) {
                totalAmount = totalAmount.add(subTotalAmount);
            }
        }
        
        return totalAmount;
    }

    protected KualiDecimal calculateAndUpdateUsableAmountForCashAdvanceLinesReferencedByRegularLines(
            List<ConcurStandardAccountingExtractDetailLine> regularDetailLines) {
        if (unusedCashAdvanceAmountsByReportEntryId.isEmpty()) {
            return KualiDecimal.ZERO;
        }
        
        KualiDecimal usableAmount = KualiDecimal.ZERO;
        
        for (ConcurStandardAccountingExtractDetailLine detailLine : regularDetailLines) {
            KualiDecimal lineAmount = detailLine.getJournalAmount();
            KualiDecimal oldUnusedAmount = unusedCashAdvanceAmountsByReportEntryId.getOrDefault(
                    detailLine.getReportEntryId(), KualiDecimal.ZERO);
            if (lineAmount.isPositive() && oldUnusedAmount.isNegative()) {
                KualiDecimal newUnusedAmount = unusedCashAdvanceAmountsByReportEntryId.merge(
                        detailLine.getReportEntryId(), lineAmount, KualiDecimal::add);
                if (newUnusedAmount.isPositive()) {
                    usableAmount = usableAmount.add(oldUnusedAmount);
                } else {
                    usableAmount = usableAmount.add(lineAmount.negated());
                }
            }
        }
        
        return usableAmount;
    }

    protected Optional<OriginEntryFull> buildOriginEntryForCorporateCardOffset(
            OriginEntryFull corporateCardEntry, List<ConcurStandardAccountingExtractDetailLine> corporateCardLines) {
        KualiDecimal corporateCardAmount = getSignedAmountFromOriginEntry(corporateCardEntry);
        
        OriginEntryFull offsetEntry = buildOffsetOriginEntry(corporateCardEntry, corporateCardAmount);
        offsetEntry.setChartOfAccountsCode(collectorHelper.getPrepaidOffsetChartCode());
        offsetEntry.setAccountNumber(collectorHelper.getPrepaidOffsetAccountNumber());
        offsetEntry.setSubAccountNumber(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        offsetEntry.setFinancialObjectCode(collectorHelper.getPrepaidOffsetObjectCode());
        offsetEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        
        return Optional.of(offsetEntry);
    }

    protected Optional<OriginEntryFull> doNotBuildAnyOriginEntriesForCashAdvanceOffset(
            OriginEntryFull cashAdvanceEntry, List<ConcurStandardAccountingExtractDetailLine> cashAdvanceLines) {
        return Optional.empty();
    }

    protected OriginEntryFull buildOffsetOriginEntry(OriginEntryFull baseEntry, KualiDecimal amountToOffset) {
        OriginEntryFull offsetEntry = new OriginEntryFull(baseEntry);
        KualiDecimal offsetAmount = amountToOffset.negated();
        setTransactionSequenceNumberToNextAvailableValue(offsetEntry);
        configureAmountAndDebitCreditCodeOnOriginEntry(offsetEntry, offsetAmount);
        return offsetEntry;
    }

    protected OriginEntryFull buildOriginEntry(ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        if (collectorHelper.isCashAdvanceLine(detailLine)) {
            return buildCashAdvanceOriginEntry(detailLine, amount);
        } else {
            return buildRegularOriginEntry(detailLine, amount);
        }
    }

    protected OriginEntryFull buildCashAdvanceOriginEntry(ConcurStandardAccountingExtractDetailLine detailLine,
            KualiDecimal amount) {
        OriginEntryFull originEntry;
        if (isAtmCashAdvanceLine(detailLine)){
            originEntry = buildOriginEntryFromDetailLineForATMCashAdvance(detailLine);
        } else {
            ConcurRequestedCashAdvance requestedCashAdvance = requestedCashAdvancesByCashAdvanceKey
                    .get(detailLine.getCashAdvanceKey());
            if (requestedCashAdvance == null) {
                throw new IllegalStateException(
                        "Did not find cash advance when building Collector line; this should NEVER happen. Key: "
                                + detailLine.getCashAdvanceKey());
            }
            originEntry = buildOriginEntryFromRequestedCashAdvance(detailLine, requestedCashAdvance);
        }

        configureOriginEntryGeneratedFromLine(originEntry, detailLine, amount);
        return originEntry;
    }
    
    private OriginEntryFull buildOriginEntryFromDetailLineForATMCashAdvance(ConcurStandardAccountingExtractDetailLine detailLine) {
        OriginEntryFull originEntry = new OriginEntryFull();
        originEntry.setChartOfAccountsCode(detailLine.getReportChartOfAccountsCode());
        originEntry.setAccountNumber(detailLine.getReportAccountNumber());
        originEntry.setSubAccountNumber(defaultToDashesIfBlank(detailLine.getReportSubAccountNumber(),
                KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(collectorHelper.atmCashAdvanceObjectCode);
        originEntry.setFinancialSubObjectCode(StringUtils.EMPTY);
        originEntry.setProjectCode(
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildOriginEntryFromDetailLineForATMCashAdvance, origin entry: " + originEntry.toString());
        }
        return originEntry;
    }
    
    private OriginEntryFull buildOriginEntryFromRequestedCashAdvance(
            ConcurStandardAccountingExtractDetailLine detailLine, ConcurRequestedCashAdvance requestedCashAdvance) {
        OriginEntryFull originEntry = new OriginEntryFull();
        originEntry.setChartOfAccountsCode(requestedCashAdvance.getChart());
        originEntry.setAccountNumber(requestedCashAdvance.getAccountNumber());
        originEntry.setSubAccountNumber(defaultToDashesIfBlank(requestedCashAdvance.getSubAccountNumber(),
                KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(
                StringUtils.defaultIfBlank(requestedCashAdvance.getObjectCode(), detailLine.getJournalAccountCode()));
        originEntry.setFinancialSubObjectCode(
                defaultToDashesIfBlank(requestedCashAdvance.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE));
        originEntry.setProjectCode(
                defaultToDashesIfBlank(requestedCashAdvance.getProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(
                StringUtils.defaultIfBlank(requestedCashAdvance.getOrgRefId(), StringUtils.EMPTY));
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildOriginEntryFromRequestedCashAdvance, origin entry: " + originEntry.toString());
        }
        return originEntry;
    }
                 

    protected OriginEntryFull buildCorpCardPersonalExpenseOriginEntry(
            ConcurStandardAccountingExtractDetailLine detailLine, String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, KualiDecimal amount) {
        OriginEntryFull originEntry = new OriginEntryFull();
        
        originEntry.setChartOfAccountsCode(chartCode);
        originEntry.setAccountNumber(accountNumber);
        originEntry.setSubAccountNumber(defaultToDashesIfBlank(subAccountNumber, KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(objectCode);
        originEntry.setFinancialSubObjectCode(
                defaultToDashesIfBlank(subObjectCode, KFSPropertyConstants.SUB_OBJECT_CODE));
        originEntry.setProjectCode(
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(
                StringUtils.defaultIfBlank(detailLine.getReportOrgRefId(), StringUtils.EMPTY));
        
        configureOriginEntryGeneratedFromLine(originEntry, detailLine, amount);
        
        return originEntry;
    }

    protected OriginEntryFull buildRegularOriginEntry(ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        LOG.info("buildRegularOriginEntry, entering");
        OriginEntryFull originEntry = new OriginEntryFull();
        if (isAtmFeeLine(detailLine)) {
            LOG.info("buildRegularOriginEntry, ATM fee");
            originEntry.setChartOfAccountsCode(collectorHelper.atmFeeDebitChartCode);
            originEntry.setAccountNumber(collectorHelper.atmFeeDebitAccountNumber);
            originEntry.setSubAccountNumber(collectorHelper.atmFeeDebitSubAccountNumber);
            originEntry.setFinancialObjectCode(collectorHelper.atmFeeDebitObjectCode);
        } else {
            LOG.info("buildRegularOriginEntry, regular");
            originEntry.setChartOfAccountsCode(detailLine.getChartOfAccountsCode());
            originEntry.setAccountNumber(detailLine.getAccountNumber());
            originEntry.setSubAccountNumber(defaultToDashesIfBlank(detailLine.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
            originEntry.setFinancialObjectCode(detailLine.getJournalAccountCode());
            originEntry.setFinancialSubObjectCode(defaultToDashesIfBlank(detailLine.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE));
        }
        originEntry.setProjectCode(defaultToDashesIfBlank(detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
        
        configureOriginEntryGeneratedFromLine(originEntry, detailLine, amount);
        
        return originEntry;
    }

    protected void configureOriginEntryGeneratedFromLine(
            OriginEntryFull originEntry, ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        // Default constructor sets fiscal year to zero; need to forcibly clear it to allow auto-setup by the Poster, as per the spec.
        originEntry.setUniversityFiscalYear(null);
        
        originEntry.setFinancialBalanceTypeCode(collectorHelper.getActualFinancialBalanceTypeCode());
        originEntry.setFinancialDocumentTypeCode(collectorHelper.getDocumentTypeCode());
        originEntry.setFinancialSystemOriginationCode(collectorHelper.getSystemOriginationCode());
        originEntry.setDocumentNumber(buildDocumentNumber(detailLine));
        originEntry.setTransactionLedgerEntryDescription(buildTransactionDescription(detailLine));
        originEntry.setTransactionDate(collectorHelper.getTransmissionDate());
        
        setTransactionSequenceNumberToNextAvailableValue(originEntry);
        configureAmountAndDebitCreditCodeOnOriginEntry(originEntry, amount);
    }

    protected void setTransactionSequenceNumberToNextAvailableValue(OriginEntryFull originEntry) {
        originEntry.setTransactionLedgerEntrySequenceNumber(nextTransactionSequenceNumber++);
    }

    protected String buildDocumentNumber(ConcurStandardAccountingExtractDetailLine detailLine) {
        return collectorHelper.getDocumentTypeCode() + StringUtils.left(detailLine.getReportId(), REPORT_ID_LENGTH_FOR_DOC_NUMBER);
    }

    protected String buildTransactionDescription(ConcurStandardAccountingExtractDetailLine detailLine) {
        String formattedEndDate = collectorHelper.formatDate(detailLine.getReportEndDate(), ConcurConstants.DATE_FORMAT);
        
        return String.format(TRANSACTION_DESCRIPTION_FORMAT,
                StringUtils.left(detailLine.getEmployeeLastName(), NAME_LENGTH_FOR_DESCRIPTION),
                StringUtils.left(detailLine.getEmployeeFirstName(), NAME_LENGTH_FOR_DESCRIPTION),
                formattedEndDate);
    }

    protected void configureAmountAndDebitCreditCodeOnOriginEntry(OriginEntryFull originEntry, KualiDecimal amount) {
        String debitCreditCode = getGeneralLedgerDebitCreditCode(amount);
        originEntry.setTransactionDebitCreditCode(debitCreditCode);
        originEntry.setTransactionLedgerEntryAmount(amount.abs());
    }

    protected String getGeneralLedgerDebitCreditCode(KualiDecimal amount) {
        if (amount.isPositive()) {
            return KFSConstants.GL_DEBIT_CODE;
        } else if (amount.isNegative()) {
            return KFSConstants.GL_CREDIT_CODE;
        } else {
            throw new IllegalArgumentException("Cannot have a zero amount for an Origin Entry");
        }
    }

    protected KualiDecimal getSignedAmountFromOriginEntry(OriginEntryFull originEntry) {
        switch (originEntry.getTransactionDebitCreditCode()) {
            case KFSConstants.GL_DEBIT_CODE :
                return originEntry.getTransactionLedgerEntryAmount();
            case KFSConstants.GL_CREDIT_CODE :
                return originEntry.getTransactionLedgerEntryAmount().negated();
            default :
                throw new IllegalArgumentException("originEntry does not have a valid debit/credit code");
        }
    }

    protected void reportAllLinesForGroupAsFailuresDueToOrphanedCashAdvanceLines(
            BiConsumer<ConcurStandardAccountingExtractDetailLine, String> lineErrorReporter) {
        List<ConcurStandardAccountingExtractDetailLine> linesForGroup = getDetailLinesSortedBySequenceNumberNumerically();
        Set<String> sequenceNumbersForOrphanedCashAdvanceLines = getSequenceNumbersForOrphanedCashAdvanceLines();
        
        for (ConcurStandardAccountingExtractDetailLine detailLine : linesForGroup) {
            String errorMessage;
            if (sequenceNumbersForOrphanedCashAdvanceLines.contains(detailLine.getSequenceNumber())) {
                errorMessage = collectorHelper.getFormattedValidationMessage(
                        ConcurKeyConstants.CONCUR_SAE_ORPHANED_CASH_ADVANCE, detailLine.getCashAdvanceKey());
            } else {
                errorMessage = collectorHelper.getValidationMessage(ConcurKeyConstants.CONCUR_SAE_GROUP_WITH_ORPHANED_CASH_ADVANCE);
            }
            lineErrorReporter.accept(detailLine, errorMessage);
        }
    }

    protected Set<String> getSequenceNumbersForOrphanedCashAdvanceLines() {
        List<ConcurStandardAccountingExtractDetailLine> orphanedKeyList = consolidatedCashAdvanceLines.get(ORPHANED_CASH_ADVANCES_KEY);
        if (CollectionUtils.isEmpty(orphanedKeyList)) {
            LOG.debug("getSequenceNumbersForOrphanedCashAdvanceLines, found an empty list of orphaned keys, returning empty list.");
            return Collections.EMPTY_SET;
        } else {
            LOG.debug("getSequenceNumbersForOrphanedCashAdvanceLines, found a list of orphaned keys, streaming the list.");
            return orphanedKeyList.stream().map(ConcurStandardAccountingExtractDetailLine::getSequenceNumber)
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }

    protected List<ConcurStandardAccountingExtractDetailLine> getDetailLinesSortedBySequenceNumberNumerically() {
        List<ConcurStandardAccountingExtractDetailLine> linesForGroup = new ArrayList<>();
        
        for (ConcurDetailLineSubGroupForCollector subGroup : consolidatedRegularLines.values()) {
            linesForGroup.addAll(subGroup.getCashLines());
            linesForGroup.addAll(subGroup.getCorporateCardLines());
        }
        for (List<ConcurStandardAccountingExtractDetailLine> cashAdvanceLines : consolidatedCashAdvanceLines.values()) {
            linesForGroup.addAll(cashAdvanceLines);
        }
        
        Collections.sort(linesForGroup, this::compareBySequenceNumberAsPositiveNumericString);
        return linesForGroup;
    }

    protected int compareBySequenceNumberAsPositiveNumericString(
            ConcurStandardAccountingExtractDetailLine line1, ConcurStandardAccountingExtractDetailLine line2) {
        int lengthComparison = line1.getSequenceNumber().length() - line2.getSequenceNumber().length();
        if (lengthComparison != 0) {
            return lengthComparison;
        }
        return line1.getSequenceNumber().compareTo(line2.getSequenceNumber());
    }
    
    private boolean isAtmFeeLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        return StringUtils.equalsIgnoreCase(detailLine.getExpenseType(), ConcurConstants.EXPENSE_TYPE_ATM_FEE);
    }
    
    private boolean isAtmCashAdvanceLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        return StringUtils.equalsIgnoreCase(detailLine.getCashAdvancePaymentCode(), ConcurConstants.CASH_ADVANCE_PAYMENT_CODE_NAME_UNIVERSITY_BILLED_OR_PAID);
    }

}
