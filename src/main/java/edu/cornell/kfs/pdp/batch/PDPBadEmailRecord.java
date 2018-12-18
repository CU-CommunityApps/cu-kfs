package edu.cornell.kfs.pdp.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.rice.core.api.util.type.KualiInteger;

public class PDPBadEmailRecord {
    private static final Logger LOG = LogManager.getLogger(PDPBadEmailRecord.class);
    
    private String payeeId;
    private KualiInteger paymentGroupId;
    private String emailAddress;
    
    public PDPBadEmailRecord(String payeeId, KualiInteger paymentGroupId, String emailAddress) {
        this.payeeId = payeeId;
        this.paymentGroupId = paymentGroupId;
        this.emailAddress = emailAddress;
    }
    
    public String getPayeeId() {
        return payeeId;
    }

    public KualiInteger getPaymentGroupId() {
        return paymentGroupId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void logBadEmailRecord() {
        LOG.error("logBadEmailRecord, payeeIdL '" + payeeId + "' payment group ID: '" + paymentGroupId + "' email address: '" + emailAddress + "'");
    }
}
