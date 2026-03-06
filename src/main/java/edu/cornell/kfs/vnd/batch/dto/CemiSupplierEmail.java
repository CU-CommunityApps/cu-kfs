package edu.cornell.kfs.vnd.batch.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.vnd.CemiVendorConstants;

public class CemiSupplierEmail {
    
    private final VendorDetail vendorDetail;
    private final String supplierId;
    
    private final List<CemiSupplierEmailSubEntry> supplierEmails;
    
    public CemiSupplierEmail(final VendorDetail vendorDetail, final String supplierId) {
        this.vendorDetail = vendorDetail;
        this.supplierId = supplierId;
        this.supplierEmails = new ArrayList<CemiSupplierEmailSubEntry>();

        List<VendorAddress> vendorAddresses = vendorDetail.getVendorAddresses() != null ? vendorDetail
                .getVendorAddresses().stream().filter(VendorAddress::isActive)
                .filter(a -> StringUtils.isNotBlank(a.getVendorAddressEmailAddress())).collect(Collectors.toList())
                : Collections.emptyList();

        final VendorAddress primaryAddress = findPrimaryAddress(vendorDetail.getVendorHeader().getVendorTypeCode(),
                vendorAddresses);
        List<VendorAddress> remainingAddresses;
        if (primaryAddress != null) {
            remainingAddresses = vendorAddresses.stream().filter(a -> !a.getVendorAddressGeneratedIdentifier()
                    .equals(primaryAddress.getVendorAddressGeneratedIdentifier())).collect(Collectors.toList());
        } else {
            remainingAddresses = vendorAddresses.stream().collect(Collectors.toList());
        }

        int addressCount = vendorAddresses.size();
        if (addressCount >= 1) {
            CemiSupplierEmailSubEntry cemiSupplierEmailSubEntry1 = new CemiSupplierEmailSubEntry(primaryAddress,
                    supplierId, true, 1);
            this.supplierEmails.add(cemiSupplierEmailSubEntry1);
        } else {
            CemiSupplierEmailSubEntry cemiSupplierEmailSubEntry1 = new CemiSupplierEmailSubEntry();
            this.supplierEmails.add(cemiSupplierEmailSubEntry1);
        }

        // use remaining addresses
        int remainingAddressCount = remainingAddresses.size();
        if (remainingAddressCount >= 1) {
            CemiSupplierEmailSubEntry cemiSupplierEmailSubEntry2 = new CemiSupplierEmailSubEntry(
                    remainingAddresses.get(0), supplierId, false, 2);
            this.supplierEmails.add(cemiSupplierEmailSubEntry2);
        } else {
            CemiSupplierEmailSubEntry cemiSupplierEmailSubEntry2 = new CemiSupplierEmailSubEntry();
            this.supplierEmails.add(cemiSupplierEmailSubEntry2);
        }

        if (remainingAddressCount >= 2) {
            CemiSupplierEmailSubEntry cemiSupplierEmailSubEntry3 = new CemiSupplierEmailSubEntry(
                    remainingAddresses.get(1), supplierId, false, 3);
            this.supplierEmails.add(cemiSupplierEmailSubEntry3);
        } else {
            CemiSupplierEmailSubEntry cemiSupplierEmailSubEntry3 = new CemiSupplierEmailSubEntry();
            this.supplierEmails.add(cemiSupplierEmailSubEntry3);
        }

    }
    
    private VendorAddress findPrimaryAddress(String vendorTypeCode, List<VendorAddress> vendorAddresses) {
        VendorAddress primaryAddress = null;
        boolean primaryFound = false;
        for(VendorAddress vendorAddress :  vendorAddresses) {
            boolean primary = determineWhetherAddressIsPrimary(vendorTypeCode, vendorAddress);
            if(primary) {
                primaryAddress = vendorAddress;
                primaryFound = true;
                break;
            }
        }
        if(!primaryFound && vendorAddresses.size() >= 1) {
            primaryAddress = vendorAddresses.get(0);
        }
        return primaryAddress;
        
    }

    private static boolean isPurchaseOrderVendor(String vendorTypeCode) {
        return (StringUtils.equals(vendorTypeCode, VendorConstants.VendorTypes.PURCHASE_ORDER));
    }
    
    private static boolean addressTypeIsActiveAndIsDefaultAndMatches(String vendorAddressType, VendorAddress vendorAddress) {
        if ((vendorAddress.getVendorAddressTypeCode()).equalsIgnoreCase(vendorAddressType)
                && vendorAddress.isActive()
                && vendorAddress.isVendorDefaultAddressIndicator()) {
            return true;
        }
        return false;
    }
    
    // For PO vendors, mark default PO address as Primary
    // For non-PO vendors, mark default Remit address as Primary
    private static boolean determineWhetherAddressIsPrimary(String vendorTypeCode, VendorAddress vendorAddress) {
        if (isPurchaseOrderVendor(vendorTypeCode) ){
            if (addressTypeIsActiveAndIsDefaultAndMatches(CemiVendorConstants.AllDefinedAddressTypes.PURCHASE_ORDER, vendorAddress)) {
                return true;
            } 
        } else if (addressTypeIsActiveAndIsDefaultAndMatches(CemiVendorConstants.AllDefinedAddressTypes.REMIT, vendorAddress)) {
                return true;
        }
        return false;
    }

    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }
    
    public String getSupplierId() {
        return supplierId;
    }

    public List<CemiSupplierEmailSubEntry> getSupplierEmails() {
        return supplierEmails;
    }

}
