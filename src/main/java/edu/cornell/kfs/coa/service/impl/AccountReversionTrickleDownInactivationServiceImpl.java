package edu.cornell.kfs.coa.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.datadictionary.legacy.MaintenanceDocumentDictionaryService;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.dao.MaintenanceDocumentDao;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.DocumentHeaderService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.service.AccountReversionService;
import edu.cornell.kfs.coa.service.AccountReversionTrickleDownInactivationService;
import edu.cornell.kfs.sys.CUKFSConstants.FinancialDocumentTypeCodes;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

@Transactional
public class AccountReversionTrickleDownInactivationServiceImpl implements AccountReversionTrickleDownInactivationService {
    
	private static final Logger LOG = LogManager.getLogger(AccountReversionTrickleDownInactivationServiceImpl.class);

    protected AccountReversionService accountReversionService;
    protected MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService;
    protected MaintenanceDocumentDao maintenanceDocumentDao;
    protected NoteService noteService;
    protected ConfigurationService kualiConfigurationService;
    protected DocumentHeaderService documentHeaderService;
    protected UniversityDateService universityDateService;
    protected DocumentTypeService documentTypeService;
    
    /**
     * Will generate Maintenance Locks for all (active or not) AccountReversions in the system related to the inactivated account using the AccountReversion
     * maintainable registered for the AccountReversion maintenance document
     * 
     * @see edu.cornell.kfs.coa.service.AccountReversionTrickleDownInactivationService#generateTrickleDownMaintenanceLocks(org.kuali.kfs.coa.businessobject.Account, java.lang.String)
     */
    public List<MaintenanceLock> generateTrickleDownMaintenanceLocks(Account inactivatedAccount, String documentNumber) {
        List<MaintenanceLock> maintenanceLocks = new ArrayList<MaintenanceLock>();
        
        Maintainable accountReversionMaintainable;
        try {
            accountReversionMaintainable = (Maintainable) maintenanceDocumentDictionaryService.getMaintainableClass(AccountReversion.class.getName()).newInstance();
            accountReversionMaintainable.setDataObjectClass(AccountReversion.class);
            accountReversionMaintainable.setDocumentNumber(documentNumber);
            accountReversionMaintainable.setMaintenanceAction(KRADConstants.MAINTENANCE_EDIT_ACTION);
        }
        catch (Exception e) {
            LOG.error("Unable to instantiate Account Reversion Maintainable" , e);
            throw new RuntimeException("Unable to instantiate Account Reversion Maintainable" , e);
        }
        
        List<AccountReversion> accountReversionRules = new ArrayList<AccountReversion>();
        List<AccountReversion> matchingAccountReversionRules = accountReversionService.getAccountReversionsByChartAndAccount(inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        if(ObjectUtils.isNotNull(matchingAccountReversionRules) && CollectionUtils.isNotEmpty(matchingAccountReversionRules)){
            accountReversionRules.addAll(matchingAccountReversionRules);
        }
        
        List<AccountReversion> cashAccountReversionRules = accountReversionService.getAccountReversionsByCashReversionAcount(inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        
        if(ObjectUtils.isNotNull(cashAccountReversionRules) && cashAccountReversionRules.size() > 0){
            accountReversionRules.addAll(cashAccountReversionRules);
        }
        
        List<AccountReversion> budgetAccountReversionRules = accountReversionService.getAccountReversionsByBudgetReversionAcount(inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        
        if(ObjectUtils.isNotNull(budgetAccountReversionRules) && budgetAccountReversionRules.size() > 0){
            accountReversionRules.addAll(budgetAccountReversionRules);
        }
        
        if (ObjectUtils.isNotNull(accountReversionRules) && !accountReversionRules.isEmpty()) {
            for (AccountReversion accountReversion : accountReversionRules) {         
                accountReversionMaintainable.setBusinessObject(accountReversion);
                maintenanceLocks.addAll(accountReversionMaintainable.generateMaintenanceLocks());
            }
        }
        return maintenanceLocks;
    }
    
    /**
     * Inactivates all related AccountReversion rules.
     * 
     * @see edu.cornell.kfs.coa.service.AccountReversionTrickleDownInactivationService#trickleDownInactivateAccountReversions(org.kuali.kfs.coa.businessobject.Account, java.lang.String)
     */
    public void trickleDownInactivateAccountReversions(Account inactivatedAccount, String documentNumber) {
        List<AccountReversion> inactivatedAccountReversions = new ArrayList<AccountReversion>();
        Map<AccountReversion, String> alreadyLockedAccountReversions = new HashMap<AccountReversion, String>();
        List<AccountReversion> errorPersistingAccountReversions = new ArrayList<AccountReversion>();
        
        Maintainable accountReversionMaintainable;
        try {
            accountReversionMaintainable = (Maintainable) maintenanceDocumentDictionaryService.getMaintainableClass(AccountReversion.class.getName()).newInstance();
            accountReversionMaintainable.setDataObjectClass(AccountReversion.class);
            accountReversionMaintainable.setDocumentNumber(documentNumber);
            accountReversionMaintainable.setMaintenanceAction(KRADConstants.MAINTENANCE_EDIT_ACTION);
        }
        catch (Exception e) {
            LOG.error("Unable to instantiate accountReversionMaintainable Maintainable" , e);
            throw new RuntimeException("Unable to instantiate accountReversionMaintainable Maintainable" , e);
        }
        

        Integer universityFiscalYear = universityDateService.getCurrentFiscalYear() - 1;
        List<AccountReversion> accountReversionRules = new ArrayList<AccountReversion>();
        List<AccountReversion> matchingAccountReversionRules = accountReversionService.getAccountReversionsByChartAndAccount(inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        if(ObjectUtils.isNotNull(matchingAccountReversionRules) && CollectionUtils.isNotEmpty(matchingAccountReversionRules)){
            accountReversionRules.addAll(matchingAccountReversionRules);
        }
        
        List<AccountReversion> cashAccountReversionRules = accountReversionService.getAccountReversionsByCashReversionAcount(inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        
        if(ObjectUtils.isNotNull(cashAccountReversionRules) && cashAccountReversionRules.size() > 0){
            accountReversionRules.addAll(cashAccountReversionRules);
        }
        
        List<AccountReversion> budgetAccountReversionRules = accountReversionService.getAccountReversionsByBudgetReversionAcount(inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        
        if(ObjectUtils.isNotNull(budgetAccountReversionRules) && budgetAccountReversionRules.size() > 0){
            accountReversionRules.addAll(budgetAccountReversionRules);
        }
        
        /*
         * The code above retrieves Account Reversions from the database via OJB, causing OJB to cache them locally.
         * The code below changes the active flag on each appropriate Account Reversion, but the subsequent code in
         * AccountReversionMaintainableImpl needs to re-retrieve the Reversion from the DB *before* saving the update.
         * The line below is needed to clear the OJB cache, so that the re-retrieval code does not accidentally
         * return the unsaved locally-changed object from OJB's cache.
         */
        accountReversionService.forciblyClearCache();
        
        if (ObjectUtils.isNotNull(accountReversionRules) && !accountReversionRules.isEmpty()) {
            for (AccountReversion accountReversion : accountReversionRules ) {
               
                if (accountReversion.isActive()) {
                    accountReversionMaintainable.setBusinessObject(accountReversion);
                    List<MaintenanceLock> accountReversionLocks = accountReversionMaintainable.generateMaintenanceLocks();
                    
                    MaintenanceLock failedLock = verifyAllLocksFromThisDocument(accountReversionLocks, documentNumber);
                    if (failedLock != null) {
                        // another document has locked this AccountReversion, so we don't try to inactivate the account
                        alreadyLockedAccountReversions.put(accountReversion, failedLock.getDocumentNumber());
                    }
                    else {
                        // no locks other than our own (but there may have been no locks at all), just go ahead and try to update
                        accountReversion.setActive(false);
                        
                        try {
                            accountReversionMaintainable.saveBusinessObject();
                            inactivatedAccountReversions.add(accountReversion);
                        }
                        catch (RuntimeException e) {
                            LOG.error("Unable to trickle-down inactivate accountReversion " + accountReversion.toString(), e);
                            errorPersistingAccountReversions.add(accountReversion);
                        }
                    }
                }
            }
            
            addNotesToDocument(documentNumber, inactivatedAccount, inactivatedAccountReversions, alreadyLockedAccountReversions, errorPersistingAccountReversions);
        }
    }

    /**
     * Adds notes about inactivated AccountReversions, any errors while persisting inactivated account reversions or account reversions that were loccked.
     * 
     * @param documentNumber
     * @param inactivatedAccount
     * @param inactivatedAccountReversions
     * @param alreadyLockedAccountReversions
     * @param errorPersistingAccountReversions
     */
    protected void addNotesToDocument(String documentNumber, Account inactivatedAccount, List<AccountReversion> inactivatedAccountReversions,
            Map<AccountReversion, String> alreadyLockedAccountReversions, List<AccountReversion> errorPersistingAccountReversions) {
        if (inactivatedAccountReversions.isEmpty() && alreadyLockedAccountReversions.isEmpty() && errorPersistingAccountReversions.isEmpty()) {
            // if we didn't try to inactivate any AccountReversions, then don't bother
            return;
        }
        PersistableBusinessObject noteParent = getNoteTargetForTrickleDownInactivations(inactivatedAccount, documentNumber);
        Note newNote = new Note();
        
        addNotes(documentNumber, inactivatedAccountReversions, CUKFSKeyConstants.ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION, noteParent, newNote);
        addNotes(documentNumber, errorPersistingAccountReversions, CUKFSKeyConstants.ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE, noteParent, newNote);
        addMaintenanceLockedNotes(documentNumber, alreadyLockedAccountReversions, CUKFSKeyConstants.ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION_RECORD_ALREADY_MAINTENANCE_LOCKED, noteParent, newNote);
    }

    protected PersistableBusinessObject getNoteTargetForTrickleDownInactivations(
            Account inactivatedAccount, String documentNumber) {
        DocumentType documentType = documentTypeService.findByDocumentId(documentNumber);
        if (ObjectUtils.isNull(documentType)) {
            throw new IllegalStateException("Document type was null for document '" + documentNumber
                    + "', this should NEVER happen!");
        }
        
        if (StringUtils.equalsAnyIgnoreCase(documentType.getName(),
                FinancialDocumentTypeCodes.ACCOUNT, FinancialDocumentTypeCodes.ACCOUNT_GLOBAL)) {
            return inactivatedAccount;
        } else {
            return documentHeaderService.getDocumentHeaderById(documentNumber);
        }
    }

    /**
     * Adds notes about any maintence locks on Account Reversions.
     * 
     * @param documentNumber
     * @param lockedAccountReversions
     * @param messageKey
     * @param noteParent
     * @param noteTemplate
     */
    protected void addMaintenanceLockedNotes(String documentNumber, Map<AccountReversion, String> lockedAccountReversions, String messageKey, PersistableBusinessObject noteParent, Note noteTemplate) {
        for (Map.Entry<AccountReversion, String> entry : lockedAccountReversions.entrySet()) {
            try {
                AccountReversion accountReversion = entry.getKey();
                String accountReversionString = accountReversion.getUniversityFiscalYear() + " - " + accountReversion.getChartOfAccountsCode() + " - " + accountReversion.getAccountNumber();
                if (StringUtils.isNotBlank(accountReversionString)) {
                    String noteTextTemplate = kualiConfigurationService.getPropertyValueAsString(messageKey);
                    String noteText = MessageFormat.format(noteTextTemplate, accountReversionString, entry.getValue());
                    Note note = noteService.createNote(noteTemplate, noteParent, GlobalVariables.getUserSession().getPrincipalId());
                    note.setNoteText(noteText);
                    noteService.save(note);
                }
            }
            catch (Exception e) {
                LOG.error("Unable to create/save notes for document " + documentNumber, e);
                throw new RuntimeException("Unable to create/save notes for document " + documentNumber, e);
            }
        }
    }

    /**
     * Adds notes for the given account reversions.
     * 
     * @param documentNumber
     * @param listOfAccountReversions
     * @param messageKey
     * @param noteParent
     * @param noteTemplate
     */
    protected void addNotes(String documentNumber, List<AccountReversion> listOfAccountReversions, String messageKey, PersistableBusinessObject noteParent, Note noteTemplate) {
        for (int i = 0; i < listOfAccountReversions.size(); i += getNumAccountReversionsPerNote()) {
            try {
                String accountReversionString = createAccountReversionChunk(listOfAccountReversions, i, i + getNumAccountReversionsPerNote());
                if (StringUtils.isNotBlank(accountReversionString)) {
                    String noteTextTemplate = kualiConfigurationService.getPropertyValueAsString(messageKey);
                    String noteText = MessageFormat.format(noteTextTemplate, accountReversionString);
                    Note note = noteService.createNote(noteTemplate, noteParent, GlobalVariables.getUserSession().getPrincipalId());
                    note.setNoteText(noteText);
                    note.setNotePostedTimestampToCurrent();
                    noteService.save(note);
                }
            }
            catch (Exception e) {
                LOG.error("Unable to create/save notes for document " + documentNumber, e);
                throw new RuntimeException("Unable to create/save notes for document " + documentNumber, e);
            }
        }
    }
    
    /**
     * Creates a String for the given account reversions.
     * 
     * @param listOfAccountReversions
     * @param startIndex
     * @param endIndex
     * @return
     */
    protected String createAccountReversionChunk(List<AccountReversion> listOfAccountReversions, int startIndex, int endIndex) {
        StringBuilder buf = new StringBuilder(); 
        for (int i = startIndex; i < endIndex && i < listOfAccountReversions.size(); i++) {
            AccountReversion accountReversion = listOfAccountReversions.get(i);
            buf.append(accountReversion.getUniversityFiscalYear()).append(" - ").append(accountReversion.getChartOfAccountsCode()).append(" - ")
                    .append(accountReversion.getAccountNumber());
            if (i + 1 < endIndex && i + 1 < listOfAccountReversions.size()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }
    
    /**
     * Returns the number of account reversions to be included in one note.
     * 
     * @return
     */
    protected int getNumAccountReversionsPerNote() {
        return 20;
    }
    
    /**
     * Verify if a lock exists.
     * 
     * @param maintenanceLocks
     * @param documentNumber
     * @return the maintenance lock
     */
    protected MaintenanceLock verifyAllLocksFromThisDocument(List<MaintenanceLock> maintenanceLocks, String documentNumber) {
        for (MaintenanceLock maintenanceLock : maintenanceLocks) {
            String lockingDocNumber = maintenanceDocumentDao.getLockingDocumentNumber(maintenanceLock.getLockingRepresentation(), documentNumber);
            if (StringUtils.isNotBlank(lockingDocNumber)) {
                return maintenanceLock;
            }
        }
        return null;
    }

    /**
     * Sets the maintenanceDocumentDictionaryService.
     * 
     * @param maintenanceDocumentDictionaryService
     */
    public void setMaintenanceDocumentDictionaryService(MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService) {
        this.maintenanceDocumentDictionaryService = maintenanceDocumentDictionaryService;
    }

    /**
     * Sets the maintenanceDocumentDao.
     * 
     * @param maintenanceDocumentDao
     */
    public void setMaintenanceDocumentDao(MaintenanceDocumentDao maintenanceDocumentDao) {
        this.maintenanceDocumentDao = maintenanceDocumentDao;
    }

    /**
     * Sets the noteService.
     * 
     * @param noteService
     */
    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Sets the kualiConfigurationService.
     * 
     * @param kualiConfigurationService
     */
    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * Sets the documentHeaderService.
     * 
     * @param documentHeaderService
     */
    public void setDocumentHeaderService(DocumentHeaderService documentHeaderService) {
        this.documentHeaderService = documentHeaderService;
    }

    /**
     * Gets the universityDateService.
     * 
     * @return universityDateService
     */
    public UniversityDateService getUniversityDateService() {
        return universityDateService;
    }

    /**
     * Sets the universityDateService.
     * 
     * @param universityDateService
     */
    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

	/**
	 * Gets the accountReversionService.
	 * @return accountReversionService
	 */
	public AccountReversionService getAccountReversionService() {
		return accountReversionService;
	}

	/**
	 * Sets the accountReversionService.
	 * 
	 * @param accountReversionService
	 */
	public void setAccountReversionService(AccountReversionService accountReversionService) {
		this.accountReversionService = accountReversionService;
	}

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

}
