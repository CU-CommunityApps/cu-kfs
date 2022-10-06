package edu.cornell.kfs.module.purap.document.dataaccess.impl;

import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.dataaccess.impl.PaymentRequestDaoOjb;
import org.kuali.kfs.module.purap.util.VendorGroupingHelper;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.dataaccess.CuPaymentRequestDao;

@SuppressWarnings("unchecked")
public class CuPaymentRequestDaoOjb extends PaymentRequestDaoOjb implements CuPaymentRequestDao {
	private static final Logger LOG = LogManager.getLogger();

    @Override
    public List<PaymentRequestDocument> getPaymentRequestsToExtract(boolean onlySpecialPayments, String campusCode, Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsToExtract() started");

        Criteria criteria = new Criteria();
        if (campusCode != null) {
            criteria.addEqualTo("processingCampusCode", campusCode);
        }
        //criteria.addIn(PurapPropertyConstants.STATUS_CODE, Arrays.asList(PaymentRequestStatuses.STATUSES_ALLOWED_FOR_EXTRACTION));
        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("holdIndicator", Boolean.FALSE);
        criteria.addEqualTo("paymentMethodCode", "P");
        
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

        return (List<PaymentRequestDocument>) getPersistenceBrokerTemplate()
                .getCollectionByQuery(new QueryByCriteria(CuPaymentRequestDocument.class, criteria));
    }
    
    public Collection<PaymentRequestDocument> getPaymentRequestsToExtractForVendor(String campusCode, VendorGroupingHelper vendor, Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsToExtract() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo("processingCampusCode", campusCode);
        //criteria.addIn(PurapPropertyConstants.STATUS_CODE, statuses);
        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("holdIndicator", Boolean.FALSE);
        criteria.addEqualTo("paymentMethodCode", "P");
        
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

        return (List<PaymentRequestDocument>) getPersistenceBrokerTemplate()
        		.getCollectionByQuery(new QueryByCriteria(CuPaymentRequestDocument.class, criteria));
    }
    
    @Override
    public int countDocumentsByPurchaseOrderId(Integer poPurApId, String applicationDocumentStatus) {

        Criteria criteria = new Criteria();
        criteria.addEqualTo(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, poPurApId);
        if (StringUtils.isNotBlank(applicationDocumentStatus)) {
            criteria.addEqualTo(KFSPropertyConstants.DOCUMENT_HEADER + "." + KFSPropertyConstants.APPLICATION_DOCUMENT_STATUS, applicationDocumentStatus);
        }

        QueryByCriteria query = QueryFactory.newQuery(PaymentRequestDocument.class, criteria);

        final int numOfPreqs = getPersistenceBrokerTemplate().getCount(query);
        return numOfPreqs;
    }

    @Override
    public String getObjectIdByPaymentRequestDocumentNumber(String documentNumber) {
        // Build PREQ query that matches only on document number.
        Criteria crit = new Criteria();
        crit.addEqualTo("documentNumber", documentNumber);
        
        // Prepare report query that only retrieves object ID.
        ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(PaymentRequestDocument.class, crit);
        reportQuery.setAttributes(new String[] {"objectId"});
        reportQuery.setJdbcTypes(new int[] {java.sql.Types.VARCHAR});
        
        // Run query and return results.
        Iterator<Object[]> results = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery);
        if (results.hasNext()) {
            return (String) results.next()[0];
        }
        
        return null;
    }

}
