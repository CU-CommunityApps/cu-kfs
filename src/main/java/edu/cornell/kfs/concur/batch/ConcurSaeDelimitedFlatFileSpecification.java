package edu.cornell.kfs.concur.batch;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.FlatFileObjectSpecification;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractLineBase;
import edu.cornell.kfs.sys.batch.CuDelimitedFlatFileSpecification;

public class ConcurSaeDelimitedFlatFileSpecification extends CuDelimitedFlatFileSpecification {

    private static final Logger LOG = LogManager.getLogger();

    private static final Pattern TABS_PATTERN = Pattern.compile("[\\t]");
    private static final Pattern REMOVABLE_CHARS_PATTERN = Pattern.compile("[^\\x20\\x21\\x23-\\x7E]");

    @Override
    public void parseLineIntoObject(
            final FlatFileObjectSpecification parseSpecification, final String[] lineSegments,
            final Object parseIntoObject, final int lineNumber) {
        if (!(parseIntoObject instanceof ConcurStandardAccountingExtractLineBase)) {
            throw new IllegalStateException("Line " + lineNumber
                    + " has an unexpected Concur SAE line type: " + parseIntoObject.getClass());
        }
        final ConcurStandardAccountingExtractLineBase saeLine =
                (ConcurStandardAccountingExtractLineBase) parseIntoObject;
        saeLine.setLineNumber(lineNumber);

        final String[] cleanedSegments = new String[lineSegments.length];
        int index = -1;
        for (final String lineSegment : lineSegments) {
            index++;
            final String cleanedSegment = cleanLineSegment(lineSegment);
            cleanedSegments[index] = cleanedSegment;
            if (!StringUtils.equals(lineSegment, cleanedSegment)) {
                LOG.warn("parseLineIntoObject, Found tabs, special characters or extra quotes on SAE line {} "
                        + "at column {}", lineNumber, index + 1);
                saeLine.addColumnNumberContainingSpecialCharacters(index + 1);
            }
        }
        super.parseLineIntoObject(parseSpecification, cleanedSegments, parseIntoObject, lineNumber);
    }

    private String cleanLineSegment(final String lineSegment) {
        if (StringUtils.isEmpty(lineSegment)) {
            return lineSegment;
        }
        String cleanedSegment = replaceTabsWithSpaces(lineSegment);
        cleanedSegment = removeInvalidCharacters(cleanedSegment);
        return cleanedSegment;
    }

    private String replaceTabsWithSpaces(final String lineSegment) {
        return TABS_PATTERN.matcher(lineSegment).replaceAll(KFSConstants.BLANK_SPACE);
    }

    private String removeInvalidCharacters(final String lineSegment) {
        return REMOVABLE_CHARS_PATTERN.matcher(lineSegment).replaceAll(KFSConstants.EMPTY_STRING);
    }

}
