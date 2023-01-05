package edu.cornell.kfs.gl.batch.service.impl;

import java.util.List;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;

import edu.cornell.kfs.gl.batch.service.ReversionUnitOfWorkService;
import edu.cornell.kfs.gl.businessobject.ReversionUnitOfWork;

@ConfigureContext
public class ReversionUnitOfWorkServiceImplIntegTest extends KualiIntegTestBase {

    private ReversionUnitOfWorkService accountReversionUnitOfWorkService;
    private UnitTestSqlDao unitTestSqlDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountReversionUnitOfWorkService = SpringContext.getBean(ReversionUnitOfWorkService.class, "glAcctReversionUnitOfWorkService");
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
    }

    public void testLoadCategories() {

        ReversionUnitOfWork unitOfWork = new ReversionUnitOfWork("IT", "1003000", "-----");

        if (!unitOfworkExists()) {
            createUnitOfWork();
        }

        if (!categoryAmountExists()) {
            createCategoryAmounts();
        }
        accountReversionUnitOfWorkService.loadCategories(unitOfWork);

        assertEquals(true, unitOfWork.getCategoryAmounts().size() > 0);

    }

    private void createUnitOfWork() {
        String objId = java.util.UUID.randomUUID().toString();
        String insertUnitOfWorkSql = "insert into GL_RVRSN_UNIT_WRK_T (FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR, OBJ_ID, VER_NBR, TOT_RVRSN_AMT, TOT_CF_AMT, TOT_AVAIL_AMT, TOT_CSH_AMT) values ('IT', '1003000', '-----', '" + objId + "', 1, 1, 0, 0, 0)";
        unitTestSqlDao.sqlCommand(insertUnitOfWorkSql);
    }

    private void createCategoryAmounts() {
        String objId = java.util.UUID.randomUUID().toString();

        String insertCategoryAmtSql = "insert into GL_RVRSN_CTGRY_AMT_T (FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR, RVRSN_CTGRY_CD, OBJ_ID, VER_NBR, TOT_ACTL_AMT, TOT_BDGT_AMT, TOT_ENCUM_AMT, TOT_CF_AMT, TOT_AVAIL_AMT) values ('IT', '1003000', '-----', 'A1', '" + objId + "', 1, 1, 0, 0, 0, 0)";

        unitTestSqlDao.sqlCommand(insertCategoryAmtSql);
    }

    private boolean categoryAmountExists() {
        List results = unitTestSqlDao.sqlSelect("select * from GL_RVRSN_CTGRY_AMT_T where FIN_COA_CD = 'IT' and ACCOUNT_NBR = '1003000' and SUB_ACCT_NBR = '-----'");

        if (ObjectUtils.isNotNull(results) && !results.isEmpty()) {
            return true;
        } else
            return false;
    }

    private boolean unitOfworkExists() {
        List results = unitTestSqlDao.sqlSelect("select * from GL_RVRSN_UNIT_WRK_T where FIN_COA_CD = 'IT' and ACCOUNT_NBR = '1003000' and SUB_ACCT_NBR = '-----'");

        if (ObjectUtils.isNotNull(results) && !results.isEmpty()) {
            return true;
        } else
            return false;
    }

}