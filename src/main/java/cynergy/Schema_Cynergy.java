package cynergy;

import org.springframework.jdbc.core.JdbcTemplate;

public class Schema_Cynergy {
    
//    JdbcTemplate destTemplate.update("call disable_constraint()");
//    JdbcTemplate destTemplate.update("call enable_constraint()");
    
    private static final String DATABASE_TEST_SQL = "SELECT 1 FROM DUAL";
    private static final String DB_VALIDATION_SQL = "select 1 from dual";
}