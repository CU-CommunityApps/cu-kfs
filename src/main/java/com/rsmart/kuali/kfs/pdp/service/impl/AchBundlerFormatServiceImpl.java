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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.DisbursementNumberRange;
import org.kuali.kfs.pdp.businessobject.FormatProcessSummary;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.pdp.service.PendingTransactionService;
import org.kuali.kfs.pdp.service.impl.exception.FormatException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.springframework.transaction.annotation.Transactional;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;
import edu.cornell.kfs.pdp.service.impl.CuFormatServiceImpl;

/**
 * SEE THE VERY BOTTOM OF THIS FILE FOR THE MODS.  Everything else is out of the box.
 * @todo Figure out how to use extends of the FormatServiceImpl class instead of copy and paste.  Main 
 * issue is private members on parent not exposed with getters.
 */
@Transactional
public class AchBundlerFormatServiceImpl extends CuFormatServiceImpl {
	private static final Logger LOG = LogManager.getLogger(AchBundlerFormatServiceImpl.class);
    
    
    /**
     * KFSPTS-1460:
     * Differences between this file and the file of the same name received from UConn are:
     *   --implements FormatService changed to extends FormatService.
     *   --all code duplicated from FormatServiceImpl was removed.
     *   --removed all privately declared data elements that already existed in the parent class
     *   --removed locally declared accessor methods for those privately declared data items
     *   --modified the scope in the parent class from private to protected for the private data items that were removed and still needed.
     *   --changed directly used data items to accessors.
     *   --method shouldBundleAchPayments was duplicated in this class and in class AchBundlerExtractPaymentServiceImpl.
     *     -created a single method in class AchBundlerHelperService and to be called from both the format and extract payment classes.
     *     -added private service variable achBundlerHelperService
     *     -added accessor methods
     */
    
    protected AchBundlerHelperService achBundlerHelperService;

    /**
     * Constructs a FormatServiceImpl.java.
     */
    public AchBundlerFormatServiceImpl() {
        super();
    }

    
    /**************************************************************************************************
     MODS START HERE
     **************************************************************************************************/
    
    /**
     * MOD: Overridden to detect if the Bundle ACH Payments system parameter is on and if so, to 
     * call the new helper method 
     * @see org.kuali.kfs.pdp.service.FormatService#performFormat(java.lang.Integer)
     */
    @Override
    public void performFormat(Integer processId) throws FormatException {
        LOG.info("performFormat() started - ACH Bundler Mod for processId {}", processId);
        final String pdpFormatFailureToEmailAddress = getAchBundlerHelperService().getPdpFormatFailureToEmailAddress();

        // get the PaymentProcess for the given id
        @SuppressWarnings("rawtypes")
        Map primaryKeys = new HashMap();
        primaryKeys.put(PdpPropertyConstants.PaymentProcess.PAYMENT_PROCESS_ID, processId);
        PaymentProcess paymentProcess = (PaymentProcess) businessObjectService.findByPrimaryKey(PaymentProcess.class, primaryKeys);
        if (paymentProcess == null) {
            LOG.error("performFormat() Invalid proc ID " + processId);
            throw new RuntimeException("Invalid proc ID");
        }
        
        String processCampus = paymentProcess.getCampusCode();
        FormatProcessSummary postFormatProcessSummary = new FormatProcessSummary();

        // step 1 get ACH or Check, Bank info, ACH info, sorting
        Iterator<PaymentGroup> paymentGroupIterator = SpringContext.getBean(PaymentGroupService.class).getByProcess(paymentProcess);
        while (paymentGroupIterator.hasNext()) {
            PaymentGroup paymentGroup = paymentGroupIterator.next();
            LOG.debug("performFormat() Step 1 Payment Group ID " + paymentGroup.getId());

            // process payment group data
            boolean groupProcessed = processPaymentGroup(paymentGroup, paymentProcess, false);
            if (!groupProcessed) {
                LOG.info("Sending failure email to {}", pdpFormatFailureToEmailAddress);
                sendFailureEmail(pdpFormatFailureToEmailAddress, processId);
                throw new FormatException("Error encountered during format");
            }

            // save payment group
            businessObjectService.save(paymentGroup);

            // Add to summary information
            postFormatProcessSummary.add(paymentGroup);
        }

        /** 
         * MOD: This mod calls a new method that bundles both Checks 
         * and ACHs into single disbursements.
         */
        boolean disbursementNumbersAssigned = false;
        if(getAchBundlerHelperService().shouldBundleAchPayments()) {
            LOG.info("ACH BUNDLER MOD: ACTIVE - bundling ACH payments for processId {}", processId);
            disbursementNumbersAssigned = assignDisbursementNumbersAndBundle(paymentProcess, postFormatProcessSummary);
        } else {
        	//KFSPTS-1460: Our method signature for FormatServiceImpl.assignDisbursementNumbersAndCombineChecks did not
        	//match this method call: disbursementNumbersAssigned = assignDisbursementNumbersAndCombineChecks(paymentProcess, postFormatProcessSummary);
        	//Added parameter "processCampus" so method signatures matched.
        	LOG.info("ACH BUNDLER MOD: NOT Active - ACH payments will NOT be bundled for processId {}", processId);
            disbursementNumbersAssigned = assignDisbursementNumbersAndCombineChecks(paymentProcess, postFormatProcessSummary);
            
        }
        /** END MOD */
        
        if (!disbursementNumbersAssigned) {
            sendFailureEmail(pdpFormatFailureToEmailAddress, processId);
            throw new FormatException("Error encountered during format for processId " + processId);
        }

        // step 3 save the summarizing info
        LOG.info("performFormat() Save summarizing information for processId {}", processId);
        postFormatProcessSummary.save();

        // step 4 set formatted indicator to true and save in the db
        paymentProcess.setFormattedIndicator(true);
        businessObjectService.save(paymentProcess);

        // step 5 end the format process for this campus
        LOG.info("performFormat() End the format process for this campus, processId {}", processId);
        endFormatProcess(processCampus);

        /**
         * MOD: No longer automatically kick off the extractChecks process.  This has to be scheduled in the batch system or run manually there.
         * Otherwise, may conflict with grouping by payee done.
         */
        // step 6 tell the extract batch job to start
        // LOG.debug("performFormat() Start extract");
        // extractChecks();

        LOG.info("Send summary email for processId: {}", processId);
        sendSummaryEmail(postFormatProcessSummary);
    }
    
    /**
     * MOD: This method assigns disbursement numbers and tries to combine payment groups with disbursement type check and ACH if possible.
     * 
     * @param paymentProcess
     * @param postFormatProcessSummary
     */
    protected boolean assignDisbursementNumbersAndBundle(PaymentProcess paymentProcess, FormatProcessSummary postFormatProcessSummary) {
        boolean successful = true;

        // keep a map with paymentGroupKey and PaymentInfo (disbursementNumber, noteLines)
        Map<String, PaymentInfo> combinedPaymentGroupMap = new HashMap<String, PaymentInfo>();

        Iterator<PaymentGroup> paymentGroupIterator = SpringContext.getBean(PaymentGroupService.class).getByProcess(paymentProcess);
        int maxNoteLines = getMaxNoteLines();

        while (paymentGroupIterator.hasNext()) {
            PaymentGroup paymentGroup = paymentGroupIterator.next();
            LOG.debug("performFormat() Payment Group ID " + paymentGroup.getId());

            //Use the customer's profile's campus code to check for disbursement ranges
            String campus = paymentGroup.getBatch().getCustomerProfile().getFormatCampusCode();
            
            //Where should this come from?
            List<DisbursementNumberRange> disbursementRanges = paymentDetailDao.getDisbursementNumberRanges(campus);
            
            DisbursementNumberRange range = getRange(disbursementRanges, paymentGroup.getBank(), paymentGroup.getDisbursementType().getCode());

            if (range == null) {
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, PdpKeyConstants.Format.ErrorMessages.ERROR_FORMAT_DISBURSEMENT_MISSING, campus, paymentGroup.getBank().getBankCode(), paymentGroup.getDisbursementType().getCode());
                successful = false;
                return successful;
            }

            if (PdpConstants.DisbursementTypeCodes.CHECK.equals(paymentGroup.getDisbursementType().getCode()) || 
                    PdpConstants.DisbursementTypeCodes.ACH.equals(paymentGroup.getDisbursementType().getCode())) {

                if (paymentGroup.getPymtAttachment().booleanValue() || paymentGroup.getProcessImmediate().booleanValue() || paymentGroup.getPymtSpecialHandling().booleanValue() || (!paymentGroup.getCombineGroups())) {
                    assignDisbursementNumber(campus, range, paymentGroup, postFormatProcessSummary);
                }
                else {
                    String paymentGroupKey = paymentGroup.toStringKey();
                    // check if there was another paymentGroup we can combine with
                    if (combinedPaymentGroupMap.containsKey(paymentGroupKey)) {
                        PaymentInfo paymentInfo = combinedPaymentGroupMap.get(paymentGroupKey);
                        paymentInfo.noteLines = paymentInfo.noteLines.add(new KualiInteger(paymentGroup.getNoteLines()));

                        // if ACH OR 
                        // if CHECK and noteLines don't excede the maximum assign the same disbursementNumber
                        if (PdpConstants.DisbursementTypeCodes.ACH.equals(paymentGroup.getDisbursementType().getCode()) || 
                                (PdpConstants.DisbursementTypeCodes.CHECK.equals(paymentGroup.getDisbursementType().getCode()) && paymentInfo.noteLines.intValue() <= maxNoteLines)) {
                            paymentGroup.setDisbursementNbr(paymentInfo.disbursementNumber);

                            // update payment info for new noteLines value
                            combinedPaymentGroupMap.put(paymentGroupKey, paymentInfo);
                        }
                        // if noteLines more than maxNoteLines we remove the old entry and get a new disbursement number
                        else if (PdpConstants.DisbursementTypeCodes.CHECK.equals(paymentGroup.getDisbursementType().getCode()) && paymentInfo.noteLines.intValue() > maxNoteLines) {
                            // remove old entry for this paymentGroupKey
                            combinedPaymentGroupMap.remove(paymentGroupKey);

                            // get a new check number and the paymentGroup noteLines
                            KualiInteger checkNumber = assignDisbursementNumber(campus, range, paymentGroup, postFormatProcessSummary);
                            int noteLines = paymentGroup.getNoteLines();

                            // create new payment info with these two
                            paymentInfo = new PaymentInfo(checkNumber, new KualiInteger(noteLines));

                            // add new entry in the map for this paymentGroupKey
                            combinedPaymentGroupMap.put(paymentGroupKey, paymentInfo);

                        }
                        else {
                            // if it isn't check or ach, we're in trouble
                            LOG.error("assignDisbursementNumbersAndBundle() Payment group " + paymentGroup.getId() + " must be CHCK or ACH.  It is: " + paymentGroup.getDisbursementType());
                            throw new IllegalArgumentException("Payment group " + paymentGroup.getId() + " must be Check or ACH");
                        }
                    }
                    // if no entry in the map for this payment group we create a new one
                    else {
                        // get a new disbursement number and the paymentGroup noteLines
                        KualiInteger disbursementNumber = assignDisbursementNumber(campus, range, paymentGroup, postFormatProcessSummary);
                        int noteLines = paymentGroup.getNoteLines();

                        // create new payment info with these two
                        PaymentInfo paymentInfo = new PaymentInfo(disbursementNumber, new KualiInteger(noteLines));

                        // add new entry in the map for this paymentGroupKey
                        combinedPaymentGroupMap.put(paymentGroupKey, paymentInfo);
                    }
                }
            }
            else {
                // if it isn't check or ach, we're in trouble
                LOG.error("assignDisbursementNumbers() Payment group " + paymentGroup.getId() + " must be CHCK or ACH.  It is: " + paymentGroup.getDisbursementType());
                throw new IllegalArgumentException("Payment group " + paymentGroup.getId() + " must be Check or ACH");
            }

            businessObjectService.save(paymentGroup);

            // Generate a GL entry for CHCK & ACH
            SpringContext.getBean(PendingTransactionService.class).generatePaymentGeneralLedgerPendingEntry(paymentGroup); 
            
            // Update all the ranges
            LOG.debug("assignDisbursementNumbers() Save ranges");
            for (DisbursementNumberRange element : disbursementRanges) {
                businessObjectService.save(element);
            }
        }

        return successful;
    }
    
    
    public AchBundlerHelperService getAchBundlerHelperService() {
        return achBundlerHelperService;
    }

    public void setAchBundlerHelperService(AchBundlerHelperService achBundlerHelperService) {
        this.achBundlerHelperService = achBundlerHelperService;
    }
    /**************************************************************************************************
    MODS END HERE
    **************************************************************************************************/
}

