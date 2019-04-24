package edu.cornell.kfs.rass.batch.service;

import java.util.List;

import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlProcessingResults;

public interface RassService {

    List<RassXmlFileParseResult> readXML();

    RassXmlProcessingResults updateKFS(List<RassXmlFileParseResult> successfullyParsedFiles);

}
