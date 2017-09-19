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

public class VendorUpdateSummary {

    private List<VendorUpdateSummaryLine> vendorsCreated;
    private List<VendorUpdateSummaryLine> vendorsDirectUpdate;
    private List<VendorUpdateSummaryLine> vendorsRejected;

    public VendorUpdateSummary() {
        vendorsCreated = new ArrayList<VendorUpdateSummaryLine>();
        vendorsDirectUpdate = new ArrayList<VendorUpdateSummaryLine>();
        vendorsRejected = new ArrayList<VendorUpdateSummaryLine>();
    }

    public List<VendorUpdateSummaryLine> getVendorsCreated() {
        return vendorsCreated;
    }

    public void setVendorsCreated(List<VendorUpdateSummaryLine> vendorsCreated) {
        this.vendorsCreated = vendorsCreated;
    }

    public List<VendorUpdateSummaryLine> getVendorsDirectUpdate() {
        return vendorsDirectUpdate;
    }

    public void setVendorsDirectUpdate(List<VendorUpdateSummaryLine> vendorsDirectUpdate) {
        this.vendorsDirectUpdate = vendorsDirectUpdate;
    }

    public List<VendorUpdateSummaryLine> getVendorsRejected() {
        return vendorsRejected;
    }

    public void setVendorsRejected(List<VendorUpdateSummaryLine> vendorsRejected) {
        this.vendorsRejected = vendorsRejected;
    }

}
