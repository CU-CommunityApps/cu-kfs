package edu.cornell.kfs.module.purap.document.service.impl;

import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.entity.KimEntityAddress;
import org.kuali.rice.kim.bo.entity.KimEntityEntityType;
import org.kuali.rice.kim.bo.entity.dto.KimEntityEntityTypeInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityInfo;
import org.kuali.rice.kim.bo.entity.dto.KimPrincipalInfo;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kim.util.KimConstants;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.businessobject.PersonData;
import edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class IWantDocumentServiceImpl implements IWantDocumentService {
    
    private LevelOrganizationDao collegeLevelOrganizationDao;

    public String getPersonCampusAddress(String principalName) {
        IdentityManagementService identityManagementService = SpringContext.getBean(IdentityManagementService.class);

        KimEntityInfo entityInfo = identityManagementService.getEntityInfoByPrincipalName(principalName);

        KimEntityEntityType entityEntityType = getPersonEntityEntityType(entityInfo);

        KimEntityAddress foundAddress = getPersonEntityEntityAddress(entityEntityType);

        String addressLine1 = foundAddress.getLine1Unmasked();//StringUtils.isBlank(currentUser.getAddressLine1()) ? "" : currentUser.getAddressLine1() + "\n";
        String addressLine2 = foundAddress.getLine2Unmasked();//StringUtils.isBlank(currentUser.getAddressLine2()) ? "" : currentUser.getAddressLine2() + "\n";
        String city = foundAddress.getCityNameUnmasked(); //StringUtils.isBlank(currentUser.getAddressCityName()) ? "" : currentUser.getAddressCityName()+ "\n";
        String stateCode = foundAddress.getStateCodeUnmasked();//StringUtils.isBlank(currentUser.getAddressStateCode()) ? "" : currentUser.getAddressStateCode() + "\n";
        String postalCode = foundAddress.getPostalCodeUnmasked();//StringUtils.isBlank(currentUser.getAddressPostalCode()) ? "" : currentUser.getAddressPostalCode() + "\n";
        String countryCode = foundAddress.getCountryCodeUnmasked();//StringUtils.isBlank(currentUser.getAddressCountryCode()) ? "" : currentUser.getAddressCountryCode() + "\n";

        String initiatorAddress = addressLine1 + "\n" + addressLine2 + "\n"
                + city + "\n" + stateCode + "\n"
                + postalCode + "\n" + countryCode;

        return initiatorAddress;
    }

    protected KimEntityAddress getPersonEntityEntityAddress(KimEntityEntityType entityEntityType) {
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

    protected KimEntityEntityType getPersonEntityEntityType(KimEntityInfo entityInfo) {
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

    public List<LevelOrganization> getCLevelOrganizations() {
        // TODO Auto-generated method stub
        return collegeLevelOrganizationDao.getCLevelOrganizations();
    }

    public LevelOrganizationDao getCollegeLevelOrganizationDao() {
        return collegeLevelOrganizationDao;
    }

    public void setCollegeLevelOrganizationDao(LevelOrganizationDao collegeLevelOrganizationDao) {
        this.collegeLevelOrganizationDao = collegeLevelOrganizationDao;
    }

    public List<LevelOrganization> getDLevelOrganizations(String cLevelOrg) {
        // TODO Auto-generated method stub
        return collegeLevelOrganizationDao.getDLevelOrganizations(cLevelOrg);
    }

    public String getDLevelOrganizationsString(String cLevelOrg) {
        List<LevelOrganization> dLevelOrgs= getDLevelOrganizations( cLevelOrg);
        StringBuffer dLevelOrgsString = new StringBuffer("");
        
        for(LevelOrganization organization : dLevelOrgs){
        dLevelOrgsString.append(organization.getCode() + " " + organization.getCodeAndDescription() + "#"); 
        }
        
        return dLevelOrgsString.toString();
    }

    public String getCLevelOrganizationForDLevelOrg(String dLevelOrg){
        return collegeLevelOrganizationDao.getCLevelOrganizationForDLevelOrg(dLevelOrg);
    }

    public PersonData getPersonData(String principalName) {
        
        PersonData personData = new PersonData();
        PersonService personService = SpringContext.getBean(PersonService.class);

        Person person = personService.getPersonByPrincipalName(principalName);
        personData.setPersonName(person.getNameUnmasked());
        personData.setNetID(principalName);
        personData.setEmailAddress(person.getEmailAddressUnmasked());
        personData.setPhoneNumber(person.getPhoneNumberUnmasked());
        personData.setCampusAddress(getPersonCampusAddress(principalName));
        
        return personData;
    }
}
