package edu.cornell.kfs.fp.document.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherPaymentReasonServiceImpl;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import static org.junit.Assert.*;

public class CuDisbursementVoucherPaymentReasonServiceImplTest {
    private CuDisbursementVoucherPaymentReasonServiceImpl cuDisbursementVoucherPaymentReasonService;
    private TestableDisbursementVoucherPaymentReasonServiceImpl disbursementVoucherPaymentReasonService;
    private Map<String, String> payeeTypeCodeMap;

    @Before
    public void setUp() throws Exception {
        populatePayeetypeCodeMap();
        disbursementVoucherPaymentReasonService = new TestableDisbursementVoucherPaymentReasonServiceImpl();
        disbursementVoucherPaymentReasonService.setDisbursementVoucherPayeeService(buildMockDisbursementVoucherPayeeService());
        cuDisbursementVoucherPaymentReasonService = new CuDisbursementVoucherPaymentReasonServiceImpl();
        cuDisbursementVoucherPaymentReasonService.setDisbursementVoucherPayeeService(buildMockDisbursementVoucherPayeeService());
    }
    
    @After
    public void tearDown() {
        disbursementVoucherPaymentReasonService = null;
        disbursementVoucherPaymentReasonService = null;
        payeeTypeCodeMap = null;
    }

    private void populatePayeetypeCodeMap() {
        payeeTypeCodeMap = new HashMap<String, String>();
        payeeTypeCodeMap.put("E", "Employees Students Alumni");
        payeeTypeCodeMap.put("S", "Employees Students Alumni");
        payeeTypeCodeMap.put("A", "Employees Students Alumni");
        payeeTypeCodeMap.put("V", "Vendor");
    }

    private DisbursementVoucherPayeeService buildMockDisbursementVoucherPayeeService() {
        DisbursementVoucherPayeeService payeeService = Mockito.mock(DisbursementVoucherPayeeService.class);
        Mockito.when(payeeService.getPayeeTypeDescription(Mockito.any())).then(this::findPayeeTypeDescription);
        return payeeService;
    }
    
    private String findPayeeTypeDescription(InvocationOnMock invocation) {
        String payeeTypeCode = invocation.getArgument(0);
        if (payeeTypeCodeMap.containsKey(payeeTypeCode)) {
            return payeeTypeCodeMap.get(payeeTypeCode);
        }
        return StringUtils.EMPTY;
    }
    
    @Test
    public void testGetDescriptivePayeeTypesAsString() {
        Collection<String> payeeTypeCds = buildPayeeTypeCodeCollection();

        String eduResult = cuDisbursementVoucherPaymentReasonService.getDescriptivePayeeTypesAsString(payeeTypeCds);
        String orgResult = disbursementVoucherPaymentReasonService.getDescriptivePayeeTypesAsString(payeeTypeCds);

        if (eduResult.toString().length() >= orgResult.toString().length()) {
            fail("The edu method should return a smaller string");
        }

    }

    protected Collection<String> buildPayeeTypeCodeCollection() {
        Collection<String> payeeTypeCds = new ArrayList<String>();
        payeeTypeCds.add("E");
        payeeTypeCds.add("F");
        payeeTypeCds.add("T");
        payeeTypeCds.add("V");
        payeeTypeCds.add("E");
        payeeTypeCds.add("F");
        payeeTypeCds.add("X");
        payeeTypeCds.add("S");
        return payeeTypeCds;
    }

    public class TestableDisbursementVoucherPaymentReasonServiceImpl extends DisbursementVoucherPaymentReasonServiceImpl {

        @Override
        public String getDescriptivePayeeTypesAsString(Collection<String> payeeTypeCodes) {
            List<String> payeeTypeDescriptions = new ArrayList<>();

            for (String payeeTypeCode : payeeTypeCodes) {
                String description = disbursementVoucherPayeeService.getPayeeTypeDescription(payeeTypeCode);
                payeeTypeDescriptions.add(description);
            }

            return this.convertListToString(payeeTypeDescriptions);
        }
    }

}