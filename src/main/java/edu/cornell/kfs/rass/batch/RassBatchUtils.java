package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;

public final class RassBatchUtils {
    private static final Logger LOG = LogManager.getLogger(RassBatchUtils.class);
    
    private RassBatchUtils() {
        throw new IllegalAccessError("should not build an instance of utility classes");
    }
    
    public static List<RassXmlAgencyEntry> sortRassXmlAgencyEntriesForUpdate(List<RassXmlAgencyEntry> agencies) {
        List<RassXmlAgencyEntry> sortedAgencies = new ArrayList<RassXmlAgencyEntry>();
        List<String> agencyNumberInSortedList = new ArrayList<String>();
        List<RassXmlAgencyEntry> leftOverAgencies = new ArrayList<RassXmlAgencyEntry>();
        
        for (RassXmlAgencyEntry entry : agencies) {
            if (StringUtils.isBlank(entry.getReportsToAgencyNumber())) {
                agencyNumberInSortedList.add(entry.getNumber());
                sortedAgencies.add(entry);
            } else {
                leftOverAgencies.add(entry);
            }
        }
        
        LOG.debug("sortRassXmlAgencyEntriesForUpdate, number of angencies without reports to agencies: " + sortedAgencies.size());
        
        boolean isWeedingSuccessfull = true;
        int loopCount = 0;
        while (CollectionUtils.isNotEmpty(leftOverAgencies) && isWeedingSuccessfull) {
            LOG.debug("sortRassXmlAgencyEntriesForUpdate. the loop count: " + loopCount);
            LOG.debug("sortRassXmlAgencyEntriesForUpdate, number of agencies in the sorted list during sort loop: " + sortedAgencies.size());
            List<RassXmlAgencyEntry> newLeftOverAgencies = new ArrayList<RassXmlAgencyEntry>();
            
            for (RassXmlAgencyEntry entry : leftOverAgencies) {
                if (agencyNumberInSortedList.contains(entry.getReportsToAgencyNumber())) {
                    agencyNumberInSortedList.add(entry.getNumber());
                    sortedAgencies.add(entry);
                } else {
                    newLeftOverAgencies.add(entry);
                }
            }
            
            if (leftOverAgencies.size() == newLeftOverAgencies.size()) {
                isWeedingSuccessfull = false;
            } else {
                leftOverAgencies = newLeftOverAgencies;
            }
            loopCount++;
        }
        
        LOG.debug("sortRassXmlAgencyEntriesForUpdate, number of agencies in the sorted list after main sort loop: " + sortedAgencies.size());
        
        if (CollectionUtils.isNotEmpty(leftOverAgencies)) {
            LOG.debug("sortRassXmlAgencyEntriesForUpdate, number of lefter over agencies: " + leftOverAgencies.size());
            sortedAgencies.addAll(leftOverAgencies);
        }
        
        LOG.debug("sortRassXmlAgencyEntriesForUpdate, number of agencies in the sorted list after sort completion: " + sortedAgencies.size());
        
        return sortedAgencies;
    }

}
