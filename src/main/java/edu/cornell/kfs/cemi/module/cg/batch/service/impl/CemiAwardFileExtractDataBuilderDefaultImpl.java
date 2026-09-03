package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableFactory;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableMaps;
import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants.AwardTranslateTables;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardAllocationDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardFileSubmitAwardTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardHeaderDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyNovelutionBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLineDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardSpecialConditionDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardFileSubmitAwardTabRowBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardHeaderDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CemiAwardFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiAwardFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    protected DateTimeService dateTimeService;
    protected CemiAwardExtractOrmDao cemiAwardExtractOrmDao;
    protected CemiAwardExtractDao cemiAwardExtractDao;
    protected Writer skippedAwardsWriter;
    protected final boolean maskSensitiveData;
    
    protected CemiAwardTranslateTableMaps allAwardTranslateTableMaps;

    public CemiAwardFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final CemiAwardExtractOrmDao cemiAwardExtractOrmDao,
            final CemiAwardExtractDao cemiAwardExtractDao,
            final Writer skippedAwardsWriter,
            final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiAwardFileSubmitAwardTabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(cemiAwardExtractOrmDao, "cemiAwardExtractOrmDao cannot be null");
        Validate.notNull(cemiAwardExtractDao, "cemiAwardExtractDao cannot be null");
        Validate.notNull(skippedAwardsWriter, "skippedAwardsWriter cannot be null");
        this.dateTimeService = dateTimeService;
        this.cemiAwardExtractOrmDao = cemiAwardExtractOrmDao;
        this.cemiAwardExtractDao = cemiAwardExtractDao;
        this.skippedAwardsWriter = skippedAwardsWriter;
        this.maskSensitiveData = maskSensitiveData;
        
        populateAllAwardTranslateTableMaps();
    }
    
    @Override
    public void writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage(final Iterator<Award> awards){
        int awardCount = 0;
        int totalRowsWritten = 0;
        
        for (final Award award : IteratorUtils.asIterable(awards)) {
            awardCount++;
            if (awardCount % 1000 == 0) {
                LOG.info("writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage, Processed {} "
                        + "Awards for Submit Award and counting...", awardCount);
            }
            
            //Gather all the data specific to the award being converted. 
            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
            CemiAwardLegacyNovelutionBo awardNovelutionAttributes = 
                    obtainAssociatedNovelutionData(award.getProposalNumber(), skippedAwardsWriter);
            if (ObjectUtils.isNull(awardNovelutionAttributes)) {
                //problem encountered retrieving Novelution data for award, skip (downstream processing will fail 
                continue;
            }
            
            //Database table storage of data extract
            totalRowsWritten += createAndStoreAwardFileSubmitAwardTabRowsFor(award, awardExtendedAttribute, 
                    awardNovelutionAttributes, jobRunDateString);
        }
        LOG.info("writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage, Finished writing "
                + "{} Submit Award data rows for {} Awards.", totalRowsWritten, awardCount);
    }
    
    protected int createAndStoreAwardFileSubmitAwardTabRowsFor(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute,
            final CemiAwardLegacyNovelutionBo awardNovelutionAttributes,
            final String jobRunDateString) {
        
        int numAwardFileLinesGenerated = 0;
        
        final CemiAwardHeaderDataBo headerBo = CemiAwardHeaderDataBoFactory.createAwardHeaderDataBoFrom(award,
                awardExtendedAttribute, awardNovelutionAttributes, jobRunDateString, dateTimeService,
                cemiAwardExtractDao, allAwardTranslateTableMaps, maskSensitiveData);
        
        final CemiAwardLineDataBo awardLineBo = null;
        final CemiAwardSpecialConditionDataBo specialConditionBo = null;
        final CemiAwardBudgetDataBo budgetBo = null;
        final CemiAwardAllocationDataBo allocationBo = null;

        //use factory to populate CemiAwardHeaderDataBo 
        //get the accounting lines for the award
        //if no accounting lines found
        //      log error message
        //      return 0 and put nothing in database table storage
        // accountCounter = 0;
        // numAccountingLines
        //for every account
        //    
        createAndStoreAwardFileSubmitAwardTabRowBo(headerBo, awardLineBo, specialConditionBo, budgetBo, allocationBo);
        numAwardFileLinesGenerated++;

        return numAwardFileLinesGenerated;
    }
    
    private CemiAwardLegacyNovelutionBo obtainAssociatedNovelutionData(
            final String awardProposalNumber, final Writer skippedAwardsWriter) {
        List<CemiAwardLegacyNovelutionBo> novelutionAttributes = 
                cemiAwardExtractOrmDao.getAwardNovelutionAtributesForCemiAwardExtractAsCloseableStream(awardProposalNumber);
        if (ObjectUtils.isNull(novelutionAttributes) || novelutionAttributes.size() == 0) {
            writeSkippedAwardToReportFile(awardProposalNumber, "No Novelution data was found for the award.");
            return null;
        } else if (novelutionAttributes.size() == 1) {
            return novelutionAttributes.get(0);
        } 
        writeSkippedAwardToReportFile(awardProposalNumber, "More than one row of Novelution data was found for the award.");
        return null;
    }
    
//    private CemiAwardLegacyNovelutionBo obtainAssociatedNovelutionData(
//            final String awardProposalNumber, final Writer skippedAwardsWriter) {
//        List<CemiAwardLegacyNovelutionBo> novelutionAttributes = 
//                cemiAwardExtractOrmDao.getAwardNovelutionAtributesForCemiAwardExtractAsCloseableStream(awardProposalNumber);
//        if (ObjectUtils.isNull(novelutionAttributes)) {
//            writeSkippedAwardToReportFile(awardProposalNumber, "No Novelution data was found for the award " + awardProposalNumber);
//            return null;
//        }
//        return novelutionAttributes.get(0);
//    }

    private void createAndStoreAwardFileSubmitAwardTabRowBo(
            final CemiAwardHeaderDataBo headerBo, final CemiAwardLineDataBo awardLineBo,
            final CemiAwardSpecialConditionDataBo specialConditionBo, final CemiAwardBudgetDataBo budgetBo,
            final CemiAwardAllocationDataBo allocationBo) {
        final CemiAwardFileSubmitAwardTabRowBo tabRowBo = CemiAwardFileSubmitAwardTabRowBoFactory.createTabRowBoFrom(headerBo, awardLineBo, specialConditionBo, budgetBo, allocationBo);
        storeSheetRow(tabRowBo);
    }
    
    private void populateAllAwardTranslateTableMaps() {
        allAwardTranslateTableMaps = new CemiAwardTranslateTableMaps();
        
        allAwardTranslateTableMaps.setSponsorAwardTypesMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.SPONSOR_AWARD_TYPES_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardPurposeMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_PURPOSE_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardLineLifecycleStatusMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_LINE_LIFECYCLE_STATUS_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardLineTypesMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_LINE_TYPES_QUERY, cemiAwardExtractDao));
    }
    
    private void writeSkippedAwardToReportFile(final String awardProposalNumber, final String errorMessage) {
        try {
            String errorToLog = String.join(
                    KFSConstants.BLANK_SPACE, awardProposalNumber, "was skipped. Processing encountered:", errorMessage );
            skippedAwardsWriter.write(errorToLog);
            skippedAwardsWriter.write(KFSConstants.NEWLINE);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
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

}
