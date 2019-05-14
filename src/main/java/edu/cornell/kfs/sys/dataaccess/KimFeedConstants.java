package edu.cornell.kfs.sys.dataaccess;

/*
 * TODO: Refactor this class accordingly to conform to our current coding standards!
 */
public class KimFeedConstants {

    // Helper constant for inserting blank address rows.
    public static final String BLANK_ADDRESS_LINE = " ";

    // Miscellaneous helper constants.
    public static final String YES_VAL = "Y";
    public static final String NO_VAL = "N";
    public static final String IT_PREFIX = "IT-";
    public static final String SINGLE_SPACE = " ";
    public static final String INACTIVE_VAL = "I";
    public static final String DELTA_LOAD_DATE_PROPERTY_FORMAT = "MM/dd/yyyy";
    public static final String LINE_INFO_MESSAGE_FORMAT = "%s. NetID: %s, CU_PERSON ID: %s, Employee ID: %s";

    // Constants related to the retrieved rows.
    public static final String CU_PERSON_SID_ROW = "CU_PERSON_SID";
    public static final String NETID_ROW = "NETID";
    public static final String EMPLID_ROW = "EMPLID";
    public static final String FIRST_NAME_ROW = "FIRST_NAME";
    public static final String LAST_NAME_ROW = "LAST_NAME";
    public static final String MIDDLE_NAME_ROW = "MIDDLE_NAME";
    public static final String NAME_SUFFIX_ROW = "NAME_SUFFIX";
    public static final String HOME_ADDRESS1_ROW = "HOME_ADDRESS1";
    public static final String HOME_ADDRESS2_ROW = "HOME_ADDRESS2";
    public static final String HOME_ADDRESS3_ROW = "HOME_ADDRESS3";
    public static final String HOME_CITY_ROW = "HOME_CITY";
    public static final String HOME_STATE_ROW = "HOME_STATE";
    public static final String HOME_POSTAL_ROW = "HOME_POSTAL";
    public static final String CAMPUS_ADDRESS_ROW = "CAMPUS_ADDRESS";
    public static final String CAMPUS_CITY_ROW = "CAMPUS_CITY";
    public static final String CAMPUS_STATE_ROW = "CAMPUS_STATE";
    public static final String CAMPUS_POSTAL_ROW = "CAMPUS_POSTAL";
    public static final String EMAIL_ADDRESS_ROW = "EMAIL_ADDRESS";
    public static final String CAMPUS_PHONE_ROW = "CAMPUS_PHONE";
    public static final String ACADEMIC_ROW = "ACADEMIC";
    public static final String FACULTY_ROW = "FACULTY";
    public static final String AFFILIATE_ROW = "AFFILIATE";
    public static final String DCEXP1_ROW = "DCEXP1";
    public static final String STAFF_ROW = "STAFF";
    public static final String STUDENT_ROW = "STUDENT";
    public static final String ALUMNI_ROW = "ALUMNI";
    public static final String PRIMARY_ORG_CODE_ROW = "PRIMARY_ORG_CODE";
    public static final String PRIMARY_AFFILIATION_ROW = "PRIMARY_AFFILIATION";
    public static final String LDAP_SUPPRESS_ROW = "LDAP_SUPPRESS";

    public static final String AFLTN_TYP_CD_ROW = "AFLTN_TYP_CD";

    // Constants related to the various possible affiliation employment statuses (A - Active, I - Inactive, R - Retired, N - Never had it).
    public static final char IS_ACTIVE_AFFIL = 'A';
    public static final char IS_INACTIVE_AFFIL = 'I';
    public static final char IS_RETIRED_AFFIL = 'R';
    public static final char IS_NONEXISTENT_AFFIL = 'N';

    // A helper array denoting the priorities of the various affiliation emp info statuses; intended for resolving primary emp info disputes.
    public static final char[] EMP_STAT_CHARS = {IS_ACTIVE_AFFIL, IS_INACTIVE_AFFIL, IS_RETIRED_AFFIL};

    // Constants related to the affiliation names.
    public static final String ACADEMIC_AFFIL_CONST = "ACADEM";
    public static final String ALUMNI_AFFIL_CONST = "ALUMNI";
    public static final String FACULTY_AFFIL_CONST = "FCLTY";
    public static final String AFFILIATE_AFFIL_CONST = "AFLT";
    public static final String EXCEPTION_AFFIL_CONST = "EXCPTN";
    public static final String STAFF_AFFIL_CONST = "STAFF";
    public static final String STUDENT_AFFIL_CONST = "STDNT";

    // Constants related to frequently-used SELECT SQL.
    public static final String SELECT_ENTITY_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_ENTITY_T WHERE ENTITY_ID = ?";
    public static final String SELECT_MATCHING_PRINCIPAL_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_PRNCPL_T WHERE PRNCPL_NM = ? AND ENTITY_ID = ?";
    public static final String SELECT_CONFLICTING_PRINCIPAL_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_PRNCPL_T WHERE PRNCPL_NM = ? AND ENTITY_ID <> ?";
    public static final String SELECT_AFFIL_TYPE_SQL = "SELECT AFLTN_TYP_CD FROM KRIM_ENTITY_AFLTN_T WHERE ENTITY_ID = ?";
    public static final String SELECT_EMAIL_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_ENTITY_EMAIL_T WHERE ENTITY_ID = ?";

    // Constants related to frequently-used non-affiliation UPDATE SQL.
    public static final String UPDATE_PRINCIPAL_SQL = "UPDATE KRIM_PRNCPL_T SET PRNCPL_NM = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
    public static final String UPDATE_HOME_ADDRESS_SQL = "UPDATE KRIM_ENTITY_ADDR_T SET ADDR_LINE_1 = ?, ADDR_LINE_2 = ?," +
            "ADDR_LINE_3 = ?, CITY = ?, STATE_PVC_CD = SubStr(?,1,2), POSTAL_CD = ? WHERE ENTITY_ID = ? AND ADDR_TYP_CD = 'HM'";
    public static final String UPDATE_CAMPUS_ADDRESS_SQL = "UPDATE KRIM_ENTITY_ADDR_T SET ADDR_LINE_1 = ?, ADDR_LINE_2 = ?, ADDR_LINE_3 = ?, CITY = ?" +
            ", STATE_PVC_CD = SubStr(?,1,2), POSTAL_CD = ? WHERE ENTITY_ID = ? AND ADDR_TYP_CD = 'CMP'";
    public static final String UPDATE_CAMPUS_PHONE_SQL =
            "UPDATE KRIM_ENTITY_PHONE_T SET PHONE_NBR = SubStr(?,1,20), LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ? AND PHONE_TYP_CD = 'CMP'";
    public static final String UPDATE_EMPLID_SQL = "UPDATE KRIM_ENTITY_EXT_ID_T SET EXT_ID = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
    public static final String UPDATE_NAME_SQL = "UPDATE KRIM_ENTITY_NM_T SET FIRST_NM = ?, MIDDLE_NM = ?, LAST_NM = ?, SUFFIX_NM = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
    public static final String UPDATE_EMAIL_SQL = "UPDATE KRIM_ENTITY_EMAIL_T SET EMAIL_ADDR = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
    public static final String UPDATE_PRIV_PREF_SQL = "UPDATE KRIM_ENTITY_PRIV_PREF_T SET SUPPRESS_NM_IND = 'Y', SUPPRESS_EMAIL_IND = 'Y', SUPPRESS_ADDR_IND = 'Y'," +
            "SUPPRESS_PHONE_IND = 'Y', SUPPRESS_PRSNL_IND = 'Y', LAST_UPDT_DT = sysdate WHERE ENTITY_ID = ?";



    // Constants related to frequently-used non-affiliation INSERT SQL.

    public static final String INSERT_ENTITY_SQL = "INSERT INTO KRIM_ENTITY_T (ENTITY_ID, OBJ_ID, VER_NBR, ACTV_IND,LAST_UPDT_DT) VALUES (?, SYS_GUID(), 1, ?, SYSDATE)";

    public static final String INSERT_PRINCIPAL_SQL = "Insert Into KRIM_PRNCPL_T (PRNCPL_ID, OBJ_ID, VER_NBR, PRNCPL_NM, ENTITY_ID, " +
            "PRNCPL_PSWD, ACTV_IND, LAST_UPDT_DT) Values (?, SYS_GUID(), 1, ?, ?, null, ?, SYSDATE)";

    public static final String INSERT_ENTITY_TYPE_SQL = "Insert Into KRIM_ENTITY_ENT_TYP_T (ENT_TYP_CD, ENTITY_ID, OBJ_ID, VER_NBR, ACTV_IND, LAST_UPDT_DT)" +
            " Values ('PERSON', ?, SYS_GUID(), 1, ?, SYSDATE)";

    public static final String INSERT_HOME_ADDRESS_SQL = "Insert Into KRIM_ENTITY_ADDR_T ( ENTITY_ADDR_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, " +
            "ADDR_TYP_CD, ADDR_LINE_1, ADDR_LINE_2, ADDR_LINE_3, CITY, STATE_PVC_CD, POSTAL_CD, " +
            "POSTAL_CNTRY_CD, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values ( To_Char(KRIM_ENTITY_ADDR_ID_S.NEXTVAL)," +
            "SYS_GUID(), 1, ?, 'PERSON', 'HM', ?, ?, ?, ?, SubStr(?,1,2), ?, ' ', 'Y', ?, SYSDATE)";

    public static final String INSERT_CAMPUS_ADDRESS_SQL = "Insert Into KRIM_ENTITY_ADDR_T (ENTITY_ADDR_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, ADDR_TYP_CD, " +
            "ADDR_LINE_1, ADDR_LINE_2, ADDR_LINE_3, CITY, STATE_PVC_CD, POSTAL_CD, POSTAL_CNTRY_CD, DFLT_IND, " +
            "ACTV_IND, LAST_UPDT_DT) Values (To_Char( KRIM_ENTITY_ADDR_ID_S.NEXTVAL), SYS_GUID(), 1, ?, " +
            "'PERSON', 'CMP', ?, ?, ?, ?, SubStr(?,1,2), ?, ' ', 'N', ?, SYSDATE)";

    public static final String INSERT_EMAIL_ADDRESS_SQL = "Insert Into KRIM_ENTITY_EMAIL_T (ENTITY_EMAIL_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, EMAIL_TYP_CD, " +
            "EMAIL_ADDR, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char(KRIM_ENTITY_EMAIL_ID_S.NEXTVAL), SYS_GUID(), " +
            "1, ?, 'PERSON', 'WRK', ?, 'Y', ?, SYSDATE)";

    public static final String INSERT_CAMPUS_PHONE_SQL = "Insert Into KRIM_ENTITY_PHONE_T (ENTITY_PHONE_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, PHONE_TYP_CD, PHONE_NBR, " +
            "PHONE_EXTN_NBR, POSTAL_CNTRY_CD, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char(KRIM_ENTITY_PHONE_ID_S.NEXTVAL), " +
            "SYS_GUID(), 1, ?, 'PERSON', 'CMP', SubStr(?,1,20), ' ', ' ', 'Y', ?, SYSDATE)";

    public static final String INSERT_PRIV_PREF_ON_SQL = "Insert Into KRIM_ENTITY_PRIV_PREF_T (ENTITY_ID, OBJ_ID, VER_NBR, SUPPRESS_NM_IND, SUPPRESS_EMAIL_IND, SUPPRESS_ADDR_IND," +
            "SUPPRESS_PHONE_IND, SUPPRESS_PRSNL_IND, LAST_UPDT_DT) Values (?, SYS_GUID(), 1, 'Y', 'Y', 'Y', 'Y', 'Y', SYSDATE)";

    public static final String INSERT_PRIV_PREF_OFF_SQL = "Insert Into KRIM_ENTITY_PRIV_PREF_T (ENTITY_ID, OBJ_ID, VER_NBR, SUPPRESS_NM_IND, SUPPRESS_EMAIL_IND, SUPPRESS_ADDR_IND," +
            "SUPPRESS_PHONE_IND, SUPPRESS_PRSNL_IND, LAST_UPDT_DT) Values (?, SYS_GUID(), 1, 'N', 'N', 'Y', 'N', 'N', SYSDATE)";

    public static final String INSERT_EMPLID_SQL = "Insert Into KRIM_ENTITY_EXT_ID_T (ENTITY_EXT_ID_ID, OBJ_ID, VER_NBR, ENTITY_ID, EXT_ID_TYP_CD, EXT_ID, LAST_UPDT_DT) " +
            "Values (To_Char(KRIM_ENTITY_EXT_ID_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'EMPLID', ?, SYSDATE)";

    public static final String INSERT_TAXID_SQL = "Insert Into KRIM_ENTITY_EXT_ID_T (ENTITY_EXT_ID_ID, OBJ_ID, VER_NBR, ENTITY_ID, EXT_ID_TYP_CD, EXT_ID, LAST_UPDT_DT) " +
            "Values (To_Char(KRIM_ENTITY_EXT_ID_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'TAX', 'kvcHXZFmZ0zDIqZUo3sGug==', SYSDATE)";

    public static final String INSERT_NAME_SQL = "Insert Into KRIM_ENTITY_NM_T (ENTITY_NM_ID, OBJ_ID, VER_NBR, ENTITY_ID, NM_TYP_CD, FIRST_NM, MIDDLE_NM, LAST_NM, SUFFIX_NM, " +
            "TITLE_NM, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char(KRIM_ENTITY_NM_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'PRFR', ?, ?, ?, ?, null, 'Y', ?, SYSDATE)";



    // Constants related to frequently-used affiliation and employee info SQL.
    public static final String INSERT_AFFIL_SQL = "Insert Into KRIM_ENTITY_AFLTN_T (ENTITY_AFLTN_ID, OBJ_ID, VER_NBR, ENTITY_ID, AFLTN_TYP_CD, CAMPUS_CD," +
            " DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char(KRIM_ENTITY_AFLTN_ID_S.NEXTVAL), SYS_GUID(), 1, ?, ?, 'IT', ?, 'Y', SYSDATE)";
    public static final String INSERT_EMP_INFO_SQL = "Insert Into KRIM_ENTITY_EMP_INFO_T (ENTITY_EMP_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENTITY_AFLTN_ID," +
            " EMP_STAT_CD, EMP_TYP_CD, BASE_SLRY_AMT, PRMRY_IND, ACTV_IND, LAST_UPDT_DT, PRMRY_DEPT_CD, EMP_ID, EMP_REC_ID) Values (To_Char(KRIM_ENTITY_EMP_ID_S.NEXTVAL)," +
                    " SYS_GUID(), 1, ?, To_Char(KRIM_ENTITY_AFLTN_ID_S.CURRVAL), ?, 'P', 5, ?, 'Y', SYSDATE, ?, ?, ?)";
    public static final String UPDATE_AFFIL_SQL = "update KRIM_ENTITY_AFLTN_T SET ACTV_IND = 'Y', DFLT_IND = ? WHERE ENTITY_ID = ? AND AFLTN_TYP_CD = ?";
    public static final String UPDATE_EMP_INFO_SQL = "update KRIM_ENTITY_EMP_INFO_T SET ACTV_IND = 'Y', EMP_STAT_CD = ?, PRMRY_IND = ?, EMP_REC_ID = ?" +
            " WHERE ENTITY_AFLTN_ID = (SELECT ENTITY_AFLTN_ID FROM KRIM_ENTITY_AFLTN_T WHERE ENTITY_ID = ? AND AFLTN_TYP_CD = ?)";
    public static final String DELETE_EMP_INFO_SQL = "DELETE FROM KRIM_ENTITY_EMP_INFO_T WHERE ENTITY_AFLTN_ID = " +
            "(SELECT ENTITY_AFLTN_ID FROM KRIM_ENTITY_AFLTN_T WHERE ENTITY_ID = ? AND AFLTN_TYP_CD = ?)";
    public static final String UPDATE_EMP_INFO_CD_AND_ID_SQL = "UPDATE KRIM_ENTITY_EMP_INFO_T SET PRMRY_DEPT_CD = ?, EMP_ID = ? WHERE ENTITY_ID = ?";
    
    public static final String DATABASE_TEST_SQL = "SELECT 1 FROM DUAL";

    // Constants related to reading in config data for this utility from a .properties file, and for accessing the info for the server DB.
    public static final String SERVER_DB_URL_PROP = "db-url0";
    public static final String SERVER_DB_USERNAME_PROP = "db-username0";
    public static final String SERVER_DB_PASSWORD_PROP = "db-password0";

    public static final String DEST_DB_URL_PROP = "db-url1";
    public static final String DEST_DB_USERNAME_PROP = "db-username1";
    public static final String DEST_DB_PASSWORD_PROP = "db-password1";

    public static final String SKIP_DELTA_FLAG_UPDATES_PROP = "skip-delta-flag-updates";
    public static final String LOAD_LATEST_DELTA_ONLY_PROP = "load-latest-delta-only";
    public static final String LOAD_DELTA_WITH_DATE_PROP = "load-delta-with-date";

    public static final String LOGGING_CONFIG_FILE_ARG = "loggingConfigFile";

    // Constants related to setting up the datasources.
    public static final String DB_DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
    public static final String DB_VALIDATION_SQL = "select 1 from dual";

    public static final String READ_ALL_UNPROCESSED_ROWS = "READ_ALL_UNPROCESSED_ROWS";
    public static final String READ_ROWS_FOR_LATEST_DELTA_ONLY = "READ_ROWS_FOR_LATEST_DELTA_ONLY";
}
