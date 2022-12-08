package edu.cornell.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;


public class UserProcurementProfile extends PersistableBusinessObjectBase {
	
	private static final long serialVersionUID = 1L;
	private Integer userProfileId;
	private String principalId;
    private List<FavoriteAccount> favoriteAccounts;
	private String personName;
	private boolean personSelected;
	private boolean active;
	private FavoriteAccount newSourceLine;
    private Person profileUser;
    private FavoriteAccount resultAccount;

    public UserProcurementProfile() {
        super();
        this.favoriteAccounts = new ArrayList<FavoriteAccount>();
        newSourceLine = new FavoriteAccount();

    }   

	public Integer getUserProfileId() {
		return userProfileId;
	}

	public void setUserProfileId(Integer userProfileId) {
		this.userProfileId = userProfileId;
	}

	public String getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(String principalId) {
		this.principalId = principalId;
	}

	public List<FavoriteAccount> getFavoriteAccounts() {
		return favoriteAccounts;
	}

	public void setFavoriteAccounts(
			List<FavoriteAccount> favoriteAccounts) {
		this.favoriteAccounts = favoriteAccounts;
	}
	
	@SuppressWarnings("rawtypes")
	protected LinkedHashMap toStringMapper() {
		return null;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public boolean isPersonSelected() {
		return personSelected;
	}

	public void setPersonSelected(boolean personSelected) {
		this.personSelected = personSelected;
	}

	public FavoriteAccount getNewSourceLine() {
		return newSourceLine;
	}

	public void setNewSourceLine(FavoriteAccount newSourceLine) {
		this.newSourceLine = newSourceLine;
	}

    public void deleteAccount(int lineNum) {
        if (favoriteAccounts.remove(lineNum) == null) {
            // throw error here
        }

    }

	public Person getProfileUser() {
		profileUser = SpringContext.getBean(PersonService.class).updatePersonIfNecessary(principalId, profileUser);
		return profileUser;
	}

	public void setProfileUser(Person profileUser) {
		this.profileUser = profileUser;
	}

	public FavoriteAccount getResultAccount() {
		if (resultAccount ==null) {
			resultAccount = new FavoriteAccount();
		}
		return resultAccount;
	}

	public void setResultAccount(FavoriteAccount resultAccount) {
		this.resultAccount = resultAccount;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
