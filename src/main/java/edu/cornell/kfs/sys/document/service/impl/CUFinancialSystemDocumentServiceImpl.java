package edu.cornell.kfs.sys.document.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.service.impl.FinancialSystemDocumentServiceImpl;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.sys.document.service.CUFinancialSystemDocumentService;

public class CUFinancialSystemDocumentServiceImpl extends FinancialSystemDocumentServiceImpl
		implements CUFinancialSystemDocumentService {
    
	private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(edu.cornell.kfs.sys.document.service.impl.CUFinancialSystemDocumentServiceImpl.class);

	
	/** 
     * @see org.kuali.kfs.sys.document.service.FinancialSystemDocumentService#checkAccountingLinesForChanges(org.kuali.kfs.sys.document.AccountingDocument)
     */
    
    
    
    public void checkAccountingLinesForChanges(AccountingDocument accountingDocument) {
        Map<String, String> pks = new HashMap<String, String>();
        pks.put("documentNumber", accountingDocument.getDocumentNumber());

        AccountingDocument savedDoc = (DisbursementVoucherDocument) businessObjectService.findByPrimaryKey(DisbursementVoucherDocument.class, pks);
        if (savedDoc == null) {
        	return;
        }
      
        List<SourceAccountingLine> newSourceAccountingLines = accountingDocument.getSourceAccountingLines();
        List<TargetAccountingLine> newTargetAccountingLines = accountingDocument.getTargetAccountingLines();

        List<SourceAccountingLine> savedSourceAccountingLines = savedDoc.getSourceAccountingLines();
        List<TargetAccountingLine> savedTargetAccountingLines = savedDoc.getTargetAccountingLines();


        if (ObjectUtils.isNotNull(newSourceAccountingLines) && ObjectUtils.isNotNull(savedSourceAccountingLines)) {
            for (SourceAccountingLine oldSal : savedSourceAccountingLines) {
                for (SourceAccountingLine newSal : newSourceAccountingLines) {
                    if (oldSal.getSequenceNumber().equals(newSal.getSequenceNumber())) {
                        if (!oldSal.equals(newSal)) {
                            writeNote(accountingDocument, newSal, oldSal);
                        }
                    }
                }
            }
        } 
        
        
       

        if (ObjectUtils.isNotNull(newTargetAccountingLines) && ObjectUtils.isNotNull(savedTargetAccountingLines)) {
            for (TargetAccountingLine oldTal : savedTargetAccountingLines) {
                for (TargetAccountingLine newTal : newTargetAccountingLines) {
                    if (oldTal.getSequenceNumber().equals(newTal.getSequenceNumber())) {
                        if (!oldTal.equals(newTal)) {
                            writeNote(accountingDocument, newTal, oldTal);
                        }
                    }
                }
            }
        } 

        

                // Send out FYIs to all previous approvers so they're aware of the changes to the address
//                try {
//                    Set<Person> priorApprovers = accountingDocument.getDocumentHeader().getWorkflowDocument().getAllPriorApprovers();
//                    String initiatorUserId = accountingDocument.getDocumentHeader().getWorkflowDocument().getRouteHeader().getInitiatorPrincipalId();
//                    Person finSysUser = SpringContext.getBean(PersonService.class).getPerson(initiatorUserId);
//                    setupFYIs(accountingDocument, priorApprovers, finSysUser.getPrincipalName());
//                }
//                catch (WorkflowException we) {
//                    LOG.error("Exception while attempting to retrieve all prior approvers from workflow: " + we);
//                }
//                catch (Exception unfe) {
//                    LOG.error("Exception while attempting to retrieve all prior approvers for a disbursement voucher: " + unfe);
//                }
    }
	
	protected void writeNote(AccountingDocument accountingDocument, AccountingLine newAccountingLine, AccountingLine oldAccountingLine) {
        
        DocumentService documentService = SpringContext.getBean(DocumentService.class);
        // Put a note on the document to record the change to the address
        try {
            String noteText = buildLineChangedNoteText(newAccountingLine, oldAccountingLine);

            int noteMaxSize = SpringContext.getBean(DataDictionaryService.class).getAttributeMaxLength("Note", "noteText");

            // Break up the note into multiple pieces if the note is too large to fit in the database field.
            while (noteText.length() > noteMaxSize) {
                int fromIndex = 0;
                fromIndex = noteText.lastIndexOf(';', noteMaxSize);

                String noteText1 = noteText.substring(0, fromIndex);
                Note note1 = documentService.createNoteFromDocument(accountingDocument, noteText1);
                documentService.addNoteToDocument(accountingDocument, note1);
                noteText = noteText.substring(fromIndex);
            }

            Note note = documentService.createNoteFromDocument(accountingDocument, noteText);
            documentService.addNoteToDocument(accountingDocument, note);
        }
        catch (Exception e) {
            LOG.error("Exception while attempting to create or add note: " + e);
        }
    }
	
	
	

    
    
    protected String buildLineChangedNoteText(AccountingLine newAccountingLine, AccountingLine oldAccountingLine) {
        
        return "Accounting Line changed from: "+ oldAccountingLine.toString()+" TO " +newAccountingLine.toString();
    }
}
