package edu.cornell.kfs.sys.dataaccess.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import edu.cornell.kfs.sys.dataaccess.KimDataUpdateDao;
import edu.cornell.kfs.sys.dataaccess.KimFeedConstants;
import edu.cornell.kfs.sys.dataaccess.dto.KimFeedPersonDTO;

public class KimDataUpdateDaoJdbc extends PlatformAwareDaoBaseJdbc implements KimDataUpdateDao {

    private static final Logger LOG = LogManager.getLogger(KimDataUpdateDaoJdbc.class);

    @Override
    public void checkDataSourceStatus() {
        // Do nothing?
    }

    @Override
    public void processDataUpdate(KimFeedPersonDTO personDTO) {
         if (getJdbcTemplate().queryForObject(KimFeedConstants.SELECT_ENTITY_COUNT_SQL, Integer.class, personDTO.CU_PERSON_SID) > 0) {
             updateExistingUser(personDTO);
         } else {
             createNewUser(personDTO);
         }
    }

    protected void updateExistingUser(KimFeedPersonDTO personDTO) {
        if (userHasNoPrincipalDataUnderExpectedEntityId(personDTO)) {
            throw new IllegalStateException("Could not update user because a matching current principal record does not exist");
        }
        
        // Update principal record.
        getJdbcTemplate().update(KimFeedConstants.UPDATE_PRINCIPAL_SQL, personDTO.NETID, personDTO.CU_PERSON_SID);
        
        // Update home address.
        getJdbcTemplate().update(KimFeedConstants.UPDATE_HOME_ADDRESS_SQL,
                personDTO.HOME_ADDRESS1, personDTO.HOME_ADDRESS2, personDTO.HOME_ADDRESS3, personDTO.HOME_CITY, personDTO.HOME_STATE, personDTO.HOME_POSTAL, personDTO.CU_PERSON_SID);
        
        // Update campus address.
        getJdbcTemplate().update(KimFeedConstants.UPDATE_CAMPUS_ADDRESS_SQL,
                personDTO.CAMPUS_ADDRESS1, personDTO.CAMPUS_ADDRESS2, personDTO.CAMPUS_ADDRESS3, personDTO.CAMPUS_CITY, personDTO.CAMPUS_STATE, personDTO.CAMPUS_POSTAL, personDTO.CU_PERSON_SID);
        
        // Update campus phone number.
        getJdbcTemplate().update(KimFeedConstants.UPDATE_CAMPUS_PHONE_SQL, personDTO.CAMPUS_PHONE, personDTO.CU_PERSON_SID);
        
        // Update employee ID and person's name.
        getJdbcTemplate().update(KimFeedConstants.UPDATE_EMPLID_SQL, personDTO.EMPLID, personDTO.CU_PERSON_SID);
        getJdbcTemplate().update(KimFeedConstants.UPDATE_NAME_SQL, personDTO.FIRST_NAME, personDTO.MIDDLE_NAME, personDTO.LAST_NAME, personDTO.NAME_SUFFIX, personDTO.CU_PERSON_SID);
        
        //Adjust Affiliations
        
        SqlRowSet affilRS = getJdbcTemplate().queryForRowSet(KimFeedConstants.SELECT_AFFIL_TYPE_SQL, personDTO.CU_PERSON_SID);
        List<String> affils = new ArrayList<String>();
        
        // Determine which affiliations the user already has in KIM.
        while (affilRS.next()) {
            affils.add(affilRS.getString(KimFeedConstants.AFLTN_TYP_CD_ROW));
        }
        
        // Create or update ACADEMIC affiliation (and delete any existing associated employment info) as needed.
        if (affils.contains(KimFeedConstants.ACADEMIC_AFFIL_CONST)) {
            getJdbcTemplate().update(KimFeedConstants.UPDATE_AFFIL_SQL, personDTO.PRIMARY_AFFILIATION_ACADEMIC, personDTO.CU_PERSON_SID, KimFeedConstants.ACADEMIC_AFFIL_CONST);
            getJdbcTemplate().update(KimFeedConstants.DELETE_EMP_INFO_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.ACADEMIC_AFFIL_CONST);
        } else {
            switch (personDTO.ACADEMIC) {
                case KimFeedConstants.IS_ACTIVE_AFFIL :
                case KimFeedConstants.IS_INACTIVE_AFFIL :
                case KimFeedConstants.IS_RETIRED_AFFIL :
                    getJdbcTemplate().update(KimFeedConstants.INSERT_AFFIL_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.ACADEMIC_AFFIL_CONST, personDTO.PRIMARY_AFFILIATION_ACADEMIC);
                    break;
            }
        }
        
        // Create or update ALUMNI affiliation (and delete any existing associated employment info) as needed.
        if (affils.contains(KimFeedConstants.ALUMNI_AFFIL_CONST)) {
            getJdbcTemplate().update(KimFeedConstants.UPDATE_AFFIL_SQL, personDTO.PRIMARY_AFFILIATION_ALUMNI, personDTO.CU_PERSON_SID, KimFeedConstants.ALUMNI_AFFIL_CONST);
            getJdbcTemplate().update(KimFeedConstants.DELETE_EMP_INFO_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.ALUMNI_AFFIL_CONST);
        } else {
            switch (personDTO.ALUMNI) {
                case KimFeedConstants.IS_ACTIVE_AFFIL :
                case KimFeedConstants.IS_INACTIVE_AFFIL :
                case KimFeedConstants.IS_RETIRED_AFFIL :
                    getJdbcTemplate().update(KimFeedConstants.INSERT_AFFIL_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.ALUMNI_AFFIL_CONST, personDTO.PRIMARY_AFFILIATION_ALUMNI);
                    break;
            }
        }
        
        // Create or update FCLTY (Faculty) affiliation and employment info as needed.
        if (affils.contains(KimFeedConstants.FACULTY_AFFIL_CONST)) {
            getJdbcTemplate().update(KimFeedConstants.UPDATE_AFFIL_SQL, personDTO.PRIMARY_AFFILIATION_FACULTY, personDTO.CU_PERSON_SID, KimFeedConstants.FACULTY_AFFIL_CONST);
            getJdbcTemplate().update(KimFeedConstants.UPDATE_EMP_INFO_SQL,
                    (personDTO.FACULTY != KimFeedConstants.IS_NONEXISTENT_AFFIL) ? String.valueOf(personDTO.FACULTY) : KimFeedConstants.INACTIVE_VAL,
                            personDTO.PRIMARY_EMPLOYMENT_FACULTY, KimFeedConstants.FACULTY_AFFIL_CONST, personDTO.CU_PERSON_SID, KimFeedConstants.FACULTY_AFFIL_CONST);
        } else {
            switch (personDTO.FACULTY) {
                case KimFeedConstants.IS_ACTIVE_AFFIL :
                case KimFeedConstants.IS_INACTIVE_AFFIL :
                case KimFeedConstants.IS_RETIRED_AFFIL :
                    getJdbcTemplate().update(KimFeedConstants.INSERT_AFFIL_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.FACULTY_AFFIL_CONST, personDTO.PRIMARY_AFFILIATION_FACULTY);
                    getJdbcTemplate().update(KimFeedConstants.INSERT_EMP_INFO_SQL,
                            personDTO.CU_PERSON_SID, String.valueOf(personDTO.FACULTY), personDTO.PRIMARY_EMPLOYMENT_FACULTY, personDTO.PRIMARY_ORG_CODE, personDTO.EMPLID, KimFeedConstants.FACULTY_AFFIL_CONST);
                    break;
            }
        }
        
        // Create or update AFLT (Affiliate) affiliation and employment info as needed.
        if (affils.contains(KimFeedConstants.AFFILIATE_AFFIL_CONST)) {
            getJdbcTemplate().update(KimFeedConstants.UPDATE_AFFIL_SQL, personDTO.PRIMARY_AFFILIATION_AFFILIATE, personDTO.CU_PERSON_SID, KimFeedConstants.AFFILIATE_AFFIL_CONST);
            getJdbcTemplate().update(KimFeedConstants.UPDATE_EMP_INFO_SQL,
                    (personDTO.AFFILIATE != KimFeedConstants.IS_NONEXISTENT_AFFIL) ? String.valueOf(personDTO.AFFILIATE) : KimFeedConstants.INACTIVE_VAL,
                            personDTO.PRIMARY_EMPLOYMENT_AFFILIATE, KimFeedConstants.AFFILIATE_AFFIL_CONST, personDTO.CU_PERSON_SID, KimFeedConstants.AFFILIATE_AFFIL_CONST);
        } else {
            switch (personDTO.AFFILIATE) {
                case KimFeedConstants.IS_ACTIVE_AFFIL :
                case KimFeedConstants.IS_INACTIVE_AFFIL :
                case KimFeedConstants.IS_RETIRED_AFFIL :
                    getJdbcTemplate().update(KimFeedConstants.INSERT_AFFIL_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.AFFILIATE_AFFIL_CONST, personDTO.PRIMARY_AFFILIATION_AFFILIATE);
                    getJdbcTemplate().update(KimFeedConstants.INSERT_EMP_INFO_SQL,
                            personDTO.CU_PERSON_SID, String.valueOf(personDTO.AFFILIATE), personDTO.PRIMARY_EMPLOYMENT_AFFILIATE, personDTO.PRIMARY_ORG_CODE, personDTO.EMPLID, KimFeedConstants.AFFILIATE_AFFIL_CONST);
                    break;
            }
        }
        
        // Create or update EXCPTN (Exception) affiliation (and delete any existing associated employment info) as needed.
        if (affils.contains(KimFeedConstants.EXCEPTION_AFFIL_CONST)) {
            getJdbcTemplate().update(KimFeedConstants.UPDATE_AFFIL_SQL, personDTO.PRIMARY_AFFILIATION_EXCEPTION, personDTO.CU_PERSON_SID, KimFeedConstants.EXCEPTION_AFFIL_CONST);
            getJdbcTemplate().update(KimFeedConstants.DELETE_EMP_INFO_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.EXCEPTION_AFFIL_CONST);
        } else {
            switch (personDTO.DCEXP1) {
                case KimFeedConstants.IS_ACTIVE_AFFIL :
                case KimFeedConstants.IS_INACTIVE_AFFIL :
                case KimFeedConstants.IS_RETIRED_AFFIL :
                    getJdbcTemplate().update(KimFeedConstants.INSERT_AFFIL_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.EXCEPTION_AFFIL_CONST, personDTO.PRIMARY_AFFILIATION_EXCEPTION);
                    break;
            }
        }
        
        // Create or update STAFF affiliation and employment info as needed.
        if (affils.contains(KimFeedConstants.STAFF_AFFIL_CONST)) {
            getJdbcTemplate().update(KimFeedConstants.UPDATE_AFFIL_SQL, personDTO.PRIMARY_AFFILIATION_STAFF, personDTO.CU_PERSON_SID, KimFeedConstants.STAFF_AFFIL_CONST);
            getJdbcTemplate().update(KimFeedConstants.UPDATE_EMP_INFO_SQL, (personDTO.STAFF != KimFeedConstants.IS_NONEXISTENT_AFFIL) ? String.valueOf(personDTO.STAFF) : KimFeedConstants.INACTIVE_VAL,
                    personDTO.PRIMARY_EMPLOYMENT_STAFF, KimFeedConstants.STAFF_AFFIL_CONST, personDTO.CU_PERSON_SID, KimFeedConstants.STAFF_AFFIL_CONST);
        } else {
            switch (personDTO.STAFF) {
                case KimFeedConstants.IS_ACTIVE_AFFIL :
                case KimFeedConstants.IS_INACTIVE_AFFIL :
                case KimFeedConstants.IS_RETIRED_AFFIL :
                    getJdbcTemplate().update(KimFeedConstants.INSERT_AFFIL_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.STAFF_AFFIL_CONST, personDTO.PRIMARY_AFFILIATION_STAFF);
                    getJdbcTemplate().update(KimFeedConstants.INSERT_EMP_INFO_SQL,
                            personDTO.CU_PERSON_SID, String.valueOf(personDTO.STAFF), personDTO.PRIMARY_EMPLOYMENT_STAFF, personDTO.PRIMARY_ORG_CODE, personDTO.EMPLID, KimFeedConstants.STAFF_AFFIL_CONST);
                    break;
            }
        }
        
        // Create or update STDNT (Student) affiliation (and delete any existing associated employment info) as needed.
        if (affils.contains(KimFeedConstants.STUDENT_AFFIL_CONST)) {
            getJdbcTemplate().update(KimFeedConstants.UPDATE_AFFIL_SQL, personDTO.PRIMARY_AFFILIATION_STUDENT, personDTO.CU_PERSON_SID, KimFeedConstants.STUDENT_AFFIL_CONST);
            getJdbcTemplate().update(KimFeedConstants.DELETE_EMP_INFO_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.STUDENT_AFFIL_CONST);
        } else {
            switch (personDTO.STUDENT) {
                case KimFeedConstants.IS_ACTIVE_AFFIL :
                case KimFeedConstants.IS_INACTIVE_AFFIL :
                case KimFeedConstants.IS_RETIRED_AFFIL :
                    getJdbcTemplate().update(KimFeedConstants.INSERT_AFFIL_SQL, personDTO.CU_PERSON_SID, KimFeedConstants.STUDENT_AFFIL_CONST, personDTO.PRIMARY_AFFILIATION_STUDENT);
                    break;
            }
        }
        
        // Update the department code and employee ID on all of the person's employment info records.
        getJdbcTemplate().update(KimFeedConstants.UPDATE_EMP_INFO_CD_AND_ID_SQL, personDTO.PRIMARY_ORG_CODE, personDTO.EMPLID, personDTO.CU_PERSON_SID);
        
        // Create or update the person's email address as needed.
        if (!personDTO.EMAIL_ADDRESS.equals(KimFeedConstants.SINGLE_SPACE) ) {
            if (getJdbcTemplate().queryForObject(KimFeedConstants.SELECT_EMAIL_COUNT_SQL, Integer.class, personDTO.CU_PERSON_SID) > 0) {
                getJdbcTemplate().update(KimFeedConstants.UPDATE_EMAIL_SQL, personDTO.EMAIL_ADDRESS, personDTO.CU_PERSON_SID);
            } else {
                getJdbcTemplate().update(KimFeedConstants.INSERT_EMAIL_ADDRESS_SQL, personDTO.CU_PERSON_SID, personDTO.EMAIL_ADDRESS, personDTO.ACTIVE);
            }
        }
        
        // Update the person's KIM privacy preferences based on their LDAP privacy preferences.
        if (personDTO.LDAP_SUPPRESS.equals(KimFeedConstants.YES_VAL)) {
            getJdbcTemplate().update(KimFeedConstants.UPDATE_PRIV_PREF_SQL, personDTO.CU_PERSON_SID);
        }
        
        LOG.info(buildMessageWithLineInfo("updateExistingUser: Successfully updated existing user", personDTO));
    }

    protected void createNewUser(KimFeedPersonDTO personDTO) {
        
    }

    protected boolean userHasNoPrincipalDataUnderExpectedEntityId(KimFeedPersonDTO personDTO) {
        int matchingPrincipalCount = getJdbcTemplate().queryForObject(
                KimFeedConstants.SELECT_MATCHING_PRINCIPAL_COUNT_SQL, Integer.class, personDTO.NETID, personDTO.CU_PERSON_SID);
        return matchingPrincipalCount == 0;
    }

    protected boolean userHasPrincipalDataUnderDifferentEntityId(KimFeedPersonDTO personDTO) {
        int conflictingPrincipalCount = getJdbcTemplate().queryForObject(
                KimFeedConstants.SELECT_CONFLICTING_PRINCIPAL_COUNT_SQL, Integer.class, personDTO.NETID, personDTO.CU_PERSON_SID);
        return conflictingPrincipalCount > 0;
    }

    protected String buildMessageWithLineInfo(String baseMessage, KimFeedPersonDTO personDTO) {
        return String.format(KimFeedConstants.LINE_INFO_MESSAGE_FORMAT, baseMessage, personDTO.NETID, personDTO.CU_PERSON_SID, personDTO.EMPLID);
    }

    @Override
    public void markPersonRecordsAsDisabled(List<String> principalIds) {
        Object[] queryArgs = principalIds.toArray();
        String argPlaceholders = principalIds.stream()
                .map(principalId -> "?")
                .collect(Collectors.joining(", "));
        getJdbcTemplate().update("UPDATE CYNERGY.KRIM_PRNCPL_T SET PRNCPL_NM = 'DIS-' || PRNCPL_NM WHERE PRNCPL_ID IN (" + argPlaceholders + ") " +
                                "AND PRNCPL_NM NOT LIKE 'DIS-%'", queryArgs);
    }

}
