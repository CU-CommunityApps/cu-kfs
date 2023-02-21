
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "name", "userPrintableName", "email" })
@XmlRootElement(name = "PunchoutContact")
public class PunchoutContact {

    @XmlElement(name = "Name")
    protected Name name;
    @XmlElement(name = "UserPrintableName")
    protected UserPrintableName userPrintableName;
    @XmlElement(name = "Email")
    protected Email email;

    public Name getName() {
        return name;
    }

    public void setName(Name value) {
        this.name = value;
    }

    public UserPrintableName getUserPrintableName() {
        return userPrintableName;
    }

    public void setUserPrintableName(UserPrintableName value) {
        this.userPrintableName = value;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email value) {
        this.email = value;
    }

}
