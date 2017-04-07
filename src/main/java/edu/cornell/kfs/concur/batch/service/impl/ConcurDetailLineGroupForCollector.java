package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

/**
 * Utility class for grouping together related SAE transactions, and then generating GL entries
 * and GL offset entries from them.
 */
public class ConcurDetailLineGroupForCollector {
    protected String reportId;
    protected List<ConcurStandardAccountingExtractDetailLine> detailLines;
    protected OriginEntryFull baseEntry;
    protected Map<String,KualiDecimal> paymentCodeAmounts;
    protected String chartCode;
    protected String prepaidOffsetAccountNumber;
    protected String prepaidOffsetObjectCode;
    protected String cashOffsetObjectCode;
    protected Function<String,String> dashValueGetter;

    public ConcurDetailLineGroupForCollector(String reportId, OriginEntryFull baseEntry,
            Function<String,String> parameterService, Function<String,String> dashValueGetter) {
        this.reportId = reportId;
        this.detailLines = new ArrayList<>();
        this.baseEntry = baseEntry;
        this.paymentCodeAmounts = new HashMap<>();
        this.chartCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CHART_CODE);
        this.prepaidOffsetAccountNumber = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER);
        this.prepaidOffsetObjectCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_OBJECT_CODE);
        this.cashOffsetObjectCode = parameterService.apply(ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CASH_OFFSET_OBJECT_CODE);
        this.dashValueGetter = dashValueGetter;
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
        
        List<OriginEntryFull> originEntries = new ArrayList<>();
        
        if (corporateCardAmount.isNonZero()) {
            originEntries.add(buildOriginEntry(corporateCardAmount));
            originEntries.add(
                    buildOffsetOriginEntry(corporateCardAmount, this::configureOriginEntryForCorporateCardOffset));
        }
        if (cashAmount.isNonZero()) {
            originEntries.add(buildOriginEntry(cashAmount));
            originEntries.add(
                    buildOffsetOriginEntry(cashAmount, this::configureOriginEntryForCashOffset));
        }
        
        return originEntries;
    }

    protected void configureOriginEntryForCorporateCardOffset(OriginEntryFull originEntry) {
        originEntry.setChartOfAccountsCode(chartCode);
        originEntry.setAccountNumber(prepaidOffsetAccountNumber);
        originEntry.setSubAccountNumber(dashValueGetter.apply(KFSPropertyConstants.SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(prepaidOffsetObjectCode);
        originEntry.setFinancialSubObjectCode(dashValueGetter.apply(KFSPropertyConstants.SUB_OBJECT_CODE));
    }

    protected void configureOriginEntryForCashOffset(OriginEntryFull originEntry) {
        originEntry.setFinancialObjectCode(cashOffsetObjectCode);
        originEntry.setFinancialSubObjectCode(dashValueGetter.apply(KFSPropertyConstants.SUB_OBJECT_CODE));
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
            throw new IllegalArgumentException("Cannot have a zero amount for an Origin Entry");
        }
    }

}
