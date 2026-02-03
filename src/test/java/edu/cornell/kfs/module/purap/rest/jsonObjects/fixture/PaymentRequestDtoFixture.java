package edu.cornell.kfs.module.purap.rest.jsonObjects.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;

public enum PaymentRequestDtoFixture {

    JSON_PARSE_READ_WRITE_EXAMPLE_FILE_1("/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestDtoTest1.json",
            "V12345", "98765", "11/01/2025", "11/02/2025",
            "INV-2025-001", "1000", "Line1", "Line2",
            "Line3",
            "50", "Standard Freight", "25", "Misc Fee",
            "75", "Express Shipping",
            buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_GENERAL)),
    JSON_PARSE_READ_WRITE_FILE_2("/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestDtoTest2.json",
            "V54321", "98765", "12/15/2025", "12/16/2025",
            "INV-2025-002", "500", "No items", "No notes",
            StringUtils.EMPTY, 
            "0", StringUtils.EMPTY, "0", StringUtils.EMPTY,
            "0", StringUtils.EMPTY, buildItems(), buildNotes()),
    JSON_PARSE_READ_WRITE_FILE_3("/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestDtoTest3.json",
            "V67890", "98765", "10/05/2025", "10/06/2025",
            "INV-2025-003", "2500", "Multiple items", "Multiple notes",
            "Extra handling",
            "100", "Bulk Freight", "75", "Service Fee",
            "150", "Overnight Shipping",
            buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_5_200, PaymentRequestLineItemDtoFixture.ITEM_2_3_400,
                    PaymentRequestLineItemDtoFixture.ITEM_3_2_500),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_FIRST, PaymentRequestNoteDtoFixture.NOTE_SECOND,
                    PaymentRequestNoteDtoFixture.NOTE_THIRD)),
    JSON_PARSE_READ_FILE_4("/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestDtoTest_LegacyVersion.json",
           "73371-1", "1808975", "12/01/2025", "12/02/2025", 
           "123460", "67.89", "line 1", "line 2", "line 3",
           "10", "freight", "20", "misc", 
           "11", "shipping",
           buildItems(PaymentRequestLineItemDtoFixture.ITEM_LEGACY),
           buildNotes(PaymentRequestNoteDtoFixture.LEGACY_NOTE_1, PaymentRequestNoteDtoFixture.LEGACY_NOTE_2)),
    VALIDATION_TEST_EMPTY_NO_ITEMS_NO_NOTES(StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(), buildNotes(), false, buildMessageList(),
            buildMessageList("Vendor Number is a required field.", "PO Number is a required field.",
                    "Invoice Date is a required field.", "Received Date is a required field.",
                    "Invoice Number is a required field.",
                    "Invoice Amount is a required field.", "At least one item must be entered.",
                    "At least one note must be entered.")),
    VALIDATION_TEST_EMPTY(StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.EMPTY_ITEM),
            buildNotes(PaymentRequestNoteDtoFixture.EMPTY_NOTE), false, buildMessageList(),
            buildMessageList("Vendor Number is a required field.", "PO Number is a required field.",
                    "Invoice Date is a required field.", "Received Date is a required field.",
                    "Invoice Number is a required field.",
                    "Invoice Amount is a required field.", "Item Price is a required field.",
                    "Item Quantity is a required field.",
                    "Item Line Number is a required field.", "Note Text is a required field.")),
    VALIDATION_TEST_FULL_VALUE_BAD_NOTE_TYPE(StringUtils.EMPTY, "vendNumber", "98765",
            "11/25/2025", "11/26/2025",
            "invoiceNumber", "50", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_FIRST), false, buildMessageList(),
            buildMessageList("If a note type is entered, it must be 'Other' or 'Invoice Image'.")),
    VALIDATION_TEST_BAD_VENDOR(StringUtils.EMPTY, "13245-1", "98765",
            "11/25/2025", "11/26/2025",
            "invoiceNumber", "50", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
            buildMessageList("Vendor Number 13245-1 is not valid.")),
    VALIDATION_TEST_GOOD_VENDOR_BAD_PO(StringUtils.EMPTY, "1234-1", "98765",
            "11/25/2025", "11/26/2025",
            "invoiceNumber", "50", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
            buildMessageList("PO Number 98765 is not valid.")),
    VALIDATION_TEST_GOOD_VENDOR_GOOD_PO_BAD_VENDOR(StringUtils.EMPTY, "1234-1", "98766",
            "11/25/2025", "11/26/2025",
            "invoiceNumber", "50", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
            buildMessageList("PO Number 98766 has a vendor number 987-32 but the vendor number supplied was 1234-1.")),
    VALIDATION_TEST_GOOD_VENDOR_GOOD_PO_NOT_OPEN(StringUtils.EMPTY, "1234-1", "98767",
            "11/25/2025", "11/26/2025",
            "invoiceNumber", "50", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
            buildMessageList("PO Number 98767 is not open, it has a status of Awaiting Purchasing Approval.")),
    VALIDATION_TEST_GOOD_VENDOR_GOOD_PO_OPEN_BAD_LINE(StringUtils.EMPTY, "1234-1", "98768",
            "11/25/2025", "11/26/2025",
            "invoiceNumber", "50", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
            buildMessageList("PO Number 98768 does not have a line number 1.")),
    VALIDATION_TEST_GOOD_VENDOR_GOOD_PO_OPEN_GOOD_LINE(StringUtils.EMPTY, "1234-1", "98769",
            "11/25/2025", "11/26/2025",
            "invoiceNumber", "50", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            null, StringUtils.EMPTY, null, StringUtils.EMPTY, null,
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), true,
            buildMessageList(), buildMessageList()),
    VALIDATION_TEST_FORMAT_ERRORS(StringUtils.EMPTY, "1234-1", "98769A",
            "11/25/2025X", "111/26/2025",
            "invoiceNumber", "50.Y", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            "29-", StringUtils.EMPTY, "$13.00", StringUtils.EMPTY, "abcd",
            StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_FORMAT_ERRORS),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false,
            buildMessageList(), 
            buildMessageList("PO Number is not a valid integer", "Invoice Date must be in the format of MM/DD/YYYY.",
                "Received Date must be in the format of MM/DD/YYYY.", "Invoice Number is not a valid decimal.",
                "Item Price is not a valid decimal.", "Item Quantity is not a valid decimal.", "Item Line Number is not a valid integer"));

    public final String jsonFileName;
    public final String vendorNumber;
    public final String poNumber;
    public final String invoiceDate;
    public final String receivedDate;
    public final String invoiceNumber;
    public final String invoiceAmount;
    public final String specialHandlingLine1;
    public final String specialHandlingLine2;
    public final String specialHandlingLine3;
    public final List<PaymentRequestLineItemDtoFixture> items;
    public final String freightPrice;
    public final String freightDescription;
    public final String miscellaneousPrice;
    public final String miscellaneousDescription;
    public final String shippingPrice;
    public final String shippingDescription;
    public final List<PaymentRequestNoteDtoFixture> notes;
    public boolean expectedValidation;
    public List<String> expectedSuccessMessages;
    public List<String> expectedErrorMessages;

    private PaymentRequestDtoFixture(String jsonFileName, String vendorNumber, String poNumber, String invoiceDate,
            String receivedDate, String invoiceNumber, String invoiceAmount, String specialHandlingLine1,
            String specialHandlingLine2,
            String specialHandlingLine3, String freightPrice, String freightDescription,
            String miscellaneousPrice,
            String miscellaneousDescription, String shippingPrice, String shippingDescription,
            List<PaymentRequestLineItemDtoFixture> items, List<PaymentRequestNoteDtoFixture> notes) {
        this(jsonFileName, vendorNumber, poNumber, invoiceDate, receivedDate, invoiceNumber, invoiceAmount,
                specialHandlingLine1, specialHandlingLine2, specialHandlingLine3, freightPrice, freightDescription,
                miscellaneousPrice, miscellaneousDescription, shippingPrice, shippingDescription, items, notes,
                false, null, null);
    }

    private PaymentRequestDtoFixture(String jsonFileName, String vendorNumber, String poNumber, String invoiceDate,
            String receivedDate, String invoiceNumber, String invoiceAmount, String specialHandlingLine1,
            String specialHandlingLine2,
            String specialHandlingLine3, String freightPrice, String freightDescription,
            String miscellaneousPrice,
            String miscellaneousDescription, String shippingPrice, String shippingDescription,
            List<PaymentRequestLineItemDtoFixture> items, List<PaymentRequestNoteDtoFixture> notes,
            boolean expectedValidation, List<String> expectedSuccessMessages, List<String> expectedErrorMessages) {
        this.jsonFileName = jsonFileName;
        this.vendorNumber = vendorNumber;
        this.poNumber = poNumber;
        this.invoiceDate = invoiceDate;
        this.receivedDate = receivedDate;
        this.invoiceNumber = invoiceNumber;
        this.invoiceAmount = invoiceAmount;
        this.specialHandlingLine1 = specialHandlingLine1;
        this.specialHandlingLine2 = specialHandlingLine2;
        this.specialHandlingLine3 = specialHandlingLine3;
        this.freightPrice = freightPrice;
        this.freightDescription = freightDescription;
        this.miscellaneousPrice = miscellaneousPrice;
        this.miscellaneousDescription = miscellaneousDescription;
        this.shippingPrice = shippingPrice;
        this.shippingDescription = shippingDescription;
        this.items = items;
        this.notes = notes;
        this.expectedValidation = expectedValidation;
        this.expectedSuccessMessages = expectedSuccessMessages;
        this.expectedErrorMessages = expectedErrorMessages;
    }

    private static List<PaymentRequestLineItemDtoFixture> buildItems(PaymentRequestLineItemDtoFixture... items) {
        return Collections.unmodifiableList(Arrays.asList(items));
    }

    private static List<PaymentRequestNoteDtoFixture> buildNotes(PaymentRequestNoteDtoFixture... notes) {
        return Collections.unmodifiableList(Arrays.asList(notes));
    }

    private static List<String> buildMessageList(String... messages) {
        return Collections.unmodifiableList(Arrays.asList(messages));
    }

    public PaymentRequestDto toPaymentRequestDto() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setVendorNumber(vendorNumber);
        dto.setPoNumber(poNumber);
        dto.setInvoiceDate(invoiceDate);
        dto.setReceivedDate(receivedDate);
        dto.setInvoiceNumber(invoiceNumber);
        dto.setInvoiceAmount(invoiceAmount);
        dto.setSpecialHandlingLine1(specialHandlingLine1);
        dto.setSpecialHandlingLine2(specialHandlingLine2);
        dto.setSpecialHandlingLine3(specialHandlingLine3);
        dto.setFreightPrice(freightPrice);
        dto.setFreightDescription(freightDescription);
        dto.setMiscellaneousPrice(miscellaneousPrice);
        dto.setMiscellaneousDescription(miscellaneousDescription);
        dto.setShippingPrice(shippingPrice);
        dto.setShippingDescription(shippingDescription);
        items.stream().forEach(item -> dto.getItems().add(item.toPaymentRequestLineItemDto()));
        notes.stream().forEach(note -> dto.getNotes().add(note.toPaymentRequestNoteDto()));
        return dto;
    }
}
