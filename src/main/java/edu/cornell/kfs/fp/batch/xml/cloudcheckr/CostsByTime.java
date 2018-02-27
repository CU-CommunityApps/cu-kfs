package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GroupingByTime", namespace = StringUtils.EMPTY)
public class CostsByTime {
    
    @XmlElementWrapper(name = "Groups", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "Grouping", namespace = StringUtils.EMPTY, required = true)
    private List<CostsByTimeGrouping> groupByTimeGroupings;
    
    @XmlElementWrapper(name = "CostDates", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "GroupCostDate", namespace = StringUtils.EMPTY, required = true)
    private List<CostDate> costDates;
}
