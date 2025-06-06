package edu.cornell.kfs.tax.batch.util;

import edu.cornell.kfs.sys.dataaccess.TestDataHelperDao;
import edu.cornell.kfs.sys.util.CuSqlQuery;

public final class TestTaxSqlUtils {

    public static final class TableNames {
        public static final String TX_TRANSACTION_DETAIL_T = "KFS.TX_TRANSACTION_DETAIL_T";
        public static final String PUR_VNDR_HDR_T = "KFS.PUR_VNDR_HDR_T";
        public static final String PUR_VNDR_DTL_T = "KFS.PUR_VNDR_DTL_T";
        public static final String PUR_VNDR_ADDR_T = "KFS.PUR_VNDR_ADDR_T";
        public static final String KRNS_NTE_T = "KFS.KRNS_NTE_T";
        public static final String FS_DOC_HEADER_T = "KFS.FS_DOC_HEADER_T";
    }

    public static final class ColumnNames {
        public static final String VENDOR_TAX_NBR = "VENDOR_TAX_NBR";
        public static final String VNDR_US_TAX_NBR  = "VNDR_US_TAX_NBR";
    }

    public static void createTransactionDetailTable(final TestDataHelperDao testDataHelperDao) {
        final CuSqlQuery tableCreationQuery = CuSqlQuery.of(
                "CREATE TEXT TABLE ", TableNames.TX_TRANSACTION_DETAIL_T, " (",
                        "IRS_1099_1042S_DETAIL_ID VARCHAR2(40 BYTE) NOT NULL, ",
                        "REPORT_YEAR NUMBER(4, 0), ",
                        "FDOC_NBR VARCHAR2(14 BYTE), ",
                        "DOC_TYPE VARCHAR2(4 BYTE), ",
                        "FDOC_LINE_NBR NUMBER(8, 0), ",
                        "FIN_OBJECT_CD VARCHAR2(4 BYTE), ",
                        "NET_PMT_AMT NUMBER(19, 2), ",
                        "DOC_TITLE VARCHAR2(255 BYTE), ",
                        "VENDOR_TAX_NBR VARCHAR2(255 BYTE), ",
                        "INCOME_CODE VARCHAR2(2 BYTE), ",
                        "INCOME_CODE_SUB_TYPE VARCHAR2(1 BYTE), ",
                        "DV_CHK_STUB_TXT VARCHAR2(1400 BYTE), ",
                        "PAYEE_ID VARCHAR2(25 BYTE), ",
                        "VENDOR_NAME VARCHAR2(45 BYTE), ",
                        "PARENT_VENDOR_NAME VARCHAR2(45 BYTE), ",
                        "VNDR_TYP_CD VARCHAR2(4 BYTE), ",
                        "VNDR_OWNR_CD VARCHAR2(4 BYTE), ",
                        "VNDR_OWNR_CTGRY_CD VARCHAR2(4 BYTE), ",
                        "VNDR_FRGN_IND VARCHAR2(1 BYTE), ",
                        "VNDR_EMAIL_ADDR VARCHAR2(90 BYTE), ",
                        "VNDR_CHAP_4_STAT_CD VARCHAR2(2 BYTE), ",
                        "VNDR_GIIN VARCHAR2(19 BYTE), ",
                        "VNDR_LN1_ADDR VARCHAR2(45 BYTE), ",
                        "VNDR_LN2_ADDR VARCHAR2(45 BYTE), ",
                        "VNDR_CTY_NM VARCHAR2(45 BYTE), ",
                        "VNDR_ST_CD VARCHAR2(2 BYTE), ",
                        "VNDR_ZIP_CD VARCHAR2(20 BYTE), ",
                        "VNDR_FRGN_LN1_ADDR VARCHAR2(45 BYTE), ",
                        "VNDR_FRGN_LN2_ADDR VARCHAR2(45 BYTE), ",
                        "VNDR_FRGN_CTY_NM VARCHAR2(45 BYTE), ",
                        "VNDR_FRGN_ZIP_CD VARCHAR2(20 BYTE), ",
                        "VNDR_FRGN_PROV_NM VARCHAR2(45 BYTE), ",
                        "VNDR_FRGN_CNTRY_CD VARCHAR2(2 BYTE), ",
                        "NRA_PAYMENT_IND VARCHAR2(1 BYTE), ",
                        "PMT_DT DATE, ",
                        "PMT_PAYEE_NM VARCHAR2(123 BYTE), ",
                        "INC_CLS_CD VARCHAR2(2 BYTE), ",
                        "INC_TAX_TRTY_EXMPT_IND CHAR(1 BYTE), ",
                        "FRGN_SRC_INC_IND CHAR(1 BYTE), ",
                        "FED_INC_TAX_PCT NUMBER(5,2), ",
                        "PMT_DESC VARCHAR2(100 BYTE), ",
                        "PMT_LN1_ADDR VARCHAR2(55 BYTE), ",
                        "PMT_CNTRY_NM VARCHAR2(30 BYTE), ",
                        "KFS_CHART VARCHAR2(2 BYTE), ",
                        "KFS_ACCOUNT VARCHAR2(7 BYTE), ",
                        "INITIATOR_NETID VARCHAR2(100 BYTE), ",
                        "FORM_1099_TYPE VARCHAR2(10 BYTE), ",
                        "FORM_1099_BOX VARCHAR2(3 BYTE), ",
                        "FORM_1099_OVERRIDDEN_TYPE VARCHAR2(10 BYTE), ",
                        "FORM_1099_OVERRIDDEN_BOX VARCHAR2(3 BYTE), ",
                        "FORM_1042S_BOX VARCHAR2(10 BYTE), ",
                        "FORM_1042S_OVERRIDDEN_BOX VARCHAR2(10 BYTE), ",
                        "PMT_REASON_CD VARCHAR2(1 BYTE), ",
                        "DISB_NBR NUMBER(9, 0), ",
                        "PMT_STAT_CD VARCHAR2(4 BYTE), ",
                        "DISB_TYP_CD VARCHAR2(4 BYTE), ",
                        "LEDGER_DOC_TYP_CD VARCHAR2(4 BYTE)",
                ")"
        );

            final CuSqlQuery primaryKeyQuery = CuSqlQuery.of("ALTER TABLE ", TableNames.TX_TRANSACTION_DETAIL_T,
                    " ADD CONSTRAINT TX_TRANSACTION_DETAIL_TP1 PRIMARY KEY (IRS_1099_1042S_DETAIL_ID)");

        testDataHelperDao.execute(tableCreationQuery);
        testDataHelperDao.execute(primaryKeyQuery);
    }

    public static void createAbridgedVendorHeaderTable(final TestDataHelperDao testDataHelperDao) {
        final CuSqlQuery tableCreationQuery = CuSqlQuery.of(
                "CREATE TEXT TABLE ", TableNames.PUR_VNDR_HDR_T, " (",
                        "VNDR_HDR_GNRTD_ID NUMBER(10,0) NOT NULL, ",
                        "VNDR_TYP_CD VARCHAR2(4 BYTE) NOT NULL, ",
                        "VNDR_US_TAX_NBR VARCHAR2(255 BYTE), ",
                        "VNDR_OWNR_CD VARCHAR2(4 BYTE), ",
                        "VNDR_OWNR_CTGRY_CD VARCHAR2(4 BYTE), ",
                        "VNDR_FRGN_IND VARCHAR2(1 BYTE), ",
                        "VNDR_GIIN VARCHAR2(19 BYTE), ",
                        "VNDR_CHAP_4_STAT_CD VARCHAR2(2 BYTE)",
                ")"
        );

        final CuSqlQuery primaryKeyQuery = CuSqlQuery.of("ALTER TABLE ", TableNames.PUR_VNDR_HDR_T,
                    " ADD CONSTRAINT PUR_VNDR_HDR_TP1 PRIMARY KEY (VNDR_HDR_GNRTD_ID)");

        testDataHelperDao.execute(tableCreationQuery);
        testDataHelperDao.execute(primaryKeyQuery);
    }

    public static void createAbridgedVendorDetailTable(final TestDataHelperDao testDataHelperDao) {
        final CuSqlQuery tableCreationQuery = CuSqlQuery.of(
                "CREATE TEXT TABLE ", TableNames.PUR_VNDR_DTL_T, " (",
                        "VNDR_HDR_GNRTD_ID NUMBER(10,0) NOT NULL, ",
                        "VNDR_DTL_ASND_ID NUMBER(10,0) NOT NULL, ",
                        "VNDR_PARENT_IND VARCHAR2(1 BYTE), ",
                        "VNDR_NM VARCHAR2(45 BYTE) NOT NULL, ",
                        "VNDR_1ST_LST_NM_IND VARCHAR2(1 BYTE)",
                ")"
        );

        final CuSqlQuery primaryKeyQuery = CuSqlQuery.of("ALTER TABLE ", TableNames.PUR_VNDR_DTL_T,
                " ADD CONSTRAINT PUR_VNDR_DTL_TP1 PRIMARY KEY (VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID)");

        testDataHelperDao.execute(tableCreationQuery);
        testDataHelperDao.execute(primaryKeyQuery);
    }

    public static void createAbridgedVendorAddressTable(final TestDataHelperDao testDataHelperDao) {
        final CuSqlQuery tableCreationQuery = CuSqlQuery.of(
                "CREATE TEXT TABLE ", TableNames.PUR_VNDR_ADDR_T, " (",
                        "VNDR_ADDR_GNRTD_ID NUMBER(10,0) NOT NULL, ",
                        "VNDR_HDR_GNRTD_ID NUMBER(10,0) NOT NULL, ",
                        "VNDR_DTL_ASND_ID NUMBER(10,0) NOT NULL, ",
                        "VNDR_ADDR_TYP_CD VARCHAR2(4 BYTE), ",
                        "VNDR_LN1_ADDR VARCHAR2(45 BYTE), ",
                        "VNDR_LN2_ADDR VARCHAR2(45 BYTE), ",
                        "VNDR_CTY_NM VARCHAR2(45 BYTE), ",
                        "VNDR_ST_CD VARCHAR2(2 BYTE), ",
                        "VNDR_ZIP_CD VARCHAR2(20 BYTE), ",
                        "VNDR_CNTRY_CD VARCHAR2(2 BYTE), ",
                        "VNDR_ATTN_NM VARCHAR2(45 BYTE), ",
                        "VNDR_ADDR_INTL_PROV_NM VARCHAR2(45 BYTE), ",
                        "VNDR_ADDR_EMAIL_ADDR VARCHAR2(90 BYTE), ",
                        "DOBJ_MAINT_CD_ACTV_IND VARCHAR2(1 BYTE)",
                ")"
        );

        final CuSqlQuery primaryKeyQuery = CuSqlQuery.of("ALTER TABLE ", TableNames.PUR_VNDR_ADDR_T,
                " ADD CONSTRAINT PUR_VNDR_ADDR_TP1 PRIMARY KEY (VNDR_ADDR_GNRTD_ID)");

        testDataHelperDao.execute(tableCreationQuery);
        testDataHelperDao.execute(primaryKeyQuery);
    }

    public static void createAbridgedNoteTable(final TestDataHelperDao testDataHelperDao) {
        final CuSqlQuery tableCreationQuery = CuSqlQuery.of(
                "CREATE TEXT TABLE ", TableNames.KRNS_NTE_T, " (",
                        "NTE_ID NUMBER(14,0) NOT NULL, ",
                        "RMT_OBJ_ID VARCHAR2(36 BYTE) NOT NULL, ",
                        "TXT VARCHAR2(800 BYTE)",
                ")"
        );

        final CuSqlQuery primaryKeyQuery = CuSqlQuery.of("ALTER TABLE ", TableNames.KRNS_NTE_T,
                " ADD CONSTRAINT KRNS_NTE_TP1 PRIMARY KEY (NTE_ID)");

        testDataHelperDao.execute(tableCreationQuery);
        testDataHelperDao.execute(primaryKeyQuery);
    }

    public static void createAbridgedDocumentHeaderTableForNoteLinkingOnly(final TestDataHelperDao testDataHelperDao) {
        final CuSqlQuery tableCreationQuery = CuSqlQuery.of(
                "CREATE TEXT TABLE ", TableNames.FS_DOC_HEADER_T, " (",
                        "FDOC_NBR VARCHAR2(14 BYTE) NOT NULL, ",
                        "OBJ_ID VARCHAR2(36 BYTE) NOT NULL",
                ")"
        );

        final CuSqlQuery primaryKeyQuery = CuSqlQuery.of("ALTER TABLE ", TableNames.FS_DOC_HEADER_T,
                " ADD CONSTRAINT FS_DOC_HEADER_TP1 PRIMARY KEY (FDOC_NBR)");

        testDataHelperDao.execute(tableCreationQuery);
        testDataHelperDao.execute(primaryKeyQuery);
    }

}
