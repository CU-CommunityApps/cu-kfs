package edu.cornell.kfs.cemi.module.cg.batch.factory;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiImportBudgetFileImportBudgetHighVolumeTabRowBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiImportBudgetFileImportBudgetHighVolumeTabRowBoFactory {

    public CemiImportBudgetFileImportBudgetHighVolumeTabRowBo createCemiImportBudgetFileImportBudgetHighVolumeTabRowBo() {
        final CemiImportBudgetFileImportBudgetHighVolumeTabRowBo row =
                new CemiImportBudgetFileImportBudgetHighVolumeTabRowBo();

        // Both of these attributes are set by CemiOrmDataBuilderBase when storeSheetRow is invoked:
        //      attribute jobRunRowIndex
        //      attribute jobRunDateString

        row.setHeaderKey("budget-header-key");
        row.setBudget(CemiBaseConstants.EMPTY_STRING);
        row.setImportMode(CemiBaseConstants.EMPTY_STRING);
        row.setAutoComplete(CemiBaseConstants.EMPTY_STRING);
        row.setComment(CemiBaseConstants.EMPTY_STRING);
        row.setWorker(CemiBaseConstants.EMPTY_STRING);
        row.setSubmit(CemiBaseConstants.EMPTY_STRING);
        row.setBudgetStructure(CemiBaseConstants.EMPTY_STRING);
        row.setBudgetName(CemiBaseConstants.EMPTY_STRING);
        row.setBudgetMemo(CemiBaseConstants.EMPTY_STRING);
        row.setBudgetPeriod(CemiBaseConstants.EMPTY_STRING);

        return row;
    }

}
