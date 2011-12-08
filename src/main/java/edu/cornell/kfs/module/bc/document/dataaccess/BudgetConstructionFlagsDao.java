package edu.cornell.kfs.module.bc.document.dataaccess;

public interface BudgetConstructionFlagsDao {

    /**
     * Returns a * if any record in the BC CSF tracker for this positionNumber has a N or
     * C as a funding status code, empty string otherwise.
     * 
     * @param positionNumber
     * @return a * if any record in the BC CSF tracker has a N or C as a funding status
     * code, empty string otherwise.
     */
    public String getFlagForPosition(String positionNumber);

    /**
     * Returns a * if any record in the BC CSF tracker for this emplid has a N or C as a
     * funding status code, empty string otherwise.
     * 
     * @param emplid
     * @return a * if any record in the BC CSF tracker for this emplid has a N or C as a
     * funding status code, empty string otherwise.
     */
    public String getFlagForIncumbent(String emplid);

}
