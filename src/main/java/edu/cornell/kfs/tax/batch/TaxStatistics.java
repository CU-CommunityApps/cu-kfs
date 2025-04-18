package edu.cornell.kfs.tax.batch;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
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

    @Override
    public void increment(final TaxStatType baseEntryType, final String documentType) {
        increment(baseEntryType);
        if (!baseEntryType.hasTaxSourceSpecificSubStat) {
            return;
        }
        final String taxSourceSuffix;
        if (StringUtils.equals(documentType, DisbursementVoucherConstants.DOCUMENT_TYPE_CODE)) {
            taxSourceSuffix = CUKFSConstants.UNDERSCORE + CUTaxConstants.TAX_SOURCE_DV;
        } else {
            taxSourceSuffix = CUKFSConstants.UNDERSCORE + CUTaxConstants.TAX_SOURCE_PDP;
        }
        final TaxStatType taxSourceSpecificEntryType = TaxStatType.valueOf(baseEntryType.name() + taxSourceSuffix);
        increment(taxSourceSpecificEntryType);
    }

    public int getTransactionRowCountStatistic() {
        final MutableInt value = statistics.get(TaxStatType.NUM_TRANSACTION_ROWS);
        return (value != null) ? value.getValue() : 0; 
    }

    public Map<TaxStatType, Integer> getOrderedResults() {
        return getResultsAsUnmodifiableMapWithNaturalEnumOrdering();
    }

    private Map<TaxStatType, Integer> getResultsAsUnmodifiableMapWithNaturalEnumOrdering() {
        final EnumMap<TaxStatType, Integer> results = statistics.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue().toInteger(),
                        (value1, value2) -> value1,
                        () -> new EnumMap<>(TaxStatType.class)
                ));

        return Collections.unmodifiableMap(results);
    }

}
