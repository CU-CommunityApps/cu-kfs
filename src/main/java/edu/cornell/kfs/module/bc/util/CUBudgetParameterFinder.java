package edu.cornell.kfs.module.bc.util;

import java.util.List;

import org.kuali.kfs.module.bc.document.web.struts.BudgetConstructionImportExportForm;
import org.kuali.kfs.module.bc.document.web.struts.OrganizationSelectionTreeAction;
import org.kuali.kfs.module.bc.util.BudgetParameterFinder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.service.ParameterService;

import edu.cornell.kfs.module.bc.CUBCParameterKeyConstants;

public class CUBudgetParameterFinder extends BudgetParameterFinder {
    private static ParameterService parameterService = SpringContext.getBean(ParameterService.class);

    /**
     * get the SIP executives setup in system parameters
     * 
     * @return the position numbers for the SIP executives
     */
    public static List<String> getSIPExecutives() {
        return parameterService.getParameterValues("KFS-BC", "All", CUBCParameterKeyConstants.SIP_EXECUTIVES);
    }

    /**
     * get the netid's from the SIP_EXPORT_EXECUTIVES parameter to determined who is allowed to export executive SIP data
     * 
     * @return the netid's from the SIP_EXPORT_EXECUTIVES parameter 
     */
    public static List<String> getSipExportExecutives() {
        return parameterService.getParameterValues("KFS-BC", "BudgetConstruction", CUBCParameterKeyConstants.SIP_EXPORT_EXECUTIVES);
    }

    /**
     * get the netid's from the SIP_EXPORT_AVAILABLE parameter to determined who is allowed to view the SIP Export View button
     * 
     * @return the netid's from the SIP_EXPORT_AVAILABLE parameter 
     */
    public static List<String> getSipExportAvailable() {
        return parameterService.getParameterValues("KFS-BC", "BudgetConstruction", CUBCParameterKeyConstants.SIP_EXPORT_AVAILABLE);
    }
    
    /**
     * get the netid's from the SIP_IMPORT_AVAILABLE parameter to determined who is allowed to view the SIP Import View button.
     * If no netId's are provided, then nobody will be able to see this button.
     * 
     * @return the netid's from the SIP_IMPORT_AVAILABLE parameter 
     */
    public static List<String> getSipImportAvailable() {
        return parameterService.getParameterValues("KFS-BC", "BudgetConstruction", CUBCParameterKeyConstants.SIP_IMPORT_AVAILABLE);
    }

    /**
     * get the mode from the SIP_IMPORT_MODE parameter to determined whether it will run in UPDATE or REPORT mode
     * 
     * @return a string value specifying either UPDATE or REPORT 
     */
    public static String getSipImportMode() {
        return parameterService.getParameterValue("KFS-BC", "BudgetConstruction", CUBCParameterKeyConstants.SIP_IMPORT_MODE);
    }

    /**
     * get the mode from the SIP_IMPORT_AWARD_CHECK parameter.  Provides the maximum percent that a SIP award can be based on the 
     * annual rate as provided by PeopleSoft and located in CU_PS_JOB_DATA in the ANNL_RT column.
     * 
     * @return a string value specifying either UPDATE or REPORT 
     */
    public static String getSipImportAwardCheck() {
        return parameterService.getParameterValue("KFS-BC", "BudgetConstruction", CUBCParameterKeyConstants.SIP_IMPORT_AWARD_CHECK);
    }


}
