package edu.cornell.kfs.tax.batch.service;

import edu.cornell.kfs.tax.batch.dto.util.TaxDtoExtractorDefinition;

public interface TaxTableMetadataLookupService {

    <T> TaxDtoExtractorDefinition<T> getDtoExtractionMetadata(final Class<T> dtoClass);

}
