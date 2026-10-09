package edu.cornell.kfs.cemi.module.purap.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.businessobject.PurApItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderDocumentLite;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderItemLite;
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

    public static List<PurchaseOrderAccount> getOutstandingEncumberedAccountingLines(
            final PurchaseOrderItem purchaseOrderItem) {
        return purchaseOrderItem.getSourceAccountingLines().stream()
                .map(PurchaseOrderAccount.class::cast)
                .filter(CemiPurchaseOrderUtils::accountingLineHasOutstandingEncumbrances)
                .sorted(Comparator.comparing(PurchaseOrderAccount::getAccountIdentifier))
                .collect(Collectors.toUnmodifiableList());
    }

    public static boolean accountingLineHasOutstandingEncumbrances(final PurchaseOrderAccount purchaseOrderAccount) {
        final KualiDecimal amount = purchaseOrderAccount.getItemAccountOutstandingEncumbranceAmount();
        return amount != null && amount.isGreaterThan(KualiDecimal.ZERO);
    }

    // The two methods below copy the PO amount logic from the PurchaseOrderDocument.getTotalDollarAmount() methods.

    public static KualiDecimal getTotalPurchaseOrderDollarAmount(final CemiPurchaseOrderDocumentLite poDocument) {
        // return total without inactive and with below the line
        return getTotalPurchaseOrderDollarAmount(poDocument, false, true);
    }

    public static KualiDecimal getTotalPurchaseOrderDollarAmount(final CemiPurchaseOrderDocumentLite poDocument,
            final boolean includeInactive, final boolean includeBelowTheLine) {
        KualiDecimal total = new KualiDecimal(BigDecimal.ZERO);
        for (final CemiPurchaseOrderItemLite item : poDocument.getItems()) {
            if ((includeBelowTheLine || isLineItem(item)) && (includeInactive || item.isItemActiveIndicator())) {
                final KualiDecimal totalAmount = getTotalItemAmount(poDocument, item);
                final KualiDecimal itemTotal = totalAmount != null ? totalAmount : KualiDecimal.ZERO;
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    @SuppressWarnings("deprecation")
    public static boolean isLineItem(final CemiPurchaseOrderItemLite item) {
        return StringUtils.equalsAny(item.getItemTypeCode(), ItemTypeCodes.ITEM_TYPE_ITEM_CODE,
                ItemTypeCodes.ITEM_TYPE_SERVICE_CODE, ItemTypeCodes.ITEM_TYPE_UNORDERED_ITEM_CODE);
    }

    // The methods below copy the item amount calculation logic from PurApItemBase.

    public static KualiDecimal getTotalItemAmount(
            final CemiPurchaseOrderDocumentLite poDocument, final CemiPurchaseOrderItemLite item) {
        KualiDecimal totalAmount = getItemExtendedPrice(item);
        if (ObjectUtils.isNull(totalAmount)) {
            totalAmount = KualiDecimal.ZERO;
        }

        KualiDecimal taxAmount = getItemTaxAmount(poDocument, item);
        if (ObjectUtils.isNull(taxAmount)) {
            taxAmount = KualiDecimal.ZERO;
        }

        totalAmount = totalAmount.add(taxAmount);

        return totalAmount;
    }

    public static KualiDecimal getItemExtendedPrice(final CemiPurchaseOrderItemLite item) {
        KualiDecimal extendedPrice = KualiDecimal.ZERO;
        final BigDecimal itemUnitPrice = item.getItemUnitPrice();
        if (ObjectUtils.isNotNull(itemUnitPrice)) {
            if (isAmountBasedItem(item)) {
                // SERVICE ITEM: return unit price as extended price
                extendedPrice = new KualiDecimal(itemUnitPrice.toString());
            } else if (ObjectUtils.isNotNull(item.getItemQuantity())) {
                final BigDecimal calcExtendedPrice = itemUnitPrice.multiply(item.getItemQuantity().bigDecimalValue());
                // ITEM TYPE (qty driven): return (unitPrice x qty)
                extendedPrice = new KualiDecimal(calcExtendedPrice.setScale(KualiDecimal.SCALE,
                        KualiDecimal.ROUND_BEHAVIOR));
            }
        }
        return extendedPrice;
    }

    
    public static boolean isAmountBasedItem(final CemiPurchaseOrderItemLite item) {
        return !isQuantityBasedItem(item);
    }

    @SuppressWarnings("deprecation")
    public static boolean isQuantityBasedItem(final CemiPurchaseOrderItemLite item) {
        return StringUtils.equalsAny(item.getItemTypeCode(), ItemTypeCodes.ITEM_TYPE_ITEM_CODE,
                ItemTypeCodes.ITEM_TYPE_UNORDERED_ITEM_CODE);
    }

    public static KualiDecimal getItemTaxAmount(
            final CemiPurchaseOrderDocumentLite poDocument, final CemiPurchaseOrderItemLite item) {
        KualiDecimal taxAmount = KualiDecimal.ZERO;

        if (!poDocument.isUseTaxIndicator()) {
            taxAmount = item.getItemSalesTaxAmount();
        } else {
            // sum use tax item tax amounts
            for (final PurApItemUseTax useTaxItem : item.getUseTaxItems()) {
                taxAmount = taxAmount.add(useTaxItem.getTaxAmount());
            }
        }

        return taxAmount;
    }

}
