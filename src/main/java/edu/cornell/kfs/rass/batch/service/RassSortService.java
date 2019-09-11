package edu.cornell.kfs.rass.batch.service;

import java.util.List;

import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;

public interface RassSortService {
    List<RassXmlAgencyEntry> sortRassXmlAgencyEntriesForUpdate(List<RassXmlAgencyEntry> agencies);
    List<RassXmlFileParseResult> sortRassXmlFileParseResult(List<RassXmlFileParseResult> results);
}
