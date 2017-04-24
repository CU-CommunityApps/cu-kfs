package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

/**
 * Utility class for grouping together related SAE transactions, and then generating GL entries
 * and GL offset entries from them. The calling code should create one instance of this class
 * for each Report ID in the SAE file.
 */
public class ConcurDetailLineGroupForCollector {

    protected static final String FAKE_OBJECT_CODE_PREFIX = "?NONE?";
    protected static final String ACCOUNTING_FIELDS_KEY_FORMAT = "%s|%s|%s|%s|%s|%s|%s";
    protected static final String TRANSACTION_DESCRIPTION_FORMAT = "%s,%s,%s";
    protected static final int REPORT_ID_LENGTH_FOR_DOC_NUMBER = 10;
    protected static final int NAME_LENGTH_FOR_DESCRIPTION = 14;

    protected String reportId;
    protected ConcurDetailLineGroupForCollectorHelper collectorHelper;
    protected Map<String, ConcurDetailLineSubGroupForCollector> consolidatedRegularLines;
    protected Map<String, List<ConcurStandardAccountingExtractDetailLine>> consolidatedCashAdvanceLines;
    protected Map<String, ConcurStandardAccountingExtractDetailLine> cashAdvanceLinesByReportEntryId;
    protected Map<String, ConcurRequestedCashAdvance> requestedCashAdvancesByCashAdvanceKey;
    protected int nextFakeObjectCode;
    protected int nextTransactionSequenceNumber;

    public ConcurDetailLineGroupForCollector(String reportId, ConcurDetailLineGroupForCollectorHelper collectorHelper) {
        this.reportId = reportId;
        this.collectorHelper = collectorHelper;
        this.consolidatedRegularLines = new LinkedHashMap<>();
        this.consolidatedCashAdvanceLines = new LinkedHashMap<>();
        this.cashAdvanceLinesByReportEntryId = new LinkedHashMap<>();
        this.requestedCashAdvancesByCashAdvanceKey = new LinkedHashMap<>();
        this.nextFakeObjectCode = 1;
        this.nextTransactionSequenceNumber = 1;
    }

    public String getReportId() {
        return reportId;
    }

    public void addDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        if (isCashAdvanceLine(detailLine)) {
            ConcurRequestedCashAdvance requestedCashAdvance = requestedCashAdvancesByCashAdvanceKey.computeIfAbsent(
                    "myCashAdvanceKey", this::getExistingRequestedCashAdvanceByCashAdvanceKey);
            String accountingFieldsKey = buildAccountingFieldsKeyForCashAdvance(detailLine, requestedCashAdvance);
            consolidatedCashAdvanceLines.computeIfAbsent(accountingFieldsKey, (key) -> new ArrayList<>())
                    .add(detailLine);
            cashAdvanceLinesByReportEntryId.put("reportEntryId", detailLine);
        } else {
            String accountingFieldsKey = buildAccountingFieldsKey(detailLine);
            consolidatedRegularLines.computeIfAbsent(accountingFieldsKey, ConcurDetailLineSubGroupForCollector::new)
                    .addDetailLine(detailLine);
        }
    }

    protected boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        return StringUtils.isNotBlank("\n\n\n\n\n\n");
    }

    protected ConcurRequestedCashAdvance getExistingRequestedCashAdvanceByCashAdvanceKey(String cashAdvanceKey) {
        ConcurRequestedCashAdvance requestedCashAdvance = collectorHelper.getRequestedCashAdvanceByCashAdvanceKey(cashAdvanceKey);
        if (ObjectUtils.isNull(requestedCashAdvance)) {
            throw new IllegalStateException("A requested cash advance does not exist for key: " + cashAdvanceKey);
        }
        return requestedCashAdvance;
    }

    protected String buildAccountingFieldsKey(ConcurStandardAccountingExtractDetailLine detailLine) {
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

    protected String buildAccountingFieldsKeyForCashAdvance(
            ConcurStandardAccountingExtractDetailLine detailLine, ConcurRequestedCashAdvance requestedCashAdvance) {
        String objectCodeForKey = StringUtils.defaultIfBlank(requestedCashAdvance.getObjectCode(), detailLine.getJournalAccountCode());
        objectCodeForKey = convertToFakeObjectCodeIfNecessary(objectCodeForKey, detailLine);
        
        return String.format(ACCOUNTING_FIELDS_KEY_FORMAT,
                requestedCashAdvance.getChart(),
                requestedCashAdvance.getAccountNumber(),
                defaultToDashesIfBothAreBlank(
                        requestedCashAdvance.getSubAccountNumber(), detailLine.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                objectCodeForKey,
                defaultToDashesIfBothAreBlank(
                        requestedCashAdvance.getSubObjectCode(), detailLine.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBothAreBlank(
                        requestedCashAdvance.getProjectCode(), detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                defaultToEmptyIfBothAreBlank(requestedCashAdvance.getOrgRefId(), detailLine.getOrgRefId()));
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

    /**
     * @return preferredValue if non-blank, otherwise backupValue if non-blank, otherwise an empty String.
     */
    protected String defaultToEmptyIfBothAreBlank(String preferredValue, String backupValue) {
        return StringUtils.defaultIfBlank(preferredValue, StringUtils.defaultIfBlank(backupValue, StringUtils.EMPTY));
    }

    /**
     * @return preferredValue if non-blank, otherwise backupValue if non-blank, otherwise the dash-only value for the propertyName.
     */
    protected String defaultToDashesIfBothAreBlank(String preferredValue, String backupValue, String propertyName) {
        return StringUtils.defaultIfBlank(preferredValue, defaultToDashesIfBlank(backupValue, propertyName));
    }

    protected String defaultToDashesIfBlank(String value, String propertyName) {
        return StringUtils.defaultIfBlank(value, collectorHelper.getDashOnlyPropertyValue(propertyName));
    }

    public void buildAndAddOriginEntries(Consumer<OriginEntryFull> entryConsumer) {
        nextTransactionSequenceNumber = 1;
        
        for (ConcurDetailLineSubGroupForCollector subGroup : consolidatedRegularLines.values()) {
            addOriginEntriesForCorporateCardLines(entryConsumer, subGroup.getCorporateCardLines());
            addOriginEntriesForCashLines(entryConsumer, subGroup.getCashLines());
        }
        
        for (List<ConcurStandardAccountingExtractDetailLine> cashAdvanceSubGroup : consolidatedCashAdvanceLines.values()) {
            addOriginEntriesForCashAdvanceLines(entryConsumer, cashAdvanceSubGroup);
        }
    }

    protected void addOriginEntriesForCashLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> cashLines) {
        addOriginEntriesForLines(entryConsumer, cashLines, this::buildOriginEntryForCashOffset);
    }

    protected void addOriginEntriesForCorporateCardLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> corporateCardLines) {
        addOriginEntriesForLines(entryConsumer, corporateCardLines, this::buildOriginEntryForCorporateCardOffset);
    }

    protected void addOriginEntriesForCashAdvanceLines(
            Consumer<OriginEntryFull> entryConsumer, List<ConcurStandardAccountingExtractDetailLine> cashAdvanceLines) {
        addOriginEntriesForLines(entryConsumer, cashAdvanceLines, this::buildOriginEntryForCashAdvanceOffset);
    }

    /**
     * @param originEntries The List to add the generated origin entries to; won't be modified if detailLines is empty or has zero-sum lines.
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

    protected Optional<OriginEntryFull> buildOriginEntryForCashOffset(
            OriginEntryFull cashEntry, List<ConcurStandardAccountingExtractDetailLine> cashLines) {
        KualiDecimal cashAmount = getSignedAmountFromOriginEntry(cashEntry);
        KualiDecimal cashAdvanceAmount = calculateTotalAmountForCashAdvanceLinesReferencedByRegularLines(cashLines);
        KualiDecimal cashAmountToOffset = cashAmount.add(cashAdvanceAmount);
        if (cashAmountToOffset.isZero()) {
            return Optional.empty();
        }
        
        OriginEntryFull offsetEntry = buildOffsetOriginEntry(cashEntry, cashAmountToOffset);
        offsetEntry.setFinancialObjectCode(collectorHelper.getCashOffsetObjectCode());
        offsetEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        
        return Optional.of(offsetEntry);
    }

    protected KualiDecimal calculateTotalAmountForCashAdvanceLinesReferencedByRegularLines(
            List<ConcurStandardAccountingExtractDetailLine> regularDetailLines) {
        return regularDetailLines.stream()
                .map(ConcurStandardAccountingExtractDetailLine::getBatchID)
                .distinct()
                .map(cashAdvanceLinesByReportEntryId::get)
                .filter((cashAdvanceLine) -> cashAdvanceLine != null)
                .map(ConcurStandardAccountingExtractDetailLine::getJournalAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
    }

    protected Optional<OriginEntryFull> buildOriginEntryForCorporateCardOffset(
            OriginEntryFull corporateCardEntry, List<ConcurStandardAccountingExtractDetailLine> corporateCardLines) {
        KualiDecimal corporateCardAmount = getSignedAmountFromOriginEntry(corporateCardEntry);
        
        OriginEntryFull offsetEntry = buildOffsetOriginEntry(corporateCardEntry, corporateCardAmount);
        offsetEntry.setChartOfAccountsCode(collectorHelper.getChartCode());
        offsetEntry.setAccountNumber(collectorHelper.getPrepaidOffsetAccountNumber());
        offsetEntry.setSubAccountNumber(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        offsetEntry.setFinancialObjectCode(collectorHelper.getPrepaidOffsetObjectCode());
        offsetEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        
        return Optional.of(offsetEntry);
    }

    protected Optional<OriginEntryFull> buildOriginEntryForCashAdvanceOffset(
            OriginEntryFull cashAdvanceEntry, List<ConcurStandardAccountingExtractDetailLine> cashAdvanceLines) {
        KualiDecimal cashAdvanceAmountToOffset = calculateTotalAmountForCashAdvanceOffset(cashAdvanceLines);
        if (cashAdvanceAmountToOffset.isZero()) {
            return Optional.empty();
        }
        
        OriginEntryFull offsetEntry = buildOffsetOriginEntry(cashAdvanceEntry, cashAdvanceAmountToOffset);
        offsetEntry.setFinancialObjectCode(collectorHelper.getPersonalOffsetObjectCode());
        offsetEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        
        return Optional.of(offsetEntry);
    }

    protected KualiDecimal calculateTotalAmountForCashAdvanceOffset(
            List<ConcurStandardAccountingExtractDetailLine> cashAdvanceLines) {
        return cashAdvanceLines.stream()
                .filter(this::lineRepresentsUnspentCashAdvanceAmount)
                .map(ConcurStandardAccountingExtractDetailLine::getJournalAmount)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
    }

    protected boolean lineRepresentsUnspentCashAdvanceAmount(ConcurStandardAccountingExtractDetailLine cashAdvanceLine) {
        return StringUtils.equals(ConcurConstants.PAYMENT_CODE_PSEUDO, cashAdvanceLine.getPaymentCode());
    }

    protected OriginEntryFull buildOffsetOriginEntry(OriginEntryFull baseEntry, KualiDecimal amountToOffset) {
        OriginEntryFull offsetEntry = new OriginEntryFull(baseEntry);
        KualiDecimal offsetAmount = amountToOffset.negated();
        setTransactionSequenceNumberToNextAvailableValue(offsetEntry);
        configureAmountAndDebitCreditCodeOnOriginEntry(offsetEntry, offsetAmount);
        return offsetEntry;
    }

    protected OriginEntryFull buildOriginEntry(ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        if (isCashAdvanceLine(detailLine)) {
            return buildCashAdvanceOriginEntry(detailLine, amount);
        } else {
            return buildRegularOriginEntry(detailLine, amount);
        }
    }

    protected OriginEntryFull buildCashAdvanceOriginEntry(ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        OriginEntryFull originEntry = new OriginEntryFull();
        ConcurRequestedCashAdvance requestedCashAdvance = requestedCashAdvancesByCashAdvanceKey.get("someCaKey");
        if (requestedCashAdvance == null) {
            throw new IllegalStateException("Did not find cash advance when building Collector line; this should NEVER happen. Key: ");
        }
        
        originEntry.setChartOfAccountsCode(requestedCashAdvance.getChart());
        originEntry.setAccountNumber(requestedCashAdvance.getAccountNumber());
        originEntry.setSubAccountNumber(defaultToDashesIfBothAreBlank(
                requestedCashAdvance.getSubAccountNumber(), detailLine.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(StringUtils.defaultIfBlank(
                requestedCashAdvance.getObjectCode(), detailLine.getJournalAccountCode()));
        originEntry.setFinancialSubObjectCode(defaultToDashesIfBothAreBlank(
                requestedCashAdvance.getSubObjectCode(), detailLine.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE));
        originEntry.setProjectCode(defaultToDashesIfBothAreBlank(
                requestedCashAdvance.getProjectCode(), detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(defaultToEmptyIfBothAreBlank(
                requestedCashAdvance.getOrgRefId(), detailLine.getOrgRefId()));
        
        configureOriginEntryGeneratedFromLine(originEntry, detailLine, amount);
        
        return originEntry;
    }

    protected OriginEntryFull buildRegularOriginEntry(ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        OriginEntryFull originEntry = new OriginEntryFull();
        
        originEntry.setChartOfAccountsCode(detailLine.getChartOfAccountsCode());
        originEntry.setAccountNumber(detailLine.getAccountNumber());
        originEntry.setSubAccountNumber(
                defaultToDashesIfBlank(detailLine.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(detailLine.getJournalAccountCode());
        originEntry.setFinancialSubObjectCode(
                defaultToDashesIfBlank(detailLine.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE));
        originEntry.setProjectCode(
                defaultToDashesIfBlank(detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE));
        originEntry.setOrganizationReferenceId(
                StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
        
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

}
