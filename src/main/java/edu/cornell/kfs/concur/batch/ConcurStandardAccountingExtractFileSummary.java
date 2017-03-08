package edu.cornell.kfs.concur.batch;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Helper class containing data about an attempt to extract an SAE file to KFS.
 */
// TODO: This class needs to be modified by KFSPTS-8040 to conform to the specs!
public class ConcurStandardAccountingExtractFileSummary {

    protected static final String DEFAULT_FAILURE_MESSAGE = "An unexpected KFS error occurred";

    protected ConcurStandardAccountingExtractPdpSummary pdpSummary;
    protected ConcurStandardAccountingExtractCollectorSummary collectorSummary;

    public ConcurStandardAccountingExtractFileSummary(ConcurStandardAccountingExtractPdpSummary pdpSummary,
            ConcurStandardAccountingExtractCollectorSummary collectorSummary) {
        if (pdpSummary == null) {
            throw new IllegalArgumentException("pdpSummary cannot be null");
        } else if (collectorSummary == null) {
            throw new IllegalArgumentException("collectorSummary cannot be null");
        }
        this.pdpSummary = pdpSummary;
        this.collectorSummary = collectorSummary;
    }

    public ConcurStandardAccountingExtractPdpSummary getPdpSummary() {
        return pdpSummary;
    }

    public ConcurStandardAccountingExtractCollectorSummary getCollectorSummary() {
        return collectorSummary;
    }

    public boolean isFileProcessingSucceeded() {
        return pdpSummary.isFileProcessingSucceeded() && collectorSummary.isFileProcessingSucceeded();
    }

    public List<String> getGeneratedFiles() {
        return Arrays.asList(pdpSummary.getFilePath(), collectorSummary.getFilePath());
    }

    public String getFailureReason() {
        if (isFileProcessingSucceeded()) {
            return StringUtils.EMPTY;
        } else if (StringUtils.isNotBlank(pdpSummary.getFailureReason())) {
            return pdpSummary.getFailureReason();
        } else if (StringUtils.isNotBlank(collectorSummary.getFailureReason())) {
            return collectorSummary.getFailureReason();
        } else {
            return DEFAULT_FAILURE_MESSAGE;
        }
    }

}
