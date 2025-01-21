package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.batch.dataaccess.UpdatableTaxDataExtractor;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoFieldDefinition;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoMappingDefinition;

public class UpdatableTaxDataExtractorImpl<T, U> extends TaxDataExtractorImpl<T>
        implements UpdatableTaxDataExtractor<T, U> {

    private TaxDtoMappingDefinition<U> dtoDefinitionForUpdates;

    public UpdatableTaxDataExtractorImpl(final TaxDtoMappingDefinition<T> dtoDefinition,
            final TaxDtoMappingDefinition<U> dtoDefinitionForUpdates, final ResultSet resultSet) {
        super(dtoDefinition, resultSet);
        Validate.notNull(dtoDefinitionForUpdates, "dtoDefinitionForUpdates cannot be null");
        this.dtoDefinitionForUpdates = dtoDefinitionForUpdates;
    }

    @Override
    public void updateCurrentRow(final U dtoContainingUpdates) throws SQLException {
        for (final TaxDtoFieldDefinition<U, ?> fieldMapping : dtoDefinitionForUpdates.getFieldMappings()) {
            stageSqlUpdateFromField(dtoContainingUpdates, fieldMapping);
        }
        getResultSet().updateRow();
    }

    private <V> void stageSqlUpdateFromField(final U dtoContainingUpdates,
            final TaxDtoFieldDefinition<U, V> fieldMapping) throws SQLException {
        final V dtoFieldValue = fieldMapping.getPropertyGetter().apply(dtoContainingUpdates);
        final Object convertedValue = fieldMapping.hasFieldConverter()
                ? fieldMapping.getFieldConverter().convertToSqlValue(dtoFieldValue) : dtoFieldValue;
        getResultSet().updateObject(fieldMapping.getColumnLabel(), convertedValue);
    }

}
