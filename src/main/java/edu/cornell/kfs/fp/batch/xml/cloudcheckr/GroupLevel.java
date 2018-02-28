package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

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
    protected Long usageQuantity;
    
    @XmlElementWrapper(name = "NextLevel", namespace = StringUtils.EMPTY)
    @XmlElement(name = "GroupingLevel", namespace = StringUtils.EMPTY)
    private List<GroupLevel> groupLevel;
    
    public GroupLevel() {
        groupLevel = new ArrayList<GroupLevel>();
        cost = KualiDecimal.ZERO;
        usageQuantity = new Long(0);
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

    public Long getUsageQuantity() {
        return usageQuantity;
    }

    public void setUsageQuantity(Long usageQuantity) {
        this.usageQuantity = usageQuantity;
    }

    public List<GroupLevel> getGroupLevel() {
        return groupLevel;
    }

    public void setGroupLevel(List<GroupLevel> groupLevel) {
        this.groupLevel = groupLevel;
    }

}
