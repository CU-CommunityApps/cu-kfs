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
package edu.cornell.kfs.paymentworks.service;

import java.util.Collection;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public interface PaymentWorksVendorService {

    /**
     * Saves a new PaymentWorks vendor record in KFS
     *
     * @return
     */
    public PaymentWorksVendor savePaymentWorksVendorRecord(PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO);

    public PaymentWorksVendor savePaymentWorksVendorRecord(PaymentWorksVendorUpdatesDTO paymentWorksVendorUpdatesDTO, String processStatus, String transactionType);

    /**
     * Saves a KFS vendor record in PaymentWorks new vendor table
     *
     * @return
     */
    public PaymentWorksVendor savePaymentWorksVendorRecord(VendorDetail vendorDetail, String documentNumber, String transactionType);

    /**
     * Updates a new vendor request in KFS
     *
     */
    public PaymentWorksVendor updatePaymentWorksVendor(PaymentWorksVendor paymentWorksNewVendor);

    /**
     * Updates a new vendor request in KFS for status by document number
     *
     * @param documentNumber
     * @param processStatus
     */
    public void updatePaymentWorksVendorProcessStatusByDocumentNumber(String documentNumber, String processStatus);

    /**
     * Checks if the vendor request is already in the staging table
     *
     * @param newVendorRequestId
     * @return
     */
    public boolean isExistingPaymentWorksVendor(String newVendorRequestId, String transactionType);

    /**
     * checks for existing new vendor record by document number
     *
     * @param documentNumber
     * @return
     */
    public boolean isExistingPaymentWorksVendorByDocumentNumber(String documentNumber);

    /**
     * retrieves existing new vendor record by document numbers
     *
     * @param documentNumber
     * @return
     */
    public PaymentWorksVendor getPaymentWorksVendorByDocumentNumber(String documentNumber);

    /**
     * Retrieves PaymentWorks vendor records in KFS by process and request
     * status and transaction type
     *
     * @param processStatus
     * @param requestStatus
     * @return
     */
    public Collection<PaymentWorksVendor> getPaymentWorksVendorRecords(String processStatus, String requestStatus, String transactionType);

    public boolean isVendorUpdateEligibleForRouting(PaymentWorksVendor paymentWorksVendor);

}
