package edu.cornell.kfs.module.ld.document.authorization;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.ld.document.authorization.SalaryExpenseTransferAccountingLineAuthorizer;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.web.AccountingLineRenderingContext;
import org.kuali.kfs.sys.document.web.AccountingLineViewAction;
import org.kuali.kfs.kim.api.identity.Person;

public class CuSalaryExpenseTransferAccountingLineAuthorizer extends
    SalaryExpenseTransferAccountingLineAuthorizer {
  @Override
  public boolean determineEditPermissionOnField(AccountingDocument accountingDocument, AccountingLine accountingLine,
          String accountingLineCollectionProperty, String fieldName, boolean editablePage) {

    // No matter what, we can't edit the line when the doc is FINAL. Duh.
    if (accountingDocument.isDocumentFinalOrProcessed()) {
      return false;
    }

    // However, that's really the only Cornell customization we want in this...
    return super.determineEditPermissionOnField(accountingDocument, accountingLine, accountingLineCollectionProperty, fieldName, editablePage);
  }

  @Override
  public List<AccountingLineViewAction> getActions(AccountingDocument accountingDocument, AccountingLineRenderingContext accountingLineRenderingContext,
                                                   String accountingLinePropertyName, Integer accountingLineIndex, Person currentUser, String groupTitle) {

      // No matter what, we can't edit the line when the doc is FINAL. Duh.
      if (accountingDocument.isDocumentFinalOrProcessed()) {
        return new ArrayList<AccountingLineViewAction>();
      }

      // However, that's really the only Cornell customization we want in this...
      return super.getActions(accountingDocument, accountingLineRenderingContext, accountingLinePropertyName, accountingLineIndex, currentUser, groupTitle);
  }

}
