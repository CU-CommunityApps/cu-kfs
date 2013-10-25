package edu.cornell.kfs.vnd.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorContact extends VendorContact implements VendorRoutingComparable  {

    public boolean isEqualForRouting(Object toCompare) {
        // KFSPTS-2055
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorContact)) {

            return false;
        }
        else {
            VendorContact vndContact = (VendorContact) toCompare;

            boolean eq = new EqualsBuilder().append(
                    this.getVendorContactTypeCode(), vndContact.getVendorContactTypeCode()).append(
                    this.getVendorContactGeneratedIdentifier(), vndContact.getVendorContactGeneratedIdentifier()).append(
                    this.getVendorAttentionName(), vndContact.getVendorAttentionName()).append(
                    this.getVendorAddressInternationalProvinceName(), vndContact.getVendorAddressInternationalProvinceName()).append(
                    this.getVendorCityName(), vndContact.getVendorCityName()).append(
                    this.getVendorContactCommentText(), vndContact.getVendorContactCommentText()).append(
                    this.getVendorContactEmailAddress(), vndContact.getVendorContactEmailAddress()).append(
                    this.getVendorContactName(), vndContact.getVendorContactName()).append(
                    this.getVendorCountryCode(), vndContact.getVendorCountryCode()).append(
                    this.getVendorLine1Address(), vndContact.getVendorLine1Address()).append(
                    this.getVendorLine2Address(), vndContact.getVendorLine2Address()).append(
                    this.getVendorStateCode(), vndContact.getVendorStateCode()).append(
                    this.getVendorZipCode(), vndContact.getVendorZipCode()).append(
                    this.isActive(), vndContact.isActive()).isEquals();
            eq &= SpringContext.getBean(VendorService.class).equalMemberLists(this.getCuVendorContactPhoneNumbers(), ((CuVendorContact)vndContact).getCuVendorContactPhoneNumbers());
            return eq;
        }
    }

    private List<CuVendorContactPhoneNumber> getCuVendorContactPhoneNumbers() {
        List<CuVendorContactPhoneNumber> contactPhoneNumbers = new ArrayList<CuVendorContactPhoneNumber>();
        for (VendorContactPhoneNumber phoneNum : getVendorContactPhoneNumbers()) {
            contactPhoneNumbers.add((CuVendorContactPhoneNumber)phoneNum);
        }
        return contactPhoneNumbers;
    }

}
