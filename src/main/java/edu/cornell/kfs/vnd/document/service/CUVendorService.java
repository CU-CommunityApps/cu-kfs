package edu.cornell.kfs.vnd.document.service;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.document.service.VendorService;

public interface CUVendorService extends VendorService {

    /**
     * Retrieves the VendorDetail using its vendorName.
     * 
     * @param vendorName the vendor's name.
     * @return
     */
    public VendorDetail getVendorByVendorName(String vendorName);

    /**
     * Retrieves the VendorDetail using a combination of vendor name and last 4
     * digits of the tax ID.
     * 
     * @param vendorName
     * @param lastFour
     * @return
     */
    public VendorDetail getVendorByNamePlusLastFourOfTaxID(String vendorName, String lastFour);

    public VendorHeader getVendorByEin(String vendorEin);

}
