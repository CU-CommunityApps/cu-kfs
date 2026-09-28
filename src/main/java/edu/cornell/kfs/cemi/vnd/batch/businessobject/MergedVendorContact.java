package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class MergedVendorContact extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private final List<VendorContact> mergedContacts;
    private final Map<String, List<VendorContact>> mergedEmails;
    private final Map<String, List<VendorContactPhoneNumber>> mergedPhoneNumbers;

    public MergedVendorContact() {
        this.mergedContacts = new ArrayList<>();
        this.mergedEmails = new LinkedHashMap<>();
        this.mergedPhoneNumbers = new LinkedHashMap<>();
    }

    public void merge(final VendorContact vendorContact) {
        Validate.notNull(vendorContact, "vendorContact cannot be null");
        mergedContacts.add(vendorContact);

        if (StringUtils.isNotBlank(vendorContact.getVendorContactEmailAddress())) {
            final String emailKey = CemiUtils.generateKeyForGroupingDuplicates(
                    vendorContact.getVendorContactEmailAddress());
            final List<VendorContact> matchingEmails = mergedEmails.computeIfAbsent(emailKey, key -> new ArrayList<>());
            matchingEmails.add(vendorContact);
        }

        for (final VendorContactPhoneNumber contactPhone : vendorContact.getVendorContactPhoneNumbers()) {
            if (!contactPhone.isActive()) {
                continue;
            }
            final String phoneKey = CemiUtils.generateKeyForGroupingDuplicates(
                    contactPhone.getVendorPhoneNumber(), contactPhone.getVendorPhoneExtensionNumber());
            final List<VendorContactPhoneNumber> matchingPhoneNumbers = mergedPhoneNumbers.computeIfAbsent(
                    phoneKey, key -> new ArrayList<>());
            matchingPhoneNumbers.add(contactPhone);
        }
    }

    public List<VendorContact> getMergedContacts() {
        return mergedContacts;
    }

    public Map<String, List<VendorContact>> getMergedEmails() {
        return mergedEmails;
    }

    public Map<String, List<VendorContactPhoneNumber>> getMergedPhoneNumbers() {
        return mergedPhoneNumbers;
    }

}
