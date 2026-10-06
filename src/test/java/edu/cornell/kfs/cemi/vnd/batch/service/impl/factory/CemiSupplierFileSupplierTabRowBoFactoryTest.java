package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * KFSPTS-38411: Verifies the Default/Accepted Payment Type derivation rules.
 *
 * Rules (from the parent ticket):
 *   P + active ACH account  -> Default ACH_Manual
 *   P + no ACH account      -> Default Outsourced_Check
 *   F (Foreign Draft)       -> Default FX_Payments
 *   W (Wire Transfer)       -> Default Wire_Manual
 *   anything else           -> Default Outsourced_Check (fallback per Huron "Else" rule)
 * Accepted = Default + Check + Outsourced_Check, de-duplicated, padded to 3 entries.
 */
public class CemiSupplierFileSupplierTabRowBoFactoryTest {

    private static final String EMPTY = "";

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "P, true,  ACH_Manual",
            "P, false, Outsourced_Check",
            "F, true,  FX_Payments",
            "F, false, FX_Payments",
            "W, true,  Wire_Manual",
            "W, false, Wire_Manual",
            "NULL, true,  Outsourced_Check",
            "NULL, false, Outsourced_Check",
            "B, true,  Outsourced_Check",
            "B, false, Outsourced_Check"
    })
    void testDefaultPaymentType(final String paymentMethodCode, final boolean hasActiveBankAccounts,
            final String expectedDefaultPaymentType) {
        final String actual = CemiSupplierFileSupplierTabRowBoFactory.determineDefaultPaymentType(
                paymentMethodCode, hasActiveBankAccounts);
        assertEquals(expectedDefaultPaymentType, actual);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "P, true,  ACH_Manual,       Check, Outsourced_Check",
            "P, false, Outsourced_Check, Check, EMPTY",
            "F, true,  FX_Payments,      Check, Outsourced_Check",
            "W, true,  Wire_Manual,      Check, Outsourced_Check",
            "NULL, false, Outsourced_Check, Check, EMPTY",
            "B, false, Outsourced_Check, Check, EMPTY"
    })
    void testAcceptedPaymentTypes(final String paymentMethodCode, final boolean hasActiveBankAccounts,
            final String expectedFirst, final String expectedSecond, final String expectedThird) {
        final List<String> actual = CemiSupplierFileSupplierTabRowBoFactory.determinePaymentTypes(
                paymentMethodCode, hasActiveBankAccounts);
        assertEquals(3, actual.size());
        assertEquals(expectedFirst, actual.get(0));
        assertEquals(expectedSecond, actual.get(1));
        assertEquals(convertEmptyPlaceholder(expectedThird), actual.get(2));
    }

    private static String convertEmptyPlaceholder(final String value) {
        return "EMPTY".equals(value) ? EMPTY : value;
    }

}
