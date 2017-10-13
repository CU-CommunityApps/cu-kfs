package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAdHocRecipient;

public enum AccountingXmlDocumentAdHocRecipientFixture {
    TEST_RECIPIENT1(null, null);

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
