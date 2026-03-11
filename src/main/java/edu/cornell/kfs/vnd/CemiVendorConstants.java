package edu.cornell.kfs.vnd;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorConstants.AddressTypes;

import edu.cornell.kfs.coa.businessobject.options.CuCheckingSavingsValuesFinder.BankAccountTypes;
import edu.cornell.kfs.vnd.CUVendorConstants.CUAddressTypes;

public final class CemiVendorConstants {

    public static final String EMPTY_STRING = KFSConstants.EMPTY_STRING;
    
    public static final String SUPPLIER_ID_FORMAT = "'SUP'000000";
    public static final String SUPPLIER_REFERENCE_ID_FORMAT = "SUPU_{0}-{1}";
    public static final String ADDRESS_ID_FORMAT = "{0}_{1}_{2}";
    public static final String PHONE_ID_FORMAT = "{0}_{1}_{2}";
    public static final String BANK_ACCOUNT_ID_FORMAT = "{0}_{1}_{2}";
    public static final int SUPPLIER_HEADER_ROWS_PER_SHEET = 6;
    public static final int MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES = 3;

    public static final String SUPPLIER_OUTPUT_DEFINITION_FILE_PATH = "classpath:edu/cornell/kfs/vnd/batch/CemiSupplierExtractFileOutputDefinition.xml";
    public static final String SUPPLIER_TEMPLATE_FILE_PATH = "classpath:edu/cornell/kfs/vnd/batch/Supplier.xlsx";
    public static final String SUPPLIER_EXTRACT_FILENAME_PREFIX = "Supplier_ITH_";
    public static final String SUPPLIER_EXTRACT_PLAIN_FILENAME = "Supplier.xlsx";

    public static final String DEFAULT_SUPPLIER_CATEGORY = "Foundation_Default";
    public static final String DEFAULT_PAYMENT_TYPE = "Check";
    public static final String DEFAULT_CURRENCY = "USD";
    public static final String DEFAULT_NAME_USAGE = "Reference";
    public static final String DEFAULT_ADDRESS_TYPE = "BUSINESS";
    public static final String DEFAULT_INTERNATIONAL_PHONE_TYPE = "1";
    public static final String DEFAULT_PHONE_DEVICE_TYPE = "Landline";

    public static final String USA_EIN_TAX_TYPE = "USA-EIN";
    public static final String USA_SSN_TAX_TYPE = "USA-SSN";

    public static final String CHECKING_ACCOUNT_TYPE = "DDA";
    public static final String SAVINGS_ACCOUNT_TYPE = "SA";

    public static final String DUMMY_TAX_ID = "XXXXXXXXX";
    public static final String DUMMY_ACCOUNT_NUMBER = "XXXXXXXXX";
    public static final String ALTERNATE_NAME_USAGE_DEFAULT_VALUE = "Reference";
    public static final String COUNTRY_CODE_UNITED_STATES = KFSConstants.COUNTRY_CODE_UNITED_STATES;

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

    public static final Map<String, String> TAX_ID_TYPES = Map.ofEntries(
            Map.entry(VendorConstants.TAX_TYPE_FEIN, USA_EIN_TAX_TYPE),
            Map.entry(VendorConstants.TAX_TYPE_SSN, USA_SSN_TAX_TYPE)
    );

    public static final Map<String, String> BANK_ACCOUNT_TYPES = Map.ofEntries(
            Map.entry(BankAccountTypes.PERSONAL_CHECKING, CHECKING_ACCOUNT_TYPE),
            Map.entry(BankAccountTypes.PERSONAL_SAVINGS, SAVINGS_ACCOUNT_TYPE),
            Map.entry(BankAccountTypes.CORPORATE_CHECKING, CHECKING_ACCOUNT_TYPE),
            Map.entry(BankAccountTypes.CORPORATE_SAVINGS, SAVINGS_ACCOUNT_TYPE)
    );

    public static final class AllDefinedAddressTypes {
        public static final String PURCHASE_ORDER = VendorConstants.AddressTypes.PURCHASE_ORDER;
        public static final String REMIT = VendorConstants.AddressTypes.REMIT;
        public static final String QUOTE = VendorConstants.AddressTypes.QUOTE;
        public static final String TAX = CUVendorConstants.CUAddressTypes.TAX;
    }

    public static final class CemiQuerySettingsIds {
        public static final String SUPPLIERS = "SUPPLIERS";
    }

    public static final class SupplierExtractSheets {
        public static final String SUPPLIER = "Supplier";
        public static final String ADDRESSES = "Addresses";
        public static final String EMAILS = "Emails";
        public static final String PHONES = "Phones";
        public static final String BANK_ACCOUNTS = "Bank_Accounts";
        public static final String CHILDREN = "Children";
    }

    public static final class TaxAuthorityFormTypes {
        public static final String FORM_1099_MISC = "1099_MISC";
        public static final String FORM_1042S = "1042-S";
    }

    
    // Phone Types can be created by the functional users in the transactional system.
    // There are no constants class in base code nor Cornell's overlay that lists all phone types.
    // Defining all the values available here for the data conversion efforts (both active and inactive).
    public static final class AllDefinedPhoneTypes {
        public static final String ACCOUNTS_RECEIVABLE_PHONE = "AR";
        public static final String CONTRACT_DEVELOPMENT = "CD";
        public static final String CUSTOMER_SERVICE = "CS";
        public static final String E_INVOICING = "EI";
        public static final String FAX = "FX";
        public static final String INSURANCE = "IN";
        public static final String MOBILE = "MB";
        public static final String PAGER = "PG";
        public static final String MAIN_PHONE_NUMBER = "PH";
        public static final String PURCHASE_ORDER = "PO";
        public static final String PARTS = "PT";
        public static final String SALES = "SA";
        public static final String SERVICE_MAINTENANCE = "SM";
        public static final String TOLL_FREE = "TF";
        public static final String TECHNICAL_SUPPORT = "TS";
        public static final String VENDOR_INFORMATION = "VI";
    }
    
    public static final Map<String, List<String>> PHONE_USES = Map.ofEntries(
            Map.entry(AllDefinedPhoneTypes.ACCOUNTS_RECEIVABLE_PHONE, List.of("REMIT")),
            Map.entry(AllDefinedPhoneTypes.CUSTOMER_SERVICE, List.of("REMIT")),
            Map.entry(AllDefinedPhoneTypes.E_INVOICING, List.of("REMIT")),
            Map.entry(AllDefinedPhoneTypes.FAX, List.of("REMIT")),
            Map.entry(AllDefinedPhoneTypes.INSURANCE, List.of("BILLING", "SHIPPING")),
            Map.entry(AllDefinedPhoneTypes.MOBILE, List.of("BILLING", "SHIPPING")),
            Map.entry(AllDefinedPhoneTypes.MAIN_PHONE_NUMBER, List.of("PROCUREMENT", "REMIT")),
            Map.entry(AllDefinedPhoneTypes.PURCHASE_ORDER, List.of("PROCUREMENT")),
            Map.entry(AllDefinedPhoneTypes.PARTS, List.of("SHIPPING")),
            Map.entry(AllDefinedPhoneTypes.SALES, List.of("BILLING")),
            Map.entry(AllDefinedPhoneTypes.SERVICE_MAINTENANCE, List.of("PROCUREMENT", "REMIT")),
            Map.entry(AllDefinedPhoneTypes.TOLL_FREE, List.of("PROCUREMENT", "REMIT")),
            Map.entry(AllDefinedPhoneTypes.TECHNICAL_SUPPORT, List.of("PROCUREMENT", "REMIT")),
            Map.entry(AllDefinedPhoneTypes.VENDOR_INFORMATION, List.of("PROCUREMENT", "REMIT", "SHIPPING", "BILLING"))
    );
    
    public static final Map<String, List<String>> PHONE_TENANTED_USES = Map.ofEntries(
            Map.entry(AllDefinedPhoneTypes.ACCOUNTS_RECEIVABLE_PHONE, List.of("Remit_To")),
            Map.entry(AllDefinedPhoneTypes.CUSTOMER_SERVICE, List.of("Remit_To")),
            Map.entry(AllDefinedPhoneTypes.E_INVOICING, List.of("Remit_To")),
            Map.entry(AllDefinedPhoneTypes.FAX, List.of("Remit_To")),
            Map.entry(AllDefinedPhoneTypes.INSURANCE, List.of("Billing", "Shipping")),
            Map.entry(AllDefinedPhoneTypes.MOBILE, List.of("Billing", "Shipping")),
            Map.entry(AllDefinedPhoneTypes.MAIN_PHONE_NUMBER, List.of("Procurement", "Remit_To")),
            Map.entry(AllDefinedPhoneTypes.PURCHASE_ORDER, List.of("Procurement")),
            Map.entry(AllDefinedPhoneTypes.PARTS, List.of("Shipping")),
            Map.entry(AllDefinedPhoneTypes.SALES, List.of("Billing")),
            Map.entry(AllDefinedPhoneTypes.SERVICE_MAINTENANCE, List.of("Procurement", "Remit_To")),
            Map.entry(AllDefinedPhoneTypes.TOLL_FREE, List.of("Procurement", "Remit_To")),
            Map.entry(AllDefinedPhoneTypes.TECHNICAL_SUPPORT, List.of("Procurement", "Remit_To")),
            Map.entry(AllDefinedPhoneTypes.VENDOR_INFORMATION, List.of("Procurement", "Remit_To", "Shipping", "Billing"))
    );

}
