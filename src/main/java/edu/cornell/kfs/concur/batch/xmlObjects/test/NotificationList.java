package edu.cornell.kfs.concur.batch.xmlObjects.test;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "notification"
})
@XmlRootElement(name = "NotificationList")
public class NotificationList {

    @XmlElement(name = "Notification", required = true)
    protected List<NotificationList.Notification> notification;

    public List<NotificationList.Notification> getNotification() {
        if (notification == null) {
            notification = new ArrayList<NotificationList.Notification>();
        }
        return this.notification;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "context",
        "eventDateTime",
        "eventType",
        "notificationURI",
        "objectType",
        "objectURI"
    })
    public static class Notification {

        @XmlElement(name = "Context", required = true, nillable = true)
        protected Object context;
        @XmlElement(name = "EventDateTime", required = true)
        protected String eventDateTime;
        @XmlElement(name = "EventType", required = true)
        protected String eventType;
        @XmlElement(name = "NotificationURI", required = true)
        protected String notificationURI;
        @XmlElement(name = "ObjectType", required = true)
        protected String objectType;
        @XmlElement(name = "ObjectURI", required = true)
        protected String objectURI;

        public Object getContext() {
            return context;
        }

        public void setContext(Object value) {
            this.context = value;
        }

        public String getEventDateTime() {
            return eventDateTime;
        }

        public void setEventDateTime(String value) {
            this.eventDateTime = value;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String value) {
            this.eventType = value;
        }

        public String getNotificationURI() {
            return notificationURI;
        }

        public void setNotificationURI(String value) {
            this.notificationURI = value;
        }

        public String getObjectType() {
            return objectType;
        }

        public void setObjectType(String value) {
            this.objectType = value;
        }

        public String getObjectURI() {
            return objectURI;
        }

        public void setObjectURI(String value) {
            this.objectURI = value;
        }

    }

}
