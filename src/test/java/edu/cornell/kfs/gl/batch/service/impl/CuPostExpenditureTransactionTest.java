package edu.cornell.kfs.gl.batch.service.impl;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.impl.AccountServiceImpl;
import org.kuali.kfs.coa.service.impl.ObjectCodeServiceImpl;

@ConfigureContext
public class CuPostExpenditureTransactionTest extends KualiTestBase {
	
	private CuPostExpenditureTransaction cuPostExpenditureTransactionl;
	private AccountService accountService;
	private ObjectCodeService objectCodeService;
	
	private static final String ACCTIVE_ACCOUNT_NUMBER = "U558312";
	private static final String ACTIVE_OBJECT_NUMBER = "6870";
	private static final String INACTIVE_ACCOUNT_NUMBER = "1258320";
	private static final String INACTIVE_OBJECT_NUMBER = "6550";
	private static final String CHART_CODE = "IT";

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		cuPostExpenditureTransactionl = new CuPostExpenditureTransaction();
		accountService = SpringContext.getBean(AccountService.class);
		objectCodeService = SpringContext.getBean(ObjectCodeService.class);
	}

	@After
	protected void tearDown() throws Exception {
		super.tearDown();
		cuPostExpenditureTransactionl = null;
		accountService = null;
	}

	@Test
	public void testHasExclusionByAccountActive() {
		Account activeAccount = accountService.getByPrimaryId(CHART_CODE, ACCTIVE_ACCOUNT_NUMBER);
		ObjectCode code = objectCodeService.getByPrimaryIdForCurrentYear(CHART_CODE, ACTIVE_OBJECT_NUMBER);
		boolean results = cuPostExpenditureTransactionl.hasExclusionByAccount(activeAccount, code);
		assertTrue("Expected to be true,  but wasn't", results);
	}
	
	@Test
	public void testHasExclusionByAccountInactive() {
		Account inactiveAccount = accountService.getByPrimaryId(CHART_CODE, INACTIVE_ACCOUNT_NUMBER);
		ObjectCode code = objectCodeService.getByPrimaryIdForCurrentYear(CHART_CODE, INACTIVE_OBJECT_NUMBER);
		boolean results = cuPostExpenditureTransactionl.hasExclusionByAccount(inactiveAccount, code);
		assertFalse("Expected to be false,  but wasn't", results);
	}

}
