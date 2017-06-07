package edu.cornell.kfs.concur.rest.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Access_Token")
@XmlAccessorType(XmlAccessType.FIELD)
public class NewAccessTokenDTO extends AccessTokenDTO {

    @XmlElement(name = "Refresh_Token")
    protected String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
