/**
 * 
 */
package edu.cornell.kfs.module.cam.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;

import java.sql.Timestamp;

/**
 * @author kwk43
 *
 */
public class AssetExtension extends PersistableBusinessObjectExtensionBase {

    private Long capitalAssetNumber;
    private boolean serviceRateIndicator;
    private String lastScannedNetid;
    private Timestamp lastScannedDate;

    /**
     * @return the capitalAssetNumber
     */
    public Long getCapitalAssetNumber() {
        return capitalAssetNumber;
    }

    /**
     * @param capitalAssetNumber
     *            the capitalAssetNumber to set
     */
    public void setCapitalAssetNumber(Long capitalAssetNumber) {
        this.capitalAssetNumber = capitalAssetNumber;
    }

    /**
     * @return the serviceRateIndicator
     */
    public boolean isServiceRateIndicator() {
        return serviceRateIndicator;
    }

    /**
     * @param serviceRateIndicator
     *            the serviceRateIndicator to set
     */
    public void setServiceRateIndicator(boolean serviceRateIndicator) {
        this.serviceRateIndicator = serviceRateIndicator;
    }
    
    public String getLastScannedNetid() {
        return lastScannedNetid;
    }

    public void setLastScannedNetid(String lastScannedNetid) {
        this.lastScannedNetid = lastScannedNetid;
    }

    public Timestamp getLastScannedDate() {
        return lastScannedDate;
    }

    public void setLastScannedDate(Timestamp lastScannedDate) {
        this.lastScannedDate = lastScannedDate;
    }
}
