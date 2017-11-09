package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.batch.ProcurementCardReportType;
import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.ProcurementCardHolder;
import org.kuali.kfs.fp.businessobject.ProcurementCardSourceAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTargetAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.mail.VelocityMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.service.CuProcurementCardCreateDocumentService;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidSourceAccountingLine;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTargetAccountingLine;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionDetail;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionDetailExtendedAttribute;
import edu.cornell.kfs.fp.document.CorporateBilledCorporatePaidDocument;


public class CorporateBilledCorporatePaidCreateDocumentServiceImpl implements ProcurementCardCreateDocumentService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidCreateDocumentServiceImpl.class);
    
    protected CuProcurementCardCreateDocumentService cuProcurementCardCreateDocumentService;
    protected DataDictionaryService dataDictionaryService;
    protected DocumentService documentService;
    protected FinancialSystemDocumentService financialSystemDocumentService;
    protected ParameterService parameterService;
    
    @Override
    public boolean createProcurementCardDocuments() {
        LOG.debug("createProcurementCardDocuments, entering");
        List<List> listOfCardTransactions = cuProcurementCardCreateDocumentService.retrieveTransactions();
        List<CorporateBilledCorporatePaidDocument> documents = new ArrayList<CorporateBilledCorporatePaidDocument>();
        for (List cardTransactions : listOfCardTransactions) {
            ProcurementCardDocument pCardDocument = cuProcurementCardCreateDocumentService.createProcurementCardDocument(cardTransactions);
            CorporateBilledCorporatePaidDocument cbcpDocument;
            try {
                cbcpDocument = buildCorporateBilledCorporatePaidDocument(pCardDocument);
                try {
                    documentService.saveDocument(cbcpDocument);
                    LOG.info("createProcurementCardDocuments() Saved CBCP document: " + cbcpDocument.getDocumentNumber());
                } catch (Exception e) {
                    LOG.error("createProcurementCardDocuments() Error persisting document # " + cbcpDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
                    throw new RuntimeException("Error persisting document # " + cbcpDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
                }
            } catch (WorkflowException e1) {
                LOG.error("createProcurementCardDocuments, problem creating CBCP document", e1);
                throw new RuntimeException(e1);
            }
            
        }

        return true;
    }
    
    protected CorporateBilledCorporatePaidDocument buildCorporateBilledCorporatePaidDocument(ProcurementCardDocument pCardDocument) throws WorkflowException {
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
    
    protected List<CapitalAssetInformation> buildNewCapitalAssetInformation(List orginalCapitalAssetInformation, CorporateBilledCorporatePaidDocument cbcpDoc) {
        List<CapitalAssetInformation> capitalAssetInformationList = orginalCapitalAssetInformation;
        List<CapitalAssetInformation> newInfoList = new ArrayList<CapitalAssetInformation>();
        for (CapitalAssetInformation oldInfoLine : capitalAssetInformationList) {
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
    
    protected List<CorporateBilledCorporatePaidTransactionDetail> buildNewTransactionList(List originalTransactiolist, CorporateBilledCorporatePaidDocument cbcpDoc) {
        List<ProcurementCardTransactionDetail> procurementCardTransactionDetailList = originalTransactiolist;
        
        List<CorporateBilledCorporatePaidTransactionDetail> newTransactionList = new ArrayList<CorporateBilledCorporatePaidTransactionDetail>();
        
        for (ProcurementCardTransactionDetail originalTransaction : procurementCardTransactionDetailList) {
            CorporateBilledCorporatePaidTransactionDetail newTransaction = new CorporateBilledCorporatePaidTransactionDetail(originalTransaction, cbcpDoc.getDocumentNumber());
            
            addTransactions(newTransaction, originalTransaction.getSourceAccountingLines(), cbcpDoc);
            addTransactions(newTransaction, originalTransaction.getTargetAccountingLines(), cbcpDoc);
            
            logProcurementCardTransactionDetail(newTransaction);
            
            newTransactionList.add(newTransaction);
        }
        return newTransactionList;
    }
    
    protected void addTransactions(CorporateBilledCorporatePaidTransactionDetail newTransaction, List accountingLines, CorporateBilledCorporatePaidDocument cbcpDoc) {
        List<AccountingLineBase> accountingLineBases = accountingLines;
        for (AccountingLineBase lineBase : accountingLineBases) {
            if (lineBase.isSourceAccountingLine()) {
                LOG.info("addTransactions, found source line");
                ProcurementCardSourceAccountingLine originalLine = (ProcurementCardSourceAccountingLine) lineBase;
                CorporateBilledCorporatePaidSourceAccountingLine newLine = new CorporateBilledCorporatePaidSourceAccountingLine(originalLine, newTransaction.getDocumentNumber());
                newLine.setFinancialObjectCode(getCorporateBilledCorporatePaidDocumentParameter(
                        CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_LIABILITY_OBJECT_CODE));
                processLine(newLine, cbcpDoc);
                newTransaction.getSourceAccountingLines().add(newLine);
            } else {
                LOG.info("addTransactions, found target line");
                ProcurementCardTargetAccountingLine originalLine = (ProcurementCardTargetAccountingLine) lineBase;
                CorporateBilledCorporatePaidTargetAccountingLine newLine = new CorporateBilledCorporatePaidTargetAccountingLine(originalLine, newTransaction.getDocumentNumber());
                newLine.setFinancialObjectCode(getCorporateBilledCorporatePaidDocumentParameter(
                        CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_AMOUNT_OWED_OBJECT_CODE));
                processLine(newLine, cbcpDoc);
                newTransaction.getTargetAccountingLines().add(newLine);
            }
        }
    }
    

    
    protected void processLine(AccountingLineBase accountingLine, CorporateBilledCorporatePaidDocument cbcpDoc) {
        accountingLine.setDocumentNumber(cbcpDoc.getDocumentNumber());
        accountingLine.setAccountNumber(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_ACCOUNT));
        accountingLine.setSubAccountNumber(StringUtils.EMPTY);
        accountingLine.setFinancialSubObjectCode(StringUtils.EMPTY);
        setOrgRefId(accountingLine, cbcpDoc.getProcurementCardHolder().getCardHolderAlternateName());
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
    
    private void logProcurementCardTransactionDetail(CorporateBilledCorporatePaidTransactionDetail transactionDetail) {
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
        List<AccountingLineBase> accountinLineBases = accountingLines;
        accountinLineBases.stream().forEach(line -> sb.append(line.toString()));
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
        LOG.debug("routeProcurementCardDocuments() started");
        Collection<CorporateBilledCorporatePaidDocument> procurementCardDocumentList = retrieveCorporateBilledCorporatePaidDocuments(
                KewApiConstants.ROUTE_HEADER_SAVED_CD);
        LOG.info("routeProcurementCardDocuments() Number of CBCP documents to Route: " + procurementCardDocumentList.size());

        for (CorporateBilledCorporatePaidDocument cbcpDocument : procurementCardDocumentList) {
            try {
                LOG.info("routeProcurementCardDocuments() Routing CBCP document # " + cbcpDocument.getDocumentNumber() + ".");
                documentService.prepareWorkflowDocument(cbcpDocument);
                documentService.routeDocument(cbcpDocument, "CBCP document automatically routed", new ArrayList<AdHocRouteRecipient>());
            } catch (WorkflowException | ValidationException e) {
                LOG.error("Error routing document # " + cbcpDocument.getDocumentNumber() + " " + e.getMessage(), e);
                //throw new RuntimeException(e.getMessage(), e);
            }
        }
        return true;
    }
    
    protected Collection<CorporateBilledCorporatePaidDocument> retrieveCorporateBilledCorporatePaidDocuments(String statusCode) {
        try {
            return financialSystemDocumentService.findByWorkflowStatusCode(CorporateBilledCorporatePaidDocument.class, DocumentStatus.fromCode(statusCode));
        } catch (WorkflowException e) {
            LOG.error("Error searching for enroute procurement card documents " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean autoApproveProcurementCardDocuments() {
        //cbcp documents route to final on submission, intentionally left blank
        LOG.debug("entering autoApproveProcurementCardDocuments");
        return true;
    }

    public void setCuProcurementCardCreateDocumentService(
            CuProcurementCardCreateDocumentService cuProcurementCardCreateDocumentService) {
        this.cuProcurementCardCreateDocumentService = cuProcurementCardCreateDocumentService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
