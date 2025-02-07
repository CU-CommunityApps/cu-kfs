package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.NoteLite.NoteField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

public class NoteLiteMapper extends ReadOnlyTaxDtoRowMapperBase<NoteLite> {

    protected NoteLiteMapper(final EncryptionService encryptionService, final TaxDtoDbMetadata metadata,
            final ResultSet resultSet) {
        super(NoteLite::new, encryptionService, metadata, resultSet);
    }

    @Override
    protected void populateDtoFromCurrentRow(final NoteLite note) throws SQLException {
        note.setNoteIdentifier(getLong(NoteField.noteIdentifier));
        note.setRemoteObjectIdentifier(getString(NoteField.remoteObjectIdentifier));
        note.setNoteText(getString(NoteField.noteText));
    }

}
