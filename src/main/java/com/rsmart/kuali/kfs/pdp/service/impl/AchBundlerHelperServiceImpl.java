
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
package com.rsmart.kuali.kfs.pdp.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.dataaccess.PaymentDetailDao;
import org.kuali.kfs.sys.DynamicCollectionComparator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

/**
 * A custom service to help with ACH bundler modifications.
 */
public class AchBundlerHelperServiceImpl implements AchBundlerHelperService {
	private static final Logger LOG = LogManager.getLogger(AchBundlerHelperServiceImpl.class);
    
    /**
     * KFSPTS-1460: Changes made to code we originally received
     *   --method shouldBundleAchPayments was duplicated in class AchBundlerExtractPaymentServiceImpl and in class AchBundlerFormatServiceImpl.
     *     -created a single method in this class to be called from both the format and extract payment classes.
     *   --replaced the parameter name ACH_BUNDLER_ACTIVE_IND with parameter name ACH_PAYMENT_COMBINING_IND
     *   --replaced hard coded strings for the parameter look-up with constant references.
     *   --added local data element parameterService and its accessors.
     *   --added the following two methods so that ACH advice email bundling could be performed
     *     -getDistinctDisbursementNumbersForAchPaymentsNeedingAdviceNotification 
     */

    protected PaymentDetailDao paymentDetailDao;
    protected BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    
    /**
     * Returns all PaymentDetail records for pending ACH payments for a given bank code
     * @param bankCode the bank code of the payment group of payment details to find
     * @return an iterator of PaymentDetail records matching the given criteria
     */
    public Iterator<PaymentDetail> getPendingAchPaymentDetailsByDisbursementNumberAndBank(Integer disbursementNumber, String bankCode) {
        Map fieldValues = new HashMap();
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, PdpConstants.DisbursementTypeCodes.ACH);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYMENT_STATUS_CODE, PdpConstants.PaymentStatusCodes.PENDING_ACH);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_NBR, disbursementNumber);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BANK_CODE, bankCode);
        List<PaymentDetail> paymentDetailByDisbursementNumberList = (List<PaymentDetail>)this.businessObjectService.findMatching(PaymentDetail.class, fieldValues);
        DynamicCollectionComparator.sort(paymentDetailByDisbursementNumberList, PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_FINANCIAL_DOCUMENT_TYPE_CODE, PdpPropertyConstants.PaymentDetail.PAYMENT_DISBURSEMENT_CUST_PAYMENT_DOC_NBR);

        return paymentDetailByDisbursementNumberList.iterator();
    }
    
    /**
     * Finds all of the distinct bank codes for pending ACH payments.
     * @return a unique sorted List of bank codes
     */
    public HashSet<String> getDistinctBankCodesForPendingAchPayments() {
        Map fieldValues = new HashMap();
        fieldValues.put(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, PdpConstants.DisbursementTypeCodes.ACH);
        fieldValues.put(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYMENT_STATUS_CODE, PdpConstants.PaymentStatusCodes.PENDING_ACH);
        List<PaymentGroup> paymentGroupList = (List<PaymentGroup>)getBusinessObjectService().findMatching(PaymentGroup.class, fieldValues);
        
        //used to pair down to distinct bank codes
        HashSet<String> results = new HashSet<String>();
        
        for (PaymentGroup pg : paymentGroupList) {
            results.add(pg.getBankCode());
        }
        
        return results;
    }
    
    /**
     * Finds all of the distinct disbursement numbers for pending ACH payments by a specific bankd code.
     * @see com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService#getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(java.lang.String)
     */
    public HashSet<Integer> getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(String bankCode) {
        Map fieldValues = new HashMap();
        fieldValues.put(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, PdpConstants.DisbursementTypeCodes.ACH);
        fieldValues.put(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYMENT_STATUS_CODE, PdpConstants.PaymentStatusCodes.PENDING_ACH);
        fieldValues.put(PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BANK_CODE, bankCode);
        List<PaymentGroup> paymentGroupList = (List<PaymentGroup>)getBusinessObjectService().findMatching(PaymentGroup.class, fieldValues);
        
        //used to pair down to distinct bank codes
        HashSet<Integer> results = new HashSet<Integer>();
        
        for (PaymentGroup pg : paymentGroupList) {
            results.add(new Integer(pg.getDisbursementNbr().toString()));
        }
        
        return results;
    }
    
    /**
     * Returns all PaymentDetail records for pending ACH payments for a given bank code
     * @param bankCode the bank code of the payment group of payment details to find
     * @return an iterator of PaymentDetail records matching the given criteria
     */
    public Iterator<PaymentDetail> getPendingAchPaymentDetailsByBank(String bankCode) {
        Map fieldValues = new HashMap();
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_DISBURSEMENT_TYPE_CODE, PdpConstants.DisbursementTypeCodes.ACH);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PAYMENT_STATUS_CODE, PdpConstants.PaymentStatusCodes.PENDING_ACH);
        fieldValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_GROUP+"."+PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_BANK_CODE, bankCode);
        List<PaymentDetail> paymentDetailList = (List<PaymentDetail>)this.businessObjectService.findMatching(PaymentDetail.class, fieldValues);

        return paymentDetailList.iterator();
    }

    public PaymentDetailDao getPaymentDetailDao() {
        return paymentDetailDao;
    }

    public void setPaymentDetailDao(PaymentDetailDao paymentDetailDao) {
        this.paymentDetailDao = paymentDetailDao;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
    //KFSPTS-1460: Refactored code to have this single method.
    /**
     * Retrieves whether or not the ACH Bundler system parameter is set to Y or not.
     * 
     * @return
     */
    public boolean shouldBundleAchPayments() {
        boolean bundle = false;
        
        try {
            bundle = getParameterService().getParameterValueAsBoolean(CUKFSParameterKeyConstants.KFS_PDP, CUKFSParameterKeyConstants.ALL_COMPONENTS, CUKFSParameterKeyConstants.ACH_PAYMENT_COMBINING_IND);
        } catch(Exception e) {
            LOG.error("AchBundlerHelperServiceImpl MOD: shouldBundleAchPayments() The " + CUKFSParameterKeyConstants.KFS_PDP + ":" + CUKFSParameterKeyConstants.ALL_COMPONENTS + ":" + CUKFSParameterKeyConstants.ACH_PAYMENT_COMBINING_IND + "system parameter was not found registered in the system.");
        }
        
        return bundle;
    }

    public String getPdpFormatFailureToEmailAddress() {
        return getParameterService().getParameterValueAsString(CUKFSParameterKeyConstants.KFS_PDP, KfsParameterConstants.BATCH_COMPONENT, CUKFSParameterKeyConstants.PDP_FORMAT_FAILURE_TO_EMAIL_ADDRESS, KFSConstants.EMPTY_STRING);
    }
    
    //KFSPTS-1460: Added
    /**
     * This method retrieves the parameterService
     * 
     * @return
     */
    protected ParameterService getParameterService() {
        return parameterService;
    }
    
    //KFSPTS-1460: Added
    /**
     * This method sets the parameterService
     * 
     * @param parameterService
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
}
