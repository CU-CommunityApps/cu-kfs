package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.dto.DocumentHeaderLite;
import edu.cornell.kfs.tax.batch.dto.DocumentHeaderLite.DocumentHeaderField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

public class DocumentHeaderLiteMapper extends ReadOnlyTaxDtoRowMapperBase<DocumentHeaderLite> {

    protected DocumentHeaderLiteMapper(final EncryptionService encryptionService, final TaxDtoDbMetadata metadata,
            final ResultSet resultSet) {
        super(DocumentHeaderLite::new, encryptionService, metadata, resultSet);
    }

    @Override
    protected void populateDtoFromCurrentRow(final DocumentHeaderLite docHeader) throws SQLException {
        docHeader.setDocumentNumber(getString(DocumentHeaderField.documentNumber));
        docHeader.setObjectId(getString(DocumentHeaderField.objectId));
    }

}
