package edu.cornell.kfs.cemi.sys.batch.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.CemiOutputDefinitionService;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;

public class CemiOutputDefinitionServiceImpl implements CemiOutputDefinitionService {

    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private Map<String, String> definitionFileMappings;

    @Cacheable(cacheNames = CemiOutputDefinition.CACHE_NAME, key = "'{getCemiOutputDefinition}definitionName=' + #p0")
    @Override
    public CemiOutputDefinition getCemiOutputDefinition(final String definitionName) {
        Validate.notBlank(definitionName, "definitionName cannot be blank");
        final String definitionFile = definitionFileMappings.get(definitionName);
        Validate.notBlank(definitionFile, "Unrecognized definition: %s", definitionName);
        return readOutputDefinitionFromFile(definitionFile);
    }

    private CemiOutputDefinition readOutputDefinitionFromFile(final String fileName) {
        try (
            final InputStream inputStream = CuCoreUtilities.getResourceAsStream(fileName);
        ) {
            final byte[] fileContents = IOUtils.toByteArray(inputStream);
            return cemiOutputDefinitionFileType.parse(fileContents);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @CacheEvict(value = CemiOutputDefinition.CACHE_NAME, allEntries = true)
    @Override
    public void flushCache() {
        
    }

    public void setCemiOutputDefinitionFileType(final CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

    public void setDefinitionFileMappings(final Map<String, String> definitionFileMappings) {
        this.definitionFileMappings = definitionFileMappings;
    }

}
