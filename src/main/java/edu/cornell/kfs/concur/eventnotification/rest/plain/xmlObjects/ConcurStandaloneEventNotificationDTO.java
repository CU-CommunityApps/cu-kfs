package edu.cornell.kfs.concur.eventnotification.rest.plain.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Notification")
@XmlAccessorType(XmlAccessType.NONE)
public class ConcurStandaloneEventNotificationDTO extends ConcurEventNotificationDTO {

}
