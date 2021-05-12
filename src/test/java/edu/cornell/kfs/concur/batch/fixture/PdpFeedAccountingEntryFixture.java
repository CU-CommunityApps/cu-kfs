package edu.cornell.kfs.concur.batch.fixture;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurTestConstants.PdpFeedFileConstants;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;

public enum PdpFeedAccountingEntryFixture {
    TRANSACTION_AMOUNT_25_50_WITH_SUBOBJECT_350(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, "365", null, null, 25.50),
    TRANSACTION_AMOUNT_71_45_WITH_SUBOBJECT_350(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, "350", null, null, 71.45),
    TRANSACTION_AMOUNT_10_WITH_SUBOBJECT_350(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, "350", null, null, 10),
    TRANSACTION_AMOUNT_5_WITH_SUBOBJECT_350(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, null, PdpFeedFileConstants.OBJECT_CODE, "350", null, null, 5),
    TRANSACTION_AMOUNT_10(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, PdpFeedFileConstants.OBJECT_CODE, 10),
    TRANSACTION_AMOUNT_5(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, PdpFeedFileConstants.OBJECT_CODE, 5),
    TRANSACTION_AMOUNT_0(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, PdpFeedFileConstants.OBJECT_CODE, 0),
    TRANSACTION_AMOUNT_NEGATIVE_1(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, PdpFeedFileConstants.OBJECT_CODE, -1),
    TRANSACTION_AMOUNT_NEGATIVE_10(PdpFeedFileConstants.CHART, PdpFeedFileConstants.ACCOUNT_NUMBER, PdpFeedFileConstants.OBJECT_CODE, -10);
    
    public final String coaCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final String objectCode;
    public final String subObjectCode;
    public final String projectCode;
    public final String orgRefId;
    public final double amount;
    
    private PdpFeedAccountingEntryFixture(String coaCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode, String projectCode, 
            String orgRefId, double amount) {
        this.coaCode = coaCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.objectCode = objectCode;
        this.subObjectCode = subObjectCode;
        this.projectCode = projectCode;
        this.orgRefId = orgRefId;
        this.amount = amount;
    }
    
    private PdpFeedAccountingEntryFixture(String coaCode, String accountNumber, String objectCode, double amount) {
        this(coaCode, accountNumber, null, objectCode, null, null, null, amount);
    }
    
    public PdpFeedAccountingEntry toPdpFeedAccountingEntry() {
        PdpFeedAccountingEntry accounting = new PdpFeedAccountingEntry();
        accounting.setCoaCd(coaCode);
        accounting.setAccountNbr(accountNumber);
        accounting.setSubAccountNbr(subAccountNumber);
        accounting.setObjectCd(objectCode);
        accounting.setSubObjectCd(subObjectCode);
        accounting.setProjectCd(projectCode);
        accounting.setOrgRefId(orgRefId);
        accounting.setAmount(new KualiDecimal(amount));
        return accounting;
    }
}
