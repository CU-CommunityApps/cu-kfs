package edu.cornell.kfs.module.bc.document.service;

import org.kuali.kfs.module.bc.businessobject.BudgetConstructionCalculatedSalaryFoundationTracker;

/**
 * This class provides methods for retrieving the flags for entries in the BC CSF tracker
 * (this is whether there is changed or new data). It also provides methods to reset the
 * flags to active '-' which means there are no new changes.
 */
public interface BudgetConstructionFlagsService {

    /**
     * Gets the flag for a particular position number. If there is any entry in the BC CSF
     * tracker table for this position number that has a N or C funding status code then
     * the returned flag for the position number will be *, otherwise empty string.
     * 
     * @param positionNumber the position number for which we are retrieving the flag
     * 
     * @return If there is any entry in the BC CSF tracker table for this position number
     * that has a N or C funding status code then the returned flag for the position
     * number will be *, otherwise empty string.
     */
    public String getFlagForPosition(String positionNumber);

    /**
     * Gets the flag for a particular emplid. If there is any entry in the BC CSF tracker
     * table for this employee that has a N or C funding status code then the returned
     * flag for the emplid will be *, otherwise empty string.
     * 
     * @param emplid the employee id for which we determine the flag
     * 
     * @return If there is any entry in the BC CSF tracker table for this employee that
     * has a N or C funding status code then the returned flag for the emplid will be *,
     * otherwise empty string.
     */
    public String getFlagForIncumbent(String emplid);

    /**
     * Sets the BC CSF tracker entry and the CSF tracker entry funding status code to
     * active '-'.
     * 
     * @param budgetConstructionCalculatedSalaryFoundationTracker
     */
    public void unflagBCCSFEntry(
            BudgetConstructionCalculatedSalaryFoundationTracker budgetConstructionCalculatedSalaryFoundationTracker);

}
