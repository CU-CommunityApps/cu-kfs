package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Agency;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiSponsorFileSponsorTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiSponsorFileSponsorTabRowBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiSponsorFileExtractDataBuilder;
//import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardScheduleFileAwardScheduleTabRowBo;
//import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardScheduleFileExtractDataBuilder;
//import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractDao;
//import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractOrmDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiSponsorExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiSponsorExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;

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

public class CemiSponsorFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiSponsorFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    protected CemiSponsorExtractOrmDao cemiSponsorExtractOrmDao;
    protected CemiSponsorExtractDao cemiSponsorExtractDao;
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;

    //
    public CemiSponsorFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final CemiSponsorExtractOrmDao cemiSponsorExtractOrmDao,
            final CemiSponsorExtractDao cemiSponsorExtractDao, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiSponsorFileSponsorTabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(cemiSponsorExtractOrmDao, "CemiSponsorExtractOrmDao cannot be null");
        Validate.notNull(cemiSponsorExtractDao, "cemiSponsorExtractDao cannot be null");
        this.dateTimeService = dateTimeService;
        this.cemiSponsorExtractOrmDao = cemiSponsorExtractOrmDao;
        this.cemiSponsorExtractDao = cemiSponsorExtractDao;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writeSponsorFileSponsorTabExtractDataToIntermediateStorage(final Iterator<Agency> agencies) {
        createAndStoreSponsorFileSponsorTabRowFor(jobRunDateString);
        LOG.info("writeSponsorFileSponsorTabExtractDataToIntermediateStorage, Finished writing shell constant row to table.");

//        int awardScheduleTabRowCount = 0;
//        for (final Award award : IteratorUtils.asIterable(awards)) {
//            awardScheduleTabRowCount++;
//            if (awardScheduleTabRowCount % 1000 == 0) {
//                LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Processed {} "
//                        + "Awards for Award Schedule and counting...", awardScheduleTabRowCount);
//            }
//            //Award Schedule Tab
//            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
//            //Database table storage of data extract
//            createAndStoreAwardScheduleFileAwardScheduleTabRow(award, awardExtendedAttribute, jobRunDateString);
//        }
//        LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Finished writing {} "
//                + "Awards for Award Schedule", awardScheduleTabRowCount);
    }
    
    protected void createAndStoreSponsorFileSponsorTabRowFor(final String jobRunDateString) {
        CemiSponsorFileSponsorTabRowBoFactory factoryForShellConstantBo = 
                new CemiSponsorFileSponsorTabRowBoFactory(jobRunDateString, dateTimeService, maskSensitiveData);
        
        final CemiSponsorFileSponsorTabRowBo shellConstantLine = factoryForShellConstantBo.createCemiSponsorFileSponsorTabRowBo();
        storeSheetRow(shellConstantLine);
    }

//
// EXAMPLE: 
// This is an actual example used by a data extract. The method is called by the public routine above to create
// and store to a database table a SINGLE row of information representing a data extraction spreadsheet line.
// Depending upon this method's logic and the data objects used, this method could generate MULTIPLE lines of
// information; therefore you will need to name this protected method accordingly.
//
//    protected void createAndStoreAwardScheduleFileAwardScheduleTabRow(final CemiExampleLEGACYOBJECT legacyObject, 
//            final AwardExtendedAttribute awardExtendedAttribute, final String jobRunDateString) {
//
//        CemiAwardScheduleFileAwardScheduleTabRowBoFactory factoryForBo = 
//                new CemiAwardScheduleFileAwardScheduleTabRowBoFactory(award, awardExtendedAttribute, jobRunDateString,
//                        dateTimeService, maskSensitiveData);
//        
//        CemiAwardScheduleFileAwardScheduleTabRowBo awardScheduleTabRow = factoryForBo.createCemiAwardScheduleFileAwardScheduleTabRowBo();
//        storeSheetRow(awardScheduleTabRow);
//        
//        // EXAMPLE:
//        // Record identifier associations for Award Schedule extract file based upon batch job run date in this 
//        // separate table only if is NOT already being tracked. 
//        recordAwardScheduleIdentifiersInLegacyAssociationTable(awardScheduleTabRow.getSpreadsheetKey(), 
//                awardScheduleTabRow.getAwardScheduleReferenceId(), awardScheduleTabRow.getJobRunDateString());
//    }
//    
//    // EXAMPLE:
//    // Retain in the database an association between the new Workday data key - legacy system data key - extraction run date
//    // only if it is not already being tracked in the table used to maintain the data extraction information.
//    //protected void recordAwardScheduleIdentifiersInLegacyAssociationTable(final String spreadsheetKey,
//    //        final String  awardProposalNumber, final String jobRunDateString) {
//    //    getCemiAwardScheduleExtractDao().storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(spreadsheetKey,
//    //            awardProposalNumber, jobRunDateString);
//    //}

}
