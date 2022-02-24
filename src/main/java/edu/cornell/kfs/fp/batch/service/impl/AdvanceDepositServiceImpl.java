package edu.cornell.kfs.fp.batch.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectType;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.web.format.FormatException;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.businessobject.AdvanceDepositDetail;
import org.kuali.kfs.fp.document.AdvanceDepositDocument;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResult;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResults;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.docsearch.service.DocumentSearchService;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.FlatFileInformation;
import org.kuali.kfs.sys.batch.FlatFileTransactionInformation;
import org.kuali.kfs.sys.batch.PhysicalFlatFileInformation;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.sys.util.GlobalVariablesUtils;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.GenerateAdvanceDepositDocumentsStep;
import edu.cornell.kfs.fp.batch.LoadAchIncomeFileStep;
import edu.cornell.kfs.fp.batch.service.AdvanceDepositService;
import edu.cornell.kfs.fp.businessobject.AchIncomeFile;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileGroup;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransaction;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionDateTime;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionNote;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionOpenItemReference;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionPayerOrPayeeName;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionReference;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionSet;
import edu.cornell.kfs.fp.businessobject.AchIncomeNote;
import edu.cornell.kfs.fp.businessobject.AchIncomeTransaction;
import edu.cornell.kfs.fp.businessobject.IncomingWireAchMapping;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.LoadFileUtils;

/**
 * Portions Modified 04/2016 and Copyright Cornell University
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 Copyright Indiana University
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class AdvanceDepositServiceImpl implements AdvanceDepositService {
	private static final Logger LOG = LogManager.getLogger(AdvanceDepositServiceImpl.class);
    private static final String ACH_TRANSACTION_SEQUENCE_NUMBER = "sequenceNumber";
    private static final int MAX_NOTE_SIZE = 800;
    private static final int MIN_NOTE_SIZE = 80;

    protected AttachmentService attachmentService;
    protected BankService bankService;
    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType batchInputFileType;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    protected DocumentService documentService;
    protected EmailService emailService;
    protected NoteService noteService;
    protected ParameterService parameterService;
    protected PersonService personService;

    protected String attachmentsDirectory;
    private int achTotalPostedTransactions;
    private int achTotalSkippedTransactions;
    private int wiredTotalPostedTransactions;
    private int wiredTotalSkippedTransactions;
    private KualiDecimal achTotalPostedTransactionAmount;
    private KualiDecimal achTotalSkippedTransactionAmount;
    private KualiDecimal wiredTotalPostedTransactionAmount;
    private KualiDecimal wiredTotalSkippedTransactionAmount;

    /**
     * @see AdvanceDepositService#createDocuments()
     */
    public void createDocuments() {
        List<AchIncomeTransaction> transactions = (List<AchIncomeTransaction>) businessObjectService.findMatchingOrderBy(AchIncomeTransaction.class, new HashMap<String, Object>(), ACH_TRANSACTION_SEQUENCE_NUMBER, true);

        for (AchIncomeTransaction transaction : transactions) {
            AdvanceDepositDocument document = createAdvanceDepositDocument(transaction);
            saveDocument(document);
            createNotes(transaction, document);
        }
    }

    private void saveDocument(AdvanceDepositDocument document) {
        try {
            documentService.saveDocument(document);
            if (LOG.isInfoEnabled()) {
                LOG.info("Saved Advance Deposit document: " + document.getDocumentNumber());
            }
        } catch (Exception e) {
            LOG.error("Error persisting document # " + document.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
            throw new RuntimeException("Error persisting document # " + document.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
        }
    }

    /**
     * @see AdvanceDepositService#routeAdvanceDepositDocuments()
     */
    public boolean routeAdvanceDepositDocuments() {
        List<String> documentIdList;

        try {
            documentIdList = retrieveAdvanceDepositDocumentsToRoute(KewApiConstants.ROUTE_HEADER_SAVED_CD);
        } catch (WorkflowException | RemoteException e) {
            LOG.error("Error retrieving advance deposit documents for routing: " + e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Advance deposit to Route: " + documentIdList);
        }

        for (String documentId : documentIdList) {
            try {
                AdvanceDepositDocument advanceDocument = (AdvanceDepositDocument) documentService.getByDocumentHeaderId(documentId);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Routing advance deposit document # " + documentId + ".");
                }
                documentService.routeDocument(advanceDocument, "document routed by achIncome batch job", null);
            }
            catch (Exception e) {
                LOG.error("Error routing document # " + documentId + " due to exception: " + e.getMessage());
                logException(e);
            }
        }

        return true;
    }

    private void logException(Exception e) {
        if (e instanceof ValidationException) {
            List<String> errors = GlobalVariablesUtils.extractGlobalVariableErrors();
            if (ObjectUtils.isNotNull(errors) && !errors.isEmpty()) {
                for (String errorMessage : errors) {
                    LOG.error(errorMessage);
                }
            }
        } else {
            LOG.error(e.getStackTrace());
        }
    }


	/**
     * Returns a list of all initiated but not yet routed advance deposit documents, using the WorkflowDocumentService.
     *
     * @return a list of advance deposit documents to route
     */
    protected List<String> retrieveAdvanceDepositDocumentsToRoute(String statusCode) throws WorkflowException, RemoteException {
        List<String> documentIds = new ArrayList<String>();

        List<DocumentStatus> routeStatuses = new ArrayList<DocumentStatus>();
        routeStatuses.add(DocumentStatus.fromCode(statusCode));

        Person systemUser = getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        String principalName = systemUser.getPrincipalName();

        DocumentSearchCriteria.Builder criteria = DocumentSearchCriteria.Builder.create();
        criteria.setDocumentTypeName(KFSConstants.FinancialDocumentTypeCodes.ADVANCE_DEPOSIT);
        criteria.setDocumentStatuses(routeStatuses);
        criteria.setInitiatorPrincipalName(principalName);

        DocumentSearchResults results = getDocumentSearchService().lookupDocuments(systemUser.getPrincipalId(), criteria.build(), false);

        for (DocumentSearchResult resultRow : results.getSearchResults()) {
            DocumentRouteHeaderValue document = resultRow.getDocument();
            if (ObjectUtils.isNotNull(document)) {
                documentIds.add(document.getDocumentId());
            }
        }

        return documentIds;
    }

    /**
     * Creates a AdvanceDepositDocument from the List of transactions given.
     *
     * @param transaction List of CashReceiptDocument objects to be used for creating the document.
     * @return A AdvanceDepositDocument populated with the transactions provided.
     */
    protected AdvanceDepositDocument createAdvanceDepositDocument(AchIncomeTransaction transaction) {
        AdvanceDepositDocument advanceDepositDocument;

        try {
            String detailReferenceNumber = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, CuFPParameterConstants.AdvanceDepositDocument.DETAIL_REFERENCE_NUMBER);
            String detailDescription = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, CuFPParameterConstants.AdvanceDepositDocument.DETAIL_DESCRIPTION);
            String bankCode = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, CuFPParameterConstants.AdvanceDepositDocument.BANK_CODE);
            String documentDescription = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, CuFPParameterConstants.AdvanceDepositDocument.DOCUMENT_DESCRIPTION);

            advanceDepositDocument = (AdvanceDepositDocument) documentService.getNewDocument(KFSConstants.FinancialDocumentTypeCodes.ADVANCE_DEPOSIT);
            advanceDepositDocument.getDocumentHeader().setDocumentDescription(documentDescription);
            advanceDepositDocument.setCampusLocationCode(CuFPConstants.ADVANCE_DEPOSIT_DEFAULT_CAMPUS_CODE);
            advanceDepositDocument.setDepositDate(dateTimeService.convertToSqlDate(transaction.getBankTimestamp()));

            createSourceAccountingLine(transaction, advanceDepositDocument);

            AdvanceDepositDetail advanceDepositDetail = new AdvanceDepositDetail();
            advanceDepositDetail.setDocumentNumber(advanceDepositDocument.getDocumentNumber());
            advanceDepositDetail.setFinancialDocumentAdvanceDepositDate(dateTimeService.convertToSqlDate(transaction.getLoadTimestamp()));
            advanceDepositDetail.setFinancialDocumentAdvanceDepositAmount(transaction.getTransactionAmount());
            advanceDepositDetail.setFinancialDocumentAdvanceDepositReferenceNumber(detailReferenceNumber);
            advanceDepositDetail.setFinancialDocumentAdvanceDepositDescription(detailDescription);
            advanceDepositDetail.setFinancialDocumentBankCode(bankCode);
            Bank bank = bankService.getByPrimaryId(bankCode);
            advanceDepositDetail.setBank(bank);

            advanceDepositDocument.addAdvanceDeposit(advanceDepositDetail);
        } catch (ParseException e) {
            LOG.error("Error creating advance deposit documents: " + e.getMessage(), e);
            throw new RuntimeException("Error creating advance deposit documents: " + e.getMessage(), e);
        }

        return advanceDepositDocument;
    }

    private void createNotes(AchIncomeTransaction transaction, AdvanceDepositDocument document) {
        String fileName = CuFPConstants.ADVANCE_DEPOSIT_NOTE_FILE_PREFIX + document.getDocumentNumber() + "_" + new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US).format(dateTimeService.getCurrentDate()) + ".txt";
        StringBuilder notes = new StringBuilder();

        for (AchIncomeNote achIncomeNote : transaction.getNotes()) {
            notes.append(achIncomeNote.getNoteText());
            notes.append("\n");
        }

        byte[] notesAttachmentBytes = notes.toString().getBytes();
        String attachmentType = null;

        try {
            Attachment attachment = attachmentService.createAttachment(document.getDocumentHeader(), fileName, "text", notesAttachmentBytes.length, new ByteArrayInputStream(notesAttachmentBytes), attachmentType);
            Note note = documentService.createNoteFromDocument(document, "Attachment with transaction notes created by ach/incoming wire batch job.");
            note.setAttachment(attachment);
            attachment.setNote(note);
            noteService.save(note);
        } catch (IOException e) {
            LOG.error("Error while adding notes to advance deposit documents: " + e.getMessage(), e);
            throw new RuntimeException("Error while adding notes to the document " + document.getDocumentNumber());
        }
    }

    protected void createSourceAccountingLine(AchIncomeTransaction transaction, AdvanceDepositDocument advanceDepositDocument) {
        String chart = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, CuFPParameterConstants.AdvanceDepositDocument.CHART);
        String objectCode = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, CuFPParameterConstants.AdvanceDepositDocument.OBJECT_CODE);
        String account = parameterService.getParameterValueAsString(GenerateAdvanceDepositDocumentsStep.class, CuFPParameterConstants.AdvanceDepositDocument.ACCOUNT);

        IncomingWireAchMapping matchingIncomingWireAchMapping = null;
        Collection<IncomingWireAchMapping> incomingWireAchMappings = businessObjectService.findAll(IncomingWireAchMapping.class);

        for (IncomingWireAchMapping mapping : incomingWireAchMappings) {
            List<AchIncomeNote> notes = transaction.getNotes();
            if (doNotesMatch(mapping, notes)) {
                matchingIncomingWireAchMapping = mapping;
                break;
            }
        }

        if (ObjectUtils.isNotNull(matchingIncomingWireAchMapping)) {
            chart = matchingIncomingWireAchMapping.getChartOfAccountsCode();
            objectCode = matchingIncomingWireAchMapping.getFinancialObjectCode();
            account = matchingIncomingWireAchMapping.getAccountNumber();
        }

        setupSourceAccountingLine(transaction, advanceDepositDocument, chart, objectCode, account);
    }

    protected boolean doNotesMatch(IncomingWireAchMapping mapping, List<AchIncomeNote> notes) {
        final String shortDescription = mapping.getShortDescription();
        for (AchIncomeNote note : notes) {
            if (note.getNoteText().indexOf(shortDescription) >= 0) {
                return true;
            }
        }
        return false;
    }

    protected void setupSourceAccountingLine(AchIncomeTransaction transaction, AdvanceDepositDocument advanceDepositDocument, String chart, String objectCode, String account) {
        SourceAccountingLine sourceAccountingLine = new SourceAccountingLine();
        sourceAccountingLine.setSequenceNumber(advanceDepositDocument.getNextSourceLineNumber());
        sourceAccountingLine.setChartOfAccountsCode(chart);
        sourceAccountingLine.setPostingYear(getSourceAccountingLinePostingYear(sourceAccountingLine));
        sourceAccountingLine.setFinancialObjectCode(objectCode);
        setSourceAccountingLineAccountNumber(account, sourceAccountingLine);
        sourceAccountingLine.setFinancialDocumentLineDescription(transaction.getPayerName());
        setSourceAccountingLineAmount(transaction, sourceAccountingLine, chart, objectCode);
        sourceAccountingLine.setOverrideCode(AccountingLineOverride.CODE.NONE);
        sourceAccountingLine.setPostingYear(advanceDepositDocument.getPostingYear());
        sourceAccountingLine.setDocumentNumber(advanceDepositDocument.getDocumentNumber());
        List<SourceAccountingLine> sourceLines = new ArrayList<>();
        sourceLines.add(sourceAccountingLine);
        advanceDepositDocument.setSourceAccountingLines(sourceLines);
    }

    protected void setSourceAccountingLineAmount(AchIncomeTransaction transaction, SourceAccountingLine sourceAccountingLine, String chart, String objectCode){
        KualiDecimal amount = transaction.getTransactionAmount();
        String objectTypeCode = getObjectCodeType(chart, objectCode);

        if(CUKFSConstants.BasicAccountingCategory.ASSET.equalsIgnoreCase(objectTypeCode) || CUKFSConstants.BasicAccountingCategory.EXPENSE.equalsIgnoreCase(objectTypeCode)){
            amount = amount.negated();
        }
        sourceAccountingLine.setAmount(amount);

    }

    protected String getObjectCodeType(String chart, String objectCode) {
        Map<String, String> keys = new HashMap<String, String>();
        keys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart);
        keys.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, objectCode);

        ObjectCode objectCodeInfo = businessObjectService.findByPrimaryKey(ObjectCode.class, keys);
        objectCodeInfo.refreshReferenceObject(KFSPropertyConstants.FINANCIAL_OBJECT_TYPE);
        ObjectType objectType = objectCodeInfo.getFinancialObjectType();
        String objectTypeCode = objectType.getBasicAccountingCategoryCode();

        return objectTypeCode;
    }

    /**
     * This is split out so this class can be more easily tested.
     * @param account
     * @param sourceAccountingLine
     */
    protected void setSourceAccountingLineAccountNumber(String account, SourceAccountingLine sourceAccountingLine) {
        sourceAccountingLine.setAccountNumber(account);
    }

    /**
     * This is split out so this class can be more easily tested.
     *
     * @param sourceAccountingLine
     * @return
     */
    protected Integer getSourceAccountingLinePostingYear(SourceAccountingLine sourceAccountingLine) {
        return sourceAccountingLine.getPostingYear();
    }

    /**
     * @see AdvanceDepositService#loadFile()
     */
    public void loadFile() {
        List<PhysicalFlatFileInformation> flatFileInformationList = new ArrayList<>();
        PhysicalFlatFileInformation physicalflatFileInformation;
        List<String> fileNamesToLoad = getListOfFilesToProcess();
        if (LOG.isInfoEnabled()) {
            LOG.info("Found " + fileNamesToLoad.size() + " file(s) to process.");
        }

        for (String inputFileName : fileNamesToLoad) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Beginning processing of filename: " + inputFileName + ".");
                }
                physicalflatFileInformation = new PhysicalFlatFileInformation(inputFileName);
                flatFileInformationList.add(physicalflatFileInformation);

                if (loadFile(inputFileName, physicalflatFileInformation)) {
                    physicalflatFileInformation.addFileInfoMessage("File successfully completed processing.");
                } else {
                    physicalflatFileInformation.addFileErrorMessage("Unable to process file.");
                }
            } catch (RuntimeException e) {
                LOG.error("Caught exception trying to load: " + inputFileName, e);
                throw new RuntimeException("Caught exception trying to load: " + inputFileName, e);
            } finally {
                removeDoneFile(inputFileName);
            }
        }

        sendEmailSummary(flatFileInformationList);
    }

    public boolean loadFile(String fileName, PhysicalFlatFileInformation physicalFlatFileInformation) {
        boolean valid = true;
        byte[] fileByteContent = LoadFileUtils.safelyLoadFileBytes(fileName);

        if (LOG.isInfoEnabled()) {
            LOG.info("Attempting to parse the file ");
        }
        Object parsedObject;

        try {
            parsedObject = batchInputFileService.parse(batchInputFileType, fileByteContent);
        } catch (org.kuali.kfs.sys.exception.ParseException e) {
            LOG.error("Error parsing batch file: " + e.getMessage());
            FlatFileInformation fileInformation = new FlatFileInformation();
            fileInformation.addFileInfoMessage("Unable to process file" + StringUtils.substringAfterLast(fileName, "\\") + "." + e.getMessage());
            physicalFlatFileInformation.getFlatFileInfomationList().add(fileInformation);
            return false;
        }

        if (parsedObject != null) {
            valid = validate(parsedObject);
            copyAllMessage(parsedObject, physicalFlatFileInformation);
            if (valid) {
                loadAchIncomeTransactions(parsedObject);
            }
        }

        return valid;
    }

    public boolean validate(Object parsedFileContents) {
        boolean valid = true;
        List<AchIncomeFile> achIncomeFiles = (ArrayList<AchIncomeFile>) parsedFileContents;
        for (AchIncomeFile achIncomeFile : achIncomeFiles) {
            initializeTransactionCountsAndAmounts();
            String payerMessage = "";

            if (validateTrailerRecord(achIncomeFile)) {
                valid &= validateGroupCount(achIncomeFile);
                valid &= validateLogicFileControlNumbers(achIncomeFile);
            } else {
                valid = false;
            }

            for (AchIncomeFileGroup achIncomeFileGroup : achIncomeFile.getGroups()) {
                if (validateGroupTrailer(achIncomeFileGroup)) {
                    valid &= validateTransactionCount(achIncomeFileGroup);
                    valid &= validateGroupControlNumber(achIncomeFileGroup);
                } else {
                    valid = false;
                }

                validateFunctionalIdentifierCode(achIncomeFile, achIncomeFileGroup);

                for (AchIncomeFileTransactionSet achIncomeFileTransactionSet : achIncomeFileGroup.getTransactionSets()) {
                    if (validateTransactionTrailer(achIncomeFileTransactionSet)) {
                        valid &= validateTransactionSetControlNumbers(achIncomeFileTransactionSet);
                    } else {
                        valid = false;
                    }

                    // loop through the AchTransaction to get the total transaction count and amount
                    // for ACH and wired posted and skipped transactions
                    setAchIncomeTransactionCountsAndAmounts(achIncomeFileTransactionSet.getTransactionGuts());
                    payerMessage += validatePayerName(achIncomeFileTransactionSet.getTransactionGuts());
                }
            }

            valid &= validateTimestamp(achIncomeFile);
            achIncomeFile.setEmailMessageText(getEmailMessageText(achIncomeFile, payerMessage));
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("validate method executing");
        }

        return valid;
    }

    protected void initializeTransactionCountsAndAmounts() {
        achTotalPostedTransactions = 0;
        achTotalSkippedTransactions = 0;
        wiredTotalPostedTransactions = 0;
        wiredTotalSkippedTransactions = 0;
        achTotalPostedTransactionAmount = new KualiDecimal(0.00);
        achTotalSkippedTransactionAmount = new KualiDecimal(0.00);
        wiredTotalPostedTransactionAmount = new KualiDecimal(0.00);
        wiredTotalSkippedTransactionAmount = new KualiDecimal(0.00);
    }

    /**
     * verify there is an ISA and IEA record for each logical file
     *
     * @param achIncomeFile ach income file to validate
     * @return whether the trailer record is valid (i.e. it exists)
     */
    protected boolean validateTrailerRecord(AchIncomeFile achIncomeFile) {
        if (ObjectUtils.isNull(achIncomeFile.getTrailer())) {
            String message = "No logical file trailer found for file header :" + achIncomeFile.getInterchangeControlNumber();
            LOG.error("Error while validating the Ach Income file" + message);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
            return false;
        }

        return true;
    }

    protected boolean validateGroupCount(AchIncomeFile achIncomeFile) {
        int totalGroups = achIncomeFile.getTrailer().getTotalGroups();
        if (totalGroups != achIncomeFile.getGroups().size()) {
            String message = "The group count on the file trailer," + totalGroups + ","
                    + "does not match the number of groups," + achIncomeFile.getGroups().size()
                    + ",in the file:" + achIncomeFile.getInterchangeControlNumber();
            LOG.error("Error while validating the Ach Income file" + message);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
            return false;
        }

        return true;
    }

    protected boolean validateLogicFileControlNumbers(AchIncomeFile achIncomeFile) {
        if (!achIncomeFile.getInterchangeControlNumber().equals(achIncomeFile.getTrailer().getInterchangeControlNumber())) {
            String message = "Cannot match logical file header to file trailer for file: ISA Control Number [" + achIncomeFile.getInterchangeControlNumber() + "] "
                    + "IEA Control Number [" + achIncomeFile.getTrailer().getInterchangeControlNumber() + "]";
            LOG.error("Error while validating the Ach Income file" + message);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
            return false;
        }

        return true;
    }

    /**
     * verify there is an GS and GE record for each logical file
     *
     * @param achIncomeFileGroup ach income file group to validate
     * @return true if valid, false if otherwise
     */
    protected boolean validateGroupTrailer(AchIncomeFileGroup achIncomeFileGroup) {
        if (ObjectUtils.isNull(achIncomeFileGroup.getGroupTrailer())) {
            String message = "No group trailer found for group: [" + achIncomeFileGroup.getGroupControlNumber() + "]";
            LOG.error("Error while validating the Ach Income file" + message);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
            return false;
        }

        return true;
    }

    /**
     * verify the group trailer transactionSets count matches the numbers of transactions
     *
     * @param achIncomeFileGroup ach income file group to validate
     * @return true if valid, false if otherwise
     */
    protected boolean validateTransactionCount(AchIncomeFileGroup achIncomeFileGroup) {
        int totalTransactionSets = achIncomeFileGroup.getGroupTrailer().getTotalTransactionSets();
        if (totalTransactionSets != achIncomeFileGroup.getTransactionSets().size()) {
            String message = "The transaction count on the group trailer, " + totalTransactionSets
                    + ", does not match the number of transactions, " + achIncomeFileGroup.getTransactionSets().size()
                    + ", in the group: [" + achIncomeFileGroup.getGroupControlNumber() + "]";
            LOG.error("Error while validating the Ach Income file" + message);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
            return false;
        }

        return true;
    }

    /**
     * verify that group control number on GS and GE match
     *
     * @param achIncomeFileGroup ach income file group to validate
     * @return true if valid, false if otherwise
     */
    protected boolean validateGroupControlNumber(AchIncomeFileGroup achIncomeFileGroup) {
        if (!achIncomeFileGroup.getGroupControlNumber().equals(achIncomeFileGroup.getGroupTrailer().getGroupControlNumber())) {
            String message = "Cannot match group header to group trailer for group: GS Control Number [" + achIncomeFileGroup.getGroupControlNumber() + "] "
                    + "IEA Control Number [" + achIncomeFileGroup.getGroupTrailer().getGroupControlNumber() + "]";
            LOG.error("Error while validating the Ach Income file" + message);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
            return false;
        }

        return true;
    }

    /**
     * verify RA is on the functional identifier code on the GS record
     *
     * @param achIncomeFile ach income file to validate
     * @param achIncomeFileGroup ach income file group to validate
     */
    protected void validateFunctionalIdentifierCode(AchIncomeFile achIncomeFile, AchIncomeFileGroup achIncomeFileGroup) {
        if (!StringUtils.equals(CuFPConstants.AchIncomeFileGroup.GROUP_FUNCTIONAL_IDENTIFIER_CD_RA, achIncomeFileGroup.getGroupFunctionIdentifierCode())) {
            String message = "The Functional Identifier Code is not " + CuFPConstants.AchIncomeFileGroup.GROUP_FUNCTIONAL_IDENTIFIER_CD_RA
                    + " for group: " + achIncomeFileGroup.getGroupControlNumber() + "-" + achIncomeFileGroup.getGroupFunctionIdentifierCode();
            achIncomeFile.getFlatFileTransactionInformation().addWarnMessage(message);
            GlobalVariables.getMessageMap().putWarning(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
        }
    }

    /**
     * verify there is an ST and SE record for each logical file
     *
     * @param achIncomeFileTransactionSet ach income file transaction set to validate
     * @return true if valid, false if otherwise
     */
    protected boolean validateTransactionTrailer(AchIncomeFileTransactionSet achIncomeFileTransactionSet) {
        if (ObjectUtils.isNull(achIncomeFileTransactionSet.getTransactionSetTrailer())) {
            String message = "No transaction trailer found for transaction: [" + achIncomeFileTransactionSet.getTransactionSetControlNumber() + "]";
            LOG.error("Error while validating the Ach Income file" + message);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
            return false;
        }

        return true;
    }

    /**
     * verify that the transaction set control number on SE and ST match
     *
     * @param achIncomeFileTransactionSet ach income file transaction set to validate
     * @return true if valid, false if otherwise
     */
    protected boolean validateTransactionSetControlNumbers(AchIncomeFileTransactionSet achIncomeFileTransactionSet) {
        if (!achIncomeFileTransactionSet.getTransactionSetControlNumber().
                equals(achIncomeFileTransactionSet.getTransactionSetTrailer().getTransactionSetControlNumber())) {
            String message = "Cannot match transaction header to transaction trailer for transaction set: ST Control Number: [" + achIncomeFileTransactionSet.getTransactionSetControlNumber() + "] "
                    + "SE Control Number: [" + achIncomeFileTransactionSet.getTransactionSetTrailer().getTransactionSetControlNumber() + "]";
            LOG.error("Error while validating the Ach Income file" + message);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
            return false;
        }

        return true;
    }

    private String validatePayerName(List<AchIncomeFileTransaction> achIncomeFileTransaction) {
        StringBuilder payerMessage = new StringBuilder();

        for (AchIncomeFileTransaction transaction : achIncomeFileTransaction) {
            if (StringUtils.equals(CuFPConstants.AchIncomeFileTransaction.PAYER_NOT_IDENTIFIED, transaction.getPayerName())) {
                payerMessage.append("Payer Name was not found for transaction amount $" + getFormattedAmount("##,##,##0.00", transaction.getTransactionAmount()));
                payerMessage.append(" [Date: ");
                payerMessage.append(transaction.getEffectiveDate());
                payerMessage.append("]");
                payerMessage.append("\n");
            }
        }

        return payerMessage.toString();
    }

    protected boolean validateTimestamp(AchIncomeFile achIncomeFile) {
        try {
            getFormattedTimestamp(achIncomeFile, "fileDate/Time");
        } catch (FormatException e) {
            LOG.error("Error while validating the ACH Income file: " + e.getMessage());
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, e.getMessage());
            return false;
        }

        return true;
    }

    public void sendEmailSummary(List<PhysicalFlatFileInformation> flatFileInformationList) {
        for (PhysicalFlatFileInformation physicalFlatFileInformation : flatFileInformationList) {
            List<FlatFileInformation> fileInformations = physicalFlatFileInformation.getFlatFileInfomationList();
            for (FlatFileInformation fileInformation : fileInformations) {
                sendEmail(fileInformation);
            }
        }
    }

    private void sendEmail(FlatFileInformation fileInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail() starting");
        }
        BodyMailMessage message = new BodyMailMessage();

        String returnAddress = parameterService.getParameterValueAsString(LoadAchIncomeFileStep.class, CuFPParameterConstants.AchIncome.ACH_INCOME_SUMMARY_FROM_EMAIL_ADDRESS);
        if (StringUtils.isEmpty(returnAddress)) {
            returnAddress = emailService.getDefaultFromAddress();
        }
        message.setFromAddress(returnAddress);
        String subject = parameterService.getParameterValueAsString(LoadAchIncomeFileStep.class, CuFPParameterConstants.AchIncome.ACH_INCOME_SUMMARY_EMAIL_SUBJECT);

        message.setSubject(subject);
        List<String> toAddressList = new ArrayList<>(parameterService.getParameterValuesAsString(LoadAchIncomeFileStep.class, CuFPParameterConstants.AchIncome.ACH_INCOME_SUMMARY_TO_EMAIL_ADDRESSES));
        message.getToAddresses().addAll(toAddressList);
        String body = composeAchIncomeSummaryEmailBody(fileInformation);
        message.setMessage(body);

        emailService.sendMessage(message, false);
    }

    private String composeAchIncomeSummaryEmailBody(FlatFileInformation flatFileInformation) {
        StringBuilder body = new StringBuilder();

        for (String[] resultMessage : flatFileInformation.getMessages()) {
            body.append(resultMessage[1]);
            body.append("\n");
        }

        for (Object object : flatFileInformation.getFlatFileIdentifierToTransactionInfomationMap().values()) {
            for (String[] message : ((FlatFileTransactionInformation) object).getMessages()) {
                body.append(message[1]);
                body.append("\n");
            }
        }

        return body.toString();
    }

    protected List<String> getListOfFilesToProcess() {
        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);

        if (fileNamesToLoad == null) {
            LOG.error("BatchInputFileService.listInputFileNamesWithDoneFile(" +
                    batchInputFileType.getFileTypeIdentifier() + ") returned NULL which should never happen.");
            throw new RuntimeException("BatchInputFileService.listInputFileNamesWithDoneFile(" +
                    batchInputFileType.getFileTypeIdentifier() + ") returned NULL which should never happen.");
        }

        for (String inputFileName : fileNamesToLoad) {
            if (StringUtils.isBlank(inputFileName)) {
                LOG.error("One of the file names returned as ready to process [" + inputFileName +
                        "] was blank.  This should not happen, so throwing an error to investigate.");
                throw new RuntimeException("One of the file names returned as ready to process [" + inputFileName +
                        "] was blank.  This should not happen, so throwing an error to investigate.");
            }
        }

        return fileNamesToLoad;
    }

    /**
     * Clears out associated .done files for the processed data files.
     * @param dataFileName
     */
    protected void removeDoneFile(String dataFileName) {
        File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
        if (doneFile.exists()) {
            doneFile.delete();
        }
    }

    private void loadAchIncomeTransactions(Object parsedObject) {
        List<AchIncomeTransaction> achIncomeTransactions = new ArrayList();
        List<AchIncomeFile> achIncomeFiles = (List<AchIncomeFile>) parsedObject;
        for (AchIncomeFile achIncomeFile : achIncomeFiles) {
            setAchTransactions(achIncomeFile, achIncomeTransactions);
        }

        saveTransaction(achIncomeTransactions);
    }

    private void saveTransaction(List<AchIncomeTransaction>  achIncomeFileTransactions) {
        businessObjectService.save(achIncomeFileTransactions);
    }

    /**
     * Calls businessObjectService to remove all the ach Income transaction rows from the transaction load table.
     */
    public void cleanTransactionsTable() {
        businessObjectService.deleteMatching(AchIncomeTransaction.class, new HashMap<String, Object>());
    }

    protected void setAchTransactions(AchIncomeFile achIncomeFile, List achIncomeTransactions) {
        Timestamp bankTimestamp = getFormattedTimestamp(achIncomeFile, "fileDate/time");

        for (AchIncomeFileGroup achIncomeFileGroup : achIncomeFile.getGroups()) {
            for (AchIncomeFileTransactionSet achIncomeFileTransactionSet : achIncomeFileGroup.getTransactionSets()) {
                for (AchIncomeFileTransaction achIncomeFileTransaction : achIncomeFileTransactionSet.getTransactionGuts()) {
                    if (KFSConstants.GL_CREDIT_CODE.equals(achIncomeFileTransaction.getCreditDebitIndicator())) {
                        AchIncomeTransaction achIncomeTransaction = new AchIncomeTransaction();
                        achIncomeTransaction.setPaymentMethodCode(achIncomeFileTransaction.getPaymentMethodCode());
                        achIncomeTransaction.setEffectiveDate(achIncomeFileTransaction.getEffectiveDate());
                        achIncomeTransaction.setLoadTimestamp(dateTimeService.getCurrentTimestamp());
                        achIncomeTransaction.setBankTimestamp(bankTimestamp);
                        achIncomeTransaction.setTransactionAmount(achIncomeFileTransaction.getTransactionAmount());
                        achIncomeTransaction.setPayerName(truncatePayerNameIfNecessary(achIncomeFileTransaction));
                        achIncomeTransaction.setNotes(createNotes(achIncomeFileTransaction));
                        achIncomeTransactions.add(achIncomeTransaction);
                    }
                }
            }
        }
    }

    protected String truncatePayerNameIfNecessary(AchIncomeFileTransaction achIncomeFileTransaction) {
        String payerName = achIncomeFileTransaction.getPayerName();

        if (payerName.length() > CuFPConstants.AchIncomeFileTransactionPayerOrPayeeName.ACH_TRN_PAYER_NM_DB_SIZE) {
            payerName = payerName.substring(0, CuFPConstants.AchIncomeFileTransactionPayerOrPayeeName.ACH_TRN_PAYER_NM_DB_SIZE);
        }

        return payerName;
    }

    private List<AchIncomeNote> createNotes(AchIncomeFileTransaction achIncomeFileTransaction) {
        StringBuilder notes = addPaymentInformationToNotes(achIncomeFileTransaction);
        List<String> achNotes = addRMRAndNTELinesToNotes(achIncomeFileTransaction, notes);

        List<AchIncomeNote> achIncomeNotes = new ArrayList<>();
        int noteLineNumber = 1;
        for (String noteText : achNotes) {
            AchIncomeNote achIncomeNote = new AchIncomeNote();
            achIncomeNote.setNoteLineNumber(noteLineNumber);
            achIncomeNote.setNoteText(noteText);
            achIncomeNotes.add(achIncomeNote);
            noteLineNumber++;
        }

        return achIncomeNotes;
    }

    private StringBuilder addPaymentInformationToNotes(AchIncomeFileTransaction achIncomeFileTransaction) {
        StringBuilder notes = new StringBuilder();

        if (CuFPConstants.AchIncomeFileTransaction.TRANS_PAYMENT_METHOD_ACH.equals(achIncomeFileTransaction.getPaymentMethodCode())) {
            notes.append("ACH PAYMENT INFORMATION: ");
        }

        if (CuFPConstants.AchIncomeFileTransaction.TRANS_PAYMENT_METHOD_FWT.equals(achIncomeFileTransaction.getPaymentMethodCode())) {
            notes.append("WIRE PAYMENT INFORMATION: ");
        }
        notes.append("\n");
        notes.append("CREDIT: ");
        notes.append("$");
        notes.append(getFormattedAmount("##,##,###.00", achIncomeFileTransaction.getTransactionAmount()));
        notes.append(" ");
        notes.append(achIncomeFileTransaction.getCreditDebitIndicator());
        notes.append("\n");
        notes.append("EFFECTIVE DATE: ");
        notes.append(achIncomeFileTransaction.getEffectiveDate());
        notes.append("\n");
        notes.append("COMPANY ID: ");
        notes.append(achIncomeFileTransaction.getCompanyId());
        notes.append("\n");

        if (achIncomeFileTransaction.getTrace() != null) {
            notes.append("TRACE NUMBER: ");
            notes.append(achIncomeFileTransaction.getTrace().getTraceNumber());
            notes.append("\n");
        }

        List<AchIncomeFileTransactionReference> fileTransactionReferences = achIncomeFileTransaction.getReferences();
        for (AchIncomeFileTransactionReference fileTransactionReference : fileTransactionReferences) {
            if (CuFPConstants.AchIncomeFileTransactionReference.REF_REFERENCE_TYPE_TN.equals(fileTransactionReference.getType())) {
                notes.append("TRANSACTION NUMBER: ");
                notes.append(fileTransactionReference.getValue());
                notes.append("\n");
            }

            if (CuFPConstants.AchIncomeFileTransactionReference.REF_REFERENCE_TYPE_CT.equals(fileTransactionReference.getType())) {
                notes.append("CONTRACT NUMBER: ");
                notes.append(fileTransactionReference.getValue());
                notes.append("\n");
            }

            if (CuFPConstants.AchIncomeFileTransactionReference.REF_REFERENCE_TYPE_VV.equals(fileTransactionReference.getType())) {
                notes.append("VOUCHER NUMBER: ");
                notes.append(fileTransactionReference.getValue());
                notes.append("\n");
            }
        }

        List<AchIncomeFileTransactionDateTime> transactionDateTimes = achIncomeFileTransaction.getDateTimes();
        for (AchIncomeFileTransactionDateTime transactionDateTime : transactionDateTimes) {
            if (CuFPConstants.AchIncomeFileTransactionDateTime.DTM_DATE_TYPE_097.equals(transactionDateTime.getType())) {
                notes.append("TRANSACTION CREATION: ");
                notes.append(transactionDateTime.getDateTime());
                notes.append("\n");
            }
        }

        String payerNoteText = "";
        String receiverNoteText = "";

        List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayeeNames = achIncomeFileTransaction.getPayerOrPayees();
        for (AchIncomeFileTransactionPayerOrPayeeName payerOrPayeeName : payerOrPayeeNames) {
            if (CuFPConstants.AchIncomeFileTransactionPayerOrPayeeName.PAYER_TYPE_PE.equals(payerOrPayeeName.getType())) {
                receiverNoteText = payerOrPayeeName.getName();
                if (payerOrPayeeName.getIdCode() != null &&
                        payerOrPayeeName.getIdQualifier() != null) {
                    receiverNoteText = receiverNoteText + " " + payerOrPayeeName.getIdCode() + ":" + payerOrPayeeName.getIdQualifier();
                }
            }

            if (CuFPConstants.AchIncomeFileTransactionPayerOrPayeeName.PAYER_TYPE_PR.equals(payerOrPayeeName.getType())) {
                payerNoteText = payerOrPayeeName.getName();
            }
        }

        if (StringUtils.isBlank(payerNoteText) && achIncomeFileTransaction.getPremiumAdminsContact() != null) {
            payerNoteText = achIncomeFileTransaction.getPremiumAdminsContact().getName();
        }

        if (StringUtils.isBlank(receiverNoteText) && achIncomeFileTransaction.getPremiumReceiverName() != null) {
            receiverNoteText = achIncomeFileTransaction.getPremiumReceiverName().getName();
        }

        if (StringUtils.isNotBlank(receiverNoteText)) {
            notes.append("RECEIVER: ");
            notes.append(receiverNoteText);
            notes.append("\n");
        }

        if (StringUtils.isNotBlank(payerNoteText)) {
            notes.append("ORIGINATOR: ");
            notes.append(payerNoteText);
            notes.append("\n");
        }

        return notes;
    }

    private List<String> addRMRAndNTELinesToNotes(AchIncomeFileTransaction achIncomeFileTransaction, StringBuilder notes) {
        List<String> achNotes = new ArrayList<>();

        boolean header = true;
        Integer lineCount = 1;

        for (AchIncomeFileTransactionOpenItemReference openItemReference : achIncomeFileTransaction.getOpenItemReferences()) {
            int leftNoteSize = MAX_NOTE_SIZE - notes.length();
            if ((notes.length() >= MAX_NOTE_SIZE) || (leftNoteSize <= MIN_NOTE_SIZE)) {
                achNotes.add(notes.toString());
                notes = new StringBuilder();
                header = true;
            }
            String type = openItemReference.getType();
            if (CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_REFERENCE_TYPE_CR.equals(type) ||
                    CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_REFERENCE_TYPE_IV.equals(type) ||
                    CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_REFERENCE_TYPE_OI.equals(type)) {
                if (header) {
                    notes.append(StringUtils.rightPad("LINE", CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_LINE_COLUMN_WIDTH));
                    notes.append(StringUtils.rightPad("INVOICE NUMBER", CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_INVOICE_NUMBER_COLUMN_WIDTH));
                    notes.append(StringUtils.rightPad("NETAMOUNT PAID", CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_NET_AMOUNT_COLUMN_WIDTH));
                    notes.append(StringUtils.rightPad("INVOICE AMOUNT", CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_INVOICE_AMOUNT_COLUMN_WIDTH));
                    notes.append("\n");
                    header = false;
                }

                notes.append(StringUtils.rightPad(getFormattedAmount("00000", lineCount), CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_LINE_COLUMN_WIDTH));
                notes.append(StringUtils.rightPad(openItemReference.getInvoiceNumber(), CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_INVOICE_NUMBER_COLUMN_WIDTH));
                if (openItemReference.getNetAmount() != null) {
                    notes.append(StringUtils.leftPad(getFormattedAmount("##,##,###.00", openItemReference.getNetAmount()), CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_NET_AMOUNT_COLUMN_WIDTH));

                }
                if (openItemReference.getInvoiceAmount() != null) {
                    notes.append(StringUtils.leftPad(getFormattedAmount("##,##,###.00", openItemReference.getInvoiceAmount()), CuFPConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_INVOICE_AMOUNT_COLUMN_WIDTH));
                }
                notes.append("\n");
                lineCount++;
            }
        }

        List<AchIncomeFileTransactionNote> fileTransactionNotes = achIncomeFileTransaction.getNotes();
        for (AchIncomeFileTransactionNote fileTransactionNote : fileTransactionNotes) {
            if (fileTransactionNote.getType() != null) {
                String nteTxt = fileTransactionNote.getType().toUpperCase(Locale.US) + ": " + fileTransactionNote.getValue();
                int notesPlusNoteTxtLength = nteTxt.length() + notes.length();
                if (notesPlusNoteTxtLength >= MAX_NOTE_SIZE) {
                    achNotes.add(notes.toString());
                    notes = new StringBuilder();
                }
                notes.append(nteTxt.trim());
                notes.append("\n");
            }

        }
        achNotes.add(notes.toString());

        return achNotes;
    }

    private void setAchIncomeTransactionCountsAndAmounts(List<AchIncomeFileTransaction> achIncomeFileTransaction) {
        for (AchIncomeFileTransaction transaction : achIncomeFileTransaction) {
            boolean isSkippedTransaction = false;

            if (KFSConstants.GL_DEBIT_CODE.equals(transaction.getCreditDebitIndicator())) {
                isSkippedTransaction = true;
            }

            if (CuFPConstants.AchIncomeFileTransaction.TRANS_PAYMENT_METHOD_ACH.equals(transaction.getPaymentMethodCode())) {
                if (isSkippedTransaction) {
                    achTotalSkippedTransactions++;
                    achTotalSkippedTransactionAmount = achTotalSkippedTransactionAmount.add(transaction.getTransactionAmount());
                } else {
                    achTotalPostedTransactions++;
                    achTotalPostedTransactionAmount = achTotalPostedTransactionAmount.add(transaction.getTransactionAmount());
                }
            } else if (CuFPConstants.AchIncomeFileTransaction.TRANS_PAYMENT_METHOD_FWT.equals(transaction.getPaymentMethodCode())) {
                if (isSkippedTransaction) {
                    wiredTotalSkippedTransactions++;
                    wiredTotalSkippedTransactionAmount = wiredTotalSkippedTransactionAmount.add(transaction.getTransactionAmount());
                } else {
                    wiredTotalPostedTransactions++;
                    wiredTotalPostedTransactionAmount = wiredTotalPostedTransactionAmount.add(transaction.getTransactionAmount());
                }
            }
        }
    }

    private String getEmailMessageText(AchIncomeFile achIncomeFile, String payerMessage) {
        StringBuilder message = new StringBuilder();

        int totalPostedTransation = achTotalPostedTransactions + wiredTotalPostedTransactions;
        KualiDecimal totalPostedTransactionAmount = achTotalPostedTransactionAmount.add(wiredTotalPostedTransactionAmount);

        String fileDateTime;
        try {
            fileDateTime = getFormattedTimestamp(achIncomeFile, "fileDate/Time").toString();
        } catch (FormatException e) {
            // use the original file Date/Time string if encountered invalid format
            fileDateTime = achIncomeFile.getFileDate() + " " + achIncomeFile.getFileTime();
        }

        message.append("File Date: " + fileDateTime);
        message.append("\n");
        message.append("                    ");
        message.append("COUNT               ");
        message.append("        AMOUNT     ");
        message.append("\n");
        message.append(StringUtils.rightPad("ACH Posted", 20));
        message.append(StringUtils.rightPad(achTotalPostedTransactions + "", 20));
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", achTotalPostedTransactionAmount), 20));
        message.append("\n");
        message.append(StringUtils.rightPad("Wire Posted", 20));
        message.append(StringUtils.rightPad(wiredTotalPostedTransactions + "", 20));
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", wiredTotalPostedTransactionAmount), 20));
        message.append("\n");
        message.append(StringUtils.rightPad("Total Posted", 20));
        message.append(StringUtils.rightPad(totalPostedTransation + "", 20));
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", totalPostedTransactionAmount), 20));
        message.append("\n");
        message.append("\n");
        message.append(StringUtils.rightPad("ACH Skipped", 20));
        message.append(StringUtils.rightPad(achTotalSkippedTransactions + "", 20));
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", achTotalSkippedTransactionAmount), 20));
        message.append("\n");
        message.append(StringUtils.rightPad("Wire Skipped", 20));
        message.append(StringUtils.rightPad(wiredTotalSkippedTransactions + "", 20));
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", wiredTotalSkippedTransactionAmount), 20));
        message.append("\n");
        if (StringUtils.isNotBlank(payerMessage)) {
            message.append("\n");
            message.append("Transactions Missing Payer Name: ");
            message.append("\n");
            message.append(payerMessage);
        }
        return message.toString();
    }

    private Timestamp getFormattedTimestamp(AchIncomeFile achIncomeFile, String fieldName) {
        String fileDateTime = achIncomeFile.getFileDate() + achIncomeFile.getFileTime();

        // need to use 24 hour format, since otherwise exception will be thrown if the time falls in PM range.
        SimpleDateFormat dateFormat = new SimpleDateFormat(CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT, Locale.US);
        dateFormat.setLenient(false);

        try {
            java.util.Date parsedDate = dateFormat.parse(fileDateTime);
            return new Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            throw new FormatException(fieldName + " must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n" + e);
        }
    }

    private void copyAllMessage(Object parsedObject, PhysicalFlatFileInformation physicalFlatFileInformation) {
        List<AchIncomeFile> achIncomeFiles = (List<AchIncomeFile>) parsedObject;
        for (AchIncomeFile achIncomeFile : achIncomeFiles) {
            FlatFileInformation fileInformation = new FlatFileInformation();
            FlatFileTransactionInformation information = achIncomeFile.getFlatFileTransactionInformation();
            fileInformation.getOrAddFlatFileData(achIncomeFile.getInterchangeControlNumber(), information);
            fileInformation.addFileInfoMessage(achIncomeFile.getEmailMessageText());
            physicalFlatFileInformation.getFlatFileInfomationList().add(fileInformation);
        }
    }

    private String getFormattedAmount(String pattern, KualiDecimal amount) {
        DecimalFormat formatter = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.US));
        return formatter.format(amount);
    }

    private String getFormattedAmount(String pattern, Integer value) {
        DecimalFormat formatter = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.US));
        return formatter.format(value);
    }

    public void process(String fileName, Object parsedFileContents) {
        // nothing to do
    }

    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        return null;
    }

    protected DocumentSearchService getDocumentSearchService() {
        return KEWServiceLocator.getDocumentSearchService();
    }

    public AttachmentService getAttachmentService() {
        return attachmentService;
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public BankService getBankService() {
        return bankService;
    }

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setAttachmentsDirectory(String attachmentsDirectory) {
        this.attachmentsDirectory = attachmentsDirectory;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
