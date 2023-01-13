package edu.cornell.kfs.pdp.dataaccess.impl;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.dataaccess.impl.FormatPaymentDaoOjb;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.dataaccess.CuFormatPaymentDao;

public class CuFormatPaymentDaoOjb extends FormatPaymentDaoOjb implements CuFormatPaymentDao {
    private static final Logger LOG = LogManager.getLogger();
   
    @Override
    public Iterator markPaymentsForFormat(List customerIds, Timestamp paydateTs, String paymentTypes, String paymentDistribution) {
        LOG.debug("markPaymentsForFormat() started");

        Criteria criteria = new Criteria();

        if (customerIds.size() > 0) {
            criteria.addIn(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BATCH + "." + PdpPropertyConstants.BatchConstants.CUSTOMER_ID, customerIds);
        }

        criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYMENT_STATUS_CODE, PdpConstants.PaymentStatusCodes.OPEN);

        if (PdpConstants.PaymentTypes.DISBURSEMENTS_WITH_SPECIAL_HANDLING.equals(paymentTypes)) {
            // special handling only
            criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_SPECIAL_HANDLING, Boolean.TRUE);
        } else if (PdpConstants.PaymentTypes.DISBURSEMENTS_NO_SPECIAL_HANDLING.equals(paymentTypes)) {
            // no special handling only
            criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_SPECIAL_HANDLING, Boolean.FALSE);
        } else if (PdpConstants.PaymentTypes.DISBURSEMENTS_WITH_ATTACHMENTS.equals(paymentTypes)) {
            // attachments only
            criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_ATTACHMENT, Boolean.TRUE);
        } else if (PdpConstants.PaymentTypes.DISBURSEMENTS_NO_ATTACHMENTS.equals(paymentTypes)) {
            // no attachments only
            criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_ATTACHMENT, Boolean.FALSE);
        }

        if (PdpConstants.PaymentTypes.PROCESS_IMMEDIATE.equals(paymentTypes)) {
            criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PROCESS_IMMEDIATE, Boolean.TRUE);
        } else {
            // (Payment date <= usePaydate OR immediate = TRUE)
            Criteria criteria1 = new Criteria();
            criteria1.addEqualTo(PdpPropertyConstants.PaymentGroup.PROCESS_IMMEDIATE, Boolean.TRUE);

            Criteria criteria2 = new Criteria();
            criteria2.addLessOrEqualThan(PdpPropertyConstants.PaymentGroup.PAYMENT_DATE, paydateTs);
            criteria1.addOrCriteria(criteria2);

            criteria.addAndCriteria(criteria1);
        }
        
        if (CUPdpConstants.PaymentDistributions.PROCESS_ACH_ONLY.equals(paymentDistribution)) {
            criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, PdpConstants.DisbursementTypeCodes.ACH);
        } else if (CUPdpConstants.PaymentDistributions.PROCESS_CHECK_ONLY.equals(paymentDistribution)) {
            Criteria criteria3 = new Criteria();
            criteria3.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, PdpConstants.DisbursementTypeCodes.CHECK);
            
            Criteria criteria4 = new Criteria();
            criteria4.addIsNull(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE);
            criteria3.addOrCriteria(criteria4);
            
            criteria.addAndCriteria(criteria3);
        } 

        Iterator groupIterator = getPersistenceBrokerTemplate().getIteratorByQuery(new QueryByCriteria(PaymentGroup.class, criteria));
        return groupIterator;
    }
}
