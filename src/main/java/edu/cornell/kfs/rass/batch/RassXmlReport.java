package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RassXmlReport {

    private final List<RassXmlFileParseResult> fileParseResults;
    private final List<RassXmlObjectGroupResult> objectGroupResults;

    public RassXmlReport(List<RassXmlFileParseResult> fileParseResults, List<RassXmlObjectGroupResult> objectGroupResults) {
        this.fileParseResults = Collections.unmodifiableList(new ArrayList<>(fileParseResults));
        this.objectGroupResults = Collections.unmodifiableList(new ArrayList<>(objectGroupResults));
    }

    public List<RassXmlFileParseResult> getFileParseResults() {
        return fileParseResults;
    }

    public List<RassXmlObjectGroupResult> getObjectGroupResults() {
        return objectGroupResults;
    }

}
