package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.businessobject.ProcurementCardDefault;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.service.CorporateBilledCorporatePaidCreateDocumentService;
import edu.cornell.kfs.fp.batch.service.CorporateBilledCorporatePaidRouteStepReportService;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidCardHolder;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidCardVendor;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidDataDetail;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidDataRecord;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidSourceAccountingLine;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTargetAccountingLine;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransaction;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionDetail;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionDetailExtendedAttribute;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionExtendedAttribute;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionDetailExtendedAttribute;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionExtendedAttribute;
import edu.cornell.kfs.fp.document.CorporateBilledCorporatePaidDocument;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CorporateBilledCorporatePaidCreateDocumentServiceImpl extends ProcurementCardCreateDocumentServiceImpl implements CorporateBilledCorporatePaidCreateDocumentService {
	private static final Logger LOG = LogManager.getLogger(CorporateBilledCorporatePaidCreateDocumentServiceImpl.class);
    
    protected DataDictionaryService cbcpDatadictionaryServce;
    protected SimpleDateFormat dateFormat;
    protected CorporateBilledCorporatePaidRouteStepReportService corporateBilledCorporatePaidRouteStepReportService;
    
    @Override
    protected void cleanTransactionsTable() {
        LOG.info("cleanTransactionsTable, cleaning CorporateBilledCorporatePaidTransaction table");
        businessObjectService.deleteMatching(CorporateBilledCorporatePaidTransaction.class, new HashMap<String, String>());
    }
    
    @Override
    protected List retrieveTransactions() {
        List<List<CorporateBilledCorporatePaidTransaction>> groupedTransactions = new ArrayList<List<CorporateBilledCorporatePaidTransaction>>();

        Collection<CorporateBilledCorporatePaidTransaction> cbcpTransactions = businessObjectService.findMatchingOrderBy(CorporateBilledCorporatePaidTransaction.class, 
                new HashMap<String, String>(), KFSPropertyConstants.TRANSACTION_CREDIT_CARD_NUMBER, true);

        if (getCorporateBilledCorporatePaidDocumentParameterValueAsBoolean(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.SINGLE_TRANSACTION_IND_PARAMETER_NAME)) {
            List<CorporateBilledCorporatePaidTransaction> documentTransactions = new ArrayList<CorporateBilledCorporatePaidTransaction>();
            for (CorporateBilledCorporatePaidTransaction transaction : cbcpTransactions) {
                documentTransactions.add(transaction);
                groupedTransactions.add(documentTransactions);
                documentTransactions = new ArrayList<CorporateBilledCorporatePaidTransaction>();
            }
        } else {
            Map<String, List<CorporateBilledCorporatePaidTransaction>> cardTransactionsMap = new HashMap<String, List<CorporateBilledCorporatePaidTransaction>>();
            for (CorporateBilledCorporatePaidTransaction transaction : cbcpTransactions) {
                if (!cardTransactionsMap.containsKey(transaction.getTransactionCreditCardNumber())) {
                    cardTransactionsMap.put(transaction.getTransactionCreditCardNumber(), new ArrayList<CorporateBilledCorporatePaidTransaction>());
                }
                cardTransactionsMap.get(transaction.getTransactionCreditCardNumber()).add(transaction);
            }
            
            for (List<CorporateBilledCorporatePaidTransaction> cbcpTransaction : cardTransactionsMap.values()) {
                groupedTransactions.add(cbcpTransaction);
            }
        }

        return groupedTransactions;
    }
    
    @Override
    public ProcurementCardDocument createProcurementCardDocument(List transactions) {
        CorporateBilledCorporatePaidDocument cbcpDocument = (CorporateBilledCorporatePaidDocument) super.createProcurementCardDocument(transactions);
        cbcpDocument.getDocumentHeader().setExplanation(
                getCorporateBilledCorporatePaidDocumentParameter(CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DOCUMENT_EXPLANATION_PARAMETER_NAME));
        setOrganizationDocumentNumberToPostingDate(cbcpDocument, cbcpDocument.getProcurementCardTransactionPostingDetailDate());
        return cbcpDocument;
    }
    
    protected void setOrganizationDocumentNumberToPostingDate(CorporateBilledCorporatePaidDocument cbcpDoc, Date transactionPostingDate) {
        if (transactionPostingDate != null) {
            cbcpDoc.getDocumentHeader().setOrganizationDocumentNumber(getDateFormat().format(transactionPostingDate));
        } else {
            LOG.error("setOrganizationDocumentNumberToPostingDate, unable to set the org document number, the posting date is null.");
        }
    }
    
    @Override
    protected CorporateBilledCorporatePaidDocument buildNewProcurementCardDocument() throws WorkflowException {
        return (CorporateBilledCorporatePaidDocument) documentService.getNewDocument(CorporateBilledCorporatePaidDocument.class);
    }
    
    @Override
    protected String createTransactionDetailRecord(ProcurementCardDocument cbcpDocument, ProcurementCardTransaction transaction, Integer transactionLineNumber) {
        CorporateBilledCorporatePaidTransactionDetail transactionDetail = new CorporateBilledCorporatePaidTransactionDetail();
        transactionDetail.setCorporateBilledCorporatePaidDocument((CorporateBilledCorporatePaidDocument) cbcpDocument);

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

        if (KFSConstants.GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount().negated());
        } else {
            transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount());
        }
        
        createProcurementCardTransactionDetailExtension(transaction, transactionDetail);

        createTransactionVendorRecord(cbcpDocument, transaction, transactionDetail);
        cbcpDocument.getTransactionEntries().add(transactionDetail);
        
        String createAndValidateAccountingLineString =  createAndValidateAccountingLines(cbcpDocument, transaction, transactionDetail);
        logTransactionDetails(transactionDetail);
        return createAndValidateAccountingLineString;
    }
    
    @Override
    protected void createTransactionVendorRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, ProcurementCardTransactionDetail transactionDetail) {
        CorporateBilledCorporatePaidCardVendor transactionVendor = new CorporateBilledCorporatePaidCardVendor();

        transactionVendor.setDocumentNumber(pcardDocument.getDocumentNumber());
        transactionVendor.setFinancialDocumentTransactionLineNumber(transactionDetail.getFinancialDocumentTransactionLineNumber());
        transactionVendor.setTransactionMerchantCategoryCode(transaction.getTransactionMerchantCategoryCode());
        transactionVendor.setVendorCityName(transaction.getVendorCityName());
        transactionVendor.setVendorLine1Address(transaction.getVendorLine1Address());
        transactionVendor.setVendorLine2Address(transaction.getVendorLine2Address());
        transactionVendor.setVendorName(transaction.getVendorName());
        transactionVendor.setVendorOrderNumber(transaction.getVendorOrderNumber());
        transactionVendor.setVendorStateCode(transaction.getVendorStateCode());
        transactionVendor.setVendorZipCode(transaction.getVendorZipCode());
        transactionVendor.setVisaVendorIdentifier(transaction.getVisaVendorIdentifier());

        transactionDetail.setProcurementCardVendor(transactionVendor);
    }
    
    private void logTransactionDetails(CorporateBilledCorporatePaidTransactionDetail transactionDetail) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("logTransactionDetails, ");
            if (ObjectUtils.isNotNull(transactionDetail)) {
                sb.append("ProcurementCardTransactionDetail: ");
                sb.append("document number - ").append(transactionDetail.getDocumentNumber());
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
        accountingLineBases.stream().map(AccountingLineBase::toString).forEach(sb::append);
    }
    
    @Override
    protected CorporateBilledCorporatePaidTransactionDetailExtendedAttribute buildTransactionDetailExtensionObject(ProcurementCardTransactionDetail transactionDetail) {
        CorporateBilledCorporatePaidTransactionDetailExtendedAttribute detailExtension;
          if (ObjectUtils.isNull(transactionDetail.getExtension())) {
              detailExtension = new CorporateBilledCorporatePaidTransactionDetailExtendedAttribute();
          } else {
              detailExtension = (CorporateBilledCorporatePaidTransactionDetailExtendedAttribute) transactionDetail.getExtension();
          }
        return detailExtension;
    }
    
    @Override
    protected CorporateBilledCorporatePaidSourceAccountingLine createSourceAccountingLine(ProcurementCardTransaction transaction, 
            ProcurementCardTransactionDetail docTransactionDetail) {
        CorporateBilledCorporatePaidSourceAccountingLine sourceLine = new CorporateBilledCorporatePaidSourceAccountingLine();

        sourceLine.setDocumentNumber(docTransactionDetail.getDocumentNumber());
        sourceLine.setFinancialDocumentTransactionLineNumber(docTransactionDetail.getFinancialDocumentTransactionLineNumber());
        sourceLine.setChartOfAccountsCode(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_CHART_PARAMETER_NAME));
        sourceLine.setAccountNumber(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_ACCOUNT_PARAMETER_NAME));
        sourceLine.setFinancialObjectCode(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_LIABILITY_OBJECT_CODE_PARAMETER_NAME));
        
        CorporateBilledCorporatePaidTransactionDetail cbcpTransactionDetail = (CorporateBilledCorporatePaidTransactionDetail) docTransactionDetail;
        setOrgRefId(sourceLine, cbcpTransactionDetail.getCorporateBilledCorporatePaidDocument().getProcurementCardHolder().getCardHolderAlternateName());

        if (KFSConstants.GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            sourceLine.setAmount(transaction.getFinancialDocumentTotalAmount().negated());
        } else {
            sourceLine.setAmount(transaction.getFinancialDocumentTotalAmount());
        }

        return sourceLine;
    }
    
    @Override
    protected CorporateBilledCorporatePaidTargetAccountingLine createTargetAccountingLine(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        
        CorporateBilledCorporatePaidTargetAccountingLine targetLine = new CorporateBilledCorporatePaidTargetAccountingLine();
        targetLine.setDocumentNumber(docTransactionDetail.getDocumentNumber());
        targetLine.setFinancialDocumentTransactionLineNumber(docTransactionDetail.getFinancialDocumentTransactionLineNumber());
        targetLine.setChartOfAccountsCode(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_CHART_PARAMETER_NAME));
        targetLine.setAccountNumber(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_ACCOUNT_PARAMETER_NAME));
        targetLine.setFinancialObjectCode(getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_AMOUNT_OWED_OBJECT_CODE_PARAMETER_NAME));
        targetLine.setProjectCode(transaction.getProjectCode());

        CorporateBilledCorporatePaidTransactionDetail cbcpTransactionDetail = (CorporateBilledCorporatePaidTransactionDetail) docTransactionDetail;
        setOrgRefId(targetLine, cbcpTransactionDetail.getCorporateBilledCorporatePaidDocument().getProcurementCardHolder().getCardHolderAlternateName());

        if (KFSConstants.GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            targetLine.setAmount(transaction.getFinancialDocumentTotalAmount().negated());
        } else {
            targetLine.setAmount(transaction.getFinancialDocumentTotalAmount());
        }

        return targetLine;
    }
    
    protected void setOrgRefId(AccountingLineBase accountingLine, String orgRefId) {
        orgRefId = StringUtils.trim(orgRefId);
        String orgRefIdShortenedIfTooLong = StringUtils.substring(orgRefId, 0, cbcpDatadictionaryServce.getAttributeMaxLength(SourceAccountingLine.class, 
                KFSPropertyConstants.ORGANIZATION_REFERENCE_ID));
        if (!StringUtils.equalsIgnoreCase(orgRefIdShortenedIfTooLong, orgRefId)) {
            LOG.error("setOrgRefId, had to change '" + orgRefId + "' to '" + orgRefIdShortenedIfTooLong + "'");
        }
        accountingLine.setOrganizationReferenceId(orgRefIdShortenedIfTooLong);
    }
    
    @Override
    protected void createPurchasingDataDetails(ProcurementCardTransactionExtendedAttribute extension,
            ProcurementCardTransactionDetailExtendedAttribute detailExtension) {
        CorporateBilledCorporatePaidTransactionExtendedAttribute cbcpTransactionExtension = (CorporateBilledCorporatePaidTransactionExtendedAttribute) extension;
        List<CorporateBilledCorporatePaidDataDetail> details = new ArrayList<CorporateBilledCorporatePaidDataDetail>();
        CorporateBilledCorporatePaidTransactionDetailExtendedAttribute cbcpDetailExtension = 
                (CorporateBilledCorporatePaidTransactionDetailExtendedAttribute) detailExtension;
        
        for (CorporateBilledCorporatePaidDataRecord record : cbcpTransactionExtension.getCorporateBilledCorporatePaidDataRecords()) {
            CorporateBilledCorporatePaidDataDetail detail = new CorporateBilledCorporatePaidDataDetail();
            detail.setDocumentNumber(cbcpDetailExtension.getDocumentNumber());
            detail.setFinancialDocumentTransactionLineNumber(cbcpDetailExtension.getFinancialDocumentTransactionLineNumber());
            detail.populateFromRecord(record);
            details.add(detail);
        }
        cbcpDetailExtension.setCorporateBilledCorporatePaidDataDetails(details);
    }
    
    @Override
    protected void createCardHolderRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction) {
        CorporateBilledCorporatePaidCardHolder cardHolder = new CorporateBilledCorporatePaidCardHolder();

        cardHolder.setDocumentNumber(pcardDocument.getDocumentNumber());
        cardHolder.setTransactionCreditCardNumber(transaction.getTransactionCreditCardNumber());

        processDefaultCardHolder(transaction, cardHolder);

        if (StringUtils.isEmpty(cardHolder.getAccountNumber())) {
            cardHolder.setChartOfAccountsCode(transaction.getChartOfAccountsCode());
            cardHolder.setAccountNumber(transaction.getAccountNumber());
            cardHolder.setSubAccountNumber(transaction.getSubAccountNumber());
        }
        if (StringUtils.isEmpty(cardHolder.getCardHolderName())) {
            cardHolder.setCardCycleAmountLimit(transaction.getCardCycleAmountLimit());
            cardHolder.setCardCycleVolumeLimit(transaction.getCardCycleVolumeLimit());
            cardHolder.setCardHolderAlternateName(transaction.getCardHolderAlternateName());
            cardHolder.setCardHolderName(transaction.getCardHolderName());
            cardHolder.setCardLimit(transaction.getCardLimit());
            cardHolder.setCardNoteText(transaction.getCardNoteText());
            cardHolder.setCardStatusCode(transaction.getCardStatusCode());
        }

        pcardDocument.setProcurementCardHolder(cardHolder);
    }

    protected void processDefaultCardHolder(ProcurementCardTransaction transaction, CorporateBilledCorporatePaidCardHolder cardHolder) {
        final ProcurementCardDefault procurementCardDefault = retrieveProcurementCardDefault(transaction.getTransactionCreditCardNumber());
        if (procurementCardDefault != null) {
            if (getCorporateBilledCorporatePaidDocumentParameterValueAsBoolean(CuFPParameterConstants.CorporateBilledCorporatePaidDocument.CBCP_HOLDER_DEFAULT_IND_PARAMETER_NAME)) {
                cardHolder.setCardCycleAmountLimit(procurementCardDefault.getCardCycleAmountLimit());
                cardHolder.setCardCycleVolumeLimit(procurementCardDefault.getCardCycleVolumeLimit());
                cardHolder.setCardHolderAlternateName(procurementCardDefault.getCardHolderAlternateName());
                cardHolder.setCardHolderName(procurementCardDefault.getCardHolderName());
                cardHolder.setCardLimit(procurementCardDefault.getCardLimit());
                cardHolder.setCardNoteText(procurementCardDefault.getCardNoteText());
                cardHolder.setCardStatusCode(procurementCardDefault.getCardStatusCode());
            }
            if (getCorporateBilledCorporatePaidDocumentParameterValueAsBoolean(CuFPParameterConstants.CorporateBilledCorporatePaidDocument.CBCP_ACCOUNTING_DEFAULT_IND_PARAMETER_NAME)) {
                cardHolder.setChartOfAccountsCode(procurementCardDefault.getChartOfAccountsCode());
                cardHolder.setAccountNumber(procurementCardDefault.getAccountNumber());
                cardHolder.setSubAccountNumber(procurementCardDefault.getSubAccountNumber());
            }
        }
    }
    
    @Override
    public boolean routeProcurementCardDocuments() {
        LOG.debug("routeProcurementCardDocuments() started");
        Collection<CorporateBilledCorporatePaidDocument> cbcpDocumentList = retrieveCorporateBilledCorporatePaidDocuments(
                KewApiConstants.ROUTE_HEADER_SAVED_CD);
        LOG.info("routeProcurementCardDocuments() Number of CBCP documents to Route: " + cbcpDocumentList.size());
        
        Map<String, String> documentErrors = new HashMap<String, String>();
        List<String> successfulDocuments = new ArrayList<String>();
        for (CorporateBilledCorporatePaidDocument cbcpDocument : cbcpDocumentList) {
            try {
                LOG.info("routeProcurementCardDocuments() Routing CBCP document # " + cbcpDocument.getDocumentNumber() + ".");
                documentService.prepareWorkflowDocument(cbcpDocument);
                documentService.routeDocument(cbcpDocument, "CBCP document automatically routed", new ArrayList<AdHocRouteRecipient>());
                successfulDocuments.add(cbcpDocument.getDocumentNumber());
            } catch (ValidationException ve) {
                documentErrors.put(cbcpDocument.getDocumentNumber(), corporateBilledCorporatePaidRouteStepReportService.buildValidationErrorMessage(ve));
                GlobalVariables.getMessageMap().clearErrorMessages();
                LOG.error("routeProcurementCardDocuments, Error routing document # " + cbcpDocument.getDocumentNumber() + " " + ve.getMessage(), ve);
            }
        }
        createAndEmailReport(cbcpDocumentList, documentErrors, successfulDocuments);
        return true;
    }

    protected Collection<CorporateBilledCorporatePaidDocument> retrieveCorporateBilledCorporatePaidDocuments(String statusCode) {
        return financialSystemDocumentService.findByWorkflowStatusCode(CorporateBilledCorporatePaidDocument.class, DocumentStatus.fromCode(statusCode));
    }
    
    protected void createAndEmailReport(Collection<CorporateBilledCorporatePaidDocument> cbcpDocumentList,
            Map<String, String> documentErrors, List<String> successfulDocuments) {
        corporateBilledCorporatePaidRouteStepReportService.createReport(cbcpDocumentList.size(), successfulDocuments, documentErrors);
        String reportEmailAddres = getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.CBCP_REPORT_EMAIL_ADDRESS_PARAMETER_NAME);
        corporateBilledCorporatePaidRouteStepReportService.sendReportEmail(reportEmailAddres, reportEmailAddres);
    }
    
    @Override
    public boolean autoApproveProcurementCardDocuments() {
        LOG.debug("autoApproveProcurementCardDocuments, CBCP docs automatically route to final, this function does nothing.");
        return true;
    }
    
    @Override
    public String getCorporateBilledCorporatePaidDocumentParameter(String parameterName) {
        String parameterValue = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.CBCP_COMPONENT_NAME, parameterName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCorporateBilledCorporatePaidDocumentParameter, param name: " + parameterName + " param value: " + parameterValue);
        }
        return parameterValue;
    }
    
    public Boolean getCorporateBilledCorporatePaidDocumentParameterValueAsBoolean(String parameterName) {
        Boolean parameterValue = parameterService.getParameterValueAsBoolean(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.CBCP_COMPONENT_NAME, parameterName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCorporateBilledCorporatePaidDocumentParameterValueAsBoolean, param name: " + parameterName + " param value: " + parameterValue);
        }
        return parameterValue;
    }
    
    public SimpleDateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd, Locale.US); 
        }
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setCbcpDatadictionaryServce(DataDictionaryService cbcpDatadictionaryServce) {
        this.cbcpDatadictionaryServce = cbcpDatadictionaryServce;
    }

    public void setCorporateBilledCorporatePaidRouteStepReportService(
            CorporateBilledCorporatePaidRouteStepReportService corporateBilledCorporatePaidRouteStepReportService) {
        this.corporateBilledCorporatePaidRouteStepReportService = corporateBilledCorporatePaidRouteStepReportService;
    }

}

