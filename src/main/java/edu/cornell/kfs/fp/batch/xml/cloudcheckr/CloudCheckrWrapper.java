package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
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
    private List<GroupLevel> groupLevels;
    
    @XmlElementWrapper(name = "CostsByTime", namespace = StringUtils.EMPTY)
    @XmlElement(name = "GroupingByTime", namespace = StringUtils.EMPTY, required = true)
    private List<GroupingByTime> groupingByTimes;
    
    public CloudCheckrWrapper() {
        groupLevels = new ArrayList<GroupLevel>();
        groupingByTimes = new ArrayList<GroupingByTime>();
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
        return groupLevels;
    }

    public void setCostsByAccounts(List<GroupLevel> costsByAccounts) {
        this.groupLevels = costsByAccounts;
    }

    public List<GroupingByTime> getGroupingByTimes() {
        return groupingByTimes;
    }

    public void setGroupingByTime(List<GroupingByTime> groupingByTimes) {
        this.groupingByTimes = groupingByTimes;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total: ").append(total);
        sb.append(" maximum: ").append(maximum);
        sb.append(" minimum: ").append(minimum);
        sb.append(" average: ").append(average);
        sb.append(" costsByAccounts: ").append("[");
        groupLevels.stream().forEach(account -> sb.append("account: (").append(account.toString()).append("), "));
        groupingByTimes.stream().forEach(costtime -> sb.append("costByTime: (").append(costtime.toString()).append("), "));
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (ObjectUtils.isNotNull(o) && o instanceof CloudCheckrWrapper) {
            CloudCheckrWrapper otherWrapper = (CloudCheckrWrapper) o;
            equals = total.equals(otherWrapper.total) && maximum.equals(otherWrapper.getMaximum())
                    && minimum.equals(otherWrapper.getMinimum()) && average.equals(otherWrapper.getAverage())
                    && CollectionUtils.isEqualCollection(groupLevels, otherWrapper.getCostsByAccounts())
                    && CollectionUtils.isEqualCollection(groupingByTimes, otherWrapper.getGroupingByTimes());
        }
        return equals;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(total, maximum, minimum, average, groupLevels, groupingByTimes);
    }
}
