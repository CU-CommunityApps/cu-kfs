package edu.cornell.kfs.fp.batch.service.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionExtendedAttribute;
import edu.cornell.kfs.fp.document.CorporateBilledCorporatePaidDocument;

public class CorporateBilledCorporatePaidCreateDocumentServiceImpl extends ProcurementCardCreateDocumentServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidCreateDocumentServiceImpl.class);
    
    @Override
    protected CorporateBilledCorporatePaidDocument createProcurementCardDocument(List transactions) {
        CorporateBilledCorporatePaidDocument cbcpDocument = null;

        dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        
        try {
            // get new document from doc service
            cbcpDocument = (CorporateBilledCorporatePaidDocument) SpringContext.getBean(DocumentService.class).getNewDocument(CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_DOCUMENT_TYPE_CODE);
            
            List<CapitalAssetInformation> capitalAssets = cbcpDocument.getCapitalAssetInformation();
            for (CapitalAssetInformation capitalAsset : capitalAssets) {
                if (ObjectUtils.isNotNull(capitalAsset) && ObjectUtils.isNotNull(capitalAsset.getCapitalAssetInformationDetails())) {
                    capitalAsset.setDocumentNumber(cbcpDocument.getDocumentNumber());
                }
            }

            ProcurementCardTransaction trans = (ProcurementCardTransaction) transactions.get(0);
            String errorText = validateTransaction(trans);
            createCardHolderRecord(cbcpDocument, trans);

            // for each transaction, create transaction detail object and then acct lines for the detail
            int transactionLineNumber = 1;
            KualiDecimal documentTotalAmount = KualiDecimal.ZERO;
            ProcurementCardTransaction transaction = null;
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                /*ProcurementCardTransaction*/ transaction = (ProcurementCardTransaction) iter.next();
                
                // create transaction detail record with accounting lines
                errorText += createTransactionDetailRecord(cbcpDocument, transaction, transactionLineNumber);

                // update document total
                documentTotalAmount = documentTotalAmount.add(transaction.getFinancialDocumentTotalAmount());

                transactionLineNumber++;
            }
            
            cbcpDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(documentTotalAmount);
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
            cbcpDocument.getDocumentHeader().setDocumentDescription(docDesc);
            
            // Remove duplicate messages from errorText
            String[] messages = StringUtils.split(errorText, ".");
            for (int i = 0; i < messages.length; i++) {
                int countMatches = StringUtils.countMatches(errorText, messages[i]) - 1;
                errorText = StringUtils.replace(errorText, messages[i] + ".", "", countMatches);
            }
            // In case errorText is still too long, truncate it and indicate so.
            Integer documentExplanationMaxLength = dataDictionaryService
                    .getAttributeMaxLength(DocumentHeader.class.getName(), KFSPropertyConstants.EXPLANATION);
            if (documentExplanationMaxLength != null && errorText.length() > documentExplanationMaxLength.intValue()) {
                String truncatedMessage = " ... TRUNCATED.";
                errorText = errorText.substring(0, documentExplanationMaxLength - truncatedMessage.length()) + truncatedMessage;
            }
            cbcpDocument.getDocumentHeader().setExplanation(errorText);
        } catch (WorkflowException e) {
            LOG.error("Error creating CBCP documents: " + e.getMessage(),e);
            throw new RuntimeException("Error creating CBCP documents: " + e.getMessage(),e);
        }

        return cbcpDocument;
    }

}
