package org.kuali.kfs.module.ar.dataaccess.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import org.kuali.kfs.module.ar.dataaccess.InvoicePaidAppliedDao;

public class InvoicePaidAppliedDaoObj extends PlatformAwareDaoBaseOjb implements InvoicePaidAppliedDao {
    private static final Logger LOG = LogManager.getLogger(InvoicePaidAppliedDaoObj.class);
    
    private static final String DOCUMENT_NUMBER = "documentNumber";
    
    public void deleteAllInvoicePaidApplieds(String documentNumber) {
        LOG.info("deleteAllInvoicePaidApplieds: Removing all invoicePaidApplied items for edoc =" + documentNumber + "=");
        
        if (ObjectUtils.isNull(documentNumber) || StringUtils.isBlank(documentNumber)) {
            LOG.info("deleteAllInvoicePaidApplieds: Received a null or blank documentNumber.");
            return;
        }
        
        Criteria criteria = new Criteria();
        criteria.addEqualTo(DOCUMENT_NUMBER, documentNumber);
        
        QueryByCriteria qbc = QueryFactory.newQuery(InvoicePaidApplied.class, criteria);
        getPersistenceBrokerTemplate().deleteByQuery(qbc);
        
        getPersistenceBrokerTemplate().clearCache();
    }
    
}
