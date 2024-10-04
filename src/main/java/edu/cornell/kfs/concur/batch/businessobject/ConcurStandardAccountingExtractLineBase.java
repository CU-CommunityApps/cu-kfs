package edu.cornell.kfs.concur.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

public abstract class ConcurStandardAccountingExtractLineBase {

    private int lineNumber;
    private List<Integer> columnNumbersContainingSpecialCharacters;

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<Integer> getColumnNumbersContainingSpecialCharacters() {
        if (columnNumbersContainingSpecialCharacters == null) {
            columnNumbersContainingSpecialCharacters = new ArrayList<>();
        }
        return columnNumbersContainingSpecialCharacters;
    }

    public void setColumnNumbersContainingSpecialCharacters(
            final List<Integer> columnNumbersContainingSpecialCharacters) {
        this.columnNumbersContainingSpecialCharacters = columnNumbersContainingSpecialCharacters;
    }

    public void addColumnNumberContainingSpecialCharacters(final Integer columnNumber) {
        getColumnNumbersContainingSpecialCharacters().add(columnNumber);
    }

}
