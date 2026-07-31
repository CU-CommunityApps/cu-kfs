package edu.cornell.kfs.cemi.vnd.batch.service;

import java.util.Iterator;

import org.kuali.kfs.vnd.businessobject.VendorContact;

// This service's implementation deals with all the logic required to gather the information required for the
// data extraction file. This method will contain all of the control logic properly manage parent-child data 
// relationships. Additional private methods will be needed in the service layer to assist with that management.
//
// No data conversion logic should be placed in this implemenation.

public interface CemiEntityContactFileExtractDataBuilder {

    void writeEntityContactFileEntityContactTabExtractDataToIntermediateStorage(
            final Iterator<VendorContact> legacyVendorContacts);

}
