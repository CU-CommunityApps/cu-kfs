package edu.cornell.kfs.rass.batch;

import java.util.Optional;

import edu.cornell.kfs.rass.RassConstants.RassParseResultCode;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;

public class RassXmlFileParseResult {

    private final String rassXmlFileName;
    private final RassParseResultCode resultCode;
    private final Optional<RassXmlDocumentWrapper> rassXmlDocumentWrapper;

    public RassXmlFileParseResult(
            String rassXmlFileName, RassParseResultCode resultCode, Optional<RassXmlDocumentWrapper> rassXmlDocumentWrapper) {
        this.rassXmlFileName = rassXmlFileName;
        this.resultCode = resultCode;
        this.rassXmlDocumentWrapper = rassXmlDocumentWrapper;
    }

    public String getRassXmlFileName() {
        return rassXmlFileName;
    }

    public RassParseResultCode getResultCode() {
        return resultCode;
    }

    public boolean hasParsedDocumentWrapper() {
        return rassXmlDocumentWrapper.isPresent();
    }

    public RassXmlDocumentWrapper getParsedDocumentWrapper() {
        return rassXmlDocumentWrapper
                .orElseThrow(() -> new IllegalStateException("No successfully-parsed data exists for file " + rassXmlFileName));
    }

}
