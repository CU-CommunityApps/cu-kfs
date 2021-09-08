package edu.cornell.kfs.kew.routeheader.service;

import java.sql.Timestamp;
import java.util.Map;

import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;

public interface CuRouteHeaderService extends RouteHeaderService {

    /**
     * Retrieves the finalized date for a particular document type and date range.
     * 
     * @param documentTypeName The document type of the documents to include in the results.
     * @param startDate The start of the date range for finalization dates to return, inclusive.
     * @param endDate The end of the date range for finalization dates to return, inclusive.
     * @return A Map from docIds to finalized dates, or an empty Map if no results were found.
     * @throws IllegalArgumentException if any arguments are null or blank, or if startDate is after endDate.
     */
    Map<String, Timestamp> getFinalizedDatesForDocumentType(String documentTypeName, Timestamp startDate,
            Timestamp endDate);

}
