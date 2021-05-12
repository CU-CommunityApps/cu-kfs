package edu.cornell.kfs.fp.document;

import java.util.Iterator;

import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.document.BudgetAdjustmentDocument;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class CuBudgetAdjustmentDocument extends BudgetAdjustmentDocument {

    @Override
    public KualiDecimal getSourceTotal() {
        for (Iterator iter = this.getSourceAccountingLines().iterator(); iter.hasNext();) {
            BudgetAdjustmentAccountingLine line = (BudgetAdjustmentAccountingLine) iter.next();
            line.setAmount(line.getCurrentBudgetAdjustmentAmount());
        }

        return super.getSourceTotal();
    }

    @Override
    public KualiDecimal getTargetTotal() {
        for (Iterator iter = this.getTargetAccountingLines().iterator(); iter.hasNext();) {
            BudgetAdjustmentAccountingLine line = (BudgetAdjustmentAccountingLine) iter.next();
            line.setAmount(line.getCurrentBudgetAdjustmentAmount());
        }
        return super.getTargetTotal();
    }

}
