package edu.cornell.kfs.pdp.dataaccess.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.DynamicCollectionComparator;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.pdp.dataaccess.AchBundlerAdviceDao;

public class AchBundlerAdviceDaoObj extends PlatformAwareDaoBaseOjb implements AchBundlerAdviceDao {
	private static final Logger LOG = LogManager.getLogger(AchBundlerAdviceDaoObj.class);	
	
    //KFSPTS-1460
    /**
     * Returns distinct disbursement numbers for ACH payments needing advice email notifications.
     * 
     * NOTE:
     * This method was made transactional per fix described in KULRICE-6914 for the iterator no longer
     * containing the data and iter.hasNext() causing a run time exception.
     * 
     * @return an iterator of Disbursement Numbers matching the given criteria
     */
	@Transactional
    public HashSet<Integer> getDistinctDisbursementNumbersForAchPaymentsNeedingAdviceNotification () {
    	LOG.debug("getDistinctDisbursementNumbersForAchPaymentsNeedingAdviceNotification() started");
        
        Criteria criteria  = new Criteria();
        criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYMENT_STATUS_CODE, PdpConstants.PaymentStatusCodes.EXTRACTED);
        criteria.addEqualTo(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, PdpConstants.DisbursementTypeCodes.ACH);
        criteria.addIsNull(PdpPropertyConstants.ADVICE_EMAIL_SENT_DATE);
        QueryByCriteria disbursementNumbersQuery = QueryFactory.newQuery(PaymentGroup.class, criteria);
        Iterator iter = getPersistenceBrokerTemplate().getIteratorByQuery(disbursementNumbersQuery);
        
        //used to pair down to distinct disbursement numbers
        HashSet<Integer> results = new HashSet<Integer>();
        while (iter.hasNext()) {
        	PaymentGroup pg = (PaymentGroup)iter.next();
        	results.add(new Integer(pg.getDisbursementNbr().toString()));        	
    	}        
        return results;        
    }
    
    //KFSPTS--1460
    /**
     * Returns all PaymentDetail records needing ACH advice email notifications for a given disbursement number
     * 
     * NOTE:
     * This method was made transactional per fix described in KULRICE-6914 for the iterator no longer
     * containing the data and iter.hasNext() causing a run time exception.
     * 
     * @return an iterator of PaymentDetail records matching the given criteria
     */
	@Transactional
    public List<PaymentDetail> getAchPaymentDetailsNeedingAdviceNotificationByDisbursementNumber(Integer disbursementNumber) {
    	LOG.debug("getAchPaymentDetailsNeedingAdviceNotificationByDisbursementNumber() started");
    	
        Criteria criteria  = new Criteria();
        criteria.addEqualTo(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, PdpConstants.DisbursementTypeCodes.ACH);
        criteria.addEqualTo(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYMENT_STATUS_CODE, PdpConstants.PaymentStatusCodes.EXTRACTED);
        criteria.addEqualTo(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_NBR, disbursementNumber);
        criteria.addIsNull(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.ADVICE_EMAIL_SENT_DATE);        
        QueryByCriteria paymentDetailsQuery = QueryFactory.newQuery(PaymentDetail.class, criteria);
        List paymentDetailByDisbursementNumberList = (List)getPersistenceBrokerTemplate().getCollectionByQuery(paymentDetailsQuery);
        DynamicCollectionComparator.sort(paymentDetailByDisbursementNumberList, PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_FINANCIAL_DOCUMENT_TYPE_CODE, PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_CUST_PAYMENT_DOC_NBR);
    	
        return paymentDetailByDisbursementNumberList;
    }
    
}

