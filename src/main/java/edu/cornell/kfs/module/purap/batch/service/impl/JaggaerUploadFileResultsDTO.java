package edu.cornell.kfs.module.purap.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class JaggaerUploadFileResultsDTO {

    private boolean fileProcessedByJaggaer;
    private String fileName;
    private String responseCode;
    private String message;
    private String errorMessage;

    public boolean isFileProcessedByJaggaer() {
        return fileProcessedByJaggaer;
    }

    public void setFileProcessedByJaggaer(boolean fileProcessedByJaggaer) {
        this.fileProcessedByJaggaer = fileProcessedByJaggaer;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = cleanData(fileName);
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = cleanData(responseCode);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = cleanData(message);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = cleanData(errorMessage);
    }
    
    private String cleanData(String input) {
        return StringUtils.trim(input);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
