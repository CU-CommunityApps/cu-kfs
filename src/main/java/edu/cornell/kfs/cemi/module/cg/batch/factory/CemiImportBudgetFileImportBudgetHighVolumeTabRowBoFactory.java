package edu.cornell.kfs.cemi.module.cg.batch.factory;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiImportBudgetFileImportBudgetHighVolumeTabRowBo;

public class CemiImportBudgetFileImportBudgetHighVolumeTabRowBoFactory {

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
