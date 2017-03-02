package edu.cornell.kfs.concur.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "detailCount",
    "detailTotAmt"
})
@XmlRootElement(name = "trailer", namespace = "http://www.kuali.org/kfs/pdp/payment")
public class PdpFeedTrailerEntry {

    @XmlElement(name = "detail_count", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected Integer detailCount;
    @XmlElement(name = "detail_tot_amt", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected Double detailTotAmt;

    public Integer getDetailCount() {
        return detailCount;
    }

    public void setDetailCount(Integer value) {
        this.detailCount = value;
    }

    public Double getDetailTotAmt() {
        return detailTotAmt;
    }

    public void setDetailTotAmt(Double value) {
        this.detailTotAmt = value;
    }

}
