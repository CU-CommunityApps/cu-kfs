/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.module.purap.document.dataaccess.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.dataaccess.PaymentRequestDao;
import org.kuali.kfs.module.purap.util.VendorGroupingHelper;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * CU customization: backport FINP-8270 and FINP-8283. These were backported to the 1/28/2021
 * version of this file. These changes can be removed with the 3/9/2022 upgrade.
 */
@Transactional
public class PaymentRequestDaoOjb extends PlatformAwareDaoBaseOjb implements PaymentRequestDao {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * The special payments query should be this: select * from pur.ap_pmt_rqst_t where pmt_rqst_stat_cd in
     * ('AUTO', 'DPTA') and prcs_cmp_cd = ? and pmt_extrt_ts is NULL and pmt_hld_ind = 'N' and
     * ( ( ( pmt_spcl_handlg_instrc_ln1_txt is not NULL or pmt_spcl_handlg_instrc_ln2_txt is not NULL or
     * pmt_spcl_handlg_instrc_ln3_txt is not NULL or pmt_att_ind = 'Y') and trunc (pmt_rqst_pay_dt) <= trunc (sysdate))
     * or IMD_PMT_IND = 'Y')})
     */
    @Override
    public List<PaymentRequestDocument> getPaymentRequestsToExtract(boolean onlySpecialPayments, String campusCode,
            Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsToExtract() started");

        Criteria criteria = new Criteria();
        if (campusCode != null) {
            criteria.addEqualTo("processingCampusCode", campusCode);
        }
        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("holdIndicator", Boolean.FALSE);

        if (onlySpecialPayments) {
            Criteria a = new Criteria();

            Criteria c1 = new Criteria();
            c1.addNotNull("specialHandlingInstructionLine1Text");
            Criteria c2 = new Criteria();
            c2.addNotNull("specialHandlingInstructionLine2Text");
            Criteria c3 = new Criteria();
            c3.addNotNull("specialHandlingInstructionLine3Text");
            Criteria c4 = new Criteria();
            c4.addEqualTo("paymentAttachmentIndicator", Boolean.TRUE);

            c1.addOrCriteria(c2);
            c1.addOrCriteria(c3);
            c1.addOrCriteria(c4);

            a.addAndCriteria(c1);
            a.addLessOrEqualThan("paymentRequestPayDate", onOrBeforePaymentRequestPayDate);

            Criteria c5 = new Criteria();
            c5.addEqualTo("immediatePaymentIndicator", Boolean.TRUE);
            c5.addOrCriteria(a);

            criteria.addAndCriteria(a);
        } else {
            Criteria c1 = new Criteria();
            c1.addLessOrEqualThan("paymentRequestPayDate", onOrBeforePaymentRequestPayDate);

            Criteria c2 = new Criteria();
            c2.addEqualTo("immediatePaymentIndicator", Boolean.TRUE);

            c1.addOrCriteria(c2);
            criteria.addAndCriteria(c1);
        }

        return (List<PaymentRequestDocument>) getPersistenceBrokerTemplate().getCollectionByQuery(
                new QueryByCriteria(PaymentRequestDocument.class, criteria));
    }

    @Override
    @Deprecated
    public List<PaymentRequestDocument> getPaymentRequestsToExtract(String campusCode,
            Integer paymentRequestIdentifier, Integer purchaseOrderIdentifier,
            Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier,
            Date currentSqlDateMidnight) {
        LOG.debug("getPaymentRequestsToExtract() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo("processingCampusCode", campusCode);
        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("holdIndicator", Boolean.FALSE);

        Criteria c1 = new Criteria();
        c1.addLessOrEqualThan("paymentRequestPayDate", currentSqlDateMidnight);

        Criteria c2 = new Criteria();
        c2.addEqualTo("immediatePaymentIndicator", Boolean.TRUE);

        c1.addOrCriteria(c2);
        criteria.addAndCriteria(c1);

        if (paymentRequestIdentifier != null) {
            criteria.addEqualTo("purapDocumentIdentifier", paymentRequestIdentifier);
        }
        if (purchaseOrderIdentifier != null) {
            criteria.addEqualTo("purchaseOrderIdentifier", purchaseOrderIdentifier);
        }
        criteria.addEqualTo("vendorHeaderGeneratedIdentifier", vendorHeaderGeneratedIdentifier);
        criteria.addEqualTo("vendorDetailAssignedIdentifier", vendorDetailAssignedIdentifier);

        return (List<PaymentRequestDocument>) getPersistenceBrokerTemplate().getIteratorByQuery(
                new QueryByCriteria(PaymentRequestDocument.class, criteria));
    }

    @Override
    public List<PaymentRequestDocument> getImmediatePaymentRequestsToExtract(String campusCode) {
        LOG.debug("getImmediatePaymentRequestsToExtract() started");

        Criteria criteria = new Criteria();
        if (campusCode != null) {
            criteria.addEqualTo("processingCampusCode", campusCode);
        }

        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("immediatePaymentIndicator", Boolean.TRUE);

        return (List<PaymentRequestDocument>) getPersistenceBrokerTemplate().getCollectionByQuery(
                new QueryByCriteria(PaymentRequestDocument.class, criteria));
    }

    @Override
    public Collection<PaymentRequestDocument> getPaymentRequestsToExtractForVendor(String campusCode,
            VendorGroupingHelper vendor, Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsToExtract() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo("processingCampusCode", campusCode);
        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("holdIndicator", Boolean.FALSE);

        Criteria c1 = new Criteria();
        c1.addLessOrEqualThan("paymentRequestPayDate", onOrBeforePaymentRequestPayDate);

        Criteria c2 = new Criteria();
        c2.addEqualTo("immediatePaymentIndicator", Boolean.TRUE);

        c1.addOrCriteria(c2);
        criteria.addAndCriteria(c1);

        criteria.addEqualTo("vendorHeaderGeneratedIdentifier", vendor.getVendorHeaderGeneratedIdentifier());
        criteria.addEqualTo("vendorDetailAssignedIdentifier", vendor.getVendorDetailAssignedIdentifier());
        criteria.addEqualTo("vendorCountryCode", vendor.getVendorCountry());
        if (vendor.getVendorPostalCode() == null) {
            criteria.addIsNull("vendorPostalCode");
        } else {
            criteria.addLike("vendorPostalCode", vendor.getVendorPostalCode() + "%");
        }

        return getPersistenceBrokerTemplate().getCollectionByQuery(
                new QueryByCriteria(PaymentRequestDocument.class, criteria));
    }

    @Override
    public List<String> getEligibleForAutoApproval(Date todayAtMidnight) {
        Criteria criteria = new Criteria();
        criteria.addLessOrEqualThan(PurapPropertyConstants.PAYMENT_REQUEST_PAY_DATE, todayAtMidnight);
        criteria.addEqualTo("holdIndicator", "N");
        criteria.addEqualTo("paymentRequestedCancelIndicator", "N");
        criteria.addIn(KFSPropertyConstants.DOCUMENT_HEADER + "." + KFSPropertyConstants.APPLICATION_DOCUMENT_STATUS,
            Arrays.asList(PaymentRequestStatuses.PREQ_STATUSES_FOR_AUTO_APPROVE));

        return getDocumentNumbersOfPaymentRequestByCriteria(criteria, false);
    }

    @Override
    public String getDocumentNumberByPaymentRequestId(Integer id) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(PurapPropertyConstants.PURAP_DOC_ID, id);
        return getDocumentNumberOfPaymentRequestByCriteria(criteria);
    }

    @Override
    public List<String> getDocumentNumbersByPurchaseOrderId(Integer poPurApId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, poPurApId);
        return getDocumentNumbersOfPaymentRequestByCriteria(criteria, false);
    }

    /**
     * @param criteria list of criteria to use in the retrieve
     * @return document number for a payment request by user defined criteria.
     */
    protected String getDocumentNumberOfPaymentRequestByCriteria(Criteria criteria) {
        LOG.debug("getDocumentNumberOfPaymentRequestByCriteria() started");
        List<String> returnList = getDocumentNumbersOfPaymentRequestByCriteria(criteria, false);

        if (returnList.isEmpty()) {
            return null;
        }

        if (returnList.size() > 1) {
            // the list should have held only a single doc id of data but it holds 2 or more
            String errorMsg = "Expected single document number for given criteria but multiple (at least 2) were " +
                    "returned";
            LOG.error(errorMsg);
            throw new RuntimeException();

        } else {
            return returnList.get(0);
        }
    }

    /**
     * Retrieves a document number for a payment request by user defined criteria and sorts the values ascending if
     * orderByAscending parameter is true, descending otherwise.
     *
     * @param criteria         list of criteria to use in the retrieve
     * @param orderByAscending boolean to sort results ascending if true, descending otherwise
     * @return List of document numbers
     */
    protected List<String> getDocumentNumbersOfPaymentRequestByCriteria(Criteria criteria, boolean orderByAscending) {
        LOG.debug("getDocumentNumberOfPaymentRequestByCriteria() started");
        ReportQueryByCriteria rqbc = new ReportQueryByCriteria(PaymentRequestDocument.class, criteria);
        if (orderByAscending) {
            rqbc.addOrderByAscending(KFSPropertyConstants.DOCUMENT_NUMBER);
        } else {
            rqbc.addOrderByDescending(KFSPropertyConstants.DOCUMENT_NUMBER);
        }

        List<String> returnList = new ArrayList<>();

        List<PaymentRequestDocument> prDocs = (List<PaymentRequestDocument>) getPersistenceBrokerTemplate()
                .getCollectionByQuery(rqbc);
        for (PaymentRequestDocument prDoc : prDocs) {
            returnList.add(prDoc.getDocumentNumber());
        }

        return returnList;
    }

    /**
     * @param qbc query with criteria
     * @return a list of payment requests by user defined criteria.
     */
    protected List<PaymentRequestDocument> getPaymentRequestsByQueryByCriteria(QueryByCriteria qbc) {
        LOG.debug("getPaymentRequestsByQueryByCriteria() started");
        return (List<PaymentRequestDocument>) getPersistenceBrokerTemplate().getCollectionByQuery(qbc);
    }

    /**
     * @param vendorHeaderGeneratedId header id of the vendor id
     * @param vendorDetailAssignedId  detail id of the vendor id
     * @param invoiceNumber           invoice number as entered by AP
     * @return a list of payment requests with the given vendor id and invoice number.
     */
    @Override
    public List<PaymentRequestDocument> getActivePaymentRequestsByVendorNumberInvoiceNumber(
            Integer vendorHeaderGeneratedId, Integer vendorDetailAssignedId, String invoiceNumber) {
        LOG.debug("getActivePaymentRequestsByVendorNumberInvoiceNumber() started");
        Criteria criteria = new Criteria();
        criteria.addEqualTo("vendorHeaderGeneratedIdentifier", vendorHeaderGeneratedId);
        criteria.addEqualTo("vendorDetailAssignedIdentifier", vendorDetailAssignedId);
        criteria.addEqualTo("invoiceNumber", invoiceNumber);
        QueryByCriteria qbc = new QueryByCriteria(PaymentRequestDocument.class, criteria);

        return this.getPaymentRequestsByQueryByCriteria(qbc);
    }

    /**
     * @param vendorHeaderGeneratedId header id of the vendor id
     * @param vendorDetailAssignedId  detail id of the vendor id
     * @return a list of payment requests (using view for performance) with the given vendor id and invoice number.
     */
    /*
     * CU Customization: Backport FINP-8270 and FINP-8283
     */
    @Override
    public List<PaymentRequestView> getActivePaymentRequestsByVendorNumber(
            final Integer vendorHeaderGeneratedId,
            final Integer vendorDetailAssignedId) {
        LOG.debug("getActivePaymentRequestsByVendorNumber started");
        // Get Vendor Name and use that to find the records to return, since that is on the Payment Request
        // View, but vendor header/detail ids aren't
        final Criteria vendorCriteria = new Criteria();
        vendorCriteria.addEqualTo("vendorHeaderGeneratedIdentifier", vendorHeaderGeneratedId);
        vendorCriteria.addEqualTo("vendorDetailAssignedIdentifier", vendorDetailAssignedId);
        final QueryByCriteria vendorQuery = new QueryByCriteria(VendorDetail.class, vendorCriteria);
        final VendorDetail vendorDetail = (VendorDetail) getPersistenceBrokerTemplate().getObjectByQuery(vendorQuery);

        if (ObjectUtils.isNull(vendorDetail)) {
            return List.of();
        }
 
        final Criteria paymentRequestCriteria = new Criteria();

        paymentRequestCriteria.addEqualTo("UPPER(vendorName)", vendorDetail.getVendorName().toUpperCase(Locale.US));
        final QueryByCriteria paymentRequestQuery =
                new QueryByCriteria(PaymentRequestView.class, paymentRequestCriteria);
        return (List<PaymentRequestView>) getPersistenceBrokerTemplate().getCollectionByQuery(paymentRequestQuery);
    }

    @Override
    public List<PaymentRequestDocument> getActivePaymentRequestsByPOIdInvoiceAmountInvoiceDate(Integer poId,
            KualiDecimal vendorInvoiceAmount, Date invoiceDate) {
        LOG.debug("getActivePaymentRequestsByVendorNumberInvoiceNumber() started");
        Criteria criteria = new Criteria();
        criteria.addEqualTo(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, poId);
        criteria.addEqualTo(PurapPropertyConstants.VENDOR_INVOICE_AMOUNT, vendorInvoiceAmount);
        criteria.addEqualTo(KFSPropertyConstants.INVOICE_DATE, invoiceDate);
        QueryByCriteria qbc = new QueryByCriteria(PaymentRequestDocument.class, criteria);

        return this.getPaymentRequestsByQueryByCriteria(qbc);
    }

    @Override
    public List<PaymentRequestDocument> getActivePaymentRequestsByInvoiceAmountInvoiceDate(
            Integer vendorHeaderGeneratedId, Integer vendorDetailAssignedId, KualiDecimal vendorInvoiceAmount,
            Date invoiceDate) {
        LOG.debug("getActivePaymentRequestsByVendorNumberInvoiceNumber() started");
        Criteria criteria = new Criteria();
        criteria.addEqualTo(PurapPropertyConstants.VENDOR_HEADER_GENERATED_ID, vendorHeaderGeneratedId);
        criteria.addEqualTo(PurapPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, vendorDetailAssignedId);
        criteria.addEqualTo(PurapPropertyConstants.VENDOR_INVOICE_AMOUNT, vendorInvoiceAmount);
        criteria.addEqualTo(KFSPropertyConstants.INVOICE_DATE, invoiceDate);
        QueryByCriteria qbc = new QueryByCriteria(PaymentRequestDocument.class, criteria);

        return this.getPaymentRequestsByQueryByCriteria(qbc);
    }

    @Override
    public int getActivePaymentRequestCountForPurchaseOrder(Integer purchaseOrderId) {
        LOG.debug("getActivePaymentRequestsByVendorNumberInvoiceNumber() started");

        Criteria criteria = new Criteria();

        criteria.addEqualTo(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, purchaseOrderId);
        criteria.addIn(KFSPropertyConstants.DOCUMENT_HEADER + "." +
                        KFSPropertyConstants.APPLICATION_DOCUMENT_STATUS,
            Arrays.asList(PaymentRequestStatuses.STATUSES_POTENTIALLY_ACTIVE));
        Collection<String> workflowNotActiveStatuses = Arrays.asList(DocumentStatus.CANCELED.getCode(),
                DocumentStatus.EXCEPTION.getCode());
        criteria.addNotIn(KFSPropertyConstants.DOCUMENT_HEADER + "." +
                KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE, workflowNotActiveStatuses);
        QueryByCriteria qbc = new QueryByCriteria(PaymentRequestDocument.class, criteria);
        return this.getPersistenceBrokerTemplate().getCount(qbc);
    }

    @Override
    public List<PaymentRequestDocument> getPaymentRequestInReceivingStatus() {
        Criteria criteria = new Criteria();
        criteria.addNotEqualTo(PurapPropertyConstants.HOLD_INDICATOR, "Y");
        criteria.addNotEqualTo(PurapPropertyConstants.PAYMENT_REQUEST_CANCEL_INDICATOR, "Y");
        criteria.addEqualTo(KFSConstants.DOCUMENT_HEADER_PROPERTY_NAME + "." +
                KFSPropertyConstants.APPLICATION_DOCUMENT_STATUS,
                PaymentRequestStatuses.APPDOC_AWAITING_RECEIVING_REVIEW);

        QueryByCriteria qbc = new QueryByCriteria(PaymentRequestDocument.class, criteria);
        return this.getPaymentRequestsByQueryByCriteria(qbc);

    }
}

