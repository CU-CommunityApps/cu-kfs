/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.fp.dataaccess.impl;

import java.util.Collection;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.fp.dataaccess.DisbursementVoucherDao;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.service.DataDictionaryService;

import edu.cornell.kfs.sys.CuKFSPropertyConstants;

// KFSPTS-1891 : change in this class
public class DisbursementVoucherDaoOjb extends PlatformAwareDaoBaseOjb implements DisbursementVoucherDao {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherDaoOjb.class);

    public void save(DisbursementVoucherDocument document) {
        LOG.debug("save() started");

        getPersistenceBrokerTemplate().store(document);
    }

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
     * @see org.kuali.kfs.fp.dataaccess.DisbursementVoucherDao#getDocumentsByHeaderStatus(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection getDocumentsByHeaderStatus(String statusCode) {
        LOG.debug("getDocumentsByHeaderStatus() started");

        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuKFSPropertyConstants.DOC_HDR_FINANCIAL_DOCUMENT_STATUS_CODE, statusCode);
        criteria.addEqualTo(KFSPropertyConstants.DISB_VCHR_PAYMENT_METHOD_CODE, DisbursementVoucherConstants.PAYMENT_METHOD_CHECK);

        return getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(getDisbursementVoucherDocumentClass(), criteria));
    }
    
    @SuppressWarnings("unchecked")
    protected Class getDisbursementVoucherDocumentClass() {
        return SpringContext.getBean(DataDictionaryService.class).getDocumentClassByTypeName("DV");
    }
}

