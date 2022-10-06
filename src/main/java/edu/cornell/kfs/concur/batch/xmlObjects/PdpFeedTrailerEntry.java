package edu.cornell.kfs.concur.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

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
