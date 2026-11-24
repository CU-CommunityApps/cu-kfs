package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiImportBudgetFileImportBudgetHighVolumeTabRowBo;

public class CemiImportBudgetFileImportBudgetHighVolumeTabRowBoFactory {

    private String jobRunDateString;
    private DateTimeService dateTimeService;
    private boolean maskSensitiveData = true;

    public CemiImportBudgetFileImportBudgetHighVolumeTabRowBoFactory(final String jobRunDateString,
            final DateTimeService dateTimeService, final boolean maskSensitiveData) {
        Validate.validState(jobRunDateString != null, "jobRunDateString cannot be null for CemiImportBudgetFileImportBudgetHighVolumeTabRowBoFactory.");
        Validate.validState(dateTimeService != null, "dateTimeService cannot be null for CemiImportBudgetFileImportBudgetHighVolumeTabRowBoFactory.");
        this.jobRunDateString = jobRunDateString;
        this.dateTimeService = dateTimeService;
        this.maskSensitiveData = maskSensitiveData;
    }

    public CemiImportBudgetFileImportBudgetHighVolumeTabRowBo createCemiImportBudgetFileImportBudgetHighVolumeTabRowBo() {
        final CemiImportBudgetFileImportBudgetHighVolumeTabRowBo row =
                new CemiImportBudgetFileImportBudgetHighVolumeTabRowBo();

        row.setHeaderKey("budget-header-key");
        row.setBudget("budget");
        row.setImportMode("import-mode");
        row.setAutoComplete("auto-complete");
        row.setComment("comment");
        row.setWorker("worker");
        row.setSubmit("submit");
        row.setBudgetStructure("budget-structure");
        row.setBudgetName("budget-name");
        row.setBudgetMemo("budget-memo");
        row.setBudgetPeriod("budget-period");

        return row;
    }

}
