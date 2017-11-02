package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.ProcurementCardHolder;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.service.CuProcurementCardCreateDocumentService;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionDetailExtendedAttribute;
import edu.cornell.kfs.fp.document.CorporateBilledCorporatePaidDocument;


public class CorporateBilledCorporatePaidCreateDocumentServiceImpl implements ProcurementCardCreateDocumentService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidCreateDocumentServiceImpl.class);
    
    protected CuProcurementCardCreateDocumentService cuProcurementCardCreateDocumentService;
    protected DataDictionaryService dataDictionaryService;
    protected DocumentService documentService;
    
    @Override
    public boolean createProcurementCardDocuments() {
        LOG.info("entering createProcurementCardDocuments");
        List cardTransactions = cuProcurementCardCreateDocumentService.retrieveTransactions();
        
        for (Object cardTransactionsItems : cardTransactions) {
            List cardTtransactions = (List)cardTransactionsItems;
            ProcurementCardDocument pCardDocument = cuProcurementCardCreateDocumentService.createProcurementCardDocument(cardTtransactions);
            CorporateBilledCorporatePaidDocument cbcpDocument;
            try {
                cbcpDocument = buildCorporateBilledCorporatePaidDocument(pCardDocument);
                try {
                    documentService.saveDocument(cbcpDocument);
                    LOG.info("createProcurementCardDocuments() Saved Procurement Card document: " + cbcpDocument.getDocumentNumber());
                } catch (Exception e) {
                    LOG.error("createProcurementCardDocuments() Error persisting document # " + cbcpDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
                    throw new RuntimeException("Error persisting document # " + cbcpDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
                }
            } catch (WorkflowException e1) {
                LOG.error("createProcurementCardDocuments, problem creating CBCP document", e1);
                throw new RuntimeException(e1);
            }
            
        }
        
        /**
         * @todo deal with email
         */
        //sendEmailNotification(documents);

        return true;
    }
    
    public CorporateBilledCorporatePaidDocument buildCorporateBilledCorporatePaidDocument(ProcurementCardDocument pCardDocument) throws WorkflowException {
        CorporateBilledCorporatePaidDocument cbcpDoc = (CorporateBilledCorporatePaidDocument) documentService.getNewDocument(
                CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_DOCUMENT_TYPE_CODE);
        cbcpDoc.getDocumentHeader().setDocumentDescription(pCardDocument.getDocumentHeader().getDocumentDescription());
        /**
         * @todo use a parameter
         */
        String explanationText = "Corporate Billed Corporate Paid Daily Entries";
        cbcpDoc.getDocumentHeader().setExplanation(explanationText);
        
        cbcpDoc.setAccountingPeriod(pCardDocument.getAccountingPeriod());
        cbcpDoc.setAccountingPeriodCompositeString(pCardDocument.getAccountingPeriodCompositeString());
        cbcpDoc.setApplicationDocumentStatus(pCardDocument.getApplicationDocumentStatus());
        cbcpDoc.setAutoApprovedIndicator(pCardDocument.isAutoApprovedIndicator());
        //cbcpDoc.setCapitalAccountingLines(pCardDocument.getCapitalAccountingLines());
        cbcpDoc.setCapitalAccountingLinesExist(pCardDocument.isCapitalAccountingLinesExist());
        cbcpDoc.setCapitalAssetInformation(buildNewCapitalAssetInformation(pCardDocument.getCapitalAssetInformation(), cbcpDoc));
        cbcpDoc.setNewCollectionRecord(pCardDocument.isNewCollectionRecord());
        cbcpDoc.setNextCapitalAssetLineNumber(pCardDocument.getNextCapitalAssetLineNumber());
        cbcpDoc.setNextSourceLineNumber(pCardDocument.getNextSourceLineNumber());
        cbcpDoc.setNextTargetLineNumber(pCardDocument.getNextTargetLineNumber());
        //cbcpDoc.setNotes(pCardDocument.getNotes());
        cbcpDoc.setProcurementCardHolder(buildNewProcurementCardHolder(pCardDocument.getProcurementCardHolder(), cbcpDoc));
        cbcpDoc.setTransactionEntries(buildNewTransactionList(pCardDocument.getTransactionEntries(), cbcpDoc));
        setDocumentOrgDocumentNumber(cbcpDoc, cbcpDoc.getProcurementCardTransactionPostingDetailDate());
        return cbcpDoc;
    }
    
    protected void setDocumentOrgDocumentNumber(CorporateBilledCorporatePaidDocument cbcpDoc, Date transactionPostingDate) {
        if (transactionPostingDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd"); 
            cbcpDoc.getDocumentHeader().setOrganizationDocumentNumber(dateFormat.format(transactionPostingDate));
        } else {
            LOG.error("setDocumentOrgDocumentNumber, unable to set the org document number, the posting date is null.");
        }
    }
    
    protected List buildNewCapitalAssetInformation(List orginalCapitalAssetInformation, CorporateBilledCorporatePaidDocument cbcpDoc) {
        List newInfoList = new ArrayList();
        for (Object infoLine : orginalCapitalAssetInformation) {
            CapitalAssetInformation oldInfoLine = (CapitalAssetInformation) infoLine;
            CapitalAssetInformation newInfoLine = (CapitalAssetInformation) ObjectUtils.deepCopy(oldInfoLine);
            newInfoLine.setDocumentNumber(cbcpDoc.getDocumentNumber());
            newInfoList.add(newInfoLine);
        }
        return newInfoList;
    }
    
    protected ProcurementCardHolder buildNewProcurementCardHolder(ProcurementCardHolder originalProcurementCardHolder, CorporateBilledCorporatePaidDocument cbcpDoc) {
        ProcurementCardHolder newProcurementCardHolder = (ProcurementCardHolder) ObjectUtils.deepCopy(originalProcurementCardHolder);
        newProcurementCardHolder.setDocumentNumber(cbcpDoc.getDocumentNumber());
        return newProcurementCardHolder;
    }
    
    protected List buildNewTransactionList(List originalTransactiolist, CorporateBilledCorporatePaidDocument cbcpDoc) {
        List newTransactionList = new ArrayList();
        for (Object transaction : originalTransactiolist) {
            ProcurementCardTransactionDetail originalTransaction = (ProcurementCardTransactionDetail) transaction;
            ProcurementCardTransactionDetail newTransaction = (ProcurementCardTransactionDetail) ObjectUtils.deepCopy(originalTransaction);
            
            newTransaction.setDocumentNumber(cbcpDoc.getDocumentNumber());
            ProcurementCardTransactionDetailExtendedAttribute extension = (ProcurementCardTransactionDetailExtendedAttribute) newTransaction.getExtension();
            extension.setDocumentNumber(cbcpDoc.getDocumentNumber());

            newTransaction.getProcurementCardVendor().setDocumentNumber(cbcpDoc.getDocumentNumber());
            
            resetAccountingLine(newTransaction.getSourceAccountingLines(), cbcpDoc);
            resetAccountingLine(newTransaction.getTargetAccountingLines(), cbcpDoc);
            
            logProcurementCardTransactionDetail(newTransaction);
            newTransactionList.add(newTransaction);
        }
        return newTransactionList;
    }
    
    private void resetAccountingLine(List accountingLines, CorporateBilledCorporatePaidDocument cbcpDoc) {
        for (Object line : accountingLines) {
            AccountingLineBase accountingLine = (AccountingLineBase) line;
            accountingLine.setDocumentNumber(cbcpDoc.getDocumentNumber());
            /**
             * @todo replace these with parameters
             */
            accountingLine.setAccountNumber("G233700");
            accountingLine.setSubAccountNumber(StringUtils.EMPTY);
            if (accountingLine.isSourceAccountingLine()) {
                accountingLine.setFinancialObjectCode("2000");
            } else {
                accountingLine.setFinancialObjectCode("1640");
            }
            accountingLine.setFinancialSubObjectCode(StringUtils.EMPTY);
            setOrgRefId(accountingLine, cbcpDoc.getProcurementCardHolder().getCardHolderAlternateName());
        }
    }
    
    protected void setOrgRefId(AccountingLineBase accountingLine, String orgRefId) {
        String truncatedValue = StringUtils.substring(orgRefId, 0, dataDictionaryService.getAttributeMaxLength(SourceAccountingLine.class, 
                KFSPropertyConstants.ORGANIZATION_REFERENCE_ID));
        if (!StringUtils.equalsIgnoreCase(truncatedValue, orgRefId)) {
            LOG.info("setOrgRefId, had to change '" + orgRefId + "' to '" + truncatedValue + "'");
        }
        accountingLine.setOrganizationReferenceId(truncatedValue);
    }
    
    private void logProcurementCardTransactionDetail(ProcurementCardTransactionDetail transactionDetail) {
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder("logProcurementCardTransactionDetail, ");
            if (ObjectUtils.isNotNull(transactionDetail)) {
                sb.append("ProcurementCardTransactionDetail: ");
                sb.append("document number = ").append(transactionDetail.getDocumentNumber());
                sb.append(" tansaction reference number - ").append(transactionDetail.getTransactionReferenceNumber());
                sb.append(" financial line number - ").append(transactionDetail.getFinancialDocumentTransactionLineNumber());
                sb.append("  Source Lines - ");
                logAccountingLineBaseList(transactionDetail.getSourceAccountingLines(), sb);
                sb.append("  Target lines - ");
                logAccountingLineBaseList(transactionDetail.getTargetAccountingLines(), sb);
            }
            LOG.info(sb.toString());
        }
    }
    
    private void logAccountingLineBaseList(List accountingLines, StringBuilder sb) {
        for (Object line : accountingLines) {
            AccountingLineBase accountingLine = (AccountingLineBase) line;
            sb.append(accountingLine.toString());
        }
    }
    
    
    
    @Override
    public boolean routeProcurementCardDocuments() {
        LOG.info("entering routeProcurementCardDocuments");
        boolean results = cuProcurementCardCreateDocumentService.routeProcurementCardDocuments();
        return results;
        //return true;
    }
    @Override
    public boolean autoApproveProcurementCardDocuments() {
        LOG.info("entering autoApproveProcurementCardDocuments");
        //boolean results = cuProcurementCardCreateDocumentService.autoApproveProcurementCardDocuments();
        //return results;
        return true;
    }
    
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setCuProcurementCardCreateDocumentService(
            CuProcurementCardCreateDocumentService cuProcurementCardCreateDocumentService) {
        this.cuProcurementCardCreateDocumentService = cuProcurementCardCreateDocumentService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
    
}
