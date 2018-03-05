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
public class GroupingByTime {
    
    @XmlElementWrapper(name = "Groups", namespace = StringUtils.EMPTY)
    @XmlElement(name = "Grouping", namespace = StringUtils.EMPTY)
    private List<Grouping> groupings;
    
    @XmlElementWrapper(name = "CostDates", namespace = StringUtils.EMPTY)
    @XmlElement(name = "GroupCostDate", namespace = StringUtils.EMPTY)
    private List<GroupCostDate> groupCostDates;
    
    public GroupingByTime() {
        groupings = new ArrayList<Grouping>();
        groupCostDates = new ArrayList<GroupCostDate>();
    }

    public List<Grouping> getGroupings() {
        return groupings;
    }

    public void setGroupings(List<Grouping> grouping) {
        this.groupings = grouping;
    }

    public List<GroupCostDate> getGroupCostDates() {
        return groupCostDates;
    }

    public void setGroupCostDates(List<GroupCostDate> groupCostDates) {
        this.groupCostDates = groupCostDates;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        groupings.stream().forEach(group -> sb.append("group: (").append(group.toString()).append("), "));
        groupCostDates.stream().forEach(costDate -> sb.append("costDate: (").append(costDate.toString()).append("), "));
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (ObjectUtils.isNotNull(o) && o instanceof GroupingByTime) {
            GroupingByTime otherCostsByTime = (GroupingByTime) o;
            equals = CollectionUtils.isEqualCollection(groupings, otherCostsByTime.getGroupings())
                    && CollectionUtils.isEqualCollection(groupCostDates, otherCostsByTime.getGroupCostDates());
        }
        return equals;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupings, groupCostDates);
    }
}
