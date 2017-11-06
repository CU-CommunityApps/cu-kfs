package edu.cornell.kfs.fp.batch.service;

import java.text.DateFormat;
import java.util.List;

import org.kuali.kfs.fp.batch.ProcurementCardReportType;
import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.fp.document.ProcurementCardDocument;

public interface CuProcurementCardCreateDocumentService extends ProcurementCardCreateDocumentService {
    
    List retrieveTransactions();
    
    ProcurementCardDocument createProcurementCardDocument(List transactions);
    
    List<ProcurementCardReportType> getSortedReportSummaryList(List<ProcurementCardDocument> documents);
    
    int getBatchTotalTransactionCnt(List<ProcurementCardReportType> summaryList);
    
    DateFormat getDateFormat(String namespaceCode, String componentCode, String parameterName, String defaultValue);

}
