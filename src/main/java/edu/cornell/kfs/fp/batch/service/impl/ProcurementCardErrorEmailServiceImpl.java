package edu.cornell.kfs.fp.batch.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;







import javax.mail.MessagingException;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.exception.InvalidAddressException;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.fp.batch.service.ProcurementCardErrorEmailService;

public class ProcurementCardErrorEmailServiceImpl implements ProcurementCardErrorEmailService {

	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardErrorEmailServiceImpl.class);
	
	private EmailService emailService;
	private ParameterService parameterService;
	
	public void sendErrorEmail(ArrayList<String> errorMessages) {
        BodyMailMessage message = new BodyMailMessage();

        message.setFromAddress(emailService.getDefaultFromAddress());
        message.setSubject("Error occurred during PCard batch upload process");
        message.setToAddresses(getToAddresses());
        message.setMessage(generateBody(errorMessages));

        emailService.sendMessage(message, false);

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

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	
}
