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
package org.kuali.kfs.gl.batch.service.impl;

import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.gl.batch.dataaccess.ReversionUnitOfWorkDao;
import edu.cornell.kfs.gl.batch.service.impl.ReversionUnitOfWorkServiceImpl;

/**
 * The base implementation of OrganizationReversionUnitOfWorkService
 */
@Transactional
public class OrganizationReversionUnitOfWorkServiceImpl extends ReversionUnitOfWorkServiceImpl  {
    static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OrganizationReversionUnitOfWorkServiceImpl.class);
    private ReversionUnitOfWorkDao orgReversionUnitOfWorkDao;

    /**
     * Immediate deletion awaits all entries of the unit of work summary tables in the persistence store once
     * you call this method, for this method is both powerful and deadly and also gets called to clear out
     * those tables before every single org reversion run.
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionUnitOfWorkService#removeAll()
     */
    public void destroyAllUnitOfWorkSummaries() {
        orgReversionUnitOfWorkDao.destroyAllUnitOfWorkSummaries();
    }

   
    /**
     * Gets the orgReversionUnitOfWorkDao attribute.
     * 
     * @return Returns the orgReversionUnitOfWorkDao.
     */
    public ReversionUnitOfWorkDao getOrgReversionUnitOfWorkDao() {
        return orgReversionUnitOfWorkDao;
    }

    /**
     * Sets the orgReversionUnitOfWorkDao attribute value.
     * 
     * @param orgReversionUnitOfWorkDao The orgReversionUnitOfWorkDao to set.
     */
    public void setOrgReversionUnitOfWorkDao(ReversionUnitOfWorkDao orgReversionUnitOfWorkDao) {
        this.orgReversionUnitOfWorkDao = orgReversionUnitOfWorkDao;
    }

}
