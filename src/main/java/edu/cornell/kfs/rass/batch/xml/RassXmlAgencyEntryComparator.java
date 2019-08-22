package edu.cornell.kfs.rass.batch.xml;

import org.apache.commons.lang3.StringUtils;
import java.util.Comparator;

public class RassXmlAgencyEntryComparator implements Comparator<RassXmlAgencyEntry> {
    
    @Override
    public int compare(RassXmlAgencyEntry agency1, RassXmlAgencyEntry agency2) {
        int reportsToAgencySort = StringUtils.compare(agency1.getReportsToAgencyNumber(), agency2.getReportsToAgencyNumber());
        if (reportsToAgencySort == 0) {
            return StringUtils.compare(agency1.getNumber(), agency2.getNumber());
        } else {
            return reportsToAgencySort;
        }
    }

}
