package edu.cornell.kfs.cemi.vnd.batch.service;

import java.time.LocalDateTime;

/**
 * Service interface for the CEMI Remit To Supplier Connection extract.
 * 
 * This service generates a file containing Remit To Supplier Connection data
 * that maps KFS vendor remit addresses to Workday supplier connections.
 */
public interface CemiRemitToSupplierExtractService {

    /**
     * Resets the service state before a new extract run.
     * Clears any cached data from previous runs.
     */
    void resetState();

    /**
     * Initializes the date range settings for the extract query
     * based on parameter values.
     */
    void initializeExtractDateRangeSettings();

    /**
     * Generates the Remit To Supplier Connection extract file.
     * 
     * This method:
     * 1. Queries vendors with remit-to addresses
     * 2. Builds CemiRemitToSupplierConnection DTOs
     * 3. Writes the data to an Excel file
     * 
     * @param jobRunDate The date/time the job is running
     */
    void generateRemitToSupplierExtractFile(final LocalDateTime jobRunDate);

}