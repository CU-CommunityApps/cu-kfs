package edu.cornell.kfs.cemi.vnd.batch.service;

import java.util.Iterator;

import org.kuali.kfs.vnd.businessobject.VendorContact;

public interface CemiEntityContactFileExtractDataBuilder {

    void writeEntityContactFileEntityContactTabExtractDataToIntermediateStorage(
            final Iterator<VendorContact> legacyVendorContacts);

}
