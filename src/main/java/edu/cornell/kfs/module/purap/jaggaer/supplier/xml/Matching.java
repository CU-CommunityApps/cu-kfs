
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "matchingThreshold", "atOrBelowThreshold", "aboveThreshold", "includeSHT",
        "shtDiffPercent", "shtDiffAbove", "shtDiffBelow" })
@XmlRootElement(name = "Matching")
public class Matching {

    @XmlElement(name = "MatchingThreshold")
    protected MatchingThreshold matchingThreshold;
    @XmlElement(name = "AtOrBelowThreshold")
    protected AtOrBelowThreshold atOrBelowThreshold;
    @XmlElement(name = "AboveThreshold")
    protected AboveThreshold aboveThreshold;
    @XmlElement(name = "IncludeSHT")
    protected IncludeSHT includeSHT;
    @XmlElement(name = "SHTDiffPercent")
    protected SHTDiffPercent shtDiffPercent;
    @XmlElement(name = "SHTDiffAbove")
    protected SHTDiffAbove shtDiffAbove;
    @XmlElement(name = "SHTDiffBelow")
    protected SHTDiffBelow shtDiffBelow;

    public MatchingThreshold getMatchingThreshold() {
        return matchingThreshold;
    }

    public void setMatchingThreshold(MatchingThreshold value) {
        this.matchingThreshold = value;
    }

    public AtOrBelowThreshold getAtOrBelowThreshold() {
        return atOrBelowThreshold;
    }

    public void setAtOrBelowThreshold(AtOrBelowThreshold value) {
        this.atOrBelowThreshold = value;
    }

    public AboveThreshold getAboveThreshold() {
        return aboveThreshold;
    }

    public void setAboveThreshold(AboveThreshold value) {
        this.aboveThreshold = value;
    }

    public IncludeSHT getIncludeSHT() {
        return includeSHT;
    }

    public void setIncludeSHT(IncludeSHT value) {
        this.includeSHT = value;
    }

    public SHTDiffPercent getSHTDiffPercent() {
        return shtDiffPercent;
    }

    public void setSHTDiffPercent(SHTDiffPercent value) {
        this.shtDiffPercent = value;
    }

    public SHTDiffAbove getSHTDiffAbove() {
        return shtDiffAbove;
    }

    public void setSHTDiffAbove(SHTDiffAbove value) {
        this.shtDiffAbove = value;
    }

    public SHTDiffBelow getSHTDiffBelow() {
        return shtDiffBelow;
    }

    public void setSHTDiffBelow(SHTDiffBelow value) {
        this.shtDiffBelow = value;
    }

}
