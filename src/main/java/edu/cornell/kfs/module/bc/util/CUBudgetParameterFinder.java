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
}