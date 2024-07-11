package edu.cornell.kfs.module.purap.iwant.xml.fixture;

import edu.cornell.kfs.module.purap.iwant.xml.IWantNoteXml;

public enum IWantNoteFixture {
    NOTE_TEXT("note text"), ANOTHER_NOTE_TEXT("another note text");

    public final String noteText;

    private IWantNoteFixture(String noteText) {
        this.noteText = noteText;
    }

    public IWantNoteXml toIWantNoteXml() {
        IWantNoteXml note = new IWantNoteXml();
        note.setNoteText(noteText);
        return note;
    }

}
