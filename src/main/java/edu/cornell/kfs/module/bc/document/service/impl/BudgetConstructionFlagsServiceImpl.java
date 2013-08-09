package edu.cornell.kfs.module.bc.document.service.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.bc.businessobject.BudgetConstructionCalculatedSalaryFoundationTracker;
import org.kuali.kfs.module.bc.businessobject.CalculatedSalaryFoundationTracker;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCPropertyConstants;
import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionFlagsDao;
import edu.cornell.kfs.module.bc.document.service.BudgetConstructionFlagsService;

public class BudgetConstructionFlagsServiceImpl implements BudgetConstructionFlagsService {

    protected BudgetConstructionFlagsDao budgetConstructionFlagsDao;
    protected BusinessObjectService businessObjectService;

    /**
     * 
     * @see edu.cornell.kfs.module.bc.document.service.BudgetConstructionFlagsService#getFlagForPosition(java.lang.String)
     */
    public String getFlagForPosition(String positionNumber) {

        return budgetConstructionFlagsDao.getFlagForPosition(positionNumber);
    }

    /**
     * 
     * @see edu.cornell.kfs.module.bc.document.service.BudgetConstructionFlagsService#getFlagForIncumbent(java.lang.String)
     */
    public String getFlagForIncumbent(String emplid) {

        return budgetConstructionFlagsDao.getFlagForIncumbent(emplid);
    }


    /**
     * 
     * @see edu.cornell.kfs.module.bc.document.service.BudgetConstructionFlagsService#unflagBCCSFEntry(org.kuali.kfs.module.bc.businessobject.BudgetConstructionCalculatedSalaryFoundationTracker)
     */
    public void unflagBCCSFEntry(
            BudgetConstructionCalculatedSalaryFoundationTracker budgetConstructionCalculatedSalaryFoundationTracker) {

        //undate BC CSF Entry
        BudgetConstructionCalculatedSalaryFoundationTracker retrievedBCEntry = (BudgetConstructionCalculatedSalaryFoundationTracker) businessObjectService
                .retrieve(budgetConstructionCalculatedSalaryFoundationTracker);
        if (ObjectUtils.isNotNull(retrievedBCEntry)) {
            retrievedBCEntry.setCsfFundingStatusCode(CUBCConstants.StatusFlag.ACTIVE.getFlagValue());
            businessObjectService.save(retrievedBCEntry);
        }

        // update CSF entry

        //keys
        Map<String, Object> keyFields = new HashMap<String, Object>();
        keyFields.put(
                CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.UNIVERSITY_FISCAL_YEAR,
                budgetConstructionCalculatedSalaryFoundationTracker.getUniversityFiscalYear());
        keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.CHART_OF_ACCOUNTS,
                budgetConstructionCalculatedSalaryFoundationTracker.getChartOfAccountsCode());
        keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.ACCOUNT_NBR,
                budgetConstructionCalculatedSalaryFoundationTracker.getAccountNumber());
        keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.SUB_ACCOUNT_NBR,
                budgetConstructionCalculatedSalaryFoundationTracker.getSubAccountNumber());
        keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.FIN_OBJ_CD,
                budgetConstructionCalculatedSalaryFoundationTracker.getFinancialObjectCode());
        keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.FIN_SUB_OBJ_CD,
                budgetConstructionCalculatedSalaryFoundationTracker.getFinancialSubObjectCode());
        keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.EMPLID,
                budgetConstructionCalculatedSalaryFoundationTracker.getEmplid());
        keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.POSITION_NBR,
                budgetConstructionCalculatedSalaryFoundationTracker.getPositionNumber());
        keyFields.put(
                CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.CREATE_TIMESTAMP,
                 new Timestamp(0));

        List<CalculatedSalaryFoundationTracker> retrievedCSFEntries = (List<CalculatedSalaryFoundationTracker>) businessObjectService
                .findMatching(CalculatedSalaryFoundationTracker.class, keyFields);

        if (retrievedCSFEntries != null && retrievedCSFEntries.size() > 0) {
            for (CalculatedSalaryFoundationTracker retrievedEntry : retrievedCSFEntries) {

                if (ObjectUtils.isNotNull(retrievedEntry)) {
                    retrievedEntry.setCsfFundingStatusCode(CUBCConstants.StatusFlag.ACTIVE.getFlagValue());
                    businessObjectService.save(retrievedEntry);
                }
            }

        }
    }
    
    /**
     * Gets budgetConstructionFlagsDao.
     * 
     * @return budgetConstructionFlagsDao
     */
    public BudgetConstructionFlagsDao getBudgetConstructionFlagsDao() {
        return budgetConstructionFlagsDao;
    }

    /**
     * Sets the budgetConstructionFlagsDao.
     * 
     * @param budgetConstructionFlagsDao
     */
    public void setBudgetConstructionFlagsDao(BudgetConstructionFlagsDao budgetConstructionFlagsDao) {
        this.budgetConstructionFlagsDao = budgetConstructionFlagsDao;
    }

    /**
     * Gets the businessObjectService.
     * 
     * @return businessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService.
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
