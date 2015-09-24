package edu.cornell.kfs.module.purap.document.dataaccess;

import org.kuali.kfs.module.purap.document.dataaccess.PaymentRequestDao;

public interface CuPaymentRequestDao extends PaymentRequestDao {
	
	public int countDocumentsByPurchaseOrderId(Integer poPurApId, String applicationDocumentStatus);

    String getObjectIdByPaymentRequestDocumentNumber(String documentNumber);
}
