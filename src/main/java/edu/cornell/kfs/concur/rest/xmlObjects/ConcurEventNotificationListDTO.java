package edu.cornell.kfs.concur.rest.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.concur.ConcurConstants;

@XmlRootElement(name = "NotificationList")
@XmlAccessorType(XmlAccessType.NONE)
public class ConcurEventNotificationListDTO {
    
    @XmlElement(name = "Notification", required = true)
    private List<ConcurEventNotificationDTO> concurEventNotificationDTOs;

    public List<ConcurEventNotificationDTO> getConcurEventNotificationDTOs() {
        return concurEventNotificationDTOs;
    }

    public void setConcurEventNotificationDTO(List<ConcurEventNotificationDTO> concurEventNotificationDTOs) {
        this.concurEventNotificationDTOs = concurEventNotificationDTOs;
    }
}
