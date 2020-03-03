package edu.cornell.kfs.concur.eventnotification.rest.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="NotificationList", namespace="http://www.concursolutions.com/api/notification/2012/06")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConcurEventNotificationListDTO {
    
    @XmlElement(name = "Notification")
    private List<ConcurEventNotificationDTO> concurEventNotificationDTOs;

    public List<ConcurEventNotificationDTO> getConcurEventNotificationDTOs() {
        return concurEventNotificationDTOs;
    }

    public void setConcurEventNotificationDTO(List<ConcurEventNotificationDTO> concurEventNotificationDTOs) {
        this.concurEventNotificationDTOs = concurEventNotificationDTOs;
    }
}


