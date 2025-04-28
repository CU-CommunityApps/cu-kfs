package edu.cornell.kfs.coa.batch.dataaccess.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.mockito.Mockito;

public class WorkdayOpenAccountDaoJdbcTest {
    
    private WorkdayOpenAccountDaoJdbc workdayOpenAccountDao;

    @BeforeEach
    void setUp() throws Exception {
        workdayOpenAccountDao = new WorkdayOpenAccountDaoJdbc();
        workdayOpenAccountDao.setUniversityDateService(buildMockUniversityDateService());
    }
    
    private UniversityDateService buildMockUniversityDateService() {
        UniversityDateService dateService = Mockito.mock(UniversityDateService.class);
        Mockito.when(dateService.getCurrentFiscalYear()).thenReturn(2025);
        return dateService;
    }

    @AfterEach
    void tearDown() throws Exception {
        workdayOpenAccountDao = null;
    }

    @Test
    void testBuildFullOpenAccountSql() {
        String actualSql = workdayOpenAccountDao.buildFullOpenAccountSql();
        String expectedSql = "SELECT CAT.FIN_COA_CD, CAT.ACCOUNT_NBR, CAT.ACCOUNT_NM, CSF.SUB_FUND_GRP_WAGE_IND, CAT.SUB_FUND_GRP_CD,"
                + " CAT.FIN_HGH_ED_FUNC_CD, CAT.ACCT_EFFECT_DT,  CAT.ACCT_CLOSED_IND, CAT.ACCT_TYP_CD, '' AS SUB_ACCT_NBR, "
                + "'' AS SUB_ACCT_NM, '' AS SUB_ACCT_ACTV_CD, '' AS FIN_OBJECT_CD, '' AS FIN_SUB_OBJ_CD, '' AS FIN_SUB_OBJ_CD_NM, CAT.CG_CFDA_NBR "
                + "FROM KFS.CA_ACCOUNT_T CAT "
                + "JOIN KFS.CA_SUB_FUND_GRP_T CSF ON CAT.SUB_FUND_GRP_CD = CSF.SUB_FUND_GRP_CD "
                + "WHERE CAT.ACCT_CLOSED_IND = 'N' AND CSF.SUB_FUND_GRP_WAGE_IND = 'Y' AND CAT.FIN_COA_CD = 'IT'  "
                + "UNION "
                + "SELECT CAT.FIN_COA_CD, CAT.ACCOUNT_NBR, CAT.ACCOUNT_NM, CSF.SUB_FUND_GRP_WAGE_IND, CAT.SUB_FUND_GRP_CD, "
                + "CAT.FIN_HGH_ED_FUNC_CD, CAT.ACCT_EFFECT_DT,  CAT.ACCT_CLOSED_IND, CAT.ACCT_TYP_CD, CSA.SUB_ACCT_NBR, "
                + "CSA.SUB_ACCT_NM, CSA.SUB_ACCT_ACTV_CD, '' AS FIN_OBJECT_CD, '' AS FIN_SUB_OBJ_CD, '' AS FIN_SUB_OBJ_CD_NM, CAT.CG_CFDA_NBR "
                + "FROM KFS.CA_ACCOUNT_T CAT "
                + "JOIN KFS.CA_SUB_FUND_GRP_T CSF ON CAT.SUB_FUND_GRP_CD = CSF.SUB_FUND_GRP_CD "
                + "JOIN KFS.CA_SUB_ACCT_T CSA ON CAT.FIN_COA_CD = CSA.FIN_COA_CD AND CAT.ACCOUNT_NBR = CSA.ACCOUNT_NBR "
                + "WHERE CAT.ACCT_CLOSED_IND = 'N' AND CSF.SUB_FUND_GRP_WAGE_IND = 'Y' AND CAT.FIN_COA_CD = 'IT' AND CSA.SUB_ACCT_ACTV_CD = 'Y'  "
                + "UNION "
                + "SELECT CAT.FIN_COA_CD, CAT.ACCOUNT_NBR, CAT.ACCOUNT_NM, CSF.SUB_FUND_GRP_WAGE_IND, CAT.SUB_FUND_GRP_CD, "
                + "CAT.FIN_HGH_ED_FUNC_CD, CAT.ACCT_EFFECT_DT,  CAT.ACCT_CLOSED_IND, CAT.ACCT_TYP_CD, '' AS SUB_ACCT_NBR, '' AS SUB_ACCT_NM, "
                + "'' AS SUB_ACCT_ACTV_CD, CSO.FIN_OBJECT_CD, CSO.FIN_SUB_OBJ_CD, CSO.FIN_SUB_OBJ_CD_NM, CAT.CG_CFDA_NBR "
                + "FROM KFS.CA_ACCOUNT_T CAT "
                + "JOIN KFS.CA_SUB_FUND_GRP_T CSF ON CAT.SUB_FUND_GRP_CD = CSF.SUB_FUND_GRP_CD "
                + "JOIN KFS.CA_SUB_OBJECT_CD_T CSO ON CAT.FIN_COA_CD = CSO.FIN_COA_CD AND CAT.ACCOUNT_NBR = CSO.ACCOUNT_NBR "
                + "JOIN KFS.LD_LABOR_OBJ_T COC ON CAT.FIN_COA_CD = COC.FIN_COA_CD AND CSO.FIN_OBJECT_CD = COC.FIN_OBJECT_CD AND "
                + "CSO.UNIV_FISCAL_YR = COC.UNIV_FISCAL_YR "
                + "WHERE CAT.ACCT_CLOSED_IND = 'N' AND CSF.SUB_FUND_GRP_WAGE_IND = 'Y' AND CAT.FIN_COA_CD = 'IT' AND "
                + "CSO.UNIV_FISCAL_YR = 2025 AND COC.ACTV_IND = 'Y'  "
                + "ORDER BY FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR, FIN_SUB_OBJ_CD";
        assertEquals(expectedSql, actualSql);
    }

}
