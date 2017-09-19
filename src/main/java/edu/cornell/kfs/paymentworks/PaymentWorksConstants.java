/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.fp.CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes;

public class PaymentWorksConstants {

    public static final String PAYMENT_WORKS_FIELD_MAPPING_DOCUMENT_TYPE = "PMWM";
    public static final String PAYMENT_WORKS_VENDOR_SEQUENCE_NAME = "CU_PAYMENT_WORKS_VNDR_SEQ";
    public static final String PAYMENT_WORKS_FIELD_MAPPING_SEQUENCE_NAME = "CU_PAYMENT_WORKS_FLD_MAP_SEQ";
    public static final String SUPPLIER_FILE_NAME = "suppliers";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String QUESTION_MARK = "?";
    public static final String EQUALS_SIGN = "=";
    public static final String AMPERSAND = "&";
    public static final String FORWARD_SLASH = "/";
    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    public static final String TOKEN_HEADER_KEY = "Token ";
    public static final String PAYMENT_WORKS_NAMESPACE_CODE = "KFS-PMW";
    public static final String VENDOR_PAYMENT_TERMS_CODE_DEFAULT = "00N30";
    public static final String VENDOR_PHONE_TYPE_CODE_PHONE = "PH";
    public static final String SOURCE_USER = "PaymentWorks";
    public static final String VENDOR_REQUEST_ID_KFS_TEMP_ID_STARTER = "KFS";
    public static final String PAYEE_ACH_ACCOUNT_DEFAULT_TRANSACTION_TYPE = "PRAP";
    public static final String OUTPUT_ATTRIBUTE_BEGIN_DELIMITER = ":<";
    public static final String OUTPUT_ATTRIBUTE_END_DELIMITER = "> ";
    public static final String OUTPUT_RESTRICTED_DATA_PRESENT = "RestrictedDataPresent";

    public static class FieldNames {
        public static final String ROUTING_NUMBER = "Routing num";
        public static final String ACCOUNT_NUMBER = "Acct num";
        public static final String ACH_EMAIL = "ACH Email";
    }

    public static class PaymentWorksParameters {
        public static final String VENDOR_INITIATED_EMAIL_SUBJECT = "PAYMENT_WORKS_VENDOR_INITIATED_EMAIL_SUBJECT";
        public static final String VENDOR_INITIATED_EMAIL_FROM_ADDRESS = "PAYMENT_WORKS_VENDOR_INITIATED_EMAIL_FROM_ADDRESS";
        public static final String VENDOR_INITIATED_EMAIL_BODY = "PAYMENT_WORKS_VENDOR_INITIATED_EMAIL_BODY";
        public static final String VENDOR_APPROVED_EMAIL_SUBJECT = "PAYMENT_WORKS_VENDOR_APPROVED_EMAIL_SUBJECT";
        public static final String VENDOR_APPROVED_EMAIL_FROM_ADRESS = "PAYMENT_WORKS_VENDOR_APPROVED_EMAIL_FROM_ADDRESS";
        public static final String VENDOR_APPROVED_EMAIL_BODY = "PAYMENT_WORKS_VENDOR_APPROVED_EMAIL_BODY";
        public static final String SUPPLIER_UPLOAD_FILE_HEADER = "PAYMENT_WORKS_SUPPLIER_UPLOAD_FILE_HEADER";
    }

    public static class VendorUpdateGroups {
        public static final String COMPANY = "Company";
        public static final String CORP_ADDRESS = "Corporate Address";
        public static final String REMIT_ADDRESS = "Remittance Address";
    }

    public static class PaymentWorksURLParameters {
        public static final String STATUS = "status";
        public static final String GROUP_NAME = "group_name";
    }

    public static class PaymentWorksURLGroupNameOptions {
        public static final String BANK_ACCOUNT = "bank_account";
        public static final String COMPANY = VendorUpdateGroups.COMPANY;
        public static final String CORPORATE_ADDRESS = "corporate_address";
        public static final String REMITTANCE_ADDRESS = "remittance_address";
    }

    public static class PaymentWorksNewVendorStatus {
        public static final String PENDING = "0";
        public static final String APPROVED = "1";
        public static final String PROCESSED = "2";
        public static final String REJECTED = "4";
    }

    public static class PaymentWorksUpdateStatus {
        public static final String PENDING = "0";
        public static final String PROCESSED = "3";
    }

    public static class PaymentWorksStatusText {
        public static final String PENDING = "Pending";
        public static final String APPROVED = "Approved";
        public static final String PROCESSED = "Processed";
        public static final String REJECTED = "Rejected";
    }

    public static class ProcessStatus {
        public static final String VENDOR_REQUESTED = "Vendor Requested";
        public static final String VENDOR_CREATED = "Vendor Created";
        public static final String VENDOR_REJECTED = "Vendor Rejected";
        public static final String VENDOR_APPROVED = "Vendor Approved";
        public static final String VENDOR_DISAPPROVED = "Vendor Disapproved";
        public static final String VENDOR_UPDATE_CREATED = "Vendor Update Created";
        public static final String VENDOR_UPDATE_COMPLETE = "Vendor Update Complete";
        public static final String VENDOR_UPDATE_REJECTED = "Vendor Update Rejected";
        public static final String ACH_UPDATE_REJECTED = "Ach Update Rejected";
        public static final String ACH_UPDATE_COMPLETE = "Ach Update Complete";
        public static final String SUPPLIER_UPLOADED = "Supplier Uploaded";
        public static final String SUPPLIER_UPLOAD_FAILED = "Supplier Upload Failed";
    }

    public static class TransactionType {
        public static final String NEW_VENDOR = "NV";
        public static final String VENDOR_UPDATE = "VU";
        public static final String ACH_UPDATE = "ACH";
    }

    public static class SupplierUploadSummaryTypes {
        public static final String KFS_NEW_VENDORS = "KFS_NEW_VENDORS";
        public static final String PAYMENT_WORKS_NEW_VENDORS = "PAYMENT_WORKS_NEW_VENDORS";
        public static final String DISAPPROVED_VENDORS = "DISAPPROVED_VENDORS";
        public static final String VENDOR_UPDATES = "VENDOR_UPDATES";
    }

    public static class PoTransmissionMethods {
        public static final String EMAIL = "Email";
        public static final String FAX = "FAX";
        public static final String US_MAIL = "US Mail";
    }

    public static class PaymentWorksFieldMappingDatabaseFieldNames {
        public static final String PAYMENT_WORKS_FIELD_LABEL = "PMW_FIELD_LABEL";
        public static final String KFS_FIELD_NAME = "KFS_FIELD";
    }

    public enum OwnershipTaxClassification {
        INDIVUDUAL_PROPRIETOR("ID", "0"),
        C_CORPORATION("CP", "1"),
        S_CORPORTATION("CP", "2"),
        PARTNERTSHIP("PA", "3"),
        TRUST_ESTATE("TE", "4"),
        LLC_TAXED_C_CORPORTATION("CP", "5"),
        LLC_TAXED_S_CORPORATION("CP", "6"),
        LLC_TAXED_PARTNERSHIP("PA", "7"),
        OTHER("OTHER", "8"),
        UNKNOWN("UN", StringUtils.EMPTY);

        public final String ownershipCode;
        public final String taxClassification;

        private OwnershipTaxClassification(String ownershipCode, String taxClassification) {
            this.ownershipCode = ownershipCode;
            this.taxClassification = taxClassification;
        }

        public static OwnershipTaxClassification fromTaxClassification(String taxClassification) {
            for (OwnershipTaxClassification oc : OwnershipTaxClassification.values()) {
                if (StringUtils.equals(taxClassification, oc.taxClassification)) {
                    return oc;
                }
            }
            return OwnershipTaxClassification.UNKNOWN;
        }
    }

    public enum TinType {
        SSN("0", "SSN"),
        FEIN("1", "FEIN"),
        ITIN("2", "ITIN");

        public final String tinTypeCode;
        public final String taxTypeCode;

        private TinType(String tinTypeCode, String taxTypeCode) {
            this.tinTypeCode = tinTypeCode;
            this.taxTypeCode = taxTypeCode;
        }

        public static TinType fromTinCode(String tinTypeCode) {
            for (TinType tinType : TinType.values()) {
                if (StringUtils.equals(tinTypeCode, tinType.tinTypeCode)) {
                    return tinType;
                }
            }
            throw new IllegalArgumentException("Invalid Tin Type Code provided: " + tinTypeCode);
        }
    }

}
