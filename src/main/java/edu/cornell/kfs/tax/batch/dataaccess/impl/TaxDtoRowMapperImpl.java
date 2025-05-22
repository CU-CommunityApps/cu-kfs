package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.core.api.util.Truth;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

public class TaxDtoRowMapperImpl<T> implements TaxDtoRowMapper<T> {

    @FunctionalInterface
    private static interface FieldReader {
        Object getFieldValue(final TaxDtoRowMapperImpl<?> mapper, final TaxDtoFieldEnum field) throws SQLException;
    }

    private static final Map<Class<?>, FieldReader> FIELD_READERS = Map.ofEntries(
            Map.entry(String.class, TaxDtoRowMapperImpl::getString),
            Map.entry(Integer.class, TaxDtoRowMapperImpl::getInteger),
            Map.entry(Long.class, TaxDtoRowMapperImpl::getLong),
            Map.entry(java.sql.Date.class, TaxDtoRowMapperImpl::getDate),
            Map.entry(Boolean.class, TaxDtoRowMapperImpl::getBoolean),
            Map.entry(boolean.class, TaxDtoRowMapperImpl::getOrDefaultBoolean),
            Map.entry(KualiDecimal.class, TaxDtoRowMapperImpl::getKualiDecimal),
            Map.entry(KualiInteger.class, TaxDtoRowMapperImpl::getKualiInteger)
    );

    private static final FieldReader DEFAULT_FIELD_READER = TaxDtoRowMapperImpl::getString;
    private static final FieldReader ENCRYPTED_FIELD_READER = TaxDtoRowMapperImpl::getAndDecryptString;

    private final Supplier<T> dtoConstructor;
    private final EncryptionService encryptionService;
    private final TaxDtoDbMetadata metadata;
    private final ResultSet resultSet;

    public TaxDtoRowMapperImpl(final Supplier<T> dtoConstructor, final EncryptionService encryptionService,
            final TaxDtoDbMetadata metadata, final ResultSet resultSet) throws SQLException {
        Validate.notNull(dtoConstructor, "dtoConstructor cannot be null");
        Validate.notNull(encryptionService, "encryptionService cannot be null");
        Validate.notNull(metadata, "metadata cannot be null");
        Validate.notNull(resultSet, "resultSet cannot be null");
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
        final BeanWrapper wrappedDto = PropertyAccessorFactory.forBeanPropertyAccess(dto);
        final Class<? extends TaxDtoFieldEnum> fieldEnumClass = metadata.getFieldEnumClass();
        final TaxDtoFieldEnum[] fields = fieldEnumClass.getEnumConstants();

        for (final TaxDtoFieldEnum field : fields) {
            final Class<?> propertyType = wrappedDto.getPropertyType(field.getFieldName());
            final FieldReader fieldReader = field.needsEncryptedStorage()
                    ? ENCRYPTED_FIELD_READER
                    : FIELD_READERS.getOrDefault(propertyType, DEFAULT_FIELD_READER);
            final Object fieldValue = fieldReader.getFieldValue(this, field);
            wrappedDto.setPropertyValue(field.getFieldName(), fieldValue);
        }

        return dto;
    }

    private String getColumnAlias(final TaxDtoFieldEnum fieldDefinition) {
        return metadata.getColumnAlias(fieldDefinition);
    }

    private String getString(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        return resultSet.getString(getColumnAlias(fieldDefinition));
    }

    private Integer getInteger(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final int value = resultSet.getInt(getColumnAlias(fieldDefinition));
        return resultSet.wasNull() ? null : Integer.valueOf(value);
    }

    private Long getLong(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final long value = resultSet.getLong(getColumnAlias(fieldDefinition));
        return resultSet.wasNull() ? null : Long.valueOf(value);
    }

    private java.sql.Date getDate(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        return resultSet.getDate(getColumnAlias(fieldDefinition));
    }

    private String getAndDecryptString(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final String value = getString(fieldDefinition);
        try {
            return encryptionService.decrypt(value);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private Boolean getBoolean(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final String value = getString(fieldDefinition);
        return Truth.strToBooleanIgnoreCase(value);
    }

    private Boolean getOrDefaultBoolean(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final String value = getString(fieldDefinition);
        return Truth.strToBooleanIgnoreCase(value, Boolean.FALSE);
    }

    private KualiDecimal getKualiDecimal(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final BigDecimal value = resultSet.getBigDecimal(getColumnAlias(fieldDefinition));
        return (value != null) ? new KualiDecimal(value) : null;
    }

    private KualiInteger getKualiInteger(final TaxDtoFieldEnum fieldDefinition) throws SQLException {
        final Long value = getLong(fieldDefinition);
        return (value != null) ? new KualiInteger(value) : null;
    }

}
