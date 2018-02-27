package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

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
    @XmlElement(name = "Total", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal total;
    
    @XmlElement(name = "Max", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal maximum;
    
    @XmlElement(name = "Min", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal minimum;
    
    @XmlElement(name = "Average", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal average;
    
    @XmlElementWrapper(name = "CostsByGroup", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "GroupingLevel", namespace = StringUtils.EMPTY, required = true)
    private List<GroupLevel> costsByAccounts;
    
    @XmlElementWrapper(name = "CostsByTime", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "GroupingByTime", namespace = StringUtils.EMPTY, required = true)
    private List<CostsByTime> costsByTimes;
}
