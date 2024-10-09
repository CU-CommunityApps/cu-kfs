package edu.cornell.kfs.coa.batch.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.Truth;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class LegacyAccountAttachment extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String id;
    private String legacyAccountCode;
    private String kfsChartCode;
    private String kfsAccountNumber;
    private String fileName;
    private String addedBy;
    private String fileDescription;
    private String filePath;
    private int retryCount;
    private boolean copied;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLegacyAccountCode() {
        return legacyAccountCode;
    }

    public void setLegacyAccountCode(final String legacyAccountCode) {
        this.legacyAccountCode = legacyAccountCode;
    }

    public String getKfsChartCode() {
        return kfsChartCode;
    }

    public void setKfsChartCode(final String kfsChartCode) {
        this.kfsChartCode = kfsChartCode;
    }

    public String getKfsAccountNumber() {
        return kfsAccountNumber;
    }

    public void setKfsAccountNumber(final String kfsAccountNumber) {
        this.kfsAccountNumber = kfsAccountNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(final String addedBy) {
        this.addedBy = addedBy;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public void setFileDescription(final String fileDescription) {
        this.fileDescription = fileDescription;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(final int retryCount) {
        this.retryCount = retryCount;
    }

    public void setRetryCount(final String retryCount) {
        if (StringUtils.isNotBlank(retryCount)) {
            this.retryCount = Integer.parseInt(retryCount);
        }
    }

    public boolean isCopied() {
        return copied;
    }

    public void setCopied(final boolean copied) {
        this.copied = copied;
    }

    public void setCopied(final String copied) {
        this.copied = Truth.strToBooleanIgnoreCase(copied, Boolean.FALSE);
    }

}
