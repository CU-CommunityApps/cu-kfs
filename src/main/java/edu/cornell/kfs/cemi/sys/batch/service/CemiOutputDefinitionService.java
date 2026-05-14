package edu.cornell.kfs.cemi.sys.batch.service;

import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;

public interface CemiOutputDefinitionService {

    CemiOutputDefinition getCemiOutputDefinition(final String modulePath, final String definitionName);

}
