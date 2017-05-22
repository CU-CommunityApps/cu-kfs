package edu.cornell.kfs.concur.batch.report;

import java.util.List;

public interface ConcurEmailableReportData {
    String getConcurFileName();
    List<String> getHeaderValidationErrors();
    List<ConcurBatchReportLineValidationErrorItem> getValidationErrorFileLines();
    String getReportTypeName();
}
