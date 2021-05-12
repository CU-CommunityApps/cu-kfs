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
package edu.cornell.kfs.gl.batch.dataaccess.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;

import edu.cornell.kfs.gl.batch.dataaccess.ReversionUnitOfWorkDao;

/**
 * A JDBC implementation of the ReversionUnitOfWorkDao...we had to use this because PersistenceService
 * truncated tables, which is something you can't do on tables with primary keys.
 */
public class ReversionUnitOfWorkDaoJdbc extends PlatformAwareDaoBaseJdbc implements ReversionUnitOfWorkDao {
	private static final Logger LOG = LogManager.getLogger(ReversionUnitOfWorkDaoJdbc.class);

    /**
     * Deletes all existing records in gl_rvrsn_ctgry_amt_t and gl_rvrsn_unit_wrk_t
     * 
     * @see org..kuali.kfs.gl.batch.dataaccess.ReversionUnitOfWorkDao#destroyAllUnitOfWorkSummaries()
     */
    public void destroyAllUnitOfWorkSummaries() {
        LOG.info("Attempting to wipe out all unit of work summaries");
        getJdbcTemplate().update("delete from GL_RVRSN_CTGRY_AMT_T");
        getJdbcTemplate().update("delete from GL_RVRSN_UNIT_WRK_T");
        LOG.info("All unit of work summaries should be now removed");
    }

}

