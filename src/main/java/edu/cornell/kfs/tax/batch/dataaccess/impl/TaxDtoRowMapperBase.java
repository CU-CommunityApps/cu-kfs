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



    protected String getColumnLabel(final Enum<?> fieldDefinition) {
        return metadata.getFullColumnLabel(fieldDefinition);
    }

    protected String getString(final Enum<?> fieldDefinition) throws SQLException {
        return resultSet.getString(getColumnLabel(fieldDefinition));
    }

    protected int getInt(final Enum<?> fieldDefinition) throws SQLException {
        return resultSet.getInt(getColumnLabel(fieldDefinition));
    }

    protected long getLong(final Enum<?> fieldDefinition) throws SQLException {
        return resultSet.getLong(getColumnLabel(fieldDefinition));
    }

    protected java.sql.Date getDate(final Enum<?> fieldDefinition) throws SQLException {
        return resultSet.getDate(getColumnLabel(fieldDefinition));
    }

    protected String getAndDecryptString(final Enum<?> fieldDefinition) throws SQLException {
        final String value = getString(fieldDefinition);
        try {
            return encryptionService.decrypt(value);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected Boolean getBoolean(final Enum<?> fieldDefinition) throws SQLException {
        final String value = getString(fieldDefinition);
        return Truth.strToBooleanIgnoreCase(value);
    }

    protected Boolean getBoolean(final Enum<?> fieldDefinition, final Boolean defaultValue) throws SQLException {
        final String value = getString(fieldDefinition);
        return Truth.strToBooleanIgnoreCase(value, defaultValue);
    }

    protected KualiDecimal getKualiDecimal(final Enum<?> fieldDefinition) throws SQLException {
        final BigDecimal value = resultSet.getBigDecimal(getColumnLabel(fieldDefinition));
        return (value != null) ? new KualiDecimal(value) : null;
    }

    protected KualiInteger getKualiInteger(final Enum<?> fieldDefinition) throws SQLException {
        final long value = getLong(fieldDefinition);
        return new KualiInteger(value);
    }

    protected void updateString(final Enum<?> fieldDefinition, final String value) throws SQLException {
        resultSet.updateString(getColumnLabel(fieldDefinition), value);
    }

}
