package edu.cornell.kfs.module.purap.businessobject.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "noteText" })
@XmlRootElement(name = "note")
public class Note {

    @XmlElement(required = true)
    private String noteText;

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }

}
