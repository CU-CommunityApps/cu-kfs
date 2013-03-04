package edu.cornell.kfs.pdp.mail;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.kuali.rice.kns.mail.MailMessage;

/**
 * KFSPTS-1460: 
 * This class was created to support the sending of ACH advice emails with an attachment containing bundled payment details.
 * 
 */
public class PdpMailMessage extends MailMessage {
	
    private String attachmentFilename = "";
    private String attachmentContent = "";
    private String attachmentMimeType = "";

    public PdpMailMessage() {
        super();
    }
    
    public PdpMailMessage(MailMessage ourSuperClass) {
    	super();
    	super.setBccAddresses(ourSuperClass.getBccAddresses());
    	super.setCcAddresses(ourSuperClass.getCcAddresses());
    	super.setFromAddress(ourSuperClass.getFromAddress());
    	super.setMessage(ourSuperClass.getMessage());
    	super.setSubject(ourSuperClass.getSubject());
    	super.setToAddresses(ourSuperClass.getToAddresses());
    }
    
    /**
     * @return Returns the attachmentFilename.
     */
    public String getAttachmentFilename() {
        return attachmentFilename;
    }

    /**
     * @param attachmentFilename The attachmentFilename to set.
     */
    public void setAttachmentFilename(String attachmentFilename) {
        this.attachmentFilename = attachmentFilename;
    }
    
    /**
     * @return Returns the getAttachmentContent.
     */
    public String getAttachmentContent() {
        return attachmentContent;
    }

    /**
     * @param getAttachmentContent The getAttachmentContent to set.
     */
    public void setAttachmentContent(String attachmentContent) {
        this.attachmentContent = attachmentContent;
    }
    
    /**
     * @return Returns the attachmentMimeType.
     */
    public String getAttachmentMimeType() {
        return attachmentMimeType;
    }

    /**
     * @param attachmentMimeType The attachmentMimeType to set.
     */
    public void setAttachmentMimeType(String attachmentMimeType) {
        this.attachmentMimeType = attachmentMimeType;
    }
}
