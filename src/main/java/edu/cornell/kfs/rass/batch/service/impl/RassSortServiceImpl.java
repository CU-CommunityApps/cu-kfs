package edu.cornell.kfs.rass.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.service.RassSortService;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;

public class RassSortServiceImpl implements RassSortService {
    private static final Logger LOG = LogManager.getLogger(RassSortServiceImpl.class);

    @Override
    public List<RassXmlAgencyEntry> sortRassXmlAgencyEntriesForUpdate(List<RassXmlAgencyEntry> agencies) {
        AgenciesSortHelper agencySortHelper = new AgenciesSortHelper();
        sortAgenciesWithNoReportsToAgency(agencies, agencySortHelper);
        sortAgenciesWithReportsToAgencyThatHasBeenSorted(agencySortHelper);
        sortAnyUnsortedAgencies(agencySortHelper);
        return agencySortHelper.sortedAgencies;
    }

    private void sortAgenciesWithNoReportsToAgency(List<RassXmlAgencyEntry> agencies, AgenciesSortHelper agencySortHelper) {
        for (RassXmlAgencyEntry entry : agencies) {
            if (StringUtils.isBlank(entry.getReportsToAgencyNumber())) {
                agencySortHelper.agencyNumberInSortedList.add(entry.getNumber());
                agencySortHelper.sortedAgencies.add(entry);
            } else {
                agencySortHelper.unsortedAgencies.add(entry);
            }
        }
        LOG.debug("sortAgenciesWithNoReportsToAgency, number of angencies without reports to agencies: " + agencySortHelper.sortedAgencies.size());
    }

    private void sortAgenciesWithReportsToAgencyThatHasBeenSorted(AgenciesSortHelper agencySortHelper) {
        boolean isWeedingSuccessfull = true;
        int loopCount = 0;
        while (CollectionUtils.isNotEmpty(agencySortHelper.unsortedAgencies) && isWeedingSuccessfull) {
            LOG.debug("sortAgenciesWithReportsToAgencyThatHasBeenSorted. the loop count: " + loopCount);
            LOG.debug("sortAgenciesWithReportsToAgencyThatHasBeenSorted, number of agencies in the sorted list during sort loop: " + agencySortHelper.sortedAgencies.size());
            List<RassXmlAgencyEntry> newUnsortedAgencies = new ArrayList<RassXmlAgencyEntry>();
            
            for (RassXmlAgencyEntry entry : agencySortHelper.unsortedAgencies) {
                if (agencySortHelper.agencyNumberInSortedList.contains(entry.getReportsToAgencyNumber())) {
                    agencySortHelper.agencyNumberInSortedList.add(entry.getNumber());
                    agencySortHelper.sortedAgencies.add(entry);
                } else {
                    newUnsortedAgencies.add(entry);
                }
            }
            
            if (agencySortHelper.unsortedAgencies.size() == newUnsortedAgencies.size()) {
                isWeedingSuccessfull = false;
            }
            agencySortHelper.unsortedAgencies = newUnsortedAgencies;
            loopCount++;
        }
        
        LOG.debug("sortAgenciesWithReportsToAgencyThatHasBeenSorted, number of agencies in the sorted list after main sort loop: " + agencySortHelper.sortedAgencies.size());
    }

    private void sortAnyUnsortedAgencies(AgenciesSortHelper agencySortHelper) {
        if (CollectionUtils.isNotEmpty(agencySortHelper.unsortedAgencies)) {
            LOG.debug("sortAnyUnsortedAgencies, number of unsorted agencies: " + agencySortHelper.unsortedAgencies.size());
            sortAgenciesWithReportsToAgencyNotInRASSInput(agencySortHelper);
            LOG.debug("sortAnyUnsortedAgencies, number of unsorted agencies after finding unsorted parents: " + agencySortHelper.unsortedAgencies.size());
            if (CollectionUtils.isNotEmpty(agencySortHelper.unsortedAgencies)) {
                sortAgenciesWithReportsToAgencyThatHasBeenSorted(agencySortHelper);
                if (CollectionUtils.isNotEmpty(agencySortHelper.unsortedAgencies)) {
                    LOG.info("sortAnyUnsortedAgencies, number of unsorted agencies: " + agencySortHelper.unsortedAgencies.size());
                    LOG.info("sortAnyUnsortedAgencies, unsorted agencies: " + agencySortHelper.unsortedAgencies);
                    agencySortHelper.sortedAgencies.addAll(agencySortHelper.unsortedAgencies);
                }
            }
        } else {
            LOG.debug("sortAnyUnsortedAgencies, There were no unsorted agencies after main sort routine.");
        }
    }
    
    private void sortAgenciesWithReportsToAgencyNotInRASSInput(AgenciesSortHelper agencySortHelper) {
        List<RassXmlAgencyEntry> newUnsortedAgencies = new ArrayList<RassXmlAgencyEntry>();
        for (RassXmlAgencyEntry entry : agencySortHelper.unsortedAgencies) {
            if (isReportsToAgencyNumberAnAgencyInUnsortedAgencyList(entry.getReportsToAgencyNumber(), agencySortHelper.unsortedAgencies)) {
                newUnsortedAgencies.add(entry);
            } else {
                agencySortHelper.sortedAgencies.add(entry);
                agencySortHelper.agencyNumberInSortedList.add(entry.getNumber());
                
            }
        }
        agencySortHelper.unsortedAgencies = newUnsortedAgencies;
    }
    
    private boolean isReportsToAgencyNumberAnAgencyInUnsortedAgencyList(String reportsToAgencyNumber, List<RassXmlAgencyEntry> unsortedAgencies) {
        for (RassXmlAgencyEntry agencyEntry : unsortedAgencies) {
            if (StringUtils.equalsIgnoreCase(agencyEntry.getNumber(), reportsToAgencyNumber)) {
                return true;
            }
        }
        return false;
    }
        
    private class AgenciesSortHelper {
        public List<RassXmlAgencyEntry> sortedAgencies;
        public List<String> agencyNumberInSortedList;
        public List<RassXmlAgencyEntry> unsortedAgencies;
        
        AgenciesSortHelper() {
            sortedAgencies = new ArrayList<RassXmlAgencyEntry>();
            agencyNumberInSortedList = new ArrayList<String>();
            unsortedAgencies = new ArrayList<RassXmlAgencyEntry>();
        }
    }
    
    @Override
    public List<RassXmlFileParseResult> sortRassXmlFileParseResult(List<RassXmlFileParseResult> results) {
        return results;
    }

}
