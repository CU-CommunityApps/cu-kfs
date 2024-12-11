package edu.cornell.kfs.tax.batch;

import java.util.EnumMap;

import org.apache.commons.lang3.mutable.MutableInt;

import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public class TaxStatistics implements TaxStatisticsHandler {

    private final EnumMap<TaxStatType, MutableInt> statistics;

    public TaxStatistics() {
        this.statistics = new EnumMap<>(TaxStatType.class);
    }

    public TaxStatistics(final TaxStatType... initialZeroValueEntries) {
        this();
        for (final TaxStatType entryType : initialZeroValueEntries) {
            statistics.put(entryType, new MutableInt(0));
        }
    }

    @Override
    public void increment(final TaxStatType entryType) {
        statistics.computeIfAbsent(entryType, key -> new MutableInt(0))
                .increment();
    }

}
