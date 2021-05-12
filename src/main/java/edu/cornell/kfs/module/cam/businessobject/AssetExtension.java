/**
 * 
 */
package edu.cornell.kfs.module.cam.businessobject;

import java.sql.Timestamp;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

/**
 * @author kwk43
 *
 */
public class AssetExtension extends PersistableBusinessObjectBase implements PersistableBusinessObjectExtension {

    private Long capitalAssetNumber;
    private boolean serviceRateIndicator;
    private String lastScannedBy;
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
    
    public String getLastScannedBy() {
        return lastScannedBy;
    }

    public void setLastScannedBy(String lastScannedBy) {
        this.lastScannedBy = lastScannedBy;
    }

    public Timestamp getLastScannedDate() {
        return lastScannedDate;
    }

    public void setLastScannedDate(Timestamp lastScannedDate) {
        this.lastScannedDate = lastScannedDate;
    }
}
