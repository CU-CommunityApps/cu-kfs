package edu.cornell.kfs.coa.batch.dataaccess.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.cornell.kfs.coa.batch.businessobject.WorkdayOpenAccountDetail;

class WorkdayOpenAccountDaoJdbcTest {
    
    private WorkdayOpenAccountDaoJdbc workdayOpenAccountDao;

    @BeforeEach
    void setUp() throws Exception {
        workdayOpenAccountDao = new WorkdayOpenAccountDaoJdbc();
        workdayOpenAccountDao.setUniversityDateService(buildMockUniversityDateService());
    }
    
    private UniversityDateService buildMockUniversityDateService() {
        UniversityDateService dateService = Mockito.mock(UniversityDateService.class);
        Mockito.when(dateService.getCurrentFiscalYear()).thenReturn(2022);
        return dateService;
    }

    @AfterEach
    void tearDown() throws Exception {
        workdayOpenAccountDao = null;
    }

    @Test
    void testBuildFullOpenAccountSql() {
        String actualSql = workdayOpenAccountDao.buildFullOpenAccountSql();
        assertTrue(StringUtils.contains(actualSql, "CSO.UNIV_FISCAL_YR = 2022"));
        assertTrue(StringUtils.contains(actualSql, "ORDER BY FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR, FIN_COA_CD, FIN_SUB_OBJ_CD"));
    }

}
