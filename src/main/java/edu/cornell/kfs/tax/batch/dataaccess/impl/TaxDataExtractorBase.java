package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.core.api.util.Truth;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDataExtractor;

public abstract class TaxDataExtractorBase<T> implements TaxDataExtractor<T> {

    protected final ResultSet resultSet;
    protected final EncryptionService encryptionService;

    protected TaxDataExtractorBase(final ResultSet resultSet, final EncryptionService encryptionService) {
        Validate.notNull(resultSet, "resultSet cannot be null");
        Validate.notNull(encryptionService, "encryptionService cannot be null");
        this.resultSet = resultSet;
        this.encryptionService = encryptionService;
    }

    @Override
    public boolean moveToNextRow() throws SQLException {
        return resultSet.next();
    }

    protected String getString(final Enum<?> column) throws SQLException {
        return resultSet.getString(column.name());
    }

    protected int getInt(final Enum<?> column) throws SQLException {
        return resultSet.getInt(column.name());
    }

    protected long getLong(final Enum<?> column) throws SQLException {
        return resultSet.getLong(column.name());
    }

    protected java.sql.Date getDate(final Enum<?> column) throws SQLException {
        return resultSet.getDate(column.name());
    }

    protected String getAndDecrypt(final Enum<?> column) throws SQLException {
        final String value = getString(column);
        try {
            return encryptionService.decrypt(value);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected Boolean getAsBoolean(final Enum<?> column) throws SQLException {
        final String value = resultSet.getString(column.name());
        return Truth.strToBooleanIgnoreCase(value);
    }

    protected Boolean getAsBooleanOrDefault(final Enum<?> column, final Boolean defaultValue) throws SQLException {
        final String value = resultSet.getString(column.name());
        return Truth.strToBooleanIgnoreCase(value, defaultValue);
    }

    protected KualiDecimal getAsKualiDecimal(final Enum<?> column) throws SQLException {
        final BigDecimal value = resultSet.getBigDecimal(column.name());
        return new KualiDecimal(value);
    }

    protected KualiInteger getAsKualiInteger(final Enum<?> column) throws SQLException {
        final Long value = resultSet.getLong(column.name());
        return new KualiInteger(value);
    }

}
