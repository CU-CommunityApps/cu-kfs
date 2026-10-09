package edu.cornell.kfs.cemi.module.purap.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public final class CemiPurchaseOrderUtils {

    private static final DateTimeFormatter DATETIME_FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern(
            CUKFSConstants.DATE_FORMAT_yyyy_MM_dd, Locale.US);

    private static final ThreadLocal<DecimalFormat> AMOUNT_FORMATTERS = ThreadLocal.withInitial(
            CemiPurchaseOrderUtils::createAmountFormatter);

    private static final ThreadLocal<DecimalFormat> SPLIT_AMOUNT_FORMATTERS = ThreadLocal.withInitial(
            CemiPurchaseOrderUtils::createSplitAmountFormatter);

    private static final ThreadLocal<DecimalFormat> QUANTITY_FORMATTERS = ThreadLocal.withInitial(
            CemiPurchaseOrderUtils::createQuantityFormatter);

    private static final DecimalFormat createAmountFormatter() {
        return new DecimalFormat(CemiPurchaseOrderConstants.PURCHASE_ORDER_AMOUNT_FORMAT);
    }

    private static final DecimalFormat createSplitAmountFormatter() {
        return new DecimalFormat(CemiPurchaseOrderConstants.PURCHASE_ORDER_SPLIT_AMOUNT_FORMAT);
    }

    private static final DecimalFormat createQuantityFormatter() {
        return new DecimalFormat(CemiPurchaseOrderConstants.PURCHASE_ORDER_QUANTITY_FORMAT);
    }

    public static String formatAsDate(final LocalDateTime value) {
        return (value != null) ? DATETIME_FORMATTER_YYYY_MM_DD.format(value) : CemiBaseConstants.EMPTY_STRING;
    }

    public static String formatAmount(final KualiDecimal value) {
        return (value != null)
                ? AMOUNT_FORMATTERS.get().format(value.bigDecimalValue()) : CemiBaseConstants.EMPTY_STRING;
    }

    public static String formatAmount(final BigDecimal value) {
        return (value != null) ? AMOUNT_FORMATTERS.get().format(value) : CemiBaseConstants.EMPTY_STRING;
    }

    public static String formatSplitAmount(final KualiDecimal value) {
        return (value != null)
                ? SPLIT_AMOUNT_FORMATTERS.get().format(value.bigDecimalValue()) : CemiBaseConstants.EMPTY_STRING;
    }

    public static String formatQuantity(final KualiDecimal value) {
        return (value != null)
                ? QUANTITY_FORMATTERS.get().format(value.bigDecimalValue()) : CemiBaseConstants.EMPTY_STRING;
    }

}
