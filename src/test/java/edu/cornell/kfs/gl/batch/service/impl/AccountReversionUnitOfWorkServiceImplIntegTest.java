package edu.cornell.kfs.gl.batch.service.impl;

import java.util.List;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;

import edu.cornell.kfs.gl.batch.service.ReversionUnitOfWorkService;

@ConfigureContext
public class AccountReversionUnitOfWorkServiceImplIntegTest extends KualiIntegTestBase {
	private ReversionUnitOfWorkService accountReversionUnitOfWorkService;
	private UnitTestSqlDao unitTestSqlDao;
	
	private static String RVRSN_CTGRY_AMT = "select * from GL_RVRSN_CTGRY_AMT_T";
	private static String RVRSN_UNIT_WRK = "select * from GL_RVRSN_UNIT_WRK_T";
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        accountReversionUnitOfWorkService = SpringContext.getBean(ReversionUnitOfWorkService.class, "glAcctReversionUnitOfWorkService");
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);

    }
	
	public void testDestroyAllUnitOfWorkSummaries(){
		accountReversionUnitOfWorkService.destroyAllUnitOfWorkSummaries();
		List reversionCategoryAmtResults =  unitTestSqlDao.sqlSelect(RVRSN_CTGRY_AMT);
		assertEquals(0, reversionCategoryAmtResults.size());
		
		List reversionUnitOfWorkResults =  unitTestSqlDao.sqlSelect(RVRSN_UNIT_WRK);
		assertEquals(0, reversionUnitOfWorkResults.size());
		
	}
    

}
