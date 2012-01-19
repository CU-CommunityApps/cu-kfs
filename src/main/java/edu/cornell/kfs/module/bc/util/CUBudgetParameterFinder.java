package edu.cornell.kfs.module.bc.util;

import java.util.List;

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

}
