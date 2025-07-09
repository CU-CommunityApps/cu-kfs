package edu.cornell.kfs.fp.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.businessobject.PaymentReasonCode;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPaymentReasonService;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.krad.document.DocumentPresentationController;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.sys.service.impl.DocumentHelperServiceImpl;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.kfs.vnd.businessobject.VendorType;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.impl.identity.Person;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetailExtension;
import edu.cornell.kfs.fp.document.authorization.CuDisbursementVoucherDocumentPresentationController;
import edu.cornell.kfs.sys.util.MockPersonUtil;

public class CuDisbursementVoucherDocumentTest {

    private static final String VENDOR_PAYEE_TYPE_NAME = "Vendor";
    private static final String VENDOR_TYPE_CODE_DV = "DV";
    private static final String VENDOR_TYPE_DESCRIPTION_DV = "DISBURSEMENT VOUCHER";
    private static final String VENDOR_PAYEE_TYPE_SUFFIX = VENDOR_TYPE_CODE_DV + " - " + VENDOR_TYPE_DESCRIPTION_DV;
    private static final String VENDOR_PAYEE_TYPE_SUFFIX_FOR_DISPLAY = " (" + VENDOR_PAYEE_TYPE_SUFFIX + ")";
    private static final String VENDOR_PAYEE_TYPE_NAME_WITH_SUFFIX = VENDOR_PAYEE_TYPE_NAME + VENDOR_PAYEE_TYPE_SUFFIX_FOR_DISPLAY;

    private static CuDisbursementVoucherDocument cuDisbursementVoucherDocument;
    private static Person ccs1Person;
    private static Person mls398Person;
    private static UserSession ccs1Session;
    private static UserSession mls398Session;
    private static VendorService vendorService;
    private static DisbursementVoucherPayeeService disbursementVoucherPayeeService;
    private static TestDocumentHelperService documentHelperService;
    private static DisbursementVoucherPaymentReasonService disbursementVoucherPaymentReasonService;

    @BeforeClass
    public static void setUp() throws Exception {
        cuDisbursementVoucherDocument = Mockito.spy(new CuDisbursementVoucherDocument());
        Mockito.doAnswer(invocation -> {
            return null;
        }).when(cuDisbursementVoucherDocument).clearDvPayeeIdType();

        ccs1Person = MockPersonUtil.createMockPerson(UserNameFixture.ccs1);
        mls398Person = MockPersonUtil.createMockPerson(UserNameFixture.mls398);
        ccs1Session = MockPersonUtil.createMockUserSession(ccs1Person);
        mls398Session = MockPersonUtil.createMockUserSession(mls398Person);

        vendorService = new TestVendorService();
        disbursementVoucherPayeeService = new TestDisbursementVoucherPayeeService();
        documentHelperService = new TestDocumentHelperService();
        disbursementVoucherPaymentReasonService = new TestDisbursementVoucherPaymentReasonService();

        DisbursementVoucherDocument.setVendorService(vendorService);
        DisbursementVoucherDocument.setDisbursementVoucherPayeeService(disbursementVoucherPayeeService);
        CuDisbursementVoucherDocument.setDocumentHelperService(documentHelperService);
        DisbursementVoucherDocument.setDisbursementVoucherPaymentReasonService(disbursementVoucherPaymentReasonService);
    }

    @AfterClass
    public static void tearDown() {
        DisbursementVoucherDocument.setVendorService(null);
        CuDisbursementVoucherDocument.setDocumentHelperService(null);
        KNSGlobalVariables.getMessageList().clear();
    }

    @Before
    public void clearMessagesAndResetServices() {
        KNSGlobalVariables.getMessageList().clear();
        documentHelperService.setAuthorizedPerson(ccs1Person);
        documentHelperService.setPresentationEditModes(new HashSet<String>());
        documentHelperService.setAuthorizationEditModes(new HashSet<String>());
        documentHelperService.setupMockObjects();
    }

    @Test
    public void testClearInvalidPayeeValidPayeeVendor() throws Exception {
        setupPayeeDetail("0", "12345");

        cuDisbursementVoucherDocument.clearInvalidPayee();
        assertTrue("Should be valid and have no error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 0);
        assertEquals("DV Payee ID Number should not be cleared", "12345-0", cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    @Test
    public void testClearInvalidPayeeInvalidPayeeEmployee() throws Exception {
        setupPayeeDetail("1", "23456");

        cuDisbursementVoucherDocument.clearInvalidPayee();
        assertTrue("Should be valid and have one error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 1);
        assertEquals("The error message isn't what we expected.", FPKeyConstants.WARNING_DV_PAYEE_NON_EXISTENT_CLEARED, KNSGlobalVariables.getMessageList().get(0).getErrorKey());
        assertEquals("DV Payee ID Number should be cleared", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    @Test
    public void testClearInvalidPayeeInvalidPayeeVendor() throws Exception {
        setupPayeeDetail("1", "12345");

        cuDisbursementVoucherDocument.clearInvalidPayee();
        assertTrue("Should be valid and have one error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 1);
        assertEquals("The error message isn't what we expected.", FPKeyConstants.MESSAGE_DV_PAYEE_INVALID_PAYMENT_TYPE_CLEARED, KNSGlobalVariables.getMessageList().get(0).getErrorKey());
        assertEquals("DV Payee ID Number should be cleared", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    @Test
    public void testClearPayee() throws Exception {
        cuDisbursementVoucherDocument.setDvPayeeDetail(new CuDisbursementVoucherPayeeDetail());
        cuDisbursementVoucherDocument.getDvPayeeDetail().setDisbVchrPayeeIdNumber("12345");
        cuDisbursementVoucherDocument.clearDvPayeeIdType();
        cuDisbursementVoucherDocument.clearPayee(FPKeyConstants.WARNING_DV_PAYEE_NON_EXISTENT_CLEARED);
        assertTrue("Should be valid and have one error messages, but had " + KNSGlobalVariables.getMessageList().size(), KNSGlobalVariables.getMessageList().size() == 1);
        assertEquals("The error message isn't what we expected.", FPKeyConstants.WARNING_DV_PAYEE_NON_EXISTENT_CLEARED, KNSGlobalVariables.getMessageList().get(0).getErrorKey());
        assertEquals("DV Payee ID Number should be cleared", StringUtils.EMPTY, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    @Test
    public void testGeneratePayeeTypeDisplayName() throws Exception {
        setupPayeeDetail("0", "12345");
        assertTrue("Payee Detail Extension should have had a blank suffix", StringUtils.isBlank(getDetailExtensionFromDv().getPayeeTypeSuffix()));
        assertTrue("Payee Detail Extension should have had a blank suffix display value",
                StringUtils.isBlank(getDetailExtensionFromDv().getPayeeTypeSuffixForDisplay()));
        assertEquals("Payee Detail has the wrong Payee Type display name",
                VENDOR_PAYEE_TYPE_NAME, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeName());

        VendorDetail vendor = vendorService.getVendorDetail("12345-0");
        assertNotNull("Vendor 12345-0 should have been found", vendor);

        getDetailExtensionFromDv().setPayeeTypeSuffix(cuDisbursementVoucherDocument.createVendorPayeeTypeSuffix(vendor.getVendorHeader().getVendorType()));
        assertEquals("Payee Detail Extension has the wrong suffix defined", VENDOR_PAYEE_TYPE_SUFFIX, getDetailExtensionFromDv().getPayeeTypeSuffix());
        assertEquals("Payee Detail Extension has the wrong suffix display value",
                VENDOR_PAYEE_TYPE_SUFFIX_FOR_DISPLAY, getDetailExtensionFromDv().getPayeeTypeSuffixForDisplay());
        assertEquals("Payee Detail has the wrong Payee Type suffix-included display name",
                VENDOR_PAYEE_TYPE_NAME_WITH_SUFFIX, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeName());

        cuDisbursementVoucherDocument.setDvPayeeDetail(new TestDisbursementVoucherPayeeDetail());
        assertTrue("DV Payee ID Number should be cleared",
                StringUtils.isBlank(cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber()));
        assertTrue("Payee Type Code should be cleared",
                StringUtils.isBlank(cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode()));
        assertTrue("Payee Detail Extension should have had its suffix cleared out",
                StringUtils.isBlank(getDetailExtensionFromDv().getPayeeTypeSuffix()));
        assertTrue("Payee Detail Extension should have had a blank suffix display value due to suffix being cleared",
                StringUtils.isBlank(getDetailExtensionFromDv().getPayeeTypeSuffixForDisplay()));
        assertEquals("Payee Detail should have had an empty Payee Type display name",
                StringUtils.EMPTY, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeName());
    }

    @Test
    public void testSuccessfulPayeeSuffixRefresh() throws Exception {
        setupPayeeDetail("0", "12345");
        getDetailExtensionFromDv().setPayeeTypeSuffix(StringUtils.EMPTY);
        setupDocumentHelperWithPayeeEditMode(ccs1Person);
        assertRefreshPerformedForUser(ccs1Session, VENDOR_PAYEE_TYPE_SUFFIX);
    }

    @Test
    public void testSuccessfulPayeeSuffixRefreshToEmptyValueForInvalidVendor() throws Exception {
        setupPayeeDetail("0", "23456");
        getDetailExtensionFromDv().setPayeeTypeSuffix(VENDOR_PAYEE_TYPE_SUFFIX);
        setupDocumentHelperWithPayeeEditMode(ccs1Person);
        assertRefreshPerformedForUser(ccs1Session, StringUtils.EMPTY);
    }

    @Test
    public void testSuccessfulPayeeSuffixRefreshToEmptyValueForNonVendorPayee() throws Exception {
        setupEmployeePayeeDetail("1234567");
        getDetailExtensionFromDv().setPayeeTypeSuffix(VENDOR_PAYEE_TYPE_SUFFIX);
        setupDocumentHelperWithPayeeEditMode(ccs1Person);
        assertRefreshPerformedForUser(ccs1Session, StringUtils.EMPTY);
    }

    @Test
    public void testPayeeSuffixRefreshCanceledForUserWithoutEditPermission() throws Exception {
        setupPayeeDetail("0", "23456");
        getDetailExtensionFromDv().setPayeeTypeSuffix(VENDOR_PAYEE_TYPE_SUFFIX);
        setupDocumentHelperWithPayeeEditMode(ccs1Person);
        assertRefreshNotPerformedForUser(mls398Session);
    }

    @Test
    public void testPayeeSuffixRefreshCanceledForUserWithoutPayeeEditModeAuthorization() throws Exception {
        setupPayeeDetail("0", "23456");
        getDetailExtensionFromDv().setPayeeTypeSuffix(VENDOR_PAYEE_TYPE_SUFFIX);
        setupDocumentHelper(ccs1Person,
                createEditModeSet(KfsAuthorizationConstants.DisbursementVoucherEditMode.PAYEE_ENTRY,
                        KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY),
                createEditModeSet(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY));
        assertRefreshNotPerformedForUser(ccs1Session);
    }

    protected void assertRefreshPerformedForUser(UserSession userSession, String expectedValue) throws Exception {
        String oldSuffix = getDetailExtensionFromDv().getPayeeTypeSuffix();
        GlobalVariables.setUserSession(userSession);
        cuDisbursementVoucherDocument.refreshPayeeTypeSuffixIfPaymentIsEditable();
        assertNotEquals("Payee Type suffix should have been refreshed", oldSuffix, getDetailExtensionFromDv().getPayeeTypeSuffix());
        assertEquals("Payee Type suffix has the wrong new value", expectedValue, getDetailExtensionFromDv().getPayeeTypeSuffix());
    }

    protected void assertRefreshNotPerformedForUser(UserSession userSession) throws Exception {
        String oldSuffix = getDetailExtensionFromDv().getPayeeTypeSuffix();
        GlobalVariables.setUserSession(userSession);
        cuDisbursementVoucherDocument.refreshPayeeTypeSuffixIfPaymentIsEditable();
        assertEquals("Payee Type suffix should not have been refreshed", oldSuffix, getDetailExtensionFromDv().getPayeeTypeSuffix());
    }



    public void setupPayeeDetail(String disbVchrVendorDetailAssignedIdNumber, String disbVchrVendorHeaderIdNumber) {
        CuDisbursementVoucherPayeeDetail dvPayeeDetail = new TestDisbursementVoucherPayeeDetail();
        dvPayeeDetail.setDisbVchrPayeeIdNumber(disbVchrVendorHeaderIdNumber + "-" + disbVchrVendorDetailAssignedIdNumber);
        dvPayeeDetail.setDisbVchrVendorHeaderIdNumber(disbVchrVendorHeaderIdNumber);
        dvPayeeDetail.setDisbVchrVendorDetailAssignedIdNumber(disbVchrVendorDetailAssignedIdNumber);
        dvPayeeDetail.setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.VENDOR);
        cuDisbursementVoucherDocument.setDvPayeeDetail(dvPayeeDetail);
    }

    public void setupEmployeePayeeDetail(String employeeId) {
        CuDisbursementVoucherPayeeDetail dvPayeeDetail = new TestEmployeeDisbursementVoucherPayeeDetail();
        dvPayeeDetail.setDisbVchrPayeeIdNumber(employeeId);
        dvPayeeDetail.setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.EMPLOYEE);
        cuDisbursementVoucherDocument.setDvPayeeDetail(dvPayeeDetail);
    }

    protected void setupDocumentHelperWithPayeeEditMode(Person authorizedPerson) {
        setupDocumentHelper(authorizedPerson, createEditModeSet(KfsAuthorizationConstants.DisbursementVoucherEditMode.PAYEE_ENTRY),
                createEditModeSet(KfsAuthorizationConstants.DisbursementVoucherEditMode.PAYEE_ENTRY));
    }

    protected void setupDocumentHelper(Person authorizedPerson, Set<String> presentationEditModes, Set<String> authorizationEditModes) {
        documentHelperService.setAuthorizedPerson(authorizedPerson);
        documentHelperService.setPresentationEditModes(presentationEditModes);
        documentHelperService.setAuthorizationEditModes(authorizationEditModes);
        documentHelperService.setupMockObjects();
    }

    protected CuDisbursementVoucherPayeeDetailExtension getDetailExtensionFromDv() {
        return (CuDisbursementVoucherPayeeDetailExtension) cuDisbursementVoucherDocument.getDvPayeeDetail().getExtension();
    }

    protected Set<String> createEditModeSet(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    private static class TestVendorService implements VendorService {

        @Override
        public void saveVendorHeader(VendorDetail vendorDetail) {

        }

        @Override
        public VendorDetail getByVendorNumber(String s) {
            return getVendorDetail(s);
        }

        @Override
        public VendorDetail getVendorDetail(String s) {
            try {
                return getVendorDetail(
                        Integer.valueOf(StringUtils.substringBefore(s, "-")),
                        Integer.valueOf(StringUtils.substringAfter(s, "-")));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public VendorDetail getVendorDetail(Integer integer, Integer integer1) {
            if (integer == 23456) {
                return null;
            } else {
                VendorType vendorType = new VendorType();
                vendorType.setVendorTypeCode(VENDOR_TYPE_CODE_DV);
                vendorType.setVendorTypeDescription(VENDOR_TYPE_DESCRIPTION_DV);

                VendorHeader vendorHeader = new VendorHeader();
                vendorHeader.setVendorHeaderGeneratedIdentifier(integer);
                vendorHeader.setVendorTypeCode(VENDOR_TYPE_CODE_DV);
                vendorHeader.setVendorType(vendorType);

                VendorDetail vendorDetail = new VendorDetail();
                vendorDetail.setVendorHeaderGeneratedIdentifier(integer);
                vendorDetail.setVendorDetailAssignedIdentifier(integer1);
                vendorDetail.setVendorHeader(vendorHeader);
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
            if (StringUtils.isNotBlank(s)) {
                if (KFSConstants.PaymentPayeeTypes.VENDOR.equals(s)) {
                    return "Vendor";
                }
            }
            return StringUtils.EMPTY;
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
        
        @Override
        public boolean isPayeeSignedUpForACH(DisbursementVoucherPayeeDetail disbursementVoucherPayeeDetail) {
            return false;
        }
    }

    private static class TestDisbursementVoucherPayeeDetail extends CuDisbursementVoucherPayeeDetail {
        private CuDisbursementVoucherPayeeDetailExtension extension;

        public TestDisbursementVoucherPayeeDetail() {
            super();
            this.extension = new CuDisbursementVoucherPayeeDetailExtension();
        }

        public boolean isVendor() {
            return true;
        }

        @Override
        public PersistableBusinessObjectExtension getExtension() {
            return extension;
        }

        @Override
        public void setExtension(PersistableBusinessObjectExtension extension) {
            this.extension = (CuDisbursementVoucherPayeeDetailExtension) extension;
        }

        @Override
        protected DisbursementVoucherPayeeService getDisbursementVoucherPayeeService() {
            return disbursementVoucherPayeeService;
        }
    }

    private static class TestEmployeeDisbursementVoucherPayeeDetail extends TestDisbursementVoucherPayeeDetail {
        @Override
        public boolean isVendor() {
            return false;
        }

        @Override
        public boolean isEmployee() {
            return true;
        }
    }

    private static class TestDocumentHelperService extends DocumentHelperServiceImpl {
        private TransactionalDocumentPresentationController documentPresentationController;
        private TransactionalDocumentAuthorizer documentAuthorizer;
        private Set<String> presentationEditModes;
        private Set<String> authorizationEditModes;
        private Person authorizedPerson;

        public void setPresentationEditModes(Set<String> presentationEditModes) {
            this.presentationEditModes = presentationEditModes;
        }

        public void setAuthorizationEditModes(Set<String> authorizationEditModes) {
            this.authorizationEditModes = authorizationEditModes;
        }

        public void setAuthorizedPerson(Person authorizedPerson) {
            this.authorizedPerson = authorizedPerson;
        }



        public void setupMockObjects() {
            this.documentPresentationController = createMockPresentationController();
            this.documentAuthorizer = createMockAuthorizer();
        }

        private TransactionalDocumentPresentationController createMockPresentationController() {
            TransactionalDocumentPresentationController controller = Mockito.mock(CuDisbursementVoucherDocumentPresentationController.class);
            Mockito.when(controller.getEditModes(Mockito.any())).thenReturn(presentationEditModes);
            return controller;
        }

        private TransactionalDocumentAuthorizer createMockAuthorizer() {
            TransactionalDocumentAuthorizer documentAuthorizer = Mockito.mock(TransactionalDocumentAuthorizer.class);
            Mockito.when(documentAuthorizer.canEdit(Mockito.any(CuDisbursementVoucherDocument.class), Mockito.any())).then(this::determineEditCall);
            Mockito.when(documentAuthorizer.getEditModes(Mockito.any(CuDisbursementVoucherDocument.class), Mockito.any(),Mockito.any())).thenReturn(authorizationEditModes);
            return documentAuthorizer;
        }
        
        private boolean determineEditCall(InvocationOnMock invocation) {
            Person p = invocation.getArgument(1);
            return authorizedPerson.equals(p);
        }

        @Override
        public DocumentAuthorizer getDocumentAuthorizer(String documentType) {
            if (DisbursementVoucherConstants.DOCUMENT_TYPE_CODE.equals(documentType)) {
                return documentAuthorizer;
            }
            return null;
        }

        @Override
        public DocumentAuthorizer getDocumentAuthorizer(Document document) {
            if (document instanceof CuDisbursementVoucherDocument) {
                return documentAuthorizer;
            }
            return null;
        }

        @Override
        public DocumentPresentationController getDocumentPresentationController(String documentType) {
            if (DisbursementVoucherConstants.DOCUMENT_TYPE_CODE.equals(documentType)) {
                return documentPresentationController;
            }
            return null;
        }

        @Override
        public DocumentPresentationController getDocumentPresentationController(Document document) {
            if (document instanceof CuDisbursementVoucherDocument) {
                return documentPresentationController;
            }
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
        public void postPaymentReasonCodeUsage(String paymentReasonCode, org.kuali.kfs.kns.util.MessageList messageList) {

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

}
