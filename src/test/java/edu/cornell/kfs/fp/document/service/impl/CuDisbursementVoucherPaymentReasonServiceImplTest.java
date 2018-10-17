package edu.cornell.kfs.fp.document.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherPaymentReasonServiceImpl;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.mockito.Mockito;

import static org.junit.Assert.*;


public class CuDisbursementVoucherPaymentReasonServiceImplTest{
	private CuDisbursementVoucherPaymentReasonServiceImpl cuDisbursementVoucherPaymentReasonService;
	private DisbursementVoucherPaymentReasonServiceImpl disbursementVoucherPaymentReasonService;
	private Class cuDisbursementVoucherPaymentReasonServiceImplClass;
    private Class DisbursementVoucherPaymentReasonServiceImplClass;
	
	@Before
    public void setUp() throws Exception {
        disbursementVoucherPaymentReasonService = DisbursementVoucherPaymentReasonServiceImpl.class.newInstance();
        cuDisbursementVoucherPaymentReasonService = CuDisbursementVoucherPaymentReasonServiceImpl.class.newInstance();
        cuDisbursementVoucherPaymentReasonService.setDisbursementVoucherPayeeService(buildMockDisbursementVoucherPayeeService());
        cuDisbursementVoucherPaymentReasonServiceImplClass = CuDisbursementVoucherPaymentReasonServiceImpl.class;  
        DisbursementVoucherPaymentReasonServiceImplClass = DisbursementVoucherPaymentReasonServiceImpl.class;
	} 
	
	private DisbursementVoucherPayeeService buildMockDisbursementVoucherPayeeService() {
	    DisbursementVoucherPayeeService payeeService = Mockito.mock(DisbursementVoucherPayeeService.class);
	    Mockito.when(payeeService.getPayeeTypeDescription(Mockito.anyString())).thenReturn("Test");
	    return payeeService;
	}
	
	@Test
	public void test(){
		
       Collection<String> payeeTypeCds = new ArrayList<String>();
               
       payeeTypeCds.add("E");
       payeeTypeCds.add("F");
       payeeTypeCds.add("T");
       payeeTypeCds.add("V");
       payeeTypeCds.add("E");
       payeeTypeCds.add("F");
       payeeTypeCds.add("X");
       payeeTypeCds.add("S");       
        
        Method eduMethod = null;
        Method orgMethod = null;
		try {
				eduMethod = cuDisbursementVoucherPaymentReasonServiceImplClass.getDeclaredMethod("getDescriptivePayeeTypesAsString", Collection.class);
				orgMethod = DisbursementVoucherPaymentReasonServiceImpl.class.getDeclaredMethod("getDescriptivePayeeTypesAsString", Collection.class);
				eduMethod.setAccessible(true);
				orgMethod.setAccessible(true);
				
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Object eduResult = null;
		Object orgResult = null;
        
		try {
			eduResult = eduMethod.invoke(cuDisbursementVoucherPaymentReasonService, payeeTypeCds);
			orgResult = orgMethod.invoke(disbursementVoucherPaymentReasonService, payeeTypeCds);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		if (eduResult.toString().length() >= orgResult.toString().length()) {
			fail("The edu method should return a smaller string");
		}
		

	}
    

}