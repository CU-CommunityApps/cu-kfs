package edu.cornell.kfs.concur.eventnotification.rest.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.concur.ConcurConstants.ConcurXmlNamespaces;
import edu.cornell.kfs.concur.eventnotification.rest.plain.xmlObjects.ConcurEventNotificationDTO;

@XmlRootElement(name = "Notification", namespace = ConcurXmlNamespaces.NOTIFICATION)
@XmlAccessorType(XmlAccessType.NONE)
public class ConcurListItemEventNotificationDTO extends ConcurEventNotificationDTO {

}
