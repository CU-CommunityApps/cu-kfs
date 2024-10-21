package edu.cornell.kfs.concur.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

public abstract class ConcurStandardAccountingExtractLineBase {

    private Integer lineNumber;
    private List<Integer> columnNumbersContainingSpecialCharacters;

    public ConcurStandardAccountingExtractLineBase() {
        this.columnNumbersContainingSpecialCharacters = new ArrayList<>();
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<Integer> getColumnNumbersContainingSpecialCharacters() {
        return columnNumbersContainingSpecialCharacters;
    }

    public void setColumnNumbersContainingSpecialCharacters(
            final List<Integer> columnNumbersContainingSpecialCharacters) {
        this.columnNumbersContainingSpecialCharacters = columnNumbersContainingSpecialCharacters;
    }

    public void addColumnNumberContainingSpecialCharacters(final Integer columnNumber) {
        if (columnNumbersContainingSpecialCharacters == null) {
            columnNumbersContainingSpecialCharacters = new ArrayList<>();
        }
        getColumnNumbersContainingSpecialCharacters().add(columnNumber);
    }

}
