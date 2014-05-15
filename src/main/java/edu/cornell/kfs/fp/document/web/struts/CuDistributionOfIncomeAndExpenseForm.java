package edu.cornell.kfs.fp.document.web.struts;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.web.struts.DistributionOfIncomeAndExpenseForm;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;
import edu.cornell.kfs.fp.document.service.CULegacyTravelService;

public class CuDistributionOfIncomeAndExpenseForm extends DistributionOfIncomeAndExpenseForm{  
    
    /**
     * determines if the DI document is a travel DI and therefore should display the associated Trip #
     * 
     * @return true if the DI document is a travel DI; otherwise, return false
     */
    public boolean getCanViewTrip() {
        CuDistributionOfIncomeAndExpenseDocument disbursementVoucherDocument = (CuDistributionOfIncomeAndExpenseDocument)this.getDocument();
        boolean canViewTrip = SpringContext.getBean(CULegacyTravelService.class).isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(disbursementVoucherDocument);
        return canViewTrip;
    }

    /**
     * 
     * @param tripID
     * @return
     */
    public String getTripUrl() {
        String tripID = this.getTripID();
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
        CuDistributionOfIncomeAndExpenseDocument did = (CuDistributionOfIncomeAndExpenseDocument) this.getDocument();
        boolean isAssociated = SpringContext.getBean(CULegacyTravelService.class).isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(did);
        if (isAssociated) {
            return did.getTripId();
        } else {
            return StringUtils.EMPTY;
        }
    }

}
