/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.dataaccess.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.purap.businessobject.SciQuestPunchoutData;
import org.kuali.kfs.module.purap.dataaccess.SciQuestDataAccess;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

/**
 * SciQuestDataAccessImpl
 *  
 * @author Tom Bradford <tbradford@rsmart.com>
 */
public class SciQuestDataAccessImpl extends PlatformAwareDaoBaseOjb implements SciQuestDataAccess {
    private static final String REQS_ID = "requisitionId";
    
    public SciQuestPunchoutData getPunchoutDataForRequisition(Integer id) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(REQS_ID, id);
        QueryByCriteria qbc = new QueryByCriteria(SciQuestPunchoutData.class, criteria); 
        return (SciQuestPunchoutData) getPersistenceBrokerTemplate().getObjectByQuery(qbc);        
    }
}
