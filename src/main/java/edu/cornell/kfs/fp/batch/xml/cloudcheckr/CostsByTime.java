package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GroupingByTime", namespace = StringUtils.EMPTY)
public class CostsByTime {
    
    @XmlElementWrapper(name = "Groups", namespace = StringUtils.EMPTY)
    @XmlElement(name = "Grouping", namespace = StringUtils.EMPTY)
    private List<CostsByTimeGrouping> groupByTimeGroupings;
    
    @XmlElementWrapper(name = "CostDates", namespace = StringUtils.EMPTY)
    @XmlElement(name = "GroupCostDate", namespace = StringUtils.EMPTY)
    private List<CostDate> costDates;
    
    public CostsByTime() {
        groupByTimeGroupings = new ArrayList<CostsByTimeGrouping>();
        costDates = new ArrayList<CostDate>();
    }

    public List<CostsByTimeGrouping> getGroupByTimeGroupings() {
        return groupByTimeGroupings;
    }

    public void setGroupByTimeGroupings(List<CostsByTimeGrouping> groupByTimeGroupings) {
        this.groupByTimeGroupings = groupByTimeGroupings;
    }

    public List<CostDate> getCostDates() {
        return costDates;
    }

    public void setCostDates(List<CostDate> costDates) {
        this.costDates = costDates;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        groupByTimeGroupings.stream().forEach(group -> sb.append("group: (").append(group.toString()).append("), "));
        costDates.stream().forEach(costDate -> sb.append("costDate: (").append(costDate.toString()).append("), "));
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (ObjectUtils.isNotNull(o) && o instanceof CostsByTime) {
            CostsByTime otherCostsByTime = (CostsByTime) o;
            equals = CollectionUtils.isEqualCollection(groupByTimeGroupings, otherCostsByTime.getGroupByTimeGroupings())
                    && CollectionUtils.isEqualCollection(costDates, otherCostsByTime.getCostDates());
        }
        return equals;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupByTimeGroupings, costDates);
    }
}
