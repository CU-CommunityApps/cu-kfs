package edu.cornell.kfs.fp.businessobject;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.sys.KFSPropertyConstants;


/**
 * @author cab379
 *
 */
@SuppressWarnings("serial")
public class CapitalAssetInformationDetailExtendedAttribute extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {
 
	private String documentNumber;
    private Integer capitalAssetLineNumber;
    private Integer itemLineNumber;
	private String assetLocationStreetAddress;
    private String assetLocationCityName;
    private String assetLocationStateCode;
    private String assetLocationCountryCode;
    private String assetLocationZipCode;
    
    private CapitalAssetInformation capitalAssetInformation;
    
    public CapitalAssetInformationDetailExtendedAttribute () {
    	
    }
    

    /**
     * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
        m.put(KFSPropertyConstants.DOCUMENT_NUMBER, this.documentNumber);
        m.put(KFSPropertyConstants.ITEM_LINE_NUMBER, this.itemLineNumber);
        return m;
    }
    /**
     * Gets the documentNumber attribute.
     * 
     * @return Returns the documentNumber.
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the documentNumber attribute value.
     * 
     * @param documentNumber The documentNumber to set.
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * Gets the itemLineNumber attribute.
     * 
     * @return Returns the itemLineNumber.
     */
    public Integer getItemLineNumber() {
        return itemLineNumber;
    }

    /**
     * Sets the itemLineNumber attribute value.
     * 
     * @param itemLineNumber The itemLineNumber to set.
     */
    public void setItemLineNumber(Integer itemLineNumber) {
        this.itemLineNumber = itemLineNumber;
    }

   
    /**
     * Gets the assetLocationStreetAddress attribute.
     * 
     * @return Returns the assetLocationStreetAddress
     */
    public String getAssetLocationStreetAddress() {
        return assetLocationStreetAddress;
    }

    /**
     * Sets the assetLocationStreetAddress attribute.
     * 
     * @param assetLocationStreetAddress The assetLocationStreetAddress to set.
     */
    public void setAssetLocationStreetAddress(String assetLocationStreetAddress) {
        this.assetLocationStreetAddress = assetLocationStreetAddress;
    }


    /**
     * Gets the assetLocationCityName attribute.
     * 
     * @return Returns the assetLocationCityName
     */
    public String getAssetLocationCityName() {
        return assetLocationCityName;
    }

    /**
     * Sets the assetLocationCityName attribute.
     * 
     * @param assetLocationCityName The assetLocationCityName to set.
     */
    public void setAssetLocationCityName(String assetLocationCityName) {
        this.assetLocationCityName = assetLocationCityName;
    }


    /**
     * Gets the assetLocationStateCode attribute.
     * 
     * @return Returns the assetLocationStateCode
     */
    public String getAssetLocationStateCode() {
        return assetLocationStateCode;
    }

    /**
     * Sets the assetLocationStateCode attribute.
     * 
     * @param assetLocationStateCode The assetLocationStateCode to set.
     */
    public void setAssetLocationStateCode(String assetLocationStateCode) {
        this.assetLocationStateCode = assetLocationStateCode;
    }


    /**
     * Gets the assetLocationCountryCode attribute.
     * 
     * @return Returns the assetLocationCountryCode
     */
    public String getAssetLocationCountryCode() {
        return assetLocationCountryCode;
    }

    /**
     * Sets the assetLocationCountryCode attribute.
     * 
     * @param assetLocationCountryCode The assetLocationCountryCode to set.
     */
    public void setAssetLocationCountryCode(String assetLocationCountryCode) {
        this.assetLocationCountryCode = assetLocationCountryCode;
    }


    /**
     * Gets the assetLocationZipCode attribute.
     * 
     * @return Returns the assetLocationZipCode
     */
    public String getAssetLocationZipCode() {
        return assetLocationZipCode;
    }

    /**
     * Sets the assetLocationZipCode attribute.
     * 
     * @param assetLocationZipCode The assetLocationZipCode to set.
     */
    public void setAssetLocationZipCode(String assetLocationZipCode) {
        this.assetLocationZipCode = assetLocationZipCode;
    }

    /**
     * Gets the postalZipCode attribute.
     * 
     * @return Returns the postalZipCode
     */
    /**
     * Returns a map with the primitive field names as the key and the primitive values as the map value.
     * 
     * @return Map a map with the primitive field names as the key and the primitive values as the map value.
     */
    /**
     * Gets the capitalAssetInformation attribute.
     * 
     * @return Returns the capitalAssetInformation.
     */
    public CapitalAssetInformation getCapitalAssetInformation() {
        return capitalAssetInformation;
    }

    /**
     * Sets the capitalAssetInformation attribute value.
     * 
     * @param capitalAssetInformation The capitalAssetInformation to set.
     */
    public void setCapitalAssetInformation(CapitalAssetInformation capitalAssetInformation) {
        this.capitalAssetInformation = capitalAssetInformation;
    }

    /**
     * Gets the buildingSubRoomNumber attribute. 
     * @return Returns the buildingSubRoomNumber.
     */
    
    public Map<String, Object> getValuesMap() {
        Map<String, Object> simpleValues = new HashMap<String, Object>();

        simpleValues.put(KFSPropertyConstants.DOCUMENT_NUMBER, this.getDocumentNumber());
        
        return simpleValues;
    }


	public Integer getCapitalAssetLineNumber() {
		return capitalAssetLineNumber;
	}


	public void setCapitalAssetLineNumber(Integer capitalAssetLineNumber) {
		this.capitalAssetLineNumber = capitalAssetLineNumber;
	}
}
