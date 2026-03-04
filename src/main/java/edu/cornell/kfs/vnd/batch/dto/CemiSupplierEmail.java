package edu.cornell.kfs.vnd.batch.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CemiVendorConstants;

public class CemiSupplierEmail {
    
    private final VendorDetail vendorDetail;
    private final String supplierId;
    private final String emailId1;
    private final String emailAddress1;
    private final String emailPrimary1;
    private final String emailUseFor1_1;
    private final String emailUseFor1_2;
    private final String emailUseFor1_3;
    private final String emailUseFor1_4;
    private final String useForTenanted1_1;
    private final String useForTenanted1_2;
    private final String useForTenanted1_3;
    private final String useForTenanted1_4;
    private final String emailId2;
    private final String emailPrimary2;
    private final String emailAddress2;
    private final String emailUseFor2_1;
    private final String emailUseFor2_2;
    private final String emailUseFor2_3;
    private final String emailUseFor2_4;
    private final String useForTenanted2_1;
    private final String useForTenanted2_2;
    private final String useForTenanted2_3;
    private final String useForTenanted2_4;
    private final String emailId3;
    private final String emailPrimary3;
    private final String emailAddress3;
    private final String emailUseFor3_1;
    private final String emailUseFor3_2;
    private final String emailUseFor3_3;
    private final String useForTenanted3_1;
    private final String useForTenanted3_2;
    private final String useForTenanted3_3;
    
    public CemiSupplierEmail(final VendorDetail vendorDetail, final String supplierId) {
        this.vendorDetail = vendorDetail;
        this.supplierId = supplierId;

        List<VendorAddress> vendorAddresses = vendorDetail.getVendorAddresses() != null
                ? vendorDetail.getVendorAddresses().stream()
                        .filter(VendorAddress::isActive)
                        .filter(a -> StringUtils.isNotBlank(a.getVendorAddressEmailAddress()))
                        .collect(Collectors.toList())
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

        this.emailId1 = addressCount >= 1 ? determineEmailId(primaryAddress, supplierId, 1) : CemiVendorConstants.EMPTY_STRING;
        this.emailAddress1 = addressCount >= 1 ? primaryAddress.getVendorAddressEmailAddress() : CemiVendorConstants.EMPTY_STRING;
        this.emailPrimary1 = addressCount >= 1 ? CemiUtils.convertToBooleanValueForFileExtract(true) : CemiVendorConstants.EMPTY_STRING;
        String addressTypeCode = addressCount >= 1 ? primaryAddress.getVendorAddressTypeCode() : CemiVendorConstants.EMPTY_STRING;
        List<String> emailUses = StringUtils.isNoneBlank(addressTypeCode) ? determineEmailUseFor(addressTypeCode) : Collections.emptyList();

        this.emailUseFor1_1 = emailUses.size() > 0 ? emailUses.get(0) : CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor1_2 = emailUses.size() > 1 ? emailUses.get(1) : CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor1_3 = emailUses.size() > 2 ? emailUses.get(2) : CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor1_4 = emailUses.size() > 3 ? emailUses.get(3) : CemiVendorConstants.EMPTY_STRING;
        
        List<String> usesForTenanted = StringUtils.isNoneBlank(addressTypeCode) ? determineUsesForTenanted(addressTypeCode) : Collections.emptyList();

        this.useForTenanted1_1 = usesForTenanted.size() > 0 ? usesForTenanted.get(0) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted1_2 = usesForTenanted.size() > 1 ? usesForTenanted.get(1) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted1_3 = usesForTenanted.size() > 2 ? usesForTenanted.get(2) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted1_4 = usesForTenanted.size() > 3 ? usesForTenanted.get(3) : CemiVendorConstants.EMPTY_STRING;
        
        // use remaining addresses
        int remainingAddressCount = remainingAddresses.size();
        
        this.emailId2 = remainingAddressCount >= 1 ? determineEmailId(remainingAddresses.get(0), supplierId, 2) : CemiVendorConstants.EMPTY_STRING;
        this.emailAddress2 = remainingAddressCount >= 1 ? remainingAddresses.get(0).getVendorAddressEmailAddress() : CemiVendorConstants.EMPTY_STRING;
        this.emailPrimary2 = remainingAddressCount >= 1 ? CemiUtils.convertToBooleanValueForFileExtract(false) : CemiVendorConstants.EMPTY_STRING;
        String addressTypeCode2 = remainingAddressCount >= 1 ? remainingAddresses.get(0).getVendorAddressTypeCode() : null;
        List<String> emailUses2 = StringUtils.isNoneBlank(addressTypeCode2) ? determineEmailUseFor(addressTypeCode2) : Collections.emptyList();
        List<String> usesForTenanted2 = StringUtils.isNoneBlank(addressTypeCode2) ? determineUsesForTenanted(addressTypeCode2) : Collections.emptyList();
        this.emailUseFor2_1 = emailUses2.size() > 0 ? emailUses2.get(0) : CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor2_2 = emailUses2.size() > 1 ? emailUses2.get(1) : CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor2_3 = emailUses2.size() > 2 ? emailUses2.get(2) : CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor2_4 = emailUses2.size() > 3 ? emailUses2.get(3) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted2_1 = usesForTenanted2.size() > 0 ? usesForTenanted2.get(0) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted2_2 = usesForTenanted2.size() > 1 ? usesForTenanted2.get(1) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted2_3 = usesForTenanted2.size() > 2 ? usesForTenanted2.get(2) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted2_4 = usesForTenanted2.size() > 3 ? usesForTenanted2.get(3) : CemiVendorConstants.EMPTY_STRING;

        this.emailId3 = remainingAddressCount >= 2 ? determineEmailId(remainingAddresses.get(1), supplierId, 3) : CemiVendorConstants.EMPTY_STRING;
        this.emailAddress3 = remainingAddressCount >= 2 ? remainingAddresses.get(1).getVendorAddressEmailAddress() : CemiVendorConstants.EMPTY_STRING;
        this.emailPrimary3 = remainingAddressCount >= 2 ? CemiUtils.convertToBooleanValueForFileExtract(false) : CemiVendorConstants.EMPTY_STRING;
        String addressTypeCode3 = remainingAddressCount >= 2 ? remainingAddresses.get(1).getVendorAddressTypeCode() : null;
        List<String> emailUses3 = StringUtils.isNoneBlank(addressTypeCode3) ? determineEmailUseFor(addressTypeCode3) : Collections.emptyList();
        List<String> usesForTenanted3 = StringUtils.isNoneBlank(addressTypeCode3) ? determineUsesForTenanted(addressTypeCode3) : Collections.emptyList();
        this.emailUseFor3_1 = emailUses3.size() > 0 ? emailUses3.get(0) : CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor3_2 = emailUses3.size() > 1 ? emailUses3.get(1) : CemiVendorConstants.EMPTY_STRING;
        this.emailUseFor3_3 = emailUses3.size() > 2 ? emailUses3.get(2) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted3_1 = usesForTenanted3.size() > 0 ? usesForTenanted3.get(0) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted3_2 = usesForTenanted3.size() > 1 ? usesForTenanted3.get(1) : CemiVendorConstants.EMPTY_STRING;
        this.useForTenanted3_3 = usesForTenanted3.size() > 2 ? usesForTenanted3.get(2) : CemiVendorConstants.EMPTY_STRING;
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

    private List<String> determineUsesForTenanted(String addressTypeCode) {
        return CemiVendorConstants.ADDRESS_TENANTED_USES.get(addressTypeCode);
    }

    private String determineEmailId(VendorAddress vendorAddress, String supplierId, int index) {
        return supplierId + "_" + vendorAddress.getVendorAddressEmailAddress() + "_" + index;
    }
    
    private static List<String> determineEmailUseFor(String addressTypeCode) {
        List<String> uses = CemiVendorConstants.ADDRESS_USES.get(addressTypeCode);
        return uses;
    }

    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }
    
    public String getSupplierId() {
        return supplierId;
    }

    public String getEmailId1() {
        return emailId1;
    }

    public String getEmailAddress1() {
        return emailAddress1;
    }

    public String getEmailPrimary1() {
        return emailPrimary1;
    }

    public String getEmailId2() {
        return emailId2;
    }

    public String getEmailAddress2() {
        return emailAddress2;
    }

    public String getEmailId3() {
        return emailId3;
    }

    public String getEmailAddress3() {
        return emailAddress3;
    }

    public String getEmailUseFor1_1() {
        return emailUseFor1_1;
    }

    public String getEmailUseFor1_2() {
        return emailUseFor1_2;
    }

    public String getEmailUseFor1_3() {
        return emailUseFor1_3;
    }

    public String getEmailUseFor1_4() {
        return emailUseFor1_4;
    }

    public String getUseForTenanted1_1() {
        return useForTenanted1_1;
    }

    public String getUseForTenanted1_2() {
        return useForTenanted1_2;
    }

    public String getUseForTenanted1_3() {
        return useForTenanted1_3;
    }

    public String getUseForTenanted1_4() {
        return useForTenanted1_4;
    }

    public String getEmailUseFor2_1() {
        return emailUseFor2_1;
    }

    public String getEmailUseFor2_2() {
        return emailUseFor2_2;
    }

    public String getEmailUseFor2_3() {
        return emailUseFor2_3;
    }

    public String getEmailUseFor2_4() {
        return emailUseFor2_4;
    }

    public String getUseForTenanted2_1() {
        return useForTenanted2_1;
    }

    public String getUseForTenanted2_2() {
        return useForTenanted2_2;
    }

    public String getUseForTenanted2_3() {
        return useForTenanted2_3;
    }

    public String getUseForTenanted2_4() {
        return useForTenanted2_4;
    }

    public String getEmailUseFor3_1() {
        return emailUseFor3_1;
    }

    public String getEmailUseFor3_2() {
        return emailUseFor3_2;
    }

    public String getEmailUseFor3_3() {
        return emailUseFor3_3;
    }

    public String getUseForTenanted3_1() {
        return useForTenanted3_1;
    }

    public String getUseForTenanted3_2() {
        return useForTenanted3_2;
    }

    public String getUseForTenanted3_3() {
        return useForTenanted3_3;
    }

    public String getEmailPrimary2() {
        return emailPrimary2;
    }

    public String getEmailPrimary3() {
        return emailPrimary3;
    }



}
