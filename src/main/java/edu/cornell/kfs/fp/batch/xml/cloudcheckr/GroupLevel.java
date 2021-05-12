package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GroupingLevel", namespace = StringUtils.EMPTY)
public class GroupLevel {
    
    @XmlElement(name = "GroupName", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String groupName;
    
    @XmlElement(name = "GroupValue", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String groupValue;
    
    @XmlElement(name = "FriendlyName", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String friendlyName;
    
    @XmlElement(name = "Cost", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal cost;
    
    @XmlElement(name = "UsageQuantity", namespace = StringUtils.EMPTY)
    protected Double usageQuantity;
    
    @XmlElementWrapper(name = "NextLevel", namespace = StringUtils.EMPTY)
    @XmlElement(name = "GroupingLevel", namespace = StringUtils.EMPTY)
    private List<GroupLevel> nextLevel;
    
    public GroupLevel() {
        nextLevel = new ArrayList<GroupLevel>();
        cost = KualiDecimal.ZERO;
        usageQuantity = new Double(0);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("groupName: ").append(groupName);
        sb.append(" groupValue: ").append(groupValue);
        sb.append(" friendlyName: ").append(friendlyName);
        sb.append(" cost: ").append(cost);
        sb.append(" usageQuantity: ").append(usageQuantity);
        nextLevel.stream().forEach(level -> sb.append(" nextlevel: ").append(level.toString()).append(", "));
        return sb.toString();
    }
    
    public boolean isAwsAccountGroupLevel() {
        return StringUtils.equals(groupName, CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_AWS_ACCOUNT);
    }
    
    public boolean isCostCenterGroupLevel() {
        return StringUtils.equals(groupName, CuFPConstants.CLOUDCHECKR.GROUP_LEVEL_COST_CENTER);
    }

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

    public KualiDecimal getCost() {
        return cost;
    }

    public void setCost(KualiDecimal cost) {
        this.cost = cost;
    }

    public Double getUsageQuantity() {
        return usageQuantity;
    }

    public void setUsageQuantity(Double usageQuantity) {
        this.usageQuantity = usageQuantity;
    }

    public List<GroupLevel> getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(List<GroupLevel> nextLevel) {
        this.nextLevel = nextLevel;
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (ObjectUtils.isNotNull(o) && o instanceof GroupLevel) {
            GroupLevel otherGroupLevel = (GroupLevel) o;
            equals = StringUtils.equals(groupName, otherGroupLevel.getGroupName())
                    && StringUtils.equals(groupValue, otherGroupLevel.getGroupValue())
                    && StringUtils.equals(friendlyName, otherGroupLevel.getFriendlyName())
                    && Objects.equals(cost, otherGroupLevel.getCost())
                    && Objects.equals(usageQuantity, otherGroupLevel.getUsageQuantity())
                    && CollectionUtils.isEqualCollection(nextLevel, otherGroupLevel.getNextLevel());
        }
        return equals;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupName, groupValue, friendlyName, cost, nextLevel);
    }

}
