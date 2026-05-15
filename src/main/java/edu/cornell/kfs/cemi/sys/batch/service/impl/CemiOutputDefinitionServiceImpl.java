package edu.cornell.kfs.cemi.sys.batch.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.CemiOutputDefinitionService;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;

public class CemiOutputDefinitionServiceImpl implements CemiOutputDefinitionService {

    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;

    @Override
    public CemiOutputDefinition getCemiOutputDefinition(final String modulePath, final String definitionName) {
        Validate.isTrue(CemiUtils.isFormattedAsValidFilePath(modulePath), "modulePath is blank or malformed");
        Validate.isTrue(CemiUtils.isFormattedAsValidIdentifier(definitionName), "definitionName is blank or malformed");
        final String definitionFile = MessageFormat.format(CemiBaseConstants.CEMI_OUTPUT_DEFINITION_FILE_PATH_FORMAT,
                modulePath, definitionName);
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

    public void setCemiOutputDefinitionFileType(final CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

}
