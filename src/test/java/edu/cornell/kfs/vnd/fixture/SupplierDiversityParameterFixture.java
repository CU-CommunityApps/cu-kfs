package edu.cornell.kfs.vnd.fixture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.sys.ConfigureContext;

import edu.cornell.kfs.vnd.service.params.VendorSupplierDiversityParam;

@ConfigureContext
public enum SupplierDiversityParameterFixture {

	DISABLED("DB", new Date("12/12/3030"), true),
	HUB_ZONE("HZ", new Date("12/12/3030"), true),
	MINORITY_OWNED("MI", new Date("12/12/3030"), true);
	
	
	public final String vendorSupplierDiversityCode;
	public final Date vendorSupplierDiversityExpirationDate;
	public boolean active;
	
	private SupplierDiversityParameterFixture(String vendorSupplierDiversityCode, Date vendorSupplierDiversityExpirationDate, boolean active) {
		this.vendorSupplierDiversityCode = vendorSupplierDiversityCode;
		this.vendorSupplierDiversityExpirationDate = vendorSupplierDiversityExpirationDate;
		this.active = active;
	}
	
	public VendorSupplierDiversityParam createSupplierDiversityParameter() {
		VendorSupplierDiversityParam supplierDiversityParameter = new VendorSupplierDiversityParam();
		
		supplierDiversityParameter.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
		supplierDiversityParameter.setVendorSupplierDiversityExpirationDate(vendorSupplierDiversityExpirationDate);
		supplierDiversityParameter.setActive(active);
		
		return supplierDiversityParameter;
	}
	
	public static List<VendorSupplierDiversityParam> getAllFixtures() {
		ArrayList<VendorSupplierDiversityParam> allFixtures = new ArrayList<VendorSupplierDiversityParam>();
		
		allFixtures.add(DISABLED.createSupplierDiversityParameter());
		allFixtures.add(HUB_ZONE.createSupplierDiversityParameter());
		allFixtures.add(MINORITY_OWNED.createSupplierDiversityParameter());
		
		return allFixtures;
	}
}
