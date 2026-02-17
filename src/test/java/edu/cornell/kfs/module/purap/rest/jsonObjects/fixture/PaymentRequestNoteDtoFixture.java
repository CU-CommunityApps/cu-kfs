package edu.cornell.kfs.module.purap.rest.jsonObjects.fixture;

import org.apache.commons.lang3.StringUtils;
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
    TOO_LONG_NOTE   ("This note is longer than 15 characters so it should cause a validation error.", PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_OTHER),
    EMPTY_NOTE(null, null);

    public final String noteText;
    public final String noteType;

    private PaymentRequestNoteDtoFixture(String noteText, String noteType) {
        this.noteText = noteText;
        this.noteType = noteType;
    }

    public PaymentRequestNoteDto toPaymentRequestNoteDto() {
        PaymentRequestNoteDto dto = new PaymentRequestNoteDto();
        dto.setNoteText(noteText);
        dto.setNoteType(noteType);
        return dto;
    }

}
