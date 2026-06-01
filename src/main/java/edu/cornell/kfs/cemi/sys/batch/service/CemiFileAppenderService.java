package edu.cornell.kfs.cemi.sys.batch.service;

import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;

public interface CemiFileAppenderService {

    void populateFileFromOrmDataStorage(final CemiExcelWriter writer, final CemiOutputDefinition outputDefinition,
            final String jobRunDate);

}
