/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.document.DocumentRefreshQueue;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CU Customization: Backported the FINP-9050 changes into this file, adjusting for compatibility as needed.
 * This overlay can be removed when we upgrade to the 2023-02-08 financials patch.
 */
public class StuckDocumentServiceImpl implements StuckDocumentService {

    private StuckDocumentDao stuckDocumentDao;
    private StuckDocumentNotifier notifier;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;

    @Override
    public List<StuckDocument> findAllStuckDocuments() {
        return getStuckDocumentDao().findAllStuckDocuments();
    }

    @Override
    public StuckDocumentIncident findIncident(final String stuckDocumentIncidentId) {
        Assert.notNull(stuckDocumentIncidentId, "'stuckDocumentIncidentId' should not be null.");
        return businessObjectService.findBySinglePrimaryKey(StuckDocumentIncident.class, stuckDocumentIncidentId);
    }

    @Override
    public List<StuckDocumentIncident> findIncidents(final List<String> stuckDocumentIncidentIds) {
        Assert.notNull(stuckDocumentIncidentIds, "'stuckDocumentIncidentId' should not be null.");
        final List<StuckDocumentIncident> incidents = new ArrayList<>(stuckDocumentIncidentIds.size());
        for (final String stuckDocumentIncidentId : stuckDocumentIncidentIds) {
            final StuckDocumentIncident incident = findIncident(stuckDocumentIncidentId);
            if (incident != null) {
                incidents.add(incident);
            }
        }
        return incidents;
    }

    @Override
    public List<StuckDocumentIncident> findAllIncidents() {
        return new ArrayList<>(businessObjectService.findAllOrderBy(StuckDocumentIncident.class, "startDate", false));
    }

    @Override
    public List<StuckDocumentIncident> findIncidentsByStatus(final StuckDocumentIncident.Status status) {
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("status", status.name());
        return new ArrayList<>(businessObjectService.findMatchingOrderBy(StuckDocumentIncident.class, fieldValues,
                "startDate", false));
    }

    @Override
    public List<StuckDocumentIncident> recordNewStuckDocumentIncidents() {
        final List<String> newStuckDocuments = getStuckDocumentDao().identifyNewStuckDocuments();
        return newStuckDocuments.stream()
                .map(documentId -> businessObjectService.save(StuckDocumentIncident.startNewIncident(documentId)))
                .collect(Collectors.toList());
    }

    @Override
    public StuckDocumentFixAttempt recordNewIncidentFixAttempt(final StuckDocumentIncident stuckDocumentIncident) {
        Assert.notNull(stuckDocumentIncident, "'stuckDocumentIncident' should not be null.");
        final StuckDocumentFixAttempt auditEntry = new StuckDocumentFixAttempt();
        auditEntry.setStuckDocumentIncidentId(stuckDocumentIncident.getStuckDocumentIncidentId());
        auditEntry.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return businessObjectService.save(auditEntry);
    }

    @Override
    public List<StuckDocumentFixAttempt> findAllFixAttempts(final String stuckDocumentIncidentId) {
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("stuckDocumentIncidentId", stuckDocumentIncidentId);
        return new ArrayList<>(businessObjectService.findMatching(StuckDocumentFixAttempt.class, fieldValues));
    }

    @Override
    public int fixAttemptCount(final String stuckDocumentIncidentId) {
        return findAllFixAttempts(stuckDocumentIncidentId).size();
    }

    @Override
    public List<StuckDocumentIncident> resolveIncidentsIfPossible(final List<String> stuckDocumentIncidentIds) {
        Assert.notNull(stuckDocumentIncidentIds, "'stuckDocumentIncidentId' should not be null.");
        final List<StuckDocumentIncident> stuckIncidents = identifyStillStuckDocuments(stuckDocumentIncidentIds);
        final List<String> stuckIncidentIds =
                stuckIncidents.stream().map(StuckDocumentIncident::getStuckDocumentIncidentId).collect(
                        Collectors.toList());
        // let's find the ones that aren't stuck so that we can resolve them
        final List<String> notStuckIncidentIds = new ArrayList<>(stuckDocumentIncidentIds);
        notStuckIncidentIds.removeAll(stuckIncidentIds);
        if (!notStuckIncidentIds.isEmpty()) {
            final List<StuckDocumentIncident> notStuckIncidents = findIncidents(notStuckIncidentIds);
            notStuckIncidents.forEach(this::resolve);
        }
        return stuckIncidents;
    }

    private List<StuckDocumentIncident> identifyStillStuckDocuments(final List<String> incidentIds) {
        return incidentIds.stream().map(this::findIncident)
                .filter(incident -> stuckDocumentDao.isStuck(incident.getDocumentId()))
                .collect(Collectors.toList());
    }

    protected StuckDocumentIncident resolve(final StuckDocumentIncident stuckDocumentIncident) {
        Assert.notNull(stuckDocumentIncident, "'stuckDocumentIncident' should not be null.");
        if (stuckDocumentIncident.getStatus().equals(StuckDocumentIncident.Status.PENDING.name())) {
            // if it was pending, the document unstuck itself, let's get rid of it's incident since it's just noise
            businessObjectService.delete(stuckDocumentIncident);
            return stuckDocumentIncident;
        } else {
            stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FIXED.name());
            stuckDocumentIncident.setEndDate(new Timestamp(System.currentTimeMillis()));
            return businessObjectService.save(stuckDocumentIncident);
        }
    }

    @Override
    public StuckDocumentIncident startFixingIncident(final StuckDocumentIncident stuckDocumentIncident) {
        Assert.notNull(stuckDocumentIncident, "'stuckDocumentIncident' should not be null.");
        stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FIXING.name());
        return businessObjectService.save(stuckDocumentIncident);
    }

    @Override
    public StuckDocumentIncident recordIncidentFailure(StuckDocumentIncident stuckDocumentIncident) {
        Assert.notNull(stuckDocumentIncident, "'stuckDocumentIncident' should not be null.");
        stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FAILED.name());
        stuckDocumentIncident.setEndDate(new Timestamp(System.currentTimeMillis()));
        stuckDocumentIncident = businessObjectService.save(stuckDocumentIncident);
        notifyIncidentFailure(stuckDocumentIncident);
        return stuckDocumentIncident;
    }

    protected void notifyIncidentFailure(final StuckDocumentIncident stuckDocumentIncident) {
        if (failureNotificationEnabled()) {
            final List<StuckDocumentFixAttempt> attempts =
                    findAllFixAttempts(stuckDocumentIncident.getStuckDocumentIncidentId());
            notifier.notifyIncidentFailure(stuckDocumentIncident, attempts);
        }
    }

    @Override
    public void tryToFix(final StuckDocumentIncident incident) {
        recordNewIncidentFixAttempt(incident);
        final String docId = incident.getDocumentId();
        final DocumentRefreshQueue drq = KewApiServiceLocator.getDocumentRequeuerService(docId, 0);
        drq.refreshDocument(docId, "Document was requeued from the Stuck Document Service.");
    }

    protected StuckDocumentDao getStuckDocumentDao() {
        return stuckDocumentDao;
    }

    @Required
    public void setStuckDocumentDao(final StuckDocumentDao stuckDocumentDao) {
        this.stuckDocumentDao = stuckDocumentDao;
    }

    protected StuckDocumentNotifier getNotifier() {
        return notifier;
    }

    @Required
    public void setNotifier(final StuckDocumentNotifier notifier) {
        this.notifier = notifier;
    }

    protected boolean failureNotificationEnabled() {
        return parameterService.getParameterValueAsBoolean(StuckDocumentAutofixStep.class,
                KFSParameterKeyConstants.ENABLED_IND, Boolean.FALSE);
    }

    @Required
    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    @Required
    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
