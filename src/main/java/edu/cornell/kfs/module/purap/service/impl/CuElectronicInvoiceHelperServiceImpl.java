package edu.cornell.kfs.module.purap.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.gl.service.impl.StringHelper;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResult;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResults;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.batch.ElectronicInvoiceStep;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoice;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItem;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItemMapping;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoad;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoadSummary;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceOrder;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectReason;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.validation.event.AttributedCalculateAccountsPayableEvent;
import org.kuali.kfs.module.purap.document.validation.event.AttributedPaymentRequestForEInvoiceEvent;
import org.kuali.kfs.module.purap.exception.CxmlParseException;
import org.kuali.kfs.module.purap.exception.PurError;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceHelperServiceImpl;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceItemHolder;
import org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceOrderHolder;
import org.kuali.kfs.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.businessobject.CuPaymentRequestItemExtension;
import edu.cornell.kfs.module.purap.document.CuElectronicInvoiceRejectDocument;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.service.CuElectronicInvoiceHelperService;
import edu.cornell.kfs.sys.document.service.CUFinancialSystemDocumentService;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class CuElectronicInvoiceHelperServiceImpl extends ElectronicInvoiceHelperServiceImpl implements CuElectronicInvoiceHelperService {
    private static final Logger LOG = LogManager.getLogger();
    private StringBuffer emailTextErrorList;
    private HashMap<String, Integer> loadCounts = new HashMap<String, Integer>();
    private static final String EXTRACT_FAILURES = "Extract Failures";
    private static final String REJECT = "Reject";
    private static final String ACCEPT = "Accept";
	private static final int NOTE_TEXT_DEFAULT_MAX_LENGTH = 800;
	private static final String XMLNS_ATTRIBUTE_PREFIX = "xmlns";
	private static final Pattern ALLOWED_XMLNS_ATTRIBUTES_PATTERN = Pattern.compile("^xmlns(:xsi)?$");
	
	private WorkflowDocumentService workflowDocumentService;
	private CUFinancialSystemDocumentService financialSystemDocumentService;
	private CUPaymentMethodGeneralLedgerPendingEntryService cUPaymentMethodGeneralLedgerPendingEntryService;

	@Override
    public ElectronicInvoiceLoad loadElectronicInvoices() {
        LOG.debug("loadElectronicInvoices() started");

        //add a step to check for directory paths
        prepareDirectories(getRequiredDirectoryNames());

        final String rejectDirName = getRejectDirName();
        final String acceptDirName = getAcceptDirName();
        final String extractFailureDirName = getExtractFailureDirName();
        emailTextErrorList = new StringBuffer();

        int failedCnt = 0;

        LOG.info("Invoice Base Directory - {}", electronicInvoiceInputFileType::getDirectoryPath);
        LOG.info("Invoice Accept Directory - {}", acceptDirName);
        LOG.info("Invoice Reject Directory - {}", rejectDirName);

        if (StringUtils.isBlank(rejectDirName)) {
            throw new RuntimeException("Reject directory name should not be empty");
        }

        if (StringUtils.isBlank(acceptDirName)) {
            throw new RuntimeException("Accept directory name should not be empty");
        }
        if (StringUtils.isBlank(extractFailureDirName)) {
            throw new RuntimeException("ExtractFailure directory name should not be empty");
        }


        final File[] filesToBeProcessed = getFilesToBeProcessed();
        final ElectronicInvoiceLoad eInvoiceLoad = new ElectronicInvoiceLoad();

        if (filesToBeProcessed == null ||
            filesToBeProcessed.length == 0) {

            final StringBuffer mailText = new StringBuffer();

            mailText.append("\n\n");
            mailText.append(PurapConstants.ElectronicInvoice.NO_FILES_PROCESSED_EMAIL_MESSAGE);
            mailText.append("\n\n");

            sendSummary(mailText);
            return eInvoiceLoad;
        }

        try {
            FileUtils.forceMkdir(new File(acceptDirName));
            FileUtils.forceMkdir(new File(rejectDirName));
            FileUtils.forceMkdir(new File(extractFailureDirName));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        LOG.info("{} file(s) available for processing", filesToBeProcessed.length);
        
        final StringBuilder emailMsg = new StringBuilder();

        for (File xmlFile : filesToBeProcessed) {

            LOG.info("Processing {}....", xmlFile::getName);

            byte[] modifiedXML = addNamespaceDefinition(eInvoiceLoad, xmlFile);
            
            try {
                processElectronicInvoice(eInvoiceLoad, xmlFile, modifiedXML);
            } catch (final Exception e) {
                String msg = xmlFile.getName() + "\n";
                LOG.error(msg, e);

                //since getMessage() is empty we'll compose the stack trace and nicely format it.
                final StackTraceElement[] elements = e.getStackTrace();
                final StringBuffer trace = new StringBuffer();
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
                // KFSUPGRADE-480, KFSUPGRADE-484 : Cu channged email message, the failCnt and emailMsg are not referenced
                // in CU's email.  So, this is not critical.  But may be good to see these logs.
                emailMsg.append(msg);
                msg += "\n--------------------------------------------------------------------------------------\n" + trace;
                logProcessElectronicInvoiceError(msg);
                failedCnt++;
                // one of the scenario is that save EIRT failed vecause of validation failed.  So, no EIRT will
                // be created.
                boolean moveFiles = BooleanUtils.toBoolean(parameterService.getParameterValueAsString(ElectronicInvoiceStep.class, PurapParameterConstants.ElectronicInvoiceParameters.FILE_MOVE_AFTER_LOAD_IND));
                if(moveFiles) {
        	            if (LOG.isInfoEnabled()) {
        	                LOG.info(xmlFile.getName() + " has caused by saving EIRT failure.");
        	            }
        	        	boolean success = this.moveFile(xmlFile, getExtractFailureDirName());
        	            if (!success) {
        	                String errorMessage = "File with name '" + xmlFile.getName() + "' could not be moved";
        	                throw new PurError(errorMessage);
                	    }
        	            updateSummaryCounts(EXTRACT_FAILURES);
               }
                clearErrorMessagesToPreventSaveAndRouteErrors();

                //Do not execute rest of code below
                //continue;
            }
            
        }

        StringBuffer finalText = buildLoadSummary(eInvoiceLoad);
        sendSummary(finalText);

        clearLoadCounts(); // Need to clear the counts after each run, so the totals don't show cumulative counts with each subsequent run.
        LOG.info("Processing completed");

        return eInvoiceLoad;
    }

	private static String describeStackTraceElement(final StackTraceElement element) {
		final StringBuffer description = new StringBuffer();
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

	@Override
	protected void processAboveTheLineItem(
	        final PaymentRequestItem purapItem, 
	        final ElectronicInvoiceOrderHolder orderHolder) {
		LOG.info("Processing above the line item");

		ElectronicInvoiceItemHolder itemHolder = orderHolder.getItemByLineNumber(purapItem.getItemLineNumber().intValue());
        // KFSPTS-1719 : investigation
        if (((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() != null) {
        	itemHolder = ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem();
        }
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

		// KFSUPGRADE-478
		if (itemHolder.getSubTotalAmount() != null) {
			purapItem.setExtendedPrice(itemHolder.getSubTotalAmount());
		} else {
			if (purapItem.getItemQuantity() != null) {
				LOG.info(
                        "Item number {} needs calculation of extended price from quantity {} and unit cost {}",
                        purapItem::getItemLineNumber,
                        purapItem::getItemQuantity,
                        purapItem::getItemUnitPrice
                );
				purapItem.setExtendedPrice(purapItem.getItemQuantity().multiply(new KualiDecimal(purapItem.getItemUnitPrice())));
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

	@Override
	protected void processItemsForDiscount(
	        final PaymentRequestDocument preqDocument, 
	        final ElectronicInvoiceOrderHolder orderHolder) {
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

		final KualiDecimal discountValueToUse = new KualiDecimal(orderHolder.getInvoiceDiscountAmount().negate());
		final List<PaymentRequestItem> preqItems = preqDocument.getItems();

		boolean alreadyProcessedInvoiceDiscount = false;
		boolean hasKualiPaymentTermsDiscountItem = false;

		// if e-invoice amount is negative... it is a penalty and we must pay extra
		for (final PaymentRequestItem preqItem : preqItems) {
			hasKualiPaymentTermsDiscountItem = hasKualiPaymentTermsDiscountItem || StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE, preqItem.getItemTypeCode());

			if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT,orderHolder)) {

				alreadyProcessedInvoiceDiscount = true;

				if (StringUtils.equals(preqItem.getItemTypeCode(),PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)) {
					// Item is kuali payment terms discount item... must perform calculation
					// if discount item exists on PREQ and discount dollar amount exists... use greater amount
					LOG.info(
                            "Discount Check - E-Invoice matches PREQ item type '{}'... now checking for amount",
                            preqItem::getItemTypeCode
                    );

					final KualiDecimal preqExtendedPrice = preqItem.getExtendedPrice() == null ? KualiDecimal.ZERO : preqItem.getExtendedPrice();
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
					// item is not payment terms discount item... just add value
					// if discount item exists on PREQ and discount dollar amount exists... use greater amount
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

		/*
		 * If we have not already processed the discount amount then the mapping
		 * is pointed to an item that is not in the PREQ item list
		 * 
		 * FYI - FILE DISCOUNT AMOUNT CURRENTLY HARD CODED TO GO INTO PAYMENT
		 * TERMS DISCOUNT ITEM ONLY... ALL OTHERS WILL FAIL
		 */

		if (!alreadyProcessedInvoiceDiscount) {
			String itemTypeRequired = PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE;
			// if we already have a PMT TERMS DISC item but the e-invoice  discount wasn't processed... error out
			// if the item mapping for e-invoice discount item is not PMT TERMS DISC item and we haven't processed it... error out

			if (hasKualiPaymentTermsDiscountItem ||
                !orderHolder.isItemTypeAvailableInItemMapping(ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT)) {
				final ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_DISCOUNT_ERROR, null, orderHolder.getFileName());
				orderHolder.addInvoiceOrderRejectReason(rejectReason);
				return;
			} else if (discountValueToUse.isNonZero()) {
				final PaymentRequestItem newItem = new PaymentRequestItem();
				newItem.setItemUnitPrice(discountValueToUse.bigDecimalValue());
				// KFSUPGRADE-473
			    //	newItem.setItemTypeCode(PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE);
                newItem.setItemTypeCode(PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE);
                newItem.setItemDescription(CUPurapConstants.ElectronicInvoice.DISCOUNT_DESCRIPTION);
				newItem.setExtendedPrice(discountValueToUse);
				newItem.setPurapDocument(preqDocument);
				preqDocument.addItem(newItem);
			}
		}

		LOG.info("Completed processing payment request items for discount");
	}

	@Override
    protected void removeEmptyItems(final List<PurApItem> preqItems) {
        for(int i=preqItems.size()-1; i >= 0; i--) {
            final PurApItem item = preqItems.get(i);

            // KFSUPGRADE-478 : allow '0' unit price item for einvoice.  3.0.1 did not remove this line (0 unit price), so keep it.
            //if the unit and extended price have null or zero as a combo, remove item
            if(item.getItemLineNumber() == null && isNullOrZero(item.getItemUnitPrice()) && isNullOrZero(item.getExtendedPrice()) ) {
                preqItems.remove(i);
            }
        }
    }

    @Override
    protected void setVendorDetails(final ElectronicInvoice eInvoice) {
    	// based on code found in this class
    	// Marcia mentioned that each einvoice just for one po.
    	String poID = eInvoice.getInvoiceDetailOrders().get(0).getOrderReferenceOrderID();
        PurchaseOrderDocument po = null;

        if (StringUtils.isNotEmpty(eInvoice.getDunsNumber())) {

        	// KFSUPGRADE-479 : retrieve vendor from po vendor.
            VendorDetail vendorDetail = null;
            if (NumberUtils.isDigits(StringUtils.defaultString(poID)) && !isIntegerTooLarge(poID)) {
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
                LOG.info("Vendor match found - {}", vendorDetail::getVendorNumber);
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

    @Override
    protected void validateVendorDetails(final ElectronicInvoiceRejectDocument rejectDocument) {

        boolean vendorFound = false;

        if (StringUtils.isNotEmpty(rejectDocument.getVendorDunsNumber())) {
        	// KFSUPGRADE-479 : retrieve vendor from po vendor.
            PurchaseOrderDocument po = rejectDocument.getCurrentPurchaseOrderDocument();

            VendorDetail vendorDetail = null;
            if (po != null) {
                vendorDetail = vendorService.getVendorDetail(po.getVendorHeaderGeneratedIdentifier(), po.getVendorDetailAssignedIdentifier());
                if (vendorDetail == null) {
                    vendorDetail = vendorService.getVendorByDunsNumber(rejectDocument.getVendorDunsNumber());
                }
            } else {
               vendorDetail = vendorService.getVendorByDunsNumber(rejectDocument.getVendorDunsNumber());
            }

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

        final String newDocumentDesc = generateRejectDocumentDescription(rejectDocument);
        rejectDocument.getDocumentHeader().setDocumentDescription(newDocumentDesc);
    }

    @Override
    protected void setProcessingCampus(final PaymentRequestDocument preqDoc, final String initiatorCampusCode) {
        String campusCode = parameterService.getParameterValueAsString(ElectronicInvoiceStep.class,
                PurapParameterConstants.ElectronicInvoiceParameters.OVERRIDE_PROCESSING_CAMPUS);
        if(!StringHelper.isNullOrEmpty(campusCode)) {
            preqDoc.setProcessingCampusCode(campusCode);
        }
        else {
        	// KFSUPGRADE-481 : kfs5 already has the new "ElectronicInvoiceParameters.OVERRIDE_PROCESSING_CAMPUS"
        	// Also implemented CU's solution with a parameter DEFAULT_PROCESSING_CAMPUS
        	// not sure we need another default campus code.  anyway, move initial fix here just in case.
            if(StringUtils.isEmpty(initiatorCampusCode)) {
                if (LOG.isInfoEnabled()) {
	            	LOG.info("createPaymentRequest() - Processing Campus Code not populated.  Value of campus code: "+initiatorCampusCode);
	            	LOG.info("createPaymentRequest() - Requisition Initiator User: Id="+ preqDoc.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId()+"; Campus Code="+initiatorCampusCode);
                }
            	// Set the processing campus code to the default value.
                campusCode = parameterService.getParameterValueAsString(ElectronicInvoiceStep.class, CUPurapParameterConstants.ElectronicInvoiceParameters.DEFAULT_PROCESSING_CAMPUS);
                preqDoc.setProcessingCampusCode(campusCode);
             //   preqDoc.setProcessingCampusCode(CUPurapConstants.PaymentRequestDefaults.DEFAULT_PROCESSING_CAMPUS_CODE);
            } else {
                preqDoc.setProcessingCampusCode(initiatorCampusCode);
            }
        }
     }

    // KFSUPGRADE-483 : refactor to 2 steps
	@Override
    public boolean routeDocuments() {
    	routeEIRTDocuments();
    	routePREQDocuments();
    	
    	return true;
    }

    protected boolean routeEIRTDocuments() {
    	Collection<String> documentIdList = null;
        try {
            documentIdList = retrieveDocumentsToRoute(KewApiConstants.ROUTE_HEADER_SAVED_CD, ElectronicInvoiceRejectDocument.class);
        } catch (WorkflowException e1) {
            LOG.error("Error retrieving eirt documents for routing: " + e1.getMessage(),e1);
            throw new RuntimeException(e1.getMessage(),e1);
        } catch (RemoteException re) {
            LOG.error("Error retrieving eirt documents for routing: " + re.getMessage(),re);
            throw new RuntimeException(re.getMessage(),re);
        }
        
        //Collections.reverse(documentIdList);
        LOG.info("EIRTs to Route: "+documentIdList);
        
        for (String eirtDocumentId: documentIdList) {
            LOG.info("Retrieving EIRT document # " + eirtDocumentId + ".");
            ElectronicInvoiceRejectDocument eirtDocument = (ElectronicInvoiceRejectDocument)documentService.getByDocumentHeaderId(eirtDocumentId);
            LOG.info("Routing EIRT document # " + eirtDocumentId + ".");
            documentService.prepareWorkflowDocument(eirtDocument);
            LOG.info("EIRT document # " + eirtDocumentId + " prepared for workflow.");
            // calling workflow service to bypass business rule checks
            workflowDocumentService.route(eirtDocument.getDocumentHeader().getWorkflowDocument(), "Routed by electronic invoice batch job", null);
            LOG.info("EIRT document # " + eirtDocumentId + " routed.");
        }

    	return true;
    }
    
    /**
     * 
     * @return
     */
    protected boolean routePREQDocuments() {
    	Collection<String> documentIdList = null;
        try {
            documentIdList = retrieveDocumentsToRoute(KewApiConstants.ROUTE_HEADER_SAVED_CD, PaymentRequestDocument.class);
        } catch (WorkflowException e1) {
            LOG.error("Error retrieving preq documents for routing: " + e1.getMessage(),e1);
            throw new RuntimeException(e1.getMessage(),e1);
        } catch (RemoteException re) {
            LOG.error("Error retrieving preq documents for routing: " + re.getMessage(),re);
            throw new RuntimeException(re.getMessage(),re);
        }
        
        //Collections.reverse(documentIdList);
        LOG.info("PREQs to Route: "+documentIdList);
        
        for (String preqDocumentId: documentIdList) {
            LOG.info("Retrieving PREQ document # " + preqDocumentId + ".");
        	PaymentRequestDocument preqDocument = (PaymentRequestDocument)documentService.getByDocumentHeaderId(preqDocumentId);
            LOG.info("Routing PREQ document # " + preqDocumentId + ".");

            if (preqDocument.getPaymentRequestElectronicInvoiceIndicator()) {
            	documentService.prepareWorkflowDocument(preqDocument);
         		LOG.info("PREQ document # " + preqDocumentId + " prepared for workflow.");
            	// calling workflow service to bypass business rule checks
            	workflowDocumentService.route(preqDocument.getDocumentHeader().getWorkflowDocument(), "Routed by electronic invoice batch job", null);
          		LOG.info("PREQ document # " + preqDocumentId + " routed.");
            }
        }
    	
    	return true;
    }

    /**
     * Returns a list of all initiated but not yet routed payment request or reject documents, using the KualiWorkflowInfo service.
     * @return a list of payment request or eirt documents to route
     */
    protected Collection<String> retrieveDocumentsToRoute(String statusCode, Class<?> document) throws WorkflowException, RemoteException {
        // This is very much from pcardserviceimpl
    	Set<String> documentIds = new HashSet<String>();
        
        DocumentSearchCriteria.Builder criteria = DocumentSearchCriteria.Builder.create();
        criteria.setDocumentTypeName(getDataDictionaryService().getDocumentTypeNameByClass(document));
        criteria.setDocumentStatuses(Collections.singletonList(DocumentStatus.fromCode(statusCode)));

        DocumentSearchCriteria crit = criteria.build();

        int maxResults = financialSystemDocumentService.getMaxResultCap(crit);
        int iterations = financialSystemDocumentService.getFetchMoreIterationLimit();

        for (int i = 0; i < iterations; i++) {
            LOG.debug("Fetch Iteration: "+ i);
            criteria.setStartAtIndex(maxResults * i);
            crit = criteria.build();
            LOG.debug("Max Results: "+criteria.getStartAtIndex());
            DocumentSearchResults results = KEWServiceLocator.getDocumentSearchService().lookupDocuments(
            		GlobalVariables.getUserSession().getPrincipalId(), crit, false);
            if (results.getSearchResults().isEmpty()) {
                break;
            }
            for (DocumentSearchResult resultRow: results.getSearchResults()) {
                documentIds.add(resultRow.getDocument().getDocumentId());
                LOG.debug(resultRow.getDocument().getDocumentId());
            }
        }
        
        return documentIds;
    }
    
    @Override
    protected StringBuffer saveLoadSummary(final ElectronicInvoiceLoad eInvoiceLoad) {
    	NumberFormat twoDecForm = DecimalFormat.getCurrencyInstance(Locale.US);
    	
        final Map<String, ElectronicInvoiceLoadSummary> savedLoadSummariesMap = new HashMap<String, ElectronicInvoiceLoadSummary>();

        final StringBuffer summaryMessage = new StringBuffer();

        for (Iterator iter = eInvoiceLoad.getInvoiceLoadSummaries().keySet().iterator(); iter.hasNext();) {

            final String dunsNumber = (String) iter.next();
            final ElectronicInvoiceLoadSummary eInvoiceLoadSummary = (ElectronicInvoiceLoadSummary) eInvoiceLoad.getInvoiceLoadSummaries().get(dunsNumber);

              if (!eInvoiceLoadSummary.isEmpty().booleanValue()) {
                LOG.info("Saving Load Summary for DUNS '{}'", dunsNumber);

                final ElectronicInvoiceLoadSummary currentLoadSummary = saveElectronicInvoiceLoadSummary(eInvoiceLoadSummary);

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

        for (Iterator rejectIter = eInvoiceLoad.getRejectDocuments().iterator(); rejectIter.hasNext();) {
            final ElectronicInvoiceRejectDocument rejectDoc = (ElectronicInvoiceRejectDocument) rejectIter.next();
            saveRejectDocument(rejectDoc,savedLoadSummariesMap);
        }

        /**
         * Even if there is an exception in the reject doc routing, all the files marked as reject will
         * be moved to the reject dir
         */
        //  moveFileList(eInvoiceLoad.getRejectFilesToMove());

        return summaryMessage;
    }

    protected void saveRejectDocument(ElectronicInvoiceRejectDocument rejectDoc, Map savedLoadSummariesMap) {

        LOG.info("Saving Invoice Reject for DUNS '" + rejectDoc.getVendorDunsNumber() + "'");

        if (savedLoadSummariesMap.containsKey(rejectDoc.getVendorDunsNumber())) {
            rejectDoc.setInvoiceLoadSummary((ElectronicInvoiceLoadSummary) savedLoadSummariesMap.get(rejectDoc.getVendorDunsNumber()));
        }
        else {
            rejectDoc.setInvoiceLoadSummary((ElectronicInvoiceLoadSummary) savedLoadSummariesMap.get(UNKNOWN_DUNS_IDENTIFIER));
        }

        documentService.saveDocument(rejectDoc, DocumentSystemSaveEvent.class);

    }

    @Override
    protected PaymentRequestDocument initializePaymentRequestDocument(ElectronicInvoiceOrderHolder orderHolder) {
        LOG.info("Creating Payment Request document");

        KNSGlobalVariables.getMessageList().clear();

        validateInvoiceOrderValidForPREQCreation(orderHolder);

        if (orderHolder.isInvoiceRejected()) {
            LOG.info("Not possible to convert einvoice details into payment request");
        }else{
            LOG.info("Payment request document creation validation succeeded");
        }

        if (orderHolder.isInvoiceRejected()) {
            return null;
        }

        final PaymentRequestDocument preqDoc =  (PaymentRequestDocument) documentService.getNewDocument("PREQ");

        final PurchaseOrderDocument poDoc = orderHolder.getPurchaseOrderDocument();
        if (poDoc == null) {
            throw new RuntimeException("Purchase Order document does not exist in the system");
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
            preqDoc.setAccountsPayablePurchasingDocumentLinkIdentifier(orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
        }

        //Copied from PaymentRequestServiceImpl.populatePaymentRequest()
        //set bank code to default bank code in the system parameter
        //KFSPTS-1891
        boolean hasPaymentMethodCode = false;
        if ( preqDoc instanceof PaymentRequestDocument ) {
            String vendorPaymentMethodCode = ((VendorDetailExtension)poDoc.getVendorDetail().getExtension()).getDefaultB2BPaymentMethodCode();
            if ( StringUtils.isNotEmpty(vendorPaymentMethodCode) ) { 
                ((CuPaymentRequestDocument)preqDoc).setPaymentMethodCode(vendorPaymentMethodCode);
                hasPaymentMethodCode = true;
            } else {
                ((CuPaymentRequestDocument)preqDoc).setPaymentMethodCode(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
            }
        }
        Bank defaultBank = null;
        if ( hasPaymentMethodCode ) {
            defaultBank = cUPaymentMethodGeneralLedgerPendingEntryService.getBankForPaymentMethod( ((CuPaymentRequestDocument)preqDoc).getPaymentMethodCode() );
        } else { // default to baseline behavior - extended documents not in use
            //Copied from PaymentRequestServiceImpl.populatePaymentRequest()
            //set bank code to default bank code in the system parameter
            defaultBank = bankService.getDefaultBankByDocType(preqDoc.getClass());
        }
        
        if (defaultBank != null) {
            preqDoc.setBankCode(defaultBank.getBankCode());
            preqDoc.setBank(defaultBank);
        }

        final RequisitionDocument reqDoc = getRequisitionService().getRequisitionById(poDoc.getRequisitionIdentifier());
        final String reqDocInitiator = reqDoc.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        try {
            final Person user = personService.getPerson(reqDocInitiator);

            setProcessingCampus(preqDoc, user.getCampusCode());

        } catch (final Exception e) {
            final String extraDescription = "Error setting processing campus code - " + e.getMessage();
            final ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, extraDescription, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }

        HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList = getAccountsPayableService().expiredOrClosedAccountsList(poDoc);
        if (expiredOrClosedAccountList == null) {
            expiredOrClosedAccountList = new HashMap();
        }

        LOG.info("{} accounts has been found as Expired or Closed", expiredOrClosedAccountList::size);

        preqDoc.populatePaymentRequestFromPurchaseOrder(orderHolder.getPurchaseOrderDocument(),expiredOrClosedAccountList);
        // need to populate here for ext price.  it become per item
        // KFSPTS-1719.  convert 1st matching inv item that is qty, but po is non-qty
        checkQtyInvItemForNoQtyOrder(preqDoc, orderHolder);

        populateItemDetails(preqDoc,orderHolder);

        // KFSUPGRADE-485, KFSPTS-1719
        if (CollectionUtils.isNotEmpty(((CuElectronicInvoiceOrderHolder)orderHolder).getNonMatchItems())) {
        	for (ElectronicInvoiceItemHolder invItem : ((CuElectronicInvoiceOrderHolder)orderHolder).getNonMatchItems()) {
                PurchaseOrderItem item = (PurchaseOrderItem)ObjectUtils.deepCopy((Serializable) orderHolder.getPurchaseOrderDocument().getItems().get(invItem.getInvoiceItemLineNumber() - 1));
                item.setItemLineNumber(invItem.getInvoiceItemLineNumber());
                item.setItemDescription(((CuElectronicInvoiceItemHolder)invItem).getReferenceDescription());
                item.setItemUnitPrice(invItem.getInvoiceItemUnitPrice()); // this will be populated to reqitem.poitemunitprice
                PaymentRequestItem paymentRequestItem = new PaymentRequestItem(item, preqDoc, expiredOrClosedAccountList);
                ((CuPaymentRequestItemExtension)paymentRequestItem.getExtension()).setInvLineNumber(Integer.parseInt(((CuElectronicInvoiceItemHolder)invItem).getInvLineNumber()));
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
                ((CuElectronicInvoiceOrderHolder)orderHolder).setMisMatchItem((CuElectronicInvoiceItemHolder)invItem);
                populateItemDetailsForNonMatching(preqDoc,orderHolder);
                ((CuElectronicInvoiceOrderHolder)orderHolder).setMisMatchItem(null);
       		
        	}
        }
        

        /**
         * Validate totals,paydate
         */
        //PaymentRequestDocumentRule.processCalculateAccountsPayableBusinessRules
        kualiRuleService.applyRules(new AttributedCalculateAccountsPayableEvent(preqDoc));

        paymentRequestService.calculatePaymentRequest(preqDoc, true);

        processItemsForDiscount(preqDoc,orderHolder);

        if (orderHolder.isInvoiceRejected()) {
            return null;
        }

        paymentRequestService.calculatePaymentRequest(preqDoc, false);
        /**
         * PaymentRequestReview
         */
        //PaymentRequestDocumentRule.processRouteDocumentBusinessRules
        kualiRuleService.applyRules(new AttributedPaymentRequestForEInvoiceEvent(preqDoc));

        if(GlobalVariables.getMessageMap().hasErrors()) {
            LOG.info("***************Error in rules processing - {}", GlobalVariables::getMessageMap);
            final Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap().getErrorMessages();

            final String errors = errorMessages.toString();
            final ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, errors, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }

        if(KNSGlobalVariables.getMessageList().size() > 0) {
            LOG.info(
                    "Payment request contains {} warning message(s)",
                    () -> KNSGlobalVariables.getMessageList().size()
            );
            LOG.info("Payment request contains " + KNSGlobalVariables.getMessageList().size() + " warning message(s)");
            for (int i = 0; i < KNSGlobalVariables.getMessageList().size(); i++) {
                final ErrorMessage errorMessage = KNSGlobalVariables.getMessageList().get(i);
                LOG.info("Warning {}  - {}", i, errorMessage);
            }
        }

        addShipToNotes(preqDoc,orderHolder);

    	// KFSUPGRADE-483
//        String routingAnnotation = null;
//        if (!orderHolder.isRejectDocumentHolder()) {
//            routingAnnotation = "Routed by electronic invoice batch job";
//        }

        try {
            // KFSUPGRADE-483
            // KFSUPGRADE-490: Do save-only operations for just non-EIRT-generated PREQs.
            if (orderHolder.isRejectDocumentHolder()) {
                documentService.routeDocument(preqDoc, null, null);
            } else {
                documentService.saveDocument(preqDoc,DocumentSystemSaveEvent.class);
            }
        } catch(ValidationException e) {
            String extraDescription = GlobalVariables.getMessageMap().toString();
            ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, extraDescription, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return null;
        }

        return preqDoc;
    }
    
    @Override
    protected boolean routeCreatedPaymentRequest(
            final ElectronicInvoiceOrderHolder orderHolder,
            final PaymentRequestDocument preqDoc) {
        String routingAnnotation = null;
        if (!orderHolder.isRejectDocumentHolder()) {
            routingAnnotation = "Routed by electronic invoice batch job";
        }

        try {
            // KFSUPGRADE-483
            // KFSUPGRADE-490: Do save-only operations for just non-EIRT-generated PREQs.
            if (orderHolder.isRejectDocumentHolder()) {
                documentService.routeDocument(preqDoc, routingAnnotation, null);
            } else {
                documentService.saveDocument(preqDoc,DocumentSystemSaveEvent.class);
            }
        } catch(final ValidationException e) {
            final String extraDescription = GlobalVariables.getMessageMap().toString();
            final ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(
                    PurapConstants.ElectronicInvoice.PREQ_ROUTING_VALIDATION_ERROR, extraDescription, orderHolder.getFileName());
            orderHolder.addInvoiceOrderRejectReason(rejectReason);
            return false;
        }
        return true;
    }

	
	// end KFSUPGRADE_483


    // KFSUPGRADE-480/KFSUPGRADE-484
    
    @Transactional
    @Override
    protected boolean processElectronicInvoice(
            final ElectronicInvoiceLoad eInvoiceLoad, 
            final File invoiceFile, byte[] xmlAsBytes) {

        // Checks parameter to see if files should be moved to the accept/reject folders after load
        boolean moveFiles = BooleanUtils.toBoolean(parameterService.getParameterValueAsString(ElectronicInvoiceStep.class, PurapParameterConstants.ElectronicInvoiceParameters.FILE_MOVE_AFTER_LOAD_IND));
        ElectronicInvoice eInvoice = null;
        boolean isExtractFailure = false;
        boolean isCompleteFailure = false;

        try {
            eInvoice = loadElectronicInvoice(xmlAsBytes);
        } catch (final CxmlParseException e) {
            LOG.info("Error loading file - {}", e::getMessage);
            rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, e.getMessage(),PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
            isExtractFailure = true;
            updateSummaryCounts(EXTRACT_FAILURES);
          } catch (IllegalArgumentException iae) {
          LOG.info("Error loading file - " + iae.getMessage());
//          rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, iae.getMessage(), PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
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
		        
		        // CU also refactored getItemTypeMappings with overlay
		        final Map<String, ElectronicInvoiceItemMapping> itemTypeMappings = getItemTypeMappings(eInvoice.getVendorHeaderID(),eInvoice.getVendorDetailID());
		        final Map<String, ItemType> kualiItemTypes = getKualiItemTypes();
		        
		        if (itemTypeMappings != null && itemTypeMappings.size() > 0) {
		            LOG.info("Item mappings found");
		        }
		        
		        boolean validateHeader = true;
	
		        try {
		            // ==== CU Customization: Added patterns to test for special characters and/or whitespace. ====
		            Pattern specialCharsPattern = Pattern.compile("[^\\p{Graph}\\p{Space}]");
		            Pattern specialCharsOrWhitespacePattern = Pattern.compile("[^\\p{Graph}]");
		            
			        for (final ElectronicInvoiceOrder order : eInvoice.getInvoiceDetailOrders()) {
			
			            final String poID = order.getOrderReferenceOrderID();
			            PurchaseOrderDocument po = null;
			            
			            if (NumberUtils.isDigits(StringUtils.defaultString(poID)) && !isIntegerTooLarge(poID)) {
			                po = purchaseOrderService.getCurrentPurchaseOrder(new Integer(poID));    
			                if (po != null) {
			                    order.setInvoicePurchaseOrderID(poID);
			                    order.setPurchaseOrderID(po.getPurapDocumentIdentifier());
			                    order.setPurchaseOrderCampusCode(po.getDeliveryCampusCode());
			                    
			                    if (LOG.isInfoEnabled()) {
			                        LOG.info("PO matching Document found");
			                    }
			                }
			            }
			            
			            // ==== CU Customization: Remove special characters from certain ID and description fields. ====
			            Matcher tempMatcher;
			            
			            if (StringUtils.isNotEmpty(order.getOrderReferenceDocumentRefPayloadID())) {
			                tempMatcher = specialCharsOrWhitespacePattern.matcher(order.getOrderReferenceDocumentRefPayloadID());
			                if (tempMatcher.find()) {
			                    LOG.warn("Found document ref payload ID with special or whitespace characters; these will be removed. Order: "
			                            + order.getOrderReferenceOrderID());
			                    order.setOrderReferenceDocumentRefPayloadID(tempMatcher.replaceAll(KFSConstants.EMPTY_STRING));
			                }
			            }
			            
			            if (StringUtils.isNotEmpty(order.getOrderReferenceOrderID())) {
			                tempMatcher = specialCharsOrWhitespacePattern.matcher(order.getOrderReferenceOrderID());
			                if (tempMatcher.find()) {
			                    LOG.warn("Found ref order ID with special or whitespace characters; these will be removed. Order: "
			                            + order.getOrderReferenceOrderID());
			                    order.setOrderReferenceOrderID(tempMatcher.replaceAll(KFSConstants.EMPTY_STRING));
			                }
			            }
			            
			            for (ElectronicInvoiceItem item : order.getInvoiceItems()) {
			                if (StringUtils.isNotEmpty(item.getReferenceDescription())) {
			                    tempMatcher = specialCharsPattern.matcher(item.getReferenceDescription());
			                    if (tempMatcher.find()) {
			                        LOG.warn("Found item description with special characters; these will be removed. Line number: "
			                                + item.getInvoiceLineNumber());
			                        item.setReferenceDescription(tempMatcher.replaceAll(KFSConstants.EMPTY_STRING));
			                    }
			                }
			            }
			            
			            
			            
			            final CuElectronicInvoiceOrderHolder orderHolder = new CuElectronicInvoiceOrderHolder(eInvoice,order,po,itemTypeMappings,kualiItemTypes,validateHeader);
			            matchingService.doMatchingProcess(orderHolder);
			            
			            if (orderHolder.isInvoiceRejected()) {
			                
			                final ElectronicInvoiceRejectDocument rejectDocument = createRejectDocument(eInvoice, order,eInvoiceLoad);
			                
			                if (orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier() != null) {
			                    rejectDocument.setAccountsPayablePurchasingDocumentLinkIdentifier(orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
			                }
			                
			                final String dunsNumber = StringUtils.isEmpty(eInvoice.getDunsNumber()) ?
			                                    UNKNOWN_DUNS_IDENTIFIER :
			                                    eInvoice.getDunsNumber();
			                
			                final ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad, dunsNumber);
			                loadSummary.addFailedInvoiceOrder(rejectDocument.getTotalAmount(),eInvoice);
			                eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);
			                
			                LOG.info("Saving Load Summary for DUNS '" + dunsNumber + "'");
			                getBusinessObjectService().save(loadSummary);
			                updateSummaryCounts(REJECT);
			            } else {
			                
			                final PaymentRequestDocument preqDoc  = createPaymentRequest(orderHolder);
			                
			                if (orderHolder.isInvoiceRejected()) {
			                    clearErrorMessagesToPreventSaveAndRouteErrors();
			                    
			                    final ElectronicInvoiceRejectDocument rejectDocument = createRejectDocument(eInvoice, order, eInvoiceLoad);
			                    
			                    if (orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier() != null) {
			                        rejectDocument.setAccountsPayablePurchasingDocumentLinkIdentifier(orderHolder.getAccountsPayablePurchasingDocumentLinkIdentifier());
			                    }
			                    
			                    final ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad, eInvoice.getDunsNumber());
			                    loadSummary.addFailedInvoiceOrder(rejectDocument.getTotalAmount(),eInvoice);
			                    eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);
			                    
				                LOG.info("Saving Load Summary for DUNS '" + eInvoice.getDunsNumber() + "'");
				                getBusinessObjectService().save(loadSummary);
				                updateSummaryCounts(REJECT);
			                } else {
			                    final ElectronicInvoiceLoadSummary loadSummary = getOrCreateLoadSummary(eInvoiceLoad, eInvoice.getDunsNumber());
			                    loadSummary.addSuccessfulInvoiceOrder(preqDoc.getTotalDollarAmount(),eInvoice);
			                    eInvoiceLoad.insertInvoiceLoadSummary(loadSummary);

			                    LOG.info("Saving Load Summary for DUNS '" + eInvoice.getDunsNumber() + "'");
				                getBusinessObjectService().save(loadSummary);
				                updateSummaryCounts(ACCEPT);
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
                LOG.info(invoiceFile.getName() + " has caused a batch extract failure.");
	        	success = this.moveFile(invoiceFile, getExtractFailureDirName());
	            if (!success) {
	                String errorMessage = "File with name '" + invoiceFile.getName() + "' could not be moved";
	                throw new PurError(errorMessage);
	            }
        	} else if(isCompleteFailure || (ObjectUtils.isNotNull(eInvoice) && eInvoice.isFileRejected())) {
                LOG.info(invoiceFile.getName() + " has been rejected.");
	        	success = this.moveFile(invoiceFile, getRejectDirName());
	            if (!success) {
	                String errorMessage = "File with name '" + invoiceFile.getName() + "' could not be moved";
	                throw new PurError(errorMessage);
	            }
	        } else {
                LOG.info(invoiceFile.getName() + " has been accepted");
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

    // KFSUPGRADE-482
    private boolean isIntegerTooLarge(String poId) {
    
    	try {
    		Integer.parseInt(poId);
    	} catch (NumberFormatException nfe) {
    		return true;
    	}
    	return false;
    }
    
    @Override
	public ElectronicInvoiceRejectDocument createRejectDocument(
	        final ElectronicInvoice eInvoice,
			final ElectronicInvoiceOrder electronicInvoiceOrder, 
			final ElectronicInvoiceLoad eInvoiceLoad) {
		LOG.info(
                "Creating reject document [DUNS={},POID={}]",
                eInvoice::getDunsNumber,
                electronicInvoiceOrder::getInvoicePurchaseOrderID
        );

        final ElectronicInvoiceRejectDocument eInvoiceRejectDocument;

        eInvoiceRejectDocument = (ElectronicInvoiceRejectDocument) documentService.getNewDocument("EIRT");

        eInvoiceRejectDocument.setInvoiceProcessTimestamp(dateTimeService.getCurrentTimestamp());
        final String rejectdocDesc = generateRejectDocumentDescription(eInvoice,electronicInvoiceOrder);
        eInvoiceRejectDocument.getDocumentHeader().setDocumentDescription(rejectdocDesc);
        eInvoiceRejectDocument.setDocumentCreationInProgress(true);

        eInvoiceRejectDocument.setFileLevelData(eInvoice);
        eInvoiceRejectDocument.setInvoiceOrderLevelData(eInvoice, electronicInvoiceOrder);

        documentService.saveDocument(eInvoiceRejectDocument);

        final String noteText = "Invoice file";
        attachInvoiceXMLWithRejectDoc(eInvoiceRejectDocument, getInvoiceFile(eInvoice.getFileName()), noteText);

        eInvoiceLoad.addInvoiceReject(eInvoiceRejectDocument);

        LOG.info("Reject document has been created (DocNo={})", eInvoiceRejectDocument::getDocumentNumber);

        emailTextErrorList.append("DUNS Number - " + eInvoice.getDunsNumber() + " " +eInvoice.getVendorName()+ ":\n");
        emailTextErrorList.append("An e-invoice from file '" + eInvoice.getFileName() + "' has been rejected due to the following error(s):\n");
        // get note text max length from DD
        int noteTextMaxLength = NOTE_TEXT_DEFAULT_MAX_LENGTH;

        Integer noteTextLength = getDataDictionaryService().getAttributeMaxLength(Note.class, KRADConstants.NOTE_TEXT_PROPERTY_NAME);
        if (noteTextLength != null) {
        	noteTextMaxLength = noteTextLength.intValue();
        }        
        
        // KFSUPGRADE-489/KITI-2643 - Modified to fix bug reported in KFSPTS-292 
        // Ensure that we don't overflow the maximum size of the note by creating 
        // separate notes if necessary.
        ArrayList<StringBuffer> rejectReasonNotes = new ArrayList<StringBuffer>();
        StringBuffer rejectReasonNote = new StringBuffer();
        String rejectReason = "";
        rejectReasonNote.append("This reject document has been created because of the following reason(s):\n");

        int index = 1;
        for (final ElectronicInvoiceRejectReason reason : eInvoiceRejectDocument.getInvoiceRejectReasons()) {
            emailTextErrorList.append("    - " + reason.getInvoiceRejectReasonDescription() + "\n");
            emailTextErrorList.append("    - PO  " + eInvoiceRejectDocument.getPurchaseOrderIdentifier() + "\n");
            emailTextErrorList.append("    - EIRT  " + eInvoiceRejectDocument.getDocumentNumber() + "\n");
            //  addRejectReasonsToNote("Reject Reason " + index + ". " + reason.getInvoiceRejectReasonDescription(), eInvoiceRejectDocument);
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
        for (StringBuffer note : rejectReasonNotes) {
        	addRejectReasonsToNote(note.toString(), eInvoiceRejectDocument);
        }

        return eInvoiceRejectDocument;
    }
    
    @Override
    protected void rejectElectronicInvoiceFile(final ElectronicInvoiceLoad eInvoiceLoad, final String fileDunsNumber, final File invoiceFile, final String extraDescription, final String rejectReasonTypeCode) {
        clearErrorMessagesToPreventSaveAndRouteErrors();       
        LOG.info("Rejecting the entire invoice file - {}", invoiceFile::getName);

        final ElectronicInvoiceLoadSummary eInvoiceLoadSummary = getOrCreateLoadSummary(eInvoiceLoad, fileDunsNumber);
        eInvoiceLoadSummary.addFailedInvoiceOrder();
        eInvoiceLoad.insertInvoiceLoadSummary(eInvoiceLoadSummary);

        final ElectronicInvoiceRejectDocument eInvoiceRejectDocument = (ElectronicInvoiceRejectDocument) documentService.getNewDocument("EIRT");

        eInvoiceRejectDocument.setInvoiceProcessTimestamp(dateTimeService.getCurrentTimestamp());
        eInvoiceRejectDocument.setVendorDunsNumber(fileDunsNumber);
        eInvoiceRejectDocument.setDocumentCreationInProgress(true);

        if (invoiceFile != null) {
            eInvoiceRejectDocument.setInvoiceFileName(invoiceFile.getName());
        }

        final List<ElectronicInvoiceRejectReason> list = new ArrayList<ElectronicInvoiceRejectReason>(1);

        final String message = "Complete failure document has been created for the Invoice with Filename '" + invoiceFile.getName() + "' due to the following error:\n";
        emailTextErrorList.append(message);

        final ElectronicInvoiceRejectReason rejectReason = matchingService.createRejectReason(rejectReasonTypeCode,extraDescription, invoiceFile.getName());
        list.add(rejectReason);

        emailTextErrorList.append("    - " + rejectReason.getInvoiceRejectReasonDescription());
        emailTextErrorList.append("\n\n");

        eInvoiceRejectDocument.setInvoiceRejectReasons(list);
        eInvoiceRejectDocument.getDocumentHeader().setDocumentDescription("Complete failure");

        // KFSCNTRB-1369: Need to Save document
        documentService.saveDocument(eInvoiceRejectDocument);

        final String noteText = "Invoice file";
//        if (invoiceFile.length() > 0) {
        	// empty file will casuse attachment creation exception.  Hence, job will be stopped
        attachInvoiceXMLWithRejectDoc(eInvoiceRejectDocument,invoiceFile,noteText);
//        }

        eInvoiceLoad.addInvoiceReject(eInvoiceRejectDocument);

        LOG.info("Complete failure document has been created (DocNo:{})", eInvoiceRejectDocument::getDocumentNumber);
    }

    private void clearErrorMessagesToPreventSaveAndRouteErrors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearErrorMessagesToPreventSaveAndRouteErrors, Logging previously created error messages");
            Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap().getErrorMessages();
            for (String key : errorMessages.keySet()) {
                LOG.debug("clearErrorMessagesToPreventSaveAndRouteErrors, key: " + key + ", value: " + buldErrorString(errorMessages.get(key)));
            }
        }
        GlobalVariables.getMessageMap().clearErrorMessages();
    }
    
    private String buldErrorString(AutoPopulatingList<ErrorMessage> errorMessageList) {
        if (errorMessageList != null) {
            StringBuilder sb = new StringBuilder();
            boolean needComma = false;
            for (ErrorMessage message : errorMessageList) {
                if (needComma) {
                    sb.append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
                }
                sb.append(message);
                needComma = true;
            }
            return sb.toString();
        } else {
            return "Null Error List";
        }
    }

    protected void attachInvoiceXMLWithRejectDoc(
            final ElectronicInvoiceRejectDocument eInvoiceRejectDocument, 
            final File attachmentFile, 
            final String noteText) {
        final Note note;
        try {
            note = documentService.createNoteFromDocument(eInvoiceRejectDocument, noteText);
            // KFSCNTRB-1369: Can't add note without remoteObjectIdentifier
            note.setRemoteObjectIdentifier(eInvoiceRejectDocument.getNoteTarget().getObjectId());
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
        } catch (Exception e) {
            // it may have more than one kind of Exception
            // if attachment is not created for any reason, then don't include in note and proceed.
            // otherwise it will throw runtimeexception and cause job to stop
            LOG.error("Unable to create attachment", e);
           // throw new RuntimeException("Unable to create attachment", e);
        }

        if (attachment != null) {
        	// if attachment is not created for any reason, then don't ininclude in note and proceed.
        	// otherwise it will throw runtimeexception and cause job to stop
            note.setAttachment(attachment);
            attachment.setNote(note);
        }
        noteService.save(note);
    }

    
    protected String getExtractFailureDirName() {
        return getBaseDirName() + "extractFailure" + File.separator;
    }

    /**
     * 
     * @param eInvoiceLoad
     * @return
     */
    protected StringBuffer buildLoadSummary(ElectronicInvoiceLoad eInvoiceLoad) {
        final StringBuffer summaryText = saveLoadSummary(eInvoiceLoad);

        final StringBuffer finalText = new StringBuffer();
        finalText.append("======================================\n");
        finalText.append("               TOTALS\n");
        finalText.append("======================================\n\n");
        finalText.append(ACCEPT).append("           : ").append((Integer)(loadCounts.get(ACCEPT)!=null?loadCounts.get(ACCEPT):0)).append("\n");
        finalText.append(REJECT).append("           : ").append((Integer)(loadCounts.get(REJECT)!=null?loadCounts.get(REJECT):0)).append("\n");
        finalText.append(EXTRACT_FAILURES).append(" : ").append((Integer)(loadCounts.get(EXTRACT_FAILURES)!=null?loadCounts.get(EXTRACT_FAILURES):0)).append("\n");
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
    
    private void updateSummaryCounts(String key) {
        int count;
        if(loadCounts.containsKey(key)) {
        	count = loadCounts.get(key);
        } else {
        	count = 0;
        }
        loadCounts.put(key, ++count);
    }

// end KFSUPGRADE-484
    // KFSUPGRADE-485
    @Override
    public boolean doMatchingProcess(final ElectronicInvoiceRejectDocument rejectDocument){
        /*
         * This is needed here since if the user changes the DUNS number.
         */
        validateVendorDetails(rejectDocument);

        final Map itemTypeMappings = getItemTypeMappings(rejectDocument.getVendorHeaderGeneratedIdentifier(),
                                                   rejectDocument.getVendorDetailAssignedIdentifier());

        final Map kualiItemTypes = getKualiItemTypes();

        final CuElectronicInvoiceOrderHolder rejectDocHolder = new CuElectronicInvoiceOrderHolder(rejectDocument,itemTypeMappings,kualiItemTypes);
        matchingService.doMatchingProcess(rejectDocHolder);
        // KFSPTS-1719 : save the nomatchingitems found during match process.
        ((CuElectronicInvoiceRejectDocument)rejectDocument).setNonMatchItems(((CuElectronicInvoiceOrderHolder)rejectDocHolder).getNonMatchItems());

        /*
         * Once we're through with the matching process, it's needed to check whether it's possible
         * to create PREQ for the reject doc
         */
        if (!rejectDocHolder.isInvoiceRejected()){
            validateInvoiceOrderValidForPREQCreation(rejectDocHolder);
        }

        //  determine which of the reject reasons we should suppress based on the parameter
        final List<String> ignoreRejectTypes = new ArrayList<String>(parameterService
                .getParameterValuesAsString(PurapConstants.PURAP_NAMESPACE, "ElectronicInvoiceReject",
                        "SUPPRESS_REJECT_REASON_CODES_ON_EIRT_APPROVAL"));
        final List<ElectronicInvoiceRejectReason> rejectReasonsToDelete = new ArrayList<ElectronicInvoiceRejectReason>();
        for (final ElectronicInvoiceRejectReason rejectReason : rejectDocument.getInvoiceRejectReasons()) {
            final String rejectedReasonTypeCode = rejectReason.getInvoiceRejectReasonTypeCode();
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
            clearErrorMessagesToPreventSaveAndRouteErrors();
        }

        //  this automatically returns false if there are no reject reasons
        return !rejectDocHolder.isInvoiceRejected();
    }

    @Override
    public boolean createPaymentRequest(ElectronicInvoiceRejectDocument rejectDocument){

        if (rejectDocument.getInvoiceRejectReasons().size() > 0){
            throw new RuntimeException("Not possible to create payment request since the reject document contains " + rejectDocument.getInvoiceRejectReasons().size() + " rejects");
        }

        Map itemTypeMappings = getItemTypeMappings(rejectDocument.getVendorHeaderGeneratedIdentifier(),
                                                   rejectDocument.getVendorDetailAssignedIdentifier());

        Map kualiItemTypes = getKualiItemTypes();

        CuElectronicInvoiceOrderHolder rejectDocHolder = new CuElectronicInvoiceOrderHolder(rejectDocument,itemTypeMappings,kualiItemTypes);
        // KFSPTS-1719 : restore the nomatchingitems found during matching process.  so, preq items can be created
        rejectDocHolder.setNonMatchItems( ((CuElectronicInvoiceRejectDocument)rejectDocument).getNonMatchItems());

        /**
         * First, create a new payment request document.  Once this document is created, then update the reject document's PREQ_ID field
         * with the payment request document identifier.  This identifier is used to associate the reject document with the payment request.
         */
        PaymentRequestDocument preqDocument = createPaymentRequest(rejectDocHolder);
        if(ObjectUtils.isNotNull(preqDocument)){
        	rejectDocument.setPaymentRequestIdentifier(preqDocument.getPurapDocumentIdentifier());
        }

        return !rejectDocHolder.isInvoiceRejected();

    }

    // KFSPTS-1719 : this is for the first matched the polineitem#
    private void checkQtyInvItemForNoQtyOrder(PaymentRequestDocument preqDocument, ElectronicInvoiceOrderHolder orderHolder) {
        List<PaymentRequestItem> preqItems = preqDocument.getItems();

        List<PaymentRequestItem> nonInvItems = new ArrayList<PaymentRequestItem>(); // non qty not in invoice yet
        for (PaymentRequestItem preqItem : preqItems) {

        	//  This is to check the first item that matched to po line and convert to qty if inv is qty po is non-qty
    		if (StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE, preqItem.getItemTypeCode())) {
				if ((((CuPaymentRequestItemExtension)preqItem.getExtension()).getInvLineNumber() == null && ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() == null)) {
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

    // for non matching items, the additional charge don't have to add it again.
    protected void populateItemDetailsForNonMatching(PaymentRequestDocument preqDocument, ElectronicInvoiceOrderHolder orderHolder) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Populating invoice order items into the payment request document");
        }

        List<PaymentRequestItem> preqItems = preqDocument.getItems();

        for (PaymentRequestItem preqItem : preqItems) {

        	CuPaymentRequestItemExtension preqItemExt = (CuPaymentRequestItemExtension)preqItem.getExtension();
        	// TODO : 'no qty' po line item may have 'qty inv item'
        	// force it to match like qty item.
        	if ((preqItemExt.getInvLineNumber() == null && ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() == null) || (preqItemExt.getInvLineNumber() != null && StringUtils.equals(preqItemExt.getInvLineNumber().toString(), ((CuElectronicInvoiceItemHolder)((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem()).getInvLineNumber()))) {
        		if (StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE, preqItem.getItemTypeCode())) {
        			if (preqItem.getItemQuantity() != null && preqItem.getItemQuantity().isPositive() && StringUtils.isNotBlank(preqItem.getItemUnitOfMeasureCode())) {
        				preqItem.setItemTypeCode(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE);
        			}
        		}
        	}
            if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_ITEM, orderHolder)) {
            	if ((preqItemExt.getInvLineNumber() == null && ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() == null) ||  (preqItemExt.getInvLineNumber() != null && StringUtils.equals(preqItemExt.getInvLineNumber().toString(), ((CuElectronicInvoiceItemHolder)((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem()).getInvLineNumber()))) {
                processAboveTheLineItem(preqItem, orderHolder);
                if (preqItemExt.getInvLineNumber() == null) {
                	preqItemExt.setInvLineNumber(Integer.parseInt(((CuElectronicInvoiceItemHolder)orderHolder.getItemByLineNumber(preqItem.getItemLineNumber())).getInvLineNumber()));
                	preqItem.setItemDescription(((CuElectronicInvoiceItemHolder)orderHolder.getItemByLineNumber(preqItem.getItemLineNumber())).getReferenceDescription());
                }
            	}
                //KFSPTS-1719 : check if this works for nonqty for unitprice and extended price
            } else if (preqItem.isNoQtyItem()) {
            	//TODO : need to try ti match the inv line to
            	if ((preqItemExt.getInvLineNumber() == null && ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() == null) ||  (preqItemExt.getInvLineNumber() != null && StringUtils.equals(preqItemExt.getInvLineNumber().toString(), ((CuElectronicInvoiceItemHolder)((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem()).getInvLineNumber()))) {
            	    processAboveTheLineItemForNonQty(preqItem, orderHolder);
                    if (preqItemExt.getInvLineNumber() == null) {
                    	preqItemExt.setInvLineNumber(Integer.parseInt(((CuElectronicInvoiceItemHolder)orderHolder.getItemByLineNumber(preqItem.getItemLineNumber())).getInvLineNumber()));
                    	preqItem.setItemDescription(((CuElectronicInvoiceItemHolder)orderHolder.getItemByLineNumber(preqItem.getItemLineNumber())).getReferenceDescription());
                    }
            	}
            }
            
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Successfully populated the invoice order items");
        }

    }

    // KFSPTS-1719
    protected void processAboveTheLineItemForNonQty(PaymentRequestItem purapItem, ElectronicInvoiceOrderHolder orderHolder){

        if (LOG.isInfoEnabled()){
            LOG.info("Processing above the line item");
        }
        
        // TODO : this will not work for mismatching items because they all point to the same poitemline, so need to change this process.
        ElectronicInvoiceItemHolder itemHolder = orderHolder.getItemByLineNumber(purapItem.getItemLineNumber().intValue());
        if (((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() != null) {
        	itemHolder = ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem();
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

    @Override
    protected void processInvoiceItem(
            final PaymentRequestItem preqItem, 
            final ElectronicInvoiceOrderHolder orderHolder){
    	// TODO : 'no qty' po line item may have 'qty inv item'
    	// force it to match like qty item.
    	CuPaymentRequestItemExtension preqItemExt = (CuPaymentRequestItemExtension)preqItem.getExtension() ;
    	// Need to populate if it is null
    	if (preqItemExt == null) {
    		preqItemExt = new CuPaymentRequestItemExtension();
    		preqItem.setExtension(preqItemExt);
    	}
    	if ((preqItemExt.getInvLineNumber() == null && ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() == null) || (preqItemExt.getInvLineNumber() != null && StringUtils.equals(preqItemExt.getInvLineNumber().toString(), ((CuElectronicInvoiceItemHolder)((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem()).getInvLineNumber()))) {
    		if (StringUtils.equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE, preqItem.getItemTypeCode())) {
    			if (preqItem.getItemQuantity() != null && preqItem.getItemQuantity().isPositive() && StringUtils.isNotBlank(preqItem.getItemUnitOfMeasureCode())) {
    				preqItem.setItemTypeCode(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE);
    			}
    		}
    	}
       if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_ITEM, orderHolder)) {
        	if ((preqItemExt.getInvLineNumber() == null && ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() == null) ||  (preqItemExt.getInvLineNumber() != null && StringUtils.equals(preqItemExt.getInvLineNumber().toString(), ((CuElectronicInvoiceItemHolder)((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem()).getInvLineNumber()))) {
                processAboveTheLineItem(preqItem, orderHolder);
                if (preqItem.getPurchaseOrderItem().isNoQtyItem()) {
                    if (preqItemExt.getInvLineNumber() == null && orderHolder.getItemByLineNumber(preqItem.getItemLineNumber()) != null) { // if there po is not in inv, then don't do this
                    	preqItemExt.setInvLineNumber(Integer.parseInt(((CuElectronicInvoiceItemHolder)orderHolder.getItemByLineNumber(preqItem.getItemLineNumber())).getInvLineNumber()));
            	        preqItem.setItemDescription(((CuElectronicInvoiceItemHolder)orderHolder.getItemByLineNumber(preqItem.getItemLineNumber())).getReferenceDescription());
                    }
                } else {
                	preqItemExt.setInvLineNumber(preqItem.getPurchaseOrderItem().getItemLineNumber());
                }
        	}
            //KFSPTS-1719 : check if this works for nonqty for unitprice and extended price
        } else if (preqItem.isNoQtyItem()) {
        	//TODO : need to try ti match the inv line to
        	if ((preqItemExt.getInvLineNumber() == null && ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem() == null) ||  (preqItemExt.getInvLineNumber() != null && StringUtils.equals(preqItemExt.getInvLineNumber().toString(), ((CuElectronicInvoiceOrderHolder)orderHolder).getMisMatchItem().getInvLineNumber()))) {
        	    processAboveTheLineItemForNonQty(preqItem, orderHolder);
                if (preqItemExt.getInvLineNumber() == null) {
                	preqItemExt.setInvLineNumber(Integer.parseInt(((CuElectronicInvoiceItemHolder)orderHolder.getItemByLineNumber(preqItem.getItemLineNumber())).getInvLineNumber()));
                	preqItem.setItemDescription(((CuElectronicInvoiceItemHolder)orderHolder.getItemByLineNumber(preqItem.getItemLineNumber())).getReferenceDescription());
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
        } else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_DISCOUNT, orderHolder)) {
            processDiscountItem(preqItem, orderHolder);
        }else if (isItemValidForUpdation(preqItem.getItemTypeCode(), ElectronicInvoice.INVOICE_AMOUNT_TYPE_CODE_EXMT, orderHolder)) {
            processAboveTheLineItem(preqItem, orderHolder);
        }
    }

    @Override
    protected void processTaxItem (final PaymentRequestItem preqItem, final ElectronicInvoiceOrderHolder orderHolder){

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
        } // KFSUPGRADE-487
        else {
        	preqItem.setItemDescription("Tax");
        }

    }

    @Override
    protected void addRejectReasonsToNote(
            final String rejectReasons, 
            final ElectronicInvoiceRejectDocument eInvoiceRejectDocument){

        try {
            final Note note = documentService.createNoteFromDocument(eInvoiceRejectDocument, rejectReasons);
            // KFSCNTRB-1369: Can't add note without remoteObjectIdentifier
            note.setRemoteObjectIdentifier(eInvoiceRejectDocument.getNoteTarget().getObjectId());
            PersistableBusinessObject noteParent = eInvoiceRejectDocument.getNoteTarget();
            noteService.save(note);
        }catch (final Exception e) {
            LOG.error("Error creating reject reason note - {}", e::getMessage);
        }
    }

    /**
     * Temporary override to work around an issue where input files with unsupported "xmlns:*" attributes
     * could not be parsed successfully, but otherwise behaves the same as in the superclass.
     * The only change was an added call to a new "removeUnsupportedXmlnsAttributes" method.
     * 
     * @see org.kuali.kfs.module.purap.service.impl.ElectronicInvoiceHelperServiceImpl#addNamespaceDefinition(
     * org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoad, java.io.File)
     */
    @Override
    protected byte[] addNamespaceDefinition(final ElectronicInvoiceLoad eInvoiceLoad, final File invoiceFile) {
        LOG.debug("addNamespaceDefinition() started");

        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setValidating(false); // It's not needed to validate here
        builderFactory.setIgnoringElementContentWhitespace(true);

        final DocumentBuilder builder;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            LOG.error("addNamespaceDefinition() Error getting document builder - {}", e::getMessage);
            throw new RuntimeException(e);
        }

        final Document xmlDoc;

        try {
            LOG.info("addNamespaceDefinition: builder.parse");
            xmlDoc = builder.parse(invoiceFile);
        } catch (final Exception e) {
            LOG.info("addNamespaceDefinition: Error parsing the file - {}", e::getMessage);
            rejectElectronicInvoiceFile(eInvoiceLoad, UNKNOWN_DUNS_IDENTIFIER, invoiceFile, e.getMessage(), PurapConstants.ElectronicInvoice.FILE_FORMAT_INVALID);
            return null;
        }
        LOG.info("addNamespaceDefinition: xmlDoc.getDocumentElement()");

        final Node node = xmlDoc.getDocumentElement();
        final Element element = (Element) node;

        removeUnsupportedXmlnsAttributes(element);

        final String xmlnsValue = element.getAttribute("xmlns");
        final String xmlnsXsiValue = element.getAttribute("xmlns:xsi");

        LOG.info("addNamespaceDefinition: getInvoiceFile");

        if (StringUtils.equals(xmlnsValue, "http://www.kuali.org/kfs/purap/electronicInvoice") &&
            StringUtils.equals(xmlnsXsiValue, "http://www.w3.org/2001/XMLSchema-instance")) {
            LOG.info("addNamespaceDefinition: xmlns and xmlns:xsi attributes already exists in the invoice xml");
        } else {
            element.setAttribute("xmlns", "http://www.kuali.org/kfs/purap/electronicInvoice");
            element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final Source source = new DOMSource(xmlDoc);
            final Result result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (final TransformerException e) {
            LOG.fatal("Failed to transform {} xml.{}", invoiceFile.getName(), e);
            throw new RuntimeException(e);
        }

        LOG.info("addNamespaceDefinition() Namespace validation completed");

        return out.toByteArray();
    }

    protected void removeUnsupportedXmlnsAttributes(Element element) {
        NamedNodeMap attributesMap = element.getAttributes();
        Attr[] attributes = new Attr[attributesMap.getLength()];
        
        for (int i = 0; i < attributesMap.getLength(); i++) {
            attributes[i] = (Attr) attributesMap.item(i);
        }
        
        Arrays.stream(attributes)
                .filter(this::isUnsupportedXmlnsAttribute)
                .forEach(element::removeAttributeNode);
    }
    
    // CU customization: base code method is private and cannot be accessed from custom extension
    private void logProcessElectronicInvoiceError(final String msg) {
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

    protected boolean isUnsupportedXmlnsAttribute(Attr attribute) {
        String attributeName = StringUtils.lowerCase(attribute.getName(), Locale.US);
        return StringUtils.startsWith(attributeName, XMLNS_ATTRIBUTE_PREFIX)
                && !ALLOWED_XMLNS_ATTRIBUTES_PATTERN.matcher(attributeName).matches();
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

    public void setFinancialSystemDocumentService(CUFinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public void setcUPaymentMethodGeneralLedgerPendingEntryService(
            CUPaymentMethodGeneralLedgerPendingEntryService cUPaymentMethodGeneralLedgerPendingEntryService) {
        this.cUPaymentMethodGeneralLedgerPendingEntryService = cUPaymentMethodGeneralLedgerPendingEntryService;
    }

}
