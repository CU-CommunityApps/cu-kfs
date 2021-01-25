package edu.cornell.kfs.concur;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This DTO just provides a convenient way to wrap mock-server-related errors in an XML DTO entity.
 * This does not necessarily represent the actual format of error responses returned by Concur.
 */
@XmlRootElement(name = "Error")
@XmlAccessorType(XmlAccessType.FIELD)
public class MockConcurErrorDTO {

    @XmlElement(name = "Message")
    private String message;

    public MockConcurErrorDTO() {}

    public MockConcurErrorDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
