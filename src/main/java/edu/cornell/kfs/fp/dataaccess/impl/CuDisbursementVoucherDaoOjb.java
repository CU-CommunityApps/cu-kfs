package edu.cornell.kfs.fp.dataaccess.impl;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.fp.dataaccess.impl.DisbursementVoucherDaoOjb;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;

public class CuDisbursementVoucherDaoOjb extends DisbursementVoucherDaoOjb {
    private static final Logger LOG = LogManager.getLogger();
	
    public DisbursementVoucherDocument getDocument(final String fdocNbr) {
        LOG.debug("getDocument() started");

        final Criteria criteria = new Criteria();
        criteria.addEqualTo(KRADPropertyConstants.DOCUMENT_NUMBER, fdocNbr);

        return (DisbursementVoucherDocument) getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(getDisbursementVoucherDocumentClass(), criteria));
    }
	
    public Collection<DisbursementVoucherDocument> getDocumentsByHeaderStatus(final String statusCode, final boolean immediatesOnly) {
        LOG.debug("getDocumentsByHeaderStatus() started");

        final Criteria criteria = new Criteria();
        criteria.addEqualTo(KFSPropertyConstants.DOCUMENT_HEADER + KFSConstants.DELIMITER +
            KFSPropertyConstants.FINANCIAL_DOCUMENT_STATUS_CODE, statusCode);
        criteria.addEqualTo(KFSPropertyConstants.DISB_VCHR_PAYMENT_METHOD_CODE, KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
        if (immediatesOnly) {
            criteria.addEqualTo(KFSPropertyConstants.IMMEDIATE_PAYMENT_INDICATOR, Boolean.TRUE);
        }

        return getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(getDisbursementVoucherDocumentClass(), criteria));
    }

    @SuppressWarnings("unchecked")
    protected Class getDisbursementVoucherDocumentClass() {
        return SpringContext.getBean(DataDictionaryService.class).getDocumentClassByTypeName("DV");
    }
}
