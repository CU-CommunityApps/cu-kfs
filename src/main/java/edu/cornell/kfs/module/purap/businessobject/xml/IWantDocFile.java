package edu.cornell.kfs.module.purap.businessobject.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import edu.cornell.kfs.sys.businessobject.XmlFragmentable;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "iWantDocuments" })
@XmlRootElement(name = "i_want_doc_file", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
public class IWantDocFile implements XmlFragmentable{

    @XmlElement(name = "iWantDocument", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
    private List<IWantDocument> iWantDocuments;

    public List<IWantDocument> getiWantDocuments() {
        if (iWantDocuments == null) {
            iWantDocuments = new ArrayList<IWantDocument>();
        }
        return iWantDocuments;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }

    @Override
    public String getXmlPrefix() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    }

    @Override
    public boolean shouldMarshalAsFragment() {
        return true;
    }
    
    @Override
    public Map<String, Object> getAdditionalJAXBProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.kuali.org/kfs/purap/iWantDocument iWantDocument.xsd");
        return properties;
    }

}
