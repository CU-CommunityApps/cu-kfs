package edu.cornell.kfs.kim.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.kim.batch.dataaccess.KimFeedEdwDao;
import edu.cornell.kfs.kim.businessobject.EdwPerson;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;

public class KimFeedEdwDaoJdbc extends PlatformAwareDaoBaseJdbc implements KimFeedEdwDao {

    private static final Logger LOG = LogManager.getLogger();

    private enum EdwColumn {
        CU_PERSON_SID("CU_PERSON_SID", EdwPerson::setCuPersonId),
        EMPLID("EMPLID", EdwPerson::setEmployeeId),
        NETID("NETID", EdwPerson::setNetId),
        NATIONAL_ID("NATIONAL_ID", EdwPerson::setNationalId),
        ACADEMIC("ACADEMIC", EdwPerson::setAcademicAffil),
        STAFF("STAFF", EdwPerson::setStaffAffil),
        FACULTY("FACULTY", EdwPerson::setFacultyAffil),
        STUDENT("STUDENT", EdwPerson::setStudentAffil),
        ALUMNI("ALUMNI", EdwPerson::setAlumniAffil),
        AFFILIATE("AFFILIATE", EdwPerson::setAffiliateAffil),
        EXCEPTION_DCEXP1("EXCEPTION", "dcexp1", EdwPerson::setExceptionAffil),
        PRIMARY_AFFILIATION("PRIMARY_AFFILIATION", EdwPerson::setPrimaryAffiliation),
        CU_SUPPRESS_ADDR("CU_SUPPRESS_ADDR", EdwPerson::setSuppressAddress),
        LDAP_SUPPRESS("LDAP_SUPPRESS", EdwPerson::setLdapSuppress),
        NAME("NAME", EdwPerson::setName),
        NAME_PREFIX("NAME_PREFIX", EdwPerson::setNamePrefix),
        NAME_SUFFIX("NAME_SUFFIX", EdwPerson::setNameSuffix),
        LAST_NAME("LAST_NAME", EdwPerson::setLastName),
        FIRST_NAME("FIRST_NAME", EdwPerson::setFirstName),
        MIDDLE_NAME("MIDDLE_NAME", EdwPerson::setMiddleName),
        PREF_NAME("PREF_NAME", EdwPerson::setPreferredName),
        HOME_ADDRESS1("HOME_ADDRESS1", EdwPerson::setHomeAddressLine1),
        HOME_ADDRESS2("HOME_ADDRESS2", EdwPerson::setHomeAddressLine2),
        HOME_ADDRESS3("HOME_ADDRESS3", EdwPerson::setHomeAddressLine3),
        HOME_CITY("HOME_CITY", EdwPerson::setHomeCity),
        HOME_STATE("HOME_STATE", EdwPerson::setHomeState),
        HOME_POSTAL("HOME_POSTAL", EdwPerson::setHomePostalCode),
        HOME_COUNTRY("HOME_COUNTRY", EdwPerson::setHomeCountryCode),
        HOME_PHONE("HOME_PHONE", EdwPerson::setHomePhone),
        EMAIL_ADDRESS("EMAIL_ADDRESS", EdwPerson::setEmailAddress),
        CAMPUS_ADDRESS("CAMPUS_ADDRESS", EdwPerson::setCampusAddress),
        CAMPUS_CITY("CAMPUS_CITY", EdwPerson::setCampusCity),
        CAMPUS_STATE("CAMPUS_STATE", EdwPerson::setCampusState),
        CAMPUS_POSTAL("CAMPUS_POSTAL", EdwPerson::setCampusPostalCode),
        CAMPUS_PHONE("CAMPUS_PHONE", EdwPerson::setCampusPhone),
        PRIMARY_JOBCODE("PRIMARY_JOBCODE", EdwPerson::setPrimaryJobCode),
        PRIMARY_DEPTID("PRIMARY_DEPTID", EdwPerson::setPrimaryDepartmentId),
        PRIMARY_UNITID("PRIMARY_UNITID", EdwPerson::setPrimaryUnitId),
        PRIMARY_ORG_CODE("PRIMARY_ORG_CODE", EdwPerson::setPrimaryOrgCode),
        PRIMARY_EMPL_STATUS("PRIMARY_EMPL_STATUS", EdwPerson::setPrimaryEmploymentStatus),
        ACTIVE("ACTIVE", EdwPerson::setActive);
        
        private final String columnName;
        private final String columnAlias;
        private final BiConsumer<EdwPerson, String> pojoPropertySetter;
        
        private EdwColumn(String columnName, BiConsumer<EdwPerson, String> pojoPropertySetter) {
            this(columnName, null, pojoPropertySetter);
        }
        
        private EdwColumn(String columnName, String columnAlias, BiConsumer<EdwPerson, String> pojoPropertySetter) {
            this.columnName = columnName;
            this.columnAlias = columnAlias;
            this.pojoPropertySetter = pojoPropertySetter;
        }
        
        @Override
        public String toString() {
            return getColumnSelector();
        }
        
        private String getColumnSelector() {
            return StringUtils.isNotBlank(columnAlias)
                    ? columnName + KFSConstants.BLANK_SPACE + columnAlias
                    : columnName;
        }
        
        private String getLabel() {
            return StringUtils.defaultIfBlank(columnAlias, columnName);
        }
    }

    private ParameterService parameterService;
    private DateTimeService dateTimeService;

    @Override
    public Stream<EdwPerson> getEdwDataAsCloseableStream() {
        CuSqlQuery query = buildEdwDeltaLoadQuery();
        return getJdbcTemplate().queryForStream(
                query.getQueryString(), this::mapToPersonDto, query.getParametersArray());
    }

    private CuSqlQuery buildEdwDeltaLoadQuery() {
        String deltasToLoad = parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.KFS, KfsParameterConstants.BATCH_COMPONENT,
                CUKFSParameterKeyConstants.KIM_FEED_DELTAS_TO_LOAD);
        CuSqlChunk query = CuSqlChunk.of(
                "SELECT ", StringUtils.join(EdwColumn.values(), CUKFSConstants.COMMA_AND_SPACE),
                " FROM EDW.CU_PERSON_DATA_KFS_DELTA P ",
                "WHERE NETID != ' ' AND CU_PERSON_SID = CU_MASTER_PERSON_SID "
        );

        if (StringUtils.isBlank(deltasToLoad)) {
            throw new IllegalStateException(CUKFSParameterKeyConstants.KIM_FEED_DELTAS_TO_LOAD
                    + " parameter cannot be blank");
        } else if (StringUtils.equals(CUKFSConstants.KimFeedConstants.ALL_UNPROCESSED_DELTAS_MODE, deltasToLoad)) {
            query.append("AND LOAD_SEQ IN (SELECT LOAD_SEQ FROM EDW.CU_PERSON_DATA_KFS_DELTA_MSTR ")
                    .append("WHERE READ_BY_KFS = 'N')");
        } else if (StringUtils.equals(CUKFSConstants.KimFeedConstants.LATEST_DATE_ONLY_MODE, deltasToLoad)) {
            query.append("AND LOAD_SEQ = (SELECT MAX(LOAD_SEQ) FROM EDW.CU_PERSON_DATA_KFS_DELTA_MSTR)");
        } else {
            Date deltaLoadDate = parseDeltaLoadDate(deltasToLoad);
            Date deltaDateRangeStart = dateTimeService.getUtilDateAtStartOfDay(deltaLoadDate);
            Date deltaDateRangeEnd = dateTimeService.getUtilDateAtEndOfDay(deltaLoadDate);
            query.append("AND LOAD_SEQ IN (SELECT LOAD_SEQ FROM EDW.CU_PERSON_DATA_KFS_DELTA_MSTR ")
                    .append("WHERE FILE_DATE BETWEEN ")
                    .appendAsParameter(Types.TIMESTAMP, new Timestamp(deltaDateRangeStart.getTime()))
                    .append(" AND ")
                    .appendAsParameter(Types.TIMESTAMP, new Timestamp(deltaDateRangeEnd.getTime()))
                    .append(")");
        }

        CuSqlQuery fullQuery = query.toQuery();
        LOG.info("buildEdwDeltaLoadQuery, Query string: {}", fullQuery.getQueryString());
        LOG.info("buildEdwDeltaLoadQuery, Query parameters: {}", fullQuery.getParameters());
        return fullQuery;
    }

    private Date parseDeltaLoadDate(String deltaLoadDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT);
        try {
            return dateFormat.parse(deltaLoadDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private EdwPerson mapToPersonDto(ResultSet rs, int rowNum) throws SQLException {
        LOG.info("mapToPersonDto, Generating DTO for result row index {}", rowNum);
        EdwPerson edwPerson = new EdwPerson();
        for (EdwColumn edwColumn : EdwColumn.values()) {
            String columnValue = rs.getString(edwColumn.getLabel());
            edwColumn.pojoPropertySetter.accept(edwPerson, columnValue);
        }
        return edwPerson;
    }

    @Override
    public List<String> getIdsOfPersonsToMarkAsDisabled() {
        return getJdbcTemplate().queryForList("SELECT CU_PERSON_SID FROM EDW.CU_PERSON_DATA_KFS_DELTA "
                + "WHERE PROCESS_FLAG = 'D' AND LOAD_SEQ = (SELECT MAX(LOAD_SEQ) FROM EDW.CU_PERSON_DATA_KFS_DELTA)",
                String.class);
    }

    @Override
    public int markEdwDataAsRead() {
        return getJdbcTemplate().update("UPDATE EDW.CU_PERSON_DATA_KFS_DELTA_MSTR SET READ_BY_KFS = 'Y'");
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
