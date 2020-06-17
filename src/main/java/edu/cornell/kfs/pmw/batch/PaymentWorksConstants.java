package edu.cornell.kfs.pmw.batch;

import java.util.Arrays;
import java.util.regex.Pattern;

import edu.cornell.kfs.coa.businessobject.options.CuCheckingSavingsValuesFinder;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.purap.CUPurapConstants;

public class PaymentWorksConstants {
    
    public static final String PAYMENTWORKS_NAMESPACE_CODE = "KFS-PMW";
    public static final String PAYMENTWORKS_WEB_SERVICE_GROUP_CODE = "PMW";

    public static final String OUTPUT_ATTRIBUTE_BEGIN_DELIMITER = ":<";
    public static final String OUTPUT_ATTRIBUTE_END_DELIMITER = "> ";
    public static final String OUTPUT_RESTRICTED_DATA_PRESENT = "RestrictedDataPresent";

    public final static String DIVERSITY_EXPIRATION_DATE_CERTIFIED = "Certified";
    public final static String REGEX_FOR_MM_SLASH_DD_SLASH_YYYY = "^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$";
    public final static Pattern PATTERN_COMPILED_REGEX_FOR_MM_SLASH_DD_SLASH_YYYY = Pattern.compile(REGEX_FOR_MM_SLASH_DD_SLASH_YYYY);
    public final static String REGEX_FOR_YYYY_SLASH_MM_SLASH_DD = "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$";
    public final static Pattern PATTERN_COMPILED_REGEX_FOR_YYYY_SLASH_MM_SLASH_DD = Pattern.compile(REGEX_FOR_YYYY_SLASH_MM_SLASH_DD);
    
    public static final String NULL_STRING = "NULL";
    public static final String SUPPLIER_UPLOAD_DELETE_INDICATOR = "DEL";
    
    public static final String FOREIGN_FORM_PROCESSING_NOT_IMPLEMENTED_LOG_MESSAGE = "PMW foreign mode processing not implemented yet.";
    
    public static final class PaymentWorksCustomFieldBooleanPrimitive {
        public static final String YES = "YES";
        public static final String NO = "NO";
    }
    
    public static final class KFSVendorProcessingStatus {
        public static final String VENDOR_REQUESTED = "Vendor Requested";
        public static final String VENDOR_CREATED = "Vendor Created";
        public static final String VENDOR_REJECTED = "Vendor Rejected";
        public static final String VENDOR_APPROVED = "Vendor Approved";
        public static final String VENDOR_DISAPPROVED = "Vendor Disapproved";
    }
    
    public static final class KFSAchProcessingStatus {
        public static final String PENDING_PVEN = "Pending PVEN";
        public static final String NO_VENDOR_IDENTIFIERS = "No Vendor Identifiers";
        public static final String NO_ACH_DATA = "No ACH Data";
        public static final String ACH_REQUESTED = "ACH Requested";
        public static final String ACH_REJECTED = "ACH Rejected";
        public static final String ACH_CREATED = "ACH Created";
        public static final String PVEN_DISAPPROVED = "PVEN Disapproved";
        public static final String EXCEPTION_GENERATED = "Exception Generated";
    }
    
    public static final class SupplierUploadStatus {
        public static final String PENDING_PAAT = "Pending PAAT";
        public static final String INELIGIBLE_FOR_UPLOAD = "Ineligible for Upload";
        public static final String READY_FOR_UPLOAD = "Ready for Upload";
        public static final String VENDOR_UPLOADED = "Vendor Uploaded";
        public static final String UPLOAD_FAILED = "Upload Failed";
    }
    
    public static final class PaymentWorksTransactionType {
        public static final String NEW_VENDOR = "NV";
        public static final String KFS_ORIGINATING_VENDOR = "KV";
    }
    
    public static final class PaymentWorksVendorType {
        public static final String PURCHASE_ORDER = "Purchase Order";
    }
    
    public static final class PaymentWorksNewVendorTaxBusinessRule {
        //INDIVIDUAL means "Individual, sole proprietor or single-member LLC" = Yes
        //NOT_INDIVIDUAL means "Individual, sole proprietor or single-member LLC" = No
        public static final int INDIVIDUAL_US_SSN = 1;
        public static final int INDIVIDUAL_US_EIN = 2;
        public static final int NOT_INDIVIDUAL_US = 3;
        public static final int INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_INDIVIDUAL = 41;
        public static final int INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_OTHER = 42;  //Generate error when encountered
        public static final int INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_INDIVIDUAL = 51;
        public static final int INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_OTHER = 52;  //Generate error when encountered
        public static final int NOT_INDIVIDUAL_NOT_US_EIN = 6;
        public static final int NOT_INDIVIDUAL_NOT_US_FOREIGN = 7;
        public static final int COULD_NOT_DETERMINE_TAX_RULE_TO_USE = -1;
    }
    
    public static final class KFSVendorMaintenaceDocumentConstants {
        public static final String DESCRIPTION_SUFFIX_FOR_NEW_VENDOR = "--new vendor";
        public static final Integer DESCRIPTION_MAX_LENGTH = 40;
        public static final String PAYMENTWORKS_NEW_VENDOR_CREATE_ROUTE_ANNOTATION = "Generated by PaymentWorks New Vendor Create Batch Process";
        
        public static final class KFSPoVendorConstants {
            public static final String PAYMENT_TERMS_NET_60_DAYS_CODE = "00N60";
            public static final String VENDOR_SHIPPING_TITLE_DESTINATION_CODE = "DE";
            public static final String VENDOR_SHIPPING_PAYMENT_TERMS_PREPAID_AND_ADD_CODE = "PA";
        }
    }
    
    public static final class KFSPayeeAchMaintenanceDocumentConstants {
        public static final String DESCRIPTION_PREFIX_FOR_NEW_PAYEE_ACH = "New";
        public static final Integer DESCRIPTION_MAX_LENGTH = 40;
        public static final String PAYMENTWORKS_NEW_VENDOR_ACH_CREATE_ROUTE_ANNOTATION = "Generated by PaymentWorks New Vendor ACH Create Batch Process";
        public static final String ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE = "PRAP";
    }
    
    public static final class KFSVendorContactTypes {
        public static final String ACCOUNTS_RECEIVABLE = "AR";
        public static final String E_INVOICING = "EI";
        public static final String INSURANCE = "IN";
        public static final String SALES = "SR";
        public static final String VENDOR_INFORMATION_FORM = "VI";
    }
    
    public static final class KFSVendorContactPhoneTypes {
        public static final String ACCOUNTS_RECEIVABLE_PHONE = "AR";
        public static final String E_INVOICING = "EI";
        public static final String INSURANCE = "IN";
        public static final String SALES = "SA";
        public static final String VENDOR_INFORMATION = "VI";
    }
    
    public static final class PaymentWorksBatchReportNames {
        public static final String NEW_VENDOR_REQUESTS_REPORT_NAME = "New Vendor Requests Create KFS Vendor";
        public static final String NEW_VENDOR_REQUESTS_PAYEE_ACH_REPORT_NAME = "New Vendor Requests Create KFS Payee ACH Account";
        public static final String NEW_VENDOR_REQUESTS_SUPPLIER_UPLOAD_REPORT_NAME = "New Vendor Requests Upload Suppliers to PaymentWorks";
    }
    
    public static final class PaymentWorksVendorStagingTableParseExceptionConstants {
        public static final String DEFAULT_EXCEPTION_MESSAGE = "Could not determine exception failure reason for initial KFS staging table save of PaymentWorks vendor.";
        public static final String EXCEPTION_REASON_START_STRING = "value too large for column";
        public static final char EXCEPTION_REASON_END_CHAR = ')';
    }
    
    public static final class CustomAttributeValueToUse {
        public static final String FILE = "file";
        public static final String FIELD_VALUE = "field_value";
    }
    
    public enum PaymentWorksNewVendorRequestStatusType {
        APPROVED(1, "1", "Approved"),
        PROCESSED(2, "2", "Processed"),
        CONNECTED(3, "3", "Connected"),
        REJECTED(4, "4", "Rejected");
        
        public final int code;
        public final String codeAsString;
        public final String text;
        
        private PaymentWorksNewVendorRequestStatusType(int code, String codeAsString, String text) {
            this.code = code;
            this.codeAsString = codeAsString;
            this.text = text;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getCodeAsString() {
            return codeAsString;
        }
        
        public String getText() {
            return text;
        }
    }
    
    public enum PaymentWorksTinType {
        SSN(0, "0", "SSN", "SSN"),
        FEIN(1, "1", "FEIN", "FEIN"),
        ITIN(2, "2", "ITIN", "NONE"),
        FOREIGN_TIN(3, "3", "Foreign TIN", "NONE");
        
        public final int pmwCode;
        public final String pmwCodeAsString;
        public final String pmwText;
        public final String kfsTaxTypeCodeAsString;
        
        private PaymentWorksTinType(int pmwCode, String pmwCodeAsString, String pmwText, String kfsTaxTypeCodeAsString) {
            this.pmwCode = pmwCode;
            this.pmwCodeAsString = pmwCodeAsString;
            this.pmwText = pmwText;
            this.kfsTaxTypeCodeAsString = kfsTaxTypeCodeAsString;
        }
        
        public int getPmwCode() {
            return pmwCode;
        }
        
        public String getPmwCodeAsString() {
            return pmwCodeAsString;
        }
        
        public String getPmwText() {
            return pmwText;
        }
        
        public String getKfsTaxTypeCodeAsString() {
            return kfsTaxTypeCodeAsString;
        }
    }
    //Constants were declared instead of using the values directly in the 
    //enumeration to allow the use of switch statements in the code.
    public static final int INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR  = 0;
    public static final int C_CORPORATION_TAX_CLASSIFICATION_INDICATOR  = 1;
    public static final int S_CORPORATION_TAX_CLASSIFICATION_INDICATOR  = 2;
    public static final int PARTNERSHIP_TAX_CLASSIFICATION_INDICATOR  = 3;
    public static final int TRUST_ESTATE_TAX_CLASSIFICATION_INDICATOR  = 4;
    public static final int LLC_TAXED_AS_C_CORPORATION_TAX_CLASSIFICATION_INDICATOR  = 5;
    public static final int LLC_TAXED_AS_S_CORPORATION_TAX_CLASSIFICATION_INDICATOR  = 6;
    public static final int LLC_TAXED_AS_PARTNERSHIP_TAX_CLASSIFICATION_INDICATOR  = 7;
    public static final int OTHER_TAX_CLASSIFICATION_INDICATOR  = 8;
    public enum PaymentWorksTaxClassification {
        INDIVIDUAL_SOLE_PROPRIETOR(INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR, "Individual/sole proprietor or single-member LLC", "ID"),
        C_CORPORATION(C_CORPORATION_TAX_CLASSIFICATION_INDICATOR, "C Corporation", "CP"),
        S_CORPORATION(S_CORPORATION_TAX_CLASSIFICATION_INDICATOR, "S Corporation", "SC"),
        PARTNERSHIP(PARTNERSHIP_TAX_CLASSIFICATION_INDICATOR, "Partnership", "PT"),
        TRUST_ESTATE(TRUST_ESTATE_TAX_CLASSIFICATION_INDICATOR, "Trust/estate", "ET"), 
        LLC_TAXED_AS_C_CORPORATION(LLC_TAXED_AS_C_CORPORATION_TAX_CLASSIFICATION_INDICATOR, "LLC taxed as C Corporation", "CP"),
        LLC_TAXED_AS_S_CORPORATION(LLC_TAXED_AS_S_CORPORATION_TAX_CLASSIFICATION_INDICATOR, "LLC taxed as S Corporation", "SC"),
        LLC_TAXED_AS_PARTNERSHIP(LLC_TAXED_AS_PARTNERSHIP_TAX_CLASSIFICATION_INDICATOR, "LLC taxed as Partnership", "PT"),
        OTHER(OTHER_TAX_CLASSIFICATION_INDICATOR, "Other", "OT");
        
        public final int pmwCode;
        public final String pmwDescription;
        public final String translationToKfsOwnershipTypeCode;
        
        private PaymentWorksTaxClassification(int pmwCode, String pmwDescription, String translationToKfsOwnershipTypeCode) {
            this.pmwCode = pmwCode;
            this.pmwDescription = pmwDescription;
            this.translationToKfsOwnershipTypeCode = translationToKfsOwnershipTypeCode;
        }
        
        public String getTranslationToKfsOwnershipTypeCode() {
            return translationToKfsOwnershipTypeCode;
        }
    }
    
    public enum PaymentWorksGoodsVsServicesOptions {
        GOODS("Goods"),
        SERVICES("Services"),
        GOODS_WITH_SERVICES("Goods with a service component");
        
        public final String optionValueAsString;
        
        private PaymentWorksGoodsVsServicesOptions(String optionValueAsString) {
            this.optionValueAsString = optionValueAsString;
        }
        
        public String getOptionValueAsString() {
            return optionValueAsString;
        }
    }
    
    public static final int NOTE_TEXT_DEFAULT_MAX_LENGTH = 800;
    
    public enum ErrorDescriptorForBadKfsNote {
        W9("W9"),
        GOODS_AND_SERVICES("Goods and Services"),
        INITIATOR("Initiator"),
        BUSINESS_PURPOSE("Business Purpose"),
        CONFLICT_OF_INTEREST("Conflict of Interest"),
        INSURANCE_CERTIFICATE("Insurance Certificate"),
        PAAT_PMW_VENDOR_ID("PAAT Created From PMW Vendor ID");
        
        public final String noteDescriptionString;
        
        private ErrorDescriptorForBadKfsNote(String noteDescriptionString) {
            this.noteDescriptionString = noteDescriptionString;
        }
        
        public String getNoteDescriptionString() {
            return noteDescriptionString;
        }
    }
    
    public enum PaymentWorksBankAccountType {
        COMPANY_CHECKING("0", "Company Checking", CuCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_CHECKING, "Corporate Checking"),
        COMPANY_SAVINGS("1", "Company Savings", CuCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_SAVINGS, "Corporate Savings"),
        PERSONAL_CHECKING("2", "Personal Checking", CuCheckingSavingsValuesFinder.BankAccountTypes.PERSONAL_CHECKING, "Personal Checking"),
        PERSONAL_SAVINGS("3", "Personal Savings", CuCheckingSavingsValuesFinder.BankAccountTypes.PERSONAL_SAVINGS, "Personal Savings");
        
        public final String pmwCode;
        public final String pmwName;
        public final String translationToKfsBankAccountTypeCode;
        public final String translationToKfsBankAccountTypeDescription;
        
        private PaymentWorksBankAccountType(String pmwCode, String pmwName, String translationToKfsBankAccountTypeCode, String translationToKfsBankAccountTypeDescription) {
            this.pmwCode = pmwCode;
            this.pmwName = pmwName;
            this.translationToKfsBankAccountTypeCode = translationToKfsBankAccountTypeCode;
            this.translationToKfsBankAccountTypeDescription = translationToKfsBankAccountTypeDescription;
        }
        
        public String getPmwCode() {
            return pmwCode;
        }
        
        public String getTranslationToKfsBankAccountTypeCode() {
            return translationToKfsBankAccountTypeCode;
        }
    }

    public enum PaymentWorksUploadFileColumn {
        REQUEST_ID("RequestId", "pmwVendorRequestId"),
        VENDOR_NUM("VendorNum", "kfsVendorHeaderGeneratedIdentifier"),
        DIVISION_IND("DivisionInd", "kfsVendorDetailAssignedIdentifier"),
        VENDOR_NAME("VendorName", "requestingCompanyLegalNameForProcessing"),
        REMIT_ADDRESS1("RemitAddress1", "remittanceAddressStreet1"),
        REMIT_ADDRESS2("RemitAddress2", "remittanceAddressStreet2"),
        REMIT_CITY("RemitCity", "remittanceAddressCity"),
        REMIT_STATE("RemitState", "remittanceAddressState"),
        REMIT_COUNTRY("RemitCountry", "remittanceAddressCountry"),
        REMIT_POSTAL_CODE("RemitPostalCode", "remittanceAddressZipCode"),
        US_TAX_NUMBER("USTaxNumber", "requestingCompanyTin"),
        VI_CONTACT_EMAIL("VIContactEmail", "vendorInformationEmail");
        
        public final String headerLabel;
        public final String pmwVendorPropertyName;
        
        private PaymentWorksUploadFileColumn(String headerLabel, String pmwVendorPropertyName) {
            this.headerLabel = headerLabel;
            this.pmwVendorPropertyName = pmwVendorPropertyName;
        }
        
        public String getHeaderLabel() {
            return headerLabel;
        }
        
        public String getPmwVendorPropertyName() {
            return pmwVendorPropertyName;
        }
    }

    //Enum just deals with performing conversion from mixed case KFS-FIPS country name to KFS-FIPS
    //country code for items in the drop down associated with vendor answers to PMW form question:
    //"Is PO Address Country the United States, Canada, Australia, or Other?"
    //
    //Custom field "PO Country" is a mixed case KFS-FIPS country name tied to the selection of "Other" in the
    //previsouly described drop down and returns the user selected value for all of the foreign countries.
    //
    //Conversion routine "convertFipsCountryStringToFipsCountryCode" will need to be written when Foreign PO
    //vendors are implemented to deal with converting this mixed case string to a FIPS country code.
    public static final String FIPS_COUNTRY_CODE_CANADA = "CA";
    public static final String FIPS_COUNTRY_CODE_AUSTRALIA = "AS";
    public enum PaymentWorksPurchaseOrderCountryFipsOption {
        UNITED_STATES("US", "US"),
        CANADA("Canada", "CA"),
        AUSTRALIA("Australia", "AS"),
        OTHER("Other", "");

        public final String pmwCountryOptionAsString;
        public final String fipsCountryCode;

        private PaymentWorksPurchaseOrderCountryFipsOption(String pmwCountryOptionAsString, String fipsCountryCode) {
            this.pmwCountryOptionAsString = pmwCountryOptionAsString;
            this.fipsCountryCode = fipsCountryCode;
        }

        public String getPmwCountryOptionAsString() {
            return pmwCountryOptionAsString;
        }

        public String getFipsCountryCode() {
            return fipsCountryCode;
        }
    }

    public enum PaymentWorksMethodOfPoTransmission {
        EMAIL("Email", CUPurapConstants.POTransmissionMethods.EMAIL),
        FAX("FAX", CUPurapConstants.POTransmissionMethods.FAX),
        US_MAIL("US Mail", CUPurapConstants.POTransmissionMethods.MANUAL);

        public final String pmwPoTransmissionMethodAsText;
        public final String kfsPoTransmissionMethodCode;

        private PaymentWorksMethodOfPoTransmission(String pmwPoTransmissionMethodAsText, String kfsPoTransmissionMethodCode) {
            this.pmwPoTransmissionMethodAsText = pmwPoTransmissionMethodAsText;
            this.kfsPoTransmissionMethodCode = kfsPoTransmissionMethodCode;
        }

        public String getPmwPoTransmissionMethodAsText() {
            return pmwPoTransmissionMethodAsText;
        }

        public String getKfsPoTransmissionMethodCode() {
            return kfsPoTransmissionMethodCode;
        }
    }

}
