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
package com.rsmart.kuali.kfs.cr.batch;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.service.PendingTransactionService;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.bo.KualiCode;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.document.service.GlTransactionService;

import edu.cornell.kfs.pdp.businessobject.PaymentDetailExtendedAttribute;
import edu.cornell.kfs.pdp.service.CuPendingTransactionService;

/**
 * GlTransactionStep
 * 
 * @author Derek Helbert
 */
public class GlTransactionStep extends AbstractStep {

	private static final Logger LOG = LogManager.getLogger(GlTransactionStep.class);
    
    private GlTransactionService glTransactionService;
    
    private BusinessObjectService businessObjectService;
    
    private CuPendingTransactionService glPendingTransactionService;
    

    /**
     * Execute
     * 
     * @param jobName Job Name
     * @param jobRunDate Job Date
     * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("Started GlTransactionStep @ " + (new Date()).toString());
        
        LOG.info("Get Bank List");
        Collection<Bank> banks = businessObjectService.findAll(Bank.class);
        
        List<String> bankCodes = null;
        Collection<PaymentGroup> paymentGroups = null;
        Map<String,Object> fieldValues = null;
        Collection<CheckReconciliation> records = null;
        
        // Stop payments
        fieldValues = new HashMap<String,Object>();
        fieldValues.put("glTransIndicator", "N");
        fieldValues.put("status", CRConstants.STOP);
        fieldValues.put("sourceCode", CRConstants.PDP_SRC);
        fieldValues.put("active", true);

        records = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
            
        for(CheckReconciliation cr : records) {
            bankCodes = new ArrayList<String>();
            
            // Generate list of valid bank codes
            setBankCodes(banks, cr, bankCodes);
            
            if( bankCodes.size() > 0 ) {
                paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
                
                if( paymentGroups.isEmpty() ) {
                    LOG.warn("No payment group found id : " + cr.getId() );
                }
                else {
                    for (PaymentGroup paymentGroup : paymentGroups) {
                        // KFSUPGRADE-636
                        //Create cancellation offsets for STOPed checks. KFSPTS-1741
                        glPendingTransactionService.generateStopGeneralLedgerPendingEntry(paymentGroup);
                        
                        //glTransactionService.generateGlPendingTransactionStop(paymentGroup);
                        
                        KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, cr.getStatus());
                        if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                            paymentGroup.setPaymentStatus((PaymentStatus) code);
                        }
                        Date lastUpdate = cr.getStatusChangeDate();
                        if(ObjectUtils.isNull(lastUpdate)) {
                            lastUpdate = new Date();
                        }
                        paymentGroup.setLastUpdatedTimestamp(new Timestamp(lastUpdate.getTime()));
                        businessObjectService.save(paymentGroup);
                    
                        // Update status
                        cr.setGlTransIndicator(Boolean.TRUE);
                        businessObjectService.save(cr);
                    
                        LOG.info("Generated Stop GL Pending Transacation");        
                    }
                }
            }
        }
        
        // Canceled payments
        fieldValues = new HashMap<String,Object>();
        fieldValues.put("glTransIndicator", "N");
        fieldValues.put("status", CRConstants.CANCELLED);
        fieldValues.put("sourceCode", CRConstants.PDP_SRC);
        fieldValues.put("active", true);

        records = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
            
        for(CheckReconciliation cr : records) {
            bankCodes = new ArrayList<String>();
            
            // Generate list of valid bank codes
            setBankCodes(banks, cr, bankCodes);
            
            if( bankCodes.size() > 0 ) {
                paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
                
                if( paymentGroups.isEmpty() ) {
                    LOG.warn("No payment group found id : " + cr.getId() );
                }
                else {
                    for (PaymentGroup paymentGroup : paymentGroups) {
                      //KFSPTS-2260
                    	glPendingTransactionService.generateCRCancellationGeneralLedgerPendingEntry(paymentGroup);
                        //glTransactionService.generateGlPendingTransactionCancel(paymentGroup);
                    
                        KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, cr.getStatus());
                        if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                            paymentGroup.setPaymentStatus((PaymentStatus) code);
                        }
                        Date lastUpdate = cr.getStatusChangeDate();
                        if(ObjectUtils.isNull(lastUpdate)) {
                            lastUpdate = new Date();
                        }
                        paymentGroup.setLastUpdatedTimestamp(new Timestamp(lastUpdate.getTime()));
                        businessObjectService.save(paymentGroup);

						// update cancel flag on payment details
						for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
							// paymentDetail.refreshReferenceObject("extension");
							PaymentDetailExtendedAttribute extendedAttribute = (PaymentDetailExtendedAttribute) paymentDetail
									.getExtension();
							extendedAttribute
									.setCrCancelledPayment(Boolean.TRUE);
							businessObjectService.save(paymentDetail);
						}
                    
                        // Update status
                        cr.setGlTransIndicator(Boolean.TRUE);
                    
                        businessObjectService.save(cr);
                        LOG.info("Generated Cancelled GL Pending Transacation");        
                    }
                }
            }
        }

        // VOID payments
        fieldValues = new HashMap<String,Object>();
        fieldValues.put("glTransIndicator", "N");
        fieldValues.put("status", CRConstants.VOIDED);
        fieldValues.put("sourceCode", CRConstants.PDP_SRC);
        fieldValues.put("active", true);

        try {
            records = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
        } catch (RuntimeException e) {
            logClassLoaderDebugInfo();
            throw e;
        }
            
        for(CheckReconciliation cr : records) {
            bankCodes = new ArrayList<String>();
            
            // Generate list of valid bank codes
            setBankCodes(banks, cr, bankCodes);
    
           if( bankCodes.size() > 0 ) {
                paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
                
                if( paymentGroups.isEmpty() ) {
                    LOG.warn("No payment group found id : " + cr.getId() );
                }
                else {
                    for (PaymentGroup paymentGroup : paymentGroups) {
                        //Do not generate GL tarsactions for VIODED trasactions 

//                        glTransactionService.generateGlPendingTransactionStop(paymentGroup);
                    
                        KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, cr.getStatus());
                        if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                            paymentGroup.setPaymentStatus((PaymentStatus) code);
                        }
                        Date lastUpdate = cr.getStatusChangeDate();
                        if(ObjectUtils.isNull(lastUpdate)) {
                            lastUpdate = new Date();
                        }
                        paymentGroup.setLastUpdatedTimestamp(new Timestamp(lastUpdate.getTime()));
                        businessObjectService.save(paymentGroup);
                    
                        // Update status
                        cr.setGlTransIndicator(Boolean.TRUE);
                        businessObjectService.save(cr);
                    
                        LOG.info("Generated VOID GL Pending Transacation");        
                    }
                }
            }
        }
        

        
        
        
        // Stale payments
        fieldValues = new HashMap<String,Object>();
        fieldValues.put("glTransIndicator", "N");
        fieldValues.put("status", CRConstants.STALE);
        fieldValues.put("sourceCode", CRConstants.PDP_SRC);
        fieldValues.put("active", true);

        records = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
            
        for(CheckReconciliation cr : records) {
            bankCodes = new ArrayList<String>();
            
            // Generate list of valid bank codes
            setBankCodes(banks, cr, bankCodes);
            
            if( bankCodes.size() > 0 ) {
                paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
                
                if( paymentGroups.isEmpty() ) {
                    LOG.warn("No payment group found id : " + cr.getId() );
                }
                else {
                     for (PaymentGroup paymentGroup : paymentGroups) {
                         
                         //KFSPTS-2246
                    	 glPendingTransactionService.generateStaleGeneralLedgerPendingEntry(paymentGroup);
                         //glPendingTransactionService.g .generateStaleGeneralLedgerPendingEntry(paymentGroup);
                    
                         KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, cr.getStatus());
                         if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                             paymentGroup.setPaymentStatus((PaymentStatus) code);
                         }
                         Date lastUpdate = cr.getStatusChangeDate();
                         if(ObjectUtils.isNull(lastUpdate)) {
                            lastUpdate = new Date();
                         }
                         paymentGroup.setLastUpdatedTimestamp(new Timestamp(lastUpdate.getTime()));
                         businessObjectService.save(paymentGroup);
                    
                         // Update status
                         cr.setGlTransIndicator(Boolean.TRUE);
                         businessObjectService.save(cr);
                    
                         LOG.info("Generated Stale GL Pending Transacation");        
                     }
                }
            }
        }
        
        LOG.info("Completed GlTransactionStep @ " + (new Date()).toString());

        return true;
    }
    
    private void logClassLoaderDebugInfo() {
        LOG.info("logClassLoaderDebugInfo:: OJB Broker ClassHelper ClassLoader: {}", org.apache.ojb.broker.util.ClassHelper.getClassLoader());
        
        Class repoClass = org.apache.ojb.broker.metadata.ClassDescriptor.class;
        LOG.info("logClassLoaderDebugInfo:: Repository Class Loader: {}", repoClass.getClassLoader());

        Class bankClass = org.kuali.kfs.sys.businessobject.Bank.class;
        LOG.info("logClassLoaderDebugInfo:: Bank Class Loader: {}", bankClass.getClassLoader());
    }

    /**
     * Set Bank Codes List
     * 
     * @param banks
     * @param cr
     * @param bankCodes
     */
    private void setBankCodes(Collection<Bank> banks, CheckReconciliation cr, List<String> bankCodes) {
        for( Bank bank : banks ) {
            if( bank.getBankAccountNumber().equals(cr.getBankAccountNumber()) ) {
                bankCodes.add(bank.getBankCode());
            }
        }
    }

    /**
     * Get GlTransactionService
     * 
     * @return GlTransactionService
     */
    public GlTransactionService getGlTransactionService() {
        return glTransactionService;
    }

    /**
     * Set GlTransactionService
     * 
     * @param glTransactionService
     */
    public void setGlTransactionService(GlTransactionService glTransactionService) {
        this.glTransactionService = glTransactionService;
    }

    /**
     * Get BusinessObjectService
     * 
     * @return BusinessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Set BusinessObjectService
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
    /**
     * Gets the glPendingTransactionService.
     * 
     * @return glPendingTransactionService
     */
    public CuPendingTransactionService getGlPendingTransactionService() {
        return glPendingTransactionService;
    }

    /**
     * Sets the glPendingTransactionService.
     * 
     * @param glPendingTransactionService
     */
    public void setGlPendingTransactionService(CuPendingTransactionService glPendingTransactionService) {
        this.glPendingTransactionService = glPendingTransactionService;
    }
    
}