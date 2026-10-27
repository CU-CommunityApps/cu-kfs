package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import java.util.HashSet;
import java.util.Set;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiSupplierClassificationMappingBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String workdayRefId;
    private Set<String> kfsSupplierDiversityCodes;

    public String getWorkdayRefId() {
        return workdayRefId;
    }

    public void setWorkdayRefId(final String workdayRefId) {
        this.workdayRefId = workdayRefId;
    }

    public Set<String> getKfsSupplierDiversityCodes() {
        if (kfsSupplierDiversityCodes == null) {
            kfsSupplierDiversityCodes = new HashSet<>();
        }
        return kfsSupplierDiversityCodes;
    }

    public void setKfsSupplierDiversityCodes(final Set<String> kfsSupplierDiversityCodes) {
        this.kfsSupplierDiversityCodes = kfsSupplierDiversityCodes;
    }

}
