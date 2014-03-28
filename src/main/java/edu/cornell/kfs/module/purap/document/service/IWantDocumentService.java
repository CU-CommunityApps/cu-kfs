package edu.cornell.kfs.module.purap.document.service;

import java.util.List;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.web.struts.RequisitionForm;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.businessobject.PersonData;
import edu.cornell.kfs.module.purap.document.IWantDocument;

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
     * Creates a RequisitionDocument based on the information on the I Want Document.
     * 
     * @param iWantDocument
     * @return a requisition document
     */
    public RequisitionDocument setUpRequisitionDetailsFromIWantDoc(IWantDocument iWantDocument, RequisitionDocument requisitionDocumentBase, RequisitionForm requisitionForm) throws Exception;
    
    /**
     * Creates a DisbursementVoucherDocument based on the information on the I Want Document.
     * 
     * @param iWantDocument
     * @return a DisbursementVoucherDocument
     */
    public DisbursementVoucherDocument setUpDVDetailsFromIWantDoc(IWantDocument iWantDocument, DisbursementVoucherDocument disbursementVoucherDocument, DisbursementVoucherForm disbursementVoucherForm) throws Exception;

    // KFSPTS-2527
    /**
     * Gets the related I Want document ID for the given DV document ID. This will be null if the DV was not created from an I Want doc.
     * 
     * @param dvID
     * @return the related I Want document ID if DV was created from an I Want doc, null otherwise.
     */
    public String getIWantDocIDByDVId(String dvID);
    
    /**
     * Determines if the given DV was created from an I Want doc.
     * 
     * @param dvID
     * @return true if DV was created from an I Want doc, false otherwise
     */
    public boolean isDVgeneratedByIWantDoc(String dvID);
    // end KFSPTS-2527
}
