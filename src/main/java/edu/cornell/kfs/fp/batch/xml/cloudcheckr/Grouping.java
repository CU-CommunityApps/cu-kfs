package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Grouping", namespace = StringUtils.EMPTY)
public class Grouping {
    
    @XmlElement(name = "GroupName", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String groupName;
    
    @XmlElement(name = "GroupValue", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String groupValue;
    
    @XmlElement(name = "FriendlyName", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String friendlyName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupValue() {
        return groupValue;
    }

    public void setGroupValue(String groupValue) {
        this.groupValue = groupValue;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("groupName: ").append(groupName);
        sb.append(" groupValue: ").append(groupValue);
        sb.append(" friendlyName: ").append(friendlyName);
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (ObjectUtils.isNotNull(o) && o instanceof Grouping) {
            Grouping otherCostsByTimeGrouping = (Grouping) o;
            equals = StringUtils.equals(groupName, otherCostsByTimeGrouping.getGroupName())
                    && StringUtils.equals(groupValue, otherCostsByTimeGrouping.getGroupValue())
                    && StringUtils.equals(friendlyName, otherCostsByTimeGrouping.getFriendlyName());
        }
        return equals;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupName, groupValue, friendlyName);
    }

}
