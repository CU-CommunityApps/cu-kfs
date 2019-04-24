package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RassBatchJobReport {

    private final List<RassXmlFileParseResult> fileParseResults;
    private final RassXmlProcessingResults processingResults;

    public RassBatchJobReport(List<RassXmlFileParseResult> fileParseResults, RassXmlProcessingResults processingResults) {
        this.fileParseResults = Collections.unmodifiableList(new ArrayList<>(fileParseResults));
        this.processingResults = processingResults;
    }

    public List<RassXmlFileParseResult> getFileParseResults() {
        return fileParseResults;
    }

    public RassXmlProcessingResults getProcessingResults() {
        return processingResults;
    }

}
