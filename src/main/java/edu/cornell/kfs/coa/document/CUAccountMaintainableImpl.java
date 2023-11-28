package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.document.AccountMaintainableImpl;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.maintenance.MaintenanceUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.AppropriationAccount;
import edu.cornell.kfs.coa.businessobject.SubFundProgram;
import edu.cornell.kfs.coa.service.AccountReversionTrickleDownInactivationService;
import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * @author kwk43
 *
 */
@SuppressWarnings("deprecation")
public class CUAccountMaintainableImpl extends AccountMaintainableImpl {

    private static final long serialVersionUID = 1L;
    private static final String SUB_FUND_GROUP_CODE = "subFundGroupCode";
    protected static final String INITIATOR_ACCOUNT_FYI_SPLIT_NODE = "InitiatorAccountFYISplit";
    
    private static final Logger LOG = LogManager.getLogger();
    
    protected transient NoteService noteService;
    
    @Override
    public void saveBusinessObject() {
        final boolean isClosingAccount = isClosingAccount();
        
        Account account = (Account) getBusinessObject();
        AccountExtendedAttribute aea = (AccountExtendedAttribute) (account.getExtension());
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
        
        HashMap<String,String> keys = new HashMap<String,String>();
        keys.put("programCode", aea.getProgramCode());
        keys.put(SUB_FUND_GROUP_CODE,aea.getSubFundGroupCode());
        SubFundProgram sfp = (SubFundProgram) bos.findByPrimaryKey(SubFundProgram.class, keys);
        aea.setSubFundProgram(sfp);
        aea.setSubFundGroupCode(account.getSubFundGroupCode());
        
        keys = new HashMap<String,String>();
        keys.put("appropriationAccountNumber", aea.getAppropriationAccountNumber());
        keys.put(SUB_FUND_GROUP_CODE,aea.getSubFundGroupCode());
        AppropriationAccount aan = (AppropriationAccount) bos.findByPrimaryKey(AppropriationAccount.class, keys);
        aea.setAppropriationAccount(aan);
        
        if (account.isClosed() && aea.getAccountClosedDate() == null) {
            aea.setAccountClosedDate(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
        } else if (!account.isClosed() && aea.getAccountClosedDate() != null) {
            aea.setAccountClosedDate(null);           
        }
        super.saveBusinessObject();
        
        // trickle down Account Reversion inactivation
        if (isClosingAccount) {
            SpringContext.getBean(AccountReversionTrickleDownInactivationService.class).trickleDownInactivateAccountReversions((Account) getBusinessObject(), getDocumentNumber());
        }
        
		// KFSPTS-3877 save xml again so that object id is persisted, this way
		// notes on the doc will be retrieved form the DB instead of xml doc
		// content and will display any future notes on account edits as well
        try {
            Document document = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(getDocumentNumber());
            Account xmlaccount = (Account) ((MaintenanceDocument) document).getNewMaintainableObject().getBusinessObject();
            if (ObjectUtils.isNull(xmlaccount.getObjectId()) &&( KFSConstants.MAINTENANCE_NEW_ACTION.equals(getMaintenanceAction()) || KFSConstants.MAINTENANCE_COPY_ACTION.equals(getMaintenanceAction()))) {
                ((MaintenanceDocument) document).getNewMaintainableObject().setBusinessObject(account);
                SpringContext.getBean(DocumentService.class).saveDocument(document);
            }
        } catch (Exception e) {
            LOG.error("Account doc not saved successfully "+ e.getMessage());
        }
    }
    

    @Override
    public void processAfterEdit(MaintenanceDocument document, Map<String,String[]> parameters) {
        System.out.println("Inside processAfterEdit");
        document.getNotes().add(getNewBoNote(CUKFSConstants.AccountCreateAndUpdateNotePrefixes.EDIT));
    }
    
    /**
     * Checks if the document should be routed to initiator for FYI when a new account was created.
     * 
     * @see org.kuali.kfs.sys.document.FinancialSystemMaintainable#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    protected boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(INITIATOR_ACCOUNT_FYI_SPLIT_NODE)) {
            return isNewAccount();
        }
        // this is not a node we recognize
        throw new UnsupportedOperationException("AccountMaintainableImpl.answerSplitNodeQuestion cannot answer split node question " + "for the node called('" + nodeName + "')");
    }

    /**
     * Checks if this is a newly created account.
     * 
     * @return true if new (NEW, COPY actions), false otherwise
     */
    private boolean isNewAccount() {
        boolean retVal = false;
        String maintAction = super.getMaintenanceAction();

        if ((maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION)) || (maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_COPY_ACTION))) {
            retVal = true;
        }

        return retVal;
    }

    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        List<MaintenanceLock> maintenanceLocks = super.generateMaintenanceLocks();

        if (isClosingAccount()) {
            maintenanceLocks.addAll(SpringContext.getBean(AccountReversionTrickleDownInactivationService.class).generateTrickleDownMaintenanceLocks((Account) getBusinessObject(), getDocumentNumber()));
        }
        return maintenanceLocks;
    }

    /**
     * Overridden to force the old maintenance object to include the relevant ICR account sections
     * if the new object has them. This is necessary to work around a section size mismatch issue
     * on certain ACCT maintenance documents.
     * 
     * @see org.kuali.kfs.kns.maintenance.MaintainableImpl#getSections(org.kuali.kfs.kns.document.MaintenanceDocument, Maintainable)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List getSections(org.kuali.kfs.kns.document.MaintenanceDocument document, Maintainable oldMaintainable) {
        // The special handling only applies to the old maintainable.
        if (this == document.getOldMaintainableObject()) {
            Account oldAccount = (Account) getDataObject();
            Account newAccount = (Account) document.getNewMaintainableObject().getDataObject();
            
            if (oldAccount.getIndirectCostRecoveryAccounts().size() < newAccount.getIndirectCostRecoveryAccounts().size()) {
                // If necessary, add ICR accounts on the old account to match the quantity on the new account.
                List sections;
                List<IndirectCostRecoveryAccount> oldIcrAccounts = oldAccount.getIndirectCostRecoveryAccounts();
                oldAccount.setIndirectCostRecoveryAccounts(new ArrayList<IndirectCostRecoveryAccount>());
                
                for (IndirectCostRecoveryAccount oldIcrAccount : oldIcrAccounts) {
                    oldAccount.getIndirectCostRecoveryAccounts().add((IndirectCostRecoveryAccount) ObjectUtils.deepCopy(oldIcrAccount));
                }
                for (int i = newAccount.getIndirectCostRecoveryAccounts().size() - oldAccount.getIndirectCostRecoveryAccounts().size() - 1; i >= 0; i--) {
                    oldAccount.getIndirectCostRecoveryAccounts().add(new IndirectCostRecoveryAccount());
                }
                
                // Generate the sections using the temporarily-overridden list.
                sections = super.getSections(document, oldMaintainable);
                oldAccount.setIndirectCostRecoveryAccounts(oldIcrAccounts);
                
                return sections;
            }
        }
        
        return super.getSections(document, oldMaintainable);
    }
    
    @Override
    public void processAfterCopy(final MaintenanceDocument document, final Map<String, String[]> parameters) {
    	document.getNotes().add(getNewBoNote(CUKFSConstants.AccountCreateAndUpdateNotePrefixes.ADD));
    	super.processAfterCopy(document, parameters);
    }
    
    @Override
    public void processAfterNew(final MaintenanceDocument document, final Map<String, String[]> requestParameters) {
    	document.getNotes().add(getNewBoNote(CUKFSConstants.AccountCreateAndUpdateNotePrefixes.ADD));
    	super.processAfterNew(document, requestParameters);
    }
    
    @Override
	public void processAfterPost(final MaintenanceDocument document, final Map<String, String[]> requestParameters) {
		super.processAfterPost(document, requestParameters);
		final String statusCode = document.getDocumentHeader().getWorkflowDocument().getStatus().getCode();
		final boolean isPreRoute = DocumentStatus.INITIATED.getCode().equals(statusCode)
				|| DocumentStatus.SAVED.getCode().equals(statusCode);

		if (isPreRoute) {
			// Search for edit note and update it to include the doc description
			for (final Note note : document.getNotes()) {
				if (note.getNoteText().startsWith(CUKFSConstants.AccountCreateAndUpdateNotePrefixes.EDIT + CUKFSConstants.ACCOUNT_NOTE_TEXT + getDocumentNumber())) {
					note.setNoteText(CUKFSConstants.AccountCreateAndUpdateNotePrefixes.EDIT + CUKFSConstants.ACCOUNT_NOTE_TEXT + getDocumentNumber() + " " + document.getDocumentHeader().getDocumentDescription());
				}
			}
		}
	}
    
    @Override
    public void doRouteStatusChange(final DocumentHeader documentHeader) {
        super.doRouteStatusChange(documentHeader);
        if (MaintenanceUtils.shouldClearCacheOnStatusChange(documentHeader)) {
            MaintenanceUtils.clearBlockingCache();
        }
    }
    
    /**
     * creates a new bo note and sets the timestamp.
     *
     * @return a newly created note
     */
    protected Note getNewBoNote(final String prefix){
        Note newBoNote = new Note();
        newBoNote.setNoteText(prefix + CUKFSConstants.ACCOUNT_NOTE_TEXT + getDocumentNumber());
        newBoNote.setNotePostedTimestampToCurrent();

        try {
            newBoNote = getNoteService().createNote(newBoNote, this.getBusinessObject(), GlobalVariables.getUserSession().getPrincipalId());
        }
        catch (Exception e) {
            throw new RuntimeException("Caught Exception While Trying To Add Note to Account", e);
        }

        return newBoNote;
    }

	public NoteService getNoteService() {
		if(noteService == null){
			this.noteService = KRADServiceLocator.getNoteService();
		}
		return noteService;
	}

	public void setNoteService(final NoteService noteService) {
		this.noteService = noteService;
	}

}
