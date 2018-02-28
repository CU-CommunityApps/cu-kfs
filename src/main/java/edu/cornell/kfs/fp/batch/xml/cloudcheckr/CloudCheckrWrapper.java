package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GetDetailedBillingWithGroupingResponseV2", namespace = StringUtils.EMPTY)
public class CloudCheckrWrapper {
    @XmlElement(name = "Total", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal total;
    
    @XmlElement(name = "Max", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal maximum;
    
    @XmlElement(name = "Min", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal minimum;
    
    @XmlElement(name = "Average", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal average;
    
    @XmlElementWrapper(name = "CostsByGroup", namespace = StringUtils.EMPTY)
    @XmlElement(name = "GroupingLevel", namespace = StringUtils.EMPTY, required = true)
    private List<GroupLevel> costsByAccounts;
    
    @XmlElementWrapper(name = "CostsByTime", namespace = StringUtils.EMPTY)
    @XmlElement(name = "GroupingByTime", namespace = StringUtils.EMPTY, required = true)
    private List<CostsByTime> costsByTimes;
    
    public CloudCheckrWrapper() {
        costsByAccounts = new ArrayList<GroupLevel>();
        costsByTimes = new ArrayList<CostsByTime>();
        total = KualiDecimal.ZERO;
        maximum = KualiDecimal.ZERO;
        minimum = KualiDecimal.ZERO;
        average = KualiDecimal.ZERO;
    }

    public KualiDecimal getTotal() {
        return total;
    }

    public void setTotal(KualiDecimal total) {
        this.total = total;
    }

    public KualiDecimal getMaximum() {
        return maximum;
    }

    public void setMaximum(KualiDecimal maximum) {
        this.maximum = maximum;
    }

    public KualiDecimal getMinimum() {
        return minimum;
    }

    public void setMinimum(KualiDecimal minimum) {
        this.minimum = minimum;
    }

    public KualiDecimal getAverage() {
        return average;
    }

    public void setAverage(KualiDecimal average) {
        this.average = average;
    }

    public List<GroupLevel> getCostsByAccounts() {
        return costsByAccounts;
    }

    public void setCostsByAccounts(List<GroupLevel> costsByAccounts) {
        this.costsByAccounts = costsByAccounts;
    }

    public List<CostsByTime> getCostsByTimes() {
        return costsByTimes;
    }

    public void setCostsByTimes(List<CostsByTime> costsByTimes) {
        this.costsByTimes = costsByTimes;
    }
}
