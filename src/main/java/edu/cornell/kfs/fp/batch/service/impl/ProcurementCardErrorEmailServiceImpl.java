package edu.cornell.kfs.fp.batch.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;







import javax.mail.MessagingException;

import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.exception.InvalidAddressException;
import org.kuali.rice.krad.service.MailService;

import edu.cornell.kfs.fp.batch.service.ProcurementCardErrorEmailService;

public class ProcurementCardErrorEmailServiceImpl implements ProcurementCardErrorEmailService {

	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardErrorEmailServiceImpl.class);
	
	private MailService mailService;
	private ParameterService parameterService;
	
	public void sendErrorEmail(ArrayList<String> errorMessages) {
		MailMessage message = new MailMessage();
		
		message.setFromAddress(mailService.getBatchMailingList());
		message.setSubject("Error occurred during PCard batch upload process");
		message.setToAddresses(getToAddresses());
		message.setMessage(generateBody(errorMessages));
		
        try {
            mailService.sendMessage(message);
        }
        catch (InvalidAddressException e) {
            LOG.error("sendErrorEmail() Invalid email address.  Message not sent", e);
        } catch (MessagingException e) {
            LOG.error("sendErrorEmail() unable to send msessage.  Message not sent", e);
		}
	}
	
	private Set<String> getToAddresses() {
		Set<String> addresses = new HashSet<String>();
		addresses.add(parameterService.getParameterValueAsString("KFS-FP", "ProcurementCard", "PCARD_UPLOAD_ERROR_EMAIL_ADDR"));
		return addresses;
	}

	private String generateBody(ArrayList<String> errorMessages) {
		StringBuffer sb = new StringBuffer();
		sb.append("Errors occured during the PCard upload process.");
		sb.append("\r\n\r\n");
		sb.append("Error details: \r\n\r\n");
		for (String message: errorMessages) {
			sb.append(message);
			sb.append("\r\n");
		}
		return sb.toString();
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	
}
