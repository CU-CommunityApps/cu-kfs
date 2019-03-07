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
package edu.cornell.kfs.module.ld.document.validation.impl;

import org.kuali.kfs.fp.service.AccountingDocumentPreRuleService;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.module.ld.document.SalaryExpenseTransferDocument;
import org.kuali.kfs.module.ld.document.validation.impl.SalaryExpenseTransferDocumentPreRules;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.krad.document.Document;

/**
 * Checks warnings and prompt conditions for ST documents.
 */
public class CUSalaryExpenseTransferDocumentPreRules extends SalaryExpenseTransferDocumentPreRules {

    /**
     * Will call methods to examine a ST Document. Includes Error Certification Statement for approval by a fiscal officer if
     * appropriate. Checks for expired accounts, per Cornell customization specs.
     *
     * @see org.kuali.kfs.module.ld.document.validation.impl.SalaryExpenseTransferDocumentPreRules#doPrompts(org.kuali.kfs.kns.document.Document)
     */
    @Override
    public boolean doPrompts(Document document) {
        boolean preRulesOK = super.doPrompts(document);

        preRulesOK &= SpringContext.getBean(AccountingDocumentPreRuleService.class).expiredAccountOverrideQuestion((AccountingDocumentBase) document, (PromptBeforeValidationBase)this);

        return preRulesOK;
    }

}
