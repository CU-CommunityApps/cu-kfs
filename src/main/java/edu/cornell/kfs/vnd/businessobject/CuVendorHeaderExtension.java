package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;

public class CuVendorHeaderExtension extends PersistableBusinessObjectExtensionBase {
    private static Logger LOG = Logger.getLogger(CuVendorHeaderExtension.class);

    private Integer vendorHeaderGeneratedIdentifier;
    private Date vendorW9ReceivedDate;

    public Date getVendorW9ReceivedDate() {
        return vendorW9ReceivedDate;
    }

    public void setVendorW9ReceivedDate(Date vendorW9ReceivedDate) {
        this.vendorW9ReceivedDate = vendorW9ReceivedDate;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(
            Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public boolean isEqualForRouting(CuVendorHeaderExtension vhe) {
        LOG.debug("Entering isEqualForRouting.");
        return new EqualsBuilder()
                .append(getVendorW9ReceivedDate(), vhe.getVendorW9ReceivedDate())
                .isEquals();
    }

}
