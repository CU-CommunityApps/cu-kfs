package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "list-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksCustomFieldDTO {

    @XmlElement(name = "field_id")
    private String fieldId;
    
    @XmlElement(name = "field_label")
    private String fieldLabel;
    
    @XmlElement(name = "field_value")
    private String fieldValue;
    
    @XmlElement(name = "field_file")
    private String fieldFile;
    
    @XmlElement(name = "display_order")
    private String displayOrder;

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldFile() {
        return fieldFile;
    }

    public void setFieldFile(String fieldFile) {
        this.fieldFile = fieldFile;
    }

    public String getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(String displayOrder) {
        this.displayOrder = displayOrder;
    }

}
