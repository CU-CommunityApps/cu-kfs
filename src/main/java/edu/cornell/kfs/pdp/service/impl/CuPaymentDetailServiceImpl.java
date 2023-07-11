package edu.cornell.kfs.pdp.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.service.impl.PaymentDetailServiceImpl;
import org.kuali.kfs.sys.DynamicCollectionComparator;

import edu.cornell.kfs.pdp.service.CuPaymentDetailService;

public class CuPaymentDetailServiceImpl extends PaymentDetailServiceImpl implements CuPaymentDetailService {

    private static final Logger LOG = LogManager.getLogger();

    private CriteriaLookupService criteriaLookupService;

    /*
     * Copied the superclass's 4-arg version of this method and added handling of the Process Immediate flag.
     * Also adjusted the query to use the CriteriaLookupService, since the Process Immediate flag could
     * potentially be null.
     */
    @Override
    public Iterator<PaymentDetail> getByDisbursementNumber(Integer disbursementNumber, Integer processId,
            String disbursementType, String bankCode, boolean processImmediate) {
        LOG.debug("getByDisbursementNumber() started");

        QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_NUMBER,
                        disbursementNumber),
                PredicateFactory.equal(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP + "." +
                        PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PROCESS_ID, processId),
                PredicateFactory.equal(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP + "." +
                        PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, disbursementType),
                PredicateFactory.equal(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP + "." +
                        PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BANK_CODE, bankCode),
                createProcessImmediateCondition(processImmediate));
        GenericQueryResults<PaymentDetail> results = criteriaLookupService.lookup(PaymentDetail.class, criteria);
        List<PaymentDetail> paymentDetailByDisbursementNumberList = new ArrayList<>(results.getResults());
        DynamicCollectionComparator.sort(paymentDetailByDisbursementNumberList,
                PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_FINANCIAL_DOCUMENT_TYPE_CODE,
                PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_CUST_PAYMENT_DOC_NBR);

        return paymentDetailByDisbursementNumberList.iterator();
    }

    private Predicate createProcessImmediateCondition(boolean processImmediate) {
        if (processImmediate) {
            return PredicateFactory.equal(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_IMMEDIATE,
                    KRADConstants.YES_INDICATOR_VALUE);
        } else {
            return PredicateFactory.or(
                    PredicateFactory.equal(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_IMMEDIATE,
                            KRADConstants.NO_INDICATOR_VALUE),
                    PredicateFactory.isNull(PdpPropertyConstants.PaymentDetail.PAYMENT_PROCESS_IMMEDIATE));
        }
    }

    public void setCriteriaLookupService(CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

}
