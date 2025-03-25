package edu.cornell.kfs.sys.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger LOG = LogManager.getLogger(CuDelimitedFlatFileParser.class);

    protected Class<? extends FlatFileParseTracker> flatFileParseTrackerClass;

    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileByteContent);
                InputStreamReader streamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(streamReader);
//                preprocessCSV(bufferedReader);
                CSVReader flatFileReader = buildFlatFileReader(bufferedReader)) {
            return parseResultFromReader(flatFileReader);
        } catch (Exception e) {
            throw new ParseException("Exception encountered while parsing delimited flat file", e);
        }
    }
    
//    public static String preprocessLine(String line) {
//        // Regex pattern to match values enclosed in double quotes
//        Pattern quotedPattern = Pattern.compile("\"([^\"]*)\"");
//        Matcher matcher = quotedPattern.matcher(line);
//        
//        StringBuffer cleanedLine = new StringBuffer();
//
//        while (matcher.find()) {
//            String value = matcher.group(1);
//            // Fix unescaped quotes inside quoted fields by replacing `"` with `""`
//            String fixedValue = value.replace("\"", "\"\"");
//            matcher.appendReplacement(cleanedLine, "\"" + fixedValue + "\"");
//        }
//        matcher.appendTail(cleanedLine);
//
//        // Remove stray/unmatched quotes outside of proper quoted fields
//        return cleanedLine.toString().replaceAll("(?<!\\|)\"(?!\\|)", "");
//    }
//
//    public static BufferedReader processFileLineByLine(String filePath) throws IOException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                writer.write(preprocessLine(line));
//                writer.newLine();
//            }
//        }
//        
//        writer.flush();
//        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray()), StandardCharsets.UTF_8));
//    }

    protected Object parseResultFromReader(CSVReader flatFileReader) {
        int lineNumber = 0;
        FlatFileSpecificationForProcessingPreSplitLines preSplitSpec = getFlatFileSpecificationForPreSplitLines();
        FlatFileParseTracker tracker = buildNewParseTrackerInstanceFromPrototype();
        tracker.initialize(preSplitSpec);
        
        try {
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
        } catch (RuntimeException e) {
            LOG.error("parseResultFromReader: File parsing encountered an exception on line " + lineNumber
                    + " or line " + (lineNumber + 1) + "; the data file may have a syntax error", e);
            throw e;
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
        .withQuoteChar('"')  // Ensure quoted values are handled correctly
        .withIgnoreLeadingWhiteSpace(true)
        .withStrictQuotes(false) // Allow unescaped quotes within fields
        .build();
        return new CSVReaderBuilder(flatFileContent)
                .withCSVParser(parser)
                .build();
    }

    protected FlatFileSpecificationForProcessingPreSplitLines getFlatFileSpecificationForPreSplitLines() {
        if (flatFileSpecification == null) {
            throw new IllegalStateException("The flat file specification cannot be null");
        } else if (!(flatFileSpecification instanceof FlatFileSpecificationForProcessingPreSplitLines)) {
            throw new IllegalStateException("The flat file specification of type " + flatFileSpecification.getClass().getName()
                    + " is not an implementation of " + FlatFileSpecificationForProcessingPreSplitLines.class.getName());
        }
        return (FlatFileSpecificationForProcessingPreSplitLines) flatFileSpecification;
    }

    protected FlatFileParseTracker buildNewParseTrackerInstanceFromPrototype() {
        if (flatFileParseTrackerClass == null) {
            return SpringContext.getBean(FlatFileParseTracker.class);
        } else {
            try {
                return flatFileParseTrackerClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Could not create tracker of type " + flatFileParseTrackerClass.getName(), e);
            }
        }
        
    }

    public void setFlatFileParseTrackerClass(Class<? extends FlatFileParseTracker> flatFileParseTrackerClass) {
        this.flatFileParseTrackerClass = flatFileParseTrackerClass;
    }

}
