package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.TaxColumns.NoteColumn;
import edu.cornell.kfs.tax.businessobject.NoteLite;

public class NoteLiteExtractorImpl extends TaxDataExtractorBase<NoteLite> {

    public NoteLiteExtractorImpl(final ResultSet resultSet, final EncryptionService encryptionService) {
        super(resultSet, encryptionService);
    }

    @Override
    public NoteLite getCurrentRow() throws SQLException {
        final NoteLite note = new NoteLite();
        note.setNoteIdentifier(getLong(NoteColumn.NTE_ID));
        note.setRemoteObjectIdentifier(getString(NoteColumn.RMT_OBJ_ID));
        note.setNoteText(getString(NoteColumn.TXT));
        return note;
    }

}
