
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
package com.rsmart.kuali.kfs.pdp.batch.service.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.batch.service.impl.ExtractPaymentServiceImpl;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.rice.kns.bo.Country;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

/**
 * MOD: this class is responsible for detecting if bundled ACH payments (by payee/disb nbr) are desired via the setting of the 
 * system parameter to Y, and if so, bundles the extracted payments.
 */
@Transactional
public class AchBundlerExtractPaymentServiceImpl extends ExtractPaymentServiceImpl {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AchBundlerExtractPaymentServiceImpl.class);
    
    /**
     * KFSPTS-1460:
     * Differences between this file and the file of the same name received from UConn are:
     *   --implements ExtractPaymentService removed.
     *   --method shouldBundleAchPayments was duplicated in this class and in class AchBundlerFormatServiceImpl.
     *     -created a single method in class AchBundlerHelperService and to be called from both the format and extract payment classes.
     */
    
    public AchBundlerExtractPaymentServiceImpl() {
        super();
    }

    /**
     * MOD: Overridden to detect if the Bundle ACH Payments system parameter is on and if so, to 
     * call the new extraction bundler method
     * @see org.kuali.kfs.pdp.batch.service.ExtractPaymentService#extractAchPayments()
     */
    @Override
    public void extractAchPayments() {
        LOG.debug("AchBundlerExtractPaymentServiceImpl MOD - extractAchPayments() started");

        Date processDate = getDateTimeService().getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        PaymentStatus extractedStatus = (PaymentStatus) getBusinessObjectService().findBySinglePrimaryKey(PaymentStatus.class, PdpConstants.PaymentStatusCodes.EXTRACTED);

        String achFilePrefix = getKualiConfigurationService().getPropertyString(PdpKeyConstants.ExtractPayment.ACH_FILENAME);
        achFilePrefix = MessageFormat.format(achFilePrefix, new Object[] { null });

        String filename = getOutputFile(achFilePrefix, processDate);
        LOG.debug("AchBundlerExtractPayment MOD: extractAchPayments() filename = " + filename);

        /** 
         * MOD: This is the only section in the method that is changed.  This mod calls a new method that bundles 
         * ACHs into single disbursements if the flag to do so is turned on.
         */
        if(getAchBundlerHelperService().shouldBundleAchPayments()) {
            writeExtractBundledAchFile(extractedStatus, filename, processDate, sdf);
        } else {
            writeExtractAchFile(extractedStatus, filename, processDate, sdf);
        }
    }
    
    
    /**
     * KFSPTS-1460: 
     * Re-factored 
     * Changes made to this method due to re-factoring the code so that common pieces could be used 
     * by both ExtractPaymentServiceImpl.writeExtractAchFile and AchBundlerExtractPaymentServiceImpl.writeExtractBundledAchFile
     * as well as incorporating the Mellon file creation.
     * --Added the call to method writeExtractAchFileMellonBankFastTrack
     * --Added the call to writePayeeSpecificsToAchFile for re-factored code
     * --Added the call to writePaymentDetailToAchFile for re-factored code
     * --Made the "finally" clause match the ExtractPaymentServiceImpl.writeExtractAchFile finally so that the XML files are named the same regardless of which routine is invoked.
     * --Added call to get the parameterized bank notification email addresses
     */
    /**
     * A custom method that goes through and extracts all pending ACH payments and bundles them by payee/disbursement nbr.
     * 
     * @param extractedStatus
     * @param filename
     * @param processDate
     * @param sdf
     */
    protected void writeExtractBundledAchFile(PaymentStatus extractedStatus, String filename, Date processDate, SimpleDateFormat sdf) {
    	LOG.info("AchBundledExtractPaymentServiceImpl.writeExtractBundledAchFile started.");
        BufferedWriter os = null;

        try {
        	
        	//KFSPTS-1460: parameterized the hard coded email addresses
        	List<String> notificationEmailAddresses = super.getBankPaymentFileNotificationEmailAddresses();  
        	
            // Writes out the BNY Mellon Fast Track formatted file for ACH payments.  We need to do this first since the status is set in this method which
        	//   causes the writeExtractAchFileMellonBankFastTrack method to not find anything.
        	writeExtractAchFileMellonBankFastTrack(extractedStatus, filename, processDate, sdf, notificationEmailAddresses);
        	
            // totals for summary
            Map<String, Integer> unitCounts = new HashMap<String, Integer>();
            Map<String, KualiDecimal> unitTotals = new HashMap<String, KualiDecimal>();
        	
            os = new BufferedWriter(new FileWriter(filename));
            os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writeOpenTag(os, 0, "achPayments");            

            HashSet<String> bankCodes = getAchBundlerHelperService().getDistinctBankCodesForPendingAchPayments();

            for (String bankCode : bankCodes) {
                HashSet<Integer> disbNbrs = getAchBundlerHelperService().getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(bankCode);
                for (Iterator<Integer> iter = disbNbrs.iterator(); iter.hasNext();) {
                    Integer disbursementNbr = iter.next();

                    boolean first = true;

                    KualiDecimal totalNetAmount = new KualiDecimal(0);

                    // this seems wasteful, but since the total net amount is needed on the first payment detail...it's needed
                    Iterator<PaymentDetail> i2 = getAchBundlerHelperService().getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
                    while (i2.hasNext()) {
                        PaymentDetail pd = i2.next();
                        totalNetAmount = totalNetAmount.add(pd.getNetPaymentAmount());
                    }

                    Iterator<PaymentDetail> paymentDetails = getAchBundlerHelperService().getPendingAchPaymentDetailsByDisbursementNumberAndBank(disbursementNbr, bankCode);
                    while (paymentDetails.hasNext()) {
                        PaymentDetail paymentDetail = paymentDetails.next();
                        PaymentGroup paymentGroup = paymentDetail.getPaymentGroup();
                        if (!testMode) {
                        	paymentGroup.setDisbursementDate(new java.sql.Date(processDate.getTime()));
                        	paymentGroup.setPaymentStatus(extractedStatus);
                            getBusinessObjectService().save(paymentGroup);
                        }

                        if (first) {
                        	writePayeeSpecificsToAchFile(os, paymentGroup, processDate, sdf);  //KFSPTS-1460: re--factored

                            writeOpenTag(os, 4, "payments");
                        }
                                               
                        writePaymentDetailToAchFile(os, paymentGroup, paymentDetail, unitCounts, unitTotals, sdf);   //KFSPTS-1460: re-factored
                        
                        first = false;
                    }
                    writeCloseTag(os, 4, "payments");
                    writeCloseTag(os, 2, "ach");   //open for this tag is in method writePayeeSpecificsToAchFile
                }
            }
            writeCloseTag(os, 0, "achPayments");
            
            // send summary email
            getPaymentFileEmailService().sendAchSummaryEmail(unitCounts, unitTotals, getDateTimeService().getCurrentDate());
        }
        catch (IOException ie) {
            LOG.error("AchBunderlExtract MOD: extractAchFile() Problem reading file:  " + filename, ie);
            throw new IllegalArgumentException("Error writing to output file: " + ie.getMessage());
        }
        finally {
            // Close file
            if (os != null) {
                try {
                    os.close();
                    renameFile(filename, filename + ".NOT_USED");  //  Need to do this at the end to indicate that the file is NOT USED after it is closed.
                }
                catch (IOException ie) {
                    // Not much we can do now
                	LOG.error("IOException encountered in writeExtractBundledAchFile.  Message is: " + ie.getMessage());
                }
            }
        }
    }    
    
}