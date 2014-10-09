package edu.cornell.kfs.fp.businessobject.lookup;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.lookup.DisbursementPayeeLookupableHelperServiceImpl;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.kns.lookup.LookupableHelperService;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.exception.ValidationException;

import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;

@ConfigureContext(session = ccs1)
public class CuDisbursementPayeeLookupableHelperServiceImplTest extends KualiTestBase {
	private DisbursementPayeeLookupableHelperServiceImpl disbursementPayeeLookupableHelperServiceImpl;

	private PersonService personService;
	private UnitTestSqlDao unitTestSqlDao;
	private String alumniSql = "SELECT dv_payee_id_nbr,fdoc_nbr FROM FP_DV_PAYEE_DTL_T where dv_payee_typ_cd = 'A'";
	private String vendorSql = "select VNDR_NM from PUR_VNDR_DTL_T where rownum <= 1";
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

	public void test() {

		Person alumni = null;
		Person student = null;

		List alumniResults = unitTestSqlDao.sqlSelect(alumniSql);
		Object aresult = alumniResults.get(0);
		String[] asplit = aresult.toString().split(",");
		String aFdocSplit[] = asplit[0].split("=");
		String aFdoc = aFdocSplit[1].replaceAll(",", "");
		aFdoc = aFdoc.trim();
		String aIdSplit[] = asplit[1].split("=");
		String aentid = aIdSplit[1].replaceAll("}", "");
		aentid = aentid.trim();
		alumniResults.clear();

		List<Person> people;

		people = personService.findPeople(Collections.singletonMap(
				KIMPropertyConstants.Person.ENTITY_ID, aentid));
		alumni = people.get(0);
		people.clear();

		List vendorResults = unitTestSqlDao.sqlSelect(vendorSql);
		Object vresult = vendorResults.get(0);		
		String vSplit[] = vresult.toString().split("=");
		String vNm = vSplit[1].replaceAll("}", "");
		vNm = vNm.trim();
		vendorResults.clear();			
		
		
		Map<String, String> m = new LinkedHashMap();
		Map<String, String> v = new LinkedHashMap();
		List<DisbursementPayee> searchResults = new ArrayList<DisbursementPayee>();

		m.put(KIMPropertyConstants.Person.PRINCIPAL_NAME,
				alumni.getPrincipalName());

		v.put(KFSPropertyConstants.VENDOR_NAME, vNm);

		List<? extends BusinessObject> p_payee = cuDisbursementPayeeLookupableHelperService
				.getSearchResults(m);
		
		List<? extends BusinessObject> v_payee = cuDisbursementPayeeLookupableHelperService
				.getSearchResults(v);
	
		
		try {
			cuDisbursementPayeeLookupableHelperService.validateSearchParameters(v);
			cuDisbursementPayeeLookupableHelperService.validateSearchParameters(m);
		} catch (ValidationException e) {
			fail("Failed validation");
		}

		assertTrue(!p_payee.isEmpty());
		assertTrue(!v_payee.isEmpty());

	}

}