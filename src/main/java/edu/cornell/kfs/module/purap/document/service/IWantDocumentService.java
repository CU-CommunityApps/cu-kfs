package edu.cornell.kfs.module.purap.document.service;

import java.util.List;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.businessobject.PersonData;

public interface IWantDocumentService {
    
    /**
     * This method ...
     * 
     * @param principalID
     * @return
     */
    public String getPersonCampusAddress(String principalID);
    
    /**
     * This method ...
     * 
     * @return
     */
    public List<LevelOrganization> getCLevelOrganizations();
    
    /**
     * This method ...
     * 
     * @param cLevelOrg
     * @return
     */
    public List<LevelOrganization> getDLevelOrganizations(String cLevelOrg);
    
    /**
     * This method ...
     * 
     * @param cLevelOrg
     * @return
     */
    public String getDLevelOrganizationsString(String cLevelOrg);
    
    /**
     * This method ...
     * 
     * @param dLevelOrg
     * @return
     */
    public String getCLevelOrganizationForDLevelOrg(String dLevelOrg); 
    
    /**
     * This method ...
     * 
     * @param principalID
     * @return
     */
    public PersonData getPersonData(String principalID);

}
