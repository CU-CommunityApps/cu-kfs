package edu.cornell.kfs.sys.batch.dataaccess.impl;

import java.text.MessageFormat;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.kuali.kfs.sys.KFSConstants;
import edu.cornell.kfs.sys.batch.dataaccess.KimFeedConstants;
import edu.cornell.kfs.sys.batch.dataaccess.impl.SchemaDao;

public class KrimEntityAddressTableJdbc extends SchemaDao {
    private static final Logger LOG = LogManager.getLogger(KrimEntityAddressTableJdbc.class);
    
//SQL from class CynergyKimFeed
//    private static final String UPDATE_HOME_ADDRESS_SQL = "UPDATE KRIM_ENTITY_ADDR_T SET ADDR_LINE_1 = ?, ADDR_LINE_2 = ?, ADDR_LINE_3 = ?, CITY = ?, STATE_PVC_CD = SubStr(?,1,2), POSTAL_CD = ? WHERE ENTITY_ID = ? AND ADDR_TYP_CD = 'HM'";
//
//    private static final String UPDATE_CAMPUS_ADDRESS_SQL = "UPDATE KRIM_ENTITY_ADDR_T SET ADDR_LINE_1 = ?, ADDR_LINE_2 = ?, ADDR_LINE_3 = ?, CITY = ?, STATE_PVC_CD = SubStr(?,1,2), POSTAL_CD = ? WHERE ENTITY_ID = ? AND ADDR_TYP_CD = 'CMP'";
//
//    private static final String INSERT_HOME_ADDRESS_SQL = 
//            "Insert Into KRIM_ENTITY_ADDR_T "
//            + "(ENTITY_ADDR_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, ADDR_TYP_CD, ADDR_LINE_1, ADDR_LINE_2, ADDR_LINE_3, CITY, STATE_PVC_CD, POSTAL_CD, POSTAL_CNTRY_CD, DFLT_IND, ACTV_IND, LAST_UPDT_DT) "
//            + "Values ( To_Char(KRIM_ENTITY_ADDR_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'PERSON', 'HM', ?, ?, ?, ?, SubStr(?,1,2), ?, ' ', 'Y', ?, SYSDATE)";
//
//    private static final String INSERT_CAMPUS_ADDRESS_SQL = 
//            "Insert Into KRIM_ENTITY_ADDR_T "
//            + "(ENTITY_ADDR_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, ADDR_TYP_CD, ADDR_LINE_1, ADDR_LINE_2, ADDR_LINE_3, CITY, STATE_PVC_CD, POSTAL_CD, POSTAL_CNTRY_CD, DFLT_IND, ACTV_IND, LAST_UPDT_DT) "
//            + "Values (To_Char( KRIM_ENTITY_ADDR_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'PERSON', 'CMP', ?, ?, ?, ?, SubStr(?,1,2), ?, ' ', 'N', ?, SYSDATE)";
//
//    JdbcTemplate destTemplate.update("Truncate Table KRIM_ENTITY_ADDR_T Drop Storage");
    
    //NOTE: Current CynergyKimFeed SQL does not set LAST_UPDT_DT or MOD_DT to SYSDATE when update is performed.....should either of those values be updated as well??
    private static final String UPDATE_ADDRESS_SQL = "UPDATE {0}.{1} SET ADDR_LINE_1 = ?, ADDR_LINE_2 = ?, ADDR_LINE_3 = ?, CITY = ?, STATE_PVC_CD = SubStr(?,1,2), POSTAL_CD = ? WHERE ENTITY_ID = ? AND ADDR_TYP_CD = ?";
    
    private static final String INSERT_ADDRESS_SQL = "Insert Into {0}.{1} (ENTITY_ADDR_ID, OBJ_ID, VER_NBR, ENTITY_ID, ENT_TYP_CD, ADDR_TYP_CD, ADDR_LINE_1, ADDR_LINE_2, ADDR_LINE_3, CITY, STATE_PVC_CD, POSTAL_CD, POSTAL_CNTRY_CD, DFLT_IND, ACTV_IND, LAST_UPDT_DT) Values (To_Char( KRIM_ENTITY_ADDR_ID_S.NEXTVAL), SYS_GUID(), 1, ?, 'PERSON', ?, ?, ?, ?, ?, SubStr(?,1,2), ?, ' ', ?, ?, SYSDATE)";

    private static final String TRUNCATE_TABLE_DROPPING_STORAGE_SQL = "TRUNCATE TABLE {0}.{1} DROP STORAGE";
    
    public final static String TABLE_NAME = "KRIM_ENTITY_ADDR_T";
    
    public final static String HOME_ADDR_TYP_CD = "HM";
    public final static String CAMPUS_ADDR_TYP_CD = "CMP";
    
    public KrimEntityAddressTableJdbc(String schemaName) {
        initialize(schemaName);
    }

    protected void initialize(String schemaName) {
        super.setSchemaName(schemaName);
        Object[] sqlArguments = new Object[2];
        sqlArguments[0] = super.getSchemaName();
        sqlArguments[1] = TABLE_NAME;
        super.setFullyQualifiedTableNameArguments(sqlArguments);
    }
    
    public Object[] configureUpdateHomeAddressArguments(Map<String, Object> argumentDataValues) {
        return configureColumnsForUpdate(argumentDataValues.get(KimFeedConstants.HOME_ADDRESS1_KEY), 
                argumentDataValues.get(KimFeedConstants.HOME_ADDRESS2_KEY), argumentDataValues.get(KimFeedConstants.HOME_ADDRESS3_KEY), 
                argumentDataValues.get(KimFeedConstants.HOME_CITY_KEY), argumentDataValues.get(KimFeedConstants.HOME_STATE_KEY),
                argumentDataValues.get(KimFeedConstants.HOME_POSTAL_KEY), argumentDataValues.get(KimFeedConstants.CU_PERSON_SID_KEY), HOME_ADDR_TYP_CD);
    }
    
    public Object[] configureUpdateCampusAddressArguments(Map<String, Object> argumentDataValues) {
        return configureColumnsForUpdate(argumentDataValues.get(KimFeedConstants.CAMPUS_ADDRESS1_KEY), 
                argumentDataValues.get(KimFeedConstants.CAMPUS_ADDRESS2_KEY), argumentDataValues.get(KimFeedConstants.CAMPUS_ADDRESS3_KEY), 
                argumentDataValues.get(KimFeedConstants.CAMPUS_CITY_KEY), argumentDataValues.get(KimFeedConstants.CAMPUS_STATE_KEY),
                argumentDataValues.get(KimFeedConstants.CAMPUS_POSTAL_KEY), argumentDataValues.get(KimFeedConstants.CU_PERSON_SID_KEY), CAMPUS_ADDR_TYP_CD);
    }
    
    private Object[] configureColumnsForUpdate(Object address1, Object address2, Object address3, Object city, 
            Object provinceState, Object postalCode, Object personId, Object addressType) {
        Object[] orderedUpdateColumns = new Object[8];
        orderedUpdateColumns[0] = address1;
        orderedUpdateColumns[1] = address2;
        orderedUpdateColumns[2] = address3;
        orderedUpdateColumns[3] = city;
        orderedUpdateColumns[4] = provinceState;
        orderedUpdateColumns[5] = postalCode;
        orderedUpdateColumns[6] = personId;
        orderedUpdateColumns[7] = addressType;
        return orderedUpdateColumns;
    }
    
    public String getUpdateAddressSql() {
        return MessageFormat.format(UPDATE_ADDRESS_SQL, super.getFullyQualifiedTableNameArguments());
    }
    
    public Object[] configureInsertHomeAddressArguments(Map<String, Object> argumentDataValues) {
        return configureColumnsForInsert(argumentDataValues.get(KimFeedConstants.CU_PERSON_SID_KEY), HOME_ADDR_TYP_CD, 
                argumentDataValues.get(KimFeedConstants.HOME_ADDRESS1_KEY), argumentDataValues.get(KimFeedConstants.HOME_ADDRESS2_KEY), 
                argumentDataValues.get(KimFeedConstants.HOME_ADDRESS3_KEY), argumentDataValues.get(KimFeedConstants.HOME_CITY_KEY), 
                argumentDataValues.get(KimFeedConstants.HOME_STATE_KEY), argumentDataValues.get(KimFeedConstants.HOME_POSTAL_KEY), 
                KimFeedConstants.DEFAULT_YES, argumentDataValues.get(KimFeedConstants.ACTIVE_KEY));
    }
    
    public Object[] configureInsertCampusAddressArguments(Map<String, Object> argumentDataValues) {
        return configureColumnsForInsert(argumentDataValues.get(KimFeedConstants.CU_PERSON_SID_KEY), CAMPUS_ADDR_TYP_CD,
                argumentDataValues.get(KimFeedConstants.CAMPUS_ADDRESS1_KEY), argumentDataValues.get(KimFeedConstants.CAMPUS_ADDRESS2_KEY), 
                argumentDataValues.get(KimFeedConstants.CAMPUS_ADDRESS3_KEY), argumentDataValues.get(KimFeedConstants.CAMPUS_CITY_KEY), 
                argumentDataValues.get(KimFeedConstants.CAMPUS_STATE_KEY), argumentDataValues.get(KimFeedConstants.CAMPUS_POSTAL_KEY), 
                KimFeedConstants.DEFAULT_NO, argumentDataValues.get(KimFeedConstants.ACTIVE_KEY));
    }
    
    private Object[] configureColumnsForInsert(Object personId, Object addressType, Object address1, Object address2, 
            Object address3, Object city, Object provinceState, Object postalCode, Object defaultIndicator, Object activeIndicator) {
        Object[] orderedInsertColumns = new Object[10];
        orderedInsertColumns[0] = personId;
        orderedInsertColumns[1] = addressType;
        orderedInsertColumns[2] = address1;
        orderedInsertColumns[3] = address2;
        orderedInsertColumns[4] = address3;
        orderedInsertColumns[5] = city;
        orderedInsertColumns[6] = provinceState;
        orderedInsertColumns[7] = postalCode;
        orderedInsertColumns[8] = defaultIndicator;
        orderedInsertColumns[9] = activeIndicator;
        return orderedInsertColumns;
    }
    
    public String getInsertAddressSql() {
        return MessageFormat.format(INSERT_ADDRESS_SQL, super.getFullyQualifiedTableNameArguments());
    }
    
    public Object[] configureSelectHomeAddressArguments(Map<String, Object> argumentDataValues) {
        return configureColumnsForSelect(argumentDataValues.get(KimFeedConstants.CU_PERSON_SID_KEY), HOME_ADDR_TYP_CD);
    }
    
    public Object[] configureSelectCampusAddressArguments(Map<String, Object> argumentDataValues) {
        return configureColumnsForSelect(argumentDataValues.get(KimFeedConstants.CU_PERSON_SID_KEY), CAMPUS_ADDR_TYP_CD);
    }
    
    private Object[] configureColumnsForSelect(Object personId, Object addressType) {
        Object[] orderedSelectColumns = new Object[2];
        orderedSelectColumns[0] = personId;
        orderedSelectColumns[1] = addressType;
        return orderedSelectColumns;
    }
    
    public String getTruncateAddressTableSql() {
        return MessageFormat.format(TRUNCATE_TABLE_DROPPING_STORAGE_SQL, super.getFullyQualifiedTableNameArguments());
    }
    
    public String getTableName() {
        return TABLE_NAME;
    }

    public static String getHomeAddrTypCd() {
        return HOME_ADDR_TYP_CD;
    }

    public static String getCampusAddrTypCd() {
        return CAMPUS_ADDR_TYP_CD;
    }



}
