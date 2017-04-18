package edu.cornell.kfs.concur.batch.service;

import java.sql.Date;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

public interface ConcurStandardAccountExtractPdpEntryService {
    
    PdpFeedHeaderEntry buildPdpFeedHeaderEntry(Date batchDate);
    
    PdpFeedGroupEntry buildPdpFeedGroupEntry(ConcurStandardAccountingExtractDetailLine line);
    
    String buildSourceDocumentNumber(String reportId);
    
    PdpFeedPayeeIdEntry buildPayeeIdEntry(ConcurStandardAccountingExtractDetailLine line);
    
    PdpFeedDetailEntry buildPdpFeedDetailEntry(ConcurStandardAccountingExtractDetailLine line);

    PdpFeedAccountingEntry buildPdpFeedAccountingEntry(ConcurAccountInfo concurAccountInfo);
    
    PdpFeedTrailerEntry buildPdpFeedTrailerEntry(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, ConcurStandardAccountingExtractBatchReportData reportData);

}
