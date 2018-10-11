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
package com.rsmart.kuali.kfs.cr.dataaccess;

import java.sql.Date;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.rice.core.api.util.type.KualiInteger;

import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

/**
 * Check Reconciliation Dao
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public interface CheckReconciliationDao {

    /**
     * Get Canceled Checks
     * 
     * @return Collection
     */
    public Collection<Integer> getCanceledChecks();
    
    /**
     * Get All
     * 
     * @return List
     */
    public List getAll();
    
    /**
     * Get New Check Reconciliations
     * 
     * @return Collection
     */
    public Collection<CheckReconciliation> getNewCheckReconciliations(Collection<Bank> banks);
    
    /**
     * Get AllCheckReconciliationForSearchCriteria
     * 
     * @param startDate
     * @param endDate
     * 
     * @return List
     */
    public List getAllCheckReconciliationForSearchCriteria(Date startDate, Date endDate);
    
    /**
     * Get AllPaymentGroupForSearchCriteria
     * 
     * @param disbNbr
     * @param bankCodes
     * 
     * @return
     */
    public List<PaymentGroup> getAllPaymentGroupForSearchCriteria(KualiInteger disbNbr, Collection<String> bankCodes);

    public CheckReconciliation findByCheckNumber(String checkNumber, String bankCode);
}
