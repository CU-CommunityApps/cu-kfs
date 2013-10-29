package edu.cornell.kfs.module.purap.document.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.doctype.DocumentTypeService;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.identity.address.EntityAddress;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.service.MailService;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.businessobject.PersonData;
import edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class IWantDocumentServiceImpl implements IWantDocumentService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(IWantDocumentServiceImpl.class);

    private LevelOrganizationDao collegeLevelOrganizationDao;
    private MailService mailService;
    private PersonService personService;
    private DocumentTypeService documentTypeService;

    /**
     * @see edu.cornell.kfs.module.purap.document.service.IWantDocumentService#getPersonCampusAddress(java.lang.String)
     */
    public String getPersonCampusAddress(String principalName) {
    	
        EntityAddress foundAddress = getPersonEntityAddress(principalName);

        String addressLine1 = foundAddress.getLine1Unmasked();
        String addressLine2 = foundAddress.getLine2Unmasked();
        String city = foundAddress.getCityUnmasked();
        String stateCode = foundAddress.getStateProvinceCodeUnmasked();
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
    protected EntityAddress getPersonEntityAddress(String principalName) {
        List<? extends EntityAddress> addresses = KimApiServiceLocator.getIdentityService().getEntityByPrincipalName(principalName).getEntityTypeContactInfoByTypeCode(KimConstants.EntityTypes.PERSON).getAddresses();
        EntityAddress foundAddress = null;
        int count = 0;

        while (count < addresses.size() && foundAddress == null) {
            final EntityAddress currentAddress = addresses.get(count);
            if (currentAddress.getAddressType().getCode().equals("CMP")) {
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

        WorkflowDocument workflowDocument = iWantDocument.getDocumentHeader().getWorkflowDocument();
        String initiator = workflowDocument.getInitiatorPrincipalId();
        String documentNumber = iWantDocument.getDocumentNumber();
        Person initiatorPerson = personService.getPerson(initiator);
        String initiatorEmail = initiatorPerson.getEmailAddress();

        MailMessage message = new MailMessage();

        message.addToAddress(initiatorEmail);
        message.setFromAddress(initiatorEmail);

        message.setSubject("I Want document: " + documentNumber + " has been finalized");

        StringBuffer emailBody = new StringBuffer();
        String vendorName = iWantDocument.getVendorName();
        
        if(vendorName == null){
            vendorName = StringUtils.EMPTY;
        }

        emailBody.append("This is a message to inform you that the I Want document "
                + iWantDocument.getDocumentNumber() + " has been finalized" + ": \n\n");
        emailBody.append("From: " + initiatorPerson.getNameUnmasked() + "\n");
        emailBody.append("Title: " + iWantDocument.getDocumentTitle() + "\n");
        emailBody.append("Type: " + workflowDocument.getDocumentTypeName()  + "\n");
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
    private String getDocumentURL(String documentNumber, WorkflowDocument workflowDocument) {
        String docUrl = workflowDocument.getDocumentHandlerUrl();

        if (StringUtils.isNotBlank(docUrl)) {
            if (docUrl.indexOf("?") == -1) {
                docUrl += "?";
            } else {
                docUrl += "&";
            }
            docUrl += KewApiConstants.DOCUMENT_ID_PARAMETER + "="
                    + documentNumber;
            docUrl += "&" + KewApiConstants.COMMAND_PARAMETER + "="
                    + KewApiConstants.ACTIONLIST_COMMAND;
        }

        return docUrl;
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
}
