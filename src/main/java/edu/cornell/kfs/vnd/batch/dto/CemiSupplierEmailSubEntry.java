package edu.cornell.kfs.vnd.batch.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CemiVendorConstants;

public class CemiSupplierEmailSubEntry {
    
    private final VendorAddress vendorAddress;
    private final String supplierId;
    private final String emailId;
    private final String emailAddress;
    private final String emailPrimary;
    private final List<String> emailUseFor;
    private final List<String> useForTenanted;
    
    public CemiSupplierEmailSubEntry() {
        this.vendorAddress = null;
        this.supplierId = CemiVendorConstants.EMPTY_STRING;
        this.emailId = CemiVendorConstants.EMPTY_STRING;
        this.emailAddress = CemiVendorConstants.EMPTY_STRING;
        this.emailPrimary = CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor = Collections.nCopies(4, CemiVendorConstants.EMPTY_STRING);
        this.useForTenanted = Collections.nCopies(4, CemiVendorConstants.EMPTY_STRING);
    }
    
    public CemiSupplierEmailSubEntry(VendorAddress vendorAddress, String supplierId, boolean primaryAddress, int index) {
        this.vendorAddress = vendorAddress;
        this.supplierId = supplierId;
        this.emailId = determineEmailId(vendorAddress, supplierId, index);
        this.emailAddress = vendorAddress.getVendorAddressEmailAddress();
        this.emailPrimary = CemiUtils.convertToBooleanValueForFileExtract(primaryAddress);
        String addressTypeCode = vendorAddress.getVendorAddressTypeCode();
        this.emailUseFor = StringUtils.isNotBlank(addressTypeCode) ? determineEmailUseFor(addressTypeCode) : Collections.nCopies(4, CemiVendorConstants.EMPTY_STRING);
        this.useForTenanted = StringUtils.isNotBlank(addressTypeCode) ? determineUsesForTenanted(addressTypeCode) : Collections.nCopies(4, CemiVendorConstants.EMPTY_STRING);
    }    

    private List<String> determineUsesForTenanted(String addressTypeCode) {
        
        List<String> uses = CemiVendorConstants.ADDRESS_TENANTED_USES.get(addressTypeCode);
        if (uses == null) {
            return Collections.emptyList();
        }
        
        List<String> result = new ArrayList<>(uses);
        while (result.size() < 4) {
            result.add(CemiVendorConstants.EMPTY_STRING);
        }
        return result;
    }

    private String determineEmailId(VendorAddress vendorAddress, String supplierId, int index) {
        return supplierId + "_" + vendorAddress.getVendorAddressEmailAddress() + "_" + index;
    }
    
    
    private static List<String> determineEmailUseFor(String addressTypeCode) {
        List<String> uses = CemiVendorConstants.ADDRESS_USES.get(addressTypeCode);
        if (uses == null) {
            return Collections.emptyList();
        }
        
        List<String> result = new ArrayList<>(uses);
        while (result.size() < 4) {
            result.add(CemiVendorConstants.EMPTY_STRING);
        }
        return result;
    }

    public VendorAddress getVendorAddress() {
        return vendorAddress;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEmailPrimary() {
        return emailPrimary;
    }

    public List<String> getEmailUseFor() {
        return emailUseFor;
    }

    public List<String> getUseForTenanted() {
        return useForTenanted;
    }

}
