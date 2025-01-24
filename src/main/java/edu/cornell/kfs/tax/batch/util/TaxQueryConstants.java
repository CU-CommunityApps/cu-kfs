package edu.cornell.kfs.tax.batch.util;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.QueryTableAliases;

public final class TaxQueryConstants {

    public enum SortOrder {
        ASC("ASC"),
        ASC_NULLS_FIRST("ASC NULLS FIRST"),
        ASC_NULLS_LAST("ASC NULLS LAST"),
        DESC("DESC"),
        DESC_NULLS_FIRST("DESC NULLS FIRST"),
        DESC_NULLS_LAST("DESC NULLS LAST");

        private final String sqlChunk;

        private SortOrder(final String sqlChunk) {
            this.sqlChunk = sqlChunk;
        }

        @Override
        public String toString() {
            return sqlChunk;
        }
    }



    public static final class TransactionDetailProps {
        public static final String REPORT_YEAR = "reportYear";
        public static final String FORM_1042S_BOX = "form1042SBox";
        public static final String VENDOR_TAX_NUMBER = "vendorTaxNumber";
        public static final String INCOME_CODE = "incomeCode";
        public static final String INCOME_CODE_SUB_TYPE = "incomeCodeSubType";
    }

    public static final class VendorHeaderProps {
        public static final String PREFIX = QueryTableAliases.VENDOR_HEADER + KFSConstants.DELIMITER;
        public static final String VENDOR_HEADER_GENERATED_ID =
                PREFIX + KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID;
    }

    public static final class VendorDetailProps {
        public static final String PREFIX = QueryTableAliases.VENDOR_DETAIL + KFSConstants.DELIMITER;
        public static final String VENDOR_HEADER_GENERATED_ID =
                PREFIX + KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID;
        public static final String VENDOR_DETAIL_ASSIGNED_ID = PREFIX + KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID;
        public static final String VENDOR_PARENT_INDICATOR = PREFIX + VendorPropertyConstants.VENDOR_PARENT_INDICATOR;
    }

    public static final class VendorAddressProps {
        public static final String VENDOR_ADDRESS_GENERATED_ID = KFSPropertyConstants.VENDOR_ADDRESS_GENERATED_ID;
        public static final String VENDOR_HEADER_GENERATED_ID = KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID;
        public static final String VENDOR_DETAIL_ASSIGNED_ID = KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID;
        public static final String VENDOR_ADDRESS_COUNTRY = VendorPropertyConstants.VENDOR_ADDRESS_COUNTRY;
        public static final String VENDOR_ADDRESS_TYPE_CODE = VendorPropertyConstants.VENDOR_ADDRESS_TYPE_CODE;
        public static final String ACTIVE = KFSPropertyConstants.ACTIVE;
    }

    public static final class NoteProps {
        public static final String REMOTE_OBJECT_ID = "remoteObjectIdentifier";
    }

    public static final class DocumentHeaderProps {
        public static final String DOCUMENT_NUMBER = KFSPropertyConstants.DOCUMENT_NUMBER;
        public static final String OBJECT_ID = KFSPropertyConstants.OBJECT_ID;
    }

}
