package edu.cornell.kfs.rass.batch.service;

import java.util.List;
import java.util.Map;

import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlFileProcessingResult;

public interface RassService {

    List<RassXmlFileParseResult> readXML();

    Map<String, RassXmlFileProcessingResult> updateKFS(List<RassXmlFileParseResult> successfullyParsedFiles);

}
