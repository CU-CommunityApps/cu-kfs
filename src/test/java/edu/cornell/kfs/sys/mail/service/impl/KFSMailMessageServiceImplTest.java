package edu.cornell.kfs.sys.mail.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.HashSet;
import java.util.Set;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.sys.mail.KFSMailMessage;
import edu.cornell.kfs.sys.mail.service.KFSMailMessageService;

@ConfigureContext(session = ccs1)
public class KFSMailMessageServiceImplTest extends KualiTestBase {
    private KFSMailMessageService kFSMailMessageService;
    private String sender = "testSender@test.kuali.org";
    private String recipient = "testRecipient@test.kuali.org";

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        kFSMailMessageService = SpringContext.getBean(KFSMailMessageService.class);
    }

    /*
     * test with empty message data to force exception.
     * 
     */
    public void testMessageNotSend() {
        KFSMailMessage messageData = new KFSMailMessage();
        try {
            kFSMailMessageService.send(messageData);
            assertTrue("should get runtime exception ", false);            
        } catch (RuntimeException re) {
            assertTrue("should get runtime exception " + re.getMessage(), true);            
            
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
            assertTrue("should send email ", true);            
        } catch (Exception re) {
            assertTrue("should not get exceltion " + re.getMessage(), false);            
            
        }
    }
  

}
