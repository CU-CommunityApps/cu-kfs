package edu.cornell.kfs.pmw.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

public class KfsVendorDataWrapper {
    
    private VendorDetail vendorDetail;
    private List<Note> vendorNotes;
    private List<String> errorMessages;
    
    public KfsVendorDataWrapper() {
        this.vendorDetail = new VendorDetail();
        this.vendorNotes = new ArrayList<Note>();
        this.errorMessages = new ArrayList<String>();
    }
    
    public KfsVendorDataWrapper(VendorDetail vendorDetail, List<Note> vendorNotes, List<String> errorMessages) {
        this.vendorDetail = vendorDetail;
        this.vendorNotes = vendorNotes;
        this.errorMessages = errorMessages;
    }
    
    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }
    public void setVendorDetail(VendorDetail vendorDetail) {
        this.vendorDetail = vendorDetail;
    }
    public List<Note> getVendorNotes() {
        return vendorNotes;
    }
    public void setVendorNotes(List<Note> vendorNotes) {
        this.vendorNotes = vendorNotes;
    }
    public List<String> getErrorMessages() {
        return errorMessages;
    }
    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
    
    public boolean noProcessingErrorsGenerated() {
         return errorMessages.isEmpty();
    }
    
}
