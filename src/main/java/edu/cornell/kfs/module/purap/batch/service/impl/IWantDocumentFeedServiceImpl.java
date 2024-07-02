package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kns.rule.event.KualiAddLineEvent;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.batch.service.IWantDocumentFeedService;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantDocumentBatchFeed;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.businessobject.xml.IWantDocumentWrapperXml;
import edu.cornell.kfs.module.purap.document.BatchIWantDocument;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.module.purap.document.validation.event.AddIWantItemEvent;
import edu.cornell.kfs.sys.service.CUMarshalService;
import jakarta.xml.bind.JAXBException;

@Transactional
public class IWantDocumentFeedServiceImpl implements IWantDocumentFeedService {

	private static final Logger LOG = LogManager.getLogger(IWantDocumentFeedServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType iWantDocumentInputFileType;
    protected BusinessObjectService businessObjectService;
    protected DocumentService documentService;
    protected PersonService personService;
    protected IWantDocumentService iWantDocumentService;
    protected KualiRuleService ruleService;
    protected AttachmentService attachmentService;
    protected Properties mimeTypeProperties;
    protected VendorService vendorService;
    protected CUMarshalService cuMarshalService;

	/**
     * @see edu.cornell.kfs.module.purap.batch.service.IWantDocumentFeedService#processIWantDocumentFiles()
     */
    @Override
    public boolean processIWantDocumentFiles() {
        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(iWantDocumentInputFileType);

        List<String> processedFiles = new ArrayList<String>();

        for (String incomingFileName : fileNamesToLoad) {
            try {   
                //LOG.debug("processIWantDocumentFiles  () Processing " + incomingFileName);                
                //IWantDocumentBatchFeed batchFeed = parseInputFile(incomingFileName);
                LOG.info("processIWantDocumentFiles() Processing " + incomingFileName);
                File xmlFile = new File(incomingFileName);
                IWantDocumentWrapperXml documentWrapper = cuMarshalService.unmarshalFile(xmlFile, IWantDocumentWrapperXml.class);
                IWantDocumentBatchFeed batchFeed = documentWrapper.toIWantDocumentBatchFeed();

                if (batchFeed != null && !batchFeed.getBatchIWantDocuments().isEmpty()) {
                    loadIWantDocuments(batchFeed, incomingFileName, GlobalVariables.getMessageMap());      
                    processedFiles.add(incomingFileName);
                }
            } catch (RuntimeException | JAXBException e) {
                LOG.error("Caught exception trying to load i want document file: " + incomingFileName, e);
                // remove done files
                List<String> badFiles = new ArrayList<String>();
                badFiles.add(incomingFileName);
            	removeDoneFiles(badFiles);
                throw new RuntimeException("Caught exception trying to load i want document file: " + incomingFileName, e);
            } 
        }
        
        // remove done files
    	 removeDoneFiles(processedFiles);

        return true;

    }

    /**
     * @TODO remove this
     * Parses the input file.
     * 
     * @param incomingFileName
     * @return an IWantDocumentBatchFeed containing input data
     
    protected IWantDocumentBatchFeed parseInputFile(String incomingFileName) {
        LOG.info("Parsing file: " + incomingFileName);

        FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        } catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        Object parsed = null;
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            parsed = batchInputFileService.parse(iWantDocumentInputFileType, fileByteContent);
        } catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        } catch (ParseException e1) {
            LOG.error("Error parsing xml " + e1.getMessage());
            throw new RuntimeException("Error parsing xml " + e1.getMessage(), e1);
        }
        finally{
        	IOUtils.closeQuietly(fileContents);
        }

        return (IWantDocumentBatchFeed) parsed;

    }*/

    /**
     * Creates I Wantd documents from the data in the input files.
     * 
     * @param batchFeed
     * @param incomingFileName
     * @param MessageMap
     */
    public void loadIWantDocuments(IWantDocumentBatchFeed batchFeed, String incomingFileName, MessageMap MessageMap) {

        LOG.info("Loading I Want documents from incoming file file: " + incomingFileName);

        for (BatchIWantDocument batchIWantDocument : batchFeed.getBatchIWantDocuments()) {

            populateIWantDocument(batchIWantDocument, incomingFileName);

        }

    }

    /**
     * Populates an I Want document based on the input data.
     * 
     * @param batchIWantDocument
     */
    private void populateIWantDocument(BatchIWantDocument batchIWantDocument, String incomingFileName) {

        boolean noErrors = true;
        List<AdHocRoutePerson> adHocRoutePersons = new ArrayList<AdHocRoutePerson>();

        LOG.info("Creating I Want document from data related to source number: " + batchIWantDocument.getSourceNumber());

        try {
            if (StringUtils.isBlank(batchIWantDocument.getInitiator())) {
                LOG.error("Initiator net ID cannot be empty: " + batchIWantDocument.getInitiator());
                noErrors = false;
            }

			// if initiator is blank we cannot create the I Want doc
			if (noErrors) {
				Person initiator = personService.getPersonByPrincipalName(batchIWantDocument.getInitiator());

				if (ObjectUtils.isNull(initiator)) {
					LOG.error("Initiator net ID is not valid: " + batchIWantDocument.getInitiator());
					noErrors = false;
				}

				// if initiator is not valid we cannot create the I Want doc
				if (noErrors) {

					IWantDocument iWantDocument = (IWantDocument) documentService.getNewDocument(CUPurapConstants.IWNT_DOC_TYPE, batchIWantDocument.getInitiator());

					
					iWantDocumentService.setUpIWantDocDefaultValues(iWantDocument, initiator);
					

					if (StringUtils.isNotBlank(batchIWantDocument.getInitiatorNetID())) {
						iWantDocument.setInitiatorNetID(batchIWantDocument.getInitiatorNetID());
					}

					iWantDocument.getDocumentHeader().setExplanation(batchIWantDocument.getBusinessPurpose());
					iWantDocument.setExplanation(batchIWantDocument.getBusinessPurpose());
					if (StringUtils.isNotBlank(batchIWantDocument.getCollegeLevelOrganization())) {
						iWantDocument.setCollegeLevelOrganization(batchIWantDocument.getCollegeLevelOrganization());
					}

					if (StringUtils.isNotBlank(batchIWantDocument.getDepartmentLevelOrganization())) {
						iWantDocument.setDepartmentLevelOrganization(batchIWantDocument.getDepartmentLevelOrganization());
					}

					iWantDocument.getDocumentHeader().setOrganizationDocumentNumber(batchIWantDocument.getSourceNumber());

					// populate requester fields
					populateIWantDocRequestorSection(initiator, batchIWantDocument, iWantDocument);

					// populate deliver to section
					populateIWantDocDeliverToSection(batchIWantDocument, iWantDocument);

					// populate vendor data
					if(StringUtils.isNotBlank(batchIWantDocument.getVendorNumber())){
						String[] vendorNumbers = batchIWantDocument.getVendorNumber().split("-");
						if(vendorNumbers.length == 2){
							try{
							Integer vendorHeaderId = new Integer(vendorNumbers[0]);
							Integer vendorId = new Integer(vendorNumbers[1]);
		                
							String phoneNumber = "Phone: ";

							Map<String,Object> fieldValues = new HashMap<String,Object>();
							fieldValues.put("vendorHeaderGeneratedIdentifier", vendorHeaderId);
							fieldValues.put("vendorDetailAssignedIdentifier", vendorId);
							fieldValues.put("vendorPhoneTypeCode", "PH");
							Collection<VendorPhoneNumber> vendorPhoneNumbers = businessObjectService.findMatching(VendorPhoneNumber.class,
									fieldValues);
							if (ObjectUtils.isNotNull(vendorPhoneNumbers) && vendorPhoneNumbers.size() > 0) {
								VendorPhoneNumber retrievedVendorPhoneNumber = vendorPhoneNumbers.toArray(new VendorPhoneNumber[1])[0];
								phoneNumber += retrievedVendorPhoneNumber.getVendorPhoneNumber();
							}
							
		                
							Map<String,Object> fieldValuesVendorDetail = new HashMap<String,Object>();
							fieldValuesVendorDetail.put("vendorHeaderGeneratedIdentifier", vendorHeaderId);
							fieldValuesVendorDetail.put("vendorDetailAssignedIdentifier", vendorId);
							VendorDetail vendorDetail = businessObjectService.findByPrimaryKey(VendorDetail.class, fieldValuesVendorDetail);
		                
							if(ObjectUtils.isNotNull(vendorDetail)){
								iWantDocument.setVendorHeaderGeneratedIdentifier(vendorHeaderId);
								iWantDocument.setVendorDetailAssignedIdentifier(vendorId);
								iWantDocument.setVendorName(vendorDetail.getVendorName());
								
								updateDefaultVendorAddress(vendorDetail);

								// populate vendor info
								String addressLine1 = vendorDetail.getDefaultAddressLine1() != null ? vendorDetail
										.getDefaultAddressLine1() : StringUtils.EMPTY;
								String addressLine2 = vendorDetail.getDefaultAddressLine2() != null ? vendorDetail
										.getDefaultAddressLine2() : StringUtils.EMPTY;
		                        String cityName = vendorDetail.getDefaultAddressCity() != null ? vendorDetail.getDefaultAddressCity()
		                        		: StringUtils.EMPTY;
		                        String stateCode = vendorDetail.getDefaultAddressStateCode() != null ? vendorDetail.getDefaultAddressStateCode()
		                        		: StringUtils.EMPTY;
		                        String countryCode = vendorDetail.getDefaultAddressCountryCode() != null ? vendorDetail
		                        		.getDefaultAddressCountryCode() : StringUtils.EMPTY;
		                        String postalCode = vendorDetail.getDefaultAddressPostalCode() != null ? vendorDetail.getDefaultAddressPostalCode()
		                        		: StringUtils.EMPTY;
		                        String faxNumber = "Fax: "
		                        		+ (vendorDetail.getDefaultFaxNumber() != null ? vendorDetail.getDefaultFaxNumber()
		                                : StringUtils.EMPTY);

		                        String url = "URL: " + (vendorDetail.getVendorUrlAddress() != null ? vendorDetail.getVendorUrlAddress() : StringUtils.EMPTY);

		                        String vendorInfo = new StringBuilder(100).append(addressLine1).append('\n').append(addressLine2).append('\n').append(
		                            cityName).append(", ").append(postalCode).append(", ").append(stateCode).append(", ").append(countryCode).append('\n').append(
		                            faxNumber).append('\n').append(
		                            phoneNumber).append(" \n").append(
		                            url).toString();

		                        iWantDocument.setVendorDescription(vendorInfo);
							
							}
							else{
								//Invalid vendor number
								LOG.error("Vendor with id: " + batchIWantDocument.getVendorNumber() + " does not exist.");
								noErrors = false;
							}
							}
							catch (NumberFormatException e) {
								LOG.error("Vendor id: " + batchIWantDocument.getVendorNumber() + " is not valid.");
								noErrors = false;
							}
						}
						else{
							//Invalid vendor number
							LOG.error("Vendor ID is not valid: " + batchIWantDocument.getVendorNumber());
							noErrors = false;
						}
		               
					}
					else{
						if (StringUtils.isNotEmpty(batchIWantDocument.getVendorName())) {
							iWantDocument.setVendorName(batchIWantDocument.getVendorName());
						}

						if (StringUtils.isNotEmpty(batchIWantDocument.getVendorDescription())) {
							iWantDocument.setVendorDescription(batchIWantDocument.getVendorDescription());
						}
					}

					// add items
					noErrors &= populateIWantDocItems(batchIWantDocument, iWantDocument);

					// add accounts
					noErrors &= populateIWantDocAccounts(batchIWantDocument, iWantDocument);

					// account Description
					if (StringUtils.isNotBlank(batchIWantDocument.getAccountDescriptionTxt())) {
						iWantDocument.setAccountDescriptionTxt(batchIWantDocument.getAccountDescriptionTxt());
					}

					if (StringUtils.isNotBlank(batchIWantDocument.getCommentsAndSpecialInstructions())) {
						iWantDocument.setCommentsAndSpecialInstructions(batchIWantDocument.getCommentsAndSpecialInstructions());
					}

					iWantDocument.setGoods(batchIWantDocument.isGoods());

					if (StringUtils.isNotBlank(batchIWantDocument.getServicePerformedOnCampus())) {
						iWantDocument.setServicePerformedOnCampus(batchIWantDocument.getServicePerformedOnCampus());
					}

					if (StringUtils.isNotBlank(batchIWantDocument.getCurrentRouteToNetId())) {

						Person adHocRouteTo = personService.getPersonByPrincipalName(batchIWantDocument.getCurrentRouteToNetId());

						if (ObjectUtils.isNull(adHocRouteTo)) {
							LOG.error("Ad Hoc Route to net ID is not valid: " + batchIWantDocument.getCurrentRouteToNetId());
							noErrors = false;
						} else {

							iWantDocument.setCurrentRouteToNetId(batchIWantDocument.getCurrentRouteToNetId());

							AdHocRoutePerson recipient = new AdHocRoutePerson();
							recipient.setId(iWantDocument.getCurrentRouteToNetId());
							recipient.setActionRequested(KewApiConstants.ACTION_REQUEST_APPROVE_REQ);

							adHocRoutePersons.add(recipient);

							iWantDocument.setAdHocRoutePersons(adHocRoutePersons);
						}

					}

					iWantDocumentService.setIWantDocumentDescription(iWantDocument);

					// add notes
					addNotes(iWantDocument, batchIWantDocument);

					boolean rulePassed = true;

					// call business rules
					rulePassed &= ruleService.applyRules(new SaveDocumentEvent("", iWantDocument));
					if (!rulePassed) {
						LOG.error("I Want document " + iWantDocument.getDocumentNumber() + "not saved due to errors");
						logErrorMessages();

					} else if (noErrors) {
						documentService.saveDocument(iWantDocument);
					}
				}
			}

        } catch (Exception e) {
            LOG.error("error while creating I Want document:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to create I Want document " + e.getMessage(), e);
        }

    }
    
    private void updateDefaultVendorAddress(VendorDetail vendor) {
        VendorAddress defaultAddress = vendorService.getVendorDefaultAddress(vendor.getVendorAddresses(), vendor.getVendorHeader().getVendorType().getAddressType().getVendorAddressTypeCode(), "");
        if (ObjectUtils.isNotNull(defaultAddress)) {

            if (ObjectUtils.isNotNull(defaultAddress.getVendorState())) {
                vendor.setVendorStateForLookup(defaultAddress.getVendorState().getName());
            } else {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Warning - unable to retrieve state for " + defaultAddress.getVendorCountryCode() + " / " + defaultAddress.getVendorStateCode() );
                }
                vendor.setVendorStateForLookup("");
            }
            vendor.setDefaultAddressLine1(defaultAddress.getVendorLine1Address());
            vendor.setDefaultAddressLine2(defaultAddress.getVendorLine2Address());
            vendor.setDefaultAddressCity(defaultAddress.getVendorCityName());
            vendor.setDefaultAddressPostalCode(defaultAddress.getVendorZipCode());
            vendor.setDefaultAddressStateCode(defaultAddress.getVendorStateCode());
            vendor.setDefaultAddressInternationalProvince(defaultAddress.getVendorAddressInternationalProvinceName());
            vendor.setDefaultAddressCountryCode(defaultAddress.getVendorCountryCode());
            vendor.setDefaultFaxNumber(defaultAddress.getVendorFaxNumber());
        } else {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Warning - default vendor address was null for " + vendor.getVendorNumber() + " / " + vendor.getVendorHeader().getVendorType().getAddressType().getVendorAddressTypeCode() );
            }
            vendor.setVendorStateForLookup("");
        }
    }

    /**
     * Populates the Requestor section of the I Want doc section.
     * 
     * @param initiator
     * @param batchIWantDocument
     * @param iWantDocument
     * @return true if no errors, false otherwise
     */
    protected boolean populateIWantDocRequestorSection(Person initiator, BatchIWantDocument batchIWantDocument, IWantDocument iWantDocument){
    	boolean noErrors = true;
        if (StringUtils.isBlank(batchIWantDocument.getInitiatorName())) {

            // populate with data from doc initiator
            String initiatorNetID = initiator.getPrincipalName();

            iWantDocument.setInitiatorNetID(initiatorNetID);

            String initiatorName = initiator.getName();
            String initiatorPhoneNumber = initiator.getPhoneNumber();
            String initiatorEmailAddress = initiator.getEmailAddress();

            String address = iWantDocumentService.getPersonCampusAddress(initiatorNetID);

            iWantDocument.setInitiatorName(initiatorName);
            iWantDocument.setInitiatorPhoneNumber(initiatorPhoneNumber);
            iWantDocument.setInitiatorEmailAddress(initiatorEmailAddress);
            iWantDocument.setInitiatorAddress(address);
        } else {
        	
            iWantDocument.setInitiatorName(batchIWantDocument.getInitiatorName());

            if (StringUtils.isNotBlank(batchIWantDocument.getInitiatorEmailAddress())) {
                iWantDocument.setInitiatorEmailAddress(batchIWantDocument.getInitiatorEmailAddress());
            }

            if (StringUtils.isNotBlank(batchIWantDocument.getInitiatorPhoneNumber())) {
                iWantDocument.setInitiatorPhoneNumber(batchIWantDocument.getInitiatorPhoneNumber());
            }

            if (StringUtils.isNotBlank(batchIWantDocument.getInitiatorAddress())) {
                iWantDocument.setInitiatorAddress(batchIWantDocument.getInitiatorAddress());
            }
        }
        
        return noErrors;
    }
    
    /**
     * Populates the Deliver To section of the I Want document.
     * 
     * @param batchIWantDocument
     * @param iWantDocument
     * 
     * @return true if no errors occur, false otherwise
     */
    protected boolean populateIWantDocDeliverToSection(BatchIWantDocument batchIWantDocument, IWantDocument iWantDocument){
    	boolean noErrors = true;
        if (batchIWantDocument.isSameAsInitiator()) {
            iWantDocument.setSameAsInitiator(true);
            iWantDocument.setDeliverToNetID(iWantDocument.getInitiatorNetID());
            iWantDocument.setDeliverToName(iWantDocument.getInitiatorName());
            iWantDocument.setDeliverToEmailAddress(iWantDocument.getInitiatorEmailAddress());
            iWantDocument.setDeliverToPhoneNumber(iWantDocument.getInitiatorPhoneNumber());
            iWantDocument.setDeliverToAddress(iWantDocument.getInitiatorAddress());
        } else {

            if (StringUtils.isNotBlank(batchIWantDocument.getDeliverToNetID())) {
            	 iWantDocument.setDeliverToNetID(batchIWantDocument.getDeliverToNetID());
                 Person deliverToPerson = personService.getPersonByPrincipalName(batchIWantDocument.getDeliverToNetID());

                 if (ObjectUtils.isNull(deliverToPerson)) {
                     LOG.error("Deliver to net ID is not valid: " + deliverToPerson);
                     noErrors = false;
                 }
                 else{
 	                String deliverToName = deliverToPerson.getName();
 	                String deliverToPhoneNumber = deliverToPerson.getPhoneNumber();
 	                String deliverToEmailAddress = deliverToPerson.getEmailAddress();
 	
 	                String address = iWantDocumentService.getPersonCampusAddress(batchIWantDocument.getDeliverToNetID());
 	                
 	
 	                iWantDocument.setDeliverToName(deliverToName);
 	
 	                if (StringUtils.isNotBlank(batchIWantDocument.getDeliverToEmailAddress())) {
 	                    iWantDocument.setDeliverToEmailAddress(batchIWantDocument.getDeliverToEmailAddress());
 	                }
 	                else{
 	                	iWantDocument.setDeliverToEmailAddress(deliverToEmailAddress);
 	                }
 	                if (StringUtils.isNotBlank(batchIWantDocument.getDeliverToPhoneNumber())) {
 	                    iWantDocument.setDeliverToPhoneNumber(batchIWantDocument.getDeliverToPhoneNumber());
 	                }
 	                else{
 	                	iWantDocument.setDeliverToPhoneNumber(deliverToPhoneNumber);
 	                }
 	
 	                if (StringUtils.isNotBlank(batchIWantDocument.getDeliverToAddress())) {
 	                    iWantDocument.setDeliverToAddress(batchIWantDocument.getDeliverToAddress());
 	                }
 	                else
 	                	 iWantDocument.setDeliverToAddress(address);
 	                }
             }
             else{
 	
 	            if (StringUtils.isNotBlank(batchIWantDocument.getDeliverToEmailAddress())) {
 	                iWantDocument.setDeliverToEmailAddress(batchIWantDocument.getDeliverToEmailAddress());
 	            }
 	
 	            if (StringUtils.isNotBlank(batchIWantDocument.getDeliverToPhoneNumber())) {
 	                iWantDocument.setDeliverToPhoneNumber(batchIWantDocument.getDeliverToPhoneNumber());
 	            }
 	
 	            if (StringUtils.isNotBlank(batchIWantDocument.getDeliverToAddress())) {
 	                iWantDocument.setDeliverToAddress(batchIWantDocument.getDeliverToAddress());
 	            }
             }
        }
        return noErrors;
    }
    
    /**
     * Populates I Want doc items.
     * 
     * @param batchIWantDocument
     * @param iWantDocument
     * @return true if no errors encountered, false otherwise
     */
    protected boolean populateIWantDocItems(BatchIWantDocument batchIWantDocument, IWantDocument iWantDocument) {
        LOG.info("Populate I Want doc items");

        boolean noErrors = true;
        // items
        List<IWantItem> iWantItems = batchIWantDocument.getItems();
        if (CollectionUtils.isNotEmpty(iWantItems)) {
            for (IWantItem item : iWantItems) {
                IWantItem addItem = new IWantItem();
                addItem.setItemDescription(item.getItemDescription());
                addItem.setItemTypeCode(item.getItemUnitOfMeasureCode());
                addItem.setItemCatalogNumber(item.getItemCatalogNumber());
                addItem.setItemUnitPrice(item.getItemUnitPrice());
                addItem.setPurchasingCommodityCode(item.getPurchasingCommodityCode());
                addItem.setItemQuantity(item.getItemQuantity());

                boolean rulePassed = ruleService.applyRules(new AddIWantItemEvent(StringUtils.EMPTY, iWantDocument, addItem));
                if (rulePassed) {
                    iWantDocument.addItem(addItem);
                } else {
                    logErrorMessages();
                }
                noErrors &= rulePassed;
            }
        }

        return noErrors;
    }

    /**
     * Populates the I Want document accounts
     * 
     * @param batchIWantDocument
     * @param iWantDocument
     * @return true if no errors encountered, false otherwise
     */
    protected boolean populateIWantDocAccounts(BatchIWantDocument batchIWantDocument, IWantDocument iWantDocument) {
        LOG.info("Populate I Want doc accounts");
        boolean noErrors = true;

        // accounts
        List<IWantAccount> iWantAccounts = batchIWantDocument.getAccounts();
        if (CollectionUtils.isNotEmpty(iWantAccounts)) {
            for (IWantAccount account : iWantAccounts) {
                IWantAccount addAccount = new IWantAccount();
                addAccount.setAccountNumber(account.getAccountNumber());
                addAccount.setSubAccountNumber(account.getSubAccountNumber());
                addAccount.setChartOfAccountsCode(account.getChartOfAccountsCode());
                addAccount.setFinancialObjectCode(account.getFinancialObjectCode());
                addAccount.setFinancialSubObjectCode(account.getFinancialSubObjectCode());
                addAccount.setOrganizationReferenceId(account.getOrganizationReferenceId());
                addAccount.setProjectCode(account.getProjectCode());
                addAccount.setAmountOrPercent(account.getAmountOrPercent());
                
                if("P".equalsIgnoreCase(account.getUseAmountOrPercent())){
                	addAccount.setUseAmountOrPercent(CUPurapConstants.PERCENT);    	
                }
                if("A".equalsIgnoreCase(account.getUseAmountOrPercent())){
                	addAccount.setUseAmountOrPercent(CUPurapConstants.AMOUNT);  
                }

                boolean rulePassed = ruleService.applyRules(new KualiAddLineEvent(iWantDocument, "accounts", addAccount));

                if (rulePassed) {
                    iWantDocument.addAccount(addAccount);
                } else {
                    logErrorMessages();
                }

                noErrors &= rulePassed;
            }
        }

        return noErrors;
    }

    /**
     * Adds notes to the I Want document.
     * @param document
     * @param batchIWantDocument
     */
    private void addNotes(IWantDocument document, BatchIWantDocument batchIWantDocument) {
        // set notes
        for (Iterator iterator = batchIWantDocument.getNotes().iterator(); iterator.hasNext();) {
            Note note = (Note) iterator.next();
            note.setRemoteObjectIdentifier(document.getObjectId());
            note.setAuthorUniversalIdentifier(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId());
            note.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
            note.setNotePostedTimestampToCurrent();
            note.setNoteText("Note: " + note.getNoteText());
            
            document.addNote(note); 
        }

    }

    /**
     * Logs error messages from GlobalVariables.
     */
    protected void logErrorMessages() {
        Map<String, AutoPopulatingList<ErrorMessage>> errors = GlobalVariables.getMessageMap().getErrorMessages();
        for (AutoPopulatingList<ErrorMessage> error : errors.values()) {
            Iterator<ErrorMessage> iterator = error.iterator();
            while (iterator.hasNext()) {
                ErrorMessage errorMessage = iterator.next();
                LOG.error(errorMessage.toString());
            }
        }

        GlobalVariables.getMessageMap().clearErrorMessages();
    }

    /**
     * Clears out associated .done files for the processed data files.
     * 
     * @param dataFileNames
     */
    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    /**
     * Gets the batchInputFileService.
     * 
     * @return Returns the batchInputFileService.
     */
    protected BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    /**
     * Sets the batchInputFileService.
     * 
     * @param batchInputFileService
     *            The batchInputFileService to set.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * Gets the iWantDocumentInputFileType.
     * 
     * @return iWantDocumentInputFileType
     */
    public BatchInputFileType getiWantDocumentInputFileType() {
        return iWantDocumentInputFileType;
    }

    /**
     * Sets the iWantDocumentInputFileType
     * 
     * @param iWantDocumentInputFileType
     */
    public void setiWantDocumentInputFileType(BatchInputFileType iWantDocumentInputFileType) {
        this.iWantDocumentInputFileType = iWantDocumentInputFileType;
    }

    /**
     * Gets the documentService.
     * 
     * @return documentService
     */
    public DocumentService getDocumentService() {
        return documentService;
    }

    /**
     * Sets the documentService.
     * 
     * @param documentService
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Gets the personService.
     * 
     * @return personService
     */
    public PersonService getPersonService() {
        return personService;
    }

    /**
     * Sets the personService.
     * 
     * @param personService
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Gets the iWantDocumentService.
     * 
     * @return iWantDocumentService
     */
    public IWantDocumentService getiWantDocumentService() {
        return iWantDocumentService;
    }

    /**
     * Sets the iWantDocumentService.
     * 
     * @param iWantDocumentService
     */
    public void setiWantDocumentService(IWantDocumentService iWantDocumentService) {
        this.iWantDocumentService = iWantDocumentService;
    }

    /**
     * Gets the ruleService.
     * 
     * @return ruleService
     */
    public KualiRuleService getRuleService() {
        return ruleService;
    }

    /**
     * Sets the ruleService.
     * 
     * @param ruleService
     */
    public void setRuleService(KualiRuleService ruleService) {
        this.ruleService = ruleService;
    }

	public AttachmentService getAttachmentService() {
		return attachmentService;
	}

	public void setAttachmentService(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}
	
    /**
     * Gets the mimeTypeProperties.
     * 
     * @return mimeTypeProperties
     */
    public Properties getMimeTypeProperties() {
		return mimeTypeProperties;
	}

	/**
	 * Sets the mimeTypeProperties.
	 * 
	 * @param mimeTypeProperties
	 */
	public void setMimeTypeProperties(Properties mimeTypeProperties) {
		this.mimeTypeProperties = mimeTypeProperties;
	}

	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	public VendorService getVendorService() {
		return vendorService;
	}

	public void setVendorService(VendorService vendorService) {
		this.vendorService = vendorService;
	}

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

}