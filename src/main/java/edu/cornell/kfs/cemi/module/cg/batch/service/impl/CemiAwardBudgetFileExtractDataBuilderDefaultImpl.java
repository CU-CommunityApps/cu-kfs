package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.util.Iterator;

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

    protected final boolean maskSensitiveData;

    public CemiAwardBudgetFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiAwardBudgetFileBudgetTemplateTabRowBo.class);
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writeAwardBudgetFileBudgetTemplateTabExtractDataToIntermediateStorage(final Iterator<Award> awards) {
//TODO FIXME replace shell constant row with per Award row creation in the base data iteration of this extract.
        createAndStoreBudgetTemplateTabShellRow();
        LOG.info("writeAwardBudgetFileBudgetTemplateTabExtractDataToIntermediateStorage, Finished writing "
                + "shell constant row to table.");
    }

    private void createAndStoreBudgetTemplateTabShellRow() {
        final CemiAwardBudgetFileBudgetTemplateTabRowBoFactory budgetTemplateTabRowBoFactory =
                new CemiAwardBudgetFileBudgetTemplateTabRowBoFactory(jobRunDateString, maskSensitiveData);
        storeSheetRow(budgetTemplateTabRowBoFactory.createCemiAwardBudgetFileBudgetTemplateTabRowBo());
    }

}
