package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.BillingAddress;
import org.kuali.kfs.module.purap.businessobject.DefaultPrincipalAddress;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchasingService;
import org.kuali.kfs.module.purap.document.web.struts.RequisitionForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.entity.KimEntityAddress;
import org.kuali.rice.kim.bo.entity.KimEntityEntityType;
import org.kuali.rice.kim.bo.entity.dto.KimEntityEntityTypeInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityInfo;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.bo.Attachment;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.bo.PersistableBusinessObject;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.mail.MailMessage;
import org.kuali.rice.kns.service.AttachmentService;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.MailService;
import org.kuali.rice.kns.service.NoteService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.service.PersistenceService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.businessobject.PersonData;
import edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class IWantDocumentServiceImpl implements IWantDocumentService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(IWantDocumentServiceImpl.class);

    private AttachmentService attachmentService;
    private LevelOrganizationDao collegeLevelOrganizationDao;
    private MailService mailService;
    private NoteService noteService;
    private PersonService personService;
    private ParameterService parameterService;
    private PurapService purapService;
    private PurchasingService purchasingService;
    private PersistenceService persistenceService;
    private BusinessObjectService businessObjectService;
    private DocumentTypeService documentTypeService;
    private DocumentService documentService;
    private FinancialSystemUserService financialSystemUserService;

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getPersonCampusAddress(java.lang.String)
     */
    public String getPersonCampusAddress(String principalName) {
        IdentityManagementService identityManagementService = SpringContext.getBean(IdentityManagementService.class);

        KimEntityInfo entityInfo = identityManagementService.getEntityInfoByPrincipalName(principalName);

        KimEntityEntityType entityEntityType = getPersonEntityType(entityInfo);

        KimEntityAddress foundAddress = getPersonEntityAddress(entityEntityType);

        String addressLine1 = foundAddress.getLine1Unmasked();
        String addressLine2 = foundAddress.getLine2Unmasked();
        String city = foundAddress.getCityNameUnmasked();
        String stateCode = foundAddress.getStateCodeUnmasked();
        String postalCode = foundAddress.getPostalCodeUnmasked();
        String countryCode = foundAddress.getCountryCodeUnmasked();

        String initiatorAddress = addressLine1 + "\n" + addressLine2 + "\n"
                + city + "\n" + stateCode + "\n"
                + postalCode + "\n" + countryCode;

        return initiatorAddress;
    }

    /**
     * Gets Person entity address.
     * 
     * @param entityEntityType
     * @return Person entity address
     */
    protected KimEntityAddress getPersonEntityAddress(KimEntityEntityType entityEntityType) {
        List<? extends KimEntityAddress> addresses = entityEntityType.getAddresses();
        KimEntityAddress foundAddress = null;
        int count = 0;

        while (count < addresses.size() && foundAddress == null) {
            final KimEntityAddress currentAddress = addresses.get(count);
            if (currentAddress.getAddressTypeCode().equals("CMP")) {
                foundAddress = currentAddress;
            }
            count += 1;
        }

        return foundAddress;
    }

    /**
     * Gets the person entity type.
     * 
     * @param entityInfo
     * @return person entity type
     */
    protected KimEntityEntityType getPersonEntityType(KimEntityInfo entityInfo) {
        final List<KimEntityEntityTypeInfo> entityEntityTypes = entityInfo.getEntityTypes();
        int count = 0;
        KimEntityEntityType foundInfo = null;

        while (count < entityEntityTypes.size() && foundInfo == null) {
            if (entityEntityTypes.get(count).getEntityTypeCode().equals(KimConstants.EntityTypes.PERSON)) {
                foundInfo = entityEntityTypes.get(count);
            }
            count += 1;
        }

        return foundInfo;
    }

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getCLevelOrganizations()
     */
    public List<LevelOrganization> getCLevelOrganizations() {
        return collegeLevelOrganizationDao.getCLevelOrganizations();
    }

    /**
     * Gets the collegeLevelOrganizationDao.
     * 
     * @return collegeLevelOrganizationDao
     */
    public LevelOrganizationDao getCollegeLevelOrganizationDao() {
        return collegeLevelOrganizationDao;
    }

    /**
     * Sets the collegeLevelOrganizationDao.
     * 
     * @param collegeLevelOrganizationDao
     */
    public void setCollegeLevelOrganizationDao(LevelOrganizationDao collegeLevelOrganizationDao) {
        this.collegeLevelOrganizationDao = collegeLevelOrganizationDao;
    }

    /**
     * 
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getDLevelOrganizations(java.lang.String)
     */
    public List<LevelOrganization> getDLevelOrganizations(String cLevelOrg) {
        return collegeLevelOrganizationDao.getDLevelOrganizations(cLevelOrg);
    }

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getDLevelOrganizationsString(java.lang.String)
     */
    public String getDLevelOrganizationsString(String cLevelOrg) {
        List<LevelOrganization> dLevelOrgs = getDLevelOrganizations(cLevelOrg);
        StringBuffer dLevelOrgsString = new StringBuffer("");

        for (LevelOrganization organization : dLevelOrgs) {
            dLevelOrgsString.append(organization.getCode() + " " + organization.getCodeAndDescription() + "#");
        }

        return dLevelOrgsString.toString();
    }

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getCLevelOrganizationForDLevelOrg(java.lang.String)
     */
    public String getCLevelOrganizationForDLevelOrg(String dLevelOrg) {
        return collegeLevelOrganizationDao.getCLevelOrganizationForDLevelOrg(dLevelOrg);
    }

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getPersonData(java.lang.String)
     */
    public PersonData getPersonData(String principalName) {

        PersonData personData = new PersonData();

        Person person = personService.getPersonByPrincipalName(principalName);
        personData.setPersonName(person.getNameUnmasked());
        personData.setNetID(principalName);
        personData.setEmailAddress(person.getEmailAddressUnmasked());
        personData.setPhoneNumber(person.getPhoneNumberUnmasked());
        personData.setCampusAddress(getPersonCampusAddress(principalName));

        return personData;
    }

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#sendDocumentFinalizedMessage(edu.cornell.kfs.module.purap.document.IWantDocument)
     */
    public void sendDocumentFinalizedMessage(IWantDocument iWantDocument) {
        MailMessage message = buildDocumentFinalizedMessage(iWantDocument);

        try {
            mailService.sendMessage(message);
        } catch (Exception e) {
            // Don't stop the show if the email has problem, log it and continue.
            LOG.error("iWantDocumentService: email problem. Message not sent.", e);
        }
    }

    /**
     * Builds an email message to be sent when the input document has been finalized.
     * 
     * @param iWantDocument
     * @return an email message to be sent when the input document has been finalized
     */
    private MailMessage buildDocumentFinalizedMessage(IWantDocument iWantDocument) {

        KualiWorkflowDocument workflowDocument = iWantDocument.getDocumentHeader().getWorkflowDocument();
        String initiator = workflowDocument.getInitiatorPrincipalId();
        String documentNumber = iWantDocument.getDocumentNumber();
        Person initiatorPerson = personService.getPerson(initiator);
        String initiatorEmail = initiatorPerson.getEmailAddressUnmasked();

        MailMessage message = new MailMessage();

        message.addToAddress(initiatorEmail);
        message.setFromAddress(initiatorEmail);

        message.setSubject("I Want document: " + documentNumber + " has been finalized");

        StringBuffer emailBody = new StringBuffer();
        String vendorName = iWantDocument.getVendorName();
        
        if (vendorName == null) {
            vendorName = StringUtils.EMPTY;
        }

        emailBody.append("This is a message to inform you that the I Want document "
                + iWantDocument.getDocumentNumber() + " has been finalized" + ": \n\n");
        emailBody.append("From: " + initiatorPerson.getNameUnmasked() + "\n");
        emailBody.append("Title: " + iWantDocument.getDocumentTitle() + "\n");
        emailBody.append("Type: "
                + documentTypeService.getDocumentTypeVO(workflowDocument.getDocumentType()).getDocTypeLabel() + "\n");
        emailBody.append("Id: " + documentNumber + "\n");
        emailBody.append("Vendor Name: " + vendorName + "\n\n");

        String docUrl = getDocumentURL(documentNumber, workflowDocument);

        emailBody.append(" Go here to view this item: " + docUrl + "\n");

        message.setMessage(emailBody.toString());

        return message;
    }

    /**
     * Builds the document URL.
     * 
     * @param documentNumber
     * @param workflowDocument
     * @return the document URL
     */
    private String getDocumentURL(String documentNumber, KualiWorkflowDocument workflowDocument) {
        String docUrl = workflowDocument.getRouteHeader().getDocumentUrl();

        if (StringUtils.isNotBlank(docUrl)) {
            if (docUrl.indexOf("?") == -1) {
                docUrl += "?";
            } else {
                docUrl += "&";
            }
            docUrl += KEWConstants.ROUTEHEADER_ID_PARAMETER + "="
                    + documentNumber;
            docUrl += "&" + KEWConstants.COMMAND_PARAMETER + "="
                    + KEWConstants.ACTIONLIST_COMMAND;
        }

        return docUrl;
    }

    /**
     * 
     * @throws Exception
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#setUpRequisitionDetailsFromIWantDoc(edu.cornell.kfs.module.purap.document.IWantDocument,
     * org.kuali.kfs.module.purap.document.RequisitionDocument)
     */
    public RequisitionDocument setUpRequisitionDetailsFromIWantDoc(IWantDocument iWantDocument,
            RequisitionDocument requisitionDocument, RequisitionForm requisitionForm) throws Exception {

        //Person requestertUser = personService.getPerson(iWantDocument.getDocumentHeader().getWorkflowDocument()
                //.getInitiatorPrincipalId());

        //requisitionDocument.getDocumentHeader().setDocumentDescription(
                //iWantDocument.getDepartmentLevelOrganization() + ", "
                        //+ requestertUser.getLastNameUnmasked() + ", "
                        //+ "I Want doc #" + iWantDocument.getDocumentNumber());
        
        requisitionDocument.getDocumentHeader().setDocumentDescription(
        		iWantDocument.getDocumentHeader().getDocumentDescription());

        //set req explanation field to I Want doc business purpose
        requisitionDocument.getDocumentHeader().setExplanation(iWantDocument.getExplanation());

        requisitionDocument.setRequisitionSourceCode(PurapConstants.RequisitionSources.IWNT);
        requisitionDocument.setStatusCode(PurapConstants.RequisitionStatuses.IN_PROCESS);

        requisitionDocument.setPurchaseOrderCostSourceCode(PurapConstants.POCostSources.ESTIMATE);
        requisitionDocument.setPurchaseOrderTransmissionMethodCode(parameterService.getParameterValue(
                RequisitionDocument.class, PurapParameterConstants.PURAP_DEFAULT_PO_TRANSMISSION_CODE));
        requisitionDocument.setDocumentFundingSourceCode(parameterService.getParameterValue(RequisitionDocument.class,
                PurapParameterConstants.DEFAULT_FUNDING_SOURCE));
        requisitionDocument.setUseTaxIndicator(purchasingService.getDefaultUseTaxIndicatorValue(requisitionDocument));

        // set up document link identifier.
        requisitionDocument.setAccountsPayablePurchasingDocumentLinkIdentifier(iWantDocument.getAccountsPayablePurchasingDocumentLinkIdentifier());
        
        // save doc before adding attachments
        purapService.saveDocumentNoValidation(requisitionDocument);

        // copy attachments from I Want document
        copyIWantDocAttachments(requisitionDocument, iWantDocument);

        // set up deliver to section
        setUpDeliverToSectionOfReqDoc(requisitionDocument, iWantDocument);

        // set up items tab
        setUpItemsTabForReqDoc(requisitionDocument, iWantDocument);

        //populate vendor
        setUpVendorSectionForReqDoc(requisitionDocument, iWantDocument);

        //set up accounting lines from IWant Doc
        copyIWantDocAccountingLinesToReqDoc(requisitionDocument, iWantDocument, requisitionForm);
        requisitionForm.setHideDistributeAccounts(false);

        return requisitionDocument;
    }

    /**
     * Sets up the deliver to section of the Requisition document based on the information
     * on the I Want Document.
     * 
     * @param requisitionDocument
     * @param iWantDocument
     */
    private void setUpDeliverToSectionOfReqDoc(RequisitionDocument requisitionDocument, IWantDocument iWantDocument) {

        Person deliverTo = null;

        if (StringUtils.isNotBlank(iWantDocument.getDeliverToNetID())) {

            requisitionDocument.setDeliveryBuildingRoomNumber(KFSConstants.NOT_AVAILABLE_STRING);
            requisitionDocument.setDeliveryCountryCode("US");
            requisitionDocument.setDeliveryBuildingOtherIndicator(true);

            deliverTo = personService.getPersonByPrincipalName(iWantDocument.getDeliverToNetID());

            if (ObjectUtils.isNotNull(deliverTo)) {
                Person currentUser = GlobalVariables.getUserSession().getPerson();
                ChartOrgHolder purapChartOrg = financialSystemUserService.getPrimaryOrganization(currentUser,
                        PurapConstants.PURAP_NAMESPACE);
                if (ObjectUtils.isNotNull(purapChartOrg)) {
                    requisitionDocument.setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
                    requisitionDocument.setOrganizationCode(purapChartOrg.getOrganizationCode());
                }
                requisitionDocument.setDeliveryCampusCode(deliverTo.getCampusCode());
                requisitionDocument.setDeliveryToName(iWantDocument.getDeliverToName());
                requisitionDocument.setDeliveryToEmailAddress(iWantDocument.getDeliverToEmailAddress());
                requisitionDocument.setDeliveryToPhoneNumber(iWantDocument.getDeliverToPhoneNumber());
                requisitionDocument.setRequestorPersonName(iWantDocument.getInitiatorName());
                requisitionDocument.setRequestorPersonEmailAddress(iWantDocument.getInitiatorEmailAddress());
                requisitionDocument.setRequestorPersonPhoneNumber(iWantDocument.getInitiatorPhoneNumber());
                parseAndSetRequestorAddress(iWantDocument.getDeliverToAddress(), requisitionDocument);

                requisitionDocument.setOrganizationAutomaticPurchaseOrderLimit(purapService.getApoLimit(
                        requisitionDocument.getVendorContractGeneratedIdentifier(),
                        requisitionDocument.getChartOfAccountsCode(), requisitionDocument.getOrganizationCode()));

                // populate billing address
                BillingAddress billingAddress = new BillingAddress();
                billingAddress.setBillingCampusCode(requisitionDocument.getDeliveryCampusCode());
                Map keys = persistenceService.getPrimaryKeyFieldValues(billingAddress);
                billingAddress = (BillingAddress) businessObjectService.findByPrimaryKey(BillingAddress.class, keys);
                requisitionDocument.templateBillingAddress(billingAddress);
            }
        }

        if (StringUtils.isBlank(iWantDocument.getDeliverToNetID()) || ObjectUtils.isNull(deliverTo)) {
            // populate requisition fields from I Want doc initiator

            deliverTo = personService.getPerson(iWantDocument.getDocumentHeader().getWorkflowDocument()
                    .getInitiatorPrincipalId());

            if (ObjectUtils.isNotNull(deliverTo)) {

                Person currentUser = GlobalVariables.getUserSession().getPerson();
                ChartOrgHolder purapChartOrg = financialSystemUserService.getPrimaryOrganization(currentUser,
                        PurapConstants.PURAP_NAMESPACE);

                if (ObjectUtils.isNotNull(purapChartOrg)) {
                    requisitionDocument.setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
                    requisitionDocument.setOrganizationCode(purapChartOrg.getOrganizationCode());
                }
                requisitionDocument.setDeliveryCampusCode(deliverTo.getCampusCode());
                requisitionDocument.setDeliveryToName(deliverTo.getName());
                requisitionDocument.setDeliveryToEmailAddress(deliverTo.getEmailAddressUnmasked());
                requisitionDocument.setDeliveryToPhoneNumber(SpringContext.getBean(PhoneNumberService.class)
                        .formatNumberIfPossible(deliverTo.getPhoneNumber()));
                requisitionDocument.setRequestorPersonName(deliverTo.getName());
                requisitionDocument.setRequestorPersonEmailAddress(deliverTo.getEmailAddressUnmasked());
                requisitionDocument.setRequestorPersonPhoneNumber(SpringContext.getBean(PhoneNumberService.class)
                        .formatNumberIfPossible(deliverTo.getPhoneNumber()));

                DefaultPrincipalAddress defaultPrincipalAddress = new DefaultPrincipalAddress(
                        deliverTo.getPrincipalId());
                Map addressKeys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(
                        defaultPrincipalAddress);
                defaultPrincipalAddress = (DefaultPrincipalAddress) SpringContext.getBean(BusinessObjectService.class)
                        .findByPrimaryKey(DefaultPrincipalAddress.class, addressKeys);

                if (ObjectUtils.isNotNull(defaultPrincipalAddress)
                        && ObjectUtils.isNotNull(defaultPrincipalAddress.getBuilding())) {
                    if (defaultPrincipalAddress.getBuilding().isActive()) {
                        requisitionDocument.setDeliveryCampusCode(defaultPrincipalAddress.getCampusCode());
                        requisitionDocument.templateBuildingToDeliveryAddress(defaultPrincipalAddress.getBuilding());
                        requisitionDocument.setDeliveryBuildingRoomNumber(defaultPrincipalAddress
                                .getBuildingRoomNumber());
                    } else {
                        //since building is now inactive, delete default building record
                        SpringContext.getBean(BusinessObjectService.class).delete(defaultPrincipalAddress);
                    }
                }

                // set the APO limit
                requisitionDocument
                        .setOrganizationAutomaticPurchaseOrderLimit(SpringContext.getBean(PurapService.class)
                                .getApoLimit(requisitionDocument.getVendorContractGeneratedIdentifier(),
                                        requisitionDocument.getChartOfAccountsCode(),
                                        requisitionDocument.getOrganizationCode()));

                // populate billing address
                BillingAddress billingAddress = new BillingAddress();
                billingAddress.setBillingCampusCode(requisitionDocument.getDeliveryCampusCode());
                Map keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(billingAddress);
                billingAddress = (BillingAddress) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(
                        BillingAddress.class, keys);
                requisitionDocument.templateBillingAddress(billingAddress);

            }
        }
    }

    private void parseAndSetRequestorAddress(String requestorAddress, RequisitionDocument requisitionDocument) {

        if (StringUtils.isNotBlank(requestorAddress)) {
            String[] addressParts = requestorAddress.split("\n");

            if (addressParts.length != 0) {

                if (addressParts.length >= 1) {
                    requisitionDocument.setDeliveryBuildingLine1Address(addressParts[0]);
                }

                if (addressParts.length >= 2) {
                    requisitionDocument.setDeliveryBuildingLine2Address(addressParts[1]);
                }

                if (addressParts.length >= 3) {
                    requisitionDocument.setDeliveryCityName(addressParts[2]);
                }

                if (addressParts.length >= 4) {
                    requisitionDocument.setDeliveryStateCode(addressParts[3]);
                }

                if (addressParts.length >= 5) {
                    requisitionDocument.setDeliveryPostalCode(addressParts[4]);
                }

                if (addressParts.length >= 6) {
                    requisitionDocument.setDeliveryCountryCode(addressParts[5]);
                }

            }
        }

    }

    /**
     * Set up items on the Requisition document based on the information on the I Want
     * document.
     * 
     * @param requisitionDocument
     * @param iWantDocument
     */
    private void setUpItemsTabForReqDoc(RequisitionDocument requisitionDocument, IWantDocument iWantDocument) {
        List<IWantItem> iWantItems = iWantDocument.getItems();

        if (iWantDocument.getItems() != null && iWantItems.size() > 0) {

            for (IWantItem iWantItem : iWantItems) {
                RequisitionItem requisitionItem = new RequisitionItem();

                requisitionItem.setItemQuantity(iWantItem.getItemQuantity());
                requisitionItem.setItemDescription(iWantItem.getItemDescription());
                requisitionItem.setItemUnitOfMeasureCode(iWantItem.getItemUnitOfMeasureCode());
                requisitionItem.setItemCatalogNumber(iWantItem.getItemCatalogNumber());
                requisitionItem.setItemUnitPrice(iWantItem.getItemUnitPrice());

                requisitionDocument.addItem(requisitionItem);

            }
        }
    }

    private void setUpVendorSectionForReqDoc(RequisitionDocument requisitionDocument, IWantDocument iWantDocument) {

        requisitionDocument.setVendorDetailAssignedIdentifier(iWantDocument.getVendorDetailAssignedIdentifier());
        requisitionDocument.setVendorHeaderGeneratedIdentifier(iWantDocument.getVendorHeaderGeneratedIdentifier());

        if (requisitionDocument.getVendorDetailAssignedIdentifier() != null
                && requisitionDocument.getVendorHeaderGeneratedIdentifier() != null) {
            requisitionDocument.setVendorContractGeneratedIdentifier(null);
            requisitionDocument.refreshReferenceObject("vendorContract");

            // retrieve vendor based on selection from vendor lookup
            requisitionDocument.refreshReferenceObject("vendorDetail");
            requisitionDocument.templateVendorDetail(requisitionDocument.getVendorDetail());

            // populate default address based on selected vendor
            VendorAddress defaultAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(
                    requisitionDocument.getVendorDetail().getVendorAddresses(),
                    requisitionDocument.getVendorDetail().getVendorHeader().getVendorType().getAddressType()
                            .getVendorAddressTypeCode(), requisitionDocument.getDeliveryCampusCode());
            requisitionDocument.templateVendorAddress(defaultAddress);

            //vendor address holds method of po transmission that should be used
            requisitionDocument.setPurchaseOrderTransmissionMethodCode(defaultAddress
                    .getPurchaseOrderTransmissionMethodCode());
        } else {
            if (StringUtils.isNotBlank(iWantDocument.getVendorName())) {
                requisitionDocument.setVendorName(iWantDocument.getVendorName());
            }
        }

    }

    /**
     * Copies all attachments from the I Want document to the requisition document
     * 
     * @param requisitionDocument
     * @param iWantDocument
     * @param requisitionForm
     * @throws Exception
     */
    private void copyIWantDocAttachments(AccountingDocument document, IWantDocument iWantDocument) throws Exception {
        
        purapService.saveDocumentNoValidation(document);
        if (iWantDocument.getDocumentHeader().getBoNotes() != null
              && iWantDocument.getDocumentHeader().getBoNotes().size() > 0) {

          for (Iterator iterator = iWantDocument.getDocumentHeader().getBoNotes().iterator(); iterator.hasNext();) {
              Note note = (Note) iterator.next();
                try {
                    Note copyingNote = SpringContext.getBean(DocumentService.class).createNoteFromDocument(document, note.getNoteText());
                    purapService.saveDocumentNoValidation(document);
                    copyingNote.setNotePostedTimestamp(note.getNotePostedTimestamp());
                    copyingNote.setAuthorUniversalIdentifier(note.getAuthorUniversalIdentifier());
                    copyingNote.setNoteTopicText(note.getNoteTopicText());
                    Attachment originalAttachment = SpringContext.getBean(AttachmentService.class).getAttachmentByNoteId(note.getNoteIdentifier());
                    if (originalAttachment != null) {
                        Attachment newAttachment = SpringContext.getBean(AttachmentService.class).createAttachment((PersistableBusinessObject)copyingNote, originalAttachment.getAttachmentFileName(), originalAttachment.getAttachmentMimeTypeCode(), originalAttachment.getAttachmentFileSize().intValue(), originalAttachment.getAttachmentContents(), originalAttachment.getAttachmentTypeCode());//new Attachment();

                        if (ObjectUtils.isNotNull(originalAttachment) && ObjectUtils.isNotNull(newAttachment)) {
                            copyingNote.addAttachment(newAttachment);
                        }
                        document.addNote(copyingNote);

                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        purapService.saveDocumentNoValidation(document);

    }
    
    private void copyIWantdDocAttachmentsToDV(DisbursementVoucherDocument dvDocument, DisbursementVoucherForm disbursementVoucherForm, IWantDocument iWantDocument) {
        
        purapService.saveDocumentNoValidation(dvDocument);
        if (iWantDocument.getDocumentHeader().getBoNotes() != null && iWantDocument.getDocumentHeader().getBoNotes().size() > 0) {

            for (Iterator iterator = iWantDocument.getDocumentHeader().getBoNotes().iterator(); iterator.hasNext();) {
                Note note = (Note) iterator.next();

                Note copyNote;
                try {
                    copyNote = noteService.createNote(new Note(), dvDocument.getDocumentHeader());

                    copyNote.setNoteText(note.getNoteText());
                    copyNote.setAuthorUniversalIdentifier(note.getAuthorUniversalIdentifier());
                    copyNote.setRemoteObjectIdentifier(dvDocument.getObjectId());
                    copyNote.setNotePostedTimestamp(note.getNotePostedTimestamp());

                    String attachmentType = StringUtils.EMPTY;

                    Attachment attachment = note.getAttachment();
                    if (attachment != null) {
                        Note newNote = disbursementVoucherForm.getNewNote();
                        String propertyName = getNoteService().extractNoteProperty(newNote);
                        // get BO to set
                        PersistableBusinessObject noteParent = (PersistableBusinessObject) ObjectUtils.getPropertyValue(dvDocument, propertyName);

                        Attachment copyAttachment = attachmentService.createAttachment(noteParent, attachment.getAttachmentFileName(), attachment.getAttachmentMimeTypeCode(), attachment.getAttachmentFileSize().intValue(), attachment.getAttachmentContents(), attachment.getAttachmentTypeCode());

                            if (copyAttachment != null) {
                                copyNote.addAttachment(copyAttachment);
                                getNoteService().save(copyNote);
                                dvDocument.getDocumentHeader().addNote(copyNote);
                                purapService.saveDocumentNoValidation(dvDocument);
                                
                            }     
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }


    /**
     * Copies the accounting lines from the I Want document to the Requisition document
     * 
     * @param requisitionDocument
     * @param iWantDocument
     * @param requisitionForm
     */
    private void copyIWantDocAccountingLinesToReqDoc(RequisitionDocument requisitionDocument,
            IWantDocument iWantDocument, RequisitionForm requisitionForm) {

        int itemsSize = iWantDocument.getItems() != null ? iWantDocument.getItems().size() : 0;
        int accountsSize = iWantDocument.getAccounts() != null ? iWantDocument.getAccounts().size() : 0;

        // we only add accounts information if there or only one item or there is only one account, otherwise ignore
        if (itemsSize == 1 || accountsSize == 1) {

            for (IWantAccount iWantAccount : iWantDocument.getAccounts()) {

                PurApAccountingLine requisitionAccount = requisitionForm.getAccountDistributionnewSourceLine();

                requisitionAccount.setChartOfAccountsCode(iWantAccount.getChartOfAccountsCode());
                requisitionAccount.setAccountNumber(iWantAccount.getAccountNumber());
                requisitionAccount.setSubAccountNumber(iWantAccount.getSubAccountNumber());
                requisitionAccount.setFinancialObjectCode(iWantAccount.getFinancialObjectCode());
                requisitionAccount.setFinancialSubObjectCode(iWantAccount.getFinancialSubObjectCode());
                requisitionAccount.setProjectCode(iWantAccount.getProjectCode());
                requisitionAccount.setOrganizationReferenceId(iWantAccount.getOrganizationReferenceId());

                if (CUPurapConstants.PERCENT.equalsIgnoreCase(iWantAccount.getUseAmountOrPercent())) {
                    requisitionAccount.setAccountLinePercent((iWantAccount.getAmountOrPercent().bigDecimalValue().setScale(0, RoundingMode.HALF_UP)));
                } else {
                    //compute amount based on percent
                    BigDecimal requisitionAccountPercent = BigDecimal.ZERO;
                    KualiDecimal iWantDocTotalDollarAmount = iWantDocument.getTotalDollarAmount();

                    if (iWantDocTotalDollarAmount != null && iWantAccount.getAmountOrPercent() != null
                            && iWantDocTotalDollarAmount.isNonZero() && iWantAccount.getAmountOrPercent().isNonZero()) {
                        requisitionAccountPercent = (iWantAccount.getAmountOrPercent()
                                .divide(iWantDocTotalDollarAmount)
                                .bigDecimalValue()).multiply(new BigDecimal("100"));
                        
                        requisitionAccountPercent = requisitionAccountPercent.setScale(0, RoundingMode.HALF_UP);
                    }
                    requisitionAccount.setAccountLinePercent(requisitionAccountPercent);
                }

                requisitionForm.addAccountDistributionsourceAccountingLine(requisitionAccount);
            }
        }
    }
    
    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#setUpDVDetailsFromIWantDoc(edu.cornell.kfs.module.purap.document.IWantDocument, org.kuali.kfs.fp.document.DisbursementVoucherDocument, org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm)
     */
    public DisbursementVoucherDocument setUpDVDetailsFromIWantDoc(IWantDocument iWantDocument, DisbursementVoucherDocument disbursementVoucherDocument, DisbursementVoucherForm disbursementVoucherForm) throws Exception {
        
        // DV explanation = I Want Doc business purpose
        disbursementVoucherDocument.getDocumentHeader().setExplanation(iWantDocument.getDocumentHeader().getExplanation());
        // DV desc = IWantDoc desc
        disbursementVoucherDocument.getDocumentHeader().setDocumentDescription(iWantDocument.getDocumentHeader().getDocumentDescription());
        
        //copy over attachments
        copyIWantdDocAttachmentsToDV(disbursementVoucherDocument, disbursementVoucherForm, iWantDocument);
        
        //DV check amount - IWantDoc total amount
        disbursementVoucherDocument.setDisbVchrCheckTotalAmount(iWantDocument.getTotalDollarAmount());
  
        // default bank code
        Bank defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(DisbursementVoucherDocument.class);
        if (defaultBank != null) {
            disbursementVoucherDocument.setDisbVchrBankCode( defaultBank.getBankCode());
            disbursementVoucherDocument.setBank( defaultBank);
        }
        
        purapService.saveDocumentNoValidation(disbursementVoucherDocument);
        
        //populate accounting lines
        copyIWantDocAccountingLinesToDVDoc(disbursementVoucherDocument, iWantDocument, disbursementVoucherForm);
 
        return disbursementVoucherDocument;
    }
    

    /**
     * Copies the accounting lines from the I Want document to the Requisition document
     * 
     * @param requisitionDocument
     * @param iWantDocument
     * @param requisitionForm
     */
    private void copyIWantDocAccountingLinesToDVDoc(DisbursementVoucherDocument disbursementVoucherDocument,
            IWantDocument iWantDocument, DisbursementVoucherForm disbursementVoucherForm) {

        int accountsSize = iWantDocument.getAccounts() != null ? iWantDocument.getAccounts().size() : 0;

        if (accountsSize > 0) {

            for (IWantAccount iWantAccount : iWantDocument.getAccounts()) {

                SourceAccountingLine dvAccount = disbursementVoucherForm.getNewSourceLine();
                dvAccount.setChartOfAccountsCode(iWantAccount.getChartOfAccountsCode());
                dvAccount.setAccountNumber(iWantAccount.getAccountNumber());
                dvAccount.setSubAccountNumber(iWantAccount.getSubAccountNumber());
                dvAccount.setFinancialObjectCode(iWantAccount.getFinancialObjectCode());
                dvAccount.setFinancialSubObjectCode(iWantAccount.getFinancialSubObjectCode());
                dvAccount.setProjectCode(iWantAccount.getProjectCode());
                dvAccount.setOrganizationReferenceId(iWantAccount.getOrganizationReferenceId());

                if (CUPurapConstants.PERCENT.equalsIgnoreCase(iWantAccount.getUseAmountOrPercent()) && iWantAccount.getAmountOrPercent()!=null) {
                    //compute amount
                    KualiDecimal lineAmount = iWantAccount.getAmountOrPercent().multiply(iWantDocument.getTotalDollarAmount());
                    if(lineAmount.isNonZero()){
                        lineAmount = lineAmount.divide(new KualiDecimal(100));
                    }
                    dvAccount.setAmount(lineAmount);
                } else {
                   
                    dvAccount.setAmount(iWantAccount.getAmountOrPercent());
                }

                disbursementVoucherDocument.addSourceAccountingLine(dvAccount);
                disbursementVoucherForm.setNewSourceLine(null);
            }
        }
    }
    
    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getIWantDocIDByDVId(java.lang.String)
     */
    public String getIWantDocIDByDVId(String dvID) {
        String iWantDocID = StringUtils.EMPTY;
        Map<String,String> fieldValues = new HashMap<String, String>();
        fieldValues.put("dvDocId", dvID);
        Collection<IWantDocument> results= businessObjectService.findMatching(IWantDocument.class, fieldValues);
        if(ObjectUtils.isNotNull(results)){
            iWantDocID = results.iterator().next().getDocumentNumber();
        }
        
        return iWantDocID;
    }
    

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#isDVgeneratedByIWantDoc(java.lang.String)
     */
    public boolean isDVgeneratedByIWantDoc(String dvID) {
        String iWantDocID = StringUtils.EMPTY;
        Map<String,String> fieldValues = new HashMap<String, String>();
        fieldValues.put("dvDocId", dvID);
        int count = businessObjectService.countMatching(IWantDocument.class, fieldValues);
        
        if(count != 0){
            return true;
        }
        else
            return false;
        
    }


    /**
     * Gets the mailService.
     * 
     * @return mailService
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * Sets the mailService.
     * 
     * @param mailService
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
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
     * Gets the documentTypeService.
     * 
     * @return documentTypeService
     */
    public DocumentTypeService getDocumentTypeService() {
        return documentTypeService;
    }

    /**
     * Sets the documentTypeService.
     * 
     * @param documentTypeService
     */
    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public PurchasingService getPurchasingService() {
        return purchasingService;
    }

    public void setPurchasingService(PurchasingService purchasingService) {
        this.purchasingService = purchasingService;
    }

    public FinancialSystemUserService getFinancialSystemUserService() {
        return financialSystemUserService;
    }

    public void setFinancialSystemUserService(FinancialSystemUserService financialSystemUserService) {
        this.financialSystemUserService = financialSystemUserService;
    }

    public PurapService getPurapService() {
        return purapService;
    }

    public void setPurapService(PurapService purapService) {
        this.purapService = purapService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public AttachmentService getAttachmentService() {
        return attachmentService;
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public NoteService getNoteService() {
        return noteService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

}
