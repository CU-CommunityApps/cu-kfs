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

public class SupplierUploadSummaryLine {

    private String vendorRequestId;
    private String vendorName;
    private String documentNumber;
    private String vendorNumber;
    private boolean sendToPaymentWorks;

    public String getVendorRequestId() {
        return vendorRequestId;
    }

    public void setVendorRequestId(String vendorRequestId) {
        this.vendorRequestId = vendorRequestId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getVendorNumber() {
        return vendorNumber;
    }

    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    public boolean isSendToPaymentWorks() {
        return sendToPaymentWorks;
    }

    public void setSendToPaymentWorks(boolean sendToPaymentWorks) {
        this.sendToPaymentWorks = sendToPaymentWorks;
    }

}
