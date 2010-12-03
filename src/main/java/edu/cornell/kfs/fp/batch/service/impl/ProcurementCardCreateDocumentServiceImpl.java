package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.KFSConstants.FinancialDocumentTypeCodes.PROCUREMENT_CARD;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kns.bo.DocumentHeader;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;


public class ProcurementCardCreateDocumentServiceImpl extends org.kuali.kfs.fp.batch.service.impl.ProcurementCardCreateDocumentServiceImpl {

	
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardCreateDocumentServiceImpl.class);
    private DataDictionaryService dataDictionaryService;

	
    /**
     * Creates a ProcurementCardDocument from the List of transactions given.
     * 
     * @param transactions List of ProcurementCardTransaction objects to be used for creating the document.
     * @return A ProcurementCardDocument populated with the transactions provided.
     */
    @Override
    protected ProcurementCardDocument createProcurementCardDocument(List transactions) {
        ProcurementCardDocument pcardDocument = null;

        dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        
        try {
            // get new document from doc service
            pcardDocument = (ProcurementCardDocument) SpringContext.getBean(DocumentService.class).getNewDocument(PROCUREMENT_CARD);
            if (ObjectUtils.isNotNull(pcardDocument.getCapitalAssetInformation())) {
                pcardDocument.getCapitalAssetInformation().setDocumentNumber(pcardDocument.getDocumentNumber());
            }

            // set the card holder record on the document from the first transaction
            createCardHolderRecord(pcardDocument, (ProcurementCardTransaction) transactions.get(0));

            // for each transaction, create transaction detail object and then acct lines for the detail
            int transactionLineNumber = 1;
            KualiDecimal documentTotalAmount = KualiDecimal.ZERO;
            String errorText = "";
            ProcurementCardTransaction transaction = null;
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                /*ProcurementCardTransaction*/ transaction = (ProcurementCardTransaction) iter.next();
                
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

            if (cardHolderName.length() > 15 && vendorName.length() > 19) {
            	cardHolderName = cardHolderName.substring(0, 15);
            	vendorName = vendorName.substring(0, 19);
            }         	
            if (cardHolderName.length() > 15 && vendorName.length() <= 19) {
            	Integer endIndice = 0;
            	if ( (15+(19-vendorName.length())) > cardHolderName.length() ) {
            		endIndice = cardHolderName.length();
            	} else {
            		endIndice = (15 + ( 19 - vendorName.length()));
            	}
            	cardHolderName = cardHolderName.substring(0, endIndice);
            }
            if (vendorName.length() > 19 && cardHolderName.length() <= 15) {
            	Integer endIndice = 0;
            	if ( (19+(15-cardHolderName.length())) > vendorName.length() ) {
            		endIndice = vendorName.length();
            	} else {
            		endIndice = (19 + ( 15 - cardHolderName.length()));
            	}
            	vendorName = vendorName.substring(0, endIndice);
            }
            
            String creditCardNumber = transaction.getTransactionCreditCardNumber();
            String lastFour = "";

            if (creditCardNumber.length()>4) {
            	lastFour = creditCardNumber.substring(creditCardNumber.length()-4);
            }
            String docDesc = cardHolderName + "/" + vendorName + "/" + lastFour;
 
            if (docDesc.length() > 40) {
            	docDesc = docDesc.substring(0, 40);
            }
            pcardDocument.getDocumentHeader().setDocumentDescription(docDesc);
            
            // Remove duplicate messages from errorText
            String messages[] = StringUtils.split(errorText, ".");
            for (int i = 0; i < messages.length; i++) {
                int countMatches = StringUtils.countMatches(errorText, messages[i]) - 1;
                errorText = StringUtils.replace(errorText, messages[i] + ".", "", countMatches);
            }
            // In case errorText is still too long, truncate it and indicate so.
            Integer documentExplanationMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(), KFSPropertyConstants.EXPLANATION);
            if (documentExplanationMaxLength != null && errorText.length() > documentExplanationMaxLength.intValue()) {
                String truncatedMessage = " ... TRUNCATED.";
                errorText = errorText.substring(0, documentExplanationMaxLength - truncatedMessage.length()) + truncatedMessage;
            }
            pcardDocument.getDocumentHeader().setExplanation(errorText);
        }
        catch (WorkflowException e) {
            LOG.error("Error creating pcdo documents: " + e.getMessage(),e);
            throw new RuntimeException("Error creating pcdo documents: " + e.getMessage(),e);
        }

        return pcardDocument;
    }

	
}
