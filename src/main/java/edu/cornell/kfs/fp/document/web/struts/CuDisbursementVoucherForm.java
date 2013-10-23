package edu.cornell.kfs.fp.document.web.struts;

import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CULegacyTravelService;

public class CuDisbursementVoucherForm extends DisbursementVoucherForm {

    /**
     * determine whether the selected payee is a student
     */
    public boolean isStudent() {
        CuDisbursementVoucherDocument disbursementVoucherDocument = (CuDisbursementVoucherDocument) this.getDocument();
        return disbursementVoucherDocument.getDvPayeeDetail().isStudent();
    }

    /**
     * determine whether the selected payee is an alumni
     */
    public boolean isAlumni() {
        CuDisbursementVoucherDocument disbursementVoucherDocument = (CuDisbursementVoucherDocument) this.getDocument();
        return disbursementVoucherDocument.getDvPayeeDetail().isAlumni();
    }
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
