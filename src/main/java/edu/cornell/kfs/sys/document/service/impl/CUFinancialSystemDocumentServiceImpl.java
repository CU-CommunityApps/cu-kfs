package edu.cornell.kfs.sys.document.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.service.impl.FinancialSystemDocumentServiceImpl;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DocumentService;

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
      
        Map<Integer, AccountingLine> newSourceLines = buildAccountingLineMap(accountingDocument.getSourceAccountingLines());
        Map<Integer, AccountingLine> savedSourceLines = buildAccountingLineMap(savedDoc.getSourceAccountingLines());

        int maxSourceKey = Math.max(Collections.max(newSourceLines.keySet()), Collections.max(savedSourceLines.keySet())); 
        int minSourceKey = Math.min(Collections.min(newSourceLines.keySet()), Collections.min(savedSourceLines.keySet())); 
        
        for (int i = minSourceKey; i < maxSourceKey+1; i++) {
        	AccountingLine newLine = newSourceLines.get(i);
        	AccountingLine oldLine = savedSourceLines.get(i);
        	if ( !compareTo(newLine, oldLine) )  {
        		String diff = buildLineChangedNoteText(newLine, oldLine);
        		if (StringUtils.isNotBlank(diff)) {
        			writeNote(accountingDocument, diff);
        		}
        	}
        }
        
        if (!accountingDocument.getTargetAccountingLines().isEmpty()) {

        	Map<Integer, AccountingLine> newTargetLines = buildAccountingLineMap(accountingDocument.getTargetAccountingLines());
        	Map<Integer, AccountingLine> savedTargetLines = buildAccountingLineMap(savedDoc.getTargetAccountingLines());

        	int maxTargetKey = Math.max(Collections.max(newTargetLines.keySet()), Collections.max(savedTargetLines.keySet())); 
        	int minTargetKey = Math.min(Collections.min(newTargetLines.keySet()), Collections.min(savedTargetLines.keySet())); 

        	for (int i = minTargetKey; i < maxTargetKey+1; i++) {
        		AccountingLine newLine = newTargetLines.get(i);
        		AccountingLine oldLine = savedTargetLines.get(i);
        		if ( !compareTo(newLine, oldLine)) {
        			String diff = buildLineChangedNoteText(newLine, oldLine);
        			if (StringUtils.isNotBlank(diff)) {
        				writeNote(accountingDocument, diff);
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
	
	protected void writeNote(AccountingDocument accountingDocument, String noteText) {
        
        DocumentService documentService = SpringContext.getBean(DocumentService.class);
        // Put a note on the document to record the change to the address
        try {
        //    String noteText = buildLineChangedNoteText(newAccountingLine, oldAccountingLine);

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
	
	
    protected String buildLineChangedNoteText(AccountingLine newLine, AccountingLine oldLine) {
        
    	if (newLine == null && oldLine != null) {
    		return "Accounting Line deleted: "+oldLine;
    	} else if (oldLine == null && newLine != null) {
    		return "Accounting Line added: "+newLine;
    	} else {
    		return "Accounting Line changed from: "+ oldLine.toString()+" TO " +newLine.toString();
    	}
    }    
    
    /**
     * @param accountingLines
     * @return Map containing accountingLines from the given List, indexed by their sequenceNumber
     */
    protected Map buildAccountingLineMap(List<AccountingLine> accountingLines) {
        Map lineMap = new HashMap();

        for (Iterator i = accountingLines.iterator(); i.hasNext();) {
            AccountingLine accountingLine = (AccountingLine) i.next();
            Integer sequenceNumber = accountingLine.getSequenceNumber();

            lineMap.put(sequenceNumber, accountingLine);
        }

        return lineMap;
    }

    public boolean compareTo(AccountingLine newLine, AccountingLine oldLine) {
        
    	if ((oldLine == null && newLine == null) ) {
    		return true;
    	}
    	if (oldLine == null && newLine != null) {
    		return false;
    	}
//    	if (oldLine == null) {
//    		return false;
//    	} 
//    	
        return new EqualsBuilder().append(newLine.getChartOfAccountsCode(), oldLine.getChartOfAccountsCode()).append(newLine.getAccountNumber(), oldLine.getAccountNumber()).append(newLine.getSubAccountNumber(), oldLine.getSubAccountNumber()).append(newLine.getFinancialObjectCode(), oldLine.getFinancialObjectCode()).append(newLine.getFinancialSubObjectCode(), oldLine.getFinancialSubObjectCode()).append(newLine.getProjectCode(), oldLine.getProjectCode()).append(newLine.getOrganizationReferenceId(), oldLine.getOrganizationReferenceId()).append(newLine.getAmount(), oldLine.getAmount()).isEquals();
    }
    
    
}
