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
package org.kuali.kfs.kew.web;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kew.impl.stuck.StuckDocument;
import org.kuali.kfs.kew.impl.stuck.StuckDocumentFixAttempt;
import org.kuali.kfs.kew.impl.stuck.StuckDocumentIncident;
import org.kuali.kfs.kew.impl.stuck.StuckDocumentService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kns.web.struts.action.KualiAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/*
 * CU Customization: Backported this file from the 2021-04-08 financials patch to bring in the FINP-7470 changes.
 * This overlay should be removed when we upgrade to the 2021-04-08 financials patch.
 */
public class StuckDocumentsAction extends KualiAction {

    public ActionForward report(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        List<StuckDocument> stuckDocuments = getStuckDocumentService().findAllStuckDocuments();
        request.setAttribute("stuckDocuments", stuckDocuments);
        return mapping.findForward("report");
    }

    public ActionForward autofixReport(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        StuckDocumentService stuckDocumentService = getStuckDocumentService();
        StuckDocumentsForm form = (StuckDocumentsForm) actionForm;
        StuckDocumentsForm.Status selectedStatus = form.getSelectedStatus();
        List<StuckDocumentIncident> incidents;
        if (selectedStatus == null || selectedStatus.getValue().equals("All")) {
            incidents = stuckDocumentService.findAllIncidents();
        } else {
            incidents = stuckDocumentService.findIncidentsByStatus(
                    StuckDocumentIncident.Status.valueOf(selectedStatus.getValue()));
        }
        List<IncidentHistory> history = incidents.stream().map(incident -> {
            List<StuckDocumentFixAttempt> attempts =
                    stuckDocumentService.findAllFixAttempts(incident.getStuckDocumentIncidentId());
            String documentTypeLabel = getDocumentTypeService().findByDocumentId(incident.getDocumentId()).getLabel();
            return new IncidentHistory(incident, attempts, documentTypeLabel);
        }).collect(Collectors.toList());
        request.setAttribute("history", history);
        return mapping.findForward("autofixReport");
    }

    private StuckDocumentService getStuckDocumentService() {
        return KEWServiceLocator.getStuckDocumentService();
    }

    private DocumentTypeService getDocumentTypeService() {
        return KEWServiceLocator.getDocumentTypeService();
    }

    public static class IncidentHistory {

        private final StuckDocumentIncident incident;
        private final List<StuckDocumentFixAttempt> attempts;
        private final String documentTypeLabel;

        IncidentHistory(StuckDocumentIncident incident, List<StuckDocumentFixAttempt> attempts,
                String documentTypeLabel) {
            this.incident = incident;
            this.attempts = attempts;
            this.documentTypeLabel = documentTypeLabel;
        }

        public String getDocumentId() {
            return incident.getDocumentId();
        }

        public String getStartDate() {
            return incident.getStartDate().toString();
        }

        public String getEndDate() {
            if (incident.getEndDate() == null) {
                return "";
            }
            return incident.getEndDate().toString();
        }

        public String getStatus() {
            return incident.getStatus();
        }

        public String getFixAttempts() {
            return attempts.stream().
                    map(attempt -> attempt.getTimestamp().toString()).
                    collect(Collectors.joining(", "));
        }

        public String getDocumentTypeLabel() {
            return documentTypeLabel;
        }
    }
}
