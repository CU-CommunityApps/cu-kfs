package edu.cornell.kfs.module.purap.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.rice.kns.util.KualiDecimal;

/**
 * Utility class for calculating IWantDocument item and account
 * amounts and totals via DWR when making client-side updates.
 */
public class IWantAmountUtil {

	private static final String QUANTITY_ONE = "1";
	private static final String AMOUNT_ZERO = "0";

	/**
	 * Calculate only a change in a single item line total, without updating the all-items total.
	 * Useful for updating item newlines.
	 * 
	 * @param itemQuantity The quantity of the given item, as a String.
	 * @param itemUnitPrice The price of the item per unit quantity, as a String.
	 * @return The product of the item quantity and unit price, as a String.
	 */
	public static String calculateSingleItemTotal(String itemQuantity, String itemUnitPrice) {
		KualiDecimal[] itemTotals;
		try {
			itemTotals = findTotalOfOneItemAndAll(new String[] {itemQuantity}, new String[] {itemUnitPrice}, 0);
		} catch (Exception e) {
			itemTotals = new KualiDecimal[] {KualiDecimal.ZERO, KualiDecimal.ZERO};
		}
		return itemTotals[0].toString();
	}

	/**
	 * Calculates the updates to various totals as the result of an item line change. Such a change requires an
	 * update to the item line's total, the all-items total, the all-accounts total, and the difference between
	 * the item and account totals.
	 * 
	 * @param itemQuantities The quantities from all the item lines, as Strings.
	 * @param itemUnitPrices The unit prices from all the item lines, as Strings.
	 * @param itemIndex The index of the updated item line.
	 * @param useAmountsOrPercents The amount-or-percent indicators from all the account lines, as Strings.
	 * @param accountAmountsOrPercents The amount-or-percent values from all the account lines, as Strings.
	 * @return A String array with the updated line's total at index 0, the all-items total at 1, the all-accounts total at 2, and the totals difference at 3.
	 */
	public static String[] calculateTotalsForItemChange(String[] itemQuantities, String[] itemUnitPrices, int itemIndex,
			String[] useAmountsOrPercents, String[] accountAmountsOrPercents) {
		KualiDecimal[] itemTotals;
		KualiDecimal[] accountAndDifferenceTotals;
		
		// Calculate items totals, and find the new total of the changed item line.
		try {
			itemTotals = findTotalOfOneItemAndAll(itemQuantities, itemUnitPrices, itemIndex);
		} catch (Exception e) {
			itemTotals = new KualiDecimal[] {KualiDecimal.ZERO, KualiDecimal.ZERO};
		}
		
		// Calculate accounts totals and the difference between the item and account totals.
		try {
			accountAndDifferenceTotals = findAccountsTotalAndDifference(useAmountsOrPercents, accountAmountsOrPercents, itemTotals[1]);
		} catch (Exception e) {
			accountAndDifferenceTotals = new KualiDecimal[] {KualiDecimal.ZERO, KualiDecimal.ZERO};
		}
		
		// Convert the results into a String array.
		return new String[] {itemTotals[0].toString(), itemTotals[1].toString(), accountAndDifferenceTotals[0].toString(), accountAndDifferenceTotals[1].toString()};
	}

	/**
	 * Calculates the updates to various totals as the result of an account line change. Such a change requires
	 * an update to the all-accounts total and the difference between the item and account totals.
	 * 
	 * @param useAmountsOrPercents The amount-or-percent indicators from all the account lines, as Strings.
	 * @param accountAmountsOrPercents The amount-or-percent values from all the account lines, as Strings.
	 * @param itemsTotalString The all-items total, as a String.
	 * @return A String array with the all-accounts total at index 0 and the totals difference at index 1.
	 */
	public static String[] calculateTotalsForAccountChange(String[] useAmountsOrPercents, String[] accountAmountsOrPercents, String itemsTotalString) {
		KualiDecimal itemsTotal;
		KualiDecimal[] accountAndDifferenceTotals;
		DecimalFormat numFormatter = new DecimalFormat();
		numFormatter.setParseBigDecimal(true);
		
		// Convert the all-items total to a number.
		try {
			itemsTotal = new KualiDecimal(numFormatter.parse(itemsTotalString).toString());
		} catch (Exception e) {
			itemsTotal = KualiDecimal.ZERO;
		}
		
		// Calculate accounts totals and the difference between the item and account totals.
		try {
			accountAndDifferenceTotals = findAccountsTotalAndDifference(useAmountsOrPercents, accountAmountsOrPercents, itemsTotal);
		} catch (Exception e) {
			accountAndDifferenceTotals = new KualiDecimal[] {KualiDecimal.ZERO, KualiDecimal.ZERO};
		}
		
		// Convert the results into a String array.
		return new String[] {accountAndDifferenceTotals[0].toString(), accountAndDifferenceTotals[1].toString()};
	}

	/**
	 * Internal helper method for calculating all-items total and retrieving the new total of the updated item line.
	 * 
	 * @param itemQuantities The quantities from all the item lines, as Strings.
	 * @param itemUnitPrices The unit prices from all the item lines, as Strings.
	 * @param itemIndex The index of the updated item line.
	 * @return A KualiDecimal array with the updated item line's total at index 0 and the all-items total at index 1.
	 * @throws Exception
	 */
	private static final KualiDecimal[] findTotalOfOneItemAndAll(String[] itemQuantities, String[] itemUnitPrices, int itemIndex) throws Exception {
		KualiDecimal[] totals = new KualiDecimal[2];
		KualiDecimal itemsTotal = KualiDecimal.ZERO;
		KualiDecimal itemTotal = KualiDecimal.ZERO;
		DecimalFormat numFormatter = new DecimalFormat();
		numFormatter.setParseBigDecimal(true);
		
		// Add up the quantity*unitPrice product for each item line, and record the updated item line's total if needed.
		for (int i = 0; i < itemQuantities.length; i++) {
			String itemQuantity = itemQuantities[i];
			String itemUnitPrice = itemUnitPrices[i];
			if (StringUtils.isBlank(itemQuantity)) {
				itemQuantity = QUANTITY_ONE;
			}
			if (StringUtils.isBlank(itemUnitPrice)) {
				itemQuantity = AMOUNT_ZERO;
			}
			KualiDecimal newQuantity = new KualiDecimal(numFormatter.parse(itemQuantity).toString());
			BigDecimal newUnitPrice = new BigDecimal(numFormatter.parse(itemUnitPrice).toString());
			itemTotal = new KualiDecimal(newQuantity.bigDecimalValue().multiply(newUnitPrice));
			if (i == itemIndex) {
				totals[0] = itemTotal;
			}
			itemsTotal = itemsTotal.add(itemTotal);
		}

		totals[1] = itemsTotal;
		return totals;
	}

	/**
	 * Internal helper method for calculating the all-accounts total and calculating the difference between the item and account totals.
	 * 
	 * @param useAmountsOrPercents The amount-or-percent indicators from all the account lines, as Strings.
	 * @param accountAmountsOrPercents The amount-or-percent values from all the account lines, as Strings.
	 * @param itemsTotal The total of all the item line amounts.
	 * @return A KualiDecimal array with the all-accounts total at index 0 and the difference between the item and account totals at index 1.
	 * @throws Exception
	 */
	private static final KualiDecimal[] findAccountsTotalAndDifference(String[] useAmountsOrPercents, String[] accountAmountsOrPercents,
			KualiDecimal itemsTotal) throws Exception {
		KualiDecimal[] totals = new KualiDecimal[2];
		KualiDecimal accountsTotal = KualiDecimal.ZERO;
		KualiDecimal accountLineTotal = KualiDecimal.ZERO;
		DecimalFormat numFormatter = new DecimalFormat();
		numFormatter.setParseBigDecimal(true);
		
		// Add up the dollar amounts for each account line.
		for (int i = 0; i < useAmountsOrPercents.length; i++) {
			String useAmountOrPercent = useAmountsOrPercents[i];
			String accountAmountOrPercent = accountAmountsOrPercents[i];
			
			if (StringUtils.isBlank(accountAmountOrPercent)) {
				accountAmountOrPercent = AMOUNT_ZERO;
			}
			
			if (CUPurapConstants.PERCENT.equalsIgnoreCase(useAmountOrPercent)) {
				accountLineTotal = new KualiDecimal(numFormatter.parse(accountAmountOrPercent).toString());
				accountLineTotal = accountLineTotal.multiply(itemsTotal).divide(new KualiDecimal(100));
			} else if (CUPurapConstants.AMOUNT.equalsIgnoreCase(useAmountOrPercent)) {
				accountLineTotal = new KualiDecimal(numFormatter.parse(accountAmountOrPercent).toString());
			} else {
				accountLineTotal = KualiDecimal.ZERO;
			}
			
			accountsTotal = accountsTotal.add(accountLineTotal);
		}
		
		// Record the accounts total and compute the difference between the item and account totals.
		totals[0] = accountsTotal;
		totals[1] = itemsTotal.subtract(accountsTotal);
		
		return totals;
	}
}
