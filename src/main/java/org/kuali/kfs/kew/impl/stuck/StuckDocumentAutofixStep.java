/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kew.impl.stuck;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * CU Customization: Backported this file from the 2021-04-08 financials patch to bring in the FINP-7470 changes.
 * This overlay should be removed when we upgrade to the 2021-04-08 financials patch.
 */
public class StuckDocumentAutofixStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    private StuckDocumentService stuckDocumentService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) {
        List<StuckDocumentIncident> incidents = findIncidentsToProcess();
        if (!incidents.isEmpty()) {
            LOG.info("Identified " + incidents.size() + " stuck documents to process.");
            LOG.info("Attempting to fix the following documents: " + incidents.stream()
                    .map(StuckDocumentIncident::getDocumentId)
                    .collect(Collectors.joining(", ")));
            List<String> incidentIds = incidents.stream()
                    .map(StuckDocumentIncident::getStuckDocumentIncidentId)
                    .collect(Collectors.toList());
            List<StuckDocumentIncident> stillStuck = stuckDocumentService.resolveIncidentsIfPossible(incidentIds);

            stillStuck.forEach(stillStuckIncident -> {
                final int fixAttemptCount =
                        stuckDocumentService.fixAttemptCount(stillStuckIncident.getStuckDocumentIncidentId());
                if (fixAttemptCount > autofixMaxAttempts()) {
                    LOG.info("Exceeded autofixMaxAttempts of " + autofixMaxAttempts() + " for doc id " +
                            stillStuckIncident.getDocumentId() + ". Marking stuck document incident as failure.");
                    stuckDocumentService.recordIncidentFailure(stillStuckIncident);
                } else {
                    LOG.info("Stuck document for doc id " + stillStuckIncident.getDocumentId() + " is still remaining, " +
                            "will try again.");
                    processIncident(stillStuckIncident);
                }
            });
        }
        return true;
    }

    private List<StuckDocumentIncident> findIncidentsToProcess() {
        List<StuckDocumentIncident> incidents = stuckDocumentService.recordNewStuckDocumentIncidents();
        incidents.addAll(stuckDocumentService.findIncidentsByStatus(StuckDocumentIncident.Status.PENDING));
        incidents.addAll(stuckDocumentService.findIncidentsByStatus(StuckDocumentIncident.Status.FIXING));

        return filterDuplicateIncidents(incidents);
    }

    private List<StuckDocumentIncident> filterDuplicateIncidents(List<StuckDocumentIncident> incidents) {
        Set<String> uniqueDocumentIds = new HashSet<>();
        return incidents.stream().filter(incident -> {
            if (uniqueDocumentIds.contains(incident.getDocumentId())) {
                return false;
            }
            uniqueDocumentIds.add(incident.getDocumentId());
            return true;
        }).collect(Collectors.toList());
    }

    private void processIncident(StuckDocumentIncident incident) {
        if (StuckDocumentIncident.Status.PENDING.name().equals(incident.getStatus())) {
            incident = stuckDocumentService.startFixingIncident(incident);
        }
        try {
            stuckDocumentService.tryToFix(incident);
        } catch (Throwable t) {
            // we catch and log here because we don't want one bad apple to ruin the whole bunch!
            LOG.error("Error occurred when attempting to fix stuck document incident for doc id " +
                    incident.getDocumentId(), t);
        }
    }

    private int autofixMaxAttempts() {
        final int defaultMaxAttempts = 2;
        final String maxAttemptsString = parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.WORKFLOW,
                KfsParameterConstants.ALL_COMPONENT,
                KFSParameterKeyConstants.MAX_ATTEMPTS,
                String.valueOf(defaultMaxAttempts)
        );

        return StringUtils.isNumeric(maxAttemptsString) ? Integer.parseInt(maxAttemptsString) : defaultMaxAttempts;
    }

    public void setStuckDocumentService(StuckDocumentService stuckDocumentService) {
        this.stuckDocumentService = stuckDocumentService;
    }
}
