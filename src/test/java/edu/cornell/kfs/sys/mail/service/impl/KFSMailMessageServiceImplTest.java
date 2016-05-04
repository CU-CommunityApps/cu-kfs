package edu.cornell.kfs.sys.mail.service.impl;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.sys.mail.KFSMailMessage;
import edu.cornell.kfs.sys.mail.service.KFSMailMessageService;

public class KFSMailMessageServiceImplTest extends KualiTestBase {
    private KFSMailMessageService kFSMailMessageService;
    private String sender = "testSender@test.kuali.org";
    private String recipient = "testRecipient@test.kuali.org";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        kFSMailMessageService = new testableKFSMailMessageServiceImpl();
        ((testableKFSMailMessageServiceImpl)kFSMailMessageService).setKualiConfigurationService(new testableConfigurationService());
    }

    /*
     * test with empty message data to force exception.
     * 
     */
    public void testMessageNotSend() {
        KFSMailMessage messageData = new KFSMailMessage();
        try {
            kFSMailMessageService.send(messageData);
            assertTrue("Expected a runtime exception, but did not happen.", false);            
        } catch (RuntimeException re) {
            assertTrue("Expected a runtime exception, and we did: " + re.getMessage(), true);            
        }
    }
  
    /*
     * TODO : Tested fine if local smtp is set up. if 'develop' has no smtp set up, then comment out this.
     */
    public void testMessageSend() {
        Set<String> addresses = new HashSet<String>();
        addresses.add(recipient);
        KFSMailMessage messageData = new KFSMailMessage();
        messageData.setFromAddress(sender);
        messageData.setSubject("KFSMailMessageServiceImplTest");
        messageData.setToAddresses(addresses);
        messageData.setMessage("KFSMailMessageServiceImplTest - testMessageSend");
        try {
            kFSMailMessageService.send(messageData);
            assertTrue("Mail was successfully sent", true);            
        } catch (Exception re) {
            assertTrue("Mail should have been sent, but was not: " + re.getMessage(), false);            
        }
    }
    
    private class testableConfigurationService implements ConfigurationService {
		@Override
		public String getPropertyValueAsString(String key) {
			// TODO Auto-generated method stub
			return "does_not_matter";
		}
		@Override
		public boolean getPropertyValueAsBoolean(String key) {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public Map<String, String> getAllProperties() {
			// TODO Auto-generated method stub
			return null;
		}
    }
    
    private class testableKFSMailMessageServiceImpl extends KFSMailMessageServiceImpl {
    	@Override
    	protected void transportMessage(MimeMessage message) throws MessagingException {
    		//We don't care that the message actually sends or not, we care about business rules earlier on.
    	}
    }

}
