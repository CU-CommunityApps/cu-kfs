package edu.cornell.kfs.pdp.dataaccess;

import java.util.HashSet;
import java.util.List;

import org.kuali.kfs.pdp.businessobject.PaymentDetail;

public interface AchBundlerAdviceDao {

    //KFSPTS-1460 -- Added to code received
    /**
     * Returns distinct disbursement numbers for ACH payments needing advice email notifications.
     *
     */
    public abstract HashSet<Integer> getDistinctDisbursementNumbersForAchPaymentsNeedingAdviceNotification();
    
  //KFSPTS-1460 -- Added to code received
    public List<PaymentDetail> getAchPaymentDetailsNeedingAdviceNotificationByDisbursementNumber(Integer disbursementNumber);
}
