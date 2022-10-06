package edu.cornell.kfs.sys.document.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;
import org.kuali.kfs.sys.document.service.impl.FinancialSystemDocumentServiceImpl;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.document.service.CUFinancialSystemDocumentService;

public class CUFinancialSystemDocumentServiceImpl extends FinancialSystemDocumentServiceImpl
		implements CUFinancialSystemDocumentService {
	
	protected ParameterService parameterService;
	private static final int DEFAULT_FETCH_MORE_ITERATION_LIMIT = 10;
    
    private static final Logger LOG = LogManager.getLogger();
	
	/**
	 *  new == null, old == null : no change; line deleted previously.
	 *  new == blah, old == blah : no change
	 *  new == blah, old == meh : changed
	 *  new == null, old == blah : deleted
	 *  new == blah, old == null : added
	 *  
	 *  
     * @see org.kuali.kfs.sys.document.service.FinancialSystemDocumentService#checkAccountingLinesForChanges(org.kuali.kfs.sys.document.AccountingDocument)
     */
    
    
    
    public void checkAccountingLinesForChanges(AccountingDocument accountingDocument) {
        
        DocumentService docService = SpringContext.getBean(DocumentService.class);
        AccountingDocument savedDoc = null;
        savedDoc = (AccountingDocument)docService.getByDocumentHeaderId(accountingDocument.getDocumentNumber());

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
        DocumentDictionaryService documentDictionaryService = SpringContext.getBean(DocumentDictionaryService.class);
        Class<? extends DocumentAuthorizer> documentAuthorizerClass = (Class<? extends DocumentAuthorizer>) documentDictionaryService.getDocumentEntryByClass(doc.getClass()).getDocumentAuthorizerClass();
        
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
    
    /**
     * Returns the maximum number of results that should be returned from the document search.
     *
     * @param criteria the criteria in which to check for a max results value
     * @return the maximum number of results that should be returned from a document search
     */
    public int getMaxResultCap(DocumentSearchCriteria criteria) {
        int systemLimit = KewApiConstants.DOCUMENT_LOOKUP_DEFAULT_RESULT_CAP;
        String resultCapValue = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.WORKFLOW,
                KRADConstants.DetailTypes.DOCUMENT_SEARCH_DETAIL_TYPE, KewApiConstants.DOC_SEARCH_RESULT_CAP);
        if (StringUtils.isNotBlank(resultCapValue)) {
            try {
                int configuredLimit = Integer.parseInt(resultCapValue);
                if (configuredLimit <= 0) {
                    LOG.warn(KewApiConstants.DOC_SEARCH_RESULT_CAP + " was less than or equal to zero.  Please " +
                            "use a positive integer.");
                } else {
                    systemLimit = configuredLimit;
                }
            } catch (NumberFormatException e) {
                LOG.warn(KewApiConstants.DOC_SEARCH_RESULT_CAP + " is not a valid number.  Value was " +
                        resultCapValue + ".  Using default: " + KewApiConstants.DOCUMENT_LOOKUP_DEFAULT_RESULT_CAP);
            }
        }
        int maxResults = systemLimit;
        if (criteria.getMaxResults() != null) {
            int criteriaLimit = criteria.getMaxResults();
            if (criteriaLimit > systemLimit) {
                LOG.warn("Result set cap of " + criteriaLimit + " is greater than system value of " + systemLimit);
            } else {
                if (criteriaLimit < 0) {
                    LOG.warn("Criteria results limit was less than zero.");
                    criteriaLimit = 0;
                }
                maxResults = criteriaLimit;
            }
        }
        return maxResults;
    }

    public int getFetchMoreIterationLimit() {
        int fetchMoreLimit = DEFAULT_FETCH_MORE_ITERATION_LIMIT;
        String fetchMoreLimitValue = CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.WORKFLOW, KRADConstants.DetailTypes.DOCUMENT_SEARCH_DETAIL_TYPE,
                KewApiConstants.DOC_SEARCH_FETCH_MORE_ITERATION_LIMIT);
        if (StringUtils.isNotBlank(fetchMoreLimitValue)) {
            try {
                fetchMoreLimit = Integer.parseInt(fetchMoreLimitValue);
                if (fetchMoreLimit < 0) {
                    LOG.warn(KewApiConstants.DOC_SEARCH_FETCH_MORE_ITERATION_LIMIT + " was less than zero.  " +
                            "Please use a value greater than or equal to zero.");
                    fetchMoreLimit = DEFAULT_FETCH_MORE_ITERATION_LIMIT;
                }
            } catch (NumberFormatException e) {
                LOG.warn(KewApiConstants.DOC_SEARCH_FETCH_MORE_ITERATION_LIMIT + " is not a valid number.  " +
                        "Value was " + fetchMoreLimitValue);
            }
        }
        return fetchMoreLimit;
    }
    
    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
}
