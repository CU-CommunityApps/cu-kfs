package edu.cornell.kfs.cemi.patterntemplate.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
//import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.patterntemplate.batch.businessobject.CemiExampleLEGACYOBJECT;
import edu.cornell.kfs.cemi.patterntemplate.batch.businessobject.CemiEXTRACTNAMEFileTABNAMETabRowBo;
//import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardScheduleFileAwardScheduleTabRowBo;
//import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardScheduleFileExtractDataBuilder;
//import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractDao;
//import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractOrmDao;
import edu.cornell.kfs.cemi.patterntemplate.batch.service.CemiEXTRACTNAMEFileExtractDataBuilder;
import edu.cornell.kfs.cemi.patterntemplate.dataaccess.CemiEXTRACTNAMEExtractDao;
import edu.cornell.kfs.cemi.patterntemplate.dataaccess.CemiEXTRACTNAMEExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

// The code from an existing data extract was left as comments in each method to provide specific examples.
// The constructor for this class will need to accept and verify as valid any and all services required to perform
// the data gathering logic. 
//
// Extended attributes may need to be retrieved. Depending on how the data mapping template is designed,
// cardinality may be across multiple tabs OR may be a single tab where parts of a row repeats with
// the unique portion of the data on the end of the row. Meaning parent-child relationships could span 
// tabs or could need to be dealt with on a single tab. This service implementation would deal with that complexity.
//
// This service would also perform the call to the business object factory/factories needed to appropriately store 
// the data rows in table(s) that represent the actual sheets of the data extract spreadsheet being created.

public class CemiEXTRACTNAMEFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiEXTRACTNAMEFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    protected CemiEXTRACTNAMEExtractOrmDao cemiEXTRACTNAMEExtractOrmDao;
    protected CemiEXTRACTNAMEExtractDao cemiEXTRACTNAMEExtractDao;
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;

    //
    public CemiEXTRACTNAMEFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final CemiEXTRACTNAMEExtractOrmDao cemiEXTRACTNAMEExtractOrmDao,
            final CemiEXTRACTNAMEExtractDao cemiEXTRACTNAMEExtractDao, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiEXTRACTNAMEFileTABNAMETabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(cemiEXTRACTNAMEExtractOrmDao, "CemiEXTRACTNAMEExtractOrmDao cannot be null");
        Validate.notNull(cemiEXTRACTNAMEExtractDao, "cemiAwardScheduleExtractDao cannot be null");
        this.dateTimeService = dateTimeService;
        this.cemiEXTRACTNAMEExtractOrmDao = cemiEXTRACTNAMEExtractOrmDao;
        this.cemiEXTRACTNAMEExtractDao = cemiEXTRACTNAMEExtractDao;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writeEXTRACTNAMEFileTABNAMETabExtractDataToIntermediateStorage(final Iterator<CemiExampleLEGACYOBJECT> legacyObjects){
//        int awardScheduleTabRowCount = 0; // MAY NEED TO SET TO PREVIOUS RUN STARTING POINT
//        for (final Award award : IteratorUtils.asIterable(awards)) {
//            if (awardScheduleTabRowCount % 1000 == 0) {
//                LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Processed {} "
//                        + "Awards for Award Schedule and counting...", awardScheduleTabRowCount);
//            }
//            //Award Schedule Tab
//            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
//            //Database table storage of data extract
//            createAndStoreAwardScheduleFileAwardScheduleTabRows(award, awardExtendedAttribute, getJobRunDateString(), awardScheduleTabRowCount);
//        }
//        LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Finished writing {} "
//                + "Awards for Award Schedule", awardScheduleTabRowCount);
    }
    
    protected void createAndStoreAwardScheduleFileAwardScheduleTabRows(final CemiExampleLEGACYOBJECT legacyObject, 
            final AwardExtendedAttribute awardExtendedAttribute, final String jobRunDateString, final int awardScheduleTabRowCount) {
//
//        CemiAwardScheduleFileAwardScheduleTabRowBoFactory factoryForBo = 
//                new CemiAwardScheduleFileAwardScheduleTabRowBoFactory(award, awardExtendedAttribute, jobRunDateString,
//                        getDateTimeService(), maskSensitiveData);
//        
//        CemiAwardScheduleFileAwardScheduleTabRowBo awardScheduleTabRow = factoryForBo.createCemiAwardScheduleFileAwardScheduleTabRowBo();
//        storeSheetRow(awardScheduleTabRow);
//        
//        // EXAMPLE:
//        // Record identifier associations for Award Schedule extract file based upon batch job run date in this 
//        // separate table only if is NOT already being tracked. 
//        recordAwardScheduleIdentifiersInLegacyAssociationTable(awardScheduleTabRow.getSpreadsheetKey(), 
//                awardScheduleTabRow.getAwardScheduleReferenceId(), awardScheduleTabRow.getJobRunDateString());
    }
    
    // EXAMPLE:
    // Retain in the database an association between the new Workday data key - legacy system data key - extraction run date
    // only if it is not already being tracked in the table used to maintain the data extraction information.
    //protected void recordAwardScheduleIdentifiersInLegacyAssociationTable(final String spreadsheetKey,
    //        final String  awardProposalNumber, final String jobRunDateString) {
    //    getCemiAwardScheduleExtractDao().storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(spreadsheetKey,
    //            awardProposalNumber, jobRunDateString);
    //}

    public CemiEXTRACTNAMEExtractDao getCemiAwardScheduleExtractDao() {
        return cemiEXTRACTNAMEExtractDao;
    }

    public void setCemiEXTRACTNAMEExtractDao(CemiEXTRACTNAMEExtractDao cemiEXTRACTNAMEExtractDao) {
        this.cemiEXTRACTNAMEExtractDao = cemiEXTRACTNAMEExtractDao;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
