package edu.cornell.kfs.concur.eventnotification.rest.plain.xmlObjects;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ConcurEventNotificationDTO {
    @XmlElement(name = "Context")
    private String context;
    @XmlElement(name = "EventDateTime")
    private Date eventDateTime;
    @XmlElement(name = "EventType")
    private String eventType;
    @XmlElement(name = "ObjectType")
    private String objectType;
    @XmlElement(name = "ObjectURI")
    private String objectURI;
    @XmlElement(name = "NotificationURI")
    private String notificationURI;
    
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Date getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectURI() {
        return objectURI;
    }

    public void setObjectURI(String objectURI) {
        this.objectURI = objectURI;
    }

    public String getNotificationURI() {
        return notificationURI;
    }

    public void setNotificationURI(String notificationURI) {
        this.notificationURI = notificationURI;
    }
}
