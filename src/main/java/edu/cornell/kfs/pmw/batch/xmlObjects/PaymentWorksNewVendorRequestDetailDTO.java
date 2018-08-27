package edu.cornell.kfs.pmw.batch.xmlObjects;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.cornell.kfs.sys.xmladapters.XSDDateTimeStringToTimestampAdapter;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorRequestDetailDTO {

    private String id;
    private String request_status;
    @XmlJavaTypeAdapter(XSDDateTimeStringToTimestampAdapter.class)
    private Timestamp last_submitted_ts;
    @XmlElement(name = "custom_fields")
    private PaymentWorksCustomFieldsDTO custom_fields;
    private PaymentWorksRequestingCompanyDTO requesting_company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequest_status() {
        return request_status;
    }

    public void setRequest_status(String request_status) {
        this.request_status = request_status;
    }

    public Timestamp getLast_submitted_ts() {
        return last_submitted_ts;
    }

    public void setLast_submitted_ts(Timestamp last_submitted_ts) {
        this.last_submitted_ts = last_submitted_ts;
    }

    public PaymentWorksRequestingCompanyDTO getRequesting_company() {
        return requesting_company;
    }

    public void setRequesting_company(PaymentWorksRequestingCompanyDTO requesting_company) {
        this.requesting_company = requesting_company;
    }

    public PaymentWorksCustomFieldsDTO getCustom_fields() {
        return custom_fields;
    }

    public void setCustom_fields(PaymentWorksCustomFieldsDTO custom_fields) {
        this.custom_fields = custom_fields;
    }

}
