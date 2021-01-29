package edu.cornell.kfs.sys.util;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.upload.FormFile;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.sys.CUKFSConstants.ConfidentialAttachmentTypeCodes;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

/**
 * Utility class containing various helper methods for handling
 * the "Confidential" attachment type. 
 */
@SuppressWarnings("deprecation")
public final class ConfidentialAttachmentUtil {

    private ConfidentialAttachmentUtil() {
        throw new UnsupportedOperationException("do not call");
    }

    /**
     * Helper method for verifying that the attachment is either a non-confidential one
     * that does not potentially contain confidential data, or a confidential attachment
     * that the user is authorized to add. The message map will be updated accordingly
     * if such validation fails, with the assumption that the page is using the
     * standard note/attachment input fields.
     * 
     * @param note The note being added.
     * @param document The document that the note is being added to.
     * @param attachmentFile The file that is being attached; may be null.
     * @param documentAuthorizer The document's authorizer.
     * @return True if a non-confidential attachment without confidential naming, or a confidential one and the user is authorized to add it; false otherwise.
     * @throws Exception if an unexpected error occurs or if one of the confidential-attachment text patterns is not a valid regex.
     */
    public static boolean attachmentIsNonConfidentialOrCanAddConfAttachment(Note note, Document document,
            FormFile attachmentFile, DocumentAuthorizer documentAuthorizer) throws Exception {
        boolean valid = true;
        
        // If a "Confidential" attachment, make sure the user is authorized to add it.
        if (note.getAttachment() != null
                && ConfidentialAttachmentTypeCodes.CONFIDENTIAL_ATTACHMENT_TYPE.equals(note.getAttachment().getAttachmentTypeCode())
                && !documentAuthorizer.canAddNoteAttachment(
                        document, note.getAttachment().getAttachmentTypeCode(), GlobalVariables.getUserSession().getPerson())) {
            GlobalVariables.getMessageMap().putError("newNote.attachment.attachmentTypeCode", CUKFSKeyConstants.ERROR_DOCUMENT_ADD_TYPED_ATTACHMENT,
                    note.getAttachment().getAttachmentTypeCode());
            valid = false;
        }
        
        // Make sure the user is not trying to add a potentially-confidential note or attachment unless it's been explicitly flagged as such.
        if (valid && note.getAttachment() != null
                && !ConfidentialAttachmentTypeCodes.CONFIDENTIAL_ATTACHMENT_TYPE.equals(note.getAttachment().getAttachmentTypeCode())
                && attachmentFile != null && StringUtils.isNotBlank(attachmentFile.getFileName())) {
            Collection<String> confAttachmentPatterns = CoreFrameworkServiceLocator.getParameterService().getParameterValuesAsString(
                    KFSConstants.CoreModuleNamespaces.KFS, KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE,
                    CUKFSParameterKeyConstants.CONFIDENTIAL_ATTACHMENT_FILENAME_PATTERNS);
            
            // Check if the note text indicates a potentially-confidential attachment.
            if (StringUtils.isNotBlank(note.getNoteText()) && foundMatchingPattern(note.getNoteText(), confAttachmentPatterns)) {
                GlobalVariables.getMessageMap().putError("newNote.noteText", CUKFSKeyConstants.ERROR_DOCUMENT_ADD_UNFLAGGED_CONFIDENTIAL_ATTACHMENT);
                valid = false;
            }
            
            // Check if the filename indicates a potentially-confidential attachment.
            if (valid && foundMatchingPattern(attachmentFile.getFileName(), confAttachmentPatterns)) {
                GlobalVariables.getMessageMap().putError("attachmentFile", CUKFSKeyConstants.ERROR_DOCUMENT_ADD_UNFLAGGED_CONFIDENTIAL_ATTACHMENT);
                valid = false;
            }
        }
        
        return valid;
    }

    /*
     * Helper method for determining if the given text matches at least one of the listed patterns.
     * The text will be auto-uppercased prior to matching.
     */
    private static boolean foundMatchingPattern(String text, Collection<String> patterns) {
        text = text.toUpperCase(Locale.US);
        for (String pattern : patterns) {
            if (Pattern.compile(pattern).matcher(text).find()) {
                return true;
            }
        }
        
        return false;
    }

}
