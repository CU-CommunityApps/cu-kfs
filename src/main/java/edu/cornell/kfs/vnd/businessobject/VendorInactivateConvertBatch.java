package edu.cornell.kfs.vnd.businessobject;




public class VendorInactivateConvertBatch  {

    /**
     * @author cab379
     */
    
    private String vendorId;
    private String action;
    private String reason;
    private String note;
    private String convertType;
    
    
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getConvertType() {
		return convertType;
	}
	public void setConvertType(String convertType) {
		this.convertType = convertType;
	}
    
        

    

}
