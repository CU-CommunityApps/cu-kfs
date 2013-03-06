package edu.cornell.kfs.sys.mail.service;

import edu.cornell.kfs.sys.mail.KFSMailMessage;


public interface KFSMailMessageService {
		
		//KFSPTS-1460
		/**
		 * Send a MimeMessage via email with an attachment.
		 * 
		 * @param messageData
		 */
		public void send(KFSMailMessage messageData); 

	}
