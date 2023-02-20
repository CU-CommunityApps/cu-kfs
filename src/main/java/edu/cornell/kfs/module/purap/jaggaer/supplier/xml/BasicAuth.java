
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
