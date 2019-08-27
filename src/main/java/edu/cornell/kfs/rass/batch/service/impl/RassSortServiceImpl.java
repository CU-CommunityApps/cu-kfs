package edu.cornell.kfs.rass.batch.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.rass.batch.service.RassSortService;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntryComparator;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;

public class RassSortServiceImpl implements RassSortService {
    private static final Logger LOG = LogManager.getLogger(RassSortServiceImpl.class);

    @Override
    public List<RassXmlAgencyEntry> sortRassXmlAgencyEntriesForUpdate(List<RassXmlAgencyEntry> agencies) {
        AgenciesSortHelper agencySortHelper = new AgenciesSortHelper();
        sortParentAgencies(agencies, agencySortHelper);
        sortChildAgencies(agencySortHelper);
        sortLeftOverAgencies(agencySortHelper);
        return agencySortHelper.sortedAgencies;
    }

    private void sortParentAgencies(List<RassXmlAgencyEntry> agencies, AgenciesSortHelper agencySortHelper) {
        for (RassXmlAgencyEntry entry : agencies) {
            if (StringUtils.isBlank(entry.getReportsToAgencyNumber())) {
                agencySortHelper.agencyNumberInSortedList.add(entry.getNumber());
                agencySortHelper.sortedAgencies.add(entry);
            } else {
                agencySortHelper.leftOverAgencies.add(entry);
            }
        }
        LOG.debug("sortParentAgencies, number of angencies without reports to agencies: " + agencySortHelper.sortedAgencies.size());
    }

    private void sortChildAgencies(AgenciesSortHelper agencySortHelper) {
        boolean isWeedingSuccessfull = true;
        int loopCount = 0;
        while (CollectionUtils.isNotEmpty(agencySortHelper.leftOverAgencies) && isWeedingSuccessfull) {
            LOG.debug("sortChildAgencies. the loop count: " + loopCount);
            LOG.debug("sortChildAgencies, number of agencies in the sorted list during sort loop: " + agencySortHelper.sortedAgencies.size());
            List<RassXmlAgencyEntry> newLeftOverAgencies = new ArrayList<RassXmlAgencyEntry>();
            
            for (RassXmlAgencyEntry entry : agencySortHelper.leftOverAgencies) {
                if (agencySortHelper.agencyNumberInSortedList.contains(entry.getReportsToAgencyNumber())) {
                    agencySortHelper.agencyNumberInSortedList.add(entry.getNumber());
                    agencySortHelper.sortedAgencies.add(entry);
                } else {
                    newLeftOverAgencies.add(entry);
                }
            }
            
            if (agencySortHelper.leftOverAgencies.size() == newLeftOverAgencies.size()) {
                isWeedingSuccessfull = false;
            }
            agencySortHelper.leftOverAgencies = newLeftOverAgencies;
            loopCount++;
        }
        
        LOG.debug("sortChildAgencies, number of agencies in the sorted list after main sort loop: " + agencySortHelper.sortedAgencies.size());
    }

    private void sortLeftOverAgencies(AgenciesSortHelper agencySortHelper) {
        if (CollectionUtils.isNotEmpty(agencySortHelper.leftOverAgencies)) {
            LOG.debug("sortLeftOverAgencies, number of left over agencies: " + agencySortHelper.leftOverAgencies.size());
            Collections.sort(agencySortHelper.leftOverAgencies, new RassXmlAgencyEntryComparator());
            sortLeftOverParents(agencySortHelper);
            agencySortHelper.sortedAgencies.addAll(agencySortHelper.leftOverAgencies);
        }
    }
    
    private void sortLeftOverParents(AgenciesSortHelper agencySortHelper) {
        List<String> reportsToAgencyNumbes = agencySortHelper.leftOverAgencies.stream()
                .map(a -> a.getReportsToAgencyNumber())
                .collect(Collectors.toList());
        
        List<RassXmlAgencyEntry> newLeftOverAgencies = new ArrayList<RassXmlAgencyEntry>();
        for (RassXmlAgencyEntry entry : agencySortHelper.leftOverAgencies) {
            if (reportsToAgencyNumbes.contains(entry.getNumber())) {
                agencySortHelper.sortedAgencies.add(entry);
            } else {
                newLeftOverAgencies.add(entry);
            }
        }
        agencySortHelper.leftOverAgencies = newLeftOverAgencies;
        
        LOG.debug("sortLeftOverParents, number of agencies in the sorted list after adding in parent agencies in the left over list: " + agencySortHelper.sortedAgencies.size());
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
