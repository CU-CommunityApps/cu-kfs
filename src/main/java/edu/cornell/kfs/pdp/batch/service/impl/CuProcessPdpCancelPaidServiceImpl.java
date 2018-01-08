package edu.cornell.kfs.pdp.batch.service.impl;

import java.sql.Date;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.pdp.batch.service.impl.ProcessPdpCancelPaidServiceImpl;
import org.kuali.kfs.pdp.businessobject.ExtractionUnit;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.pdp.batch.service.ProcessPdpCancelPaidHelperService;

@Transactional
public class CuProcessPdpCancelPaidServiceImpl extends ProcessPdpCancelPaidServiceImpl {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuProcessPdpCancelPaidServiceImpl.class);

    protected ProcessPdpCancelPaidHelperService processPdpCancelPaidHelperService;

    /**
     * Overridden to use a helper service to process each payment detail in its own transaction.
     * 
     * @see org.kuali.kfs.pdp.batch.service.impl.ProcessPdpCancelPaidServiceImpl#processPdpCancels()
     */
    @Override
    public void processPdpCancels() {
        LOG.debug("processPdpCancels() started");

        Date processDate = dateTimeService.getCurrentSqlDate();
        List<ExtractionUnit> extractionUnits = getExtractionUnits();
        Iterator<PaymentDetail> details = paymentDetailService.getUnprocessedCancelledDetails(extractionUnits);
        
        while (details.hasNext()) {
            PaymentDetail paymentDetail = details.next();
            processPdpCancelPaidHelperService.processPdpCancel(paymentDetail, processDate);
        }
    }

    /**
     * Overridden to use a helper service to process each payment detail in its own transaction.
     * 
     * @see org.kuali.kfs.pdp.batch.service.impl.ProcessPdpCancelPaidServiceImpl#processPdpPaids()
     */
    @Override
    public void processPdpPaids() {
        LOG.debug("processPdpPaids() started");

        Date processDate = dateTimeService.getCurrentSqlDate();
        List<ExtractionUnit> extractionUnits = getExtractionUnits();
        Iterator<PaymentDetail> details = paymentDetailService.getUnprocessedPaidDetails(extractionUnits);
        
        while (details.hasNext()) {
            PaymentDetail paymentDetail = details.next();
            processPdpCancelPaidHelperService.processPdpPaid(paymentDetail, processDate);
        }
    }

    public void setProcessPdpCancelPaidHelperService(ProcessPdpCancelPaidHelperService processPdpCancelPaidHelperService) {
        this.processPdpCancelPaidHelperService = processPdpCancelPaidHelperService;
    }

}