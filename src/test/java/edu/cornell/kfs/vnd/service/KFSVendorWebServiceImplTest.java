package edu.cornell.kfs.vnd.service;

import java.util.List;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;
import edu.cornell.kfs.vnd.fixture.AddressParameterFixture;
import edu.cornell.kfs.vnd.fixture.PhoneNumberParameterFixture;
import edu.cornell.kfs.vnd.fixture.SupplierDiversityParameterFixture;
import edu.cornell.kfs.vnd.fixture.VendorContactFixture;
import edu.cornell.kfs.vnd.fixture.VendorDetailExtensionFixture;
import edu.cornell.kfs.vnd.fixture.VendorDetailFixture;
import edu.cornell.kfs.vnd.service.params.VendorAddressParam;
import edu.cornell.kfs.vnd.service.params.VendorContactParam;
import edu.cornell.kfs.vnd.service.params.VendorPhoneNumberParam;
import edu.cornell.kfs.vnd.service.params.VendorSupplierDiversityParam;

@ConfigureContext
public class KFSVendorWebServiceImplTest extends KualiTestBase {

	KFSVendorWebService kfsVendorWebService;
	
	private static final String failureString = "Vendor Not Found";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		kfsVendorWebService = new KFSVendorWebServiceImpl();
	}
	
	public void testVendorWebService() {
		try {
			String result = kfsVendorWebService.retrieveKfsVendor(VendorDetailFixture.NO_SUCH_VENDOR.vendorName, "not a type");
			
			assertTrue("Attempt to retrieve vendor that doesn't exist results in failure", result.equals(failureString));
			VendorDetailFixture vdf = VendorDetailFixture.VENDOR_TO_CREATE;

			List<VendorPhoneNumberParam> thePhoneNumbers = PhoneNumberParameterFixture.ONE.getAllFixtures();
			List<VendorAddressParam> theAddresses = AddressParameterFixture.ONE.getAllFixtures();
			List<VendorSupplierDiversityParam> theSupplierDiversities = SupplierDiversityParameterFixture.getAllFixtures();
			List<VendorContactParam> theContacts = VendorContactFixture.ONE.getAllFixtures();
						
			
			VendorDetail vd = vdf.createVendorDetail();				
			VendorHeader vh = vd.getVendorHeader();
			VendorDetailExtension vdx = VendorDetailExtensionFixture.EXTENSION.createVendorDetailExtension();
			vd.setExtension(vdx);
			
			
			String vendorWebServiceResult = "";
			boolean vendorExists = false;

			//need to determine what is problematic about the fixtures before these addVendor calls will work
			//
			
//			vendorWebServiceResult = kfsVendorWebService.addVendor(vd.getVendorName(), vh.getVendorTypeCode(), vh.getVendorForeignIndicator(), vh.getVendorTaxNumber(),
//					vh.getVendorTaxTypeCode(), vh.getVendorOwnershipCode(), vd.isTaxableIndicator(), vdx.isEinvoiceVendorIndicator(), theAddresses, theContacts, thePhoneNumbers, theSupplierDiversities);
						
//			vendorWebServiceResult = kfsVendorWebService.addVendor(vdf.vendorName, vdf.vendorTypeCode, vdf.isForeign, vdf.taxNumber, vdf.taxNumberType, 
//					vdf.ownershipTypeCode, vdf.isTaxable, vdf.isEInvoice, theAddresses, theContacts, thePhoneNumbers, theSupplierDiversities);

			
			VendorDetail vdetail = VendorDetailFixture.ADD_ASSOCIATES_INC.createVendorDetail();

			vendorExists = kfsVendorWebService.vendorExists("4154-0", "VENDORID");
			
			assertTrue("Able to retrieve the vendor with vendor Id 4154-0",vendorExists);
			
			String compositeId = vdetail.getVendorHeaderGeneratedIdentifier().toString() + "-" + vdetail.getVendorDetailAssignedIdentifier().toString();
			
			vendorExists = kfsVendorWebService.vendorExists(compositeId , "VENDORID");
			
			assertTrue("Able to retrieve vendor: " + compositeId, vendorExists);
			
			vendorExists = kfsVendorWebService.vendorExists("839874281", "DUNS");
		
			assertTrue("Able to retrive vendor by DUNS number", vendorExists);			
			
			vendorWebServiceResult = kfsVendorWebService.retrieveKfsVendorByEin("999999999");

			assertTrue("Attempt to retrieve non-existent vendor by Ein fails: " + vendorWebServiceResult, vendorWebServiceResult.equals(failureString));
	
			vendorWebServiceResult = kfsVendorWebService.retrieveKfsVendorByNamePlusLastFour(VendorDetailFixture.NO_SUCH_VENDOR.vendorName, "9999");

			assertTrue("Attempt to retrieve non-existant vendor using last vendor name and last 4 digits fails:" + vendorWebServiceResult, failureString.equals(vendorWebServiceResult));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
