package edu.cornell.kfs.paymentworks.service;

import java.util.List;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public interface PaymentWorksAchService {
	
	/**
	 * Creates a Payee ACH Account object based on a DTO pulled in from PaymentWorks
	 * @param vendorUpdate
	 * @param vendorNumber
	 * @return
	 */
	PayeeACHAccount createPayeeAchAccount(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumber);
	
	/**
	 * Creates a Payee ACH Account based on another Payee ACH Account.
	 * @param payeeAchAccountOld
	 * @param routingNumber
	 * @param accountNumber
	 * @return
	 */
	PayeeACHAccount createPayeeAchAccount(PayeeACHAccount payeeAchAccountOld, String routingNumber, String accountNumber);
	
	boolean processACHUpdates(List<PaymentWorksVendorUpdatesDTO> achUpdates, boolean hasErrors);
}
