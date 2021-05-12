package edu.cornell.kfs.kew.routeheader.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;

/*
 * NOTE: A few areas of our code (most notably in the Tax module) depend on this interface.
 * We have copied it over from Cynergy and renamed it accordingly, to allow the dependent code
 * to compile with only minimal changes. We either need to copy and remediate Cynergy's implementation
 * of this interface, or we need to update the dependent code to retrieve the needed data
 * in another way.
 */
/**
 * Custom sub-interface of RouteHeaderService that adds new methods
 * for single and bulk retrieval of last-modified-date and finalized-date.
 */
public interface CuRouteHeaderService extends RouteHeaderService {

    /**
     * Retrieves the Last Modified Date for a particular document.
     * 
     * @param documentId The document's ID.
     * @return The last-modified date of the given document, or null if no such document exists.
     * @throws IllegalArgumentException if documentId is blank.
     */
    Timestamp getLastModifiedDate(String documentId);

    /**
     * Retrieves the Last Modified Date for a list of documents.
     * 
     * @param documentIds The documents' IDs.
     * @return A Map from docIds to last-modified dates, or an empty Map if no results were found.
     * @throws IllegalArgumentException if documentIds is null.
     */
    Map<String, Timestamp> getLastModifiedDates(List<String> documentIds);

    /**
     * Retrieves the finalized date for a particular document.
     * 
     * @param documentId The document's ID.
     * @return The finalized date of the given document, or null if no such document exists.
     * @throws IllegalArgumentException if documentId is blank.
     */
    Timestamp getFinalizedDate(String documentId);

    /**
     * Retrieves the finalized date for a list of documents.
     * 
     * @param documentIds The documents' IDs.
     * @return A Map from docIds to finalized dates, or an empty Map if no results were found.
     * @throws IllegalArgumentException if documentIds is null.
     */
    Map<String, Timestamp> getFinalizedDates(List<String> documentIds);

    /**
     * Retrieves the finalized date for a particular document type and date range.
     * 
     * @param documentTypeName The document type of the documents to include in the results.
     * @param startDate The start of the date range for finalization dates to return, inclusive.
     * @param endDate The end of the date range for finalization dates to return, inclusive.
     * @return A Map from docIds to finalized dates, or an empty Map if no results were found.
     * @throws IllegalArgumentException if documentTypeName is blank, startDate is null, endDate is null, or startDate is after endDate.
     */
    Map<String, Timestamp> getFinalizedDatesForDocumentType(String documentTypeName, Timestamp startDate, Timestamp endDate);

}
