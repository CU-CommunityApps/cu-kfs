package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.ProcurementCardHolder;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.service.CuProcurementCardCreateDocumentService;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionDetailExtendedAttribute;
import edu.cornell.kfs.fp.document.CorporateBilledCorporatePaidDocument;


public class CorporateBilledCorporatePaidCreateDocumentServiceImpl implements ProcurementCardCreateDocumentService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidCreateDocumentServiceImpl.class);
    
    protected CuProcurementCardCreateDocumentService cuProcurementCardCreateDocumentService;
    protected DataDictionaryService dataDictionaryService;
    protected DocumentService documentService;
    protected ParameterService parameterService;
    
    @Override
    public boolean createProcurementCardDocuments() {
        LOG.debug("createProcurementCardDocuments, entering");
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
        cbcpDoc.getDocumentHeader().setExplanation(
                getCorporateBilledCorporatePaidDocumentParameter(CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DOCUMENT_EXPLANATION));
        
        cbcpDoc.setAccountingPeriod(pCardDocument.getAccountingPeriod());
        cbcpDoc.setAccountingPeriodCompositeString(pCardDocument.getAccountingPeriodCompositeString());
        cbcpDoc.setApplicationDocumentStatus(pCardDocument.getApplicationDocumentStatus());
        cbcpDoc.setAutoApprovedIndicator(pCardDocument.isAutoApprovedIndicator());
        cbcpDoc.setCapitalAccountingLinesExist(pCardDocument.isCapitalAccountingLinesExist());
        cbcpDoc.setCapitalAssetInformation(buildNewCapitalAssetInformation(pCardDocument.getCapitalAssetInformation(), cbcpDoc));
        cbcpDoc.setNewCollectionRecord(pCardDocument.isNewCollectionRecord());
        cbcpDoc.setNextCapitalAssetLineNumber(pCardDocument.getNextCapitalAssetLineNumber());
        cbcpDoc.setNextSourceLineNumber(pCardDocument.getNextSourceLineNumber());
        cbcpDoc.setNextTargetLineNumber(pCardDocument.getNextTargetLineNumber());
        cbcpDoc.setProcurementCardHolder(buildNewProcurementCardHolder(pCardDocument.getProcurementCardHolder(), cbcpDoc));
        cbcpDoc.setTransactionEntries(buildNewTransactionList(pCardDocument.getTransactionEntries(), cbcpDoc));
        resetOrganizationDocumentNumberToPostingDate(cbcpDoc, cbcpDoc.getProcurementCardTransactionPostingDetailDate());
        return cbcpDoc;
    }
    
    protected void resetOrganizationDocumentNumberToPostingDate(CorporateBilledCorporatePaidDocument cbcpDoc, Date transactionPostingDate) {
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
            accountingLine.setAccountNumber(getCorporateBilledCorporatePaidDocumentParameter(
                    CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_ACCOUNT));
            accountingLine.setSubAccountNumber(StringUtils.EMPTY);
            if (accountingLine.isSourceAccountingLine()) {
                accountingLine.setFinancialObjectCode(getCorporateBilledCorporatePaidDocumentParameter(
                        CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_LIABILITY_OBJECT_CODE));
            } else {
                accountingLine.setFinancialObjectCode(getCorporateBilledCorporatePaidDocumentParameter(
                        CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_AMOUNT_OWED_OBJECT_CODE));
            }
            accountingLine.setFinancialSubObjectCode(StringUtils.EMPTY);
            setOrgRefId(accountingLine, cbcpDoc.getProcurementCardHolder().getCardHolderAlternateName());
        }
    }
    
    protected void setOrgRefId(AccountingLineBase accountingLine, String orgRefId) {
        orgRefId = StringUtils.trim(orgRefId);
        String orgRefIdShortenedIfTooLong = StringUtils.substring(orgRefId, 0, dataDictionaryService.getAttributeMaxLength(SourceAccountingLine.class, 
                KFSPropertyConstants.ORGANIZATION_REFERENCE_ID));
        if (!StringUtils.equalsIgnoreCase(orgRefIdShortenedIfTooLong, orgRefId)) {
            LOG.error("setOrgRefId, had to change '" + orgRefId + "' to '" + orgRefIdShortenedIfTooLong + "'");
        }
        accountingLine.setOrganizationReferenceId(orgRefIdShortenedIfTooLong);
    }
    
    private void logProcurementCardTransactionDetail(ProcurementCardTransactionDetail transactionDetail) {
        if (LOG.isDebugEnabled()) {
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
            } else {
                sb.append("transaction detail is NULL");
            }
            LOG.debug(sb.toString());
        }
    }
    
    private void logAccountingLineBaseList(List accountingLines, StringBuilder sb) {
        for (Object line : accountingLines) {
            AccountingLineBase accountingLine = (AccountingLineBase) line;
            sb.append(accountingLine.toString());
        }
    }
    
    protected String getCorporateBilledCorporatePaidDocumentParameter(String parameterName){
        String parameterValue = parameterService.getParameterValueAsString("KFS-FP", 
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.COMPONENT_NAME, parameterName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCorporateBilledCorporatePaidDocumentParameter, param name: " + parameterName + " param value: " + parameterValue);
        }
        return parameterValue;
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
        //cbcp documents route to final on submission, intentionally left blank
        LOG.info("entering autoApproveProcurementCardDocuments");
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

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
}
