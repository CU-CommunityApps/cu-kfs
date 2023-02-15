package edu.cornell.kfs.module.purap.jaggaer.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "lookupErrorMessage" })
@XmlRootElement(name = "LookupErrors")
public class LookupErrors {

    @XmlElement(name = "LookupErrorMessage", required = true)
    protected List<LookupErrorMessage> lookupErrorMessage;

    public List<LookupErrorMessage> getLookupErrorMessage() {
        if (lookupErrorMessage == null) {
            lookupErrorMessage = new ArrayList<LookupErrorMessage>();
        }
        return this.lookupErrorMessage;
    }

}
