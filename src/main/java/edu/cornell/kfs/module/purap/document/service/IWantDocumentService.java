package edu.cornell.kfs.module.purap.document.service;

import java.util.List;

import org.kuali.rice.kns.mail.MailMessage;

import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.businessobject.PersonData;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;

public interface IWantDocumentService {
    
    /**
     * Retrieves a person's campus address based on their principal ID.
     * 
     * @param principalID
     * @return a person's campus address
     */
    public String getPersonCampusAddress(String principalID);
    
    /**
     * Retrieves all C level organizations in the system.
     * 
     * @return a list of C level organizations 
     */
    public List<LevelOrganization> getCLevelOrganizations();
    
    /**
     * Retrieves a list of all D level organizations for the input C level org.
     * 
     * @param cLevelOrg
     * @return a list of all D level organizations for the input C level org
     */
    public List<LevelOrganization> getDLevelOrganizations(String cLevelOrg);
    
    /**
     * Retrieves  a string of all D level organizations for the input C level org.
     * 
     * @param cLevelOrg
     * @return a string of all D level organizations for the input C level org
     */
    public String getDLevelOrganizationsString(String cLevelOrg);
    
    /**
     * Get the C level organization for the input D level organization.
     * 
     * @param dLevelOrg
     * @return the C level organization for the input D level organization.
     */
    public String getCLevelOrganizationForDLevelOrg(String dLevelOrg); 
    
    /**
     * Retrieves the Person data for the given principal ID.
     * 
     * @param principalID
     * @return the Person data for the given principal ID
     */
    public PersonData getPersonData(String principalID);
    
    /**
     * Builds and emails a message when the input document has reached the FINAL state.
     * 
     * @param iWantDocument
     */
    public void sendDocumentFinalizedMessage(IWantDocument iWantDocument);
    
    /**
     * KFSPTS-985 :
     * Populate primary favorite account to IWantAccount if it is setup
     * @return
     */
	public IWantAccount getFavoriteIWantAccount();

	/**
	 * populate selected favorite account to IWantAccount 
	 */
	public IWantAccount getFavoriteIWantAccount(FavoriteAccount favoriteAccount);
}
