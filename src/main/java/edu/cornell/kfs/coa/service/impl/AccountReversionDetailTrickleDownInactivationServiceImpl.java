
/*
 * Copyright 2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentHeaderService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.AccountReversionDetail;
import edu.cornell.kfs.coa.businessobject.ReversionCategory;
import edu.cornell.kfs.coa.service.AccountReversionDetailTrickleDownInactivationService;
import edu.cornell.kfs.sys.CUKFSConstants.FinancialDocumentTypeCodes;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * The default implementation of the OrganizationReversionDetailTrickleDownService
 */
public class AccountReversionDetailTrickleDownInactivationServiceImpl implements AccountReversionDetailTrickleDownInactivationService {
	private static final Logger LOG = LogManager.getLogger(AccountReversionDetailTrickleDownInactivationServiceImpl.class);
    protected NoteService noteService;
    protected ConfigurationService kualiConfigurationService;
    protected BusinessObjectService businessObjectService;
    protected DocumentHeaderService documentHeaderService;
    protected DocumentTypeService documentTypeService;
    
    /**
     * @see org.kuali.kfs.coa.service.AcciybtReversionDetailTrickleDownInactivationService#trickleDownInactiveAccountReversionDetails(org.kuali.kfs.coa.businessobject.AccountReversion, java.lang.String)
     */
    public void trickleDownInactiveAccountReversionDetails(AccountReversion accountReversion, String documentNumber) {
        accountReversion.refreshReferenceObject("accountReversionDetails");
        trickleDownInactivations(accountReversion.getAccountReversionDetails(), documentNumber);
    }

    /**
     * @see org.kuali.kfs.coa.service.OrganizationReversionDetailTrickleDownInactivationService#trickleDownInactiveAccountReversionDetails(org.kuali.kfs.coa.businessobject.ReversionCategory, java.lang.String)
     */
    public void trickleDownInactiveAccountReversionDetails(ReversionCategory reversionCategory, String documentNumber) {
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put("reversionCategoryCode", reversionCategory.getReversionCategoryCode());
        Collection acctReversionDetails = businessObjectService.findMatching(AccountReversionDetail.class, fieldValues);
        
        List<AccountReversionDetail> accountReversionDetailList = new ArrayList<AccountReversionDetail>();
        for (Object acctRevDetailAsObject : acctReversionDetails) {
        	accountReversionDetailList.add((AccountReversionDetail)acctRevDetailAsObject);
        }
        trickleDownInactivations(accountReversionDetailList, documentNumber);
    }
    
    /**
     * @see org.kuali.kfs.coa.service.AccountReversionDetailTrickleDownInactivationService#trickleDownActiveAccountReversionDetails(org.kuali.kfs.coa.businessobject.AccountReversion, java.lang.String)
     */
    public void trickleDownActiveAccountReversionDetails(AccountReversion accountReversion, String documentNumber) {
        accountReversion.refreshReferenceObject("accountReversionDetails");
        trickleDownActivations(accountReversion.getAccountReversionDetails(), documentNumber);
    }

    /**
     * @see org.kuali.kfs.coa.service.AccountReversionDetailTrickleDownInactivationService#trickleDownActiveAccountReversionDetails(org.kuali.kfs.coa.businessobject.ReversionCategory, java.lang.String)
     */
    public void trickleDownActiveAccountReversionDetails(ReversionCategory reversionCategory, String documentNumber) {
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put("reversionCategoryCode", reversionCategory.getReversionCategoryCode());
        Collection acctReversionDetails = businessObjectService.findMatching(AccountReversionDetail.class, fieldValues);
        
        List<AccountReversionDetail> acctReversionDetailList = new ArrayList<AccountReversionDetail>();
        for (Object acctRevDetailAsObject : acctReversionDetails) {
            acctReversionDetailList.add((AccountReversionDetail)acctRevDetailAsObject);
        }
        trickleDownActivations(acctReversionDetailList, documentNumber);
    }

    /**
     * The method which actually does the work of inactivating the details
     * @param organizationReversionDetails the details to inactivate
     * @param documentNumber the document number which has the inactivations as part of it
     * @return an inactivation status object which will help us save notes
     */
    protected void trickleDownInactivations(List<AccountReversionDetail> accountReversionDetails, String documentNumber) {
        TrickleDownStatus status = new TrickleDownStatus(CUKFSKeyConstants.ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_INACTIVATION, CUKFSKeyConstants.ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE);

        if (!ObjectUtils.isNull(accountReversionDetails) && !accountReversionDetails.isEmpty()) {
            for (AccountReversionDetail detail : accountReversionDetails) {
                if (detail.isActive()) {
                    detail.setActive(false);
                    try {
                        businessObjectService.save(detail);
                        status.addAccountReversionDetail(detail);
                    }
                    catch (RuntimeException re) {
                        LOG.error("Unable to trickle-down inactivate sub-account " + detail.toString(), re);
                        status.addErrorPersistingAccountReversionDetail(detail);
                    }
                }
            }
        }
        
        if (shouldAddNotesForTrickleDownDetailChanges(documentNumber)) {
            status.saveSuccesfullyChangedNotes(documentNumber);
            status.saveErrorNotes(documentNumber);
        } else {
            LOG.info("trickleDownInactivations, Skipping creation of notes for Detail changes resulting from document "
                    + documentNumber
                    + " because the updates were triggered by an Account or Account Global document.");
        }
    }
    
    /**
     * The method which actually does the work of activating the details
     * @param organizationReversionDetails the details to inactivate
     * @param documentNumber the document number which has the inactivations as part of it
     * @return an inactivation status object which will help us save notes
     */
    protected void trickleDownActivations(List<AccountReversionDetail> accountReversionDetails, String documentNumber) {
        TrickleDownStatus status = new TrickleDownStatus(CUKFSKeyConstants.ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_ACTIVATION, CUKFSKeyConstants.ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_ACTIVATION_ERROR_DURING_PERSISTENCE);
                
        if (!ObjectUtils.isNull(accountReversionDetails) && !accountReversionDetails.isEmpty()) {
            for (AccountReversionDetail detail : accountReversionDetails) {
                if (!detail.isActive() && allowActivation(detail)) {
                    detail.setActive(true);
                    try {
                        businessObjectService.save(detail);
                        status.addAccountReversionDetail(detail);
                    }
                    catch (RuntimeException re) {
                        LOG.error("Unable to trickle-down inactivate sub-account " + detail.toString(), re);
                        status.addErrorPersistingAccountReversionDetail(detail);
                    }
                }
            }
        }
        
        if (shouldAddNotesForTrickleDownDetailChanges(documentNumber)) {
            status.saveSuccesfullyChangedNotes(documentNumber);
            status.saveErrorNotes(documentNumber);
        } else {
            LOG.info("trickleDownActivations, Skipping creation of notes for Detail changes resulting from document "
                    + documentNumber
                    + " because the updates were triggered by an Account or Account Global document");
        }
    }
    
    /**
     * Determines whether the given organization reversion detail can be activated: ie, that both its owning OrganizationReversion and its related
     * OrganizationReversionCategory are both active
     * @param detail the detail to check
     * @return true if the detail can be activated, false otherwise
     */
    protected boolean allowActivation(AccountReversionDetail detail) {
        boolean result = true;
        if (!ObjectUtils.isNull(detail.getAccountReversion())) {
            result &= detail.getAccountReversion().isActive();
        }
        if (!ObjectUtils.isNull(detail.getReversionCategory())) {
            result &= detail.getReversionCategory().isActive();
        }
        return result;
    }

    protected boolean shouldAddNotesForTrickleDownDetailChanges(String documentNumber) {
        DocumentType documentType = documentTypeService.findByDocumentId(documentNumber);
        if (ObjectUtils.isNull(documentType)) {
            throw new IllegalStateException("Document type was null for document '" + documentNumber
                    + "', this should NEVER happen!");
        }
        return !StringUtils.equalsAnyIgnoreCase(documentType.getName(),
                FinancialDocumentTypeCodes.ACCOUNT, FinancialDocumentTypeCodes.ACCOUNT_GLOBAL);
    }

    /**
     * Inner class to keep track of what organization reversions were inactivated and which
     * had errors when the persisting of the inactivation was attempted
     */
    protected class TrickleDownStatus {
        private List<AccountReversionDetail> accountReversionDetails;
        private List<AccountReversionDetail> errorPersistingAccountReversionDetails;
        private String successfullyChangedAccountReversionDetailsMessageKey;
        private String erroredOutAccountReversionDetailsMessageKey;
        
        /**
         * Constructs a AccountReversionDetailTrickleDownInactivationServiceImpl
         */
        public TrickleDownStatus(String successfullyChangedAccountReversionDetailsMessageKey, String erroredOutAccountReversionDetailsMessageKey) {
            accountReversionDetails = new ArrayList<AccountReversionDetail>();
            errorPersistingAccountReversionDetails = new ArrayList<AccountReversionDetail>();
            this.successfullyChangedAccountReversionDetailsMessageKey = successfullyChangedAccountReversionDetailsMessageKey;
            this.erroredOutAccountReversionDetailsMessageKey = erroredOutAccountReversionDetailsMessageKey;
        }
        
        /**
         * Adds an organization reversion detail which had a successfully persisted activation to the message list
         * @param organizationReversionDetail the detail to add to the list
         */
        public void addAccountReversionDetail(AccountReversionDetail accountReversionDetail) {
            accountReversionDetails.add(accountReversionDetail);
        }
        
        /**
         * Adds an organization reversion detail which could not successful persist its activation to the error message list
         * @param organizationReversionDetail the detail to add to the list
         */
        public void addErrorPersistingAccountReversionDetail(AccountReversionDetail accountReversionDetail) {
            errorPersistingAccountReversionDetails.add(accountReversionDetail);
        }
        
        /**
         * @return the number of details we want per note
         */
        protected int getDetailsPerNote() {
            return 20;
        }
        
        /**
         * Builds a List of Notes out of a list of OrganizationReversionDescriptions
         * @param messageKey the key of the note text in ApplicationResources.properties
         * @param noteParent the thing to stick the note on
         * @param organizationReversionDetails the List of OrganizationReversionDetails to make notes about
         * @return a List of Notes
         */
        protected List<Note> generateNotes(String messageKey, PersistableBusinessObject noteParent, List<AccountReversionDetail> accountReversionDetails) {
            List<Note> notes = new ArrayList<Note>();
            List<String> accountReversionDetailsDescriptions = generateAccountReversionDetailsForNotes(accountReversionDetails);
            Note noteTemplate = new Note();
            for (String description : accountReversionDetailsDescriptions) {
                if (!StringUtils.isBlank(description)) {
                    notes.add(buildNote(description, messageKey, noteTemplate, noteParent));
                }
            }
            return notes;
        }
        
        /**
         * Builds a note
         * @param description a description to put into the message of the note
         * @param messageKey the key of the note text in ApplicationResources.properties
         * @param noteTemplate the template for the note
         * @param noteParent the thing to stick the note on
         * @return the built note
         */
        protected Note buildNote(String description, String messageKey, Note noteTemplate, PersistableBusinessObject noteParent) {
            Note note = null;
            try {
                final String noteTextTemplate = kualiConfigurationService.getPropertyValueAsString(messageKey);
                final String noteText = MessageFormat.format(noteTextTemplate, description);
                note = noteService.createNote(noteTemplate, noteParent, GlobalVariables.getUserSession().getPrincipalId());
                note.setNoteText(noteText);
                note.setNotePostedTimestampToCurrent();
            }
            catch (Exception e) {
                // noteService.createNote throws *Exception*???
                // weak!!
                throw new RuntimeException("Cannot create note", e);
            }
            return note;
        }
        
        /**
         * Builds organization reverion detail descriptions to populate notes
         * @param organizationReversionDetails the list of details to convert to notes
         * @return a List of notes
         */
        protected List<String> generateAccountReversionDetailsForNotes(List<AccountReversionDetail> accountReversionDetails) {
            List<String> acctRevDetailDescriptions = new ArrayList<String>();
            
            if (accountReversionDetails.size() > 0) {
                StringBuilder description = new StringBuilder();
                description.append(getAccountReversionDetailDescription(accountReversionDetails.get(0)));
                
                int count = 1;
                while (count < accountReversionDetails.size()) {
                    if (count % getDetailsPerNote() == 0) { // time for a new note
                        acctRevDetailDescriptions.add(description.toString());
                        description = new StringBuilder();
                    } else {
                        description.append(", ");
                    }
                    description.append(getAccountReversionDetailDescription(accountReversionDetails.get(count)));
                    count += 1;
                }
                
                // add the last description
                acctRevDetailDescriptions.add(description.toString());
            }
            
            return acctRevDetailDescriptions;
        }
        
        /**
         * Beautifully and eloquently describes an organization reversion detail
         * @param organizationReversionDetail the organization reversion detail to describe
         * @return the funny, heart-breaking, and ultimately inspiring resultant description
         */
        protected String getAccountReversionDetailDescription(AccountReversionDetail accountReversionDetail) {
            return accountReversionDetail.getUniversityFiscalYear() + " - "
                    + accountReversionDetail.getChartOfAccountsCode() + " - "
                    + accountReversionDetail.getAccountNumber() + " Category: " + accountReversionDetail.getAccountReversionCategoryCode();
        }
        
        /**
         * Saves notes to a document
         * @param organizationReversionDetails the details to make notes about
         * @param messageKey the message key of the text of the note
         * @param documentNumber the document number to write to
         */
        protected void saveAllNotes(List<AccountReversionDetail> accountReversionDetails, String messageKey, String documentNumber) {
            DocumentHeader noteParent = documentHeaderService.getDocumentHeaderById(documentNumber);
            List<Note> notes = generateNotes(messageKey, noteParent, accountReversionDetails);
            noteService.saveNoteList(notes);
        }
        
        /**
         * Adds all the notes about successful inactivations
         * @param documentNumber document number to save them to
         */
        public void saveSuccesfullyChangedNotes(String documentNumber) {
            saveAllNotes(accountReversionDetails, successfullyChangedAccountReversionDetailsMessageKey, documentNumber);
        }
        
        /**
         * Adds all the notes about inactivations which couldn't be saved
         * @param documentNumber the document number to save them to
         */
        public void saveErrorNotes(String documentNumber) {
            saveAllNotes(errorPersistingAccountReversionDetails, erroredOutAccountReversionDetailsMessageKey, documentNumber);
        }

        /**
         * Sets the erroredOutOrganizationReversionDetailsMessageKey attribute value.
         * @param erroredOutOrganizationReversionDetailsMessageKey The erroredOutOrganizationReversionDetailsMessageKey to set.
         */
        public void setErroredOutAccounteversionDetailsMessageKey(String erroredOutAccountReversionDetailsMessageKey) {
            this.erroredOutAccountReversionDetailsMessageKey = erroredOutAccountReversionDetailsMessageKey;
        }

        /**
         * Sets the successfullyChangedOrganizationReversionDetailsMessageKey attribute value.
         * @param successfullyChangedOrganizationReversionDetailsMessageKey The successfullyChangedOrganizationReversionDetailsMessageKey to set.
         */
        public void setSuccessfullyChangedAccountReversionDetailsMessageKey(String successfullyChangedAccountReversionDetailsMessageKey) {
            this.successfullyChangedAccountReversionDetailsMessageKey = successfullyChangedAccountReversionDetailsMessageKey;
        }
    }

    /**
     * Gets the kualiConfigurationService attribute. 
     * @return Returns the kualiConfigurationService.
     */
    public ConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }

    /**
     * Sets the kualiConfigurationService attribute value.
     * @param kualiConfigurationService The kualiConfigurationService to set.
     */
    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * Gets the noteService attribute. 
     * @return Returns the noteService.
     */
    public NoteService getNoteService() {
        return noteService;
    }

    /**
     * Sets the noteService attribute value.
     * @param noteService The noteService to set.
     */
    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Gets the businessObjectService attribute. 
     * @return Returns the businessObjectService.
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService attribute value.
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Gets the documentHeaderService attribute. 
     * @return Returns the documentHeaderService.
     */
    public DocumentHeaderService getDocumentHeaderService() {
        return documentHeaderService;
    }

    /**
     * Sets the documentHeaderService attribute value.
     * @param documentHeaderService The documentHeaderService to set.
     */
    public void setDocumentHeaderService(DocumentHeaderService documentHeaderService) {
        this.documentHeaderService = documentHeaderService;
    }

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }
}
