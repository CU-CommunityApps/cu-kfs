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

public class NewVendorSummary {

    private List<NewVendorSummaryLine> approvedVendors;
    private List<NewVendorSummaryLine> rejectedVendors;

    public NewVendorSummary() {
        approvedVendors = new ArrayList<NewVendorSummaryLine>();
        rejectedVendors = new ArrayList<NewVendorSummaryLine>();
    }

    public List<NewVendorSummaryLine> getApprovedVendors() {
        return approvedVendors;
    }

    public void setApprovedVendors(List<NewVendorSummaryLine> approvedVendors) {
        this.approvedVendors = approvedVendors;
    }

    public List<NewVendorSummaryLine> getRejectedVendors() {
        return rejectedVendors;
    }

    public void setRejectedVendors(List<NewVendorSummaryLine> rejectedVendors) {
        this.rejectedVendors = rejectedVendors;
    }

}
