package edu.cornell.kfs.sys.batch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.FlatFileObjectSpecification;
import org.kuali.kfs.sys.batch.FlatFileParseTracker;
import org.kuali.kfs.sys.batch.FlatFileParserBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.ParseException;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CuDelimitedFlatFileParser extends FlatFileParserBase {

    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        try (
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileByteContent);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            CSVReader flatFileReader = buildFlatFileReader(bufferedReader);
        ) {
            return parseResultFromReader(flatFileReader);
        } catch (Exception e) {
            throw new ParseException("Exception encountered while parsing delimited flat file", e);
        }
    }

    protected Object parseResultFromReader(CSVReader flatFileReader) {
        int lineNumber = 0;
        FlatFileSpecificationForProcessingPreSplitLines preSplitSpec = getFlatFileSpecificationForPreSplitLines();
        FlatFileParseTracker tracker = buildNewParseTrackerInstanceFromPrototype();
        tracker.initialize(preSplitSpec);
        
        for (String[] segmentedLine : flatFileReader) {
            lineNumber++;
            String firstLineSegment = (segmentedLine.length > 0) ? segmentedLine[0] : KFSConstants.EMPTY_STRING;
            Object parseIntoObject = tracker.getObjectToParseInto(firstLineSegment);
            if (parseIntoObject != null) {
                FlatFileObjectSpecification parseSpecification = preSplitSpec.getObjectSpecification(parseIntoObject.getClass());
                if (CollectionUtils.isNotEmpty(parseSpecification.getParseProperties())) {
                    preSplitSpec.parseLineIntoObject(parseSpecification, segmentedLine, parseIntoObject, lineNumber);
                    tracker.completeLineParse();
                }
            }
        }
        
        return tracker.getParsedObjects();
    }

    protected CSVReader buildFlatFileReader(Reader flatFileContent) {
        FlatFileSpecificationForProcessingPreSplitLines preSplitSpec = getFlatFileSpecificationForPreSplitLines();
        String delimiter = preSplitSpec.getDelimiter();
        if (StringUtils.length(delimiter) != 1) {
            throw new IllegalStateException("The flat file specification should have had a single-character delimiter, but instead had '"
                    + delimiter + "'");
        }
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(delimiter.charAt(0))
                .build();
        return new CSVReaderBuilder(flatFileContent)
                .withCSVParser(parser)
                .build();
    }

    protected FlatFileSpecificationForProcessingPreSplitLines getFlatFileSpecificationForPreSplitLines() {
        if (flatFileSpecification == null) {
            throw new IllegalStateException("The flat file specification cannot be null");
        } else if (!(flatFileSpecification instanceof FlatFileSpecificationForProcessingPreSplitLines)) {
            throw new IllegalStateException("The given flat file specification does not support processing pre-split file lines");
        }
        return (FlatFileSpecificationForProcessingPreSplitLines) flatFileSpecification;
    }

    protected FlatFileParseTracker buildNewParseTrackerInstanceFromPrototype() {
        return SpringContext.getBean(FlatFileParseTracker.class);
    }

}
