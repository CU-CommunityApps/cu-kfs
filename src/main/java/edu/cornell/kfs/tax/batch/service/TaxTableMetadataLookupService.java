package edu.cornell.kfs.tax.batch.service;

import edu.cornell.kfs.tax.batch.metadata.TaxDtoMappingDefinition;

public interface TaxTableMetadataLookupService {

    <T> TaxDtoMappingDefinition<T> getDatabaseMappingMetadataForDto(final Class<T> dtoClass);

}
