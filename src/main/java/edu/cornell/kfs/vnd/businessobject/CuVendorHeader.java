package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;

import org.kuali.kfs.vnd.businessobject.VendorHeader;

public class CuVendorHeader extends VendorHeader {
	// casting issue.  not extending for now
//    private List<CuVendorSupplierDiversity> vendorSupplierDiversities;
    public CuVendorHeader() {
        super();
    }
    /* BEGIN CORNELL SPECIFIC MODIFICATIONS */
    
    private Date vendorW9ReceivedDate;

	public Date getVendorW9ReceivedDate() {
		return vendorW9ReceivedDate;
	}

	public void setVendorW9ReceivedDate(Date vendorW9ReceivedDate) {
		this.vendorW9ReceivedDate = vendorW9ReceivedDate;
	}



	/* END CORNELL SPECIFIC MODIFICATIONS */ 

}
