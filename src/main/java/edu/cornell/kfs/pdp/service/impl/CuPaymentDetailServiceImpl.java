package edu.cornell.kfs.pdp.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.service.impl.PaymentDetailServiceImpl;
import org.kuali.kfs.sys.DynamicCollectionComparator;

import edu.cornell.kfs.pdp.service.CuPaymentDetailService;

public class CuPaymentDetailServiceImpl extends PaymentDetailServiceImpl implements CuPaymentDetailService {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;

    /*
     * Copied the superclass's 4-arg version of this method and added an arg for the Process Immediate flag.
     */
    @Override
    public Iterator<PaymentDetail> getByDisbursementNumber(final Integer disbursementNumber, Integer processId,
            String disbursementType, String bankCode, boolean processImmediate) {
        LOG.debug("getByDisbursementNumber() started");

        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER, disbursementNumber);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP + "." +
                PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PROCESS_ID, processId);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP + "." +
                PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, disbursementType);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP + "." +
                PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BANK_CODE, bankCode);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_IMMEDIATE,
                processImmediate ? KRADConstants.YES_INDICATOR_VALUE : KRADConstants.NO_INDICATOR_VALUE);
        final List<PaymentDetail> paymentDetailByDisbursementNumberList =
                (List<PaymentDetail>) businessObjectService.findMatching(PaymentDetail.class, fieldValues);
        DynamicCollectionComparator.sort(paymentDetailByDisbursementNumberList,
                PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_FINANCIAL_DOCUMENT_TYPE_CODE,
                PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_CUST_PAYMENT_DOC_NBR);

        return paymentDetailByDisbursementNumberList.iterator();
    }

    @Override
    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        super.setBusinessObjectService(businessObjectService);
        this.businessObjectService = businessObjectService;
    }

}
