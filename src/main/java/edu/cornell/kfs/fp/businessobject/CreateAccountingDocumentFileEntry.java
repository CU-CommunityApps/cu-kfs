package edu.cornell.kfs.fp.businessobject;

import java.sql.Timestamp;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class CreateAccountingDocumentFileEntry extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 5714970318621809904L;

    private String fileName;
    private Timestamp fileCreatedDate;
    private Timestamp fileProcessedDate;
    private String reportEmailAddress;
    private String fileOverview;
    private Integer documentCount;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Timestamp getFileCreatedDate() {
        return fileCreatedDate;
    }

    public void setFileCreatedDate(Timestamp fileCreatedDate) {
        this.fileCreatedDate = fileCreatedDate;
    }

    public Timestamp getFileProcessedDate() {
        return fileProcessedDate;
    }

    public void setFileProcessedDate(Timestamp fileProcessedDate) {
        this.fileProcessedDate = fileProcessedDate;
    }

    public String getReportEmailAddress() {
        return reportEmailAddress;
    }

    public void setReportEmailAddress(String reportEmailAddress) {
        this.reportEmailAddress = reportEmailAddress;
    }

    public String getFileOverview() {
        return fileOverview;
    }

    public void setFileOverview(String fileOverview) {
        this.fileOverview = fileOverview;
    }

    public Integer getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Integer documentCount) {
        this.documentCount = documentCount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
