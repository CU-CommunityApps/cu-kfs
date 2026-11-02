package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiImportBudgetFileBudgetLinesDataTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiImportBudgetFileImportBudgetHighVolumeTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiImportBudgetFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiImportBudgetExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiImportBudgetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;

public class CemiImportBudgetFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiImportBudgetFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    protected CemiImportBudgetExtractOrmDao cemiImportBudgetExtractOrmDao;
    protected CemiImportBudgetExtractDao cemiImportBudgetExtractDao;
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;

    public CemiImportBudgetFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final CemiImportBudgetExtractOrmDao cemiImportBudgetExtractOrmDao,
            final CemiImportBudgetExtractDao cemiImportBudgetExtractDao, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString,
                CemiImportBudgetFileImportBudgetHighVolumeTabRowBo.class,
                CemiImportBudgetFileBudgetLinesDataTabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(cemiImportBudgetExtractOrmDao, "cemiImportBudgetExtractOrmDao cannot be null");
        Validate.notNull(cemiImportBudgetExtractDao, "cemiImportBudgetExtractDao cannot be null");
        this.dateTimeService = dateTimeService;
        this.cemiImportBudgetExtractOrmDao = cemiImportBudgetExtractOrmDao;
        this.cemiImportBudgetExtractDao = cemiImportBudgetExtractDao;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writeImportBudgetFileImportBudgetHighVolumeTabExtractDataToIntermediateStorage(
            final Iterator<Award> legacyObjects) {
        int rowCount = 0;
        for (final Award legacyObject : IteratorUtils.asIterable(legacyObjects)) {
            rowCount++;
            if (rowCount % 1000 == 0) {
                LOG.info("writeImportBudgetFileImportBudgetHighVolumeTabExtractDataToIntermediateStorage,"
                        + " Processed {} items and counting...", rowCount);
            }
        }
        LOG.info("writeImportBudgetFileImportBudgetHighVolumeTabExtractDataToIntermediateStorage,"
                + " Finished writing {} items", rowCount);
    }

    @Override
    public void writeImportBudgetFileBudgetLinesDataTabExtractDataToIntermediateStorage(
            final Iterator<Award> legacyObjects) {
        int rowCount = 0;
        for (final Award legacyObject : IteratorUtils.asIterable(legacyObjects)) {
            rowCount++;
            if (rowCount % 1000 == 0) {
                LOG.info("writeImportBudgetFileBudgetLinesDataTabExtractDataToIntermediateStorage,"
                        + " Processed {} items and counting...", rowCount);
            }
        }
        LOG.info("writeImportBudgetFileBudgetLinesDataTabExtractDataToIntermediateStorage,"
                + " Finished writing {} items", rowCount);
    }

}
