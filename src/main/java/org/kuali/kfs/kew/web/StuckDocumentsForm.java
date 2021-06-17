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

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.impl.stuck.StuckDocumentIncident;
import org.kuali.kfs.kns.web.struts.form.KualiForm;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/*
 * CU Customization: Backported this file from the 2021-04-08 financials patch to bring in the FINP-7470 changes.
 * This overlay should be removed when we upgrade to the 2021-04-08 financials patch.
 */
public class StuckDocumentsForm extends KualiForm {

    private List<Status> statuses;

    public StuckDocumentsForm() {
        this.statuses = new ArrayList<>();
        this.statuses.add(new Status("All", false));
        for (StuckDocumentIncident.Status status : StuckDocumentIncident.Status.values()) {
            this.statuses.add(new Status(status.name(), false));
        }
    }

    @Override
    public void populate(HttpServletRequest request) {
        super.populate(request);
        // determine if they set the status filter
        String statusFilter = request.getParameter("statusFilter");
        if (StringUtils.isNotBlank(statusFilter)) {
            for (Status status : this.statuses) {
                if (status.getValue().equals(statusFilter)) {
                    status.setSelected(true);
                    break;
                }
            }
        }
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    public Status getSelectedStatus() {
        for (Status status : statuses) {
            if (status.isSelected()) {
                return status;
            }
        }
        return null;
    }

    public static class Status {
        private String value;
        private boolean selected;

        public Status(String value, boolean selected) {
            this.value = value;
            this.selected = selected;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

}
