package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

public abstract class ReadOnlyTaxDtoRowMapperBase<T> extends TaxDtoRowMapperBase<T, T> {

    protected ReadOnlyTaxDtoRowMapperBase(final Supplier<T> dtoConstructor, final EncryptionService encryptionService,
            final TaxDtoDbMetadata metadata, final ResultSet resultSet) {
        super(dtoConstructor, encryptionService, metadata, resultSet);
    }

    @Override
    public final void prepareCurrentRowForUpdate(final T dtoContainingUpdates) throws SQLException {
        throw new UnsupportedOperationException("This mapper implementation does not support row updates");
    }

    @Override
    protected void updateString(final TaxDtoFieldEnum fieldDefinition, final String value) throws SQLException {
        throw new UnsupportedOperationException("This mapper implementation does not support row updates");
    }

}
