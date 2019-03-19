package edu.cornell.kfs.rass.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Kfs", namespace = StringUtils.EMPTY)
public class RassXmlDocumentWrapper {
    
    
    @XmlElement(name = "Extract_Begin_Timestamp", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    private Date extractDate;
    
    @XmlElementWrapper(name = "Awards", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "Award", namespace = StringUtils.EMPTY, required = true)
    private List<RassXmlAwardEntry> awards;
    
    public RassXmlDocumentWrapper() {
        awards = new ArrayList<RassXmlAwardEntry>();
    }

}
