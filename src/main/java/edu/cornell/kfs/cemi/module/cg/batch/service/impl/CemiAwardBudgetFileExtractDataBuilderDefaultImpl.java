package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetFileBudgetTemplateTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardBudgetFileBudgetTemplateTabRowBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardBudgetFileExtractDataBuilder;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;

public class CemiAwardBudgetFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiAwardBudgetFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    public CemiAwardBudgetFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString) {
        super(businessObjectService, jobRunDateString, CemiAwardBudgetFileBudgetTemplateTabRowBo.class);
    }

    @Override
    public void writeAwardBudgetFileBudgetTemplateTabExtractDataToIntermediateStorage(final Iterator<Award> awards) {
        int awardCounter = 0;

        for (final Award award : IteratorUtils.asIterable(awards)) {
            awardCounter++;
            if (awardCounter % 1000 == 0) {
                LOG.info("writeAwardBudgetFileBudgetTemplateTabExtractDataToIntermediateStorage, Processed {} "
                        + "Awards for Budget Template and counting...", awardCounter);
            }

            final CemiAwardBudgetFileBudgetTemplateTabRowBoFactory budgetTemplateTabRowBoFactory =
                    new CemiAwardBudgetFileBudgetTemplateTabRowBoFactory(award);
            storeSheetRow(budgetTemplateTabRowBoFactory.createCemiAwardBudgetFileBudgetTemplateTabRowBo());
        }
        LOG.info("writeAwardBudgetFileBudgetTemplateTabExtractDataToIntermediateStorage, Finished writing "
                + "{} Budget Template data rows.", awardCounter);
    }

}
