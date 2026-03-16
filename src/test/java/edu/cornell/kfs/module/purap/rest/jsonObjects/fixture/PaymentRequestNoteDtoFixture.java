package edu.cornell.kfs.module.purap.rest.jsonObjects.fixture;

import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.util.Base64;
import org.kuali.kfs.module.purap.PurapConstants;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestNoteDto;

public enum PaymentRequestNoteDtoFixture {

    NOTE_GENERAL("Note text", "General"),
    NOTE_FIRST("First note", "General"),
    NOTE_SECOND("Second note", "Urgent"),
    NOTE_THIRD("Third note", "Follow-up"),
    NOTE_VALID("A testing note", PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_OTHER),
    LEGACY_NOTE_1("Please contact ega-list@cornell.edu with questions about this transaction", StringUtils.EMPTY),
    LEGACY_NOTE_2("Another note for fun", PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_OTHER),
    TOO_LONG_NOTE("This note is longer than 15 characters so it should cause a validation error.", PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_OTHER),
    ATTACHMENT_GOOD("Attachment", PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_INVOICE_IMAGE, "invoice.pdf", "pdf", "content"),
    ATTACHMENT_BAD("Attachment", PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_INVOICE_IMAGE, " ", null, "content"),
    EMPTY_NOTE(null, null);

    public final String noteText;
    public final String noteType;
    public final String attachmentFileName;
    public final String attachmentMimeType;
    public final String attachmentContent;

    private PaymentRequestNoteDtoFixture(String noteText, String noteType) {
        this(noteText, noteType, null, null, null);
    }

    private PaymentRequestNoteDtoFixture(String noteText, String noteType, String attachmentFileName, String attachmentMimeType, String attachmentContent) {
        this.noteText = noteText;
        this.noteType = noteType;
        this.attachmentFileName = attachmentFileName;
        this.attachmentMimeType = attachmentMimeType;
        if (attachmentContent != null) {
            this.attachmentContent = Base64.encodeString(attachmentContent);
        } else {
            this.attachmentContent = null;
        }
    }

    public PaymentRequestNoteDto toPaymentRequestNoteDto() {
        PaymentRequestNoteDto dto = new PaymentRequestNoteDto();
        dto.setNoteText(noteText);
        dto.setNoteType(noteType);
        dto.setAttachmentContent(attachmentContent);
        dto.setAttachmentFileName(attachmentFileName);
        dto.setAttachmentMimeType(attachmentMimeType);
        return dto;
    }

}
