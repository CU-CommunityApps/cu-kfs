package edu.cornell.kfs.fp.dataaccess.impl;

import java.util.Collection;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.fp.dataaccess.impl.DisbursementVoucherDaoOjb;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.DataDictionaryService;

public class CuDisbursementVoucherDaoOjb extends DisbursementVoucherDaoOjb {
	
	 private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuDisbursementVoucherDaoOjb.class);
	
    /**
     * @see org.kuali.kfs.fp.dataaccess.DisbursementVoucherDao#getDocument(java.lang.String)
     */
    public DisbursementVoucherDocument getDocument(String fdocNbr) {
        LOG.debug("getDocument() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo("documentNumber", fdocNbr);

        return (DisbursementVoucherDocument) getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(getDisbursementVoucherDocumentClass(), criteria));
    }
	
    /**
     * @see org.kuali.kfs.fp.dataaccess.DisbursementVoucherDao#getDocumentsByHeaderStatus(java.lang.String, boolean)
     */
    public Collection getDocumentsByHeaderStatus(String statusCode, boolean immediatesOnly) {
        LOG.debug("getDocumentsByHeaderStatus() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo("documentHeader.financialDocumentStatusCode", statusCode);
        criteria.addEqualTo("disbVchrPaymentMethodCode", DisbursementVoucherConstants.PAYMENT_METHOD_CHECK);
        if (immediatesOnly) {
            criteria.addEqualTo("immediatePaymentIndicator", Boolean.TRUE);
        }

        return getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(getDisbursementVoucherDocumentClass(), criteria));
    }

    @SuppressWarnings("unchecked")
    protected Class getDisbursementVoucherDocumentClass() {
        return SpringContext.getBean(DataDictionaryService.class).getDocumentClassByTypeName("DV");
    }
}
