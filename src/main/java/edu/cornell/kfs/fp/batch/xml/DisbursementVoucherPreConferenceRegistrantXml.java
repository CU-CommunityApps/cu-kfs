package edu.cornell.kfs.fp.batch.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registrant", namespace = StringUtils.EMPTY)
public class DisbursementVoucherPreConferenceRegistrantXml {
    
    @XmlElement(name = "name", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String name;
    
    @XmlElement(name = "department_code", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String departmentCode;
    
    @XmlElement(name = "pre_conference_request_number", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String preConferenceRequestNumber;
    
    @XmlElement(name = "amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal amount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getPreConferenceRequestNumber() {
        return preConferenceRequestNumber;
    }

    public void setPreConferenceRequestNumber(String preConferenceRequestNumber) {
        this.preConferenceRequestNumber = preConferenceRequestNumber;
    }

    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(KualiDecimal amount) {
        this.amount = amount;
    }

}
