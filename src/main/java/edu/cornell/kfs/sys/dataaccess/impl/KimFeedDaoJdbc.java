package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.dao.DataAccessException;

import edu.cornell.kfs.sys.dataaccess.KimDataUpdateDao;
import edu.cornell.kfs.sys.dataaccess.KimFeedConstants;
import edu.cornell.kfs.sys.dataaccess.KimFeedDao;
import edu.cornell.kfs.sys.dataaccess.dto.KimFeedPersonDTO;

public class KimFeedDaoJdbc extends PlatformAwareDaoBaseJdbc implements KimFeedDao {

    private static final Logger LOG = LogManager.getLogger(KimFeedDaoJdbc.class);

    protected KimDataUpdateDao kimDataUpdateDao;
    protected String rowTimePeriodIndicator;
    protected boolean markDeltaRowsAsReadWhenFinished;

    @Override
    public void processDataUpdates() {
        try {
            refresh();
            if (markDeltaRowsAsReadWhenFinished) {
                getJdbcTemplate().execute("UPDATE EDW.CU_PERSON_DATA_KFS_DELTA_MSTR set READ_BY_KFS = 'Y'");
            } else {
                LOG.info("processDataUpdates: Application is configured to avoid mark-as-read updates to EDW; skipping this step...");
            }
        } catch (Exception e) {
            LOG.error("processDataUpdates: An error occurred that caused the nightly delta feed to abort completely.", e);
            throw e;
        }
        LOG.info("processDataUpdates: The nightly delta feed completed successfully.");
    }

    private void refresh() {
        LOG.info("refresh: Starting KIM feed processing...");
        
        kimDataUpdateDao.checkDataSourceStatus();
        
        List<String> usersToDisable = getJdbcTemplate().query(
                "SELECT CU_PERSON_SID FROM edw.cu_person_data_kfs_delta WHERE PROCESS_FLAG = 'D' AND LOAD_SEQ= (SELECT max(LOAD_SEQ) FROM edw.cu_person_data_kfs_delta)",
                (resultSet, rowNumber) -> resultSet.getString(1));
        
        if (!usersToDisable.isEmpty()) {
            kimDataUpdateDao.markPersonRecordsAsDisabled(usersToDisable);
            LOG.info("refresh: The following users were marked as disabled: " + usersToDisable);
        } else {
            LOG.info("refresh: No users were marked as disabled.");
        }
        
        Integer rowCount = readAndProcessEDWData();
        
        if (rowCount == 0) {
            LOG.info("refresh: No users were inserted or updated by the KIM feed load.");
        }
    }

    protected Integer readAndProcessEDWData() {
        Object[] queryArgs = new Object[0];
        String table = "edw.cu_person_data_kfs_delta";
        
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
        
        getSQL += "AND PROCESS_FLAG != 'S' AND PROCESS_FLAG != 'D'";
        
        switch (rowTimePeriodIndicator) {
            case KimFeedConstants.READ_ALL_UNPROCESSED_ROWS :
                LOG.info("getEDWData: Preparing to get EDW data for only the loads that have not been read yet");
                getSQL += " AND LOAD_SEQ in (SELECT LOAD_SEQ from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR WHERE READ_BY_KFS = 'N')";
                break;
            case KimFeedConstants.READ_ROWS_FOR_LATEST_DELTA_ONLY :
                LOG.info("getEDWData: Preparing to get EDW data for only the most recent load, regardless of whether it has already been read");
                getSQL += " AND LOAD_SEQ = (SELECT MAX(LOAD_SEQ) from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR)";
                break;
            default :
                LOG.info("getEDWData: Preparing to get EDW data for the load with this date, regardless of whether it has already been read: "
                        + rowTimePeriodIndicator);
                getSQL += " AND LOAD_SEQ in (SELECT LOAD_SEQ from EDW.CU_PERSON_DATA_KFS_DELTA_MSTR WHERE FILE_DATE BETWEEN ? AND ?)";
                DateTime deltaLoadDate = getParsedDeltaLoadDate(rowTimePeriodIndicator);
                queryArgs = new Object[] { getTimestampForStartOfDay(deltaLoadDate), getTimestampForEndOfDay(deltaLoadDate) };
                break;
        }
        getSQL += " ORDER BY LOAD_SEQ, EMPLID";
        
        LOG.info("getEDWData: the generated SQL: " + getSQL);
        if (queryArgs != null) {
            for (Object arg : queryArgs) {
                LOG.info("getEDWData: query argument: " + arg);
            }
        }
        
        //Get data from the EDW
        LOG.info("getEDWData: getting edw data");
        //return serverTemplate.queryForRowSet(getSQL, queryArgs);
        return 0;
    }

    protected DateTime getParsedDeltaLoadDate(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(KimFeedConstants.DELTA_LOAD_DATE_PROPERTY_FORMAT);
        return formatter.parseDateTime(dateTimeString);
    }

    protected Timestamp getTimestampForStartOfDay(DateTime dateTime) {
        MutableDateTime startOfDay = new MutableDateTime(dateTime);
        startOfDay.setHourOfDay(0);
        startOfDay.setMinuteOfHour(0);
        startOfDay.setSecondOfMinute(0);
        return new Timestamp(startOfDay.getMillis());
    }

    protected Timestamp getTimestampForEndOfDay(DateTime dateTime) {
        MutableDateTime endOfDay = new MutableDateTime(dateTime);
        endOfDay.setHourOfDay(23);
        endOfDay.setMinuteOfHour(59);
        endOfDay.setSecondOfMinute(59);
        return new Timestamp(endOfDay.getMillis());
    }

    protected Integer runUpdatesAndReturnRowCount(ResultSet rs) throws SQLException, DataAccessException {
        int rowCount = 0;
        
        while (rs.next()) {
            rowCount++;
            KimFeedPersonDTO personDTO = getDataAndSanitize(rs);
            try {
                kimDataUpdateDao.processDataUpdate(personDTO);   
            } catch (Exception e) {
                LOG.error(buildMessageWithLineInfo("runUpdatesAndReturnRowCount: ERROR encountered while updating user", personDTO), e);
            }
        }
        
        return Integer.valueOf(rowCount);
    }

    protected KimFeedPersonDTO getDataAndSanitize(ResultSet rs) throws SQLException {
        KimFeedPersonDTO personDTO = new KimFeedPersonDTO();
        personDTO.CU_PERSON_SID = rs.getString(KimFeedConstants.CU_PERSON_SID_ROW);
        personDTO.NETID = rs.getString(KimFeedConstants.NETID_ROW);
        personDTO.EMPLID = rs.getString(KimFeedConstants.EMPLID_ROW);
        personDTO.FIRST_NAME = rs.getString(KimFeedConstants.FIRST_NAME_ROW);
        personDTO.LAST_NAME = rs.getString(KimFeedConstants.LAST_NAME_ROW);
        personDTO.MIDDLE_NAME = rs.getString(KimFeedConstants.MIDDLE_NAME_ROW);
        personDTO.NAME_SUFFIX = rs.getString(KimFeedConstants.NAME_SUFFIX_ROW);
        personDTO.HOME_ADDRESS1 = rs.getString(KimFeedConstants.HOME_ADDRESS1_ROW);
        personDTO.HOME_ADDRESS2 = rs.getString(KimFeedConstants.HOME_ADDRESS2_ROW);
        personDTO.HOME_ADDRESS3 = rs.getString(KimFeedConstants.HOME_ADDRESS3_ROW);
        personDTO.HOME_CITY = rs.getString(KimFeedConstants.HOME_CITY_ROW);
        personDTO.HOME_STATE = rs.getString(KimFeedConstants.HOME_STATE_ROW);
        personDTO.HOME_POSTAL = rs.getString(KimFeedConstants.HOME_POSTAL_ROW);
        personDTO.CAMPUS_ADDRESS1 = rs.getString(KimFeedConstants.CAMPUS_ADDRESS_ROW);
        personDTO.CAMPUS_CITY = rs.getString(KimFeedConstants.CAMPUS_CITY_ROW);
        personDTO.CAMPUS_STATE = rs.getString(KimFeedConstants.CAMPUS_STATE_ROW);
        personDTO.CAMPUS_POSTAL = rs.getString(KimFeedConstants.CAMPUS_POSTAL_ROW);
        personDTO.EMAIL_ADDRESS = rs.getString(KimFeedConstants.EMAIL_ADDRESS_ROW);
        //personDTO.HOME_PHONE = rs.getString("HOME_PHONE");
        personDTO.CAMPUS_PHONE = rs.getString(KimFeedConstants.CAMPUS_PHONE_ROW);
        personDTO.ACADEMIC = rs.getString(KimFeedConstants.ACADEMIC_ROW).charAt(0);
        personDTO.FACULTY = rs.getString(KimFeedConstants.FACULTY_ROW).charAt(0);
        personDTO.AFFILIATE = rs.getString(KimFeedConstants.AFFILIATE_ROW).charAt(0);
        personDTO.DCEXP1 = rs.getString(KimFeedConstants.DCEXP1_ROW).charAt(0);
        personDTO.STAFF = rs.getString(KimFeedConstants.STAFF_ROW).charAt(0);
        personDTO.STUDENT = rs.getString(KimFeedConstants.STUDENT_ROW).charAt(0);
        personDTO.ALUMNI = rs.getString(KimFeedConstants.ALUMNI_ROW).charAt(0);
        personDTO.PRIMARY_ORG_CODE = KimFeedConstants.IT_PREFIX + rs.getString(KimFeedConstants.PRIMARY_ORG_CODE_ROW);
        personDTO.PRIMARY_AFFILIATION_VALUE = rs.getString(KimFeedConstants.PRIMARY_AFFILIATION_ROW).charAt(0);
        personDTO.PRIMARY_AFFILIATION_ACADEMIC = (personDTO.PRIMARY_AFFILIATION_VALUE == 'A') ? KimFeedConstants.YES_VAL : KimFeedConstants.NO_VAL;
        personDTO.PRIMARY_AFFILIATION_FACULTY = (personDTO.PRIMARY_AFFILIATION_VALUE == 'F') ? KimFeedConstants.YES_VAL : KimFeedConstants.NO_VAL;
        personDTO.PRIMARY_AFFILIATION_AFFILIATE = (personDTO.PRIMARY_AFFILIATION_VALUE == 'I') ? KimFeedConstants.YES_VAL : KimFeedConstants.NO_VAL;
        personDTO.PRIMARY_AFFILIATION_EXCEPTION = (personDTO.PRIMARY_AFFILIATION_VALUE == 'X') ? KimFeedConstants.YES_VAL : KimFeedConstants.NO_VAL;
        personDTO.PRIMARY_AFFILIATION_STAFF = (personDTO.PRIMARY_AFFILIATION_VALUE == 'E') ? KimFeedConstants.YES_VAL : KimFeedConstants.NO_VAL;
        personDTO.PRIMARY_AFFILIATION_STUDENT = (personDTO.PRIMARY_AFFILIATION_VALUE == 'S') ? KimFeedConstants.YES_VAL : KimFeedConstants.NO_VAL;
        personDTO.PRIMARY_AFFILIATION_ALUMNI = (personDTO.PRIMARY_AFFILIATION_VALUE == 'L') ? KimFeedConstants.YES_VAL : KimFeedConstants.NO_VAL;
        
        // Truncate "home" address lines, and move truncated characters to subsequent blank lines as needed.
        if (StringUtils.isNotEmpty(personDTO.HOME_ADDRESS1) && personDTO.HOME_ADDRESS1.length() > 40) {
            if (StringUtils.isBlank(personDTO.HOME_ADDRESS2)) {
                personDTO.HOME_ADDRESS2 = personDTO.HOME_ADDRESS1.substring(40);
            }
            personDTO.HOME_ADDRESS1 = personDTO.HOME_ADDRESS1.substring(0, 40);
        }
        if (StringUtils.isNotEmpty(personDTO.HOME_ADDRESS2) && personDTO.HOME_ADDRESS2.length() > 40) {
            if (StringUtils.isBlank(personDTO.HOME_ADDRESS3)) {
                personDTO.HOME_ADDRESS3 = personDTO.HOME_ADDRESS2.substring(40);
            }
            personDTO.HOME_ADDRESS2 = personDTO.HOME_ADDRESS2.substring(0, 40);
        }
        if (StringUtils.isNotEmpty(personDTO.HOME_ADDRESS3) && personDTO.HOME_ADDRESS3.length() > 40) {
            personDTO.HOME_ADDRESS3 = personDTO.HOME_ADDRESS3.substring(0, 40);
        }
        
        // Truncate "campus" address lines, and move truncated characters to subsequent blank lines as needed.
        if (StringUtils.isNotEmpty(personDTO.CAMPUS_ADDRESS1) && personDTO.CAMPUS_ADDRESS1.length() > 40) {
            if (personDTO.CAMPUS_ADDRESS1.length() > 80) {
                personDTO.CAMPUS_ADDRESS2 = personDTO.CAMPUS_ADDRESS1.substring(40, 80);
                personDTO.CAMPUS_ADDRESS3 = (personDTO.CAMPUS_ADDRESS1.length() > 120) ? personDTO.CAMPUS_ADDRESS1.substring(80, 120) : personDTO.CAMPUS_ADDRESS1.substring(80);
            } else {
                personDTO.CAMPUS_ADDRESS2 = personDTO.CAMPUS_ADDRESS1.substring(40);
                personDTO.CAMPUS_ADDRESS3 = KimFeedConstants.BLANK_ADDRESS_LINE;
            }
            personDTO.CAMPUS_ADDRESS1 = personDTO.CAMPUS_ADDRESS1.substring(0, 40);
        } else {
            personDTO.CAMPUS_ADDRESS2 = KimFeedConstants.BLANK_ADDRESS_LINE;
            personDTO.CAMPUS_ADDRESS3 = KimFeedConstants.BLANK_ADDRESS_LINE;
        }
        
        // Determine primary employment info, which requires special handling if the primary affiliation is Academic, Student, Exception, or Alumni,
        // since those four affiliation types are not supposed to have employment info.
        switch (personDTO.PRIMARY_AFFILIATION_VALUE) {
            case 'A' :
            case 'L' :
            case 'S' :
            case 'X' :
                personDTO.PRIMARY_EMPLOYMENT_FACULTY = KimFeedConstants.NO_VAL;
                personDTO.PRIMARY_EMPLOYMENT_AFFILIATE = KimFeedConstants.NO_VAL;
                personDTO.PRIMARY_EMPLOYMENT_STAFF = KimFeedConstants.NO_VAL;
                // If necessary, set a Faculty, Staff, or Affiliate as having the primary employment info.
                // Active employment infos take precedence over inactive ones, and inactive ones take precedence over retired ones.
                boolean foundPrimaryEmployment = false;
                for (int i = 0; !foundPrimaryEmployment && i < KimFeedConstants.EMP_STAT_CHARS.length; i++) {
                    if (personDTO.FACULTY == KimFeedConstants.EMP_STAT_CHARS[i]) {
                        personDTO.PRIMARY_EMPLOYMENT_FACULTY = KimFeedConstants.YES_VAL;
                        foundPrimaryEmployment = true;
                    } else if (personDTO.STAFF == KimFeedConstants.EMP_STAT_CHARS[i]) {
                        personDTO.PRIMARY_EMPLOYMENT_STAFF = KimFeedConstants.YES_VAL;
                        foundPrimaryEmployment = true;
                    } else if (personDTO.AFFILIATE == KimFeedConstants.EMP_STAT_CHARS[i]) {
                        personDTO.PRIMARY_EMPLOYMENT_AFFILIATE = KimFeedConstants.YES_VAL;
                        foundPrimaryEmployment = true;
                    }
                }
                break;
            default :
                personDTO.PRIMARY_EMPLOYMENT_FACULTY = personDTO.PRIMARY_AFFILIATION_FACULTY;
                personDTO.PRIMARY_EMPLOYMENT_AFFILIATE = personDTO.PRIMARY_AFFILIATION_AFFILIATE;
                personDTO.PRIMARY_EMPLOYMENT_STAFF = personDTO.PRIMARY_AFFILIATION_STAFF;
                break;
        }
        
        personDTO.LDAP_SUPPRESS = rs.getString(KimFeedConstants.LDAP_SUPPRESS_ROW);
        personDTO.ACTIVE = KimFeedConstants.YES_VAL; //rs.getString("ACTIVE");
        return personDTO;
    }

    protected String buildMessageWithLineInfo(String baseMessage, KimFeedPersonDTO personDTO) {
        return String.format(KimFeedConstants.LINE_INFO_MESSAGE_FORMAT, baseMessage, personDTO.NETID, personDTO.CU_PERSON_SID, personDTO.EMPLID);
    }

    public void setKimDataUpdateDao(KimDataUpdateDao kimDataUpdateDao) {
        this.kimDataUpdateDao = kimDataUpdateDao;
    }

    public void setRowTimePeriodIndicator(String rowTimePeriodIndicator) {
        this.rowTimePeriodIndicator = rowTimePeriodIndicator;
    }

    public void setMarkDeltaRowsAsReadWhenFinished(boolean markDeltaRowsAsReadWhenFinished) {
        this.markDeltaRowsAsReadWhenFinished = markDeltaRowsAsReadWhenFinished;
    }

}
