package edu.cornell.kfs.fp.document.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.Collections;
import java.util.List;

import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;

import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;

@ConfigureContext(session = ccs1)
public class CuDisbursementVoucherPayeeServiceImplIntegTest extends KualiIntegTestBase {
	private PersonService personService;
    private CuDisbursementVoucherPayeeService cuDisbursementVoucherPayeeService;
    private UnitTestSqlDao unitTestSqlDao;
    private String alumniSql = "SELECT dv_payee_id_nbr,fdoc_nbr FROM FP_DV_PAYEE_DTL_T where dv_payee_typ_cd = 'A'";
    private String studentSql = "SELECT dv_payee_id_nbr,fdoc_nbr FROM FP_DV_PAYEE_DTL_T where dv_payee_typ_cd = 'S'";
    private String employeeSql = "SELECT dv_payee_id_nbr,fdoc_nbr FROM FP_DV_PAYEE_DTL_T where dv_payee_typ_cd = 'E'";    
    private BusinessObjectService businessObjectService;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        personService = SpringContext.getBean(PersonService.class);
        cuDisbursementVoucherPayeeService = CuDisbursementVoucherPayeeServiceImpl.class.newInstance();
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        
	}          
	
                  
	public void testService(){
		
		Person alumni = null;
		Person student = null;
		Person employee = null;
		boolean isAlumni = false;
		boolean isStudent = false;
		boolean isEmployee = false;

		List alumniResults =  unitTestSqlDao.sqlSelect(alumniSql);
		Object aresult = alumniResults.get(0);
		String[] asplit = aresult.toString().split(",");
		String aFdocSplit [] = asplit[0].split("=");
		String aFdoc = aFdocSplit[1].replaceAll(",", "");
		aFdoc = aFdoc.trim();
		String aIdSplit []  = asplit[1].split("=");
		String aentid = aIdSplit[1].replaceAll("}", "");
		aentid = aentid.trim();
		alumniResults.clear();
		
		List studentResults =  unitTestSqlDao.sqlSelect(studentSql);
		Object sresult = studentResults.get(0);
		String[] stSplit = sresult.toString().split(",");
		String sFdocSplit [] = stSplit[0].split("=");
		String sFdoc = sFdocSplit[1].replaceAll(",", "");
		sFdoc = sFdoc.trim();
		String sIdSplit []  = stSplit[1].split("=");
		String sentid = sIdSplit[1].replaceAll("}", "");
		sentid = sentid.trim();
		studentResults.clear();
		
		List employeeResults =  unitTestSqlDao.sqlSelect(employeeSql);
		Object eresult = employeeResults.get(0);
		String[] esplit = eresult.toString().split(",");
		String eFdocSplit [] = esplit[0].split("=");
		String eFdoc = eFdocSplit[1].replaceAll(",", "");
		eFdoc = eFdoc.trim();
		String eIdSplit []  = esplit[1].split("=");
		String emEmpid = eIdSplit[1].replaceAll("}", "");
		emEmpid = emEmpid.trim();
		employeeResults.clear();
		
		
		List<Person> people;
		
		people = personService.findPeople( Collections.singletonMap(KIMPropertyConstants.Person.ENTITY_ID, aentid) );
		alumni = people.get(0);
		people.clear();
		people = personService.findPeople( Collections.singletonMap(KIMPropertyConstants.Person.ENTITY_ID, sentid) );
		student = people.get(0);		
		people.clear();		
		people = personService.findPeople( Collections.singletonMap(KIMPropertyConstants.Person.EMPLOYEE_ID, emEmpid) );		
		employee = people.get(0);
								
		
		DisbursementPayee alumniPayee = cuDisbursementVoucherPayeeService.getPayeeFromPerson(alumni);
		CuDisbursementPayee cuAlumniPayee = (CuDisbursementPayee) cuDisbursementVoucherPayeeService.getPayeeFromPerson(alumni, "A");
			
		DisbursementPayee studentPayee = cuDisbursementVoucherPayeeService.getPayeeFromPerson(student);
		CuDisbursementPayee cuStudentPayee = (CuDisbursementPayee) cuDisbursementVoucherPayeeService.getPayeeFromPerson(student, "S");

		DisbursementPayee employeePayee = cuDisbursementVoucherPayeeService.getPayeeFromPerson(employee);
		CuDisbursementPayee cuEmployeePayee = (CuDisbursementPayee) cuDisbursementVoucherPayeeService.getPayeeFromPerson(employee, employeePayee.getPayeeTypeCode());

		
			
			isAlumni = cuDisbursementVoucherPayeeService.isAlumni(cuAlumniPayee);			
			isStudent = cuDisbursementVoucherPayeeService.isStudent(cuStudentPayee);
			isEmployee = cuDisbursementVoucherPayeeService.isEmployee(cuEmployeePayee);
							
			//test affliation check by payee
			assertTrue(isAlumni);
			assertTrue(isStudent);
			assertTrue(isEmployee);
			
			isAlumni = false;
			isStudent = false;
			isEmployee = false;
			
			//Test getpayeefrom person sets principal name
			assertTrue(student.getPrincipalName().equalsIgnoreCase(cuStudentPayee.getPrincipalName()));
			assertTrue(alumni.getPrincipalName().equalsIgnoreCase(cuAlumniPayee.getPrincipalName()));
			
			
			 CuDisbursementVoucherDocument dvA = businessObjectService.findBySinglePrimaryKey(CuDisbursementVoucherDocument.class, aFdoc);
			 
			 System.out.println("FDOC number  "+aFdoc);
			 System.out.println("DV Payee detail "+dvA.getDvPayeeDetail());
			 
			 isAlumni = cuDisbursementVoucherPayeeService.isAlumni((CuDisbursementVoucherPayeeDetail)dvA.getDvPayeeDetail());
			 
			 CuDisbursementVoucherDocument dvS = businessObjectService.findBySinglePrimaryKey(CuDisbursementVoucherDocument.class, sFdoc);
			 isStudent = cuDisbursementVoucherPayeeService.isStudent((CuDisbursementVoucherPayeeDetail)dvS.getDvPayeeDetail());
			 
			 CuDisbursementVoucherDocument dvE = businessObjectService.findBySinglePrimaryKey(CuDisbursementVoucherDocument.class, eFdoc);
			 isEmployee = cuDisbursementVoucherPayeeService.isEmployee(dvE.getDvPayeeDetail());
			 
			//test affliation check by dvpayeedetail
			 assertTrue(isAlumni);
			 assertTrue(isStudent);
			 assertTrue(isEmployee);
			 
			 

		}
           

}
