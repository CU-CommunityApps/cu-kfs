package edu.cornell.kfs.concur.batch.fixture;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;

public enum PdpFeedFileBaseEntryFixture {

    MARSHAL_TEST(EnumSet.of(PdpFeedGroupEntryFixture.MARSHAL_TEST)),
    FEED_ONE_TRANS_ZERO(EnumSet.of(PdpFeedGroupEntryFixture.GROUP_ONE_TRANS_ZERO)),
    FEED_ONE_TRANS_ZER_ONE_TRANS_POSITIVE(EnumSet.of(PdpFeedGroupEntryFixture.GROUP_ONE_TRANS_ZERO_ONE_TRANS_POSITIVE)),
    FEED_TWO_TRANS_SUM_TO_POSITIVE(EnumSet.of(PdpFeedGroupEntryFixture.GROUP_TWO_TRANS_SUM_TO_POSITIVE)),
    FEED_TWO_TRANS_SUM_TO_ZERO(EnumSet.of(PdpFeedGroupEntryFixture.GROUP_TWO_TRANS_SUM_TO_ZERO)),
    FEED_TWO_TRANS_SUM_TO_NEGATIVE(EnumSet.of(PdpFeedGroupEntryFixture.GROUP_TWO_TRANS_SUM_TO_NEGATIVE)),
    FEED_TWO_DETAILS_ONE_POSITIVE_ONE_ZERO(EnumSet.of(PdpFeedGroupEntryFixture.GROUP_TWO_DETAILS_ONE_POSITIVE_ONE_ZERO)),
    FEED_TWO_GROUPS_ONE_ZERO(EnumSet.of(PdpFeedGroupEntryFixture.GROUP_TWO_TRANS_SUM_TO_ZERO, PdpFeedGroupEntryFixture.GROUP_TWO_TRANS_SUM_TO_POSITIVE));
    
    public final PdpFeedHeaderEntryFixture headerEntry;
    public final Collection<PdpFeedGroupEntryFixture> groupEntries;
    public final PdpFeedTrailerEntryFixture trailerEntry;
    
    private PdpFeedFileBaseEntryFixture(PdpFeedHeaderEntryFixture headerEntry, PdpFeedTrailerEntryFixture trailerEntry, EnumSet<PdpFeedGroupEntryFixture> groupEntries) {
        this.headerEntry = headerEntry;
        this.trailerEntry = trailerEntry;
        this.groupEntries = Collections.unmodifiableSet(groupEntries);
    }
    
    private PdpFeedFileBaseEntryFixture(EnumSet<PdpFeedGroupEntryFixture> groupEntries) {
        this(PdpFeedHeaderEntryFixture.BASIC_HEADER, PdpFeedTrailerEntryFixture.BASIC_TRAILER, groupEntries);
    }
    
    public PdpFeedFileBaseEntry toPdpFeedFileBaseEntry() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = new PdpFeedFileBaseEntry();
        pdpFeedFileBaseEntry.setVersion("1.0");
        pdpFeedFileBaseEntry.setHeader(headerEntry.toPdpFeedHeaderEntry());
        pdpFeedFileBaseEntry.setTrailer(trailerEntry.toPdpFeedTrailerEntry());
        for (PdpFeedGroupEntryFixture group : groupEntries) {
            pdpFeedFileBaseEntry.getGroup().add(group.toPdpFeedGroupEntry());
        }
        return pdpFeedFileBaseEntry;
    }

}
