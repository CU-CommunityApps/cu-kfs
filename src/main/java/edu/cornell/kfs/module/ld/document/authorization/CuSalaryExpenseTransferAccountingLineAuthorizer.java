/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.module.ld.document.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.ld.document.authorization.SalaryExpenseTransferAccountingLineAuthorizer;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.web.AccountingLineRenderingContext;
import org.kuali.kfs.sys.document.web.AccountingLineViewAction;
import org.kuali.rice.kim.api.identity.Person;

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
