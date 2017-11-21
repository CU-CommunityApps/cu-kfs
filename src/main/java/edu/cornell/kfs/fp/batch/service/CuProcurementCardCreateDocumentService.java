package edu.cornell.kfs.fp.batch.service;

import java.util.List;

import org.kuali.kfs.fp.batch.ProcurementCardReportType;
import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.fp.document.ProcurementCardDocument;

public interface CuProcurementCardCreateDocumentService extends ProcurementCardCreateDocumentService {
    
    public List<ProcurementCardReportType> getSortedReportSummaryList(List<ProcurementCardDocument> documents);
    
}
