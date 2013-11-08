/*
 * Copyright 2008 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.batch.ElectronicInvoiceInputFileType;
import org.kuali.kfs.module.purap.batch.ElectronicInvoiceStep;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoice;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItem;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItemMapping;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoad;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoadSummary;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceOrder;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectReason;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectReasonType;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.dataaccess.ElectronicInvoicingDao;
import org.kuali.kfs.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.AccountsPayableService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.service.RequisitionService;
import org.kuali.kfs.module.purap.document.validation.event.AttributedCalculateAccountsPayableEvent;
import org.kuali.kfs.module.purap.document.validation.event.AttributedPaymentRequestForEInvoiceEvent;
import org.kuali.kfs.module.purap.exception.CxmlParseException;
import org.kuali.kfs.module.purap.exception.PurError;
import org.kuali.kfs.module.purap.service.ElectronicInvoiceHelperService;
import org.kuali.kfs.module.purap.service.ElectronicInvoiceMatchingService;
import org.kuali.kfs.module.purap.util.ElectronicInvoiceUtils;
import org.kuali.kfs.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kew.dto.DocumentSearchCriteriaDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultRowDTO;
import org.kuali.rice.kew.dto.KeyValueDTO;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.bo.Attachment;
import org.kuali.rice.kns.bo.DocumentHeader;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.bo.PersistableBusinessObject;
import org.kuali.rice.kns.exception.ValidationException;
import org.kuali.rice.kns.mail.InvalidAddressException;
import org.kuali.rice.kns.mail.MailMessage;
import org.kuali.rice.kns.service.AttachmentService;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.KualiRuleService;
import org.kuali.rice.kns.service.MailService;
import org.kuali.rice.kns.service.NoteService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowInfo;
import org.kuali.rice.kns.workflow.service.WorkflowDocumentService;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

/**
 * This is a helper service to parse electronic invoice file, match it with a PO and create PREQs based on the eInvoice. Also, it 
 * provides helper methods to the reject document to match it with a PO and create PREQ.
 */

public class ElectronicInvoiceHelperServiceImpl implements ElectronicInvoiceHelperService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ElectronicInvoiceHelperServiceImpl.class);

    protected final String UNKNOWN_DUNS_IDENTIFIER = "Unknown";
    protected final String INVOICE_FILE_MIME_TYPE = "text/xml";  
    public static final String WORKFLOW_SEARCH_RESULT_KEY = "routeHeaderId";

	private static final int NOTE_TEXT_DEFAULT_MAX_LENGTH = 800;
	//KFSPTS-1891
    protected static final String DEFAULT_EINVOICE_PAYMENT_METHOD_CODE = "A";

    private StringBuffer emailTextErrorList;
    private HashMap<String, Integer> loadCounts = new HashMap<String, Integer>();
    
    protected ElectronicInvoiceInputFileType electronicInvoiceInputFileType;
    protected MailService mailService;
    protected ElectronicInvoiceMatchingService matchingService; 
    protected ElectronicInvoicingDao electronicInvoicingDao;
    protected BatchInputFileService batchInputFileService;
    protected VendorService vendorService;
    protected PurchaseOrderService purchaseOrderService;
    protected PaymentRequestService paymentRequestService;
    protected KualiConfigurationService kualiConfigurationService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    
    private static final String EXTRACT_FAILURES = "Extract Failures";
    private static final String REJECT = "Reject";
    private static final String ACCEPT = "Accept";
    
    /**
     * 
     * 
     * @return 
     */
    public void loadElectronicInvoices() {

        String baseDirName = getBaseDirName();
        String rejectDirName = getRejectDirName();
        String acceptDirName = getAcceptDirName();
        String extractFailureDirName = getExtractFailureDirName();
        emailTextErrorList = new StringBuffer();

        if (LOG.isInfoEnabled()){
            LOG.info("Invoice Base Directory - " + electronicInvoiceInputFileType.getDirectoryPath());
            LOG.info("Invoice Accept Directory - " + acceptDirName);
            LOG.info("Invoice Reject Directory - " + rejectDirName);
        }

        if (StringUtils.isBlank(rejectDirName)) {
            throw new RuntimeException("Reject directory name should not be empty");
        }

        if (StringUtils.isBlank(acceptDirName)) {
            throw new RuntimeException("Accept directory name should not be empty");
        }

        if (StringUtils.isBlank(extractFailureDirName)) {
            throw new RuntimeException("ExtractFailure directory name should not be empty");
        }

        File baseDir = new File(baseDirName);
        if (!baseDir.exists()){
            throw new RuntimeException("Base dir [" + baseDirName + "] doesn't exists in the system");
        }
        
        // Retrieve a list of files to be processed by the einvoice load
        File[] filesToBeProcessed = baseDir.listFiles(new FileFilter() {
                                                            public boolean accept(File file) {
                                                                String fullPath = FilenameUtils.getFullPath(file.getAbsolutePath());
                                                                String fileName = FilenameUtils.getBaseName(file.getAbsolutePath());
                                                                File processedFile = new File(fullPath + File.separator + fileName + ".processed");
                                                                return (!file.isDirectory() && 
                                                                        file.getName().endsWith(".xml") &&
                                                                        !processedFile.exists());
                                                            }
                                                        });

        
        // Send email indicating no files processed if no files available for loading.
        if (filesToBeProcessed == null || filesToBeProcessed.length == 0) {
            sendSummary(new StringBuffer().append("\n\n").append(PurapConstants.ElectronicInvoice.NO_FILES_PROCESSED_EMAIL_MESSAGE).append("\n\n"));
            return;
        }

        // Create directories, if not there
        try {
            FileUtils.forceMkdir(new File(acceptDirName));
            FileUtils.forceMkdir(new File(rejectDirName));
            FileUtils.forceMkdir(new File(extractFailureDirName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        if (LOG.isInfoEnabled()){
            LOG.info(filesToBeProcessed.length + " file(s) available for processing");
        }

        ElectronicInvoiceLoad eInvoiceLoad = new ElectronicInvoiceLoad();

        // Process einvoice files
        for (File xmlFile : filesToBeProcessed) {

            LOG.info("Processing " + xmlFile.getName() + "....");

            byte[] modifiedXML = addNamespaceDefinition(eInvoiceLoad, xmlFile);
            
            processElectronicInvoice(eInvoiceLoad, xmlFile, modifiedXML);    
            
        }
        
        StringBuffer finalText = buildLoadSummary(eInvoiceLoad);
        sendSummary(finalText);

        LOG.info("Processing completed");
        clearLoadCounts(); // Need to clear the counts after each run, so the totals don't show cumulative counts with each subsequent run.
    }
    
    /**
     * 
     * @return 
     */
    public boolean routeDocuments() {
    	routeEIRTDocuments();
    	routePREQDocuments();
    	
    	return true;
    }

    /**
     * 
     * @return
     */
    protected boolean routeEIRTDocuments() {
    	List<String> documentIdList = null;
        try {
            documentIdList = retrieveDocumentsToRoute(KEWConstants.ROUTE_HEADER_SAVED_CD, ElectronicInvoiceRejectDocument.class);
        } catch (WorkflowException e1) {
            LOG.error("Error retrieving eirt documents for routing: " + e1.getMessage(),e1);
            throw new RuntimeException(e1.getMessage(),e1);
        } catch (RemoteException re) {
            LOG.error("Error retrieving eirt documents for routing: " + re.getMessage(),re);
            throw new RuntimeException(re.getMessage(),re);
        }
        
        //Collections.reverse(documentIdList);
        if ( LOG.isInfoEnabled() ) {
            LOG.info("EIRTs to Route: "+documentIdList);
        }
        
        DocumentService documentService = SpringContext.getBean(DocumentService.class);
        WorkflowDocumentService workflowDocumentService = SpringContext.getBean(WorkflowDocumentService.class);
        
        for (String eirtDocumentId: documentIdList) {
            try {
            	if ( LOG.isInfoEnabled() ) {
                    LOG.info("Retrieving EIRT document # " + eirtDocumentId + ".");
                }
                ElectronicInvoiceRejectDocument eirtDocument = (ElectronicInvoiceRejectDocument)documentService.getByDocumentHeaderId(eirtDocumentId);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("Routing EIRT document # " + eirtDocumentId + ".");
                }
                documentService.prepareWorkflowDocument(eirtDocument);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("EIRT document # " + eirtDocumentId + " prepared for workflow.");
                }
                // calling workflow service to bypass business rule checks
                workflowDocumentService.route(eirtDocument.getDocumentHeader().getWorkflowDocument(), "Routed by electronic invoice batch job", null);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("EIRT document # " + eirtDocumentId + " routed.");
                }
            }
            catch (WorkflowException e) {
                LOG.error("Error routing document # " + eirtDocumentId + " " + e.getMessage());
                throw new RuntimeException(e.getMessage(),e);
            }
        }

  
    	
    	return true;
    }
    
    /**
     * 
     * @return
     */
    protected boolean routePREQDocuments() {
    	List<String> documentIdList = null;
        try {
            documentIdList = retrieveDocumentsToRoute(KEWConstants.ROUTE_HEADER_SAVED_CD, PaymentRequestDocument.class);
        } catch (WorkflowException e1) {
            LOG.error("Error retrieving preq documents for routing: " + e1.getMessage(),e1);
            throw new RuntimeException(e1.getMessage(),e1);
        } catch (RemoteException re) {
            LOG.error("Error retrieving preq documents for routing: " + re.getMessage(),re);
            throw new RuntimeException(re.getMessage(),re);
        }
        
        //Collections.reverse(documentIdList);
        if ( LOG.isInfoEnabled() ) {
            LOG.info("PREQs to Route: "+documentIdList);
        }
        
        DocumentService documentService = SpringContext.getBean(DocumentService.class);
        WorkflowDocumentService workflowDocumentService = SpringContext.getBean(WorkflowDocumentService.class);
        
        for (String preqDocumentId: documentIdList) {
            try {
            	if ( LOG.isInfoEnabled() ) {
                    LOG.info("Retrieving PREQ document # " + preqDocumentId + ".");
                }
            	PaymentRequestDocument preqDocument = (PaymentRequestDocument)documentService.getByDocumentHeaderId(preqDocumentId);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("Routing PREQ document # " + preqDocumentId + ".");
                }
                
                if (preqDocument.getPaymentRequestElectronicInvoiceIndicator()) {
                	documentService.prepareWorkflowDocument(preqDocument);
                	if ( LOG.isInfoEnabled() ) {
                		LOG.info("PREQ document # " + preqDocumentId + " prepared for workflow.");
                	}
                	// calling workflow service to bypass business rule checks
                	workflowDocumentService.route(preqDocument.getDocumentHeader().getWorkflowDocument(), "Routed by electronic invoice batch job", null);
                	if ( LOG.isInfoEnabled() ) {
                		LOG.info("PREQ document # " + preqDocumentId + " routed.");
                	}
                }
            }
            catch (WorkflowException e) {
                LOG.error("Error routing document # " + preqDocumentId + " " + e.getMessage());
                throw new RuntimeException(e.getMessage(),e);
            }
        }

  
    	
    	return true;
    }
    
    /**
     * Returns a list of all initiated but not yet routed payment request or reject documents, using the KualiWorkflowInfo service.
     * @return a list of payment request or eirt documents to route
     */
    protected List<String> retrieveDocumentsToRoute(String statusCode, Class<?> document) throws WorkflowException, RemoteException {
        List<String> documentIds = new ArrayList<String>();
        
        DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(SpringContext.getBean(DataDictionaryService.class).getDocumentTypeNameByClass(document));
        criteria.setDocRouteStatus(statusCode);
        DocumentSearchResultDTO results = SpringContext.getBean(KualiWorkflowInfo.class).performDocumentSearch(GlobalVariables.getUserSession().getPerson().getPrincipalId(), criteria);
        
        for (DocumentSearchResultRowDTO resultRow: results.getSearchResults()) {
            for (KeyValueDTO field : resultRow.getFieldValues()) {
                if (field.getKey().equals(WORKFLOW_SEARCH_RESULT_KEY)) {
                    documentIds.add(parseDocumentIdFromRouteDocHeader(field.getValue()));
                }
            }
        }
        
        return documentIds;
    }
    
    /**
     * Retrieves the document id out of the route document header
     * @param routeDocHeader the String representing an HTML link to the document
     * @return the document id
     */
    protected String parseDocumentIdFromRouteDocHeader(String routeDocHeader) {
        int rightBound = routeDocHeader.indexOf('>') + 1;
        int leftBound = routeDocHeader.indexOf('<', rightBound);
        return routeDocHeader.substring(rightBound, leftBound);
    }

    /**
     * 
     * @param eInvoiceLoad
     * @param invoiceFile
     * @return
     */
    protected byte[] addNamespaceDefinition(ElectronicInvoiceLoad eInvoiceLoad, File invoiceFile) {
        
        if (LOG.isInfoEnabled()){
            LOG.info("Adding namespace definition");
        }
        
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setValidating(false); // It's not needed to validate here
        builderFactory.setIgnoringElementContentWhitespace(true); 
        
        DocumentBuilder builder = null;
        try {
          builder = builderFactory.newDocumentBuilder();  // Create the parser
        } catch(ParserConfigurationException e) {
            LOG.error("Error getting document builder - " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        Document xmlDoc = null;

        try {
            xmlDoc = builder.parse(invoiceFile);
        } catch(Exception e) {
            if (LOG.isInfoEnabled()){
                LOG.info("Error parsing the file - " + e.getMessage());
            }
            rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, e.getMessage(),PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
            return null;
        }
        
        Node node = xmlDoc.getDocumentElement();
        Element element = (Element)node;

        String xmlnsValue = element.getAttribute("xmlns");
        String xmlnsXsiValue = element.getAttribute("xmlns:xsi");
        
        if (StringUtils.equals(xmlnsValue, "http://www.kuali.org/kfs/purap/electronicInvoice") && 
            StringUtils.equals(xmlnsXsiValue, "http://www.w3.org/2001/XMLSchema-instance")){
            if (LOG.isInfoEnabled()){
                LOG.info("xmlns and xmlns:xsi attributes already exists in the invoice xml");
            }
        } else {
            element.setAttribute("xmlns", "http://www.kuali.org/kfs/purap/electronicInvoice");
            element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        }
        
        OutputFormat outputFormat = new OutputFormat(xmlDoc);
        outputFormat.setOmitDocumentType(true);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLSerializer serializer = new XMLSerializer( out,outputFormat );
        try {
            serializer.asDOMSerializer();
            serializer.serialize( xmlDoc.getDocumentElement());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        if (LOG.isInfoEnabled()){
            LOG.info("Namespace validation completed");
        }

        return out.toByteArray();

    }
    
    /**
     * This method processes a single electronic invoice file
     * 
     * @param eInvoiceLoad the load summary to be modified
     * @return boolean where true means there has been some type of reject
     */
    @Transactional
    protected boolean processElectronicInvoice(ElectronicInvoiceLoad eInvoiceLoad, File invoiceFile, byte[] xmlAsBytes) {

        // Checks parameter to see if files should be moved to the accept/reject folders after load
        boolean moveFiles = BooleanUtils.toBoolean(parameterService.getParameterValue(ElectronicInvoiceStep.class, PurapParameterConstants.ElectronicInvoiceParameters.FILE_MOVE_AFTER_LOAD_IND));

        ElectronicInvoice eInvoice = null;
        boolean isExtractFailure = false;
        boolean isCompleteFailure = false;
        
        try {
            eInvoice = loadElectronicInvoice(xmlAsBytes);
        } catch (CxmlParseException e) {
            LOG.info("Error parsing file - " + e.getMessage());
            rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, e.getMessage(), PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
            isExtractFailure = true;
            updateSummaryCounts(EXTRACT_FAILURES);
        } catch (IllegalArgumentException iae) {
            LOG.info("Error loading file - " + iae.getMessage());
            rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, iae.getMessage(), PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
            isExtractFailure = true;
            updateSummaryCounts(EXTRACT_FAILURES);
        }

        if(!isExtractFailure) {
	        if(ObjectUtils.isNotNull(eInvoice)) {
	        	eInvoice.setFileName(invoiceFile.getName());
	        }
	        
	        isCompleteFailure = checkForCompleteFailure(eInvoiceLoad,eInvoice,invoiceFile); 
	        
	        if (!isCompleteFailure) {
		        setVendorDUNSNumber(eInvoice);
		        setVendorDetails(eInvoice);
		        
		        Map<String, ElectronicInvoiceItemMapping> itemTypeMappings = getItemTypeMappings(eInvoice.getVendorHeaderID(),eInvoice.getVendorDetailID());
		        Map<String, ItemType> kualiItemTypes = getKualiItemTypes();
		        
		        if (LOG.isInfoEnabled()){
		            if (itemTypeMappings != null && itemTypeMappings.size() > 0) {
		                LOG.info("Item mappings found");
		            }
		        }
		        
		        boolean validateHeader = true;
	
		        try {
			        for (ElectronicInvoiceOrder order : eInvoice.getInvoiceDetailOrders()) {
			
			            String poID = order.getOrderReferenceOrderID();
			            PurchaseOrderDocument po = null;
			            
			            if (NumberUtils.isDigits(StringUtils.defaultString(poID))){
			                po = purchaseOrderService.getCurrentPurchaseOrder(new Integer(poID));    
			                if (po != null){
			                    order.setInvoicePurchaseOrderID(poID);
			                    order.setPurchaseOrderID(po.getPurapDocumentIdentifier());
			                    order.setPurchaseOrderCampusCode(po.getDeliveryCampusCode());
			                    
			                    if (LOG.isInfoEnabled()){
			                        LOG.info("PO matching Document found");
			                    }
			                }
			            }
			            
			            ElectronicInvoiceOrderHolder orderHolder = new ElectronicInvoiceOrderHolder(eInvoice,order,po,itemTypeMappings,kualiItemTypes,validateHeader);
			            matchingService.doMatchingProcess(orderHolder);
			            
			            if (orderHolder.isInvoiceRejected()){
			                
			                ElectronicInvoiceRejectDocument rejectDocument = createRejectDocument(eInvoice, order,eInvoiceLoad);
			                
			                if (orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier() != null){
			                    rejectDocument.setAccountsPayablePurchasingDocumentLinkIdentifier(orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
			                }
			                
			                String dunsNumber = StringUtils.isEmpty(eInvoice.getDunsNumber()) ?
			                                    UNKNOWN_DUNS_IDENTIFIER :
			                                    eInvoice.getDunsNumber();
			                
			                ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad, dunsNumber);
			                loadSummary.addFailedInvoiceOrder(rejectDocument.getTotalAmount(),eInvoice);
			                eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);
			                
			                LOG.info("Saving Load Summary for DUNS '" + dunsNumber + "'");
			                updateSummaryCounts(REJECT);
			                SpringContext.getBean(BusinessObjectService.class).save(loadSummary);
			            } else {
			                
			                PaymentRequestDocument preqDoc  = createPaymentRequest(orderHolder);
			                
			                if (orderHolder.isInvoiceRejected()){
			                    /**
			                     * This is required. If there is anything in the error map, then it's not possible to route the doc since the rice
			                     * is throwing error if errormap is not empty before routing the doc. 
			                     */
			                    GlobalVariables.getMessageMap().clearErrorMessages();
			                    
			                    ElectronicInvoiceRejectDocument rejectDocument = createRejectDocument(eInvoice, order, eInvoiceLoad);
			                    
			                    if (orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier() != null){
			                        rejectDocument.setAccountsPayablePurchasingDocumentLinkIdentifier(orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
			                    }
			                    
			                    ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad, eInvoice.getDunsNumber());
			                    loadSummary.addFailedInvoiceOrder(rejectDocument.getTotalAmount(),eInvoice);
			                    eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);
			                    
				                LOG.info("Saving Load Summary for DUNS '" + eInvoice.getDunsNumber() + "'");
				                updateSummaryCounts(REJECT);
				                SpringContext.getBean(BusinessObjectService.class).save(loadSummary);
			                } else {
			                    ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad, eInvoice.getDunsNumber());
			                    loadSummary.addSuccessfulInvoiceOrder(preqDoc.getTotalDollarAmount(),eInvoice);
			                    eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);

			                    LOG.info("Saving Load Summary for DUNS '" + eInvoice.getDunsNumber() + "'");
				                updateSummaryCounts(ACCEPT);
				                SpringContext.getBean(BusinessObjectService.class).save(loadSummary);
			                }
			                
			            }
			            
			            validateHeader = false;
			        }
		        } catch(Exception ex) {
		            LOG.info("Error parsing file - " + ex.getMessage());
		            LOG.info("Exception: "+ ex.toString());
		            rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, ex.getMessage(), PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
		        	isExtractFailure = true;
		            updateSummaryCounts(EXTRACT_FAILURES);
		        }
		    }
        }
        
	    // Move files into accept/reject folder as appropriate
        boolean success = true;
        if(moveFiles) {
        	if(isExtractFailure) {
	            if (LOG.isInfoEnabled()){
	                LOG.info(invoiceFile.getName() + " has caused a batch extract failure.");
	            }
	        	success = this.moveFile(invoiceFile, getExtractFailureDirName());
	            if (!success) {
	                String errorMessage = "File with name '" + invoiceFile.getName() + "' could not be moved";
	                throw new PurError(errorMessage);
	            }
        	} else if(isCompleteFailure || (ObjectUtils.isNotNull(eInvoice) && eInvoice.isFileRejected())) {
                if (LOG.isInfoEnabled()){
                    LOG.info(invoiceFile.getName() + " has been rejected.");
                }
	        	success = this.moveFile(invoiceFile, getRejectDirName());
	            if (!success) {
	                String errorMessage = "File with name '" + invoiceFile.getName() + "' could not be moved";
	                throw new PurError(errorMessage);
	            }
	        } else {
                if (LOG.isInfoEnabled()){
                    LOG.info(invoiceFile.getName() + " has been accepted");
                }
	        	success = this.moveFile(invoiceFile, getAcceptDirName());
	            if (!success) {
	                String errorMessage = "File with name '" + invoiceFile.getName() + "' could not be moved";
	                throw new PurError(errorMessage);
	            }
	        }
        } else { // Add .processed file to each successfully processed einvoice file if move files is not enabled.
        	if(!isExtractFailure) {
	            String fullPath = FilenameUtils.getFullPath(invoiceFile.getAbsolutePath());
	            String fileName = FilenameUtils.getBaseName(invoiceFile.getAbsolutePath());
	            File processedFile = new File(fullPath + File.separator + fileName + ".processed");
	            try {
	                FileUtils.touch(processedFile);
	            }
	            catch (IOException e) {
	                throw new RuntimeException(e);
	            }
        	}
        }

        if(ObjectUtils.isNull(eInvoice)) {
        	return true;
        }
        
        return eInvoice.isFileRejected();
    }
    
	/**
	 *     
	 * @param key
	 */
    private void updateSummaryCounts(String key) {
        int count;
        if(loadCounts.containsKey(key)) {
        	count = loadCounts.get(key);
        } else {
        	count = 0;
        }
        loadCounts.put(key, ++count);
    }
    
    /**
     * 
     * @param eInvoice
     */
    protected void setVendorDUNSNumber(ElectronicInvoice eInvoice) {
        
        String dunsNumber = null;
        
        if (StringUtils.equals(eInvoice.getCxmlHeader().getFromDomain(),"DUNS")) {
            dunsNumber = eInvoice.getCxmlHeader().getFromIdentity();
        }else if (StringUtils.equals(eInvoice.getCxmlHeader().getSenderDomain(),"DUNS")) {
            dunsNumber = eInvoice.getCxmlHeader().getSenderIdentity();
        }
        
        if (StringUtils.isNotEmpty((dunsNumber))) {
            if (LOG.isInfoEnabled()){
                LOG.info("Setting Vendor DUNS number - " + dunsNumber);
            }
            eInvoice.setDunsNumber(dunsNumber);
        }
        
    }
    
    /**
     * 
     * @param eInvoice
     */
    protected void setVendorDetails(ElectronicInvoice eInvoice){
        
        if (StringUtils.isNotEmpty(eInvoice.getDunsNumber())){
        	// based on code found in this class
        	// Marcia mentioned that each einvoice just for one po.
        	String poID = eInvoice.getInvoiceDetailOrders().get(0).getOrderReferenceOrderID();
            PurchaseOrderDocument po = null;
            
            VendorDetail vendorDetail = null;
            if (NumberUtils.isDigits(StringUtils.defaultString(poID))){
                po = purchaseOrderService.getCurrentPurchaseOrder(new Integer(poID));    
            }
            if (po != null) {
                vendorDetail = vendorService.getVendorDetail(po.getVendorHeaderGeneratedIdentifier(), po.getVendorDetailAssignedIdentifier());
                if (vendorDetail == null) {
                    vendorDetail = vendorService.getVendorByDunsNumber(eInvoice.getDunsNumber());
                }
            } else {
               vendorDetail = vendorService.getVendorByDunsNumber(eInvoice.getDunsNumber());
            }
            
            if (vendorDetail != null) {
                if (LOG.isInfoEnabled()){
                    LOG.info("Vendor match found - " + vendorDetail.getVendorNumber());
                }
                eInvoice.setVendorHeaderID(vendorDetail.getVendorHeaderGeneratedIdentifier());
                eInvoice.setVendorDetailID(vendorDetail.getVendorDetailAssignedIdentifier());
                eInvoice.setVendorName(vendorDetail.getVendorName());
            }else{
                eInvoice.setVendorHeaderID(null);
                eInvoice.setVendorDetailID(null);
                eInvoice.setVendorName(null);
            }
        }
    }
    
    /**
     * 
     * @param rejectDocument
     */
    protected void validateVendorDetails(ElectronicInvoiceRejectDocument rejectDocument){
        
        boolean vendorFound = false;
        
        if (StringUtils.isNotEmpty(rejectDocument.getVendorDunsNumber())){
            
            PurchaseOrderDocument po = rejectDocument.getCurrentPurchaseOrderDocument();
            
            VendorDetail vendorDetail = null;
            if (po != null){
                vendorDetail = vendorService.getVendorDetail(po.getVendorHeaderGeneratedIdentifier(), po.getVendorDetailAssignedIdentifier());
                if (vendorDetail == null) {
                    vendorDetail = vendorService.getVendorByDunsNumber(rejectDocument.getVendorDunsNumber());
                }
            } else {
               vendorDetail = vendorService.getVendorByDunsNumber(rejectDocument.getVendorDunsNumber());
            }
            
            if (vendorDetail != null) {
                if (LOG.isInfoEnabled()){
                    LOG.info("Vendor [" + vendorDetail.getVendorNumber() + "] match found for the DUNS - " + rejectDocument.getVendorDunsNumber());
                }
                rejectDocument.setVendorHeaderGeneratedIdentifier(vendorDetail.getVendorHeaderGeneratedIdentifier());
                rejectDocument.setVendorDetailAssignedIdentifier(vendorDetail.getVendorDetailAssignedIdentifier());
                rejectDocument.setVendorDetail(vendorDetail);
                vendorFound = true;
            }
        }
        
        if (!vendorFound){
            rejectDocument.setVendorHeaderGeneratedIdentifier(null);
            rejectDocument.setVendorDetailAssignedIdentifier(null);
            rejectDocument.setVendorDetail(null);
        }
        
        String newDocumentDesc = generateRejectDocumentDescription(rejectDocument); 
        rejectDocument.getDocumentHeader().setDocumentDescription(newDocumentDesc);
    }
    
    /**
     * 
     * @param vendorHeaderId
     * @param vendorDetailId
     * @return
     */
    protected Map<String, ElectronicInvoiceItemMapping> getItemTypeMappings(Integer vendorHeaderId, Integer vendorDetailId) {

        Map<String, ElectronicInvoiceItemMapping> itemTypeMappings = null;

        if (vendorHeaderId != null && vendorDetailId != null) {
               itemTypeMappings = electronicInvoicingDao.getItemMappingMap(vendorHeaderId,vendorDetailId);
        }

        if (itemTypeMappings == null || itemTypeMappings.isEmpty()){
            itemTypeMappings = electronicInvoicingDao.getDefaultItemMappingMap();
        }

        return itemTypeMappings;
    }
    
    /**
     * 
     * @param vendorHeaderId
     * @param vendorDetailId
     * @return
     */
    protected String getVendorNumber(Integer vendorHeaderId,
                                   Integer vendorDetailId ){
        
        if (vendorHeaderId != null && vendorDetailId != null) {
            VendorDetail forVendorNo = new VendorDetail();
            forVendorNo.setVendorHeaderGeneratedIdentifier(vendorHeaderId);
            forVendorNo.setVendorDetailAssignedIdentifier(vendorDetailId);
            return forVendorNo.getVendorNumber();
        }else{
            return null;
        }
    }

    /**
     * 
     * @return
     */
    protected Map<String, ItemType> getKualiItemTypes(){
        
        Collection<ItemType> collection = SpringContext.getBean(BusinessObjectService.class).findAll(ItemType.class);
        Map<String, ItemType> kualiItemTypes = new HashMap<String, ItemType>();
        
        if (collection == null || collection.size() == 0){
            throw new RuntimeException("Kauli Item types not available");
        }else{
            if (collection != null){
                ItemType[] itemTypes = new ItemType[collection.size()];
                collection.toArray(itemTypes);
                for (ItemType itemType : itemTypes) {
                    kualiItemTypes.put(itemType.getItemTypeCode(),itemType);
                }
            }
        }
        
        return kualiItemTypes;
    }
    
    /**
     * 
     * @param electronicInvoiceLoad
     * @param electronicInvoice
     * @param invoiceFile
     * @return
     */
    protected boolean checkForCompleteFailure(ElectronicInvoiceLoad electronicInvoiceLoad, 
                                            ElectronicInvoice electronicInvoice,
                                            File invoiceFile){
        
        if (LOG.isInfoEnabled()){
            LOG.info("Checking for complete failure...");
        }
        
        if (electronicInvoice.getInvoiceDetailRequestHeader().isHeaderInvoiceIndicator()) {
            rejectElectronicInvoiceFile(electronicInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile,PurapConstants.ElectronicInvoice.HEADER_INVOICE_IND_ON);
            return true;
        }
        
        if (electronicInvoice.getInvoiceDetailOrders().size() < 1) {
            rejectElectronicInvoiceFile(electronicInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile,PurapConstants.ElectronicInvoice.INVOICE_ORDERS_NOT_FOUND);
            return true;
        }
        
        //it says - Future Release - Enter valid location for Customer Number from E-Invoice
        //mappingService.getInvoiceCustomerNumber() doesnt have any implementation
//        electronicInvoice.setCustomerNumber(mappingService.getInvoiceCustomerNumber(electronicInvoice));
        
        for (ElectronicInvoiceOrder invoiceOrder : electronicInvoice.getInvoiceDetailOrders()) {
          for (ElectronicInvoiceItem invoiceItem : invoiceOrder.getInvoiceItems()) {
            if (invoiceItem != null) {
              invoiceItem.setCatalogNumber(invoiceItem.getReferenceItemIDSupplierPartID());
            }
          }
        }
        
        if (LOG.isInfoEnabled()){
            LOG.info("No Complete failure");
        }
        
        return false;
        
    }
    
    /**
     * 
     * @param rejectReasonTypeCode
     * @return
     */
    protected ElectronicInvoiceRejectReasonType getRejectReasonType(String rejectReasonTypeCode){
        return matchingService.getElectronicInvoiceRejectReasonType(rejectReasonTypeCode);
    }

    /**
     * 
     * @param eInvoiceLoad
     * @param fileDunsNumber
     * @param filename
     * @param rejectReasonTypeCode
     */
    protected void rejectElectronicInvoiceFile(ElectronicInvoiceLoad eInvoiceLoad, 
                                             String fileDunsNumber, 
                                             File filename, 
                                             String rejectReasonTypeCode) {

        rejectElectronicInvoiceFile(eInvoiceLoad,fileDunsNumber,filename,null,rejectReasonTypeCode);
    }

    /**
     * 
     * @param eInvoiceLoad
     * @param fileDunsNumber
     * @param invoiceFile
     * @param extraDescription
     * @param rejectReasonTypeCode
     */
    protected void rejectElectronicInvoiceFile(ElectronicInvoiceLoad eInvoiceLoad, 
                                             String fileDunsNumber, 
                                             File invoiceFile, 
                                             String extraDescription,
                                             String rejectReasonTypeCode) {
        if (LOG.isInfoEnabled()){
            LOG.info("Rejecting the entire invoice file - " + invoiceFile.getName());
        }
        
        ElectronicInvoiceLoadSummary eInvoiceLoadSummary = getOrCreateLoadSummary(eInvoiceLoad, fileDunsNumber);
        eInvoiceLoadSummary.addFailedInvoiceOrder();
        eInvoiceLoad.insertInvoiceLoadSummary(eInvoiceLoadSummary);
        
        ElectronicInvoiceRejectDocument eInvoiceRejectDocument = null;
        try {
            eInvoiceRejectDocument = (ElectronicInvoiceRejectDocument) SpringContext.getBean(DocumentService.class).getNewDocument("EIRT");
            
            eInvoiceRejectDocument.setInvoiceProcessTimestamp(dateTimeService.getCurrentTimestamp());
            eInvoiceRejectDocument.setVendorDunsNumber(fileDunsNumber);
            eInvoiceRejectDocument.setDocumentCreationInProgress(true);
            
            if (invoiceFile != null){
                eInvoiceRejectDocument.setInvoiceFileName(invoiceFile.getName());
            }
            
            List<ElectronicInvoiceRejectReason> list = new ArrayList<ElectronicInvoiceRejectReason>(1);
            
            String message = "Complete failure document has been created for the e-invoice with filename '" + invoiceFile.getName() + "' due to the following error:\n";
            emailTextErrorList.append(message);
            
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(rejectReasonTypeCode,extraDescription, invoiceFile.getName());
            list.add(rejectReason);
            
            emailTextErrorList.append("    - " + rejectReason.getInvoiceRejectReasonDescription()).append("\n");
            emailTextErrorList.append("    - EIRT " + eInvoiceRejectDocument.getDocumentNumber()).append("\n");
            emailTextErrorList.append("\n");
            
            eInvoiceRejectDocument.setInvoiceRejectReasons(list);
            eInvoiceRejectDocument.getDocumentHeader().setDocumentDescription("Complete failure");
            
            String noteText = "Invoice file";
            attachInvoiceXMLWithRejectDoc(eInvoiceRejectDocument,invoiceFile,noteText);
            
            eInvoiceLoad.addInvoiceReject(eInvoiceRejectDocument);
            
        }catch (WorkflowException e) {
            throw new RuntimeException(e);
        }

        if (LOG.isInfoEnabled()){
            LOG.info("Complete failure document has been created (DocNo:" + eInvoiceRejectDocument.getDocumentNumber() + ")");
        }
    }
    
    /**
     * 
     * @param eInvoiceRejectDocument
     * @param attachmentFile
     * @param noteText
     */
    protected void attachInvoiceXMLWithRejectDoc(ElectronicInvoiceRejectDocument eInvoiceRejectDocument,
                                               File attachmentFile,
                                               String noteText){
        
        Note note = null;
        try {
            note = SpringContext.getBean(DocumentService.class).createNoteFromDocument(eInvoiceRejectDocument, noteText);
        }catch (Exception e1) {
            throw new RuntimeException("Unable to create note from document: ", e1);
        }
        
        String attachmentType = null;
        BufferedInputStream fileStream = null;
        try {
            fileStream = new BufferedInputStream(new FileInputStream(attachmentFile));
        }catch (FileNotFoundException e) {
            LOG.error( "Exception opening attachment file", e);
        }
        
        Attachment attachment = null;
        try {
            attachment = SpringContext.getBean(AttachmentService.class).createAttachment(eInvoiceRejectDocument, attachmentFile.getName(),INVOICE_FILE_MIME_TYPE , (int)attachmentFile.length(), fileStream, attachmentType);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to create attachment", e);
        }finally{
            if (fileStream != null){
                try {
                    fileStream.close();
                }catch (IOException e) {
                    LOG.error( "Exception closing file", e);
                }
            }
        }
        
        note.setAttachment(attachment);
        attachment.setNote(note);
        
        PersistableBusinessObject noteParent = getNoteParent(eInvoiceRejectDocument, note);
        noteParent.addNote(note);
        //eInvoiceRejectDocument.getDocumentHeader().addNote(note);
    }
    
    /**
     * 
     * @param document
     * @param newNote
     * @return
     */
    protected PersistableBusinessObject getNoteParent(ElectronicInvoiceRejectDocument document, Note newNote) {
        //get the property name to set (this assumes this is a document type note)
        String propertyName = SpringContext.getBean(NoteService.class).extractNoteProperty(newNote);
        //get BO to set
        PersistableBusinessObject noteParent = (PersistableBusinessObject)ObjectUtils.getPropertyValue(document, propertyName);
        return noteParent;
    }
    
    /**
     * 
     * @param eInvoice
     * @param electronicInvoiceOrder
     * @param eInvoiceLoad
     * @return
     */
    public ElectronicInvoiceRejectDocument createRejectDocument(ElectronicInvoice eInvoice,
                                                                ElectronicInvoiceOrder electronicInvoiceOrder,
                                                                ElectronicInvoiceLoad eInvoiceLoad) {

        if (LOG.isInfoEnabled()){
            LOG.info("Creating reject document [DUNS=" + eInvoice.getDunsNumber() + ",POID=" + electronicInvoiceOrder.getInvoicePurchaseOrderID() + "]");
        }

        ElectronicInvoiceRejectDocument eInvoiceRejectDocument;
        
        try {

            eInvoiceRejectDocument = (ElectronicInvoiceRejectDocument) SpringContext.getBean(DocumentService.class).getNewDocument("EIRT");

            eInvoiceRejectDocument.setInvoiceProcessTimestamp(dateTimeService.getCurrentTimestamp());
            String rejectdocDesc = generateRejectDocumentDescription(eInvoice,electronicInvoiceOrder);
            eInvoiceRejectDocument.getDocumentHeader().setDocumentDescription(rejectdocDesc);
            eInvoiceRejectDocument.setDocumentCreationInProgress(true);
            
            eInvoiceRejectDocument.setFileLevelData(eInvoice);
            eInvoiceRejectDocument.setInvoiceOrderLevelData(eInvoice, electronicInvoiceOrder);

            String noteText = "Invoice file";
            attachInvoiceXMLWithRejectDoc(eInvoiceRejectDocument, getInvoiceFile(eInvoice.getFileName()), noteText);

            eInvoiceLoad.addInvoiceReject(eInvoiceRejectDocument);
            
        } catch (WorkflowException e) {
            throw new RuntimeException(e);
        }
        
        if (LOG.isInfoEnabled()){
            LOG.info("Reject document has been created (DocNo=" + eInvoiceRejectDocument.getDocumentNumber() + ")");
        }
        
        emailTextErrorList.append("An e-invoice from file '" + eInvoice.getFileName() + "' has been rejected due to the following error(s):\n");
        
        // get note text max length from DD
        int noteTextMaxLength = NOTE_TEXT_DEFAULT_MAX_LENGTH;

        Integer noteTextLength = SpringContext.getBean(DataDictionaryService.class).getAttributeMaxLength(Note.class, KNSConstants.NOTE_TEXT_PROPERTY_NAME);
        if (noteTextLength != null) {
        	noteTextMaxLength = noteTextLength.intValue();
        }        
        
        // KITI-2643 - Modified to fix bug reported in KFSPTS-292 
        // Ensure that we don't overflow the maximum size of the note by creating 
        // separate notes if necessary.
        ArrayList<StringBuffer> rejectReasonNotes = new ArrayList<StringBuffer>();
        StringBuffer rejectReasonNote = new StringBuffer();
        String rejectReason = "";
        
        rejectReasonNote.append("This reject document has been created because of the following reason(s):\n");
        int index = 1;
        for (ElectronicInvoiceRejectReason reason : eInvoiceRejectDocument.getInvoiceRejectReasons()) {
            emailTextErrorList.append("    - " + reason.getInvoiceRejectReasonDescription() + "\n");
            emailTextErrorList.append("    - PO  " + eInvoiceRejectDocument.getPurchaseOrderIdentifier() + "\n");
            emailTextErrorList.append("    - EIRT  " + eInvoiceRejectDocument.getDocumentNumber() + "\n");
          
          rejectReason = " " + index + ". " + reason.getInvoiceRejectReasonDescription() + "\n";
          if (rejectReasonNote.length() + rejectReason.length() > noteTextMaxLength) {
        	  rejectReasonNotes.add(rejectReasonNote);
        	  rejectReasonNote = new StringBuffer();
        	  rejectReasonNote.append("Reject document creation reasons continued:\n");
          }
          rejectReasonNote.append(rejectReason);
          index++;
        }
        rejectReasonNotes.add(rejectReasonNote);
        
        emailTextErrorList.append("\n");
        
        for (StringBuffer noteText : rejectReasonNotes) {
        	addRejectReasonsToNote(noteText.toString(), eInvoiceRejectDocument);
        }
        
        return eInvoiceRejectDocument;
    }
    
    /**
     * 
     * @param rejectReasons
     * @param eInvoiceRejectDocument
     */
	protected void addRejectReasonsToNote(String rejectReasons, ElectronicInvoiceRejectDocument eInvoiceRejectDocument){

        try {
            Note note = SpringContext.getBean(DocumentService.class).createNoteFromDocument(eInvoiceRejectDocument, rejectReasons);
            PersistableBusinessObject noteParent = getNoteParent(eInvoiceRejectDocument, note);
            noteParent.addNote(note);
        } catch (Exception e) {
            LOG.error("Error creating reject reason note - " + e.getMessage());
        }
    }
    
	/**
	 * 
	 * @param eInvoice
	 * @param electronicInvoiceOrder
	 * @return
	 */
    protected String generateRejectDocumentDescription(ElectronicInvoice eInvoice,
                                                     ElectronicInvoiceOrder electronicInvoiceOrder){
        
        String poID = StringUtils.isEmpty(electronicInvoiceOrder.getInvoicePurchaseOrderID()) ?
                      "UNKNOWN" :
                      electronicInvoiceOrder.getInvoicePurchaseOrderID();
        
        String vendorName = StringUtils.isEmpty(eInvoice.getVendorName()) ? 
                            "UNKNOWN" :
                            eInvoice.getVendorName();
        
        String description = "PO: " + poID + " Vendor: " + vendorName;
        
        return checkDescriptionLengthAndStripIfNeeded(description);
    }
    
    /**
     * 
     * @param rejectDoc
     * @return
     */
    protected String generateRejectDocumentDescription(ElectronicInvoiceRejectDocument rejectDoc) {

        String poID = StringUtils.isEmpty(rejectDoc.getInvoicePurchaseOrderNumber()) ? 
                      "UNKNOWN" : 
                      rejectDoc.getInvoicePurchaseOrderNumber();

        String vendorName = "UNKNOWN";
        if (rejectDoc.getVendorDetail() != null){
            vendorName = rejectDoc.getVendorDetail().getVendorName();
        }

        String description = "PO: " + poID + " Vendor: " + vendorName;

        return checkDescriptionLengthAndStripIfNeeded(description);
    }
    
    /**
     * 
     * @param description
     * @return
     */
    protected String checkDescriptionLengthAndStripIfNeeded(String description){
        
        int noteTextMaxLength = SpringContext.getBean(DataDictionaryService.class).getAttributeMaxLength(DocumentHeader.class, KNSPropertyConstants.DOCUMENT_DESCRIPTION).intValue();

        if (noteTextMaxLength < description.length()) {
            description = description.substring(0, noteTextMaxLength);
        }
        
        return description;
    }
    
    /**
     * 
     * @param eInvoiceLoad
     * @param fileDunsNumber
     * @return
     */
    public ElectronicInvoiceLoadSummary getOrCreateLoadSummary(ElectronicInvoiceLoad eInvoiceLoad,
                                                               String fileDunsNumber){
        ElectronicInvoiceLoadSummary eInvoiceLoadSummary;
        
        if (eInvoiceLoad.getInvoiceLoadSummaries().containsKey(fileDunsNumber)) {
            eInvoiceLoadSummary = (ElectronicInvoiceLoadSummary) eInvoiceLoad.getInvoiceLoadSummaries().get(fileDunsNumber);
        }
        else {
            eInvoiceLoadSummary = new ElectronicInvoiceLoadSummary(fileDunsNumber);
        }
        
        return eInvoiceLoadSummary;
        
    }
    
    /**
     * 
     * @param xmlAsBytes
     * @return
     * @throws CxmlParseException
     */
    public ElectronicInvoice loadElectronicInvoice(byte[] xmlAsBytes) throws CxmlParseException {
      
      if (LOG.isInfoEnabled()){
          LOG.info("Loading Invoice File");
      }
      
      ElectronicInvoice electronicInvoice = null;
      
      try {
          electronicInvoice = (ElectronicInvoice) batchInputFileService.parse(electronicInvoiceInputFileType, xmlAsBytes);
      }catch (ParseException e) {
          throw new CxmlParseException(e.getMessage());
      }
      
      if (LOG.isInfoEnabled()){
          LOG.info("Successfully loaded the Invoice File");
      }
      
      return electronicInvoice;
      
    }
    
    /**
     * 
     * @param eInvoiceLoad
     * @return
     */
    protected StringBuffer saveLoadSummary(ElectronicInvoiceLoad eInvoiceLoad) {
    	NumberFormat twoDecForm = DecimalFormat.getCurrencyInstance();
    	
        Map<String, ElectronicInvoiceLoadSummary> savedLoadSummariesMap = new HashMap<String, ElectronicInvoiceLoadSummary>();
        StringBuffer summaryMessage = new StringBuffer();
        
        for (String dunsNumber : eInvoiceLoad.getInvoiceLoadSummaries().keySet()) {
            
            ElectronicInvoiceLoadSummary eInvoiceLoadSummary = (ElectronicInvoiceLoadSummary) eInvoiceLoad.getInvoiceLoadSummaries().get(dunsNumber);
            
              if (!eInvoiceLoadSummary.isEmpty().booleanValue()){  
                summaryMessage.append("DUNS Number - ").append(eInvoiceLoadSummary.getVendorDescriptor()).append(":\n");

                summaryMessage.append("     ").append(eInvoiceLoadSummary.getInvoiceLoadSuccessCount());
                summaryMessage.append(" successfully processed invoices for a total of ");
                summaryMessage.append(twoDecForm.format(eInvoiceLoadSummary.getInvoiceLoadSuccessAmount().doubleValue()));
                summaryMessage.append("\n");
                
                summaryMessage.append("     ").append(eInvoiceLoadSummary.getInvoiceLoadFailCount());
                summaryMessage.append(" rejected invoices for an approximate total of ");
                summaryMessage.append(twoDecForm.format(eInvoiceLoadSummary.getInvoiceLoadFailAmount().doubleValue()));
                summaryMessage.append("\n");
                
                summaryMessage.append("\n\n");
                
                savedLoadSummariesMap.put(eInvoiceLoadSummary.getVendorDunsNumber(), eInvoiceLoadSummary);
                
            } else {
                LOG.info("Not saving Load Summary for DUNS '" + dunsNumber + "' because empty indicator is '" + eInvoiceLoadSummary.isEmpty().booleanValue() + "'");
            }
        }
        
        summaryMessage.append("\n\n");
        
        for (ElectronicInvoiceRejectDocument rejectDoc : eInvoiceLoad.getRejectDocuments()) {
            saveRejectDocument(rejectDoc,savedLoadSummariesMap);
        }
        
        return summaryMessage;
    }
    
    /**
     * 
     * @param rejectDoc
     * @param savedLoadSummariesMap
     */
    protected void saveRejectDocument(ElectronicInvoiceRejectDocument rejectDoc, Map<String, ElectronicInvoiceLoadSummary> savedLoadSummariesMap){
        
        LOG.info("Saving Invoice Reject for DUNS '" + rejectDoc.getVendorDunsNumber() + "'");
        
        if (savedLoadSummariesMap.containsKey(rejectDoc.getVendorDunsNumber())) {
            rejectDoc.setInvoiceLoadSummary(savedLoadSummariesMap.get(rejectDoc.getVendorDunsNumber()));
        }
        else {
            rejectDoc.setInvoiceLoadSummary(savedLoadSummariesMap.get(UNKNOWN_DUNS_IDENTIFIER));
        }
        
        try {
            SpringContext.getBean(DocumentService.class).saveDocument(rejectDoc, DocumentSystemSaveEvent.class);
        }
        catch (WorkflowException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * 
     * @param eInvoiceLoad
     * @return
     */
    protected StringBuffer buildLoadSummary(ElectronicInvoiceLoad eInvoiceLoad) {
        StringBuffer summaryText = saveLoadSummary(eInvoiceLoad);

        StringBuffer finalText = new StringBuffer();
        finalText.append("======================================\n");
        finalText.append("               TOTALS\n");
        finalText.append("======================================\n\n");
        finalText.append(ACCEPT).append("           : ").append(loadCounts.get(ACCEPT)!=null?loadCounts.get(ACCEPT):0).append("\n");
        finalText.append(REJECT).append("           : ").append(loadCounts.get(REJECT)!=null?loadCounts.get(REJECT):0).append("\n");
        finalText.append(EXTRACT_FAILURES).append(" : ").append(loadCounts.get(EXTRACT_FAILURES)!=null?loadCounts.get(EXTRACT_FAILURES):0).append("\n");
        finalText.append("\n");
        finalText.append("\n");
        finalText.append("======================================\n");
        finalText.append("             LOAD SUMMARY\n");
        finalText.append("======================================\n\n");
        finalText.append(summaryText);
        finalText.append("\n");
        finalText.append("======================================\n");
        finalText.append("              FAILURES\n");
        finalText.append("======================================\n\n");
        finalText.append(emailTextErrorList);

        LOG.info(finalText);
        
        return finalText;
    }
    
    /**
     * 
     */
    protected void clearLoadCounts() {
    	loadCounts.clear();
    }
    
    /**
     * 
     * @param message
     */
    protected void sendSummary(StringBuffer message) {

        String fromMailId = parameterService.getParameterValue(ElectronicInvoiceStep.class, PurapParameterConstants.ElectronicInvoiceParameters.DAILY_SUMMARY_REPORT_FROM_EMAIL_ADDRESS);
        List<String> toMailIds = parameterService.getParameterValues(ElectronicInvoiceStep.class, PurapParameterConstants.ElectronicInvoiceParameters.DAILY_SUMMARY_REPORT_TO_EMAIL_ADDRESSES);
        
        LOG.info("From email address parameter value:"+fromMailId);
        LOG.info("To email address parameter value:"+toMailIds);
        
        if (StringUtils.isBlank(fromMailId) || toMailIds.isEmpty()){
            LOG.error("From/To mail addresses are empty. Unable to send the message");
        } else {
        
            MailMessage mailMessage = new MailMessage();
            
            mailMessage.setFromAddress(fromMailId);
            setMessageToAddressesAndSubject(mailMessage,toMailIds);
            mailMessage.setMessage(message.toString());
            
            try {
                mailService.sendMessage(mailMessage);
            } catch (InvalidAddressException e) {
                LOG.error("Invalid email address. Message not sent", e);
            }
        }
        
    }
    
    /**
     * 
     * @param message
     * @param toAddressList
     * @return
     */
    protected MailMessage setMessageToAddressesAndSubject(MailMessage message, List<String> toAddressList) {
        
        if (!toAddressList.isEmpty()) {
            for (int i = 0; i < toAddressList.size(); i++) {
                if (StringUtils.isNotEmpty(toAddressList.get(i))) {
                    message.addToAddress(toAddressList.get(i).trim());
                }
            }
        }

        String mailTitle = "E-Invoice Load Results for " + ElectronicInvoiceUtils.getDateDisplayText(dateTimeService.getCurrentDate());
        
        if (kualiConfigurationService.isProductionEnvironment()) {
            message.setSubject(mailTitle);
        } else {
            message.setSubject(kualiConfigurationService.getPropertyString(KFSConstants.ENVIRONMENT_KEY) + " - " + mailTitle);
        }
        return message;
    }
    
    /**
     * This method is responsible for the matching process for a reject document
     *    
     * @return true if the matching process is succeed
     */
    public boolean doMatchingProcess(ElectronicInvoiceRejectDocument rejectDocument){

        /**
         * This is needed here since if the user changes the DUNS number.
         */
        validateVendorDetails(rejectDocument);
        
        Map<String, ElectronicInvoiceItemMapping> itemTypeMappings = getItemTypeMappings(rejectDocument.getVendorHeaderGeneratedIdentifier(),
                                                   rejectDocument.getVendorDetailAssignedIdentifier());
        
        Map<String, ItemType> kualiItemTypes = getKualiItemTypes();

        ElectronicInvoiceOrderHolder rejectDocHolder = new ElectronicInvoiceOrderHolder(rejectDocument,itemTypeMappings,kualiItemTypes);
        matchingService.doMatchingProcess(rejectDocHolder);
        // KFSPTS-1719 : save the nomatchingitems found during match process.
        rejectDocument.setNonMatchItems(rejectDocHolder.getNonMatchItems());
        /**
         * Once we're through with the matching process, it's needed to check whether it's possible
         * to create PREQ for the reject doc
         */
        if (!rejectDocHolder.isInvoiceRejected()) {
            validateInvoiceOrderValidForPREQCreation(rejectDocHolder);
        }
        
        //  determine which of the reject reasons we should suppress based on the parameter
	 	List<String> ignoreRejectTypes = parameterService.getParameterValues(PurapConstants.PURAP_NAMESPACE, "ElectronicInvoiceReject", PurapParameterConstants.ElectronicInvoiceParameters.SUPPRESS_REJECT_REASON_CODES_ON_EIRT_APPROVAL);
	 	List<ElectronicInvoiceRejectReason> rejectReasonsToDelete = new ArrayList<ElectronicInvoiceRejectReason>();
	 	for (ElectronicInvoiceRejectReason rejectReason : rejectDocument.getInvoiceRejectReasons()) {
	 		String rejectedReasonTypeCode = rejectReason.getInvoiceRejectReasonTypeCode();
	 	 	if (StringUtils.isNotBlank(rejectedReasonTypeCode)) {
	 	 		if (ignoreRejectTypes.contains(rejectedReasonTypeCode)) {
	 	 			rejectReasonsToDelete.add(rejectReason);
	 	 		}
	 	 	}
	 	}

	 	//  remove the flagged reject reasons
	 	if (!rejectReasonsToDelete.isEmpty()) {
	 		rejectDocument.getInvoiceRejectReasons().removeAll(rejectReasonsToDelete);
	 	 }
	 	 	
	 	//  if no reject reasons, then clear error messages
	 	if (rejectDocument.getInvoiceRejectReasons().isEmpty()) {
	 		GlobalVariables.getMessageMap().clearErrorMessages();
	 	}
	 	 	
	 	//  this automatically returns false if there are no reject reasons
        return !rejectDocHolder.isInvoiceRejected();
    }
    
    /**
     * 
     */
    public boolean createPaymentRequest(ElectronicInvoiceRejectDocument rejectDocument){
     
        if (rejectDocument.getInvoiceRejectReasons().size() > 0){
            throw new RuntimeException("Not possible to create payment request since the reject document contains " + rejectDocument.getInvoiceRejectReasons().size() + " rejects");
        }
        
        Map<String, ElectronicInvoiceItemMapping> itemTypeMappings = getItemTypeMappings(rejectDocument.getVendorHeaderGeneratedIdentifier(), rejectDocument.getVendorDetailAssignedIdentifier());

        Map<String, ItemType> kualiItemTypes = getKualiItemTypes();

        ElectronicInvoiceOrderHolder rejectDocHolder = new ElectronicInvoiceOrderHolder(rejectDocument,itemTypeMappings,kualiItemTypes);
        // KFSPTS-1719 : restore the nomatchingitems found during matching process.  so, preq items can be created
        rejectDocHolder.setNonMatchItems(rejectDocument.getNonMatchItems());

        createPaymentRequest(rejectDocHolder);
        
        return !rejectDocHolder.isInvoiceRejected();
        
    }
    
    /**
     * 
     * @param orderHolder
     * @return
     */
    protected PaymentRequestDocument createPaymentRequest(ElectronicInvoiceOrderHolder orderHolder){
        
        if (LOG.isInfoEnabled()){
            LOG.info("Creating Payment Request document");
        }
        
        GlobalVariables.getMessageList().clear();
        
        validateInvoiceOrderValidForPREQCreation(orderHolder);
        
        if (LOG.isInfoEnabled()){
            if (orderHolder.isInvoiceRejected()){
                LOG.info("Not possible to convert einvoice details into payment request");
            }else{
                LOG.info("Payment request document creation validation succeeded");
            }
        }
        
        if (orderHolder.isInvoiceRejected()){
            return null;
        }
        
        PaymentRequestDocument preqDoc = null;
        try {
            preqDoc = (PaymentRequestDocument) SpringContext.getBean(DocumentService.class).getNewDocument("PREQ");
        }
        catch (WorkflowException e) {
            String extraDescription = "Error=" + e.getMessage();
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_WORKLOW_EXCEPTION,          
                                                                                            extraDescription, 
                                                                                            orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            LOG.error("Error creating Payment request document - " + e.getMessage());
            return null;
        }
        
        PurchaseOrderDocument poDoc = orderHolder.getPurchaseOrderDocument();
        if (poDoc == null){
            throw new RuntimeException("Purchase Order document (POId=" + preqDoc.getPurapDocumentIdentifier() + ") does not exist in the system");
        }
        
        preqDoc.getDocumentHeader().setDocumentDescription(generatePREQDocumentDescription(poDoc));        
        preqDoc.setStatusCode(PurapConstants.PaymentRequestStatuses.IN_PROCESS);
        preqDoc.setInvoiceDate(orderHolder.getInvoiceDate());
        preqDoc.setInvoiceNumber(orderHolder.getInvoiceNumber());
        preqDoc.setVendorInvoiceAmount(new KualiDecimal(orderHolder.getInvoiceNetAmount()));
        preqDoc.setAccountsPayableProcessorIdentifier("E-Invoice");
        preqDoc.setVendorCustomerNumber(orderHolder.getCustomerNumber());
        preqDoc.setPaymentRequestElectronicInvoiceIndicator(true);
        
        if (orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier() != null){
            preqDoc.setAccountsPayablePurchasingDocumentLinkIdentifier(orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
        }
        
        //Copied from PaymentRequestServiceImpl.populatePaymentRequest()
        //set bank code to default bank code in the system parameter
        //KFSPTS-1891
        boolean hasPaymentMethodCode = false;
        if ( preqDoc instanceof PaymentRequestDocument ) {
            String vendorPaymentMethodCode = ((VendorDetailExtension)poDoc.getVendorDetail().getExtension()).getDefaultB2BPaymentMethodCode();
            if ( StringUtils.isNotEmpty(vendorPaymentMethodCode) ) { 
                ((PaymentRequestDocument)preqDoc).setPaymentMethodCode(vendorPaymentMethodCode);
                hasPaymentMethodCode = true;
            } else {
                ((PaymentRequestDocument)preqDoc).setPaymentMethodCode(DEFAULT_EINVOICE_PAYMENT_METHOD_CODE);
            }
        }
        Bank defaultBank = null;
        if ( hasPaymentMethodCode ) {
            defaultBank = SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class).getBankForPaymentMethod( ((PaymentRequestDocument)preqDoc).getPaymentMethodCode() );
        } else { // default to baseline behavior - extended documents not in use
            //Copied from PaymentRequestServiceImpl.populatePaymentRequest()
            //set bank code to default bank code in the system parameter
            defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(PaymentRequestDocument.class);
        }
        if (defaultBank != null) {
            preqDoc.setBankCode(defaultBank.getBankCode());
            preqDoc.setBank(defaultBank);
        }
        
        RequisitionDocument reqDoc = SpringContext.getBean(RequisitionService.class).getRequisitionById(poDoc.getRequisitionIdentifier());
        String reqDocInitiator = reqDoc.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        if (LOG.isInfoEnabled()) {
        	LOG.info("createPaymentRequest() - Requisition Document Initiator: "+reqDocInitiator);
        }
        try {
            Person user = SpringContext.getBean(org.kuali.rice.kim.service.PersonService.class).getPerson(reqDocInitiator);
            String processingCampusCode = user.getCampusCode();
            if(StringUtils.isEmpty(processingCampusCode)) {
                if (LOG.isInfoEnabled()){
	            	LOG.info("createPaymentRequest() - Processing Campus Code not populated.  Value of campus code: "+processingCampusCode);
	            	LOG.info("createPaymentRequest() - Requisition Initiator User: Name="+user.getNameUnmasked()+"; Campus Code="+user.getCampusCode());
                }
            	// Set the processing campus code to the default value.
            	processingCampusCode = CUPurapConstants.PaymentRequestDefaults.DEFAULT_PROCESSING_CAMPUS_CODE;

            }
            preqDoc.setProcessingCampusCode(processingCampusCode);
        } catch(Exception e) {
            String extraDescription = "Error setting processing campus code - " + e.getMessage();
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, extraDescription, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }
        
        HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList = SpringContext.getBean(AccountsPayableService.class).expiredOrClosedAccountsList(poDoc);
        if (expiredOrClosedAccountList == null){
            expiredOrClosedAccountList = new HashMap<String, ExpiredOrClosedAccountEntry>();
        }
        
        if (LOG.isInfoEnabled()){
             LOG.info(expiredOrClosedAccountList.size() + " accounts has been found as Expired or Closed");
        }
        
        // TODO : add a list of matchint line in poitem, so it can be added to preqitem ? instead of doing it here ?
        preqDoc.populatePaymentRequestFromPurchaseOrder(orderHolder.getPurchaseOrderDocument(),expiredOrClosedAccountList);
        // need to populate here for ext price.  it become per item
        // TODO : no qty po may have qty inv item
        
        // KFSPTS-1719.  convert 1st matching inv item that is qty, but po is non-qty
        checkQtyInvItemForNoQtyOrder(preqDoc, orderHolder);
        populateItemDetails(preqDoc,orderHolder);
        // TODO : investigation Do NOT commit
        if (CollectionUtils.isNotEmpty(orderHolder.getNonMatchItems())) {
        	for (ElectronicInvoiceItemHolder invItem : orderHolder.getNonMatchItems()) {
                PurchaseOrderItem item = (PurchaseOrderItem)ObjectUtils.deepCopy((Serializable) orderHolder.getPurchaseOrderDocument().getItems().get(invItem.getInvoiceItemLineNumber() - 1));
                item.setItemLineNumber(invItem.getInvoiceItemLineNumber());
                item.setItemDescription(invItem.getReferenceDescription());
                item.setItemUnitPrice(invItem.getInvoiceItemUnitPrice()); // this will be populated to reqitem.poitemunitprice
                PaymentRequestItem paymentRequestItem = new PaymentRequestItem(item, preqDoc, expiredOrClosedAccountList);
                paymentRequestItem.setInvLineNumber(Integer.parseInt(invItem.getInvLineNumber()));
                // need following in case inv item is qty item
                paymentRequestItem.setItemQuantity(new KualiDecimal(invItem.getInvoiceItemQuantity()));
                paymentRequestItem.setItemUnitOfMeasureCode(invItem.getInvoiceItemUnitOfMeasureCode());
                paymentRequestItem.setPurchaseOrderItemUnitPrice(invItem.getInvoiceItemUnitPrice());
                // if non qty don't need this unit price set, then this need to have a check
               	if (invItem.getInvoiceItemQuantity() != null && (new KualiDecimal(invItem.getInvoiceItemQuantity())).isPositive()) {
                paymentRequestItem.setItemUnitPrice(invItem.getInvoiceItemUnitPrice());
               	}
               	paymentRequestItem.setItemCatalogNumber(invItem.getCatalogNumberStripped());
                preqDoc.getItems().add(paymentRequestItem);
                orderHolder.setMisMatchItem(invItem);
                populateItemDetailsForNonMatching(preqDoc,orderHolder);
                orderHolder.setMisMatchItem(null);
       		
        	}
        }
        
        /**
         * Validate totals,paydate
         */
        //PaymentRequestDocumentRule.processCalculateAccountsPayableBusinessRules
        SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedCalculateAccountsPayableEvent(preqDoc));
        
        SpringContext.getBean(PaymentRequestService.class).calculatePaymentRequest(preqDoc,true);
        
        processItemsForDiscount(preqDoc,orderHolder);
        
        if (orderHolder.isInvoiceRejected()){
            return null;
        }
        
        SpringContext.getBean(PaymentRequestService.class).calculatePaymentRequest(preqDoc,false);
        /**
         * PaymentRequestReview 
         */
        //PaymentRequestDocumentRule.processRouteDocumentBusinessRules
        SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedPaymentRequestForEInvoiceEvent(preqDoc));
        
        if(GlobalVariables.getMessageMap().hasErrors()){
            if (LOG.isInfoEnabled()){
                LOG.info("***************Error in rules processing - " + GlobalVariables.getMessageMap());
            }
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, GlobalVariables.getMessageMap().toString(), orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }
        
        if(GlobalVariables.getMessageList().size() > 0){
            if (LOG.isInfoEnabled()){
                LOG.info("Payment request contains " + GlobalVariables.getMessageList().size() + " warning message(s)");
                for (int i = 0; i < GlobalVariables.getMessageList().size(); i++) {
                    LOG.info("Warning " + i + "  - " +GlobalVariables.getMessageList().get(i));
                }
            }
        }
        
        addShipToNotes(preqDoc,orderHolder);
        
//        String routingAnnotation = null;
//        if (!orderHolder.isRejectDocumentHolder()){
//            routingAnnotation = "Routed by electronic invoice batch job";
//        }
        
        try {
        	// ==== CU Customization (KFSPTS-1797): Do save-only operations for just non-EIRT-generated PREQs. ====
        	if (orderHolder.isRejectDocumentHolder()) {
        		SpringContext.getBean(DocumentService.class).routeDocument(preqDoc,null, null);
        	} else {
        		SpringContext.getBean(DocumentService.class).saveDocument(preqDoc,DocumentSystemSaveEvent.class);
        	}
            // ==== End CU Customization ====
        }
        catch (WorkflowException e) {
            e.printStackTrace();
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_ROUTING_FAILURE, e.getMessage(), orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }catch(ValidationException e){
            String extraDescription = GlobalVariables.getMessageMap().toString();
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, extraDescription, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }
        
        return preqDoc;
    }
    
    // KFSPTS-1719 : this is for the first matched the polineitem#
    private void checkQtyInvItemForNoQtyOrder(PaymentRequestDocument preqDocument, ElectronicInvoiceOrderHolder orderHolder) {
        List<PaymentRequestItem> preqItems = preqDocument.getItems();

        List<PaymentRequestItem> nonInvItems = new ArrayList<PaymentRequestItem>(); // non qty not in invoice yet
        for (PaymentRequestItem preqItem : preqItems) {

        	// TODO : This is to check the first item that matched to po line and convert to qty if inv is qty po is non-qty
    		if (StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE, preqItem.getItemTypeCode())) {
				if ((preqItem.getInvLineNumber() == null && orderHolder.getMisMatchItem() == null)) {
					ElectronicInvoiceItemHolder itemHolder = orderHolder.getItemByLineNumber(preqItem.getItemLineNumber());
					if (itemHolder != null) { // if the poitem is not in invoice, then skip this
						preqItem.setItemCatalogNumber(itemHolder.getCatalogNumberStripped());
						// even if inv is non qty still set up these values.
						// if (itemHolder.getInvoiceItemQuantity() != null &&
						// (new
						// KualiDecimal(itemHolder.getInvoiceItemQuantity())).isPositive())
						// {
						preqItem.setItemQuantity(new KualiDecimal(itemHolder.getInvoiceItemQuantity()));
						preqItem.setItemUnitOfMeasureCode(itemHolder.getInvoiceItemUnitOfMeasureCode());
						// don't set unit price for nonqty and see what happened
						if (itemHolder.getInvoiceItemQuantity() != null
								&& (new KualiDecimal(itemHolder.getInvoiceItemQuantity())).isPositive()) {
							preqItem.setItemUnitPrice(itemHolder.getInvoiceItemUnitPrice());
						}
						preqItem.setPurchaseOrderItemUnitPrice(itemHolder.getInvoiceItemUnitPrice());
					} else {
						nonInvItems.add(preqItem);
					}
				}
        	}

        }
        if (!nonInvItems.isEmpty()) {
        	preqDocument.getItems().removeAll(nonInvItems);
        }
    }

    /**
     * 
     * @param preqDoc
     * @param orderHolder
     */
    protected void addShipToNotes(PaymentRequestDocument preqDoc, 
                                ElectronicInvoiceOrderHolder orderHolder){
        
        String shipToAddress = orderHolder.getInvoiceShipToAddressAsString();
        
        try {
            Note noteObj = SpringContext.getBean(DocumentService.class).createNoteFromDocument(preqDoc, shipToAddress);
            preqDoc.addNote(noteObj);
        } catch (Exception e) {
             LOG.error("Error creating ShipTo notes - " + e.getMessage());
        }
    }

    /**
     * 
     * @param preqDocument
     * @param orderHolder
     */
    protected void processItemsForDiscount(PaymentRequestDocument preqDocument, 
                                         ElectronicInvoiceOrderHolder orderHolder){
        
        if (LOG.isInfoEnabled()){
            LOG.info("Processing payment request items for discount");
        }
        
        if (!orderHolder.isItemTypeAvailableInItemMapping(ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT)){
            if (LOG.isInfoEnabled()){
                LOG.info("Skipping discount processing since there is no mapping of discount type for this vendor");
            }
            return;
        }
        
        if (orderHolder.getInvoiceDiscountAmount() == null ||
            orderHolder.getInvoiceDiscountAmount() == BigDecimal.ZERO){
            if (LOG.isInfoEnabled()){
                LOG.info("Skipping discount processing since there is no discount amount found in the invoice file");
            }
            return;
        }
        
        KualiDecimal discountValueToUse = new KualiDecimal(orderHolder.getInvoiceDiscountAmount().negate());
        List<PaymentRequestItem> preqItems = preqDocument.getItems();
        
        boolean alreadyProcessedInvoiceDiscount = false;
        boolean hasKualiPaymentTermsDiscountItem = false;
        
        //if e-invoice amount is negative... it is a penalty and we must pay extra 
        for (PaymentRequestItem preqItem : preqItems) {
            
            hasKualiPaymentTermsDiscountItem = hasKualiPaymentTermsDiscountItem || (StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE,preqItem.getItemTypeCode()));
            
            if (isItemValidForUpdation(preqItem.getItemTypeCode(),ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT,orderHolder)){
                
                alreadyProcessedInvoiceDiscount = true;
                
                if (StringUtils.equals(preqItem.getItemTypeCode(),PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)){
                    //Item is kuali payment terms discount item... must perform calculation
                    // if discount item exists on PREQ and discount dollar amount exists... use greater amount
                    if (LOG.isInfoEnabled()){
                        LOG.info("Discount Check - E-Invoice matches PREQ item type '" + preqItem.getItemTypeCode() + "'... now checking for amount");
                    }
                    
                    KualiDecimal preqExtendedPrice = preqItem.getExtendedPrice() == null ? KualiDecimal.ZERO : preqItem.getExtendedPrice();
                    if ( (discountValueToUse.compareTo(preqExtendedPrice)) < 0 ) {
                        if (LOG.isInfoEnabled()){
                            LOG.info("Discount Check - Using E-Invoice amount (" + discountValueToUse + ") as it is more discount than current payment terms amount " + preqExtendedPrice);
                        }
                        preqItem.setItemUnitPrice(discountValueToUse.bigDecimalValue());
                        preqItem.setExtendedPrice(discountValueToUse);
                      }
                } else {
                    // item is not payment terms discount item... just add value
                    // if discount item exists on PREQ and discount dollar amount exists... use greater amount
                    if (LOG.isInfoEnabled()){
                        LOG.info("Discount Check - E-Invoice matches PREQ item type '" + preqItem.getItemTypeCode() + "'");
                        LOG.info("Discount Check - Using E-Invoice amount (" + discountValueToUse + ") as it is greater than payment terms amount");
                    }
                    preqItem.addToUnitPrice(discountValueToUse.bigDecimalValue());
                    preqItem.addToExtendedPrice(discountValueToUse);
                  }
                }
         }
        
        /*
         *   If we have not already processed the discount amount then the mapping is pointed
         *   to an item that is not in the PREQ item list
         *   
         *   FYI - FILE DISCOUNT AMOUNT CURRENTLY HARD CODED TO GO INTO PAYMENT TERMS DISCOUNT ITEM ONLY... ALL OTHERS WILL FAIL
         */
        
        if (!alreadyProcessedInvoiceDiscount) {
            // if we already have a PMT TERMS DISC item but the e-invoice discount wasn't processed... error out
            // if the item mapping for e-invoice discount item is not PMT TERMS DISC item and we haven't processed it... error out
            
            if (hasKualiPaymentTermsDiscountItem || 
                !orderHolder.isItemTypeAvailableInItemMapping(ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT)) {
                ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_DISCOUNT_ERROR, null, orderHolder.getFileName());
                orderHolder.addInvoiceOrderRejectReason(rejectReason);
                return;
            }
            else if (discountValueToUse.isNonZero()) {
                PaymentRequestItem newItem = new PaymentRequestItem();
                newItem.setItemUnitPrice(discountValueToUse.bigDecimalValue());
                newItem.setItemTypeCode(PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE);
                newItem.setItemDescription(PurapConstants.ElectronicInvoice.DISCOUNT_DESCRIPTION);
                newItem.setExtendedPrice(discountValueToUse);
                newItem.setPurapDocument(preqDocument);
                preqDocument.addItem(newItem);                
            }
        }
        
        if (LOG.isInfoEnabled()){
            LOG.info("Completed processing payment request items for discount");
        }
        
    }
    
    /**
     * 
     * @param preqDocument
     * @param orderHolder
     */
    protected void populateItemDetails(PaymentRequestDocument preqDocument, ElectronicInvoiceOrderHolder orderHolder) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Populating invoice order items into the payment request document");
        }

        List<PaymentRequestItem> preqItems = preqDocument.getItems();

        for (PaymentRequestItem preqItem : preqItems) {

        	// TODO : 'no qty' po line item may have 'qty inv item'
        	// force it to match like qty item.
        	if ((preqItem.getInvLineNumber() == null && orderHolder.getMisMatchItem() == null) || (preqItem.getInvLineNumber() != null && StringUtils.equals(preqItem.getInvLineNumber().toString(), orderHolder.getMisMatchItem().getInvLineNumber()))) {
        		if (StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE, preqItem.getItemTypeCode())) {
        			if (preqItem.getItemQuantity() != null && preqItem.getItemQuantity().isPositive() && StringUtils.isNotBlank(preqItem.getItemUnitOfMeasureCode())) {
        				preqItem.setItemTypeCode(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE);
        			}
        		}
        	}
            if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_ITEM, orderHolder)) {
            	if ((preqItem.getInvLineNumber() == null && orderHolder.getMisMatchItem() == null) ||  (preqItem.getInvLineNumber() != null && StringUtils.equals(preqItem.getInvLineNumber().toString(), orderHolder.getMisMatchItem().getInvLineNumber()))) {
                    processAboveTheLineItem(preqItem, orderHolder);
                    if (preqItem.getPurchaseOrderItem().isNoQtyItem()) {
                        if (preqItem.getInvLineNumber() == null && orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()) != null) { // if there po is not in inv, then don't do this
                	        preqItem.setInvLineNumber(Integer.parseInt(orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()).getInvLineNumber()));
                	        preqItem.setItemDescription(orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()).getReferenceDescription());
                        }
                    } else {
                    	preqItem.setInvLineNumber(preqItem.getPurchaseOrderItem().getItemLineNumber());
                    }
            	}
                //KFSPTS-1719 : check if this works for nonqty for unitprice and extended price
            } else if (preqItem.isNoQtyItem()) {
            	//TODO : need to try ti match the inv line to
            	if ((preqItem.getInvLineNumber() == null && orderHolder.getMisMatchItem() == null) ||  (preqItem.getInvLineNumber() != null && StringUtils.equals(preqItem.getInvLineNumber().toString(), orderHolder.getMisMatchItem().getInvLineNumber()))) {
            	    processAboveTheLineItemForNonQty(preqItem, orderHolder);
                    if (preqItem.getInvLineNumber() == null) {
                	    preqItem.setInvLineNumber(Integer.parseInt(orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()).getInvLineNumber()));
                    	preqItem.setItemDescription(orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()).getReferenceDescription());
                    }
            	}
            }else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_TAX, orderHolder)) {
                processTaxItem(preqItem, orderHolder);
            } else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_SHIPPING, orderHolder)) {
                processShippingItem(preqItem, orderHolder);
            } else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_SPECIAL_HANDLING, orderHolder)) {
                processSpecialHandlingItem(preqItem, orderHolder);
            } else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DEPOSIT, orderHolder)) {
                processDepositItem(preqItem, orderHolder);
            } else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DUE, orderHolder)) {
                processDueItem(preqItem, orderHolder);
            }
            
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Successfully populated the invoice order items");
        }

    }
    
    // for non matching items, the additional charge don't have to add it again.
    protected void populateItemDetailsForNonMatching(PaymentRequestDocument preqDocument, ElectronicInvoiceOrderHolder orderHolder) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Populating invoice order items into the payment request document");
        }

        List<PaymentRequestItem> preqItems = preqDocument.getItems();

        for (PaymentRequestItem preqItem : preqItems) {

        	// TODO : 'no qty' po line item may have 'qty inv item'
        	// force it to match like qty item.
        	if ((preqItem.getInvLineNumber() == null && orderHolder.getMisMatchItem() == null) || (preqItem.getInvLineNumber() != null && StringUtils.equals(preqItem.getInvLineNumber().toString(), orderHolder.getMisMatchItem().getInvLineNumber()))) {
        		if (StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE, preqItem.getItemTypeCode())) {
        			if (preqItem.getItemQuantity() != null && preqItem.getItemQuantity().isPositive() && StringUtils.isNotBlank(preqItem.getItemUnitOfMeasureCode())) {
        				preqItem.setItemTypeCode(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE);
        			}
        		}
        	}
            if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_ITEM, orderHolder)) {
            	if ((preqItem.getInvLineNumber() == null && orderHolder.getMisMatchItem() == null) ||  (preqItem.getInvLineNumber() != null && StringUtils.equals(preqItem.getInvLineNumber().toString(), orderHolder.getMisMatchItem().getInvLineNumber()))) {
                processAboveTheLineItem(preqItem, orderHolder);
                if (preqItem.getInvLineNumber() == null) {
                	preqItem.setInvLineNumber(Integer.parseInt(orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()).getInvLineNumber()));
                	preqItem.setItemDescription(orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()).getReferenceDescription());
                }
            	}
                //KFSPTS-1719 : check if this works for nonqty for unitprice and extended price
            } else if (preqItem.isNoQtyItem()) {
            	//TODO : need to try ti match the inv line to
            	if ((preqItem.getInvLineNumber() == null && orderHolder.getMisMatchItem() == null) ||  (preqItem.getInvLineNumber() != null && StringUtils.equals(preqItem.getInvLineNumber().toString(), orderHolder.getMisMatchItem().getInvLineNumber()))) {
            	    processAboveTheLineItemForNonQty(preqItem, orderHolder);
                    if (preqItem.getInvLineNumber() == null) {
                	    preqItem.setInvLineNumber(Integer.parseInt(orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()).getInvLineNumber()));
                    	preqItem.setItemDescription(orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()).getReferenceDescription());
                    }
            	}
            }
            
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Successfully populated the invoice order items");
        }

    }
    /**
     * 
     * @param purapItem
     * @param orderHolder
     */
    protected void processAboveTheLineItem(PaymentRequestItem purapItem, ElectronicInvoiceOrderHolder orderHolder){

        if (LOG.isInfoEnabled()){
            LOG.info("Processing above the line item");
        }
        
        ElectronicInvoiceItemHolder itemHolder = orderHolder.getItemByLineNumber(purapItem.getItemLineNumber().intValue());
        // KFSPTS-1719 : investigation
        if (orderHolder.getMisMatchItem() != null) {
        	itemHolder = orderHolder.getMisMatchItem();
        }
        if (itemHolder == null){
            LOG.info("Electronic Invoice does not have item with Ref Item Line number " + purapItem.getItemLineNumber());
            return;
        }
        
        purapItem.setItemUnitPrice(itemHolder.getInvoiceItemUnitPrice());
        purapItem.setItemQuantity(new KualiDecimal(itemHolder.getInvoiceItemQuantity()));
        purapItem.setItemTaxAmount(new KualiDecimal(itemHolder.getTaxAmount()));
        
        if (itemHolder.getSubTotalAmount() != null){

            purapItem.setExtendedPrice(itemHolder.getSubTotalAmount());
            
        } else {
            
            if (purapItem.getItemQuantity() != null) {
                if (LOG.isInfoEnabled()){
                    LOG.info("Item number " + purapItem.getItemLineNumber() + " needs calculation of extended " +
                             "price from quantity " + purapItem.getItemQuantity() + " and unit cost " + purapItem.getItemUnitPrice());
                }
                purapItem.setExtendedPrice(purapItem.getItemQuantity().multiply(new KualiDecimal(purapItem.getItemUnitPrice())));
              } else {
                  if (LOG.isInfoEnabled()){
                      LOG.info("Item number " + purapItem.getItemLineNumber() + " has no quantity so extended price " +
                               "equals unit price of " + purapItem.getItemUnitPrice());
                  }
                  purapItem.setExtendedPrice(new KualiDecimal(purapItem.getItemUnitPrice()));
              }
        }
        
    }
    
    // KFSPTS-1719
    protected void processAboveTheLineItemForNonQty(PaymentRequestItem purapItem, ElectronicInvoiceOrderHolder orderHolder){

        if (LOG.isInfoEnabled()){
            LOG.info("Processing above the line item");
        }
        
        // TODO : this will not work for mismatching items because they all point to the same poitemline, so need to change this process.
        ElectronicInvoiceItemHolder itemHolder = orderHolder.getItemByLineNumber(purapItem.getItemLineNumber().intValue());
        if (orderHolder.getMisMatchItem() != null) {
        	itemHolder = orderHolder.getMisMatchItem();
        }
        if (itemHolder == null){
            LOG.info("Electronic Invoice does not have item with Ref Item Line number " + purapItem.getItemLineNumber());
            return;
        }
        
//        purapItem.setItemUnitPrice(itemHolder.getInvoiceItemUnitPrice());
        purapItem.setItemTaxAmount(new KualiDecimal(itemHolder.getTaxAmount()));
        
        if (itemHolder.getSubTotalAmount() != null){

            purapItem.setExtendedPrice(itemHolder.getSubTotalAmount());
            // TODO : this is probably ok if it is the first preq.  if it is the second, then need to fure out what has been already paid.
//            purapItem.setItemUnitPrice((new KualiDecimal(purapItem.getPurchaseOrderItemUnitPrice()).subtract(itemHolder.getSubTotalAmount())).bigDecimalValue());
            
        } 
        
    }

    /**
     * 
     * @param purapItem
     * @param orderHolder
     */
    protected void processSpecialHandlingItem(PaymentRequestItem purapItem, ElectronicInvoiceOrderHolder orderHolder){
        
        if (LOG.isInfoEnabled()){
            LOG.info("Processing special handling item");
        }

        purapItem.addToUnitPrice(orderHolder.getInvoiceSpecialHandlingAmount());
        purapItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceSpecialHandlingAmount()));
        
        String invoiceSpecialHandlingDescription = orderHolder.getInvoiceSpecialHandlingDescription();
        
        if (StringUtils.isNotEmpty(invoiceSpecialHandlingDescription)) {
            if (StringUtils.isEmpty(purapItem.getItemDescription())) {
                purapItem.setItemDescription(invoiceSpecialHandlingDescription);
            }
            else {
                purapItem.setItemDescription(purapItem.getItemDescription() + " - " + invoiceSpecialHandlingDescription);
            }
        }
        
    }
    
    /**
     * 
     * @param preqItem
     * @param orderHolder
     */
    protected void processTaxItem (PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder){
        
        if (LOG.isInfoEnabled()){
            LOG.info("Processing Tax Item");
        }
        
        preqItem.addToUnitPrice(orderHolder.getTaxAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getTaxAmount()));
        
        if (StringUtils.isNotEmpty(orderHolder.getTaxDescription())) {
            if (StringUtils.isEmpty(preqItem.getItemDescription())) {
                preqItem.setItemDescription(orderHolder.getTaxDescription());
            } else {
                preqItem.setItemDescription(preqItem.getItemDescription() + " - " + orderHolder.getTaxDescription());
            }
        }
        else {
        	preqItem.setItemDescription("Tax");
        }
        
    }
    
    /**
     * 
     * @param preqItem
     * @param orderHolder
     */
    protected void processShippingItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder){
        
        if (LOG.isInfoEnabled()){
            LOG.info("Processing Shipping Item");
        }
        
        preqItem.addToUnitPrice(orderHolder.getInvoiceShippingAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceShippingAmount()));
        
        if (StringUtils.isNotEmpty(orderHolder.getInvoiceShippingDescription())) {
            if (StringUtils.isEmpty(preqItem.getItemDescription())) {
                preqItem.setItemDescription(orderHolder.getInvoiceShippingDescription());
            } else {
                preqItem.setItemDescription(preqItem.getItemDescription() + " - " + orderHolder.getInvoiceShippingDescription());
            }
        }
        
    }
    
    /**
     * 
     * @param preqItem
     * @param orderHolder
     */
    protected void processDepositItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder){

        LOG.info("Processing Deposit Item");
        
        preqItem.addToUnitPrice(orderHolder.getInvoiceDepositAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceDepositAmount()));
        
    }
    
    /**
     * 
     * @param preqItem
     * @param orderHolder
     */
    protected void processDueItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder){

        LOG.info("Processing Deposit Item");
        
        preqItem.addToUnitPrice(orderHolder.getInvoiceDueAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceDueAmount()));
        
    }
    
    /**
     * 
     * @param preqItem
     */
    protected void setItemDefaultDescription(PaymentRequestItem preqItem){
        
        //If description is empty and item is not type "ITEM"... use default description
        if (StringUtils.isEmpty(preqItem.getItemDescription()) &&
            !StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE, preqItem.getItemTypeCode())){
            if (ArrayUtils.contains(PurapConstants.ElectronicInvoice.ITEM_TYPES_REQUIRES_DESCRIPTION, preqItem.getItemTypeCode())){
                preqItem.setItemDescription(PurapConstants.ElectronicInvoice.DEFAULT_BELOW_LINE_ITEM_DESCRIPTION);
            }
        }
    }
    
    /**
     * 
     * @param itemTypeCode
     * @param invoiceItemTypeCode
     * @param orderHolder
     * @return
     */
    protected boolean isItemValidForUpdation(String itemTypeCode, String invoiceItemTypeCode, ElectronicInvoiceOrderHolder orderHolder){
        
        return orderHolder.isItemTypeAvailableInItemMapping(invoiceItemTypeCode) && 
               StringUtils.equals(orderHolder.getKauliItemTypeCodeFromMappings(invoiceItemTypeCode),itemTypeCode);
    }
     
    /**
     * 
     * @param poDocument
     * @return
     */
    protected String generatePREQDocumentDescription(PurchaseOrderDocument poDocument) {
        String description = "PO: " + poDocument.getPurapDocumentIdentifier() + " Vendor: " + poDocument.getVendorName() + " Electronic Invoice";
        return checkDescriptionLengthAndStripIfNeeded(description);
    }
    
    /**
     * This validates an electronic invoice and makes sure it can be turned into a Payment Request
     * 
     */
    public void validateInvoiceOrderValidForPREQCreation(ElectronicInvoiceOrderHolder orderHolder){
        
        if (LOG.isInfoEnabled()){
            LOG.info("Validiting ElectronicInvoice Order to make sure that it can be turned into a Payment Request document");
        }
        
        PurchaseOrderDocument poDoc = orderHolder.getPurchaseOrderDocument();
        
        if ( poDoc == null){
            throw new RuntimeException("PurchaseOrder not available");
        }
            
        if (!poDoc.getStatusCode().equals(PurchaseOrderStatuses.OPEN)) {
            orderHolder.addInvoiceOrderRejectReason(matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PO_NOT_OPEN,null,orderHolder.getFileName()));
            return;
        }
        
        if (!orderHolder.isInvoiceNumberAcceptIndicatorEnabled()){
            List preqs = paymentRequestService.getPaymentRequestsByVendorNumberInvoiceNumber(poDoc.getVendorHeaderGeneratedIdentifier(), 
                                                                                             poDoc.getVendorDetailAssignedIdentifier(), 
                                                                                             orderHolder.getInvoiceNumber());
            
            if (preqs != null && preqs.size() > 0){
                ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.INVOICE_ORDER_DUPLICATE,null,orderHolder.getFileName());
                orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_FILE_NUMBER,PurapKeyConstants.ERROR_REJECT_INVOICE_DUPLICATE);
                return;
            }
        }
        
        if (orderHolder.getInvoiceDate() == null){
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.INVOICE_DATE_INVALID,null,orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_FILE_DATE,PurapKeyConstants.ERROR_REJECT_INVOICE_DATE_INVALID);
            return;
        } else if (orderHolder.getInvoiceDate().after(dateTimeService.getCurrentDate())) {
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.INVOICE_DATE_GREATER,null,orderHolder.getFileName()); 
            orderHolder.addInvoiceOrderRejectReason(rejectReason,PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_FILE_DATE,PurapKeyConstants.ERROR_REJECT_INVOICE_DATE_GREATER);
            return;
        }
        
    }
    
    /**
     * 
     * @param fileForMove
     * @param location
     * @return
     */
    protected boolean moveFile(File fileForMove, String location) {
        File moveDir = new File(location);
        return fileForMove.renameTo(new File(moveDir, fileForMove.getName()));
    }

    /**
     * 
     * @return
     */
    protected String getBaseDirName(){
        return electronicInvoiceInputFileType.getDirectoryPath() + File.separator;
    }

    /**
     * 
     * @return
     */
    protected String getRejectDirName(){
        return getBaseDirName() + "reject" + File.separator;
    }
    
    /**
     * 
     * @return
     */
    protected String getAcceptDirName(){
        return getBaseDirName() + "accept" + File.separator;
    }

    /**
     * 
     * @return
     */
    protected String getExtractFailureDirName(){
        return getBaseDirName() + "extractFailure" + File.separator;
    }

    /**
     * 
     * @param fileName
     * @return
     */
    protected File getInvoiceFile(String fileName){
        return new File(getBaseDirName() + fileName);
    }

    /**
     * 
     * @param electronicInvoiceInputFileType
     */
    public void setElectronicInvoiceInputFileType(ElectronicInvoiceInputFileType electronicInvoiceInputFileType) {
        this.electronicInvoiceInputFileType = electronicInvoiceInputFileType;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setElectronicInvoicingDao(ElectronicInvoicingDao electronicInvoicingDao) {
        this.electronicInvoicingDao = electronicInvoicingDao;
    }
    
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setElectronicInvoiceMatchingService(ElectronicInvoiceMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public void setPaymentRequestService(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }
    
    public void setKualiConfigurationService(KualiConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
    
    public void setParameterService(ParameterService parameterService) {
	 	 this.parameterService = parameterService;    
    }
}

