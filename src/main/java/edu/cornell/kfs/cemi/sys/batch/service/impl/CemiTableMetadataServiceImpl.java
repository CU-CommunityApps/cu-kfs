package edu.cornell.kfs.cemi.sys.batch.service.impl;

import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiTableMetadata;
import edu.cornell.kfs.cemi.sys.batch.service.CemiTableMetadataService;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;

public class CemiTableMetadataServiceImpl implements CemiTableMetadataService {

    @Cacheable(cacheNames = CemiTableMetadata.CACHE_NAME,
            key = "'{getCemiTableMetadata}outputDefinition=' + #p0.name + ',sheetName=' + #p1")
    @Override
    public CemiTableMetadata getCemiTableMetadata(final CemiOutputDefinition outputDefinition, final String sheetName) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notBlank(sheetName, "sheetName cannot be blank");

        final CemiSheetDefinition sheetDefinition = outputDefinition.getSheets().stream()
                .filter(sheet -> Strings.CS.equals(sheet.getName(), sheetName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Sheet definition not found: " + sheetName));

        return CemiTableMetadata.of(outputDefinition.getName(), sheetDefinition);
    }

    @CacheEvict(value = CemiTableMetadata.CACHE_NAME, allEntries = true)
    @Override
    public void flushCache() {

    }

}
