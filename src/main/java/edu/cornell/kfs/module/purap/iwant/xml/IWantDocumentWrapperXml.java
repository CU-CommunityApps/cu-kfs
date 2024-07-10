package edu.cornell.kfs.module.purap.iwant.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import edu.cornell.kfs.module.purap.businessobject.IWantDocumentBatchFeed;
import edu.cornell.kfs.sys.businessobject.XmlFragmentable;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "iWantDocuments" })
@XmlRootElement(name = "i_want_doc_file", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
public class IWantDocumentWrapperXml implements XmlFragmentable {

    @XmlElement(name = "iWantDocument", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE, required = true)
    private List<IWantDocumentXml> iWantDocuments;

    public List<IWantDocumentXml> getiWantDocuments() {
        if (iWantDocuments == null) {
            iWantDocuments = new ArrayList<>();
        }
        return iWantDocuments;
    }
    
    public IWantDocumentBatchFeed toIWantDocumentBatchFeed() {
        IWantDocumentBatchFeed batchFeed = new IWantDocumentBatchFeed();
        for (IWantDocumentXml xmlDocs : iWantDocuments) {
            batchFeed.getBatchIWantDocuments().add(xmlDocs.toBatchIWantDocument());
        }
        return batchFeed;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }

    @Override
    public String getXmlPrefix() {
        return IWantXmlConstants.IWANT_XML_WRAPPER_XML_PREFIX;
    }

    @Override
    public boolean shouldMarshalAsFragment() {
        return true;
    }

    @Override
    public Map<String, Object> getAdditionalJAXBProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(Marshaller.JAXB_SCHEMA_LOCATION, IWantXmlConstants.IWANT_DOCUMENT_SCHEMA_LOCATION);
        return properties;
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
