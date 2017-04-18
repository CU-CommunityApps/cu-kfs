package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
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
    protected Map<String,List<ConcurStandardAccountingExtractDetailLine>> consolidatedRegularLines;
    protected int nextFakeObjectCode;

    public ConcurDetailLineGroupForCollector(String reportId, ConcurDetailLineGroupForCollectorHelper collectorHelper) {
        this.reportId = reportId;
        this.collectorHelper = collectorHelper;
        this.consolidatedRegularLines = new LinkedHashMap<>();
        this.nextFakeObjectCode = 1;
    }

    public String getReportId() {
        return reportId;
    }

    public void addDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        String accountingFieldsKey = buildAccountingFieldsKey(detailLine);
        consolidatedRegularLines.computeIfAbsent(accountingFieldsKey, (key) -> new ArrayList<>())
                .add(detailLine);
    }

    protected String buildAccountingFieldsKey(ConcurStandardAccountingExtractDetailLine detailLine) {
        String objectCodeForKey = detailLine.getJournalAccountCode();
        if (Boolean.TRUE.equals(detailLine.getJournalAccountCodeOverridden())) {
            objectCodeForKey = buildFakeObjectCodeToLeaveDetailLineUnmerged();
        }
        
        return String.format(ACCOUNTING_FIELDS_KEY_FORMAT,
                detailLine.getChartOfAccountsCode(),
                detailLine.getAccountNumber(),
                defaultToDashesIfBlank(detailLine.getSubAccountNumber(), KFSPropertyConstants.SUB_ACCOUNT_NUMBER),
                objectCodeForKey,
                defaultToDashesIfBlank(detailLine.getSubObjectCode(), KFSPropertyConstants.SUB_OBJECT_CODE),
                defaultToDashesIfBlank(detailLine.getProjectCode(), KFSPropertyConstants.PROJECT_CODE),
                StringUtils.defaultIfBlank(detailLine.getOrgRefId(), StringUtils.EMPTY));
    }

    protected String buildFakeObjectCodeToLeaveDetailLineUnmerged() {
        return FAKE_OBJECT_CODE_PREFIX + nextFakeObjectCode++;
    }

    protected String defaultToDashesIfBlank(String value, String propertyName) {
        return StringUtils.defaultIfBlank(value, collectorHelper.getDashOnlyPropertyValue(propertyName));
    }

    public List<OriginEntryFull> buildOriginEntries() {
        List<OriginEntryFull> originEntries = new ArrayList<>();
        
        for (List<ConcurStandardAccountingExtractDetailLine> subGroup : consolidatedRegularLines.values()) {
            Map<String,List<ConcurStandardAccountingExtractDetailLine>> linesByPaymentCode = groupDetailLinesByPaymentCode(subGroup);
            List<ConcurStandardAccountingExtractDetailLine> cashLines = linesByPaymentCode.getOrDefault(
                    ConcurConstants.PAYMENT_CODE_CASH, Collections.emptyList());
            List<ConcurStandardAccountingExtractDetailLine> corporateCardLines = linesByPaymentCode.getOrDefault(
                    ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID, Collections.emptyList());
            
            addOriginEntriesForCorporateCardLines(originEntries, corporateCardLines);
            addOriginEntriesForCashLines(originEntries, cashLines);
        }
        
        setTransactionSequenceNumbersOnOriginEntries(originEntries);
        
        return originEntries;
    }

    protected void setTransactionSequenceNumbersOnOriginEntries(List<OriginEntryFull> originEntries) {
        int nextTransactionSequenceNumber = 1;
        for (OriginEntryFull originEntry : originEntries) {
            originEntry.setTransactionLedgerEntrySequenceNumber(nextTransactionSequenceNumber++);
        }
    }

    protected Map<String,List<ConcurStandardAccountingExtractDetailLine>> groupDetailLinesByPaymentCode(
            List<ConcurStandardAccountingExtractDetailLine> detailLines) {
        return detailLines.stream()
                .collect(Collectors.groupingBy(
                        ConcurStandardAccountingExtractDetailLine::getPaymentCode, HashMap::new, Collectors.toCollection(ArrayList::new)));
    }

    protected void addOriginEntriesForCashLines(
            List<OriginEntryFull> originEntries, List<ConcurStandardAccountingExtractDetailLine> cashLines) {
        addOriginEntriesForLines(originEntries, cashLines, this::buildOriginEntryForCashOffset);
    }

    protected void addOriginEntriesForCorporateCardLines(
            List<OriginEntryFull> originEntries, List<ConcurStandardAccountingExtractDetailLine> corporateCardLines) {
        addOriginEntriesForLines(originEntries, corporateCardLines, this::buildOriginEntryForCorporateCardOffset);
    }

    /**
     * @param originEntries The List to add the generated origin entries to; won't be modified if detailLines is empty or has zero-sum lines.
     * @param detailLines The SAE lines to generate a single origin entry from; may be empty.
     * @param offsetGenerator A BiFunction that may create an offset entry using the generated regular entry and the detailLines List.
     */
    protected void addOriginEntriesForLines(
            List<OriginEntryFull> originEntries, List<ConcurStandardAccountingExtractDetailLine> detailLines,
            BiFunction<OriginEntryFull,List<ConcurStandardAccountingExtractDetailLine>,Optional<OriginEntryFull>> offsetGenerator) {
        if (CollectionUtils.isEmpty(detailLines)) {
            return;
        }
        
        KualiDecimal totalAmount = calculateTotalAmountForLines(detailLines);
        if (totalAmount.isNonZero()) {
            ConcurStandardAccountingExtractDetailLine firstDetailLine = detailLines.get(0);
            OriginEntryFull originEntry = buildOriginEntry(firstDetailLine, totalAmount);
            originEntries.add(originEntry);
            
            Optional<OriginEntryFull> optionalOffsetEntry = offsetGenerator.apply(originEntry, detailLines);
            if (optionalOffsetEntry.isPresent()) {
                originEntries.add(optionalOffsetEntry.get());
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
        
        OriginEntryFull offsetEntry = buildOffsetOriginEntry(cashEntry, cashAmount);
        offsetEntry.setFinancialObjectCode(collectorHelper.getCashOffsetObjectCode());
        offsetEntry.setFinancialSubObjectCode(collectorHelper.getDashOnlyPropertyValue(KFSPropertyConstants.SUB_OBJECT_CODE));
        
        return Optional.of(offsetEntry);
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

    protected OriginEntryFull buildOffsetOriginEntry(OriginEntryFull baseEntry, KualiDecimal amountToOffset) {
        OriginEntryFull offsetEntry = new OriginEntryFull(baseEntry);
        KualiDecimal offsetAmount = amountToOffset.negated();
        configureAmountAndDebitCreditCodeOnOriginEntry(offsetEntry, offsetAmount);
        return offsetEntry;
    }

    protected OriginEntryFull buildOriginEntry(ConcurStandardAccountingExtractDetailLine detailLine, KualiDecimal amount) {
        OriginEntryFull originEntry = new OriginEntryFull();
        
        // Default constructor sets fiscal year to zero; need to forcibly clear it to allow auto-setup by the Poster, as per the spec.
        originEntry.setUniversityFiscalYear(null);
        
        originEntry.setFinancialBalanceTypeCode(collectorHelper.getActualFinancialBalanceTypeCode());
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
        
        originEntry.setFinancialDocumentTypeCode(collectorHelper.getDocumentTypeCode());
        originEntry.setFinancialSystemOriginationCode(collectorHelper.getSystemOriginationCode());
        originEntry.setDocumentNumber(buildDocumentNumber(detailLine));
        originEntry.setTransactionLedgerEntryDescription(buildTransactionDescription(detailLine));
        originEntry.setTransactionDate(collectorHelper.getTransmissionDate());
        
        configureAmountAndDebitCreditCodeOnOriginEntry(originEntry, amount);
        
        return originEntry;
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
