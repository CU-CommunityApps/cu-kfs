package edu.cornell.kfs.concur.eventnotification.rest.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.concur.ConcurConstants.ConcurXmlNamespaces;
import edu.cornell.kfs.concur.eventnotification.rest.plain.xmlObjects.ConcurEventNotificationDTO;

@XmlRootElement(name = "Notification", namespace = ConcurXmlNamespaces.NOTIFICATION)
@XmlAccessorType(XmlAccessType.NONE)
public class ConcurListItemEventNotificationDTO extends ConcurEventNotificationDTO {

}
