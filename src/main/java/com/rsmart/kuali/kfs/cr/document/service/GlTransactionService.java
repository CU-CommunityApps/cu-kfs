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
package com.rsmart.kuali.kfs.cr.document.service;

import java.util.Collection;
import java.util.List;

import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

/**
 * GlTransactionService
 * 
 * @author Derek Helbert
 */
public interface GlTransactionService {

    /**
     * Get Canceled Checks
     * 
     * @return Collection
     */
    public Collection<Integer> getCanceledChecks();
    
    /**
     * Generate GlPendingTransaction Stale
     * 
     * @param paymentGroup
     */
    public void generateGlPendingTransactionStale(PaymentGroup paymentGroup);
    
    /**
     * Generate GlPendingTransaction Cancel
     * 
     * @param paymentGroup
     */
    public void generateGlPendingTransactionCancel(PaymentGroup paymentGroup);
    
    /**
     * Generate GlPendingTransaction Stop
     * 
     * @param paymentGroup
     */
    public void generateGlPendingTransactionStop(PaymentGroup paymentGroup);
    
    /**
     * Get New Check Reconciliations
     * 
     * @return Collection
     */
    public Collection<CheckReconciliation> getNewCheckReconciliations(Collection<Bank> banks);
    
    /**
     * Get AllPaymentGroupForSearchCriteria
     * 
     * @param disbNbr
     * @param bankCodes
     * 
     * @return List
     */
    public List<PaymentGroup> getAllPaymentGroupForSearchCriteria(KualiInteger disbNbr, Collection bankCodes);
}
