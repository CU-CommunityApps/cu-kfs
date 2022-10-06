package edu.cornell.kfs.pmw.batch.xmlObjects;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "custom_fields")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksCustomFieldsDTO {

    @XmlElement(name = "list-item")
    private List<PaymentWorksCustomFieldDTO> custom_fields;

    public List<PaymentWorksCustomFieldDTO> getCustom_fields() {
        return custom_fields;
    }

    public void setCustom_fields(List<PaymentWorksCustomFieldDTO> custom_fields) {
        this.custom_fields = custom_fields;
    }

}
