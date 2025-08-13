package edu.cornell.kfs.tax.batch.service;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

public interface TaxTableMetadataLookupService {

    TaxDtoDbMetadata getDatabaseMappingMetadataForDto(final Class<? extends TaxDtoFieldEnum> dtoFieldEnumClass);

    TaxDtoDbMetadata getDatabaseMappingMetadataForDto(final Class<? extends TaxDtoFieldEnum> dtoFieldEnumClass,
            final int aliasSuffixOffset);

}
