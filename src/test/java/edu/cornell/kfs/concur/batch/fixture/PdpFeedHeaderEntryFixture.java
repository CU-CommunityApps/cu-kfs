package edu.cornell.kfs.concur.batch.fixture;

import edu.cornell.kfs.concur.ConcurTestConstants.PdpFeedFileConstants;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;

public enum PdpFeedHeaderEntryFixture {
    BASIC_HEADER(PdpFeedFileConstants.CHART, PdpFeedFileConstants.BATCH_DATE, PdpFeedFileConstants.UNIT, PdpFeedFileConstants.SUB_UNIT);

    public final String chart;
    public final String creationDate;
    public final String unit;
    public final String subUnit;

    private PdpFeedHeaderEntryFixture(String chart, String creationDate, String unit, String subUnit) {
        this.chart = chart;
        this.creationDate = creationDate;
        this.unit = unit;
        this.subUnit = subUnit;
    }
    
    public PdpFeedHeaderEntry toPdpFeedHeaderEntry() {
        PdpFeedHeaderEntry header = new PdpFeedHeaderEntry();
        header.setChart(chart);
        header.setCreationDate(creationDate);
        header.setSubUnit(subUnit);
        header.setUnit(unit);
        return header;
    }
}
