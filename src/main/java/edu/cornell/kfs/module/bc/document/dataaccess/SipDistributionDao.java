package edu.cornell.kfs.module.bc.document.dataaccess;

import java.util.List;

import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionGeneralLedger;
import org.kuali.rice.kns.util.KualiInteger;

public interface SipDistributionDao {

    /**
     * Retrieves the total of the expenditure lines for the given document number.
     * 
     * @param documentNumber
     * @param fiscalYear
     * @return the total of the expenditure lines for the given document number
     */
    public KualiInteger getExpendituresTotal(String documentNumber, int fiscalYear);

    /**
     * Retrieves the total of the revenue lines for the given document number.
     * 
     * @param documentNumber
     * @param fiscalYear
     * @return Retrieves the total of the revenue lines for the given document number.
     */
    public KualiInteger getRevenuesTotal(String documentNumber, int fiscalYear);

    /**
     * Retrieves the amount of the 2PLG entry for a given document number.
     * 
     * @param documentNumber
     * @param fiscalYear
     * @return the amount of the 2PLG entry for a given document number
     */
    public KualiInteger get2PLGAmount(String documentNumber, int fiscalYear);

    /**
     * Retrieves the total amount of the sip pool entries for a given document number.
     * 
     * @param documentNumber
     * @param sipLevelObjectCodes
     * @param fiscalYear
     * @return the total amount of the sip pool entries for a given document number
     */
    public KualiInteger getSipPoolAmount(String documentNumber, List<String> sipLevelObjectCodes, int fiscalYear);

    /**
     * Retrieves the sip pool entries for the given document numbers.
     * 
     * @param docNbrs
     * @param sipLevelObjectCodes
     * @param fiscalYear
     * @return the sip pool entries for the given document numbers
     */
    public List<PendingBudgetConstructionGeneralLedger> getSipPoolEntries(List<String> docNbrs,
            List<String> sipLevelObjectCodes, int fiscalYear);

    /**
     * Retrieves all object codes that have a level code equal to SIP.
     * 
     * @param fiscalYear
     * @return all object codes that have a level code equal to SIP
     */
    public List<String> getSipLevelObjectCodes(int fiscalYear);

}
