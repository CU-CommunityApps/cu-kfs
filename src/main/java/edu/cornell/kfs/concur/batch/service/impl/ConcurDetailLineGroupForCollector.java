package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

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
    protected static final String FAKE_OBJECT_CODE_PREFIX = "?NONE?";
    protected static final String ORPHANED_CASH_ADVANCES_KEY = "ORPHANED_CASH_ADVANCES";
    protected static final String ACCOUNTING_FIELDS_KEY_FORMAT = "%s|%s|%s|%s|%s|%s|%s";
    protected static final String TRANSACTION_DESCRIPTION_FORMAT = "%s,%s,%s";
    protected static final int REPORT_ID_LENGTH_FOR_DOC_NUMBER = 10;
    protected static final int NAME_LENGTH_FOR_DESCRIPTION = 14;

    protected String reportId;
    protected ConcurDetailLineGroupForCollectorHelper collectorHelper;
    protected Set<String> orderedKeysForGroupingRegularCashAndCorpCardLines;
    protected Map<ConcurSAECollectorLineType, Map<String, List<ConcurStandardAccountingExtractDetailLine>>> consolidatedLines;
    protected Map<String, KualiDecimal> totalCashAdvanceAmountsByReportEntryId;
    protected Map<String, KualiDecimal> unusedCashAdvanceAmountsByReportEntryId;
    protected Map<String, ConcurRequestedCashAdvance> requestedCashAdvancesByCashAdvanceKey;
    protected boolean hasMissingRequestedCashAdvance;
    protected int nextFakeObjectCode;
    protected int nextTransactionSequenceNumber;

    public ConcurDetailLineGroupForCollector(String reportId, ConcurDetailLineGroupForCollectorHelper collectorHelper) {
        this.reportId = reportId;
        this.collectorHelper = collectorHelper;
        this.orderedKeysForGroupingRegularCashAndCorpCardLines = new LinkedHashSet<>();
        this.consolidatedLines = new EnumMap<>(ConcurSAECollectorLineType.class);
        this.totalCashAdvanceAmountsByReportEntryId = new LinkedHashMap<>();
        this.unusedCashAdvanceAmountsByReportEntryId = new LinkedHashMap<>();
        this.requestedCashAdvancesByCashAdvanceKey = new LinkedHashMap<>();
        this.hasMissingRequestedCashAdvance = false;
        this.nextFakeObjectCode = 1;
        this.nextTransactionSequenceNumber = 1;
    }

    public String getReportId() {
        return reportId;
    }

    public void addDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        if (collectorHelper.isCashAdvanceLine(detailLine)) {
            addCashAdvanceLine(detailLine);
        } else if (collectorHelper.isAtmFeeDebitLine(detailLine)) {
            addAtmFeeDebitLine(detailLine);
        } else if (collectorHelper.lineRepresentsPersonalExpenseChargedToCorporateCard(detailLine)) {
            addCorpCardPersonalExpenseDetailLine(detailLine);
        } else {
            addRegularDetailLine(detailLine);
        }
    }

    protected void addRegularDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        String accountingFieldsKey = buildAccountingFieldsKey(detailLine);
        ConcurSAECollectorLineType lineType = getLineTypeForRegularLine(detailLine);
        orderedKeysForGroupingRegularCashAndCorpCardLines.add(accountingFieldsKey);
        addLineToSubGroup(lineType, accountingFieldsKey, detailLine);
    }

    protected ConcurSAECollectorLineType getLineTypeForRegularLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        switch (detailLine.getPaymentCode()) {
            case ConcurConstants.PAYMENT_CODE_CASH :
                return ConcurSAECollectorLineType.CASH;
            case ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID :
                return ConcurSAECollectorLineType.CORP_CARD;
            case ConcurConstants.PAYMENT_CODE_PRE_PAID_OR_OTHER :
                return ConcurSAECollectorLineType.PRE_PAID_OR_OTHER;
            default :
                throw new IllegalArgumentException("Found an unexpected payment code for regular detail line; this should NEVER happen! Code: "
                        + detailLine.getPaymentCode());
        }
    }

    protected void addCashAdvanceLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        if (collectorHelper.isAtmCashAdvanceLine(detailLine)) {
            addAtmCashAdvanceLine(detailLine);
        } else {
            addRequestedCashAdvanceLine(detailLine);
        }
    }

    protected void addRequestedCashAdvanceLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        String accountingFieldsKey = buildAccountingFieldsKeyForRequestedCashAdvance(detailLine);
        addLineToSubGroup(ConcurSAECollectorLineType.REQUESTED_CASH_ADVANCE, accountingFieldsKey, detailLine);
        recordCashAdvanceAmountForPaymentOffsetCalculations(detailLine);
    }

    protected void addAtmCashAdvanceLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        String accountingFieldsKey = buildAccountingFieldsKeyForAtmCashAdvance(detailLine);
        addLineToSubGroup(ConcurSAECollectorLineType.ATM_CASH_ADVANCE, accountingFieldsKey, detailLine);
        
        if (collectorHelper.isAtmCashAdvanceLineWithUnusedAmount(detailLine)) {
            addUnusedAtmAmountLineForOffset(detailLine);
        } else if (!collectorHelper.isAtmFeeCreditLine(detailLine)) {
            recordCashAdvanceAmountForPaymentOffsetCalculations(detailLine);
        }
    }

    protected void recordCashAdvanceAmountForPaymentOffsetCalculations(ConcurStandardAccountingExtractDetailLine detailLine) {
        totalCashAdvanceAmountsByReportEntryId.merge(
                detailLine.getReportEntryId(), detailLine.getJournalAmount(), KualiDecimal::add);
    }

    protected void addUnusedAtmAmountLineForOffset(ConcurStandardAccountingExtractDetailLine detailLine) {
        String accountingFieldsKey = buildAccountingFieldsKeyForUnusedAtmCashAdvanceAmount(detailLine);
        addLineToSubGroup(ConcurSAECollectorLineType.ATM_UNUSED_CASH_ADVANCE, accountingFieldsKey, detailLine);
    }

    protected void addAtmFeeDebitLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        String accountingFieldsKey = buildAccountingFieldsKeyForAtmFeeDebit(detailLine);
        addLineToSubGroup(ConcurSAECollectorLineType.ATM_FEE_DEBIT, accountingFieldsKey, detailLine);
    }

    protected void addCorpCardPersonalExpenseDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        String accountingFieldsKey = buildAccountingFieldsKeyForCorpCardPersonalExpense(detailLine);
        ConcurSAECollectorLineType lineType = getConsolidatedPersonalExpenseTypeForLine(detailLine);
        addLineToSubGroup(lineType, accountingFieldsKey, detailLine);
    }

    protected ConcurSAECollectorLineType getConsolidatedPersonalExpenseTypeForLine(
            ConcurStandardAccountingExtractDetailLine detailLine) {
        if (detailLine.getJournalAmount().isPositive()) {
            return getConsolidatedPersonalExpenseTypeForDebitLine(detailLine);
        } else {
            return getConsolidatedPersonalExpenseTypeForCreditLine(detailLine);
        }
    }

    protected ConcurSAECollectorLineType getConsolidatedPersonalExpenseTypeForDebitLine(
            ConcurStandardAccountingExtractDetailLine detailLine) {
        if (collectorHelper.lineRepresentsReturnOfCorporateCardPersonalExpenseToUser(detailLine)) {
            return ConcurSAECollectorLineType.CORP_CARD_PERSONAL_CREDIT;
        } else {
            return ConcurSAECollectorLineType.CORP_CARD_PERSONAL_DEBIT;
        }
    }

    protected ConcurSAECollectorLineType getConsolidatedPersonalExpenseTypeForCreditLine(
            ConcurStandardAccountingExtractDetailLine detailLine) {
        if (collectorHelper.lineRepresentsReturnOfCorporateCardPersonalExpenseToUniversity(detailLine)) {
            return ConcurSAECollectorLineType.CORP_CARD_PERSONAL_DEBIT;
        } else {
            return ConcurSAECollectorLineType.CORP_CARD_PERSONAL_CREDIT;
        }
    }

    protected void addLineToSubGroup(
            ConcurSAECollectorLineType lineType, String accountingFieldsKey, ConcurStandardAccountingExtractDetailLine detailLine) {
        Map<String, List<ConcurStandardAccountingExtractDetailLine>> consolidatedLinesSubMap = consolidatedLines.computeIfAbsent(
                lineType, (key) -> new LinkedHashMap<>());
        consolidatedLinesSubMap.computeIfAbsent(accountingFieldsKey, (key) -> new ArrayList<>())
                .add(detailLine);
    }

    protected List<ConcurStandardAccountingExtractDetailLine> getSubGroup(ConcurSAECollectorLineType lineType, String accountingFieldsKey) {
        return getLineMapOfType(lineType).getOrDefault(accountingFieldsKey, Collections.emptyList());
    }

    protected Collection<List<ConcurStandardAccountingExtractDetailLine>> getSubGroupsOfType(ConcurSAECollectorLineType lineType) {
        return getLineMapOfType(lineType).values();
    }

    protected Map<String, List<ConcurStandardAccountingExtractDetailLine>> getLineMapOfType(ConcurSAECollectorLineType lineType) {
        return consolidatedLines.getOrDefault(lineType, Collections.emptyMap());
    }

    protected ConcurRequestedCashAdvance getExistingRequestedCashAdvanceByCashAdvanceKey(String cashAdvanceKey) {
        ConcurRequestedCashAdvance requestedCashAdvance = collectorHelper.getRequestedCashAdvanceByCashAdvanceKey(cashAdvanceKey);
        if (ObjectUtils.isNull(requestedCashAdvance)) {
            hasMissingRequestedCashAdvance = true;
        }
        return requestedCashAdvance;
    }

    protected String buildAccountingFieldsKey(ConcurStandardAccountingExtractDetailLine detailLine) {
        String objectCodeForKey = detailLine.getJournalAccountCode();
        objectCodeForKey = convertToFakeObjectCodeIfNecessary(objectCodeForKey, detailLine);
        
        return buildAccountingFieldsKey(
                detailLine.getChartOfAccountsCode(),
                detailLine.getAccountNumber(),
                defaultToDashesIfBlank(detailLine.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                objectCodeForKey,
                defaultToDashesIfBlank(detailLine.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
    }

    protected String buildAccountingFieldsKeyForAtmCashAdvance(ConcurStandardAccountingExtractDetailLine detailLine) {
        return buildAccountingFieldsKey(
                collectorHelper.getPrepaidOffsetChartCode(),
                collectorHelper.getPrepaidOffsetAccountNumber(),
                collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                collectorHelper.getPrepaidOffsetObjectCode(),
                collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getReportOrgRefId(), StringUtils.EMPTY));
    }

    protected String buildAccountingFieldsKeyForRequestedCashAdvance(ConcurStandardAccountingExtractDetailLine detailLine) {
        ConcurRequestedCashAdvance requestedCashAdvance = requestedCashAdvancesByCashAdvanceKey.computeIfAbsent(
                detailLine.getCashAdvanceKey(), this::getExistingRequestedCashAdvanceByCashAdvanceKey);
        if (ObjectUtils.isNull(requestedCashAdvance)) {
            return ORPHANED_CASH_ADVANCES_KEY;
        }
        
        String objectCodeForKey = StringUtils.defaultIfBlank(requestedCashAdvance.getObjectCode(), detailLine.getJournalAccountCode());
        objectCodeForKey = convertToFakeObjectCodeIfNecessary(objectCodeForKey, detailLine);
        
        return buildAccountingFieldsKey(
                requestedCashAdvance.getChart(),
                requestedCashAdvance.getAccountNumber(),
                defaultToDashesIfBlank(requestedCashAdvance.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                objectCodeForKey,
                defaultToDashesIfBlank(requestedCashAdvance.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(requestedCashAdvance.getProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(requestedCashAdvance.getOrgRefId(), StringUtils.EMPTY));
    }

    protected String buildAccountingFieldsKeyForAtmFeeDebit(ConcurStandardAccountingExtractDetailLine detailLine) {
        return buildAccountingFieldsKey(
                collectorHelper.getAtmFeeDebitChartCode(),
                collectorHelper.getAtmFeeDebitAccountNumber(),
                collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                collectorHelper.getAtmFeeDebitObjectCode(),
                collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getReportOrgRefId(), StringUtils.EMPTY));
    }

    protected String buildAccountingFieldsKeyForUnusedAtmCashAdvanceAmount(ConcurStandardAccountingExtractDetailLine detailLine) {
        return buildAccountingFieldsKey(
                detailLine.getReportChartOfAccountsCode(),
                detailLine.getReportAccountNumber(),
                defaultToDashesIfBlank(detailLine.getReportSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                collectorHelper.getPrepaidOffsetObjectCode(),
                collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getReportOrgRefId(), StringUtils.EMPTY));
    }

    protected String buildAccountingFieldsKeyForCorpCardPersonalExpense(ConcurStandardAccountingExtractDetailLine detailLine) {
        String objectCodeForKey = detailLine.getJournalAccountCode();
        objectCodeForKey = convertToFakeObjectCodeIfNecessary(objectCodeForKey, detailLine);
        
        return buildAccountingFieldsKey(
                detailLine.getReportChartOfAccountsCode(),
                detailLine.getReportAccountNumber(),
                defaultToDashesIfBlank(detailLine.getReportSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                objectCodeForKey,
                defaultToDashesIfBlank(detailLine.getReportSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getReportOrgRefId(), StringUtils.EMPTY));
    }

    protected String buildAccountingFieldsKey(String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, String projectCode, String orgRefId) {
        return String.format(ACCOUNTING_FIELDS_KEY_FORMAT, chartCode, accountNumber, subAccountNumber,
                objectCode, subObjectCode, projectCode, orgRefId);
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

    public void reportErrors(TriConsumer<ConcurStandardAccountingExtractDetailLine, String, Boolean> lineErrorReporter) {
        reportAllLinesForGroupAsFailuresDueToOrphanedCashAdvanceLines(lineErrorReporter);
    }

    public void buildAndAddOriginEntries(Consumer<OriginEntryFull> entryConsumer) {
        nextTransactionSequenceNumber = 1;
        unusedCashAdvanceAmountsByReportEntryId.clear();
        unusedCashAdvanceAmountsByReportEntryId.putAll(totalCashAdvanceAmountsByReportEntryId);
        List<OriginEntryFull> temporaryCollectorEntries = new ArrayList<OriginEntryFull>();
        List<OriginEntryFull> temporaryCorporateCardPersonalExpenseCollectorEntries = new ArrayList<OriginEntryFull>();
        
        for (String keyForGroupingOfRegularLines : orderedKeysForGroupingRegularCashAndCorpCardLines) {
            addOriginEntriesForCorporateCardLines(temporaryCollectorEntries::add,
                    getSubGroup(ConcurSAECollectorLineType.CORP_CARD, keyForGroupingOfRegularLines));
            addOriginEntriesForCashLines(temporaryCollectorEntries::add,
                    getSubGroup(ConcurSAECollectorLineType.CASH, keyForGroupingOfRegularLines));
        }
        
        for (List<ConcurStandardAccountingExtractDetailLine> atmFeeDebitSubGroup : getSubGroupsOfType(ConcurSAECollectorLineType.ATM_FEE_DEBIT)) {
            addOriginEntriesForAtmFeeDebitLines(temporaryCollectorEntries::add, atmFeeDebitSubGroup);
        }
        
        for (List<ConcurStandardAccountingExtractDetailLine> cashAdvanceSubGroup : getSubGroupsOfType(ConcurSAECollectorLineType.REQUESTED_CASH_ADVANCE)) {
            addOriginEntriesForRequestedCashAdvanceLines(temporaryCollectorEntries::add, cashAdvanceSubGroup);
        }
        
        for (List<ConcurStandardAccountingExtractDetailLine> atmCashAdvanceSubGroup : getSubGroupsOfType(ConcurSAECollectorLineType.ATM_CASH_ADVANCE)) {
            addOriginEntriesForAtmCashAdvanceLines(temporaryCollectorEntries::add, atmCashAdvanceSubGroup);
        }
        
        for (List<ConcurStandardAccountingExtractDetailLine> unusedAtmAmountSubGroup : getSubGroupsOfType(
                ConcurSAECollectorLineType.ATM_UNUSED_CASH_ADVANCE)) {
            addOffsetOriginEntriesForUnusedAtmCashAdvanceAmountLines(temporaryCollectorEntries::add, unusedAtmAmountSubGroup);
        }
        
        addOriginEntriesForCorpCardPersonalExpenseDebitLines(temporaryCollectorEntries, temporaryCorporateCardPersonalExpenseCollectorEntries);
        addOriginEntriesForCorpCardPersonalExpenseCreditLines(temporaryCollectorEntries::add);

        addEntries(entryConsumer, temporaryCollectorEntries);
    }

    private void cleanupCashAndAddCorporateCardPersonalExpenseEntries(List<OriginEntryFull> temporaryCollectorEntries,
            List<OriginEntryFull> temporaryCorporateCardPersonalExpenseCollectorEntries) {

        List<OriginEntryFull> paymentOffsetCashEntries = temporaryCollectorEntries.stream()
                .filter(entry -> collectorHelper.paymentOffsetObjectCode.equalsIgnoreCase(entry.getFinancialObjectCode())).collect(Collectors.toList());
        KualiDecimal cashTotal = calculateTotalAmountForEntries(paymentOffsetCashEntries);

        List<OriginEntryFull> paymentOffsetCorporateCardPersonalExpenseDebitEntries = temporaryCorporateCardPersonalExpenseCollectorEntries.stream()
                .filter(entry -> collectorHelper.paymentOffsetObjectCode.equalsIgnoreCase(entry.getFinancialObjectCode())).collect(Collectors.toList());
        List<OriginEntryFull> nonPaymentOffsetCorporateCardPersonalExpenseDebitEntries = temporaryCorporateCardPersonalExpenseCollectorEntries.stream()
                .filter(entry -> !collectorHelper.paymentOffsetObjectCode.equalsIgnoreCase(entry.getFinancialObjectCode())).collect(Collectors.toList());

        KualiDecimal corporateCardPersonalExpenseDebitTotal = calculateTotalAmountForEntries(paymentOffsetCorporateCardPersonalExpenseDebitEntries);

        if (cashTotal.isGreaterEqual(corporateCardPersonalExpenseDebitTotal)) {
            KualiDecimal totalDeductionsLeft = corporateCardPersonalExpenseDebitTotal;
            for (OriginEntryFull entry : paymentOffsetCashEntries) {
                KualiDecimal newTransactionAmount = entry.getTransactionLedgerEntryAmount().subtract(totalDeductionsLeft);
                if (newTransactionAmount.isNegative()) {
                    totalDeductionsLeft = newTransactionAmount.abs();
                    newTransactionAmount = KualiDecimal.ZERO;
                } else {
                    totalDeductionsLeft = KualiDecimal.ZERO;
                }
                if (newTransactionAmount.isZero()) {
                    temporaryCollectorEntries.remove(entry);
                } else {
                    entry.setTransactionLedgerEntryAmount(newTransactionAmount);
                }
            }
        } else {
            addEntries(temporaryCollectorEntries::add, paymentOffsetCorporateCardPersonalExpenseDebitEntries);
        }
        addEntries(temporaryCollectorEntries::add, nonPaymentOffsetCorporateCardPersonalExpenseDebitEntries);
    }

    protected void addEntries(Consumer<OriginEntryFull> entryConsumer, List<OriginEntryFull> entries) {
        for (OriginEntryFull entry : entries) {
            entryConsumer.accept(entry);
        }
    }

    protected KualiDecimal calculateTotalAmountForEntries(List<OriginEntryFull> originEntries) {
        return originEntries.stream().map(OriginEntryFull::getTransactionLedgerEntryAmount).reduce(KualiDecimal.ZERO, KualiDecimal::add);
    }

    protected void addOriginEntriesForCashLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> cashLines) {
        addOriginEntriesForLines(entryConsumer, cashLines, this::buildOriginEntryForPaymentOffset);
    }

    protected void addOriginEntriesForCorporateCardLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> corporateCardLines) {
        addOriginEntriesForLines(entryConsumer, corporateCardLines, this::buildOriginEntryForCorporateCardOffset);
    }

    protected void addOriginEntriesForAtmFeeDebitLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> atmFeeLines) {
        addOriginEntriesForLines(entryConsumer, atmFeeLines, (originEntry, lines) -> Optional.empty());
    }

    protected void addOriginEntriesForRequestedCashAdvanceLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> cashAdvanceLines) {
        addOriginEntriesForLines(entryConsumer, cashAdvanceLines, (originEntry, lines) -> Optional.empty());
    }

    protected void addOriginEntriesForAtmCashAdvanceLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> atmCashAdvanceLines) {
        addOriginEntriesForLines(entryConsumer, atmCashAdvanceLines, (originEntry, lines) -> Optional.empty());
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

    protected void addOffsetOriginEntriesForUnusedAtmCashAdvanceAmountLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> unusedAtmAmountLines) {
        if (CollectionUtils.isEmpty(unusedAtmAmountLines)) {
            return;
        }
        
        KualiDecimal totalAmount = calculateTotalAmountForLines(unusedAtmAmountLines);
        if (totalAmount.isNonZero()) {
            OriginEntryFull offsetEntry = this.buildOriginEntryForUnusedAtmAmountOffset(unusedAtmAmountLines, totalAmount);
            entryConsumer.accept(offsetEntry);
        }
    }

    protected void addOriginEntriesForCorpCardPersonalExpenseCreditLines(Consumer<OriginEntryFull> entryConsumer) {
        if (getLineMapOfType(ConcurSAECollectorLineType.CORP_CARD_PERSONAL_CREDIT).isEmpty()) {
            return;
        }
        
        for (List<ConcurStandardAccountingExtractDetailLine> corpCardPersonalCredits : getSubGroupsOfType(
                ConcurSAECollectorLineType.CORP_CARD_PERSONAL_CREDIT)) {
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

    protected void addOriginEntriesForCorpCardPersonalExpenseDebitLines(List<OriginEntryFull> temporaryCollectorEntries, List<OriginEntryFull> temporaryCorporateCardPersonalExpenseCollectorEntries) {
        if (getLineMapOfType(ConcurSAECollectorLineType.CORP_CARD_PERSONAL_DEBIT).isEmpty()) {
            return;
        }
        
        KualiDecimal netCashAmount = calculateTotalCashAmountNotOffsetByCashAdvances();
        KualiDecimal currentReimbursableCashAmount = netCashAmount;
        
        for (List<ConcurStandardAccountingExtractDetailLine> corpCardPersonalDebits : getSubGroupsOfType(
                ConcurSAECollectorLineType.CORP_CARD_PERSONAL_DEBIT)) {
            KualiDecimal totalSubGroupAmount = calculateTotalAmountForLines(corpCardPersonalDebits);
            if (totalSubGroupAmount.isZero()) {
                continue;
            }
            
            KualiDecimal newReimbursableCashAmount = currentReimbursableCashAmount.subtract(totalSubGroupAmount);
            KualiDecimal paymentOffsetAdjustment = calculatePaymentOffsetAdjustmentForCorpCardPersonalExpenseSubGroup(
                    totalSubGroupAmount, currentReimbursableCashAmount, newReimbursableCashAmount);
            KualiDecimal personalOffsetAdjustment = calculatePersonalOffsetAdjustmentForCorpCardPersonalExpenseSubGroup(
                    totalSubGroupAmount, currentReimbursableCashAmount, newReimbursableCashAmount);
            
            addOriginEntriesForCorpCardPersonalExpenseDebitLinesIfNecessary(temporaryCorporateCardPersonalExpenseCollectorEntries::add, corpCardPersonalDebits,
                    paymentOffsetAdjustment, personalOffsetAdjustment);
            
            currentReimbursableCashAmount = newReimbursableCashAmount;
        }

        cleanupCashAndAddCorporateCardPersonalExpenseEntries(temporaryCollectorEntries, temporaryCorporateCardPersonalExpenseCollectorEntries);
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
        List<ConcurStandardAccountingExtractDetailLine> allCashLines = getSubGroupsOfType(ConcurSAECollectorLineType.CASH)
                .stream()
                .flatMap(List::stream)
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

    protected OriginEntryFull buildOriginEntryForUnusedAtmAmountOffset(
            List<ConcurStandardAccountingExtractDetailLine> unusedAtmAmountLines, KualiDecimal amountToOffset) {
        if (unusedAtmAmountLines.isEmpty()) {
            throw new IllegalArgumentException("unusedAtmAmountLines list cannot be empty; this should NEVER happen!");
        }
        ConcurStandardAccountingExtractDetailLine firstUnusedAmountLine = unusedAtmAmountLines.get(0);
        
        OriginEntryFull regularEntry = buildOriginEntryWithoutAccountingIdentifiers(firstUnusedAmountLine, amountToOffset);
        configureOriginEntryFromDetailLineForAtmCashAdvance(regularEntry, firstUnusedAmountLine);
        resetTransactionSequenceNumberCounterToPreviousValue();
        
        OriginEntryFull offsetEntry = buildOffsetOriginEntry(regularEntry, amountToOffset);
        offsetEntry.setChartOfAccountsCode(firstUnusedAmountLine.getReportChartOfAccountsCode());
        offsetEntry.setAccountNumber(firstUnusedAmountLine.getReportAccountNumber());
        offsetEntry.setSubAccountNumber(
                defaultToDashesIfBlank(firstUnusedAmountLine.getReportSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        offsetEntry.setFinancialObjectCode(collectorHelper.getAtmUnusedAmountOffsetObjectCode());
        offsetEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        
        return offsetEntry;
    }

    protected OriginEntryFull buildOffsetOriginEntry(OriginEntryFull baseEntry, KualiDecimal amountToOffset) {
        OriginEntryFull offsetEntry = new OriginEntryFull(baseEntry);
        KualiDecimal offsetAmount = amountToOffset.negated();
        setTransactionSequenceNumberToNextAvailableValue(offsetEntry);
        configureAmountAndDebitCreditCodeOnOriginEntry(offsetEntry, offsetAmount);
        return offsetEntry;
    }

    protected OriginEntryFull buildOriginEntry(ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        BiConsumer<OriginEntryFull, ConcurStandardAccountingExtractDetailLine> originEntryConfigurer;
        
        if (collectorHelper.isCashAdvanceLine(detailLine)) {
            originEntryConfigurer = getConfigurerForCashAdvanceOriginEntry(detailLine);
        } else if (collectorHelper.isAtmFeeDebitLine(detailLine)) {
            originEntryConfigurer = this::configureAtmFeeDebitOriginEntry;
        } else {
            originEntryConfigurer = this::configureRegularOriginEntry;
        }
        
        OriginEntryFull originEntry = buildOriginEntryWithoutAccountingIdentifiers(detailLine, amount);
        originEntryConfigurer.accept(originEntry, detailLine);
        return originEntry;
    }

    protected BiConsumer<OriginEntryFull, ConcurStandardAccountingExtractDetailLine> getConfigurerForCashAdvanceOriginEntry(
            ConcurStandardAccountingExtractDetailLine detailLine) {
        if (collectorHelper.isAtmCashAdvanceLine(detailLine)) {
            return this::configureOriginEntryFromDetailLineForAtmCashAdvance;
        } else {
            return this::configureOriginEntryFromRequestedCashAdvance;
        }
    }

    protected void configureOriginEntryFromDetailLineForAtmCashAdvance(
            OriginEntryFull originEntry, ConcurStandardAccountingExtractDetailLine detailLine) {
        originEntry.setChartOfAccountsCode(collectorHelper.getPrepaidOffsetChartCode());
        originEntry.setAccountNumber(collectorHelper.getPrepaidOffsetAccountNumber());
        originEntry.setSubAccountNumber(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(collectorHelper.getPrepaidOffsetObjectCode());
        originEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        originEntry.setProjectCode(
                defaultToDashesIfBlank(detailLine.getReportProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(StringUtils.defaultIfBlank(detailLine.getReportOrgRefId(), StringUtils.EMPTY));
    }

    protected void configureOriginEntryFromRequestedCashAdvance(
            OriginEntryFull originEntry, ConcurStandardAccountingExtractDetailLine detailLine) {
        ConcurRequestedCashAdvance requestedCashAdvance = requestedCashAdvancesByCashAdvanceKey.get(detailLine.getCashAdvanceKey());
        if (requestedCashAdvance == null) {
            throw new IllegalStateException(
                    "Did not find cash advance when building Collector line; this should NEVER happen. Key: "
                            + detailLine.getCashAdvanceKey());
        }
        
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
    }

    protected OriginEntryFull buildCorpCardPersonalExpenseOriginEntry(
            ConcurStandardAccountingExtractDetailLine detailLine, String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, KualiDecimal amount) {
        OriginEntryFull originEntry = buildOriginEntryWithoutAccountingIdentifiers(detailLine, amount);
        
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
        
        return originEntry;
    }

    protected void configureAtmFeeDebitOriginEntry(OriginEntryFull originEntry, ConcurStandardAccountingExtractDetailLine detailLine) {
        originEntry.setChartOfAccountsCode(collectorHelper.getAtmFeeDebitChartCode());
        originEntry.setAccountNumber(collectorHelper.getAtmFeeDebitAccountNumber());
        originEntry.setSubAccountNumber(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(collectorHelper.getAtmFeeDebitObjectCode());
        originEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        originEntry.setProjectCode(defaultToDashesIfBlank(detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
    }

    protected void configureRegularOriginEntry(OriginEntryFull originEntry, ConcurStandardAccountingExtractDetailLine detailLine) {
        originEntry.setChartOfAccountsCode(detailLine.getChartOfAccountsCode());
        originEntry.setAccountNumber(detailLine.getAccountNumber());
        originEntry.setSubAccountNumber(defaultToDashesIfBlank(detailLine.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(detailLine.getJournalAccountCode());
        originEntry.setFinancialSubObjectCode(defaultToDashesIfBlank(detailLine.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE));
        originEntry.setProjectCode(defaultToDashesIfBlank(detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
    }

    protected OriginEntryFull buildOriginEntryWithoutAccountingIdentifiers(
            ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        OriginEntryFull originEntry = new OriginEntryFull();
        
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
        
        return originEntry;
    }

    protected void setTransactionSequenceNumberToNextAvailableValue(OriginEntryFull originEntry) {
        originEntry.setTransactionLedgerEntrySequenceNumber(nextTransactionSequenceNumber++);
    }

    protected void resetTransactionSequenceNumberCounterToPreviousValue() {
        nextTransactionSequenceNumber--;
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
            TriConsumer<ConcurStandardAccountingExtractDetailLine, String, Boolean> lineErrorReporter) {
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
            lineErrorReporter.accept(detailLine, errorMessage, false);
        }
    }

    protected Set<String> getSequenceNumbersForOrphanedCashAdvanceLines() {
        List<ConcurStandardAccountingExtractDetailLine> orphanedKeyList = getSubGroup(
                ConcurSAECollectorLineType.REQUESTED_CASH_ADVANCE, ORPHANED_CASH_ADVANCES_KEY);
        return orphanedKeyList.stream()
                .map(ConcurStandardAccountingExtractDetailLine::getSequenceNumber)
                .collect(Collectors.toCollection(HashSet::new));
    }

    protected List<ConcurStandardAccountingExtractDetailLine> getDetailLinesSortedBySequenceNumberNumerically() {
        return consolidatedLines.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(List::stream)
                .sorted(this::compareBySequenceNumberAsPositiveNumericString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected int compareBySequenceNumberAsPositiveNumericString(
            ConcurStandardAccountingExtractDetailLine line1, ConcurStandardAccountingExtractDetailLine line2) {
        int lengthComparison = line1.getSequenceNumber().length() - line2.getSequenceNumber().length();
        if (lengthComparison != 0) {
            return lengthComparison;
        }
        return line1.getSequenceNumber().compareTo(line2.getSequenceNumber());
    }
    
}
