package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardScheduleFileAwardScheduleTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardScheduleFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CemiAwardScheduleFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiAwardScheduleFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    protected CemiAwardScheduleExtractOrmDao cemiAwardScheduleExtractOrmDao;
    protected CemiAwardScheduleExtractDao cemiAwardScheduleExtractDao;
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;

    public CemiAwardScheduleFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final CemiAwardScheduleExtractOrmDao cemiAwardScheduleExtractOrmDao,
            final CemiAwardScheduleExtractDao cemiAwardScheduleExtractDao, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiAwardScheduleFileAwardScheduleTabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(cemiAwardScheduleExtractOrmDao, "cemiAwardScheduleExtractOrmDao cannot be null");
        Validate.notNull(cemiAwardScheduleExtractDao, "cemiAwardScheduleExtractDao cannot be null");
        this.dateTimeService = dateTimeService;
        this.cemiAwardScheduleExtractOrmDao = cemiAwardScheduleExtractOrmDao;
        this.cemiAwardScheduleExtractDao = cemiAwardScheduleExtractDao;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage(final Iterator<Award> awards) {
        int awardScheduleTabRowCount = 0; // MAY NEED TO SET TO PREVIOUS RUN STARTING POINT
        for (final Award award : IteratorUtils.asIterable(awards)) {
            if (awardScheduleTabRowCount % 1000 == 0) {
                LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Processed {} "
                        + "Awards for Award Schedule and counting...", awardScheduleTabRowCount);
            }
            //Award Schedule Tab
            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
            //Database table storage of data extract
            createAndStoreAwardScheduleFileAwardScheduleTabRows(award, awardExtendedAttribute, getJobRunDateString(), awardScheduleTabRowCount);
        }
        LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Finished writing {} "
                + "Awards for Award Schedule", awardScheduleTabRowCount);
    }
    
    protected void createAndStoreAwardScheduleFileAwardScheduleTabRows(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute, final String jobRunDateString, final int awardScheduleTabRowCount) {

        CemiAwardScheduleFileAwardScheduleTabRowBoFactory factoryForBo = 
                new CemiAwardScheduleFileAwardScheduleTabRowBoFactory(award, awardExtendedAttribute, jobRunDateString,
                        getDateTimeService(), maskSensitiveData);
        
        CemiAwardScheduleFileAwardScheduleTabRowBo awardScheduleTabRow = factoryForBo.createCemiAwardScheduleFileAwardScheduleTabRowBo();
        storeSheetRow(awardScheduleTabRow);
        
        //Record identifier associations for Award Schedule extract file based upon batch job run date
        recordAwardScheduleIdentifiersInLegacyAssociationTable(awardScheduleTabRow.getSpreadsheetKey(), 
                awardScheduleTabRow.getAwardScheduleReferenceId(), awardScheduleTabRow.getJobRunDateString());
    }
    
    // Retain in the database an association between the new Workday data key - legacy system data key - extraction run date
    protected void recordAwardScheduleIdentifiersInLegacyAssociationTable(final String spreadsheetKey,
            final String  awardProposalNumber, final String jobRunDateString) {
        getCemiAwardScheduleExtractDao().storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(spreadsheetKey,
                awardProposalNumber, jobRunDateString);
    }

    public CemiAwardScheduleExtractDao getCemiAwardScheduleExtractDao() {
        return cemiAwardScheduleExtractDao;
    }

    public void setCemiAwardScheduleExtractDao(CemiAwardScheduleExtractDao cemiAwardScheduleExtractDao) {
        this.cemiAwardScheduleExtractDao = cemiAwardScheduleExtractDao;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
