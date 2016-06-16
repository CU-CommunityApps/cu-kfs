package edu.cornell.kfs.fp.document;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.service.impl.CULegacyTravelServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.sys.KFSKeyConstants;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CuDisbursementVoucherDocumentTest {

    @Mock
    private static CuDisbursementVoucherDocument cuDisbursementVoucherDocument;

    @BeforeClass
    public static void setUp() throws Exception {
        ArrayList<String> methodNames = new ArrayList<>();
        for (Method method : CuDisbursementVoucherDocument.class.getMethods()) {
            if (!Modifier.isFinal(method.getModifiers()) && !method.getName().startsWith("set") && !method.getName().startsWith("get") && !method.getName().equals("toCopy")) {
                methodNames.add(method.getName());
            }
        }
        methodNames.add("clearDvPayeeIdType");

        IMockBuilder<CuDisbursementVoucherDocument> builder = EasyMock.createMockBuilder(CuDisbursementVoucherDocument.class).addMockedMethods(methodNames.toArray(new String[0]));
        cuDisbursementVoucherDocument = builder.createNiceMock();
    }

    @Before
    public void clearMessages() {
        KNSGlobalVariables.getMessageList().clear();
    }

    @Test
    public void testClearPayee() throws Exception {
        cuDisbursementVoucherDocument.setDvPayeeDetail(new CuDisbursementVoucherPayeeDetail());
        cuDisbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPayeeIdNumber("12345");
        cuDisbursementVoucherDocument.clearDvPayeeIdType();
        EasyMock.replay(cuDisbursementVoucherDocument);
        cuDisbursementVoucherDocument.clearPayee(KFSKeyConstants.WARNING_DV_PAYEE_NONEXISTANT_CLEARED);
        EasyMock.verify(cuDisbursementVoucherDocument);
        assertTrue("Should be valid and have one error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 1);
        assertEquals("The error message isn't what we expected.", KFSKeyConstants.WARNING_DV_PAYEE_NONEXISTANT_CLEARED, KNSGlobalVariables.getMessageList().get(0).getErrorKey());
        assertEquals("DV Payee ID Number should be cleared", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    @Test
    public void testClearFields() throws Exception {
        cuDisbursementVoucherDocument.setDisbVchrContactPhoneNumber("123-456-7890");
        cuDisbursementVoucherDocument.setDisbVchrContactEmailId("joe@msn.com");
        cuDisbursementVoucherDocument.setDisbVchrPayeeTaxControlCode("123456789");
        cuDisbursementVoucherDocument.setTripAssociationStatusCode(CULegacyTravelServiceImpl.TRIP_ASSOCIATIONS.IS_TRIP_DOC);
        cuDisbursementVoucherDocument.setTripId("12345");

        cuDisbursementVoucherDocument.clearFieldsThatShouldNotBeCopied();

        assertEquals("DV Contact Phone Number should be empty.", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDisbVchrContactPhoneNumber());
        assertEquals("DV Contact Email ID should be empty.", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDisbVchrContactEmailId());
        assertEquals("DV Payee Tax Control Code should be empty.", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDisbVchrPayeeTaxControlCode());
        assertEquals("Trip Association Status Code should be NOT Associated value.", CULegacyTravelServiceImpl.TRIP_ASSOCIATIONS.IS_NOT_TRIP_DOC, cuDisbursementVoucherDocument.getTripAssociationStatusCode());
        assertNull("Trip ID should be null", cuDisbursementVoucherDocument.getTripId());
    }

}