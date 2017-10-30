package edu.cornell.kfs.fp.batch.service.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.batch.service.CuProcurementCardCreateDocumentService;
import edu.cornell.kfs.fp.document.CorporateBilledCorporatePaidDocument;


public class CorporateBilledCorporatePaidCreateDocumentServiceImpl implements ProcurementCardCreateDocumentService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidCreateDocumentServiceImpl.class);
    
    protected CuProcurementCardCreateDocumentService cuProcurementCardCreateDocumentService;
    protected DocumentService documentService;
    
    @Override
    public boolean createProcurementCardDocuments() {
        LOG.info("entering createProcurementCardDocuments");

        List<CorporateBilledCorporatePaidDocument> cbcpDocuments = new ArrayList<CorporateBilledCorporatePaidDocument>();
        List cardTransactions = cuProcurementCardCreateDocumentService.retrieveTransactions();
        
        for (Object cardTransactionsItems : cardTransactions) {
            List cardTtransactions = (List)cardTransactionsItems;
            ProcurementCardDocument pCardDocument = cuProcurementCardCreateDocumentService.createProcurementCardDocument(cardTtransactions);
            try {
                cbcpDocuments.add(buildCorporateBilledCorporatePaidDocument(pCardDocument));
            } catch (WorkflowException e) {
                LOG.error("createProcurementCardDocuments, had an error converting pcard document into cbcp document", e);
                throw new RuntimeException(e);
            }
        }
        for (CorporateBilledCorporatePaidDocument cbcpDocument : cbcpDocuments) {
            try {
                documentService.saveDocument(cbcpDocument);
                LOG.info("createProcurementCardDocuments() Saved Procurement Card document: " + cbcpDocument.getDocumentNumber());
            } catch (Exception e) {
                LOG.error("createProcurementCardDocuments() Error persisting document # " + cbcpDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
                throw new RuntimeException("Error persisting document # " + cbcpDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
            }
        }
        /**
         * @todo deal with email
         */
        //sendEmailNotification(documents);

        return true;
    }
    
    public CorporateBilledCorporatePaidDocument buildCorporateBilledCorporatePaidDocument(ProcurementCardDocument pCardDocument) throws WorkflowException {
        CorporateBilledCorporatePaidDocument cbcpDoc = (CorporateBilledCorporatePaidDocument) documentService.getNewDocument(CorporateBilledCorporatePaidDocument.class);
        cbcpDoc.getDocumentHeader().setDocumentDescription(pCardDocument.getDocumentHeader().getDocumentDescription());
        cbcpDoc.getDocumentHeader().setExplanation(pCardDocument.getDocumentHeader().getExplanation());
        cbcpDoc.setAccountingPeriod(pCardDocument.getAccountingPeriod());
        cbcpDoc.setAccountingPeriodCompositeString(pCardDocument.getAccountingPeriodCompositeString());
        cbcpDoc.setAdHocRoutePersons(pCardDocument.getAdHocRoutePersons());
        cbcpDoc.setAdHocRouteWorkgroups(pCardDocument.getAdHocRouteWorkgroups());
        cbcpDoc.setApplicationDocumentStatus(pCardDocument.getApplicationDocumentStatus());
        cbcpDoc.setAutoApprovedIndicator(pCardDocument.isAutoApprovedIndicator());
        cbcpDoc.setCapitalAccountingLines(pCardDocument.getCapitalAccountingLines());
        cbcpDoc.setCapitalAccountingLinesExist(pCardDocument.isCapitalAccountingLinesExist());
        cbcpDoc.setCapitalAssetInformation(pCardDocument.getCapitalAssetInformation());
        cbcpDoc.setGeneralLedgerPendingEntries(pCardDocument.getGeneralLedgerPendingEntries());
        cbcpDoc.setNewCollectionRecord(pCardDocument.isNewCollectionRecord());
        cbcpDoc.setNextCapitalAssetLineNumber(pCardDocument.getNextCapitalAssetLineNumber());
        cbcpDoc.setNextSourceLineNumber(pCardDocument.getNextSourceLineNumber());
        cbcpDoc.setNextTargetLineNumber(pCardDocument.getNextTargetLineNumber());
        cbcpDoc.setNotes(pCardDocument.getNotes());
        cbcpDoc.setPostingPeriodCode(pCardDocument.getPostingPeriodCode());
        cbcpDoc.setPostingYear(pCardDocument.getPostingYear());
        cbcpDoc.setProcurementCardHolder(pCardDocument.getProcurementCardHolder());
        cbcpDoc.setSourceAccountingLines(pCardDocument.getSourceAccountingLines());
        cbcpDoc.setTargetAccountingLines(pCardDocument.getTargetAccountingLines());
        cbcpDoc.setTransactionEntries(pCardDocument.getTransactionEntries());
        
        return cbcpDoc;
    }
    
    @Override
    public boolean routeProcurementCardDocuments() {
        LOG.info("entering routeProcurementCardDocuments");
        boolean results = cuProcurementCardCreateDocumentService.routeProcurementCardDocuments();
        return results;
    }
    @Override
    public boolean autoApproveProcurementCardDocuments() {
        LOG.info("entering autoApproveProcurementCardDocuments");
        boolean results = cuProcurementCardCreateDocumentService.autoApproveProcurementCardDocuments();
        return results;
    }
    
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setCuProcurementCardCreateDocumentService(
            CuProcurementCardCreateDocumentService cuProcurementCardCreateDocumentService) {
        this.cuProcurementCardCreateDocumentService = cuProcurementCardCreateDocumentService;
    }
    
}
