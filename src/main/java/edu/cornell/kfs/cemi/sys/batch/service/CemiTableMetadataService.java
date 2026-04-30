package edu.cornell.kfs.cemi.sys.batch.service;

import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiTableMetadata;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;

public interface CemiTableMetadataService {

    CemiTableMetadata getCemiTableMetadata(final CemiOutputDefinition outputDefinition, final String sheetName);

    void flushCache();

}
