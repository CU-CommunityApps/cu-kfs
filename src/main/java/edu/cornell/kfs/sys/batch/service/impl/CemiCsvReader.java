package edu.cornell.kfs.sys.batch.service.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.sys.KFSConstants;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVParser;

public class CemiCsvReader implements Closeable {

    private final FileInputStream inputStream;
    private final InputStreamReader streamReader;
    private final CSVReader csvReader;
    private final File inputFile;

    public CemiCsvReader(final String inputFileName) throws IOException {
        this(convertToFile(inputFileName));
    }

    private static File convertToFile(final String inputFileName) {
        Validate.notBlank(inputFileName, "inputFileName cannot be blank");
        return new File(inputFileName);
    }

    public CemiCsvReader(final File inputFile) throws IOException {
        Validate.notNull(inputFile, "inputFile cannot be null");

        FileInputStream inputStream = null;
        InputStreamReader streamReader = null;
        CSVReader csvReader = null;
        boolean success = false;

        try {
            inputStream = new FileInputStream(inputFile);
            streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            csvReader = buildCSVReader(streamReader);
            this.inputStream = inputStream;
            this.streamReader = streamReader;
            this.csvReader = csvReader;
            this.inputFile = inputFile;
            success = true;
        } finally {
            if (!success) {
                IOUtils.closeQuietly(csvReader, streamReader, inputStream);
            }
        }
    }

    private static CSVReader buildCSVReader(final Reader fileReader) {
        final ICSVParser csvParser = new CSVParserBuilder()
                .withSeparator(KFSConstants.COMMA.charAt(0))
                .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build();

        return new CSVReaderBuilder(fileReader)
                .withCSVParser(csvParser)
                .build();
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(csvReader, streamReader, inputStream);
    }

    public Iterator<String[]> iterator() {
        return csvReader.iterator();
    }

    public CSVReader getCsvReader() {
        return csvReader;
    }

    public File getInputFile() {
        return inputFile;
    }

}
