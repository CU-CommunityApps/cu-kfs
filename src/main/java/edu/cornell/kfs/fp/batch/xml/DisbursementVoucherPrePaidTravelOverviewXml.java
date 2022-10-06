package edu.cornell.kfs.fp.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "pre_paid_travel", namespace = StringUtils.EMPTY)
public class DisbursementVoucherPrePaidTravelOverviewXml {
    
    @XmlElement(name = "location", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String location;
    
    @XmlElement(name = "type", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    
    @XmlElement(name = "start_date", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date startDate;
    
    @XmlElement(name = "end_date", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date endDate;
    
    @XmlElementWrapper(name = "prepaid_registrants", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "registrant", namespace = StringUtils.EMPTY, required = true)
    protected List<DisbursementVoucherPreConferenceRegistrantXml> registrants;
    
    public DisbursementVoucherPrePaidTravelOverviewXml() {
        registrants = new ArrayList<DisbursementVoucherPreConferenceRegistrantXml>();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<DisbursementVoucherPreConferenceRegistrantXml> getRegistrants() {
        return registrants;
    }

    public void setRegistrants(List<DisbursementVoucherPreConferenceRegistrantXml> registrants) {
        this.registrants = registrants;
    }

}
