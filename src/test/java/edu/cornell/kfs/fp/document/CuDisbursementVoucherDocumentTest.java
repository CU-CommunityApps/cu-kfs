package edu.cornell.kfs.fp.document;

import com.rsmart.kuali.kfs.fp.businessobject.DisbursementVoucherDocumentExtension;
import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetailExtension;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.businessobject.PaymentReasonCode;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPaymentReasonService;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.kns.util.MessageList;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.rice.krad.document.Document;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CuDisbursementVoucherDocumentTest {

    @Mock
    private static CuDisbursementVoucherDocument cuDisbursementVoucherDocument;
    private static VendorService vendorService;
    private static DisbursementVoucherPayeeService disbursementVoucherPayeeService;
    private static DisbursementVoucherPaymentReasonService disbursementVoucherPaymentReasonService;
    private static CuDisbursementVoucherPayeeDetail dvPayeeDetail;

    @BeforeClass
    public static void setUp() throws Exception {
        ArrayList<String> methodNames = new ArrayList<>();
        for (Method method : CuDisbursementVoucherDocument.class.getMethods()) {
            if (!Modifier.isFinal(method.getModifiers()) && !method.getName().startsWith("set") && !method.getName().startsWith("get")) {
                methodNames.add(method.getName());
            }
        }
        methodNames.add("clearDvPayeeIdType");
        IMockBuilder<CuDisbursementVoucherDocument> builder = EasyMock.createMockBuilder(CuDisbursementVoucherDocument.class).addMockedMethods(methodNames.toArray(new String[0]));

        cuDisbursementVoucherDocument = builder.createNiceMock();

        vendorService = new TestVendorService();
        disbursementVoucherPayeeService = new TestDisbursementVoucherPayeeService();
        disbursementVoucherPaymentReasonService = new TestDisbursementVoucherPaymentReasonService();

        cuDisbursementVoucherDocument.setVendorService(vendorService);
        cuDisbursementVoucherDocument.setDisbursementVoucherPayeeService(disbursementVoucherPayeeService);
        cuDisbursementVoucherDocument.setDvPymentReasonService(disbursementVoucherPaymentReasonService);
    }

    @Before
    public void clearMessages() {
        KNSGlobalVariables.getMessageList().clear();
    }

    @Test
    public void testClearInvalidPayeeValidPayeeVendor() throws Exception {
        setupPayeeDetail("0", "12345");

        cuDisbursementVoucherDocument.clearInvalidPayee();
        assertTrue("Should be valid and have no error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 0);
        assertEquals("DV Payee ID Number should not be cleared", "0", cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    @Test
    public void testClearInvalidPayeeInvalidPayeeEmployee() throws Exception {
        setupPayeeDetail("1", "23456");

        cuDisbursementVoucherDocument.clearInvalidPayee();
        assertTrue("Should be valid and have one error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 1);
        assertEquals("The error message isn't what we expected.", KFSKeyConstants.WARNING_DV_PAYEE_NONEXISTANT_CLEARED, KNSGlobalVariables.getMessageList().get(0).getErrorKey());
        assertEquals("DV Payee ID Number should be cleared", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    @Test
    public void testClearInvalidPayeeInvalidPayeeVendor() throws Exception {
        setupPayeeDetail("1", "12345");

        cuDisbursementVoucherDocument.clearInvalidPayee();
        assertTrue("Should be valid and have one error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 1);
        assertEquals("The error message isn't what we expected.", CUKFSKeyConstants.MESSAGE_DV_PAYEE_INVALID_PAYMENT_TYPE, KNSGlobalVariables.getMessageList().get(0).getErrorKey());
        assertEquals("DV Payee ID Number should be cleared", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    @Test
    public void testClearPayee() throws Exception {
        cuDisbursementVoucherDocument.clearPayee(KFSKeyConstants.WARNING_DV_PAYEE_NONEXISTANT_CLEARED);
        assertTrue("Should be valid and have one error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 1);
        assertEquals("The error message isn't what we expected.", KFSKeyConstants.WARNING_DV_PAYEE_NONEXISTANT_CLEARED, KNSGlobalVariables.getMessageList().get(0).getErrorKey());
        assertEquals("DV Payee ID Number should be cleared", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    public void setupPayeeDetail(String disbVchrPayeeIdNumber, String disbVchrVendorHeaderIdNumber) {
        CuDisbursementVoucherPayeeDetail dvPayeeDetail = new TestDisbursementVoucherPayeeDetail();
        dvPayeeDetail.setDisbVchrPayeeIdNumber(disbVchrPayeeIdNumber);
        dvPayeeDetail.setDisbVchrVendorHeaderIdNumber(disbVchrVendorHeaderIdNumber);
        cuDisbursementVoucherDocument.setDvPayeeDetail(dvPayeeDetail);
    }

    private static class TestVendorService implements VendorService {

        @Override
        public void saveVendorHeader(VendorDetail vendorDetail) {

        }

        @Override
        public VendorDetail getByVendorNumber(String s) {
            return null;
        }

        @Override
        public VendorDetail getVendorDetail(String s) {
            return null;
        }

        @Override
        public VendorDetail getVendorDetail(Integer integer, Integer integer1) {
            if (integer == 23456) {
                return null;
            } else {
                VendorDetail vendorDetail = new VendorDetail();
                vendorDetail.setVendorHeaderGeneratedIdentifier(integer);
                vendorDetail.setVendorDetailAssignedIdentifier(integer1);
                return vendorDetail;
            }
        }

        @Override
        public VendorDetail getParentVendor(Integer integer) {
            return null;
        }

        @Override
        public VendorDetail getVendorByDunsNumber(String s) {
            return null;
        }

        @Override
        public KualiDecimal getApoLimitFromContract(Integer integer, String s, String s1) {
            return null;
        }

        @Override
        public VendorAddress getVendorDefaultAddress(Integer integer, Integer integer1, String s, String s1) {
            return null;
        }

        @Override
        public VendorAddress getVendorDefaultAddress(Collection<VendorAddress> collection, String s, String s1) {
            return null;
        }

        @Override
        public boolean shouldVendorRouteForApproval(String s) {
            return false;
        }

        @Override
        public boolean equalMemberLists(List<? extends VendorRoutingComparable> list, List<? extends VendorRoutingComparable> list1) {
            return false;
        }

        @Override
        public boolean noRouteSignificantChangeOccurred(VendorDetail vendorDetail, VendorHeader vendorHeader, VendorDetail vendorDetail1, VendorHeader vendorHeader1) {
            return false;
        }

        @Override
        public boolean isVendorInstitutionEmployee(Integer integer) {
            return false;
        }

        @Override
        public boolean isVendorForeign(Integer integer) {
            return false;
        }

        @Override
        public boolean isSubjectPaymentVendor(Integer integer) {
            return false;
        }

        @Override
        public boolean isRevolvingFundCodeVendor(Integer integer) {
            return false;
        }

        @Override
        public VendorContract getVendorB2BContract(VendorDetail vendorDetail, String s) {
            return null;
        }

        @Override
        public List<Note> getVendorNotes(VendorDetail vendorDetail) {
            return null;
        }

        @Override
        public boolean isVendorContractExpired(Document document, Integer integer, VendorDetail vendorDetail) {
            return false;
        }

        @Override
        public VendorAddress getVendorDefaultAddress(Integer integer, Integer integer1, String s, String s1, boolean b) {
            return null;
        }
    }

    private static class TestDisbursementVoucherPayeeService implements DisbursementVoucherPayeeService {

        @Override
        public String getPayeeTypeDescription(String s) {
            return null;
        }

        @Override
        public boolean isEmployee(DisbursementVoucherPayeeDetail disbursementVoucherPayeeDetail) {
            return false;
        }

        @Override
        public boolean isEmployee(DisbursementPayee disbursementPayee) {
            return false;
        }

        @Override
        public boolean isVendor(DisbursementVoucherPayeeDetail disbursementVoucherPayeeDetail) {
            return false;
        }

        @Override
        public boolean isVendor(DisbursementPayee disbursementPayee) {
            return false;
        }

        @Override
        public boolean isPayeeIndividualVendor(DisbursementVoucherPayeeDetail disbursementVoucherPayeeDetail) {
            return false;
        }

        @Override
        public boolean isPayeeIndividualVendor(DisbursementPayee disbursementPayee) {
            return false;
        }

        @Override
        public void checkPayeeAddressForChanges(DisbursementVoucherDocument disbursementVoucherDocument) {

        }

        @Override
        public String getVendorOwnershipTypeCode(DisbursementPayee disbursementPayee) {
            return null;
        }

        @Override
        public Map<String, String> getFieldConversionBetweenPayeeAndVendor() {
            return null;
        }

        @Override
        public Map<String, String> getFieldConversionBetweenPayeeAndPerson() {
            return null;
        }

        @Override
        public DisbursementPayee getPayeeFromVendor(VendorDetail vendorDetail) {
            CuDisbursementPayee cuDisbursementPayee = new CuDisbursementPayee();
            cuDisbursementPayee.setPayeeIdNumber(vendorDetail.getVendorNumber());
            return cuDisbursementPayee;
        }

        @Override
        public DisbursementPayee getPayeeFromPerson(Person person) {
            return null;
        }
    }

    private static class TestDisbursementVoucherPaymentReasonService implements DisbursementVoucherPaymentReasonService {

        @Override
        public boolean isPayeeQualifiedForPayment(DisbursementPayee disbursementPayee, String s) {
            if (disbursementPayee.getPayeeIdNumber().equals("12345-1")) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean isPayeeQualifiedForPayment(DisbursementPayee disbursementPayee, String s, Collection<String> collection) {
            return false;
        }

        @Override
        public boolean isNonEmployeeTravelPaymentReason(String s) {
            return false;
        }

        @Override
        public boolean isMovingPaymentReason(String s) {
            return false;
        }

        @Override
        public boolean isPrepaidTravelPaymentReason(String s) {
            return false;
        }

        @Override
        public boolean isResearchPaymentReason(String s) {
            return false;
        }

        @Override
        public boolean isRevolvingFundPaymentReason(String s) {
            return false;
        }

        @Override
        public boolean isDecedentCompensationPaymentReason(String s) {
            return false;
        }

        @Override
        public boolean isPaymentReasonOfType(String s, String s1) {
            return false;
        }

        @Override
        public String getReserchNonVendorPayLimit() {
            return null;
        }

        @Override
        public Collection<String> getPayeeTypesByPaymentReason(String s) {
            return null;
        }

        @Override
        public PaymentReasonCode getPaymentReasonByPrimaryId(String s) {
            return null;
        }

        @Override
        public void postPaymentReasonCodeUsage(String s, MessageList messageList) {

        }

        @Override
        public boolean isTaxReviewRequired(String s) {
            return false;
        }

        @Override
        public Collection<String> getVendorOwnershipTypesByPaymentReason(String s) {
            return null;
        }
    }

    private static class TestDisbursementVoucherPayeeDetail extends CuDisbursementVoucherPayeeDetail {

        public boolean isVendor() {
            return true;
        }

        public PersistableBusinessObjectExtension getExtension() {
            return new DisbursementVoucherDocumentExtension();
        }
    }
}