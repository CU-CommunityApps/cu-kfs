package edu.cornell.kfs.fp.document.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class CuDisbursementVoucherForm extends DisbursementVoucherForm {

    private static final Logger LOG = LogManager.getLogger(CuDisbursementVoucherForm.class);
    
    /**
     * determine whether the selected payee is a student
     */
    public boolean isStudent() {
        final CuDisbursementVoucherDocument disbursementVoucherDocument = (CuDisbursementVoucherDocument) this.getDocument();
        final CuDisbursementVoucherPayeeDetail dvPayeeDetail = (CuDisbursementVoucherPayeeDetail) disbursementVoucherDocument.getDvPayeeDetail();
        return dvPayeeDetail.isStudent();
    }

    /**
     * determine whether the selected payee is an alumni
     */
    public boolean isAlumni() {
        final CuDisbursementVoucherDocument disbursementVoucherDocument = (CuDisbursementVoucherDocument) this.getDocument();
        final CuDisbursementVoucherPayeeDetail dvPayeeDetail = (CuDisbursementVoucherPayeeDetail) disbursementVoucherDocument.getDvPayeeDetail();
        return dvPayeeDetail.isAlumni();
    }

    public boolean getCanViewTrip() {
        final CuDisbursementVoucherDocument disbursementVoucherDocument = (CuDisbursementVoucherDocument)this.getDocument();
        return disbursementVoucherDocument.isLegacyTrip();
    }

    public String getTripID() {
        final CuDisbursementVoucherDocument dvd = (CuDisbursementVoucherDocument) this.getDocument();
        return dvd.isLegacyTrip() ? dvd.getTripId() : StringUtils.EMPTY;
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
        final String tripID = getIwantDocID();
        LOG.info("getIWantDocUrl() called");
        final StringBuffer url = new StringBuffer();
        url.append(SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY) + "/DocHandler.do?docId=").append(tripID).append("&command=displayDocSearchView");
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
