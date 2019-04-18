package edu.cornell.kfs.rass.batch;

import java.util.Optional;

import edu.cornell.kfs.rass.RassConstants.RassResultCode;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;

public class RassXmlFileParseResult {

    private final String rassXmlFileName;
    private final RassResultCode resultCode;
    private final Optional<RassXmlDocumentWrapper> parsedContent;

    public RassXmlFileParseResult(
            String rassXmlFileName, RassResultCode resultCode, Optional<RassXmlDocumentWrapper> parsedContent) {
        this.rassXmlFileName = rassXmlFileName;
        this.resultCode = resultCode;
        this.parsedContent = parsedContent;
    }

    public String getRassXmlFileName() {
        return rassXmlFileName;
    }

    public RassResultCode getResultCode() {
        return resultCode;
    }

    public boolean hasParsedContent() {
        return parsedContent.isPresent();
    }

    public RassXmlDocumentWrapper getParsedContent() {
        return parsedContent
                .orElseThrow(() -> new IllegalStateException("No successfully-parsed data exists for file " + rassXmlFileName));
    }

}
