package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAdHocRecipient;

public enum AccountingXmlDocumentAdHocRecipientFixture {
    JDH34_APPROVE("jdh34", "Approve"),
    SE12_FYI("se12", "FYI"),
    CCS1_COMPLETE("ccs1", "Complete"),
    NKK4_ACKNOWLEDGE("nkk4", "Acknowledge");

    public final String netId;
    public final String actionRequested;

    private AccountingXmlDocumentAdHocRecipientFixture(String netId, String actionRequested) {
        this.netId = netId;
        this.actionRequested = actionRequested;
    }

    public AccountingXmlDocumentAdHocRecipient toAdHocRecipientPojo() {
        AccountingXmlDocumentAdHocRecipient adHocRecipient = new AccountingXmlDocumentAdHocRecipient();
        adHocRecipient.setNetId(netId);
        adHocRecipient.setActionRequested(actionRequested);
        return adHocRecipient;
    }

}
