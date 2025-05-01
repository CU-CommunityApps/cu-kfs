package edu.cornell.kfs.sys.batch;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.ICSVParser;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

/**
 * Custom OpenCSV parser implementation that attempts to clean up improperly configured internal quotes,
 * rather than throwing an exception. It wraps an existing CSVParser and, if it finds a lone double-quote
 * character or an odd number of repeating double-quote characters, it will append an extra double-quote
 * to force the sequence to have an even number of them. This will allow the CSVParser to successfully
 * parse the improperly quoted data.
 * 
 * Note that this implementation does NOT impact quotes that are directly adjacent to a cell boundary.
 * If quotes are found that are up against a cell boundary but are not properly paired, they should be treated
 * as errors since those situations cannot easily discern whether the delimiters were meant to be quoted.
 * (The existing CSVParser should detect all or most of those errors accordingly.)
 */
public class CuCSVParserWithInternalQuoteCleanup implements ICSVParser {

    private static final Logger LOG = LogManager.getLogger();

    private static final Pattern ODD_QUOTES_NOT_NEAR_CELL_BOUNDARY = Pattern.compile(
            "([^\\x7C\\x22])(\\x22(?:\\x22{2})*)([^\\x7C\\x22])");

    private static final String REPLACEMENT_FOR_APPENDING_EXTRA_QUOTE = "$1$2\"$3";

    private final ICSVParser actualParser;

    public CuCSVParserWithInternalQuoteCleanup(final ICSVParser actualParser) {
        this.actualParser = actualParser;
    }

    @Override
    public String getPendingText() {
        return actualParser.getPendingText();
    }

    @Override
    public char getQuotechar() {
        return actualParser.getQuotechar();
    }

    @Override
    public char getSeparator() {
        return actualParser.getSeparator();
    }

    @Override
    public boolean isPending() {
        return actualParser.isPending();
    }

    @Override
    public CSVReaderNullFieldIndicator nullFieldIndicator() {
        return actualParser.nullFieldIndicator();
    }

    @Override
    public String[] parseLine(final String nextLine) throws IOException {
        final String cleanedLine = cleanInternalQuotesThatAreNotAlreadyDoubled(nextLine);
        return actualParser.parseLine(cleanedLine);
    }

    @Override
    public String[] parseLineMulti(final String nextLine) throws IOException {
        final String cleanedLine = cleanInternalQuotesThatAreNotAlreadyDoubled(nextLine);
        return actualParser.parseLineMulti(cleanedLine);
    }

    private String cleanInternalQuotesThatAreNotAlreadyDoubled(final String nextLine) {
        final Matcher matcher = ODD_QUOTES_NOT_NEAR_CELL_BOUNDARY.matcher(nextLine);
        final StringBuilder cleanedLine = new StringBuilder(nextLine.length());
        int replacementCount = 0;
        while (matcher.find()) {
            matcher.appendReplacement(cleanedLine, REPLACEMENT_FOR_APPENDING_EXTRA_QUOTE);
            replacementCount++;
        }
        matcher.appendTail(cleanedLine);
        if (replacementCount > 0) {
            LOG.warn("cleanInternalQuotesThatAreNotAlreadyDoubled, Found {} double quotes that needed cleanup "
                    + "prior to splitting a particular CSV line", replacementCount);
        }
        return cleanedLine.toString();
    }

    @Override
    public String parseToLine(String[] values, boolean applyQuotesToAll) {
        return actualParser.parseToLine(values, applyQuotesToAll);
    }

    @Override
    public void parseToLine(String[] values, boolean applyQuotesToAll, Appendable appendable) throws IOException {
        actualParser.parseToLine(values, applyQuotesToAll, appendable);
    }

    @Override
    public void setErrorLocale(Locale errorLocale) {
        actualParser.setErrorLocale(errorLocale);
    }

}
