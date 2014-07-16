package edu.cornell.kfs.vnd.businessobject;


public class VendorBatchAdditionalNote {

	private String initiator;
	private String dvReason;
	private String doBusinessAs;
	
    public VendorBatchAdditionalNote(String[] note) {
    	initiator = note[0];
    	dvReason = note[1];
    	doBusinessAs = note[2];
    }

	public String getInitiator() {
		return initiator;
	}
	public void setInitiator(String initiator) {
		this.initiator = initiator;
	}
	public String getDvReason() {
		return dvReason;
	}
	public void setDvReason(String dvReason) {
		this.dvReason = dvReason;
	}
	public String getDoBusinessAs() {
		return doBusinessAs;
	}
	public void setDoBusinessAs(String doBusinessAs) {
		this.doBusinessAs = doBusinessAs;
	}
}
