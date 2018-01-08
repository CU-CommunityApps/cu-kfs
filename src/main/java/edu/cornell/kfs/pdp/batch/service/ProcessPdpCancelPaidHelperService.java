package edu.cornell.kfs.pdp.batch.service;

import java.sql.Date;

import org.kuali.kfs.pdp.businessobject.PaymentDetail;

/**
 * Helper interface for processing canceled/paid PDP payment details individually,
 * allowing each one to be processed in a separate transaction.
 */
public interface ProcessPdpCancelPaidHelperService {

    void processPdpCancel(PaymentDetail paymentDetail, Date processDate);

    void processPdpPaid(PaymentDetail paymentDetail, Date processDate);
}
