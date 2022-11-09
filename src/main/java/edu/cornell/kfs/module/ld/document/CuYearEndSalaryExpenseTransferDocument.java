package edu.cornell.kfs.module.ld.document;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.fp.document.YearEndDocument;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;

/**
 * Labor Document Class for the Year End Salary Expense Transfer Document for Cornell University.
 */
@SuppressWarnings("unchecked")
@NAMESPACE(namespace = KFSConstants.OptionalModuleNamespaces.LABOR_DISTRIBUTION)
@COMPONENT(component = "YearEndSalaryExpenseTransfer")
public class CuYearEndSalaryExpenseTransferDocument extends CuSalaryExpenseTransferDocument implements YearEndDocument {

  private static final long serialVersionUID = 1L;

    /**
     * Class constructor that invokes <code>SalaryExpenseTransferDocument</code> constructor.
     */
    public CuYearEndSalaryExpenseTransferDocument() {
        super();
    }

    @Override
    public Class<? extends AccountingDocument> getDocumentClassForAccountingLineValueAllowedValidation() {
        return CuSalaryExpenseTransferDocument.class;
    }
}
