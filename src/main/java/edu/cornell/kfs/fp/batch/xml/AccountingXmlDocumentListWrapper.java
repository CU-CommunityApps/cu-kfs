package edu.cornell.kfs.fp.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DocumentWrapper", namespace = StringUtils.EMPTY)
public class AccountingXmlDocumentListWrapper {

    @XmlElement(name = "CreateDate", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date createDate;

    @XmlElement(name = "ReportEmail", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String reportEmail;

    @XmlElement(name = "Overview", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String overview;

    @XmlElementWrapper(name = "DocumentList", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "Document", namespace = StringUtils.EMPTY, required = true)
    protected List<AccountingXmlDocumentEntry> documents;

    public AccountingXmlDocumentListWrapper() {
        this.documents = new ArrayList<>();
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

    public List<AccountingXmlDocumentEntry> getDocuments() {
        return documents;
    }

    public void setDocuments(List<AccountingXmlDocumentEntry> documents) {
        this.documents = documents;
    }

}
