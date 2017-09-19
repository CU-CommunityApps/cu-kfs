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

import java.util.List;

import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksSupplierUploadDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksUpdateVendorStatus;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public interface PaymentWorksWebService {

    /**
     * Retrieves new vendor requests from PaymentWorks where the status is
     * Pending (0)
     *
     * @return
     */
    public List<PaymentWorksNewVendorDTO> getPendingNewVendorRequestsFromPaymentWorks();

    /**
     * Retrieves company vendor updates from PaymentWorks where the status is
     * Pending (0)
     *
     * @return
     */
    public List<PaymentWorksVendorUpdatesDTO> getPendingCompanyVendorUpdatesFromPaymentWorks();

    /**
     * Retrieves address vendor updates from PaymentWorks where the status is
     * Pending (0)
     *
     * @return
     */
    public List<PaymentWorksVendorUpdatesDTO> getPendingAddressVendorUpdatesFromPaymentWorks();

    /**
     * Retrieves ach updates from PaymentWorks where the status is Pending (0)
     *
     * @return
     */
    public List<PaymentWorksVendorUpdatesDTO> getPendingAchUpdatesFromPaymentWorks();

    /**
     * Retrieves a single vendor detail record from PaymentWorks
     *
     * @return
     */
    public PaymentWorksNewVendorDetailDTO getVendorDetailFromPaymentWorks(String newVendorRequestId);

    /**
     * Uploads supplier csv created from input DTO to PaymentWorks
     *
     * @return
     */
    public boolean uploadSuppliers(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadDTO);

    /**
     * Updates new vendor request status (processed or rejected) in PaymentWorks
     */
    public void updateNewVendorStatusInPaymentWorks(List<PaymentWorksNewVendorUpdateVendorStatus> paymentWorksUpdateNewVendorStatus);

    /**
     * Updates new vendor update status (processed or rejected) in PaymentWorks
     */
    public void updateNewVendorUpdatesStatusInPaymentWorks(List<PaymentWorksUpdateVendorStatus> paymentWorksUpdateVendorStatus);

    /**
     * Updates existing vendor update status (processed or rejected) in
     * PaymentWorks
     */
    public void updateExistingVendorUpdatesStatusInPaymentWorks(List<PaymentWorksUpdateVendorStatus> paymentWorksUpdateVendorStatus);

}
