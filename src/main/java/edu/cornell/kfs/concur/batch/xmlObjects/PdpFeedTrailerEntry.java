package edu.cornell.kfs.concur.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "detailCount",
    "detailTotAmt"
})
@XmlRootElement(name = "trailer", namespace = ConcurConstants.PDP_XML_NAMESPACE)
public class PdpFeedTrailerEntry {

    @XmlElement(name = "detail_count", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    protected Integer detailCount;
    @XmlElement(name = "detail_tot_amt", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal detailTotAmt;

    public Integer getDetailCount() {
        return detailCount;
    }

    public void setDetailCount(Integer value) {
        this.detailCount = value;
    }

    public KualiDecimal getDetailTotAmt() {
        return detailTotAmt;
    }

    public void setDetailTotAmt(KualiDecimal value) {
        this.detailTotAmt = value;
    }

}
