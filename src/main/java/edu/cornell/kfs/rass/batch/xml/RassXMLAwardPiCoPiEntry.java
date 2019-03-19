package edu.cornell.kfs.rass.batch.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PI_or_CoPI", namespace = StringUtils.EMPTY)
public class RassXMLAwardPiCoPiEntry {
    
    @XmlElement(name = "Primary", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String primaryString;
    
    @XmlElement(name = "ReportEmail", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String principalName;

}
