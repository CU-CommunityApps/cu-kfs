
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "preventNotes", "preventAttachments", "preventShipVia", "preventDeliveryDate" })
@XmlRootElement(name = "CXMLSettings")
public class CXMLSettings {

    @XmlElement(name = "PreventNotes")
    protected PreventNotes preventNotes;
    @XmlElement(name = "PreventAttachments")
    protected PreventAttachments preventAttachments;
    @XmlElement(name = "PreventShipVia")
    protected PreventShipVia preventShipVia;
    @XmlElement(name = "PreventDeliveryDate")
    protected PreventDeliveryDate preventDeliveryDate;

    public PreventNotes getPreventNotes() {
        return preventNotes;
    }

    public void setPreventNotes(PreventNotes value) {
        this.preventNotes = value;
    }

    public PreventAttachments getPreventAttachments() {
        return preventAttachments;
    }

    public void setPreventAttachments(PreventAttachments value) {
        this.preventAttachments = value;
    }

    public PreventShipVia getPreventShipVia() {
        return preventShipVia;
    }

    public void setPreventShipVia(PreventShipVia value) {
        this.preventShipVia = value;
    }

    public PreventDeliveryDate getPreventDeliveryDate() {
        return preventDeliveryDate;
    }

    public void setPreventDeliveryDate(PreventDeliveryDate value) {
        this.preventDeliveryDate = value;
    }

}
