package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.PersistenceStructureService;

public class SubObjectCodeGlobalEdit extends PersistableBusinessObjectBase implements GlobalBusinessObject, MutableInactivatable {

	private static final Logger LOG = LogManager.getLogger(SubObjectCodeGlobalEdit.class);

    protected String documentNumber;
    protected Integer universityFiscalYear;
    protected String chartOfAccountsCode;
    protected String financialSubObjectCode;
    protected String financialSubObjectCodeName;
    protected String financialSubObjectCodeShortName;
    protected boolean active;

    protected DocumentHeader financialDocument;
    protected SystemOptions universityFiscal;
    protected Chart chartOfAccounts;

    protected List<SubObjectCodeGlobalEditDetail> subObjCdGlobalEditDetails;

    /**
     * Default constructor.
     */
    public SubObjectCodeGlobalEdit() {
    	subObjCdGlobalEditDetails = new ArrayList<SubObjectCodeGlobalEditDetail>();
    }

    /**
     * Gets the documentNumber attribute.
     * 
     * @return Returns the documentNumber
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the documentNumber attribute.
     * 
     * @param documentNumber The documentNumber to set.
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }


    /**
     * Gets the universityFiscalYear attribute.
     * 
     * @return Returns the universityFiscalYear
     */
    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    /**
     * Sets the universityFiscalYear attribute.
     * 
     * @param universityFiscalYear The universityFiscalYear to set.
     */
    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }


    /**
     * Gets the chartOfAccountsCode attribute.
     * 
     * @return Returns the chartOfAccountsCode
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    /**
     * Sets the chartOfAccountsCode attribute.
     * 
     * @param chartOfAccountsCode The chartOfAccountsCode to set.
     */
    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }


    /**
     * Gets the financialSubObjectCode attribute.
     * 
     * @return Returns the financialSubObjectCode
     */
    public String getFinancialSubObjectCode() {
        return financialSubObjectCode;
    }

    /**
     * Sets the financialSubObjectCode attribute.
     * 
     * @param financialSubObjectCode The financialSubObjectCode to set.
     */
    public void setFinancialSubObjectCode(String financialSubObjectCode) {
        this.financialSubObjectCode = financialSubObjectCode;
    }


    /**
     * Gets the financialSubObjectCodeName attribute.
     * 
     * @return Returns the financialSubObjectCodeName
     */
    public String getFinancialSubObjectCodeName() {
        return financialSubObjectCodeName;
    }

    /**
     * Sets the financialSubObjectCodeName attribute.
     * 
     * @param financialSubObjectCodeName The financialSubObjectCodeName to set.
     */
    public void setFinancialSubObjectCodeName(String financialSubObjectCodeName) {
        this.financialSubObjectCodeName = financialSubObjectCodeName;
    }


    /**
     * Gets the financialSubObjectCodeShortName attribute.
     * 
     * @return Returns the financialSubObjectCodeShortName
     */
    public String getFinancialSubObjectCodeShortName() {
        return financialSubObjectCodeShortName;
    }

    /**
     * Sets the financialSubObjectCodeShortName attribute.
     * 
     * @param financialSubObjectCodeShortName The financialSubObjectCodeShortName to set.
     */
    public void setFinancialSubObjectCodeShortName(String financialSubObjectCdshortNm) {
        this.financialSubObjectCodeShortName = financialSubObjectCdshortNm;
    }


    /**
     * Gets the active attribute.
     * 
     * @return Returns the active
     */
    public boolean isActive() {
        return active;
    }


    /**
     * Sets the active attribute.
     * 
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }


    /**
     * Gets the financialDocument attribute.
     * 
     * @return Returns the financialDocument
     */
    public DocumentHeader getFinancialDocument() {
        return financialDocument;
    }

    /**
     * Sets the financialDocument attribute.
     * 
     * @param financialDocument The financialDocument to set.
     * @deprecated
     */
    public void setFinancialDocument(DocumentHeader financialDocument) {
        this.financialDocument = financialDocument;
    }

    /**
     * Gets the universityFiscal attribute.
     * 
     * @return Returns the universityFiscal
     */
    public SystemOptions getUniversityFiscal() {
        return universityFiscal;
    }

    /**
     * Sets the universityFiscal attribute.
     * 
     * @param universityFiscal The universityFiscal to set.
     * @deprecated
     */
    public void setUniversityFiscal(SystemOptions universityFiscal) {
        this.universityFiscal = universityFiscal;
    }

    /**
     * Gets the chartOfAccounts attribute.
     * 
     * @return Returns the chartOfAccounts
     */
    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    /**
     * Sets the chartOfAccounts attribute.
     * 
     * @param chartOfAccounts The chartOfAccounts to set.
     * @deprecated
     */
    public void setChartOfAccounts(Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    public List<SubObjectCodeGlobalEditDetail> getSubObjCdGlobalEditDetails() {
        return subObjCdGlobalEditDetails;
    }

    public void setSubObjCdGlobalEditDetails(List<SubObjectCodeGlobalEditDetail> subObjCdGlobalEditDetails) {
        this.subObjCdGlobalEditDetails = subObjCdGlobalEditDetails;
    }


    /**
     * @see org.kuali.kfs.krad.document.GlobalBusinessObject#getGlobalChangesToDelete()
     */
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        return null;
    }


    /**
     * @see org.kuali.kfs.krad.document.GlobalBusinessObject#isPersistable()
     */
    @Override
    public boolean isPersistable() {
        PersistenceStructureService persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);

        // fail if the PK for this object is emtpy
        if (StringUtils.isBlank(documentNumber)) {
            return false;
        }

        // fail if the PKs for any of the contained objects are empty
        for (SubObjectCodeGlobalEditDetail detail  : getSubObjCdGlobalEditDetails()) {
            if (!persistenceStructureService.hasPrimaryKeyFieldValues(detail)) {
                return false;
            }
        }

        // otherwise, its all good
        return true;
    }

	/**
	 * @see org.kuali.kfs.krad.bo.GlobalBusinessObject#generateGlobalChangesToPersist()
	 */
	@Override
	public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
		 // the list of persist-ready BOs
        List<PersistableBusinessObject> persistables = new ArrayList<PersistableBusinessObject>();
    
        // walk over each change detail record
        for (SubObjectCodeGlobalEditDetail detail : subObjCdGlobalEditDetails) {
    
            // load the object by keys
            SubObjectCode subObjectCode = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(SubObjectCode.class, detail.getPrimaryKeys());
    
            // if we got a valid subObjectCode, inactivate it 
            if (subObjectCode != null) {
            	subObjectCode.setActive(false);
                persistables.add(subObjectCode);
    
            }
        }

        return persistables;
	}

	/**
	 * @see org.kuali.kfs.krad.bo.GlobalBusinessObject#getAllDetailObjects()
	 */
	@Override
	public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
		for (SubObjectCodeGlobalEditDetail detail : subObjCdGlobalEditDetails) {
			detail.refreshReferenceObject("account");
		}
		return subObjCdGlobalEditDetails;
	}
	
    @Override
    public List buildListOfDeletionAwareLists() {
        List deletionAwareList = super.buildListOfDeletionAwareLists();
        deletionAwareList.add(subObjCdGlobalEditDetails);
        return deletionAwareList;
    }   

}