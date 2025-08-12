package edu.cornell.kfs.fp.document.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;

public class CuDisbursementVoucherTaxServiceImplTest{
	
	private static final Integer FOREIGN_VENDOR_HEADER_ID = 1000;
	private static final Integer NON_FOREIGN_VENDOR_HEADER_ID = 2000;
	private static final String PAYMENT_REASON_THAT_REQUIRES_TAX_REVIEW_FOR_FOREIGN_VENDOR = "Z";
	private static final String PAYMENT_REASON_THAT_DOES_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR = "F";
	
	private CuDisbursementVoucherTaxServiceImpl disbursementVoucherTaxService;
	
	@Before
	public void setUp() throws Exception {
		disbursementVoucherTaxService = new CuDisbursementVoucherTaxServiceImpl();
		disbursementVoucherTaxService.setVendorService(new MockVendorService());
		disbursementVoucherTaxService.setParameterEvaluatorService(new MockParameterEvaluatorService());	
	}
	
	@Test
	public void testIsForeignVendorAndTaxReviewRequired_true()  {
		boolean isForeignVendorAndTaxReviewRequired = disbursementVoucherTaxService.isForeignVendorAndTaxReviewRequired(KFSConstants.PaymentPayeeTypes.VENDOR, PAYMENT_REASON_THAT_REQUIRES_TAX_REVIEW_FOR_FOREIGN_VENDOR, FOREIGN_VENDOR_HEADER_ID);
        assertTrue("isForeignVendorAndTaxReviewRequired method should have returned true when input is foreign vendor and payment reason that requires tax review", isForeignVendorAndTaxReviewRequired);
    }
	
	@Test
	public void testIsForeignVendorAndTaxReviewRequired_false_PayeeTypeNotVendor()  {
		boolean isForeignVendorAndTaxReviewRequired = disbursementVoucherTaxService.isForeignVendorAndTaxReviewRequired(KFSConstants.PaymentPayeeTypes.EMPLOYEE, PAYMENT_REASON_THAT_REQUIRES_TAX_REVIEW_FOR_FOREIGN_VENDOR, null);
        assertFalse("isForeignVendorAndTaxReviewRequired method should have returned false when input payee type is not Vendor", isForeignVendorAndTaxReviewRequired);
    }	
	
	@Test
	public void testIsForeignVendorAndTaxReviewRequired_false_notForeign()  {
		boolean isForeignVendorAndTaxReviewRequired = disbursementVoucherTaxService.isForeignVendorAndTaxReviewRequired(KFSConstants.PaymentPayeeTypes.VENDOR, PAYMENT_REASON_THAT_REQUIRES_TAX_REVIEW_FOR_FOREIGN_VENDOR, NON_FOREIGN_VENDOR_HEADER_ID);
        assertFalse("isForeignVendorAndTaxReviewRequired method should have returned false when input is non foreign vendor and payment reason tha trequires tax review", isForeignVendorAndTaxReviewRequired);
    }
	
	@Test
	public void testIsForeignVendorAndTaxReviewRequired_false_notPaymentReasonWithNoTaxReview()  {
		boolean isForeignVendorAndTaxReviewRequired = disbursementVoucherTaxService.isForeignVendorAndTaxReviewRequired(KFSConstants.PaymentPayeeTypes.VENDOR, PAYMENT_REASON_THAT_DOES_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR, FOREIGN_VENDOR_HEADER_ID);
        assertFalse("isForeignVendorWithTaxReviewRequired method should have returned false when input is foreign vendor and payment reason that does not require tax review", isForeignVendorAndTaxReviewRequired);
    }
	
	@Test
	public void testIsForeignVendorAndTaxReviewNotRequired_true(){
		boolean isForeignVendorAndTaxReviewNotRequired = disbursementVoucherTaxService.isForeignVendorAndTaxReviewNotRequired(KFSConstants.PaymentPayeeTypes.VENDOR, PAYMENT_REASON_THAT_DOES_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR, FOREIGN_VENDOR_HEADER_ID);
        assertTrue("isForeignVendorAndTaxReviewNotRequired method should have returned true when input is foreign vendor and payment reason that does not require tax review", isForeignVendorAndTaxReviewNotRequired);
	}
	
	@Test
	public void testIsForeignVendorAndTaxReviewNotRequired_false_PayeeTypeNotVendor(){
		boolean isForeignVendorAndTaxReviewNotRequired = disbursementVoucherTaxService.isForeignVendorAndTaxReviewNotRequired(KFSConstants.PaymentPayeeTypes.EMPLOYEE, PAYMENT_REASON_THAT_DOES_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR, null);
        assertFalse("isForeignVendorAndTaxReviewNotRequired method should have returned false when employee type is not vendor", isForeignVendorAndTaxReviewNotRequired);
	}
	
	@Test
	public void testIsForeignVendorAndTaxReviewNotRequired_false_NotForeign(){
		boolean isForeignVendorAndTaxReviewNotRequired = disbursementVoucherTaxService.isForeignVendorAndTaxReviewNotRequired(KFSConstants.PaymentPayeeTypes.VENDOR, PAYMENT_REASON_THAT_DOES_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR, NON_FOREIGN_VENDOR_HEADER_ID);
        assertFalse("isForeignVendorAndTaxReviewNotRequired method should have returned false when input is non foreign vendor and payment reason that does not require tax review", isForeignVendorAndTaxReviewNotRequired);
	}
	
	@Test
	public void testIsForeignVendorAndTaxReviewNotRequired_false_PaymentReasonRequiresTaxReview(){
		boolean isForeignVendorAndTaxReviewNotRequired = disbursementVoucherTaxService.isForeignVendorAndTaxReviewNotRequired(KFSConstants.PaymentPayeeTypes.VENDOR, PAYMENT_REASON_THAT_REQUIRES_TAX_REVIEW_FOR_FOREIGN_VENDOR, FOREIGN_VENDOR_HEADER_ID);
        assertFalse("isForeignVendorAndTaxReviewNotRequired method should have returned false when input is foreign vendor and payment reason that requires tax review", isForeignVendorAndTaxReviewNotRequired);
	}
	
    @Test
    public void testIsForeignVendor_true() {
        boolean isForeign = disbursementVoucherTaxService.isForeignVendor(KFSConstants.PaymentPayeeTypes.VENDOR,
                FOREIGN_VENDOR_HEADER_ID);
        assertTrue("isForeign method should have returned true when input is foreign vendor", isForeign);
    }

    @Test
    public void testIsForeignVendor_false_employee() {
        boolean isForeign = disbursementVoucherTaxService.isForeignVendor(KFSConstants.PaymentPayeeTypes.EMPLOYEE, null);
        assertFalse("isForeign method should have returned false when input payee type is not Vendor", isForeign);
    }
    
    @Test
    public void testIsForeignVendor_false_domestic() {
        boolean isForeign = disbursementVoucherTaxService.isForeignVendor(KFSConstants.PaymentPayeeTypes.VENDOR, 
                NON_FOREIGN_VENDOR_HEADER_ID);
        assertFalse("isForeign method should have returned false when input payee type is a domestic vendor", isForeign);
    }
	
	 private class MockVendorService implements VendorService {

		@Override
		public void saveVendorHeader(VendorDetail vendorDetail) {
			
		}

		@Override
		public VendorDetail getByVendorNumber(String vendorNumber) {
			return null;
		}

		@Override
		public VendorDetail getVendorDetail(String vendorNumber) {
			return null;
		}

		@Override
		public VendorDetail getVendorDetail(Integer headerId, Integer detailId) {
			return null;
		}

		@Override
		public VendorDetail getParentVendor(
				Integer vendorHeaderGeneratedIdentifier) {
			return null;
		}

		@Override
		public VendorDetail getVendorByDunsNumber(String vendorDunsNumber) {
			return null;
		}

		@Override
		public KualiDecimal getApoLimitFromContract(Integer contractId,
				String chart, String org) {
			return null;
		}

		@Override
		public VendorAddress getVendorDefaultAddress(Integer vendorHeaderId,
				Integer vendorDetailId, String addressType, String campus) {
			return null;
		}

		@Override
		public VendorAddress getVendorDefaultAddress(
				Collection<VendorAddress> addresses, String addressType,
				String campus) {
			return null;
		}

		@Override
		public boolean shouldVendorRouteForApproval(String documentId) {
			return false;
		}

		@Override
		public boolean equalMemberLists(
				List<? extends VendorRoutingComparable> list_a,
				List<? extends VendorRoutingComparable> list_b) {
			return false;
		}

		@Override
		public boolean noRouteSignificantChangeOccurred(VendorDetail newVDtl,
				VendorHeader newVHdr, VendorDetail oldVDtl, VendorHeader oldVHdr) {
			return false;
		}

		@Override
		public boolean isVendorInstitutionEmployee(
				Integer vendorHeaderGeneratedIdentifier) {
			return false;
		}

		@Override
		public boolean isVendorForeign(Integer vendorHeaderGeneratedIdentifier) {
			return FOREIGN_VENDOR_HEADER_ID.equals(vendorHeaderGeneratedIdentifier);
		}

		@Override
		public boolean isSubjectPaymentVendor(
				Integer vendorHeaderGeneratedIdentifier) {
			return false;
		}

		@Override
		public boolean isRevolvingFundCodeVendor(
				Integer vendorHeaderGeneratedIdentifier) {
			return false;
		}

		@Override
		public VendorContract getVendorB2BContract(VendorDetail vendorDetail,
				String campus) {
			return null;
		}

		@Override
		public List<Note> getVendorNotes(VendorDetail vendorDetail) {
			return null;
		}

		@Override
		public boolean isVendorContractExpired(Document document,
				Integer vendorContractGeneratedIdentifier,
				VendorDetail vendorDetail) {
			return false;
		}

		@Override
		public VendorAddress getVendorDefaultAddress(Integer vendorHeaderId,
				Integer vendorDetailId, String addressType, String campus,
				boolean activeCheck) {
			return null;
		}
		
		@Override
		public Collection<VendorAddress> getVendorAddresses(int vendorHeaderId, int vendorDetailId, String addressType){
		    return null;
		}
		 
	 }
	 
	 private class MockParameterEvaluatorService implements ParameterEvaluatorService {

		@Override
		public ParameterEvaluator getParameterEvaluator(
				Class componentClass, String parameterName) {
			return null;
		}

		@Override
		public ParameterEvaluator getParameterEvaluator(
				Class componentClass, String parameterName,
				String constrainedValue) {
			return new MockParameterEvaluator(componentClass, parameterName, constrainedValue);
		}

		@Override
		public ParameterEvaluator getParameterEvaluator(String namespaceCode,
				String detailTypeCode, String parameterName,
				String constrainedValue) {
			return null;
		}

		@Override
		public ParameterEvaluator getParameterEvaluator(
				Class componentClass, String parameterName,
				String constrainingValue, String constrainedValue) {
			return null;
		}

		@Override
		public ParameterEvaluator getParameterEvaluator(
				Class componentClass,
				String allowParameterName, String denyParameterName,
				String constrainingValue, String constrainedValue) {
			return null;
		}
		 
	 }
	 
	 private class MockParameterEvaluator implements ParameterEvaluator{
		 
		 private Class componentClass;
		 private String parameterName;
		 private String constrainedValue;
		 
		 public MockParameterEvaluator(Class componentClass, String parameterName, String constrainedValue) {
			 this.componentClass = componentClass;
			 this.parameterName = parameterName;
			 this.constrainedValue = constrainedValue;
		}
		 

		@Override
		public boolean evaluationSucceeds() {
			return PAYMENT_REASON_THAT_DOES_NOT_REQUIRE_TAX_REVIEW_FOR_FOREIGN_VENDOR.equalsIgnoreCase(constrainedValue);
		}

		@Override
		public boolean evaluateAndAddError(
				Class businessObjectOrDocumentClass,
				String constrainedPropertyName) {

			return false;
		}

		@Override
		public boolean evaluateAndAddError(
				Class businessObjectOrDocumentClass,
				String constrainedPropertyName, String userEditablePropertyName) {
			return false;
		}

		@Override
		public boolean constraintIsAllow() {
			return false;
		}

		@Override
		public String getParameterValuesForMessage() {
			return null;
		}

		@Override
		public String getValue() {
			return null;
		}
	 }

}
