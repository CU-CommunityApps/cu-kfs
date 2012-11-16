/*
 * Copyright 2008 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.cr.document.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.Date;
import java.util.List;

import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliationReport;
import com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao;
import com.rsmart.kuali.kfs.cr.document.service.CheckReconciliationReportService;

/**
 * Check Reconciliation Report Service Impl
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class CheckReconciliationReportServiceImpl implements CheckReconciliationReportService {

    private CheckReconciliationDao checkReconciliationDao;
    
    /**
     * Build Reports
     * 
     * @see com.rsmart.kuali.kfs.cr.document.service.CheckReconciliationReportService#buildReports(com.rsmart.kuali.kfs.cr.document.web.struts.CheckReconciliationForm)
     */
    public List<CheckReconciliationReport> buildReports(Date startDate, Date endDate) {
        List<CheckReconciliationReport> data = new ArrayList<CheckReconciliationReport>();
        
        List list = checkReconciliationDao.getAllCheckReconciliationForSearchCriteria(startDate,endDate);
        
        for(int i=0; i<list.size(); i++) {
            data.add(new CheckReconciliationReport( (CheckReconciliation)list.get(i) ) );
        }
        
        return data;
    }

    /**
     * Get Check Reconciliation Dao
     * 
     * @return CheckReconciliationDao
     */
    public CheckReconciliationDao getCheckReconciliationDao() {
        return checkReconciliationDao;
    }

    /**
     * Set Check Reconciliation Dao
     * 
     * @param checkReconciliationDao
     */
    public void setCheckReconciliationDao(CheckReconciliationDao checkReconciliationDao) {
        this.checkReconciliationDao = checkReconciliationDao;
    }

    
}
