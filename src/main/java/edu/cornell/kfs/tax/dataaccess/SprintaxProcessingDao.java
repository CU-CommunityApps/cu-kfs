package edu.cornell.kfs.tax.dataaccess;

import java.util.List;

public interface SprintaxProcessingDao {

    /**
     * Performs the tax processing for the given time period and tax type.
     * 
     * <p>The general processing steps are as follows:</p>
     * 
     * <ol>
     *   <li>Any existing transaction detail rows for the given report year and tax type will be deleted.</li>
     *   <li>New transaction detail rows will be created from KFS source data based on the given arguments.</li>
     *   <li>New output files will be created using data from the new transaction rows.</li>
     *   <li>The transaction row contents will be printed to a separate created file.</li>
     *   <li>Various summary statistics will be logged accordingly.</p>
     * </ol>
     * 
     * @param reportYear The tax year to do the reporting for.
     * @param startDate The start date to use when limiting tax source data retrieval; should be in the same year as reportYear.
     * @param endDate The end date to use when limiting tax source data retrieval; should be in the same year as reportYear.
     * @param processingStartDate The date-time when the current tax processing execution started.
     */
    void doSprintaxProcessing(int reportYear, java.sql.Date startDate, java.sql.Date endDate, java.util.Date processingStartDate);

    /**
     * Helper method that takes a list of document IDs and returns a List containing only the ones
     * representing Foreign Draft or Wire Transfer DVs or PRNCs.
     * 
     * @param documentIds The list of document IDs to filter; cannot be null.
     * @param helperObject An implementation-specific object containing data for building any needed queries; cannot be null.
     * @param docType doc type for which we get foreign wire and transfers; could be DV or PRNC.
     * @return A new List containing only the Foreign Draft and Wire Transfer DV IDs or PRNC IDs.
     */
    List<String> findForeignDraftsAndWireTransfers(List<String> documentIds, Object helperObject, String docType);
}
