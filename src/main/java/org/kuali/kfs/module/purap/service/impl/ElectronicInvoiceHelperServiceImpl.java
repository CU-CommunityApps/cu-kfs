/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.gl.service.impl.StringHelper;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.batch.ElectronicInvoiceInputFileType;
import org.kuali.kfs.module.purap.batch.ElectronicInvoiceStep;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoice;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItem;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoad;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoadSummary;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceOrder;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectReason;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectReasonType;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.dataaccess.ElectronicInvoicingDao;
import org.kuali.kfs.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
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
import org.kuali.kfs.sys.batch.InitiateDirectoryBase;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a helper service to parse electronic invoice file, match it with a PO and create PREQs based on the
 * eInvoice. Also, it provides helper methods to the reject document to match it with a PO and create PREQ.
 */
// CU customization to change method access to protected
public class ElectronicInvoiceHelperServiceImpl extends InitiateDirectoryBase implements
        ElectronicInvoiceHelperService {

    private static final Logger LOG = LogManager.getLogger();

    protected final String UNKNOWN_DUNS_IDENTIFIER = "Unknown";
    protected final String INVOICE_FILE_MIME_TYPE = "text/xml";

    private StringBuffer emailTextErrorList;

    protected ElectronicInvoiceInputFileType electronicInvoiceInputFileType;
    protected EmailService emailService;
    protected ElectronicInvoiceMatchingService matchingService;
    protected ElectronicInvoicingDao electronicInvoicingDao;
    protected BatchInputFileService batchInputFileService;
    protected VendorService vendorService;
    protected PurchaseOrderService purchaseOrderService;
    protected PaymentRequestService paymentRequestService;
    protected ConfigurationService kualiConfigurationService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected DocumentService documentService;
    protected AttachmentService attachmentService;
    protected NoteService noteService;
    protected BankService bankService;
    protected KualiRuleService kualiRuleService;
    private BusinessObjectService businessObjectService;
    private DataDictionaryService dataDictionaryService;
    private RequisitionService requisitionService;
    private AccountsPayableService accountsPayableService;
    protected PersonService personService;

    @Override
    public ElectronicInvoiceLoad loadElectronicInvoices() {
        LOG.debug("loadElectronicInvoices() started");

        //add a step to check for directory paths
        prepareDirectories(getRequiredDirectoryNames());

        String rejectDirName = getRejectDirName();
        String acceptDirName = getAcceptDirName();
        emailTextErrorList = new StringBuffer();

        boolean moveFiles = parameterService.getParameterValueAsBoolean(ElectronicInvoiceStep.class,
                PurapParameterConstants.ElectronicInvoiceParameters.FILE_MOVE_AFTER_LOAD_IND);

        int failedCnt = 0;

        LOG.info("Invoice Base Directory - {}", electronicInvoiceInputFileType::getDirectoryPath);
        LOG.info("Invoice Accept Directory - {}", acceptDirName);
        LOG.info("Invoice Reject Directory - {}", rejectDirName);
        LOG.info("Is moving files allowed - {}", moveFiles);

        if (StringUtils.isBlank(rejectDirName)) {
            throw new RuntimeException("Reject directory name should not be empty");
        }

        if (StringUtils.isBlank(acceptDirName)) {
            throw new RuntimeException("Accept directory name should not be empty");
        }

        File[] filesToBeProcessed = getFilesToBeProcessed();
        ElectronicInvoiceLoad eInvoiceLoad = new ElectronicInvoiceLoad();

        if (filesToBeProcessed == null ||
            filesToBeProcessed.length == 0) {

            StringBuffer mailText = new StringBuffer();

            mailText.append("\n\n");
            mailText.append(PurapConstants.ElectronicInvoice.NO_FILES_PROCESSED_EMAIL_MESSAGE);
            mailText.append("\n\n");

            sendSummary(mailText);
            return eInvoiceLoad;
        }

        try {
            FileUtils.forceMkdir(new File(acceptDirName));
            FileUtils.forceMkdir(new File(rejectDirName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOG.info("{} file(s) available for processing", filesToBeProcessed.length);

        StringBuilder emailMsg = new StringBuilder();

        for (final File xmlFile : filesToBeProcessed) {
            LOG.info("Processing {}....", xmlFile::getName);

            byte[] modifiedXML = null;
            //process only if file exists and not empty
            if (xmlFile.length() != 0L) {
                modifiedXML = addNamespaceDefinition(eInvoiceLoad, xmlFile);
            }

            boolean isRejected = false;

            if (modifiedXML == null) {
                // Not able to parse the xml
                isRejected = true;
            } else {
                try {
                    isRejected = processElectronicInvoice(eInvoiceLoad, xmlFile, modifiedXML);
                } catch (Exception e) {
                    String msg = xmlFile.getName() + "\n";
                    LOG.error(msg, e);

                    //since getMessage() is empty we'll compose the stack trace and nicely format it.
                    StackTraceElement[] elements = e.getStackTrace();
                    StringBuffer trace = new StringBuffer();
                    trace.append(e.getClass().getName());
                    if (e.getMessage() != null) {
                        trace.append(": ");
                        trace.append(e.getMessage());
                    }
                    trace.append("\n");
                    for (final StackTraceElement element : elements) {
                        trace.append("    at ");
                        trace.append(describeStackTraceElement(element));
                        trace.append("\n");
                    }

                    LOG.error(trace);
                    emailMsg.append(msg);
                    msg += "\n--------------------------------------------------------------------------------------\n" +
                            trace;
                    logProcessElectronicInvoiceError(msg);
                    failedCnt++;

                    // Clear the error map, so that subsequent EIRT routing isn't prevented since validation is throwing
                    // a ValidationException if the error map is not empty before routing the doc.
                    GlobalVariables.getMessageMap().clearErrorMessages();
                    continue;
                }
            }

            // If there is a single order has rejects and the remainings are accepted in a invoice file, then the
            // entire file has been moved to the reject dir.
            if (isRejected) {
                LOG.info("{} has been rejected", xmlFile::getName);
                if (moveFiles) {
                    LOG.info("{} has been marked to move to {}", xmlFile::getName, () -> rejectDirName);

                    eInvoiceLoad.addRejectFileToMove(xmlFile, rejectDirName);
                }
            } else {
                LOG.info("{} has been accepted", xmlFile::getName);
                if (moveFiles) {
                    if (!moveFile(xmlFile, acceptDirName)) {
                        String msg = xmlFile.getName() + " unable to move";
                        LOG.error(msg);
                        throw new PurError(msg);
                    }
                }
            }

            if (!moveFiles) {
                String fullPath = FilenameUtils.getFullPath(xmlFile.getAbsolutePath());
                String fileName = FilenameUtils.getBaseName(xmlFile.getAbsolutePath());
                File processedFile = new File(fullPath + File.separator + fileName + ".processed");
                try {
                    FileUtils.touch(processedFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            deleteDoneFile(xmlFile);
        }

        emailTextErrorList.append("\nFAILED FILES\n");
        emailTextErrorList.append("-----------------------------------------------------------\n\n");
        emailTextErrorList.append(emailMsg);
        emailTextErrorList.append("\nTOTAL COUNT\n");
        emailTextErrorList.append("===========================\n");
        emailTextErrorList.append("      ").append(failedCnt).append(" FAILED\n");
        emailTextErrorList.append("===========================\n");

        StringBuffer summaryText = saveLoadSummary(eInvoiceLoad);

        StringBuffer finalText = new StringBuffer();
        finalText.append(summaryText);
        finalText.append("\n");
        finalText.append(emailTextErrorList);
        sendSummary(finalText);

        LOG.info("Processing completed");

        return eInvoiceLoad;
    }

    protected File[] getFilesToBeProcessed() {
        File[] filesToBeProcessed;
        String baseDirName = getBaseDirName();
        File baseDir = new File(baseDirName);
        if (!baseDir.exists()) {
            throw new RuntimeException("Base dir [" + baseDirName + "] doesn't exists in the system");
        }
        filesToBeProcessed = baseDir.listFiles(file -> {
            String fullPath = FilenameUtils.getFullPath(file.getAbsolutePath());
            String fileName = FilenameUtils.getBaseName(file.getAbsolutePath());
            File processedFile = new File(fullPath + File.separator + fileName + ".processed");
            return !file.isDirectory() && file.getName().endsWith(electronicInvoiceInputFileType.getFileExtension())
                    && !processedFile.exists();
        });

        return filesToBeProcessed;
    }
    
    // CU customization: change to protected
    protected void logProcessElectronicInvoiceError(final String msg) {
        final File file = new File(electronicInvoiceInputFileType.getReportPath() + "/" +
            electronicInvoiceInputFileType.getReportPrefix() + "_" +
            dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate()) + "." +
            electronicInvoiceInputFileType.getReportExtension());

        try (BufferedWriter writer = new BufferedWriter(new PrintWriter(file, StandardCharsets.UTF_8))) {
            writer.write(msg);
            writer.newLine();
        } catch (final FileNotFoundException e) {
            LOG.error("{} not found  {}", () -> file, e::getMessage);
            throw new RuntimeException(file + " not found " + e.getMessage(), e);
        } catch (final IOException e) {
            LOG.error("Error writing to BufferedWriter {}", e::getMessage);
            throw new RuntimeException("Error writing to BufferedWriter " + e.getMessage(), e);
        }
    }

    /**
     * @param element
     * @return String describing the given StackTraceElement
     */
    private static String describeStackTraceElement(StackTraceElement element) {
        StringBuffer description = new StringBuffer();
        if (element == null) {
            description.append("invalid (null) element");
        }
        description.append(element.getClassName());
        description.append(".");
        description.append(element.getMethodName());
        description.append("(");
        description.append(element.getFileName());
        description.append(":");
        description.append(element.getLineNumber());
        description.append(")");

        return description.toString();
    }

    protected byte[] addNamespaceDefinition(ElectronicInvoiceLoad eInvoiceLoad, File invoiceFile) {
        LOG.debug("addNamespaceDefinition() started");

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        // It's not needed to validate here
        builderFactory.setValidating(false);
        builderFactory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOG.error("addNamespaceDefinition() Error getting document builder - {}", e::getMessage);
            throw new RuntimeException(e);
        }

        Document xmlDoc;
        try {
            LOG.info("addNamespaceDefinition: builder.parse");
            xmlDoc = builder.parse(invoiceFile);
        } catch (Exception e) {
            LOG.info("addNamespaceDefinition: Error parsing the file - {}", e::getMessage);
            rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, e.getMessage(),
                    PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
            return null;
        }
        LOG.info("addNamespaceDefinition: xmlDoc.getDocumentElement()");

        Node node = xmlDoc.getDocumentElement();
        Element element = (Element) node;

        String xmlnsValue = element.getAttribute("xmlns");
        String xmlnsXsiValue = element.getAttribute("xmlns:xsi");

        LOG.info("addNamespaceDefinition: getInvoiceFile");

        if (StringUtils.equals(xmlnsValue, "http://www.kuali.org/kfs/purap/electronicInvoice")
                && StringUtils.equals(xmlnsXsiValue, "http://www.w3.org/2001/XMLSchema-instance")) {
            LOG.info("addNamespaceDefinition: xmlns and xmlns:xsi attributes already exists in the invoice xml");
        } else {
            element.setAttribute("xmlns", "http://www.kuali.org/kfs/purap/electronicInvoice");
            element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            Source source = new DOMSource(xmlDoc);
            Result result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            LOG.fatal("Failed to transform {} xml.{}", invoiceFile.getName(), e);
            throw new RuntimeException(e);
        }

        LOG.info("addNamespaceDefinition() Namespace validation completed");

        return out.toByteArray();
    }

    /**
     * This method processes a single electronic invoice file
     *
     * @param eInvoiceLoad the load summary to be modified
     * @return boolean where true means there has been some type of reject
     */
    @Transactional
    protected boolean processElectronicInvoice(ElectronicInvoiceLoad eInvoiceLoad, File invoiceFile,
            byte[] xmlAsBytes) {
        ElectronicInvoice eInvoice;
        try {
            eInvoice = loadElectronicInvoice(xmlAsBytes);
        } catch (CxmlParseException e) {
            LOG.info("Error loading file - {}", e::getMessage);
            rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, e.getMessage(),
                    PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
            return true;
        }

        eInvoice.setFileName(invoiceFile.getName());

        boolean isCompleteFailure = checkForCompleteFailure(eInvoiceLoad, eInvoice, invoiceFile);

        if (isCompleteFailure) {
            return true;
        }

        setVendorDUNSNumber(eInvoice);
        setVendorDetails(eInvoice);

        Map itemTypeMappings = getItemTypeMappings(eInvoice.getVendorHeaderID(), eInvoice.getVendorDetailID());
        Map kualiItemTypes = getKualiItemTypes();

        if (itemTypeMappings != null && itemTypeMappings.size() > 0) {
            LOG.info("Item mappings found");
        }

        boolean validateHeader = true;

        for (ElectronicInvoiceOrder order : eInvoice.getInvoiceDetailOrders()) {
            String poID = order.getOrderReferenceOrderID();
            PurchaseOrderDocument po = null;

            if (NumberUtils.isDigits(StringUtils.defaultString(poID))) {
                po = purchaseOrderService.getCurrentPurchaseOrder(new Integer(poID));
                if (po != null) {
                    order.setInvoicePurchaseOrderID(poID);
                    order.setPurchaseOrderID(po.getPurapDocumentIdentifier());
                    order.setPurchaseOrderCampusCode(po.getDeliveryCampusCode());

                    LOG.info("PO matching Document found");
                }
            }

            ElectronicInvoiceOrderHolder orderHolder = new ElectronicInvoiceOrderHolder(eInvoice, order, po,
                    itemTypeMappings, kualiItemTypes, validateHeader);
            matchingService.doMatchingProcess(orderHolder);

            if (orderHolder.isInvoiceRejected()) {
                ElectronicInvoiceRejectDocument rejectDocument = createRejectDocument(eInvoice, order, eInvoiceLoad);

                if (orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier() != null) {
                    rejectDocument.setAccountsPayablePurchasingDocumentLinkIdentifier(
                            orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
                }

                String dunsNumber = StringUtils.isEmpty(eInvoice.getDunsNumber()) ? UNKNOWN_DUNS_IDENTIFIER :
                    eInvoice.getDunsNumber();

                ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad, dunsNumber);
                loadSummary.addFailedInvoiceOrder(rejectDocument.getTotalAmount(), eInvoice);
                eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);
            } else {
                PaymentRequestDocument preqDoc = createPaymentRequest(orderHolder);

                if (orderHolder.isInvoiceRejected()) {
                    // This is required. If there is anything in the error map, then it's not possible to route the
                    // doc since an error is being thrown if errorMap is not empty before routing the doc.
                    GlobalVariables.getMessageMap().clearErrorMessages();

                    ElectronicInvoiceRejectDocument rejectDocument = createRejectDocument(eInvoice, order,
                            eInvoiceLoad);

                    if (orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier() != null) {
                        rejectDocument.setAccountsPayablePurchasingDocumentLinkIdentifier(
                                orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
                    }

                    ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad,
                            eInvoice.getDunsNumber());
                    loadSummary.addFailedInvoiceOrder(rejectDocument.getTotalAmount(), eInvoice);
                    eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);
                } else {
                    ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad,
                            eInvoice.getDunsNumber());
                    loadSummary.addSuccessfulInvoiceOrder(preqDoc.getTotalDollarAmount(), eInvoice);
                    eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);
                }
            }

            validateHeader = false;
        }

        return eInvoice.isFileRejected();
    }

    protected void setVendorDUNSNumber(ElectronicInvoice eInvoice) {
        String dunsNumber = null;

        if (StringUtils.equals(eInvoice.getCxmlHeader().getFromDomain(), "DUNS")) {
            dunsNumber = eInvoice.getCxmlHeader().getFromIdentity();
        } else if (StringUtils.equals(eInvoice.getCxmlHeader().getSenderDomain(), "DUNS")) {
            dunsNumber = eInvoice.getCxmlHeader().getSenderIdentity();
        }

        if (StringUtils.isNotEmpty(dunsNumber)) {
            LOG.info("Setting Vendor DUNS number - {}", dunsNumber);

            eInvoice.setDunsNumber(dunsNumber);
        }
    }

    protected void setVendorDetails(ElectronicInvoice eInvoice) {
        if (StringUtils.isNotEmpty(eInvoice.getDunsNumber())) {
            VendorDetail vendorDetail = vendorService.getVendorByDunsNumber(eInvoice.getDunsNumber());

            if (vendorDetail != null) {
                LOG.info("Vendor match found - {}", vendorDetail::getVendorNumber);

                eInvoice.setVendorHeaderID(vendorDetail.getVendorHeaderGeneratedIdentifier());
                eInvoice.setVendorDetailID(vendorDetail.getVendorDetailAssignedIdentifier());
                eInvoice.setVendorName(vendorDetail.getVendorName());
            } else {
                eInvoice.setVendorHeaderID(null);
                eInvoice.setVendorDetailID(null);
                eInvoice.setVendorName(null);
            }
        }
    }

    protected void validateVendorDetails(ElectronicInvoiceRejectDocument rejectDocument) {
        boolean vendorFound = false;

        if (StringUtils.isNotEmpty(rejectDocument.getVendorDunsNumber())) {
            VendorDetail vendorDetail = vendorService.getVendorByDunsNumber(rejectDocument.getVendorDunsNumber());

            if (vendorDetail != null) {
                LOG.info(
                        "Vendor [{}] match found for the DUNS - {}",
                        vendorDetail::getVendorNumber,
                        rejectDocument::getVendorDunsNumber
                );

                rejectDocument.setVendorHeaderGeneratedIdentifier(vendorDetail.getVendorHeaderGeneratedIdentifier());
                rejectDocument.setVendorDetailAssignedIdentifier(vendorDetail.getVendorDetailAssignedIdentifier());
                rejectDocument.setVendorDetail(vendorDetail);
                vendorFound = true;
            }
        }

        if (!vendorFound) {
            rejectDocument.setVendorHeaderGeneratedIdentifier(null);
            rejectDocument.setVendorDetailAssignedIdentifier(null);
            rejectDocument.setVendorDetail(null);
        }

        String newDocumentDesc = generateRejectDocumentDescription(rejectDocument);
        rejectDocument.getDocumentHeader().setDocumentDescription(newDocumentDesc);
    }

    protected Map getItemTypeMappings(Integer vendorHeaderId, Integer vendorDetailId) {
        Map itemTypeMappings = null;

        if (vendorHeaderId != null && vendorDetailId != null) {
            itemTypeMappings = electronicInvoicingDao.getItemMappingMap(vendorHeaderId, vendorDetailId);
        }

        if (itemTypeMappings == null || itemTypeMappings.isEmpty()) {
            itemTypeMappings = electronicInvoicingDao.getDefaultItemMappingMap();
        }

        return itemTypeMappings;
    }

    protected String getVendorNumber(Integer vendorHeaderId, Integer vendorDetailId) {
        if (vendorHeaderId != null && vendorDetailId != null) {
            VendorDetail forVendorNo = new VendorDetail();
            forVendorNo.setVendorHeaderGeneratedIdentifier(vendorHeaderId);
            forVendorNo.setVendorDetailAssignedIdentifier(vendorDetailId);
            return forVendorNo.getVendorNumber();
        } else {
            return null;
        }
    }

    protected Map<String, ItemType> getKualiItemTypes() {
        Collection<ItemType> collection = businessObjectService.findAll(ItemType.class);
        Map kualiItemTypes = new HashMap<String, ItemType>();

        if (collection == null || collection.size() == 0) {
            throw new RuntimeException("Kuali Item types not available");
        } else {
            ItemType[] itemTypes = new ItemType[collection.size()];
            collection.toArray(itemTypes);
            for (ItemType itemType : itemTypes) {
                kualiItemTypes.put(itemType.getItemTypeCode(), itemType);
            }
        }

        return kualiItemTypes;
    }

    protected boolean checkForCompleteFailure(ElectronicInvoiceLoad electronicInvoiceLoad,
            ElectronicInvoice electronicInvoice, File invoiceFile) {
        LOG.debug("checkForCompleteFailure() started");

        if (electronicInvoice.getInvoiceDetailRequestHeader().isHeaderInvoiceIndicator()) {
            rejectElectronicInvoiceFile(electronicInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile,
                    PurapConstants.ElectronicInvoice.HEADER_INVOICE_IND_ON);
            return true;
        }

        if (electronicInvoice.getInvoiceDetailOrders().size() < 1) {
            rejectElectronicInvoiceFile(electronicInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile,
                    PurapConstants.ElectronicInvoice.INVOICE_ORDERS_NOT_FOUND);
            return true;
        }

        for (ElectronicInvoiceOrder invoiceOrder : electronicInvoice.getInvoiceDetailOrders()) {
            for (ElectronicInvoiceItem invoiceItem : invoiceOrder.getInvoiceItems()) {
                if (invoiceItem != null) {
                    invoiceItem.setCatalogNumber(invoiceItem.getReferenceItemIDSupplierPartID());
                }
            }
        }

        LOG.info("No Complete failure");

        return false;
    }

    protected ElectronicInvoiceRejectReasonType getRejectReasonType(String rejectReasonTypeCode) {
        return matchingService.getElectronicInvoiceRejectReasonType(rejectReasonTypeCode);
    }

    protected void rejectElectronicInvoiceFile(ElectronicInvoiceLoad eInvoiceLoad, String fileDunsNumber,
            File filename, String rejectReasonTypeCode) {
        rejectElectronicInvoiceFile(eInvoiceLoad, fileDunsNumber, filename, null, rejectReasonTypeCode);
    }

    protected void rejectElectronicInvoiceFile(ElectronicInvoiceLoad eInvoiceLoad, String fileDunsNumber,
            File invoiceFile, String extraDescription, String rejectReasonTypeCode) {
        LOG.info("Rejecting the entire invoice file - {}", invoiceFile::getName);

        ElectronicInvoiceLoadSummary eInvoiceLoadSummary = getOrCreateLoadSummary(eInvoiceLoad, fileDunsNumber);
        eInvoiceLoadSummary.addFailedInvoiceOrder();
        eInvoiceLoad.insertInvoiceLoadSummary(eInvoiceLoadSummary);

        ElectronicInvoiceRejectDocument eInvoiceRejectDocument;
        eInvoiceRejectDocument = (ElectronicInvoiceRejectDocument) documentService.getNewDocument("EIRT");

        eInvoiceRejectDocument.setInvoiceProcessTimestamp(dateTimeService.getCurrentTimestamp());
        eInvoiceRejectDocument.setVendorDunsNumber(fileDunsNumber);
        eInvoiceRejectDocument.setDocumentCreationInProgress(true);

        if (invoiceFile != null) {
            eInvoiceRejectDocument.setInvoiceFileName(invoiceFile.getName());
        }

        List<ElectronicInvoiceRejectReason> list = new ArrayList<>(1);

        String message = "Complete failure document has been created for the Invoice with Filename '" +
                invoiceFile.getName() + "' due to the following error:\n";
        emailTextErrorList.append(message);

        ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(rejectReasonTypeCode,
                extraDescription, invoiceFile.getName());
        list.add(rejectReason);

        emailTextErrorList.append("    - ").append(rejectReason.getInvoiceRejectReasonDescription());
        emailTextErrorList.append("\n\n");

        eInvoiceRejectDocument.setInvoiceRejectReasons(list);
        eInvoiceRejectDocument.getDocumentHeader().setDocumentDescription("Complete failure");

        documentService.saveDocument(eInvoiceRejectDocument);

        String noteText = "Invoice file";
        attachInvoiceXMLWithRejectDoc(eInvoiceRejectDocument, invoiceFile, noteText);

        eInvoiceLoad.addInvoiceReject(eInvoiceRejectDocument);

        LOG.info("Complete failure document has been created (DocNo:{})", eInvoiceRejectDocument::getDocumentNumber);
    }

    // CU customization: change to protected
    protected void attachInvoiceXMLWithRejectDoc(
            final ElectronicInvoiceRejectDocument eInvoiceRejectDocument,
            final File attachmentFile,
            final String noteText
    ) {
        Note note;
        try {
            note = documentService.createNoteFromDocument(eInvoiceRejectDocument, noteText);
            note.setRemoteObjectIdentifier(eInvoiceRejectDocument.getDocumentHeader().getObjectId());
        } catch (final Exception e1) {
            throw new RuntimeException("Unable to create note from document: ", e1);
        }

        Attachment attachment = null;
        try (BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(attachmentFile))) {
            final String attachmentType = null;
            attachment = attachmentService.createAttachment(eInvoiceRejectDocument.getNoteTarget(),
                    attachmentFile.getName(), INVOICE_FILE_MIME_TYPE, (int) attachmentFile.length(), fileStream,
                    attachmentType);
        } catch (final FileNotFoundException e) {
            LOG.error("Exception opening attachment file", e);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to create attachment", e);
        }

        note.setAttachment(attachment);
        attachment.setNote(note);
        noteService.save(note);
    }

    public ElectronicInvoiceRejectDocument createRejectDocument(ElectronicInvoice eInvoice,
            ElectronicInvoiceOrder electronicInvoiceOrder, ElectronicInvoiceLoad eInvoiceLoad) {
        LOG.info(
                "Creating reject document [DUNS={},POID={}]",
                eInvoice::getDunsNumber,
                electronicInvoiceOrder::getInvoicePurchaseOrderID
        );

        ElectronicInvoiceRejectDocument eInvoiceRejectDocument;

        eInvoiceRejectDocument = (ElectronicInvoiceRejectDocument) documentService.getNewDocument("EIRT");

        eInvoiceRejectDocument.setInvoiceProcessTimestamp(dateTimeService.getCurrentTimestamp());
        String rejectDocDesc = generateRejectDocumentDescription(eInvoice, electronicInvoiceOrder);
        eInvoiceRejectDocument.getDocumentHeader().setDocumentDescription(rejectDocDesc);
        eInvoiceRejectDocument.setDocumentCreationInProgress(true);

        eInvoiceRejectDocument.setFileLevelData(eInvoice);
        eInvoiceRejectDocument.setInvoiceOrderLevelData(eInvoice, electronicInvoiceOrder);

        documentService.saveDocument(eInvoiceRejectDocument);

        String noteText = "Invoice file";
        attachInvoiceXMLWithRejectDoc(eInvoiceRejectDocument, getInvoiceFile(eInvoice.getFileName()), noteText);

        eInvoiceLoad.addInvoiceReject(eInvoiceRejectDocument);

        LOG.info("Reject document has been created (DocNo={})", eInvoiceRejectDocument::getDocumentNumber);

        emailTextErrorList.append("DUNS Number - ").append(eInvoice.getDunsNumber()).append(" ")
                .append(eInvoice.getVendorName()).append(":\n");
        emailTextErrorList.append("An Invoice from file '").append(eInvoice.getFileName())
                .append("' has been rejected due to the following error(s):\n");

        int index = 1;
        for (ElectronicInvoiceRejectReason reason : eInvoiceRejectDocument.getInvoiceRejectReasons()) {
            emailTextErrorList.append("    - ").append(reason.getInvoiceRejectReasonDescription()).append("\n");
            addRejectReasonsToNote("Reject Reason " + index + ". " +
                    reason.getInvoiceRejectReasonDescription(), eInvoiceRejectDocument);
            index++;
        }

        emailTextErrorList.append("\n");

        return eInvoiceRejectDocument;
    }

    protected void addRejectReasonsToNote(String rejectReasons,
            ElectronicInvoiceRejectDocument eInvoiceRejectDocument) {
        try {
            Note note = documentService.createNoteFromDocument(eInvoiceRejectDocument, rejectReasons);
            note.setRemoteObjectIdentifier(eInvoiceRejectDocument.getDocumentHeader().getObjectId());
            noteService.save(note);
        } catch (Exception e) {
            LOG.error("Error creating reject reason note - {}", e::getMessage);
        }
    }

    protected String generateRejectDocumentDescription(ElectronicInvoice eInvoice,
            ElectronicInvoiceOrder electronicInvoiceOrder) {
        String poID = StringUtils.isEmpty(electronicInvoiceOrder.getInvoicePurchaseOrderID()) ?
            "UNKNOWN" :
            electronicInvoiceOrder.getInvoicePurchaseOrderID();

        String vendorName = StringUtils.isEmpty(eInvoice.getVendorName()) ?
            "UNKNOWN" :
            eInvoice.getVendorName();

        String description = "PO: " + poID + " Vendor: " + vendorName;

        return checkDescriptionLengthAndStripIfNeeded(description);
    }

    protected String generateRejectDocumentDescription(ElectronicInvoiceRejectDocument rejectDoc) {
        String poID = StringUtils.isEmpty(rejectDoc.getInvoicePurchaseOrderNumber()) ?
            "UNKNOWN" :
            rejectDoc.getInvoicePurchaseOrderNumber();

        String vendorName = "UNKNOWN";
        if (rejectDoc.getVendorDetail() != null) {
            vendorName = rejectDoc.getVendorDetail().getVendorName();
        }

        String description = "PO: " + poID + " Vendor: " + vendorName;

        return checkDescriptionLengthAndStripIfNeeded(description);
    }

    protected String checkDescriptionLengthAndStripIfNeeded(String description) {
        int noteTextMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class,
                KRADPropertyConstants.DOCUMENT_DESCRIPTION);

        if (noteTextMaxLength < description.length()) {
            description = description.substring(0, noteTextMaxLength);
        }

        return description;
    }

    public ElectronicInvoiceLoadSummary getOrCreateLoadSummary(ElectronicInvoiceLoad eInvoiceLoad,
            String fileDunsNumber) {
        ElectronicInvoiceLoadSummary eInvoiceLoadSummary;

        if (eInvoiceLoad.getInvoiceLoadSummaries().containsKey(fileDunsNumber)) {
            eInvoiceLoadSummary = (ElectronicInvoiceLoadSummary) eInvoiceLoad.getInvoiceLoadSummaries()
                    .get(fileDunsNumber);
        } else {
            eInvoiceLoadSummary = new ElectronicInvoiceLoadSummary(fileDunsNumber);
        }

        return eInvoiceLoadSummary;
    }

    public ElectronicInvoice loadElectronicInvoice(byte[] xmlAsBytes)
        throws CxmlParseException {

        LOG.info("Loading Invoice File");

        ElectronicInvoice electronicInvoice;

        try {
            electronicInvoice = (ElectronicInvoice) batchInputFileService.parse(electronicInvoiceInputFileType,
                    xmlAsBytes);
        } catch (ParseException e) {
            throw new CxmlParseException(e.getMessage());
        }

        LOG.info("Successfully loaded the Invoice File");

        return electronicInvoice;
    }

    protected StringBuffer saveLoadSummary(ElectronicInvoiceLoad eInvoiceLoad) {
        Map savedLoadSummariesMap = new HashMap();
        StringBuffer summaryMessage = new StringBuffer();

        for (Object key : eInvoiceLoad.getInvoiceLoadSummaries().keySet()) {
            String dunsNumber = (String) key;
            ElectronicInvoiceLoadSummary eInvoiceLoadSummary =
                    (ElectronicInvoiceLoadSummary) eInvoiceLoad.getInvoiceLoadSummaries().get(dunsNumber);

            if (!eInvoiceLoadSummary.isEmpty()) {
                LOG.info("Saving Load Summary for DUNS '{}'", dunsNumber);

                ElectronicInvoiceLoadSummary currentLoadSummary =
                        saveElectronicInvoiceLoadSummary(eInvoiceLoadSummary);

                summaryMessage.append("DUNS Number - ").append(eInvoiceLoadSummary.getVendorDescriptor())
                        .append(":\n");
                summaryMessage.append("     ").append(eInvoiceLoadSummary.getInvoiceLoadSuccessCount())
                        .append(" successfully processed invoices for a total of $ ")
                        .append(eInvoiceLoadSummary.getInvoiceLoadSuccessAmount().doubleValue()).append("\n");
                summaryMessage.append("     ").append(eInvoiceLoadSummary.getInvoiceLoadFailCount())
                        .append(" rejected invoices for an approximate total of $ ")
                        .append(eInvoiceLoadSummary.getInvoiceLoadFailAmount().doubleValue()).append("\n");
                summaryMessage.append("\n\n");

                savedLoadSummariesMap.put(currentLoadSummary.getVendorDunsNumber(), eInvoiceLoadSummary);
            } else {
                LOG.info(
                        "Not saving Load Summary for DUNS '{}' because empty indicator is '{}'",
                        () -> dunsNumber,
                        eInvoiceLoadSummary::isEmpty
                );
            }
        }

        summaryMessage.append("\n\n");

        for (Object doc : eInvoiceLoad.getRejectDocuments()) {
            ElectronicInvoiceRejectDocument rejectDoc = (ElectronicInvoiceRejectDocument) doc;
            routeRejectDocument(rejectDoc, savedLoadSummariesMap);
        }

        // Even if there is an exception in the reject doc routing, all the files marked as reject will be moved to
        // the reject dir
        moveFileList(eInvoiceLoad.getRejectFilesToMove());

        return summaryMessage;
    }

    protected void routeRejectDocument(ElectronicInvoiceRejectDocument rejectDoc, Map savedLoadSummariesMap) {
        LOG.info("Saving Invoice Reject for DUNS '{}'", rejectDoc::getVendorDunsNumber);

        if (savedLoadSummariesMap.containsKey(rejectDoc.getVendorDunsNumber())) {
            rejectDoc.setInvoiceLoadSummary((ElectronicInvoiceLoadSummary) savedLoadSummariesMap.get(
                    rejectDoc.getVendorDunsNumber()));
        } else {
            rejectDoc.setInvoiceLoadSummary((ElectronicInvoiceLoadSummary) savedLoadSummariesMap.get(
                    UNKNOWN_DUNS_IDENTIFIER));
        }

        documentService.routeDocument(rejectDoc, "Routed by electronic invoice batch job", null);
    }

    protected void sendSummary(StringBuffer message) {
        String fromMailId = parameterService.getParameterValueAsString(ElectronicInvoiceStep.class,
                PurapParameterConstants.ElectronicInvoiceParameters.DAILY_SUMMARY_REPORT_FROM_EMAIL_ADDRESS);
        List<String> toMailIds = new ArrayList<>(parameterService
                .getParameterValuesAsString(ElectronicInvoiceStep.class,
                        PurapParameterConstants.ElectronicInvoiceParameters.DAILY_SUMMARY_REPORT_TO_EMAIL_ADDRESSES));

        LOG.info("From email address parameter value:{}", fromMailId);
        LOG.info("To email address parameter value:{}", toMailIds);

        if (StringUtils.isBlank(fromMailId) || toMailIds.isEmpty()) {
            LOG.error("From/To mail addresses are empty. Unable to send the message");
        } else {
            BodyMailMessage mailMessage = new BodyMailMessage();

            mailMessage.setFromAddress(fromMailId);
            setMessageToAddressesAndSubject(mailMessage, toMailIds);
            mailMessage.setMessage(message.toString());

            try {
                emailService.sendMessage(mailMessage, false);
            } catch (Exception e) {
                LOG.error("Invalid email address. Message not sent", e);
            }
        }
    }

    protected BodyMailMessage setMessageToAddressesAndSubject(BodyMailMessage message, List<String> toAddressList) {
        if (!toAddressList.isEmpty()) {
            for (String aToAddressList : toAddressList) {
                if (StringUtils.isNotEmpty(aToAddressList)) {
                    message.addToAddress(aToAddressList.trim());
                }
            }
        }

        String mailTitle = "E-Invoice Load Results for " + ElectronicInvoiceUtils.getDateDisplayText(
                dateTimeService.getCurrentDate());

        message.setSubject(mailTitle);
        return message;
    }

    /**
     * This method is responsible for the matching process for a reject document
     *
     * @return true if the matching process is succeed
     */
    @Override
    public boolean doMatchingProcess(ElectronicInvoiceRejectDocument rejectDocument) {
        // This is needed here since if the user changes the DUNS number.
        validateVendorDetails(rejectDocument);

        Map itemTypeMappings = getItemTypeMappings(rejectDocument.getVendorHeaderGeneratedIdentifier(),
                rejectDocument.getVendorDetailAssignedIdentifier());

        Map kualiItemTypes = getKualiItemTypes();

        ElectronicInvoiceOrderHolder rejectDocHolder = new ElectronicInvoiceOrderHolder(rejectDocument,
                itemTypeMappings, kualiItemTypes);
        matchingService.doMatchingProcess(rejectDocHolder);

        // Once we're through with the matching process, it's needed to check whether it's possible to create PREQ
        // for the reject doc
        if (!rejectDocHolder.isInvoiceRejected()) {
            validateInvoiceOrderValidForPREQCreation(rejectDocHolder);
        }

        //  determine which of the reject reasons we should suppress based on the parameter
        List<String> ignoreRejectTypes = new ArrayList<>(parameterService
                .getParameterValuesAsString(PurapConstants.PURAP_NAMESPACE, "ElectronicInvoiceReject",
                        "SUPPRESS_REJECT_REASON_CODES_ON_EIRT_APPROVAL"));
        List<ElectronicInvoiceRejectReason> rejectReasonsToDelete = new ArrayList<>();
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

    @Override
    public boolean createPaymentRequest(ElectronicInvoiceRejectDocument rejectDocument) {
        if (rejectDocument.getInvoiceRejectReasons().size() > 0) {
            throw new RuntimeException("Not possible to create payment request since the reject document contains " +
                    rejectDocument.getInvoiceRejectReasons().size() + " rejects");
        }

        Map itemTypeMappings = getItemTypeMappings(rejectDocument.getVendorHeaderGeneratedIdentifier(),
                rejectDocument.getVendorDetailAssignedIdentifier());

        Map kualiItemTypes = getKualiItemTypes();

        ElectronicInvoiceOrderHolder rejectDocHolder = new ElectronicInvoiceOrderHolder(rejectDocument,
                itemTypeMappings, kualiItemTypes);

        // First, create a new payment request document. Once this document is created, then update the reject
        // document's PREQ_ID field with the payment request document identifier. This identifier is used to associate
        // the reject document with the payment request.
        PaymentRequestDocument preqDocument = createPaymentRequest(rejectDocHolder);
        rejectDocument.setPaymentRequestIdentifier(preqDocument.getPurapDocumentIdentifier());

        return !rejectDocHolder.isInvoiceRejected();
    }

    protected PaymentRequestDocument createPaymentRequest(ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Creating Payment Request document");

        KNSGlobalVariables.getMessageList().clear();

        validateInvoiceOrderValidForPREQCreation(orderHolder);

        if (orderHolder.isInvoiceRejected()) {
            LOG.info("Not possible to convert einvoice details into payment request");
        } else {
            LOG.info("Payment request document creation validation succeeded");
        }

        if (orderHolder.isInvoiceRejected()) {
            return null;
        }

        PaymentRequestDocument preqDoc;
        preqDoc = (PaymentRequestDocument) documentService.getNewDocument("PREQ");

        PurchaseOrderDocument poDoc = orderHolder.getPurchaseOrderDocument();
        if (poDoc == null) {
            throw new RuntimeException("Purchase Order document (POId=" + poDoc.getPurapDocumentIdentifier() +
                    ") does not exist in the system");
        }

        preqDoc.getDocumentHeader().setDocumentDescription(generatePREQDocumentDescription(poDoc));
        preqDoc.updateAndSaveAppDocStatus(PaymentRequestStatuses.APPDOC_IN_PROCESS);

        preqDoc.setInvoiceDate(orderHolder.getInvoiceDate());
        preqDoc.setInvoiceNumber(orderHolder.getInvoiceNumber());
        preqDoc.setVendorInvoiceAmount(new KualiDecimal(orderHolder.getInvoiceNetAmount()));
        preqDoc.setAccountsPayableProcessorIdentifier("E-Invoice");
        preqDoc.setVendorCustomerNumber(orderHolder.getCustomerNumber());
        preqDoc.setPaymentRequestElectronicInvoiceIndicator(true);

        if (orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier() != null) {
            preqDoc.setAccountsPayablePurchasingDocumentLinkIdentifier(
                    orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
        }

        // Copied from PaymentRequestServiceImpl.populatePaymentRequest() set bank code to default bank code in the
        // system parameter
        Bank defaultBank = bankService.getDefaultBankByDocType(preqDoc.getClass());
        if (defaultBank != null) {
            preqDoc.setBankCode(defaultBank.getBankCode());
            preqDoc.setBank(defaultBank);
        }

        RequisitionDocument reqDoc = requisitionService.getRequisitionById(poDoc.getRequisitionIdentifier());
        String reqDocInitiator = reqDoc.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        try {
            Person user = personService.getPerson(reqDocInitiator);

            setProcessingCampus(preqDoc, user.getCampusCode());

        } catch (Exception e) {
            String extraDescription = "Error setting processing campus code - " + e.getMessage();
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(
                    PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, extraDescription,
                    orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }

        HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList =
                accountsPayableService.expiredOrClosedAccountsList(poDoc);
        if (expiredOrClosedAccountList == null) {
            expiredOrClosedAccountList = new HashMap<>();
        }

        LOG.info("{} accounts has been found as Expired or Closed", expiredOrClosedAccountList::size);

        preqDoc.populatePaymentRequestFromPurchaseOrder(orderHolder.getPurchaseOrderDocument(),
                expiredOrClosedAccountList);

        populateItemDetails(preqDoc, orderHolder);

        // Validate totals, paydate
        kualiRuleService.applyRules(new AttributedCalculateAccountsPayableEvent(preqDoc));

        paymentRequestService.calculatePaymentRequest(preqDoc, true);

        processItemsForDiscount(preqDoc, orderHolder);

        if (orderHolder.isInvoiceRejected()) {
            return null;
        }

        paymentRequestService.calculatePaymentRequest(preqDoc, false);
        // PaymentRequestReview
        kualiRuleService.applyRules(new AttributedPaymentRequestForEInvoiceEvent(preqDoc));

        if (GlobalVariables.getMessageMap().hasErrors()) {
            LOG.info("***************Error in rules processing - {}", GlobalVariables::getMessageMap);

            Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap()
                    .getErrorMessages();

            String errors = errorMessages.toString();
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(
                    PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, errors, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }

        if (KNSGlobalVariables.getMessageList().size() > 0) {
            LOG.info(
                    "Payment request contains {} warning message(s)",
                    () -> KNSGlobalVariables.getMessageList().size()
            );
            for (int i = 0; i < KNSGlobalVariables.getMessageList().size(); i++) {
                final ErrorMessage errorMessage = KNSGlobalVariables.getMessageList().get(i);
                LOG.info("Warning {}  - {}", i, errorMessage);
            }
        }

        addShipToNotes(preqDoc, orderHolder);

        String routingAnnotation = null;
        if (!orderHolder.isRejectDocumentHolder()) {
            routingAnnotation = "Routed by electronic invoice batch job";
        }

        try {
            documentService.routeDocument(preqDoc, routingAnnotation, null);
        } catch (ValidationException e) {
            String extraDescription = GlobalVariables.getMessageMap().toString();
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(
                    PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, extraDescription,
                    orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }

        return preqDoc;
    }

    /**
     * This method check OVERRIDE_PROCESSING_CAMPUS parameter to set processing campus code. If parameter value is
     * populated, it set the processing campus to the value in parameter, otherwise use requisition initiator's
     * campus code.
     *
     * @param preqDoc
     * @param initiatorCampusCode
     */
    protected void setProcessingCampus(PaymentRequestDocument preqDoc, String initiatorCampusCode) {
        String campusCode = parameterService.getParameterValueAsString(ElectronicInvoiceStep.class,
                PurapParameterConstants.ElectronicInvoiceParameters.OVERRIDE_PROCESSING_CAMPUS);
        if (!StringHelper.isNullOrEmpty(campusCode)) {
            preqDoc.setProcessingCampusCode(campusCode);
        } else {
            preqDoc.setProcessingCampusCode(initiatorCampusCode);
        }
    }

    protected void addShipToNotes(PaymentRequestDocument preqDoc, ElectronicInvoiceOrderHolder orderHolder) {
        String shipToAddress = orderHolder.getInvoiceShipToAddressAsString();

        try {
            Note noteObj = documentService.createNoteFromDocument(preqDoc, shipToAddress);
            preqDoc.addNote(noteObj);
        } catch (Exception e) {
            LOG.error("Error creating ShipTo notes - {}", e::getMessage);
        }
    }

    protected void processItemsForDiscount(PaymentRequestDocument preqDocument,
            ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Processing payment request items for discount");

        if (!orderHolder.isItemTypeAvailableInItemMapping(ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT)) {
            LOG.info("Skipping discount processing since there is no mapping of discount type for this vendor");
            return;
        }

        if (orderHolder.getInvoiceDiscountAmount() == null ||
            orderHolder.getInvoiceDiscountAmount() == BigDecimal.ZERO) {
            LOG.info("Skipping discount processing since there is no discount amount found in the invoice file");

            return;
        }

        KualiDecimal discountValueToUse = new KualiDecimal(orderHolder.getInvoiceDiscountAmount().negate());
        List<PaymentRequestItem> preqItems = preqDocument.getItems();

        boolean alreadyProcessedInvoiceDiscount = false;
        boolean hasKualiPaymentTermsDiscountItem = false;

        //if e-invoice amount is negative... it is a penalty and we must pay extra
        for (PaymentRequestItem preqItem : preqItems) {
            hasKualiPaymentTermsDiscountItem = hasKualiPaymentTermsDiscountItem
                                               || StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE,
                                                   preqItem.getItemTypeCode());

            if (isItemValidForUpdation(preqItem.getItemTypeCode(),
                    ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT, orderHolder)) {

                alreadyProcessedInvoiceDiscount = true;

                if (StringUtils.equals(preqItem.getItemTypeCode(),
                        PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)) {
                    // Item is kuali payment terms discount item... must perform calculation if discount item exists
                    // on PREQ and discount dollar amount exists... use greater amount
                    LOG.info(
                            "Discount Check - E-Invoice matches PREQ item type '{}'... now checking for amount",
                            preqItem::getItemTypeCode
                    );

                    KualiDecimal preqExtendedPrice =
                            preqItem.getExtendedPrice() == null ? KualiDecimal.ZERO : preqItem.getExtendedPrice();
                    if (discountValueToUse.compareTo(preqExtendedPrice) < 0) {
                        LOG.info(
                                "Discount Check - Using E-Invoice amount ({}) as it is more discount than current payment terms amount {}",
                                discountValueToUse,
                                preqExtendedPrice
                        );

                        preqItem.setItemUnitPrice(discountValueToUse.bigDecimalValue());
                        preqItem.setExtendedPrice(discountValueToUse);
                    }
                } else {
                    // item is not payment terms discount item... just add value; if discount item exists on PREQ and
                    // discount dollar amount exists... use greater amount
                    LOG.info("Discount Check - E-Invoice matches PREQ item type '{}'", preqItem::getItemTypeCode);
                    LOG.info(
                            "Discount Check - Using E-Invoice amount ({}) as it is greater than payment terms amount",
                            discountValueToUse
                    );

                    preqItem.addToUnitPrice(discountValueToUse.bigDecimalValue());
                    preqItem.addToExtendedPrice(discountValueToUse);
                }
            }
        }

        // If we have not already processed the discount amount then the mapping is pointed to an item that is not in
        // the PREQ item list FYI - FILE DISCOUNT AMOUNT CURRENTLY HARD CODED TO GO INTO PAYMENT TERMS DISCOUNT ITEM
        // ONLY... ALL OTHERS WILL FAIL
        if (!alreadyProcessedInvoiceDiscount) {
            // if we already have a PMT TERMS DISC item but the e-invoice discount wasn't processed... error out
            // if the item mapping for e-invoice discount item is not PMT TERMS DISC item and we haven't processed
            // it... error out
            if (hasKualiPaymentTermsDiscountItem
                    || !orderHolder.isItemTypeAvailableInItemMapping(
                            ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT)) {
                ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(
                        PurapConstants.ElectronicInvoice.PREQ_DISCOUNT_ERROR, null, orderHolder.getFileName());
                orderHolder.addInvoiceOrderRejectReason(rejectReason);
                return;
            } else {
                PaymentRequestItem newItem = new PaymentRequestItem();
                newItem.setItemUnitPrice(discountValueToUse.bigDecimalValue());
                newItem.setItemTypeCode(PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE);
                newItem.setExtendedPrice(discountValueToUse);
                newItem.setPurapDocument(preqDocument);
                preqDocument.addItem(newItem);
            }
        }

        LOG.info("Completed processing payment request items for discount");
    }

    protected void populateItemDetails(PaymentRequestDocument preqDocument, ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Populating invoice order items into the payment request document");

        List<PurApItem> preqItems = preqDocument.getItems();

        //process all preq items and apply amounts from order holder
        for (PurApItem item : preqItems) {
            PaymentRequestItem preqItem = (PaymentRequestItem) item;
            processInvoiceItem(preqItem, orderHolder);
        }

        // Now we'll add any missing mapping items that did not have an existing payment request item
        addMissingMappedItems(preqItems, orderHolder);

        //as part of a clean up, remove any preq items that have zero or null unit/extended price
        removeEmptyItems(preqItems);

        LOG.info("Successfully populated the invoice order items");
    }

    /**
     * Removes preq items from the list that have null or zero unit and extended price
     *
     * @param preqItems
     */
    protected void removeEmptyItems(List<PurApItem> preqItems) {
        for (int i = preqItems.size() - 1; i >= 0; i--) {
            PurApItem item = preqItems.get(i);

            //if the unit and extended price have null or zero as a combo, remove item
            if (isNullOrZero(item.getItemUnitPrice()) && isNullOrZero(item.getExtendedPrice())) {
                preqItems.remove(i);
            }
        }
    }

    /**
     * Ensures that the mapped items, item type code, exist as a payment request item so they're process correctly
     * within populateItemDetails
     *
     * @param preqItems
     * @param orderHolder
     */
    protected void addMissingMappedItems(List<PurApItem> preqItems, ElectronicInvoiceOrderHolder orderHolder) {
        PurchasingAccountsPayableDocument purapDoc = null;
        Integer purapDocIdentifier = null;

        //grab all the required item types that should be on the payment request
        List requiredItemTypeCodeList = createInvoiceRequiresItemTypeCodeList(orderHolder);

        if (ObjectUtils.isNotNull(requiredItemTypeCodeList) && !requiredItemTypeCodeList.isEmpty()) {
            //loop through existing payment request items and remove ones we already have
            for (PurApItem item : preqItems) {
                //if the preq item exists in the list already, remove
                requiredItemTypeCodeList.remove(item.getItemTypeCode());

                //utility grab the document identifier and document
                purapDoc = item.getPurapDocument();
                purapDocIdentifier = item.getPurapDocumentIdentifier();
            }

            if (ObjectUtils.isNotNull(requiredItemTypeCodeList) && !requiredItemTypeCodeList.isEmpty()) {
                //if we have any left, it means they didn't exist on the payment request and we must add them.
                for (Object requiredItemTypeCode : requiredItemTypeCodeList) {
                    PaymentRequestItem preqItem = new PaymentRequestItem();
                    preqItem.resetAccount();
                    preqItem.setPurapDocumentIdentifier(purapDocIdentifier);
                    preqItem.setPurapDocument(purapDoc);
                    preqItem.setItemTypeCode((String) requiredItemTypeCode);

                    processInvoiceItem(preqItem, orderHolder);

                    //Add to preq Items if the value is not zero
                    if (ObjectUtils.isNotNull(preqItem.getItemUnitPrice())
                            && preqItem.getItemUnitPrice() != BigDecimal.ZERO
                            && ObjectUtils.isNotNull(preqItem.getExtendedPrice())
                            && preqItem.getExtendedPrice() != KualiDecimal.ZERO) {
                        preqItems.add(preqItem);
                    }
                }
            }
        }
    }

    /**
     * Creates a list of item types the eInvoice requires on the payment request due to valid amounts.
     */
    protected List createInvoiceRequiresItemTypeCodeList(ElectronicInvoiceOrderHolder orderHolder) {
        List itemTypeCodeList = new ArrayList();
        addToListIfExists(itemTypeCodeList, ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_TAX, orderHolder);
        addToListIfExists(itemTypeCodeList, ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_SHIPPING, orderHolder);
        addToListIfExists(itemTypeCodeList, ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_SPECIAL_HANDLING, orderHolder);
        addToListIfExists(itemTypeCodeList, ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DEPOSIT, orderHolder);
        addToListIfExists(itemTypeCodeList, ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DUE, orderHolder);
        addToListIfExists(itemTypeCodeList, ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT, orderHolder);
        return itemTypeCodeList;
    }

    /**
     * Utility method to add a kuali item type code to a list from a invoice item type code
     *
     * @param itemTypeCodeList
     * @param invoiceItemTypeCode
     * @param orderHolder
     */
    protected void addToListIfExists(List itemTypeCodeList, String invoiceItemTypeCode,
            ElectronicInvoiceOrderHolder orderHolder) {
        String itemTypeCode = orderHolder.getKualiItemTypeCodeFromMappings(invoiceItemTypeCode);

        if (ObjectUtils.isNotNull(itemTypeCode)) {
            itemTypeCodeList.add(itemTypeCode);
        }
    }

    /**
     * Finds the mapped item type code to invoice item type code and applies the appropriate values to the correct
     * payment request item.
     *
     * @param preqItem
     * @param orderHolder
     */
    protected void processInvoiceItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder) {
        if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_ITEM,
                orderHolder)) {
            processAboveTheLineItem(preqItem, orderHolder);
        } else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_TAX,
                orderHolder)) {
            processTaxItem(preqItem, orderHolder);
        } else if (isItemValidForUpdation(preqItem.getItemTypeCode(),
                ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_SHIPPING, orderHolder)) {
            processShippingItem(preqItem, orderHolder);
        } else if (isItemValidForUpdation(preqItem.getItemTypeCode(),
                ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_SPECIAL_HANDLING, orderHolder)) {
            processSpecialHandlingItem(preqItem, orderHolder);
        } else if (isItemValidForUpdation(preqItem.getItemTypeCode(),
                ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DEPOSIT, orderHolder)) {
            processDepositItem(preqItem, orderHolder);
        } else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DUE,
                orderHolder)) {
            processDueItem(preqItem, orderHolder);
        } else if (isItemValidForUpdation(preqItem.getItemTypeCode(),
                ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT, orderHolder)) {
            processDiscountItem(preqItem, orderHolder);
        } else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_EXMT,
                orderHolder)) {
            processAboveTheLineItem(preqItem, orderHolder);
        }
    }

    protected void processAboveTheLineItem(PaymentRequestItem purapItem, ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Processing above the line item");

        ElectronicInvoiceItemHolder itemHolder = orderHolder.getItemByLineNumber(purapItem.getItemLineNumber());
        if (itemHolder == null) {
            LOG.info(
                    "Electronic Invoice does not have item with Ref Item Line number {}",
                    purapItem::getItemLineNumber
            );
            return;
        }

        purapItem.setItemUnitPrice(itemHolder.getInvoiceItemUnitPrice());
        purapItem.setItemQuantity(new KualiDecimal(itemHolder.getInvoiceItemQuantity()));
        purapItem.setItemTaxAmount(new KualiDecimal(itemHolder.getTaxAmount()));
        purapItem.setItemCatalogNumber(itemHolder.getInvoiceItemCatalogNumber());
        purapItem.setItemDescription(itemHolder.getInvoiceItemDescription());

        if (itemHolder.getSubTotalAmount() != null
                && itemHolder.getSubTotalAmount().compareTo(KualiDecimal.ZERO) != 0) {
            purapItem.setExtendedPrice(itemHolder.getSubTotalAmount());
        } else {
            if (purapItem.getItemQuantity() != null) {
                LOG.info(
                        "Item number {} needs calculation of extended price from quantity {} and unit cost {}",
                        purapItem::getItemLineNumber,
                        purapItem::getItemQuantity,
                        purapItem::getItemUnitPrice
                );
                purapItem.setExtendedPrice(purapItem.getItemQuantity()
                        .multiply(new KualiDecimal(purapItem.getItemUnitPrice())));
            } else {
                LOG.info(
                        "Item number {} has no quantity so extended price equals unit price of {}",
                        purapItem::getItemLineNumber,
                        purapItem::getItemUnitPrice
                );
                purapItem.setExtendedPrice(new KualiDecimal(purapItem.getItemUnitPrice()));
            }
        }
    }

    protected void processSpecialHandlingItem(PaymentRequestItem purapItem, ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Processing special handling item");

        purapItem.addToUnitPrice(orderHolder.getInvoiceSpecialHandlingAmount());
        purapItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceSpecialHandlingAmount()));

        String invoiceSpecialHandlingDescription = orderHolder.getInvoiceSpecialHandlingDescription();

        if (invoiceSpecialHandlingDescription == null && orderHolder.getInvoiceSpecialHandlingAmount() != null
            && BigDecimal.ZERO.compareTo(orderHolder.getInvoiceSpecialHandlingAmount()) != 0) {
            invoiceSpecialHandlingDescription = PurapConstants.ElectronicInvoice.DEFAULT_SPECIAL_HANDLING_DESCRIPTION;
        }
        if (StringUtils.isNotEmpty(invoiceSpecialHandlingDescription)) {
            if (StringUtils.isEmpty(purapItem.getItemDescription())) {
                purapItem.setItemDescription(invoiceSpecialHandlingDescription);
            } else {
                purapItem.setItemDescription(purapItem.getItemDescription() + " - " +
                        invoiceSpecialHandlingDescription);
            }
        }
    }

    protected void processTaxItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Processing Tax Item");

        preqItem.addToUnitPrice(orderHolder.getTaxAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getTaxAmount()));

        if (StringUtils.isNotEmpty(orderHolder.getTaxDescription())) {
            if (StringUtils.isEmpty(preqItem.getItemDescription())) {
                preqItem.setItemDescription(orderHolder.getTaxDescription());
            } else {
                preqItem.setItemDescription(preqItem.getItemDescription() + " - " + orderHolder.getTaxDescription());
            }
        }
    }

    protected void processShippingItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Processing Shipping Item");

        preqItem.addToUnitPrice(orderHolder.getInvoiceShippingAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceShippingAmount()));

        if (StringUtils.isNotEmpty(orderHolder.getInvoiceShippingDescription())) {
            if (StringUtils.isEmpty(preqItem.getItemDescription())) {
                preqItem.setItemDescription(orderHolder.getInvoiceShippingDescription());
            } else {
                preqItem.setItemDescription(preqItem.getItemDescription() + " - " +
                        orderHolder.getInvoiceShippingDescription());
            }
        }
    }

    protected void processDiscountItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Processing Discount Item");
        preqItem.addToUnitPrice(orderHolder.getInvoiceDiscountAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceDiscountAmount()));
    }

    protected void processDepositItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Processing Deposit Item");
        preqItem.addToUnitPrice(orderHolder.getInvoiceDepositAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceDepositAmount()));
    }

    protected void processDueItem(PaymentRequestItem preqItem, ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Processing Deposit Item");
        preqItem.addToUnitPrice(orderHolder.getInvoiceDueAmount());
        preqItem.addToExtendedPrice(new KualiDecimal(orderHolder.getInvoiceDueAmount()));
    }

    protected boolean isNullOrZero(BigDecimal value) {
        return ObjectUtils.isNull(value) || value.compareTo(BigDecimal.ZERO) == 0;
    }

    protected boolean isNullOrZero(KualiDecimal value) {
        return ObjectUtils.isNull(value) || value.isZero();
    }

    protected void setItemDefaultDescription(PaymentRequestItem preqItem) {
        //If description is empty and item is not type "ITEM"... use default description
        if (StringUtils.isEmpty(preqItem.getItemDescription())
                && !StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE, preqItem.getItemTypeCode())) {
            if (ArrayUtils.contains(PurapConstants.ElectronicInvoice.ITEM_TYPES_REQUIRES_DESCRIPTION,
                    preqItem.getItemTypeCode())) {
                preqItem.setItemDescription(PurapConstants.ElectronicInvoice.DEFAULT_BELOW_LINE_ITEM_DESCRIPTION);
            }
        }
    }

    protected boolean isItemValidForUpdation(String itemTypeCode, String invoiceItemTypeCode,
            ElectronicInvoiceOrderHolder orderHolder) {
        boolean isItemTypeAvailableInItemMapping = orderHolder.isItemTypeAvailableInItemMapping(invoiceItemTypeCode);
        String itemTypeCodeFromMappings = orderHolder.getKualiItemTypeCodeFromMappings(invoiceItemTypeCode);
        return isItemTypeAvailableInItemMapping && StringUtils.equals(itemTypeCodeFromMappings, itemTypeCode);
    }

    protected String generatePREQDocumentDescription(PurchaseOrderDocument poDocument) {
        String description = "PO: " + poDocument.getPurapDocumentIdentifier() + " Vendor: " +
                poDocument.getVendorName() + " Electronic Invoice";
        return checkDescriptionLengthAndStripIfNeeded(description);
    }

    /**
     * This validates an electronic invoice and makes sure it can be turned into a Payment Request
     */
    public void validateInvoiceOrderValidForPREQCreation(ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Validating ElectronicInvoice Order to make sure that it can be turned into a Payment " +
                "Request document");

        PurchaseOrderDocument poDoc = orderHolder.getPurchaseOrderDocument();

        if (poDoc == null) {
            throw new RuntimeException("PurchaseOrder not available");
        }

        if (!orderHolder.isInvoiceNumberAcceptIndicatorEnabled()) {
            List preqs = paymentRequestService.getPaymentRequestsByVendorNumberInvoiceNumber(
                    poDoc.getVendorHeaderGeneratedIdentifier(),
                poDoc.getVendorDetailAssignedIdentifier(),
                orderHolder.getInvoiceNumber());

            if (preqs != null && preqs.size() > 0) {
                ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(
                        PurapConstants.ElectronicInvoice.INVOICE_ORDER_DUPLICATE, null, orderHolder.getFileName());
                orderHolder.addInvoiceOrderRejectReason(rejectReason,
                        PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_FILE_NUMBER,
                        PurapKeyConstants.ERROR_REJECT_INVOICE_DUPLICATE);
                return;
            }
        }

        if (orderHolder.getInvoiceDate() == null) {
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(
                    PurapConstants.ElectronicInvoice.INVOICE_DATE_INVALID, null, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,
                    PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_FILE_DATE,
                    PurapKeyConstants.ERROR_REJECT_INVOICE_DATE_INVALID);
        } else if (orderHolder.getInvoiceDate().after(dateTimeService.getCurrentDate())) {
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(
                    PurapConstants.ElectronicInvoice.INVOICE_DATE_GREATER, null, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason,
                    PurapConstants.ElectronicInvoice.RejectDocumentFields.INVOICE_FILE_DATE,
                    PurapKeyConstants.ERROR_REJECT_INVOICE_DATE_GREATER);
        }
    }

    protected void moveFileList(Map filesToMove) {
        for (Object file : filesToMove.keySet()) {
            File fileToMove = (File) file;

            boolean success = this.moveFile(fileToMove, (String) filesToMove.get(fileToMove));
            if (!success) {
                String errorMessage = "File with name '" + fileToMove.getName() + "' could not be moved";
                throw new PurError(errorMessage);
            }
        }
    }

    protected boolean moveFile(File fileForMove, String location) {
        File moveDir = new File(location);
        return fileForMove.renameTo(new File(moveDir, fileForMove.getName()));
    }

    protected void deleteDoneFile(File invoiceFile) {
        File doneFile = new File(invoiceFile.getAbsolutePath()
                .replace(electronicInvoiceInputFileType.getFileExtension(), ".done"));
        if (doneFile.exists()) {
            doneFile.delete();
        }
    }

    /**
     * @param errorMap
     * @return a list of all error messages as a string
     */
    protected String getErrorMessages(Map<String, ArrayList> errorMap) {
        ArrayList errorMessages;
        StringBuffer errorList = new StringBuffer();
        String errorText;

        for (Map.Entry<String, ArrayList> errorEntry : errorMap.entrySet()) {
            errorMessages = errorEntry.getValue();

            for (Object message : errorMessages) {
                ErrorMessage errorMessage = (ErrorMessage) message;

                // get error text
                errorText = kualiConfigurationService.getPropertyValueAsString(errorMessage.getErrorKey());
                // apply parameters
                errorText = MessageFormat.format(errorText, (Object[]) errorMessage.getMessageParameters());

                // add key and error message together
                errorList.append(errorText).append("\n");
            }
        }

        return errorList.toString();
    }

    protected String getBaseDirName() {
        return electronicInvoiceInputFileType.getDirectoryPath() + File.separator;
    }

    public String getRejectDirName() {
        return getBaseDirName() + "reject" + File.separator;
    }

    public String getAcceptDirName() {
        return getBaseDirName() + "accept" + File.separator;
    }

    protected File getInvoiceFile(String fileName) {
        return new File(getBaseDirName() + fileName);
    }

    protected ElectronicInvoiceLoadSummary saveElectronicInvoiceLoadSummary(ElectronicInvoiceLoadSummary eils) {
        businessObjectService.save(eils);
        eils.refreshNonUpdateableReferences();
        return eils;
    }

    @Override
    public List<String> getRequiredDirectoryNames() {
        return List.of(getBaseDirName(), getAcceptDirName(), getRejectDirName());
    }

    public void setElectronicInvoiceInputFileType(ElectronicInvoiceInputFileType electronicInvoiceInputFileType) {
        this.electronicInvoiceInputFileType = electronicInvoiceInputFileType;
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

    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

    public void setKualiRuleService(KualiRuleService kualiRuleService) {
        this.kualiRuleService = kualiRuleService;
    }

    // known user: Cornell
    protected BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    // known user: Cornell
    protected DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    // known user: Cornell
    protected RequisitionService getRequisitionService() {
        return requisitionService;
    }

    public void setRequisitionService(RequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    // known user: Cornell
    protected AccountsPayableService getAccountsPayableService() {
        return accountsPayableService;
    }

    public void setAccountsPayableService(AccountsPayableService accountsPayableService) {
        this.accountsPayableService = accountsPayableService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }
}
