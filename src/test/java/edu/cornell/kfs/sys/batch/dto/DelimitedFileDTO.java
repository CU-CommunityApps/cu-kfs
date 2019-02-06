package edu.cornell.kfs.sys.batch.dto;

import java.util.ArrayList;
import java.util.List;

public class DelimitedFileDTO {

    private String fileId;
    private String description;
    private List<DelimitedFileLineDTO> fileLines;

    public DelimitedFileDTO() {
        this.fileLines = new ArrayList<>();
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DelimitedFileLineDTO> getFileLines() {
        return fileLines;
    }

    public void setFileLines(List<DelimitedFileLineDTO> fileLines) {
        this.fileLines = fileLines;
    }

}
