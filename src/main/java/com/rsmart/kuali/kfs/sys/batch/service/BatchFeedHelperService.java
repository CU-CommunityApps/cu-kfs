/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.sys.batch.service;

import java.util.List;

import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.report.ReportInfo;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.MessageMap;

import com.rsmart.kuali.kfs.sys.businessobject.BatchFeedStatusBase;

/**
 * Provides general batch helper methods for jobs loading files and creating documents (PO and DV)
 */
public interface BatchFeedHelperService {

    /**
     * Gets the file contents and calls batch file service to parse the XML contents and populate the Object
     * 
     * @param batchInputFileType batch input file type for document
     * @param incomingFileName file name of file to load
     * @param batchStatus BatchFeedStatusBase that will hold any parsing exception messages
     * @return Object loaded from file contents
     */
    public Object parseBatchFile(BatchInputFileType batchInputFileType, String incomingFileName, BatchFeedStatusBase batchStatus);

    /**
     * Generates a report for the document batch to the file system
     * 
     * @param batchStatus populated with information for audit report
     */
    public void generateAuditReport(ReportInfo reportInfo, BatchFeedStatusBase batchStatus);

    /**
     * Generate audit message for batch report. If errors are contained in the error map, they are printed out in the audit message
     * with the error prefix, else the message given from the successful key is written to message
     * 
     * @param successfulErrorKey resource key for message to use if no errors
     * @param documentNumber doc number for success message
     * @param errorMap ErrorMap of errors to write out
     * @return String message
     */
    public String getAuditMessage(String successfulErrorKey, String documentNumber, MessageMap errorMap);

    /**
     * Gets the attributes defined in the DD for the bo class, checks if the fields is marked force uppercase, and if so uppercases
     * the value (given it is not null)
     * 
     * @param entryName name of the entry in the DD to pick up attribute definitions
     * @param businessObject business object to uppercase attributes for
     */
    public void performForceUppercase(String entryName, Object businessObject);

    /**
     * Loads attachments from attachment directory and creates a note attachment for each one on the document
     * 
     * @param document document to populate
     * @param attachments List of Attachment objects to load
     * @param attachmentsPath directory where attachments are located
     * @param attachmentType type of attachment to create
     * @param errorMap ErrorMap for adding encountered errors
     */
    public void loadDocumentAttachments(Document document, List<Attachment> attachments, String attachmentsPath, String attachmentType, MessageMap errorMap);

    /**
     * Attempts to retrieve a reference on the purchase order document for existence and active indicator validation
     * 
     * @param businessObject object with reference to check
     * @param referenceName name of reference to check
     * @param propertyName name of property to associate errors with
     * @param errorMap Map for adding errors
     */
    public void performExistenceAndActiveValidation(PersistableBusinessObject businessObject, String referenceName, String propertyName, MessageMap errorMap);

    /**
     * Adds an error to the given map for a required failure
     * 
     * @param businessObject object with property with error
     * @param propertyName name of property that failed
     * @param errorMap Map that error will be added to
     */
    public void addRequiredError(PersistableBusinessObject businessObject, String propertyName, MessageMap errorMap);

    /**
     * Adds an error to the given map for an existence failure
     * 
     * @param propertyName name of property that failed
     * @param propertyValue property value that is invalid
     * @param errorMap Map that error will be added to
     */
    public void addExistenceError(String propertyName, String propertyValue, MessageMap errorMap);

    /**
     * Adds an error to the given map for an active failure
     * 
     * @param propertyName name of property that failed
     * @param propertyValue property value that is inactive
     * @param errorMap Map that error will be added to
     */
    public void addInactiveError(String propertyName, String propertyValue, MessageMap errorMap);

    /**
     * Clears out the associated .done file for the processed data file
     * 
     * @param dataFileName the name of date file with done file to remove
     */
    public void removeDoneFile(String dataFileName);

    /**
     * Helper method to return the Person object for the system user
     * 
     * @return system user Person object
     */
    public Person getSystemUser();
}
