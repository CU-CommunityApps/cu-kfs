package edu.iu.ebs.kfs.fp.batch.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kuali.kfs.fp.businessobject.AdvanceDepositDetail;
import org.kuali.kfs.fp.document.AdvanceDepositDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.Document;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.document.attribute.DocumentAttribute;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.bo.Attachment;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.service.AttachmentService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.NoteService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.iu.ebs.kfs.fp.FinancialProcessingConstants;
import edu.iu.ebs.kfs.fp.FinancialProcessingParameterConstants;
import edu.iu.ebs.kfs.fp.batch.GenerateAdvanceDepositDocumentsStep;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeNote;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeTransaction;
import org.kuali.rice.krad.workflow.service.WorkflowDocumentService;

/**
Copyright Indiana University
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.
   
   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
public class AdvanceDepositServiceImpl extends AbstractAdvanceDepositServiceBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AdvanceDepositServiceImpl.class);
    private static final String ACH_TRANSACTION_SEQUENCE_NUMBER = "sequenceNumber" ;
    public static final String WORKFLOW_SEARCH_RESULT_KEY = "routeHeaderId";
    

    private DocumentService documentService;
    private NoteService noteService;
    private PersonService personService;
    private String attachmentsDirectory; 
    protected WorkflowDocumentService workflowDocumentService;

    public void createDocuments() {
        List<AdvanceDepositDocument> advanceDepositDocuments = new ArrayList<AdvanceDepositDocument>();
        List<AchIncomeTransaction> transactions = (List<AchIncomeTransaction>) businessObjectService.findMatchingOrderBy(AchIncomeTransaction.class, new HashMap(), ACH_TRANSACTION_SEQUENCE_NUMBER, true);

        for (AchIncomeTransaction transaction : transactions ) {
            AdvanceDepositDocument document  = createAdvanceDepositDocument(transaction);
            saveDocument(document);
            // add notes after document is saved
            createNotes(transaction, document); 
        }

    }


    private void saveDocument(AdvanceDepositDocument document) {
        try {
            documentService.saveDocument(document);
            LOG.info("Saved Advance Deposit document: "+document.getDocumentNumber());
        }
        catch (Exception e) {
            LOG.error("Error persisting document # " + document.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
            throw new RuntimeException("Error persisting document # " + document.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
        }

    }
    
    
    /**
     * This method retrieves all SAVED advance deposit documents  and routes them to the next step in the
     * routing path.
     * 
     * @return True if the routing was performed successfully.  A runtime exception will be thrown if any errors occur while routing.
     * 
     * @see org.kuali.kfs.fp.batch.service.AdvanceDepositDocumentService#routeAdvanceDepositDocuments(List)
     */
    public boolean routeAdvanceDepositDocuments() {
        List<String> documentIdList = null;
        
        try {
            documentIdList = retrieveAdvanceDepositDocumentsToRoute(KewApiConstants.ROUTE_HEADER_SAVED_CD);
        } catch (WorkflowException e1) {
            LOG.error("Error retrieving advance deposit documents for routing: " + e1.getMessage(),e1);
            throw new RuntimeException(e1.getMessage(),e1);
        } catch (RemoteException re) {
            LOG.error("Error retrieving advance deposit documents for routing: " + re.getMessage(),re);
            throw new RuntimeException(re.getMessage(),re);
        }
        
        //Collections.reverse(documentIdList);
        if ( LOG.isInfoEnabled() ) {
            LOG.info("Advance deposit to Route: "+documentIdList);
        }
        
        for (String documentId: documentIdList) {
            try {
                AdvanceDepositDocument  advanceDocument = (AdvanceDepositDocument)documentService.getByDocumentHeaderId(documentId);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("Routing advance deposit document # " + documentId + ".");
                }
                
                documentService.prepareWorkflowDocument(advanceDocument);
                
                workflowDocumentService.route(advanceDocument.getDocumentHeader().getWorkflowDocument(), "document routed by achIncome batch job", null);
                
            }
            catch (WorkflowException e) {
                LOG.error("Error routing document # " + documentId + " " + e.getMessage());
                throw new RuntimeException(e.getMessage(),e);
            }
        }

        return true;
    }
    
    /**
     * Returns a list of all initiated but not yet routed advance deposit documents, using the KualiWorkflowInfo service.
     * @return a list of advance deposit documents to route
     */
    protected List<String> retrieveAdvanceDepositDocumentsToRoute(String statusCode) throws WorkflowException, RemoteException {
        List<String> documentIds = new ArrayList<String>();
        
        List<DocumentStatus> routeStatuses = new ArrayList<DocumentStatus>();
        routeStatuses.add(DocumentStatus.fromCode(statusCode));

        Person systemUser = getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        String principalName = systemUser.getPrincipalName();
        
        
        DocumentSearchCriteria.Builder criteria = DocumentSearchCriteria.Builder.create();
        criteria.setDocumentTypeName(KFSConstants.FinancialDocumentTypeCodes.ADVANCE_DEPOSIT);
        criteria.setDocumentStatuses(routeStatuses);
        criteria.setInitiatorPrincipalName(principalName);
        

        DocumentSearchResults results = KewApiServiceLocator.getWorkflowDocumentService().documentSearch(systemUser.getPrincipalId(), criteria.build());
        
        // TODO: DOES THIS STILL WORK???
        for (DocumentSearchResult resultRow: results.getSearchResults()) {
            //for (Document document : resultRow.getDocument()) {
                //if (field.getName().equals(WORKFLOW_SEARCH_RESULT_KEY)) {
                //    documentIds.add(parseDocumentIdFromRouteDocHeader((String)field.getValue()));
               // }
               Document document = resultRow.getDocument();
               if(ObjectUtils.isNotNull(document)) {
                   documentIds.add(document.getDocumentId());
               }
            }
        //}
        
        return documentIds;
    }
    
    /**
     * Retrieves the document id out of the route document header
     * @param routeDocHeader the String representing an HTML link to the document
     * @return the document id
     */
    protected String parseDocumentIdFromRouteDocHeader(String routeDocHeader) {
        int rightBound = routeDocHeader.indexOf('>') + 1;
        int leftBound = routeDocHeader.indexOf('<', rightBound);
        return routeDocHeader.substring(rightBound, leftBound);
    }


    /**
     * Creates a AdvanceDepositDocument from the List of transactions given.
     * 
     * @param transaction List of CashReceiptDocument objects to be used for creating the document.
     * @return A AdvanceDepositDocument populated with the transactions provided.
     */
    protected AdvanceDepositDocument createAdvanceDepositDocument(AchIncomeTransaction transaction) {
        AdvanceDepositDocument advanceDepositDocument = null;

        try {
            String detailReferenceNumber = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, FinancialProcessingParameterConstants.AdvanceDepositDocument.DETAIL_REFERENCE_NUMBER);
            String detailDescription = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, FinancialProcessingParameterConstants.AdvanceDepositDocument.DETAIL_DESCRIPTION); 
            String bankCode = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, FinancialProcessingParameterConstants.AdvanceDepositDocument.BANK_CODE);
            String documentDescription = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, FinancialProcessingParameterConstants.AdvanceDepositDocument.DOCUMENT_DESCRIPTION);

            advanceDepositDocument = (AdvanceDepositDocument) SpringContext.getBean(DocumentService.class).getNewDocument(KFSConstants.FinancialDocumentTypeCodes.ADVANCE_DEPOSIT);

            advanceDepositDocument.getDocumentHeader().setDocumentDescription(documentDescription);

            advanceDepositDocument.setCampusLocationCode(FinancialProcessingConstants.ADVACE_DEPOSIT_DEFAULT_CAMPUS_CODE);
            advanceDepositDocument.setDepositDate(dateTimeService.convertToSqlDate(transaction.getBankTimestamp()));

            //create account line detail on the document from the transaction
            createSourceAccountingLine(transaction, advanceDepositDocument);

            // set the advance deposit detail on the document from the transaction
            AdvanceDepositDetail advanceDepositDetail = new AdvanceDepositDetail();
            advanceDepositDetail.setDocumentNumber(advanceDepositDocument.getDocumentNumber());
            advanceDepositDetail.setFinancialDocumentAdvanceDepositDate(dateTimeService.convertToSqlDate(transaction.getLoadTimestamp()));
            advanceDepositDetail.setFinancialDocumentAdvanceDepositAmount(transaction.getTransactionAmount());
            advanceDepositDetail.setFinancialDocumentAdvanceDepositReferenceNumber(detailReferenceNumber);
            advanceDepositDetail.setFinancialDocumentAdvanceDepositDescription(detailDescription);
            advanceDepositDetail.setFinancialDocumentBankCode(bankCode);
            Bank bank = (Bank) SpringContext.getBean(BankService.class).getByPrimaryId(bankCode);
            advanceDepositDetail.setBank(bank);


            // add advance deposit detail to advance deposit document
            advanceDepositDocument.addAdvanceDeposit(advanceDepositDetail);
            // need to wait after document is saved to add notes
            //createNotes(transaction, advanceDepositDocument);



        } catch (WorkflowException we) {
            LOG.error("Error creating advance deposit documents: " + we.getMessage(),we);
            throw new RuntimeException("Error creating advance deposit documents: " + we.getMessage(),we);
        }catch (ParseException pe) {
            LOG.error("Error creating advance deposit documents: " + pe.getMessage(),pe);
            throw new RuntimeException("Error creating advance deposit documents: " + pe.getMessage(),pe);
        }

        return advanceDepositDocument;

    }

    //add note lines to the document note
    private void createNotes(AchIncomeTransaction transaction, AdvanceDepositDocument document) {

        String attachmentFile =   attachmentsDirectory + "/" + FinancialProcessingConstants.ADVANCE_DEPOSIT_NOTE_FILE_PREFIX 
        +  document.getDocumentNumber() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(dateTimeService.getCurrentDate()) + ".txt";
        BufferedWriter os = null;

        try {
            os = new BufferedWriter(new FileWriter(attachmentFile));

            for(AchIncomeNote achIncomeNote :transaction.getNotes()) {
                os.append(achIncomeNote.getNoteText());
                os.append("\n");
            }
            os.close();

            Note note = documentService.createNoteFromDocument(document, "");

            String attachmentType = null;
            BufferedInputStream fileStream = null;
            File file = null;
            try {
                fileStream = new BufferedInputStream(new FileInputStream(attachmentFile));
                file = new File(attachmentFile);
            }catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Attachment attachment = null;
            try {
                attachment = SpringContext.getBean(AttachmentService.class).createAttachment(document.getDocumentHeader(), file.getName(),"text" , (int)file.length(), fileStream, attachmentType);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally{
                if (fileStream != null){
                    try {
                        fileStream.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            note.setAttachment(attachment);
            attachment.setNote(note);

            noteService.save(note);
        }catch (Exception e) {
            LOG.error("Error while adding notes to advance deposit documents: " + e.getMessage(),e);
            throw new RuntimeException("Error while adding notes to the document " + document.getDocumentNumber());
        }
        finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException ie) {

                }
            }
        }

    }

    private  void createSourceAccountingLine(AchIncomeTransaction transaction , AdvanceDepositDocument advanceDepositDocument) {

        String chart = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, FinancialProcessingParameterConstants.AdvanceDepositDocument.CHART);
        String objectCode = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, FinancialProcessingParameterConstants.AdvanceDepositDocument.OBJECT_CODE);
        String account = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, FinancialProcessingParameterConstants.AdvanceDepositDocument.ACCOUNT);
        SourceAccountingLine sourceAccountingLine = new SourceAccountingLine();
        sourceAccountingLine.setSequenceNumber(advanceDepositDocument.getNextSourceLineNumber());
        sourceAccountingLine.setChartOfAccountsCode(chart);
        sourceAccountingLine.setPostingYear(sourceAccountingLine.getPostingYear());
        sourceAccountingLine.setFinancialObjectCode(objectCode);
        sourceAccountingLine.setAccountNumber(account); //9323000 //6812760
        if(ObjectUtils.isNotNull(transaction.getReferenceNumber())) {
            sourceAccountingLine.setReferenceNumber(transaction.getReferenceNumber());
            sourceAccountingLine.setReferenceOriginCode(FinancialProcessingConstants.ADVACE_DEPOSIT_REFERENCE_ORIGIN_CODE);
            sourceAccountingLine.setReferenceTypeCode(FinancialProcessingConstants.ADVACE_DEPOSIT_REFEENCE_TYPE_CODE);
        }
        sourceAccountingLine.setFinancialDocumentLineDescription(transaction.getPayerName());
        sourceAccountingLine.setAmount(transaction.getTransactionAmount());
        sourceAccountingLine.setOverrideCode(AccountingLineOverride.CODE.NONE);
        sourceAccountingLine.setPostingYear(advanceDepositDocument.getPostingYear());
        sourceAccountingLine.setDocumentNumber(advanceDepositDocument.getDocumentNumber());
        List<SourceAccountingLine> sourceLines = new ArrayList<SourceAccountingLine>();
        sourceLines.add(sourceAccountingLine);
        advanceDepositDocument.setSourceAccountingLines(sourceLines);

    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * @return Returns the personService.
     */
    protected PersonService getPersonService() {
        if(personService==null)
            personService = SpringContext.getBean(PersonService.class);
        return personService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }


    public void setAttachmentsDirectory(String attachmentsDirectory) {
        this.attachmentsDirectory = attachmentsDirectory;
    }


    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }
    
    


}
