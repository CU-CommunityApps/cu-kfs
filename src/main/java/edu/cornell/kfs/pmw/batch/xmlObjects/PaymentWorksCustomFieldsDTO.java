package edu.cornell.kfs.pmw.batch.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "custom_fields")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksCustomFieldsDTO {

    @XmlElement(name = "list-item")
    private List<PaymentWorksCustomFieldDTO> customFields;

    public List<PaymentWorksCustomFieldDTO> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<PaymentWorksCustomFieldDTO> customFields) {
        this.customFields = customFields;
    }

}
