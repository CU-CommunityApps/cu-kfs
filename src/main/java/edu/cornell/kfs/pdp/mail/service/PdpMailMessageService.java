package edu.cornell.kfs.pdp.mail.service;

import edu.cornell.kfs.pdp.mail.PdpMailMessage;

public interface PdpMailMessageService {
	
	//KFSPTS-1460
	/**
	 * Send a MimeMessage via email with an attachment.
	 * 
	 * @param messageData
	 */
	public void send(PdpMailMessage messageData); 

}

