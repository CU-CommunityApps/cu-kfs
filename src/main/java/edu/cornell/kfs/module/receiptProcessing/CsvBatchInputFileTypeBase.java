/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.module.receiptProcessing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import com.opencsv.CSVReader;

/**
 * Base class for BatchInputFileType implementations that validate using an Enum class (use as the CSV file header)
 * and parse using CSV comma delimited
 */
public abstract class CsvBatchInputFileTypeBase<E extends Enum<E>> extends BatchInputFileTypeBase {
    private static final Logger LOG = LogManager.getLogger();

    private Class<?> csvEnumClass;

    public CsvBatchInputFileTypeBase() {
        super();
    }

    public void setCsvEnumClass(Class<?> csvEnumClass) {
        this.csvEnumClass = csvEnumClass;
    }

    @Override
    public void process(String fileName, Object parsedFileContents) {
        // default impl does nothing
    }

    /**
     * @return parsed object in structure - List<Map<String, String>>
     */
    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        // handle null objects and zero byte contents
        String errorMessage = fileByteContent == null ? "an invalid(null) argument was given" :
            fileByteContent.length == 0 ? "an invalid argument was given, empty input stream" : "";

        if (!errorMessage.isEmpty()) {
            LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        List<String> headerList = getCsvHeaderList();
        Object parsedContents;

        validateCSVHeader(fileByteContent, headerList);

        final CSVParser csvParser = new CSVParserBuilder().withSeparator(',').withQuoteChar('"').withEscapeChar('|').build();
        try (
                final InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(fileByteContent), StandardCharsets.UTF_8);
                final CSVReader csvReader = new CSVReaderBuilder(inputStreamReader).withSkipLines(1).withCSVParser(csvParser).build();
        ) {
            List<String[]> dataList = csvReader.readAll();

            //parse and create List of Maps base on enum value names as map keys
            List<Map<String, String>> dataMapList = new ArrayList<>();
            Map<String, String> rowMap;
            int index;
            for (String[] row : dataList) {
                rowMap = new LinkedHashMap<>();
                // reset index
                index = 0;

                for (String header : headerList) {
                    rowMap.put(header, row[index++]);
                }
                dataMapList.add(rowMap);
            }

            parsedContents = dataMapList;
        } catch (final CsvException | IOException ex) {
            LOG.error(ex);
            throw new ParseException(ex.getMessage(), ex);
        }
        return convertParsedObjectToVO(parsedContents);
    }

    protected void validateCSVHeader(byte[] fileByteContent, List<String> headerList) throws ParseException {
        try (ByteArrayInputStream validateFileContents = new ByteArrayInputStream(fileByteContent)) {
            validateCSVFileInput(headerList, validateFileContents);
        } catch (CsvException | IOException ex) {
            LOG.error("validateCSVHeader: " + ex.getMessage(), ex);
            throw new ParseException(ex.getMessage(), ex);
        }
    }

    /**
     * Validates the CSV file content against the CSVEnum items as header
     * 1. content header must match CSVEnum order/value
     * 2. each data row should have same size as the header
     *
     * @param expectedHeaderList expected CSV header String list
     * @param fileContents       contents to validate
     * @throws IOException
     */
    protected void validateCSVFileInput(final List<String> expectedHeaderList, InputStream fileContents) throws 
            CsvException, IOException {
        //use csv reader to parse the csv content
        final CSVParser csvParser = new CSVParserBuilder().withSeparator(',').withQuoteChar('"').withEscapeChar('|').build();
        try (
                final InputStreamReader inputStreamReader = new InputStreamReader(fileContents, StandardCharsets.UTF_8);
                CSVReader csvReader = new CSVReaderBuilder(inputStreamReader).withCSVParser(csvParser).build();
        ) {
            List<String> inputHeaderList = Arrays.asList(csvReader.readNext());

            String errorMessage;

            if (!CollectionUtils.isEqualCollection(expectedHeaderList, inputHeaderList)) {
                errorMessage = "CSV Batch Input File contains incorrect number of headers";
                //collection has same elements, now check the exact content orders by looking at the toString comparisons
            } else if (!expectedHeaderList.equals(inputHeaderList)) {
                errorMessage = "CSV Batch Input File headers are different";
            } else {
                errorMessage = validateDetailRowsContainExpectedNumberOfFields(expectedHeaderList, csvReader);
            }

            if (errorMessage != null) {
                LOG.error(errorMessage);
                throw new ParseException(errorMessage);
            }
        }
    }
    
    private String validateDetailRowsContainExpectedNumberOfFields(List<String> expectedHeaderList,
            CSVReader csvReader) throws CsvException, IOException {
        String errorMessage = null;

        int line = 1;
        String[] nextLine;
        while ((nextLine = csvReader.readNext()) != null) {
            List<String> inputDataList = Arrays.asList(nextLine);
            if (inputDataList.size() != expectedHeaderList.size()) {
                errorMessage = "line " + line + " layout does not match the header";
                break;
            }
            line++;
        }

        return errorMessage;
    }

    /**
     * build the csv header list base on provided csv enum class
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected List<String> getCsvHeaderList() {
        List<String> headerList = new ArrayList<>();
        final EnumSet<E> enums = EnumSet.allOf((Class) csvEnumClass);
        for (final Enum<E> e : enums) {
            headerList.add(e.name());
        }
        return headerList;
    }

    /**
     * convert the parsed content into VOs
     *
     * @param parsedContent
     * @return
     */
    protected abstract Object convertParsedObjectToVO(Object parsedContent);

    @Override
    public String getAuthorPrincipalName(File file) {
        String[] fileNameParts = StringUtils.split(file.getName(), "_");
        if (fileNameParts.length > 3) {
            return fileNameParts[2];
        }
        return null;
    }
}
