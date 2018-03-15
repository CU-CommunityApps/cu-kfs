package edu.cornell.kfs.pmw.batch.businessobject;

import java.io.Serializable;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class PaymentWorksIsoCountryToFipsCountryAssociation extends PersistableBusinessObjectBase implements Serializable{
    
    private static final long serialVersionUID = -229089609514914655L;
    
    private Integer id;
    private String isoCountryCode;
    private String fipsCountryCode;
   
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getIsoCountryCode() {
        return isoCountryCode;
    }
    
    public void setIsoCountryCode(String isoCountryCode) {
        this.isoCountryCode = isoCountryCode;
    }
    
    public String getFipsCountryCode() {
        return fipsCountryCode;
    }
    
    public void setFipsCountryCode(String fipsCountryCode) {
        this.fipsCountryCode = fipsCountryCode;
    }
    
}
