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
package com.rsmart.kuali.kfs.cr.document.web.struts;

import java.sql.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.form.KualiForm;

/**
 * Check Reconciliation Action
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class CheckReconciliationReportForm extends KualiForm {

    private Date startDate;
    
    private Date endDate;

    private String format;
    
    /**
     * Get Start Date
     * 
     * @return Date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set Start Date
     * 
     * @param startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Get End Date
     * 
     * @return Date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set End Date
     * 
     * @param endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    /**
     * Get Format
     * 
     * @return String
     */
    public String getFormat() {
        return format;
    }

    /**
     * Set Format
     * 
     * @param format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Validate
     * 
     * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors ae = new ActionErrors();
        
        return ae;
    }
}
