package edu.cornell.kfs.sys.mail.service.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.sys.mail.KFSMailMessage;
import edu.cornell.kfs.sys.mail.service.KFSMailMessageService;


/**
 * KFSPTS-1460: 
 * This class was created to support the sending of ACH advice emails with 
 * an attachment containing bundled payment details.  It was made generic 
 * so that it could be used by other areas when needed.
 * 
 */

public class KFSMailMessageServiceImpl implements KFSMailMessageService {
	
	 private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(KFSMailMessageServiceImpl.class);
	 
	 private String EMAIL_ADDRESS_DELIMITER = KFSConstants.SEMICOLON;
	 
	 private ConfigurationService kualiConfigurationService;

	 /**
	 * This method will use the data in the input parameter to create a MimeMessage with
	 * an attachment (when the data is included) and send it to the indicated recipients.
	 * It is the caller's responsibility to ensure that the "from" address is not null;
	 * otherwise, a RuntimeException will be thrown when the send of the email is attempted.
	 * 
	 * @param message
	 */
	public void send(KFSMailMessage messageData) {
		LOG.debug("KFSMailMessageServiceImpl.send() starting");		
						
		String host = kualiConfigurationService.getPropertyValueAsString("mail.smtp.host");		
		Properties properties = System.getProperties();		 
		properties.setProperty("mail.smtp.host", host);		 
		Session session = Session.getInstance(properties);
		
		try {
			//create the object
			MimeMessage message = new MimeMessage(session);		
						
			message.setFrom(new InternetAddress(messageData.getFromAddress()));
			if ((messageData.getFromAddress() == null) || (messageData.getFromAddress().isEmpty())) {
				throw new RuntimeException("KFSMailMessageServiceImpl.send has detected a null or blank email fromAddress.");
			}
			
			//get all of the "TO" addresses into a single string and set To Address in message header
			message.setRecipients(Message.RecipientType.TO, this.convertListToString(messageData.getToAddresses()));
			
			//get all of the "CC" addresses into a single string and set CC Address in message header
			message.setRecipients(Message.RecipientType.CC, this.convertListToString(messageData.getCcAddresses()));
			
			//get all of the "BCC" addresses into a single string and set BCC Address in message header
			message.setRecipients(Message.RecipientType.BCC, this.convertListToString(messageData.getBccAddresses()));
			
			//set Subject in header
			message.setSubject(messageData.getSubject());
			
			//create the Body part for the email and fill it in
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(messageData.getMessage());
			//Create the muli-part email and set the body
			Multipart multipartMessage = new MimeMultipart();
			multipartMessage.addBodyPart(messageBodyPart);
				
			if (messageData.getAttachmentContent() != null && !messageData.getAttachmentContent().isEmpty()) {
				//Ensure there is data for the attachment. 
				//Empty or null attachment attribute will cause an exception when email send is attempted and
				//attachment data is not required.
				
				//create the Attachment for the email in memory
				DataSource attachmentSource = new ByteArrayDataSource(messageData.getAttachmentContent(), messageData.getAttachmentMimeType());			
				BodyPart attachmentBodyPart = new MimeBodyPart();
				attachmentBodyPart.setDataHandler(new DataHandler(attachmentSource));
				attachmentBodyPart.setFileName(messageData.getAttachmentFilename());
				//set the attachment in the multi-part email
				multipartMessage.addBodyPart(attachmentBodyPart);
			}			
			message.setContent(multipartMessage);
			
			//send the email
			Transport.send(message);
			
		}
		catch (IOException ioe){
			LOG.error("KFSMailMessageServiceImpl.send() IOException creating attachment. Message not sent", ioe);
			//throwing run time exception so caller does not update the database that the ACH advice was sent.
			throw new RuntimeException("KFSMailMessageServiceImpl.send caught IOException for message Subject Line " + messageData.getSubject());
		}
		catch (MessagingException mexpt){
			LOG.error("KFSMailMessageServiceImpl.send() MessagingException. Message not sent", mexpt);
			//throwing run time exception so caller does not update the database that the ACH advice was sent.
			throw new RuntimeException("KFSMailMessageServiceImpl.send caught MessagingException for message Subject Line " + messageData.getSubject());
		}
	}
	
	
	/**
	 * Input array is expected to be valid email addresses that will be combined into
	 * a single String with a semi-colon as a delimiter.
	 * 
	 * @param arrayToMakeIntoString
	 * @return
	 */
	private String convertListToString (Set arrayToMakeIntoString) {
		
		//get all email addresses in the array into a single string
		StringBuffer emailAddresses = new StringBuffer();
		Iterator arrayInterator = arrayToMakeIntoString.iterator();
		while (arrayInterator.hasNext()){
			String address = (String) arrayInterator.next();
			emailAddresses.append(address);
			if (arrayInterator.hasNext()){
				emailAddresses.append(EMAIL_ADDRESS_DELIMITER);
			}
		}		
		return emailAddresses.toString();
	}
        
    /**
     * Sets the parameterService attribute value.
     * 
     * @param parameterService The parameterService to set.
     */
    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }
      
    
}
