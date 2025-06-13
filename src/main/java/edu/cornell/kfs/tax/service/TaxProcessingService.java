package edu.cornell.kfs.tax.service;

import java.time.LocalDateTime;
import java.util.List;

import edu.cornell.kfs.tax.batch.TaxDataDefinition;
import edu.cornell.kfs.tax.batch.TaxOutputDefinition;
import edu.cornell.kfs.tax.businessobject.ObjectCodeBucketMapping;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;

/**
 * Primary interface for services that perform 1099/1042S tax processing
 * and retrieve related helper data or metadata.
 */
public interface TaxProcessingService {

    /**
     * Performs the main tax processing tasks.
     * 
     * @param taxType The type of tax processing to perform (1099, 1042S, etc.).
     * @param processingStartDate The date-time when the current tax processing started.
     * @throws IllegalArgumentException if taxType is blank or an unsupported value, or if processingStartDate is null.
     */
    void doTaxProcessing(String taxType, LocalDateTime processingStartDate);

    /**
     * Retrieves all active object-code-to-tax-bucket mappings for the given tax type.
     * 
     * @param taxType The type of tax processing being performed (1099, 1042S, etc.).
     * @return A list of all active ObjectCodeBucketMapping objects for the given tax type.
     * @throws IllegalArgumentException if taxType is blank or an unsupported value.
     */
    List<ObjectCodeBucketMapping> getBucketMappings(String taxType);

    /**
     * Retrieves all active tax bucket overrides for the given tax type and date range.
     * 
     * @param taxType taxType The type of tax processing being performed (1099, 1042S, etc.).
     * @param startDate The start of the date range to obtain transaction overrides for.
     * @param endDate The end of the date range to obtain transaction overrides for.
     * @return A list of all active TransactionOverride objects for the given tax type and date range.
     * @throws IllegalArgumentException if any arguments are null/blank.
     */
    List<TransactionOverride> getTransactionOverrides(String taxType, java.sql.Date startDate, java.sql.Date endDate);

    /**
     * Retrieves an output definition by parsing the XML content from the indicated file,
     * which can be used to define how the tax data should be appended to the output files.
     * 
     * <p>The path of the file to parse will be derived from the parameter that has the given prefix,
     * and whose suffix is either the given report year or the string "default" (with the former
     * taking precedence).</p>
     * 
     * @param taxParamPrefix The prefix of the parameter containing the path of the XML file to parse.
     * @param reportYear The report year to use as the parameter suffix.
     * @return A TaxOutputDefinition parsed from the given report-year-specific or default file.
     */
    TaxOutputDefinition getOutputDefinition(String taxParamPrefix, int reportYear);

    /**
     * Retrieves a data definition by parsing the XML content from the indicated file,
     * which can be used to define how to read/write tax data for the KFS tables.
     * 
     * <p>The path of the file to parse will be derived from the parameter that has the given prefix,
     * and whose suffix is either the given report year or the string "default" (with the former
     * taking precedence).</p>
     * 
     * @param taxParamPrefix The prefix of the parameter containing the path of the XML file to parse.
     * @param reportYear The report year to use as the parameter suffix.
     * @return A TaxDataDefinition parsed from the given report-year-specific or default file.
     */
    TaxDataDefinition getDataDefinition(String taxParamPrefix, int reportYear);
}
