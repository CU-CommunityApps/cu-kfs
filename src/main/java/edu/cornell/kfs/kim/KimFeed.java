package edu.cornell.kfs.kim;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import util.SimpleCommandLineParser;

public class KimFeed {

    private static final Logger LOG = LogManager.getLogger(KimFeed.class);

	// Helper constant for inserting blank address rows.
	private static final String BLANK_ADDRESS_LINE = " ";

	// Miscellaneous helper constants.
	private static final String YES_VAL = "Y";
	private static final String NO_VAL = "N";
	private static final String IT_PREFIX = "IT-";
	private static final String SINGLE_SPACE = " ";
	private static final String INACTIVE_VAL = "I";
	private static final String DELTA_LOAD_DATE_PROPERTY_FORMAT = "MM/dd/yyyy";
	private static final DateTimeFormatter DATE_FORMATTER_FOR_PARSED_DELTA_LOAD_DATE = DateTimeFormatter.ofPattern(DELTA_LOAD_DATE_PROPERTY_FORMAT, Locale.US);
	private static final String LINE_INFO_MESSAGE_FORMAT = "%s. NetID: %s, CU_PERSON ID: %s, Employee ID: %s";

	// Constants related to the retrieved rows.
	private static final String CU_PERSON_SID_ROW = "CU_PERSON_SID";
	private static final String NETID_ROW = "NETID";
	private static final String EMPLID_ROW = "EMPLID";
	private static final String FIRST_NAME_ROW = "FIRST_NAME";
	private static final String LAST_NAME_ROW = "LAST_NAME";
	private static final String MIDDLE_NAME_ROW = "MIDDLE_NAME";
	private static final String NAME_SUFFIX_ROW = "NAME_SUFFIX";
	private static final String HOME_ADDRESS1_ROW = "HOME_ADDRESS1";
	private static final String HOME_ADDRESS2_ROW = "HOME_ADDRESS2";
	private static final String HOME_ADDRESS3_ROW = "HOME_ADDRESS3";
	private static final String HOME_CITY_ROW = "HOME_CITY";
	private static final String HOME_STATE_ROW = "HOME_STATE";
	private static final String HOME_POSTAL_ROW = "HOME_POSTAL";
	private static final String CAMPUS_ADDRESS_ROW = "CAMPUS_ADDRESS";
	private static final String CAMPUS_CITY_ROW = "CAMPUS_CITY";
	private static final String CAMPUS_STATE_ROW = "CAMPUS_STATE";
	private static final String CAMPUS_POSTAL_ROW = "CAMPUS_POSTAL";
	private static final String EMAIL_ADDRESS_ROW = "EMAIL_ADDRESS";
	private static final String CAMPUS_PHONE_ROW = "CAMPUS_PHONE";
	private static final String ACADEMIC_ROW = "ACADEMIC";
	private static final String FACULTY_ROW = "FACULTY";
	private static final String AFFILIATE_ROW = "AFFILIATE";
	private static final String DCEXP1_ROW = "DCEXP1";
	private static final String STAFF_ROW = "STAFF";
	private static final String STUDENT_ROW = "STUDENT";
	private static final String ALUMNI_ROW = "ALUMNI";
	private static final String PRIMARY_ORG_CODE_ROW = "PRIMARY_ORG_CODE";
	private static final String PRIMARY_AFFILIATION_ROW = "PRIMARY_AFFILIATION";
	private static final String LDAP_SUPPRESS_ROW = "LDAP_SUPPRESS";

	private static final String AFLTN_TYP_CD_ROW = "AFLTN_TYP_CD";

	// Constants related to the various possible affiliation employment statuses (A - Active, I - Inactive, R - Retired, N - Never had it).
	private static final char IS_ACTIVE_AFFIL = 'A';
	private static final char IS_INACTIVE_AFFIL = 'I';
	private static final char IS_RETIRED_AFFIL = 'R';
	private static final char IS_NONEXISTENT_AFFIL = 'N';

	// A helper array denoting the priorities of the various affiliation emp info statuses; intended for resolving primary emp info disputes.
	private static final char[] EMP_STAT_CHARS = {IS_ACTIVE_AFFIL, IS_INACTIVE_AFFIL, IS_RETIRED_AFFIL};

	// Constants related to the affiliation names.
	private static final String ACADEMIC_AFFIL_CONST = "ACADEM";
	private static final String ALUMNI_AFFIL_CONST = "ALUMNI";
	private static final String FACULTY_AFFIL_CONST = "FCLTY";
	private static final String AFFILIATE_AFFIL_CONST = "AFLT";
	private static final String EXCEPTION_AFFIL_CONST = "EXCPTN";
	private static final String STAFF_AFFIL_CONST = "STAFF";
	private static final String STUDENT_AFFIL_CONST = "STDNT";

	// Constants related to frequently-used SELECT SQL.
	private static final String SELECT_ENTITY_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_ENTITY_T WHERE ENTITY_ID = ?";
	private static final String SELECT_MATCHING_PRINCIPAL_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_PRNCPL_T WHERE PRNCPL_NM = ? AND ENTITY_ID = ?";
	private static final String SELECT_CONFLICTING_PRINCIPAL_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_PRNCPL_T WHERE PRNCPL_NM = ? AND ENTITY_ID <> ?";
	private static final String SELECT_AFFIL_TYPE_SQL = "SELECT AFLTN_TYP_CD FROM KRIM_ENTITY_AFLTN_T WHERE ENTITY_ID = ?";
	private static final String SELECT_EMAIL_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_ENTITY_EMAIL_T WHERE ENTITY_ID = ?";

	// Constants related to frequently-used non-affiliation UPDATE SQL.
	private static final String UPDATE_PRINCIPAL_SQL = "UPDATE KRIM_PRNCPL_T SET PRNCPL_NM = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
	private static final String UPDATE_HOME_ADDRESS_SQL = "UPDATE KRIM_ENTITY_ADDR_T SET ADDR_LINE_1 = ?, ADDR_LINE_2 = ?," +
			"ADDR_LINE_3 = ?, CITY = ?, STATE_PVC_CD = SubStr(?,1,2), POSTAL_CD = ? WHERE ENTITY_ID = ? AND ADDR_TYP_CD = 'HM'";
	private static final String UPDATE_CAMPUS_ADDRESS_SQL = "UPDATE KRIM_ENTITY_ADDR_T SET ADDR_LINE_1 = ?, ADDR_LINE_2 = ?, ADDR_LINE_3 = ?, CITY = ?" +
			", STATE_PVC_CD = SubStr(?,1,2), POSTAL_CD = ? WHERE ENTITY_ID = ? AND ADDR_TYP_CD = 'CMP'";
	private static final String UPDATE_CAMPUS_PHONE_SQL =
			"UPDATE KRIM_ENTITY_PHONE_T SET PHONE_NBR = SubStr(?,1,20), LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ? AND PHONE_TYP_CD = 'CMP'";
	private static final String UPDATE_EMPLID_SQL = "UPDATE KRIM_ENTITY_EXT_ID_T SET EXT_ID = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
	private static final String UPDATE_NAME_SQL = "UPDATE KRIM_ENTITY_NM_T SET FIRST_NM = ?, MIDDLE_NM = ?, LAST_NM = ?, SUFFIX_NM = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
	private static final String UPDATE_EMAIL_SQL = "UPDATE KRIM_ENTITY_EMAIL_T SET EMAIL_ADDR = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
	private static final String UPDATE_PRIV_PREF_SQL = "UPDATE KRIM_ENTITY_PRIV_PREF_T SET SUPPRESS_NM_IND = 'Y', SUPPRESS_EMAIL_IND = 'Y', SUPPRESS_ADDR_IND = 'Y'," +
			"SUPPRESS_PHONE_IND = 'Y', SUPPRESS_PRSNL_IND = 'Y', LAST_UPDT_DT = sysdate WHERE ENTITY_ID = ?";



	// Constants related to frequently-used non-affiliation INSERT SQL.

	private static final String INSERT_ENTITY_SQL = "INSERT INTO KRIM_ENTITY_T (ENTITY_ID, OBJ_ID, VER_NBR, ACTV_IND,LAST_UPDT_DT) VALUES (?, SYS_GUID(), 1, ?, SYSDATE)";

	private static final String INSERT_PRINCIPAL_SQL = "Insert Into KRIM_PRNCPL_T (PRNCPL_ID, OBJ_ID, VER_NBR, PRNCPL_NM, ENTITY_ID, " +
			"PRNCPL_PSWD, ACTV_IND, LAST_UPDT_DT) Values (?, SYS_GUID(), 1, ?, ?, null, ?, SYSDATE)";

	private static final String INSERT_ENTITY_TYPE_SQL = "Insert Into KRIM_ENTITY_ENT_TYP_T (ENT_TYP_CD, ENTITY_ID, OBJ_ID, VER_NBR, ACTV_IND, LAST_UPDT_DT)" +
			" Values ('PERSON', ?, SYS_GUID(), 1, ?, SYSDATE)";

	private static final String INSERT_HOME_ADDRESS_SQL = "Insert Into KRIM_ENTITY_ADDR_T ( ENTITY_ADDR_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, " +
			"ADDR_TYP_CD, ADDR_LINE_1, ADDR_LINE_2, ADDR_LINE_3, CITY, STATE_PVC_CD, POSTAL_CD, " +
			"POSTAL_CNTRY_CD, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values ( To_Char(KRIM_ENTITY_ADDR_ID_S.NEXTVAL)," +
			"SYS_GUID(), 1, ?, 'PERSON', 'HM', ?, ?, ?, ?, SubStr(?,1,2), ?, ' ', 'Y', ?, SYSDATE)";

	private static final String INSERT_CAMPUS_ADDRESS_SQL = "Insert Into KRIM_ENTITY_ADDR_T (ENTITY_ADDR_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, ADDR_TYP_CD, " +
			"ADDR_LINE_1, ADDR_LINE_2, ADDR_LINE_3, CITY, STATE_PVC_CD, POSTAL_CD, POSTAL_CNTRY_CD, DFLT_IND, " +
			"ACTV_IND, LAST_UPDT_DT) Values (To_Char( KRIM_ENTITY_ADDR_ID_S.NEXTVAL), SYS_GUID(), 1, ?, " +
			"'PERSON', 'CMP', ?, ?, ?, ?, SubStr(?,1,2), ?, ' ', 'N', ?, SYSDATE)";

	private static final String INSERT_EMAIL_ADDRESS_SQL = "Insert Into KRIM_ENTITY_EMAIL_T (ENTITY_EMAIL_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, EMAIL_TYP_CD, " +
			"EMAIL_ADDR, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char(KRIM_ENTITY_EMAIL_ID_S.NEXTVAL), SYS_GUID(), " +
			"1, ?, 'PERSON', 'WRK', ?, 'Y', ?, SYSDATE)";

	private static final String INSERT_CAMPUS_PHONE_SQL = "Insert Into KRIM_ENTITY_PHONE_T (ENTITY_PHONE_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, PHONE_TYP_CD, PHONE_NBR, " +
			"PHONE_EXTN_NBR, POSTAL_CNTRY_CD, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char(KRIM_ENTITY_PHONE_ID_S.NEXTVAL), " +
			"SYS_GUID(), 1, ?, 'PERSON', 'CMP', SubStr(?,1,20), ' ', ' ', 'Y', ?, SYSDATE)";

	private static final String INSERT_PRIV_PREF_ON_SQL = "Insert Into KRIM_ENTITY_PRIV_PREF_T (ENTITY_ID, OBJ_ID, VER_NBR, SUPPRESS_NM_IND, SUPPRESS_EMAIL_IND, SUPPRESS_ADDR_IND," +
			"SUPPRESS_PHONE_IND, SUPPRESS_PRSNL_IND, LAST_UPDT_DT) Values (?, SYS_GUID(), 1, 'Y', 'Y', 'Y', 'Y', 'Y', SYSDATE)";

	private static final String INSERT_PRIV_PREF_OFF_SQL = "Insert Into KRIM_ENTITY_PRIV_PREF_T (ENTITY_ID, OBJ_ID, VER_NBR, SUPPRESS_NM_IND, SUPPRESS_EMAIL_IND, SUPPRESS_ADDR_IND," +
			"SUPPRESS_PHONE_IND, SUPPRESS_PRSNL_IND, LAST_UPDT_DT) Values (?, SYS_GUID(), 1, 'N', 'N', 'Y', 'N', 'N', SYSDATE)";

	private static final String INSERT_EMPLID_SQL = "Insert Into KRIM_ENTITY_EXT_ID_T (ENTITY_EXT_ID_ID, OBJ_ID, VER_NBR, ENTITY_ID, EXT_ID_TYP_CD, EXT_ID, LAST_UPDT_DT) " +
			"Values (To_Char(KRIM_ENTITY_EXT_ID_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'EMPLID', ?, SYSDATE)";

	private static final String INSERT_TAXID_SQL = "Insert Into KRIM_ENTITY_EXT_ID_T (ENTITY_EXT_ID_ID, OBJ_ID, VER_NBR, ENTITY_ID, EXT_ID_TYP_CD, EXT_ID, LAST_UPDT_DT) " +
			"Values (To_Char(KRIM_ENTITY_EXT_ID_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'TAX', 'kvcHXZFmZ0zDIqZUo3sGug==', SYSDATE)";

	private static final String INSERT_NAME_SQL = "Insert Into KRIM_ENTITY_NM_T (ENTITY_NM_ID, OBJ_ID, VER_NBR, ENTITY_ID, NM_TYP_CD, FIRST_NM, MIDDLE_NM, LAST_NM, SUFFIX_NM, " +
			"TITLE_NM, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char(KRIM_ENTITY_NM_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'PRFR', ?, ?, ?, ?, null, 'Y', ?, SYSDATE)";



	// Constants related to frequently-used affiliation and employee info SQL.
	private static final String INSERT_AFFIL_SQL = "Insert Into KRIM_ENTITY_AFLTN_T (ENTITY_AFLTN_ID, OBJ_ID, VER_NBR, ENTITY_ID, AFLTN_TYP_CD, CAMPUS_CD," +
			" DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char(KRIM_ENTITY_AFLTN_ID_S.NEXTVAL), SYS_GUID(), 1, ?, ?, 'IT', ?, 'Y', SYSDATE)";
	private static final String INSERT_EMP_INFO_SQL = "Insert Into KRIM_ENTITY_EMP_INFO_T (ENTITY_EMP_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENTITY_AFLTN_ID," +
			" EMP_STAT_CD, EMP_TYP_CD, BASE_SLRY_AMT, PRMRY_IND, ACTV_IND, LAST_UPDT_DT, PRMRY_DEPT_CD, EMP_ID, EMP_REC_ID) Values (To_Char(KRIM_ENTITY_EMP_ID_S.NEXTVAL)," +
					" SYS_GUID(), 1, ?, To_Char(KRIM_ENTITY_AFLTN_ID_S.CURRVAL), ?, 'P', 5, ?, 'Y', SYSDATE, ?, ?, ?)";
	private static final String UPDATE_AFFIL_SQL = "update KRIM_ENTITY_AFLTN_T SET ACTV_IND = 'Y', DFLT_IND = ? WHERE ENTITY_ID = ? AND AFLTN_TYP_CD = ?";
	private static final String UPDATE_EMP_INFO_SQL = "update KRIM_ENTITY_EMP_INFO_T SET ACTV_IND = 'Y', EMP_STAT_CD = ?, PRMRY_IND = ?, EMP_REC_ID = ?" +
			" WHERE ENTITY_AFLTN_ID = (SELECT ENTITY_AFLTN_ID FROM KRIM_ENTITY_AFLTN_T WHERE ENTITY_ID = ? AND AFLTN_TYP_CD = ?)";
	private static final String DELETE_EMP_INFO_SQL = "DELETE FROM KRIM_ENTITY_EMP_INFO_T WHERE ENTITY_AFLTN_ID = " +
			"(SELECT ENTITY_AFLTN_ID FROM KRIM_ENTITY_AFLTN_T WHERE ENTITY_ID = ? AND AFLTN_TYP_CD = ?)";
	private static final String UPDATE_EMP_INFO_CD_AND_ID_SQL = "UPDATE KRIM_ENTITY_EMP_INFO_T SET PRMRY_DEPT_CD = ?, EMP_ID = ? WHERE ENTITY_ID = ?";
	
	private static final String DATABASE_TEST_SQL = "SELECT 1 FROM DUAL";

	// Constants related to reading in config data for this utility from a .properties file, and for accessing the info for the server DB.
	private static final String SERVER_DB_URL_PROP = "db-url0";
	private static final String SERVER_DB_USERNAME_PROP = "db-username0";
	private static final String SERVER_DB_PASSWORD_PROP = "db-password0";

	private static final String DEST_DB_URL_PROP = "db-url1";
	private static final String DEST_DB_USERNAME_PROP = "db-username1";
	private static final String DEST_DB_PASSWORD_PROP = "db-password1";

	public static final String SKIP_DELTA_FLAG_UPDATES_PROP = "skip-delta-flag-updates";
	public static final String LOAD_LATEST_DELTA_ONLY_PROP = "load-latest-delta-only";
	public static final String LOAD_DELTA_WITH_DATE_PROP = "load-delta-with-date";

	private static final String LOGGING_CONFIG_FILE_ARG = "loggingConfigFile";

	// Constants related to setting up the datasources.
	private static final String DB_DRIVER_NAME = "oracle.jdbc.OracleDriver";
	private static final String DB_VALIDATION_SQL = "select 1 from dual";

	// Passed to indicate properties should be read from System properties instead of a file
	private static final String SYSTEM_PROPERTIES_FILE_INDICATOR = "-";

	private static final class Affiliations {
		private static final char ACADEMIC = 'A';
		private static final char AFFILIATE = 'I';
		private static final char ALUMNI = 'L';
		private static final char EXCEPTION = 'X';
		private static final char FACULTY = 'F';
		private static final char STAFF = 'E';
		private static final char STUDENT = 'S';
	}

	private JdbcTemplate serverTemplate;
	private Properties dbProps;

	//Data feed Items
	//Person Identifiers
	private String CU_PERSON_SID;
	private String NETID;
	private String EMPLID;

	//Name Info
	private String FIRST_NAME;
	private String LAST_NAME;
	private String MIDDLE_NAME;
	private String NAME_SUFFIX;

	//Home Address
	private String HOME_ADDRESS1;
	private String HOME_ADDRESS2;
	private String HOME_ADDRESS3;
	private String HOME_CITY;
	private String HOME_STATE;
	private String HOME_POSTAL;

	//Campus Address
	private String CAMPUS_ADDRESS1;
	private String CAMPUS_ADDRESS2;
	private String CAMPUS_ADDRESS3;
	private String CAMPUS_CITY;
	private String CAMPUS_STATE;
	private String CAMPUS_POSTAL;

	//Email
	private String EMAIL_ADDRESS;

	//Home Phone
	//private String HOME_PHONE;

	//Campus Phone
	private String CAMPUS_PHONE;

	//is person an academic
	private char ACADEMIC;

	//is person a faculty member
	private char FACULTY;

	//is person an affiliate 
	private char AFFILIATE;

	//is person an exception 
	private char DCEXP1;

	//is person a staff member
	private char STAFF;

	//is person a student member
	private char STUDENT;

	//is person a alumni
	private char ALUMNI;

	//Organization
	private String PRIMARY_ORG_CODE;

	// The indicator of which affiliation is primary.
	private char PRIMARY_AFFILIATION_VALUE;

	//Primary AFFLIATION
	private String PRIMARY_AFFILIATION_ACADEMIC;
	private String PRIMARY_AFFILIATION_FACULTY;
	private String PRIMARY_AFFILIATION_AFFILIATE;
	private String PRIMARY_AFFILIATION_EXCEPTION;
	private String PRIMARY_AFFILIATION_STAFF;
	private String PRIMARY_AFFILIATION_STUDENT;
	private String PRIMARY_AFFILIATION_ALUMNI;

	//Primary EMPLOYMENT (in case primary/default affiliation is not supposed to have employment info)
	private String PRIMARY_EMPLOYMENT_FACULTY;
	private String PRIMARY_EMPLOYMENT_STAFF;

	//FERPA Flag
	private String LDAP_SUPPRESS;

	//Active indicator
	private String ACTIVE;

	// Convenience variables for reusing Object[] arrays in queries involving prepared statements.
	private final Object[] oneArgArray = new Object[1];
	private final Object[] twoArgArray = new Object[2];
	private final Object[] threeArgArray = new Object[3];
	private final Object[] fourArgArray = new Object[4];

	/**
	 * Constructs a new instance of this utility using properties from the given file (or system properties if the filename is given as "-").
	 * 
	 * @param dbPropFileName
	 */
	public KimFeed(Properties props) {
		dbProps = props;		
        String serverDBUrl = dbProps.getProperty(SERVER_DB_URL_PROP);
        serverTemplate = new JdbcTemplate(createDataSource(serverDBUrl, dbProps.getProperty(SERVER_DB_USERNAME_PROP), dbProps.getProperty(SERVER_DB_PASSWORD_PROP)));
        testJDBCConnection(serverTemplate, serverDBUrl);
	}
	
    public void testJDBCConnection(JdbcTemplate template, String connectionUrl) {
        try {
            template.execute(DATABASE_TEST_SQL);
            LOG.info("testJDBCConnection, successfully tested " + connectionUrl);
        } catch (Exception e) {
            LOG.error("testJDBCConnection, unable to test query " + connectionUrl, e);
            throw new RuntimeException(e);
        }
    }

	/**
	 * Creates a new DataSource using the given URL, username, and password. The returned DataSource is configured for use with an Oracle DB.
	 */
	private static DataSource createDataSource(String url, String username, String password) {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(DB_DRIVER_NAME);
		dataSource.setUrl(url);
		dataSource.setMaxActive(50);
		dataSource.setMinIdle(7);
		dataSource.setInitialSize(7);
		dataSource.setValidationQuery(DB_VALIDATION_SQL);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setAccessToUnderlyingConnectionAllowed(true);
		return dataSource;
	}

	/**
	 * Retrieves the DB connection and encryption key information from a .properties file.
	 * See the comments in this project's sample .properties file for usage instructions.
	 */
	private Properties getDbAndEncryptionProperties(String propFileName) {
		Properties newProps = new Properties();
		if (SYSTEM_PROPERTIES_FILE_INDICATOR.equals(propFileName)) {
			LOG.debug("getDbAndEncryptionProperties: Reading settings from System properties");
			newProps.putAll(System.getProperties());
		} else {
			LOG.debug("getDbAndEncryptionProperties: Reading settings from file: '" + propFileName + "'");
			FileInputStream propStream = null;
			try {
				propStream = new FileInputStream(propFileName);
				newProps.load(propStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try { propStream.close(); } catch (Exception e) {}
			}
		}
		return newProps;
	}

	/**
	 * A convenience method for populating a one-item Object[] array and making the array available for logging (if an SQL exception occurs).
	 */
	private Object[] get1Args(Object arg0) {
		oneArgArray[0] = arg0;
		return oneArgArray;
	}

	/**
	 * A convenience method for populating a two-item Object[] array and making the array available for logging (if an SQL exception occurs).
	 */
	private Object[] get2Args(Object arg0, Object arg1) {
		twoArgArray[0] = arg0;
		twoArgArray[1] = arg1;
		return twoArgArray;
	}

	/**
	 * A convenience method for populating a three-item Object[] array and making the array available for logging (if an SQL exception occurs).
	 */
	private Object[] get3Args(Object arg0, Object arg1, Object arg2) {
		threeArgArray[0] = arg0;
		threeArgArray[1] = arg1;
		threeArgArray[2] = arg2;
		return threeArgArray;
	}

	/**
	 * A convenience method for populating a four-item Object[] array and making the array available for logging (if an SQL exception occurs).
	 */
	private Object[] get4Args(Object arg0, Object arg1, Object arg2, Object arg3) {
		fourArgArray[0] = arg0;
		fourArgArray[1] = arg1;
		fourArgArray[2] = arg2;
		fourArgArray[3] = arg3;
		return fourArgArray;
	}

	/**
	 * A convenience method for creating a multi-item Object[] array and making the array available for logging (if an SQL exception occurs).
	 */
	private Object[] getMArgs(Object... args) {
		return args;
	}

	private String buildMessageWithLineInfo(String baseMessage) {
        return String.format(LINE_INFO_MESSAGE_FORMAT, baseMessage, NETID, CU_PERSON_SID, EMPLID);
    }

	/**
	 * Truncates the relevant KIM tables in the DB given by the provided JdbcTemplate.
	 */
	private void truncateTables(JdbcTemplate destTemplate) {
		//Disable Constraints
		LOG.info("truncateTables: disabling constraints");
		destTemplate.update("call disable_constraint()");
		
		// Truncate tables
		LOG.info("truncateTables: truncating tables...");
		
		destTemplate.update("truncate table KRIM_ENTITY_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_T");   
		destTemplate.update("Truncate Table KRIM_PRNCPL_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_PRNCPL_T");
		destTemplate.update("Truncate Table KRIM_ENTITY_ADDR_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_ADDR_T");
		destTemplate.update("Truncate Table KRIM_ENTITY_EMAIL_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_EMAIL_T");
		destTemplate.update("Truncate Table KRIM_ENTITY_PHONE_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_PHONE_T");
		destTemplate.update("Truncate Table KRIM_ENTITY_PRIV_PREF_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_PRIV_PREF_T");
		
		destTemplate.update("Truncate Table KRIM_ENTITY_EMP_INFO_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_EMP_INFO_T");
		destTemplate.update("truncate table KRIM_ENTITY_AFLTN_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_AFLTN_T");
		   
		destTemplate.update("Truncate Table KRIM_ENTITY_EXT_ID_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_EXT_ID_T");
		destTemplate.update("Truncate Table KRIM_ENTITY_NM_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_NM_T");
		   
		destTemplate.update("Truncate Table KRIM_ENTITY_ENT_TYP_T Drop Storage");
		LOG.info("truncateTables: truncated KRIM_ENTITY_ENT_TYP_T");
		
		LOG.info("truncateTables: tables truncated!");
		
		//enable constraints
		LOG.info("truncateTables: enabling constraints");
		destTemplate.update("call enable_constraint()");
	}

	/**
	 * Adds the system and administrative users ("kr", "kfs", and "admin") to the DB.
	 */
	private void addSystemUsers(JdbcTemplate destTemplate) {
		
	    LOG.info("addSystemUsers: Adding System Users");
		destTemplate.update("Insert into krim_entity_t (ENTITY_ID,OBJ_ID,VER_NBR,ACTV_IND,LAST_UPDT_DT) values ('1','7ECD903B6A9F48C0E04400144F00411E',1,'Y',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
		destTemplate.update("Insert into krim_entity_t (ENTITY_ID,OBJ_ID,VER_NBR,ACTV_IND,LAST_UPDT_DT) values ('2','7ECD903B6AA048C0E04400144F00411E',1,'Y',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
		destTemplate.update("Insert into krim_entity_t (ENTITY_ID,OBJ_ID,VER_NBR,ACTV_IND,LAST_UPDT_DT) values ('3','7ECD903B6AA148C0E04400144F00411E',1,'Y',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");

		destTemplate.update("Insert into krim_prncpl_t (PRNCPL_ID,OBJ_ID,VER_NBR,PRNCPL_NM,ENTITY_ID,PRNCPL_PSWD,ACTV_IND,LAST_UPDT_DT) values ('1','7ECD903B6A9C48C0E04400144F00411E',175,'kr','1',null,'N',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
		destTemplate.update("Insert into krim_prncpl_t (PRNCPL_ID,OBJ_ID,VER_NBR,PRNCPL_NM,ENTITY_ID,PRNCPL_PSWD,ACTV_IND,LAST_UPDT_DT) values ('2','7ECD903B6A9D48C0E04400144F00411E',5,'kfs','2',null,'N',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
		destTemplate.update("Insert into krim_prncpl_t (PRNCPL_ID,OBJ_ID,VER_NBR,PRNCPL_NM,ENTITY_ID,PRNCPL_PSWD,ACTV_IND,LAST_UPDT_DT) values ('3','7ECD903B6A9E48C0E04400144F00411E',271,'admin','3',null,'N',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");

		destTemplate.update("Insert into krim_entity_ent_typ_t (ENT_TYP_CD,ENTITY_ID,OBJ_ID,VER_NBR,ACTV_IND,LAST_UPDT_DT) values ('SYSTEM','1','7ECD903B6AA248C0E04400144F00411E',1,'Y',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
		destTemplate.update("Insert into krim_entity_ent_typ_t (ENT_TYP_CD,ENTITY_ID,OBJ_ID,VER_NBR,ACTV_IND,LAST_UPDT_DT) values ('SYSTEM','2','7ECD903B6AA348C0E04400144F00411E',1,'Y',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
		destTemplate.update("Insert into krim_entity_ent_typ_t (ENT_TYP_CD,ENTITY_ID,OBJ_ID,VER_NBR,ACTV_IND,LAST_UPDT_DT) values ('PERSON','3','7ECD903B6AA448C0E04400144F00411E',1,'Y',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
	
	}

	/**
	 * Retrieves a full or delta load of users from EDW as an SqlRowSet.
	 */
	private SqlRowSet getEDWData(boolean full) {
	    Object[] queryArgs = new Object[0];
		String table = "edw.cu_person_data_kfs_delta";
		if (full) table = "edw.cu_person_data";
		
		String getSQL = "Select CU_PERSON_SID, EMPLID, NETID, " +
			"NATIONAL_ID, ACADEMIC, STAFF, FACULTY, STUDENT, " +
			"ALUMNI, AFFILIATE, EXCEPTION dcexp1, " +
			"PRIMARY_AFFILIATION, CU_SUPPRESS_ADDR," +
			"LDAP_SUPPRESS, NAME, NAME_PREFIX, NAME_SUFFIX," +
			"LAST_NAME, FIRST_NAME, MIDDLE_NAME, PREF_NAME, " +
			"HOME_ADDRESS1, HOME_ADDRESS2, HOME_ADDRESS3, " +
			"HOME_CITY, HOME_STATE, HOME_POSTAL, HOME_COUNTRY, " +
			"HOME_PHONE, EMAIL_ADDRESS, CAMPUS_ADDRESS, CAMPUS_CITY, " +
			"CAMPUS_STATE, CAMPUS_POSTAL, CAMPUS_PHONE, " +
			"PRIMARY_JOBCODE, PRIMARY_DEPTID, PRIMARY_UNITID," +
			"PRIMARY_ORG_CODE, PRIMARY_EMPL_STATUS, ACTIVE, LDAP_SUPPRESS " +
			"From " + table + " p " +
			"Where netid != ' ' " +
			"and cu_person_sid = cu_master_person_sid ";
			//"and cu_person_sid = (select cu_master_person_sid from " + table + " p1 ";
		
		if (!full) {
			getSQL += "AND PROCESS_FLAG != 'S' AND PROCESS_FLAG != 'D'";
			
			if (shouldOnlyUseDeltaLoadWithHighestSequenceNumber()) {
			    LOG.info("getEDWData: Preparing to get EDW data for only the most recent load, regardless of whether it has already been read");
			    getSQL += " AND LOAD_SEQ = (SELECT MAX(LOAD_SEQ) from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR)";
			} else if (shouldOnlyUseDeltaLoadWithSpecificDate()) {
			    LOG.info("getEDWData: Preparing to get EDW data for the load with this date, regardless of whether it has already been read: "
			            + getDeltaLoadDate());
			    getSQL += " AND LOAD_SEQ in (SELECT LOAD_SEQ from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR WHERE FILE_DATE BETWEEN ? AND ?)";
			    LocalDate deltaLoadDate = getParsedDeltaLoadDate();
			    queryArgs = new Object[] { getTimestampForStartOfDay(deltaLoadDate), getTimestampForEndOfDay(deltaLoadDate) };
			} else {
			    LOG.info("getEDWData: Preparing to get EDW data for only the loads that have not been read yet");
			    getSQL += " AND LOAD_SEQ in (SELECT LOAD_SEQ from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR WHERE READ_BY_KFS = 'N')";
			}
			getSQL += " ORDER BY LOAD_SEQ, EMPLID";
		}
		
		LOG.info("getEDWData, the generated SQL: " + getSQL);
        if (queryArgs != null) {
            for (Object arg : queryArgs) {
                LOG.info("getEDWData, query argument: " + arg);
            }
        }
		
		//Get data from the EDW
		LOG.info("getEDWData: getting edw data");
		return serverTemplate.queryForRowSet(getSQL, queryArgs); 
	}

	/**
	 * Gets the data for the current user in the SqlRowSet.
	 */
	private void getDataAndSanitize (SqlRowSet rs) {
		
		CU_PERSON_SID = rs.getString(CU_PERSON_SID_ROW);
		NETID = rs.getString(NETID_ROW);
		EMPLID = rs.getString(EMPLID_ROW);
		FIRST_NAME = rs.getString(FIRST_NAME_ROW);
		LAST_NAME = rs.getString(LAST_NAME_ROW);
		MIDDLE_NAME = rs.getString(MIDDLE_NAME_ROW);
		NAME_SUFFIX = rs.getString(NAME_SUFFIX_ROW);
		HOME_ADDRESS1 = rs.getString(HOME_ADDRESS1_ROW);
		HOME_ADDRESS2 = rs.getString(HOME_ADDRESS2_ROW);
		HOME_ADDRESS3 = rs.getString(HOME_ADDRESS3_ROW);
		HOME_CITY = rs.getString(HOME_CITY_ROW);
		HOME_STATE = rs.getString(HOME_STATE_ROW);
		HOME_POSTAL = rs.getString(HOME_POSTAL_ROW);
		CAMPUS_ADDRESS1 = rs.getString(CAMPUS_ADDRESS_ROW);
		CAMPUS_CITY = rs.getString(CAMPUS_CITY_ROW);
		CAMPUS_STATE = rs.getString(CAMPUS_STATE_ROW);
		CAMPUS_POSTAL = rs.getString(CAMPUS_POSTAL_ROW);
		EMAIL_ADDRESS = rs.getString(EMAIL_ADDRESS_ROW);
		//HOME_PHONE = rs.getString("HOME_PHONE");
		CAMPUS_PHONE = rs.getString(CAMPUS_PHONE_ROW);
		ACADEMIC = rs.getString(ACADEMIC_ROW).charAt(0);
		FACULTY = rs.getString(FACULTY_ROW).charAt(0);
		AFFILIATE = rs.getString(AFFILIATE_ROW).charAt(0);
		DCEXP1 = rs.getString(DCEXP1_ROW).charAt(0);
		STAFF = rs.getString(STAFF_ROW).charAt(0);
		STUDENT = rs.getString(STUDENT_ROW).charAt(0);
		ALUMNI = rs.getString(ALUMNI_ROW).charAt(0);
		PRIMARY_ORG_CODE = IT_PREFIX + rs.getString(PRIMARY_ORG_CODE_ROW);
		PRIMARY_AFFILIATION_VALUE = rs.getString(PRIMARY_AFFILIATION_ROW).charAt(0);
		PRIMARY_AFFILIATION_ACADEMIC = (PRIMARY_AFFILIATION_VALUE == Affiliations.ACADEMIC) ? YES_VAL : NO_VAL;
		PRIMARY_AFFILIATION_FACULTY = (PRIMARY_AFFILIATION_VALUE == Affiliations.FACULTY) ? YES_VAL : NO_VAL;
		PRIMARY_AFFILIATION_AFFILIATE = (PRIMARY_AFFILIATION_VALUE == Affiliations.AFFILIATE) ? YES_VAL : NO_VAL;
		PRIMARY_AFFILIATION_EXCEPTION = (PRIMARY_AFFILIATION_VALUE == Affiliations.EXCEPTION) ? YES_VAL : NO_VAL;
		PRIMARY_AFFILIATION_STAFF = (PRIMARY_AFFILIATION_VALUE == Affiliations.STAFF) ? YES_VAL : NO_VAL;
		PRIMARY_AFFILIATION_STUDENT = (PRIMARY_AFFILIATION_VALUE == Affiliations.STUDENT) ? YES_VAL : NO_VAL;
		PRIMARY_AFFILIATION_ALUMNI = (PRIMARY_AFFILIATION_VALUE == Affiliations.ALUMNI) ? YES_VAL : NO_VAL;
		
		// Truncate "home" address lines, and move truncated characters to subsequent blank lines as needed.
		if (StringUtils.isNotEmpty(HOME_ADDRESS1) && HOME_ADDRESS1.length() > 40) {
			if (StringUtils.isBlank(HOME_ADDRESS2)) {
				HOME_ADDRESS2 = HOME_ADDRESS1.substring(40);
			}
			HOME_ADDRESS1 = HOME_ADDRESS1.substring(0, 40);
		}
		if (StringUtils.isNotEmpty(HOME_ADDRESS2) && HOME_ADDRESS2.length() > 40) {
			if (StringUtils.isBlank(HOME_ADDRESS3)) {
				HOME_ADDRESS3 = HOME_ADDRESS2.substring(40);
			}
			HOME_ADDRESS2 = HOME_ADDRESS2.substring(0, 40);
		}
		if (StringUtils.isNotEmpty(HOME_ADDRESS3) && HOME_ADDRESS3.length() > 40) {
			HOME_ADDRESS3 = HOME_ADDRESS3.substring(0, 40);
		}
		
		// Truncate "campus" address lines, and move truncated characters to subsequent blank lines as needed.
		if (StringUtils.isNotEmpty(CAMPUS_ADDRESS1) && CAMPUS_ADDRESS1.length() > 40) {
			if (CAMPUS_ADDRESS1.length() > 80) {
				CAMPUS_ADDRESS2 = CAMPUS_ADDRESS1.substring(40, 80);
				CAMPUS_ADDRESS3 = (CAMPUS_ADDRESS1.length() > 120) ? CAMPUS_ADDRESS1.substring(80, 120) : CAMPUS_ADDRESS1.substring(80);
			} else {
				CAMPUS_ADDRESS2 = CAMPUS_ADDRESS1.substring(40);
				CAMPUS_ADDRESS3 = BLANK_ADDRESS_LINE;
			}
			CAMPUS_ADDRESS1 = CAMPUS_ADDRESS1.substring(0, 40);
		} else {
			CAMPUS_ADDRESS2 = BLANK_ADDRESS_LINE;
			CAMPUS_ADDRESS3 = BLANK_ADDRESS_LINE;
		}
		
		// Determine primary employment info, which requires special handling if the primary affiliation is Academic, Affiliate, Student, Exception, or Alumni,
		// since those five affiliation types are not supposed to have employment info.
		switch (PRIMARY_AFFILIATION_VALUE) {
			case Affiliations.ACADEMIC :
			case Affiliations.AFFILIATE :
			case Affiliations.ALUMNI :
			case Affiliations.STUDENT :
			case Affiliations.EXCEPTION :
				PRIMARY_EMPLOYMENT_FACULTY = NO_VAL;
				PRIMARY_EMPLOYMENT_STAFF = NO_VAL;
				// If necessary, set a Faculty or Staff as having the primary employment info.
				// Active employment infos take precedence over inactive ones, and inactive ones take precedence over retired ones.
				boolean foundPrimaryEmployment = false;
				for (int i = 0; !foundPrimaryEmployment && i < EMP_STAT_CHARS.length; i++) {
					if (FACULTY == EMP_STAT_CHARS[i]) {
						PRIMARY_EMPLOYMENT_FACULTY = YES_VAL;
						foundPrimaryEmployment = true;
					} else if (STAFF == EMP_STAT_CHARS[i]) {
						PRIMARY_EMPLOYMENT_STAFF = YES_VAL;
						foundPrimaryEmployment = true;
					}
				}
				break;
			default :
				PRIMARY_EMPLOYMENT_FACULTY = PRIMARY_AFFILIATION_FACULTY;
				PRIMARY_EMPLOYMENT_STAFF = PRIMARY_AFFILIATION_STAFF;
				break;
		}
		
		LDAP_SUPPRESS = rs.getString(LDAP_SUPPRESS_ROW);
		ACTIVE = YES_VAL; //rs.getString("ACTIVE");	
	}

	/**
	 * Updates a user's KIM entity data as appropriate.
	 */
	@SuppressWarnings("deprecation")
	private void RunUpdates(JdbcTemplate destTemplate) {
		// Determine whether to insert new user info or update existing user info.
		if (destTemplate.queryForObject(SELECT_ENTITY_COUNT_SQL, get1Args(CU_PERSON_SID), Integer.class) > 0) {
			/*
			 * ==========================
			 * Update existing user info.
			 * ==========================
			 */
			
		    if (currentUserHasNoPrincipalDataUnderExpectedEntityId(destTemplate)) {
		        throw new IllegalStateException("Could not update user because a matching current principal record does not exist");
		    }
		    
			// Update principal record.
			destTemplate.update(UPDATE_PRINCIPAL_SQL, get2Args(NETID, CU_PERSON_SID) );
			
			// Update home address.
			destTemplate.update(UPDATE_HOME_ADDRESS_SQL, getMArgs(HOME_ADDRESS1, HOME_ADDRESS2, HOME_ADDRESS3, HOME_CITY, HOME_STATE, HOME_POSTAL, CU_PERSON_SID) );
			
			// Update campus address.
			destTemplate.update(UPDATE_CAMPUS_ADDRESS_SQL,
					getMArgs(CAMPUS_ADDRESS1, CAMPUS_ADDRESS2, CAMPUS_ADDRESS3, CAMPUS_CITY, CAMPUS_STATE, CAMPUS_POSTAL, CU_PERSON_SID ) );
			
			// Update campus phone number.
			destTemplate.update(UPDATE_CAMPUS_PHONE_SQL, get2Args(CAMPUS_PHONE, CU_PERSON_SID) );
			
			// Update employee ID and person's name.
			destTemplate.update(UPDATE_EMPLID_SQL, get2Args(EMPLID, CU_PERSON_SID));
			destTemplate.update(UPDATE_NAME_SQL, getMArgs(FIRST_NAME, MIDDLE_NAME, LAST_NAME, NAME_SUFFIX, CU_PERSON_SID) );
			
			//Adjust Affiliations
			
			SqlRowSet affilRS = destTemplate.queryForRowSet(SELECT_AFFIL_TYPE_SQL, get1Args(CU_PERSON_SID) );
			List<String> affils = new ArrayList<String>();
			
			// Determine which affiliations the user already has in KIM.
			while(affilRS.next()) {
				affils.add(affilRS.getString(AFLTN_TYP_CD_ROW));
			}
			
			// Create or update ACADEMIC affiliation (and delete any existing associated employment info) as needed.
			if (affils.contains(ACADEMIC_AFFIL_CONST)) {
				destTemplate.update(UPDATE_AFFIL_SQL, get3Args(PRIMARY_AFFILIATION_ACADEMIC, CU_PERSON_SID, ACADEMIC_AFFIL_CONST) );
				destTemplate.update(DELETE_EMP_INFO_SQL, get2Args(CU_PERSON_SID, ACADEMIC_AFFIL_CONST) );
			} else {
				switch (ACADEMIC) {
					case IS_ACTIVE_AFFIL :
					case IS_INACTIVE_AFFIL :
					case IS_RETIRED_AFFIL :
						destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, ACADEMIC_AFFIL_CONST, PRIMARY_AFFILIATION_ACADEMIC) );
						break;
				}
			}
			
			// Create or update ALUMNI affiliation (and delete any existing associated employment info) as needed.
			if (affils.contains(ALUMNI_AFFIL_CONST)) {
				destTemplate.update(UPDATE_AFFIL_SQL, get3Args(PRIMARY_AFFILIATION_ALUMNI, CU_PERSON_SID, ALUMNI_AFFIL_CONST) );
				destTemplate.update(DELETE_EMP_INFO_SQL, get2Args(CU_PERSON_SID, ALUMNI_AFFIL_CONST) );
			} else {
				switch (ALUMNI) {
					case IS_ACTIVE_AFFIL :
					case IS_INACTIVE_AFFIL :
					case IS_RETIRED_AFFIL :
						destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, ALUMNI_AFFIL_CONST, PRIMARY_AFFILIATION_ALUMNI) );
						break;
				}
			}
			
			// Create or update FCLTY (Faculty) affiliation and employment info as needed.
			if (affils.contains(FACULTY_AFFIL_CONST)) {
				destTemplate.update(UPDATE_AFFIL_SQL, get3Args(PRIMARY_AFFILIATION_FACULTY, CU_PERSON_SID, FACULTY_AFFIL_CONST) );
				destTemplate.update(UPDATE_EMP_INFO_SQL, getMArgs((FACULTY != IS_NONEXISTENT_AFFIL) ? String.valueOf(FACULTY) : INACTIVE_VAL,
						PRIMARY_EMPLOYMENT_FACULTY, FACULTY_AFFIL_CONST, CU_PERSON_SID, FACULTY_AFFIL_CONST) );
			} else {
				switch (FACULTY) {
					case IS_ACTIVE_AFFIL :
					case IS_INACTIVE_AFFIL :
					case IS_RETIRED_AFFIL :
						destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, FACULTY_AFFIL_CONST, PRIMARY_AFFILIATION_FACULTY) );
						destTemplate.update(INSERT_EMP_INFO_SQL, getMArgs(
								CU_PERSON_SID, String.valueOf(FACULTY), PRIMARY_EMPLOYMENT_FACULTY, PRIMARY_ORG_CODE, EMPLID, FACULTY_AFFIL_CONST) );
						break;
				}
			}
			
			// Create or update AFLT (Affiliate) affiliation (and delete any existing associated employment info) as needed.
			if (affils.contains(AFFILIATE_AFFIL_CONST)) {
				destTemplate.update(UPDATE_AFFIL_SQL, get3Args(PRIMARY_AFFILIATION_AFFILIATE, CU_PERSON_SID, AFFILIATE_AFFIL_CONST) );
				destTemplate.update(DELETE_EMP_INFO_SQL, get2Args(CU_PERSON_SID, AFFILIATE_AFFIL_CONST));
			} else {
				switch (AFFILIATE) {
					case IS_ACTIVE_AFFIL :
					case IS_INACTIVE_AFFIL :
					case IS_RETIRED_AFFIL :
						destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, AFFILIATE_AFFIL_CONST, PRIMARY_AFFILIATION_AFFILIATE) );
						break;
				}
			}
			
			// Create or update EXCPTN (Exception) affiliation (and delete any existing associated employment info) as needed.
			if (affils.contains(EXCEPTION_AFFIL_CONST)) {
				destTemplate.update(UPDATE_AFFIL_SQL, get3Args(PRIMARY_AFFILIATION_EXCEPTION, CU_PERSON_SID, EXCEPTION_AFFIL_CONST) );
				destTemplate.update(DELETE_EMP_INFO_SQL, get2Args(CU_PERSON_SID, EXCEPTION_AFFIL_CONST) );
			} else {
				switch (DCEXP1) {
					case IS_ACTIVE_AFFIL :
					case IS_INACTIVE_AFFIL :
					case IS_RETIRED_AFFIL :
						destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, EXCEPTION_AFFIL_CONST, PRIMARY_AFFILIATION_EXCEPTION) );
						break;
				}
			}
			
			// Create or update STAFF affiliation and employment info as needed.
			if (affils.contains(STAFF_AFFIL_CONST)) {
				destTemplate.update(UPDATE_AFFIL_SQL, get3Args(PRIMARY_AFFILIATION_STAFF, CU_PERSON_SID, STAFF_AFFIL_CONST) );
				destTemplate.update(UPDATE_EMP_INFO_SQL, getMArgs((STAFF != IS_NONEXISTENT_AFFIL) ? String.valueOf(STAFF) : INACTIVE_VAL,
						PRIMARY_EMPLOYMENT_STAFF, STAFF_AFFIL_CONST, CU_PERSON_SID, STAFF_AFFIL_CONST) );
			} else {
				switch (STAFF) {
					case IS_ACTIVE_AFFIL :
					case IS_INACTIVE_AFFIL :
					case IS_RETIRED_AFFIL :
						destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, STAFF_AFFIL_CONST, PRIMARY_AFFILIATION_STAFF) );
						destTemplate.update(INSERT_EMP_INFO_SQL, getMArgs(
								CU_PERSON_SID, String.valueOf(STAFF), PRIMARY_EMPLOYMENT_STAFF, PRIMARY_ORG_CODE, EMPLID, STAFF_AFFIL_CONST) );
						break;
				}
			}
			
			// Create or update STDNT (Student) affiliation (and delete any existing associated employment info) as needed.
			if (affils.contains(STUDENT_AFFIL_CONST)) {
				destTemplate.update(UPDATE_AFFIL_SQL, get3Args(PRIMARY_AFFILIATION_STUDENT, CU_PERSON_SID, STUDENT_AFFIL_CONST) );
				destTemplate.update(DELETE_EMP_INFO_SQL, get2Args(CU_PERSON_SID, STUDENT_AFFIL_CONST) );
			} else {
				switch (STUDENT) {
					case IS_ACTIVE_AFFIL :
					case IS_INACTIVE_AFFIL :
					case IS_RETIRED_AFFIL :
						destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, STUDENT_AFFIL_CONST, PRIMARY_AFFILIATION_STUDENT));
						break;
				}
			}
			
			// Update the department code and employee ID on all of the person's employment info records.
			destTemplate.update(UPDATE_EMP_INFO_CD_AND_ID_SQL, get3Args(PRIMARY_ORG_CODE, EMPLID, CU_PERSON_SID) );
			
			// Create or update the person's email address as needed.
			if (!EMAIL_ADDRESS.equals(SINGLE_SPACE) ) {
				if (destTemplate.queryForObject(SELECT_EMAIL_COUNT_SQL, get1Args(CU_PERSON_SID), Integer.class) > 0) {
					destTemplate.update(UPDATE_EMAIL_SQL, get2Args(EMAIL_ADDRESS, CU_PERSON_SID) );
				} else {
					destTemplate.update(INSERT_EMAIL_ADDRESS_SQL, get3Args(CU_PERSON_SID, EMAIL_ADDRESS, ACTIVE) );
				}
			}
			
			// Update the person's KIM privacy preferences based on their LDAP privacy preferences.
			if (LDAP_SUPPRESS.equals(YES_VAL)) {
				destTemplate.update(UPDATE_PRIV_PREF_SQL, get1Args(CU_PERSON_SID) );
			}
			
			LOG.info(buildMessageWithLineInfo("RunUpdates: Successfully updated existing user"));
		} else {
			/*
			 * =========================
			 * Create info for new user.
			 * =========================
			 */
			
		    if (currentUserHasPrincipalDataUnderDifferentEntityId(destTemplate)) {
                throw new IllegalStateException("Could not insert user because one or more conflicting principal records already exist.");
            }
		    
			// Create the person's entity record.
			destTemplate.update(INSERT_ENTITY_SQL, get2Args(CU_PERSON_SID, ACTIVE) );

			// Create the person's principal record.
			destTemplate.update(INSERT_PRINCIPAL_SQL, get4Args(CU_PERSON_SID, NETID, CU_PERSON_SID, ACTIVE) );

			// Create the person's entity type record.
			destTemplate.update(INSERT_ENTITY_TYPE_SQL, get2Args(CU_PERSON_SID, ACTIVE) );

			// Set the person's home address.
			destTemplate.update(INSERT_HOME_ADDRESS_SQL, getMArgs(CU_PERSON_SID, HOME_ADDRESS1, HOME_ADDRESS2, HOME_ADDRESS3, HOME_CITY, HOME_STATE, HOME_POSTAL, ACTIVE) );

			// Set the person's campus address.
			destTemplate.update(INSERT_CAMPUS_ADDRESS_SQL,
					getMArgs(CU_PERSON_SID, CAMPUS_ADDRESS1, CAMPUS_ADDRESS2, CAMPUS_ADDRESS3, CAMPUS_CITY, CAMPUS_STATE, CAMPUS_POSTAL, ACTIVE) );

			// Set the person's email address, if defined.
			if (!EMAIL_ADDRESS.equals(SINGLE_SPACE) ) {
				destTemplate.update(INSERT_EMAIL_ADDRESS_SQL, get3Args(CU_PERSON_SID, EMAIL_ADDRESS, ACTIVE) );
			}
			
			// Set the person's campus phone number.
			destTemplate.update(INSERT_CAMPUS_PHONE_SQL, get3Args(CU_PERSON_SID, CAMPUS_PHONE, ACTIVE) );
			
			// Set the person's privacy preferences, based on the person's LDAP privacy preferences.
			if (LDAP_SUPPRESS.equals(YES_VAL)) {
				destTemplate.update(INSERT_PRIV_PREF_ON_SQL, get1Args(CU_PERSON_SID) );
			} else {
				destTemplate.update(INSERT_PRIV_PREF_OFF_SQL, get1Args(CU_PERSON_SID) );
			}
			
			// Add the person's ACADEMIC affiliation, if necessary.
			switch (ACADEMIC) {
				case IS_ACTIVE_AFFIL :
				case IS_INACTIVE_AFFIL :
				case IS_RETIRED_AFFIL :
					destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, ACADEMIC_AFFIL_CONST, PRIMARY_AFFILIATION_ACADEMIC) );
					break;
			}
			
			// Add the person's ALUMNI affiliation, if necessary.
			switch (ALUMNI) {
				case IS_ACTIVE_AFFIL :
				case IS_INACTIVE_AFFIL :
				case IS_RETIRED_AFFIL :
					destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, ALUMNI_AFFIL_CONST, PRIMARY_AFFILIATION_ALUMNI) );
					break;
			}
			
			// Add the person's FCLTY (Faculty) affiliation and employment info, if necessary.
			switch (FACULTY) {
				case IS_ACTIVE_AFFIL :
				case IS_INACTIVE_AFFIL :
				case IS_RETIRED_AFFIL :
					destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, FACULTY_AFFIL_CONST, PRIMARY_AFFILIATION_FACULTY) );
					destTemplate.update(INSERT_EMP_INFO_SQL, getMArgs(
							CU_PERSON_SID, String.valueOf(FACULTY), PRIMARY_EMPLOYMENT_FACULTY, PRIMARY_ORG_CODE, EMPLID, FACULTY_AFFIL_CONST) );
					break;
			}
			
			// Add the person's AFLT (Affiliate) affiliation, if necessary.
			switch (AFFILIATE) {
				case IS_ACTIVE_AFFIL :
				case IS_INACTIVE_AFFIL :
				case IS_RETIRED_AFFIL :
					destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, AFFILIATE_AFFIL_CONST, PRIMARY_AFFILIATION_AFFILIATE) );
					break;
			}
			
			// Add the person's EXCPTN (Exception) affiliation, if necessary.
			switch (DCEXP1) {
				case IS_ACTIVE_AFFIL :
				case IS_INACTIVE_AFFIL :
				case IS_RETIRED_AFFIL :
					destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, EXCEPTION_AFFIL_CONST, PRIMARY_AFFILIATION_EXCEPTION) );
					break;
			}
			
			// Add the person's STAFF affiliation and employment info, if necessary.
			switch (STAFF) {
				case IS_ACTIVE_AFFIL :
				case IS_INACTIVE_AFFIL :
				case IS_RETIRED_AFFIL :
					destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, STAFF_AFFIL_CONST, PRIMARY_AFFILIATION_STAFF) );   
					destTemplate.update(INSERT_EMP_INFO_SQL, getMArgs(
							CU_PERSON_SID, String.valueOf(STAFF), PRIMARY_EMPLOYMENT_STAFF, PRIMARY_ORG_CODE, EMPLID, STAFF_AFFIL_CONST) );
					break;
			}
			
			// Add the person's STDNT (Student) affiliation, if necessary.
			switch (STUDENT) {
				case IS_ACTIVE_AFFIL :
				case IS_INACTIVE_AFFIL :
				case IS_RETIRED_AFFIL :
					destTemplate.update(INSERT_AFFIL_SQL, get3Args(CU_PERSON_SID, STUDENT_AFFIL_CONST, PRIMARY_AFFILIATION_STUDENT) );
					break;
			}
			
			// Set the person's employee ID.
			destTemplate.update(INSERT_EMPLID_SQL, get2Args(CU_PERSON_SID, EMPLID));

			// Set the person's tax ID(?).
			destTemplate.update(INSERT_TAXID_SQL, get1Args(CU_PERSON_SID));
			
			// Set the person's name.
			destTemplate.update(INSERT_NAME_SQL, getMArgs(CU_PERSON_SID, FIRST_NAME, MIDDLE_NAME, LAST_NAME, NAME_SUFFIX, ACTIVE) );
			
			LOG.info(buildMessageWithLineInfo("RunUpdates: Successfully inserted new user"));
		}	
	}

	private boolean currentUserHasNoPrincipalDataUnderExpectedEntityId(JdbcTemplate destTemplate) {
	    int matchingPrincipalCount = destTemplate.queryForObject(SELECT_MATCHING_PRINCIPAL_COUNT_SQL, get2Args(NETID, CU_PERSON_SID), Integer.class);
	    return matchingPrincipalCount == 0;
	}

	private boolean currentUserHasPrincipalDataUnderDifferentEntityId(JdbcTemplate destTemplate) {
	    int conflictingPrincipalCount = destTemplate.queryForObject(SELECT_CONFLICTING_PRINCIPAL_COUNT_SQL, get2Args(NETID, CU_PERSON_SID), Integer.class);
	    return conflictingPrincipalCount > 0;
	}
	
	/**
	 * Performs full or delta load updates.
	 */
	private void refresh(boolean full) {
		String deleteIDs = "";
		LOG.info("refresh: Starting KIM feed processing...");
		
        String destinationDBUrl = dbProps.getProperty(DEST_DB_URL_PROP);
        JdbcTemplate destTemplate = new JdbcTemplate(createDataSource(destinationDBUrl, dbProps.getProperty(DEST_DB_USERNAME_PROP), dbProps.getProperty(DEST_DB_PASSWORD_PROP)));

        testJDBCConnection(destTemplate, destinationDBUrl);
		
		if (full) {
			truncateTables(destTemplate);
			addSystemUsers(destTemplate);
		}
		
		if (!full) {
			SqlRowSet deleters = serverTemplate.queryForRowSet("SELECT CU_PERSON_SID FROM edw.cu_person_data_kfs_delta WHERE PROCESS_FLAG = 'D' AND LOAD_SEQ= (SELECT max(LOAD_SEQ) FROM edw.cu_person_data_kfs_delta)");
			
			while(deleters.next()) {
				deleteIDs += "'" + deleters.getString("CU_PERSON_SID") + "',";
			}
			
			if (deleteIDs.length() > 0 ) {
				deleteIDs = deleteIDs.substring(0, deleteIDs.length() -1);
				destTemplate.execute("UPDATE KRIM_PRNCPL_T SET PRNCPL_NM = 'DIS-' || PRNCPL_NM WHERE PRNCPL_ID IN (" + deleteIDs + ") " +
										"		AND PRNCPL_NM NOT LIKE 'DIS-%'");	
				LOG.info("refresh: The following users were marked as deleted/discontinued: " + deleteIDs);
			} else {
			    LOG.info("refresh: No users were marked as deleted/discontinued.");
			}
		}

		
		SqlRowSet rs  = getEDWData(full);
		boolean emptyLoad = true;

		LOG.info("refresh: Running updates...");

		while (rs.next()) {
		    emptyLoad = false;
			getDataAndSanitize(rs);
			try {
				RunUpdates(destTemplate);   
			} catch (Exception e) {
				LOG.error(buildMessageWithLineInfo("refresh: ERROR encountered while updating user"), e);
			}
		}
		
		if (emptyLoad) {
		    LOG.info("refresh: No users were inserted or updated by the KIM feed load.");
		}
	}

	/**
	 * Executes a full refresh.
	 */
	public void FullRefresh() {
	    try {
	        refresh(true);
	    } catch (Exception e) {
	        LOG.error("FullRefresh: An error occurred that caused the full refresh to abort completely.", e);
	        throw e;
	    }
	    LOG.info("FullRefresh: The full refresh completed successfully.");
	}

	/**
	 * Executes a delta refresh.
	 */
	public void Delta() {
	    try {
	        refresh(false);
	        if (shouldMarkDeltaRowsAsRead()) {
	            serverTemplate.execute("UPDATE EDW.CU_PERSON_DATA_KFS_DELTA_MSTR set READ_BY_KFS = 'Y'");
	        } else {
	            LOG.info("Delta: Application is configured to avoid mark-as-read updates to EDW; skipping this step...");
	        }
	    } catch (Exception e) {
	        LOG.error("Delta: An error occurred that caused the nightly delta feed to abort completely.", e);
	        throw e;
	    }
	    LOG.info("Delta: The nightly delta feed completed successfully.");
	}

    private boolean shouldMarkDeltaRowsAsRead() {
        return !Boolean.parseBoolean(dbProps.getProperty(SKIP_DELTA_FLAG_UPDATES_PROP));
    }

    private boolean shouldOnlyUseDeltaLoadWithHighestSequenceNumber() {
        return Boolean.parseBoolean(dbProps.getProperty(LOAD_LATEST_DELTA_ONLY_PROP));
    }

    private boolean shouldOnlyUseDeltaLoadWithSpecificDate() {
        return StringUtils.isNotBlank(getDeltaLoadDate());
    }

    private LocalDate getParsedDeltaLoadDate() {
        String deltaLoadDate = getDeltaLoadDate();
        if (ObjectUtils.isNull(deltaLoadDate) || StringUtils.isBlank(deltaLoadDate)) {
            LOG.info("getParsedDeltaLoadDate: Properties file deltaLoadDate was detected to be blank or null. Returning null.");
            return null;
        }
        LocalDate parsedDeltaLoadDateAsLocalDate = LocalDate.parse(deltaLoadDate, DATE_FORMATTER_FOR_PARSED_DELTA_LOAD_DATE);
        LOG.info("getParsedDeltaLoadDate: parsedDeltaLoadDateAsLocalDate={}", parsedDeltaLoadDateAsLocalDate.toString());
        return parsedDeltaLoadDateAsLocalDate;
    }

    private Timestamp getTimestampForStartOfDay(LocalDate date) {
        if (ObjectUtils.isNull(date)) {
            LOG.info("getTimestampForStartOfDay: Input parameter date of type LocalDate was detected to be blank or null. Returning null.");
            return null;
        }
        LocalDateTime startOfDayAsLocalDateTime = date.atStartOfDay();
        Timestamp startOfDayAsTimestamp = new Timestamp(startOfDayAsLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        LOG.info("getTimestampForStartOfDay: startOfDayAsLocalDateTime={}   startOfDayAsTimestamp={}", startOfDayAsLocalDateTime.toString(), startOfDayAsTimestamp.toString());
        return startOfDayAsTimestamp;
    }

    private Timestamp getTimestampForEndOfDay(LocalDate date) {
        if (ObjectUtils.isNull(date)) {
            LOG.info("getTimestampForEndOfDay: Input parameter date of type LocalDate was detected to be blank or null. Returning null.");
            return null;
        }
        LocalDateTime endOfDayAsLocalDateTime = date.atTime(23, 59, 59, 59);
        Timestamp endOfDayTimestamp = new Timestamp(endOfDayAsLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        LOG.info("getTimestampForEndOfDay: endOfDayAsLocalDateTime={}   endOfDayTimestamp={}", endOfDayAsLocalDateTime.toString(), endOfDayTimestamp.toString());
        return endOfDayTimestamp;
    }

    private String getDeltaLoadDate() {
        return dbProps.getProperty(LOAD_DELTA_WITH_DATE_PROP);
    }

	/**
	 * @param args
	 */
	public static void runKimFeed(Properties props, String type) {
	    String[] args = new String[0];
		SimpleCommandLineParser parser = new SimpleCommandLineParser(args);
		String configfile = parser.getValue("filename", "file", "f");
		//String type = parser.getValue("type", "t");
		
		if (configfile == null) {
			LOG.debug("runKimFeed: No config file ...");
			configfile = "-";
		}
		
		KimFeed kimfeed = new KimFeed(props); 
		
		if (type != null && type.equals("full")) {
			LOG.info("runKimFeed: Running full refresh");
			kimfeed.FullRefresh();
		} else {
			LOG.info("runKimFeed: Running delta....");
			kimfeed.Delta();
		}
		LOG.info("runKimFeed: All done!");
	}

}
