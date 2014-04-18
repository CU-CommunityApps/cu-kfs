package edu.cornell.kfs.fp.document.web.struts;

import org.kuali.kfs.fp.document.web.struts.DistributionOfIncomeAndExpenseForm;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.document.service.CULegacyTravelService;

public class CuDistributionOfIncomeAndExpenseForm extends DistributionOfIncomeAndExpenseForm{  
    
    /**
     * determines if the DI document is a travel DI and therefore should display the associated Trip #
     * 
     * @return true if the DI document is a travel DI; otherwise, return false
     */
    public boolean getCanViewTrip() {
        return SpringContext.getBean(CULegacyTravelService.class).isLegacyTravelGeneratedKfsDocument(this.getDocId());
    }

    /**
     * 
     * @param tripID
     * @return
     */
    public String getTripUrl() {
        String tripID = SpringContext.getBean(CULegacyTravelService.class).getLegacyTripID(this.getDocId());
        LOG.info("getTripUrl() called");
        StringBuffer url = new StringBuffer();
        url.append(SpringContext.getBean(CULegacyTravelService.class).getTravelUrl());
        url.append("/navigation?form_action=0&tripid=").append(tripID).append("&link=true");
        return url.toString();
    }
    
    /**
     * 
     * @return
     */
    public String getTripID() {
        return SpringContext.getBean(CULegacyTravelService.class).getLegacyTripID(this.getDocId());
    }

}
