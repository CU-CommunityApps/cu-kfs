package edu.cornell.kfs.concur.eventnotification.rest.xmlObjects;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="NotificationList", namespace="http://www.concursolutions.com/api/notification/2012/06")
@XmlAccessorType(XmlAccessType.NONE)
public class ConcurEventNotificationListDTO {
    
    @XmlElement(name = "Notification")
    private List<ConcurListItemEventNotificationDTO> concurEventNotificationDTOs;

    public List<ConcurListItemEventNotificationDTO> getConcurEventNotificationDTOs() {
        return concurEventNotificationDTOs;
    }

    public void setConcurEventNotificationDTO(List<ConcurListItemEventNotificationDTO> concurEventNotificationDTOs) {
        this.concurEventNotificationDTOs = concurEventNotificationDTOs;
    }
}


