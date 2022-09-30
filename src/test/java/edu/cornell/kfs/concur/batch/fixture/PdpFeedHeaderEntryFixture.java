package edu.cornell.kfs.concur.batch.fixture;

import edu.cornell.kfs.concur.ConcurTestConstants.PdpFeedFileConstants;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;

public enum PdpFeedHeaderEntryFixture {
    BASIC_HEADER(PdpFeedFileConstants.CAMPUS, PdpFeedFileConstants.BATCH_DATE, PdpFeedFileConstants.UNIT, PdpFeedFileConstants.SUB_UNIT);

    public final String campus;
    public final String creationDate;
    public final String unit;
    public final String subUnit;

    private PdpFeedHeaderEntryFixture(String chart, String creationDate, String unit, String subUnit) {
        this.campus = chart;
        this.creationDate = creationDate;
        this.unit = unit;
        this.subUnit = subUnit;
    }
    
    public PdpFeedHeaderEntry toPdpFeedHeaderEntry() {
        PdpFeedHeaderEntry header = new PdpFeedHeaderEntry();
        header.setCampus(campus);
        header.setCreationDate(creationDate);
        header.setSubUnit(subUnit);
        header.setUnit(unit);
        return header;
    }
}
