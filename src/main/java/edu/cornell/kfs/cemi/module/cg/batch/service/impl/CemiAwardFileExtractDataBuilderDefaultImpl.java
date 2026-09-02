package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;

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
   
    protected CemiAwardExtractOrmDao cemiAwardExtractOrmDao;
    protected CemiAwardExtractDao cemiAwardExtractDao;
    protected CemiAwardTranslateTableMaps allAwardTranslateTableMaps;
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;
    

    public CemiAwardFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final CemiAwardExtractOrmDao cemiAwardExtractOrmDao,
            final CemiAwardExtractDao cemiAwardExtractDao,
            final CemiAwardTranslateTableMaps allAwardTranslateTableMaps,
            final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiAwardFileSubmitAwardTabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(cemiAwardExtractOrmDao, "cemiAwardExtractOrmDao cannot be null");
//        Validate.notNull(cemiAwardExtractDao, "cemiAwardExtractDao cannot be null");
        Validate.notNull(allAwardTranslateTableMaps, "allAwardTranslateTableMaps cannot be null");
        
        //validate each translate tables map has been loaded
        Validate.notNull(allAwardTranslateTableMaps.getAwardLineLifecycleStatusMap(), "allAwardTranslateTableMaps.awardLineLifecycleStatusMap cannot be null");
        Validate.notNull(allAwardTranslateTableMaps.getAwardLineTypesMap(), "allAwardTranslateTableMaps.awardLineTypesMap cannot be null");
        Validate.notNull(allAwardTranslateTableMaps.getAwardPurposeMap(), "allAwardTranslateTableMaps.awardPurposeMap cannot be null");
        Validate.notNull(allAwardTranslateTableMaps.getSponsorAwardTypesMap(), "allAwardTranslateTableMaps.sponsorAwardTypesMap cannot be null");
        
        this.dateTimeService = dateTimeService;
        this.cemiAwardExtractOrmDao = cemiAwardExtractOrmDao;
        this.cemiAwardExtractDao = cemiAwardExtractDao;
        this.allAwardTranslateTableMaps = allAwardTranslateTableMaps;
        this.maskSensitiveData = maskSensitiveData;
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
            CemiAwardLegacyNovelutionBo awardNovelutionAttributes = obtainAssociatedNovelutionData(award.getProposalNumber());
            
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
    
    private CemiAwardLegacyNovelutionBo obtainAssociatedNovelutionData(String awardProposalNumber) {
        List<CemiAwardLegacyNovelutionBo> novelutionAttributes = 
                cemiAwardExtractOrmDao.getAwardNovelutionAtributesForCemiAwardExtractAsCloseableStream(awardProposalNumber);
        if (novelutionAttributes.isEmpty()) {
            throw new IllegalStateException("This should never happen. No Novelution data was found for award: " + awardProposalNumber);
        } else if (novelutionAttributes.size() != 1) {
            throw new IllegalStateException("This should never happen. More than one row of Novelution data was found for award: " + awardProposalNumber);
        } 
        return novelutionAttributes.get(0);
    }

    private void createAndStoreAwardFileSubmitAwardTabRowBo(
            final CemiAwardHeaderDataBo headerBo, final CemiAwardLineDataBo awardLineBo,
            final CemiAwardSpecialConditionDataBo specialConditionBo, final CemiAwardBudgetDataBo budgetBo,
            final CemiAwardAllocationDataBo allocationBo) {
        final CemiAwardFileSubmitAwardTabRowBo tabRowBo = CemiAwardFileSubmitAwardTabRowBoFactory.createTabRowBoFrom(headerBo, awardLineBo, specialConditionBo, budgetBo, allocationBo);
        storeSheetRow(tabRowBo);
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
