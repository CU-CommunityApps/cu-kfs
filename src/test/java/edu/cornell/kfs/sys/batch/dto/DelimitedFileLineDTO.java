package edu.cornell.kfs.sys.batch.dto;

import java.sql.Date;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class DelimitedFileLineDTO {

    private String lineId;
    private String description;
    private Date lineDate;
    private KualiDecimal lineAmount;
    private Boolean lineFlag;

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLineDate() {
        return lineDate;
    }

    public void setLineDate(Date lineDate) {
        this.lineDate = lineDate;
    }

    public KualiDecimal getLineAmount() {
        return lineAmount;
    }

    public void setLineAmount(KualiDecimal lineAmount) {
        this.lineAmount = lineAmount;
    }

    public Boolean getLineFlag() {
        return lineFlag;
    }

    public void setLineFlag(Boolean lineFlag) {
        this.lineFlag = lineFlag;
    }

}
