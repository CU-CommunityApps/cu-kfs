package edu.cornell.kfs.sys.batch.xml;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "KualiDeveloperWrapper", namespace = StringUtils.EMPTY)
public class KualiDeveloperXmlListWrapper {

    @XmlElement(name = "CreateDate", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date createDate;

    @XmlElement(name = "ReportEmail", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String reportEmail;

    @XmlElement(name = "Overview", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String overview;

    @XmlElementWrapper(name = "KualiDeveloperList", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "KualiDeveloper", namespace = StringUtils.EMPTY, required = true)
    protected List<KualiDeveloperXmlEntry> kualiDevelopers;

    public KualiDeveloperXmlListWrapper() {
        this.kualiDevelopers = new ArrayList<>();
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getReportEmail() {
        return reportEmail;
    }

    public void setReportEmail(String reportEmail) {
        this.reportEmail = reportEmail;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public List<KualiDeveloperXmlEntry> getKualiDevelopers() {
        return kualiDevelopers;
    }

    public void setKualiDevelopers(List<KualiDeveloperXmlEntry> kualiDevelopers) {
        this.kualiDevelopers = kualiDevelopers;
    }

}
