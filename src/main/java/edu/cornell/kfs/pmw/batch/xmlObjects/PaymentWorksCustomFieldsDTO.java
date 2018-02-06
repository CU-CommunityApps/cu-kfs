package edu.cornell.kfs.pmw.batch.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
