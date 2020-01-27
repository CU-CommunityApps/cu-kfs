package cynergy;

import org.springframework.jdbc.core.JdbcTemplate;

public class Table_KRIM_PRNCPL_T {
    
//    private static final String SELECT_MATCHING_PRINCIPAL_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_PRNCPL_T WHERE PRNCPL_NM = ? AND ENTITY_ID = ?";
//    private static final String SELECT_CONFLICTING_PRINCIPAL_COUNT_SQL = "SELECT COUNT(*) FROM KRIM_PRNCPL_T WHERE PRNCPL_NM = ? AND ENTITY_ID <> ?";
//    private static final String UPDATE_PRINCIPAL_SQL = "UPDATE KRIM_PRNCPL_T SET PRNCPL_NM = ?, LAST_UPDT_DT = SYSDATE WHERE ENTITY_ID = ?";
//    private static final String INSERT_PRINCIPAL_SQL = "Insert Into KRIM_PRNCPL_T (PRNCPL_ID, OBJ_ID, VER_NBR, PRNCPL_NM, ENTITY_ID, PRNCPL_PSWD, ACTV_IND, LAST_UPDT_DT) Values (?, SYS_GUID(), 1, ?, ?, null, ?, SYSDATE)";

//    JdbcTemplate destTemplate.update("Insert into krim_prncpl_t (PRNCPL_ID,OBJ_ID,VER_NBR,PRNCPL_NM,ENTITY_ID,PRNCPL_PSWD,ACTV_IND,LAST_UPDT_DT) values ('1','7ECD903B6A9C48C0E04400144F00411E',175,'kr','1',null,'N',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
//    JdbcTemplate destTemplate.update("Insert into krim_prncpl_t (PRNCPL_ID,OBJ_ID,VER_NBR,PRNCPL_NM,ENTITY_ID,PRNCPL_PSWD,ACTV_IND,LAST_UPDT_DT) values ('2','7ECD903B6A9D48C0E04400144F00411E',5,'kfs','2',null,'N',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
//    JdbcTemplate destTemplate.update("Insert into krim_prncpl_t (PRNCPL_ID,OBJ_ID,VER_NBR,PRNCPL_NM,ENTITY_ID,PRNCPL_PSWD,ACTV_IND,LAST_UPDT_DT) values ('3','7ECD903B6A9E48C0E04400144F00411E',271,'admin','3',null,'N',to_timestamp('04-FEB-10','DD-MON-RR HH.MI.SSXFF AM'))");
////    
//    JdbcTemplate destTemplate.update("Truncate Table KRIM_PRNCPL_T Drop Storage");
    
//    destTemplate.execute("UPDATE KRIM_PRNCPL_T SET PRNCPL_NM = 'DIS-' || PRNCPL_NM WHERE PRNCPL_ID IN (" + deleteIDs + ") " +
//            "       AND PRNCPL_NM NOT LIKE 'DIS-%'");   
    
}