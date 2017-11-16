package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.batch.ProcurementCardCreateDocumentsStep;
import org.kuali.kfs.fp.businessobject.ProcurementCardDefault;
import org.kuali.kfs.fp.businessobject.ProcurementCardSourceAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTargetAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidSourceAccountingLine;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTargetAccountingLine;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionDetail;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionDetailExtendedAttribute;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionDetailExtendedAttribute;
import edu.cornell.kfs.fp.document.CorporateBilledCorporatePaidDocument;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CorporateBilledCorporatePaidCreateDocumentServiceImpl extends ProcurementCardCreateDocumentServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidCreateDocumentServiceImpl.class);
    
    protected DataDictionaryService dataDictionaryService;
    
    protected SimpleDateFormat dateFormat;
    
    @Override
    public ProcurementCardDocument createProcurementCardDocument(List transactions) {
        CorporateBilledCorporatePaidDocument cbcpDocument = (CorporateBilledCorporatePaidDocument) super.createProcurementCardDocument(transactions);
        cbcpDocument.getDocumentHeader().setExplanation(
                getCorporateBilledCorporatePaidDocumentParameter(CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DOCUMENT_EXPLANATION));
        setOrganizationDocumentNumberToPostingDate(cbcpDocument, cbcpDocument.getProcurementCardTransactionPostingDetailDate());
        return cbcpDocument;
    }
    
    protected void setOrganizationDocumentNumberToPostingDate(CorporateBilledCorporatePaidDocument cbcpDoc, Date transactionPostingDate) {
        if (transactionPostingDate != null) {
            cbcpDoc.getDocumentHeader().setOrganizationDocumentNumber(getDateFormat().format(transactionPostingDate));
        } else {
            LOG.error("setDocumentOrgDocumentNumber, unable to set the org document number, the posting date is null.");
        }
    }
    
    @Override
    protected CorporateBilledCorporatePaidDocument buildNewDocument() throws WorkflowException {
        return (CorporateBilledCorporatePaidDocument) documentService.getNewDocument(CorporateBilledCorporatePaidDocument.class);
    }
    
    @Override
    protected String createTransactionDetailRecord(ProcurementCardDocument cbcpDocument, ProcurementCardTransaction transaction, Integer transactionLineNumber) {
        CorporateBilledCorporatePaidTransactionDetail transactionDetail = new CorporateBilledCorporatePaidTransactionDetail();
        transactionDetail.setCorporateBilledCorporatePaidDocument((CorporateBilledCorporatePaidDocument) cbcpDocument);

        // set the document transaction detail fields from the loaded transaction record
        transactionDetail.setDocumentNumber(cbcpDocument.getDocumentNumber());
        transactionDetail.setFinancialDocumentTransactionLineNumber(transactionLineNumber);
        transactionDetail.setTransactionDate(transaction.getTransactionDate());
        transactionDetail.setTransactionReferenceNumber(transaction.getTransactionReferenceNumber());
        transactionDetail.setTransactionBillingCurrencyCode(transaction.getTransactionBillingCurrencyCode());
        transactionDetail.setTransactionCurrencyExchangeRate(transaction.getTransactionCurrencyExchangeRate());
        transactionDetail.setTransactionDate(transaction.getTransactionDate());
        transactionDetail.setTransactionOriginalCurrencyAmount(transaction.getTransactionOriginalCurrencyAmount());
        transactionDetail.setTransactionOriginalCurrencyCode(transaction.getTransactionOriginalCurrencyCode());
        transactionDetail.setTransactionPointOfSaleCode(transaction.getTransactionPointOfSaleCode());
        transactionDetail.setTransactionPostingDate(transaction.getTransactionPostingDate());
        transactionDetail.setTransactionPurchaseIdentifierDescription(transaction.getTransactionPurchaseIdentifierDescription());
        transactionDetail.setTransactionPurchaseIdentifierIndicator(transaction.getTransactionPurchaseIdentifierIndicator());
        transactionDetail.setTransactionSalesTaxAmount(transaction.getTransactionSalesTaxAmount());
        transactionDetail.setTransactionSettlementAmount(transaction.getTransactionSettlementAmount());
        transactionDetail.setTransactionTaxExemptIndicator(transaction.getTransactionTaxExemptIndicator());
        transactionDetail.setTransactionTravelAuthorizationCode(transaction.getTransactionTravelAuthorizationCode());
        transactionDetail.setTransactionUnitContactName(transaction.getTransactionUnitContactName());

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount().negated());
        } else {
            transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount());
        }
        
        createProcurementCardTransactionDetailExtension(transaction, transactionDetail);

        createTransactionVendorRecord(cbcpDocument, transaction, transactionDetail);
        logTransactionDetails(transactionDetail);
        cbcpDocument.getTransactionEntries().add(transactionDetail);
        
        
        return createAndValidateAccountingLines(cbcpDocument, transaction, transactionDetail);
    }
    
    private void logTransactionDetails(CorporateBilledCorporatePaidTransactionDetail transactionDetail) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("logTransactionDetails, ");
            if (ObjectUtils.isNotNull(transactionDetail)) {
                sb.append("ProcurementCardTransactionDetail: ");
                sb.append("document number = ").append(transactionDetail.getDocumentNumber());
                sb.append(" transaction reference number - ").append(transactionDetail.getTransactionReferenceNumber());
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
        List<AccountingLineBase> accountingLineBases = accountingLines;
        accountingLineBases.stream().map(line -> line.toString()).forEach(sb::append);
    }
    
    @Override
    protected ProcurementCardTransactionDetailExtendedAttribute buildTransactionDetailExtension(ProcurementCardTransactionDetail transactionDetail) {
        CorporateBilledCorporatePaidTransactionDetailExtendedAttribute detailExtension;
          if (ObjectUtils.isNull(transactionDetail.getExtension())) {
              detailExtension = new CorporateBilledCorporatePaidTransactionDetailExtendedAttribute();
          } else {
              detailExtension = (CorporateBilledCorporatePaidTransactionDetailExtendedAttribute) transactionDetail.getExtension();
          }
        return detailExtension;
    }
    
    @Override
    protected ProcurementCardSourceAccountingLine createSourceAccountingLine(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        CorporateBilledCorporatePaidSourceAccountingLine sourceLine = new CorporateBilledCorporatePaidSourceAccountingLine();

        sourceLine.setDocumentNumber(docTransactionDetail.getDocumentNumber());
        sourceLine.setFinancialDocumentTransactionLineNumber(docTransactionDetail.getFinancialDocumentTransactionLineNumber());
        sourceLine.setChartOfAccountsCode(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_CHART));
        sourceLine.setAccountNumber(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_ACCOUNT));
        sourceLine.setFinancialObjectCode(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_LIABILITY_OBJECT_CODE));
        
        //CorporateBilledCorporatePaidTransactionDetail cbcpTransactionDetail = (CorporateBilledCorporatePaidTransactionDetail) docTransactionDetail;
        //setOrgRefId(sourceLine, cbcpTransactionDetail.getCorporateBilledCorporatePaidDocument().getProcurementCardHolder().getCardHolderAlternateName());

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            sourceLine.setAmount(transaction.getFinancialDocumentTotalAmount().negated());
        } else {
            sourceLine.setAmount(transaction.getFinancialDocumentTotalAmount());
        }

        return sourceLine;
    }
    
    @Override
    protected ProcurementCardTargetAccountingLine createTargetAccountingLine(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        
        CorporateBilledCorporatePaidTargetAccountingLine targetLine = new CorporateBilledCorporatePaidTargetAccountingLine();
        targetLine.setDocumentNumber(docTransactionDetail.getDocumentNumber());
        targetLine.setFinancialDocumentTransactionLineNumber(docTransactionDetail.getFinancialDocumentTransactionLineNumber());
        targetLine.setChartOfAccountsCode(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_CHART));
        targetLine.setAccountNumber(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_ACCOUNT));
        targetLine.setFinancialObjectCode(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_AMOUNT_OWED_OBJECT_CODE));
        targetLine.setProjectCode(transaction.getProjectCode());

        //CorporateBilledCorporatePaidTransactionDetail cbcpTransactionDetail = (CorporateBilledCorporatePaidTransactionDetail) docTransactionDetail;
        //setOrgRefId(targetLine, cbcpTransactionDetail.getCorporateBilledCorporatePaidDocument().getProcurementCardHolder().getCardHolderAlternateName());

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            targetLine.setAmount(transaction.getFinancialDocumentTotalAmount().negated());
        } else {
            targetLine.setAmount(transaction.getFinancialDocumentTotalAmount());
        }

        return targetLine;
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
                LOG.error("routeProcurementCardDocuments, Error routing document # " + cbcpDocument.getDocumentNumber() + " " + e.getMessage(), e);
            }
        }
        return true;
    }
    
    protected Collection<CorporateBilledCorporatePaidDocument> retrieveCorporateBilledCorporatePaidDocuments(String statusCode) {
        try {
            return financialSystemDocumentService.findByWorkflowStatusCode(CorporateBilledCorporatePaidDocument.class, DocumentStatus.fromCode(statusCode));
        } catch (WorkflowException e) {
            LOG.error("retrieveCorporateBilledCorporatePaidDocuments, Error searching for enroute procurement card documents " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean autoApproveProcurementCardDocuments() {
        LOG.debug("autoApproveProcurementCardDocuments, CBCP docs automatically route to final, this function does nothing.");
        return true;
    }
    
    protected String getCorporateBilledCorporatePaidDocumentParameter(String parameterName) {
        String parameterValue = parameterService.getParameterValueAsString(KFSConstants.ParameterNamespaces.FINANCIAL, 
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.COMPONENT_NAME, parameterName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCorporateBilledCorporatePaidDocumentParameter, param name: " + parameterName + " param value: " + parameterValue);
        }
        return parameterValue;
    }
    
    public SimpleDateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd); 
        }
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

}
