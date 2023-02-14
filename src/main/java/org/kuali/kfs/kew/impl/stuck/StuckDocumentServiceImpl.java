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
    public StuckDocumentIncident findIncident(String stuckDocumentIncidentId) {
        Assert.notNull(stuckDocumentIncidentId, "'stuckDocumentIncidentId' should not be null.");
        return businessObjectService.findBySinglePrimaryKey(StuckDocumentIncident.class, stuckDocumentIncidentId);
    }

    @Override
    public List<StuckDocumentIncident> findIncidents(List<String> stuckDocumentIncidentIds) {
        Assert.notNull(stuckDocumentIncidentIds, "'stuckDocumentIncidentId' should not be null.");
        List<StuckDocumentIncident> incidents = new ArrayList<>(stuckDocumentIncidentIds.size());
        for (String stuckDocumentIncidentId : stuckDocumentIncidentIds) {
            StuckDocumentIncident incident = findIncident(stuckDocumentIncidentId);
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
    public List<StuckDocumentIncident> findIncidentsByStatus(StuckDocumentIncident.Status status) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("status", status.name());
        return new ArrayList<>(businessObjectService.findMatchingOrderBy(StuckDocumentIncident.class, fieldValues,
                "startDate", false));
    }

    @Override
    public List<StuckDocumentIncident> recordNewStuckDocumentIncidents() {
        List<String> newStuckDocuments = getStuckDocumentDao().identifyNewStuckDocuments();
        return newStuckDocuments.stream()
                .map(documentId -> businessObjectService.save(StuckDocumentIncident.startNewIncident(documentId)))
                .collect(Collectors.toList());
    }

    @Override
    public StuckDocumentFixAttempt recordNewIncidentFixAttempt(StuckDocumentIncident stuckDocumentIncident) {
        Assert.notNull(stuckDocumentIncident, "'stuckDocumentIncident' should not be null.");
        StuckDocumentFixAttempt auditEntry = new StuckDocumentFixAttempt();
        auditEntry.setStuckDocumentIncidentId(stuckDocumentIncident.getStuckDocumentIncidentId());
        auditEntry.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return businessObjectService.save(auditEntry);
    }

    @Override
    public List<StuckDocumentFixAttempt> findAllFixAttempts(String stuckDocumentIncidentId) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("stuckDocumentIncidentId", stuckDocumentIncidentId);
        return new ArrayList<>(businessObjectService.findMatching(StuckDocumentFixAttempt.class, fieldValues));
    }

    @Override
    public int fixAttemptCount(String stuckDocumentIncidentId) {
        return findAllFixAttempts(stuckDocumentIncidentId).size();
    }

    @Override
    public List<StuckDocumentIncident> resolveIncidentsIfPossible(List<String> stuckDocumentIncidentIds) {
        Assert.notNull(stuckDocumentIncidentIds, "'stuckDocumentIncidentId' should not be null.");
        List<StuckDocumentIncident> stuckIncidents = identifyStillStuckDocuments(stuckDocumentIncidentIds);
        List<String> stuckIncidentIds =
                stuckIncidents.stream().map(StuckDocumentIncident::getStuckDocumentIncidentId).collect(
                        Collectors.toList());
        // let's find the ones that aren't stuck so that we can resolve them
        List<String> notStuckIncidentIds = new ArrayList<>(stuckDocumentIncidentIds);
        notStuckIncidentIds.removeAll(stuckIncidentIds);
        if (!notStuckIncidentIds.isEmpty()) {
            List<StuckDocumentIncident> notStuckIncidents = findIncidents(notStuckIncidentIds);
            notStuckIncidents.forEach(this::resolve);
        }
        return stuckIncidents;
    }

    private List<StuckDocumentIncident> identifyStillStuckDocuments(List<String> incidentIds) {
        return incidentIds.stream().map(this::findIncident)
                .filter(incident -> stuckDocumentDao.isStuck(incident.getDocumentId()))
                .collect(Collectors.toList());
    }

    protected StuckDocumentIncident resolve(StuckDocumentIncident stuckDocumentIncident) {
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
    public StuckDocumentIncident startFixingIncident(StuckDocumentIncident stuckDocumentIncident) {
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

    protected void notifyIncidentFailure(StuckDocumentIncident stuckDocumentIncident) {
        if (failureNotificationEnabled()) {
            List<StuckDocumentFixAttempt> attempts =
                    findAllFixAttempts(stuckDocumentIncident.getStuckDocumentIncidentId());
            notifier.notifyIncidentFailure(stuckDocumentIncident, attempts);
        }
    }

    @Override
    public void tryToFix(StuckDocumentIncident incident) {
        recordNewIncidentFixAttempt(incident);
        String docId = incident.getDocumentId();
        DocumentRefreshQueue drq = KewApiServiceLocator.getDocumentRequeuerService(docId, 0);
        drq.refreshDocument(docId, "Document was requeued from the Stuck Document Service.");
    }

    protected StuckDocumentDao getStuckDocumentDao() {
        return stuckDocumentDao;
    }

    @Required
    public void setStuckDocumentDao(StuckDocumentDao stuckDocumentDao) {
        this.stuckDocumentDao = stuckDocumentDao;
    }

    protected StuckDocumentNotifier getNotifier() {
        return notifier;
    }

    @Required
    public void setNotifier(StuckDocumentNotifier notifier) {
        this.notifier = notifier;
    }

    protected boolean failureNotificationEnabled() {
        return parameterService.getParameterValueAsBoolean(StuckDocumentAutofixStep.class,
                KFSParameterKeyConstants.ENABLED_IND, Boolean.FALSE);
    }

    @Required
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    @Required
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
