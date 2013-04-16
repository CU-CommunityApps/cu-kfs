package edu.cornell.kfs.module.purap.document.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.sys.context.SpringContext;
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
import org.kuali.rice.kns.mail.MailMessage;
import org.kuali.rice.kns.service.MailService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.businessobject.PersonData;
import edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.service.UserFavoriteAccountService;

public class IWantDocumentServiceImpl implements IWantDocumentService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(IWantDocumentServiceImpl.class);

    private LevelOrganizationDao collegeLevelOrganizationDao;
    private MailService mailService;
    private PersonService personService;
    private UserFavoriteAccountService userFavoriteAccountService;
    private DocumentTypeService documentTypeService;

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

	public void setUserFavoriteAccountService(
			UserFavoriteAccountService userFavoriteAccountService) {
		this.userFavoriteAccountService = userFavoriteAccountService;
	}
	
	/**
	 * KFSPTS-985 : populate IWANT account from primary favorite account
	 */
	public IWantAccount getFavoriteIWantAccount() {
		FavoriteAccount favoriteAccount = userFavoriteAccountService.getFavoriteAccount(GlobalVariables.getUserSession().getPrincipalId());
		return getFavoriteIWantAccount(favoriteAccount);
	}
	
	/**
	 * KFSPTS-985 : populate IWANT account from selected favorite account
	 */
	public IWantAccount getFavoriteIWantAccount(FavoriteAccount favoriteAccount) {
    	if (ObjectUtils.isNotNull(favoriteAccount)) {
    		IWantAccount iWantAccount = new IWantAccount();
    		iWantAccount.setAccountNumber(favoriteAccount.getAccountNumber());
    		iWantAccount.setChartOfAccountsCode(favoriteAccount.getChartOfAccountsCode());
    		iWantAccount.setSubAccountNumber(favoriteAccount.getSubAccountNumber());
    		iWantAccount.setFinancialObjectCode(favoriteAccount.getFinancialObjectCode());
    		iWantAccount.setFinancialSubObjectCode(favoriteAccount.getFinancialSubObjectCode());
    		iWantAccount.setProjectCode(favoriteAccount.getProjectCode());
    		iWantAccount.setOrganizationReferenceId(favoriteAccount.getOrganizationReferenceId());
    		if (CUPurapConstants.PERCENT.equalsIgnoreCase(iWantAccount.getUseAmountOrPercent())) {
    		    iWantAccount.setAmountOrPercent(new KualiDecimal(100));
    		}
    		return iWantAccount;
    	}
    	return null;
	}

}
