package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RassBatchJobReport {

    private final List<RassXmlFileParseResult> fileParseResults;
    private final Map<String, RassXmlFileProcessingResult> fileProcessingResults;

    public RassBatchJobReport(List<RassXmlFileParseResult> fileParseResults,
            Map<String, RassXmlFileProcessingResult> fileProcessingResults) {
        this.fileParseResults = Collections.unmodifiableList(new ArrayList<>(fileParseResults));
        this.fileProcessingResults = Collections.unmodifiableMap(new HashMap<>(fileProcessingResults));
    }

    public List<RassXmlFileParseResult> getFileParseResults() {
        return fileParseResults;
    }

    public Map<String, RassXmlFileProcessingResult> getFileProcessingResults() {
        return fileProcessingResults;
    }

}
