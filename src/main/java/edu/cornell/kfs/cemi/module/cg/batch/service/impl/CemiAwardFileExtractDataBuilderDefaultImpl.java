package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.text.MessageFormat;
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

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableFactory;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableMaps;
import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants.AwardTranslateTables;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardAllocationDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardFileSubmitAwardTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardHeaderDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyNovelutionBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLineDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardSpecialConditionDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardAllocationDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardBudgetDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardFileSubmitAwardTabRowBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardHeaderDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardLegacyNovelutionBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardLineDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardSpecialConditionDataBoFactory;
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
            String awardOrgCode = cemiAwardExtractDao.findOrganizationCodeForInScopeAward(award.getProposalNumber());
            
            CemiAwardLegacyNovelutionBo awardNovelutionAttributes =
                    obtainAssociatedNovelutionData(award.getProposalNumber(), skippedAwardsWriter);
            
            if (ObjectUtils.isNull(awardNovelutionAttributes)) {
                // problem encountered retrieving Novelution data for award
                // set to emply business object as downstream processing will fail 
                awardNovelutionAttributes = CemiAwardLegacyNovelutionBoFactory.createEmptyCemiAwardLegacyNovelutionBo();
            }
            //Database table storage of data extract
            totalRowsWritten += createAndStoreAwardFileSubmitAwardTabRowsFor(award, awardExtendedAttribute, awardOrgCode,
                    awardNovelutionAttributes, jobRunDateString);
        }
        LOG.info("writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage, Finished writing "
                + "{} Submit Award data rows for {} Awards.", totalRowsWritten, awardCount);
    }
    
    protected int createAndStoreAwardFileSubmitAwardTabRowsFor(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute,
            final String awardOrgCode,
            final CemiAwardLegacyNovelutionBo awardNovelutionAttributes,
            final String jobRunDateString) {
        
        int numAwardFileLinesGenerated = 0;
        
        final CemiAwardHeaderDataBo headerBo = CemiAwardHeaderDataBoFactory.createAwardHeaderDataBoFrom(award,
                awardExtendedAttribute, awardOrgCode, awardNovelutionAttributes, jobRunDateString, dateTimeService,
                cemiAwardExtractDao, allAwardTranslateTableMaps, maskSensitiveData);
        
        final CemiAwardLineDataBo awardLineBo = CemiAwardLineDataBoFactory.createEmptyCemiAwardLineDataBo();
        final CemiAwardSpecialConditionDataBo specialConditionBo = CemiAwardSpecialConditionDataBoFactory.createEmptyCemiAwardSpecialConditionDataBo();
        final CemiAwardBudgetDataBo budgetBo = CemiAwardBudgetDataBoFactory.createEmptyCemiAwardBudgetDataBo();
        final CemiAwardAllocationDataBo allocationBo = CemiAwardAllocationDataBoFactory.createEmptyCemiAwardAllocationDataBo();

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
        String spreadsheetKeyToSearchFor = MessageFormat.format(CemiAwardConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
        if (cemiAwardExtractDao.novelutionDataContainsAwardExtractBuiltReferenceId(spreadsheetKeyToSearchFor)) {
            CemiAwardLegacyNovelutionBo novelutionAttributes = 
                    cemiAwardExtractOrmDao.getAwardNovelutionAtributesForCemiAwardExtractBySpreadsheetKey(spreadsheetKeyToSearchFor, skippedAwardsWriter);
            return novelutionAttributes;
        }
        writeSkippedAwardToReportFile(awardProposalNumber, 
                "More than one row or no rows of Novelution data found for spreadsheet_key: " + spreadsheetKeyToSearchFor);
        return null;
    }
    
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


}
