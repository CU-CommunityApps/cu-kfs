package edu.cornell.kfs.fp.businessobject.lookup;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;

@ConfigureContext(session = ccs1)
public class CuDisbursementPayeeLookupableHelperServiceImplIntegTest extends KualiIntegTestBase {

	private PersonService personService;
	private UnitTestSqlDao unitTestSqlDao;
	private String alumniSql = "SELECT f.dv_payee_id_nbr " +
			"FROM kfs.FP_DV_PAYEE_DTL_T f, kfs.krim_person_t k " +
			"where f.dv_payee_typ_cd = 'A' " +
			"and f.dv_payee_id_nbr = k.entity_id " +
			"and k.afltn_alumni = 'A' " +
			"and k.dflt_ind = 'Y' " +
			"and k.actv_ind = 'Y' " +
			"and rownum <= 1";

	private String vendorSql = "select VNDR_NM from PUR_VNDR_DTL_T where dobj_maint_cd_actv_ind = 'Y' and rownum <= 1";
	@SuppressWarnings("deprecation")
	private LookupableHelperService cuDisbursementPayeeLookupableHelperService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		cuDisbursementPayeeLookupableHelperService = LookupableSpringContext.getLookupableHelperService("disbursementPayeeLookupableHelperService");
		cuDisbursementPayeeLookupableHelperService.setBusinessObjectClass(CuDisbursementPayee.class);

		personService = SpringContext.getBean(PersonService.class);
		unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
	}

	public void testLookupAlumni() {
		List alumniResults = unitTestSqlDao.sqlSelect(alumniSql);
		assertFalse("alumni query didn't return any results, which is just wrong", alumniResults.isEmpty());
		HashMap alumniResult = (HashMap)alumniResults.get(0);
		String entityId = (String)alumniResult.get("DV_PAYEE_ID_NBR");
		assertTrue("couldn't get entityId from query results", StringUtils.isNotBlank(entityId));

		List<Person> people = personService.findPeople(Collections.singletonMap(KIMPropertyConstants.Person.ENTITY_ID, entityId));
		assertFalse("couldn't find a person for entityId " + entityId, people.isEmpty());
		Person alumni = people.get(0);

		Map<String, String> fieldValues = new LinkedHashMap();
		fieldValues.put(KIMPropertyConstants.Principal.PRINCIPAL_NAME, alumni.getPrincipalName());

		validateSearch("Alumni", fieldValues);
	}

	public void testLookupVendor() {
		List vendorResults = unitTestSqlDao.sqlSelect(vendorSql);
		assertFalse("vendor query didn't return any results, which is just wrong", vendorResults.isEmpty());
		HashMap vendorResult = (HashMap)vendorResults.get(0);
		String vendorName = (String)vendorResult.get("VNDR_NM");
		assertTrue("couldn't get vendorName from query results", StringUtils.isNotBlank(vendorName));

		Map<String, String> fieldValues = new LinkedHashMap();

		fieldValues.put(KFSPropertyConstants.ACTIVE, "Y");
		fieldValues.put(KFSPropertyConstants.VENDOR_NAME, vendorName);
		fieldValues.put(KFSPropertyConstants.VENDOR_NUMBER, StringUtils.EMPTY);
		fieldValues.put(KFSPropertyConstants.TAX_NUMBER, StringUtils.EMPTY);

		validateSearch("Vendor", fieldValues);
	}

	private void validateSearch(String searchType, Map<String, String> fieldValues) {
		try {
			cuDisbursementPayeeLookupableHelperService.validateSearchParameters(fieldValues);
		} catch (ValidationException e) {
			fail(searchType + " Search Parameters failed validation");
		}

		List<? extends BusinessObject> payee = cuDisbursementPayeeLookupableHelperService.getSearchResults(fieldValues);

		assertFalse(searchType + " search didn't return any results, but it should have", payee.isEmpty());
	}

}