package edu.cornell.kfs.paymentworks.businessobject;

import java.io.Serializable;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class PaymentWorksFieldMapping extends PersistableBusinessObjectBase implements Serializable {
	
	private static final long serialVersionUID = -2187008550220892821L;
	private long paymentWorksFieldMappingId;
	private String paymentWorksFieldName;
	private String kfsFieldName;
	
	public long getPaymentWorksFieldMappingId() {
		return paymentWorksFieldMappingId;
	}
	public void setPaymentWorksFieldMappingId(long paymentWorksFieldMappingId) {
		this.paymentWorksFieldMappingId = paymentWorksFieldMappingId;
	}
	public String getPaymentWorksFieldName() {
		return paymentWorksFieldName;
	}
	public void setPaymentWorksFieldName(String paymentWorksFieldName) {
		this.paymentWorksFieldName = paymentWorksFieldName;
	}
	public String getKfsFieldName() {
		return kfsFieldName;
	}
	public void setKfsFieldName(String kfsFieldName) {
		this.kfsFieldName = kfsFieldName;
	}
	
}
