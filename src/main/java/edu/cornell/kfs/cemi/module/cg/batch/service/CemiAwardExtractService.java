package  edu.cornell.kfs.cemi.module.cg.batch.service;

import java.time.LocalDateTime;

public interface CemiAwardExtractService {
    
    void resetState();
    
    void initializeExtractDateSettings();
    
    void captureInScopeBusinessObjectKeysToProcessingTable();
    
//    void gatherAndPopulateRawDataTables(final LocalDateTime jobRunDate);
//    // Using the in scope data keys (from the tables that WILL be truncated each time the batch job runs)
//    // gather the raw information into unflattened tables that will be read to create the data extract.
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    @Override
//    public void gatherAndPopulateRawDataTables(final LocalDateTime jobRunDate) {
//        LOG.info("gatherAndPopulateRawDataTables, Generating RAW data rows that will be placed in parent-child TABLE storage.");
//        
//        try ( 
//                final Stream<Award> legacyObjects = 
//                        cemiAwardExtractOrmDao.getAwardForCemiAwardExtractAsCloseableStream();
//        ) {
//            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
//            final CemiAwardFileExtractDataBuilderDefaultImpl rawDataBuilder = new CemiAwardFileExtractDataBuilderDefaultImpl(
//                    businessObjectService, jobRunDateString, dateTimeService, cemiAwardExtractOrmDao,
//                    cemiAwardExtractDao, shouldMaskCemiSensitiveData());
//            final Iterator<Award> legacyObjectIterator = legacyObjects.iterator();
//            //class org.kuali.kfs.cemi.module.cg.businessobject.CemiAwardFileRawAwardHeaderFieldsRowBo needs to be loaded
//            //with the raw data by the data builder
//            rawDataBuilder.writeAwardFileRawAwardHeaderDataInterimStorage(legacyObjectIterator);
//        }
//    }
    
    
    
    void generateIntermediateExtractData(final LocalDateTime jobRunDate);
    
    void generateDataConversionExtractFile(final LocalDateTime jobRunDate);
    
}
