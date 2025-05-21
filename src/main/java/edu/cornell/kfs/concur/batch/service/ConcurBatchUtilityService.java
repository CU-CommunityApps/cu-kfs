package edu.cornell.kfs.concur.batch.service;

import java.sql.Date;

import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.exception.FileStorageException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;

public interface ConcurBatchUtilityService {
    
    /**
     * Builds the fully qualified name of the Concur Cash Advance PDP XML data file that will
     * be imported during normal KFS PDP import processing.
     *
     * @param paymentImportDirectory File system location where standard PDP processing expects to find its XML files to load.
     * @param pdpInputfileName Name and extension of the data file received from Concur.
     * @return fullyQualifiedPdpCashAdvanceOutputFileName
     */
    String buildFullyQualifiedPdpCashAdvanceOutputFileName(String paymentImportDirectory, String pdpInputfileName);

    /**
     * Uses the fullyQualifiedFileName parameter to create a corresponding
     * .done file at the same location with the same file name.
     *
     * @param fullyQualifiedFileName
     */
    void createDoneFileFor(String fullyQualifiedFileName) throws FileStorageException;
    
    /**
     * Creates the Concur PDP XML file specified by fullyQualifiedPdpFileName
     * from the data in pdpFeedFileBaseEntry. Log file entries are made for
     * success or failure of this method's execution.
     *
     * @param pdpFeedFileBaseEntry
     * @param fullyQualifiedPdpFileName
     * @return true when Concur XML was successfully created and perform a Log.info;
     *         false when Concur XML file was not created and performs a Log.error.
     */
    boolean createPdpFeedFile(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, String fullyQualifiedPdpFileName);
    

    /**
     * Date to be formatted into MM/dd/yyyy format.
     *
     * @param date
     * @return String in MM/dd/yyyy representation for the date passed in.
     */
    String formatDate_MMddyyyy(Date date);
    
    /**
     * Uses the input parameters to build, format, and size the PDP payee name
     * that will be used in the PDP feed file being created from Concur data.
     *
     * @param lastName
     * @param firstName
     * @param middleInitial
     * @return Properly formated PDP feed file payee name
     */
    String formatPdpPayeeName(String lastName, String firstName, String middleInitial);
    
    /**
     * Method concatenates the concurDocumentId to the documentTypeCode and
     * truncates that result to the number of characters specified by
     * SOURCE_DOCUMENT_NUMBER_FIELD_SIZE.
     *
     * @param documentTypeCode The four character Concur document type setup in KFS.
     * @param concurDocumentId This value will be the RequestId for request extract files and the ReportId for SAE files.
     * @return Formatted string to use for Concur PDP source doc number or Collector financial document number.
     */
    String formatSourceDocumentNumber(String documentTypeCode, String concurDocumentId);
    
    /**
     * Use the KFS ParameterService to lookup the KFS system parameter specified
     * in the KFS Concur name space by parameterName.
     *
     * @param parameterName KFS Concur name space parameter to lookup
     * @return value associated to parameterName in KFS Concur name space
     */
    String getConcurParameterValue(String parameterName);
    
    /**
     * Use the KFS ParameterService to lookup the KFS system parameter specified
     * in the KFS Concur name space by parameterName.
     * @param parameterName
     * @return
     */
    boolean getConcurParameterBooleanValue(String parameterName);
    
    /**
     * Sets the value of the given Concur-namespaced parameter.
     * 
     * @param parameterName The name of the Concur-namespaced parameter to update
     * @param parameterValue The new value to set
     */
    void setConcurParameterValue(String parameterName, String parameterValue);
    
    /**
     * Parses a physical file on the file system specified by fullyQualifiedFileName
     * into the Java object associated to the batchInputFileType by the flatFileSpecification.
     * This configuration needs to be setup in Spring for the business object
     * that will hold this parsed data. The returned data Object can be cast
     * to the business object in that defining.
     *
     * @param fullyQualifiedFileName Fully qualified file system location and file name to be loaded.
     * @param batchInputFileType Type of file defined in Spring that the FlatFileParser should attempt to read and load.
     * @return Data object associated with the batchInputFileType that this returned object can be cast to.
     */
    Object loadFile(String fullyQualifiedFileName, BatchInputFileType batchInputFileType);
    
    /**
     * Removes the .done file at the location and with the name specified by the input parameter.
     * @param fullyQualifiedFileName
     */
    void removeDoneFileFor(String fullyQualifiedFileName) throws FileStorageException;
    
    /**
     * Determines whether the given SAE detail line represents a corporate card transaction
     * that was used to pay a personal expense. Such transactions should not be reimbursed
     * to the traveler.
     * 
     * @param line The SAE line object to examine.
     * @return True if the SAE line is a corporate card charge and is flagged as a personal expense, false otherwise.
     */
    boolean lineRepresentsPersonalExpenseChargedToCorporateCard(ConcurStandardAccountingExtractDetailLine line);
    
    /**
     * Determines whether the given SAE detail line represents a corporate-card-paid
     * personal expense that was returned/credited to the user. Such transactions should be deducted
     * from corporate-card-paid personal amounts that the user owes to the university.
     * 
     * @param line The SAE line object to examine.
     * @return True if the SAE line is a corporate-card-paid personal debit with the university as the payer and the user as the payee, false otherwise.
     */
    boolean lineRepresentsReturnOfCorporateCardPersonalExpenseToUser(ConcurStandardAccountingExtractDetailLine line);
    
    /**
     * Determines whether the given SAE detail line represents a corporate-card-paid
     * personal expense that was returned/credited to the university. Such transactions should be deducted
     * from corporate-card-paid personal amounts that the university owes to the credit card company.
     * 
     * @param line The SAE line object to examine.
     * @return True if the SAE line is a corporate-card-paid personal credit with the corp card as the payer and the university as the payee, false otherwise.
     */
    boolean lineRepresentsReturnOfCorporateCardPersonalExpenseToUniversity(ConcurStandardAccountingExtractDetailLine line);
    
    /**
     * Returns the contents of a file as a String.
     * @deprecated use LoadFileUtils.safelyLoadFileString()
     * @param fileName
     * @return
     */
    @Deprecated
    String getFileContents(String fileName);

    /**
     * Determines whether the given traveler/employee status is valid for use
     * when creating KFS PDP payment information under the Employee type.
     * 
     * @param status The Concur traveler/employee status to examine.
     * @return True if the status value is a recognized one for KFS PDP employee payment processing, false otherwise.
     */
    boolean isValidTravelerStatusForProcessingAsPDPEmployeeType(String status);

    /**
     * Returns true when KFS System parameter CONCUR_PROCESS_CASH_ADVANCES_FROM_SAE_DATA_IND is set to Y;
     * otherwise returns false.
     * @return
     */
    boolean shouldProcessRequestedCashAdvancesFromSaeData();

}
