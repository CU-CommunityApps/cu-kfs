package edu.cornell.kfs.module.purap.rest.jsonOnjects.fixture;

import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestNoteDto;

public enum PaymentRequestNoteDtoFixture {

    NOTE_GENERAL("Note text", "General"),
    NOTE_FIRST("First note", "General"),
    NOTE_SECOND("Second note", "Urgent"),
    NOTE_THIRD("Third note", "Follow-up");

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
