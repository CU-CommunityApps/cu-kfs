package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.bo.GlobalBusinessObject;
import org.kuali.rice.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.service.PersistenceStructureService;

public class SubAccountGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {
	private String documentNumber;
	
    private boolean inactivate;

    
    private List<SubAccountGlobalDetail> subAccountGlobalDetails;
    
    public SubAccountGlobal() {
    	super();
    	subAccountGlobalDetails = new ArrayList<SubAccountGlobalDetail>();
	}

	@Override
	public List<PersistableBusinessObject> generateDeactivationsToPersist() {
		List<PersistableBusinessObject>  objectsToDeactivate = new ArrayList<PersistableBusinessObject>();
		if(inactivate){
			for(SubAccountGlobalDetail subAccountGlobalDetail : subAccountGlobalDetails){
				subAccountGlobalDetail.refreshReferenceObject("subAccount");
				objectsToDeactivate.add(subAccountGlobalDetail.getSubAccount());			
			}
		}
		
		return objectsToDeactivate;
	}

	@Override
	public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
		List<PersistableBusinessObject>  changesToPersist = new ArrayList<PersistableBusinessObject>();
		
		if(inactivate){
			for(SubAccountGlobalDetail subAccountGlobalDetail : subAccountGlobalDetails){
				subAccountGlobalDetail.refreshReferenceObject("subAccount");
				SubAccount subAccount = subAccountGlobalDetail.getSubAccount();
				subAccount.setActive(false);
				
				changesToPersist.add(subAccount);			
			}
		}
		
		return changesToPersist;
	}

	@Override
	public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
		return subAccountGlobalDetails;
	}

	@Override
	public boolean isPersistable() {
        PersistenceStructureService persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);

        // fail if the PK for this object is emtpy
        if (StringUtils.isBlank(documentNumber)) {
            return false;
        }

        // fail if the PKs for any of the contained objects are empty
        for (SubAccountGlobalDetail subAccount : getSubAccountGlobalDetails()) {
            if (!persistenceStructureService.hasPrimaryKeyFieldValues(subAccount)) {
                return false;
            }
        }

        // otherwise, its all good
        return true;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public List<SubAccountGlobalDetail> getSubAccountGlobalDetails() {
		return subAccountGlobalDetails;
	}

	public void setSubAccountGlobalDetails(
			List<SubAccountGlobalDetail> subAccountGlobalDetails) {
		this.subAccountGlobalDetails = subAccountGlobalDetails;
	}

	public boolean isInactivate() {
		return inactivate;
	}

	public void setInactivate(boolean inactivate) {
		this.inactivate = inactivate;
	}


}
