package edu.cornell.kfs.tax.batch;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.web.format.FormatException;
import org.kuali.kfs.core.web.format.Formatter;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.CUTaxPropertyConstants;
import edu.cornell.kfs.tax.FormTypes1099;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;

/**
 * Custom KFS file type that reads Transaction Override objects from a tab-delimited file instead of a comma-delimited one.
 */
public class TransactionOverrideCsvBatchInputFileType extends CsvBatchInputFileTypeBase<TransactionOverrideCsv> {
	private static final Logger LOG = LogManager.getLogger(TransactionOverrideCsvBatchInputFileType.class);

    private static final String UPLOAD_FILE_PREFIX = "taxoverride";
    private static final String NULL_STRING = "null";
    private static final String NONE_STRING = "none";

    private DataDictionaryService dataDictionaryService;
    private CriteriaLookupService criteriaLookupService;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private Formatter dateFormatter;

    // This field is duplicated from the superclass because access to this class is needed here, but the superclass makes it private without any getters.
    private Class<?> csvEnumClass;

    /**
     * This implementation builds a file name with a "taxoverride" prefix, and with default construction
     * logic copied and tweaked from FlatFileParserBase.
     * 
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(java.lang.String, java.lang.Object, java.lang.String)
     */
    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(UPLOAD_FILE_PREFIX).append(principalName);
        if (StringUtils.isNotBlank(fileUserIdentifier)) {
            fileName.append('_').append(fileUserIdentifier);
        }
        fileName.append('_').append(dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate()));
        return StringUtils.remove(fileName.toString(), KFSConstants.BLANK_SPACE);
    }

    @Override
    public String getFileTypeIdentifier() {
        return CUTaxConstants.TRANSACTION_OVERRIDE_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    /**
     * Implemented to use the same logic as FlatFileParserBase for obtaining
     * the file author from the file name, but with a fixed prefix instead.
     * 
     * @see org.kuali.kfs.sys.batch.BatchInputType#getAuthorPrincipalName(java.io.File)
     */
    @Override
    public String getAuthorPrincipalName(File file) {
        return StringUtils.substringBetween(file.getName(), UPLOAD_FILE_PREFIX, "_");
    }

    @Override
    public String getTitleKey() {
        return CUTaxKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_TRANSACTION_OVERRIDE;
    }

    /**
     * Overridden to parse a tab-delimited file instead of a comma-delimited one (unlike similar subclasses),
     * and to use the convertParsedObjectToVO method to create the desired objects (like similar subclasses).
     * Some other tweaks have also been made to improve upon the existing superclass code.
     * 
     * @see org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase#parse(byte[])
     */
    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        // handle null objects and zero byte contents
        String errorMessage = fileByteContent == null ? "an invalid(null) argument was given"
                : (fileByteContent.length == 0 ? "an invalid argument was given, empty input stream" : "");
        
        if (!errorMessage.isEmpty()) {
            LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        List<String> headerList = getCsvHeaderList();
        Object parsedContents = null;
        CSVReader csvReader = null;
        try {
            // validate csv header
            ByteArrayInputStream validateFileContents = new ByteArrayInputStream(fileByteContent);
            validateCSVFileInput(headerList, validateFileContents);
            
            //use csv reader to parse tab-delimited content

            csvReader = new CSVReaderBuilder(new InputStreamReader(new ByteArrayInputStream(fileByteContent),
                    StandardCharsets.UTF_8))
            .withCSVParser(new CSVParserBuilder().withSeparator('\t').build())
            .build();
            List<String[]> dataList = csvReader.readAll();
            
            //remove first header line
            dataList.remove(0);
            
            //parse and create List of Maps base on enum value names as map keys
            List<Map<String, String>> dataMapList = new ArrayList<Map<String, String>>();
            Map<String, String> rowMap;
            int index = 0;
            for (String[] row : dataList) {
                rowMap = new LinkedHashMap<String, String>();
                // reset index
                index = 0;
                
                for (String header : headerList) {
                    rowMap.put(header, row[index++]);
                }
                dataMapList.add(rowMap);
            }
            
            parsedContents = dataMapList;
        } catch (CsvException | IOException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ParseException(ex.getMessage(), ex);
        } finally {
            IOUtils.closeQuietly(csvReader);
        }
        
        return convertParsedObjectToVO(parsedContents);
    }

    /**
     * Copied from the superclass, and tweaked to work with tab-delimited files instead of comma-delimited ones
     * (as well as to increase visibility from "private" to "protected").
     */
    protected void validateCSVFileInput(final List<String> expectedHeaderList, InputStream fileContents) throws CsvException, IOException {
        //use csv reader to parse tab-delimited content
        CSVReader csvReader= new CSVReaderBuilder(new InputStreamReader(fileContents,
                StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder()
                .withSeparator('\t').build())
                .build();
        try {
            List<String> inputHeaderList = Arrays.asList(csvReader.readNext());

            String errorMessage = null;
            
            // validate
            if (!CollectionUtils.isEqualCollection(expectedHeaderList, inputHeaderList)) {
                errorMessage = "CSV Batch Input File contains incorrect number of headers";
                //collection has same elements, now check the exact content orders by looking at the toString comparisons
            } else if (!expectedHeaderList.equals(inputHeaderList)) {
                errorMessage = "CSV Batch Input File headers are different";
            } else {
                
                //check the content size as well if headers are validated
                int line = 1;
                List<String> inputDataList = Arrays.asList(csvReader.readNext());
                while (inputDataList != null && errorMessage != null) {
                    //if the data list size does not match header list (its missing data)
                    if (inputDataList.size() != expectedHeaderList.size()) {
                        errorMessage = "line " + line + " layout does not match the header";
                    }
                    line++;
                    inputDataList = Arrays.asList(csvReader.readNext());
                }
            }
            
            if (errorMessage != null) {
                LOG.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } finally {
            IOUtils.closeQuietly(csvReader);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object convertParsedObjectToVO(Object parsedContent) {
        List<Map<String,String>> parsedDataList = (List<Map<String,String>>) parsedContent;
        List<Object> transactionOverrides = new ArrayList<Object>(parsedDataList.size());
        int docNumberMaxLength = dataDictionaryService.getAttributeMaxLength(TransactionOverride.class, KFSPropertyConstants.DOCUMENT_NUMBER).intValue();
        int boxNumberMaxLength = dataDictionaryService.getAttributeMaxLength(TransactionOverride.class, CUTaxPropertyConstants.BOX_NUMBER).intValue();
        
        int lineNumber = 1;
        for (Map<String,String> parsedLine : parsedDataList) {
            transactionOverrides.add(convertParsedLineToVO(parsedLine, lineNumber, docNumberMaxLength, boxNumberMaxLength));
            lineNumber++;
        }
        
        return transactionOverrides;
    }

    /**
     * Transforms each parsed line into a TransactionOverride BO if content is valid,
     * or into a String representation of the parsed line if content is invalid.
     * Assumes that TransactionOverrideCsv is the header enum.
     * 
     * @param parsedLine The line to process, as a Map from header names to values.
     * @param lineNumber The current line number, starting from 1.
     * @param docNumberMaxLength The max length of the BO's document number property.
     * @param boxNumberMaxLength The max length of the BO's tax box property.
     * @return A TransactionOverride object if a valid line, otherwise a tab-delimited String representation of the line.
     */
    protected Object convertParsedLineToVO(Map<String,String> parsedLine, int lineNumber, int docNumberMaxLength, int boxNumberMaxLength) {
        boolean valid = true;
        TransactionOverride transOverride = new TransactionOverride();
        
        // Verify that exactly one type of tax box was specified, and that the tax box has the expected max length. Also setup tax box properties if valid.
        String formType1099 = getUpperCasedFieldValue(TransactionOverrideCsv.Form_1099_Type, parsedLine);
        String box1099 = getUpperCasedFieldValue(TransactionOverrideCsv.Form_1099_Box, parsedLine);
        String box1042S = getUpperCasedFieldValue(TransactionOverrideCsv.Form_1042S_Box, parsedLine);
        if (StringUtils.isBlank(box1099)) {
            if (StringUtils.isBlank(box1042S)) {
                LOG.error("Found a line that does not specify a 1099 or 1042S tax box override. Line number: " + Integer.toString(lineNumber));
                valid = false;
            } else if (StringUtils.isNotBlank(formType1099)) {
                LOG.error("Found a line that specifies a 1099 form type but also specifies 1042S tax box override. "
                        + "Line number: " + Integer.toString(lineNumber));
                valid = false;
            } else if (box1042S.length() > boxNumberMaxLength) {
                LOG.error("Found a line with a 1042S box number that is too long. Line number: " + Integer.toString(lineNumber));
                valid = false;
            } else {
                // Validation succeeded; configure override of a 1042S tax box.
                transOverride.setTaxType(CUTaxConstants.TAX_TYPE_1042S);
                transOverride.setBoxNumber(NULL_STRING.equalsIgnoreCase(box1042S) ? CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY : box1042S);
            }
        } else if (StringUtils.isNotBlank(box1042S)) {
            LOG.error("Found a line that specifies both a 1099 and 1042S tax box override. Line number: " + Integer.toString(lineNumber));
            valid = false;
        } else if (StringUtils.isBlank(formType1099)) {
            LOG.error("Found a line with a 1099 box number that is missing a 1099 form type. Line number: "
                    + Integer.toString(lineNumber));
            valid = false;
        } else if (findPotentiallyPresentFormTypes1099FromFormCode(formType1099).isEmpty()
                && !StringUtils.equalsIgnoreCase(formType1099, NULL_STRING)) {
            LOG.error("Found a line with a 1099 box number that has an invalid 1099 form type. Line number: "
                    + Integer.toString(lineNumber));
            valid = false;
        } else if (box1099.length() > CUTaxConstants.TAX_1099_MAX_BUCKET_LENGTH && !NULL_STRING.equalsIgnoreCase(box1099)) {
            LOG.error("Found a line with a 1099 box number that is too long. Line number: " + Integer.toString(lineNumber));
            valid = false;
        } else {
            // Validation succeeded; configure override of a 1099 tax box.
            transOverride.setTaxType(CUTaxConstants.TAX_TYPE_1099);
            transOverride.setFormType(
                    NULL_STRING.equalsIgnoreCase(formType1099) ? CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE : formType1099);
            transOverride.setBoxNumber(NULL_STRING.equalsIgnoreCase(box1099) ? CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY : box1099);
        }
        
        // Setup document number, and verify that the value is non-blank and is not too large.
        transOverride.setDocumentNumber(parsedLine.get(TransactionOverrideCsv.Doc_Number.toString()));
        if (StringUtils.isBlank(transOverride.getDocumentNumber())) {
            LOG.error("Found a line with a blank document number. Line number: " + Integer.toString(lineNumber));
            valid = false;
        } else if (transOverride.getDocumentNumber().length() > docNumberMaxLength) {
            LOG.error("Found a line with a document number that is too long. Line number: " + Integer.toString(lineNumber));
            valid = false;
        }
        
        // Setup document line number, which should be a valid integer.
        try {
            transOverride.setFinancialDocumentLineNumber(Integer.valueOf(parsedLine.get(TransactionOverrideCsv.Doc_Line_Number.toString())));
        } catch (NumberFormatException e) {
            LOG.error("Found a line whose document line number is null or invalid. Line number: " + Integer.toString(lineNumber));
            valid = false;
        }
        
        // Setup university/payment date, which should be a valid SQL Date.
        try {
            String rawUniversityDate = parsedLine.get(TransactionOverrideCsv.Payment_Date.toString());
            java.sql.Date universityDateParsed = parseUniversityDate(rawUniversityDate);
            transOverride.setUniversityDate(universityDateParsed);

            if (transOverride.getUniversityDate() == null) {
                LOG.error("Found a line with a null payment date. Line number: " + Integer.toString(lineNumber));
                valid = false;
            }
        } catch (FormatException | ClassCastException e) {
            LOG.error("Found a line with an invalid payment date. Line number: " + Integer.toString(lineNumber));
            valid = false;
        }
        
        if (valid) {
            // No validation errors found; return the newly-constructed object.
            return transOverride;
        } else {
            // Validation failed; return the line as a tab-limited String instead (with the values ordered accordingly).
            List<String> lineValues = new ArrayList<String>();
            for (String headerName : getCsvHeaderList()) {
                lineValues.add(parsedLine.get(headerName));
            }
            return StringUtils.join(lineValues, '\t');
        }
    }

    private Optional<FormTypes1099> findPotentiallyPresentFormTypes1099FromFormCode(String formCode) {
        try {
            return Optional.of(FormTypes1099.findFormTypes1099FromFormCode(formCode));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private java.sql.Date parseUniversityDate(String universityDateRaw) {
        java.sql.Date ret = null;
        try {
            ret = dateTimeService.convertToSqlDate(universityDateRaw);
        } catch (java.text.ParseException ex) {
            LOG.error("parseUniversityDate: " + ex.toString());
            ret = (java.sql.Date) dateFormatter.convertFromPresentationFormat(universityDateRaw);
        }
        return ret;
    }

    private String getUpperCasedFieldValue(TransactionOverrideCsv fieldKey, Map<String, String> parsedLine) {
        String fieldValue = parsedLine.get(fieldKey.toString());
        return StringUtils.upperCase(fieldValue, Locale.US);
    }

    /**
     * Overridden to save Transaction Override BOs and also print errors to an output file accordingly.
     * 
     * @see org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase#process(java.lang.String, java.lang.Object)
     */
    @Override
    public void process(String fileName, Object parsedFileContents) {
        List<?> parsedObjects = (List<?>) parsedFileContents;
        List<String> invalidLines = new ArrayList<String>();
        
        // Save the valid transaction overrides, and aggregate the invalid lines.
        for (Object parsedObject : parsedObjects) {
            if (parsedObject instanceof TransactionOverride) {
                // Valid line; add new override or update existing override.
                TransactionOverride transOverride = (TransactionOverride) parsedObject;
                String oldBoxOverride = NONE_STRING;
                transOverride.setActive(true);
                
                // Check if override already exists.
                List<TransactionOverride> existingOverride = criteriaLookupService.lookup(TransactionOverride.class, QueryByCriteria.Builder.fromPredicates(
                        PredicateFactory.equal(KFSPropertyConstants.UNIVERSITY_DATE, transOverride.getUniversityDate()),
                        PredicateFactory.equal(KFSPropertyConstants.DOCUMENT_NUMBER, transOverride.getDocumentNumber()),
                        PredicateFactory.equal(KFSPropertyConstants.FINANCIAL_DOCUMENT_LINE_NUMBER, transOverride.getFinancialDocumentLineNumber()),
                        PredicateFactory.equal(CUTaxPropertyConstants.TAX_TYPE, transOverride.getTaxType())
                )).getResults();
                
                if (!existingOverride.isEmpty()) {
                    // If override already exists, copy appropriate properties to the updated object.
                    transOverride.setObjectId(existingOverride.get(0).getObjectId());
                    transOverride.setVersionNumber(existingOverride.get(0).getVersionNumber());
                    oldBoxOverride = existingOverride.get(0).getBoxNumber();
                }
                
                // Save and log addition/update.
                transOverride = businessObjectService.save(transOverride);
                LOG.info("Saved override for date " + transOverride.getUniversityDate() + ", doc " + transOverride.getDocumentNumber()
                        + ", line " + transOverride.getFinancialDocumentLineNumber() + ", and tax type " + transOverride.getTaxType()
                        + " --- Old Box Override: " + oldBoxOverride + ", New Box Override: " + transOverride.getBoxNumber());
                
            } else if (parsedObject instanceof String) {
                // Invalid line; add to list.
                invalidLines.add((String) parsedObject);
            }
        }
        
        // Print any invalid lines to an error output file.
        if (!invalidLines.isEmpty()) {
            writeValidationErrorFile(fileName, invalidLines);
        }
    }

    /**
     * Prints the header line and the problem lines to a separate file;
     * should only be called if one or more override lines contained errors.
     * 
     * @param fileName The filename and path to use; typically the name that would have been used if the parser's shouldSave() method had returned true.
     * @param invalidLines The error lines that should be printed to the file.
     */
    protected void writeValidationErrorFile(String fileName, List<String> invalidLines) {
        fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_error" + fileName.substring(fileName.lastIndexOf('.'));
        File errorFile = new File(fileName);
        if (errorFile.exists()) {
            throw new FileStorageException("Transaction override error file already exists: " + fileName);
        }
        
        BufferedWriter writer = null;
        try {
            // Create the file, and add a header line and the error lines.
            writer = new BufferedWriter(new PrintWriter(errorFile, StandardCharsets.UTF_8));
            writer.write(StringUtils.join(getCsvHeaderList(), '\t'));
            writer.write('\n');
            for (String invalidLine : invalidLines) {
                writer.write(invalidLine);
                writer.write('\n');
            }
            writer.flush();
        } catch (IOException e) {
            throw new FileStorageException("Encountered error while writing transaction error output file", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        
        LOG.warn("Found " + Integer.toString(invalidLines.size()) + " error lines, these have been written to a new error file.");
        GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, CUTaxConstants.CUTaxKeyConstants.ERROR_BATCH_UPLOAD_INVALID_TRANSACTION_OVERRIDES,
                Integer.toString(invalidLines.size()));
    }

    /**
     * Overridden to build a list of the enum constants' toString() values instead,
     * since the expected enum class is using custom toString() header names in this case.
     * TransactionOverrideCsv is the expected enum class.
     * 
     * Subclasses that use a different enum class will need to override this accordingly.
     * 
     * @see org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase#getCsvHeaderList()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<String> getCsvHeaderList() {
        List<String> headers = new ArrayList<String>();
        for (TransactionOverrideCsv enumConst : EnumSet.allOf((Class<TransactionOverrideCsv>) getCsvEnumClass())) {
            headers.add(enumConst.toString());
        }
        return headers;
    }

    protected Class<?> getCsvEnumClass() {
        return csvEnumClass;
    }

    @Override
    public void setCsvEnumClass(Class<?> csvEnumClass) {
        super.setCsvEnumClass(csvEnumClass);
        this.csvEnumClass = csvEnumClass;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setCriteriaLookupService(CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setDateFormatter(Formatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

}
