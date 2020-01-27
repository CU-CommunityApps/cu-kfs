package edu.cornell.kfs.sys.batch.dataaccess;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public class KimFeedDestinationJdbcTemplate extends JdbcTemplate {
    
    
    public KimFeedDestinationJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }
    
}
