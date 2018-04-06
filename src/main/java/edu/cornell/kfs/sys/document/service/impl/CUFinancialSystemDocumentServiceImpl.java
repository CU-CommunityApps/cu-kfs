package edu.cornell.kfs.sys.document.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;
import org.kuali.kfs.sys.document.service.impl.FinancialSystemDocumentServiceImpl;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;

import edu.cornell.kfs.sys.document.service.CUFinancialSystemDocumentService;

public class CUFinancialSystemDocumentServiceImpl extends FinancialSystemDocumentServiceImpl
		implements CUFinancialSystemDocumentService {
    
	private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(edu.cornell.kfs.sys.document.service.impl.CUFinancialSystemDocumentServiceImpl.class);

	
	/**
	 *  new == null, old == null : no change; line deleted previously.
	 *  new == blah, old == blah : no change
	 *  new == blah, old == meh : changed
	 *  new == null, old == blah : deleted
	 *  new == blah, old == null : added
	 * @throws WorkflowException 
	 *  
	 *  
     * @see org.kuali.kfs.sys.document.service.FinancialSystemDocumentService#checkAccountingLinesForChanges(org.kuali.kfs.sys.document.AccountingDocument)
     */
    
    
    
    public void checkAccountingLinesForChanges(AccountingDocument accountingDocument) {
        
        DocumentService docService = SpringContext.getBean(DocumentService.class);
        AccountingDocument savedDoc = null;
        try {
        	savedDoc = (AccountingDocument)docService.getByDocumentHeaderId(accountingDocument.getDocumentNumber());
       
        } catch (WorkflowException we) {
        	LOG.error("Unable to retrieve document number "+ accountingDocument.getDocumentNumber()+ " to evaluate accounting line changes");
        }

        if (savedDoc == null) {
        	return;
        }
      
        if (!accountingDocument.getSourceAccountingLines().isEmpty()) {
        	Map<Integer, AccountingLine> newSourceLines = buildAccountingLineMap(accountingDocument.getSourceAccountingLines());
        	Map<Integer, AccountingLine> savedSourceLines = buildAccountingLineMap(savedDoc.getSourceAccountingLines());

        	if (newSourceLines.isEmpty())
        		return;
        	
        	
        	int maxSourceKey = findMinOrMaxKeyValue(newSourceLines, savedSourceLines, false); 
        	int minSourceKey = findMinOrMaxKeyValue(newSourceLines, savedSourceLines, true); 

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
        }
        
        if (!accountingDocument.getTargetAccountingLines().isEmpty()) {

        	Map<Integer, AccountingLine> newTargetLines = buildAccountingLineMap(accountingDocument.getTargetAccountingLines());
        	Map<Integer, AccountingLine> savedTargetLines = buildAccountingLineMap(savedDoc.getTargetAccountingLines());

        	if (newTargetLines.isEmpty())
        		return;
        	
        	int maxTargetKey = findMinOrMaxKeyValue(newTargetLines, savedTargetLines, false); 
        	int minTargetKey = findMinOrMaxKeyValue(newTargetLines, savedTargetLines, true); 

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
    }
    
	protected int findMinOrMaxKeyValue(Map<Integer, AccountingLine> newSourceLines,
			Map<Integer, AccountingLine> savedSourceLines, boolean isMinSearch) {
		int newSourceValue = findKeySetMaxOrMin(newSourceLines, isMinSearch);
		int savedSourceValue = findKeySetMaxOrMin(savedSourceLines, isMinSearch);
		int returnValue = isMinSearch ? Math.min(newSourceValue, savedSourceValue) : 
			Math.max(newSourceValue, savedSourceValue);
		return returnValue;
	}
	
	protected Integer findKeySetMaxOrMin(Map<Integer, AccountingLine> sourceLines, boolean isMinSearch) {
		if (sourceLines != null && sourceLines.keySet().size() > 0) {
			return isMinSearch ? Collections.min(sourceLines.keySet()) : Collections.max(sourceLines.keySet());
		} else {
			return 0;
		}
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
                accountingDocument.addNote(note1);
                noteText = noteText.substring(fromIndex);
            }

            Note note = documentService.createNoteFromDocument(accountingDocument, noteText);
            accountingDocument.addNote(note);
        }
        catch (Exception e) {
            LOG.error("Exception while attempting to create or add note: " + e);
        }
    }
	
	
    protected String buildLineChangedNoteText(AccountingLine newLine, AccountingLine oldLine) {
        
    	if (newLine == null && oldLine != null) {
    		return "Accounting Line deleted: "+toString(oldLine);
    	} else if (oldLine == null && newLine != null) {
    		return "Accounting Line added: "+ toString(newLine);
    	} else {
    		return "Accounting Line changed from: "+ toString(oldLine)+" to " +toString(newLine);
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
            if (sequenceNumber != null) {
            	lineMap.put(sequenceNumber, accountingLine);
            } 
        }

        return lineMap;
    }
    
    protected boolean compareTo(AccountingLine newLine, AccountingLine oldLine) {
        //no change; line deleted previously
    	if ((oldLine == null && newLine == null) ) {
    		return true;
    	}
    	//line added
    	if (oldLine == null && newLine != null) {
    		return false;
    	}
    	
    	//line deleted
    	if (oldLine != null && newLine == null) {
    		return false;
    	}
  	
        return new EqualsBuilder().append(newLine.getChartOfAccountsCode(), oldLine.getChartOfAccountsCode()).append(newLine.getAccountNumber(), oldLine.getAccountNumber()).append(newLine.getSubAccountNumber(), oldLine.getSubAccountNumber()).append(newLine.getFinancialObjectCode(), oldLine.getFinancialObjectCode()).append(newLine.getFinancialSubObjectCode(), oldLine.getFinancialSubObjectCode()).append(newLine.getProjectCode(), oldLine.getProjectCode()).append(newLine.getOrganizationReferenceId(), oldLine.getOrganizationReferenceId()).append(newLine.getAmount(), oldLine.getAmount()).append(newLine.getFinancialDocumentLineDescription(), oldLine.getFinancialDocumentLineDescription()).append(newLine.getReferenceNumber(),oldLine.getReferenceNumber()).isEquals();
    }
    
    protected String toString(AccountingLine line) {
    	StringBuilder builder = new StringBuilder();
    	builder.append('(');
    	builder.append(line.getDocumentNumber());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getSequenceNumber());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getFinancialDocumentLineTypeCode());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getPostingYear());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getAmount());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getDebitCreditCode());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getEncumbranceUpdateCode());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getFinancialDocumentLineDescription());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getChartOfAccountsCode());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getAccountNumber());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getFinancialObjectCode());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getSubAccountNumber());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getFinancialSubObjectCode());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getProjectCode());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getBalanceTypeCode());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getOrganizationReferenceId());
    	builder.append(KFSConstants.COMMA);
    	builder.append(line.getReferenceNumber());
    	builder.append(')');
    	return builder.toString();
    }

    protected void setupFYIs(Document doc, Set<Person> priorApprovers, String initiatorUserId) {
        List<AdHocRoutePerson> adHocRoutePersons = doc.getAdHocRoutePersons();
        final FinancialSystemTransactionalDocumentAuthorizerBase documentAuthorizer = getDocumentAuthorizer(doc);
        
        // Add FYI for each approver who has already approved the document
        for (Person approver : priorApprovers) {
            if (documentAuthorizer.canReceiveAdHoc(doc, approver, KewApiConstants.ACTION_REQUEST_FYI_REQ)) {
                String approverPersonUserId = approver.getPrincipalName();
                adHocRoutePersons.add(buildFyiRecipient(approverPersonUserId));
            }
        }

        // Add FYI for initiator
        adHocRoutePersons.add(buildFyiRecipient(initiatorUserId));
    }
    
    protected AdHocRoutePerson buildFyiRecipient(String userId) {
        AdHocRoutePerson adHocRoutePerson = new AdHocRoutePerson();
        adHocRoutePerson.setActionRequested(KewApiConstants.ACTION_REQUEST_FYI_REQ);
        adHocRoutePerson.setId(userId);
        return adHocRoutePerson;
    }
    
    protected FinancialSystemTransactionalDocumentAuthorizerBase getDocumentAuthorizer(Document doc) {
    	DataDictionaryService dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        final String docTypeName = dataDictionaryService.getDocumentTypeNameByClass(doc.getClass());
        Class<? extends DocumentAuthorizer> documentAuthorizerClass = (Class<? extends DocumentAuthorizer>) dataDictionaryService.getDataDictionary().getDocumentEntry(docTypeName).getDocumentAuthorizerClass();
        
        FinancialSystemTransactionalDocumentAuthorizerBase documentAuthorizer = null;
        try {
            documentAuthorizer = (FinancialSystemTransactionalDocumentAuthorizerBase)documentAuthorizerClass.newInstance();
        }
        catch (InstantiationException ie) {
            throw new RuntimeException("Could not construct document authorizer: "+documentAuthorizerClass.getName(), ie);
        }
        catch (IllegalAccessException iae) {
            throw new RuntimeException("Could not construct document authorizer: "+documentAuthorizerClass.getName(), iae);
        }
        
        return documentAuthorizer;
    }
    
}
