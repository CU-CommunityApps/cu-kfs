package edu.cornell.kfs.concur.eventnotification.rest.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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


