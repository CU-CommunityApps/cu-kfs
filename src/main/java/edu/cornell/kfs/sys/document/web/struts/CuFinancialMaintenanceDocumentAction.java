package edu.cornell.kfs.sys.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.authorization.DocumentAuthorizer;
import org.kuali.rice.kns.util.WebUtils;
import org.kuali.rice.kns.web.struts.action.KualiMaintenanceDocumentAction;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.krad.bo.Attachment;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.datadictionary.DataDictionary;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.rules.rule.event.AddNoteEvent;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADPropertyConstants;
import org.kuali.rice.krad.util.NoteType;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Custom subclass of KualiMaintenanceDocumentAction that adds some BO-level note
 * add/delete fixes from the Rice 2.5.x line. The fixes have also been tweaked
 * to only apply to specific BOs.
 */
@SuppressWarnings("deprecation")
public class CuFinancialMaintenanceDocumentAction extends KualiMaintenanceDocumentAction {

    /**
     * Overridden to include a Rice 2.5.x fix for persisting BO note additions,
     * and to delegate the fix's boolean logic to a new shouldSaveDocumentAfterNoteUpdate()
     * method so that it can be further limited based on BO class.
     * 
     * Some other cleanup has also been done to improve line lengths
     * and remove certain comments, but other than that and the changes stated above,
     * this method is the same as the one from KualiDocumentActionBase.
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#insertBONote(
     *      org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward insertBONote(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        Document document = kualiDocumentFormBase.getDocument();
        Note newNote = kualiDocumentFormBase.getNewNote();
        newNote.setNotePostedTimestampToCurrent();

        String attachmentTypeCode = null;

        FormFile attachmentFile = kualiDocumentFormBase.getAttachmentFile();
        if (attachmentFile == null) {
            GlobalVariables.getMessageMap().putError(
                    String.format("%s.%s",
                            KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
                            KRADConstants.NOTE_ATTACHMENT_FILE_PROPERTY_NAME),
                    RiceKeyConstants.ERROR_UPLOADFILE_NULL);
        }

        if (newNote.getAttachment() != null) {
            attachmentTypeCode = newNote.getAttachment().getAttachmentTypeCode();
        }

        // check authorization for adding notes
        DocumentAuthorizer documentAuthorizer = getDocumentHelperService().getDocumentAuthorizer(document);
        if (!documentAuthorizer.canAddNoteAttachment(document, attachmentTypeCode, GlobalVariables.getUserSession().getPerson())) {
            throw buildAuthorizationException("annotate", document);
        }

        // create the attachment first, so that failure-to-create-attachment can be treated as a validation failure

        Attachment attachment = null;
        if (attachmentFile != null && !StringUtils.isBlank(attachmentFile.getFileName())) {
            if (attachmentFile.getFileSize() == 0) {
                GlobalVariables.getMessageMap().putError(
                        String.format("%s.%s",
                                KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
                                KRADConstants.NOTE_ATTACHMENT_FILE_PROPERTY_NAME),
                        RiceKeyConstants.ERROR_UPLOADFILE_EMPTY,
                        attachmentFile.getFileName());
            } else {
                String attachmentType = null;
                Attachment newAttachment = kualiDocumentFormBase.getNewNote().getAttachment();
                if (newAttachment != null) {
                    attachmentType = newAttachment.getAttachmentTypeCode();
                }
                attachment = getAttachmentService().createAttachment(document.getNoteTarget(), attachmentFile.getFileName(), attachmentFile.getContentType(),
                        attachmentFile.getFileSize(), attachmentFile.getInputStream(), attachmentType);
            }
        }

        DataDictionary dataDictionary = getDataDictionaryService().getDataDictionary();
        org.kuali.rice.krad.datadictionary.DocumentEntry entry = dataDictionary.getDocumentEntry(document.getClass().getName());

        if (entry.getDisplayTopicFieldInNotes()) {
            String topicText = kualiDocumentFormBase.getNewNote().getNoteTopicText();
            if (StringUtils.isBlank(topicText)) {
                GlobalVariables.getMessageMap().putError(
                        String.format("%s.%s",
                                KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
                                KRADConstants.NOTE_TOPIC_TEXT_PROPERTY_NAME),
                        RiceKeyConstants.ERROR_REQUIRED,
                        "Note Topic (Note Topic)");
            }
        }

        // create a new note from the data passed in
        Person kualiUser = GlobalVariables.getUserSession().getPerson();
        if (kualiUser == null) {
            throw new IllegalStateException("Current UserSession has a null Person.");
        }
        Note tmpNote = getNoteService().createNote(newNote, document.getNoteTarget(), kualiUser.getPrincipalId());

        ActionForward forward = checkAndWarnAboutSensitiveData(mapping, form, request, response,
                KRADPropertyConstants.NOTE, tmpNote.getNoteText(), "insertBONote", "");
        if (forward != null) {
            return forward;
        }

        // validate the note
        boolean rulePassed = getKualiRuleService().applyRules(new AddNoteEvent(document, tmpNote));

        // if the rule evaluation passed, let's add the note
        if (rulePassed) {
            tmpNote.refresh();


            DocumentHeader documentHeader = document.getDocumentHeader();

            // associate note with object now
            document.addNote(tmpNote);

            // persist the note if the document is already saved the getObjectId check is to get around a bug with certain documents where
            // "saved" doesn't really persist, if you notice any problems with missing notes check this line
            //maintenance document BO note should only be saved into table when document is in the PROCESSED workflow status
            if (!documentHeader.getWorkflowDocument().isInitiated() && StringUtils.isNotEmpty(document.getNoteTarget().getObjectId())
                    && !(document instanceof MaintenanceDocument && NoteType.BUSINESS_OBJECT.getCode().equals(tmpNote.getNoteTypeCode()))
                    ) {
                getNoteService().save(tmpNote);
            }
            // adding the attachment after refresh gets called, since the attachment record doesn't get persisted
            // until the note does (and therefore refresh doesn't have any attachment to autoload based on the id, nor does it
            // autopopulate the id since the note hasn't been persisted yet)
            if (attachment != null) {
                tmpNote.addAttachment(attachment);
                // save again for attachment, note this is because sometimes the attachment is added first to the above then ojb tries to save
                //without the PK on the attachment I think it is safer then trying to get the sequence manually
                if (!documentHeader.getWorkflowDocument().isInitiated() && StringUtils.isNotEmpty(document.getNoteTarget().getObjectId())
                        && !(document instanceof MaintenanceDocument && NoteType.BUSINESS_OBJECT.getCode().equals(tmpNote.getNoteTypeCode()))
                        ) {
                    getNoteService().save(tmpNote);
                }
            }

            // Added some logic which saves the document after a BO note is added to the document
            if (shouldSaveDocumentAfterNoteUpdate(document, tmpNote)) {
                getDocumentService().saveDocument(document);
            }
            // reset the new note back to an empty one
            kualiDocumentFormBase.setNewNote(new Note());
        }


        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    /**
     * Overridden to include a Rice 2.5.x fix for deleting INITIATED-doc notes and persisting BO note deletions,
     * and to delegate the fix's boolean logic to a new shouldSaveDocumentAfterNoteUpdate()
     * method so that it can be further limited based on BO class.
     * 
     * Some other cleanup has also been done to remove certain comments,
     * but other than that and the changes stated above,
     * this method is the same as the one from KualiDocumentActionBase.
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#deleteBONote(
     *      org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward deleteBONote(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        Document document = kualiDocumentFormBase.getDocument();


        // ok to delete the note/attachment
        // derive the note property from the newNote on the form
        Note newNote = kualiDocumentFormBase.getNewNote();
        Note note = document.getNote(getLineToDelete(request));
        Attachment attachment = note.getAttachment();
        String attachmentTypeCode = null;
        if (attachment != null) {
            attachmentTypeCode = attachment.getAttachmentTypeCode();
        }
        String authorUniversalIdentifier = note.getAuthorUniversalIdentifier();
        if (!WebUtils.canDeleteNoteAttachment(document, attachmentTypeCode, authorUniversalIdentifier)) {
            throw buildAuthorizationException("annotate", document);
        }

        if (attachment != null) { // only do this if the note has been persisted
            //KFSMI-798 - refresh() changed to refreshNonUpdateableReferences()
            //All references for the business object Attachment are auto-update="none",
            //so refreshNonUpdateableReferences() should work the same as refresh()
            if (note.getNoteIdentifier() != null) { // KULRICE-2343 don't blow away note reference if the note wasn't persisted
                attachment.refreshNonUpdateableReferences();
            }
            getAttachmentService().deleteAttachmentContents(attachment);
        }
        // Removed the if check so it no longer checks if the document is initiated before deleting the BO's note per KULRICE- 12327
        getNoteService().deleteNote(note);
        
        document.removeNote(note);
        if (shouldSaveDocumentAfterNoteUpdate(document, note)) {
            // If this is a maintenance document and we're deleting a BO note then try to save the document so the note is removed from the content
            getDocumentService().saveDocument(document);
        }

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    /**
     * Determines whether the document should be auto-saved after successfully adding or deleting a note.
     * The default implementation runs the same boolean logic from the Rice 2.5.x version of the BO note fix,
     * and also delegates to the set CUKFSConstants.OBJECTS_WITH_IMMEDIATE_BO_LEVEL_NOTE_UPDATE
     * to determine whether the maintained business object allows for such BO-level note action.
     * 
     * @param document The document that the note is being added to or deleted from.
     * @param updatedNote The newly-updated note.
     * @return true if the document is not in INITIATED status, the document is a maintenance document, the note is of "BO" type,
     *         and the maintained business object class exists in a specific set of allowable immediate-note-update BO classes; false otherwise.
     */
    protected boolean shouldSaveDocumentAfterNoteUpdate(Document document, Note newNote) {
        return !document.getDocumentHeader().getWorkflowDocument().isInitiated()
                && document instanceof MaintenanceDocument
                && NoteType.BUSINESS_OBJECT.getCode().equals(newNote.getNoteTypeCode())
                && CUKFSConstants.OBJECTS_WITH_IMMEDIATE_BO_LEVEL_NOTE_UPDATE.contains(
                        ((MaintenanceDocument) document).getNewMaintainableObject().getDataObjectClass().getName());
    }
}
