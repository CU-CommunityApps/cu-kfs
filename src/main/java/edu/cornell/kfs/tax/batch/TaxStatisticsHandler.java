package edu.cornell.kfs.tax.batch;

import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public interface TaxStatisticsHandler {

    void increment(final TaxStatType entryType);

}
