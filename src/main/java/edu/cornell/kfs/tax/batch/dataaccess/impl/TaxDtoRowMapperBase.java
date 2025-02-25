package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.core.api.util.Truth;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

public abstract class TaxDtoRowMapperBase<T, U> implements TaxDtoRowMapper<T, U> {

    protected final Supplier<T> dtoConstructor;
    protected final EncryptionService encryptionService;
    protected final TaxDtoDbMetadata metadata;
    protected final ResultSet resultSet;

    protected TaxDtoRowMapperBase(final Supplier<T> dtoConstructor, final EncryptionService encryptionService,
            final TaxDtoDbMetadata metadata, final ResultSet resultSet) {
        Validate.notNull("dtoConstructor cannot be null");
        Validate.notNull("encryptionService cannot be null");
        Validate.notNull("metadata cannot be null");
        Validate.notNull("resultSet cannot be null");
        this.dtoConstructor = dtoConstructor;
        this.encryptionService = encryptionService;
        this.metadata = metadata;
        this.resultSet = resultSet;
    }

    @Override
    public boolean moveToNextRow() throws SQLException {
        return resultSet.next();
    }

    @Override
    public T readCurrentRow() throws SQLException {
        final T dto = dtoConstructor.get();
        populateDtoFromCurrentRow(dto);
        return dto;
    }

    protected abstract void populateDtoFromCurrentRow(final T dto) throws SQLException;

    @Override
    public void updateCurrentRow(final U dtoContainingUpdates) throws SQLException {
        prepareCurrentRowForUpdate(dtoContainingUpdates);
        resultSet.updateRow();
    }

    protected abstract void prepareCurrentRowForUpdate(final U dtoContainingUpdates) throws SQLException;

    protected String getColumnAlias(final TaxDtoFieldEnum fieldDefinition) {
        return metadata.getColumnAlias(fieldDefinition);
    }

    protected String getString(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        return resultSet.getString(getColumnAlias(fieldDefinition));
    }

    protected int getInt(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        return resultSet.getInt(getColumnAlias(fieldDefinition));
    }

    protected long getLong(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        return resultSet.getLong(getColumnAlias(fieldDefinition));
    }

    protected java.sql.Date getDate(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        return resultSet.getDate(getColumnAlias(fieldDefinition));
    }

    protected String getAndDecryptString(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final String value = getString(fieldDefinition);
        try {
            return encryptionService.decrypt(value);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected Boolean getBoolean(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final String value = getString(fieldDefinition);
        return Truth.strToBooleanIgnoreCase(value);
    }

    protected Boolean getBoolean(final TaxDtoFieldEnum fieldDefinition, final Boolean defaultValue)
            throws SQLException {
        final String value = getString(fieldDefinition);
        return Truth.strToBooleanIgnoreCase(value, defaultValue);
    }

    protected KualiDecimal getKualiDecimal(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final BigDecimal value = resultSet.getBigDecimal(getColumnAlias(fieldDefinition));
        return (value != null) ? new KualiDecimal(value) : null;
    }

    protected KualiInteger getKualiInteger(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final long value = getLong(fieldDefinition);
        return new KualiInteger(value);
    }

    protected void updateString(final TaxDtoFieldEnum fieldDefinition, final String value) throws SQLException {
        resultSet.updateString(getColumnAlias(fieldDefinition), value);
    }

}
