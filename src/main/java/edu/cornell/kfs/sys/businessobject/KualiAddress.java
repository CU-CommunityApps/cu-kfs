package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

public class KualiAddress extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 1L;

    private String addressId;
    private String objectId;
    private String streetAddressLine1;
    private String addressType;
    private String streetAddressLine2;
    private String streetAddressLine3;
    private String city;
    private String stateCode;
    private String countryCode;
    private String zipCode;
    private Boolean active;
    // private List<String> addressLines; TODO
	
    public KualiAddress() {
		super();
        this.active = true;
		//addressType = AddressType.HOME;
	}

     @Override
     public void setActive(boolean active) {
         this.active = active;
     }

    @Override
    public boolean isActive() {
        return this.active;
    }

    // private List<String> getAddressLines() {
    // List<String> ret = new LinkedList<String>();
    // ret.add(streetAddressLine1);
    // if (!Strings.isNullOrEmpty(streetAddressLine2)) {
    // ret.add(streetAddressLine1);
    // }
    // if (!Strings.isNullOrEmpty(streetAddressLine3)) {
    // ret.add(streetAddressLine3);
    // }
    // return ret;
    // }

    public String getStreetAddressLine1() {
        return streetAddressLine1;
    }

    public void setStreetAddressLine1(String streetAddressLine1) {
        this.streetAddressLine1 = streetAddressLine1;
    }

    public String getStreetAddressLine2() {
        return streetAddressLine2;
    }

    public void setStreetAddressLine2(String streetAddressLine2) {
        this.streetAddressLine2 = streetAddressLine2;
    }

    public String getStreetAddressLine3() {
        return streetAddressLine3;
    }

    public void setStreetAddressLine3(String streetAddressLine3) {
        this.streetAddressLine3 = streetAddressLine3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
