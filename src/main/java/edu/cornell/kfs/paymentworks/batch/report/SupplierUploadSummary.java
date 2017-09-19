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

public class SupplierUploadSummary {

    private List<SupplierUploadSummaryLine> paymentWorksNewVendors;
    private List<SupplierUploadSummaryLine> kfsNewVendors;
    private List<SupplierUploadSummaryLine> vendorUpdates;
    private List<SupplierUploadSummaryLine> newVendorDisapproved;
    private List<SupplierUploadSummaryLine> uploadedVendors;

    public SupplierUploadSummary() {
        paymentWorksNewVendors = new ArrayList<SupplierUploadSummaryLine>();
        kfsNewVendors = new ArrayList<SupplierUploadSummaryLine>();
        vendorUpdates = new ArrayList<SupplierUploadSummaryLine>();
        newVendorDisapproved = new ArrayList<SupplierUploadSummaryLine>();
        uploadedVendors = new ArrayList<SupplierUploadSummaryLine>();

    }

    public List<SupplierUploadSummaryLine> getPaymentWorksNewVendors() {
        return paymentWorksNewVendors;
    }

    public void setPaymentWorksNewVendors(List<SupplierUploadSummaryLine> paymentWorksNewVendors) {
        this.paymentWorksNewVendors = paymentWorksNewVendors;
    }

    public List<SupplierUploadSummaryLine> getKfsNewVendors() {
        return kfsNewVendors;
    }

    public void setKfsNewVendors(List<SupplierUploadSummaryLine> kfsNewVendors) {
        this.kfsNewVendors = kfsNewVendors;
    }

    public List<SupplierUploadSummaryLine> getNewVendorDisapproved() {
        return newVendorDisapproved;
    }

    public void setNewVendorDisapproved(List<SupplierUploadSummaryLine> newVendorDisapproved) {
        this.newVendorDisapproved = newVendorDisapproved;
    }

    public List<SupplierUploadSummaryLine> getVendorUpdates() {
        return vendorUpdates;
    }

    public void setVendorUpdates(List<SupplierUploadSummaryLine> vendorUpdates) {
        this.vendorUpdates = vendorUpdates;
    }

    public List<SupplierUploadSummaryLine> getUploadedVendors() {
        return uploadedVendors;
    }

    public void setUploadedVendors(List<SupplierUploadSummaryLine> uploadedVendors) {
        this.uploadedVendors = uploadedVendors;
    }

}
