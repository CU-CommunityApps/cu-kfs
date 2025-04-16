package edu.cornell.kfs.sys.batch;

import org.kuali.kfs.sys.batch.FlatFileObjectSpecification;
import org.kuali.kfs.sys.batch.FlatFileSpecification;

/**
 * Base interface for flat file specifications that expect the parser to pre-split the input lines,
 * rather than having the specification instance split the lines itself.
 */
public interface FlatFileSpecificationForProcessingPreSplitLines extends FlatFileSpecification {

    void parseLineIntoObject(FlatFileObjectSpecification parseSpecification, String[] lineSegments, Object parseIntoObject, int lineNumber);

    String getDelimiter();

    boolean isPerformCleanupOfInternalQuotes();

}
