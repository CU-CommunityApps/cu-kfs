package edu.cornell.kfs.vnd;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.vnd.VendorConstants.AddressTypes;

import edu.cornell.kfs.vnd.CUVendorConstants.CUAddressTypes;

public final class CemiVendorConstants {

    public static final String SUPPLIER_ID_FORMAT = "'SUP'000000";
    public static final String SUPPLIER_REFERENCE_ID_FORMAT = "ITH_{0}-{1}";
    public static final String ADDRESS_ID_FORMAT = "{0}_{1}_{2}";
    public static final int SUPPLIER_HEADER_ROWS_PER_SHEET = 6;

    public static final String SUPPLIER_OUTPUT_DEFINITION_FILE_PATH = "classpath:edu/cornell/kfs/vnd/batch/CemiSupplierExtractFileOutputDefinition.xml";
    public static final String SUPPLIER_TEMPLATE_FILE_PATH = "classpath:edu/cornell/kfs/vnd/batch/Supplier.xlsx";
    public static final String SUPPLIER_EXTRACT_FILENAME_PREFIX = "Supplier_ITH_";

    public static final String DEFAULT_SUPPLIER_CATEGORY = "Foundation_Default";
    public static final String DEFAULT_PAYMENT_TYPE = "Check";
    public static final String DEFAULT_CURRENCY = "USD";
    public static final String DEFAULT_NAME_USAGE = "Reference";
    public static final String DEFAULT_ADDRESS_TYPE = "BUSINESS";

    public static final Map<String, List<String>> ADDRESS_USES = Map.ofEntries(
            Map.entry(AddressTypes.PURCHASE_ORDER, List.of("PROCUREMENT", "SHIPPING")),
            Map.entry(AddressTypes.REMIT, List.of("REMIT")),
            Map.entry(CUAddressTypes.TAX, List.of("TAX"))
    );

    public static final Map<String, List<String>> ADDRESS_TENANTED_USES = Map.ofEntries(
            Map.entry(AddressTypes.PURCHASE_ORDER, List.of("Procurement", "Shipping")),
            Map.entry(AddressTypes.REMIT, List.of("Remit_To")),
            Map.entry(CUAddressTypes.TAX, List.of("Tax"))
    );

    public static final class CemiQuerySettingsIds {
        public static final String SUPPLIERS = "SUPPLIERS";
    }

    public static final class SupplierExtractSheets {
        public static final String SUPPLIER = "Supplier";
    }

    public static final class TaxAuthorityFormTypes {
        public static final String FORM_1099_MISC = "1099_MISC";
        public static final String FORM_1042S = "1042-S";
    }

}
