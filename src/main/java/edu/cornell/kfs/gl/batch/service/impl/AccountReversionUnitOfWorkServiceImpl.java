

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
package edu.cornell.kfs.gl.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.gl.batch.dataaccess.ReversionUnitOfWorkDao;

/**
 * The base implementation of OrganizationReversionUnitOfWorkService
 */
@Transactional
public class AccountReversionUnitOfWorkServiceImpl extends ReversionUnitOfWorkServiceImpl  {
	private static final Logger LOG = LogManager.getLogger(AccountReversionUnitOfWorkServiceImpl.class);
    private ReversionUnitOfWorkDao acctReversionUnitOfWorkDao;

    /**
     * Immediate deletion awaits all entries of the unit of work summary tables in the persistence store once
     * you call this method, for this method is both powerful and deadly and also gets called to clear out
     * those tables before every single org reversion run.
     * @see org.kuali.kfs.gl.batch.service.AccountReversionUnitOfWorkService#removeAll()
     */
    public void destroyAllUnitOfWorkSummaries() {
        acctReversionUnitOfWorkDao.destroyAllUnitOfWorkSummaries();
    }

   
    /**
     * Gets the acctReversionUnitOfWorkDao attribute.
     * 
     * @return Returns the acctReversionUnitOfWorkDao.
     */
    public ReversionUnitOfWorkDao getAcctReversionUnitOfWorkDao() {
        return acctReversionUnitOfWorkDao;
    }

    /**
     * Sets the acctReversionUnitOfWorkDao attribute value.
     * 
     * @param acctReversionUnitOfWorkDao The acctReversionUnitOfWorkDao to set.
     */
    public void setAcctReversionUnitOfWorkDao(ReversionUnitOfWorkDao acctReversionUnitOfWorkDao) {
        this.acctReversionUnitOfWorkDao = acctReversionUnitOfWorkDao;
    }

}
