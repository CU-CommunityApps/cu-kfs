package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.AddressException;

import org.kuali.kfs.pdp.batch.service.AchAdviceNotificationService;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import edu.cornell.kfs.pdp.batch.PDPBadEmailRecord;
import edu.cornell.kfs.pdp.batch.service.CuAchAdviceNotificationErrorReportService;
import edu.cornell.kfs.pdp.dataaccess.AchBundlerAdviceDao;
import edu.cornell.kfs.pdp.service.CuPdpEmailService;

/**
 * @see org.kuali.kfs.pdp.batch.service.AchAdviceNotificationService
 */
public class CuAchAdviceNotificationServiceImpl implements AchAdviceNotificationService {
    private static final Logger LOG = LogManager.getLogger(CuAchAdviceNotificationServiceImpl.class);
    
    private CuPdpEmailService pdpEmailService;
    private PaymentGroupService paymentGroupService;

    private DateTimeService dateTimeService;
    private BusinessObjectService businessObjectService;
    private AchBundlerHelperService achBundlerHelperService;
    private AchBundlerAdviceDao achBundlerAdviceDao;                 //KFSPTS-1460 - added
    protected CuAchAdviceNotificationErrorReportService cuAchAdviceNotificationErrorReportService;

    /**
     * @see org.kuali.kfs.pdp.batch.service.AchAdviceNotificationService#sendAdviceNotifications()
     */
    @Override
    public void sendAdviceNotifications() {
    	
    	//KFSPTS-1460 - Changes
    	// Added achBundlerHelperService to class.
    	// Added "if-then-clause" to this method to determine how the ACH payments were formatted
    	// so corresponding advice email notifications are also sent to the vendors in the same manner.
    	//
    	// Method pdpEmailService.sendAchAdviceEmail with the new method signature will be called by
    	// both data controlling loops resulting in the same advice email appearance regardless of 
    	// the data being sent to it.
    	//
    	// The "if-then-clause" is needed because the data retrieval for bundled vs unbundled is 
    	// different which requires different looping.
    	//
    	// The original code obtains payment groups and loops over the payment details associated with each 
    	// payment group to send the advices. The looping over payment details will no longer be perform here
    	// and instead that list of payment details will be passed to pdpEmailService.sendAchAdviceEmail for processing.
    	//
    	// The ACH bundled code needs to retrieve disbursement numbers and then get the payment details associated with each 
    	// unique disbursement number because that mod groups ACH payments based upon all payment groups associated to a single
    	// disbursement number; thus requiring two nested data loops to send the advices.    
    	
        List<PDPBadEmailRecord> badEmailRecords = new ArrayList<PDPBadEmailRecord>();
        
    	if (achBundlerHelperService.shouldBundleAchPayments()) {
    		//ACH payments were bundled so the corresponding advice email notifications should also be bundled
    		HashSet<Integer> disbNbrs = achBundlerAdviceDao.getDistinctDisbursementNumbersForAchPaymentsNeedingAdviceNotification();
    		
    		for (Iterator<Integer> disbIter = disbNbrs.iterator(); disbIter.hasNext();) {
    			Integer disbursementNbr = disbIter.next();
    			
    			//get all payment details to include in the advice based on disbursement number which is associated to a single vendor
                List<PaymentDetail> paymentDetails = achBundlerAdviceDao.getAchPaymentDetailsNeedingAdviceNotificationByDisbursementNumber(disbursementNbr);
                
                //get one payment detail record so that we can get the needed payment group record, since all payment details are for the same vendor, all payment group records should match
                Iterator<PaymentDetail> paymentDetailsIter = paymentDetails.iterator();
                PaymentDetail payDetail = paymentDetailsIter.next();
                PaymentGroup payGroup = payDetail.getPaymentGroup();
                CustomerProfile customer = payGroup.getBatch().getCustomerProfile();
                try {
                    // verify the customer profile is setup to create advices
    	            if (customer.getAdviceCreate()) {
    	                cuAchAdviceNotificationErrorReportService.validateEmailAddress(payGroup.getAdviceEmailAddress());
    	            	pdpEmailService.sendAchAdviceEmail(payGroup, paymentDetails, customer);                    
                    }
    	            
    	            //update sent date on the payment, must loop through all payment details because payment groups could be unique.
    	            for (Iterator<PaymentDetail> paymentDetailsIter2 = paymentDetails.iterator(); paymentDetailsIter2.hasNext();) {
    	            	PaymentDetail pd = paymentDetailsIter2.next();
    	                PaymentGroup pg = pd.getPaymentGroup();
    	                pg.setAdviceEmailSentDate(dateTimeService.getCurrentTimestamp());
    		            businessObjectService.save(pg);
    	            }
                } catch (AddressException ae) {
                    addBadEmailRecord(badEmailRecords, payGroup);
                }
	            	            
    		} //for each disb number    		
    		
    	}
    	else {
    		//Execute the original KFS code to send unbundled ACH advice email notifications with a
    		//change to the looping on the payment detail records as noted below with notation KFSPTS-1460

	        // get list of payments to send notification for
	        List<PaymentGroup> paymentGroups = paymentGroupService.getAchPaymentsNeedingAdviceNotification();
	        for (PaymentGroup paymentGroup : paymentGroups) {
	            CustomerProfile customer = paymentGroup.getBatch().getCustomerProfile();
	            
	            try {
    	            cuAchAdviceNotificationErrorReportService.validateEmailAddress(paymentGroup.getAdviceEmailAddress());
    	            // verify the customer profile is setup to create advices
    	            if (customer.getAdviceCreate()) {
    	            	//KFSPTS-1460 - for loop removed
    	                //for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
    	                //    pdpEmailService.sendAchAdviceEmail(paymentGroup, paymentDetail, customer);
    	                //}
    	            	List<PaymentDetail> paymentDetails = paymentGroup.getPaymentDetails();
    	            	pdpEmailService.sendAchAdviceEmail(paymentGroup, paymentDetails, customer);
    	            }
    	
    	            // update advice sent date on payment
    	            paymentGroup.setAdviceEmailSentDate(dateTimeService.getCurrentTimestamp());
    	            businessObjectService.save(paymentGroup);
	            } catch (AddressException ae) {
	                addBadEmailRecord(badEmailRecords, paymentGroup);
                }
	            
	        }
    	}
    	createAndEmailErrorReport(badEmailRecords);
    }
    
    private void addBadEmailRecord(List<PDPBadEmailRecord> badEmailRecords, PaymentGroup paymentGroup) {
        PDPBadEmailRecord badEmailRecord = new PDPBadEmailRecord(paymentGroup.getPayeeId(), paymentGroup.getId(), paymentGroup.getAdviceEmailAddress(), 
                paymentGroup.getDisbursementNbr());
        badEmailRecord.logBadEmailRecord();
        badEmailRecords.add(badEmailRecord);
    }
    
    private void createAndEmailErrorReport(List<PDPBadEmailRecord> badEmailRecords) {
        if (CollectionUtils.isNotEmpty(badEmailRecords)) {
            LOG.info("createBadEmailReport, there are " + badEmailRecords.size() + " bad email addresses to report");
            File reportFile = cuAchAdviceNotificationErrorReportService.createBadEmailReport(badEmailRecords);
            cuAchAdviceNotificationErrorReportService.emailBadEmailReport(reportFile);

        } else {
            LOG.info("createBadEmailReport, there were no bad email addresses to report.");
        }
    }

    /**
     * Sets the pdpEmailService attribute value.
     * 
     * @param pdpEmailService The pdpEmailService to set.
     */
    public void setPdpEmailService(CuPdpEmailService pdpEmailService) {
        this.pdpEmailService = pdpEmailService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Sets the businessObjectService attribute value.
     * 
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Sets the paymentGroupService attribute value.
     * 
     * @param paymentGroupService The paymentGroupService to set.
     */
    public void setPaymentGroupService(PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }
    
    //KFSPTS-1460 -- Added
    public void setAchBundlerHelperService(AchBundlerHelperService achBundlerHelperService) {
        this.achBundlerHelperService = achBundlerHelperService;
    }
    
    //KFSPTS-1460 -- Added
    public void setAchBundlerAdviceDao(AchBundlerAdviceDao achBundlerAdviceDao) {
        this.achBundlerAdviceDao = achBundlerAdviceDao;
    }
    
    public void setCuAchAdviceNotificationErrorReportService(
            CuAchAdviceNotificationErrorReportService cuAchAdviceNotificationErrorReportService) {
        this.cuAchAdviceNotificationErrorReportService = cuAchAdviceNotificationErrorReportService;
    }
}
