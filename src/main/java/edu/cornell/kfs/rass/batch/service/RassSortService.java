package edu.cornell.kfs.rass.batch.service;

import java.util.List;

import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;

public interface RassSortService {
    List<RassXmlAgencyEntry> sortRassXmlAgencyEntriesForUpdate(List<RassXmlAgencyEntry> agencies);

}
