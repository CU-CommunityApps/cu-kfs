package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "users"
})
@XmlRootElement(name = "ManagerList")
public class ManagerList {

    @XmlElementWrapper(name = "UserList")
    @XmlElement(name = "User")
    private List<String> users;

    public List<String> getUsers() {
        if (users == null) {
            users = new ArrayList<>();
        }
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

}
