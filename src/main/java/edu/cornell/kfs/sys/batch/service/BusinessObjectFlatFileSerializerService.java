package edu.cornell.kfs.sys.batch.service;

import org.kuali.rice.krad.bo.BusinessObject;

/**
 * Base interface for services that can serialize a BO into a flat file.
 */
@SuppressWarnings("deprecation")
public interface BusinessObjectFlatFileSerializerService {

    boolean serializeToFlatFile(String fileName, BusinessObject objectToSerialize);

}
