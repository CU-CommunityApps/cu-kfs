package edu.cornell.kfs.module.bc.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.kns.bo.BusinessObjectBase;

/**
 * This class represents an entry in the _error file for the psBudgetFeed job that
 * contains the position number and emplid for all the entries in the PS extract that were
 * invalid.
 */
public class PSPositionJobInvalidEntry extends BusinessObjectBase {

    private String positionNumber;
    private String emplid;

    /**
     * Gets the positionNumber.
     * 
     * @return positionNumber
     */
    public String getPositionNumber() {
        return positionNumber;
    }

    /**
     * Sets the positionNumber.
     * 
     * @param positionNumber
     */
    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    /**
     * Gets the emplid.
     * 
     * @return emplid
     */
    public String getEmplid() {
        return emplid;
    }

    /**
     * Sets the emplid.
     * 
     * @param emplid
     */
    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }

    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

}
