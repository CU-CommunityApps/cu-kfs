
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "email" })
@XmlRootElement(name = "UserEmail")
public class UserEmail {

    @XmlElement(name = "Email", required = true)
    protected Email email;

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email value) {
        this.email = value;
    }

}
