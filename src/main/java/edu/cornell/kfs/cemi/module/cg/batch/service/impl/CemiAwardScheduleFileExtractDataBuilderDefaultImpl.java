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
        int awardScheduleTabRowCount = 0;
        
        for (final Award award : IteratorUtils.asIterable(awards)) {
            awardScheduleTabRowCount++;
            if (awardScheduleTabRowCount % 1000 == 0) {
                LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Processed {} "
                        + "Awards for Award Schedule and counting...", awardScheduleTabRowCount);
            }
            //Award Schedule Tab
            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
            //Database table storage of data extract
            createAndStoreAwardScheduleFileAwardScheduleTabRow(award, awardExtendedAttribute, jobRunDateString);
        }
        LOG.info("writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage, Finished writing {} "
                + "Awards for Award Schedule", awardScheduleTabRowCount);
    }
    
    protected void createAndStoreAwardScheduleFileAwardScheduleTabRow(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute, final String jobRunDateString) {

        CemiAwardScheduleFileAwardScheduleTabRowBoFactory factoryForBo = 
                new CemiAwardScheduleFileAwardScheduleTabRowBoFactory(award, awardExtendedAttribute, jobRunDateString,
                        dateTimeService, maskSensitiveData);
        
        CemiAwardScheduleFileAwardScheduleTabRowBo awardScheduleTabRow = factoryForBo.createCemiAwardScheduleFileAwardScheduleTabRowBo();
        storeSheetRow(awardScheduleTabRow);
    }

}
