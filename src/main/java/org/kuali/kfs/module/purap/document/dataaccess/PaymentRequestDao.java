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
package org.kuali.kfs.module.purap.document.dataaccess;

import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.util.VendorGroupingHelper;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.sql.Date;
import java.util.Collection;
import java.util.List;

/**
 * CU customization: backport FINP-8270. This was backported to the 1/28/2021
 * version of this file. This change can be removed with the 3/2/2022 upgrade.
 */
public interface PaymentRequestDao {

    /**
     * @param campusCode                      limit results to a single campus
     * @param paymentRequestIdentifier        Payment Request Identifier (can be null)
     * @param purchaseOrderIdentifier         PO Identifier (can be null)
     * @param vendorHeaderGeneratedIdentifier Vendor Header ID
     * @param vendorDetailAssignedIdentifier  Vendor Detail ID
     * @param currentSqlDateMidnight          current SQL date midnight
     * @return list of all the payment requests that need to be extracted that match a credit memo.
     */
    List<PaymentRequestDocument> getPaymentRequestsToExtract(String campusCode, Integer paymentRequestIdentifier,
            Integer purchaseOrderIdentifier, Integer vendorHeaderGeneratedIdentifier,
            Integer vendorDetailAssignedIdentifier, Date currentSqlDateMidnight);

    /**
     * @param onlySpecialPayments true only include special payments, False include all
     * @param campusCode           if not null, limit results to a single campus
     * @return Collection of payment requests that need to be extracted to PDP.
     */
    List<PaymentRequestDocument> getPaymentRequestsToExtract(boolean onlySpecialPayments, String campusCode,
            Date onOrBeforePaymentRequestPayDate);

    /**
     * @param campusCode                      limit results to a single campus
     * @param vendor                          Vendor Header ID, Vendor Detail ID, Country, Zip Code
     * @param onOrBeforePaymentRequestPayDate only payment requests with a pay date on or before this value will be
     *                                        returned in the iterator
     * @return list of all the payment requests that need to be extracted that match a credit memo.
     */
    Collection<PaymentRequestDocument> getPaymentRequestsToExtractForVendor(String campusCode,
            VendorGroupingHelper vendor, Date onOrBeforePaymentRequestPayDate);

    /**
     * @param campusCode campus code
     * @return Collection of payment requests that are marked immediate that need to be extracted to PDP.
     */
    List<PaymentRequestDocument> getImmediatePaymentRequestsToExtract(String campusCode);

    /**
     * Get all payment request documents that are eligible for auto-approval. Whether or not a document is eligible
     * for auto-approval is determined according to whether or not the document total is below a pre-determined
     * minimum amount. This amount is derived from the accounts, charts and/or organizations associated with a given
     * document. If no minimum amount can be determined from chart associations a default minimum specified as a
     * system parameter is used to determine the minimum amount threshold.
     *
     * @param todayAtMidnight
     * @return an Iterator over all payment request documents eligible for automatic approval
     */
    List<String> getEligibleForAutoApproval(Date todayAtMidnight);

    /**
     * @param id PaymentRequest Id
     * @return PaymentRequest for the supplied id or null if not found
     */
    String getDocumentNumberByPaymentRequestId(Integer id);

    /**
     * @param id purchase order id
     * @return list of document numbers matching the provided purchase order id.
     */
    List<String> getDocumentNumbersByPurchaseOrderId(Integer id);

    /**
     * Retrieves a list of Payment Requests with the given vendor id and invoice number.
     *
     * @param vendorHeaderGeneratedId header id of the vendor id
     * @param vendorDetailAssignedId  detail id of the vendor id
     * @param invoiceNumber           invoice number as entered by AP
     * @return List of Payment Requests.
     */
    List getActivePaymentRequestsByVendorNumberInvoiceNumber(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId, String invoiceNumber);

    /**
     * @param vendorHeaderGeneratedId header id of the vendor id
     * @param vendorDetailAssignedId  detail id of the vendor id
     * @return List of Payment Requests with the given vendor id and invoice number.
     */
    /*
     * CU customization: backport FINP-8270
     */
    List<PaymentRequestView> getActivePaymentRequestsByVendorNumber(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId);

    /**
     * @param poId          purchase order ID
     * @param invoiceAmount amount of the invoice as entered by AP
     * @param invoiceDate   date of the invoice as entered by AP
     * @return List of Payment Requests with the given PO Id, invoice amount, and invoice date.
     */
    List<PaymentRequestDocument> getActivePaymentRequestsByPOIdInvoiceAmountInvoiceDate(Integer poId,
            KualiDecimal invoiceAmount, Date invoiceDate);

    /**
     * @param vendorHeaderGeneratedId header id of the vendor id
     * @param vendorDetailAssignedId  detail id of the vendor id
     * @param invoiceAmount amount of the invoice as entered by AP
     * @param invoiceDate   date of the invoice as entered by AP
     * @return List of Payment Requests with the given invoice amount and invoice date.
     */
    List<PaymentRequestDocument> getActivePaymentRequestsByInvoiceAmountInvoiceDate(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId, KualiDecimal invoiceAmount, Date invoiceDate);

    /**
     * @param purchaseOrderId
     * @return the number of active payment requests for a purchase order by status code. Active being defined as
     *         being enroute and before final, and workflow status not canceled or exception. The issue is that a
     *         status of vendor_tax_review may not mean that it's in review, but could be in final (as there isn't a
     *         final status code for payment request).
     */
    int getActivePaymentRequestCountForPurchaseOrder(Integer purchaseOrderId);

    /**
     * @return all payment request which are waiting in receiving status queue
     */
    List<PaymentRequestDocument> getPaymentRequestInReceivingStatus();

}
