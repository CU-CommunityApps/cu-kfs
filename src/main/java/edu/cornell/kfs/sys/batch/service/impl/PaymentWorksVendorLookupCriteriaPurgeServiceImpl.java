package edu.cornell.kfs.sys.batch.service.impl;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.query.Criteria;

public class PaymentWorksVendorLookupCriteriaPurgeServiceImpl extends TableLookupCriteriaPurgeServiceImpl {

    @Override
    public Criteria buildLookupCriteria(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        /*
        lookupCriteria.addOrCriteria(deleteOldFormNullRequestVendorApprovedAndUploaded(dateForPurge));
        lookupCriteria.addOrCriteria(deleteOldFormNullRequestVendorApprovedAndNotUploaded(dateForPurge));
        lookupCriteria.addOrCriteria(deleteNewFormProcessedRequestVendorApprovedAndUploaded(dateForPurge));
        lookupCriteria.addOrCriteria(deleteNewFormProcessedRequestVendorApprovedAndNotUploaded(dateForPurge));
        lookupCriteria.addOrCriteria(deleteNewFormProcessedRequestVendorRejected(dateForPurge));
        lookupCriteria.addOrCriteria(deleteNewFormProcessedRequestVendorDisapproved(dateForPurge));
        lookupCriteria.addOrCriteria(deleteRecordsOlderThanForceDate());
        */
        lookupCriteria.addLessOrEqualThan("PROC_TS", dateForPurge);
        return lookupCriteria;
    }
    
//PWW_TRANS_CD = KV, PMW_REQ_STAT is null, KFS_VND_PROC_STAT = Vendor Approved and SUPP_UPLD_STAT = Vendor Uploaded or
    protected Criteria deleteOldFormNullRequestVendorApprovedAndUploaded(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        addTransactionTypeToCriteria(lookupCriteria, "KV");
        addPaymentWorksRequestStatusToCriteria(lookupCriteria, null);
        return lookupCriteria;
    }
    
    //PWW_TRANS_CD = KV, PMW_REQ_STAT is null, KFS_VND_PROC_STAT = Vendor Approved and SUPP_UPLD_STAT = Upload Failed or
    protected Criteria deleteOldFormNullRequestVendorApprovedAndNotUploaded(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        addTransactionTypeToCriteria(lookupCriteria, "KV");
        addPaymentWorksRequestStatusToCriteria(lookupCriteria, null);
        return lookupCriteria;
    }
    
    //PMW_TRANS_CD = NV, PMW_REQ_STAT = Processed, KFS_VND_PROC_STAT = Vendor Approved and SUPP_UPLD_STAT = Vendor Uploaded
    protected Criteria deleteNewFormProcessedRequestVendorApprovedAndUploaded(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        addTransactionTypeToCriteria(lookupCriteria, "NV");
        return lookupCriteria;
    }
    
    //PMW_TRANS_CD = NV, PMW_REQ_STAT = Processed, KFS_VND_PROC_STAT = Vendor Approved and SUPP_UPLD_STAT = Upload Failed
    protected Criteria deleteNewFormProcessedRequestVendorApprovedAndNotUploaded(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        addTransactionTypeToCriteria(lookupCriteria, "NV");
        return lookupCriteria;
    }
    
    //PMW_TRANS_CD = 'NV' and PMW_REQ_STAT = 'Processed' and KFS_VND_PROC_STAT = 'Vendor Rejected' and SUPP_UPLD_STAT is null
    protected Criteria deleteNewFormProcessedRequestVendorRejected(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        addTransactionTypeToCriteria(lookupCriteria, "NV");
        return lookupCriteria;
    }
    
    //PMW_TRANS_CD = 'NV' and PMW_REQ_STAT = 'Processed' and KFS_VND_PROC_STAT = 'Vendor Disapproved' and SUPP_UPLD_STAT = 'Ineligible for Upload'
    protected Criteria deleteNewFormProcessedRequestVendorDisapproved(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        addTransactionTypeToCriteria(lookupCriteria, "NV");
        return lookupCriteria;
    }
    
    //force delete time
    protected Criteria deleteRecordsOlderThanForceDate() {
        Criteria lookupCriteria = new Criteria();
        return lookupCriteria;
    }
    
    private void addTransactionTypeToCriteria( Criteria lookupCriteria, String transactionType) {
        lookupCriteria.addEqualTo("PMW_TRANS_CD", transactionType);
    }
    
    private void addPaymentWorksRequestStatusToCriteria( Criteria lookupCriteria, String requestStatus) {
        if (StringUtils.isBlank(requestStatus)) {
            lookupCriteria.addIsNull("PMW_REQ_STAT");
        } else {
            lookupCriteria.addEqualTo("PMW_REQ_STAT", requestStatus);
        }
    }

}
