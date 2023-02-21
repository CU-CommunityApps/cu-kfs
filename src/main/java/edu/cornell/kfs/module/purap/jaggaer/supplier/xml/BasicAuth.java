
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "username", "password" })
@XmlRootElement(name = "BasicAuth")
public class BasicAuth {

    @XmlElement(name = "Username", required = true)
    protected Username username;
    @XmlElement(name = "Password", required = true)
    protected Password password;

    public Username getUsername() {
        return username;
    }

    public void setUsername(Username value) {
        this.username = value;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password value) {
        this.password = value;
    }

}
