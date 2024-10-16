package edu.cornell.kfs.concur.batch;

import java.util.regex.Matcher;
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
            if (StringUtils.isEmpty(lineSegment)) {
                cleanedSegments[index] = lineSegment;
                continue;
            }
            String cleanedSegment = TABS_PATTERN.matcher(lineSegment).replaceAll(KFSConstants.BLANK_SPACE);
            final Matcher removableCharsMatcher = REMOVABLE_CHARS_PATTERN.matcher(cleanedSegment);
            if (removableCharsMatcher.find()) {
                LOG.warn("parseLineIntoObject, Found special characters or extra quotes on SAE line {} at column {}",
                        lineNumber, index + 1);
                saeLine.addColumnNumberContainingSpecialCharacters(index + 1);
                removableCharsMatcher.reset();
                cleanedSegment = removableCharsMatcher.replaceAll(KFSConstants.EMPTY_STRING);
            }
            cleanedSegments[index] = cleanedSegment;
        }
        super.parseLineIntoObject(parseSpecification, cleanedSegments, parseIntoObject, lineNumber);
    }

}
