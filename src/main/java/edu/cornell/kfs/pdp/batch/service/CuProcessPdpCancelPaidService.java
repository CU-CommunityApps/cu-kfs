package edu.cornell.kfs.pdp.batch.service;

import java.sql.Date;

import org.kuali.kfs.pdp.batch.service.ProcessPdpCancelPaidService;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;

public interface CuProcessPdpCancelPaidService extends ProcessPdpCancelPaidService {

    void processPdpCancel(PaymentDetail paymentDetail, Date processDate);

    void processPdpPaid(PaymentDetail paymentDetail, Date processDate);

}
