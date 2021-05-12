package edu.cornell.kfs.concur.batch.service;

import org.kuali.kfs.krad.bo.BusinessObject;

/**
 * Base interface for services that can serialize a BO into a flat file.
 */
@SuppressWarnings("deprecation")
public interface BusinessObjectFlatFileSerializerService {

    boolean serializeToFlatFile(String fileName, BusinessObject objectToSerialize);

}
