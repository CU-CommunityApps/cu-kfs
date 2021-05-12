package edu.cornell.kfs.concur.batch.fixture;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;

public enum PdpFeedTrailerEntryFixture {
    BASIC_TRAILER(1, 96.95);
    
    public final int count;
    public final double amount;
    
    private PdpFeedTrailerEntryFixture(int count, double amount) {
        this.count = count;
        this.amount = amount;
    }
    
    public PdpFeedTrailerEntry toPdpFeedTrailerEntry() {
        PdpFeedTrailerEntry trailer = new PdpFeedTrailerEntry();
        trailer.setDetailCount(new Integer(count));
        trailer.setDetailTotAmt(new KualiDecimal(amount));
        return trailer;
    }
}
