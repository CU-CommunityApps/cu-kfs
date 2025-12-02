package edu.cornell.kfs.module.purap.rest.jsonObjects.fixture;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;

public enum PaymentRequestDtoFixture {

    JSON_PARSE_EXAMPLE_FILE_1("/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestDtoTest1.json",
            "V12345", Integer.valueOf(98765), "11/01/2025", "11/02/2025",
            "INV-2025-001", new KualiDecimal(1000), "Line1", "Line2",
            "Line3",
            new KualiDecimal(50), "Standard Freight", new KualiDecimal(25), "Misc Fee",
            new KualiDecimal(75), "Express Shipping",
            buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_GENERAL)),
    JSON_PARSE_EXAMPLE_FILE_2("/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestDtoTest2.json",
            "V54321", Integer.valueOf(98765), "12/15/2025", "12/16/2025",
            "INV-2025-002", new KualiDecimal(500), "No items", "No notes",
            StringUtils.EMPTY,
            new KualiDecimal(0), StringUtils.EMPTY, new KualiDecimal(0), StringUtils.EMPTY,
            new KualiDecimal(0), StringUtils.EMPTY, buildItems(), buildNotes()),
    JSON_PARSE_EXAMPLE_FILE_3("/edu/cornell/kfs/modules/purap/rest/jsonObjects/fixture/PaymentRequestDtoTest3.json",
            "V67890", Integer.valueOf(98765), "10/05/2025", "10/06/2025",
            "INV-2025-003", new KualiDecimal(2500), "Multiple items", "Multiple notes",
            "Extra handling",
            new KualiDecimal(100), "Bulk Freight", new KualiDecimal(75), "Service Fee",
            new KualiDecimal(150), "Overnight Shipping",
            buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_5_200, PaymentRequestLineItemDtoFixture.ITEM_2_3_400,
                    PaymentRequestLineItemDtoFixture.ITEM_3_2_500),
            buildNotes(PaymentRequestNoteDtoFixture.NOTE_FIRST, PaymentRequestNoteDtoFixture.NOTE_SECOND,
                    PaymentRequestNoteDtoFixture.NOTE_THIRD)),
    VALIDATION_TEST_EMPTY_NO_ITEMS_NO_NOTES(StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, 
        StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(), buildNotes(), false, buildMessageList(),
        buildMessageList("Vendor Number is a required field.", "PO Number is a required field.", 
            "Invoice Date is a required field.", "Received Date is a required field.", "Invoice Number is a required field.", 
            "Invoice Amount is a required field.", "At least one item must be entered.", "At least one note must be entered.")),
    VALIDATION_TEST_EMPTY(StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, 
        StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.EMPTY_ITEM), 
        buildNotes(PaymentRequestNoteDtoFixture.EMPTY_NOTE), false, buildMessageList(),
        buildMessageList("Vendor Number is a required field.", "PO Number is a required field.", 
            "Invoice Date is a required field.", "Received Date is a required field.", "Invoice Number is a required field.", 
            "Invoice Amount is a required field.", "Item Price is a required field.", "Item Quantity is a required field.",
            "Item Line Number is a required field.", "Note Text is a required field.")),
    VALIDATION_TEST_FULL_VALUE_BAD_NOTE_TYPE(StringUtils.EMPTY, "vendNumber", Integer.valueOf(98765),  
        "11/25/2025",  "11/26/2025", 
        "invoiceNumber", new KualiDecimal(50), StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100), 
        buildNotes(PaymentRequestNoteDtoFixture.NOTE_FIRST), false, buildMessageList(),
        buildMessageList("If a note type is entered, it must be 'Other' or 'Invoice Image'.")),
    VALIDATION_TEST_BAD_VENDOR(StringUtils.EMPTY, "13245-1", Integer.valueOf(98765),  
        "11/25/2025",  "11/26/2025", 
        "invoiceNumber", new KualiDecimal(50), StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100), 
        buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
        buildMessageList("Vendor Number 13245-1 is not valid.")),
    VALIDATION_TEST_GOOD_VENDOR_BAD_PO(StringUtils.EMPTY, "1234-1", Integer.valueOf(98765),  
        "11/25/2025",  "11/26/2025", 
        "invoiceNumber", new KualiDecimal(50), StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100), 
        buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
        buildMessageList("PO Number 98765 is not valid.")),
    VALIDATION_TEST_GOOD_VENDOR_GOOD_PO_BAD_VENDOR(StringUtils.EMPTY, "1234-1", Integer.valueOf(98766),  
        "11/25/2025",  "11/26/2025", 
        "invoiceNumber", new KualiDecimal(50), StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100), 
        buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
        buildMessageList("PO Number 98766 has a vendor number 987-32 but the vendor number supplied was 1234-1.")),
    VALIDATION_TEST_GOOD_VENDOR_GOOD_PO_NOT_OPEN(StringUtils.EMPTY, "1234-1", Integer.valueOf(98767),  
        "11/25/2025",  "11/26/2025", 
        "invoiceNumber", new KualiDecimal(50), StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100), 
        buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
        buildMessageList("PO Number 98767 is not open, it has a status of Awaiting Purchasing Approval.")),
    VALIDATION_TEST_GOOD_VENDOR_GOOD_PO_OPEN_BAD_LINE(StringUtils.EMPTY, "1234-1", Integer.valueOf(98768),  
        "11/25/2025",  "11/26/2025", 
        "invoiceNumber", new KualiDecimal(50), StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100), 
        buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), false, buildMessageList(),
        buildMessageList("PO Number 98768 does not have a line number 1.")),
    VALIDATION_TEST_GOOD_VENDOR_GOOD_PO_OPEN_GOOD_LINE(StringUtils.EMPTY, "1234-1", Integer.valueOf(98769),  
        "11/25/2025",  "11/26/2025", 
        "invoiceNumber", new KualiDecimal(50), StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 
        null, StringUtils.EMPTY, null, StringUtils.EMPTY, null, 
        StringUtils.EMPTY, buildItems(PaymentRequestLineItemDtoFixture.ITEM_1_10_100), 
        buildNotes(PaymentRequestNoteDtoFixture.NOTE_VALID), true, 
        buildMessageList("Successfully passed validation"), buildMessageList());

    private static final DateTimeFormatter DATE_FORMATTER_MMDDYYYY = DateTimeFormatter.ofPattern(
            KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);

    public final String jsonFileName;
    public final String vendorNumber;
    public final Integer poNumber;
    public final String invoiceDate;
    public final String receivedDate;
    public final String invoiceNumber;
    public final KualiDecimal invoiceAmount;
    public final String specialHandlingLine1;
    public final String specialHandlingLine2;
    public final String specialHandlingLine3;
    public final List<PaymentRequestLineItemDtoFixture> items;
    public final KualiDecimal freightPrice;
    public final String freightDescription;
    public final KualiDecimal miscellaneousPrice;
    public final String miscellaneousDescription;
    public final KualiDecimal shippingPrice;
    public final String shippingDescription;
    public final List<PaymentRequestNoteDtoFixture> notes;
    public boolean expectedValidation;
    public List<String> expectedSuccessMessages;
    public List<String> expectedErrorMessages;

    private PaymentRequestDtoFixture(String jsonFileName, String vendorNumber, Integer poNumber, String invoiceDate,
            String receivedDate, String invoiceNumber, KualiDecimal invoiceAmount, String specialHandlingLine1,
            String specialHandlingLine2,
            String specialHandlingLine3, KualiDecimal freightPrice, String freightDescription,
            KualiDecimal miscellaneousPrice,
            String miscellaneousDescription, KualiDecimal shippingPrice, String shippingDescription,
            List<PaymentRequestLineItemDtoFixture> items, List<PaymentRequestNoteDtoFixture> notes) {
        this(jsonFileName, vendorNumber, poNumber, invoiceDate, receivedDate, invoiceNumber, invoiceAmount, 
            specialHandlingLine1, specialHandlingLine2, specialHandlingLine3, freightPrice, freightDescription, 
            miscellaneousPrice, miscellaneousDescription, shippingPrice, shippingDescription, items, notes, 
            false, null, null);
    }

    private PaymentRequestDtoFixture(String jsonFileName, String vendorNumber, Integer poNumber, String invoiceDate,
            String receivedDate, String invoiceNumber, KualiDecimal invoiceAmount, String specialHandlingLine1,
            String specialHandlingLine2,
            String specialHandlingLine3, KualiDecimal freightPrice, String freightDescription,
            KualiDecimal miscellaneousPrice,
            String miscellaneousDescription, KualiDecimal shippingPrice, String shippingDescription,
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

    private static List<String> buildMessageList(String ... messages) {
        return Collections.unmodifiableList(Arrays.asList(messages));
    }

    public PaymentRequestDto toPaymentRequestDto() {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setVendorNumber(vendorNumber);
        dto.setPoNumber(poNumber);
        dto.setInvoiceDate(buildDate(invoiceDate));
        dto.setReceivedDate(buildDate(receivedDate));
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

    private Date buildDate(String dateString) {
        try {
            LocalDate localDate = LocalDate.parse(dateString, DATE_FORMATTER_MMDDYYYY);
            return Date.valueOf(localDate);
        } catch (Exception e) {
            return null;
        }
    }
}
