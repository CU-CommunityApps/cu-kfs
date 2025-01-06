package edu.cornell.kfs.tax.batch.dto;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public abstract class TaxFileRowBase implements TaxFileRow {

    protected final DecimalFormat amountFormat;
    protected final DecimalFormat percentFormat;
    protected final SimpleDateFormat dateFormat;

    protected TaxFileRowBase(final DecimalFormat amountFormat, final DecimalFormat percentFormat,
            final SimpleDateFormat dateFormat) {
        this.amountFormat = amountFormat;
        this.percentFormat = percentFormat;
        this.dateFormat = dateFormat;
    }

    protected Map.Entry<String, String> buildEntry(final String key, final String value) {
        return Map.entry(key, StringUtils.defaultString(value));
    }

    protected Map.Entry<String, String> buildEntry(final String key, final Integer value) {
        return Map.entry(key, value != null ? value.toString() : KFSConstants.EMPTY_STRING);
    }

    protected Map.Entry<String, String> buildNegatedEntry(final String key, final Integer value) {
        return Map.entry(key, value != null ? String.valueOf(0 - value.intValue()) : KFSConstants.EMPTY_STRING);
    }

    protected Map.Entry<String, String> buildEntryForAmount(final String key, final BigDecimal value) {
        return Map.entry(key, value != null ? amountFormat.format(value) : KFSConstants.EMPTY_STRING);
    }

    protected Map.Entry<String, String> buildNegatedEntryForAmount(final String key, final BigDecimal value) {
        return Map.entry(key, value != null ? amountFormat.format(value.negate()) : KFSConstants.EMPTY_STRING);
    }

    protected Map.Entry<String, String> buildEntryForPercent(final String key, final BigDecimal value) {
        return Map.entry(key, value != null ? percentFormat.format(value) : KFSConstants.EMPTY_STRING);
    }

    protected Map.Entry<String, String> buildNegatedEntryForPercent(final String key, final BigDecimal value) {
        return Map.entry(key, value != null ? percentFormat.format(value.negate()) : KFSConstants.EMPTY_STRING);
    }

    protected Map.Entry<String, String> buildEntry(final String key, final java.sql.Date value) {
        return Map.entry(key, value != null ? dateFormat.format(value) : KFSConstants.EMPTY_STRING);
    }

}
