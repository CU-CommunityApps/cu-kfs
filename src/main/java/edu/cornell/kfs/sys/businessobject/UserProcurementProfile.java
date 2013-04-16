package edu.cornell.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.businessobject.CommodityContractManager;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.TypedArrayList;



public class UserProcurementProfile extends PersistableBusinessObjectBase {
	
	private Integer userProfileId;
	private String principalId;
//	private String documentTypeCode;
    private List<FavoriteAccount> favoriteAccounts;
	private String personName;
	private boolean personSelected;
	private FavoriteAccount newSourceLine;
    private Person profileUser;

    public UserProcurementProfile() {
        super();
        this.favoriteAccounts = new TypedArrayList(FavoriteAccount.class);
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

//    public FavoriteAccount getFavoriteAccount(int index) {
//        if (index >= favoriteAccounts.size()) {
//            for (int i = favoriteAccounts.size(); i <= index; i++) {
//            	favoriteAccounts.add(new FavoriteAccount());
//            }
//        }
//        return favoriteAccounts.get(index);
//    }

	public List<FavoriteAccount> getFavoriteAccounts() {
		return favoriteAccounts;
	}


	public void setFavoriteAccounts(
			List<FavoriteAccount> favoriteAccounts) {
		this.favoriteAccounts = favoriteAccounts;
	}

	@Override
	protected LinkedHashMap toStringMapper() {
		// TODO Auto-generated method stub
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
		profileUser = SpringContext.getBean(org.kuali.rice.kim.service.PersonService.class).updatePersonIfNecessary(principalId, profileUser);
		return profileUser;
	}



	public void setProfileUser(Person profileUser) {
		this.profileUser = profileUser;
	}


//	public String getDocumentTypeCode() {
//		return documentTypeCode;
//	}
//
//
//
//	public void setDocumentTypeCode(String documentTypeCode) {
//		this.documentTypeCode = documentTypeCode;
//	}





}
