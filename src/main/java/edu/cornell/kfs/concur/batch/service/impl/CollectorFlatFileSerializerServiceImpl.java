package edu.cornell.kfs.concur.batch.service.impl;

import java.util.List;

import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.krad.bo.BusinessObject;

/**
 * Helper service for serializing CollectorBatch objects to Collector flat files.
 * 
 * It is assumed that the CollectorBatch object has all of its header and trailer fields populated,
 * and has configured its list of OriginEntryFull objects appropriately. The CollectorBatch BO
 * will be used as both the header and trailer/footer objects; the associated utility classes
 * will forcibly override the record type appropriately when writing the lines.
 * 
 * This implementation does not support serializing any CollectorDetail BOs on the CollectorBatch BO.
 */
@SuppressWarnings("deprecation")
public class CollectorFlatFileSerializerServiceImpl extends BusinessObjectFlatFileSerializerServiceBase {

    @Override
    protected BusinessObject getHeader(BusinessObject objectToSerialize) {
        return objectToSerialize;
    }

    @Override
    protected List<? extends BusinessObject> getLineItems(BusinessObject objectToSerialize) {
        return ((CollectorBatch) objectToSerialize).getOriginEntries();
    }

    @Override
    protected BusinessObject getFooter(BusinessObject objectToSerialize) {
        return objectToSerialize;
    }

}
