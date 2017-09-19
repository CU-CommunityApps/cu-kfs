/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.batch.report;

import java.util.ArrayList;
import java.util.List;

public class AchUpdateSummary {

    private List<AchUpdateSummaryLine> approvedVendors;
    private List<AchUpdateSummaryLine> rejectedVendors;

    public AchUpdateSummary() {
        approvedVendors = new ArrayList<AchUpdateSummaryLine>();
        rejectedVendors = new ArrayList<AchUpdateSummaryLine>();
    }

    public List<AchUpdateSummaryLine> getApprovedVendors() {
        return approvedVendors;
    }

    public void setApprovedVendors(List<AchUpdateSummaryLine> approvedVendors) {
        this.approvedVendors = approvedVendors;
    }

    public List<AchUpdateSummaryLine> getRejectedVendors() {
        return rejectedVendors;
    }

    public void setRejectedVendors(List<AchUpdateSummaryLine> rejectedVendors) {
        this.rejectedVendors = rejectedVendors;
    }

}
