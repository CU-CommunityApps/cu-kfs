package edu.cornell.kfs.rass.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.rass.batch.service.RassSortService;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;

public class RassSortServiceImpl implements RassSortService {
    private static final Logger LOG = LogManager.getLogger(RassSortServiceImpl.class);

    @Override
    public List<RassXmlAgencyEntry> sortRassXmlAgencyEntriesForUpdate(List<RassXmlAgencyEntry> agencies) {
        AgenciesSortHelper agencySortHelper = new AgenciesSortHelper();
        sortAgenciesWithNoReportsToAgency(agencies, agencySortHelper);
        sortAgenciesWithReportsToAgencyThatHasBeenSorted(agencySortHelper);
        sortLeftOverAgencies(agencySortHelper);
        return agencySortHelper.sortedAgencies;
    }

    private void sortAgenciesWithNoReportsToAgency(List<RassXmlAgencyEntry> agencies, AgenciesSortHelper agencySortHelper) {
        for (RassXmlAgencyEntry entry : agencies) {
            if (StringUtils.isBlank(entry.getReportsToAgencyNumber())) {
                agencySortHelper.agencyNumberInSortedList.add(entry.getNumber());
                agencySortHelper.sortedAgencies.add(entry);
            } else {
                agencySortHelper.leftOverAgencies.add(entry);
            }
        }
        LOG.debug("sortAgenciesWithNoReportsToAgency, number of angencies without reports to agencies: " + agencySortHelper.sortedAgencies.size());
    }

    private void sortAgenciesWithReportsToAgencyThatHasBeenSorted(AgenciesSortHelper agencySortHelper) {
        boolean isWeedingSuccessfull = true;
        int loopCount = 0;
        while (CollectionUtils.isNotEmpty(agencySortHelper.leftOverAgencies) && isWeedingSuccessfull) {
            LOG.debug("sortAgenciesWithReportsToAgencyThatHasBeenSorted. the loop count: " + loopCount);
            LOG.debug("sortAgenciesWithReportsToAgencyThatHasBeenSorted, number of agencies in the sorted list during sort loop: " + agencySortHelper.sortedAgencies.size());
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
        
        LOG.debug("sortAgenciesWithReportsToAgencyThatHasBeenSorted, number of agencies in the sorted list after main sort loop: " + agencySortHelper.sortedAgencies.size());
    }

    private void sortLeftOverAgencies(AgenciesSortHelper agencySortHelper) {
        if (CollectionUtils.isNotEmpty(agencySortHelper.leftOverAgencies)) {
            LOG.debug("sortLeftOverAgencies, number of left over agencies: " + agencySortHelper.leftOverAgencies.size());
            sortAgenciesWithReportsToAgencyNotInRASSInput(agencySortHelper);
            LOG.debug("sortLeftOverAgencies, number of left over agencies after finding left over parents: " + agencySortHelper.leftOverAgencies.size());
            if (CollectionUtils.isNotEmpty(agencySortHelper.leftOverAgencies)) {
                sortAgenciesWithReportsToAgencyThatHasBeenSorted(agencySortHelper);
                if (CollectionUtils.isNotEmpty(agencySortHelper.leftOverAgencies)) {
                    LOG.info("sortLeftOverAgencies, number of left over agencies that could NOT be sorted: " + agencySortHelper.leftOverAgencies.size());
                    LOG.info("sortLeftOverAgencies, unsorted agencies: " + agencySortHelper.leftOverAgencies);
                    agencySortHelper.sortedAgencies.addAll(agencySortHelper.leftOverAgencies);
                }
            }
        }
    }
    
    private void sortAgenciesWithReportsToAgencyNotInRASSInput(AgenciesSortHelper agencySortHelper) {
        List<RassXmlAgencyEntry> newLeftOverAgencies = new ArrayList<RassXmlAgencyEntry>();
        for (RassXmlAgencyEntry entry : agencySortHelper.leftOverAgencies) {
            if (isReportsToAgencyNumberAnAgencyInLeftOverAgencyList(entry.getReportsToAgencyNumber(), agencySortHelper.leftOverAgencies)) {
                newLeftOverAgencies.add(entry);
            } else {
                agencySortHelper.sortedAgencies.add(entry);
                agencySortHelper.agencyNumberInSortedList.add(entry.getNumber());
                
            }
        }
        agencySortHelper.leftOverAgencies = newLeftOverAgencies;
    }
    
    private boolean isReportsToAgencyNumberAnAgencyInLeftOverAgencyList(String reportsToAgencyNumber, List<RassXmlAgencyEntry> leftOverAgencies) {
        for (RassXmlAgencyEntry leftOverAgency : leftOverAgencies) {
            if (StringUtils.equalsIgnoreCase(leftOverAgency.getNumber(), reportsToAgencyNumber)) {
                return true;
            }
        }
        return false;
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
