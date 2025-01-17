package edu.cornell.kfs.tax.batch.service;

import edu.cornell.kfs.tax.batch.dto.util.TaxDtoExtractorDefinition;
import edu.cornell.kfs.tax.batch.dto.util.TaxDtoUpdaterDefinition;

public interface TaxTableMetadataLookupService {

    <T> TaxDtoExtractorDefinition<T> getDtoMetadataForSqlExtraction(final Class<T> dtoClass);

    <T> TaxDtoUpdaterDefinition<T> getDtoMetadataForSqlUpdates(final Class<T> dtoClass);

}
