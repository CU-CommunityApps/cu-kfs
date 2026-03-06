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
        this.supplierEmails = new ArrayList<>();

        List<VendorAddress> vendorAddresses = getActiveAddressesWithEmail(vendorDetail);
        VendorAddress primaryAddress = findPrimaryAddress(
            vendorDetail.getVendorHeader().getVendorTypeCode(), vendorAddresses
        );
        List<VendorAddress> remainingAddresses = getRemainingAddresses(vendorAddresses, primaryAddress);

        supplierEmails.add(buildPrimaryEntry(primaryAddress, vendorAddresses));
        supplierEmails.add(buildSecondaryEntry(remainingAddresses, 0, 2));
        supplierEmails.add(buildSecondaryEntry(remainingAddresses, 1, 3));
    }

    private List<VendorAddress> getActiveAddressesWithEmail(VendorDetail vendorDetail) {
        if (vendorDetail.getVendorAddresses() == null) {
            return Collections.emptyList();
        }
        return vendorDetail.getVendorAddresses().stream()
            .filter(VendorAddress::isActive)
            .filter(a -> StringUtils.isNotBlank(a.getVendorAddressEmailAddress()))
            .collect(Collectors.toList());
    }

    private List<VendorAddress> getRemainingAddresses(List<VendorAddress> addresses, VendorAddress primaryAddress) {
        if (primaryAddress == null) {
            return new ArrayList<>(addresses);
        }
        return addresses.stream()
            .filter(a -> !a.getVendorAddressGeneratedIdentifier()
                .equals(primaryAddress.getVendorAddressGeneratedIdentifier()))
            .collect(Collectors.toList());
    }

    private CemiSupplierEmailSubEntry buildPrimaryEntry(VendorAddress primaryAddress, List<VendorAddress> allAddresses) {
        return allAddresses.isEmpty()
            ? new CemiSupplierEmailSubEntry()
            : new CemiSupplierEmailSubEntry(primaryAddress, supplierId, true, 1);
    }

    private CemiSupplierEmailSubEntry buildSecondaryEntry(List<VendorAddress> remaining, int index, int slot) {
        return remaining.size() > index
            ? new CemiSupplierEmailSubEntry(remaining.get(index), supplierId, false, slot)
            : new CemiSupplierEmailSubEntry();
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
