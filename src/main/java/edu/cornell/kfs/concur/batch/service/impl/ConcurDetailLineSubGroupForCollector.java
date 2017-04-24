package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

/**
 * Utility class for grouping together related SAE transactions with the same accounting data,
 * and then partitioning them by payment code.
 * 
 * Only CASH (out of pocket) and CBCP (corporate card) SAE lines will be recorded by this class.
 * The caller is expected to avoid sending in lines containing other payment codes.
 */
public class ConcurDetailLineSubGroupForCollector {

    protected List<ConcurStandardAccountingExtractDetailLine> cashLines;
    protected List<ConcurStandardAccountingExtractDetailLine> corporateCardLines;

    public ConcurDetailLineSubGroupForCollector() {
        this.cashLines = new ArrayList<>();
        this.corporateCardLines = new ArrayList<>();
    }

    /**
     * Convenience constructor that takes a Map-related key as input and then discards it;
     * is only meant for use as a method reference to simplify certain Map operations.
     */
    public <T> ConcurDetailLineSubGroupForCollector(T mapKey) {
        this();
    }

    public void addDetailLine(ConcurStandardAccountingExtractDetailLine detailLine) {
        switch (detailLine.getPaymentCode()) {
            case ConcurConstants.PAYMENT_CODE_CASH :
                cashLines.add(detailLine);
                break;
            case ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID :
                corporateCardLines.add(detailLine);
                break;
            default :
                throw new IllegalArgumentException("Found a row with an unprocessable payment code; this should NEVER happen! Code: "
                        + detailLine.getPaymentCode());
        }
    }

    public List<ConcurStandardAccountingExtractDetailLine> getCashLines() {
        return cashLines;
    }

    public void setCashLines(List<ConcurStandardAccountingExtractDetailLine> cashLines) {
        this.cashLines = cashLines;
    }

    public List<ConcurStandardAccountingExtractDetailLine> getCorporateCardLines() {
        return corporateCardLines;
    }

    public void setCorporateCardLines(List<ConcurStandardAccountingExtractDetailLine> corporateCardLines) {
        this.corporateCardLines = corporateCardLines;
    } 

}
