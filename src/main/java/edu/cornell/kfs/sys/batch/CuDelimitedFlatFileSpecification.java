package edu.cornell.kfs.sys.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.DelimitedFlatFilePropertySpecification;
import org.kuali.kfs.sys.batch.DelimitedFlatFileSpecification;
import org.kuali.kfs.sys.batch.FlatFileObjectSpecification;
import org.kuali.kfs.sys.batch.FlatFilePropertySpecification;

public class CuDelimitedFlatFileSpecification extends DelimitedFlatFileSpecification
        implements FlatFileSpecificationForProcessingPreSplitLines {

    private static final Logger LOG = LogManager.getLogger();

    private boolean performCleanupOfInternalQuotes;

    @Override
    public void parseLineIntoObject(
            final FlatFileObjectSpecification parseSpecification, final String lineToParse,
            final Object parseIntoObject, final int lineNumber) {
        throw new UnsupportedOperationException("This specification does not support splitting up parsed lines on its own; "
                + "please use the overloaded method that accepts a pre-split line as input and has similar logic to the superclass's parsing.");
    }

    @Override
    public void parseLineIntoObject(
            final FlatFileObjectSpecification parseSpecification, final String[] lineSegments,
            final Object parseIntoObject, final int lineNumber) {
        for (final FlatFilePropertySpecification propertySpecification : parseSpecification.getParseProperties()) {
            try {
                final DelimitedFlatFilePropertySpecification delimitedPropertySpecification = (DelimitedFlatFilePropertySpecification) propertySpecification;
                final String lineSegment = lineSegments[delimitedPropertySpecification.getLineSegmentIndex()];
                propertySpecification.setProperty(lineSegment, parseIntoObject, lineNumber);
            } catch (final ArrayIndexOutOfBoundsException e) {
                LOG.debug(
                        "parseLineIntoObject: Unable to set property {} since lineSegmentIndex does not exist for line",
                        propertySpecification::getPropertyName
                );
            }
        }
    }

    @Override
    public String getDelimiter() {
        return delimiter;
    }

    @Override
    public boolean isPerformCleanupOfInternalQuotes() {
        return performCleanupOfInternalQuotes;
    }

    public void setPerformCleanupOfInternalQuotes(
            final boolean performCleanupOfInternalQuotes) {
        this.performCleanupOfInternalQuotes = performCleanupOfInternalQuotes;
    }

}
