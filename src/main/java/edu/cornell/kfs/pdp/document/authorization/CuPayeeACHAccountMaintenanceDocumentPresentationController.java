package edu.cornell.kfs.pdp.document.authorization;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.authorization.PayeeACHAccountMaintenanceDocumentPresentationController;
import org.kuali.rice.kns.document.MaintenanceDocument;


public class CuPayeeACHAccountMaintenanceDocumentPresentationController extends PayeeACHAccountMaintenanceDocumentPresentationController {
    
    /**
     * Adds the payeeEmailAddress field as readOnly if payee type is Employee or Entity.
     * @see org.kuali.rice.kns.document.authorization.MaintenanceDocumentPresentationControllerBase#getConditionallyReadOnlyPropertyNames(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    public Set<String> getConditionallyReadOnlyPropertyNames(MaintenanceDocument document) {
        Set<String> readOnlyPropertyNames = super.getConditionallyReadOnlyPropertyNames(document);
        
        PayeeACHAccount payeeAccount = (PayeeACHAccount)document.getNewMaintainableObject().getBusinessObject();
        String payeeIdTypeCode = payeeAccount.getPayeeIdentifierTypeCode();

        // make name and email address readOnly if payee type is Employee or Entity
        if (StringUtils.equalsIgnoreCase(payeeIdTypeCode, PayeeIdTypeCodes.EMPLOYEE) ||
                StringUtils.equalsIgnoreCase(payeeIdTypeCode, PayeeIdTypeCodes.ENTITY)) {
            
            if(readOnlyPropertyNames.contains(PdpPropertyConstants.PAYEE_EMAIL_ADDRESS)){
                readOnlyPropertyNames.remove(PdpPropertyConstants.PAYEE_EMAIL_ADDRESS);
            }
        }
        
        return readOnlyPropertyNames;                
    }

}
