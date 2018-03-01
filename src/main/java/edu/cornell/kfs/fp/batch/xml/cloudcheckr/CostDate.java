package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.xmladapters.CloudcheckrStringToJavaDateTimeAdapter;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GroupCostDate", namespace = StringUtils.EMPTY)
public class CostDate {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(CUKFSConstants.DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am);
    
    @XmlElement(name = "Date", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(CloudcheckrStringToJavaDateTimeAdapter.class)
    private Date date;
    
    @XmlElement(name = "Cost", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    private KualiDecimal cost;
    
    @XmlElement(name = "UsageQuantity", namespace = StringUtils.EMPTY)
    private Double usageQuantity;
    
    public CostDate() {
        cost = KualiDecimal.ZERO;
        usageQuantity = new Double(0);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cost: ").append(cost);
        sb.append(" date: ");
        if (ObjectUtils.isNotNull(date)) {
            sb.append(DATE_FORMATTER.print(date.getTime()));
        } else {
            sb.append("null date");
        }
        sb.append(" usageQuantity: ").append(usageQuantity);
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (ObjectUtils.isNotNull(o) && o instanceof CostDate) {
            CostDate otherCostDate = (CostDate) o;
            equals = Objects.equals(cost, otherCostDate.getCost())
                    && Objects.equals(usageQuantity, otherCostDate.getUsageQuantity())
                    && Objects.equals(date, otherCostDate.getDate());
                            
        }
        return equals;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(date, cost, usageQuantity);
    }

}
