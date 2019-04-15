package edu.cornell.kfs.rass.batch.service;

import java.util.List;

import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlObjectGroupResult;

public interface RassService {

    List<RassXmlFileParseResult> readXML();

    List<RassXmlObjectGroupResult> updateKFS(List<RassXmlFileParseResult> successfullyParsedFiles);

}
