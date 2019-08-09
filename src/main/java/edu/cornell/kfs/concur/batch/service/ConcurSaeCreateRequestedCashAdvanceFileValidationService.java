package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurSaeRequestedCashAdvanceBatchReportData;

public interface ConcurSaeCreateRequestedCashAdvanceFileValidationService {

    boolean saeHeaderRowValidatesToFileContentsForRequestedCashAdvances(ConcurStandardAccountingExtractFile standardAccountingExtractFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData);

    void performSaeDetailLineValidationForRequestedCashAdvances(ConcurStandardAccountingExtractDetailLine detailFileLine, List<String> uniqueRequestedCashAdvanceKeysInFile);

}
