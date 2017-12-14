package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

import java.util.LinkedHashMap;
import java.util.UUID;

public class KualiAddress extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 1L;

    private String addressId;
    private String objectId;
    private String streetAddress;
    private String addressTypeCode;
    private String streetAddressExtra;
    private String city;
    private String stateCode;
    private String countryCode;
    private String zipCode;
    private Boolean active;
	
    public KualiAddress() {
		super();
        this.active = true;
        this.objectId = UUID.randomUUID().toString();
	}

     @Override
     public void setActive(boolean active) {
         this.active = active;
     }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getStreetAddressExtra() {
        return streetAddressExtra;
    }

    public void setStreetAddressExtra(String streetAddressExtra) {
        this.streetAddressExtra = streetAddressExtra;
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

    public String getAddressTypeCode() {
        return addressTypeCode;
    }

    public void setAddressTypeCode(String addressTypeCode) {
        this.addressTypeCode = addressTypeCode;
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

    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put("addressTypeCode", this.addressTypeCode);

        return m;
    }
}
