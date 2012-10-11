package edu.cornell.kfs.module.purap.dataaccess;

import java.util.List;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;

/**
 * This class provides methods to retrieve D level and C level orgs.
 */
public interface LevelOrganizationDao {
    /**
     * Retrieves all College level orgs
     * 
     * @return
     */
    public List<LevelOrganization> getCLevelOrganizations();
    
    /**
     * Retrieves all Department level orgs for the given College level org
     * 
     * @param cLevelOrg
     * @return
     */
    public List<LevelOrganization> getDLevelOrganizations(String cLevelOrg);
    
    /**
     * Retrieves the College level org for the given D level org
     * 
     * @param dLevelOrg
     * @return
     */
    public String getCLevelOrganizationForDLevelOrg(String dLevelOrg); 

}
