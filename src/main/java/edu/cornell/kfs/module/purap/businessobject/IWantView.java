package edu.cornell.kfs.module.purap.businessobject;

import java.util.List;

import org.kuali.kfs.module.purap.businessobject.AbstractRelatedView;
import org.kuali.rice.krad.bo.Note;

import edu.cornell.kfs.module.purap.CUPurapConstants;

/**
 * IWantDocument View Business Object.
 */
public class IWantView extends AbstractRelatedView {

    private static final long serialVersionUID = 1L;

    private String reqsDocId;

    public String getReqsDocId() {
        return this.reqsDocId;
    }

    public void setReqsDocId(String reqsDocId) {
        this.reqsDocId = reqsDocId;
    }

    @Override
    public String getDocumentTypeName() {
        return CUPurapConstants.IWNT_DOC_TYPE;
    }

    // Copied the methods below from the RequisitionView class.

    /**
     * The next three methods are overridden but shouldnt be! If they arent overridden, they dont show up in the tag, not sure why
     * at this point! (AAP)
     *
     * @see org.kuali.kfs.module.purap.businessobject.AbstractRelatedView#getPurapDocumentIdentifier()
     */
    @Override
    public Integer getPurapDocumentIdentifier() {
        return super.getPurapDocumentIdentifier();
    }

    @Override
    public String getDocumentIdentifierString() {
        return super.getDocumentIdentifierString();
    }

    /**
     * @see org.kuali.kfs.module.purap.businessobject.AbstractRelatedView#getNotes()
     */
    @Override
    public List<Note> getNotes() {
        return super.getNotes();
    }

    /**
     * @see org.kuali.kfs.module.purap.businessobject.AbstractRelatedView#getUrl()
     */
    @Override
    public String getUrl() {
        return super.getUrl();
    }
    
    /*
     * We need to specify this method to prevent rendering errors on the requisition document;
     * however, we've changed the IWNT doc's version of this method to return a String so as to
     * print a custom message instead of a PO identifier.
     */
    // TODO: Is it safe to return a String instead of an Integer?
    public String getPurchaseOrderIdentifier() {
        return "Please Refer to Related Documents";
    }
}