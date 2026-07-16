package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.io.IOException;
import java.sql.Date;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.CemiAwardScheduleConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardScheduleFileAwardScheduleTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardScheduleFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CemiAwardScheduleFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiAwardScheduleFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    protected CemiAwardScheduleExtractOrmDao cemiAwardScheduleExtractOrmDao;
    protected CemiAwardScheduleExtractDao cemiAwardScheduleExtractDao;
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;

    public CemiAwardScheduleFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDate,
            final DateTimeService dateTimeService,
            final CemiAwardScheduleExtractOrmDao cemiAwardScheduleExtractOrmDao,
            final CemiAwardScheduleExtractDao cemiAwardScheduleExtractDao, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDate, CemiAwardScheduleFileAwardScheduleTabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(cemiAwardScheduleExtractOrmDao, "cemiAwardScheduleExtractOrmDao cannot be null");
        Validate.notNull(cemiAwardScheduleExtractDao, "cemiAwardScheduleExtractDao cannot be null");
        this.dateTimeService = dateTimeService;
        this.cemiAwardScheduleExtractOrmDao = cemiAwardScheduleExtractOrmDao;
        this.cemiAwardScheduleExtractDao = cemiAwardScheduleExtractDao;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage(
            final Iterator<Award> awards, String jobRunDateString) {

        int awardScheduleTabRowCount = 0; // MAY NEED TO SET TO PREVIOUS RUN STARTING POINT
        
        for (final Award award : IteratorUtils.asIterable(awards)) {
            
            if (awardScheduleTabRowCount % 1000 == 0) {
                LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Processed {} "
                        + "Awards for Award Schedule and counting...", awardScheduleTabRowCount);
            }
            
            //Award Schedule Tab
            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
            
//            final CemiAwardScheduleFileAwardScheduleTabRowBo awardSchedule = new CemiAwardScheduleFileAwardScheduleTabRowBo(award, spreadsheetKey, 
//            awardScheduleReferenceId, awardIntervalStartDate, awardIntervalEndDate, maskSensitiveData);
  
            //Database table storage of data extract
//original   createAndStoreAwardScheduleFileAwardScheduleTabRows(awardSchedule, award.getProposalNumber(), jobRunDate, awardScheduleTabTableSequence);
            createAndStoreAwardScheduleFileAwardScheduleTabRows(award, awardExtendedAttribute, jobRunDateString, awardScheduleTabRowCount);
            
            //Record identifier associations for Award Schedule extract file based upon batch job run date
            recordAwardScheduleIdentifiersInLegacyAssociationTable(spreadsheetKey, awardScheduleReferenceId, jobRunDate);

            
//
//            //Award Schedule Tab
//            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
//  in foctory          String awardIntervalStartDateString = determineFormattedDate(awardExtendedAttribute.getBudgetBeginningDate());
//   in factory         String awardIntervalEndDateString = determineFormattedDate(awardExtendedAttribute.getBudgetEndingDate());
//   in factory         String spreadsheetKey = buildSpreadsheetKey(award.getProposalNumber());
//    in factory        String awardScheduleReferenceId = buildAwardScheduleReferenceId(award.getProposalNumber());
//   was dto         final CemiAwardSchedule awardSchedule = new CemiAwardSchedule(award, spreadsheetKey, 
//                    awardScheduleReferenceId, awardIntervalStartDate, awardIntervalEndDate, maskSensitiveData);
//            //Database table storage of data extract
//            saveAwardScheduleRowToTable(awardSchedule, award.getProposalNumber(), jobRunDate, awardScheduleTabTableSequence);
//            
//            //csv storage of data extract
//            writeAwardScheduleRowToFiles(awardSchedule);
//            
            //Record identifier associations for Award Schedule extract file based upon batch job run date
            recordAwardScheduleIdentifiersInLegacyAssociationTable(spreadsheetKey, awardScheduleReferenceId, jobRunDate);
        }
        LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Finished writing {} "
                + "Awards for Award Schedule", awardScheduleTabRowCount);
    }
    
    protected void createAndStoreAwardScheduleFileAwardScheduleTabRows(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute, final String jobRunDateString, final int awardScheduleTabRowCount) {

        CemiAwardScheduleFileAwardScheduleTabRowBo awardScheduleTabRow = 
//                new CemiAwardScheduleFileAwardScheduleTabRowBoFactory(award, awardExtendedAttribute, proposalNumberUsed, jobRunDate, awardScheduleTabTableSequence);
                new CemiAwardScheduleFileAwardScheduleTabRowBoFactory(award, awardExtendedAttribute, jobRunDateString, awardScheduleTabRowCount);
        
        storeSheetRow(awardScheduleTabRow);
    }
    
    // Retain in the database an association between the new Workday data key - legacy system data key - extraction run date
    protected void recordAwardScheduleIdentifiersInLegacyAssociationTable(final String spreadsheetKey,
            final String  awardProposalNumber, final LocalDateTime jobRunDate) {
        getCemiAwardScheduleExtractDao().storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(spreadsheetKey,
                awardProposalNumber, jobRunDate);
    }

    public CemiAwardScheduleExtractDao getCemiAwardScheduleExtractDao() {
        return cemiAwardScheduleExtractDao;
    }

    public void setCemiAwardScheduleExtractDao(CemiAwardScheduleExtractDao cemiAwardScheduleExtractDao) {
        this.cemiAwardScheduleExtractDao = cemiAwardScheduleExtractDao;
    }

}
