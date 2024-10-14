package edu.cornell.kfs.concur.batch.report;

import java.util.ArrayList;
import java.util.List;

public class ConcurBatchReportRemovedCharactersWarningItem {

    private Integer lineNumber;
    private List<Integer> columnNumbers;

    public ConcurBatchReportRemovedCharactersWarningItem() {
        this.columnNumbers = new ArrayList<>();
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<Integer> getColumnNumbers() {
        return columnNumbers;
    }

    public void setColumnNumbers(final List<Integer> columnNumbers) {
        this.columnNumbers = columnNumbers;
    }

    public void addColumnNumber(final Integer columnNumber) {
        if (columnNumbers == null) {
            columnNumbers = new ArrayList<>();
        }
        columnNumbers.add(columnNumber);
    }

}
