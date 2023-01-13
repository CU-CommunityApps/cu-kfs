/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.gl.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.gl.batch.dataaccess.ReversionUnitOfWorkDao;
import edu.cornell.kfs.gl.batch.service.impl.ReversionUnitOfWorkServiceImpl;

/**
 * The base implementation of OrganizationReversionUnitOfWorkService
 */
@Transactional
public class OrganizationReversionUnitOfWorkServiceImpl extends ReversionUnitOfWorkServiceImpl  {
    private static final Logger LOG = LogManager.getLogger();
    private ReversionUnitOfWorkDao orgReversionUnitOfWorkDao;

    /**
     * Immediate deletion awaits all entries of the unit of work summary tables in the persistence store once
     * you call this method, for this method is both powerful and deadly and also gets called to clear out
     * those tables before every single org reversion run.
     */
    @Override
    public void destroyAllUnitOfWorkSummaries() {
        orgReversionUnitOfWorkDao.destroyAllUnitOfWorkSummaries();
    }

    public ReversionUnitOfWorkDao getOrgReversionUnitOfWorkDao() {
        return orgReversionUnitOfWorkDao;
    }

    public void setOrgReversionUnitOfWorkDao(ReversionUnitOfWorkDao orgReversionUnitOfWorkDao) {
        this.orgReversionUnitOfWorkDao = orgReversionUnitOfWorkDao;
    }

}
