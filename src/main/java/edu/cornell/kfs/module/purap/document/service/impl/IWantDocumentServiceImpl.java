package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.identity.address.EntityAddress;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmployment;
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.RequisitionStatuses;
import org.kuali.kfs.module.purap.businessobject.BillingAddress;
import org.kuali.kfs.module.purap.businessobject.DefaultPrincipalAddress;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurchasingService;
import org.kuali.kfs.module.purap.document.web.struts.RequisitionForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.vnd.service.PhoneNumberService;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantDocUserOptions;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.businessobject.PersonData;
import edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.CuPurapService;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.sys.CUKFSConstants.ConfidentialAttachmentTypeCodes;
import edu.cornell.kfs.sys.businessobject.NoteExtendedAttribute;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;

public class IWantDocumentServiceImpl implements IWantDocumentService {

	private static final Logger LOG = LogManager.getLogger(IWantDocumentServiceImpl.class);

    private static final String CMP_ADDRESS_TYPE = "CMP";

    
    
    private LevelOrganizationDao collegeLevelOrganizationDao;
    private AttachmentService attachmentService;
    private NoteService noteService;
    private ParameterService parameterService;
    private CuPurapService purapService;
    private BusinessObjectService businessObjectService;
    private PersonService personService;
    private FinancialSystemUserService financialSystemUserService;
    private EmailService emailService;
    private PersistenceService persistenceService;
    private DocumentService documentService;
    private PhoneNumberService phoneNumberService;
    private ConfigurationService configurationService;

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getPersonCampusAddress(java.lang.String)
     */
    public String getPersonCampusAddress(String principalName) {
    
        EntityAddress foundAddress = getPersonEntityAddress(principalName);

        String addressLine1 = StringUtils.trimToEmpty(foundAddress.getLine1Unmasked());
        String addressLine2 = StringUtils.trimToEmpty(foundAddress.getLine2Unmasked());
        String city = StringUtils.trimToEmpty(foundAddress.getCityUnmasked());
        String stateCode = StringUtils.trimToEmpty(foundAddress.getStateProvinceCodeUnmasked());
        String postalCode = StringUtils.trimToEmpty(foundAddress.getPostalCodeUnmasked());
        String countryCode = StringUtils.trimToEmpty(foundAddress.getCountryCodeUnmasked());

        String initiatorAddress = new StringBuilder(100).append(addressLine1).append(KRADConstants.NEWLINE).append(
                addressLine2).append(KRADConstants.NEWLINE).append(
                city).append(KRADConstants.NEWLINE).append(stateCode).append(KRADConstants.NEWLINE).append(
                postalCode).append(KRADConstants.NEWLINE).append(countryCode).toString();

        return initiatorAddress;
    }

    /**
     * Gets Person entity address.
     * 
     * @param entityEntityType
     * @return Person entity address
     */
    protected EntityAddress getPersonEntityAddress(String principalName) {
        List<? extends EntityAddress> addresses = KimApiServiceLocator.getIdentityService().getEntityByPrincipalName(
                principalName).getEntityTypeContactInfoByTypeCode(KimConstants.EntityTypes.PERSON).getAddresses();
        EntityAddress foundAddress = null;
        int count = 0;

        while (count < addresses.size() && foundAddress == null) {
            final EntityAddress currentAddress = addresses.get(count);
            if (CMP_ADDRESS_TYPE.equals(currentAddress.getAddressType().getCode())) {
                foundAddress = currentAddress;
            }
            count += 1;
        }

        return foundAddress;
    }

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getCLevelOrganizations()
     */
    public List<LevelOrganization> getCLevelOrganizations() {
        return collegeLevelOrganizationDao.getCLevelOrganizations();
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
        StringBuilder dLevelOrgsString = new StringBuilder();

        for (LevelOrganization organization : dLevelOrgs) {
            dLevelOrgsString.append(organization.getCode()).append(' ').append(organization.getCodeAndDescription()).append('#');
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
        BodyMailMessage message = buildDocumentFinalizedMessage(iWantDocument);

        try {
            emailService.sendMessage(message, false);
        } catch (Exception e) {
            LOG.error(("sendDocumentFinalizedMessage, Email could not be sent for IWNT edoc# " + iWantDocument.getDocumentNumber()), e);
        }
    }

    /**
     * Builds an email message to be sent when the input document has been finalized.
     * 
     * @param iWantDocument
     * @return an email message to be sent when the input document has been finalized
     */
    private BodyMailMessage buildDocumentFinalizedMessage(IWantDocument iWantDocument) {

        WorkflowDocument workflowDocument = iWantDocument.getDocumentHeader().getWorkflowDocument();
        String initiator = workflowDocument.getInitiatorPrincipalId();
        String documentNumber = iWantDocument.getDocumentNumber();
        Person initiatorPerson = personService.getPerson(initiator);
        String initiatorEmail = initiatorPerson.getEmailAddressUnmasked();

        BodyMailMessage message = new BodyMailMessage();

        message.addToAddress(initiatorEmail);
        message.setFromAddress(emailService.getDefaultFromAddress());

        message.setSubject("I Want document: " + documentNumber + " has been finalized");

        StringBuilder emailBody = new StringBuilder();
        String vendorName = iWantDocument.getVendorName();
        
        if (vendorName == null) {
            vendorName = StringUtils.EMPTY;
        }

        emailBody.append("This is a message to inform you that the I Want document ").append(
                iWantDocument.getDocumentNumber()).append(" has been finalized: ").append(KRADConstants.NEWLINE).append(KRADConstants.NEWLINE);
        emailBody.append("From: ").append(initiatorPerson.getNameUnmasked()).append(KRADConstants.NEWLINE);
        emailBody.append("Title: ").append(iWantDocument.getDocumentTitle()).append(KRADConstants.NEWLINE);
        emailBody.append("Type: ").append(workflowDocument.getDocumentTypeName()).append(KRADConstants.NEWLINE);
        emailBody.append("Id: ").append(documentNumber).append(KRADConstants.NEWLINE);
        emailBody.append("Vendor Name: ").append(vendorName).append("\n\n");

        String docUrl = getDocumentURL(documentNumber, workflowDocument);

        emailBody.append(" Go here to view this item: ").append(docUrl).append(KRADConstants.NEWLINE);

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
    private String getDocumentURL(String documentNumber, WorkflowDocument workflowDocument) {
        String docUrl = workflowDocument.getDocumentHandlerUrl();

        if (StringUtils.isNotBlank(docUrl)) {
            StringBuilder newUrl = new StringBuilder(docUrl.length() + 50).append(docUrl);
            newUrl.append((docUrl.indexOf('?') == -1) ? '?' : '&');
            newUrl.append(KewApiConstants.DOCUMENT_ID_PARAMETER).append('=').append(documentNumber);
            newUrl.append('&').append(KewApiConstants.COMMAND_PARAMETER).append('=').append(KewApiConstants.ACTIONLIST_COMMAND);
            docUrl = newUrl.toString();
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

        

        requisitionDocument.getDocumentHeader().setDocumentDescription(iWantDocument.getDocumentHeader().getDocumentDescription());

        // set req explanation field to I Want doc business purpose
        requisitionDocument.getDocumentHeader().setExplanation(iWantDocument.getExplanation());

        requisitionDocument.setRequisitionSourceCode(CUPurapConstants.RequisitionSources.IWNT);
        requisitionDocument.setApplicationDocumentStatus(RequisitionStatuses.APPDOC_IN_PROCESS);

        requisitionDocument.setPurchaseOrderCostSourceCode(PurapConstants.POCostSources.ESTIMATE);
        requisitionDocument.setPurchaseOrderTransmissionMethodCode(parameterService.getParameterValueAsString(
                RequisitionDocument.class, PurapParameterConstants.PURAP_DEFAULT_PO_TRANSMISSION_CODE));
        requisitionDocument.setUseTaxIndicator(SpringContext.getBean(PurchasingService.class).getDefaultUseTaxIndicatorValue(requisitionDocument));
        
        // if org doc number present on I Want doc, copy it to REQ
        if(StringUtils.isNotBlank(iWantDocument.getDocumentHeader().getOrganizationDocumentNumber())){
        	requisitionDocument.getDocumentHeader().setOrganizationDocumentNumber(iWantDocument.getDocumentHeader().getOrganizationDocumentNumber());
        }

        // set up document link identifier.
        requisitionDocument.setAccountsPayablePurchasingDocumentLinkIdentifier(iWantDocument.getAccountsPayablePurchasingDocumentLinkIdentifier());

        // save doc before adding attachments
        purapService.saveDocumentNoValidation(requisitionDocument);

        // copy attachments from I Want document
        copyIWantDocAttachments(requisitionDocument, iWantDocument, false, true);

        // set up deliver to section
        setUpDeliverToSectionOfReqDoc(requisitionDocument, iWantDocument, purapService);

        // set up items tab
        setUpItemsTabForReqDoc(requisitionDocument, iWantDocument);

        // populate vendor
        setUpVendorSectionForReqDoc(requisitionDocument, iWantDocument);

        // set up accounting lines from IWant Doc
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
    private void setUpDeliverToSectionOfReqDoc(RequisitionDocument requisitionDocument, IWantDocument iWantDocument, CuPurapService purapService) {

        Person deliverTo = null;          

        if (StringUtils.isNotBlank(iWantDocument.getDeliverToNetID())) {

            requisitionDocument.setDeliveryBuildingRoomNumber(KFSConstants.NOT_AVAILABLE_STRING);
            requisitionDocument.setDeliveryCountryCode(KFSConstants.COUNTRY_CODE_UNITED_STATES);
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
                requisitionDocument.setDeliveryToPhoneNumber(
                        phoneNumberService.formatNumberIfPossible(iWantDocument.getDeliverToPhoneNumber()));
                requisitionDocument.setRequestorPersonName(iWantDocument.getInitiatorName());
                requisitionDocument.setRequestorPersonEmailAddress(iWantDocument.getInitiatorEmailAddress());
                requisitionDocument.setRequestorPersonPhoneNumber(
                        phoneNumberService.formatNumberIfPossible(iWantDocument.getInitiatorPhoneNumber()));
                parseAndSetRequestorAddress(iWantDocument.getDeliverToAddress(), requisitionDocument);

                requisitionDocument.setOrganizationAutomaticPurchaseOrderLimit(
                        purapService.getApoLimit(requisitionDocument));

                // populate billing address
                BillingAddress billingAddress = new BillingAddress();
                billingAddress.setBillingCampusCode(requisitionDocument.getDeliveryCampusCode());
                @SuppressWarnings("unchecked")
                Map<String,?> keys = persistenceService.getPrimaryKeyFieldValues(billingAddress);
                billingAddress = businessObjectService.findByPrimaryKey(BillingAddress.class, keys);
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
                requisitionDocument.setDeliveryToPhoneNumber(
                        phoneNumberService.formatNumberIfPossible(deliverTo.getPhoneNumber()));
                requisitionDocument.setRequestorPersonName(deliverTo.getName());
                requisitionDocument.setRequestorPersonEmailAddress(deliverTo.getEmailAddressUnmasked());
                requisitionDocument.setRequestorPersonPhoneNumber(
                        phoneNumberService.formatNumberIfPossible(deliverTo.getPhoneNumber()));

                DefaultPrincipalAddress defaultPrincipalAddress = new DefaultPrincipalAddress(
                        deliverTo.getPrincipalId());
                @SuppressWarnings("unchecked")
                Map<String,?> addressKeys = persistenceService.getPrimaryKeyFieldValues(
                        defaultPrincipalAddress);
                defaultPrincipalAddress = (DefaultPrincipalAddress) businessObjectService
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
                        businessObjectService.delete(defaultPrincipalAddress);
                    }
                }

                // set the APO limit
                requisitionDocument
                        .setOrganizationAutomaticPurchaseOrderLimit(purapService
                                .getApoLimit(requisitionDocument));

                // populate billing address
                BillingAddress billingAddress = new BillingAddress();
                billingAddress.setBillingCampusCode(requisitionDocument.getDeliveryCampusCode());
                @SuppressWarnings("unchecked")
                Map<String,?> keys = persistenceService.getPrimaryKeyFieldValues(billingAddress);
                billingAddress = (BillingAddress) businessObjectService.findByPrimaryKey(
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
        @SuppressWarnings("unchecked")
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
            requisitionDocument.setPurchaseOrderTransmissionMethodCode(((CuVendorAddressExtension) defaultAddress.getExtension())
                    .getPurchaseOrderTransmissionMethodCode());
        } else {
            if (StringUtils.isNotBlank(iWantDocument.getVendorName())) {
                requisitionDocument.setVendorName(iWantDocument.getVendorName());
            }
        }

    }

    /**
     * Copies all attachments from the I Want document to the accounting document
     *
     * NOTE: A true extCopyNoteIndicator value will cause the copied notes to also
     * include an extended attribute with copyNoteIndicator set to true.
     *
     * @param document
     * @param iWantDocument
     * @param copyConfidentialAttachments
     * @param extCopyNoteIndicator
     * @throws Exception
     */
    private void copyIWantDocAttachments(AccountingDocument document, IWantDocument iWantDocument, boolean copyConfidentialAttachments,
            boolean extCopyNoteIndicator) throws Exception {
        
        purapService.saveDocumentNoValidation(document);
        if (iWantDocument.getNotes() != null
              && iWantDocument.getNotes().size() > 0) {

            for (Iterator iterator = iWantDocument.getNotes().iterator(); iterator.hasNext();) {
                Note note = (Note) iterator.next();
                try {
                    Note copyingNote = documentService.createNoteFromDocument(document, note.getNoteText());
                    purapService.saveDocumentNoValidation(document);
                    copyingNote.setNotePostedTimestamp(note.getNotePostedTimestamp());
                    copyingNote.setAuthorUniversalIdentifier(note.getAuthorUniversalIdentifier());
                    copyingNote.setNoteTopicText(note.getNoteTopicText());
                    Attachment originalAttachment = attachmentService.getAttachmentByNoteId(note.getNoteIdentifier());
                    if (originalAttachment != null && (copyConfidentialAttachments
                            || !ConfidentialAttachmentTypeCodes.CONFIDENTIAL_ATTACHMENT_TYPE.equals(originalAttachment.getAttachmentTypeCode()))) {
                        Attachment newAttachment = attachmentService.createAttachment((PersistableBusinessObject)copyingNote, originalAttachment.getAttachmentFileName(), originalAttachment.getAttachmentMimeTypeCode(), originalAttachment.getAttachmentFileSize().intValue(), originalAttachment.getAttachmentContents(), originalAttachment.getAttachmentTypeCode());//new Attachment();

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

        // If specified, auto-construct extended attributes on the document notes at this point. (Doing so before initial persistence can cause save problems.)
        if (extCopyNoteIndicator && CollectionUtils.isNotEmpty(document.getNotes())) {
            for (Note copiedNote : document.getNotes()) {
                NoteExtendedAttribute noteExtension = (NoteExtendedAttribute) copiedNote.getExtension();
                noteExtension.setNoteIdentifier(copiedNote.getNoteIdentifier());
                noteExtension.setCopyNoteIndicator(true);
            }
            purapService.saveDocumentNoValidation(document);
        }

    }
    
private void copyIWantdDocAttachmentsToDV(DisbursementVoucherDocument dvDocument, DisbursementVoucherForm disbursementVoucherForm, IWantDocument iWantDocument) {
        
        
        purapService.saveDocumentNoValidation(dvDocument);
        if (iWantDocument.getNotes() != null && iWantDocument.getNotes().size() > 0) {

            for (Iterator iterator = iWantDocument.getNotes().iterator(); iterator.hasNext();) {
                Note note = (Note) iterator.next();

                Note copyNote;
                
                
                try {
                    copyNote = noteService.createNote(new Note(), dvDocument.getDocumentHeader(), GlobalVariables.getUserSession().getPrincipalId());

                    copyNote.setNoteText(note.getNoteText());
                    copyNote.setAuthorUniversalIdentifier(note.getAuthorUniversalIdentifier());
                    copyNote.setRemoteObjectIdentifier(dvDocument.getObjectId());
                    copyNote.setNotePostedTimestamp(note.getNotePostedTimestamp());

                    String attachmentType = StringUtils.EMPTY;

                    Attachment attachment = note.getAttachment();
                    if (attachment != null) {
                        Note newNote = disbursementVoucherForm.getNewNote();                       
           
                        Attachment copyAttachment = attachmentService.createAttachment(iWantDocument.getDocumentHeader(), attachment.getAttachmentFileName(), attachment.getAttachmentMimeTypeCode(), attachment.getAttachmentFileSize().intValue(), attachment.getAttachmentContents(), attachment.getAttachmentTypeCode());

                            if (copyAttachment != null) {
                                copyNote.addAttachment(copyAttachment);
                                noteService.save(copyNote);
                                dvDocument.addNote(copyNote);
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
                    requisitionAccount.setAccountLinePercent(iWantAccount.getAmountOrPercent().bigDecimalValue().setScale(0, RoundingMode.HALF_UP));
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
    public CuDisbursementVoucherDocument setUpDVDetailsFromIWantDoc(IWantDocument iWantDocument, CuDisbursementVoucherDocument disbursementVoucherDocument, DisbursementVoucherForm disbursementVoucherForm) throws Exception {
        
        
        // DV explanation = I Want Doc business purpose
        disbursementVoucherDocument.getDocumentHeader().setExplanation(iWantDocument.getDocumentHeader().getExplanation());
        // DV desc = IWantDoc desc
        disbursementVoucherDocument.getDocumentHeader().setDocumentDescription(iWantDocument.getDocumentHeader().getDocumentDescription());
        
        // if org doc number present on I Want doc, copy it to DV
        if(StringUtils.isNotBlank(iWantDocument.getDocumentHeader().getOrganizationDocumentNumber())){
        	disbursementVoucherDocument.getDocumentHeader().setOrganizationDocumentNumber(iWantDocument.getDocumentHeader().getOrganizationDocumentNumber());
        }
        
        //copy over attachments
        //copyIWantdDocAttachmentsToDV(disbursementVoucherDocument, disbursementVoucherForm, iWantDocument);
        copyIWantDocAttachments(disbursementVoucherDocument, iWantDocument, true, false);
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
    
	@Override
    public void updateIWantDocumentWithRequisitionReference(IWantDocument iWantDocument, String reqsDocumentNumber) {
        iWantDocument.setReqsDocId(reqsDocumentNumber);
        addDocumentReferenceNoteToIWantDocument(iWantDocument, reqsDocumentNumber,
                CUPurapKeyConstants.IWNT_NOTE_CREATE_REQS);
        purapService.saveDocumentNoValidation(iWantDocument);
    }

    @Override
    public void updateIWantDocumentWithDisbursementVoucherReference(IWantDocument iWantDocument,
            String dvDocumentNumber) {
        iWantDocument.setDvDocId(dvDocumentNumber);
        addDocumentReferenceNoteToIWantDocument(iWantDocument, dvDocumentNumber,
                CUPurapKeyConstants.IWNT_NOTE_CREATE_DV);
        purapService.saveDocumentNoValidation(iWantDocument);
    }

    private void addDocumentReferenceNoteToIWantDocument(IWantDocument iWantDocument, String documentNumber,
            String messageKey) {
        try {
            Person currentUser = GlobalVariables.getUserSession().getPerson();
            String noteFormat = configurationService.getPropertyValueAsString(messageKey);
            String noteText = MessageFormat.format(noteFormat,
                    documentNumber, currentUser.getName(), currentUser.getPrincipalName());
            UserSession systemUserSession = new UserSession(KFSConstants.SYSTEM_USER);
            Note documentReferenceNote = GlobalVariables.doInNewGlobalVariables(systemUserSession,
                    () -> documentService.createNoteFromDocument(iWantDocument, noteText));
            noteService.save(documentReferenceNote);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#setIWantDocumentDescription(edu.cornell.kfs.module.purap.document.IWantDocument)
	 */
	@Override
	public void setIWantDocumentDescription(IWantDocument iWantDocument) {
        // add selected chart and department to document description
        String routingChart = iWantDocument.getRoutingChart() == null ? StringUtils.EMPTY : iWantDocument
                .getRoutingChart() + "-";
        String routingOrg = iWantDocument.getRoutingOrganization() == null ? StringUtils.EMPTY : iWantDocument
                .getRoutingOrganization();
        String addChartOrgToDesc = routingChart + routingOrg;
        String vendorName = iWantDocument.getVendorName() == null ? StringUtils.EMPTY : iWantDocument.getVendorName();
        String description = addChartOrgToDesc + " " + vendorName;

        int maxLengthOfDocumentDescription = KFSConstants.getMaxLengthOfDocumentDescription();
        if (StringUtils.isNotBlank(description) && description.length() > maxLengthOfDocumentDescription) {
            description = description.substring(0, maxLengthOfDocumentDescription);
        }

        // If necessary, add a default description.
        if (StringUtils.isBlank(description)) {
            description = "New IWantDocument";
        }
        
        iWantDocument.getDocumentHeader().setDocumentDescription(description);
	}
    
	/**
	 * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#setUpIWantDocDefaultValues(IWantDocument, Person)
	 */
	@Override
	public void setUpIWantDocDefaultValues(IWantDocument iWantDocument, Person initiatorUser) {
		String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
		Principal initiator = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
		String initiatorPrincipalID = initiator.getPrincipalId();
		String initiatorNetID = initiator.getPrincipalName();

		iWantDocument.setInitiatorNetID(initiatorNetID);

		String initiatorName = initiatorUser.getName();
		String initiatorPhoneNumber = initiatorUser.getPhoneNumber();
		String initiatorEmailAddress = initiatorUser.getEmailAddress();

		String address = getPersonCampusAddress(initiatorNetID);

		iWantDocument.setInitiatorName(initiatorName);
		iWantDocument.setInitiatorPhoneNumber(initiatorPhoneNumber);
		iWantDocument.setInitiatorEmailAddress(initiatorEmailAddress);
		iWantDocument.setInitiatorAddress(address);

		// check default user options
		Map<String, String> primaryKeysCollegeOption = new HashMap<String, String>();
		primaryKeysCollegeOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
		primaryKeysCollegeOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE);
		IWantDocUserOptions userOptionsCollege = (IWantDocUserOptions) getBusinessObjectService().findByPrimaryKey(IWantDocUserOptions.class, primaryKeysCollegeOption);

		Map<String, String> primaryKeysDepartmentOption = new HashMap<String, String>();
		primaryKeysDepartmentOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
		primaryKeysDepartmentOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT);
		IWantDocUserOptions userOptionsDepartment = (IWantDocUserOptions) getBusinessObjectService().findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDepartmentOption);

		// check default deliver to address info

		Map<String, String> primaryKeysdeliverToNetIDOption = new HashMap<String, String>();
		primaryKeysdeliverToNetIDOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
		primaryKeysdeliverToNetIDOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NET_ID);
		IWantDocUserOptions userOptionsDeliverToNetID = (IWantDocUserOptions) getBusinessObjectService().findByPrimaryKey(IWantDocUserOptions.class, primaryKeysdeliverToNetIDOption);

		Map<String, String> primaryKeysDeliverToNameOption = new HashMap<String, String>();
		primaryKeysDeliverToNameOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
		primaryKeysDeliverToNameOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NAME);
		IWantDocUserOptions userOptionsDeliverToName = (IWantDocUserOptions) getBusinessObjectService().findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToNameOption);

		Map<String, String> primaryKeysDeliverToEmailOption = new HashMap<String, String>();
		primaryKeysDeliverToEmailOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
		primaryKeysDeliverToEmailOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_EMAIL_ADDRESS);
		IWantDocUserOptions userOptionsDeliverToEmail = (IWantDocUserOptions) getBusinessObjectService().findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToEmailOption);

		Map<String, String> primaryKeysDeliverToPhnNbrOption = new HashMap<String, String>();
		primaryKeysDeliverToPhnNbrOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
		primaryKeysDeliverToPhnNbrOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_PHONE_NUMBER);
		IWantDocUserOptions userOptionsDeliverToPhnNbr = (IWantDocUserOptions) getBusinessObjectService().findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToPhnNbrOption);

		Map<String, String> primaryKeysDeliverToAddressOption = new HashMap<String, String>();
		primaryKeysDeliverToAddressOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
		primaryKeysDeliverToAddressOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_ADDRESS);
		IWantDocUserOptions userOptionsDeliverToAddress = (IWantDocUserOptions) getBusinessObjectService().findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToAddressOption);

		if (ObjectUtils.isNotNull(userOptionsCollege)) {
			iWantDocument.setCollegeLevelOrganization(userOptionsCollege.getOptionValue());
		}

		if (ObjectUtils.isNotNull(userOptionsDepartment)) {
			iWantDocument.setDepartmentLevelOrganization(userOptionsDepartment.getOptionValue());
		}

		// if no default user options check primary department
		if (ObjectUtils.isNull(userOptionsCollege) && ObjectUtils.isNull(userOptionsDepartment)) {
			String primaryDeptOrg = null;

			if (ObjectUtils.isNotNull(iWantDocument)) {

				Entity entityInfo = KimApiServiceLocator.getIdentityService().getEntityByPrincipalId(initiatorUser.getPrincipalId());

				if (ObjectUtils.isNotNull(entityInfo)) {
					if (ObjectUtils.isNotNull(entityInfo.getEmploymentInformation()) && entityInfo.getEmploymentInformation().size() > 0) {
						EntityEmployment employmentInformation = entityInfo.getEmploymentInformation().get(0);
						String primaryDepartment = employmentInformation.getPrimaryDepartmentCode();
						primaryDeptOrg = primaryDepartment.substring(primaryDepartment.lastIndexOf('-') + 1, primaryDepartment.length());

						String cLevelOrg = getCLevelOrganizationForDLevelOrg(primaryDepartment);
						iWantDocument.setCollegeLevelOrganization(cLevelOrg);
					}
				}
			}

			if (ObjectUtils.isNotNull(iWantDocument) && StringUtils.isNotEmpty(iWantDocument.getCollegeLevelOrganization())) {

				if (ObjectUtils.isNotNull(primaryDeptOrg)) {
					iWantDocument.setDepartmentLevelOrganization(primaryDeptOrg);
				}

			}
		}

		if (ObjectUtils.isNotNull(userOptionsDeliverToNetID)) {
			iWantDocument.setDeliverToNetID(userOptionsDeliverToNetID.getOptionValue());
		}

		if (ObjectUtils.isNotNull(userOptionsDeliverToName)) {
			iWantDocument.setDeliverToName(userOptionsDeliverToName.getOptionValue());
		}

		if (ObjectUtils.isNotNull(userOptionsDeliverToEmail)) {
			iWantDocument.setDeliverToEmailAddress(userOptionsDeliverToEmail.getOptionValue());
		}

		if (ObjectUtils.isNotNull(userOptionsDeliverToPhnNbr)) {
			iWantDocument.setDeliverToPhoneNumber(userOptionsDeliverToPhnNbr.getOptionValue());
		}

		if (ObjectUtils.isNotNull(userOptionsDeliverToAddress)) {
			iWantDocument.setDeliverToAddress(userOptionsDeliverToAddress.getOptionValue());
		}

		setIWantDocumentDescription(iWantDocument);

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
    
    public CuPurapService getPurapService() {
        return purapService;
    }
    
    public void setPurapService(CuPurapService purapService) {
        this.purapService = purapService;
    }
    
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
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
    
    public FinancialSystemUserService getFinancialSystemUserService() {
        return financialSystemUserService;
    }

    public void setFinancialSystemUserService(FinancialSystemUserService financialSystemUserService) {
        this.financialSystemUserService = financialSystemUserService;
    }

    
    /**
     * Gets the emailService.
     * 
     * @return emailService
     */
    public EmailService getEmailService() {
        return emailService;
    }

    /**
     * Sets the emailService.
     * 
     * @param emailService
     */
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
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

    public PhoneNumberService getPhoneNumberService() {
        return phoneNumberService;
    }

    public void setPhoneNumberService(PhoneNumberService phoneNumberService) {
        this.phoneNumberService = phoneNumberService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
