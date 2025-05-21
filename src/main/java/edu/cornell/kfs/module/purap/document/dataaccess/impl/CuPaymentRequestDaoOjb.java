package edu.cornell.kfs.module.purap.document.dataaccess.impl;

import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.purap.PurapConstants;
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

    private DataDictionaryService dataDictionaryService;
    
    @Override
    public List<PaymentRequestDocument> getPaymentRequestsToExtract(
            final boolean onlySpecialPayments, 
            final String campusCode, final Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsToExtract() started");

        final Criteria criteria = new Criteria();
        if (campusCode != null) {
            criteria.addEqualTo("processingCampusCode", campusCode);
        }
        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("holdIndicator", Boolean.FALSE);
        //Cornell customization
        criteria.addEqualTo("paymentMethodCode", "P");
        
        if (onlySpecialPayments) {
            final Criteria a = new Criteria();

            final Criteria c1 = new Criteria();
            c1.addNotNull("specialHandlingInstructionLine1Text");
            final Criteria c2 = new Criteria();
            c2.addNotNull("specialHandlingInstructionLine2Text");
            final Criteria c3 = new Criteria();
            c3.addNotNull("specialHandlingInstructionLine3Text");
            final Criteria c4 = new Criteria();
            c4.addEqualTo("paymentAttachmentIndicator", Boolean.TRUE);

            c1.addOrCriteria(c2);
            c1.addOrCriteria(c3);
            c1.addOrCriteria(c4);

            a.addAndCriteria(c1);
            a.addLessOrEqualThan("paymentRequestPayDate", onOrBeforePaymentRequestPayDate);

            final Criteria c5 = new Criteria();
            c5.addEqualTo("immediatePaymentIndicator", Boolean.TRUE);
            c5.addOrCriteria(a);

            criteria.addAndCriteria(a);
        } else {
            final Criteria c1 = new Criteria();
            c1.addLessOrEqualThan("paymentRequestPayDate", onOrBeforePaymentRequestPayDate);

            final Criteria c2 = new Criteria();
            c2.addEqualTo("immediatePaymentIndicator", Boolean.TRUE);

            c1.addOrCriteria(c2);
            criteria.addAndCriteria(c1);
        }

        return (List<PaymentRequestDocument>) getPersistenceBrokerTemplate()
                .getCollectionByQuery(new QueryByCriteria(paymentRequestDocumentClass(), criteria));
    }
    
    public Collection<PaymentRequestDocument> getPaymentRequestsToExtractForVendor(
            final String campusCode, final VendorGroupingHelper vendor, final Date onOrBeforePaymentRequestPayDate) {
        LOG.debug("getPaymentRequestsToExtract() started");

        final Criteria criteria = new Criteria();
        criteria.addEqualTo("processingCampusCode", campusCode);
        //criteria.addIn(PurapPropertyConstants.STATUS_CODE, statuses);
        criteria.addIsNull("extractedTimestamp");
        criteria.addEqualTo("holdIndicator", Boolean.FALSE);
        criteria.addEqualTo("paymentMethodCode", "P");
        
        final Criteria c1 = new Criteria();
        c1.addLessOrEqualThan("paymentRequestPayDate", onOrBeforePaymentRequestPayDate);

        final Criteria c2 = new Criteria();
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
        		.getCollectionByQuery(new QueryByCriteria(paymentRequestDocumentClass(), criteria));
    }
    
    @Override
    public int countDocumentsByPurchaseOrderId(
            final Integer poPurApId, final String applicationDocumentStatus) {

        final Criteria criteria = new Criteria();
        criteria.addEqualTo(PurapPropertyConstants.PURCHASE_ORDER_IDENTIFIER, poPurApId);
        if (StringUtils.isNotBlank(applicationDocumentStatus)) {
            criteria.addEqualTo(KFSPropertyConstants.DOCUMENT_HEADER + "." + KFSPropertyConstants.APPLICATION_DOCUMENT_STATUS, applicationDocumentStatus);
        }

        final QueryByCriteria query = QueryFactory.newQuery(PaymentRequestDocument.class, criteria);

        final int numOfPreqs = getPersistenceBrokerTemplate().getCount(query);
        return numOfPreqs;
    }

    @Override
    public String getObjectIdByPaymentRequestDocumentNumber(final String documentNumber) {
        // Build PREQ query that matches only on document number.
        final Criteria crit = new Criteria();
        crit.addEqualTo("documentNumber", documentNumber);
        
        // Prepare report query that only retrieves object ID.
        final ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(PaymentRequestDocument.class, crit);
        reportQuery.setAttributes(new String[] {"objectId"});
        reportQuery.setJdbcTypes(new int[] {java.sql.Types.VARCHAR});
        
        // Run query and return results.
        final Iterator<Object[]> results = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery);
        if (results.hasNext()) {
            return (String) results.next()[0];
        }
        
        return null;
    }
    
    private Class<? extends Document> paymentRequestDocumentClass() {
        final Class<? extends Document> paymentRequestDocumentClass = dataDictionaryService.getDocumentClassByTypeName(
                PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
        return paymentRequestDocumentClass != null ? paymentRequestDocumentClass : CuPaymentRequestDocument.class;
    }
    
    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

}
