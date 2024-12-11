package edu.cornell.kfs.tax.batch;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public final class TaxColumns {

    public static final class TableAliases {
        public static final String VENDOR_HEADER = "HDR";
        public static final String VENDOR_DETAIL = "DTL";
    }

    public enum TransactionDetailColumn {
        IRS_1099_1042S_DETAIL_ID,
        REPORT_YEAR,
        FDOC_NBR,
        DOC_TYPE,
        FDOC_LINE_NBR,
        FIN_OBJECT_CD,
        NET_PMT_AMT,
        DOC_TITLE,
        VENDOR_TAX_NBR,
        INCOME_CODE,
        INCOME_CODE_SUB_TYPE,
        DV_CHK_STUB_TXT,
        PAYEE_ID,
        VENDOR_NAME,
        PARENT_VENDOR_NAME,
        VNDR_TYP_CD,
        VNDR_OWNR_CD,
        VNDR_OWNR_CTGRY_CD,
        VNDR_FRGN_IND,
        VNDR_EMAIL_ADDR,
        VNDR_CHAP_4_STAT_CD,
        VNDR_GIIN,
        VNDR_LN1_ADDR,
        VNDR_LN2_ADDR,
        VNDR_CTY_NM,
        VNDR_ST_CD,
        VNDR_ZIP_CD,
        VNDR_FRGN_LN1_ADDR,
        VNDR_FRGN_LN2_ADDR,
        VNDR_FRGN_CTY_NM,
        VNDR_FRGN_ZIP_CD,
        VNDR_FRGN_PROV_NM,
        VNDR_FRGN_CNTRY_CD,
        NRA_PAYMENT_IND,
        PMT_DT,
        PMT_PAYEE_NM,
        INC_CLS_CD,
        INC_TAX_TRTY_EXMPT_IND,
        FRGN_SRC_INC_IND,
        FED_INC_TAX_PCT,
        PMT_DESC,
        PMT_LN1_ADDR,
        PMT_CNTRY_NM,
        KFS_CHART,
        KFS_ACCOUNT,
        INITIATOR_NETID,
        FORM_1099_TYPE,
        FORM_1099_BOX,
        FORM_1099_OVERRIDDEN_TYPE,
        FORM_1099_OVERRIDDEN_BOX,
        FORM_1042S_BOX,
        FORM_1042S_OVERRIDDEN_BOX,
        PMT_REASON_CD,
        DISB_NBR,
        PMT_STAT_CD,
        DISB_TYP_CD,
        LEDGER_DOC_TYP_CD;
    }

    public enum VendorDetailColumn {
        VNDR_HDR_GNRTD_ID(TableAliases.VENDOR_DETAIL),
        VNDR_DTL_ASND_ID(TableAliases.VENDOR_DETAIL),
        VNDR_PARENT_IND(TableAliases.VENDOR_DETAIL),
        VNDR_1ST_LST_NM_IND(TableAliases.VENDOR_DETAIL),
        VNDR_NM(TableAliases.VENDOR_DETAIL),
        VNDR_US_TAX_NBR(TableAliases.VENDOR_HEADER),
        VNDR_TYP_CD(TableAliases.VENDOR_HEADER),
        VNDR_OWNR_CD(TableAliases.VENDOR_HEADER),
        VNDR_OWNR_CTGRY_CD(TableAliases.VENDOR_HEADER),
        VNDR_FRGN_IND(TableAliases.VENDOR_HEADER),
        VNDR_GIIN(TableAliases.VENDOR_HEADER),
        VNDR_CHAP_4_STAT_CD(TableAliases.VENDOR_HEADER);

        private final String tableAlias;

        private VendorDetailColumn(final String tableAlias) {
            this.tableAlias = tableAlias;
        }

        @Override
        public String toString() {
            return StringUtils.join(tableAlias, KFSConstants.DELIMITER, name());
        }
    }

    public enum VendorAddressColumn {
        VNDR_ADDR_GNRTD_ID,
        VNDR_HDR_GNRTD_ID,
        VNDR_DTL_ASND_ID,
        VNDR_ADDR_TYP_CD,
        VNDR_LN1_ADDR,
        VNDR_LN2_ADDR,
        VNDR_CTY_NM,
        VNDR_ST_CD,
        VNDR_ZIP_CD,
        VNDR_CNTRY_CD,
        VNDR_ATTN_NM,
        VNDR_ADDR_INTL_PROV_NM,
        VNDR_ADDR_EMAIL_ADDR,
        DOBJ_MAINT_CD_ACTV_IND;
    }

    public enum NoteColumn {
        NTE_ID,
        RMT_OBJ_ID,
        TXT;
    }

}
