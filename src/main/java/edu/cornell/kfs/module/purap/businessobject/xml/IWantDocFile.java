package edu.cornell.kfs.module.purap.businessobject.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "iWantDocument" })
@XmlRootElement(name = "i_want_doc_file", namespace="http://www.kuali.org/kfs/purap/iWantDocument")
public class IWantDocFile {

    @XmlElement(required = true)
    private List<IWantDocument> iWantDocument;

    public List<IWantDocument> getiWantDocument() {
        if (iWantDocument == null) {
            iWantDocument = new ArrayList<IWantDocument>();
        }
        return iWantDocument;
    }

    public void setiWantDocument(List<IWantDocument> iWantDocument) {
        this.iWantDocument = iWantDocument;
    }
    
    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }
}
