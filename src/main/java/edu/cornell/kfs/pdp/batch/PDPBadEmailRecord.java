package edu.cornell.kfs.pdp.batch;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.rice.core.api.util.type.KualiInteger;

public class PDPBadEmailRecord {
    private static final Logger LOG = LogManager.getLogger(PDPBadEmailRecord.class);
    
    private String payeeId;
    private String paymentGroupId;
    private String emailAddress;
    private String disbursementNumber;
    
    public PDPBadEmailRecord(String payeeId, KualiInteger paymentGroupId, String emailAddress, KualiInteger disbursementNumber) {
        this.payeeId = payeeId != null ? payeeId : StringUtils.EMPTY;
        this.paymentGroupId = paymentGroupId != null ? paymentGroupId.toString() : StringUtils.EMPTY;
        this.emailAddress = emailAddress != null ? emailAddress : StringUtils.EMPTY;
        this.disbursementNumber = disbursementNumber != null ? disbursementNumber.toString() : StringUtils.EMPTY;
    }
    
    public String getPayeeId() {
        return payeeId;
    }

    public String getPaymentGroupId() {
        return paymentGroupId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getDisbursementNumber() {
        return disbursementNumber;
    }

    public void logBadEmailRecord() {
        LOG.error("logBadEmailRecord, payeeIdL '" + payeeId + "' payment group ID: '" + paymentGroupId + "' email address: '" + 
                emailAddress + "' disbursement number: + '" + disbursementNumber + "'");
    }
}
