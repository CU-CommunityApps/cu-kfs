package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorSupplierDiversity extends VendorSupplierDiversity {
    private static Logger LOG = Logger.getLogger(CuVendorSupplierDiversity.class);
    // TODO : include expiratindate here, so no extension needed ?
    // CU enhancement
    private Date vendorSupplierDiversityExpirationDate;
    
   public boolean isEqualForRouting(Object toCompare) {
        LOG.debug("Entering isEqualForRouting.");
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorSupplierDiversity)) {

            return false;
        }
        else {
            CuVendorSupplierDiversity vsd = (CuVendorSupplierDiversity) toCompare;

            return new EqualsBuilder().append(this.getVendorHeaderGeneratedIdentifier(), vsd.getVendorHeaderGeneratedIdentifier()).
            		append(this.getVendorSupplierDiversityCode(), vsd.getVendorSupplierDiversityCode()).
    		        append(this.getVendorSupplierDiversityExpirationDate(), vsd.getVendorSupplierDiversityExpirationDate()).isEquals();
        }
    }
public Date getVendorSupplierDiversityExpirationDate() {
	return vendorSupplierDiversityExpirationDate;
}
public void setVendorSupplierDiversityExpirationDate(
		Date vendorSupplierDiversityExpirationDate) {
	this.vendorSupplierDiversityExpirationDate = vendorSupplierDiversityExpirationDate;
}

}
