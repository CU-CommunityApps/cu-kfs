package edu.cornell.kfs.rass.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.rass.batch.service.RassSortService;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;

public class RassSortServiceImpl implements RassSortService {
    private static final Logger LOG = LogManager.getLogger(RassSortServiceImpl.class);

    @Override
    public List<RassXmlAgencyEntry> sortRassXmlAgencyEntriesForUpdate(List<RassXmlAgencyEntry> agencies) {
        AgenciesSortHelper sortedAgencyHelper = new AgenciesSortHelper();
        sortParentAgencies(agencies, sortedAgencyHelper);
        sortChildAgencies(sortedAgencyHelper);
        sortLeftOverAgencies(sortedAgencyHelper);
        return sortedAgencyHelper.sortedAgencies;
    }

    private void sortParentAgencies(List<RassXmlAgencyEntry> agencies, AgenciesSortHelper sortedAgencyHelper) {
        for (RassXmlAgencyEntry entry : agencies) {
            if (StringUtils.isBlank(entry.getReportsToAgencyNumber())) {
                sortedAgencyHelper.agencyNumberInSortedList.add(entry.getNumber());
                sortedAgencyHelper.sortedAgencies.add(entry);
            } else {
                sortedAgencyHelper.leftOverAgencies.add(entry);
            }
        }
        LOG.debug("sortParentAgencies, number of angencies without reports to agencies: " + sortedAgencyHelper.sortedAgencies.size());
    }

    private void sortChildAgencies(AgenciesSortHelper sortedAgencyHelper) {
        boolean isWeedingSuccessfull = true;
        int loopCount = 0;
        while (CollectionUtils.isNotEmpty(sortedAgencyHelper.leftOverAgencies) && isWeedingSuccessfull) {
            LOG.debug("sortChildAgencies. the loop count: " + loopCount);
            LOG.debug("sortChildAgencies, number of agencies in the sorted list during sort loop: " + sortedAgencyHelper.sortedAgencies.size());
            List<RassXmlAgencyEntry> newLeftOverAgencies = new ArrayList<RassXmlAgencyEntry>();
            
            for (RassXmlAgencyEntry entry : sortedAgencyHelper.leftOverAgencies) {
                if (sortedAgencyHelper.agencyNumberInSortedList.contains(entry.getReportsToAgencyNumber())) {
                    sortedAgencyHelper.agencyNumberInSortedList.add(entry.getNumber());
                    sortedAgencyHelper.sortedAgencies.add(entry);
                } else {
                    newLeftOverAgencies.add(entry);
                }
            }
            
            if (sortedAgencyHelper.leftOverAgencies.size() == newLeftOverAgencies.size()) {
                isWeedingSuccessfull = false;
            } else {
                sortedAgencyHelper.leftOverAgencies = newLeftOverAgencies;
            }
            loopCount++;
        }
        
        LOG.debug("sortChildAgencies, number of agencies in the sorted list after main sort loop: " + sortedAgencyHelper.sortedAgencies.size());
    }

    private void sortLeftOverAgencies(AgenciesSortHelper sortedAgencyHelper) {
        if (CollectionUtils.isNotEmpty(sortedAgencyHelper.leftOverAgencies)) {
            LOG.debug("sortRassXmlAgencyEntriesForUpdate, number of lefter over agencies: " + sortedAgencyHelper.leftOverAgencies.size());
            sortedAgencyHelper.sortedAgencies.addAll(sortedAgencyHelper.leftOverAgencies);
        }
        
        LOG.debug("sortRassXmlAgencyEntriesForUpdate, number of agencies in the sorted list after sort completion: " + sortedAgencyHelper.sortedAgencies.size());
    }
    
    private class AgenciesSortHelper {
        public List<RassXmlAgencyEntry> sortedAgencies;
        public List<String> agencyNumberInSortedList;
        public List<RassXmlAgencyEntry> leftOverAgencies;
        
        AgenciesSortHelper() {
            sortedAgencies = new ArrayList<RassXmlAgencyEntry>();
            agencyNumberInSortedList = new ArrayList<String>();
            leftOverAgencies = new ArrayList<RassXmlAgencyEntry>();
        }
    }

}
