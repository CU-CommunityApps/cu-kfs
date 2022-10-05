package edu.cornell.kfs.fp.document.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.document.web.struts.DistributionOfIncomeAndExpenseForm;

import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;

public class CuDistributionOfIncomeAndExpenseForm extends DistributionOfIncomeAndExpenseForm{

    public boolean getCanViewTrip() {
        CuDistributionOfIncomeAndExpenseDocument diDocument = (CuDistributionOfIncomeAndExpenseDocument)this.getDocument();
        return diDocument.isLegacyTrip();
    }

    public String getTripID() {
        CuDistributionOfIncomeAndExpenseDocument diDocument = (CuDistributionOfIncomeAndExpenseDocument)this.getDocument();
        return diDocument.isLegacyTrip() ? diDocument.getTripId() : StringUtils.EMPTY;
    }

}