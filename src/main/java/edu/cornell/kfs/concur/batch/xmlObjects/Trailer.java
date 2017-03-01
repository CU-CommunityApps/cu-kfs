package edu.cornell.kfs.concur.batch.xmlObjects;

import java.math.BigDecimal;
import java.math.BigInteger;
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
public class Trailer {

    @XmlElement(name = "detail_count", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected BigInteger detailCount;
    @XmlElement(name = "detail_tot_amt", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected BigDecimal detailTotAmt;

    public BigInteger getDetailCount() {
        return detailCount;
    }

    public void setDetailCount(BigInteger value) {
        this.detailCount = value;
    }

    public BigDecimal getDetailTotAmt() {
        return detailTotAmt;
    }

    public void setDetailTotAmt(BigDecimal value) {
        this.detailTotAmt = value;
    }

}
