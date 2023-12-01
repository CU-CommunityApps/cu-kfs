package edu.cornell.kfs.module.cg.document;

import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.document.AgencyMaintainableImpl;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;

public class CuAgencyMaintainableImpl extends AgencyMaintainableImpl {

    private static final long serialVersionUID = 3088332201792805640L;

    /**
     * When CGB is enabled and the document is entering PROCESSED state, AgencyMaintainableImpl does
     * some custom processing that will prematurely save the agency before performing any auto-setup
     * of keys on the referenced objects. Thus, the agency extension needs to have its agency number
     * manually set here, to avoid errors when persisting it.
     */
    @Override
    public void prepareForSave() {
        super.prepareForSave();
        final Agency agency = (Agency) getDataObject();
        final AgencyExtendedAttribute agencyExtension = (AgencyExtendedAttribute) agency.getExtension();
        agencyExtension.setAgencyNumber(agency.getAgencyNumber());
    }

}
