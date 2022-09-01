package edu.cornell.kfs.fp.batch.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.fp.batch.ProcurementCardCreateDocumentsStep;
import org.kuali.kfs.fp.batch.ProcurementCardReportType;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.ProcurementCardTargetAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;

import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionDetailExtendedAttribute;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionExtendedAttribute;
import edu.cornell.kfs.fp.businessobject.PurchasingDataDetail;
import edu.cornell.kfs.fp.businessobject.PurchasingDataRecord;
import edu.cornell.kfs.fp.CuFPParameterConstants;


public class ProcurementCardCreateDocumentServiceImpl extends org.kuali.kfs.fp.batch.service.impl.ProcurementCardCreateDocumentServiceImpl {
	private static final Logger LOG = LogManager.getLogger(ProcurementCardCreateDocumentServiceImpl.class);
    private static final int CARD_HOLDER_MAX_LENGTH = 15;
    private static int VENDOR_NAME_MAX_LENGTH = 19;
    private static final int CC_LAST_FOUR = 4;
    private static final int MAX_DOC_DESC_LENGTH = 40;

    /**
     * Creates a ProcurementCardDocument from the List of transactions given.
     * 
     * @param transactions List of ProcurementCardTransaction objects to be used for creating the document.
     * @return A ProcurementCardDocument populated with the transactions provided.
     */
    @SuppressWarnings({ "rawtypes", "deprecation" })
    @Override
    public ProcurementCardDocument createProcurementCardDocument(List transactions) {
        ProcurementCardDocument pcardDocument = null;
        
        try {
            // get new document from doc service
            pcardDocument = buildNewProcurementCardDocument();
            
            List<CapitalAssetInformation> capitalAssets = pcardDocument.getCapitalAssetInformation();
            for (CapitalAssetInformation capitalAsset : capitalAssets) {
                if (ObjectUtils.isNotNull(capitalAsset) && ObjectUtils.isNotNull(capitalAsset.getCapitalAssetInformationDetails())) {
                    capitalAsset.setDocumentNumber(pcardDocument.getDocumentNumber());
                }
            }

            ProcurementCardTransaction trans = (ProcurementCardTransaction) transactions.get(0);
            String errorText = validateTransaction(trans);
            createCardHolderRecord(pcardDocument, trans);

            // for each transaction, create transaction detail object and then acct lines for the detail
            int transactionLineNumber = 1;
            KualiDecimal documentTotalAmount = KualiDecimal.ZERO;
            ProcurementCardTransaction transaction = null;
            for (Object transactionObj : transactions) {
                transaction = (ProcurementCardTransaction) transactionObj;
                
                // create transaction detail record with accounting lines
                errorText += createTransactionDetailRecord(pcardDocument, transaction, transactionLineNumber);

                // update document total
                documentTotalAmount = documentTotalAmount.add(transaction.getFinancialDocumentTotalAmount());

                transactionLineNumber++;
            }
            
            pcardDocument.getDocumentHeader().setFinancialDocumentTotalAmount(documentTotalAmount);
//            pcardDocument.getDocumentHeader().setDocumentDescription("SYSTEM Generated");

            transaction = (ProcurementCardTransaction) transactions.get(0);
            
            String cardHolderName = transaction.getCardHolderName();
            String vendorName = transaction.getVendorName();
            String transactionType = ((ProcurementCardTransactionExtendedAttribute)transaction.getExtension()).getTransactionType();
            
            if(transactionType!=null && StringUtils.isNotBlank(transactionType)){
            	VENDOR_NAME_MAX_LENGTH = 16;
            }
            else{
            	VENDOR_NAME_MAX_LENGTH = 19;
            }

            if (cardHolderName.length() > CARD_HOLDER_MAX_LENGTH && vendorName.length() > VENDOR_NAME_MAX_LENGTH) {
                cardHolderName = cardHolderName.substring(0, CARD_HOLDER_MAX_LENGTH);
                vendorName = vendorName.substring(0, VENDOR_NAME_MAX_LENGTH);
            }
            if (cardHolderName.length() > CARD_HOLDER_MAX_LENGTH && vendorName.length() <= VENDOR_NAME_MAX_LENGTH) {
                Integer endIndice = 0;
                if ((CARD_HOLDER_MAX_LENGTH + (VENDOR_NAME_MAX_LENGTH - vendorName.length())) > cardHolderName.length()) {
                    endIndice = cardHolderName.length();
                } else {
                    endIndice = CARD_HOLDER_MAX_LENGTH + (VENDOR_NAME_MAX_LENGTH - vendorName.length());
                }
                cardHolderName = cardHolderName.substring(0, endIndice);
            }
            if (vendorName.length() > VENDOR_NAME_MAX_LENGTH && cardHolderName.length() <= CARD_HOLDER_MAX_LENGTH) {
                Integer endIndice = 0;
                if ((VENDOR_NAME_MAX_LENGTH + (CARD_HOLDER_MAX_LENGTH - cardHolderName.length())) > vendorName.length()) {
                    endIndice = vendorName.length();
                } else {
                    endIndice = VENDOR_NAME_MAX_LENGTH + (CARD_HOLDER_MAX_LENGTH - cardHolderName.length());
                }
                vendorName = vendorName.substring(0, endIndice);
            }
            
            String creditCardNumber = transaction.getTransactionCreditCardNumber();
            String lastFour = "";

            if (creditCardNumber.length() > CC_LAST_FOUR) {
                lastFour = creditCardNumber.substring(creditCardNumber.length() - CC_LAST_FOUR);
            }
            String docDesc = cardHolderName + "/" + vendorName + "/" + lastFour;
            
            if(transactionType!=null && StringUtils.isNotBlank(transactionType)){
            	docDesc = transactionType + "/" + cardHolderName + "/" + vendorName + "/" + lastFour;
            }
 
            if (docDesc.length() > MAX_DOC_DESC_LENGTH) {
                docDesc = docDesc.substring(0, MAX_DOC_DESC_LENGTH);
            }
            pcardDocument.getDocumentHeader().setDocumentDescription(docDesc);
            
            // Remove duplicate messages from errorText
            String[] messages = StringUtils.split(errorText, ".");
            for (String message : messages) {
                int countMatches = StringUtils.countMatches(errorText, message) - 1;
                errorText = StringUtils.replace(errorText, message + ".", "", countMatches);
            }
            // In case errorText is still too long, truncate it and indicate so.
            Integer documentExplanationMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(),
                    KFSPropertyConstants.EXPLANATION);
            if (documentExplanationMaxLength != null && errorText.length() > documentExplanationMaxLength.intValue()) {
                String truncatedMessage = " ... TRUNCATED.";
                errorText = errorText.substring(0, documentExplanationMaxLength - truncatedMessage.length()) + truncatedMessage;
            }
            pcardDocument.getDocumentHeader().setExplanation(errorText);
        } catch (WorkflowException e) {
            LOG.error("Error creating pcdo documents: " + e.getMessage(),e);
            throw new RuntimeException("Error creating pcdo documents: " + e.getMessage(),e);
        }

        return pcardDocument;
    }

    protected ProcurementCardDocument buildNewProcurementCardDocument() throws WorkflowException {
        return (ProcurementCardDocument) documentService
                .getNewDocument(KFSConstants.FinancialDocumentTypeCodes.PROCUREMENT_CARD);
    }
    
    /**
     * 
     * @see org.kuali.kfs.fp.batch.service.impl.ProcurementCardCreateDocumentServiceImpl#createTransactionDetailRecord(org.kuali.kfs.fp.document.ProcurementCardDocument, org.kuali.kfs.fp.businessobject.ProcurementCardTransaction, java.lang.Integer)
     */
    @Override
    protected String createTransactionDetailRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, Integer transactionLineNumber) {
      String errorText = super.createTransactionDetailRecord(pcardDocument, transaction, transactionLineNumber);
      
      // create the Extension object, which contains the Level 3 info
      createProcurementCardTransactionDetailExtension(transaction, 
                                                      (ProcurementCardTransactionDetail) pcardDocument.getTransactionEntries()
                                                                                                      .get(pcardDocument.getTransactionEntries().size() - 1));
      
      return errorText;
    }


    protected void createProcurementCardTransactionDetailExtension(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail transactionDetail) {
      ProcurementCardTransactionDetailExtendedAttribute detailExtension = buildTransactionDetailExtensionObject(
            transactionDetail);

      if (ObjectUtils.isNotNull(transaction.getExtension())) {
          ProcurementCardTransactionExtendedAttribute extension = (ProcurementCardTransactionExtendedAttribute) transaction.getExtension();

          detailExtension.setDocumentNumber(transactionDetail.getDocumentNumber());
          detailExtension.setFinancialDocumentTransactionLineNumber(transactionDetail.getFinancialDocumentTransactionLineNumber());

          createPurchasingDataDetails(extension, detailExtension);
      }
      transactionDetail.setExtension(detailExtension);
    }

    protected ProcurementCardTransactionDetailExtendedAttribute buildTransactionDetailExtensionObject(
            ProcurementCardTransactionDetail transactionDetail) {
        ProcurementCardTransactionDetailExtendedAttribute detailExtension;
          if (ObjectUtils.isNull(transactionDetail.getExtension())) {
              detailExtension = new ProcurementCardTransactionDetailExtendedAttribute();
          } else {
              detailExtension = (ProcurementCardTransactionDetailExtendedAttribute) transactionDetail.getExtension();
          }
        return detailExtension;
    }
    

    protected void createPurchasingDataDetails(ProcurementCardTransactionExtendedAttribute extension, ProcurementCardTransactionDetailExtendedAttribute detailExtension) {
        List<PurchasingDataDetail> details = new ArrayList<PurchasingDataDetail>();
        for (PurchasingDataRecord record : extension.getPurchasingDataRecords()) {
          PurchasingDataDetail detail = new PurchasingDataDetail();
          detail.setDocumentNumber(detailExtension.getDocumentNumber());
          detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
          detail.populateFromRecord(record);
          details.add(detail);
        }
        detailExtension.setPurchasingDataDetails(details);
    }
    
    @Override
    protected String validateTargetAccountingLine(ProcurementCardTargetAccountingLine targetLine) {
        targetLine.refresh();
        final String lineNumber = targetLine.getSequenceNumber() == null ? "new" :
            targetLine.getSequenceNumber().toString();

        String errorText = "";
        
        if (!accountingLineRuleUtil.isValidObjectCode("", targetLine.getObjectCode())) {
            String tempErrorText = "Target Accounting Line " + lineNumber + " Chart " 
                    + targetLine.getChartOfAccountsCode() + " Object Code " + targetLine.getFinancialObjectCode() 
                    + " is invalid; using default Object Code.";
            
            if (LOG.isInfoEnabled()) {
                LOG.info(tempErrorText);
            }
            
            errorText += " " + tempErrorText;
            

            targetLine.setFinancialObjectCode(getErrorObjectCode());
            targetLine.refresh();
        }
        errorText += " " + super.validateTargetAccountingLine(targetLine);
        return errorText;
        
    }
    
    @Override
    protected String validateTransaction(ProcurementCardTransaction transaction) {
        
        String errorText = "";
        
        UniversityDateService uds = SpringContext.getBean(UniversityDateService.class);
        ObjectCodeService ocs = SpringContext.getBean(ObjectCodeService.class);
        ObjectCode objectCode = ocs.getByPrimaryIdWithCaching(uds.getCurrentFiscalYear(), 
                transaction.getChartOfAccountsCode(), transaction.getFinancialObjectCode());
        
        if (ObjectUtils.isNull(objectCode)) {
            String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Object Code " 
                    + transaction.getFinancialObjectCode() + " is invalid; using default error Object Code.";
            
            if (LOG.isInfoEnabled()) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            transaction.setFinancialObjectCode(getErrorObjectCode());
            transaction.refresh();
        }
        
        errorText += " " + super.validateTransaction(transaction);
        
        return errorText;
        
    }
    protected String getErrorObjectCode() {
        return parameterService.getParameterValueAsString(ProcurementCardCreateDocumentsStep.class, CuFPParameterConstants.ERROR_TRANS_OBJECT_CODE_PARM_NM);
    }
    
    @Override
    public List<ProcurementCardReportType> getSortedReportSummaryList(List<ProcurementCardDocument> documents) {
        return super.getSortedReportSummaryList(documents);
    }
}
