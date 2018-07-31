package edu.cornell.kfs.fp.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DisbursementVoucherPrePaidTravelOverview", namespace = StringUtils.EMPTY)
public class DisbursementVoucherPrePaidTravelOverview {
    
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
    protected List<DisbursementVoucherPreConferenceRegistrant> registrants;
    
    public DisbursementVoucherPrePaidTravelOverview() {
        registrants = new ArrayList<DisbursementVoucherPreConferenceRegistrant>();
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

    public List<DisbursementVoucherPreConferenceRegistrant> getRegistrants() {
        return registrants;
    }

    public void setRegistrants(List<DisbursementVoucherPreConferenceRegistrant> registrants) {
        this.registrants = registrants;
    }

}
