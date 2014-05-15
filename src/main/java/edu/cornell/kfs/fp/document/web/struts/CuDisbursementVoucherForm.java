package edu.cornell.kfs.fp.document.web.struts;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CULegacyTravelService;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

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
        CuDisbursementVoucherDocument disbursementVoucherDocument = (CuDisbursementVoucherDocument)this.getDocument();
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
        CuDisbursementVoucherDocument dvd = (CuDisbursementVoucherDocument) this.getDocument();
        boolean isAssociated = SpringContext.getBean(CULegacyTravelService.class).isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(dvd);
        if (isAssociated) {
            return dvd.getTripId();
        } else {
            return StringUtils.EMPTY;
        }
    }
    
    
 // KFSPTS-2527
    /**
     * Determines if the DV document is a DV created from and I Want doc and therefore should display the associated I Wand Doc #
     * 
     * @return true if the DV document is a DV created from and I Want doc; otherwise, return false
     */
    public boolean getCanViewIWantDoc() {
        return SpringContext.getBean(IWantDocumentService.class).isDVgeneratedByIWantDoc(this.getDocId());
    }
    
    /**
     * Gets the IwantDocUrl for the related I Want Document if DV was created from an I Want doc.
     * @param IwantDocUrl
     * @return IwantDocUrl
     */
    public String getIwantDocUrl() {
        String tripID = getIwantDocID();
        LOG.info("getIWantDocUrl() called");
        StringBuffer url = new StringBuffer();
        url.append(SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.WORKFLOW_URL_KEY) + "/DocHandler.do?docId=").append(tripID).append("&command=displayDocSearchView");
        return url.toString();
    }
    
    /**
     * Gets the IwantDocID for the related I Want Document if DV was created from an I Want doc.
     * @return IwantDocID
     */
    public String getIwantDocID() {
        return SpringContext.getBean(IWantDocumentService.class).getIWantDocIDByDVId(this.getDocId());
    }
    // end KFSPTS-2527
    
    

}
