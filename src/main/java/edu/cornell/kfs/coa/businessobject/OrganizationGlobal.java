package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.sys.businessobject.Campus;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.businessobject.PostalCode;


/**
 * Primary BO for the Organization Global document.
 */
public class OrganizationGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {

    private static final long serialVersionUID = 4192231288000300773L;

    private static final String ORG_MANAGER_BO_PROPERTY = "organizationManagerUniversal";

    protected String documentNumber;

    protected String organizationManagerUniversalId;
    protected String organizationPhysicalCampusCode;
    protected String organizationLine1Address;
    protected String organizationLine2Address;
    protected String organizationCityName;
    protected String organizationStateCode;
    protected String organizationZipCode;
    protected String organizationCountryCode;
    protected List<OrganizationGlobalDetail> organizationGlobalDetails;

    protected DocumentHeader financialDocument;
    protected Person organizationManagerUniversal;
    protected Campus organizationPhysicalCampus;
    protected PostalCode postalZip;
    protected Country organizationCountry;



    public OrganizationGlobal() {
        this.organizationGlobalDetails = new ArrayList<OrganizationGlobalDetail>();
    }



    @Override
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        return null;
    }

    @Override
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
        /*
         * The logic below is based on that from the AccountGlobal class.
         */
        List<PersistableBusinessObject> globalChanges = new ArrayList<PersistableBusinessObject>();
        BusinessObjectService boService = SpringContext.getBean(BusinessObjectService.class);
        
        for (OrganizationGlobalDetail detail : organizationGlobalDetails) {
            Organization org = boService.findByPrimaryKey(Organization.class, detail.getPrimaryKeys());
            
            if (org != null) {
                // ORGANIZATION MANAGER UNIVERSAL ID
                if (StringUtils.isNotBlank(organizationManagerUniversalId)) {
                    org.setOrganizationManagerUniversalId(organizationManagerUniversalId);
                }
                
                // ORGANIZATION PHYSICAL CAMPUS CODE
                if (StringUtils.isNotBlank(organizationPhysicalCampusCode)) {
                    org.setOrganizationPhysicalCampusCode(organizationPhysicalCampusCode);
                }
                
                // ORGANIZATION LINE 1 ADDRESS
                if (StringUtils.isNotBlank(organizationLine1Address)) {
                    org.setOrganizationLine1Address(organizationLine1Address);
                }
                
                // ORGANIZATION LINE 2 ADDRESS
                if (StringUtils.isNotBlank(organizationLine2Address)) {
                    org.setOrganizationLine2Address(organizationLine2Address);
                }
                
                // ORGANIZATION CITY NAME
                if (StringUtils.isNotBlank(organizationCityName)) {
                    org.setOrganizationCityName(organizationCityName);
                }
                
                // ORGANIZATION STATE CODE
                if (StringUtils.isNotBlank(organizationStateCode)) {
                    org.setOrganizationStateCode(organizationStateCode);
                }
                
                // ORGANIZATION ZIP CODE
                if (StringUtils.isNotBlank(organizationZipCode)) {
                    org.setOrganizationZipCode(organizationZipCode);
                }
                
                // ORGANIZATION COUNTRY CODE
                if (StringUtils.isNotBlank(organizationCountryCode)) {
                    org.setOrganizationCountryCode(organizationCountryCode);
                }
                
                globalChanges.add(org);
            }
        }
        
        return globalChanges;
    }

    @Override
    public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
        return organizationGlobalDetails;
    }

    @Override
    public boolean isPersistable() {
        /*
         * Copied this method's logic from AccountGlobal, and tweaked for use with this class.
         */
        PersistenceStructureService persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);

        // fail if the PK for this object is emtpy
        if (StringUtils.isBlank(documentNumber)) {
            return false;
        }

        // fail if the PKs for any of the contained objects are empty
        for (OrganizationGlobalDetail organization : getOrganizationGlobalDetails()) {
            if (!persistenceStructureService.hasPrimaryKeyFieldValues(organization)) {
                return false;
            }
        }

        // otherwise, its all good
        return true;
    }

    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
        // Copied and tweaked the override from AccountGlobal.
        List<Collection<PersistableBusinessObject>> managedLists = super.buildListOfDeletionAwareLists();

        managedLists.add(new ArrayList<PersistableBusinessObject>(getOrganizationGlobalDetails()));

        return managedLists;
    }



    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getOrganizationManagerUniversalId() {
        return organizationManagerUniversalId;
    }

    public void setOrganizationManagerUniversalId(String organizationManagerUniversalId) {
        this.organizationManagerUniversalId = organizationManagerUniversalId;
    }

    public String getOrganizationPhysicalCampusCode() {
        return organizationPhysicalCampusCode;
    }

    public void setOrganizationPhysicalCampusCode(String organizationPhysicalCampusCode) {
        this.organizationPhysicalCampusCode = organizationPhysicalCampusCode;
    }

    public String getOrganizationLine1Address() {
        return organizationLine1Address;
    }

    public void setOrganizationLine1Address(String organizationLine1Address) {
        this.organizationLine1Address = organizationLine1Address;
    }

    public String getOrganizationLine2Address() {
        return organizationLine2Address;
    }

    public void setOrganizationLine2Address(String organizationLine2Address) {
        this.organizationLine2Address = organizationLine2Address;
    }

    public String getOrganizationCityName() {
        return organizationCityName;
    }

    public void setOrganizationCityName(String organizationCityName) {
        this.organizationCityName = organizationCityName;
    }

    public String getOrganizationStateCode() {
        return organizationStateCode;
    }

    public void setOrganizationStateCode(String organizationStateCode) {
        this.organizationStateCode = organizationStateCode;
    }

    public String getOrganizationZipCode() {
        return organizationZipCode;
    }

    public void setOrganizationZipCode(String organizationZipCode) {
        this.organizationZipCode = organizationZipCode;
    }

    public String getOrganizationCountryCode() {
        return organizationCountryCode;
    }

    public void setOrganizationCountryCode(String organizationCountryCode) {
        this.organizationCountryCode = organizationCountryCode;
    }

    public DocumentHeader getFinancialDocument() {
        return financialDocument;
    }

    public void setFinancialDocument(DocumentHeader financialDocument) {
        this.financialDocument = financialDocument;
    }

    public Person getOrganizationManagerUniversal() {
        // Copied auto-person-update logic from similar property on Organization BO.
        this.organizationManagerUniversal = SpringContext.getBean(PersonService.class).updatePersonIfNecessary(
                organizationManagerUniversalId, organizationManagerUniversal);
        return organizationManagerUniversal;
    }

    public void setOrganizationManagerUniversal(Person organizationManagerUniversal) {
        this.organizationManagerUniversal = organizationManagerUniversal;
    }

    public Campus getOrganizationPhysicalCampus() {
        return organizationPhysicalCampus;
    }

    public void setOrganizationPhysicalCampus(Campus organizationPhysicalCampus) {
        this.organizationPhysicalCampus = organizationPhysicalCampus;
    }

    public PostalCode getPostalZip() {
        return postalZip;
    }

    public void setPostalZip(PostalCode postalZip) {
        this.postalZip = postalZip;
    }

    public Country getOrganizationCountry() {
        return organizationCountry;
    }

    public void setOrganizationCountry(Country organizationCountry) {
        this.organizationCountry = organizationCountry;
    }

    public List<OrganizationGlobalDetail> getOrganizationGlobalDetails() {
        return organizationGlobalDetails;
    }

    public void setOrganizationGlobalDetails(List<OrganizationGlobalDetail> organizationGlobalDetails) {
        this.organizationGlobalDetails = organizationGlobalDetails;
    }

    /**
     * Overridden to do special org manager object refreshing. This is a tweaked copy
     * of similar handling on the regular Organization BO.
     * 
     * @see org.kuali.kfs.krad.bo.PersistableBusinessObjectBase#refreshReferenceObject(java.lang.String)
     */
    @Override
    public void refreshReferenceObject(String referenceObjectName) {
        if (ORG_MANAGER_BO_PROPERTY.equals(referenceObjectName)) {
            getOrganizationManagerUniversal();
        } else {
            super.refreshReferenceObject(referenceObjectName);
        }
    }

}
